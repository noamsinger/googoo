package com.game.states;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import java.util.Stack;

public class GameStateManager {
    private Stack<GameState> states;

    public GameStateManager() {
        states = new Stack<>();
        states.push(new MenuState(this));
    }

    public void setState(GameState state) {
        if (!states.isEmpty()) {
            states.pop();
        }
        states.push(state);
    }

    public void pushState(GameState state) {
        states.push(state);
    }

    public void popState() {
        if (!states.isEmpty()) {
            states.pop();
        }
    }

    public void update(double deltaTime) {
        if (!states.isEmpty()) {
            states.peek().update(deltaTime);
        }
    }

    public void render(GraphicsContext gc) {
        if (!states.isEmpty()) {
            states.peek().render(gc);
        }
    }

    public void keyPressed(KeyCode key) {
        if (!states.isEmpty()) {
            states.peek().keyPressed(key);
        }
    }

    public void keyReleased(KeyCode key) {
        if (!states.isEmpty()) {
            states.peek().keyReleased(key);
        }
    }
}
