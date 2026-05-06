package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SpaceInvader2SpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateSpaceInvader2SpriteSheet();
            System.out.println("Successfully generated space invader 2 sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateSpaceInvader2SpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateSpaceInvader2Frame(frame);

            int col = frame % FRAMES_PER_ROW;
            int row = frame / FRAMES_PER_ROW;
            int x = col * FRAME_SIZE;
            int y = row * FRAME_SIZE;

            sheetG2d.drawImage(frameImage, x, y, null);
        }

        sheetG2d.dispose();

        String outputDir = "src/main/resources/images";
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File outputFile = new File(outputDir, "enemy_sheet_14.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Space invader 2 sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateSpaceInvader2Frame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for space invader variant
        Color bodyColor = new Color(50, 200, 120); // Bright green body
        Color darkColor = new Color(30, 150, 80); // Dark green
        Color eyeColor = new Color(255, 100, 255); // Pink/magenta eyes
        Color clawColor = new Color(100, 255, 150); // Light green for claws

        // Space invader proportions (different design than enemy 2)
        int pixelSize = 4; // Each "pixel" in the classic design

        // Animation: alternates between two poses (tentacles up/down)
        boolean tentaclesDown = (frameIndex % 2) == 0;

        // Body width and height (squid-like invader)
        int bodyWidth = 11; // 11 pixels wide
        int bodyHeight = 6; // 6 pixels tall

        // Starting position (top-left of the invader body)
        double startX = centerX - (bodyWidth * pixelSize) / 2.0;
        double startY = centerY - (bodyHeight * pixelSize) / 2.0;

        // Draw the squid-like space invader pattern

        // Row 0 (top): tentacle tops
        if (!tentaclesDown) {
            drawPixel(g2d, startX, startY, 0, 0, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 2, 0, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 4, 0, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 6, 0, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 8, 0, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 10, 0, pixelSize, clawColor);
        }

        // Row 1: top of head
        for (int i = 1; i < 10; i++) {
            drawPixel(g2d, startX, startY, i, 1, pixelSize, bodyColor);
        }

        // Row 2: eyes and head
        drawPixel(g2d, startX, startY, 0, 2, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 1, 2, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 2, 2, pixelSize, eyeColor); // Left eye
        drawPixel(g2d, startX, startY, 3, 2, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 4, 2, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 5, 2, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 6, 2, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 7, 2, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 8, 2, pixelSize, eyeColor); // Right eye
        drawPixel(g2d, startX, startY, 9, 2, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 10, 2, pixelSize, bodyColor);

        // Row 3: body with pattern
        for (int i = 0; i < 11; i++) {
            if (i == 2 || i == 5 || i == 8) {
                drawPixel(g2d, startX, startY, i, 3, pixelSize, darkColor);
            } else {
                drawPixel(g2d, startX, startY, i, 3, pixelSize, bodyColor);
            }
        }

        // Row 4: lower body
        for (int i = 1; i < 10; i++) {
            drawPixel(g2d, startX, startY, i, 4, pixelSize, bodyColor);
        }

        // Row 5: tentacle bases
        drawPixel(g2d, startX, startY, 0, 5, pixelSize, clawColor);
        drawPixel(g2d, startX, startY, 1, 5, pixelSize, clawColor);
        drawPixel(g2d, startX, startY, 3, 5, pixelSize, clawColor);
        drawPixel(g2d, startX, startY, 4, 5, pixelSize, clawColor);
        drawPixel(g2d, startX, startY, 6, 5, pixelSize, clawColor);
        drawPixel(g2d, startX, startY, 7, 5, pixelSize, clawColor);
        drawPixel(g2d, startX, startY, 9, 5, pixelSize, clawColor);
        drawPixel(g2d, startX, startY, 10, 5, pixelSize, clawColor);

        // Row 6 & 7: tentacle extensions - alternates based on animation frame
        if (tentaclesDown) {
            // Tentacles down
            drawPixel(g2d, startX, startY, 0, 6, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 1, 6, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 3, 6, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 4, 6, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 6, 6, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 7, 6, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 9, 6, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 10, 6, pixelSize, clawColor);

            // Extended tentacles
            drawPixel(g2d, startX, startY, 0, 7, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 3, 7, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 7, 7, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 10, 7, pixelSize, clawColor);
        } else {
            // Tentacles up (shorter)
            drawPixel(g2d, startX, startY, 1, 6, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 4, 6, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 6, 6, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 9, 6, pixelSize, clawColor);
        }

        g2d.dispose();
        return image;
    }

    private static void drawPixel(Graphics2D g2d, double startX, double startY,
                                   int gridX, int gridY, int pixelSize, Color color) {
        double x = startX + gridX * pixelSize;
        double y = startY + gridY * pixelSize;

        g2d.setColor(color);
        g2d.fill(new Rectangle2D.Double(x, y, pixelSize, pixelSize));

        // Add subtle border for depth
        g2d.setColor(color.darker());
        g2d.setStroke(new BasicStroke(0.5f));
        g2d.draw(new Rectangle2D.Double(x, y, pixelSize, pixelSize));
    }
}
