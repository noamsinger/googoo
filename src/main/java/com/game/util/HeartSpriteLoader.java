package com.game.util;

import javafx.scene.image.Image;

public class HeartSpriteLoader {
    private static final int SPRITE_SIZE = 80;
    private static final int FRAMES = 16;
    private static final int COLS = 4;

    private static Image[] heartSpritesCache = null;

    public static Image getHeartFrame(int frameIndex) {
        if (heartSpritesCache == null) {
            loadHeartSprites();
        }

        if (heartSpritesCache != null && frameIndex >= 0 && frameIndex < FRAMES) {
            return heartSpritesCache[frameIndex];
        }

        return null;
    }

    public static int getFrameCount() {
        return FRAMES;
    }

    private static void loadHeartSprites() {
        heartSpritesCache = SpriteSheetLoader.loadSpriteSheet(
            "/images/heart_sheet.png", FRAMES, COLS, SPRITE_SIZE, HeartSpriteLoader.class
        );
    }
}
