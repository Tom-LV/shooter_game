package Engine;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Class for managing images for gameObject rendering.
 */
public class Sprite {
    Vector2 pivot;
    Vector2 dimensions;
    private final Image image;
    int index;

    /**
     * Creates a new sprite with given pivot point.
     * @param image sprite image
     * @param pivot image pivot
     * @param index sprite index
     */
    Sprite(Image image, Vector2 pivot, int index) {
        this.image = image;
        this.dimensions = new Vector2(image.getWidth(null), image.getHeight(null));
        if (pivot == null) {
            this.pivot = new Vector2(0.5f, 0.5f);
        }
        this.pivot = pivot;
        this.index = index;
    }

    public Image getImage() {
        return image;
    }

    public Vector2 getPivot() {
        return pivot;
    }

    public Vector2 getDimensions() {
        return dimensions;
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
        Image image = new ImageIcon(path).getImage();
        if (image != null) {
            int index = spriteIndexMap.size();
            sprites.put(index, new Sprite(image, pivot, index));
            spriteIndexMap.put(name, index);
        }
    }

    public static void loadImage(String name, String path) {
        Image image = new ImageIcon(path).getImage();
        if (image != null) {
            int index = spriteIndexMap.size();
            sprites.put(index, new Sprite(image, new Vector2(0.5f, 0.5f), index));
            spriteIndexMap.put(name, index);
        }
    }
}
