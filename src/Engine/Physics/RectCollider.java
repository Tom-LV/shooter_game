package Engine.Physics;

import Engine.Engine;
import Engine.Vector2;
import Engine.Camera;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class RectCollider extends Collider {
    Vector2 dimensions;
    Vector2 pivot;
    public RectCollider(Vector2 dimensions, Vector2 pivot, ColliderType type) {
        this.dimensions = dimensions;
        this.pivot = pivot;
        this.type = type;
    }

    @Override
    public void checkCollision(Collider other) {
        if (other instanceof CircleCollider c) {
            Vector2 start = new Vector2(dimensions.x * pivot.x, dimensions.y * pivot.y);
            start = start.rotate(parent.rotation);
            start = parent.position.subtract(start);

            Vector2 distance = other.parent.position.subtract(start);
            Vector2 relativePos = distance.rotate(-parent.rotation);
            if (relativePos.x > 0 && relativePos.y > 0) {
                if (relativePos.x < dimensions.x && relativePos.y < dimensions.y) {
                    collided(new  CollisionEvent(other, new Vector2(relativePos.x, relativePos.y)));
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        float w = dimensions.x / Camera.currentCamera.zoom;
        float h = dimensions.y / Camera.currentCamera.zoom;

        g2d.setStroke(new BasicStroke(1));
        AffineTransform old = g2d.getTransform();

        Vector2 panelDimensions = new Vector2(Engine.getCurrentScene().getWidth() / 2,
                Engine.getCurrentScene().getHeight() / 2);

        Vector2 panelPos = parent.position.subtract(Camera.currentCamera.position).divide(Camera.currentCamera.zoom);
        panelPos = panelPos.add(panelDimensions);

        g2d.translate(panelPos.x, panelPos.y);

        // Apply rotation (assumed `rotation` is in radians; if degrees, convert)
        g2d.rotate(Math.toRadians(parent.rotation));


        // Draw collider relative to pivot
        float drawX = -(w * pivot.x);
        float drawY = -(h * pivot.y);

        g2d.setColor(Color.RED);
        g2d.drawRect(Math.round(drawX), Math.round(drawY), Math.round(w), Math.round(h));

        // Reset to original transform
        g2d.setTransform(old);
    }
}
