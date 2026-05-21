package com.game.states;

import com.game.core.Game;
import com.game.util.TextUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class PauseState extends GameState {

    private Font titleFont;
    private Font menuFont;
    private int currentSelection = 0;
    private final String[] options = {"Continue", "Exit to Menu"};

    public PauseState(GameStateManager gsm) {
        super(gsm);
        init();
    }

    @Override
    public void init() {
        titleFont = Font.font("Arial", FontWeight.BOLD, 48);
        menuFont = Font.font("Arial", FontWeight.NORMAL, 30);
    }

    @Override
    public void update(double deltaTime) {
    }

    @Override
    public void render(GraphicsContext gc) {
        double w = Game.gameWidth;
        double h = Game.gameHeight;

        // Semi-transparent overlay
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRect(0, 0, w, h);

        // Sub-window
        double boxW = 320;
        double boxH = 220;
        double boxX = (w - boxW) / 2;
        double boxY = (h - boxH) / 2;

        gc.setFill(Color.rgb(10, 10, 30, 0.92));
        gc.fillRoundRect(boxX, boxY, boxW, boxH, 16, 16);
        gc.setStroke(Color.rgb(100, 150, 255, 0.6));
        gc.setLineWidth(2);
        gc.strokeRoundRect(boxX, boxY, boxW, boxH, 16, 16);

        // Title
        gc.setFont(titleFont);
        gc.setFill(Color.rgb(200, 200, 255));
        String title = "Paused";
        gc.fillText(title, TextUtils.centerTextX(title, titleFont, w), boxY + 50);

        // Menu items
        gc.setFont(menuFont);
        double centerX = w / 2.0;
        double startY = boxY + 100;
        double spacing = 55;

        for (int i = 0; i < options.length; i++) {
            double itemY = startY + i * spacing;
            String text = options[i];
            double textWidth = TextUtils.measureTextWidth(text, menuFont);
            double textHeight = TextUtils.measureTextHeight(text, menuFont);
            double textX = centerX - textWidth / 2;

            if (i == currentSelection) {
                gc.setFill(Color.rgb(255, 215, 0, 0.3));
                gc.fillRoundRect(textX - 20, itemY - textHeight + 5, textWidth + 40, textHeight + 10, 10, 10);
                gc.setFill(Color.YELLOW);
                gc.fillText(">", textX - 35, itemY);
            } else {
                gc.setFill(Color.WHITE);
            }

            gc.fillText(text, textX, itemY);
        }
    }

    @Override
    public void keyPressed(KeyCode key) {
        if (key == KeyCode.ESCAPE) {
            gsm.popState();
        } else if (key == KeyCode.UP) {
            currentSelection--;
            if (currentSelection < 0) currentSelection = 0;
        } else if (key == KeyCode.DOWN) {
            currentSelection++;
            if (currentSelection >= options.length) currentSelection = options.length - 1;
        } else if (key == KeyCode.ENTER) {
            selectOption();
        }
    }

    @Override
    public void keyReleased(KeyCode key) {
    }

    @Override
    public void mouseClicked(double x, double y) {
        double w = Game.gameWidth;
        double h = Game.gameHeight;
        double boxH = 220;
        double boxY = (h - boxH) / 2;
        double centerX = w / 2.0;
        double startY = boxY + 100;
        double spacing = 55;

        for (int i = 0; i < options.length; i++) {
            double itemY = startY + i * spacing;
            double textWidth = TextUtils.measureTextWidth(options[i], menuFont);
            double textHeight = TextUtils.measureTextHeight(options[i], menuFont);
            double left = centerX - textWidth / 2 - 20;
            double right = left + textWidth + 40;
            double top = itemY - textHeight + 5;
            double bottom = top + textHeight + 10;

            if (x >= left && x <= right && y >= top && y <= bottom) {
                currentSelection = i;
                selectOption();
                return;
            }
        }
    }

    @Override
    public void mouseMoved(double x, double y) {
        double w = Game.gameWidth;
        double h = Game.gameHeight;
        double boxH = 220;
        double boxY = (h - boxH) / 2;
        double centerX = w / 2.0;
        double startY = boxY + 100;
        double spacing = 55;

        for (int i = 0; i < options.length; i++) {
            double itemY = startY + i * spacing;
            double textWidth = TextUtils.measureTextWidth(options[i], menuFont);
            double textHeight = TextUtils.measureTextHeight(options[i], menuFont);
            double left = centerX - textWidth / 2 - 20;
            double right = left + textWidth + 40;
            double top = itemY - textHeight + 5;
            double bottom = top + textHeight + 10;

            if (x >= left && x <= right && y >= top && y <= bottom) {
                currentSelection = i;
                break;
            }
        }
    }

    private void selectOption() {
        switch (options[currentSelection]) {
            case "Continue":
                gsm.popState();
                break;
            case "Exit to Menu":
                gsm.popState();
                Game.setFullscreen(false);
                gsm.setState(new MenuState(gsm));
                break;
        }
    }
}
