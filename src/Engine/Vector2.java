package Engine;

/**
 * A vector2 class.
 */
public class Vector2 {
    public float x;
    public float y;

    /**
     * Creates a new 2d vector.
     * @param x x values of the vector
     * @param y y values of the vector
     */
    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2 add(Vector2 other) {
        return new Vector2(x + other.x, y + other.y);
    }

    public Vector2 subtract(Vector2 other) {
        return new Vector2(x - other.x, y - other.y);
    }

    public Vector2 multiply(float scalar) {
        return new Vector2(x * scalar, y * scalar);
    }

    public Vector2 divide(float scalar) {
        return new Vector2(x / scalar, y / scalar);
    }

    /**
     * Rotates the vector by the given degrees from (0, 0).
     * @param rotation rotation in degrees
     * @return rotated vector2
     */
    public Vector2 rotate(float rotation) {
        double rotationRad = (double) Math.toRadians(rotation);
        float xOffset = (float) (Math.cos(rotationRad) * x - Math.sin(rotationRad) * y);
        float yOffset = (float) (Math.sin(rotationRad) * x + Math.cos(rotationRad) * y);
        return new Vector2(xOffset, yOffset);
    }

    public float dot(Vector2 other) {
        return x * other.x + y * other.y;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public float getRotation() {
        return (float) Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Returns a vector with length 1, if the vector is (0, 0) returns the same vector.
     * @return A vector with length 1
     */
    public Vector2 normalize() {
        float len = length();
        if (len != 0) {
            return new Vector2(x / len, y / len);
        } else {
            return new Vector2(0, 0);
        }
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public static Vector2 fromRotation(float roation) {
        return new Vector2((float) Math.cos(roation), (float) Math.sin(roation));
    }
}