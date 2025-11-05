package GameObjects.GameManagment;

import Engine.GameObject;
import Engine.Networking.Client;
import Engine.Networking.NetEvent;
import Engine.Physics.ColliderType;
import Engine.Physics.CollisionEvent;
import Engine.Physics.RectCollider;
import Engine.Vector2;
import GameObjects.DoorManager;
import GameObjects.Player;

public class ClientManager extends GameObject {
    static Player player;
    DoorManager.DoorPair firstPair;
    DoorManager.DoorPair secondPair;
    float time;
    boolean firstDoorOpen = true;
    boolean secondDoorOpen = false;
    static ClientManager instance;
    boolean inReadyArea = false;
    boolean roundStarted;

    @NetEvent("round_started")
    public static void onRoundStart() {
        instance.roundStarted = true;
        instance.secondDoorOpen = true;
        instance.inReadyArea = false;
    }

    @NetEvent("all_ready")
    public static void onAllReady() {
        instance.firstDoorOpen = false;
    }

    @NetEvent("stop_ready")
    public static void onStopReady() {
        instance.firstDoorOpen = true;
    }

    @NetEvent("on_round_end")
    public static void onRoundEnd() {
        player.position = new Vector2(0, -800);
    }

    @Override
    public void setup() {
        firstPair = DoorManager.addDoor(100, -803, 167, 90, true);

        secondPair = DoorManager.addDoor(327, -803, 167, 90, true);
        firstPair.setRotation(225);
        addCollider(new RectCollider(new Vector2(213, 390), new Vector2(0, 0), ColliderType.None));
        position = new Vector2(110, -973);
    }

    @Override
    public void update(float deltaTime) {
        if (firstDoorOpen) {
            firstPair.rotate((225 - firstPair.getRotation()) / 0.5f * deltaTime);
        } else {
            firstPair.rotate((90 - firstPair.getRotation()) / 0.5f * deltaTime);
        }

        if (secondDoorOpen) {
            secondPair.rotate((190 - secondPair.getRotation()) / 0.5f * deltaTime);
        } else {
            secondPair.rotate((90 - secondPair.getRotation()) / 0.5f * deltaTime);
        }
    }

    public static boolean isReady() {
        return instance.inReadyArea;
    }

    public ClientManager(Player p) {
        player = p;
        instance = this;
    }

    @Override
    public void onCollisionEnter(CollisionEvent collider) {
        if (roundStarted) {
            return;
        }
        inReadyArea = true;
        Client.sendMessage("player_entered_ready", getOwnerUUID());
    }

    @Override
    public void onCollisionExit(CollisionEvent collider) {
        if (roundStarted) {
            return;
        }
        inReadyArea = false;
        Client.sendMessage("player_left_ready", getOwnerUUID());
    }
}
