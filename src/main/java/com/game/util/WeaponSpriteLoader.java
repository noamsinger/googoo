package com.game.util;

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

public class WeaponSpriteLoader {
    private static final int SPRITE_SIZE = 80;
    private static final int FRAMES = 16;
    private static final int COLS = 4;
    public static final int WEAPON_TYPE_COUNT = 8;

    private static Map<Integer, Image[]> weaponTypeFrames = new HashMap<>();

    public static Image getWeaponFrame(int weaponType, int frameIndex) {
        if (!weaponTypeFrames.containsKey(weaponType)) {
            loadSpriteSheet(weaponType);
        }

        Image[] frames = weaponTypeFrames.get(weaponType);
        if (frames != null && frameIndex >= 0 && frameIndex < FRAMES) {
            return frames[frameIndex];
        }

        return null;
    }

    public static int getFrameCount() {
        return FRAMES;
    }

    public static int getWeaponTypeCount() {
        return WEAPON_TYPE_COUNT;
    }

    private static void loadSpriteSheet(int weaponType) {
        String imagePath = "/images/weapon_sheet_" + weaponType + ".png";
        Image[] frames = SpriteSheetLoader.loadSpriteSheet(imagePath, FRAMES, COLS, SPRITE_SIZE, WeaponSpriteLoader.class);
        weaponTypeFrames.put(weaponType, frames);
    }
}
