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
            Vector2 rectOrigin = new Vector2(dimensions.x * pivot.x, dimensions.y * pivot.y);

            // Circle position relative to rectangle
            Vector2 circleLocal = c.parent.position.add(c.position).subtract(parent.position.add(position));
            circleLocal = circleLocal.rotate(-parent.rotation).add(rectOrigin);

            // Clamp circle center to rectangle bounds
            float closestX = Math.max(0, Math.min(dimensions.x, circleLocal.x));
            float closestY = Math.max(0, Math.min(dimensions.y, circleLocal.y));

            Vector2 closestPoint = new Vector2(closestX, closestY);

            // Normal in local space
            Vector2 normalLocal = circleLocal.subtract(closestPoint);
            float dist = normalLocal.length();

            // Check collision
            if (dist < c.radius) {
                Vector2 normalWorld;
                if (dist != 0)
                    normalWorld = normalLocal.normalize().rotate(parent.rotation);
                else
                    normalWorld = new Vector2(0, -1).rotate(parent.rotation); // fallback normal

                collided(new CollisionEvent(other, normalWorld));
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

        Vector2 panelPos = parent.position.subtract(Camera.getRenderPosition());
        panelPos = panelPos.add(position);
        panelPos = panelPos.divide(Camera.currentCamera.zoom);
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
