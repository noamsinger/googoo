package com.game.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class EnemySpriteGenerator {
    private static final int SPRITE_SIZE = 80;
    private static final int FRAMES = 16;
    private static final int COLS = 4;
    private static final int ROWS = 4;

    // Enemy types
    private static final String[] ENEMY_NAMES = {
        "Bug", "Mosquito", "Spider", "Fire", "Germ", "Alien", "Worm", "Medusa",
        "Squid", "Plankton", "Beetle", "Wasp", "Jellyfish", "Virus", "Mantis", "Scorpion"
    };

    public static void main(String[] args) {
        javafx.application.Platform.startup(() -> {});

        if (args.length > 0) {
            int enemyIndex = Integer.parseInt(args[0]);
            generateEnemySpriteSheet(enemyIndex);
        } else {
            for (int i = 0; i < 16; i++) {
                generateEnemySpriteSheet(i);
            }
        }

        javafx.application.Platform.exit();
    }

    public static void generateEnemySpriteSheet(int enemyIndex) {
        int sheetWidth = SPRITE_SIZE * COLS;
        int sheetHeight = SPRITE_SIZE * ROWS;
        WritableImage spriteSheet = new WritableImage(sheetWidth, sheetHeight);
        PixelWriter pw = spriteSheet.getPixelWriter();

        for (int i = 0; i < FRAMES; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int offsetX = col * SPRITE_SIZE;
            int offsetY = row * SPRITE_SIZE;

            double phase = (i / (double) FRAMES) * 2 * Math.PI;
            WritableImage frame = createEnemyFrame(phase, enemyIndex);

            // Copy frame to sprite sheet
            for (int y = 0; y < SPRITE_SIZE; y++) {
                for (int x = 0; x < SPRITE_SIZE; x++) {
                    Color pixel = frame.getPixelReader().getColor(x, y);
                    pw.setColor(offsetX + x, offsetY + y, pixel);
                }
            }
        }

        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(spriteSheet, null);

        try {
            File outputFile = new File("src/main/resources/images/enemy_sheet_" + enemyIndex + ".png");
            outputFile.getParentFile().mkdirs();
            ImageIO.write(bufferedImage, "png", outputFile);
            System.out.println("Enemy sprite sheet " + enemyIndex + " (" + ENEMY_NAMES[enemyIndex] + ") saved to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving sprite sheet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static WritableImage createEnemyFrame(double phase, int enemyIndex) {
        WritableImage image = new WritableImage(SPRITE_SIZE, SPRITE_SIZE);
        PixelWriter pw = image.getPixelWriter();

        int centerX = SPRITE_SIZE / 2;
        int centerY = SPRITE_SIZE / 2;

        // Get colors: 4 primary × 4 secondary = 16 combinations
        int primaryColorIndex = enemyIndex / 4; // 0-3
        int secondaryColorIndex = enemyIndex % 4; // 0-3

        Color primaryColor = getPrimaryColor(primaryColorIndex);
        Color secondaryColor = getSecondaryColor(secondaryColorIndex);

        // Draw background circle with primary color
        fillCircle(pw, centerX, centerY, 35, primaryColor, 0.8);

        // Draw the number
        drawNumber(pw, centerX, centerY, enemyIndex, secondaryColor);

        return image;
    }

    private static Color getPrimaryColor(int index) {
        switch (index) {
            case 0: return Color.CYAN;
            case 1: return Color.MAGENTA;
            case 2: return Color.YELLOW;
            case 3: default: return Color.WHITE;
        }
    }

    private static Color getSecondaryColor(int index) {
        switch (index) {
            case 0: return Color.BLUE;
            case 1: return Color.RED;
            case 2: return Color.GREEN;
            case 3: default: return Color.GRAY;
        }
    }

    // Draw a number using a simple 7-segment style display
    private static void drawNumber(PixelWriter pw, int cx, int cy, int number, Color color) {
        String numStr = String.valueOf(number);
        int digitWidth = 25;
        int startX = cx - (numStr.length() * digitWidth) / 2;

        for (int i = 0; i < numStr.length(); i++) {
            int digit = Character.getNumericValue(numStr.charAt(i));
            int digitX = startX + i * digitWidth;
            drawDigit(pw, digitX, cy, digit, color);
        }
    }

    // Draw a single digit (0-9) using segments
    private static void drawDigit(PixelWriter pw, int cx, int cy, int digit, Color color) {
        // Segment positions (7-segment display style)
        int segmentLength = 12;
        int segmentThickness = 3;

        // Define which segments are on for each digit
        boolean[][] segments = {
            {true, true, true, true, true, true, false},    // 0
            {false, true, true, false, false, false, false}, // 1
            {true, true, false, true, true, false, true},   // 2
            {true, true, true, true, false, false, true},   // 3
            {false, true, true, false, false, true, true},  // 4
            {true, false, true, true, false, true, true},   // 5
            {true, false, true, true, true, true, true},    // 6
            {true, true, true, false, false, false, false}, // 7
            {true, true, true, true, true, true, true},     // 8
            {true, true, true, true, false, true, true}     // 9
        };

        boolean[] seg = segments[digit];

        // Draw segments
        // Top (0)
        if (seg[0]) fillRect(pw, cx - segmentLength / 2, cy - segmentLength - 2, segmentLength, segmentThickness, color, 1.0);
        // Top-right (1)
        if (seg[1]) fillRect(pw, cx + segmentLength / 2 - 2, cy - segmentLength, segmentThickness, segmentLength, color, 1.0);
        // Bottom-right (2)
        if (seg[2]) fillRect(pw, cx + segmentLength / 2 - 2, cy, segmentThickness, segmentLength, color, 1.0);
        // Bottom (3)
        if (seg[3]) fillRect(pw, cx - segmentLength / 2, cy + segmentLength - 2, segmentLength, segmentThickness, color, 1.0);
        // Bottom-left (4)
        if (seg[4]) fillRect(pw, cx - segmentLength / 2, cy, segmentThickness, segmentLength, color, 1.0);
        // Top-left (5)
        if (seg[5]) fillRect(pw, cx - segmentLength / 2, cy - segmentLength, segmentThickness, segmentLength, color, 1.0);
        // Middle (6)
        if (seg[6]) fillRect(pw, cx - segmentLength / 2, cy - 2, segmentLength, segmentThickness, color, 1.0);
    }

    // Helper methods
    private static void fillRect(PixelWriter pw, int x, int y, int width, int height, Color color, double opacity) {
        for (int dy = 0; dy < height; dy++) {
            for (int dx = 0; dx < width; dx++) {
                setPixel(pw, x + dx, y + dy, color, opacity);
            }
        }
    }

    private static void fillCircle(PixelWriter pw, int cx, int cy, int radius, Color color, double opacity) {
        for (int y = cy - radius; y <= cy + radius; y++) {
            for (int x = cx - radius; x <= cx + radius; x++) {
                int dx = x - cx;
                int dy = y - cy;
                if (dx * dx + dy * dy <= radius * radius) {
                    setPixel(pw, x, y, color, opacity);
                }
            }
        }
    }

    private static void setPixel(PixelWriter pw, int x, int y, Color color, double opacity) {
        if (x >= 0 && x < SPRITE_SIZE && y >= 0 && y < SPRITE_SIZE) {
            try {
                pw.setColor(x, y, new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity));
            } catch (Exception e) {
                // Ignore out of bounds
            }
        }
    }
}
