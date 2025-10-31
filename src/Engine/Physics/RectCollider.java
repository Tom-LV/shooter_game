package Engine.Physics;

import Engine.Vector2;

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
}
