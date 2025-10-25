package Scenes;

import Engine.Networking.Server;
import Engine.Scene;
import Engine.Vector2;
import GameObjects.Background;
import GameObjects.EnemyManager;
import GameObjects.HealthBar;
import GameObjects.Minimap;
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
        addObject(player);
        addObject(new HealthBar(player));
        addObject(new Minimap());

        

        Server.addObject(new EnemyManager());
        Server.addObject(new Background());
    }
}
