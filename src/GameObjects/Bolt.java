package GameObjects;

import Engine.Engine;
import Engine.GameObject;
import Engine.Vector2;

import java.util.Random;

public class Bolt extends GameObject {
    Vector2 velocity = new Vector2(0, 0);
    float speed = 0.1f;

    public Bolt(Vector2 position) {
        this.position = position;
    }

    @Override
    public void setup() {
        Random rng = new Random();
        setSprite("bolt");
        scale = new Vector2(0.1f, 0.1f);
        rotation = rng.nextInt(360);
    }

    @Override
    public void update(float deltaTime) {
        Vector2 playerPos = Player.getInstance().position;
        Vector2 distance = playerPos.subtract(position);
        velocity = velocity.add(new Vector2(speed * deltaTime, 0));
        position = position.add(velocity.rotate(distance.getRotation()));
        if (distance.length() < 5f) {
            Player.getInstance().addBolts(1);
            Engine.destroy(this);
        }
    }
}
