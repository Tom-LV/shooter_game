package Engine;

/**
 * A camera for scenes.
 */
public class Camera {
    public static Camera currentCamera;

    public Vector2 position = new Vector2(0f, 0f);

    Camera() {
        currentCamera = this;
    }
}