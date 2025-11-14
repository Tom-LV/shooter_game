package GameObjects.Loot;

import Engine.GameObject;
import Engine.Networking.NetEvent;
import Engine.Networking.Server;
import Engine.Vector2;
import GameObjects.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Loot extends GameObject {
    float interactTimer;
    float interactRange;
    Vector2 offset;
    static HashMap<UUID, Loot> playerLoot = new HashMap<>();
    static HashMap<UUID, GameObject> playerGameObjects = new HashMap<>();

    boolean canInteract(GameObject player) {
        return false;
    }
    void onInteract(GameObject player) {}

    @NetEvent("finished_interaction")
    public static void finishedInteraction(UUID playerUUID) {
        if (playerLoot.containsKey(playerUUID)) {
            Loot loot =  playerLoot.get(playerUUID);
            loot.onInteract(playerGameObjects.get(playerUUID));
            playerLoot.remove(playerUUID);

            List<GameObject> playerObjects = Server.getClientObjectsOfClass(Player.class);
            for (GameObject playerObject : playerObjects) {
                if (playerLoot.containsKey(playerObject.getOwnerUUID())) {
                    if (playerLoot.get(playerObject.getOwnerUUID()) == loot) {
                        Server.sendMessage("hide_indicator", playerObject.getOwnerUUID());
                        playerLoot.remove(playerObject.getOwnerUUID());
                    }
                }
            }
        }


    }

    @Override
    public void update(float deltaTime) {
        List<GameObject> playerObjects = Server.getClientObjectsOfClass(Player.class);

        for (GameObject player : playerObjects) {
            if (!playerGameObjects.containsKey(player.getOwnerUUID())) {
                playerGameObjects.put(player.getOwnerUUID(), player);
            }

            float distance = player.position.subtract(position).length();
            if (playerLoot.containsKey(player.getOwnerUUID())) {
                if (playerLoot.get(player.getOwnerUUID()) == this && (distance >= interactRange || !canInteract(player))) {
                    playerLoot.remove(player.getOwnerUUID());
                    Server.sendMessage("hide_indicator", player.getOwnerUUID());
                }
            } else {
                if (distance < interactRange && canInteract(player)) {
                    playerLoot.put(player.getOwnerUUID(), this);
                    Server.sendMessage("show_indicator", player.getOwnerUUID(), position.add(offset), interactTimer);
                }
            }

        }
    }





}

