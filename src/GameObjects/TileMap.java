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

    int[][] tiles;
    Random rng;

    @Override
    protected void setup() {
        rng = new Random();
        generateTileMap();
        setLayer(-1000);
        scale = new Vector2(1.02f, 1.02f);
    }

    private void generateTileMap() {
        tiles = new int[20][20];
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                tiles[i][j] = rng.nextInt(0, 2);
            }
        }
    }

    private void getRoadSprite(int x, int y) {
        if (tiles[x][y] == 0) {
            setSprite("road_0000");
            return;
        }
        String road = "road_";
        road += isRoad(x, y - 1) ? 1 : 0;
        road += isRoad(x - 1, y) ? 1 : 0;
        road += isRoad(x + 1, y) ? 1 : 0;
        road += isRoad(x, y + 1) ? 1 : 0;
        setSprite(road);
    }

    private boolean isRoad(int x, int y) {
        if (x < 0 || y < 0 || x >= tiles.length || y >= tiles.length) {
            return true;
        }
        return tiles[x][y] == 1;
    }

    @Override
    protected void draw(Graphics2D g2d) {
        position = new Vector2(-2400, -2400);
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                drawTile(g2d, j, i);
                position.x += 400;
            }
            position.x = -1600;
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
        getRoadSprite(x, y);
        at.translate(-currentSprite.getDimensions().x * currentSprite.getPivot().x,
                -currentSprite.getDimensions().y * currentSprite.getPivot().y);

        g2d.drawImage(currentSprite.getImage(), at, null);
    }
}
