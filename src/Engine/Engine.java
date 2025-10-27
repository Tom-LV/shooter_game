package Engine;

import Engine.Inputs.Input;
import Engine.Networking.Client;
import Engine.Networking.Server;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.time.Duration;
import java.time.Instant;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * An engine class to manage the whole game state.
 */
public class Engine {
    private static Scene currentScene;
    private static JFrame jFrame;
    private static boolean running;
    private static Duration deltaTime = Duration.ZERO;
    private static Instant beginTime = Instant.now();
    private static final Input input = new Input();

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

    public static float getDeltaTime() {
        return Engine.deltaTime.toNanos() / 1_000_000_000f;

    }

    /**
     * Updates game state.
     */
    public static void update() {
        Server.update((float) Engine.getDeltaTime());
        Client.update((float) Engine.getDeltaTime());

        currentScene.update((float) Engine.getDeltaTime());

        deltaTime = Duration.between(beginTime, Instant.now());
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
        try {
            Server.start(port);
            return runClient("localhost", port);
        } catch (SocketException e) {
            return false;
        }
    }

    /**
     * Joins a hosted game.
     * @param host host ip
     * @param port port
     * @return true if joined the host
     */
    public static boolean runClient(String host, int port) {
        try {
            Client.connect(host, port);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Destroy given GameObject in currently opened scene.
     */
    public static void destroy(GameObject gameObject) {
        getCurrentScene().destroyNetworkObject(gameObject);
    }

    public static void addObject(GameObject gameObject) {
        getCurrentScene().addNetworkObject(gameObject);
    }


}
