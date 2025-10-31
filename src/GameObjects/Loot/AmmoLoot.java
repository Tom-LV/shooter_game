package GameObjects.Loot;

import Engine.GameObject;
import Engine.Vector2;

public class AmmoLoot extends Loot {

    public AmmoLoot(Vector2 pos) {
        this.position = pos;
        this.interactRange = 30f;
        this.interactTimer = 5f;
        this.offset = new Vector2(0, -5);
    }

    @Override
    boolean canInteract(GameObject player) {
        return true;
    }

    @Override
    void onInteract() {

    }

    @Override
    public void setup() {
        setSprite("ammo_crate");
        scale = new Vector2(0.1f, 0.1f);
        setLayer(-50);
    }
}
