package Engine;

/**
 * A camera for scenes.
 */
public class Camera {
    public static Camera currentCamera;

    public Vector2 position = new Vector2(0f, 0f);
    public float zoom = 1.5f;

    Camera() {
        currentCamera = this;
    }
}