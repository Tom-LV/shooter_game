package Engine.Inputs;

import Engine.Vector2;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;

/**
 * Input class for engine.
 */
public class Input implements KeyListener, MouseListener, MouseMotionListener {



    private static HashMap<Integer, Boolean> keyMap = new HashMap<Integer, Boolean>();
    public static Mouse mouse = new Mouse();


    public static void update() {

    }


    /**
     * Check if the given key is pressed.
     * @param keyCode KeyCode
     * @return true if the given key is pressed
     */
    public static boolean isKeyPressed(int keyCode) {
        if (!keyMap.containsKey(keyCode)) {
            return false;
        }
        return keyMap.get(keyCode);
    }

    // public boolean keyReleased(int keyCode) {
    //     return false;
    // }

    // public boolean wasKeyPressedThisFrame(int keyCode) {
    //     return false;
    // }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (!keyMap.containsKey(keyCode)) {
            keyMap.put(keyCode, true);
        }
        keyMap.replace(keyCode, true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (!keyMap.containsKey(keyCode)) {
            keyMap.put(keyCode, false);
        }
        keyMap.replace(keyCode, false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Not used
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == 0) {
            return;
        }
        mouse.clicked[e.getButton() - 1] = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == 0) {
            return;
        }
        mouse.clicked[e.getButton() - 1] = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Not used
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Not used
    }


    @Override
    public void mouseDragged(MouseEvent e) {
        mouse.position = new Vector2(e.getX(), e.getY());
    }


    @Override
    public void mouseMoved(MouseEvent e) {
        mouse.position = new Vector2(e.getX(), e.getY());
    }

    
}
