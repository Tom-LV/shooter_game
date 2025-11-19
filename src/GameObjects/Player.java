package GameObjects;

import Engine.Animation;
import Engine.Camera;
import Engine.Engine;
import Engine.GameObject;
import Engine.Inputs.Input;
import Engine.Networking.Client;
import Engine.Networking.NetEvent;
import Engine.Physics.CircleCollider;
import Engine.Physics.ColliderType;
import Engine.Sound.AudioClip;
import Engine.Sound.AudioPlayer;
import Engine.Vector2;
import GameObjects.GameManagment.ClientManager;
import GameObjects.UI.WeaponSelect;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * A player gameObject.
 */
public class Player extends GameObject {
    double time = 0f;
    float speed = 200f;
    Vector2 velocity = new Vector2(0f, 0f);
    AudioClip shootSfx = new AudioClip("src\\Assets\\audio\\shoot.wav");
    static AudioClip pickupSfx = new AudioClip("src\\Assets\\audio\\pickup.wav");
    static AudioClip hurtSfx = new AudioClip("src\\Assets\\audio\\hurt.wav");
    ArrayList<Animation> animations;

    static Player instance;

    int ammo = 150;
    int bolts = 0;
    int health = 100;
    int maxHealth = 100;
    int selectedWeapon = 0;
    float reloadTime = 0.3f;
    float invTimer = 0f;
    String weaponAttackType = "shoot_pistol";

    boolean hasWeapon = false;

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getAmmo() {
        return ammo;
    }

