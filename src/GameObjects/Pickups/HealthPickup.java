package GameObjects.Pickups;

import Engine.GameObject;
import Engine.Networking.Server;
import Engine.Vector2;

/**
 * A health pickup.
 */
public class HealthPickup extends Pickup {
    
    int givenHealth;

    public HealthPickup(int givenHealth) {
        this.givenHealth = givenHealth;
    }

    @Override
    protected void setup() {
        setSprite("health_pickup");
        scale = new Vector2(0.15f, 0.15f);
        System.out.println("hello");
        // setLayer(-50);
    }

    @Override
    public void onPickUp(GameObject player) {
        Server.sendMessage("health_pickup", player.getOwnerUUID(), givenHealth);
        Server.removeObject(this);
    }
}
