package GameObjects.Loot;

import Engine.GameObject;
import Engine.Networking.Server;
import Engine.Vector2;
import GameObjects.WeaponManager;

public class AmmoLoot extends Loot {

    public AmmoLoot(Vector2 pos) {
        this.position = pos;
        this.interactRange = 30f;
        this.interactTimer = 4f;
        this.offset = new Vector2(0, -5);
    }

    @Override
    boolean canInteract(GameObject player) {
        return !WeaponManager.hasWeapon(player);
    }

    @Override
    void onInteract(GameObject player) {
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
