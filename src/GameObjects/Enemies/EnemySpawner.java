package GameObjects.Enemies;

import Engine.GameObject;
import Engine.Networking.Server;
import Engine.Physics.CircleCollider;
import Engine.Physics.ColliderType;
import Engine.Vector2;
import GameObjects.GameManagment.ServerManager;
import Interfaces.Damagable;

import java.util.List;
import java.util.Random;

public class EnemySpawner extends GameObject implements Damagable {

    private float time = 0;
    private Random rng;
    private int health = 500;
    float hitAnim = 0f;

    public EnemySpawner(Vector2 position) {
        this.position = position;
    }

    @Override
    public void setup() {
        rng = new Random();
        setSprite("descrom_nest");
        scale = new Vector2(0.3f, 0.3f);
        setLayer(-99);
        addCollider(new CircleCollider(35, ColliderType.Static));
        ServerManager.addOnRoundStartedListener(this::onRoundStarted);
    }

    void onRoundStarted() {
        for (int i = 0; i < 5; i++) {
            spawnEnemy();
        }
    }

    @Override
    public void update(float deltaTime) {
        time += deltaTime;

        if (ServerManager.isRoundStarted() && time >= rng.nextFloat(5.0f, 10.0f)) {
            for (int i = 0; i < 1; i++) {
                //spawnEnemy();
            }
        }

        if (hitAnim > 0) {
            hitAnim -= deltaTime;
        } else {
            scale = new Vector2(0.3f, 0.3f);
        }
    }

    private void spawnEnemy() {
        List<GameObject> enemies = Server.getServerObjectsOfClass(Enemy.class);

        if (enemies.size() > 100) {
            return;
        }

        float x = position.x + rng.nextFloat(-10, 10);
        float y = position.y + rng.nextFloat(-10, 10);
        x = x/400*400;
        y = y/400*400;
        Enemy enemy;
        if (rng.nextFloat() < 0.1f) {
            enemy = new SentryEnemy(new Vector2(x, y));
        } else {
            enemy = new BasicEnemy(new Vector2(x, y));
        }

        Server.addObject(enemy);
        time = 0;
    }

    @Override
    public void takeDamage(int amount) {
        if (isDead()) {
            return;
        }
        health -= amount;
        if (health <= 0) {
            onKill();
        } else {
            onDamage(amount);
        }
    }

    @Override
    public void onDamage(int amount) {
        hitAnim = 0.1f;
        scale = new Vector2(0.28f, 0.28f);
    }

    @Override
    public void onKill() {
        ServerManager.removeOnRoundStartedListener(this::onRoundStarted);
        Server.removeObject(this);
    }

    @Override
    public boolean isDead() {
        return health <= 0;
    }
}
