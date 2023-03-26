package com.bhokelek.rowMatch;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class RowMatchApplication {
	// NOTE: You will need to run "CREATE DATABASE rowmatch" on Docker terminal
	// first.

	// maximum number of teams in a given sample
	private static final int sampleSize = 20;

	public static int getSamplesize() {
		return sampleSize;
	}

	private final UserRepository userRepository;
	private final TeamRepository teamRepository;

	public RowMatchApplication(UserRepository userRepository, TeamRepository teamRepository) {
		this.userRepository = userRepository;
		this.teamRepository = teamRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(RowMatchApplication.class, args);
	}

	// User methods

	/**
	 * Returns a list of all Users.
	 * 
	 * @return List<RowMatchUser>
	 */

	@GetMapping("/users/list")
	public List<RowMatchUser> getUsers() {
		return userRepository.findAll();
	}

	record NewUserRequest(String name) {
	}

	/**
	 * Adds a new user to the database.
	 * 
	 * @param request: the name of the new user
	 * @return
	 */
	@PostMapping("/users/new")
	public String addNewUser(@RequestBody NewUserRequest request) {
		RowMatchUser user = new RowMatchUser();
		user.setName(request.name);
		try {
			userRepository.save(user);
			return "New User Addition: Success";
		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}

	/**
	 * Finds the user with the given ID, finds the team with the given ID (if
	 * exists), and adds the user to the team.
	 * If no such team exists, or if it's full, a new team is generated and the user
	 * is added to that newly-generated team.
	 * 
	 * @param userId ID of the user
	 * @param teamId ID of the team
	 */
	@PostMapping(value = { "/users/{userId}/assign", "/users/{userId}/assign/{teamId}" })
	public void assignUserToTeam(@PathVariable(value = "userId") Integer userId,
			@PathVariable(value = "teamId", required = false) Integer teamId) {
		try {
			Optional<RowMatchUser> userOptional = userRepository.findById(userId);
			RowMatchUser user = userOptional.get();
			if (teamId == null) {
				// no team id is given, new team
				createNewTeam(user);
			} else {
				Optional<RowMatchTeam> teamOptional = teamRepository.findById(teamId);
				if (!teamOptional.isPresent()) {
					// the requested team doesn't exist, new team
					createNewTeam(user);
				} else {
					// assign to team
					RowMatchTeam team = teamOptional.get();
					if (!team.isFull()) {
						if (user.isInATeam()) {
							// remove from original team
							leaveTeam(userId, null);
						}
						team.setCapacity(team.getCapacity() + 1);
						team.insertMember(user);
						if (team.getCapacity() == RowMatchTeam.maxCapacity) {
							team.setFull(true);
						}
						user.setInATeam(true);
						userRepository.save(user);
						teamRepository.save(team);
					} else {
						// team is full, can't add
						throw new IllegalArgumentException(); // fix
					}
				}
			}
		} catch (Exception e) {
			// user doesn't exist, can't add
		}
	}

	/**
	 * Finds the user with the given ID, finds the team with the given ID (if
	 * exists), and removes the user from the team.
	 * If no team ID is given, the method manually tries to find the team.
	 * 
	 * @param userId ID of the user
	 * @param teamId ID of the team
	 */
	@PostMapping(value = { "users/{userId}/leave", "users/{userId}/leave/{teamId}" })
	public void leaveTeam(@PathVariable(value = "userId") Integer userId,
			@PathVariable(value = "teamId", required = false) Integer teamId) {
		try {
			Optional<RowMatchUser> userOptional = userRepository.findById(userId);
			RowMatchUser user = userOptional.get();
			RowMatchTeam ogTeam;
			if (teamId == null) {
				// try to find team yourself
				ogTeam = teamRepository.findAll().stream().filter(t -> t.getMembers()
						.stream().map(member -> member.getId()).toList().contains(userId)).toList().get(0);

			} else {
				// leave from the given team
				ogTeam = teamRepository.findById(teamId).get();
				if (!ogTeam.getMembers().stream().map(member -> member.getId()).collect(Collectors.toList())
						.contains(userId)) {
					throw new IllegalArgumentException();
				}
			}
			ogTeam.removeMember(user);
			user.setInATeam(false);
			if (ogTeam.isEmpty()) {
				// delete team
				teamRepository.delete(ogTeam);
			} else {
				teamRepository.save(ogTeam);
			}
			userRepository.save(user);
		} catch (Exception e) {
			// either user or team doesn't exist, can't remove
		}
	}

	/**
	 * Creates a new team and adds the given user to it.
	 * 
	 * @param user
	 */
	public void createNewTeam(RowMatchUser user) {
		if (user.getCoins() >= RowMatchTeam.formationPrice) {
			RowMatchTeam team = new RowMatchTeam();
			team.setCapacity(1);
			user.setCoins(user.getCoins() - RowMatchTeam.formationPrice);
			user.setInATeam(true);
			team.setMembers(Arrays.asList(user));
			teamRepository.save(team);
			userRepository.save(user);
		} else {
			// user doesn't have enough money, can't create team
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Updates the level of a given user.
	 * 
	 * @param userId
	 * @return
	 */
	@PostMapping("/users/{userId}/update-level")
	public String updateLevel(@PathVariable Integer userId) {
		Optional<RowMatchUser> userOptional = userRepository.findById(userId);
		try {
			RowMatchUser user = userOptional.get();
			user.setLevel(user.getLevel() + 1);
			user.setCoins(user.getCoins() + 25);
			userRepository.save(user);
			return "Level Update: Success";
		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}

	// Team methods

	/**
	 * Returns a list of all teams
	 * 
	 * @return
	 */
	@GetMapping("/teams")
	public List<RowMatchTeam> getTeams() {
		return teamRepository.findAll();
	}

	/**
	 * Returns the team with the given ID
	 * 
	 * @param teamId
	 * @return
	 */
	@GetMapping("/teams/{teamId}")
	public RowMatchTeam getTeamById(@PathVariable Integer teamId) {
		RowMatchTeam team = teamRepository.findById(teamId).get();
		return team;
	}

	/**
	 * Returns a sample of 20 teams that are not full
	 * 
	 * @return
	 */
	@GetMapping("/teams/sample")
	List<RowMatchTeam> sample() {
		List<RowMatchTeam> availableTeams = teamRepository.findAll().stream().filter(team -> !team.isFull()).toList();
		if (availableTeams.size() <= getSamplesize()) {
			return availableTeams;
		}
		List<Integer> randomIndices = IntStream.range(0, availableTeams.size()).boxed().collect(Collectors.toList());
		Collections.shuffle(randomIndices);
		randomIndices = randomIndices.subList(0, getSamplesize());
		List<RowMatchTeam> sampleTeams = randomIndices
				.stream()
				.map(randomIndex -> availableTeams.get(randomIndex))
				.toList();
		return sampleTeams;
	}
}
