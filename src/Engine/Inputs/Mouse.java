package Engine.Inputs;

import Engine.Camera;
import Engine.Engine;
import Engine.Vector2;

/**
 * A mouse class to manage current mouse behavour.
 */
public class Mouse {
    Vector2 position = new Vector2(0, 0);
    boolean[] clicked = new boolean[3];

    /**
     * Gets mouse position in the game world.
     * @return mouse position in the game world
     */
    public Vector2 getWorldPosition() {
        float halfWidth = Engine.getCurrentScene().getWidth() / 2;
        float halfHeight = Engine.getCurrentScene().getHeight() / 2;
        Vector2 panelDimensions = new Vector2(halfWidth, halfHeight);
        return position.subtract(panelDimensions).add(Camera.currentCamera.position);
    }

    public Vector2 getPosistion() {
        return position;
    }

    /**
     * Check if button is clicked.
     * 0 - left mouse button
     * 1 - middle mouse button
     * 2 - right mouse button
     * @param button mouse button index
     * @return if the given mouse button index is pressed
     */
    public boolean isClicked(int button) {
        if (button < 0 || button > 2) {
            return false;
        }
        return clicked[button];
    }
}
