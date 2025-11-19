package GameObjects.GameManagment;

import Engine.GameObject;
import Engine.Networking.NetEvent;
import Engine.Networking.Server;
import Engine.Physics.ColliderType;
import Engine.Physics.RectCollider;
import Engine.Vector2;
import GameObjects.DoorManager;
import GameObjects.Enemies.EnemyManager;
import GameObjects.Loot.LootManager;
import GameObjects.Pickups.WeaponPickup;
import GameObjects.WeaponManager;

import java.util.ArrayList;
import java.util.UUID;

public class ServerManager extends GameObject {
    private boolean roundStarted = false;
    int playersReady = 0;
    ArrayList<UUID> players = new ArrayList<>();
    boolean ready = false;
    float readyTimer = 0f;
    float roundTimer = 0f;

    ArrayList<UUID> killedPlayers = new ArrayList<>();

    static ArrayList<Runnable> onRoundStartedListeners = new ArrayList<>();
    static ArrayList<Runnable> onRoundFinishedListeners = new ArrayList<>();

    public static void addOnRoundStartedListener(Runnable listener) {
        onRoundStartedListeners.add(listener);
    }

    public static void removeOnRoundStartedListener(Runnable listener) {
        System.out.println("Removed onRoundStartedListener");
        System.out.println(onRoundStartedListeners.size());
        onRoundStartedListeners.remove(listener);
        System.out.println(onRoundStartedListeners.size());
    }
    public static void addOnRoundFinishedListener(Runnable listener) {
        onRoundFinishedListeners.add(listener);
    }

    public static void removeOnRoundFinishedListener(Runnable listener) {
        onRoundFinishedListeners.remove(listener);
    }

    static ServerManager instance;

    public static boolean isRoundStarted() {
        return instance.roundStarted;
    }

    public static boolean isPlayerKilled(UUID uuid) {
        return instance.killedPlayers.contains(uuid);
    }

    @NetEvent("player_entered_ready")
    public static void playerEnteredReady(UUID client) {
        instance.playersReady++;
        if (isRoundStarted()) {
            return;
        }
        if (!instance.players.contains(client)) {
            instance.players.add(client);
        }
        if (instance.playersReady == Server.getClientCount() && WeaponManager.isWeaponPickedUp()) {
            instance.sendReady();
        }

    }

    @NetEvent("player_left_ready")
    public static void playerLeftReady() {
        instance.playersReady--;
        if (instance.roundStarted) {
            return;
        }
        instance.sendNotReady();
    }

    @NetEvent("player_killed")
    public static void playerKilled(UUID client) {
        instance.killedPlayers.add(client);
        if (instance.killedPlayers.size() == Server.getClientCount()) {
            instance.sendRoundEnded();
        }
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
        roundTimer = 0f;
        ready = false;
        for (UUID uuid : players) {
            Server.sendMessage("round_started", uuid);
        }
        for (Runnable listener : onRoundStartedListeners) {
            listener.run();
        }
    }

    void sendRoundEnded() {
        if (!roundStarted) {
            return;
        }
        roundStarted = false;
        ready = false;
        roundTimer = 0f;
        killedPlayers.clear();
        for (UUID uuid : players) {
            Server.sendMessage("round_ended", uuid);
        }
        for (Runnable listener : onRoundFinishedListeners) {
            listener.run();
        }

        WeaponPickup.destroyAllGuns();
        spawnGunSelection();
        EnemyManager.destroyAllEnemies();
        LootManager.destroyAllLoot();
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

        DoorManager.DoorPair firstPair = DoorManager.addDoor(100, -803, 167, 90, true, false);

        DoorManager.DoorPair secondPair = DoorManager.addDoor(327, -803, 167, 90, true, false);
        firstPair.setRotation(90);
        secondPair.setRotation(190);
    }

    @Override
    public void update(float deltaTime) {
        if (ready && !roundStarted) {
            readyTimer += deltaTime;
            if (readyTimer >= 3f) {
                sendRoundStarted();
            }
        }
        if (roundStarted) {
            roundTimer += deltaTime;
            if (roundTimer >= 60f) {
                sendRoundEnded();
            }
        }
    }
}
