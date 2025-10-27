package Engine.Physics;

import Engine.GameObject;
import Engine.Vector2;

import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class Collider {
    ColliderType type = ColliderType.None;
    GameObject parent;

    ArrayList<Consumer<Collider>> collisionEnterListeners = new ArrayList<>();
    ArrayList<Consumer<Collider>> collisionExitListeners = new ArrayList<>();
    ArrayList<Consumer<Collider>> collisionListeners = new ArrayList<>();

    ArrayList<Collider> collisions = new ArrayList<>();
    ArrayList<Collider> newCollisions = new ArrayList<>();

    Vector2 newPosition = new Vector2(0, 0);

    public void update() {
        for (Collider collider : newCollisions) {
            callOnCollision(collider);
            if (!collisions.contains(collider)) {
                callOnCollisionEnter(collider);
                collisions.add(collider);
            }
        }
        for (int i = collisions.size() - 1; i >= 0 ; i--) {
            Collider collider = collisions.get(i);
            if (!newCollisions.contains(collider)) {
                callCollisionExit(collider);
                collisions.remove(collider);
            }
        }
        newCollisions.clear();
        parent.position = parent.position.add(newPosition);
        newPosition = new Vector2(0, 0);
    }

    public void setParent(GameObject parent) {
        this.parent = parent;
    }

    public GameObject getParent() {
        return this.parent;
    }

    public abstract void checkCollision(Collider other);

    private void callOnCollision(Collider collider) {
        for (Consumer<Collider> collisionListener : collisionListeners) {
            collisionListener.accept(collider);
        }
    }

    private void callOnCollisionEnter(Collider collider) {
        for (Consumer<Collider> collisionListener : collisionEnterListeners) {
            collisionListener.accept(collider);
        }
    }

    private void callCollisionExit(Collider collider) {
        for (Consumer<Collider> collisionListener : collisionExitListeners) {
            collisionListener.accept(collider);
        }
    }

    public void clearListeners() {
        collisionListeners.clear();
        collisionExitListeners.clear();
        collisionEnterListeners.clear();
    }

    public void onCollisionEnter(Consumer<Collider> onCollisionEnter) {
        collisionEnterListeners.add(onCollisionEnter);
    }

    public void onCollisionExit(Consumer<Collider> onCollisionExit) {
        collisionExitListeners.remove(onCollisionExit);
    }

    public void onCollision(Consumer<Collider> onCollision) {
        collisionListeners.add(onCollision);
    }

    protected void collided(Collider other) {
        if (!newCollisions.contains(other)) {
            newCollisions.add(other);
        }
    }
}
