package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class OctopusSpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateOctopusSpriteSheet();
            System.out.println("Successfully generated octopus sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateOctopusSpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateOctopusFrame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_7.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Octopus sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateOctopusFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for octopus
        Color bodyColor = new Color(200, 80, 150); // Reddish-purple body
        Color darkColor = new Color(140, 50, 100); // Dark purple
        Color lightColor = new Color(230, 120, 180); // Light pink highlights
        Color eyeColor = new Color(255, 255, 200); // Yellow-white eyes
        Color pupilColor = new Color(40, 40, 60); // Dark pupils
        Color suckerColor = new Color(255, 180, 200); // Light pink suckers

        // Pulsing animation for body
        double pulse = 0.95 + Math.sin(progress * Math.PI * 2) * 0.05;

        // Draw 8 tentacles radiating from center (viewed from above)
        int numTentacles = 8;
        for (int i = 0; i < numTentacles; i++) {
            double angle = (i / (double) numTentacles) * Math.PI * 2;

            // Each tentacle waves independently
            double waveOffset = Math.sin(progress * Math.PI * 2 + i * Math.PI / 4) * 0.3;
            double tentacleAngle = angle + waveOffset;

            // Draw tentacle as a curved path
            Path2D tentacle = new Path2D.Double();

            double baseX = centerX + Math.cos(angle) * 8 * pulse;
            double baseY = centerY + Math.sin(angle) * 8 * pulse;

            tentacle.moveTo(baseX, baseY);

            // Create curved tentacle with control points
            double midX = centerX + Math.cos(tentacleAngle) * 20;
            double midY = centerY + Math.sin(tentacleAngle) * 20;
            double tipX = centerX + Math.cos(tentacleAngle) * 28;
            double tipY = centerY + Math.sin(tentacleAngle) * 28;

            // Make tentacle taper by drawing a thick stroke
            double baseWidth = 6 * pulse;
            double midWidth = 4 * pulse;
            double tipWidth = 2 * pulse;

            // Draw tentacle body with gradient
            GradientPaint tentacleGradient = new GradientPaint(
                (float)baseX, (float)baseY, bodyColor,
                (float)tipX, (float)tipY, darkColor
            );
            g2d.setPaint(tentacleGradient);

            // Draw base section
            tentacle.curveTo(
                baseX + Math.cos(tentacleAngle) * 5, baseY + Math.sin(tentacleAngle) * 5,
                midX - Math.cos(tentacleAngle) * 3, midY - Math.sin(tentacleAngle) * 3,
                midX, midY
            );

            // Draw tip section
            tentacle.curveTo(
                midX + Math.cos(tentacleAngle) * 3, midY + Math.sin(tentacleAngle) * 3,
                tipX - Math.cos(tentacleAngle) * 2, tipY - Math.sin(tentacleAngle) * 2,
                tipX, tipY
            );

            g2d.setStroke(new BasicStroke((float)baseWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(tentacle);

            // Draw suckers along tentacle
            for (int s = 1; s <= 4; s++) {
                double suckerProgress = s / 5.0;
                double suckerX = baseX + (tipX - baseX) * suckerProgress;
                double suckerY = baseY + (tipY - baseY) * suckerProgress;
                double suckerSize = (3 - s * 0.4) * pulse;

                g2d.setColor(suckerColor);
                g2d.fill(new Ellipse2D.Double(suckerX - suckerSize / 2, suckerY - suckerSize / 2,
                                               suckerSize, suckerSize));

                // Sucker center
                g2d.setColor(darkColor);
                g2d.fill(new Ellipse2D.Double(suckerX - suckerSize / 4, suckerY - suckerSize / 4,
                                               suckerSize / 2, suckerSize / 2));
            }
        }

        // Draw main body (mantle/head - bulbous shape)
        double bodyRadius = 15 * pulse;

        // Body gradient
        GradientPaint bodyGradient = new GradientPaint(
            (float)(centerX - bodyRadius / 2), (float)(centerY - bodyRadius / 2), lightColor,
            (float)(centerX + bodyRadius / 2), (float)(centerY + bodyRadius / 2), bodyColor
        );
        g2d.setPaint(bodyGradient);
        g2d.fill(new Ellipse2D.Double(centerX - bodyRadius, centerY - bodyRadius,
                                       bodyRadius * 2, bodyRadius * 2));

        // Body texture spots
        g2d.setColor(darkColor);
        for (int i = 0; i < 8; i++) {
            double spotAngle = (i / 8.0) * Math.PI * 2 + progress * Math.PI;
            double spotDist = 5 + (i % 3) * 2;
            double spotX = centerX + Math.cos(spotAngle) * spotDist;
            double spotY = centerY + Math.sin(spotAngle) * spotDist;
            double spotSize = 2 + (i % 2);

            g2d.fill(new Ellipse2D.Double(spotX - spotSize / 2, spotY - spotSize / 2,
                                           spotSize, spotSize));
        }

        // Draw two large eyes
        double eyeOffset = 6;
        double eyeRadius = 4;

        // Left eye
        g2d.setColor(eyeColor);
        g2d.fill(new Ellipse2D.Double(centerX - eyeOffset - eyeRadius, centerY - 3 - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));

        // Left pupil
        double pupilRadius = 2;
        g2d.setColor(pupilColor);
        g2d.fill(new Ellipse2D.Double(centerX - eyeOffset - pupilRadius, centerY - 3 - pupilRadius,
                                       pupilRadius * 2, pupilRadius * 2));

        // Right eye
        g2d.setColor(eyeColor);
        g2d.fill(new Ellipse2D.Double(centerX + eyeOffset - eyeRadius, centerY - 3 - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));

        // Right pupil
        g2d.setColor(pupilColor);
        g2d.fill(new Ellipse2D.Double(centerX + eyeOffset - pupilRadius, centerY - 3 - pupilRadius,
                                       pupilRadius * 2, pupilRadius * 2));

        // Eye shine
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fill(new Ellipse2D.Double(centerX - eyeOffset - 1.5, centerY - 4, 1.5, 1.5));
        g2d.fill(new Ellipse2D.Double(centerX + eyeOffset - 1.5, centerY - 4, 1.5, 1.5));

        // Body outline
        g2d.setColor(darkColor);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(new Ellipse2D.Double(centerX - bodyRadius, centerY - bodyRadius,
                                       bodyRadius * 2, bodyRadius * 2));

        g2d.dispose();
        return image;
    }
}
