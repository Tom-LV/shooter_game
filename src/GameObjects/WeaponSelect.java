package GameObjects;

import Engine.GameObject;
import Engine.Scene;
import Engine.Vector2;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Displays weapon icons below minimap.
 */
public class WeaponSelect extends GameObject {
    static WeaponSelect weaponIcon;
    static int selectedWeapon = -1;
    BufferedImage image;

    /**
     * Create icons in scene.
     * @param scene scene to create icons in
     */
    public static void createIcons(Scene scene) {
        WeaponSelect pistol = new WeaponSelect();

        weaponIcon = pistol;
        pistol.position.y = 40;
        pistol.position.x = 40;
        selectWeapon(0);

        scene.addNetworkObject(pistol);
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
                break;
        }
        if (weaponIcon.currentSprite == null) {
            weaponIcon.image = null;
            selectedWeapon = -1;
            return;
        }
        weaponIcon.image = weaponIcon.currentSprite.getImage();
        weaponIcon.setSprite(null);
        selectedWeapon = weaponIndex;
    }

    @Override
    protected void draw(Graphics2D g2d) {
        if (image == null) {
            return;
        }
        AffineTransform at = new AffineTransform();
        at.translate(position.x, position.y);
        at.rotate(Math.toRadians(rotation));
        at.scale(scale.x, scale.y);
        at.translate(-image.getWidth() * 0.5f, 
            -image.getHeight() * 0.5f);
        g2d.drawImage(image, at, null);
    }

    /**
     * Create a weapon icon.
     */
    WeaponSelect() {
        scale = new Vector2(0.05f, 0.05f);
        setLayer(1000);
    }
}
