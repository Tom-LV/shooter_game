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
    DataOutputStream dos;
    DataInputStream dis;
    ByteArrayOutputStream bos;

    /**
     * Reads and creates a new packet.
     * @param bytes packet bytes
     */
    public Packet(byte[] bytes) {
        dis = new DataInputStream(new ByteArrayInputStream(bytes));
    }

    /**
     * Creates a new packet.
     */
    public Packet() {
        bos = new ByteArrayOutputStream();
        dos = new DataOutputStream(bos);
    }

    /**
     * Write UUID to packet.
     * @param id uuid
     * @throws IOException when there is an error
     */
    public void writeSenderId(UUID id) throws IOException {
        dos.writeLong(id.getMostSignificantBits());
        dos.writeLong(id.getLeastSignificantBits());
    }

    public void writeGameObjects(ArrayList<GameObject> objects) throws IOException {
        dos.writeInt(objects.size());
        for (GameObject gameObject : objects) {
            gameObject.toOutputStream(dos);
        }
    }

    public void writeInt(int i) throws IOException {
        dos.writeInt(i);
    }

    /**
     * Writes NetEvents to package.
     * @param messages NetEvents
     * @throws IOException when there is an issue
     */
    public void writeMessages(ArrayList<NetMessage> messages) throws IOException {
        dos.writeInt(messages.size());
        for (NetMessage message : messages) {
            if (message == null) {
                continue;
            }
            message.toOutputStream(dos);
        }
    }

    /**
     * Write acknowledged messages to package.
     * @param acknowledgedMessages acknowledged messages
     * @throws IOException when there is an issue
     */
    public void writeAcknowledged(ArrayList<Integer> acknowledgedMessages) throws IOException {
        dos.writeInt(acknowledgedMessages.size());
        for (Integer message : acknowledgedMessages) {
            dos.writeInt(message);
        }
    }

    /**
     * Get the byte array of package.
     * @return byte array of package
     * @throws IOException when there is an error
     */
    public byte[] getByteArray() throws IOException {
        dos.flush();
        return bos.toByteArray();
    }

    /**
     * Reads UUID from package.
     * @return UUID from package
     * @throws IOException when there is an error reading
     */
    public UUID readUUID() throws IOException {
        long most = dis.readLong();
        long least = dis.readLong();
        return new UUID(most, least);
    }

    public ArrayList<GameObject> readGameObjects() throws IOException {
        ArrayList<GameObject> objects = new ArrayList<>();
        int size = dis.readInt();
        for (int i = 0; i < size; i++) {
            objects.add(GameObject.fromInputStream(dis));
        }
        return objects;
    }

    /**
     * Reads messages from byte array.
     * @return Message array list
     * @throws IOException when there is an error reading
     */
    public ArrayList<NetMessage> readMessages() throws IOException {
        int messageCount = dis.readInt();
        ArrayList<NetMessage> messages = new ArrayList<>();

        for (int i = 0; i < messageCount; i++) {
            NetMessage message = NetMessage.fromInputStream(dis);
            if (message == null) {
                continue;
            }
            messages.add(message);
        }
        return messages;
    }

    /**
     * Read acknowledged messages from byte array
     * @return list of acknowledged message ids
     * @throws IOException when there is an issue reading
     */
    public ArrayList<Integer> readAcknowledgedMessages() throws IOException {
        int messageCount = dis.readInt();
        ArrayList<Integer> acknowledgedMessages = new ArrayList<>();
        for (int i = 0; i < messageCount; i++) {
            acknowledgedMessages.add(dis.readInt());
        }
        return acknowledgedMessages;
    }

    public int readInt() throws IOException {
        return dis.readInt();
    }
}
