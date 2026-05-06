package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class FireballSpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateFireballSpriteSheet();
            System.out.println("Successfully generated fireball sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateFireballSpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateFireballFrame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_15.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Fireball sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateFireballFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich red-yellow-orange color scheme for blazing fireball
        Color whiteCore = new Color(255, 255, 255); // Pure white hot core
        Color brightYellow = new Color(255, 255, 150); // Bright yellow
        Color yellow = new Color(255, 220, 0); // Yellow
        Color lightOrange = new Color(255, 180, 0); // Light orange
        Color orange = new Color(255, 140, 0); // Orange
        Color deepOrange = new Color(255, 100, 0); // Deep orange
        Color brightRed = new Color(255, 60, 0); // Bright red
        Color darkRed = new Color(200, 20, 0); // Dark red
        Color veryDarkRed = new Color(120, 10, 0); // Very dark red trail

        Random random = new Random(frameIndex * 1000); // Consistent randomness per frame

        // Pulsing and flickering animation
        double pulse = 0.9 + Math.sin(progress * Math.PI * 2) * 0.1;
        double flicker = 0.95 + Math.sin(progress * Math.PI * 8) * 0.05;
        double rotation = progress * Math.PI * 2;

        // Draw heat distortion glow (outermost layer)
        for (int i = 3; i >= 0; i--) {
            g2d.setColor(new Color(
                255, 100, 0,
                (int)(20 * pulse / (i + 1))
            ));
            double heatSize = 45 + i * 8;
            g2d.fill(new Ellipse2D.Double(
                centerX - heatSize / 2,
                centerY - heatSize / 2,
                heatSize, heatSize
            ));
        }

        // Draw trailing smoke/fire particles behind
        for (int i = 0; i < 20; i++) {
            double trailProgress = i / 20.0;
            double trailX = centerX + (random.nextDouble() - 0.5) * 12;
            double trailY = centerY + 15 + trailProgress * 20;
            double trailSize = (3 + random.nextDouble() * 6) * (1.0 - trailProgress);
            double trailOpacity = (1.0 - trailProgress) * 0.7;

            Color particleColor;
            double colorChoice = random.nextDouble();
            if (colorChoice < 0.2) {
                particleColor = orange;
            } else if (colorChoice < 0.5) {
                particleColor = brightRed;
            } else if (colorChoice < 0.8) {
                particleColor = darkRed;
            } else {
                particleColor = veryDarkRed;
            }

            g2d.setColor(new Color(
                particleColor.getRed(),
                particleColor.getGreen(),
                particleColor.getBlue(),
                (int)(trailOpacity * 255)
            ));
            g2d.fill(new Ellipse2D.Double(trailX - trailSize / 2, trailY - trailSize / 2,
                                           trailSize, trailSize));
        }

        double baseRadius = 18 * pulse * flicker;

        // Draw outer red flame tendrils (rotating)
        for (int i = 0; i < 8; i++) {
            double angle = (i / 8.0) * Math.PI * 2 + rotation;
            double tendrilLength = 12 + random.nextDouble() * 8;
            double tendrilX = centerX + Math.cos(angle) * tendrilLength;
            double tendrilY = centerY + Math.sin(angle) * tendrilLength;
            double tendrilSize = 6 + random.nextDouble() * 4;

            g2d.setColor(new Color(
                brightRed.getRed(),
                brightRed.getGreen(),
                brightRed.getBlue(),
                150
            ));
            g2d.fill(new Ellipse2D.Double(
                tendrilX - tendrilSize / 2, tendrilY - tendrilSize / 2,
                tendrilSize, tendrilSize
            ));
        }

        // Draw outer flame layer (dark red) with irregular edges
        for (int angle = 0; angle < 360; angle += 12) {
            double rad = Math.toRadians(angle);
            double offset = Math.sin(angle * 0.1 + progress * Math.PI * 4) * 4;
            double flameRadius = baseRadius * 1.4 + offset;
            double flameX = centerX + Math.cos(rad) * (offset * 0.3);
            double flameY = centerY + Math.sin(rad) * (offset * 0.3);

            g2d.setColor(new Color(
                darkRed.getRed(),
                darkRed.getGreen(),
                darkRed.getBlue(),
                120
            ));
            g2d.fill(new Ellipse2D.Double(
                flameX - flameRadius,
                flameY - flameRadius,
                flameRadius * 2,
                flameRadius * 2
            ));
        }

        // Draw bright red layer
        double redRadius = baseRadius * 1.1;
        g2d.setColor(new Color(brightRed.getRed(), brightRed.getGreen(), brightRed.getBlue(), 200));
        g2d.fill(new Ellipse2D.Double(
            centerX - redRadius, centerY - redRadius,
            redRadius * 2, redRadius * 2
        ));

        // Draw deep orange layer
        double deepOrangeRadius = baseRadius * 0.85;
        g2d.setColor(deepOrange);
        g2d.fill(new Ellipse2D.Double(
            centerX - deepOrangeRadius, centerY - deepOrangeRadius,
            deepOrangeRadius * 2, deepOrangeRadius * 2
        ));

        // Draw orange layer
        double orangeRadius = baseRadius * 0.7;
        g2d.setColor(orange);
        g2d.fill(new Ellipse2D.Double(
            centerX - orangeRadius, centerY - orangeRadius,
            orangeRadius * 2, orangeRadius * 2
        ));

        // Draw light orange layer
        double lightOrangeRadius = baseRadius * 0.55;
        g2d.setColor(lightOrange);
        g2d.fill(new Ellipse2D.Double(
            centerX - lightOrangeRadius, centerY - lightOrangeRadius,
            lightOrangeRadius * 2, lightOrangeRadius * 2
        ));

        // Draw yellow layer
        double yellowRadius = baseRadius * 0.4;
        g2d.setColor(yellow);
        g2d.fill(new Ellipse2D.Double(
            centerX - yellowRadius, centerY - yellowRadius,
            yellowRadius * 2, yellowRadius * 2
        ));

        // Draw bright yellow layer
        double brightYellowRadius = baseRadius * 0.25;
        g2d.setColor(brightYellow);
        g2d.fill(new Ellipse2D.Double(
            centerX - brightYellowRadius, centerY - brightYellowRadius,
            brightYellowRadius * 2, brightYellowRadius * 2
        ));

        // Draw white hot core
        double coreRadius = baseRadius * 0.15;
        g2d.setColor(whiteCore);
        g2d.fill(new Ellipse2D.Double(
            centerX - coreRadius, centerY - coreRadius,
            coreRadius * 2, coreRadius * 2
        ));

        // Draw prominent upward flames
        for (int i = 0; i < 16; i++) {
            double flameAngle = Math.PI * 1.3 + (random.nextDouble() - 0.5) * 1.4;
            double flameLength = 8 + random.nextDouble() * 18;
            double flameX = centerX + Math.cos(flameAngle) * flameLength;
            double flameY = centerY + Math.sin(flameAngle) * flameLength;
            double flameSize = 3 + random.nextDouble() * 7;

            Color flameColor;
            double colorChoice = random.nextDouble();
            if (colorChoice < 0.2) {
                flameColor = yellow;
            } else if (colorChoice < 0.5) {
                flameColor = orange;
            } else if (colorChoice < 0.8) {
                flameColor = deepOrange;
            } else {
                flameColor = brightRed;
            }

            g2d.setColor(new Color(
                flameColor.getRed(),
                flameColor.getGreen(),
                flameColor.getBlue(),
                220
            ));
            g2d.fill(new Ellipse2D.Double(
                flameX - flameSize / 2, flameY - flameSize / 2,
                flameSize, flameSize
            ));
        }

        // Draw side flames (more chaotic)
        for (int i = 0; i < 12; i++) {
            double sideAngle = (random.nextDouble() - 0.5) * Math.PI * 2;
            double sideLength = baseRadius + random.nextDouble() * 10;
            double sideX = centerX + Math.cos(sideAngle) * sideLength;
            double sideY = centerY + Math.sin(sideAngle) * sideLength;
            double sideSize = 2 + random.nextDouble() * 5;

            Color sideColor = (i % 3 == 0) ? yellow : (i % 3 == 1) ? orange : brightRed;
            g2d.setColor(new Color(
                sideColor.getRed(),
                sideColor.getGreen(),
                sideColor.getBlue(),
                180
            ));
            g2d.fill(new Ellipse2D.Double(
                sideX - sideSize / 2, sideY - sideSize / 2,
                sideSize, sideSize
            ));
        }

        // Add sparks
        for (int i = 0; i < 8; i++) {
            double sparkAngle = random.nextDouble() * Math.PI * 2;
            double sparkDist = baseRadius + random.nextDouble() * 15;
            double sparkX = centerX + Math.cos(sparkAngle) * sparkDist;
            double sparkY = centerY + Math.sin(sparkAngle) * sparkDist;
            double sparkSize = 1 + random.nextDouble() * 2;

            g2d.setColor(new Color(255, 255, 200, 255));
            g2d.fill(new Ellipse2D.Double(
                sparkX - sparkSize / 2, sparkY - sparkSize / 2,
                sparkSize, sparkSize
            ));
        }

        g2d.dispose();
        return image;
    }
}
