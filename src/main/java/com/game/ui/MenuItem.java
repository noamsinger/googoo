package com.game.ui;

public class MenuItem {
    private String text;
    private int x;
    private int y;

    public MenuItem(String text, int x, int y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    public String getText() {
        return text;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
