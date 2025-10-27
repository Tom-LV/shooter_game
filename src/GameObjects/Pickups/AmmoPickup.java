package GameObjects.Pickups;

import Engine.GameObject;
import Engine.Networking.Server;
import Engine.Vector2;
import GameObjects.WeaponManager;

public class AmmoPickup extends Pickup {
    @Override
    public void onPickUp(GameObject player) {
        if (WeaponManager.hasWeapon(player)) {
            return;
        }
        Server.sendMessage("ammo_pickup", player.getOwnerUUID(), 10);
        Server.removeObject(this);
    }

    @Override
    protected void setup() {
        setSprite("bullet");
        scale = new Vector2(0.1f, 0.1f);
        setLayer(-50);
    }
}
