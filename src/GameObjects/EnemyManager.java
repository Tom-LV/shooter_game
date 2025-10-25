package GameObjects;

import Engine.GameObject;
import Engine.Networking.Server;
import Engine.Vector2;
import java.util.ArrayList;
import java.util.Random;

/**
 * An enemy manager that spawns enemies randomly on the map.
 */
public class EnemyManager extends GameObject {

    private float time = 0;
    private Random rng;

    @Override
    protected void setup() {
        rng = new Random();
    }

    @Override
    public void update(float deltaTime) {
        time += deltaTime;

        if (time >= rng.nextFloat(2.0f, 4.0f)) {
            spawnEnemy();
        }
    }

    private void spawnEnemy() {
        ArrayList<GameObject> enemies = Server.getServerObjectOfClass(Enemy.class);

        if (enemies.size() > 20) {
            return;
        }

        ArrayList<GameObject> playerObjects = Server.getClientObjectOfClass(Player.class);

        if (playerObjects.size() <= 0) {
            return;
        }

        GameObject player = playerObjects.get(rng.nextInt(0, playerObjects.size()));
        float x = player.position.x + rng.nextFloat(-300, 300);
        float y = player.position.y + rng.nextFloat(-300, 300);
        Enemy enemy = new Enemy(new Vector2(x, y));
        Server.addObject(enemy);
        time = 0;
    }
}
