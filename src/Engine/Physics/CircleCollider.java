package Engine.Physics;

import Engine.Engine;
import Engine.GameObject;
import Engine.Vector2;
import Engine.Camera;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class CircleCollider extends Collider {
    float radius;

    public CircleCollider(GameObject parent, float radius, ColliderType colliderType) {
        this.parent = parent;
        this.radius = radius;
        this.type = colliderType;
    }

    public CircleCollider(float radius, ColliderType colliderType) {
        this.radius = radius;
        this.type = colliderType;
    }

    public void draw(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.RED);
        AffineTransform at = new AffineTransform();

        Vector2 panelDimensions = new Vector2(Engine.getCurrentScene().getWidth() / 2,
                Engine.getCurrentScene().getHeight() / 2);

        Vector2 panelPos = parent.position.subtract(Camera.currentCamera.position).subtract(new Vector2(radius, radius)).divide(Camera.currentCamera.zoom);
        panelPos = panelPos.add(panelDimensions);
        panelPos = panelPos.add(position);
        at.translate(panelPos.x, panelPos.y);

        g2d.drawOval((int) (panelPos.x), (int) (panelPos.y), (int) (radius * 2 / Camera.currentCamera.zoom), (int) (radius * 2 / Camera.currentCamera.zoom));
    }

    @Override
    public void checkCollision(Collider other) {
        if (other instanceof CircleCollider c) {
            Vector2 distance = c.parent.position.add(c.position).subtract(parent.position.add(c.position));
            float circleSum = c.radius + radius;
            if (distance.length() < circleSum) {
                collided(new CollisionEvent(other, distance.normalize()));
                if (type != ColliderType.Dynamic) {
                    return;
                }
                if (other.type == ColliderType.None) {
                    return;
                }
                newPosition = newPosition.subtract(distance.normalize().multiply(circleSum - distance.length()));

            }
        }

        if (other instanceof RectCollider rect) {
            Vector2 rectOrigin = new Vector2(rect.dimensions.x * rect.pivot.x, rect.dimensions.y * rect.pivot.y);

            // Circle position relative to rectangle
            Vector2 circleLocal = parent.position.add(position).subtract(rect.parent.position.add(rect.position));
            circleLocal = circleLocal.rotate(-rect.parent.rotation).add(rectOrigin);

            // Clamp circle center to rectangle bounds
            float closestX = Math.max(0, Math.min(rect.dimensions.x, circleLocal.x));
            float closestY = Math.max(0, Math.min(rect.dimensions.y, circleLocal.y));

            Vector2 closestPoint = new Vector2(closestX, closestY);

            // Normal in local space
            Vector2 normalLocal = circleLocal.subtract(closestPoint);
            float dist = normalLocal.length();

            // Check collision
            if (dist < radius) {
                Vector2 normalWorld;
                if (dist != 0)
                    normalWorld = normalLocal.normalize().rotate(rect.parent.rotation);
                else
                    normalWorld = new Vector2(0, -1).rotate(rect.parent.rotation); // fallback normal

                collided(new CollisionEvent(other, normalWorld));
                if (type != ColliderType.Dynamic) {
                    return;
                }
                if (other.type == ColliderType.None) {
                    return;
                }
                newPosition = newPosition.subtract(normalWorld.normalize().multiply(dist - radius));
            }
        }
    }
}
