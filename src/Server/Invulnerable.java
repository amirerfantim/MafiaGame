package Server;

/**
 * The type Invulnerable.
 */
public class Invulnerable extends CitizenTeam{

    private boolean hasBeenShot = false;

    /**
     * Is has been shot boolean.
     *
     * @return the boolean
     */
    public boolean isHasBeenShot() {
        return hasBeenShot;
    }

    /**
     * Sets has been shot.
     *
     * @param hasBeenShot the has been shot
     */
    public void setHasBeenShot(boolean hasBeenShot) {
        this.hasBeenShot = hasBeenShot;
    }

    @Override
    public String toString() {
        return "Invulnerable";
    }
}
