package com.game.states;

import com.game.core.Game;
import com.game.ui.MenuItem;
import com.game.util.TextUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MenuState extends GameState {
    private static final Logger LOGGER = Logger.getLogger(MenuState.class.getName());

    private List<MenuItem> menuItems;
    private int currentSelection;
    private Font titleFont;
    private Font menuFont;
    private Image backgroundImage;

    public MenuState(GameStateManager gsm) {
        super(gsm);
        init();
    }

    @Override
    public void init() {
        LOGGER.fine("MenuState init - GAME_WIDTH=" + Game.WINDOW_WIDTH + ", GAME_HEIGHT=" + Game.WINDOW_HEIGHT);

        titleFont = Font.font("Arial", FontWeight.BOLD, 48);
        menuFont = Font.font("Arial", FontWeight.NORMAL, 32);

        // Load background image
        try {
            backgroundImage = new Image(getClass().getResourceAsStream("/images/background.png"));
        } catch (Exception e) {
            LOGGER.warning("Failed to load background image: " + e.getMessage());
            backgroundImage = null;
        }

        // Store menu items with text only - positions will be calculated dynamically during render
        menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("Start", 0, 0));
        menuItems.add(new MenuItem("Instructions", 0, 0));
        menuItems.add(new MenuItem("Config", 0, 0));
        menuItems.add(new MenuItem("About", 0, 0));
        menuItems.add(new MenuItem("Exit", 0, 0));

        currentSelection = 0;
    }

    @Override
    public void update(double deltaTime) {
    }

    @Override
    public void render(GraphicsContext gc) {
        // Use actual game dimensions for rendering
        double canvasWidth = Game.gameWidth;
        double canvasHeight = Game.gameHeight;

        // Draw background image or fallback to solid color
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, canvasWidth, canvasHeight);
        } else {
            gc.setFill(Color.rgb(20, 20, 30));
            gc.fillRect(0, 0, canvasWidth, canvasHeight);
        }

        gc.setFont(titleFont);
        gc.setFill(Color.rgb(100, 200, 255));
        String title = Game.TITLE;
        double titleX = TextUtils.centerTextX(title, titleFont, canvasWidth);
        gc.fillText(title, titleX, canvasHeight / 5.0);

        gc.setFont(menuFont);

        // Calculate menu item positions dynamically based on current canvas size
        double centerX = canvasWidth / 2.0;
        double centerY = canvasHeight / 2.0;
        double[] yPositions = {centerY - 120, centerY - 40, centerY + 40, centerY + 120, centerY + 200};

        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem item = menuItems.get(i);

            // Use dynamically calculated position
            double itemX = centerX;
            double itemY = yPositions[i];

            double textWidth = TextUtils.measureTextWidth(item.getText(), menuFont);
            double textHeight = TextUtils.measureTextHeight(item.getText(), menuFont);
            double textX = itemX - textWidth / 2;

            if (i == currentSelection) {
                gc.setFill(Color.rgb(255, 215, 0, 0.3));
                gc.fillRoundRect(textX - 20, itemY - textHeight + 5, textWidth + 40, textHeight + 10, 10, 10);

                gc.setFill(Color.YELLOW);
                double arrowX = textX - 50;
                gc.fillText(">", arrowX, itemY);
            } else {
                gc.setFill(Color.WHITE);
            }

            gc.fillText(item.getText(), textX, itemY);
        }
    }

    @Override
    public void keyPressed(KeyCode key) {
        if (key == KeyCode.UP) {
            currentSelection--;
            if (currentSelection < 0) {
                currentSelection = 0;
            }
        } else if (key == KeyCode.DOWN) {
            currentSelection++;
            if (currentSelection >= menuItems.size()) {
                currentSelection = menuItems.size() - 1;
            }
        } else if (key == KeyCode.ENTER) {
            selectMenuItem();
        }
    }

    @Override
    public void keyReleased(KeyCode key) {
    }

    @Override
    public void mouseMoved(double x, double y) {
        // Calculate menu item positions dynamically
        double centerX = Game.gameWidth / 2.0;
        double centerY = Game.gameHeight / 2.0;
        double[] yPositions = {centerY - 120, centerY - 40, centerY + 40, centerY + 120, centerY + 200};

        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem item = menuItems.get(i);

            double textWidth = TextUtils.measureTextWidth(item.getText(), menuFont);
            double textHeight = TextUtils.measureTextHeight(item.getText(), menuFont);

            double left = centerX - textWidth / 2 - 20;
            double right = left + textWidth + 40;
            double top = yPositions[i] - textHeight + 5;
            double bottom = top + textHeight + 10;

            if (x >= left && x <= right && y >= top && y <= bottom) {
                currentSelection = i;
                break;
            }
        }
    }

    @Override
    public void mouseClicked(double x, double y) {
        // Calculate menu item positions dynamically
        double centerX = Game.gameWidth / 2.0;
        double centerY = Game.gameHeight / 2.0;
        double[] yPositions = {centerY - 120, centerY - 40, centerY + 40, centerY + 120, centerY + 200};

        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem item = menuItems.get(i);

            double textWidth = TextUtils.measureTextWidth(item.getText(), menuFont);
            double textHeight = TextUtils.measureTextHeight(item.getText(), menuFont);

            double left = centerX - textWidth / 2 - 20;
            double right = left + textWidth + 40;
            double top = yPositions[i] - textHeight + 5;
            double bottom = top + textHeight + 10;

            if (x >= left && x <= right && y >= top && y <= bottom) {
                currentSelection = i;
                selectMenuItem();
                break;
            }
        }
    }

    private void selectMenuItem() {
        String selected = menuItems.get(currentSelection).getText();
        LOGGER.fine("Menu item selected: " + selected);

        switch (selected) {
            case "Start":
                LOGGER.fine("Starting game - creating PlayState");
                gsm.setState(new PlayState(gsm));
                break;
            case "Instructions":
                LOGGER.fine("Opening instructions");
                gsm.setState(new InstructionsState(gsm));
                break;
            case "Config":
                LOGGER.fine("Opening config");
                gsm.setState(new ConfigState(gsm));
                break;
            case "About":
                LOGGER.fine("Opening about");
                gsm.setState(new AboutState(gsm));
                break;
            case "Exit":
                LOGGER.fine("Exiting game");
                Game.setFullscreen(false);
                System.exit(0);
                break;
        }
    }
}
