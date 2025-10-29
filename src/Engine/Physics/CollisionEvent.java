package Engine.Physics;

import Engine.Vector2;

public class CollisionEvent {
    Collider otherCollider;
    Vector2 normal;
    public CollisionEvent(Collider other, Vector2 normal) {
        this.otherCollider = other;
        this.normal = normal;
    }

    public Collider getOther() {
        return otherCollider;
    }

    public Vector2 getNormal() {
        return normal;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CollisionEvent other)) return false;
        return otherCollider.equals(other.otherCollider);
    }

    @Override
    public int hashCode() {
        return otherCollider.hashCode();
    }
}
