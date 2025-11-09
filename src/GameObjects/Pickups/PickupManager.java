package GameObjects.Pickups;

import Engine.Networking.Server;
import Engine.Vector2;

/**
 * A pickup manager that spawns pickups.
 */
public class PickupManager {


    /**
     * Creates a pickup on the ground.
     * @param position pickup position
     * @param type pickup type
     */
    public static void createPickup(Vector2 position, String type) {
        switch (type) {
            case "health_pickup":
                HealthPickup healthPickup = new HealthPickup(20);
                healthPickup.position = position;
                Server.addObject(healthPickup);
                break;
            case "weapon_pickup":
                WeaponPickup weaponPickup = new WeaponPickup(position, 0);
                Server.addObject(weaponPickup);
                break;
            case "ammo_pickup":
                AmmoPickup ammoPickup = new AmmoPickup();
                ammoPickup.position = position;
                Server.addObject(ammoPickup);
                break;
            case "bolt":
                BoltPickup boltPickup = new BoltPickup();
                boltPickup.position = position;
                Server.addObject(boltPickup);
                break;
            default:
                break;
        }
    }
}