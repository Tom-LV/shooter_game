package GameObjects.Pickups;

import Engine.GameObject;
import Engine.Networking.NetEvent;
import Engine.Networking.Server;
import Engine.Vector2;
import GameObjects.Enemy;

import java.util.List;

public class WeaponPickup extends Pickup {

    float speed = 400f;
    float thrownTimer = 0f;
    Vector2 velocity;


    @NetEvent("throw_weapon")
    public static void weaponThrow(Vector2 position, float rotation) {
        Server.addObject(new WeaponPickup(position, rotation));
    }

    @Override
    public void onPickUp(GameObject player) {
        Server.sendMessage("weapon_pickup", player.getOwnerUUID());
        Server.removeObject(this);
    }

    @Override
    protected void setup() {
        setSprite("pistol");
        scale = new Vector2(0.05f, 0.05f);
        setLayer(-10);
        despawnTime = -1;
    }

    WeaponPickup(Vector2 position, float rotation) {
        this.rotation = rotation;
        float rotationInRad = (float) Math.toRadians(rotation);
        this.position = position.add(Vector2.fromRotation(rotationInRad).multiply(35));
        velocity = Vector2.fromRotation(rotationInRad).normalize();
        despawnTime = -1;
    }

    public WeaponPickup(Vector2 position) {
        this.position = position;
        thrownTimer = 3f;
        speed = 0;
        velocity = new Vector2(0f, 0f);
        despawnTime = -1;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        thrownTimer += deltaTime;

        if (thrownTimer >= 0.5f) {

        } else {
            position = position.add(velocity.multiply(speed * deltaTime));
            speed *= 0.9999f;
            rotation += 600 * deltaTime;
            collision();
        }

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

    private void collision() {
        List<GameObject> enemies = Server.getServerObjectsOfClass(Enemy.class);
        for (GameObject enemy : enemies) {
            Vector2 distance = enemy.position.subtract(position);
            if (distance.length() < 15f) {
                Enemy e = (Enemy) enemy;
                e.hit(10);
                position = position.subtract(distance.normalize().multiply(30 - distance.length()));
                float dot = velocity.dot(distance.normalize());
                velocity = velocity.subtract(distance.normalize().multiply(2 * dot));
            }
        }
    }
}
