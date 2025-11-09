package GameObjects.Pickups;

import Engine.GameObject;
import Engine.Networking.Server;
import Engine.Vector2;
import GameObjects.WeaponManager;

import java.util.Random;

public class BoltPickup extends Pickup {

    @Override
    public void onPickUp(GameObject player) {
        Server.sendMessage("bolt_spawn", player.getOwnerUUID(), position);
        Server.removeObject(this);
    }

    @Override
    public void setup() {
        pickupDistance = 100f;
        Random rng = new Random();
        setSprite("bolt");
        scale = new Vector2(0.1f, 0.1f);
        setLayer(-50);
        rotation = rng.nextInt(360);
    }
}
