package com.game.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

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
        String path = "/images/heart_sheet.png";
        try {
            Image sheet = new Image(HeartSpriteLoader.class.getResourceAsStream(path));

            if (sheet.isError()) {
                System.err.println("Error loading heart sprite sheet: " + path);
                heartSpritesCache = null;
                return;
            }

            heartSpritesCache = new Image[FRAMES];
            for (int frame = 0; frame < FRAMES; frame++) {
                int col = frame % COLS;
                int row = frame / COLS;
                int x = col * SPRITE_SIZE;
                int y = row * SPRITE_SIZE;

                PixelReader reader = sheet.getPixelReader();
                WritableImage frameImage = new WritableImage(
                    reader, x, y, SPRITE_SIZE, SPRITE_SIZE
                );
                heartSpritesCache[frame] = frameImage;
            }

            System.out.println("Heart sprite sheet loaded successfully: " + FRAMES + " frames");
        } catch (Exception e) {
            System.err.println("Exception loading heart sprite sheet: " + e.getMessage());
            heartSpritesCache = null;
        }
    }
}
