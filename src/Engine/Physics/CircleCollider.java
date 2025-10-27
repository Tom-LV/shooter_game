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
            float circleSum = c.radius * radius;
            if (distance.length() < circleSum) {
                collided(other);
                if (type != ColliderType.Dynamic) {
                    return;
                }
                if (other.type == ColliderType.None) {
                    return;
                }
                newPosition = newPosition.subtract(distance.normalize().multiply(circleSum - distance.length()));

            }
        }
    }
}
