package Engine.Networking;

import Engine.GameObject;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A client class to join a server.
 */
public class Client extends Thread {
    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private boolean running;
    private boolean hasJoined;
    private final UUID clientId;

    public ArrayList<GameObject> gameObjects = new ArrayList<>();
    private ArrayList<NetMessage> messages = new ArrayList<>();
    private ArrayList<Integer> ackMessages = new ArrayList<>();
    private ArrayList<NetMessage> receivedMessages = new ArrayList<>();
    private ArrayList<Integer> executedMessages = new ArrayList<>();

    // For checking the connection to server
    private ArrayList<Consumer<UUID>> onConnectedHandles = new ArrayList<>();
    private ArrayList<Consumer<UUID>> onFailedHandles = new ArrayList<>();
    private ArrayList<Consumer<UUID>> onDisconnectHandles = new ArrayList<>();

    private byte[] sendingBuf;
    private byte[] receivingBuf = new byte[8192];

    public Client() {
        clientId = UUID.randomUUID();
    }

    public UUID getClientId() {
        return clientId;
    }

    public ArrayList<Integer> getAcknowledgedMessages() {
        return ackMessages;
    }

    public ArrayList<NetMessage> getMessages() {
        return messages;
    }

    /**
     * Connect to a server.
     * @param host server address
     * @param port server port
     * @throws Exception failed to connect to server
     */
    public void connect(String host, int port) throws Exception {
        socket = new DatagramSocket();
        address = InetAddress.getByName(host);
        this.port = port;
        running = true;
        hasJoined = false;
        start();
    }

    /**
     * Send all local gameObject data to the server.
     * @param gameObjects Array of local game objects
     */
    public void sendGameObjects(ArrayList<GameObject> gameObjects) {
        if (!running) {
            return;
        }
        HashMap<ClientData, ArrayList<GameObject>> gameObjectMap = new HashMap<>();
        gameObjectMap.put(null, gameObjects);
        Packet dataPacket = new Packet(clientId, gameObjectMap, messages, executedMessages);
        sendingBuf = dataPacket.getBytes();
        DatagramPacket packet = new DatagramPacket(sendingBuf, sendingBuf.length, address, port);
        
        try {
            socket.send(packet);
        } catch (Exception e) {
            System.out.println("Failed to send package");
            return;
        }
    }

    public void onConnected(Consumer<UUID> handler) {
        onConnectedHandles.add(handler);
    }

    public void onFailedConnection(Consumer<UUID> handler) {
        onFailedHandles.add(handler);
    }

    public void onDisconnected(Consumer<UUID> handler) {
        onDisconnectHandles.add(handler);
    }

    private void connected() {
        for (int i = 0; i < onConnectedHandles.size(); i++) {
            onConnectedHandles.get(i).accept(getClientId());
        }
    }

    private void failedConnection() {
        for (int i = 0; i < onFailedHandles.size(); i++) {
            onFailedHandles.get(i).accept(getClientId());
        }
    }

    private void disconnected() {
        for (int i = 0; i < onDisconnectHandles.size(); i++) {
            onDisconnectHandles.get(i).accept(getClientId());
        }
    }


    private void executeMessages(ArrayList<NetMessage> messages) {
        for (int i = 0; i < messages.size(); i++) {
            NetMessage message = messages.get(i);
            if (!executedMessages.contains(message.getId())) {
                Network.onMessageReceived(message);
                executedMessages.add(message.getId());
            }
        }
        cleanupAck(messages);
    }

    private void cleanupAck(ArrayList<NetMessage> messages) {
        for (int i = 0; i < executedMessages.size(); i++) {
            int ackId = executedMessages.get(i);
            boolean found = false;
            for (int j = 0; j < messages.size(); j++) {
                if (messages.get(i).getId() == ackId) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                executedMessages.remove(i);
                i--;
            }
        }
    }

    @Override
    public void run() {
        while (running) {
            DatagramPacket packet = new DatagramPacket(receivingBuf, receivingBuf.length);
            try {
                socket.setSoTimeout(2000);
                socket.receive(packet);
            } catch (SocketTimeoutException e) {
                if (hasJoined) {
                    disconnected();
                } else {
                    failedConnection();
                }
                close();
                return;
            } catch (Exception e) {
                System.out.println("Failed to receive packet");
                continue;
            }
            if (!hasJoined) {
                connected();
                hasJoined = true;
            }

            Packet dataPacket = new Packet(packet.getData());

            gameObjects = dataPacket.getGameObjects();
            receivedMessages = dataPacket.getMessages();
            ackMessages = dataPacket.getAcknowledged();

            executeMessages(receivedMessages);
            
        }
        socket.close();

    }

    public void addMessage(NetMessage msg) {
        messages.add(msg);
    }

    /**
     * Closes an opened connection.
     */
    public void close() {
        socket.close();
        running = false;
        port = 0;
        address = null;
        hasJoined = false;
    }
}
