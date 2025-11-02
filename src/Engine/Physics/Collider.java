package Engine.Physics;

import Engine.GameObject;
import Engine.Vector2;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class Collider {
    ColliderType type = ColliderType.None;
    GameObject parent;
    Vector2 position = new Vector2(0, 0);

    ArrayList<Consumer<CollisionEvent>> collisionEnterListeners = new ArrayList<>();
    ArrayList<Consumer<CollisionEvent>> collisionExitListeners = new ArrayList<>();
    ArrayList<Consumer<CollisionEvent>> collisionListeners = new ArrayList<>();

    Set<CollisionEvent> collisions = new HashSet<>();
    Set<CollisionEvent> newCollisions = new HashSet<>();

    Vector2 newPosition = new Vector2(0, 0);

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public void draw(Graphics2D g) {
        return;
    }

    public void update() {
        for (CollisionEvent collider : newCollisions) {
            callOnCollision(collider);

            if (!collisions.contains(collider)) {
                callOnCollisionEnter(collider);
            }
        }

        for (CollisionEvent collider : collisions) {
            if (!newCollisions.contains(collider)) {
                callCollisionExit(collider);
            }
        }

        collisions.clear();
        collisions.addAll(newCollisions);
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

    private void callOnCollision(CollisionEvent collider) {
        for (Consumer<CollisionEvent> collisionListener : collisionListeners) {
            collisionListener.accept(collider);
        }
    }

    private void callOnCollisionEnter(CollisionEvent collider) {
        for (Consumer<CollisionEvent> collisionListener : collisionEnterListeners) {
            collisionListener.accept(collider);
        }
    }

    private void callCollisionExit(CollisionEvent collider) {
        for (Consumer<CollisionEvent> collisionListener : collisionExitListeners) {

            collisionListener.accept(collider);
        }
    }

    public void clearListeners() {
        collisionListeners.clear();
        collisionExitListeners.clear();
        collisionEnterListeners.clear();
    }

    public void onCollisionEnter(Consumer<CollisionEvent> onCollisionEnter) {
        collisionEnterListeners.add(onCollisionEnter);
    }

    public void onCollisionExit(Consumer<CollisionEvent> onCollisionExit) {
        collisionExitListeners.add(onCollisionExit);
    }

    public void onCollision(Consumer<CollisionEvent> onCollision) {
        collisionListeners.add(onCollision);
    }

    protected void collided(CollisionEvent collisionEvent) {
        newCollisions.add(collisionEvent);
    }
}
