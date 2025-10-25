package Engine.Networking;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * A class that holds connected client data.
 */
public class ClientData {
    private final InetAddress address;
    private final int port;
    private final UUID clientId;
    private Instant lastSentPackage;

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
     * @param clientId client UUID
     */
    ClientData(InetAddress address, int port, UUID clientId) {
        this.clientId = clientId;
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
        return clientId;
    }

    @Override
    public int hashCode() {
        return clientId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ClientData that = (ClientData) obj;
        return clientId.equals(that.clientId);
    }

}
