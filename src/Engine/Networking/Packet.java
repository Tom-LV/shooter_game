package Engine.Networking;

import Engine.GameObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

/**
 * A class to create byte packets.
 */
public class Packet {

    byte[] data;
    ArrayList<GameObject> gameOjbects = new ArrayList<>();
    ArrayList<NetMessage> netMessages = new ArrayList<>();
    ArrayList<Integer> acknowledged = new ArrayList<>();
    UUID id;

    /**
     * Reads and creates a new packet.
     * @param bytes packet bytes
     */
    public Packet(byte[] bytes) {
        data = bytes;
        try {
            deserializeData(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new packet to send.
     * @param senderId client id
     * @param gameObjects gameObjects
     */
    public Packet(UUID senderId, HashMap<ClientData, ArrayList<GameObject>> gameObjects, 
                  ArrayList<NetMessage> messages, ArrayList<Integer> acknowledged) {
        try {
            serializeData(senderId, gameObjects, messages, acknowledged);
        } catch (Exception e) {
            data = new byte[0];
            e.printStackTrace();
        }
    }

    public ArrayList<GameObject> getGameObjects() {
        return gameOjbects;
    }

    public ArrayList<NetMessage> getMessages() {
        return netMessages;
    }

    public ArrayList<Integer> getAcknowledged() {
        return acknowledged;
    }

    public byte[] getBytes() {
        return data;
    }

    /**
     * Serializes given gameObjects.
     * @param id client id
     * @param gameObjects gameObjects
     * @throws IOException when there is a problem writing to output stream
     */
    private void serializeData(UUID id, HashMap<ClientData, ArrayList<GameObject>> gameObjects, 
                               ArrayList<NetMessage> messages, ArrayList<Integer> acknowledged)
            throws IOException {
        if (gameObjects == null) {
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        // Write id
        dos.writeLong(id.getMostSignificantBits());
        dos.writeLong(id.getLeastSignificantBits());

        // Get object count
        int objectCount = 0;
        for (Iterator<ArrayList<GameObject>> it = gameObjects.values().iterator(); it.hasNext();) {
            objectCount += it.next().size();
        }

        dos.writeInt(objectCount);

        // Serialize objects
        for (Iterator<ArrayList<GameObject>> it = gameObjects.values().iterator(); it.hasNext();) {
            ArrayList<GameObject> entityGameObjects = it.next();
            for (int i = 0; i < entityGameObjects.size(); i++) {
                entityGameObjects.get(i).toOutputStream(dos);
            }
        }

        dos.writeInt(messages.size());

        // Serialize messages
        for (int i = 0; i < messages.size(); i++) {
            NetMessage message = messages.get(i);
            message.toOutputStream(dos);
        }

        // Serialize acknowledgments
        dos.writeInt(acknowledged.size());

        for (int i = 0; i < acknowledged.size(); i++) {
            dos.writeInt(acknowledged.get(i));
        }

        dos.flush();
        data = baos.toByteArray();
    }

    private void deserializeData(byte[] data) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));

        // Read senderId
        long most = dis.readLong();
        long least = dis.readLong();
        id  = new UUID(most, least);

        // Read object count
        int objectCount = dis.readInt();

        ArrayList<GameObject> objects = new ArrayList<GameObject>();
        for (int i = 0; i < objectCount; i++) {
            GameObject obj = GameObject.fromInputStream(dis);
            if (obj != null) {
                objects.add(obj);
            }
        }

        // Read messages
        int messageCount = dis.readInt();
        ArrayList<NetMessage> messages = new ArrayList<>();

        for (int i = 0; i < messageCount; i++) {
            NetMessage message = NetMessage.fromInputStream(dis);
            if (message != null) {
                messages.add(message);
            }
        }

        // Read acknowledgments
        int acknowledgedCount = dis.readInt();
        ArrayList<Integer> acknowledged = new ArrayList<>();

        for (int i = 0; i < acknowledgedCount; i++) {
            acknowledged.add(dis.readInt());
        }

        this.acknowledged = acknowledged;
        gameOjbects = objects;
        netMessages = messages;
    }

}
