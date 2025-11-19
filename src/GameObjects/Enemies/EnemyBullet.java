package GameObjects.Enemies;

import Engine.GameObject;
import Engine.Networking.NetEvent;
import Engine.Networking.Server;
import Engine.Physics.CircleCollider;
import Engine.Physics.ColliderType;
import Engine.Physics.CollisionEvent;
import Engine.Vector2;
import GameObjects.Bullet;
import GameObjects.Player;
import Interfaces.Damagable;

import java.util.List;
import java.util.Random;

public class EnemyBullet extends GameObject {
    float time = 0f;
    float speed = 800f;
    int damage;
    Vector2 velocity = new Vector2(0f, 0f);
    int pierce = 1;

    /**
     * Creates a bullet.
     * @param position bullet position
     * @param rotation bullet rotation
     * @param damage bullet damage
     */
    public EnemyBullet(Vector2 position, float rotation, int damage) {
        this.position = position;
        this.rotation = rotation;
        this.damage = damage;
    }

    @Override
    public void setup() {
        setSprite("bullet");
        scale = new Vector2(0.1f, 0.1f);
        addCollider(new CircleCollider(3, ColliderType.None));
    }

    @Override
    public void update(float deltaTime) {
        time += deltaTime;
        float rotationInRad = (float) Math.toRadians(rotation);
        position = position.add(Vector2.fromRotation(rotationInRad).multiply(speed * deltaTime));
        collision();
        if (time >= 2) {
            Server.removeObject(this);
        }
    }

    void collision() {
        List<GameObject> playerObjects = Server.getClientObjectsOfClass(Player.class);

        float closestDistance = Float.MAX_VALUE;
        GameObject closestPlayer = null;
        for (GameObject player : playerObjects) {
            float distance = player.position.subtract(position).length();
            if (distance < closestDistance) {
                closestDistance = distance;
                closestPlayer = player;
            }
        }

        if (closestDistance <= 3f && pierce > 0) {
            Server.sendMessage("player_hit", closestPlayer.getOwnerUUID(), 15);
            Server.removeObject(this);
            pierce--;
        }
    }

    @Override
    public void onCollisionEnter(CollisionEvent e) {
        if (e.getOther().getType() == ColliderType.Static) {
            Server.removeObject(this);
        }
    }
}
