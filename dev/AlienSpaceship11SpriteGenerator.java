package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AlienSpaceship11SpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateAlienSpaceship11SpriteSheet();
            System.out.println("Successfully generated alien spaceship 11 sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateAlienSpaceship11SpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateAlienSpaceship11Frame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_11.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Alien spaceship 11 sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateAlienSpaceship11Frame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for alien spaceship type 11 (energy blade design)
        Color hullColor = new Color(220, 220, 255); // Light silver-white hull
        Color darkHullColor = new Color(140, 140, 180); // Purple-gray
        Color bladeColor = new Color(0, 255, 255); // Cyan energy blade
        Color darkBladeColor = new Color(0, 180, 180); // Dark cyan
        Color coreColor = new Color(255, 50, 255); // Magenta core
        Color energyColor = new Color(150, 100, 255); // Purple energy
        Color accentColor = new Color(255, 200, 100); // Gold accent

        // Pulsing animation
        double pulse = 0.85 + Math.sin(progress * Math.PI * 2) * 0.15;

        // Blade rotation animation
        double bladeRotation = progress * Math.PI * 2;

        // Draw energy field
        for (int i = 2; i >= 0; i--) {
            g2d.setColor(new Color(
                energyColor.getRed(),
                energyColor.getGreen(),
                energyColor.getBlue(),
                (int)(50 * pulse / (i + 1))
            ));
            double fieldSize = 32 + i * 5;
            g2d.fill(new Ellipse2D.Double(
                centerX - fieldSize / 2,
                centerY - fieldSize / 2,
                fieldSize, fieldSize
            ));
        }

        // Draw rotating energy blades
        int numBlades = 3;
        for (int i = 0; i < numBlades; i++) {
            double angle = (i / (double) numBlades) * Math.PI * 2 + bladeRotation;

            Path2D blade = new Path2D.Double();

            // Calculate blade points
            double bladeLength = 18 * pulse;
            double bladeWidth = 4;

            double baseX = centerX + Math.cos(angle) * 6;
            double baseY = centerY + Math.sin(angle) * 6;
            double tipX = centerX + Math.cos(angle) * bladeLength;
            double tipY = centerY + Math.sin(angle) * bladeLength;

            // Perpendicular angle for width
            double perpAngle = angle + Math.PI / 2;

            // Build blade shape
            blade.moveTo(
                baseX + Math.cos(perpAngle) * bladeWidth,
                baseY + Math.sin(perpAngle) * bladeWidth
            );
            blade.lineTo(
                baseX + Math.cos(perpAngle) * bladeWidth * 0.5,
                baseY + Math.sin(perpAngle) * bladeWidth * 0.5
            );
            blade.lineTo(tipX, tipY);
            blade.lineTo(
                baseX - Math.cos(perpAngle) * bladeWidth * 0.5,
                baseY - Math.sin(perpAngle) * bladeWidth * 0.5
            );
            blade.lineTo(
                baseX - Math.cos(perpAngle) * bladeWidth,
                baseY - Math.sin(perpAngle) * bladeWidth
            );
            blade.closePath();

            // Gradient for blade
            GradientPaint bladeGradient = new GradientPaint(
                (float)baseX, (float)baseY, bladeColor,
                (float)tipX, (float)tipY, new Color(0, 255, 255, 0)
            );
            g2d.setPaint(bladeGradient);
            g2d.fill(blade);

            // Blade glow
            g2d.setColor(new Color(
                bladeColor.getRed(),
                bladeColor.getGreen(),
                bladeColor.getBlue(),
                (int)(150 * pulse)
            ));
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawLine((int)baseX, (int)baseY, (int)tipX, (int)tipY);
        }

        // Draw main hull (circular core with triangular details)
        double coreRadius = 10 * pulse;

        // Main core circle
        GradientPaint coreGradient = new GradientPaint(
            (float)(centerX - coreRadius), (float)(centerY - coreRadius), hullColor,
            (float)(centerX + coreRadius), (float)(centerY + coreRadius), darkHullColor
        );
        g2d.setPaint(coreGradient);
        g2d.fill(new Ellipse2D.Double(
            centerX - coreRadius,
            centerY - coreRadius,
            coreRadius * 2, coreRadius * 2
        ));

        // Draw triangular hull segments
        int numSegments = 6;
        for (int i = 0; i < numSegments; i++) {
            double angle = (i / (double) numSegments) * Math.PI * 2;

            Path2D segment = new Path2D.Double();
            double innerR = coreRadius * 0.7;
            double outerR = coreRadius;

            double angle1 = angle - Math.PI / numSegments;
            double angle2 = angle + Math.PI / numSegments;

            segment.moveTo(centerX, centerY);
            segment.lineTo(
                centerX + Math.cos(angle1) * outerR,
                centerY + Math.sin(angle1) * outerR
            );
            segment.lineTo(
                centerX + Math.cos(angle) * innerR,
                centerY + Math.sin(angle) * innerR
            );
            segment.lineTo(
                centerX + Math.cos(angle2) * outerR,
                centerY + Math.sin(angle2) * outerR
            );
            segment.closePath();

            if (i % 2 == 0) {
                g2d.setColor(accentColor);
                g2d.fill(segment);
            }
        }

        // Draw central energy core
        double innerCoreSize = 6 * pulse;
        GradientPaint innerCoreGradient = new GradientPaint(
            (float)(centerX - innerCoreSize / 2), (float)(centerY - innerCoreSize / 2), new Color(255, 255, 255),
            (float)(centerX + innerCoreSize / 2), (float)(centerY + innerCoreSize / 2), coreColor
        );
        g2d.setPaint(innerCoreGradient);
        g2d.fill(new Ellipse2D.Double(
            centerX - innerCoreSize / 2,
            centerY - innerCoreSize / 2,
            innerCoreSize, innerCoreSize
        ));

        // Draw energy orbs around core
        int numOrbs = 4;
        for (int i = 0; i < numOrbs; i++) {
            double angle = (i / (double) numOrbs) * Math.PI * 2 - bladeRotation * 0.5;
            double orbDist = coreRadius * 0.85;
            double orbX = centerX + Math.cos(angle) * orbDist;
            double orbY = centerY + Math.sin(angle) * orbDist;
            double orbSize = 3 * pulse;

            g2d.setColor(energyColor);
            g2d.fill(new Ellipse2D.Double(
                orbX - orbSize / 2,
                orbY - orbSize / 2,
                orbSize, orbSize
            ));

            // Orb glow
            g2d.setColor(new Color(
                energyColor.getRed(),
                energyColor.getGreen(),
                energyColor.getBlue(),
                (int)(120 * pulse)
            ));
            g2d.fill(new Ellipse2D.Double(
                orbX - orbSize,
                orbY - orbSize,
                orbSize * 2, orbSize * 2
            ));
        }

        // Draw outer ring segments (rotating opposite direction)
        g2d.setStroke(new BasicStroke(2.0f));
        int numRingSegments = 8;
        for (int i = 0; i < numRingSegments; i++) {
            if (i % 2 == 0) continue; // Only draw alternating segments

            double angle1 = (i / (double) numRingSegments) * Math.PI * 2 - bladeRotation;
            double angle2 = ((i + 1) / (double) numRingSegments) * Math.PI * 2 - bladeRotation;
            double ringRadius = coreRadius * 1.3;

            g2d.setColor(new Color(
                darkBladeColor.getRed(),
                darkBladeColor.getGreen(),
                darkBladeColor.getBlue(),
                (int)(200 * pulse)
            ));

            Path2D arc = new Path2D.Double();
            arc.moveTo(
                centerX + Math.cos(angle1) * ringRadius,
                centerY + Math.sin(angle1) * ringRadius
            );

            // Draw arc with multiple segments for smoothness
            int arcSteps = 5;
            for (int step = 1; step <= arcSteps; step++) {
                double t = step / (double) arcSteps;
                double angle = angle1 + (angle2 - angle1) * t;
                arc.lineTo(
                    centerX + Math.cos(angle) * ringRadius,
                    centerY + Math.sin(angle) * ringRadius
                );
            }

            g2d.draw(arc);
        }

        // Draw core outline
        g2d.setColor(darkHullColor.darker());
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(new Ellipse2D.Double(
            centerX - coreRadius,
            centerY - coreRadius,
            coreRadius * 2, coreRadius * 2
        ));

        // Draw pulsing energy rings
        for (int ring = 0; ring < 2; ring++) {
            double ringProgress = (progress + ring * 0.5) % 1.0;
            double ringSize = coreRadius + ringProgress * 8;
            int ringAlpha = (int)((1.0 - ringProgress) * 120);

            g2d.setColor(new Color(
                bladeColor.getRed(),
                bladeColor.getGreen(),
                bladeColor.getBlue(),
                ringAlpha
            ));
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.draw(new Ellipse2D.Double(
                centerX - ringSize,
                centerY - ringSize,
                ringSize * 2, ringSize * 2
            ));
        }

        g2d.dispose();
        return image;
    }
}
