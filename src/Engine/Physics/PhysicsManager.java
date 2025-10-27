package Engine.Physics;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PhysicsManager {
    private static final ArrayList<Collider> colliders = new ArrayList<>();
    private static final ConcurrentLinkedQueue<Runnable> pendingPhysicsActions = new ConcurrentLinkedQueue<>();

    public static void physicsUpdate() {
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

    public static void addCollider(Collider collider) {
        pendingPhysicsActions.add(() -> {
            colliders.add(collider);
        });
    }

    public static void removeCollider(Collider collider) {
        pendingPhysicsActions.add(() -> {
            colliders.remove(collider);
        });
    }
}
