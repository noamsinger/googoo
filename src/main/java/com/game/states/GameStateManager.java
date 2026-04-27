package com.game.states;

import java.awt.Graphics2D;
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

    public void update() {
        if (!states.isEmpty()) {
            states.peek().update();
        }
    }

    public void render(Graphics2D g) {
        if (!states.isEmpty()) {
            states.peek().render(g);
        }
    }

    public void keyPressed(int key) {
        if (!states.isEmpty()) {
            states.peek().keyPressed(key);
        }
    }

    public void keyReleased(int key) {
        if (!states.isEmpty()) {
            states.peek().keyReleased(key);
        }
    }
}
