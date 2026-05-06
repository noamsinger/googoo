package com.game.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SpriteSaver {
    private static final int SPRITE_SIZE = 80;
    private static final int FRAMES = 4; // 4 frames for light animation
    private static final int COLS = 4;
    private static final int ROWS = 1;

    public static void saveSpriteSheetToPNG() {
        int sheetWidth = SPRITE_SIZE * COLS;
        int sheetHeight = SPRITE_SIZE * ROWS;
        WritableImage spriteSheet = new WritableImage(sheetWidth, sheetHeight);
        PixelWriter pw = spriteSheet.getPixelWriter();

        // Frame 0: All lights off
        // Frame 1: Headlight on
        // Frame 2: Nav lights on
        // Frame 3: All lights on

        for (int i = 0; i < FRAMES; i++) {
            int offsetX = i * SPRITE_SIZE;
            int offsetY = 0;

            boolean headlightOn = (i == 1 || i == 3);
            boolean navLightsOn = (i == 2 || i == 3);

            WritableImage frame = createStarshipFrame(headlightOn, navLightsOn);

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
            File outputFile = new File("src/main/resources/images/starship_sheet.png");
            outputFile.getParentFile().mkdirs();
            ImageIO.write(bufferedImage, "png", outputFile);
            System.out.println("Sprite sheet saved to: " + outputFile.getAbsolutePath());
            System.out.println("Dimensions: " + sheetWidth + "x" + sheetHeight);
            System.out.println("Frames: " + FRAMES + " (light animation frames)");
        } catch (IOException e) {
            System.err.println("Error saving sprite sheet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static WritableImage createStarshipFrame(boolean headlightOn, boolean navLightsOn) {
        WritableImage image = new WritableImage(SPRITE_SIZE, SPRITE_SIZE);
        PixelWriter pw = image.getPixelWriter();

        int centerX = SPRITE_SIZE / 2;
        int centerY = SPRITE_SIZE / 2;

        // Define starship parts (pointing right)
        int[][] leftWing = {{centerX - 15, centerY - 8}, {centerX - 25, centerY - 20}, {centerX - 15, centerY - 15}};
        int[][] rightWing = {{centerX - 15, centerY + 8}, {centerX - 25, centerY + 20}, {centerX - 15, centerY + 15}};
        int[][] body = {{centerX - 15, centerY - 8}, {centerX + 15, centerY - 5}, {centerX + 15, centerY + 5}, {centerX - 15, centerY + 8}};
        int[][] nose = {{centerX + 15, centerY - 5}, {centerX + 22, centerY}, {centerX + 15, centerY + 5}};
        int[][] cockpit = {{centerX + 8, centerY - 4}, {centerX + 15, centerY}, {centerX + 8, centerY + 4}};

        // Draw starship
        fillPolygon(pw, leftWing, Color.WHITE);
        fillPolygon(pw, rightWing, Color.WHITE);
        fillPolygon(pw, body, Color.rgb(255, 140, 0));
        fillPolygon(pw, nose, Color.rgb(255, 160, 50));
        fillPolygon(pw, cockpit, Color.rgb(0, 100, 255));

        // Draw headlight if on
        if (headlightOn) {
            fillCircle(pw, centerX + 20, centerY, 2, Color.rgb(255, 255, 100, 0.8));
        }

        // Draw navigation lights if on
        if (navLightsOn) {
            // Green on right (starboard)
            fillCircle(pw, centerX - 23, centerY + 18, 2, Color.rgb(0, 255, 0, 0.9));
            // Red on left (port)
            fillCircle(pw, centerX - 23, centerY - 18, 2, Color.rgb(255, 0, 0, 0.9));
        }

        return image;
    }

    private static void fillCircle(PixelWriter pw, int cx, int cy, int radius, Color color) {
        for (int y = cy - radius; y <= cy + radius; y++) {
            for (int x = cx - radius; x <= cx + radius; x++) {
                int dx = x - cx;
                int dy = y - cy;
                if (dx * dx + dy * dy <= radius * radius) {
                    if (x >= 0 && x < SPRITE_SIZE && y >= 0 && y < SPRITE_SIZE) {
                        pw.setColor(x, y, color);
                    }
                }
            }
        }
    }

    private static void fillPolygon(PixelWriter pw, int[][] points, Color color) {
        if (points.length < 3) return;

        int minX = points[0][0], maxX = points[0][0];
        int minY = points[0][1], maxY = points[0][1];

        for (int[] point : points) {
            minX = Math.min(minX, point[0]);
            maxX = Math.max(maxX, point[0]);
            minY = Math.min(minY, point[1]);
            maxY = Math.max(maxY, point[1]);
        }

        minX = Math.max(0, minX);
        maxX = Math.min(SPRITE_SIZE - 1, maxX);
        minY = Math.max(0, minY);
        maxY = Math.min(SPRITE_SIZE - 1, maxY);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (isInsidePolygon(x, y, points)) {
                    try {
                        pw.setColor(x, y, color);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    private static boolean isInsidePolygon(int x, int y, int[][] polygon) {
        int intersections = 0;
        int n = polygon.length;

        for (int i = 0; i < n; i++) {
            int x1 = polygon[i][0];
            int y1 = polygon[i][1];
            int x2 = polygon[(i + 1) % n][0];
            int y2 = polygon[(i + 1) % n][1];

            if (y > Math.min(y1, y2) && y <= Math.max(y1, y2)) {
                if (x <= Math.max(x1, x2)) {
                    if (y1 != y2) {
                        double xIntersection = (y - y1) * (x2 - x1) / (double)(y2 - y1) + x1;
                        if (x1 == x2 || x <= xIntersection) {
                            intersections++;
                        }
                    }
                }
            }
        }

        return (intersections % 2) == 1;
    }

    public static void main(String[] args) {
        javafx.application.Platform.startup(() -> {
            saveSpriteSheetToPNG();
            javafx.application.Platform.exit();
        });
    }
}
