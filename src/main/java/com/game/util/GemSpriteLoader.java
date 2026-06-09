package com.game.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GemSpriteLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(GemSpriteLoader.class);
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
        gemSpritesCache = new Image[GEM_TYPES * FRAMES];

        for (int type = 0; type < GEM_TYPES; type++) {
            String path = "/images/gem_sheet_" + type + ".png";
            Image[] frames = SpriteSheetLoader.loadSpriteSheet(path, FRAMES, COLS, SPRITE_SIZE, GemSpriteLoader.class);

            if (frames != null) {
                System.arraycopy(frames, 0, gemSpritesCache, type * FRAMES, FRAMES);
            }
        }
    }
}
