package com.bhokelek.rowMatch;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

@Entity
public class RowMatchUser {
    private static final int starterCoins = 5000;
    private static final int starterLevel = 1;

    @Id
    @SequenceGenerator(name = "user_id_sequence", sequenceName = "user_id_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_sequence")
    private Integer id;

    public Integer getId() {
        return id;
    }

    private Integer coins;

    public Integer getCoins() {
        return coins;
    }

    public void setCoins(Integer coins) {
        this.coins = coins;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private Integer level;

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    private boolean inATeam;
    
    public boolean isInATeam() {
        return inATeam;
    }

    public void setInATeam(boolean inATeam) {
        this.inATeam = inATeam;
    }

    public RowMatchUser(Integer id, Integer coins, String name, Integer level) {
        this.id = id;
        this.coins = coins;
        this.name = name;
        this.level = level;
    }

    public RowMatchUser() {
        this.coins = starterCoins;
        this.level = starterLevel;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        RowMatchUser other = (RowMatchUser) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "RowMatchUser [id=" + id + ", coins=" + coins + ", name=" + name + ", level=" + level + ", inATeam="
                + inATeam + "]";
    }
}
