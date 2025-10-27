package GameObjects;

import Engine.GameObject;

import java.util.HashMap;

public class WeaponManager {
    private static GameObject weaponOwner;

    public static void weaponPickup(GameObject gameObject) {
        weaponOwner = gameObject;
    }

    public static void dropWeapon() {
        weaponOwner = null;
    }

    public static boolean hasWeapon(GameObject gameObject) {
        return weaponOwner == gameObject;
    }

}
