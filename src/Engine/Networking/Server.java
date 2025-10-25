package Engine.Networking;

import Engine.GameObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;
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


    public static void setup(int port) throws SocketException {
        socket = new DatagramSocket(port);
        connections = new ArrayList<>();
        serverConnectionData = new ConnectionData(socket.getInetAddress(), port, UUID.randomUUID());
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
                    ConnectionData  connectionData;
                    if (uuidConnectionDataHashMap.containsKey(senderId)) {
                        connectionData = uuidConnectionDataHashMap.get(senderId);
                    } else {
                        connectionData = new ConnectionData(socket.getInetAddress(), socket.getPort(), senderId);
                        Server.pendingNetworkActions.add(() -> {
                            Server.addNewClient(connectionData);
                        });
                    }
                    ArrayList<GameObject> gameObjects = dataPacket.readGameObjects();
                    ArrayList<NetMessage> messages = dataPacket.readMessages();
                    ArrayList<Integer> acknowledgedMessages = dataPacket.readAcknowledgedMessages();
                    connectionData.receivedPackage();
                    Server.pendingNetworkActions.add(() -> {
                        Server.executeGameObjectUpdate(connectionData, gameObjects);
                    });
                    Server.pendingNetworkActions.add(() -> {
                        Server.executeMessages(connectionData, messages);
                    });
                    Server.pendingNetworkActions.add(() -> {
                        Server.executeAcknowledgedMessages(connectionData, acknowledgedMessages);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Server.close();
        });
        serverThread.start();
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
                System.err.println("Error while applying pending network action: " + t.getMessage());
            }
        }

        tick += deltaTime;
        if (tick >= 0.0016) {
            sendNetworkUpdate();
            tick = 0;
        }
    }

    public static int getPort() {
        return Server.socket.getLocalPort();
    }

    public static boolean isRunning() {
        return running;
    }

    public static void close() {
        socket.close();
        serverThread.interrupt();
        running = false;
    }

    public static void addNewClient(ConnectionData connection) {
        if (connections.size() >= playerCount) {
            return;
        }
        connections.add(connection);
        uuidConnectionDataHashMap.put(connection.getUUID(), connection);
    }

    public static void executeAcknowledgedMessages(ConnectionData connection, ArrayList<Integer> executedMessages) {
        for (Integer executedMessage : executedMessages) {
            connection.removeMessage(executedMessage);
        }
    }

    public static void executeMessages(ConnectionData connectionData, ArrayList<NetMessage> messages) {
        for (NetMessage message : messages) {
            if (!connectionData.isExecuted(message.getId())) {
                Network.onMessageReceived(message);
                connectionData.addExecutedMessage(message.getId());
            }
        }
    }

    public static void executeGameObjectUpdate(ConnectionData connectionData, ArrayList<GameObject> newGameObjects) {
        ArrayList<GameObject> clientGameObjects = connectionData.getConnectionObjects();
        for (GameObject newClientObject : newGameObjects) {
            boolean found = false;
            for (GameObject clientObject : clientGameObjects) {
                if (newClientObject.equals(clientObject)) {
                    clientObject.updateFromOther(newClientObject);
                    found = true;
                    break;
                }
            }
            if (found) {
                continue;
            }
            connectionData.addObject(newClientObject);
        }

        for (int i = clientGameObjects.size() - 1; i >= 0; i--) {
            GameObject serverObject = clientGameObjects.get(i);
            boolean found = newGameObjects.contains(serverObject);

            if (!found) {
                clientGameObjects.remove(serverObject);
            }
        }
    }

    /**
     * Get a list of server objects of class.
     * @param cls class of gameObject
     * @return list of gameObjects
     */
    public synchronized List<GameObject> getServerObjectsOfClass(Class<?> cls) {
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
    public synchronized List<GameObject> getClientObjectsOfClass(Class<?> cls) {
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
        });
    }

    private static void sendNetworkUpdate() {
        for (ConnectionData connectionData : connections) {
            Packet dataPacket = new Packet();
            try {
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
}
