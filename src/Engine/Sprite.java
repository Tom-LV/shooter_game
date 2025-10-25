package Engine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.imageio.ImageIO;

/**
 * Class for managing images for gameObject rendering.
 */
public class Sprite {
    Vector2 pivot;
    BufferedImage image;
    int index;

    /**
     * Creates a new sprite with given pivot point.
     * @param image sprite image
     * @param pivot image pivot
     * @param index sprite index
     */
    Sprite(BufferedImage image, Vector2 pivot, int index) {
        this.image = image;
        if (pivot == null) {
            this.pivot = new Vector2(0.5f, 0.5f);
        }
        this.pivot = pivot;
        this.index = index;
    }

    public BufferedImage getImage() {
        return image;
    }

    public Vector2 getPivot() {
        return pivot;
    }

    int getIndex() {
        return index;
    }
    
    static HashMap<Integer, Sprite> sprites = new HashMap<>();
    static HashMap<String, Integer> spriteIndexMap = new HashMap<>();

    /**
     * Loads image and stores it in cache for multiple uses.
     * @param name sprite name
     * @return bufferedImage
     */
    public static Sprite getSprite(String name) {
        if (!spriteIndexMap.containsKey(name)) {
            return null;
        }

        int index = spriteIndexMap.get(name);
        return sprites.get(index);
    }

    /**
     * Gets sprite from map with index.
     * @param index sprite index
     * @return sprite
     */
    public static Sprite getSpriteFromIndex(int index) {
        if (!sprites.containsKey(index)) {
            return null;
        }
        return sprites.get(index);
    }

    /**
     * Loads an image from path and creates a sprite for it.
     * @param name sprite name
     * @param path image path
     * @param pivot image pivot
     */
    public static void loadImage(String name, String path, Vector2 pivot) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            if (image != null) {
                int index = spriteIndexMap.size();
                sprites.put(index, new Sprite(image, pivot, index));
                spriteIndexMap.put(name, index);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
