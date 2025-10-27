package GameObjects;

import Engine.Engine;
import Engine.GameObject;
import Engine.Vector2;
import Engine.Camera;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Background grass.
 */
public class Background extends GameObject {
    private final Image bgImage = new ImageIcon("src/Assets/art/city.png").getImage();
    @Override
    protected void setup() {
        setSprite("city");
        scale = new Vector2(0.4f, 0.4f);
        setLayer(-100);
    }
}
