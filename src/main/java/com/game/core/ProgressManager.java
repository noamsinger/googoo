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
    private static final int FORMAT_VERSION = 2;
    private static ProgressManager instance;

    private Map<String, LevelProgress> progressMap;
    private int lastLevel;

    private ProgressManager() {
        progressMap = new HashMap<>();
        lastLevel = 1;
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

    public void recordLevelProgress(int level,
                                   int lives, int exp,
                                   int shipType, boolean[] ownedShips,
                                   int weaponMode, int weapon,
                                   boolean[] ownedWeaponModes, boolean[] ownedWeapons,
                                   int hitShieldRemaining, double timedShieldRemaining,
                                   String shieldType) {
        LevelProgress progress = new LevelProgress(level, lives, exp, shipType, ownedShips,
                weaponMode, weapon, ownedWeaponModes, ownedWeapons,
                hitShieldRemaining, timedShieldRemaining, shieldType);
        progressMap.put(progress.getKey(), progress);

        if (level > lastLevel) lastLevel = level;

        save();
    }

    public LevelProgress getLevelProgress(int level) {
        return progressMap.get("LEVEL_" + level);
    }

    public int getLastLevel() {
        return lastLevel;
    }

    public List<Integer> getAvailableLevels() {
        List<Integer> levels = new ArrayList<>();
        for (int i = 1; i <= lastLevel; i++) {
            levels.add(i);
        }
        return levels;
    }

    public boolean hasProgress(int level) {
        return getLevelProgress(level) != null;
    }

    private void save() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"version\": ").append(FORMAT_VERSION).append(",\n");
        sb.append("  \"lastLevel\": ").append(lastLevel).append(",\n");
        sb.append("  \"levels\": [\n");

        boolean first = true;
        for (LevelProgress progress : progressMap.values()) {
            if (!first) sb.append(",\n");
            first = false;
            sb.append("    {");
            sb.append("\"level\": ").append(progress.getLevel());
            sb.append(", \"lives\": ").append(progress.getLives());
            sb.append(", \"exp\": ").append(progress.getExp());
            sb.append(", \"shipType\": ").append(progress.getShipType());
            sb.append(", \"ownedShips\": [");
            boolean[] owned = progress.getOwnedShips();
            for (int i = 0; i < owned.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(owned[i]);
            }
            sb.append("]");
            sb.append(", \"weaponMode\": ").append(progress.getWeaponMode());
            sb.append(", \"weapon\": ").append(progress.getWeapon());
            sb.append(", \"ownedWeaponModes\": [");
            boolean[] modes = progress.getOwnedWeaponModes();
            for (int i = 0; i < modes.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(modes[i]);
            }
            sb.append("]");
            sb.append(", \"ownedWeapons\": [");
            boolean[] weapons = progress.getOwnedWeapons();
            for (int i = 0; i < weapons.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(weapons[i]);
            }
            sb.append("]");
            sb.append(", \"hitShieldRemaining\": ").append(progress.getHitShieldRemaining());
            sb.append(", \"timedShieldRemaining\": ").append(progress.getTimedShieldRemaining());
            sb.append(", \"shieldType\": \"").append(progress.getShieldType()).append("\"");
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

            // Support both old "lastLevelLives" and new "lastLevel" field names
            lastLevel = parseIntField(json, "lastLevel", 0);
            if (lastLevel == 0) {
                lastLevel = parseIntField(json, "lastLevelLives", 1);
            }

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
            int lives = parseIntField(obj, "lives", 3);
            int exp = parseIntField(obj, "exp", 0);
            if (exp == 0) {
                exp = parseIntField(obj, "score", 0);
            }
            int shipType = parseIntField(obj, "shipType", 1);
            boolean[] ownedShips = parseBoolArray(obj, "ownedShips", 8, new boolean[]{true, false, false, false, false, false, false, false});
            // Support both old and new field names
            int weaponMode = parseIntField(obj, "weaponMode", 0);
            if (weaponMode == 0) weaponMode = parseIntField(obj, "activeWeaponMode", 0);
            int weapon = parseIntField(obj, "weapon", 0);
            if (weapon == 0) weapon = parseIntField(obj, "activeWeapon", 0);
            boolean[] ownedWeaponModes = parseBoolArray(obj, "ownedWeaponModes", 4, new boolean[]{true, false, false, false});
            boolean[] ownedWeapons = parseBoolArray(obj, "ownedWeapons", 2, new boolean[]{true, false});
            int hitShieldRemaining = parseIntField(obj, "hitShieldRemaining", 0);
            double timedShieldRemaining = parseDoubleField(obj, "timedShieldRemaining", 0.0);
            String shieldType = parseStringField(obj, "shieldType", "none");
            if (shieldType.equals("none")) {
                shieldType = parseStringField(obj, "activeShieldType", "none");
            }

            LevelProgress progress = new LevelProgress(level, lives, exp, shipType, ownedShips,
                    weaponMode, weapon, ownedWeaponModes, ownedWeapons,
                    hitShieldRemaining, timedShieldRemaining, shieldType);
            progressMap.put(progress.getKey(), progress);
        } catch (Exception e) {
            LOGGER.warning("Error parsing level entry: " + e.getMessage());
        }
    }

    private boolean[] parseBoolArray(String obj, String field, int size, boolean[] defaultValue) {
        boolean[] result = defaultValue.clone();
        int idx = obj.indexOf("\"" + field + "\"");
        if (idx == -1) return result;
        int arrStart = obj.indexOf('[', idx);
        int arrEnd = obj.indexOf(']', arrStart);
        if (arrStart == -1 || arrEnd == -1) return result;
        String arr = obj.substring(arrStart + 1, arrEnd);
        String[] parts = arr.split(",");
        for (int i = 0; i < Math.min(parts.length, size); i++) {
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
                lastLevel = Integer.parseInt(props.getProperty("LAST_LEVEL_LIVES", "1"));
            } catch (NumberFormatException e) {
                lastLevel = 1;
            }

            for (String key : props.stringPropertyNames()) {
                if (key.endsWith("_LIVES") && !key.startsWith("LAST_LEVEL")) {
                    String prefix = key.substring(0, key.length() - "_LIVES".length());
                    try {
                        int lives = Integer.parseInt(props.getProperty(prefix + "_LIVES", "3"));
                        int exp = Integer.parseInt(props.getProperty(prefix + "_SCORE", "0"));

                        int level = LevelProgress.getLevelFromKey(prefix);

                        LevelProgress progress = new LevelProgress(level, lives, exp, 1, null,
                                0, 0, null, null, 0, 0.0, "none");
                        progressMap.put(progress.getKey(), progress);
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
        lastLevel = 1;
        save();
    }
}
