package Engine.Physics;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PhysicsManager {
    private final ArrayList<Collider> colliders = new ArrayList<>();
    private final ConcurrentLinkedQueue<Runnable> pendingPhysicsActions = new ConcurrentLinkedQueue<>();

    public void drawColliders(Graphics2D g) {
        for (int i = 0; i < colliders.size(); i++) {
            Collider c = colliders.get(i);
            c.draw(g);
        }
    }

    public void physicsUpdate() {
        Runnable task;
        while ((task = pendingPhysicsActions.poll()) != null) {
            try {
                task.run();
            } catch (Throwable t) {
                System.err.println("Error while applying pending physics action: " + t.getMessage());
            }
        }
        for (int i = 0; i < colliders.size(); i++) {
            for (Collider collider2 : colliders) {
                if (collider2 != colliders.get(i)) {
                    colliders.get(i).checkCollision(collider2);
                }
            }
        }

        for (Collider collider : colliders) {
            collider.update();
        }
    }

    public void addCollider(Collider collider) {
        pendingPhysicsActions.add(() -> {
            if (colliders.contains(collider)) {
                return;
            }
            colliders.add(collider);
        });
    }

    public void removeCollider(Collider collider) {
        pendingPhysicsActions.add(() -> {
            colliders.remove(collider);
        });
    }
}
