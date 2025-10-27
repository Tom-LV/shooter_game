import Engine.ClassManager;
import Engine.Engine;
import Engine.Networking.NetworkHandleRegister;
import Engine.Sprite;
import Engine.Vector2;
import Scenes.MainMenuScene;

/**
 * Main app for the program.
 */
public class Main {

    private void loadImages() {
        // Art asset path
        String artAssetPath = "src\\Assets\\art\\";


        Sprite.loadImage("player", artAssetPath + "player\\smith.png",
                new Vector2(0.45f, 0.5f));
        // Pistol sprites
        Sprite.loadImage("player_pistol", artAssetPath + "player\\smith_pistol.png",
                new Vector2(0.25f, 0.5f));
        Sprite.loadImage("player_pistol1", artAssetPath + "player\\smith_pistol1.png",
                new Vector2(0.18f, 0.5f));
        Sprite.loadImage("player_pistol2", artAssetPath + "player\\smith_pistol2.png",
                new Vector2(0.18f, 0.5f));
        Sprite.loadImage("player_pistol3", artAssetPath + "player\\smith_pistol3.png",
                new Vector2(0.18f, 0.5f));

        // Rifle sprites
        Sprite.loadImage("player_mg", artAssetPath + "player\\Smith_mg.png",
                new Vector2(0.25f, 0.6f));
        Sprite.loadImage("player_mg1", artAssetPath + "player\\Smith_mg1.png",
                new Vector2(0.18f, 0.6f));
        Sprite.loadImage("player_mg2", artAssetPath + "player\\Smith_mg2.png",
                new Vector2(0.25f, 0.6f));

        // Shotgun sprites
        Sprite.loadImage("player_sg", artAssetPath + "player\\Smith_sg.png",
                new Vector2(0.25f, 0.6f));
        Sprite.loadImage("player_sg1", artAssetPath + "player\\Smith_sg1.png",
                new Vector2(0.2f, 0.6f));
        Sprite.loadImage("player_sg2", artAssetPath + "player\\Smith_sg2.png",
                new Vector2(0.2f, 0.6f));
        Sprite.loadImage("player_sg3", artAssetPath + "player\\Smith_sg3.png",
                new Vector2(0.2f, 0.6f));
        Sprite.loadImage("player_sg4", artAssetPath + "player\\Smith_sg4.png",
                new Vector2(0.25f, 0.6f));
        Sprite.loadImage("player_sg5", artAssetPath + "player\\Smith_sg5.png",
                new Vector2(0.25f, 0.6f));
        Sprite.loadImage("player_sg6", artAssetPath + "player\\Smith_sg6.png",
                new Vector2(0.25f, 0.6f));
        Sprite.loadImage("player_sg7", artAssetPath + "player\\Smith_sg7.png",
                new Vector2(0.25f, 0.6f));

        // Other sprites
        Sprite.loadImage("bullet", artAssetPath + "bullet.png",
                new Vector2(0.85f, 0.5f));
        Sprite.loadImage("zombie", artAssetPath + "descrom.png",
                new Vector2(0.3f, 0.5f));
        Sprite.loadImage("zombie_hit", artAssetPath + "descrom_hit.png",
                new Vector2(0.3f, 0.5f));
        Sprite.loadImage("city", artAssetPath + "city.png",
                new Vector2(0.5f, 0.5f));
        Sprite.loadImage("health", artAssetPath + "health.png",
                new Vector2(0f, 0.5f));
        Sprite.loadImage("health_pickup", artAssetPath + "medkit.png",
                new Vector2(0.5f, 0.5f));
        Sprite.loadImage("descrom_nest", artAssetPath + "descrom_nest.png",
                new Vector2(0.5f, 0.5f));
        Sprite.loadImage("ammo_crate", artAssetPath + "ammo_crate.png",
                new Vector2(0.5f, 0.5f));

        // Weapons
        Sprite.loadImage("pistol", artAssetPath + "pistol.png",
                new Vector2(0.5f, 0.5f));
        Sprite.loadImage("rifle", artAssetPath + "rifle.png",
                new Vector2(0.5f, 0.5f));
        Sprite.loadImage("shotgun", artAssetPath + "shotgun.png",
                new Vector2(0.5f, 0.5f));

        // Weapon icons
        Sprite.loadImage("pistol_icon", artAssetPath + "pistol_icon.png",
                new Vector2(0.5f, 0.5f));
        Sprite.loadImage("rifle_icon", artAssetPath + "rifle_icon.png",
                new Vector2(0.5f, 0.5f));
        Sprite.loadImage("shotgun_icon", artAssetPath + "shotgun_icon.png",
                new Vector2(0.5f, 0.5f));

        // Roads
        Sprite.loadImage("road_0000", artAssetPath + "roads\\road_0000.png");
        Sprite.loadImage("road_0001", artAssetPath + "roads\\road_0001.png");
        Sprite.loadImage("road_0010", artAssetPath + "roads\\road_0010.png");
        Sprite.loadImage("road_0100", artAssetPath + "roads\\road_0100.png");
        Sprite.loadImage("road_1000", artAssetPath + "roads\\road_1000.png");
        Sprite.loadImage("road_0011", artAssetPath + "roads\\road_0011.png");
        Sprite.loadImage("road_0110", artAssetPath + "roads\\road_0110.png");
        Sprite.loadImage("road_1100", artAssetPath + "roads\\road_1100.png");
        Sprite.loadImage("road_1010", artAssetPath + "roads\\road_1010.png");
        Sprite.loadImage("road_1001", artAssetPath + "roads\\road_1001.png");
        Sprite.loadImage("road_0101", artAssetPath + "roads\\road_0101.png");
        Sprite.loadImage("road_1110", artAssetPath + "roads\\road_1110.png");
        Sprite.loadImage("road_0111", artAssetPath + "roads\\road_0111.png");
        Sprite.loadImage("road_1011", artAssetPath + "roads\\road_1011.png");
        Sprite.loadImage("road_1101", artAssetPath + "roads\\road_1101.png");
        Sprite.loadImage("road_1111", artAssetPath + "roads\\road_1111.png");
    }

    public void run() {
        // Initializes engine
        Engine.start();
        Engine.setWindowIcon("src\\Assets\\art\\player\\smith_pistol.png");
        Engine.setWindowName("Co-op Cop");
        Engine.showWindow();

        // Register network events
        NetworkHandleRegister.registerAllGameObjectHandlers("GameObjects");

        // Register GameObjects
        ClassManager.registerClassesFromBasePackage("GameObjects");
        // Register scenes - not used
        ClassManager.registerClassesFromBasePackage("Scenes");

        loadImages();

        // Load starting scene
        Engine.changeScene(new MainMenuScene());

        // Update loop
        while (Engine.isRunning()) {
            Engine.update();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
