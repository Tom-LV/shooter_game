package GameObjects.Enemies;

import Engine.GameObject;
import Engine.Networking.Server;
import Engine.Vector2;
import GameObjects.GameManagment.ServerManager;

import java.util.List;
import java.util.Random;

/**
 * An enemy manager that spawns enemies randomly on the map.
 */
public class EnemyManager extends GameObject {
    private Random rng;

    private final Runnable onRoundFinished = this::onRoundFinished;

    @Override
    public void setup() {
        rng = new Random();
        for (int i = 0; i < 10; i++) {
            createSpawner();
        }
        ServerManager.addOnRoundFinishedListener(onRoundFinished);
    }

    void onRoundFinished() {
        List<GameObject> gameObjects = Server.getServerObjectsOfClass(EnemySpawner.class);
        System.out.println(gameObjects.size());
        for (int i = 0; i < 10 - gameObjects.size(); i++) {
            createSpawner();
        }
    }

    private void createSpawner() {
        float x = rng.nextFloat(-2000, 2000);
        float y = rng.nextFloat(-2000, 2000);
        if (x > -200 && x < 800 && y < -200 && y > -1000) {
            return;
        }
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

    @Override
    public void onDestroy() {
        ServerManager.removeOnRoundFinishedListener(onRoundFinished);
    }
}
