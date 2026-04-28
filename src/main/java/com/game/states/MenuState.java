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

public class MenuState extends GameState {
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
        titleFont = Font.font("Arial", FontWeight.BOLD, 48);
        menuFont = Font.font("Arial", FontWeight.NORMAL, 32);

        menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("Start", Game.WIDTH / 2.0, 300));
        menuItems.add(new MenuItem("Config", Game.WIDTH / 2.0, 370));
        menuItems.add(new MenuItem("Exit", Game.WIDTH / 2.0, 440));

        currentSelection = 0;
    }

    @Override
    public void update(double deltaTime) {
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.rgb(20, 20, 30));
        gc.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);

        gc.setFont(titleFont);
        gc.setFill(Color.rgb(100, 200, 255));
        String title = Game.TITLE;
        Text text = new Text(title);
        text.setFont(titleFont);
        double titleWidth = text.getLayoutBounds().getWidth();
        double titleX = (Game.WIDTH - titleWidth) / 2;
        gc.fillText(title, titleX, 150);

        gc.setFont(menuFont);
        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem item = menuItems.get(i);

            text = new Text(item.getText());
            text.setFont(menuFont);
            double textWidth = text.getLayoutBounds().getWidth();
            double textX = item.getX() - textWidth / 2;

            if (i == currentSelection) {
                gc.setFill(Color.YELLOW);
                double arrowX = textX - 40;
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

    private void selectMenuItem() {
        String selected = menuItems.get(currentSelection).getText();

        switch (selected) {
            case "Start":
                gsm.setState(new PlayState(gsm));
                break;
            case "Config":
                gsm.setState(new ConfigState(gsm));
                break;
            case "Exit":
                System.exit(0);
                break;
        }
    }
}
