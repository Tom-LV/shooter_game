package GameObjects;

import java.util.ArrayList;
import Engine.Engine;

public class DoorManager {
    public static class DoorPair {
        Door leftDoor;
        Door rightDoor;
        public DoorPair(Door leftDoor, Door rightDoor) {
            this.leftDoor = leftDoor;
            this.rightDoor = rightDoor;
        }

        public void setRotation(float rotation) {
            this.leftDoor.setRotation(rotation);
            this.rightDoor.setRotation(-rotation);
        }

        public void rotate(float rotation) {
            this.leftDoor.rotation += rotation;
            this.rightDoor.rotation -= rotation;
        }

        public float getRotation() {
            return this.leftDoor.rotation % 360;
        }
    }

    static ArrayList<DoorPair> doorPairs = new ArrayList<DoorPair>();

    public static DoorPair addDoor(int x, int y, int width, float rotation, boolean pivotRight) {
        Door leftDoor = new Door(x, y, width / 2, rotation, !pivotRight);
        Door rightDoor = new Door(x, y, width / 2, -rotation, pivotRight);
        Engine.addObject(leftDoor);
        Engine.addObject(rightDoor);
        return new DoorPair(leftDoor, rightDoor);
    }

}
