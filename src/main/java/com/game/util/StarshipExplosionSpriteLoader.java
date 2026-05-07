package com.game.util;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.util.logging.Logger;

public class StarshipExplosionSpriteLoader {
    private static final Logger LOGGER = Logger.getLogger(StarshipExplosionSpriteLoader.class.getName());
    private static final int FRAME_COUNT = 256;
    private static final int FRAME_SIZE = 300;
    private static final int FRAMES_PER_ROW = 16;

    private static Image spriteSheet = null;
    private static Image[] explosionFrames = null;

    public static void loadExplosionSprites() {
        if (explosionFrames != null) {
            return; // Already loaded
        }

        try {
            spriteSheet = new Image(StarshipExplosionSpriteLoader.class.getResourceAsStream("/images/starship_explosion_sheet.png"));
            LOGGER.fine("Starship explosion sprite sheet loaded successfully: " + FRAME_COUNT + " frames");

            explosionFrames = new Image[FRAME_COUNT];

            for (int i = 0; i < FRAME_COUNT; i++) {
                int col = i % FRAMES_PER_ROW;
                int row = i / FRAMES_PER_ROW;
                int x = col * FRAME_SIZE;
                int y = row * FRAME_SIZE;

                WritableImage frameImage = new WritableImage(
                    spriteSheet.getPixelReader(),
                    x, y,
                    FRAME_SIZE, FRAME_SIZE
                );
                explosionFrames[i] = frameImage;
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to load starship explosion sprite sheet: " + e.getMessage());
            e.printStackTrace();
        }
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
