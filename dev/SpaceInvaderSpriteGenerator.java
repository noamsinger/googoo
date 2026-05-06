package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SpaceInvaderSpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateSpaceInvaderSpriteSheet();
            System.out.println("Successfully generated space invader sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateSpaceInvaderSpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateSpaceInvaderFrame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_2.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Space invader sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateSpaceInvaderFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for space invader
        Color bodyColor = new Color(150, 100, 255); // Purple body
        Color darkColor = new Color(100, 50, 200); // Darker purple for shading
        Color eyeColor = new Color(255, 50, 50); // Bright red eyes
        Color clawColor = new Color(200, 150, 255); // Light purple for claws

        // Space invader proportions
        int pixelSize = 4; // Each "pixel" in the classic design

        // Animation: alternates between two poses (legs up/down)
        boolean legsDown = (frameIndex % 2) == 0;

        // Body width and height
        int bodyWidth = 8; // 8 pixels wide
        int bodyHeight = 5; // 5 pixels tall

        // Starting position (top-left of the invader body)
        double startX = centerX - (bodyWidth * pixelSize) / 2.0;
        double startY = centerY - (bodyHeight * pixelSize) / 2.0 - pixelSize;

        // Draw the classic space invader pattern
        // Using a bitmap pattern for authenticity

        // Row 0 (top): antennas
        drawPixel(g2d, startX, startY, 1, 0, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 6, 0, pixelSize, bodyColor);

        // Row 1: antenna bases and head top
        drawPixel(g2d, startX, startY, 0, 1, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 1, 1, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 2, 1, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 3, 1, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 4, 1, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 5, 1, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 6, 1, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 7, 1, pixelSize, bodyColor);

        // Row 2: head with eyes
        drawPixel(g2d, startX, startY, 0, 2, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 1, 2, pixelSize, eyeColor); // Left eye
        drawPixel(g2d, startX, startY, 2, 2, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 3, 2, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 4, 2, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 5, 2, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 6, 2, pixelSize, eyeColor); // Right eye
        drawPixel(g2d, startX, startY, 7, 2, pixelSize, bodyColor);

        // Row 3: mouth area with zigzag
        drawPixel(g2d, startX, startY, 0, 3, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 1, 3, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 2, 3, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 3, 3, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 4, 3, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 5, 3, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 6, 3, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 7, 3, pixelSize, bodyColor);

        // Row 4: bottom of body
        drawPixel(g2d, startX, startY, 0, 4, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 1, 4, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 2, 4, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 3, 4, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 4, 4, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 5, 4, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 6, 4, pixelSize, bodyColor);
        drawPixel(g2d, startX, startY, 7, 4, pixelSize, bodyColor);

        // Row 5: arms/claws - alternates based on animation frame
        if (legsDown) {
            // Arms down
            drawPixel(g2d, startX, startY, 0, 5, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 1, 5, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 6, 5, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 7, 5, pixelSize, clawColor);
        } else {
            // Arms up
            drawPixel(g2d, startX, startY, 1, 5, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 6, 5, pixelSize, clawColor);
        }

        // Row 6: legs - alternates based on animation frame
        if (legsDown) {
            // Legs spread out
            drawPixel(g2d, startX, startY, 0, 6, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 7, 6, pixelSize, clawColor);
        } else {
            // Legs pulled in
            drawPixel(g2d, startX, startY, 1, 6, pixelSize, clawColor);
            drawPixel(g2d, startX, startY, 6, 6, pixelSize, clawColor);
        }

        // Add slight bobbing motion
        double bobOffset = Math.sin(progress * Math.PI * 2) * 2;

        // Apply bobbing by translating the entire image slightly
        // (This is already drawn, so we'll apply it in the next version if needed)

        // Add pixel-perfect outline for each body part
        g2d.setColor(darkColor);
        g2d.setStroke(new BasicStroke(0.5f));

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
