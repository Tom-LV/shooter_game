package GameObjects.Loot;

import Engine.GameObject;
import Engine.Networking.Server;
import Engine.Vector2;
import GameObjects.GameManagment.ServerManager;
import GameObjects.Pickups.PickupManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LootManager extends GameObject {
    float timer = 0f;
    Random rng;

    public LootManager() {
        rng =  new Random();
    }

    @Override
    public void setup() {
        ServerManager.addOnRoundFinishedListener(this::onRoundFinished);
    }

    void onRoundFinished() {
        List<GameObject> gameObjects = Server.getServerObjectsOfClass(AmmoLoot.class);
        for (GameObject gameObject : gameObjects) {
            Server.removeObject(gameObject);
        }
    }

    @Override
    public void update(float deltaTime) {
        timer += deltaTime;
        if (ServerManager.isRoundStarted() && timer >= 1f) {
            float x = rng.nextFloat(-1600.0f, 1600.0f);
            float y = rng.nextFloat(-1600.0f, 1600.0f);
            if (rng.nextFloat() >= 0.75f) {
                PickupManager.createPickup(new Vector2(x, y), "health_pickup");
            } else {
                Server.addObject(new AmmoLoot(new Vector2(x, y)));
            }
            timer = 0;
        }

    }

    @Override
    public void onDestroy() {
        ServerManager.removeOnRoundFinishedListener(this::onRoundFinished);
    }
}
