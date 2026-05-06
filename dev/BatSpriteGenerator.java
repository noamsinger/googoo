package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BatSpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateBatSpriteSheet();
            System.out.println("Successfully generated bat sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateBatSpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateBatFrame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_3.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Bat sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateBatFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for bat
        Color bodyColor = new Color(80, 40, 100); // Dark purple body
        Color wingColor = new Color(120, 60, 150); // Medium purple wings
        Color wingMembraneColor = new Color(100, 50, 120); // Darker purple membrane
        Color eyeColor = new Color(255, 100, 100); // Reddish eyes
        Color earColor = new Color(150, 80, 180); // Light purple ears

        // Wing flap animation - wings go up and down
        double wingFlapProgress = Math.sin(progress * Math.PI * 2);
        double wingAngle = Math.PI * 0.25 * wingFlapProgress; // Flap between -45 and +45 degrees

        // Draw bat body (oval)
        double bodyWidth = 16;
        double bodyHeight = 20;

        g2d.setColor(bodyColor);
        g2d.fill(new Ellipse2D.Double(centerX - bodyWidth / 2, centerY - bodyHeight / 2,
                                       bodyWidth, bodyHeight));

        // Draw head (smaller circle at top)
        double headRadius = 10;
        double headY = centerY - 8;

        g2d.setColor(bodyColor.brighter());
        g2d.fill(new Ellipse2D.Double(centerX - headRadius, headY - headRadius,
                                       headRadius * 2, headRadius * 2));

        // Draw ears (triangular shapes)
        Path2D leftEar = new Path2D.Double();
        leftEar.moveTo(centerX - 6, headY - 8);
        leftEar.lineTo(centerX - 10, headY - 16);
        leftEar.lineTo(centerX - 3, headY - 10);
        leftEar.closePath();

        Path2D rightEar = new Path2D.Double();
        rightEar.moveTo(centerX + 6, headY - 8);
        rightEar.lineTo(centerX + 10, headY - 16);
        rightEar.lineTo(centerX + 3, headY - 10);
        rightEar.closePath();

        g2d.setColor(earColor);
        g2d.fill(leftEar);
        g2d.fill(rightEar);

        // Draw inner ear detail
        g2d.setColor(earColor.brighter());
        Path2D leftEarInner = new Path2D.Double();
        leftEarInner.moveTo(centerX - 6, headY - 9);
        leftEarInner.lineTo(centerX - 8, headY - 14);
        leftEarInner.lineTo(centerX - 5, headY - 10);
        leftEarInner.closePath();
        g2d.fill(leftEarInner);

        Path2D rightEarInner = new Path2D.Double();
        rightEarInner.moveTo(centerX + 6, headY - 9);
        rightEarInner.lineTo(centerX + 8, headY - 14);
        rightEarInner.lineTo(centerX + 5, headY - 10);
        rightEarInner.closePath();
        g2d.fill(rightEarInner);

        // Draw wings (behind body, but drawn after so they're visible)
        // Left wing
        Path2D leftWing = new Path2D.Double();
        double leftWingStartX = centerX - bodyWidth / 2;
        double leftWingStartY = centerY;

        leftWing.moveTo(leftWingStartX, leftWingStartY);

        // Wing curves with flapping motion
        double wingSpan = 22;
        double wingHeight = 15;

        leftWing.curveTo(
            leftWingStartX - wingSpan * 0.5, leftWingStartY - wingHeight - wingAngle * 10,
            leftWingStartX - wingSpan, leftWingStartY - wingHeight * 0.3 - wingAngle * 15,
            leftWingStartX - wingSpan, leftWingStartY + wingHeight * 0.5
        );
        leftWing.lineTo(leftWingStartX, leftWingStartY + 5);
        leftWing.closePath();

        // Right wing (mirror)
        Path2D rightWing = new Path2D.Double();
        double rightWingStartX = centerX + bodyWidth / 2;
        double rightWingStartY = centerY;

        rightWing.moveTo(rightWingStartX, rightWingStartY);
        rightWing.curveTo(
            rightWingStartX + wingSpan * 0.5, rightWingStartY - wingHeight - wingAngle * 10,
            rightWingStartX + wingSpan, rightWingStartY - wingHeight * 0.3 - wingAngle * 15,
            rightWingStartX + wingSpan, rightWingStartY + wingHeight * 0.5
        );
        rightWing.lineTo(rightWingStartX, rightWingStartY + 5);
        rightWing.closePath();

        // Draw wing membranes
        g2d.setColor(wingMembraneColor);
        g2d.fill(leftWing);
        g2d.fill(rightWing);

        // Draw wing outlines
        g2d.setColor(wingColor);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(leftWing);
        g2d.draw(rightWing);

        // Draw wing bone structure (simple lines)
        g2d.setStroke(new BasicStroke(1.0f));
        // Left wing bones
        g2d.drawLine((int)leftWingStartX, (int)leftWingStartY,
                    (int)(leftWingStartX - wingSpan * 0.5), (int)(leftWingStartY - wingHeight - wingAngle * 10));
        g2d.drawLine((int)leftWingStartX, (int)leftWingStartY,
                    (int)(leftWingStartX - wingSpan), (int)(leftWingStartY - wingHeight * 0.3 - wingAngle * 15));
        g2d.drawLine((int)leftWingStartX, (int)leftWingStartY,
                    (int)(leftWingStartX - wingSpan), (int)(leftWingStartY + wingHeight * 0.5));

        // Right wing bones
        g2d.drawLine((int)rightWingStartX, (int)rightWingStartY,
                    (int)(rightWingStartX + wingSpan * 0.5), (int)(rightWingStartY - wingHeight - wingAngle * 10));
        g2d.drawLine((int)rightWingStartX, (int)rightWingStartY,
                    (int)(rightWingStartX + wingSpan), (int)(rightWingStartY - wingHeight * 0.3 - wingAngle * 15));
        g2d.drawLine((int)rightWingStartX, (int)rightWingStartY,
                    (int)(rightWingStartX + wingSpan), (int)(rightWingStartY + wingHeight * 0.5));

        // Draw eyes (glowing red)
        double eyeRadius = 2.5;
        double eyeSpacing = 5;

        g2d.setColor(eyeColor);
        g2d.fill(new Ellipse2D.Double(centerX - eyeSpacing - eyeRadius, headY - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));
        g2d.fill(new Ellipse2D.Double(centerX + eyeSpacing - eyeRadius, headY - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));

        // Eye glow effect
        g2d.setColor(new Color(255, 150, 150, 100));
        g2d.fill(new Ellipse2D.Double(centerX - eyeSpacing - eyeRadius * 1.5, headY - eyeRadius * 1.5,
                                       eyeRadius * 3, eyeRadius * 3));
        g2d.fill(new Ellipse2D.Double(centerX + eyeSpacing - eyeRadius * 1.5, headY - eyeRadius * 1.5,
                                       eyeRadius * 3, eyeRadius * 3));

        // Draw fangs (small triangles)
        g2d.setColor(Color.WHITE);
        Path2D leftFang = new Path2D.Double();
        leftFang.moveTo(centerX - 2, headY + 4);
        leftFang.lineTo(centerX - 3, headY + 7);
        leftFang.lineTo(centerX - 1, headY + 5);
        leftFang.closePath();
        g2d.fill(leftFang);

        Path2D rightFang = new Path2D.Double();
        rightFang.moveTo(centerX + 2, headY + 4);
        rightFang.lineTo(centerX + 3, headY + 7);
        rightFang.lineTo(centerX + 1, headY + 5);
        rightFang.closePath();
        g2d.fill(rightFang);

        // Draw small feet hanging down
        g2d.setColor(bodyColor.darker());
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine((int)(centerX - 4), (int)(centerY + bodyHeight / 2),
                    (int)(centerX - 4), (int)(centerY + bodyHeight / 2 + 4));
        g2d.drawLine((int)(centerX + 4), (int)(centerY + bodyHeight / 2),
                    (int)(centerX + 4), (int)(centerY + bodyHeight / 2 + 4));

        // Draw claws at end of feet
        g2d.drawLine((int)(centerX - 4), (int)(centerY + bodyHeight / 2 + 4),
                    (int)(centerX - 6), (int)(centerY + bodyHeight / 2 + 5));
        g2d.drawLine((int)(centerX - 4), (int)(centerY + bodyHeight / 2 + 4),
                    (int)(centerX - 2), (int)(centerY + bodyHeight / 2 + 5));

        g2d.drawLine((int)(centerX + 4), (int)(centerY + bodyHeight / 2 + 4),
                    (int)(centerX + 2), (int)(centerY + bodyHeight / 2 + 5));
        g2d.drawLine((int)(centerX + 4), (int)(centerY + bodyHeight / 2 + 4),
                    (int)(centerX + 6), (int)(centerY + bodyHeight / 2 + 5));

        g2d.dispose();
        return image;
    }
}
