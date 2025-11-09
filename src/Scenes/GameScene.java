package Scenes;

import Engine.Networking.Server;
import Engine.Scene;
import Engine.Vector2;
import GameObjects.*;
import GameObjects.Enemies.EnemyManager;
import GameObjects.GameManagment.ServerManager;
import GameObjects.GameManagment.ClientManager;
import GameObjects.Loot.InteractIndicator;
import GameObjects.Loot.LootManager;
import GameObjects.Pickups.PickupManager;
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

        Player player = new Player(new Vector2(0, -800));
        addNetworkObject(player);
        addNetworkObject(new HealthBar(player));

        WeaponSelect.createIcons(this);
        addObject(new AmmoLabel());
        addObject(new TileMap());
        addObject(new InteractIndicator());
        addObject(new ClientManager(player));
        PickupManager.createPickup(new Vector2(0, -400), "bolt");


        Server.addObject(new TileMap());
        Server.addObject(new EnemyManager());
        Server.addObject(new LootManager());
        Server.addObject(new ServerManager());

    }
}
