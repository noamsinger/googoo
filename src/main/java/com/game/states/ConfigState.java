package com.game.states;

import com.game.core.Game;

import java.awt.*;
import java.awt.event.KeyEvent;

public class ConfigState extends GameState {

    public ConfigState(GameStateManager gsm) {
        super(gsm);
        init();
    }

    @Override
    public void init() {
    }

    @Override
    public void update() {
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(new Color(30, 30, 40));
        g.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        String title = "Configuration";
        FontMetrics fm = g.getFontMetrics();
        int x = (Game.WIDTH - fm.stringWidth(title)) / 2;
        g.drawString(title, x, 150);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
        String msg = "Configuration options will go here";
        fm = g.getFontMetrics();
        x = (Game.WIDTH - fm.stringWidth(msg)) / 2;
        g.drawString(msg, x, 300);

        String back = "Press ESC to return to menu";
        fm = g.getFontMetrics();
        x = (Game.WIDTH - fm.stringWidth(back)) / 2;
        g.drawString(back, x, 400);
    }

    @Override
    public void keyPressed(int key) {
        if (key == KeyEvent.VK_ESCAPE) {
            gsm.setState(new MenuState(gsm));
        }
    }

    @Override
    public void keyReleased(int key) {
    }
}
