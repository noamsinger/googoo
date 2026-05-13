package com.game.util;

import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Utility functions for text measurement and positioning.
 */
public class TextUtils {

    /**
     * Measures the width of a text string with a given font.
     *
     * @param text The text string to measure
     * @param font The font to use for measurement
     * @return The width of the text in pixels
     */
    public static double measureTextWidth(String text, Font font) {
        Text temp = new Text(text);
        temp.setFont(font);
        return temp.getLayoutBounds().getWidth();
    }

    /**
     * Measures the height of a text string with a given font.
     *
     * @param text The text string to measure
     * @param font The font to use for measurement
     * @return The height of the text in pixels
     */
    public static double measureTextHeight(String text, Font font) {
        Text temp = new Text(text);
        temp.setFont(font);
        return temp.getLayoutBounds().getHeight();
    }

    /**
     * Calculates the X coordinate to center text horizontally.
     *
     * @param text The text to center
     * @param font The font of the text
     * @param canvasWidth The width of the canvas/area
     * @return The X coordinate for centered text
     */
    public static double centerTextX(String text, Font font, double canvasWidth) {
        double textWidth = measureTextWidth(text, font);
        return (canvasWidth - textWidth) / 2;
    }

    /**
     * Calculates the Y coordinate to center text vertically.
     *
     * @param text The text to center
     * @param font The font of the text
     * @param canvasHeight The height of the canvas/area
     * @return The Y coordinate for centered text
     */
    public static double centerTextY(String text, Font font, double canvasHeight) {
        double textHeight = measureTextHeight(text, font);
        return (canvasHeight + textHeight) / 2;
    }
}
