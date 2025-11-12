package GameObjects.Pickups;

import Engine.GameObject;
import Engine.Networking.Server;
import GameObjects.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * A generic pickup that can be picked up by a player.
 */
public abstract class Pickup extends GameObject {
    
    float pickupDistance = 20f;
    float despawnTime = 10f;
    float time = 0;
    boolean canPickUp = true;
    boolean pickedUp = false;

    public abstract void onPickUp(GameObject player);

    @Override
    public void update(float deltaTime) {
        time += deltaTime;
        List<GameObject> playerObjects = Server.getClientObjectsOfClass(Player.class);

        float closestDistance = Float.MAX_VALUE;
        GameObject closestPlayer = null;
        for (GameObject player : playerObjects) {
            float distance = player.position.subtract(position).length();
            if (distance < closestDistance) {
                closestDistance = distance;
                closestPlayer = player;
            }
        }

        if (closestDistance < pickupDistance && canPickUp && !pickedUp) {
            pickedUp = true;
            onPickUp(closestPlayer);
        }

        if (time >= despawnTime && despawnTime != -1) {
            Server.removeObject(this);
        }
    }
}
