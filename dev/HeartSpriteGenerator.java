package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HeartSpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid

    public static void main(String[] args) {
        try {
            generateHeartSpriteSheet();
            System.out.println("Successfully generated beating heart sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateHeartSpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateHeartFrame(frame);

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

        File outputFile = new File(outputDir, "heart_sheet.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Beating heart sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateHeartFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for heart
        Color heartColor = new Color(220, 20, 60); // Crimson red
        Color darkHeartColor = new Color(180, 0, 40); // Dark red
        Color highlightColor = new Color(255, 100, 130); // Light pink highlight
        Color shadowColor = new Color(120, 10, 30); // Deep shadow

        // Beating animation - double beat pattern (lub-dub)
        double beat1 = Math.sin(progress * Math.PI * 4);
        double beat2 = Math.sin(progress * Math.PI * 4 + Math.PI / 2);
        double beat = Math.max(0, beat1) * 0.15 + Math.max(0, beat2) * 0.1;
        double scale = 1.0 + beat;

        // Heart shape using parametric equations
        Path2D heart = new Path2D.Double();

        int numPoints = 100;
        boolean first = true;

        for (int i = 0; i <= numPoints; i++) {
            double t = (i / (double) numPoints) * Math.PI * 2;

            // Parametric heart curve
            double x = 16 * Math.pow(Math.sin(t), 3);
            double y = -(13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t));

            // Scale and position
            double scaledX = centerX + x * scale * 1.2;
            double scaledY = centerY + y * scale * 1.2 - 2;

            if (first) {
                heart.moveTo(scaledX, scaledY);
                first = false;
            } else {
                heart.lineTo(scaledX, scaledY);
            }
        }

        heart.closePath();

        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.translate(2, 3);
        g2d.fill(heart);
        g2d.translate(-2, -3);

        // Draw gradient fill
        GradientPaint heartGradient = new GradientPaint(
            (float)centerX - 15, (float)centerY - 15, highlightColor,
            (float)centerX + 15, (float)centerY + 15, darkHeartColor
        );
        g2d.setPaint(heartGradient);
        g2d.fill(heart);

        // Add darker bottom shadow
        Path2D bottomShadow = new Path2D.Double();
        first = true;
        for (int i = 50; i <= numPoints; i++) {
            double t = (i / (double) numPoints) * Math.PI * 2;
            double x = 16 * Math.pow(Math.sin(t), 3);
            double y = -(13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t));
            double scaledX = centerX + x * scale * 1.2;
            double scaledY = centerY + y * scale * 1.2 - 2;

            if (first) {
                bottomShadow.moveTo(scaledX, scaledY);
                first = false;
            } else {
                bottomShadow.lineTo(scaledX, scaledY);
            }
        }
        bottomShadow.lineTo(centerX, centerY + 20 * scale);
        bottomShadow.closePath();

        g2d.setColor(new Color(shadowColor.getRed(), shadowColor.getGreen(), shadowColor.getBlue(), 100));
        g2d.fill(bottomShadow);

        // Add highlight on top left
        Path2D highlight = new Path2D.Double();
        first = true;
        for (int i = 0; i <= 25; i++) {
            double t = (i / (double) numPoints) * Math.PI * 2;
            double x = 16 * Math.pow(Math.sin(t), 3);
            double y = -(13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t));
            double scaledX = centerX + x * scale * 1.2;
            double scaledY = centerY + y * scale * 1.2 - 2;

            if (first) {
                highlight.moveTo(scaledX, scaledY);
                first = false;
            } else {
                highlight.lineTo(scaledX, scaledY);
            }
        }
        highlight.lineTo(centerX - 8 * scale, centerY - 8 * scale);
        highlight.closePath();

        g2d.setColor(new Color(255, 150, 170, 120));
        g2d.fill(highlight);

        // Draw outline
        g2d.setColor(darkHeartColor.darker());
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(heart);

        // Add shine spot
        g2d.setColor(new Color(255, 255, 255, 180));
        double shineX = centerX - 8 * scale;
        double shineY = centerY - 10 * scale;
        g2d.fillOval((int)(shineX - 3), (int)(shineY - 2), 6, 4);

        // Pulse glow effect during beat
        if (beat > 0.05) {
            g2d.setColor(new Color(255, 100, 130, (int)(beat * 300)));
            g2d.setStroke(new BasicStroke(4.0f));
            g2d.draw(heart);
        }

        g2d.dispose();
        return image;
    }
}
