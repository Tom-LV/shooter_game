package GameObjects.Enemies;

import Engine.GameObject;
import Engine.Networking.Server;
import Engine.Vector2;

import java.util.List;
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
            createSpawner();
        }
    }

    private void createSpawner() {
        float x = rng.nextFloat(-2000, 2000);
        float y = rng.nextFloat(-2000, 2000);
        x = Math.round(x / 400) * 400;
        y = Math.round(y / 400) * 400;
        EnemySpawner enemySpawner = new EnemySpawner(new Vector2(x, y));
        Server.addObject(enemySpawner);
    }

    public static void destroyAllEnemies() {
        List<GameObject> enemies = Server.getServerObjectsOfClass(Enemy.class);
        for (GameObject object : enemies) {
            Server.removeObject(object);
        }
    }
}
