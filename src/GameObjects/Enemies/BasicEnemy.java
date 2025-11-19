package GameObjects.Enemies;

import Engine.GameObject;
import Engine.Networking.Server;
import Engine.Physics.CircleCollider;
import Engine.Physics.ColliderType;
import Engine.Vector2;

import java.util.Random;

public class BasicEnemy extends Enemy {

    public BasicEnemy(Vector2 position) {
        super(position);
    }

    @Override
    public void setup() {
        health = 30;
        rng = new Random();
        setSprite("zombie");
        rotation = rng.nextInt(360);
        scale = new Vector2(0.15f, 0.15f);
        addCollider(new CircleCollider(13, ColliderType.Dynamic));
        if (rng.nextFloat() >= 0.5) {
            followRandomPlayer();
        }
    }

    @Override
    void attack(GameObject player) {
        Server.sendMessage("player_hit", player.getOwnerUUID(), 10);
    }
}
