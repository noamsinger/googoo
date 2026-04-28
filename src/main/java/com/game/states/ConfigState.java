package com.game.states;

import com.game.core.Game;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class ConfigState extends GameState {

    public ConfigState(GameStateManager gsm) {
        super(gsm);
        init();
    }

    @Override
    public void init() {
    }

    @Override
    public void update(double deltaTime) {
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.rgb(30, 30, 40));
        gc.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        String title = "Configuration";
        Text text = new Text(title);
        text.setFont(gc.getFont());
        double textWidth = text.getLayoutBounds().getWidth();
        double x = (Game.WIDTH - textWidth) / 2;
        gc.fillText(title, x, 150);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        String msg = "Configuration options will go here";
        text = new Text(msg);
        text.setFont(gc.getFont());
        textWidth = text.getLayoutBounds().getWidth();
        x = (Game.WIDTH - textWidth) / 2;
        gc.fillText(msg, x, 300);

        String back = "Press ESC to return to menu";
        text = new Text(back);
        text.setFont(gc.getFont());
        textWidth = text.getLayoutBounds().getWidth();
        x = (Game.WIDTH - textWidth) / 2;
        gc.fillText(back, x, 400);
    }

    @Override
    public void keyPressed(KeyCode key) {
        if (key == KeyCode.ESCAPE) {
            gsm.setState(new MenuState(gsm));
        }
    }

    @Override
    public void keyReleased(KeyCode key) {
    }
}
