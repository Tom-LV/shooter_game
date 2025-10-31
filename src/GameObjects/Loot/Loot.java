package GameObjects.Loot;

import Engine.GameObject;
import Engine.Networking.NetEvent;
import Engine.Networking.Server;
import Engine.Vector2;
import GameObjects.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public abstract class Loot extends GameObject {
    float interactTimer;
    float interactRange;
    Vector2 offset;
    static HashMap<GameObject, Loot> playerLoot = new HashMap<GameObject, Loot>();

    abstract boolean canInteract(GameObject player);
    abstract void onInteract();

    @NetEvent("finished_interaction")
    public static void finishedInteraction(UUID lootId) {

    }

    @Override
    public void update(float deltaTime) {
        List<GameObject> playerObjects = Server.getClientObjectsOfClass(Player.class);

        for (GameObject player : playerObjects) {

            float distance = player.position.subtract(position).length();
            if (playerLoot.containsKey(player)) {
                if (playerLoot.get(player) == this && distance >= interactRange) {
                    playerLoot.remove(player);
                    Server.sendMessage("hide_indicator", player.getOwnerUUID());
                }
            } else {
                if (distance < interactRange && canInteract(player)) {
                    playerLoot.put(player, this);
                    Server.sendMessage("show_indicator", player.getOwnerUUID(), position.add(offset), interactTimer);
                }
            }

        }
    }





}

