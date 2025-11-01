package Engine.Networking;

import Engine.GameObject;
import Engine.GameObjectType;
import Engine.Physics.Collider;
import Engine.Physics.PhysicsManager;

import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A server class to send and receive data.
 */
public class Server {
    private static DatagramSocket socket;
    private static volatile boolean running;
    private final static byte[] buf = new byte[8192];

    private static final ConcurrentHashMap<UUID, ConnectionData> uuidConnectionDataHashMap = new ConcurrentHashMap<>();
    private static ArrayList<ConnectionData> connections;
    private static final ConcurrentLinkedQueue<Runnable> pendingNetworkActions = new ConcurrentLinkedQueue<>();

    private static ConnectionData serverConnectionData;
    private static float tick = 0f;

    private static final int playerCount = 2;
    private static Thread serverThread;

    private static PhysicsManager physicsManager = new PhysicsManager();

    public static void start(int port) throws SocketException {
        socket = new DatagramSocket(port);
        connections = new ArrayList<>();
        serverConnectionData = new ConnectionData(socket.getInetAddress(), port, UUID.randomUUID());
        serverConnectionData.onObjectRemoved(GameObject::onDestroy);
        startServerThread();
    }

    private static void startServerThread() throws SocketException {
        running = true;
        serverThread = new Thread(() -> {
            while (running) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet);
                } catch (Exception e) {
                    System.out.println("Failed to receive packet");
                    continue;
                }

                Packet dataPacket = new Packet(packet.getData());

                try {
                    UUID senderId = dataPacket.readUUID();
                    int packetId = dataPacket.readInt();
                    ConnectionData  connectionData;
                    if (uuidConnectionDataHashMap.containsKey(senderId)) {
                        connectionData = uuidConnectionDataHashMap.get(senderId);
                    } else {
                        System.out.println(packet.getAddress());
                        connectionData = new ConnectionData(packet.getAddress(), packet.getPort(), senderId);
                        Server.pendingNetworkActions.add(() -> {
                            Server.addNewClient(connectionData);
                        });
                    }
                    if (packetId < connectionData.getPackageId()) {
                        continue;
                    }
                    connectionData.setPackageId(packetId);
                    ArrayList<GameObject> gameObjects = dataPacket.readGameObjects();
                    ArrayList<NetMessage> messages = dataPacket.readMessages();
                    ArrayList<Integer> acknowledgedMessages = dataPacket.readAcknowledgedMessages();

                    connectionData.receivedPackage();
                    Server.pendingNetworkActions.add(() -> {
                        connectionData.updateGameObjects(gameObjects, false);
                    });
                    Server.pendingNetworkActions.add(() -> {
                        connectionData.executeMessages(messages);
                    });
                    Server.pendingNetworkActions.add(() -> {
                        connectionData.removeAckMessages(acknowledgedMessages);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Server.close();
        });
        serverThread.start();
    }

    public static void close() {
        socket.close();
        serverThread.interrupt();
        running = false;
    }

    /**
     * An update method that updates all server objects.
     * @param deltaTime engine delta time
     */
    public static void update(float deltaTime) {
        if (!isRunning()) {
            return;
        }

        Runnable task;
        while ((task = pendingNetworkActions.poll()) != null) {
            try {
                task.run();
            } catch (Throwable t) {
                System.err.println("Error while applying pending server action: " + t.getMessage());
            }
        }

        physicsManager.physicsUpdate();
        for (GameObject gameObject : serverConnectionData.getConnectionObjects()) {
            gameObject.update(deltaTime);
        }

        tick += deltaTime;
        if (tick >= 0.0016) {
            sendNetworkUpdate();
            tick = 0;
        }
    }

    public static void drawColliders(Graphics2D g) {
        if (isRunning()) {
            physicsManager.drawColliders(g);
        }
    }

    public static int getPort() {
        return Server.socket.getLocalPort();
    }

    public static boolean isRunning() {
        return running;
    }

    public static void addNewClient(ConnectionData connection) {
        if (connections.size() >= playerCount) {
            return;
        }
        connections.add(connection);
        uuidConnectionDataHashMap.put(connection.getUUID(), connection);
    }

    /**
     * Get a list of server objects of class.
     * @param cls class of gameObject
     * @return list of gameObjects
     */
    public static synchronized List<GameObject> getServerObjectsOfClass(Class<?> cls) {
        ArrayList<GameObject> serverObjects = serverConnectionData.getConnectionObjects();
        return serverObjects.stream()
                .filter(o -> o.isOfClass(cls))
                .toList();
    }

    /**
     * Gets all client GameObjects of class.
     * @param cls class of gameObject
     * @return list of gameObjects
     */
    public static synchronized List<GameObject> getClientObjectsOfClass(Class<?> cls) {
        ArrayList<GameObject> returned = new ArrayList<>();

        // iterate over a snapshot of client UUIDs
        for (ConnectionData connectionData : connections) {
            for (GameObject go : connectionData.getConnectionObjects()) {
                if (go != null && go.isOfClass(cls)) returned.add(go);
            }
        }
        return returned;
    }

    /**
     * Adds a new server object.
     * @param gameObject gameObject
     */
    public static void addObject(GameObject gameObject) {
        if (!Server.isRunning()) {
            return;
        }
        pendingNetworkActions.add(() -> {
            gameObject.setOwnerUUID(serverConnectionData.getUUID());
            gameObject.setGameObjectType(GameObjectType.Server);
            gameObject.setup();
            serverConnectionData.addObject(gameObject);
        });
    }

    /**
     * Removes a server object.
     * @param gameObject gameObject
     */
    public static void removeObject(GameObject gameObject) {
        if (!Server.isRunning()) {
            return;
        }
        pendingNetworkActions.add(() -> {
            serverConnectionData.removeObject(gameObject);
            gameObject.cleanUp();
        });
    }

    public static void addCollider(Collider collider) {
        physicsManager.addCollider(collider);
    }

    public static void removeCollider(Collider collider) {
        physicsManager.removeCollider(collider);
    }

    private static void sendNetworkUpdate() {
        serverConnectionData.incrementPackageId();
        for (ConnectionData connectionData : connections) {
            Packet dataPacket = new Packet();
            try {
                dataPacket.writeInt(serverConnectionData.getPackageId());
                dataPacket.writeInt(connections.size());
                dataPacket.writeGameObjects(serverConnectionData.getConnectionObjects());
                for (ConnectionData other : connections) {
                    if (other.equals(connectionData)) {
                        continue;
                    }
                    dataPacket.writeGameObjects(other.getConnectionObjects());
                }
                dataPacket.writeMessages(connectionData.getSentMessages());
                dataPacket.writeAcknowledged(connectionData.getExecutedMessages());
                byte[] sendingBuf = dataPacket.getByteArray();
                DatagramPacket packet = new DatagramPacket(sendingBuf,
                        sendingBuf.length, connectionData.getAddress(), connectionData.getPort());
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends a NetMessage from the server to the client.
     * @param type event type
     * @param client client UUID
     * @param args method arguments
     */
    public static void sendMessage(String type, UUID client, Object... args) {
        if (!isRunning()) {
            return;
        }
        if (Network.getIndexFromName(type) == -1) {
            System.err.println("Event of type " + type + " not found!");
            return;
        }
        pendingNetworkActions.add(() -> {
            NetMessage msg = new NetMessage(type, args);
            uuidConnectionDataHashMap.get(client).addMessage(msg);
        });
    }
}
