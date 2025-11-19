package GameObjects;

import java.util.ArrayList;
import Engine.Engine;
import Engine.Networking.Server;

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

    public static DoorPair addDoor(int x, int y, int width, float rotation, boolean pivotRight, boolean isClient) {
        Door leftDoor = new Door(x, y, width / 2, rotation, !pivotRight);
        Door rightDoor = new Door(x, y, width / 2, -rotation, pivotRight);
        if (isClient) {
            Engine.addObject(leftDoor);
            Engine.addObject(rightDoor);
        } else {
            Server.addObject(leftDoor);
            Server.addObject(rightDoor);
        }

        return new DoorPair(leftDoor, rightDoor);
    }

}
