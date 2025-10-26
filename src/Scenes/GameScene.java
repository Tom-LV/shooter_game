package Scenes;

import Engine.Networking.Server;
import Engine.Scene;
import Engine.Vector2;
import GameObjects.Background;
import GameObjects.EnemyManager;
import GameObjects.HealthBar;
import GameObjects.Pickups.PickupManager;
import GameObjects.Pickups.WeaponPickup;
import GameObjects.Player;
import GameObjects.WeaponSelect;
import java.awt.Color;

/**
 * Main game scene.
 */
public class GameScene extends Scene {
    
    @Override
    public void setupScene() {
        setBackground(new Color(35, 42, 46));
        
        WeaponSelect.createIcons(this);
        Player player = new Player(new Vector2(0, 0));
        addNetworkObject(player);
        addNetworkObject(new HealthBar(player));

        Server.addObject(new WeaponPickup(new Vector2(0, 100)));
        Server.addObject(new EnemyManager());
        Server.addObject(new Background());
    }
}
