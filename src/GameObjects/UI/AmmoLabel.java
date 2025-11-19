package GameObjects.UI;

import Engine.GameObject;
import Engine.Inputs.Input;
import Engine.Vector2;
import GameObjects.Player;

import java.awt.*;

public class AmmoLabel extends GameObject {

    int ammo = 0;
    int bolts = 0;

    @Override
    public void setup() {
        position = new Vector2(45, 5);
    }

    public AmmoLabel() {
        ammo = Player.getInstance().getAmmo();
        bolts = Player.getInstance().getBolts();
    }

    @Override
    public void update(float deltaTime) {
        ammo = Player.getInstance().getAmmo();
        bolts = Player.getInstance().getBolts();
    }

    @Override
    protected void draw(Graphics2D g2d) {
        g2d.setColor(Color.white);
        g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        g2d.drawString("Ammo: " + ammo + " Bolts: " + bolts, (int) position.x + 20, (int) position.y + 30);
    }
}
