package com.game.core;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;

public class GameSettings {
    private static final Logger LOGGER = Logger.getLogger(GameSettings.class.getName());
    private static final String CONFIG_DIR_NAME = ".config/googoo";
    private static final String CONFIG_FILE_NAME = "googoo.ini";
    private static final String DEBUG_CONFIG_FILE_NAME = "googoo-debug.ini";

    // Singleton instance
    private static GameSettings instance = null;

    // Settings
    private String startingLevel = "auto"; // "auto" or "1"-"32"
    private ResolutionMode resolutionMode = ResolutionMode.AUTO;
    private int customWidth = 1920;
    private int customHeight = 1080;
    private boolean debugMode = false; // Default to off, will be loaded from config

    // Private constructor for singleton
    private GameSettings() {
        loadSettings();
    }

    public static GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }
        return instance;
    }

    private Path getConfigFilePath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, CONFIG_DIR_NAME, CONFIG_FILE_NAME);
    }

    private Path getDebugConfigFilePath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, CONFIG_DIR_NAME, DEBUG_CONFIG_FILE_NAME);
    }

    private void ensureConfigDirectoryExists() {
        String userHome = System.getProperty("user.home");
        Path configDir = Paths.get(userHome, CONFIG_DIR_NAME);

        if (!Files.exists(configDir)) {
            try {
                Files.createDirectories(configDir);
                LOGGER.info("Created config directory: " + configDir);
            } catch (IOException e) {
                LOGGER.warning("Failed to create config directory: " + configDir + " - " + e.getMessage());
            }
        }
    }

    private void loadSettings() {
        Path configPath = getConfigFilePath();

        if (!Files.exists(configPath)) {
            LOGGER.fine("Config file not found at: " + configPath + ", using defaults");
            return;
        }

        Properties props = new Properties();
        try (InputStream input = Files.newInputStream(configPath)) {
            props.load(input);

            // Load starting level
            String levelStr = props.getProperty("starting_level");
            if (levelStr != null) {
                startingLevel = levelStr.trim();
                // Validate: must be "auto" or a number between 1-32
                if (!startingLevel.equals("auto")) {
                    try {
                        int level = Integer.parseInt(startingLevel);
                        level = Math.max(1, Math.min(32, level));
                        startingLevel = String.valueOf(level);
                    } catch (NumberFormatException e) {
                        LOGGER.warning("Invalid starting_level in config: " + levelStr + ", using 'auto'");
                        startingLevel = "auto";
                    }
                }
            }

            // Load resolution mode
            String resModeStr = props.getProperty("resolution_mode");
            if (resModeStr != null) {
                try {
                    resolutionMode = ResolutionMode.valueOf(resModeStr);
                } catch (IllegalArgumentException e) {
                    LOGGER.warning("Invalid resolution_mode in config: " + resModeStr);
                }
            }

            // Load custom resolution
            String widthStr = props.getProperty("custom_width");
            if (widthStr != null) {
                try {
                    customWidth = Integer.parseInt(widthStr);
                } catch (NumberFormatException e) {
                    LOGGER.warning("Invalid custom_width in config: " + widthStr);
                }
            }

            String heightStr = props.getProperty("custom_height");
            if (heightStr != null) {
                try {
                    customHeight = Integer.parseInt(heightStr);
                } catch (NumberFormatException e) {
                    LOGGER.warning("Invalid custom_height in config: " + heightStr);
                }
            }

            LOGGER.fine("Settings loaded from: " + configPath);
        } catch (IOException e) {
            LOGGER.warning("Failed to load settings from: " + configPath + " - " + e.getMessage());
        }

        // Load debug mode
        loadDebugMode();
    }

    private void loadDebugMode() {
        Path debugConfigPath = getDebugConfigFilePath();

        if (!Files.exists(debugConfigPath)) {
            LOGGER.fine("Debug config file not found at: " + debugConfigPath + ", using default (disabled)");
            return;
        }

        Properties props = new Properties();
        try (InputStream input = Files.newInputStream(debugConfigPath)) {
            props.load(input);

            String debugModeStr = props.getProperty("debug_mode");
            if (debugModeStr != null) {
                debugMode = Boolean.parseBoolean(debugModeStr);
            }

            LOGGER.fine("Debug mode loaded from: " + debugConfigPath + " - debugMode=" + debugMode);
        } catch (IOException e) {
            LOGGER.warning("Failed to load debug mode from: " + debugConfigPath + " - " + e.getMessage());
        }
    }

    public void saveSettings() {
        ensureConfigDirectoryExists();

        Path configPath = getConfigFilePath();
        Properties props = new Properties();

        props.setProperty("starting_level", startingLevel);
        props.setProperty("resolution_mode", resolutionMode.name());
        props.setProperty("custom_width", String.valueOf(customWidth));
        props.setProperty("custom_height", String.valueOf(customHeight));

        try (OutputStream output = Files.newOutputStream(configPath)) {
            props.store(output, "Googoo Game Settings");
            LOGGER.info("Settings saved to: " + configPath);
        } catch (IOException e) {
            LOGGER.warning("Failed to save settings to: " + configPath + " - " + e.getMessage());
        }
    }

    public void saveDebugMode() {
        ensureConfigDirectoryExists();

        Path debugConfigPath = getDebugConfigFilePath();
        Properties props = new Properties();

        props.setProperty("debug_mode", String.valueOf(debugMode));

        try (OutputStream output = Files.newOutputStream(debugConfigPath)) {
            props.store(output, "Googoo Debug Mode Configuration");
            LOGGER.info("Debug mode saved to: " + debugConfigPath + " - debugMode=" + debugMode);
        } catch (IOException e) {
            LOGGER.warning("Failed to save debug mode to: " + debugConfigPath + " - " + e.getMessage());
        }
    }

    public String getStartingLevel() {
        return startingLevel;
    }

    public void setStartingLevel(String level) {
        if (level.equals("auto")) {
            this.startingLevel = "auto";
        } else {
            try {
                int levelInt = Integer.parseInt(level);
                levelInt = Math.max(1, Math.min(32, levelInt));
                this.startingLevel = String.valueOf(levelInt);
            } catch (NumberFormatException e) {
                this.startingLevel = "auto";
            }
        }
        saveSettings();
    }

    public int getResolvedStartingLevel() {
        if (startingLevel.equals("auto")) {
            ProgressManager progressManager = ProgressManager.getInstance();
            return progressManager.getLastLevel();
        } else {
            try {
                return Integer.parseInt(startingLevel);
            } catch (NumberFormatException e) {
                return 1;
            }
        }
    }

    public ResolutionMode getResolutionMode() {
        return resolutionMode;
    }

    public void setResolutionMode(ResolutionMode mode) {
        this.resolutionMode = mode;
        saveSettings();
    }

    public int getCustomWidth() {
        return customWidth;
    }

    public void setCustomWidth(int width) {
        this.customWidth = width;
        saveSettings();
    }

    public int getCustomHeight() {
        return customHeight;
    }

    public void setCustomHeight(int height) {
        this.customHeight = height;
        saveSettings();
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        saveDebugMode();
    }

    public enum ResolutionMode {
        AUTO("Auto"),
        RES_256x192("256x192"),
        RES_640x480("640x480"),
        RES_1024x768("1024x768"),
        RES_1920x1080("1920x1080");

        private final String displayName;

        ResolutionMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getWidth() {
            switch (this) {
                case RES_256x192: return 256;
                case RES_640x480: return 640;
                case RES_1024x768: return 1024;
                case RES_1920x1080: return 1920;
                default: return -1; // Auto
            }
        }

        public int getHeight() {
            switch (this) {
                case RES_256x192: return 192;
                case RES_640x480: return 480;
                case RES_1024x768: return 768;
                case RES_1920x1080: return 1080;
                default: return -1; // Auto
            }
        }
    }

}
