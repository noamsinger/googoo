package com.game.util;

import javafx.scene.image.Image;

public class StarshipExplosionSpriteLoader {
    private static final int FRAME_COUNT = 256;
    private static final int FRAME_SIZE = 300;
    private static final int FRAMES_PER_ROW = 16;

    private static Image[] explosionFrames = null;

    public static void loadExplosionSprites() {
        if (explosionFrames != null) {
            return; // Already loaded
        }

        explosionFrames = SpriteSheetLoader.loadSpriteSheet(
            "/images/starship_explosion_sheet.png",
            FRAME_COUNT,
            FRAMES_PER_ROW,
            FRAME_SIZE,
            StarshipExplosionSpriteLoader.class
        );
    }

    public static Image getExplosionFrame(int frame) {
        if (explosionFrames == null) {
            loadExplosionSprites();
        }

        if (frame < 0 || frame >= FRAME_COUNT) {
            return null;
        }

        return explosionFrames[frame];
    }

    public static int getFrameCount() {
        return FRAME_COUNT;
    }
}
