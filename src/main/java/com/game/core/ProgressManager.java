package com.game.core;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ProgressManager {
    private static final Logger LOGGER = Logger.getLogger(ProgressManager.class.getName());
    private static final String CONFIG_DIR_NAME = ".config/googoo";
    private static final String PROGRESS_FILE_NAME = "googoo-levels.json";
    private static final int FORMAT_VERSION = 1;
    private static ProgressManager instance;

    private Map<String, LevelProgress> progressMap;
    private int lastLevelShield;
    private int lastLevelLives;

    private ProgressManager() {
        progressMap = new HashMap<>();
        lastLevelShield = 1;
        lastLevelLives = 1;
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

    private Path getLegacyFilePath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, CONFIG_DIR_NAME, "googoo-scores.ini");
    }

    private void ensureConfigDirectoryExists() {
        String userHome = System.getProperty("user.home");
        Path configDir = Paths.get(userHome, CONFIG_DIR_NAME);
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
        } catch (IOException e) {
            LOGGER.warning("Error creating config directory: " + e.getMessage());
        }
    }

    public void recordLevelProgress(int level, GameSettings.GameType gameType,
                                   int lives, double shield, int exp,
                                   int hitShieldRemaining, double timedShieldRemaining,
                                   int shipType, boolean[] ownedShips) {
        LevelProgress progress = new LevelProgress(level, gameType, lives, shield, exp,
                                                   hitShieldRemaining, timedShieldRemaining,
                                                   shipType, ownedShips);
        progressMap.put(progress.getKey(), progress);

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

    public LevelProgress getLevelProgress(int level, GameSettings.GameType gameType) {
        String key = gameType.name() + "_LEVEL_" + level;
        return progressMap.get(key);
    }

    public int getLastLevel(GameSettings.GameType gameType) {
        switch (gameType) {
            case SHIELD:
                return lastLevelShield;
            case LIVES:
                return lastLevelLives;
            default:
                return 1;
        }
    }

    public List<Integer> getAvailableLevels(GameSettings.GameType gameType) {
        List<Integer> levels = new ArrayList<>();
        int lastLevel = getLastLevel(gameType);
        for (int i = 1; i <= lastLevel; i++) {
            levels.add(i);
        }
        return levels;
    }

    public boolean hasProgress(int level, GameSettings.GameType gameType) {
        return getLevelProgress(level, gameType) != null;
    }

    private void save() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"version\": ").append(FORMAT_VERSION).append(",\n");
        sb.append("  \"lastLevelShield\": ").append(lastLevelShield).append(",\n");
        sb.append("  \"lastLevelLives\": ").append(lastLevelLives).append(",\n");
        sb.append("  \"levels\": [\n");

        boolean first = true;
        for (LevelProgress progress : progressMap.values()) {
            if (!first) sb.append(",\n");
            first = false;
            sb.append("    {");
            sb.append("\"level\": ").append(progress.getLevel());
            sb.append(", \"gameType\": \"").append(progress.getGameType().name()).append("\"");
            sb.append(", \"lives\": ").append(progress.getStartingLives());
            sb.append(", \"shield\": ").append(progress.getStartingShield());
            sb.append(", \"exp\": ").append(progress.getStartingExp());
            sb.append(", \"hitShield\": ").append(progress.getHitShieldRemaining());
            sb.append(", \"timedShield\": ").append(progress.getTimedShieldRemaining());
            sb.append(", \"shipType\": ").append(progress.getShipType());
            sb.append(", \"ownedShips\": [");
            boolean[] owned = progress.getOwnedShips();
            for (int i = 0; i < owned.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(owned[i]);
            }
            sb.append("]");
            sb.append("}");
        }

        sb.append("\n  ]\n");
        sb.append("}\n");

        try (FileWriter writer = new FileWriter(getProgressFilePath().toFile())) {
            writer.write(sb.toString());
        } catch (IOException e) {
            LOGGER.warning("Error saving progress: " + e.getMessage());
        }
    }

    private void load() {
        Path path = getProgressFilePath();

        if (!Files.exists(path)) {
            // Try migrating from legacy INI format
            if (Files.exists(getLegacyFilePath())) {
                migrateFromLegacy();
            }
            return;
        }

        try {
            String content = new String(Files.readAllBytes(path));
            parseJson(content);
        } catch (IOException e) {
            LOGGER.warning("Error loading progress: " + e.getMessage());
        }
    }

    private void parseJson(String json) {
        try {
            json = json.trim();
            if (!json.startsWith("{") || !json.endsWith("}")) return;

            lastLevelShield = parseIntField(json, "lastLevelShield", 1);
            lastLevelLives = parseIntField(json, "lastLevelLives", 1);

            int levelsStart = json.indexOf("\"levels\"");
            if (levelsStart == -1) return;

            int arrayStart = json.indexOf('[', levelsStart);
            int arrayEnd = json.lastIndexOf(']');
            if (arrayStart == -1 || arrayEnd == -1) return;

            String levelsArray = json.substring(arrayStart + 1, arrayEnd);

            int pos = 0;
            while (pos < levelsArray.length()) {
                int objStart = levelsArray.indexOf('{', pos);
                if (objStart == -1) break;
                int objEnd = levelsArray.indexOf('}', objStart);
                if (objEnd == -1) break;

                String obj = levelsArray.substring(objStart + 1, objEnd);
                parseLevelObject(obj);
                pos = objEnd + 1;
            }
        } catch (Exception e) {
            LOGGER.warning("Error parsing progress JSON: " + e.getMessage());
        }
    }

    private void parseLevelObject(String obj) {
        try {
            int level = parseIntField(obj, "level", 1);
            String gameTypeStr = parseStringField(obj, "gameType", "SHIELD");
            int lives = parseIntField(obj, "lives", 3);
            double shield = parseDoubleField(obj, "shield", 100.0);
            int exp = parseIntField(obj, "exp", 0);
            if (exp == 0) {
                exp = parseIntField(obj, "score", 0);
            }
            int hitShield = parseIntField(obj, "hitShield", 0);
            double timedShield = parseDoubleField(obj, "timedShield", 0.0);
            int shipType = parseIntField(obj, "shipType", 1);
            boolean[] ownedShips = parseOwnedShips(obj);

            GameSettings.GameType gameType;
            try {
                gameType = GameSettings.GameType.valueOf(gameTypeStr);
            } catch (IllegalArgumentException e) {
                gameType = GameSettings.GameType.SHIELD;
            }

            LevelProgress progress = new LevelProgress(level, gameType, lives, shield, exp,
                                                       hitShield, timedShield, shipType, ownedShips);
            progressMap.put(progress.getKey(), progress);
        } catch (Exception e) {
            LOGGER.warning("Error parsing level entry: " + e.getMessage());
        }
    }

    private boolean[] parseOwnedShips(String obj) {
        boolean[] result = {true, false, false, false, false, false, false, false};
        int idx = obj.indexOf("\"ownedShips\"");
        if (idx == -1) return result;
        int arrStart = obj.indexOf('[', idx);
        int arrEnd = obj.indexOf(']', arrStart);
        if (arrStart == -1 || arrEnd == -1) return result;
        String arr = obj.substring(arrStart + 1, arrEnd);
        String[] parts = arr.split(",");
        for (int i = 0; i < Math.min(parts.length, 8); i++) {
            result[i] = parts[i].trim().equals("true");
        }
        return result;
    }

    private int parseIntField(String json, String field, int defaultValue) {
        String value = extractFieldValue(json, field);
        if (value == null) return defaultValue;
        try {
            if (value.contains(".")) return (int) Double.parseDouble(value);
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private double parseDoubleField(String json, String field, double defaultValue) {
        String value = extractFieldValue(json, field);
        if (value == null) return defaultValue;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String parseStringField(String json, String field, String defaultValue) {
        int fieldIdx = json.indexOf("\"" + field + "\"");
        if (fieldIdx == -1) return defaultValue;
        int colonIdx = json.indexOf(':', fieldIdx);
        if (colonIdx == -1) return defaultValue;
        int quoteStart = json.indexOf('"', colonIdx + 1);
        if (quoteStart == -1) return defaultValue;
        int quoteEnd = json.indexOf('"', quoteStart + 1);
        if (quoteEnd == -1) return defaultValue;
        return json.substring(quoteStart + 1, quoteEnd);
    }

    private String extractFieldValue(String json, String field) {
        int fieldIdx = json.indexOf("\"" + field + "\"");
        if (fieldIdx == -1) return null;
        int colonIdx = json.indexOf(':', fieldIdx);
        if (colonIdx == -1) return null;

        int start = colonIdx + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;

        int end = start;
        while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}'
               && json.charAt(end) != '\n' && json.charAt(end) != ']') end++;

        return json.substring(start, end).trim();
    }

    private void migrateFromLegacy() {
        LOGGER.info("Migrating from legacy googoo-scores.ini to googoo-levels.json");
        Path legacyPath = getLegacyFilePath();

        try {
            java.util.Properties props = new java.util.Properties();
            try (FileReader reader = new FileReader(legacyPath.toFile())) {
                props.load(reader);
            }

            try {
                String shieldStr = props.getProperty("LAST_LEVEL_SHIELD", "1");
                if (shieldStr.contains(".")) {
                    lastLevelShield = (int) Double.parseDouble(shieldStr);
                } else {
                    lastLevelShield = Integer.parseInt(shieldStr);
                }
            } catch (NumberFormatException e) {
                lastLevelShield = 1;
            }

            try {
                lastLevelLives = Integer.parseInt(props.getProperty("LAST_LEVEL_LIVES", "1"));
            } catch (NumberFormatException e) {
                lastLevelLives = 1;
            }

            for (String key : props.stringPropertyNames()) {
                if (key.endsWith("_LIVES") && !key.startsWith("LAST_LEVEL")) {
                    String prefix = key.substring(0, key.length() - "_LIVES".length());
                    try {
                        int lives = Integer.parseInt(props.getProperty(prefix + "_LIVES", "3"));
                        double shield = Double.parseDouble(props.getProperty(prefix + "_SHIELD", "100.0"));
                        int exp = Integer.parseInt(props.getProperty(prefix + "_SCORE", "0"));

                        int level = LevelProgress.getLevelFromKey(prefix);
                        GameSettings.GameType gameType = LevelProgress.getGameTypeFromKey(prefix);

                        LevelProgress progress = new LevelProgress(level, gameType, lives, shield, exp, 0, 0.0, 1, null);
                        progressMap.put(prefix, progress);
                    } catch (NumberFormatException e) {
                        LOGGER.warning("Error migrating level " + prefix + ": " + e.getMessage());
                    }
                }
            }

            save();
            LOGGER.info("Migration complete. Old file preserved at: " + legacyPath);
        } catch (IOException e) {
            LOGGER.warning("Error migrating from legacy format: " + e.getMessage());
        }
    }

    public void clearAllProgress() {
        progressMap.clear();
        lastLevelShield = 1;
        lastLevelLives = 1;
        save();
    }
}
