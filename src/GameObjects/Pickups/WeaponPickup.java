package GameObjects.Pickups;

import Engine.GameObject;
import Engine.Networking.NetEvent;
import Engine.Networking.Server;
import Engine.Physics.CircleCollider;
import Engine.Physics.ColliderType;
import Engine.Physics.CollisionEvent;
import Engine.Vector2;
import GameObjects.GameManagment.ServerManager;
import GameObjects.WeaponManager;
import Interfaces.Damagable;
import java.util.List;

public class WeaponPickup extends Pickup {

    float speed = 400f;
    float thrownTimer = 0f;
    Vector2 velocity;
    int weaponIndex = 0;
    boolean pierce = true;
    int damage = 10;
    float maxThrowTimer = 0.5f;

    @NetEvent("throw_weapon")
    public static void weaponThrow(Vector2 position, float rotation, int weapon) {
        WeaponManager.dropWeapon();
        if (!ServerManager.isRoundStarted()) {
            ServerManager.spawnGunSelection();
            return;
        }
        WeaponPickup weaponPickup = new WeaponPickup(position, rotation);
        Server.addObject(weaponPickup);

        weaponPickup.weaponIndex = weapon;
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
                pierce = false;
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
            case 3:
                setSprite("uzirang");
                scale = new Vector2(0.045f, 0.045f);
                pierce = true;
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
        WeaponManager.weaponPickup(player);
        destroyAllGuns();
    }

    public static void destroyAllGuns() {
        List<GameObject> weaponPickups = Server.getServerObjectsOfClass(WeaponPickup.class);
        for (GameObject gameObject : weaponPickups){
            WeaponPickup weaponPickup = (WeaponPickup) gameObject;
            weaponPickup.canPickUp = false;
            Server.removeObject(weaponPickup);
        }
    }

    @Override
    public void setup() {
        setLayer(-10);
        despawnTime = -1;
        setWeapon(weaponIndex);
        maxThrowTimer = 0.5f;
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
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        thrownTimer += deltaTime;

        if (thrownTimer < maxThrowTimer) {
            position = position.add(velocity.multiply(speed * deltaTime));
            rotation += 600 * deltaTime;
        } else if (weaponIndex == 3) {
            if (thrownTimer < maxThrowTimer + 1f) {
                position = position.add(velocity.multiply(-speed * deltaTime));
                rotation += 600 * deltaTime;
            }
        }
    }

    @Override
    public void onCollisionEnter(CollisionEvent e) {

        if (thrownTimer < maxThrowTimer || (weaponIndex == 3 && thrownTimer < maxThrowTimer + 1f)) {
            if (e.getOther().getParent() instanceof Damagable d) {
                d.takeDamage(damage);
                if (pierce) {
                    return;
                }
            }
            maxThrowTimer += 0.1f;
            float dot = velocity.dot(e.getNormal());
            velocity = velocity.subtract(e.getNormal().multiply(2 * dot));
        }

    }
}
