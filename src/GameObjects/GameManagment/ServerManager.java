package GameObjects.GameManagment;

import Engine.GameObject;
import Engine.Networking.NetEvent;
import Engine.Networking.Server;
import Engine.Physics.ColliderType;
import Engine.Physics.CollisionEvent;
import Engine.Physics.RectCollider;
import Engine.Vector2;
import GameObjects.Pickups.WeaponPickup;
import GameObjects.WeaponManager;

import java.util.ArrayList;
import java.util.UUID;

public class ServerManager extends GameObject {
    private boolean roundStarted = false;
    int playersReady = 0;
    ArrayList<UUID> players = new ArrayList<UUID>();
    boolean ready = false;
    float readyTimer = 0f;

    static ServerManager instance;

    public static boolean isRoundStarted() {
        return instance.roundStarted;
    }

    @NetEvent("player_entered_ready")
    public static void playerEnteredReady(UUID client) {
        if (instance.roundStarted) {
            return;
        }
        instance.playersReady++;
        if (!instance.players.contains(client)) {
            instance.players.add(client);
        }
        if (instance.playersReady == 2 && WeaponManager.isWeaponPickedUp()) {
            instance.sendReady();
        }

    }

    @NetEvent("player_left_ready")
    public static void playerLeftReady() {
        if (instance.roundStarted) {
            return;
        }
        instance.playersReady--;
        instance.sendNotReady();
    }

    void sendReady() {
        if (ready) {
            return;
        }
        readyTimer = 0f;
        ready = true;
        for (UUID uuid : players) {
            Server.sendMessage("all_ready", uuid);
        }
    }

    void sendNotReady() {
        if (!ready) {
            return;
        }
        ready = false;
        for (UUID uuid : players) {
            Server.sendMessage("stop_ready", uuid);
        }
    }

    void sendRoundStarted() {
        if (roundStarted) {
            return;
        }
        roundStarted = true;
        ready = false;
        playersReady = 0;
        for (UUID uuid : players) {
            Server.sendMessage("round_started", uuid);
        }
    }

    public static void spawnGunSelection() {
        Server.addObject(new WeaponPickup(new Vector2(-100, -300), 0));
        Server.addObject(new WeaponPickup(new Vector2(0, -300), 1));
        Server.addObject(new WeaponPickup(new Vector2(100, -300), 2));
        Server.addObject(new WeaponPickup(new Vector2(200, -300), 3));
    }

    public ServerManager() {
        instance = this;
    }

    @Override
    public void setup() {
        spawnGunSelection();
    }

    @Override
    public void update(float deltaTime) {
        if (ready && !roundStarted) {
            readyTimer += deltaTime;
            if (readyTimer >= 3f) {
                sendRoundStarted();
            }
        }
    }
}
