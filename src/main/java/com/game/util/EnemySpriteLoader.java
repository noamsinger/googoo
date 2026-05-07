package com.game.util;

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

public class EnemySpriteLoader {
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
        String imagePath = "/images/enemy_sheet_" + enemyType + ".png";
        Image[] frames = SpriteSheetLoader.loadSpriteSheet(imagePath, FRAMES, COLS, SPRITE_SIZE, EnemySpriteLoader.class);
        enemyTypeFrames.put(enemyType, frames);
    }
}
