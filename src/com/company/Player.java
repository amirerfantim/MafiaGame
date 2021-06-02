package com.company;

public abstract class Player {
    private String username;
    private boolean isAlive = true;
    private boolean isMafia ;

    public Player(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isMafia() {
        return isMafia;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public void setMafia(boolean mafia) {
        isMafia = mafia;
    }

    public abstract void role();

}
