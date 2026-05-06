package com.game.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks progress for each level in each game mode.
 * Stores starting lives/shield and score for each level.
 */
public class LevelProgress {
    private int level;
    private GameSettings.GameType gameType;
    private int startingLives;
    private double startingShield;
    private int startingScore;

    public LevelProgress(int level, GameSettings.GameType gameType,
                        int startingLives, double startingShield, int startingScore) {
        this.level = level;
        this.gameType = gameType;
        this.startingLives = startingLives;
        this.startingShield = startingShield;
        this.startingScore = startingScore;
    }

    public int getLevel() {
        return level;
    }

    public GameSettings.GameType getGameType() {
        return gameType;
    }

    public int getStartingLives() {
        return startingLives;
    }

    public double getStartingShield() {
        return startingShield;
    }

    public int getStartingScore() {
        return startingScore;
    }

    public void setStartingLives(int lives) {
        this.startingLives = lives;
    }

    public void setStartingShield(double shield) {
        this.startingShield = shield;
    }

    public void setStartingScore(int score) {
        this.startingScore = score;
    }

    /**
     * Creates a unique key for this level and game type.
     */
    public String getKey() {
        return gameType.name() + "_LEVEL_" + level;
    }

    /**
     * Parses a key to extract level number.
     */
    public static int getLevelFromKey(String key) {
        String[] parts = key.split("_LEVEL_");
        if (parts.length == 2) {
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }

    /**
     * Parses a key to extract game type.
     */
    public static GameSettings.GameType getGameTypeFromKey(String key) {
        String[] parts = key.split("_LEVEL_");
        if (parts.length == 2) {
            try {
                return GameSettings.GameType.valueOf(parts[0]);
            } catch (IllegalArgumentException e) {
                return GameSettings.GameType.SHIELD;
            }
        }
        return GameSettings.GameType.SHIELD;
    }
}
