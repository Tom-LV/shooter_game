package GameObjects.UI;

import Engine.GameObject;
import Engine.Vector2;
import GameObjects.Player;

/**
 * Healthbar gameObject that displays the player health above the player.
 */
public class HealthBar extends GameObject {
    
    Player player;

    public HealthBar(Player player) {
        this.player = player;
    }

    @Override
    public void setup() {
        setSprite("health");
        scale = new Vector2(0.25f, 0.25f);
        setLayer(10);
    }

    @Override
    public void update(float deltaTime) {
        position = player.position.add(new Vector2(-25, -20));
        scale.x = (player.getHealth() / (float) player.getMaxHealth()) * 0.25f;
    }
}
