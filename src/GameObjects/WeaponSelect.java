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
    static ArrayList<WeaponSelect> weaponIcons = new ArrayList<>();
    static int selectedWeapon = -1;
    BufferedImage image;

    /**
     * Create icons in scene.
     * @param scene scene to create icons in
     */
    public static void createIcons(Scene scene) {
        WeaponSelect pistol = new WeaponSelect("pistol");
        WeaponSelect shotgun = new WeaponSelect("shotgun");
        WeaponSelect rifle = new WeaponSelect("rifle");

        weaponIcons.add(pistol);
        weaponIcons.add(shotgun);
        weaponIcons.add(rifle);
        selectWeapon(0);

        scene.addObject(pistol);
        scene.addObject(shotgun);
        scene.addObject(rifle);

        float xPos = 40;

        for (WeaponSelect weapon : weaponIcons) {
            weapon.position.y = 225;
            weapon.position.x = xPos;
            xPos += 65;
        }
    }

    /**
     * Set the currently selected weapon.
     * @param weaponIndex weapon index 0 - pistol, 1 - shotgun, 2 - rifle
     */
    public static void selectWeapon(int weaponIndex) {
        if (weaponIndex == selectedWeapon) {
            return;
        }
        for (WeaponSelect weapon : weaponIcons) {
            weapon.scale = new Vector2(0.04f, 0.04f);
        }

        WeaponSelect weapon = weaponIcons.get(weaponIndex);
        weapon.scale = new Vector2(0.05f, 0.05f);
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
     * @param imageName icon image
     */
    WeaponSelect(String imageName) {
        scale = new Vector2(0.05f, 0.05f);
        setSprite(imageName);
        image = currentSprite.getImage();
        setSprite(null);
        setLayer(1000);
    }
}
