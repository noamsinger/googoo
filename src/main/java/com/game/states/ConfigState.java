package com.game.states;

import com.game.core.Game;
import com.game.core.GameSettings;
import com.game.core.ProgressManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ConfigState extends GameState {
    private enum ConfigOption {
        STARTING_LEVEL,
        RESOLUTION,
        GAME_TYPE,
        BACK
    }

    private ConfigOption currentSelection = ConfigOption.STARTING_LEVEL;
    private Font titleFont;
    private Font optionFont;
    private Font valueFont;

    // For level selection
    private List<String> availableLevelOptions;
    private int currentLevelOptionIndex = 0;

    public ConfigState(GameStateManager gsm) {
        super(gsm);
        init();
    }

    @Override
    public void init() {
        titleFont = Font.font("Arial", FontWeight.BOLD, 48);
        optionFont = Font.font("Arial", FontWeight.NORMAL, 28);
        valueFont = Font.font("Arial", FontWeight.BOLD, 28);
        updateAvailableLevels();
    }

    private void updateAvailableLevels() {
        availableLevelOptions = new ArrayList<>();
        GameSettings settings = GameSettings.getInstance();
        ProgressManager progressManager = ProgressManager.getInstance();

        // Always add level 1
        availableLevelOptions.add("1");

        // Add levels with progress for current game type
        List<Integer> levels = progressManager.getAvailableLevels(settings.getGameType());
        for (Integer level : levels) {
            if (level > 1) {
                availableLevelOptions.add(String.valueOf(level));
            }
        }

        // Add "Last" option (represents "auto")
        availableLevelOptions.add("Last");

        // Find current level in the list
        String currentLevel = settings.getStartingLevel();
        currentLevelOptionIndex = 0;

        if (currentLevel.equals("auto")) {
            // "auto" maps to "Last" option
            currentLevelOptionIndex = availableLevelOptions.size() - 1;
        } else {
            for (int i = 0; i < availableLevelOptions.size() - 1; i++) { // -1 to exclude "Last"
                if (availableLevelOptions.get(i).equals(currentLevel)) {
                    currentLevelOptionIndex = i;
                    break;
                }
            }
        }
    }

    @Override
    public void update(double deltaTime) {
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.rgb(20, 20, 30));
        gc.fillRect(0, 0, Game.WINDOW_WIDTH, Game.WINDOW_HEIGHT);

        // Title
        gc.setFont(titleFont);
        gc.setFill(Color.rgb(100, 200, 255));
        String title = "Settings";
        Text text = new Text(title);
        text.setFont(titleFont);
        double titleWidth = text.getLayoutBounds().getWidth();
        double titleX = (Game.WINDOW_WIDTH - titleWidth) / 2;
        gc.fillText(title, titleX, 100);

        GameSettings settings = GameSettings.getInstance();
        ProgressManager progressManager = ProgressManager.getInstance();
        double startY = 200;
        double lineHeight = 80;
        int optionIndex = 0;

        // Starting Level
        String levelDisplay = availableLevelOptions.get(currentLevelOptionIndex);
        if (levelDisplay.equals("Last")) {
            int lastLevel = progressManager.getLastLevel(settings.getGameType());
            levelDisplay = "Last (" + lastLevel + ")";
        }
        renderOption(gc, "Starting Level:", levelDisplay,
                     startY + optionIndex * lineHeight, currentSelection == ConfigOption.STARTING_LEVEL);
        optionIndex++;

        // Resolution
        renderOption(gc, "Resolution:", settings.getResolutionMode().getDisplayName(),
                     startY + optionIndex * lineHeight, currentSelection == ConfigOption.RESOLUTION);
        optionIndex++;

        // Game Type
        renderOption(gc, "Game Type:", settings.getGameType().getDisplayName(),
                     startY + optionIndex * lineHeight, currentSelection == ConfigOption.GAME_TYPE);
        optionIndex++;

        // Back button
        renderOption(gc, "Back to Menu", "",
                     startY + optionIndex * lineHeight + 40, currentSelection == ConfigOption.BACK);

        // Instructions
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        gc.setFill(Color.LIGHTGRAY);
        String instructions = "Use UP/DOWN to navigate, LEFT/RIGHT to change values, ENTER to select";
        text = new Text(instructions);
        text.setFont(gc.getFont());
        double instrWidth = text.getLayoutBounds().getWidth();
        gc.fillText(instructions, (Game.WINDOW_WIDTH - instrWidth) / 2, Game.WINDOW_HEIGHT - 50);
    }

    private void renderOption(GraphicsContext gc, String label, String value, double y, boolean selected) {
        double centerX = Game.WINDOW_WIDTH / 2.0;
        double labelX = centerX - 200;
        double valueX = centerX + 50;

        // Highlight if selected
        if (selected) {
            gc.setFill(Color.rgb(255, 215, 0, 0.2));
            gc.fillRoundRect(labelX - 30, y - 30, 500, 50, 10, 10);

            gc.setFill(Color.YELLOW);
            gc.setFont(optionFont);
            gc.fillText(">", labelX - 50, y);
        } else {
            gc.setFill(Color.WHITE);
        }

        // Render label
        gc.setFont(optionFont);
        gc.fillText(label, labelX, y);

        // Render value
        if (!value.isEmpty()) {
            gc.setFont(valueFont);
            gc.setFill(selected ? Color.rgb(255, 255, 100) : Color.rgb(150, 200, 255));
            gc.fillText(value, valueX, y);

            // Show arrows if selected
            if (selected) {
                gc.setFill(Color.LIGHTGRAY);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
                gc.fillText("<", valueX - 30, y);
                gc.fillText(">", valueX + 150, y);
            }
        }
    }

    @Override
    public void keyPressed(KeyCode key) {
        GameSettings settings = GameSettings.getInstance();

        switch (key) {
            case UP:
                moveSelection(-1);
                break;
            case DOWN:
                moveSelection(1);
                break;
            case LEFT:
                adjustValue(-1);
                break;
            case RIGHT:
                adjustValue(1);
                break;
            case ENTER:
                if (currentSelection == ConfigOption.BACK) {
                    gsm.setState(new MenuState(gsm));
                }
                break;
            case ESCAPE:
                gsm.setState(new MenuState(gsm));
                break;
        }
    }

    private void moveSelection(int direction) {
        ConfigOption[] options = ConfigOption.values();
        int currentIndex = currentSelection.ordinal();
        currentIndex += direction;

        if (currentIndex < 0) {
            currentIndex = 0;
        } else if (currentIndex >= options.length) {
            currentIndex = options.length - 1;
        }

        currentSelection = options[currentIndex];
    }

    private void adjustValue(int direction) {
        GameSettings settings = GameSettings.getInstance();

        switch (currentSelection) {
            case STARTING_LEVEL:
                // Cycle through available level options
                currentLevelOptionIndex += direction;
                if (currentLevelOptionIndex < 0) {
                    currentLevelOptionIndex = availableLevelOptions.size() - 1;
                } else if (currentLevelOptionIndex >= availableLevelOptions.size()) {
                    currentLevelOptionIndex = 0;
                }

                // Set the level in settings
                String levelOption = availableLevelOptions.get(currentLevelOptionIndex);
                if (levelOption.equals("Last")) {
                    settings.setStartingLevel("auto");
                } else {
                    settings.setStartingLevel(levelOption);
                }
                break;

            case RESOLUTION:
                GameSettings.ResolutionMode[] resModes = GameSettings.ResolutionMode.values();
                int resIndex = settings.getResolutionMode().ordinal() + direction;
                if (resIndex < 0) {
                    resIndex = resModes.length - 1;
                } else if (resIndex >= resModes.length) {
                    resIndex = 0;
                }
                settings.setResolutionMode(resModes[resIndex]);
                break;

            case GAME_TYPE:
                GameSettings.GameType[] gameTypes = GameSettings.GameType.values();
                int typeIndex = settings.getGameType().ordinal() + direction;
                if (typeIndex < 0) {
                    typeIndex = gameTypes.length - 1;
                } else if (typeIndex >= gameTypes.length) {
                    typeIndex = 0;
                }
                settings.setGameType(gameTypes[typeIndex]);

                // Update available levels for new game type
                updateAvailableLevels();
                break;
        }
    }

    @Override
    public void keyReleased(KeyCode key) {
    }

    @Override
    public void mouseMoved(double x, double y) {
        double startY = 200;
        double lineHeight = 80;

        // Check which option the mouse is over
        for (int i = 0; i < ConfigOption.values().length; i++) {
            double optionY = startY + i * lineHeight;
            if (i == 3) optionY += 40; // Back button offset

            if (y >= optionY - 30 && y <= optionY + 20) {
                currentSelection = ConfigOption.values()[i];
                break;
            }
        }
    }

    @Override
    public void mouseClicked(double x, double y) {
        double centerX = Game.WINDOW_WIDTH / 2.0;
        double valueX = centerX + 50;
        double startY = 200;
        double lineHeight = 80;

        for (int i = 0; i < ConfigOption.values().length; i++) {
            double optionY = startY + i * lineHeight;
            if (i == 3) optionY += 40; // Back button offset

            if (y >= optionY - 30 && y <= optionY + 20) {
                currentSelection = ConfigOption.values()[i];

                // Check if clicked on arrows
                if (currentSelection != ConfigOption.BACK) {
                    if (x >= valueX - 30 && x <= valueX) {
                        adjustValue(-1); // Left arrow
                    } else if (x >= valueX + 150 && x <= valueX + 180) {
                        adjustValue(1); // Right arrow
                    }
                } else {
                    // Clicked on Back button
                    gsm.setState(new MenuState(gsm));
                }
                break;
            }
        }
    }
}
