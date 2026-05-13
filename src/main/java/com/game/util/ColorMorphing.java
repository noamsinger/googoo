package com.game.util;

import javafx.scene.paint.Color;

/**
 * Utility for morphing colors in cyclic patterns.
 */
public class ColorMorphing {

    /**
     * Calculates a cyclically morphing color through the rainbow spectrum.
     * The cycle goes: Red -> Magenta -> Blue -> Cyan -> Green -> Yellow -> Red
     *
     * @param timer Current time in seconds
     * @param cycleDuration Duration of one complete cycle in seconds
     * @return The morphed color at the current time
     */
    public static Color getRainbowColor(double timer, double cycleDuration) {
        double cyclePosition = (timer % cycleDuration) / cycleDuration; // 0 to 1
        int colorPhase = (int) (cyclePosition * 6); // 0 to 5
        double phaseProgress = (cyclePosition * 6) - colorPhase; // 0 to 1 within phase

        // Clamp to valid range
        if (colorPhase >= 6) {
            colorPhase = 5;
            phaseProgress = 1.0;
        }

        switch (colorPhase) {
            case 0: // Red to Magenta (255,0,0) -> (255,0,255)
                return Color.rgb(255, 0, (int)(255 * phaseProgress));
            case 1: // Magenta to Blue (255,0,255) -> (0,0,255)
                return Color.rgb((int)(255 * (1 - phaseProgress)), 0, 255);
            case 2: // Blue to Cyan (0,0,255) -> (0,255,255)
                return Color.rgb(0, (int)(255 * phaseProgress), 255);
            case 3: // Cyan to Green (0,255,255) -> (0,255,0)
                return Color.rgb(0, 255, (int)(255 * (1 - phaseProgress)));
            case 4: // Green to Yellow (0,255,0) -> (255,255,0)
                return Color.rgb((int)(255 * phaseProgress), 255, 0);
            case 5: // Yellow to Red (255,255,0) -> (255,0,0)
                return Color.rgb(255, (int)(255 * (1 - phaseProgress)), 0);
            default:
                return Color.rgb(255, 0, 0); // Red as fallback
        }
    }

    /**
     * Calculates a color gradient from green to red based on a percentage.
     * Green (100%) -> Yellow-Green (75%) -> Yellow (50%) -> Orange (25%) -> Red (0%)
     *
     * @param percentage Value from 0 to 100
     * @return The interpolated color
     */
    public static Color getHealthColor(double percentage) {
        // Clamp percentage to 0-100
        percentage = Math.max(0, Math.min(100, percentage));

        if (percentage >= 75) {
            // 100% to 75%: Green to Yellow-Green
            double t = (100 - percentage) / 25.0; // 0 to 1
            int red = (int) (0 + t * 200);
            int green = 255;
            return Color.rgb(red, green, 0);
        } else if (percentage >= 50) {
            // 75% to 50%: Yellow to Orange
            double t = (75 - percentage) / 25.0; // 0 to 1
            int red = (int) (200 + t * 55);
            int green = (int) (255 - t * 100);
            return Color.rgb(red, green, 0);
        } else if (percentage >= 25) {
            // 50% to 25%: Orange to Red-Orange
            double t = (50 - percentage) / 25.0; // 0 to 1
            int red = 255;
            int green = (int) (155 - t * 105);
            return Color.rgb(red, green, 0);
        } else {
            // 25% to 0%: Red-Orange to Pure Red
            double t = (25 - percentage) / 25.0; // 0 to 1
            int red = 255;
            int green = (int) (50 - t * 50);
            return Color.rgb(red, green, 0);
        }
    }
}
