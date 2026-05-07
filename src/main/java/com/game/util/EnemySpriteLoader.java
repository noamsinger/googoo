package com.game.util;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class EnemySpriteLoader {
    private static final Logger LOGGER = Logger.getLogger(EnemySpriteLoader.class.getName());
    private static final int SPRITE_SIZE = 80;
    private static final int FRAMES = 16;
    private static final int COLS = 4;
    private static final int ENEMY_TYPES = 16;

    // Cache for all enemy type sprite sheets
    private static Map<Integer, Image[]> enemyTypeFrames = new HashMap<>();

    public static Image getEnemyFrame(int enemyType, int frameIndex) {
        if (!enemyTypeFrames.containsKey(enemyType)) {
            loadSpriteSheet(enemyType);
        }

        Image[] frames = enemyTypeFrames.get(enemyType);
        if (frames != null && frameIndex >= 0 && frameIndex < FRAMES) {
            return frames[frameIndex];
        }

        return null;
    }

    public static int getFrameCount() {
        return FRAMES;
    }

    public static int getEnemyTypeCount() {
        return ENEMY_TYPES;
    }

    private static void loadSpriteSheet(int enemyType) {
        try {
            String imagePath = "/images/enemy_sheet_" + enemyType + ".png";
            Image spriteSheet = new Image(EnemySpriteLoader.class.getResourceAsStream(imagePath));

            if (spriteSheet.isError()) {
                LOGGER.warning("Error loading enemy sprite sheet from: " + imagePath);
                enemyTypeFrames.put(enemyType, null);
                return;
            }

            // Extract individual frames from sprite sheet
            Image[] frames = new Image[FRAMES];

            for (int i = 0; i < FRAMES; i++) {
                int col = i % COLS;
                int row = i / COLS;
                int x = col * SPRITE_SIZE;
                int y = row * SPRITE_SIZE;

                // Extract frame
                WritableImage frame = new WritableImage(
                    spriteSheet.getPixelReader(),
                    x, y,
                    SPRITE_SIZE, SPRITE_SIZE
                );
                frames[i] = frame;
            }

            enemyTypeFrames.put(enemyType, frames);
            LOGGER.fine("Enemy sprite sheet " + enemyType + " loaded successfully: " + FRAMES + " frames");

        } catch (Exception e) {
            LOGGER.warning("Exception loading enemy sprite sheet " + enemyType + ": " + e.getMessage());
            enemyTypeFrames.put(enemyType, null);
        }
    }
}
