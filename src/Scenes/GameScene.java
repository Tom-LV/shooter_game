package Scenes;

import Engine.Networking.Server;
import Engine.Scene;
import Engine.Vector2;
import GameObjects.*;
import GameObjects.Enemies.EnemyManager;
import GameObjects.Pickups.WeaponPickup;
import GameObjects.UI.AmmoLabel;
import GameObjects.UI.HealthBar;
import GameObjects.UI.WeaponSelect;

import java.awt.Color;

/**
 * Main game scene.
 */
public class GameScene extends Scene {
    
    @Override
    public void setupScene() {
        setBackground(new Color(35, 42, 46));

        Player player = new Player(new Vector2(0, 0));
        addNetworkObject(player);
        addNetworkObject(new HealthBar(player));

        WeaponSelect.createIcons(this);
        addObject(new AmmoLabel(player));
        addObject(new TileMap());
        Server.addObject(new RectangleThing());
        //addObject(new MouseFollower());

        Server.addObject(new WeaponPickup(new Vector2(-100, -100), 0));
        Server.addObject(new WeaponPickup(new Vector2(0, -100), 1));
        Server.addObject(new WeaponPickup(new Vector2(100, -100), 2));
        Server.addObject(new EnemyManager());
        Server.addObject(new LootManager());
        //Server.addObject(new RectangleThing());

    }
}
