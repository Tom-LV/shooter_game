package Engine;

import Engine.Networking.Client;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.*;

/**
 * A scene class that can be extended to act as the main game panel.
 */
public abstract class Scene extends JPanel {
    private final ArrayList<GameObject> networkObjects = new ArrayList<>();
    private final ArrayList<GameObject> serverObjects = new ArrayList<>();
    private final ArrayList<GameObject> localObjects = new ArrayList<>();

    private final ConcurrentLinkedQueue<Runnable> pendingUpdateActions = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Runnable> pendingDrawActions = new ConcurrentLinkedQueue<>();

    ArrayList<GameObject> drawOrder = new ArrayList<>();

    /**
     * Set scene layout and call setupScene.
     */
    public Scene() {
        Client.onServerObjectAdded(this::addServerObject);
        Client.onServerObjectRemoved(this::destroyServerObject);
        this.setLayout(null);
        setupScene();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        draw(g);
    }

    /**
     * Gets a list of local gameObjects of class.
     * @param cls class to get
     * @return list of local gameObjects
     */
    public synchronized List<GameObject> getObjectsOfClass(Class<?> cls) {
        return networkObjects.stream()
            .filter(o -> o.isOfClass(cls))
            .toList();
    }

    /**
     * Return a list of server gameObjects of class.
     * @param cls class of gameObject
     * @return list of gameObjects
     */
    public synchronized List<GameObject> getServerObjectOfClass(Class<?> cls) {
        return serverObjects.stream()
            .filter(o -> o.isOfClass(cls))
            .toList();
    }

    // Should be implemented with binary search
    private void updateLayer(GameObject gameObject) {
        if (!drawOrder.contains(gameObject)) {
            return;
        }

        boolean added = false;
        for (int j = 0; j < drawOrder.size(); j++) {
            GameObject other = drawOrder.get(j);
            if (gameObject.getLayer() <= other.getLayer()) {
                drawOrder.remove(gameObject);
                drawOrder.add(j, gameObject);
                added = true;
                break;
            }
        }
        if (!added) {
            drawOrder.remove(gameObject);
            drawOrder.add(gameObject);
        }
    }

    private void draw(Graphics g) {
        Runnable task;
        while ((task = pendingDrawActions.poll()) != null) {
            try {
                task.run();
            } catch (Throwable t) {
                System.err.println("Error while applying pending draw action: " + t.getMessage());
            }
        }

        for (GameObject gameObject : drawOrder) {
            if (gameObject == null) {
                continue;

            }
            gameObject.draw((Graphics2D) g);
        }
    }

    /**
     * Internal method to call update function for all scene gameObjects.
     */
    void update(float deltaTime) {
        for (GameObject gameObject : networkObjects) {
            gameObject.animationUpdate(deltaTime);
            gameObject.update(deltaTime);
            if (gameObject.needsLayerChange) {
                addToLayerChange(gameObject);
            }
        }
        for (GameObject gameObject : localObjects) {
            gameObject.animationUpdate(deltaTime);
            gameObject.update(deltaTime);
            if (gameObject.needsLayerChange) {
                addToLayerChange(gameObject);
            }
        }

        for (GameObject gameObject : serverObjects) {
            gameObject.serverObjectInterpolation(deltaTime);
            if (gameObject.needsLayerChange) {
                addToLayerChange(gameObject);
            }
        }

        Runnable task;
        while ((task = pendingUpdateActions.poll()) != null) {
            try {
                task.run();
            } catch (Throwable t) {
                System.err.println("Error while applying pending update action: " + t.getMessage());
            }
        }

        repaint();
    }

    public abstract void setupScene();

    private void addToLayerChange(GameObject gameObject) {
        gameObject.needsLayerChange = false;
        pendingDrawActions.add(() -> {
            updateLayer(gameObject);
        });

    }


    private void addDrawOrder(GameObject gameObject) {
        pendingDrawActions.add(() -> {
            if (drawOrder.contains(gameObject)) {
                return;
            }
           drawOrder.add(gameObject);
           updateLayer(gameObject);
        });
    }

    private void removeDrawOrder(GameObject gameObject) {
        pendingDrawActions.add(() -> {
            drawOrder.remove(gameObject);
        });
    }

    public void addObject(GameObject gameObject) {
        pendingUpdateActions.add(() -> {
            if (localObjects.contains(gameObject)) {
                return;
            }
            gameObject.setOwnerUUID(Client.getClientId());
            gameObject.setLayer(gameObject.getLayer());
            gameObject.setGameObjectType(GameObjectType.Local);
            gameObject.setup();
            localObjects.add(gameObject);
            addDrawOrder(gameObject);
        });
    }

    void destroyObject(GameObject gameObject) {
        pendingUpdateActions.add(() -> {
            if (!localObjects.contains(gameObject)) {
                return;
            }
            gameObject.onDestroy();
            localObjects.remove(gameObject);
            removeDrawOrder(gameObject);
            gameObject.cleanUp();
        });
    }

    /**
     * Add a gameObject to scene.
     * @param gameObject object to add
     */
    public void addNetworkObject(GameObject gameObject) {
        pendingUpdateActions.add(() -> {
            if (networkObjects.contains(gameObject)) {
                return;
            }
            gameObject.setOwnerUUID(Client.getClientId());
            gameObject.setLayer(gameObject.getLayer());
            gameObject.setGameObjectType(GameObjectType.Client);
            gameObject.setup();
            networkObjects.add(gameObject);
            addDrawOrder(gameObject);
            Client.addObject(gameObject);

        });
    }

    /**
     * Internal method for destroying scene gameObjects.
     * @param gameObject gameObject that is scene
     */
    void destroyNetworkObject(GameObject gameObject) {
        pendingUpdateActions.add(() -> {
            if (!networkObjects.contains(gameObject)) {
                return;
            }
            gameObject.onDestroy();
            networkObjects.remove(gameObject);
            removeDrawOrder(gameObject);
            Client.removeObject(gameObject);
            gameObject.cleanUp();
        });
    }


    /**
     * Adds an object that is received from the server.
     * @param gameObject gameObject
     */
    protected void addServerObject(GameObject gameObject) {
        pendingUpdateActions.add(() -> {
            gameObject.setGameObjectType(GameObjectType.Ghost);
           serverObjects.add(gameObject);
           addDrawOrder(gameObject);
        });
    }

    /**
     * Destroys an object that is from the server.
     * @param gameObject gameObject
     */
    protected void destroyServerObject(GameObject gameObject) {
        pendingUpdateActions.add(() -> {
            serverObjects.remove(gameObject);
            removeDrawOrder(gameObject);
            gameObject.cleanUp();
        });
    }
}
