package GameObjects;

import Engine.GameObject;
import Engine.Networking.Server;
import Engine.Vector2;
import GameObjects.Pickups.PickupManager;
import java.util.ArrayList;
import java.util.Random;

/**
 * A basic enemy class.
 */
public class Enemy extends GameObject {
    double time = 0;
    float speed = 75f;
    Vector2 velocity = new Vector2(0f, 0f);
    int health = 50;
    float hitAnim = 0f;
    float attackTimer = 0f;
    Random rng;

    public Enemy(Vector2 position) {
        this.position = position;
    }

    @Override
    protected void setup() {
        rng = new Random();
        setSprite("zombie");
        //setRotation(45);
        scale = new Vector2(0.15f, 0.15f);
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

        ArrayList<GameObject> playerObjects = Server.getClientObjectOfClass(Player.class);

        float closestDistance = Float.MAX_VALUE;
        GameObject closestPlayer = null;
        for (GameObject player : playerObjects) {
            float distance = player.position.subtract(position).length();
            if (distance < closestDistance) {
                closestDistance = distance;
                closestPlayer = player;
            }
        }

        goToClosestPlayer(deltaTime, closestPlayer, closestDistance);



        position = position.add(velocity.multiply(deltaTime));
        velocity = new Vector2(0, 0);

        if (position.y < -910) {
            position.y = -910;
        }
        if (position.x < -1300) {
            position.x = -1300;
        }
        if (position.y > 920) {
            position.y = 920;
        }
        if (position.x > 1350) {
            position.x = 1350;
        }
    }

    private void goToClosestPlayer(float deltaTime, GameObject player, float distance) {
        if (player == null) {
            return;
        }

        rotation = player.position.subtract(position).getRotation();

        if (distance < 25f) {
            if (attackTimer > 0.5f) {
                sendMessage("player_hit", player.getOwnerUUID(), 10);
                attackTimer = 0;
            }
        } else {
            
            velocity = velocity.add(new Vector2(speed, 0f).rotate(rotation));
        }
    } 

    /**
     * Called when this enemy takes damage.
     * @param damage ddamage to take
     */
    public void hit(int damage) {
        health -= damage;
        hitAnim = 0.05f;
        scale = new Vector2(0.14f, 0.14f);
        setSprite("zombie_hit");
        if (health <= 0) {
            Server.removeObject(this);
        }
    }

    @Override
    public void onDestroy() {
        if (rng.nextFloat() >= 0.5f) {
            PickupManager.createPickup(position, "health_pickup");
        }
    }
}
