package GameObjects;

import Engine.GameObject;
import Engine.Vector2;
import GameObjects.Pickups.PickupManager;

import java.util.Random;

public class LootManager extends GameObject {
    float timer = 0f;
    Random rng;

    public LootManager() {
        rng =  new Random();
    }

    @Override
    public void update(float deltaTime) {
        timer += deltaTime;
        if (timer >= 1f) {
            float x = rng.nextFloat(-1600.0f, 1600.0f);
            float y = rng.nextFloat(-1600.0f, 1600.0f);
            if (rng.nextFloat() >= 0.75f) {
                PickupManager.createPickup(new Vector2(x, y), "health_pickup");
            } else {
                PickupManager.createPickup(new Vector2(x, y), "ammo_pickup");
            }
            timer = 0;
        }

    }
}
