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
        Server.sendMessage("ammo_pickup", player.getOwnerUUID(), 50);
        Server.removeObject(this);
    }

    @Override
    public void setup() {
        setSprite("ammo_crate");
        scale = new Vector2(0.1f, 0.1f);
        setLayer(-50);
    }
}
