package GameObjects;

import Engine.GameObject;
import Engine.Physics.ColliderType;
import Engine.Physics.RectCollider;
import Engine.Vector2;

public class Doors extends GameObject {

    int width;
    @Override
    public void setup() {
        width = 200;

        addCollider(new RectCollider(new Vector2(width / 2.0f, 10f), new Vector2(0, 0), ColliderType.Static));

        RectCollider collider = new RectCollider(new Vector2(width / 2.0f, 10f), new Vector2(0, 1), ColliderType.Static);
        collider.setPosition(new Vector2(0, width));
        addCollider(collider);
    }

    @Override
    public void update(float deltaTime) {
        rotation += deltaTime * 30f;
    }
}
