package com.game.states;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;

public abstract class GameState {
    protected GameStateManager gsm;

    public GameState(GameStateManager gsm) {
        this.gsm = gsm;
    }

    public abstract void init();
    public abstract void update(double deltaTime);
    public abstract void render(GraphicsContext gc);
    public abstract void keyPressed(KeyCode key);
    public abstract void keyReleased(KeyCode key);
}
