package Engine.Networking;

import Engine.GameObject;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

/**
 * A class that holds connected client data.
 */
public class ConnectionData {
    private final InetAddress address;
    private final int port;
    private final UUID uuid;
    private Instant lastSentPackage;
    private final ArrayList<NetMessage> sentMessages = new ArrayList<>();
    private final ArrayList<Integer> executedMessages = new ArrayList<>();
    private final ArrayList<GameObject> connectionObjects = new ArrayList<>();

    public void addExecutedMessage(Integer executedMessage) {
        executedMessages.add(executedMessage);
    }

    public boolean isExecuted(Integer executedMessage) {
        return executedMessages.contains(executedMessage);
    }

    public ArrayList<Integer> getExecutedMessages() {
        return executedMessages;
    }

    public void addMessage(NetMessage message) {
        sentMessages.add(message);
    }

    public void removeMessage(Integer messageId) {
        sentMessages.removeIf((NetMessage message) -> message.getId() == messageId);
    }

    public ArrayList<NetMessage> getSentMessages() {
        return sentMessages;
    }

    public ArrayList<GameObject> getConnectionObjects() {
        return connectionObjects;
    }

    public void addObject(GameObject object) {
        if (!connectionObjects.contains(object)) {
            connectionObjects.add(object);
        }
    }

    public void removeObject(GameObject object) {
        connectionObjects.remove(object);
    }

    /**
     * Get the time elapsed since last package in ms.
     * @return time elapsed in ms
     */
    public int lastPackage() {
        return (int) Duration.between(lastSentPackage, Instant.now()).toMillis();
    }

    public void receivedPackage() {
        lastSentPackage = Instant.now();
    }

    /**
     * Creates a new client data class.
     * @param address client address
     * @param port client prot
     * @param uuid client UUID
     */
    ConnectionData(InetAddress address, int port, UUID uuid) {
        this.uuid = uuid;
        this.address = address;
        this.port = port;
        lastSentPackage = Instant.now();
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ConnectionData that = (ConnectionData) obj;
        return uuid.equals(that.uuid);
    }

}
