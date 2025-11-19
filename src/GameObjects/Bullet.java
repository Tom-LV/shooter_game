package GameObjects;

import Engine.GameObject;
import Engine.Networking.NetEvent;
import Engine.Networking.Server;
import Engine.Physics.CircleCollider;
import Engine.Physics.Collider;
import Engine.Physics.ColliderType;
import Engine.Physics.CollisionEvent;
import Engine.Vector2;
import Interfaces.Damagable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A basic bullet class.
 */
public class Bullet extends GameObject {
    float time = 0f;
    float speed = 800f;
    int damage;
    Vector2 velocity = new Vector2(0f, 0f);
    int pierce = 1;

    /**
     * An event that is fired when player shoots a pistol.
     * @param position player position
     * @param rotation player rotation
     */
    @NetEvent("shoot_pistol")
    public static void shootPistol(Vector2 position, float rotation) {
        Vector2 offset = new Vector2(50f, 7f).rotate(rotation);
        Vector2 bulletPosition = position.add(offset);
        Bullet bullet = new Bullet(bulletPosition, rotation, 20);
        bullet.setSprite("bullet");
        Server.addObject(bullet);
    }

    /**
     * An event that is fired when player shoots a shotgun.
     * @param position player position
     * @param rotation player rotation
     */
    @NetEvent("shoot_shotgun")
    public static void shootShotgun(Vector2 position, float rotation) {
        Vector2 offset = new Vector2(45f, -6f).rotate(rotation);
        Vector2 bulletPosition = position.add(offset);
        Random rng = new Random();
        
        for (int i = 0; i < 8; i++) {
            float randomRotation = rotation + rng.nextFloat(-25.0f, 25.0f);
            Bullet bullet = new Bullet(bulletPosition, randomRotation, 10);
            bullet.setSprite("bullet");
            Server.addObject(bullet);
        }
    }

    /**
     * An event that is fired when player shoots a rifle.
     * @param position player position
     * @param rotation player rotation
     */
    @NetEvent("shoot_minigun")
    public static void shootMinigun(Vector2 position, float rotation) {
        Random rng = new Random();
        Vector2 offset = new Vector2(50f, -6f).rotate(rotation);
        Vector2 bulletPosition = position.add(offset);
        Bullet bullet = new Bullet(bulletPosition, rotation + rng.nextFloat(-5.0f, 5.0f), 15);
        bullet.setSprite("bullet");
        Server.addObject(bullet);
    }

    @NetEvent("shoot_uzirang")
    public static void shootUzirang(Vector2 position, float rotation) {
        Random rng = new Random();
        Vector2 offset = new Vector2(55f, 5f).rotate(rotation);
        Vector2 bulletPosition = position.add(offset);
        Bullet bullet = new Bullet(bulletPosition, rotation + rng.nextFloat(-15.0f, 15.0f), 5);
        bullet.setSprite("uzirang_bullet");
        Server.addObject(bullet);
    }

    /**
     * Creates a bullet.
     * @param position bullet position
     * @param rotation bullet rotation
     * @param damage bullet damage
     */
    public Bullet(Vector2 position, float rotation, int damage) {
        this.position = position;
        this.rotation = rotation;
        this.damage = damage;
    }

    @Override
    public void setup() {
        scale = new Vector2(0.1f, 0.1f);
        addCollider(new CircleCollider(3, ColliderType.None));
    }

    @Override
    public void update(float deltaTime) {
        time += deltaTime;
        float rotationInRad = (float) Math.toRadians(rotation);
        position = position.add(Vector2.fromRotation(rotationInRad).multiply(speed * deltaTime));

        if (time >= 2) {
            Server.removeObject(this);
        }
    }

    @Override
    public void onCollisionEnter(CollisionEvent e) {
        if (pierce <= 0) {
            return;
        }
        if (e.getOther().getParent() instanceof Damagable d) {
            if (d.isDead()) {
                return;
            }
            d.takeDamage(damage);
            pierce--;
            if (pierce <= 0) {
                Server.removeObject(this);
            }
            return;
        }
        if (e.getOther().getType() == ColliderType.Static) {
            Server.removeObject(this);
        }
    }
}
