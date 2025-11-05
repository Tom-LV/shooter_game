package GameObjects;

import Engine.GameObject;
import Engine.Physics.ColliderType;
import Engine.Physics.RectCollider;
import Engine.Vector2;

public class Door extends GameObject {

    public Door(int x, int y, int width, float rotation, boolean leftSide) {
        position = new Vector2(x, y);
        this.width =  width;
        this.rotation = rotation;
        if (leftSide) {
            pivotPosition = new Vector2(0, 0);
        }else {
            pivotPosition = new Vector2(0, 1);
        }
        position = position.subtract(new Vector2(width, 0f).rotate(rotation));
    }

    int width;
    Vector2 pivotPosition;
    @Override
    public void setup() {
        addCollider(new RectCollider(new Vector2(width, 10f), pivotPosition, ColliderType.Static));

    }
}
