package com.game.util;

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

public class BlackHoleSpriteLoader {
    private static final int SPRITE_SIZE = 120;
    private static final int FRAMES = 16;
    private static final int COLS = 4;
    private static final int COLOR_SCHEMES = 16;

    // Cache for all color scheme sprite sheets
    private static Map<Integer, Image[]> colorSchemeFrames = new HashMap<>();

    public static Image getBlackHoleFrame(int colorScheme, int frameIndex) {
        if (!colorSchemeFrames.containsKey(colorScheme)) {
            loadSpriteSheet(colorScheme);
        }

        Image[] frames = colorSchemeFrames.get(colorScheme);
        if (frames != null && frameIndex >= 0 && frameIndex < FRAMES) {
            return frames[frameIndex];
        }

        return null;
    }

    public static int getFrameCount() {
        return FRAMES;
    }

    public static int getColorSchemeCount() {
        return COLOR_SCHEMES;
    }

    private static void loadSpriteSheet(int colorScheme) {
        String imagePath = "/images/blackhole_sheet_" + colorScheme + ".png";
        Image[] frames = SpriteSheetLoader.loadSpriteSheet(imagePath, FRAMES, COLS, SPRITE_SIZE, BlackHoleSpriteLoader.class);
        colorSchemeFrames.put(colorScheme, frames);
    }
}
