package GameObjects;

import Engine.Camera;
import Engine.Engine;
import Engine.GameObject;
import Engine.Physics.Collider;
import Engine.Physics.ColliderType;
import Engine.Physics.RectCollider;
import Engine.Vector2;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class RectangleThing extends GameObject {
    Vector2 dimensions;

    @Override
    public void setup() {
        dimensions = new Vector2(200, 100);
        // RectCollider(dimensions, pivot)
        addCollider(new RectCollider(dimensions, new Vector2(0.5f, 0.5f), ColliderType.Static));
        setLayer(900);
        scale = new Vector2(0.5f, 0.5f);
        rotation = 30f;
    }

    @Override
    public void update(float deltaTime) {
        rotation += 50f * deltaTime;
    }
}
