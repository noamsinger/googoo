package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MosquitoSpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateMosquitoSpriteSheet();
            System.out.println("Successfully generated mosquito sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateMosquitoSpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateMosquitoFrame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_8.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Mosquito sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateMosquitoFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for mosquito
        Color bodyColor = new Color(100, 100, 120); // Gray-blue body
        Color stripesColor = new Color(200, 180, 100); // Yellow-tan stripes
        Color legColor = new Color(80, 70, 60); // Dark brown legs
        Color wingColor = new Color(200, 220, 240, 100); // Translucent white wings
        Color eyeColor = new Color(180, 50, 50); // Red eyes

        // Wing flapping animation - very fast
        double wingFlap = Math.abs(Math.sin(progress * Math.PI * 4)); // Fast flapping
        double wingAngle = wingFlap * Math.PI / 4; // 0 to 45 degrees

        // Draw wings (behind body)
        // Wings are nearly invisible when moving fast, more visible when slow
        double wingOpacity = 100 + wingFlap * 100;

        // Left wing
        Path2D leftWing = new Path2D.Double();
        double wingBaseX = centerX - 3;
        double wingBaseY = centerY - 5;

        leftWing.moveTo(wingBaseX, wingBaseY);
        leftWing.curveTo(
            wingBaseX - 20, wingBaseY - 15 - wingAngle * 5,
            wingBaseX - 22, wingBaseY - 5 - wingAngle * 8,
            wingBaseX - 18, wingBaseY + 5
        );
        leftWing.lineTo(wingBaseX, wingBaseY + 3);
        leftWing.closePath();

        // Right wing (mirror)
        Path2D rightWing = new Path2D.Double();
        double rightWingBaseX = centerX + 3;

        rightWing.moveTo(rightWingBaseX, wingBaseY);
        rightWing.curveTo(
            rightWingBaseX + 20, wingBaseY - 15 - wingAngle * 5,
            rightWingBaseX + 22, wingBaseY - 5 - wingAngle * 8,
            rightWingBaseX + 18, wingBaseY + 5
        );
        rightWing.lineTo(rightWingBaseX, wingBaseY + 3);
        rightWing.closePath();

        g2d.setColor(new Color(200, 220, 240, (int)wingOpacity));
        g2d.fill(leftWing);
        g2d.fill(rightWing);

        // Wing veins
        g2d.setColor(new Color(150, 170, 190, (int)(wingOpacity * 0.8)));
        g2d.setStroke(new BasicStroke(0.5f));
        g2d.drawLine((int)wingBaseX, (int)wingBaseY, (int)(wingBaseX - 18), (int)(wingBaseY - 10 - wingAngle * 5));
        g2d.drawLine((int)wingBaseX, (int)wingBaseY, (int)(wingBaseX - 18), (int)(wingBaseY + 3));
        g2d.drawLine((int)rightWingBaseX, (int)wingBaseY, (int)(rightWingBaseX + 18), (int)(wingBaseY - 10 - wingAngle * 5));
        g2d.drawLine((int)rightWingBaseX, (int)wingBaseY, (int)(rightWingBaseX + 18), (int)(wingBaseY + 3));

        // Draw 6 legs (3 on each side)
        g2d.setColor(legColor);
        g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (int side = 0; side < 2; side++) {
            int direction = (side == 0) ? -1 : 1;

            for (int i = 0; i < 3; i++) {
                double legStartY = centerY - 3 + i * 4;
                double legStartX = centerX + direction * 2;

                // Leg animation
                double legPhase = Math.sin(progress * Math.PI * 2 + i * Math.PI / 3) * 0.2;
                double legAngle = direction * (Math.PI / 2.5 + legPhase);

                // First segment
                double leg1Length = 8;
                double leg1EndX = legStartX + Math.cos(legAngle) * leg1Length;
                double leg1EndY = legStartY + Math.sin(legAngle) * leg1Length * 0.5;

                g2d.drawLine((int)legStartX, (int)legStartY, (int)leg1EndX, (int)leg1EndY);

                // Second segment
                double leg2Angle = legAngle + direction * Math.PI / 3;
                double leg2Length = 6;
                double leg2EndX = leg1EndX + Math.cos(leg2Angle) * leg2Length;
                double leg2EndY = leg1EndY + Math.sin(leg2Angle) * leg2Length;

                g2d.drawLine((int)leg1EndX, (int)leg1EndY, (int)leg2EndX, (int)leg2EndY);
            }
        }

        // Draw abdomen (segmented, striped)
        double abdomenWidth = 6;
        double abdomenLength = 18;
        int numSegments = 6;

        for (int i = 0; i < numSegments; i++) {
            double segT = i / (double) numSegments;
            double segY = centerY + 2 + segT * abdomenLength;
            double segWidth = abdomenWidth * (1.0 - segT * 0.3);

            // Alternate colors for segments
            if (i % 2 == 0) {
                g2d.setColor(bodyColor);
            } else {
                g2d.setColor(stripesColor);
            }

            g2d.fill(new Ellipse2D.Double(centerX - segWidth / 2, segY - 1.5,
                                           segWidth, 3));
        }

        // Draw thorax (middle body part)
        double thoraxWidth = 5;
        double thoraxHeight = 8;

        g2d.setColor(bodyColor.darker());
        g2d.fill(new Ellipse2D.Double(centerX - thoraxWidth / 2, centerY - thoraxHeight / 2,
                                       thoraxWidth, thoraxHeight));

        // Draw head
        double headRadius = 3.5;

        g2d.setColor(bodyColor);
        g2d.fill(new Ellipse2D.Double(centerX - headRadius, centerY - 8 - headRadius,
                                       headRadius * 2, headRadius * 2));

        // Draw proboscis (long needle-like mouth)
        g2d.setColor(legColor.darker());
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine((int)centerX, (int)(centerY - 8), (int)centerX, (int)(centerY - 18));

        // Draw proboscis tip
        g2d.setColor(new Color(60, 50, 40));
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine((int)centerX, (int)(centerY - 18), (int)centerX, (int)(centerY - 20));

        // Draw eyes (compound eyes)
        g2d.setColor(eyeColor);
        double eyeRadius = 1.5;
        g2d.fill(new Ellipse2D.Double(centerX - 3 - eyeRadius, centerY - 9 - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));
        g2d.fill(new Ellipse2D.Double(centerX + 3 - eyeRadius, centerY - 9 - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));

        // Draw antennae
        g2d.setColor(legColor);
        g2d.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Left antenna
        double antennaAngle = -Math.PI / 4;
        double antennaLength = 8;
        g2d.drawLine((int)(centerX - 2), (int)(centerY - 10),
                    (int)(centerX - 2 + Math.cos(antennaAngle) * antennaLength),
                    (int)(centerY - 10 + Math.sin(antennaAngle) * antennaLength));

        // Right antenna
        double rightAntennaAngle = -Math.PI * 3 / 4;
        g2d.drawLine((int)(centerX + 2), (int)(centerY - 10),
                    (int)(centerX + 2 + Math.cos(rightAntennaAngle) * antennaLength),
                    (int)(centerY - 10 + Math.sin(rightAntennaAngle) * antennaLength));

        g2d.dispose();
        return image;
    }
}
