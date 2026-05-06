package com.game.util;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public class StarshipSpriteLoader {

    private static final int SPRITE_SIZE = 80;
    private static final int FRAMES = 4;
    private static final int COLS = 4;
    private static Image spriteSheet = null;
    private static Image[] frames = null;

    public static Image createStarshipSprite() {
        // For backward compatibility, return frame 0
        return getStarshipFrame(0);
    }

    public static Image getStarshipFrame(int frameIndex) {
        if (frames == null) {
            loadSpriteSheet();
        }

        if (frames != null && frameIndex >= 0 && frameIndex < FRAMES) {
            return frames[frameIndex];
        }

        return createFallbackSprite();
    }

    public static int getFrameCount() {
        return FRAMES;
    }

    private static void loadSpriteSheet() {
        try {
            String imagePath = "/images/starship_sheet.png";
            spriteSheet = new Image(StarshipSpriteLoader.class.getResourceAsStream(imagePath));

            if (spriteSheet.isError()) {
                System.err.println("Error loading sprite sheet from: " + imagePath);
                frames = null;
                return;
            }

            // Extract individual frames from sprite sheet
            frames = new Image[FRAMES];
            for (int i = 0; i < FRAMES; i++) {
                int col = i % COLS;
                int row = i / COLS;
                int x = col * SPRITE_SIZE;
                int y = row * SPRITE_SIZE;

                // Extract frame
                WritableImage frame = new WritableImage(
                    spriteSheet.getPixelReader(),
                    x, y,
                    SPRITE_SIZE, SPRITE_SIZE
                );
                frames[i] = frame;
            }

            System.out.println("Sprite sheet loaded successfully: " + FRAMES + " frames");

        } catch (Exception e) {
            System.err.println("Exception loading sprite sheet: " + e.getMessage());
            frames = null;
        }
    }

    private static Image createFallbackSprite() {
        System.err.println("Using fallback sprite generation");
        return createProgrammaticSprite();
    }

    private static Image createProgrammaticSprite() {
        javafx.scene.image.WritableImage image = new javafx.scene.image.WritableImage(80, 80);
        javafx.scene.image.PixelWriter pw = image.getPixelWriter();

        int centerX = 40;
        int centerY = 40;

        int[][] leftWing = {{centerX - 15, centerY - 8}, {centerX - 25, centerY - 20}, {centerX - 15, centerY - 15}};
        int[][] rightWing = {{centerX - 15, centerY + 8}, {centerX - 25, centerY + 20}, {centerX - 15, centerY + 15}};
        int[][] body = {{centerX - 15, centerY - 8}, {centerX + 15, centerY - 5}, {centerX + 15, centerY + 5}, {centerX - 15, centerY + 8}};
        int[][] nose = {{centerX + 15, centerY - 5}, {centerX + 22, centerY}, {centerX + 15, centerY + 5}};
        int[][] cockpit = {{centerX + 8, centerY - 4}, {centerX + 15, centerY}, {centerX + 8, centerY + 4}};

        fillPolygon(pw, leftWing, javafx.scene.paint.Color.WHITE);
        fillPolygon(pw, rightWing, javafx.scene.paint.Color.WHITE);
        fillPolygon(pw, body, javafx.scene.paint.Color.rgb(255, 140, 0));
        fillPolygon(pw, nose, javafx.scene.paint.Color.rgb(255, 160, 50));
        fillPolygon(pw, cockpit, javafx.scene.paint.Color.rgb(0, 100, 255));

        return image;
    }

    private static void fillPolygon(javafx.scene.image.PixelWriter pw, int[][] points, javafx.scene.paint.Color color) {
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
        maxX = Math.min(79, maxX);
        minY = Math.max(0, minY);
        maxY = Math.min(79, maxY);

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
}
