package GameObjects.Pickups;

import Engine.GameObject;
import Engine.Networking.NetEvent;
import Engine.Networking.Server;
import Engine.Physics.CircleCollider;
import Engine.Physics.Collider;
import Engine.Physics.ColliderType;
import Engine.Physics.CollisionEvent;
import Engine.Vector2;
import GameObjects.Enemy;
import GameObjects.WeaponManager;
import Interfaces.Damagable;

import java.util.ArrayList;
import java.util.List;

public class WeaponPickup extends Pickup {

    float speed = 400f;
    float thrownTimer = 0f;
    Vector2 velocity;
    int weaponIndex = 0;
    boolean pierce = true;
    int damage = 10;

    @NetEvent("throw_weapon")
    public static void weaponThrow(Vector2 position, float rotation, int weapon) {
        WeaponPickup weaponPickup = new WeaponPickup(position, rotation);
        weaponPickup.setWeapon(weapon);
        Server.addObject(weaponPickup);
        WeaponManager.dropWeapon();
    }

    void setWeapon(int weaponIndex) {
        this.weaponIndex = weaponIndex;
        switch (weaponIndex) {
            case 0:
                setSprite("pistol");
                scale = new Vector2(0.05f, 0.05f);
                pierce = false;
                addCollider(new CircleCollider(7, ColliderType.None));
                break;
            case 1:
                setSprite("shotgun");
                scale = new Vector2(0.07f, 0.07f);
                pierce = true;
                addCollider(new CircleCollider(10, ColliderType.None));
                pickupDistance = 30f;
                speed = 600f;
                break;
            case 2:
                setSprite("rifle");
                scale = new Vector2(0.07f, 0.07f);
                pierce = false;
                addCollider(new CircleCollider(10, ColliderType.None));
                pickupDistance = 30f;
                break;
            default:
                break;
        }
    }

    @Override
    public void onPickUp(GameObject player) {
        if (WeaponManager.hasWeapon(player)) {
            return;
        }
        Server.sendMessage("weapon_pickup", player.getOwnerUUID(), weaponIndex);
        Server.removeObject(this);
        List<GameObject> weaponPickups = Server.getServerObjectsOfClass(WeaponPickup.class);
        WeaponManager.weaponPickup(player);
        for (GameObject gameObject : weaponPickups){
            WeaponPickup weaponPickup = (WeaponPickup) gameObject;
            weaponPickup.canPickUp = false;
            Server.removeObject(weaponPickup);
        }
    }

    @Override
    protected void setup() {
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

    public WeaponPickup(Vector2 position, int weaponIndex) {
        this.position = position;
        thrownTimer = 3f;
        speed = 0;
        velocity = new Vector2(0f, 0f);
        despawnTime = -1;
        this.weaponIndex = weaponIndex;
        setWeapon(weaponIndex);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        thrownTimer += deltaTime;

        if (thrownTimer < 0.5f) {
            position = position.add(velocity.multiply(speed * deltaTime));
            rotation += 600 * deltaTime;
        } else if (weaponIndex == 1) {
            if (thrownTimer < 1.5f) {
                position = position.add(velocity.multiply(-speed * deltaTime));
                rotation += 600 * deltaTime;
            }
        }


        if (position.y < -910) {
            position.y = -910;
            velocity = new Vector2(velocity.x, -velocity.y);
        }
        if (position.x < -1300) {
            position.x = -1300;
            velocity = new Vector2(-velocity.x, velocity.y);
        }
        if (position.y > 920) {
            position.y = 920;
            velocity = new Vector2(velocity.x, -velocity.y);
        }
        if (position.x > 1350) {
            position.x = 1350;
            velocity = new Vector2(-velocity.x, velocity.y);
        }
    }

    @Override
    public void onCollisionEnter(CollisionEvent e) {

        if (thrownTimer < 0.5f || (weaponIndex == 1 && thrownTimer < 1.5f)) {
            if (e.getOther().getParent() instanceof Damagable d) {
                d.takeDamage(damage);
                if (pierce) {
                    return;
                }
            }
            float dot = velocity.dot(e.getNormal());
            velocity = velocity.subtract(e.getNormal().multiply(2 * dot));
        }

    }
}
