package GameObjects.Enemies;

import Engine.GameObject;
import Engine.Networking.Server;
import Engine.Physics.CircleCollider;
import Engine.Physics.ColliderType;
import Engine.Vector2;
import GameObjects.Pickups.PickupManager;
import GameObjects.Player;
import Interfaces.Damagable;

import java.util.List;
import java.util.Random;

/**
 * A basic enemy class.
 */
public class Enemy extends GameObject implements Damagable {
    double time = 0;
    float speed = 150f;
    Vector2 velocity = new Vector2(0f, 0f);
    int health = 20;
    float hitAnim = 0f;
    float attackTimer = 0f;
    Random rng;
    GameObject followPlayer;

    public Enemy(Vector2 position) {
        this.position = position;
    }

    @Override
    public void setup() {
        rng = new Random();
        setSprite("zombie");
        rotation = rng.nextInt(360);
        scale = new Vector2(0.15f, 0.15f);
        addCollider(new CircleCollider(13, ColliderType.Dynamic));
        if (rng.nextFloat() >= 0.5) {
            followRandomPlayer();
        }
    }

    public void followRandomPlayer() {
        List<GameObject> playerObjects = Server.getClientObjectsOfClass(Player.class);
        followPlayer = playerObjects.get(rng.nextInt(playerObjects.size()));
    }

    @Override
    public void update(float deltaTime) {
        time += deltaTime;
        attackTimer += deltaTime;

        if (hitAnim > 0) {
            hitAnim -= deltaTime;
        } else {
            scale = new Vector2(0.15f, 0.15f);
            setSprite("zombie");
        }

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

        if (closestDistance <= 300f) {
            followPlayer = closestPlayer;
        } else {
            rotation += rng.nextFloat(-400.0f, 450.0f) * deltaTime;
            velocity = velocity.add(new Vector2(speed * 0.25f, 0f).rotate(rotation));
        }

        goToPlayer(deltaTime, followPlayer);
        position = position.add(velocity.multiply(deltaTime));
        velocity = new Vector2(0, 0);
    }

    private void goToPlayer(float deltaTime, GameObject player) {
        if (player == null) {
            return;
        }
        float distance = player.position.subtract(position).length();
        rotation = player.position.subtract(position).getRotation();

        if (distance < 25f) {
            if (attackTimer > 0.5f) {
                Server.sendMessage("player_hit", player.getOwnerUUID(), 10);
                attackTimer = 0;
            }
        } else {

            velocity = velocity.add(new Vector2(speed, 0f).rotate(rotation));
        }
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
        hitAnim = 0.05f;
        scale = new Vector2(0.14f, 0.14f);
        setSprite("zombie_hit");
    }

    @Override
    public void onKill() {
        Server.removeObject(this);
    }

    @Override
    public boolean isDead() {
        return health <= 0;
    }

    @Override
    public void onDestroy() {
        PickupManager.createPickup(position, "bolt");
    }
}
