package Engine;

import Engine.Networking.Client;
import Engine.Networking.Server;
import Engine.Physics.Collider;
import Engine.Physics.CollisionEvent;
import Engine.Physics.PhysicsManager;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;



/**
 * Abstract GameObject that exists in scenes.
 */
public class GameObject implements Serializable {
    private static final float LERP_SPEED = 10.0f;

    private final UUID id;
    public Vector2 position = new Vector2(0, 0);
    public Vector2 scale = new Vector2(1, 1);
    public float rotation = 0.0f;
    private int layerIndex = 0;
    public Sprite currentSprite;
    private final Class<?> myClass;
    private UUID ownerUUID;
    private final ArrayList<Collider> colliders = new ArrayList<>();

    private boolean playingAnimation = false;
    boolean needsLayerChange = false;
    private float animTime = 0f;
    private int frameIndex = 0;
    private Animation currentAnimation;
    private GameObjectType gameObjectType;

    public void setOwnerUUID(UUID uuid) {
        ownerUUID = uuid;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setGameObjectType(GameObjectType gameObjectType) {
        this.gameObjectType = gameObjectType;
    }

    public GameObjectType getGameObjectType() {
        return gameObjectType;
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
        if (!(obj instanceof GameObject that)) {
            return false;
        }
        return id.equals(that.id);
    }

    /**
     * Sets the current sprite to the specified one.
     * @param name sprite name
     */
    public void setSprite(String name) {
        currentSprite = Sprite.getSprite(name);
    }

    private void setSprite(int index) {
        currentSprite = Sprite.getSpriteFromIndex(index);
    }

    public void setRotation(float degrees) {
        rotation = degrees;
    }

    /**
     * Draws the object on game panel.
     * @param g2d the Graphics2D component of the game panel
     */
    protected void draw(Graphics2D g2d) {
        if (currentSprite == null) {
            return;
        }
        AffineTransform at = new AffineTransform();

        Vector2 panelDimensions = new Vector2(Engine.getCurrentScene().getWidth() / 2, 
            Engine.getCurrentScene().getHeight() / 2);
        
        Vector2 panelPos = position.subtract(Camera.currentCamera.position).divide(Camera.currentCamera.zoom);
        panelPos = panelPos.add(panelDimensions);
        at.translate(panelPos.x, panelPos.y);
        at.rotate(Math.toRadians(rotation));
        at.scale(scale.x / Camera.currentCamera.zoom, scale.y / Camera.currentCamera.zoom);
        at.translate(-currentSprite.getDimensions().x * currentSprite.pivot.x,
            -currentSprite.getDimensions().y * currentSprite.pivot.y);

        g2d.drawImage(currentSprite.getImage(), at, null);
    }

    public void setup() {}

    public void onDestroy() {}

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

    public void serverUpdateFromOther(GameObject other) {
        targetPos = other.position;
        this.scale = other.scale;
        this.rotation = other.rotation;

        if (other.currentSprite != null) {
            setSprite(other.currentSprite.getIndex());
        }
    }

    public void updateFromOther(GameObject other) {
        position = other.position;
        scale = other.scale;
        rotation = other.rotation;

        if (other.currentSprite != null) {
            setSprite(other.currentSprite.getIndex());
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

    public void addCollider(Collider collider) {
        colliders.add(collider);
        collider.setParent(this);
        if (gameObjectType == GameObjectType.Server) {
            Server.addCollider(collider);
            collider.onCollision(this::onCollision);
            collider.onCollisionEnter(this::onCollisionEnter);
            collider.onCollisionExit(this::onCollisionExit);
        } else if (gameObjectType == GameObjectType.Ghost) {
            Engine.addCollider(collider);
        } else {
            Engine.addCollider(collider);
            collider.onCollision(this::onCollision);
            collider.onCollisionEnter(this::onCollisionEnter);
            collider.onCollisionExit(this::onCollisionExit);
        }
    }

    public void cleanUp() {
        for (Collider collider : colliders) {
            if (gameObjectType == GameObjectType.Server) {
                Server.removeCollider(collider);
            } else {
                Engine.removeCollider(collider);
            }
            collider.clearListeners();
        }
        colliders.clear();
    }

    public void onCollision(CollisionEvent collider) {

    }
    public void onCollisionEnter(CollisionEvent collider) {

    }
    public void onCollisionExit(CollisionEvent collider) {

    }
}

