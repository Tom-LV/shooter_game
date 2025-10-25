package Engine;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;

/**
 * A scene class that can be extended to act as the main game panel.
 */
public abstract class Scene extends JPanel {
    ArrayList<GameObject> gameObjects = new ArrayList<GameObject>();
    ArrayList<GameObject> serverObjects = new ArrayList<GameObject>();


    ArrayList<GameObject> toAddObject = new ArrayList<>();
    ArrayList<GameObject> toRemoveObject = new ArrayList<>();

    ArrayList<GameObject> toDrawOrder = new ArrayList<>();
    ArrayList<GameObject> removeDrawOrder = new ArrayList<>();
    ArrayList<GameObject> layerChange = new ArrayList<>();

    ArrayList<GameObject> drawOrder = new ArrayList<>();
    


    /**
     * Set scene layout and call setupScene.
     */
    public Scene() {
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
     * @return list of lical gameObjects
     */
    public synchronized List<GameObject> getObjectsOfClass(Class<?> cls) {
        return gameObjects.stream()
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
    private void layerChange() {
        for (int i = 0; i < layerChange.size(); i++) {
            GameObject gameObject = layerChange.get(i);
            if (!drawOrder.contains(gameObject)) {
                continue;
            }
            if (gameObject == null) {
                continue;
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

        layerChange = new ArrayList<>();
    }

    private void draw(Graphics g) {
        for (GameObject gameObject : toDrawOrder) {
            if (!drawOrder.contains(gameObject)) {
                drawOrder.add(gameObject);
            }
        }
        toDrawOrder.clear();

        for (GameObject gameObject : removeDrawOrder) {
            if (drawOrder.contains(gameObject)) {
                drawOrder.remove(gameObject);
            }
        }
        removeDrawOrder.clear();
        layerChange();
        for (int i = 0; i < drawOrder.size(); i++) {
            if (drawOrder.get(i) == null) {
                continue;

            }
            drawOrder.get(i).draw((Graphics2D) g);
        }
    }

    /**
     * Internal method to call update function for all scene gameObjects.
     */
    void update(float deltaTime) {

        for (Iterator<GameObject> it = gameObjects.iterator(); it.hasNext();) {
            GameObject gameObject = it.next();
            gameObject.animationUpdate(deltaTime);
            gameObject.update(deltaTime);
            if (gameObject.needsLayerChange) {
                addToLayerChange(gameObject);
            }
        }

        for (Iterator<GameObject> it = serverObjects.iterator(); it.hasNext();) {
            GameObject gameObject = it.next();
            gameObject.serverObjectInterpolation(deltaTime);
            if (gameObject.needsLayerChange) {
                addToLayerChange(gameObject);
            }
        }

        for (GameObject gameObject : toAddObject) {
            if (!gameObjects.contains(gameObject)) {
                gameObject.setLayer(gameObject.getLayer());
                gameObjects.add(gameObject);
                addDrawOrder(gameObject);
            }
        }
        toAddObject.clear();

        for (GameObject gameObject : toRemoveObject) {
            if (gameObjects.contains(gameObject)) {
                gameObject.onDestroy();
                gameObjects.remove(gameObject);
                removeDrawOrder(gameObject);
            }
        }
        toRemoveObject.clear();

        repaint();
    }

    public abstract void setupScene();

    private synchronized void addToLayerChange(GameObject gameObject) {
        if (layerChange.contains(gameObject)) {
            return;
        }
        gameObject.needsLayerChange = false;
        layerChange.add(gameObject);
    }


    private synchronized void addDrawOrder(GameObject gameObject) {
        toDrawOrder.add(gameObject);
    }

    private synchronized void removeDrawOrder(GameObject gameObject) {
        removeDrawOrder.add(gameObject);
    }

    /**
     * Add a gameObject to scene.
     * @param gameObject object to add
     */
    public synchronized void addObject(GameObject gameObject) {
        gameObject.setOwnerUUID(Engine.getClient().getClientId());
        toAddObject.add(gameObject);
    }

    /**
     * Internal method for destroying scene gameObjects.
     * @param gameObject gameObject that is scene
     */
    synchronized void destroyObject(GameObject gameObject) {
        toRemoveObject.add(gameObject);
    }


    /**
     * Adds an object that is received from the server.
     * @param gameObject gameObject
     */
    protected void addServerObject(GameObject gameObject) {
        if (serverObjects.contains(gameObject) || gameObject == null) {
            return;
        }
        gameObject.setLayer(gameObject.getLayer());
        serverObjects.add(gameObject);
        addDrawOrder(gameObject);
    }

    /**
     * Destroys an object that is from the server.
     * @param gameObject gameObject
     */
    protected void destroyServerObject(GameObject gameObject) {
        if (serverObjects.contains(gameObject)) {
            removeDrawOrder(gameObject);
            gameObject.onDestroy();
            serverObjects.remove(gameObject);
        }
    }

    public ArrayList<GameObject> getGameObjects() {
        return gameObjects;
    }

    protected ArrayList<GameObject> getServerObject() {
        return serverObjects;
    }
}
