package Engine;

/**
 * A camera for scenes.
 */
public class Camera {
    public static Camera currentCamera;
    public static Vector2 renderPos;

    public Vector2 position = new Vector2(0f, 0f);
    public float zoom = 1f;

    Camera() {
        currentCamera = this;
    }

    public static void setRenderPos() {
        renderPos = currentCamera.position;
    }

    public static Vector2 getRenderPosition() {
        return renderPos;
    }
}