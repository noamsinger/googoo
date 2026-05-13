package com.game.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

import java.util.logging.Logger;

/**
 * Utility class for loading sprite sheets and extracting individual frames.
 * Provides common functionality used by all sprite loaders.
 */
public class SpriteSheetLoader {
    private static final Logger LOGGER = Logger.getLogger(SpriteSheetLoader.class.getName());

    /**
     * Loads a sprite sheet and extracts frames from it.
     *
     * @param resourcePath Path to the sprite sheet image resource
     * @param frameCount Number of frames in the sprite sheet
     * @param cols Number of columns in the sprite sheet grid
     * @param spriteSize Size of each sprite (width and height, assumed square)
     * @param loaderClass Class requesting the load (for resource stream and logging)
     * @return Array of extracted frame images, or null if loading fails
     */
    public static Image[] loadSpriteSheet(String resourcePath, int frameCount, int cols, int spriteSize, Class<?> loaderClass) {
        try {
            Image sheet = new Image(loaderClass.getResourceAsStream(resourcePath));

            if (sheet.isError()) {
                LOGGER.warning("Error loading sprite sheet: " + resourcePath);
                return null;
            }

            Image[] frames = new Image[frameCount];
            PixelReader reader = sheet.getPixelReader();

            for (int i = 0; i < frameCount; i++) {
                int col = i % cols;
                int row = i / cols;
                int x = col * spriteSize;
                int y = row * spriteSize;

                WritableImage frameImage = new WritableImage(reader, x, y, spriteSize, spriteSize);
                frames[i] = frameImage;
            }

            LOGGER.fine("Sprite sheet loaded successfully: " + resourcePath + " (" + frameCount + " frames)");
            return frames;

        } catch (Exception e) {
            LOGGER.warning("Exception loading sprite sheet " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Loads a sprite sheet with non-square sprites.
     *
     * @param resourcePath Path to the sprite sheet image resource
     * @param frameCount Number of frames in the sprite sheet
     * @param cols Number of columns in the sprite sheet grid
     * @param spriteWidth Width of each sprite
     * @param spriteHeight Height of each sprite
     * @param loaderClass Class requesting the load (for resource stream and logging)
     * @return Array of extracted frame images, or null if loading fails
     */
    public static Image[] loadSpriteSheet(String resourcePath, int frameCount, int cols,
                                          int spriteWidth, int spriteHeight, Class<?> loaderClass) {
        try {
            Image sheet = new Image(loaderClass.getResourceAsStream(resourcePath));

            if (sheet.isError()) {
                LOGGER.warning("Error loading sprite sheet: " + resourcePath);
                return null;
            }

            Image[] frames = new Image[frameCount];
            PixelReader reader = sheet.getPixelReader();

            for (int i = 0; i < frameCount; i++) {
                int col = i % cols;
                int row = i / cols;
                int x = col * spriteWidth;
                int y = row * spriteHeight;

                WritableImage frameImage = new WritableImage(reader, x, y, spriteWidth, spriteHeight);
                frames[i] = frameImage;
            }

            LOGGER.fine("Sprite sheet loaded successfully: " + resourcePath + " (" + frameCount + " frames)");
            return frames;

        } catch (Exception e) {
            LOGGER.warning("Exception loading sprite sheet " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }
}
