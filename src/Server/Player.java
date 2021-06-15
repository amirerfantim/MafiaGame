package Server;

/**
 * The type Player.
 */
public abstract class Player {
    //private String username;
    private boolean isAlive = true;

    /**
     * Instantiates a new Player.
     */
    public Player() {
    }

    /*
    public Player(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

 */

    /**
     * Is alive boolean.
     *
     * @return the boolean
     */
    public boolean isAlive() {
        return isAlive;
    }


    /*
    public void setUsername(String username) {
        this.username = username;
    }

 */

    /**
     * Sets alive.
     *
     * @param alive the alive
     */
    public void setAlive(boolean alive) {
        isAlive = alive;
    }

}
