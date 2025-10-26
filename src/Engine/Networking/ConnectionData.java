package Engine.Networking;

import Engine.GameObject;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

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
    private int packageId = 0;

    private final ArrayList<Consumer<GameObject>> objectAddedRunnable = new ArrayList<>();
    private final ArrayList<Consumer<GameObject>> objectRemovedRunnable = new ArrayList<>();

    public int getPackageId() {
        return packageId;
    }

    public void incrementPackageId() {
        packageId++;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }

    public void onObjectAdded(Consumer<GameObject> consumer) {
        objectAddedRunnable.add(consumer);
    }

    public void onObjectRemoved(Consumer<GameObject> consumer) {
        objectRemovedRunnable.add(consumer);
    }

    private void callAdded(GameObject object) {
        for (Consumer<GameObject> consumer: objectAddedRunnable) {
            consumer.accept(object);
        }
    }

    private void callRemoved(GameObject object) {
        for (Consumer<GameObject> consumer: objectRemovedRunnable) {
            consumer.accept(object);
        }
    }

    private void addExecutedMessage(Integer executedMessage) {
        executedMessages.add(executedMessage);
    }

    private boolean isExecuted(Integer executedMessage) {
        return executedMessages.contains(executedMessage);
    }

    private void removeMessage(Integer messageId) {
        sentMessages.removeIf((NetMessage message) -> message.getId() == messageId);
    }

    public ArrayList<Integer> getExecutedMessages() {
        return executedMessages;
    }

    public void addMessage(NetMessage message) {
        sentMessages.add(message);
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
            callAdded(object);
        }
    }

    public void removeObject(GameObject object) {
        connectionObjects.remove(object);
        callRemoved(object);
    }

    public void updateGameObjects(ArrayList<GameObject> newGameObjects, boolean interpolate) {
        ArrayList<GameObject> localObjects = getConnectionObjects();
        for (GameObject serverObject : newGameObjects) {
            boolean found = false;
            for (GameObject localObject : localObjects) {
                if (serverObject.equals(localObject)) {
                    if (interpolate) {
                        localObject.serverUpdateFromOther(serverObject);
                    } else {
                        localObject.updateFromOther(serverObject);
                    }

                    found = true;
                    break;
                }
            }
            if (found) {
                continue;
            }

            addObject(serverObject);
        }

        for (int i = localObjects.size() - 1; i >= 0; i--) {
            GameObject serverObject = localObjects.get(i);
            boolean found = newGameObjects.contains(serverObject);

            if (!found) {
                removeObject(serverObject);
            }
        }
    }

    public void executeMessages(ArrayList<NetMessage> messages) {
        for (NetMessage message : messages) {
            if (!isExecuted(message.getId())) {
                Network.onMessageReceived(message);
                addExecutedMessage(message.getId());
            }
        }
        cleanUpAck(messages);
    }

    private void cleanUpAck(ArrayList<NetMessage> messages) {
        for (int i = executedMessages.size() - 1; i >= 0; i--) {
            int ackId = executedMessages.get(i);
            boolean found = false;
            for (NetMessage message : messages) {
                if (message.getId() == ackId) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                executedMessages.remove(i);
            }
        }
    }

    public void removeAckMessages(ArrayList<Integer> ackMessages) {
        for (Integer executedMessage : ackMessages) {
            removeMessage(executedMessage);
        }
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
