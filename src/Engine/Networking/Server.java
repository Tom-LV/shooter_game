package Engine.Networking;

import Engine.GameObject;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

/**
 * A server class to send and receive data.
 */
public class Server extends Thread {
    private DatagramSocket socket;
    private int port;
    private boolean running;
    private byte[] buf = new byte[8192];
    private byte[] sendingBuf;

    private HashSet<ClientData> clients = new HashSet<>();
    private ArrayList<UUID> clientUUIDs = new ArrayList<>();
    private HashMap<UUID, ClientData> uuidHashMap = new HashMap<>();
    private HashMap<ClientData, ArrayList<GameObject>> allObjects = new HashMap<>();
    private HashMap<ClientData, ArrayList<Integer>> executedMessages = new HashMap<>();
    private HashMap<UUID, ArrayList<NetMessage>> messages = new HashMap<>();

    private final UUID serverId = new UUID(0, 0);
    private ClientData serverClientData;
    private float tick = 0f;

    private static Server server;
    private static final int PLAYER_COUNT = 2;

    public Server() {
        Server.server = this;
    }

    public HashMap<ClientData, ArrayList<GameObject>> getAllObjects() {
        return allObjects;
    }

    /**
     * Get a list of server objects of class.
     * @param cls class of gameObject
     * @return list of gameObjects
     */
    public static ArrayList<GameObject> getServerObjectOfClass(Class<?> cls) {
        ArrayList<GameObject> returnedObjects = new ArrayList<>();
        ArrayList<GameObject> serverObjects = Server.server.allObjects.get(
                Server.server.serverClientData);
        for (int i = 0; i < serverObjects.size(); i++) {
            if (serverObjects.get(i) == null) {
                continue;
            }
            if (serverObjects.get(i).isOfClass(cls)) {
                returnedObjects.add(serverObjects.get(i));
            }
        }
        return returnedObjects;
    }

    /**
     * Gets all client GameObjects of class.
     * @param cls class of gameObject
     * @return list of gameObjects
     */
    public static ArrayList<GameObject> getClientObjectOfClass(Class<?> cls) {
        ArrayList<GameObject> returnedObjects = new ArrayList<>();
        ArrayList<UUID> clients = getClientUUIDs();

        for (int i = 0; i < clients.size(); i++) {
            ClientData client = getClientFromUUID(clients.get(i));
            ArrayList<GameObject> serverObjects = getClientObjects(client);
            for (int j = 0; j < serverObjects.size(); j++) {
                if (serverObjects.get(j).isOfClass(cls)) {
                    returnedObjects.add(serverObjects.get(j));
                }
            }
        }
        return returnedObjects;
    }

    public static int getPort() {
        return Server.server.port;
    }

    /**
     * Set a port and start server.
     * @param port port
     * @throws Exception throws an exception if server cannot be opened
     */
    public void startServer(int port) throws Exception {
        socket = new DatagramSocket(port);
        this.port = port;
        serverClientData = new ClientData(socket.getInetAddress(), port, serverId);
        allObjects.put(serverClientData, new ArrayList<>());
        running = true;
        start();
    }

    public static ArrayList<UUID> getClientUUIDs() {
        return Server.server.clientUUIDs;
    }

    /**
     * Get ClientData from the client UUID.
     * @param id client UUID
     * @return ClientData
     */
    public static ClientData getClientFromUUID(UUID id) {
        return Server.server.uuidHashMap.get(id);
    }

    public static ArrayList<GameObject> getClientObjects(ClientData client) {
        return Server.server.allObjects.get(client);
    }

    /**
     * An update method that updates all server objects.
     * @param deltaTime engine delta time
     */
    public void update(float deltaTime) {
        if (!running) {
            return;
        }
        
        tick += deltaTime;
        if (tick >= 0.0016) {
            sendServerObjects(allObjects);
            tick = 0;
        }
        ArrayList<GameObject> gameObjects = Server.server.allObjects.get(serverClientData);

        for (int i = gameObjects.size() - 1; i >= 0; i--) {
            GameObject gameObject = gameObjects.get(i);
            if (gameObject == null) {
                gameObjects.remove(i);
            }
        }

        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject gameObject = gameObjects.get(i);
            gameObject.animationUpdate(deltaTime);
            gameObject.update(deltaTime);
        }

        ArrayList<ClientData> toRemoveClient = new ArrayList<>();
        for (int i = 0; i < clientUUIDs.size(); i++) {
            ClientData client = Server.getClientFromUUID(Server.server.clientUUIDs.get(i));
            if (client == null) {
                continue;
            }
            if (client.lastPackage() >= 2000) {
                toRemoveClient.add(client);
            }
        }

