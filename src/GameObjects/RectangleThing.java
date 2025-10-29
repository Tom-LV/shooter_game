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
    protected void setup() {
        dimensions = new Vector2(200, 100);
        // RectCollider(dimensions, pivot)
        addCollider(new RectCollider(dimensions, new Vector2(0.5f, 0.5f), ColliderType.Static));
        setLayer(900);
        scale = new Vector2(0.5f, 0.5f);
        rotation = 30f;
    }

    @Override
    protected void draw(Graphics2D g2d) {
        Vector2 size = dimensions;    // collider width/height
        Vector2 pivot = new Vector2(0.5f, 0.5f);

        // Apply object scale
        float w = size.x * scale.x;
        float h = size.y * scale.y;

        // Save original transform
        AffineTransform old = g2d.getTransform();

        Vector2 panelDimensions = new Vector2(Engine.getCurrentScene().getWidth() / 2,
                Engine.getCurrentScene().getHeight() / 2);

        Vector2 panelPos = position.subtract(Camera.currentCamera.position).divide(Camera.currentCamera.zoom);
        panelPos = panelPos.add(panelDimensions);

        g2d.translate(panelPos.x, panelPos.y);

        // Apply rotation (assumed `rotation` is in radians; if degrees, convert)
        g2d.rotate(Math.toRadians(rotation));


        // Draw collider relative to pivot
        float drawX = -(w * pivot.x) * 2;
        float drawY = -(h * pivot.y) * 2;

        g2d.setColor(Color.RED);
        g2d.drawRect(Math.round(drawX), Math.round(drawY), Math.round(w * 2), Math.round(h * 2));

        // Reset to original transform
        g2d.setTransform(old);
    }

    @Override
    public void update(float deltaTime) {
        rotation += 50f * deltaTime;
    }
}
