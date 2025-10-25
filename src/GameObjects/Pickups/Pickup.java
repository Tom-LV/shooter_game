package GameObjects.Pickups;

import Engine.GameObject;
import Engine.Networking.Server;
import GameObjects.Player;
import java.util.ArrayList;

/**
 * A generic pickup that can be picked up by a player.
 */
public abstract class Pickup extends GameObject {
    
    float pickupDistance = 20f;
    float despawnTime = 10f;
    float time = 0;

    public abstract void onPickUp(GameObject player);

    @Override
    public void update(float deltaTime) {
        time += deltaTime;
        ArrayList<GameObject> playerObjects = Server.getClientObjectOfClass(Player.class);

        float closestDistance = Float.MAX_VALUE;
        GameObject closestPlayer = null;
        for (GameObject player : playerObjects) {
            float distance = player.position.subtract(position).length();
            if (distance < closestDistance) {
                closestDistance = distance;
                closestPlayer = player;
            }
        }

        if (closestDistance < pickupDistance) {
            onPickUp(closestPlayer);
        }

        if (time >= despawnTime) {
            Server.removeObject(this);
        }
    }
}
