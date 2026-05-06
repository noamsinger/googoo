package com.game.states;

import com.game.core.Game;
import com.game.ui.MenuItem;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MenuState extends GameState {
    private static final Logger LOGGER = Logger.getLogger(MenuState.class.getName());

    private List<MenuItem> menuItems;
    private int currentSelection;
    private Font titleFont;
    private Font menuFont;

    public MenuState(GameStateManager gsm) {
        super(gsm);
        init();
    }

    @Override
    public void init() {
        LOGGER.fine("MenuState init - GAME_WIDTH=" + Game.WINDOW_WIDTH + ", GAME_HEIGHT=" + Game.WINDOW_HEIGHT);

        titleFont = Font.font("Arial", FontWeight.BOLD, 48);
        menuFont = Font.font("Arial", FontWeight.NORMAL, 32);

        menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("Start", Game.WINDOW_WIDTH / 2.0, Game.WINDOW_HEIGHT / 2.0 - 50));
        menuItems.add(new MenuItem("Config", Game.WINDOW_WIDTH / 2.0, Game.WINDOW_HEIGHT / 2.0 + 50));
        menuItems.add(new MenuItem("Exit", Game.WINDOW_WIDTH / 2.0, Game.WINDOW_HEIGHT / 2.0 + 150));

        currentSelection = 0;
    }

    @Override
    public void update(double deltaTime) {
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.rgb(20, 20, 30));
        gc.fillRect(0, 0, Game.WINDOW_WIDTH, Game.WINDOW_HEIGHT);

        gc.setFont(titleFont);
        gc.setFill(Color.rgb(100, 200, 255));
        String title = Game.TITLE;
        Text text = new Text(title);
        text.setFont(titleFont);
        double titleWidth = text.getLayoutBounds().getWidth();
        double titleX = (Game.WINDOW_WIDTH - titleWidth) / 2;
        gc.fillText(title, titleX, Game.WINDOW_HEIGHT / 3.0);

        gc.setFont(menuFont);
        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem item = menuItems.get(i);

            text = new Text(item.getText());
            text.setFont(menuFont);
            double textWidth = text.getLayoutBounds().getWidth();
            double textHeight = text.getLayoutBounds().getHeight();
            double textX = item.getX() - textWidth / 2;

            if (i == currentSelection) {
                gc.setFill(Color.rgb(255, 215, 0, 0.3));
                gc.fillRoundRect(textX - 20, item.getY() - textHeight + 5, textWidth + 40, textHeight + 10, 10, 10);

                gc.setFill(Color.YELLOW);
                double arrowX = textX - 50;
                gc.fillText(">", arrowX, item.getY());
            } else {
                gc.setFill(Color.WHITE);
            }

            gc.fillText(item.getText(), textX, item.getY());
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
        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem item = menuItems.get(i);

            Text text = new Text(item.getText());
            text.setFont(menuFont);
            double textWidth = text.getLayoutBounds().getWidth();
            double textHeight = text.getLayoutBounds().getHeight();

            double left = item.getX() - textWidth / 2 - 20;
            double right = left + textWidth + 40;
            double top = item.getY() - textHeight + 5;
            double bottom = top + textHeight + 10;

            if (x >= left && x <= right && y >= top && y <= bottom) {
                currentSelection = i;
                break;
            }
        }
    }

    @Override
    public void mouseClicked(double x, double y) {
        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem item = menuItems.get(i);

            Text text = new Text(item.getText());
            text.setFont(menuFont);
            double textWidth = text.getLayoutBounds().getWidth();
            double textHeight = text.getLayoutBounds().getHeight();

            double left = item.getX() - textWidth / 2 - 20;
            double right = left + textWidth + 40;
            double top = item.getY() - textHeight + 5;
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
            case "Config":
                LOGGER.fine("Opening config");
                gsm.setState(new ConfigState(gsm));
                break;
            case "Exit":
                LOGGER.fine("Exiting game");
                Game.setFullscreen(false);
                System.exit(0);
                break;
        }
    }
}
