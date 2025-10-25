package GameObjects;

import Engine.Engine;
import Engine.GameObject;
import Engine.Scene;
import Engine.Vector2;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.ArrayList;

/**
 * Minimap in game display:
 *  - Players: blue dots
 *  - Enemies:  red dots.
 */
public class Minimap extends GameObject {

    private int mapWidth = 180;
    private int mapHeight = 180;
    private int margin = 12;

    /**
     * Creates a minimap.
     */
    public Minimap() {
        setLayer(100000);
        position = new Vector2(0, 0);
        scale = new Vector2(1, 1);
    }

    @Override
    protected void draw(Graphics2D g2d) {
        Scene scene = Engine.getCurrentScene();
        if (scene == null) {
            return;
        }



        List<GameObject> playersLocal  = scene.getObjectsOfClass(Player.class);
        List<GameObject> playersRemote = scene.getServerObjectOfClass(Player.class);
        List<GameObject> enemies       = scene.getServerObjectOfClass(Enemy.class);

        ArrayList<Vector2> playerPositions = new ArrayList<>();
        for (GameObject go : playersLocal) {
            playerPositions.add(go.position);
        }
        for (GameObject go : playersRemote) {
            playerPositions.add(go.position);
        }

        ArrayList<Vector2> enemyPositions = new ArrayList<>();
        for (GameObject go : enemies) {
            enemyPositions.add(go.position);
        }

        if (playerPositions.isEmpty() && enemyPositions.isEmpty()) {
            return;
        }

        // Bounding box
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        for (Vector2 p : playerPositions) {
            if (p.x < minX) {
                minX = p.x;
            }
            if (p.y < minY) {
                minY = p.y;
            }
            if (p.x > maxX) {
                maxX = p.x;
            }
            if (p.y > maxY) {
                maxY = p.y;
            }
        }
        for (Vector2 p : enemyPositions) {
            if (p.x < minX) {
                minX = p.x;
            }
            if (p.y < minY) {
                minY = p.y;
            }
            if (p.x > maxX) {
                maxX = p.x;
            }
            if (p.y > maxY) {
                maxY = p.y;
            }
        }

        float pad = 200f;
        minX -= pad; 
        minY -= pad;
        maxX += pad; 
        maxY += pad;

        float worldW = Math.max(1f, maxX - minX);
        float worldH = Math.max(1f, maxY - minY);


        // Location of minimap
        int x0 = margin;
        int y0 = margin;

        // Frame
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        g2d.setColor(new Color(0, 0, 0));
        g2d.fillRect(x0, y0, mapWidth, mapHeight);
        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.setStroke(new BasicStroke(2f));
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.drawRect(x0, y0, mapWidth, mapHeight);

        // Players
        g2d.setColor(new Color(80, 170, 255));
        for (Vector2 p : playerPositions) {
            float nx = (p.x - minX) / worldW; // 0..1
            float ny = (p.y - minY) / worldH; // 0..1
            int px = x0 + (int) (nx * (mapWidth  - 8)) + 4;
            int py = y0 + (int) (ny * (mapHeight - 8)) + 4;
            g2d.fillOval(px - 3, py - 3, 6, 6);
        }

        // Enemies
        g2d.setColor(new Color(230, 70, 70));
        for (Vector2 p : enemyPositions) {
            float nx = (p.x - minX) / worldW;
            float ny = (p.y - minY) / worldH;
            int px = x0 + (int) (nx * (mapWidth  - 8)) + 4;
            int py = y0 + (int) (ny * (mapHeight - 8)) + 4;
            g2d.fillOval(px - 3, py - 3, 6, 6);
        }
    }
}
