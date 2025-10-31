package GameObjects.Loot;

import Engine.Engine;
import Engine.GameObject;
import Engine.Inputs.Input;
import Engine.Networking.NetEvent;
import Engine.Vector2;
import Engine.Camera;

import java.awt.*;
import java.awt.event.KeyEvent;

public class InteractIndicator extends GameObject {
    private static InteractIndicator indicator;
    private boolean hidden = true;
    private float maxTimer;
    private float timer;
    private boolean interacting = false;

    public InteractIndicator() {
        indicator = this;
        setLayer(1000);
    }

    @NetEvent("show_indicator")
    public static void showIndicator(Vector2 pos, float timer) {
        indicator.position = pos;
        indicator.show();
        indicator.maxTimer = timer;
        indicator.timer = 0;
    }

    @NetEvent("hide_indicator")
    public static void hideIndicator() {
        indicator.hide();
    }

    public static void startInteraction() {
        indicator.interacting = true;
    }

    public static void stopInteraction() {
        indicator.interacting = false;
        indicator.timer = 0;
    }

    @Override
    public void update(float deltaTime) {
        if (interacting) {
            timer += deltaTime;
            if (timer >= maxTimer) {
                timer = maxTimer;
            }
            if (!Input.isKeyPressed(KeyEvent.VK_E)) {
                interacting = false;
                timer = 0;
            }
        }

        if (!interacting && !hidden) {
            if (Input.isKeyPressed(KeyEvent.VK_E)) {
                interacting = true;
            }
        }
    }

    @Override
    protected void draw(Graphics2D g2d) {
        if (hidden) {
            return;
        }
        Vector2 panelDimensions = new Vector2(Engine.getCurrentScene().getWidth() / 2,
                Engine.getCurrentScene().getHeight() / 2);

        Vector2 panelPos = position.subtract(Camera.currentCamera.position).divide(Camera.currentCamera.zoom);
        panelPos = panelPos.add(panelDimensions);
        panelPos = panelPos.add(new Vector2(-15, -30));
        g2d.setStroke(new BasicStroke(8));
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawArc((int) panelPos.x, (int) panelPos.y, 30, 30, -30, 240);
        g2d.setColor(Color.WHITE);
        g2d.drawArc((int) panelPos.x, (int) panelPos.y, 30, 30, 210, (int) -((240 * timer / maxTimer)));
    }

    void show() {
        hidden = false;
    }

    void hide() {
        hidden = true;
    }
}
