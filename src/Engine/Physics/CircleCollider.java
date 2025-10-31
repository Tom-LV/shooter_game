package Engine.Physics;

import Engine.GameObject;
import Engine.Vector2;

import java.awt.*;

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
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.drawOval((int) (parent.position.x - radius), (int) (parent.position.y - radius), (int) (radius / 2.0f), (int) (radius / 2.0f));
    }

    @Override
    public void checkCollision(Collider other) {
        if (other instanceof CircleCollider c) {
            Vector2 distance = c.parent.position.subtract(parent.position);
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
            Vector2 circleLocal = parent.position.subtract(rect.parent.position);
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
