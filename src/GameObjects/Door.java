package GameObjects;

import Engine.Camera;
import Engine.Engine;
import Engine.GameObject;
import Engine.Physics.ColliderType;
import Engine.Physics.RectCollider;
import Engine.Vector2;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class Door extends GameObject {
    Vector2 pivot;

    public Door(int x, int y, int width, float rotation, boolean leftSide) {
        position = new Vector2(x, y);
        this.width =  width;
        this.rotation = rotation;
        if (leftSide) {
            pivotPosition = new Vector2(0, 0);
        }else {
            pivotPosition = new Vector2(0, 1);
        }
        pivot = pivotPosition;
        position = position.subtract(new Vector2(width, 0f).rotate(rotation));
    }

    @Override
    protected void draw(Graphics2D g2d) {
        float w = width / Camera.currentCamera.zoom;
        float h = 10 / Camera.currentCamera.zoom;

        g2d.setStroke(new BasicStroke(1));
        AffineTransform old = g2d.getTransform();

        Vector2 panelDimensions = new Vector2(Engine.getCurrentScene().getWidth() / 2,
                Engine.getCurrentScene().getHeight() / 2);

        Vector2 panelPos = position.subtract(Camera.getRenderPosition());
        panelPos = panelPos.divide(Camera.currentCamera.zoom);
        panelPos = panelPos.add(panelDimensions);

        g2d.translate(panelPos.x, panelPos.y);

        g2d.rotate(Math.toRadians(rotation));


        // Draw collider relative to pivot
        float drawX = -(w * pivot.x);
        float drawY = -(h * pivot.y);

        g2d.setColor(Color.gray);
        g2d.fillRect(Math.round(drawX), Math.round(drawY), Math.round(w), Math.round(h));

        // Reset to original transform
        g2d.setTransform(old);
    }

    int width;
    Vector2 pivotPosition;
    @Override
    public void setup() {
        addCollider(new RectCollider(new Vector2(width, 10f), pivotPosition, ColliderType.Static));

    }
}
