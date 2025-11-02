package GameObjects.Enemies;

import Engine.GameObject;
import Engine.Networking.Server;
import Engine.Vector2;

import java.util.Random;

/**
 * An enemy manager that spawns enemies randomly on the map.
 */
public class EnemyManager extends GameObject {
    private Random rng;

    @Override
    public void setup() {
        rng = new Random();
        for (int i = 0; i < 10; i++) {
            //createSpawner();
        }
    }

    private void createSpawner() {
        float x = rng.nextFloat(-1200, 1200);
        float y = rng.nextFloat(-800, 800);
        EnemySpawner enemySpawner = new EnemySpawner(new Vector2(x, y));
        Server.addObject(enemySpawner);
    }
}
