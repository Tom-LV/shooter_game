package Engine.Networking;

import Engine.Engine;
import Engine.GameObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * A client class to join a server.
 */
public class Client {
    private static DatagramSocket socket;
    private static ConnectionData clientConnectionData;
    private static ConnectionData serverConnectionData;
    private static boolean running;
    private static boolean isConnected;

    // For checking the connection to server
    private static final ArrayList<Runnable> onConnectedHandles = new ArrayList<>();
    private static final ArrayList<Runnable> onFailedHandles = new ArrayList<>();
    private static final ArrayList<Runnable> onDisconnectHandles = new ArrayList<>();
    private static final ConcurrentLinkedQueue<Runnable> pendingNetworkActions = new ConcurrentLinkedQueue<>();
    private static float tick = 0f;

    private static final byte[] receivingBuf = new byte[32768];
    private static Thread clientThread;

    /**
     * Connect to a server.
     * @param host server address
     * @param port server port
     * @throws Exception failed to connect to server
     */
    public static void connect(String host, int port) throws Exception {
        socket = new DatagramSocket();
        socket.setSoTimeout(2000);
        clientConnectionData = new ConnectionData(InetAddress.getLocalHost(), port, UUID.randomUUID());
        serverConnectionData = new ConnectionData(InetAddress.getByName(host), port, UUID.randomUUID());
        isConnected = false;
        startClientThread();
    }

    private static void startClientThread() {
        running = true;
        clientThread = new Thread(() -> {
            while (running) {
                DatagramPacket packet = new DatagramPacket(receivingBuf, receivingBuf.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    if (isConnected) {
                        Client.pendingNetworkActions.add(Client::disconnected);
                    } else {
                        Client.pendingNetworkActions.add(Client::failedConnection);
                    }
                    close();
                    return;
                } catch (IOException e) {
                    System.out.println("Failed to receive packet");
                    continue;
                }
                if (!isConnected) {
                    Client.pendingNetworkActions.add(Client::connected);
                    isConnected = true;
                }

                Packet dataPacket = new Packet(packet.getData());

                try {
                    int packageId = dataPacket.readInt();
                    if (packageId < serverConnectionData.getPackageId()) {
                        continue;
                    }
                    serverConnectionData.setPackageId(packageId);
                    int connectionCount = dataPacket.readInt();
                    ArrayList<GameObject> gameObjects = new ArrayList<>();
                    for (int i = 0; i < connectionCount; i++) {
                        gameObjects.addAll(dataPacket.readGameObjects());
                    }

                    ArrayList<NetMessage> netMessages = dataPacket.readMessages();
                    ArrayList<Integer> ackMessages = dataPacket.readAcknowledgedMessages();
                    Client.pendingNetworkActions.add(() -> {
                        serverConnectionData.executeMessages(netMessages);
                    });
                    Client.pendingNetworkActions.add(() -> {
                        serverConnectionData.removeAckMessages(ackMessages);
                    });
                    Client.pendingNetworkActions.add(() -> {
                        serverConnectionData.updateGameObjects(gameObjects, true);
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Client.close();
        });
        clientThread.start();
    }

    public static void close() {
        socket.close();
        clientThread.interrupt();
        running = false;
        onDisconnectHandles.clear();
        onFailedHandles.clear();
        onConnectedHandles.clear();
        pendingNetworkActions.clear();
    }

    public static void onConnected(Runnable runnable) {
        onConnectedHandles.add(runnable);
    }

    public static void onFailedConnection(Runnable runnable) {
        onFailedHandles.add(runnable);
    }

    public static void onDisconnected(Runnable runnable) {
        onDisconnectHandles.add(runnable);
    }

    private static void connected() {
        onConnectedHandles.forEach(Runnable::run);
    }

    private static void failedConnection() {
        onFailedHandles.forEach(Runnable::run);
    }

    private static void disconnected() {
        onDisconnectHandles.forEach(Runnable::run);
    }

    public static boolean isRunning() {
        return running;
    }

    public static UUID getClientId() {
        return clientConnectionData.getUUID();
    }

    public static void update(float deltaTime) {
        if (!isRunning()) {
            return;
        }

        Runnable task;
        while ((task = pendingNetworkActions.poll()) != null) {
            try {
                task.run();
            } catch (Throwable t) {
                System.err.println("Error while applying pending client action: " + t.getMessage());
            }
        }

        tick += deltaTime;
        if (tick >= 0.0016) {
            sendNetworkUpdate();
            tick = 0;
        }
    }

    private static void sendNetworkUpdate() {
        Packet dataPacket = new Packet();
        try {
            dataPacket.writeSenderId(clientConnectionData.getUUID());
            dataPacket.writeInt(clientConnectionData.getPackageId());
            clientConnectionData.incrementPackageId();
            dataPacket.writeGameObjects(clientConnectionData.getConnectionObjects());
            dataPacket.writeMessages(serverConnectionData.getSentMessages());
            dataPacket.writeAcknowledged(serverConnectionData.getExecutedMessages());
            byte[] sendingBuf = dataPacket.getByteArray();
            DatagramPacket packet = new DatagramPacket(sendingBuf,
                    sendingBuf.length, serverConnectionData.getAddress(), serverConnectionData.getPort());
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(String type, Object... args) {
        if (!isRunning()) {
            return;
        }
        if (Network.getIndexFromName(type) == -1) {
            System.err.println("Event of type " + type + " not found!");
            return;
        }
        pendingNetworkActions.add(() -> {
            NetMessage msg = new NetMessage(type, args);
            serverConnectionData.addMessage(msg);
        });
    }

    public static void addObject(GameObject object) {
        pendingNetworkActions.add(() -> {
            clientConnectionData.addObject(object);
        });
    }

    public static void removeObject(GameObject object) {
        pendingNetworkActions.add(() -> {
            clientConnectionData.removeObject(object);
        });
    }

    public static void onServerObjectAdded(Consumer<GameObject> consumer) {
        if (!isRunning()) {
            return;
        }

        serverConnectionData.onObjectAdded(consumer);
    }

    public static void onServerObjectRemoved(Consumer<GameObject> consumer) {
        if (!isRunning()) {
            return;
        }
        serverConnectionData.onObjectRemoved(consumer);
    }
}
