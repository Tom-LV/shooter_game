package GameObjects;

import Engine.Engine;
import Engine.GameObject;
import Engine.Sprite;
import Engine.Vector2;
import Engine.Camera;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Random;

public class TileMap extends GameObject {

    String[][] tiles;
    int[][] roads;
    Random rng;
    int height = 20;
    int width = 20;

    @Override
    public void setup() {
        rng = new Random(1235233);
        generateTileMap();
        setLayer(-1000);
        scale = new Vector2(1.02f, 1.02f);
    }

    private void generateTileMap() {
        tiles = new String[width][height];
        roads = new int[width * 2 + 1][height * 2 + 1];

        for (int x = 0; x < width * 2 + 1; x++) {
            for (int y = 1; y < height * 2 + 1; y+=2) {
                if (y % 3 == 0) {
                    roads[x][y] = 1;
                }
            }
        }

        for (int y = 3; y < height * 2 - 4; y+=6) {
            for (int x = 1; x < width * 2 + 1; x+=2) {
                if (rng.nextFloat() < 0.1f) {
                    roads[x][y + 1] = 1;
                    roads[x][y + 2] = 1;
                    roads[x][y + 3] = 1;
                    roads[x][y + 4] = 1;
                    roads[x][y + 5] = 1;
                }
            }

        }

        int baseX = 10;
        int baseY = 8;

        for (int x = -1; x < 4; x++) {
            for (int y = -1; y < 4; y++) {
                roads[baseX * 2 + 1 + x][baseY * 2 + 1 + y] = 0;
            }
        }
        roads[baseX * 2 + 4][baseY * 2 + 1] = 1;
        roads[baseX * 2 + 5][baseY * 2 + 1] = 1;
        roads[baseX * 2 + 5][baseY * 2] = 1;

        // Now assign sprites based on roads
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rx = x * 2 + 1;
                int ry = y * 2 + 1;

                boolean up = roads[rx][ry - 1] == 1;
                boolean left = roads[rx - 1][ry] == 1;
                boolean right = roads[rx + 1][ry] == 1;
                boolean down = roads[rx][ry + 1] == 1;

                if (roads[rx][ry] == 1)
                    tiles[x][y] = "road_" + (up ? "1" : "0") + (left ? "1" : "0") + (right ? "1" : "0") + (down ? "1" : "0");
                else
                    tiles[x][y] = "road_0000";
            }
        }

        tiles[baseX][baseY] = "base";
        tiles[10][9] = "";
        tiles[11][8] = "";
        tiles[11][9] = "";

    }

    @Override
    protected void draw(Graphics2D g2d) {
        position = new Vector2(-4000, -4000);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                drawTile(g2d, j, i);
                position.x += 400;
            }
            position.x = -4000;
            position.y += 400;
        }
    }

    private void drawTile(Graphics2D g2d, int x, int y) {
        AffineTransform at = new AffineTransform();

        Vector2 panelDimensions = new Vector2(Engine.getCurrentScene().getWidth() / 2,
                Engine.getCurrentScene().getHeight() / 2);

        Vector2 panelPos = position.subtract(Camera.currentCamera.position).divide(Camera.currentCamera.zoom);
        panelPos = panelPos.add(panelDimensions);
        at.translate(panelPos.x, panelPos.y);
        at.scale(scale.x / Camera.currentCamera.zoom, scale.y / Camera.currentCamera.zoom);

        currentSprite = Sprite.getSprite(tiles[x][y]);
        if (currentSprite == null) return;

        at.translate(-currentSprite.getDimensions().x * currentSprite.getPivot().x,
                -currentSprite.getDimensions().y * currentSprite.getPivot().y);

        g2d.drawImage(currentSprite.getImage(), at, null);
    }
}
