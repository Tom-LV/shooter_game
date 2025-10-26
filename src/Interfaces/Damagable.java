package Interfaces;

public interface Damagable {

    /**
     * Apply incoming damage to this object.
     * Implementations should modify HP and call onDamage or onKill when needed.
     *
     * @param amount the amount of damage taken
     */
    void takeDamage(int amount);

    /**
     * Called whenever damage is applied, but the target is still alive.
     *
     * @param amount the amount of damage taken
     */
    void onDamage(int amount);

    /**
     * Called when HP reaches 0 or below.
     */
    void onKill();
}
