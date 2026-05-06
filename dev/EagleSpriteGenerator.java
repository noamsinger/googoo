package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class EagleSpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateEagleSpriteSheet();
            System.out.println("Successfully generated eagle sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateEagleSpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateEagleFrame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_6.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Eagle sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateEagleFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for eagle (from above)
        Color bodyColor = new Color(80, 60, 40); // Dark brown body
        Color wingColor = new Color(120, 90, 60); // Medium brown wings
        Color featherTipColor = new Color(40, 30, 20); // Dark brown feather tips
        Color headColor = new Color(220, 220, 200); // White/cream head
        Color beakColor = new Color(200, 180, 50); // Yellow beak
        Color eyeColor = new Color(220, 180, 50); // Golden eyes

        // Wing flapping animation - 90 degree range
        double wingFlap = Math.sin(progress * Math.PI * 2);
        double wingAngle = wingFlap * Math.PI / 4; // 45 degrees each direction = 90 degree range

        // Draw wings (spread wide from above)
        // Left wing
        Path2D leftWing = new Path2D.Double();
        double wingBaseX = centerX - 8;
        double wingBaseY = centerY;

        leftWing.moveTo(wingBaseX, wingBaseY);

        // Primary feathers (long outer feathers)
        for (int i = 0; i < 5; i++) {
            double featherAngle = Math.PI + Math.PI / 3 - i * Math.PI / 12 + wingAngle;
            double featherLength = 20 - i * 1.5;
            double tipX = wingBaseX + Math.cos(featherAngle) * featherLength;
            double tipY = wingBaseY + Math.sin(featherAngle) * featherLength;

            if (i == 0) {
                leftWing.lineTo(tipX, tipY);
            } else {
                leftWing.lineTo(tipX, tipY);
            }
        }

        // Wing trailing edge
        leftWing.curveTo(
            centerX - 12, centerY + 8,
            centerX - 8, centerY + 10,
            wingBaseX, wingBaseY + 8
        );

        leftWing.closePath();

        // Right wing (mirror)
        Path2D rightWing = new Path2D.Double();
        double rightWingBaseX = centerX + 8;

        rightWing.moveTo(rightWingBaseX, wingBaseY);

        // Primary feathers
        for (int i = 0; i < 5; i++) {
            double featherAngle = -Math.PI / 3 + i * Math.PI / 12 - wingAngle;
            double featherLength = 20 - i * 1.5;
            double tipX = rightWingBaseX + Math.cos(featherAngle) * featherLength;
            double tipY = wingBaseY + Math.sin(featherAngle) * featherLength;

            rightWing.lineTo(tipX, tipY);
        }

        // Wing trailing edge
        rightWing.curveTo(
            centerX + 12, centerY + 8,
            centerX + 8, centerY + 10,
            rightWingBaseX, wingBaseY + 8
        );

        rightWing.closePath();

        // Draw wings
        g2d.setColor(wingColor);
        g2d.fill(leftWing);
        g2d.fill(rightWing);

        // Draw feather details on wings
        g2d.setColor(featherTipColor);
        g2d.setStroke(new BasicStroke(1.5f));

        // Left wing feathers
        for (int i = 0; i < 5; i++) {
            double featherAngle = Math.PI + Math.PI / 3 - i * Math.PI / 12 + wingAngle;
            double featherLength = 20 - i * 1.5;
            double tipX = wingBaseX + Math.cos(featherAngle) * featherLength;
            double tipY = wingBaseY + Math.sin(featherAngle) * featherLength;

            g2d.drawLine((int)wingBaseX, (int)wingBaseY, (int)tipX, (int)tipY);
        }

        // Right wing feathers
        for (int i = 0; i < 5; i++) {
            double featherAngle = -Math.PI / 3 + i * Math.PI / 12 - wingAngle;
            double featherLength = 20 - i * 1.5;
            double tipX = rightWingBaseX + Math.cos(featherAngle) * featherLength;
            double tipY = wingBaseY + Math.sin(featherAngle) * featherLength;

            g2d.drawLine((int)rightWingBaseX, (int)wingBaseY, (int)tipX, (int)tipY);
        }

        // Draw body (central oval)
        double bodyWidth = 16;
        double bodyHeight = 20;

        g2d.setColor(bodyColor);
        g2d.fill(new Ellipse2D.Double(centerX - bodyWidth / 2, centerY - bodyHeight / 2 + 2,
                                       bodyWidth, bodyHeight));

        // Body feather pattern
        g2d.setColor(wingColor);
        for (int i = 0; i < 4; i++) {
            double patternY = centerY - 6 + i * 4;
            g2d.drawArc((int)(centerX - 6), (int)patternY, 12, 6, 0, 180);
        }

        // Draw tail (fan shape)
        Path2D tail = new Path2D.Double();
        double tailBaseY = centerY + 10;

        tail.moveTo(centerX - 5, tailBaseY);
        tail.lineTo(centerX - 8, tailBaseY + 12);
        tail.lineTo(centerX - 4, tailBaseY + 10);
        tail.lineTo(centerX, tailBaseY + 12);
        tail.lineTo(centerX + 4, tailBaseY + 10);
        tail.lineTo(centerX + 8, tailBaseY + 12);
        tail.lineTo(centerX + 5, tailBaseY);
        tail.closePath();

        g2d.setColor(bodyColor.darker());
        g2d.fill(tail);

        // Tail feather lines
        g2d.setColor(featherTipColor);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawLine((int)(centerX - 6), (int)tailBaseY, (int)(centerX - 8), (int)(tailBaseY + 12));
        g2d.drawLine((int)(centerX - 2), (int)tailBaseY, (int)(centerX - 4), (int)(tailBaseY + 10));
        g2d.drawLine((int)centerX, (int)tailBaseY, (int)centerX, (int)(tailBaseY + 12));
        g2d.drawLine((int)(centerX + 2), (int)tailBaseY, (int)(centerX + 4), (int)(tailBaseY + 10));
        g2d.drawLine((int)(centerX + 6), (int)tailBaseY, (int)(centerX + 8), (int)(tailBaseY + 12));

        // Draw head and neck
        double headWidth = 10;
        double headHeight = 12;
        double headY = centerY - 8;

        g2d.setColor(headColor);
        g2d.fill(new Ellipse2D.Double(centerX - headWidth / 2, headY - headHeight / 2,
                                       headWidth, headHeight));

        // Draw beak
        Path2D beak = new Path2D.Double();
        beak.moveTo(centerX - 2, headY - 4);
        beak.lineTo(centerX, headY - 8);
        beak.lineTo(centerX + 2, headY - 4);
        beak.closePath();

        g2d.setColor(beakColor);
        g2d.fill(beak);

        // Beak detail
        g2d.setColor(beakColor.darker());
        g2d.setStroke(new BasicStroke(0.8f));
        g2d.drawLine((int)centerX, (int)(headY - 8), (int)centerX, (int)(headY - 5));

        // Draw eyes
        g2d.setColor(eyeColor);
        double eyeRadius = 1.5;
        g2d.fill(new Ellipse2D.Double(centerX - 3 - eyeRadius, headY - 2 - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));
        g2d.fill(new Ellipse2D.Double(centerX + 3 - eyeRadius, headY - 2 - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));

        // Eye pupils
        g2d.setColor(Color.BLACK);
        double pupilRadius = 0.8;
        g2d.fill(new Ellipse2D.Double(centerX - 3 - pupilRadius, headY - 2 - pupilRadius,
                                       pupilRadius * 2, pupilRadius * 2));
        g2d.fill(new Ellipse2D.Double(centerX + 3 - pupilRadius, headY - 2 - pupilRadius,
                                       pupilRadius * 2, pupilRadius * 2));

        // Draw talons (visible at bottom)
        g2d.setColor(new Color(60, 60, 60));
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Left talon
        g2d.drawLine((int)(centerX - 3), (int)(centerY + 8),
                    (int)(centerX - 3), (int)(centerY + 11));
        g2d.drawLine((int)(centerX - 3), (int)(centerY + 11),
                    (int)(centerX - 5), (int)(centerY + 13));
        g2d.drawLine((int)(centerX - 3), (int)(centerY + 11),
                    (int)(centerX - 1), (int)(centerY + 13));

        // Right talon
        g2d.drawLine((int)(centerX + 3), (int)(centerY + 8),
                    (int)(centerX + 3), (int)(centerY + 11));
        g2d.drawLine((int)(centerX + 3), (int)(centerY + 11),
                    (int)(centerX + 1), (int)(centerY + 13));
        g2d.drawLine((int)(centerX + 3), (int)(centerY + 11),
                    (int)(centerX + 5), (int)(centerY + 13));

        g2d.dispose();
        return image;
    }
}
