package com.game.states;

import com.game.core.Game;
import com.game.ui.MenuItem;

import java.awt.*;
import java.awt.event.KeyEvent;
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
        titleFont = new Font("Arial", Font.BOLD, 48);
        menuFont = new Font("Arial", Font.PLAIN, 32);

        menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("Start", Game.WIDTH / 2, 300));
        menuItems.add(new MenuItem("Config", Game.WIDTH / 2, 370));
        menuItems.add(new MenuItem("Exit", Game.WIDTH / 2, 440));

        currentSelection = 0;
    }

    @Override
    public void update() {
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(new Color(20, 20, 30));
        g.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);

        g.setFont(titleFont);
        g.setColor(new Color(100, 200, 255));
        String title = Game.TITLE;
        FontMetrics fm = g.getFontMetrics();
        int titleX = (Game.WIDTH - fm.stringWidth(title)) / 2;
        g.drawString(title, titleX, 150);

        g.setFont(menuFont);
        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem item = menuItems.get(i);
            if (i == currentSelection) {
                g.setColor(Color.YELLOW);
                int arrowX = item.getX() - fm.stringWidth(item.getText()) / 2 - 40;
                g.drawString(">", arrowX, item.getY());
            } else {
                g.setColor(Color.WHITE);
            }

            fm = g.getFontMetrics();
            int textX = item.getX() - fm.stringWidth(item.getText()) / 2;
            g.drawString(item.getText(), textX, item.getY());
        }
    }

    @Override
    public void keyPressed(int key) {
        if (key == KeyEvent.VK_UP) {
            currentSelection--;
            if (currentSelection < 0) {
                currentSelection = menuItems.size() - 1;
            }
        } else if (key == KeyEvent.VK_DOWN) {
            currentSelection++;
            if (currentSelection >= menuItems.size()) {
                currentSelection = 0;
            }
        } else if (key == KeyEvent.VK_ENTER) {
            selectMenuItem();
        }
    }

    @Override
    public void keyReleased(int key) {
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
