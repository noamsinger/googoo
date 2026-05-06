package com.game.core;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Manages saving and loading of level progress from googoo-scores.ini file.
 */
public class ProgressManager {
    private static final String CONFIG_DIR_NAME = ".config/googoo";
    private static final String PROGRESS_FILE_NAME = "googoo-scores.ini";
    private static ProgressManager instance;

    private Map<String, LevelProgress> progressMap;
    private int lastLevelShield;
    private int lastLevelLives;
    private int lastLevelLiveForever;

    private ProgressManager() {
        progressMap = new HashMap<>();
        lastLevelShield = 1;
        lastLevelLives = 1;
        lastLevelLiveForever = 1;
        ensureConfigDirectoryExists();
        load();
    }

    public static ProgressManager getInstance() {
        if (instance == null) {
            instance = new ProgressManager();
        }
        return instance;
    }

    private Path getProgressFilePath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, CONFIG_DIR_NAME, PROGRESS_FILE_NAME);
    }

    private void ensureConfigDirectoryExists() {
        String userHome = System.getProperty("user.home");
        Path configDir = Paths.get(userHome, CONFIG_DIR_NAME);
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
        } catch (IOException e) {
            System.err.println("Error creating config directory: " + e.getMessage());
        }
    }

    /**
     * Records progress for a specific level and game type.
     * Does not record progress for LIVE_FOREVER mode.
     */
    public void recordLevelProgress(int level, GameSettings.GameType gameType,
                                   int lives, double shield, int score) {
        // Don't record progress for Live Forever mode
        if (gameType == GameSettings.GameType.LIVE_FOREVER) {
            return;
        }

        LevelProgress progress = new LevelProgress(level, gameType, lives, shield, score);
        progressMap.put(progress.getKey(), progress);

        // Update last level reached for this game type
        switch (gameType) {
            case SHIELD:
                if (level > lastLevelShield) lastLevelShield = level;
                break;
            case LIVES:
                if (level > lastLevelLives) lastLevelLives = level;
                break;
        }

        save();
    }

    /**
     * Gets progress for a specific level and game type.
     * Returns null if no progress exists.
     */
    public LevelProgress getLevelProgress(int level, GameSettings.GameType gameType) {
        String key = gameType.name() + "_LEVEL_" + level;
        return progressMap.get(key);
    }

    /**
     * Gets the highest level reached for a game type.
     */
    public int getLastLevel(GameSettings.GameType gameType) {
        switch (gameType) {
            case SHIELD:
                return lastLevelShield;
            case LIVES:
                return lastLevelLives;
            case LIVE_FOREVER:
                return lastLevelLiveForever;
            default:
                return 1;
        }
    }

    /**
     * Gets all levels with recorded progress for a game type.
     * Returns levels 1 through the last level reached.
     * For LIVE_FOREVER mode, only returns level 1.
     */
    public java.util.List<Integer> getAvailableLevels(GameSettings.GameType gameType) {
        java.util.List<Integer> levels = new java.util.ArrayList<>();

        // Live Forever mode only has level 1 available
        if (gameType == GameSettings.GameType.LIVE_FOREVER) {
            levels.add(1);
            return levels;
        }

        int lastLevel = getLastLevel(gameType);

        // Add all levels from 1 to last level reached
        for (int i = 1; i <= lastLevel; i++) {
            levels.add(i);
        }

        return levels;
    }

    /**
     * Checks if a level has recorded progress for a game type.
     */
    public boolean hasProgress(int level, GameSettings.GameType gameType) {
        return getLevelProgress(level, gameType) != null;
    }

    /**
     * Saves progress to googoo-scores.ini file in ~/.config/googoo/
     */
    private void save() {
        Properties props = new Properties();

        // Save last levels
        props.setProperty("LAST_LEVEL_SHIELD", String.valueOf(lastLevelShield));
        props.setProperty("LAST_LEVEL_LIVES", String.valueOf(lastLevelLives));
        props.setProperty("LAST_LEVEL_LIVE_FOREVER", String.valueOf(lastLevelLiveForever));

        // Save all level progress
        for (Map.Entry<String, LevelProgress> entry : progressMap.entrySet()) {
            LevelProgress progress = entry.getValue();
            String prefix = entry.getKey();

            props.setProperty(prefix + "_LIVES", String.valueOf(progress.getStartingLives()));
            props.setProperty(prefix + "_SHIELD", String.valueOf(progress.getStartingShield()));
            props.setProperty(prefix + "_SCORE", String.valueOf(progress.getStartingScore()));
        }

        try (FileWriter writer = new FileWriter(getProgressFilePath().toFile())) {
            props.store(writer, "Googoo Game Progress");
        } catch (IOException e) {
            System.err.println("Error saving progress: " + e.getMessage());
        }
    }

    /**
     * Loads progress from googoo-scores.ini file in ~/.config/googoo/
     */
    private void load() {
        Path path = getProgressFilePath();
        if (!Files.exists(path)) {
            return; // No file yet, start fresh
        }

        Properties props = new Properties();
        try (FileReader reader = new FileReader(path.toFile())) {
            props.load(reader);

            // Load last levels
            lastLevelShield = Integer.parseInt(props.getProperty("LAST_LEVEL_SHIELD", "1"));
            lastLevelLives = Integer.parseInt(props.getProperty("LAST_LEVEL_LIVES", "1"));
            lastLevelLiveForever = Integer.parseInt(props.getProperty("LAST_LEVEL_LIVE_FOREVER", "1"));

            // Load all level progress
            for (String key : props.stringPropertyNames()) {
                if (key.endsWith("_LIVES")) {
                    String prefix = key.substring(0, key.length() - "_LIVES".length());

                    int lives = Integer.parseInt(props.getProperty(prefix + "_LIVES", "3"));
                    double shield = Double.parseDouble(props.getProperty(prefix + "_SHIELD", "100.0"));
                    int score = Integer.parseInt(props.getProperty(prefix + "_SCORE", "0"));

                    int level = LevelProgress.getLevelFromKey(prefix);
                    GameSettings.GameType gameType = LevelProgress.getGameTypeFromKey(prefix);

                    LevelProgress progress = new LevelProgress(level, gameType, lives, shield, score);
                    progressMap.put(prefix, progress);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading progress: " + e.getMessage());
        }
    }

    /**
     * Clears all progress (for testing or reset).
     */
    public void clearAllProgress() {
        progressMap.clear();
        lastLevelShield = 1;
        lastLevelLives = 1;
        lastLevelLiveForever = 1;
        save();
    }
}
