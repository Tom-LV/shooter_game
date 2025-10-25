package Engine;

import Engine.Inputs.Input;
import Engine.Networking.Client;
import Engine.Networking.NetMessage;
import Engine.Networking.Server;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * An engine class to manage the whole game state.
 */
public class Engine {
    private static Server server;
    private static Client client;
    private static Scene currentScene;
    private static JFrame jFrame;
    private static boolean running;
    private static Duration deltaTime = Duration.ZERO;
    private static Duration tick = Duration.ZERO;
    private static Instant beginTime = Instant.now();
    private static Input input = new Input();

    /**
     * Setup engine.
     */
    public static void start() {
        jFrame = new JFrame();
        jFrame.setSize(new Dimension(500, 600));
        jFrame.setMinimumSize(new Dimension(400, 400));
        jFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int minW = jFrame.getMinimumSize().width;
                int minH = jFrame.getMinimumSize().height;

                int w = jFrame.getWidth();
                int h = jFrame.getHeight();

                boolean changed = false;

                if (w < minW) {
                    w = minW;
                    changed = true;
                }
                if (h < minH) {
                    h = minH;
                    changed = true;
                }

                if (changed) {
                    jFrame.setSize(w, h);
                }
            }
        });

        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.addKeyListener(input);
        
        new Camera();
        running = true;
    }

    public static void showWindow() {
        jFrame.setVisible(true);
    }

    /**
     * Sets the window icon.
     * @param path image to icon
     */
    public static void setWindowIcon(String path) {
        try {
            Image image = (Image) ImageIO.read(new File(path));
            ImageIcon icon = new ImageIcon();
            icon.setImage(image);
            jFrame.setIconImage(icon.getImage());
        } catch (IOException e) {
            System.err.println("Couldn't set icon!");
        }
    }

    public static void setWindowName(String name) {
        jFrame.setTitle(name);
    }

    public static Scene getCurrentScene() {
        return Engine.currentScene;
    }

    public static boolean isRunning() {
        return running;
    }

    public static float getDeltaTIme() {
        return Engine.deltaTime.toNanos() / 1_000_000_000f;
    }

    public static Client getClient() {
        return client;
    }

    public static Server getServer() {
        return server;
    }

    public static boolean isClientRunning() {
        return client != null;
    }

    public static boolean isServerRunning() {
        return server != null;
    }

    /**
     * Updates client server received objects.
     */
    private static void clientNetworkUpdate() {
        if (Engine.client != null) {
            Engine.client.sendGameObjects(getCurrentScene().getGameObjects());

            // receiving server objects
            ArrayList<GameObject> serverObjects = Engine.client.gameObjects;
            ArrayList<GameObject> localObjects = getCurrentScene().getServerObject();
            for (GameObject gameObject : serverObjects) {
                boolean found = false;
                for (GameObject clientObject : localObjects) {
                    if (gameObject.equals(clientObject)) {
                        clientObject.updateValues(gameObject.position, 
                            gameObject.scale, 
                            gameObject.rotation,
                            gameObject.currentSprite != null ? gameObject.currentSprite.index : -1);
                        clientObject.setLayer(gameObject.getLayer());
                        found = true;
                        break;
                    }
                }
                if (found) {
                    continue;
                }

                if (!getCurrentScene().getGameObjects().contains(gameObject)) {
                    Engine.getCurrentScene().addServerObject(gameObject);
                }
            }

            for (int i = 0; i < localObjects.size(); i++) {
                if (!serverObjects.contains(localObjects.get(i))) {
                    Engine.getCurrentScene().destroyServerObject(localObjects.get(i));
                }
            }

            // Removing ack messages
            ArrayList<Integer> ackMessages = Engine.client.getAcknowledgedMessages();
            ArrayList<NetMessage> messages = Engine.client.getMessages();
            for (int i = 0; i < messages.size(); i++) {
                if (ackMessages.contains(messages.get(i).getId())) {
                    messages.get(i).setAcknowledged(true);
                    messages.remove(i);
                    i--;
                }
            }
        }
    }

    /**
     * Updates game state.
     */
    public static void update() {
        if (tick.toMillis() >= 16) {
            Engine.clientNetworkUpdate();
            tick = Duration.ZERO;
        }
        
        if (Engine.server != null) {
            Engine.server.update(Engine.getDeltaTIme());
        }

        currentScene.update(Engine.getDeltaTIme());
        

        deltaTime = Duration.between(beginTime, Instant.now());
        tick = tick.plus(deltaTime);
        beginTime = Instant.now();
    }

    /**
     * Replace the current scene.
     * @param scene scene
     */
    public static void changeScene(Scene scene) {
        if (getCurrentScene() != null) {
            Engine.getCurrentScene().removeMouseListener(input);
            Engine.getCurrentScene().removeMouseMotionListener(input);
            Engine.jFrame.remove(Engine.getCurrentScene());
        }
        
        Engine.currentScene = scene;
        
        Engine.jFrame.add(scene);
        scene.addMouseMotionListener(input);
        scene.addMouseListener(input);
        Engine.jFrame.validate();
        Engine.jFrame.requestFocus();
    }

    /**
     * Setup server for game. The host also joins as a client.
     * @param port port
     * @return true if successful server is made
     */
    public static boolean runServer(int port) {
        Engine.server = new Server();
        try {
            Engine.server.startServer(port);
        } catch (Exception e) {
            Engine.server = null;
            return false;
        }

        return runClient("localhost", port);
    }

    /**
     * Joins a hosted game.
     * @param host host ip
     * @param port port
     * @return true if joined the host
     */
    public static boolean runClient(String host, int port) {
        Engine.client = new Client();
        try {
            Engine.client.connect(host, port);
        } catch (Exception e) {
            Engine.client = null;
            return false;
        }
        return true;
    }

    /**
     * Destroy given GameObject in currently opened scene.
     */
    public static void destroy(GameObject gameObjecet) {
        getCurrentScene().destroyObject(gameObjecet);
    }

    public static void addObject(GameObject gameObject) {
        getCurrentScene().addObject(gameObject);
    }


}
