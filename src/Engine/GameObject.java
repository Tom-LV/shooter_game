package Engine;

import Engine.Networking.NetMessage;
import Engine.Networking.Network;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;



/**
 * Abstract GameObject that exsists in scenes.
 */
public class GameObject implements Serializable {
    private static final float LERP_SPEED = 10.0f;

    private final UUID id;
    public Vector2 position = new Vector2(0, 0);
    public Vector2 scale = new Vector2(1, 1);
    public float rotation = 0.0f;
    private int layerIndex = 0;
    public Sprite currentSprite;
    private BufferedImage currentImage;
    private Class<?> myClass;
    private UUID ownerUUID;

    private boolean playingAnimation = false;
    boolean needsLayerChange = false;
    private float animTime = 0f;
    private int frameIndex = 0;
    private Animation currentAnimation;

    public void setOwnerUUID(UUID uuid) {
        ownerUUID = uuid;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    /**
     * Changes layer of gameObject.
     * @param layer layer index
     */
    public void setLayer(int layer) {
        layerIndex = layer;
        needsLayerChange = true;
    }

    public int getLayer() {
        return layerIndex;
    }

    // For smooth server object movement
    private Vector2 targetPos;

    /**
     * Creates a new gameObject with unique UUID.
     */
    public GameObject() {
        this.id = UUID.randomUUID();
        this.myClass = getClass();
        setup();
    }

    public boolean isOfClass(Class<?> cls) {
        return myClass == cls;
    }

    /**
     * Play animation.
     * @param animation animation
     */
    public void playAnimation(Animation animation) {
        currentAnimation = animation;
        playingAnimation = true;
        animTime = 0f;
        frameIndex = 0;
    }

    public void stopAnimation() {
        playingAnimation = false;
    }

    /**
     * An update call that manages animation playing.
     * @param deltaTime Engine delta time
     */
    public void animationUpdate(float deltaTime) {
        if (!playingAnimation) {
            return;
        }
        animTime += deltaTime;
        if (animTime >= currentAnimation.getFrameDelay(frameIndex)) {
            setSprite(currentAnimation.getFrameName(frameIndex));
            if (currentAnimation.hasNextFrame(frameIndex)) {
                frameIndex++;
                animTime = 0;
            } else {
                playingAnimation = false;
            }
        }
    }

    /**
     * Creates new gameObject from received data.
     * @param id UUID of gameObject
     * @param position position
     * @param scale scale
     * @param rotation rotation
     * @param imageIndex sprite index
     */
    GameObject(UUID id, Vector2 position, Vector2 scale, 
               float rotation, int imageIndex, Class<?> cls, UUID ownerId, int layerIndex) {
        if (imageIndex != -1) {
            setSprite(imageIndex);
        }
        this.id = id;
        this.position = position;
        this.scale = scale;
        this.scale = scale;
        this.rotation = rotation;
        this.myClass = cls;
        this.ownerUUID = ownerId;
        this.layerIndex = layerIndex;
        targetPos = position;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof GameObject)) {
            return false;
        }
        GameObject that = (GameObject) obj;
        return id.equals(that.id);
    }

    /**
     * Sets the current sprite to the specified one.
     * @param name sprite name
     */
    public void setSprite(String name) {
        currentSprite = Sprite.getSprite(name);
        if (currentSprite == null) {
            currentImage = null;
        } else {
            currentImage = currentSprite.getImage();
        }
    }

    private void setSprite(int index) {
        currentSprite = Sprite.getSpriteFromIndex(index);
        if (currentSprite == null) {
            currentImage = null;
        } else {
            currentImage = currentSprite.getImage();
        }
        
    }

    public void setRotation(float degrees) {
        rotation = degrees;
    }

    /**
     * Draws the object on game panel.
     * @param g2d the Graphics2D component of the game panel
     */
    protected void draw(Graphics2D g2d) {
        if (currentImage == null) {
            return;
        }
        AffineTransform at = new AffineTransform();

        Vector2 panelDimensions = new Vector2(Engine.getCurrentScene().getWidth() / 2, 
            Engine.getCurrentScene().getHeight() / 2);
        
        Vector2 panelPos = position.subtract(Camera.currentCamera.position);
        panelPos = panelPos.add(panelDimensions);
        at.translate(panelPos.x, panelPos.y);
        at.rotate(Math.toRadians(rotation));
        at.scale(scale.x, scale.y);
        at.translate(-currentImage.getWidth() * currentSprite.pivot.x, 
            -currentImage.getHeight() * currentSprite.pivot.y);

        g2d.drawImage(currentImage, at, null);
    }

    protected void setup() {
        return;
    }

    public void onDestroy() {
        return;
    }

    public void update(float deltaTime) {

    }

    /**
     * Interpolates server received object position for smoother movement.
     * @param deltaTime engine delta time
     */
    public void serverObjectInterpolation(float deltaTime) {
        if (getClass() != GameObject.class) {
            return;
        }
        float t = (LERP_SPEED * deltaTime);

        if (t > 1.0f) {
            t = 1.0f;
        }
        position = position.add(targetPos.subtract(position).multiply(t));
    }

    /**
     * Update object values from sender.
     * @param position position
     * @param scale scale
     * @param rotation rotation
     * @param spriteIndex sprite index
     */
    public void updateValues(Vector2 position, Vector2 scale, float rotation, int spriteIndex) {
        targetPos = position;
        this.scale = scale;
        this.rotation = rotation;
        if (spriteIndex != -1) {
            setSprite(spriteIndex);
        }
    }

    /**
     * Writes gameObject to the given output stream.
     * @param dos DataOutputStream
     * @throws IOException when there is a problem with writing to output stream
     */
    public void toOutputStream(DataOutputStream dos) throws IOException {
        int classIndex = ClassManager.getIndexFromClass(myClass);
        dos.writeInt(classIndex);
        dos.writeLong(id.getMostSignificantBits());
        dos.writeLong(id.getLeastSignificantBits());
        dos.writeLong(ownerUUID.getMostSignificantBits());
        dos.writeLong(ownerUUID.getLeastSignificantBits());
        dos.writeFloat(position.x);
        dos.writeFloat(position.y);
        dos.writeFloat(scale.x);
        dos.writeFloat(scale.y);
        dos.writeFloat(rotation);
        dos.writeInt(layerIndex);
        if (currentSprite == null) {
            dos.writeInt(-1);
        } else {
            dos.writeInt(currentSprite.getIndex());
        }
    }

    /**
     * Reads the input stream to get a gameObject.
     * @param dis DataInputStream
     * @return Created gameObject
     * @throws IOException when there is a problem with reading input stream
     */
    public static GameObject fromInputStream(DataInputStream dis) throws IOException {
        int classIndex = dis.readInt();
        long most = dis.readLong();
        long least = dis.readLong();
        long ownerMost = dis.readLong();
        long ownerLeast = dis.readLong();
        float x = dis.readFloat();
        float y = dis.readFloat();
        float scaleX = dis.readFloat();
        float scaleY = dis.readFloat();
        float rotation = dis.readFloat();
        int layerIndex = dis.readInt();
        int imageIndex = dis.readInt();
        UUID id = new UUID(most, least);
        UUID ownerId = new UUID(ownerMost, ownerLeast);
        Vector2 position = new Vector2(x, y);
        Vector2 scale = new Vector2(scaleX, scaleY);
        Class<?> cls = ClassManager.getClassFromIndex(classIndex);

        return new GameObject(id, position, scale, rotation, imageIndex, cls, ownerId, layerIndex);
    }

    /**
     * Sends a NetMessage with given type from the client to the server.
     * @param type NetEvent type
     * @param args method arguments
     * @return Generated NetMessage
     */
    public NetMessage sendMessage(String type, Object... args) {
        if (!Engine.isClientRunning()) {
            System.err.println("Cannot send message: " + type + ". Client is not running.");
            return null;
        }
        if (Network.getIndexFromName(type) == -1) {
            System.err.println("Event of type " + type + " not found!");
            return null;
        }

        NetMessage msg = new NetMessage(type, args);
        Engine.getClient().addMessage(msg);
        return msg;
    }

    /**
     * Sends a NetMessage from the server to the client.
     * @param type event type
     * @param client client UUID
     * @param args method arguments
     * @return Generated NetMessage
     */
    public NetMessage sendMessage(String type, UUID client, Object... args) {
        if (!Engine.isServerRunning()) {
            return null;
        }
        
        if (Network.getIndexFromName(type) == -1) {
            System.err.println("Event of type " + type + " not found!");
            return null;
        }
        NetMessage msg = new NetMessage(type, args);
        Engine.getServer().addMessage(msg, client);

        return msg;
    }
}

