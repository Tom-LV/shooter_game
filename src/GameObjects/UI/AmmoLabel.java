package GameObjects.UI;

import Engine.GameObject;
import Engine.Vector2;
import GameObjects.Player;

import java.awt.*;

public class AmmoLabel extends GameObject {

    Player player;
    int ammo = 0;

    @Override
    public void setup() {
        position = new Vector2(45, 5);
    }

    public AmmoLabel(Player player) {
        this.player = player;
        ammo = player.getAmmo();
    }

    @Override
    public void update(float deltaTime) {
        ammo = player.getAmmo();
    }

    @Override
    protected void draw(Graphics2D g2d) {
        g2d.setColor(Color.white);
        g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        g2d.drawString("Ammo: " + ammo, (int) position.x + 20, (int) position.y + 30);
    }
}
