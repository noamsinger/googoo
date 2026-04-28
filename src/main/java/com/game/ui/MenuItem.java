package com.game.ui;

public class MenuItem {
    private String text;
    private double x;
    private double y;

    public MenuItem(String text, double x, double y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    public String getText() {
        return text;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
