package GameObjects;

import Engine.Camera;
import Engine.GameObject;
import Engine.Inputs.Input;
import Engine.Physics.CircleCollider;
import Engine.Physics.Collider;
import Engine.Physics.ColliderType;
import Engine.Physics.CollisionEvent;
import Engine.Vector2;

public class MouseFollower extends GameObject {

    @Override
    public void setup() {
        setSprite("zombie");
        scale = new Vector2(0.1f, 0.1f);
        addCollider(new CircleCollider(10f, ColliderType.None));
        setLayer(1000);
    }

    @Override
    public void update(float deltaTime) {
        position = Input.mouse.getWorldPosition();
    }

    @Override
    public void onCollision(CollisionEvent e) {
        if (e.getOther().getParent() instanceof RectangleThing) {
            setSprite("zombie_hit");
        }
        rotation = e.getNormal().getRotation();
    }

    @Override
    public void onCollisionExit(CollisionEvent e) {
        if (e.getOther().getParent() instanceof RectangleThing) {
            setSprite("zombie");
        }
    }
}
