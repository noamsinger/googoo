package com.game.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

import java.util.logging.Logger;

public class GemSpriteLoader {
    private static final Logger LOGGER = Logger.getLogger(GemSpriteLoader.class.getName());
    private static final int SPRITE_SIZE = 80;
    private static final int FRAMES = 16;
    private static final int COLS = 4;
    private static final int GEM_TYPES = 16;

    private static Image[] gemSpritesCache = null;

    public static Image getGemFrame(int gemType, int frameIndex) {
        if (gemSpritesCache == null) {
            loadGemSprites();
        }

        if (gemSpritesCache != null && gemType >= 0 && gemType < GEM_TYPES &&
            frameIndex >= 0 && frameIndex < FRAMES) {
            return gemSpritesCache[gemType * FRAMES + frameIndex];
        }

        return null;
    }

    public static int getFrameCount() {
        return FRAMES;
    }

    public static int getGemTypeCount() {
        return GEM_TYPES;
    }

    private static void loadGemSprites() {
        gemSpritesCache = new Image[GEM_TYPES * FRAMES]; // 16 gem types * 16 frames each

        for (int type = 0; type < GEM_TYPES; type++) {
            String path = "/images/gem_sheet_" + type + ".png";
            try {
                Image sheet = new Image(GemSpriteLoader.class.getResourceAsStream(path));

                if (sheet.isError()) {
                    LOGGER.warning("Error loading gem sprite sheet: " + path);
                    continue;
                }

                for (int frame = 0; frame < FRAMES; frame++) {
                    int col = frame % COLS;
                    int row = frame / COLS;
                    int x = col * SPRITE_SIZE;
                    int y = row * SPRITE_SIZE;

                    PixelReader reader = sheet.getPixelReader();
                    WritableImage frameImage = new WritableImage(
                        reader, x, y, SPRITE_SIZE, SPRITE_SIZE
                    );
                    gemSpritesCache[type * FRAMES + frame] = frameImage;
                }

                LOGGER.fine("Gem sprite sheet " + type + " loaded successfully: " + FRAMES + " frames");
            } catch (Exception e) {
                LOGGER.warning("Exception loading gem sprite sheet " + type + ": " + e.getMessage());
            }
        }
    }
}
