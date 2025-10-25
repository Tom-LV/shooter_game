package GameObjects;

import Engine.Animation;
import Engine.Camera;
import Engine.Engine;
import Engine.GameObject;
import Engine.Inputs.Input;
import Engine.Networking.NetEvent;
import Engine.Sound.AudioClip;
import Engine.Sound.AudioPlayer;
import Engine.Vector2;
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

    int health = 100;
    int maxHealth = 100;
    int selectedWeapon = 0;
    float reloadTime = 0.3f;
    float invTimer = 0f;
    String weaponAttackType = "shoot_pistol";

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    private void selectWeapon(int index) {
        selectedWeapon = index;
        stopAnimation();
        switch (index) {
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
            default:
                break;
        }
    }

    public Player(Vector2 position) {
        this.position = position;
    }

    /**
     * An event that is fired when player takes damage.
     * @param damage amount of damage to take
     */
    @NetEvent("player_hit")
    public static void hit(int damage) {
        List<GameObject> players = Engine.getCurrentScene().getObjectsOfClass(Player.class);
        Player player;
        if (players.size() == 0) {
            return;
        }
        AudioPlayer.playAudio(Player.hurtSfx, false);
        player = (Player) players.get(0);
        if (player.invTimer > 0) {
            return;
        }
        player.health -= damage;
        if (player.getHealth() <= 0) {
            player.health = 100;
            player.position = new Vector2(0, 0);
            player.invTimer = 1f;
        }
    }

    /**
     * An event that is fired when the player picks up a health pickup.
     * @param health the amount of health to give to the player
     */
    @NetEvent("health_pickup")
    public static void healthPickup(int health) {
        List<GameObject> players = Engine.getCurrentScene().getObjectsOfClass(Player.class);
        Player player;
        if (players.size() == 0) {
            return;
        }
        AudioPlayer.playAudio(Player.pickupSfx, false);
        player = (Player) players.get(0);
        player.health += health;
        if (player.getHealth() > player.maxHealth) {
            player.health = player.maxHealth;
        }

    }

    @Override
    protected void setup() {
        setSprite("player_pistol");
        setLayer(1);
        scale = new Vector2(0.15f, 0.15f);
        selectWeapon(0);
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

        animations = new ArrayList<>();
        animations.add(pistolAnim);
        animations.add(shotgunAnim);
        animations.add(rifleAnim);
    }

    @Override
    public void update(float deltaTime) {
        time += deltaTime;
        if (invTimer > 0) {
            invTimer -= deltaTime;
        }

        if (Input.isKeyPressed(KeyEvent.VK_1)) {
            selectWeapon(0);
        } else if (Input.isKeyPressed(KeyEvent.VK_2)) {
            selectWeapon(1);
        } else if (Input.isKeyPressed(KeyEvent.VK_3)) {
            selectWeapon(2);
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
        if (Input.mouse.isClicked(0) && time >= reloadTime) {
            
            AudioPlayer.playAudio(shootSfx, false);
            Vector2 bulletPosition = position;
            sendMessage(weaponAttackType, bulletPosition, rotation);
            time = 0;
            playAnimation(animations.get(selectedWeapon));
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

        Camera.currentCamera.position = position;

    }
    
}
