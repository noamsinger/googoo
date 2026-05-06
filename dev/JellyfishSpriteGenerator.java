package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class JellyfishSpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateJellyfishSpriteSheet();
            System.out.println("Successfully generated jellyfish sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateJellyfishSpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateJellyfishFrame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_4.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Jellyfish sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateJellyfishFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for jellyfish
        Color bellColor = new Color(100, 180, 255); // Light blue bell
        Color bellRimColor = new Color(150, 200, 255); // Lighter blue rim
        Color tentacleColor = new Color(50, 150, 255); // Darker blue tentacles
        Color glowColor = new Color(200, 230, 255); // Glow effect

        // Pulsing animation for bell
        double pulseProgress = Math.sin(progress * Math.PI * 2);
        double bellScale = 1.0 + pulseProgress * 0.15;

        // Draw bell (dome shape)
        double bellWidth = 24 * bellScale;
        double bellHeight = 20 * bellScale;
        double bellTop = centerY - 8;

        // Create bell shape using Path2D
        Path2D bell = new Path2D.Double();
        bell.moveTo(centerX - bellWidth / 2, bellTop + bellHeight);

        // Left curve of bell
        bell.curveTo(
            centerX - bellWidth / 2, bellTop + bellHeight * 0.3,
            centerX - bellWidth / 2, bellTop,
            centerX, bellTop - 2
        );

        // Right curve of bell
        bell.curveTo(
            centerX + bellWidth / 2, bellTop,
            centerX + bellWidth / 2, bellTop + bellHeight * 0.3,
            centerX + bellWidth / 2, bellTop + bellHeight
        );

        bell.closePath();

        // Draw glow effect behind bell
        g2d.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 50));
        g2d.fill(new Ellipse2D.Double(centerX - bellWidth / 2 - 3, bellTop - 5,
                                       bellWidth + 6, bellHeight + 6));

        // Draw bell with gradient
        GradientPaint bellGradient = new GradientPaint(
            (float)centerX, (float)bellTop, bellRimColor,
            (float)centerX, (float)(bellTop + bellHeight), bellColor
        );
        g2d.setPaint(bellGradient);
        g2d.fill(bell);

        // Draw bell outline
        g2d.setColor(bellColor.darker());
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(bell);

        // Draw frilly underside
        g2d.setColor(bellRimColor);
        Path2D frills = new Path2D.Double();
        frills.moveTo(centerX - bellWidth / 2, bellTop + bellHeight);

        int numFrills = 8;
        for (int i = 0; i <= numFrills; i++) {
            double t = i / (double) numFrills;
            double x = centerX - bellWidth / 2 + t * bellWidth;
            double y = bellTop + bellHeight + Math.sin(t * Math.PI * numFrills + progress * Math.PI * 2) * 2;
            if (i == 0) {
                frills.moveTo(x, y);
            } else {
                frills.lineTo(x, y);
            }
        }

        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(frills);

        // Draw tentacles (4 main tentacles)
        int numTentacles = 4;
        double tentacleStartY = bellTop + bellHeight;

        for (int i = 0; i < numTentacles; i++) {
            double xOffset = (i - 1.5) * 6;
            double startX = centerX + xOffset;

            // Each tentacle has its own wave phase
            double wavePhase = progress * Math.PI * 2 + (i * Math.PI / 2);

            // Draw wavy tentacle
            Path2D tentacle = new Path2D.Double();
            tentacle.moveTo(startX, tentacleStartY);

            int tentacleSegments = 8;
            double tentacleLength = 25;

            for (int seg = 1; seg <= tentacleSegments; seg++) {
                double segT = seg / (double) tentacleSegments;
                double x = startX + Math.sin(segT * Math.PI * 3 + wavePhase) * 4;
                double y = tentacleStartY + segT * tentacleLength;
                tentacle.lineTo(x, y);
            }

            g2d.setColor(tentacleColor);
            g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(tentacle);
        }

        // Draw inner pattern on bell
        g2d.setColor(new Color(bellRimColor.getRed(), bellRimColor.getGreen(), bellRimColor.getBlue(), 150));
        double innerBellWidth = bellWidth * 0.6;
        double innerBellHeight = bellHeight * 0.5;
        g2d.fill(new Ellipse2D.Double(centerX - innerBellWidth / 2, bellTop + 5,
                                       innerBellWidth, innerBellHeight));

        // Draw central spot pattern
        g2d.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 200));
        for (int i = 0; i < 3; i++) {
            double angle = (i / 3.0) * Math.PI * 2 + progress * Math.PI;
            double spotRadius = 2;
            double orbitRadius = 5;
            double spotX = centerX + Math.cos(angle) * orbitRadius;
            double spotY = bellTop + bellHeight / 2 + Math.sin(angle) * orbitRadius;
            g2d.fill(new Ellipse2D.Double(spotX - spotRadius, spotY - spotRadius,
                                           spotRadius * 2, spotRadius * 2));
        }

        g2d.dispose();
        return image;
    }
}
