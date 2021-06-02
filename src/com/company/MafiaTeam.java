package com.company;

public abstract class MafiaTeam extends Player{
    public MafiaTeam(String username) {
        super(username);
        setMafia(true);
    }
}