        for (int i = 0; i < toRemoveClient.size(); i++) {
            System.out.println(toRemoveClient.get(i).getUUID());
            removeClient(toRemoveClient.get(i));
        }
    }

    private void removeAckMessages(ArrayList<Integer> ackMessages, ClientData client) {
        ArrayList<NetMessage> messages = this.messages.get(client.getUUID());
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (ackMessages.contains(messages.get(i).getId())) {
                messages.get(i).setAcknowledged(true);
                messages.remove(i);
            }
        }
    }

    private void executeMessages(ArrayList<NetMessage> messages, ClientData client) {
        ArrayList<Integer> clientAck = executedMessages.get(client);
        for (int i = 0; i < messages.size(); i++) {
            NetMessage message = messages.get(i);
            if (!clientAck.contains(message.getId())) {
                Network.onMessageReceived(message);
                clientAck.add(message.getId());
            }
        }
        cleanupAck(clientAck, messages);
    }

    private void cleanupAck(ArrayList<Integer> clientAck, ArrayList<NetMessage> messages) {
        for (int i = clientAck.size() - 1; i >= 0; i--) {
            int ackId = clientAck.get(i);
            boolean found = false;
            for (int j = 0; j < messages.size(); j++) {
                if (messages.get(i).getId() == ackId) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                clientAck.remove(i);
            }
        }
    }

    public void addMessage(NetMessage msg, UUID clientId) {
        messages.get(clientId).add(msg);
    }

    /**
     * Adds a new server object.
     * @param gameObject gameObject
     */
    public static void addObject(GameObject gameObject) {
        if (Server.server == null) {
            return;
        }
        if (gameObject == null) {
            return;
        }

        ArrayList<GameObject> gameObjects = Server.server.allObjects.get(
                Server.server.serverClientData);
        if (!gameObjects.contains(gameObject)) {
            gameObject.setOwnerUUID(Server.server.serverId);
            gameObjects.add(gameObject);
        }
    }

    /**
     * Removes a server object.
     * @param gameObject gameObject
     */
    public static void removeObject(GameObject gameObject) {
        if (Server.server == null) {
            return;
        }
        ArrayList<GameObject> gameObjects = Server.server.allObjects.get(
                Server.server.serverClientData);
        if (gameObjects.contains(gameObject)) {
            gameObject.onDestroy();
            gameObjects.remove(gameObject);
        }
    }

    public static ArrayList<GameObject> getObjects() {
        return Server.server.allObjects.get(Server.server.serverClientData);
    }

    private void sendServerObjects(HashMap<ClientData, ArrayList<GameObject>> serverObjects) {
        if (!running) {
            return;
        }

        for (int i = 0; i < clientUUIDs.size(); i++) {
            ClientData client = Server.getClientFromUUID(clientUUIDs.get(i));
            ArrayList<NetMessage> clientMessages = messages.get(client.getUUID());
            Packet dataPacket = new Packet(serverId, serverObjects, 
                                           clientMessages, executedMessages.get(client));
            sendingBuf = dataPacket.getBytes();
            
            DatagramPacket packet = new DatagramPacket(sendingBuf, 
                sendingBuf.length, client.getAddress(), client.getPort());

            try {
                socket.send(packet);
            } catch (Exception e) {
                System.out.println("Failed to send package");
                return;
            }
        }
        
    }

    /**
     * Updates client gameObject states.
     * @param newGameObjects new client gameObjects
     * @param clientData client that the objects belong to
     */
    private void updateGameObjects(ArrayList<GameObject> newGameObjects, ClientData clientData) {
        ArrayList<GameObject> clientGameObjects = allObjects.get(clientData);
        for (Iterator<GameObject> it = newGameObjects.iterator(); it.hasNext();) {
            GameObject newClientObject = it.next();
            boolean found = false;

            for (int i = 0; i < clientGameObjects.size(); i++) {
                GameObject serverObject = clientGameObjects.get(i);
                if (newClientObject.equals(serverObject)) {
                    serverObject.position = newClientObject.position;
                    serverObject.scale = newClientObject.scale;
                    serverObject.rotation = newClientObject.rotation;
                    if (newClientObject.currentSprite != null) {
                        serverObject.currentSprite = newClientObject.currentSprite;
                    }
                    found = true;
                    break;
                }
            }
            if (found) {
                continue;
            }
            if (newClientObject != null) {
                clientGameObjects.add(newClientObject);
            }
            
        }

        for (int i = clientGameObjects.size() - 1; i >= 0; i--) {
            GameObject serverObject = clientGameObjects.get(i);
            boolean found = newGameObjects.contains(serverObject);
            
            if (!found) {
                clientGameObjects.remove(serverObject);
            }
        }
    }

    private void addNewClient(ClientData client) {
        if (clients.size() >= PLAYER_COUNT) {
            return;
        }

        if (!clients.contains(client)) {
            clients.add(client);
            allObjects.put(client, new ArrayList<>());
            executedMessages.put(client, new ArrayList<>());
            messages.put(client.getUUID(), new ArrayList<>());
            clientUUIDs.add(client.getUUID());
            uuidHashMap.put(client.getUUID(), client);
        }
    }

    private void removeClient(ClientData client) {
        if (!clients.contains(client)) {
            return;
        }
        clients.remove(client);
        allObjects.remove(client);
        executedMessages.remove(client);
        messages.remove(client.getUUID());
        clientUUIDs.remove(client.getUUID());
        uuidHashMap.remove(client.getUUID());
    }

    @Override
    public void run() {
        while (running) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (Exception e) {
                System.out.println("Failed to receive packet");
                continue;
            }
            
            Packet dataPacket = new Packet(packet.getData());

            ClientData currentClient = new ClientData(packet.getAddress(), 
                packet.getPort(), dataPacket.id);
            
            
            if (clients.contains(currentClient)) {
                ClientData client = Server.getClientFromUUID(currentClient.getUUID());
                client.receivedPackage();
                
                updateGameObjects(dataPacket.getGameObjects(), client);
                executeMessages(dataPacket.getMessages(), client);
                removeAckMessages(dataPacket.getAcknowledged(), client);
            } else {
                addNewClient(currentClient);
            }
        }
        socket.close();
    }
}
