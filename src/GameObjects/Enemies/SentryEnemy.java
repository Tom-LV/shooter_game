package GameObjects.Enemies;

import Engine.GameObject;
import Engine.Networking.Server;
import Engine.Physics.CircleCollider;
import Engine.Physics.ColliderType;
import Engine.Vector2;

import java.util.Random;

public class SentryEnemy extends Enemy {

    public SentryEnemy(Vector2 position) {
        super(position);
    }

    @Override
    public void setup() {
        health = 80;
        speed = 75f;
        attackRange = 200f;
        attackTime = 2f;
        rng = new Random();
        setSprite("descrom_sentry");
        rotation = rng.nextInt(360);
        scale = new Vector2(0.15f, 0.15f);
        addCollider(new CircleCollider(13, ColliderType.Dynamic));
        if (rng.nextFloat() >= 0.5) {
            followRandomPlayer();
        }
    }

    @Override
    void attack(GameObject player) {
        EnemyBullet enemyBullet = new EnemyBullet(position, rotation, 15);
        Server.addObject(enemyBullet);
    }
}