    public void setAmmo(int ammo) {
        this.ammo =  ammo;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getBolts() {
        return bolts;
    }

    public void addBolts(int bolts) {
        this.bolts += bolts;
    }

    private void selectWeapon(int index) {
        selectedWeapon = index;
        stopAnimation();
        switch (index) {
            case -1:
                reloadTime = 0.3f;
                weaponAttackType = "";
                setSprite("player");
                WeaponSelect.selectWeapon(-1);
                break;
            case 0:
                reloadTime = 0.3f;
                weaponAttackType = "shoot_pistol";
                setSprite("player_pistol");
                WeaponSelect.selectWeapon(0);
                break;
            case 1:
                reloadTime = 0.9f;
                weaponAttackType = "shoot_shotgun";
                setSprite("player_sg");
                WeaponSelect.selectWeapon(1);
                break;
            case 2:
                reloadTime = 0.1f;
                weaponAttackType = "shoot_minigun";
                setSprite("player_mg");
                WeaponSelect.selectWeapon(2);
                break;
            case 3:
                reloadTime = 0.05f;
                weaponAttackType = "shoot_uzirang";
                setSprite("player_uzirang");
                WeaponSelect.selectWeapon(3);
                break;
            default:
                break;
        }
    }

    public Player(Vector2 position) {
        this.position = position;
        instance = this;
    }

    public static Player getInstance() {
        return instance;
    }

    /**
     * An event that is fired when player takes damage.
     * @param damage amount of damage to take
     */
    @NetEvent("player_hit")
    public static void hit(int damage) {
        List<GameObject> players = Engine.getCurrentScene().getObjectsOfClass(Player.class);
        Player player;
        if (players.isEmpty()) {
            return;
        }
        AudioPlayer.playAudio(Player.hurtSfx, false);
        player = (Player) players.get(0);
        if (player.invTimer > 0) {
            return;
        }
        player.health -= damage;
        if (player.getHealth() <= 0) {
            Client.sendMessage("drop_weapon", player.position, player.selectedWeapon);
            player.hasWeapon = false;
            player.selectWeapon(-1);
            Client.sendMessage("player_killed", Client.getClientId());
            player.health = 100;
            player.position = new Vector2(0, -800);
            player.invTimer = 1f;
        }
    }

    @NetEvent("bolt_spawn")
    public static void spawnBolt(Vector2 pos) {
        Engine.addObject(new Bolt(pos));
    }

    /**
     * An event that is fired when the player picks up a health pickup.
     * @param health the amount of health to give to the player
     */
    @NetEvent("health_pickup")
    public static void healthPickup(int health) {
        List<GameObject> players = Engine.getCurrentScene().getObjectsOfClass(Player.class);
        Player player;
        if (players.isEmpty()) {
            return;
        }
        AudioPlayer.playAudio(Player.pickupSfx, false);
        player = (Player) players.get(0);
        player.health += health;
        if (player.getHealth() > player.maxHealth) {
            player.health = player.maxHealth;
        }
    }

    @NetEvent("ammo_pickup")
    public static void ammoPickup(int ammo) {
        List<GameObject> players = Engine.getCurrentScene().getObjectsOfClass(Player.class);
        Player player;
        if (players.isEmpty()) {
            return;
        }
        AudioPlayer.playAudio(Player.pickupSfx, false);
        player = (Player) players.get(0);
        player.ammo += ammo;
    }

    @NetEvent("weapon_pickup")
    public static void pickupWeapon(int weaponIndex) {
        List<GameObject> players = Engine.getCurrentScene().getObjectsOfClass(Player.class);
        Player player;
        if (players.isEmpty()) {
            return;
        }
        player = (Player) players.get(0);
        player.hasWeapon = true;
        player.selectWeapon(weaponIndex);
    }

    @Override
    public void setup() {
        setSprite("player_pistol");
        setLayer(1);
        scale = new Vector2(0.15f, 0.15f);
        Animation pistolAnim = new Animation();
        pistolAnim.addFrame("player_pistol1", 0.03f);
        pistolAnim.addFrame("player_pistol2", 0.03f);
        pistolAnim.addFrame("player_pistol3", 0.03f);
        pistolAnim.addFrame("player_pistol", 0.03f);

        Animation rifleAnim = new Animation();
        rifleAnim.addFrame("player_mg1", 0.03f);
        rifleAnim.addFrame("player_mg2", 0.03f);
        rifleAnim.addFrame("player_mg", 0.03f);

        Animation shotgunAnim = new Animation();
        shotgunAnim.addFrame("player_sg1", 0.03f);
        shotgunAnim.addFrame("player_sg2", 0.03f);
        shotgunAnim.addFrame("player_sg3", 0.03f);
        shotgunAnim.addFrame("player_sg", 0.03f);
        shotgunAnim.addFrame("player_sg4", 0.45f);
        shotgunAnim.addFrame("player_sg5", 0.04f);
        shotgunAnim.addFrame("player_sg6", 0.04f);
        shotgunAnim.addFrame("player_sg7", 0.04f);
        shotgunAnim.addFrame("player_sg4", 0.04f);
        shotgunAnim.addFrame("player_sg", 0.04f);

        Animation uziAnim = new Animation();
        uziAnim.addFrame("player_uzirang1", 0.015f);
        uziAnim.addFrame("player_uzirang2", 0.015f);
        uziAnim.addFrame("player_uzirang3", 0.015f);
        uziAnim.addFrame("player_uzirang", 0.015f);


        animations = new ArrayList<>();
        animations.add(pistolAnim);
        animations.add(shotgunAnim);
        animations.add(rifleAnim);
        animations.add(uziAnim);
        selectWeapon(-1);
        addCollider(new CircleCollider(10f, ColliderType.Dynamic));

    }

    @Override
    public void update(float deltaTime) {
        Camera.currentCamera.position = position;
        time += deltaTime;
        if (invTimer > 0) {
            invTimer -= deltaTime;
        }

        // Movement
        if (Input.isKeyPressed(KeyEvent.VK_W)) {
            velocity.y -= speed;
        }
        if (Input.isKeyPressed(KeyEvent.VK_S)) {
            velocity.y += speed;
        }
        if (Input.isKeyPressed(KeyEvent.VK_A)) {
            velocity.x -= speed;
        }
        if (Input.isKeyPressed(KeyEvent.VK_D)) {
            velocity.x += speed;
        }

        position = position.add(velocity.multiply(deltaTime));
        velocity = new Vector2(0, 0);
        rotation = Input.mouse.getWorldPosition().subtract(position).getRotation();

        // Shooting
        if (Input.mouse.isClicked(0) && time >= reloadTime && hasWeapon && ammo > 0) {
            if (selectedWeapon == 1) {
                ammo -= 8;
            } else {
                ammo--;
            }
            AudioPlayer.playAudio(shootSfx, false);
            Vector2 bulletPosition = position;
            Client.sendMessage(weaponAttackType, bulletPosition, rotation);
            time = 0;
            playAnimation(animations.get(selectedWeapon));
        }

        if (Input.isKeyPressed(KeyEvent.VK_Q) && hasWeapon && !ClientManager.isReady()) {
            hasWeapon = false;
            Client.sendMessage("throw_weapon", position, rotation, selectedWeapon);
            selectWeapon(-1);
        }



    }
    
}
