package com.game.util;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

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
        try {
            String imagePath = "/images/blackhole_sheet_" + colorScheme + ".png";
            Image spriteSheet = new Image(BlackHoleSpriteLoader.class.getResourceAsStream(imagePath));

            if (spriteSheet.isError()) {
                System.err.println("Error loading black hole sprite sheet from: " + imagePath);
                colorSchemeFrames.put(colorScheme, null);
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

            colorSchemeFrames.put(colorScheme, frames);
            System.out.println("Black hole sprite sheet " + colorScheme + " loaded successfully: " + FRAMES + " frames");

        } catch (Exception e) {
            System.err.println("Exception loading black hole sprite sheet " + colorScheme + ": " + e.getMessage());
            colorSchemeFrames.put(colorScheme, null);
        }
    }
}
