package GameObjects.UI;

import Engine.GameObject;
import Engine.Scene;
import Engine.Sprite;
import Engine.Vector2;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Displays weapon icons below minimap.
 */
public class WeaponSelect extends GameObject {
    static WeaponSelect weaponIcon;
    static int selectedWeapon = -1;
    Sprite sprite;

    /**
     * Create icons in scene.
     * @param scene scene to create icons in
     */
    public static void createIcons(Scene scene) {
        WeaponSelect pistol = new WeaponSelect();

        weaponIcon = pistol;
        pistol.position.y = 40;
        pistol.position.x = 40;
        selectWeapon(-1);

        scene.addObject(pistol);
    }

    /**
     * Set the currently selected weapon.
     * @param weaponIndex weapon index 0 - pistol, 1 - shotgun, 2 - rifle
     */
    public static void selectWeapon(int weaponIndex) {
        if (weaponIndex == selectedWeapon) {
            return;
        }
        switch (weaponIndex) {
            case 0:
                weaponIcon.setSprite("pistol_icon");
                break;
            case 1:
                weaponIcon.setSprite("shotgun_icon");
                break;
            case 2:
                weaponIcon.setSprite("rifle_icon");
                break;
            default:
                weaponIcon.setSprite(null);
                weaponIndex = -1;
                break;
        }
        weaponIcon.sprite = weaponIcon.currentSprite;
        weaponIcon.setSprite(null);
        selectedWeapon = weaponIndex;
    }

    @Override
    protected void draw(Graphics2D g2d) {
        if (sprite == null) {
            return;
        }
        AffineTransform at = new AffineTransform();
        at.translate(position.x, position.y);
        at.rotate(Math.toRadians(rotation));
        at.scale(scale.x, scale.y);
        at.translate(-sprite.getDimensions().x * 0.5f,
            -sprite.getDimensions().y * 0.5f);
        g2d.drawImage(sprite.getImage(), at, null);
    }

    /**
     * Create a weapon icon.
     */
    WeaponSelect() {
        scale = new Vector2(0.05f, 0.05f);
        setLayer(1000);
    }
}
