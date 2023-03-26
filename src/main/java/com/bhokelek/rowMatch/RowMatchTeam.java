package com.bhokelek.rowMatch;

import java.util.Arrays;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;

@Entity
public class RowMatchTeam {
    public static final int formationPrice = 1000;
    public static final int maxCapacity = 20;

    @Id
    @SequenceGenerator(name = "team_id_sequence", sequenceName = "team_id_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "team_id_sequence")
    private int id;
    private int capacity;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = false)
    private List<RowMatchUser> members;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    private boolean isFull;

    public boolean isFull() {
        return isFull;
    }

    public void setFull(boolean isFull) {
        this.isFull = isFull;
    }

    public List<RowMatchUser> getMembers() {
        return members;
    }

    public void setMembers(List<RowMatchUser> members) {
        this.members = members;
    }

    public void insertMember(RowMatchUser user) {
        this.members.add(user);
    }

    public void removeMember(RowMatchUser user) {
        this.members.remove(user);
        this.capacity--;
        this.isFull = false;
    }

    public boolean isEmpty() {
        return this.members.size() == 0;
    }

    public RowMatchTeam(int id, int capacity, List<RowMatchUser> members) {
        this.id = id;
        this.capacity = capacity;
        this.members = members;
    }

    public RowMatchTeam() {
    }

    public RowMatchTeam(RowMatchUser user) {
        this.capacity = 1;
        this.members = Arrays.asList(user);
        this.isFull = false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RowMatchTeam other = (RowMatchTeam) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "RowMatchTeam [id=" + id + ", capacity=" + capacity + ", members="
                + members.stream().map(member -> member.getId()).toList() + "]";
    }

}
