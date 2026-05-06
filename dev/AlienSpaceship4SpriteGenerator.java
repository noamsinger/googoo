package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AlienSpaceship4SpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateAlienSpaceship4SpriteSheet();
            System.out.println("Successfully generated alien spaceship 4 sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateAlienSpaceship4SpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateAlienSpaceship4Frame(frame);

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
        System.out.println("Alien spaceship 4 sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateAlienSpaceship4Frame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for alien spaceship type 4 (biomechanical organic ship)
        Color hullColor = new Color(180, 100, 255); // Purple hull
        Color darkHullColor = new Color(100, 40, 150); // Dark purple
        Color accentColor = new Color(255, 150, 255); // Light magenta
        Color organicColor = new Color(150, 255, 200); // Cyan-green organic parts
        Color energyColor = new Color(255, 255, 100); // Yellow energy
        Color coreColor = new Color(255, 100, 255); // Bright magenta core

        // Pulsing animation (organic breathing effect)
        double pulse = 0.85 + Math.sin(progress * Math.PI * 2) * 0.15;

        // Slight wobble
        double wobble = Math.sin(progress * Math.PI * 2) * 1.5;

        // Draw energy aura/field
        for (int i = 3; i > 0; i--) {
            g2d.setColor(new Color(
                energyColor.getRed(),
                energyColor.getGreen(),
                energyColor.getBlue(),
                (int)(40 * pulse / i)
            ));
            double auraSize = 35 * pulse + i * 5;
            g2d.fill(new Ellipse2D.Double(
                centerX - auraSize / 2,
                centerY + wobble - auraSize / 2,
                auraSize, auraSize
            ));
        }

        // Draw main organic hull (rounded biomechanical shape)
        Path2D hull = new Path2D.Double();

        // Front section (rounded head)
        hull.moveTo(centerX, centerY - 16 * pulse + wobble);

        // Left side (organic curves)
        hull.curveTo(
            centerX - 10 * pulse, centerY - 12 * pulse + wobble,
            centerX - 14 * pulse, centerY - 4 + wobble,
            centerX - 14 * pulse, centerY + 4 + wobble
        );

        // Left rear
        hull.curveTo(
            centerX - 12 * pulse, centerY + 8 + wobble,
            centerX - 8 * pulse, centerY + 12 + wobble,
            centerX - 4, centerY + 14 + wobble
        );

        // Center rear (indented)
        hull.lineTo(centerX, centerY + 12 + wobble);

        // Right rear
        hull.lineTo(centerX + 4, centerY + 14 + wobble);
        hull.curveTo(
            centerX + 8 * pulse, centerY + 12 + wobble,
            centerX + 12 * pulse, centerY + 8 + wobble,
            centerX + 14 * pulse, centerY + 4 + wobble
        );

        // Right side
        hull.curveTo(
            centerX + 14 * pulse, centerY - 4 + wobble,
            centerX + 10 * pulse, centerY - 12 * pulse + wobble,
            centerX, centerY - 16 * pulse + wobble
        );

        hull.closePath();

        // Gradient for hull
        GradientPaint hullGradient = new GradientPaint(
            (float)centerX, (float)(centerY - 16 * pulse + wobble), accentColor,
            (float)centerX, (float)(centerY + 14 + wobble), darkHullColor
        );
        g2d.setPaint(hullGradient);
        g2d.fill(hull);

        // Draw organic veins/patterns
        g2d.setColor(organicColor);
        g2d.setStroke(new BasicStroke(1.5f));

        // Central vein
        Path2D vein1 = new Path2D.Double();
        vein1.moveTo(centerX, centerY - 14 * pulse + wobble);
        vein1.curveTo(
            centerX - 2, centerY - 6 + wobble,
            centerX + 2, centerY + 2 + wobble,
            centerX, centerY + 10 + wobble
        );
        g2d.draw(vein1);

        // Left veins
        Path2D vein2 = new Path2D.Double();
        vein2.moveTo(centerX - 6, centerY - 10 * pulse + wobble);
        vein2.curveTo(
            centerX - 10, centerY - 4 + wobble,
            centerX - 10, centerY + 2 + wobble,
            centerX - 6, centerY + 8 + wobble
        );
        g2d.draw(vein2);

        // Right veins
        Path2D vein3 = new Path2D.Double();
        vein3.moveTo(centerX + 6, centerY - 10 * pulse + wobble);
        vein3.curveTo(
            centerX + 10, centerY - 4 + wobble,
            centerX + 10, centerY + 2 + wobble,
            centerX + 6, centerY + 8 + wobble
        );
        g2d.draw(vein3);

        // Draw energy nodes/orbs
        g2d.setColor(new Color(
            energyColor.getRed(),
            energyColor.getGreen(),
            energyColor.getBlue(),
            (int)(200 * pulse)
        ));

        // Top node
        g2d.fill(new Ellipse2D.Double(centerX - 2, centerY - 10 * pulse + wobble, 4, 4));

        // Left node
        g2d.fill(new Ellipse2D.Double(centerX - 8, centerY + wobble, 3.5, 3.5));

        // Right node
        g2d.fill(new Ellipse2D.Double(centerX + 4.5, centerY + wobble, 3.5, 3.5));

        // Bottom node
        g2d.fill(new Ellipse2D.Double(centerX - 1.5, centerY + 6 + wobble, 3, 3));

        // Draw central core (pulsing energy)
        double coreSize = 8 * pulse;
        GradientPaint coreGradient = new GradientPaint(
            (float)centerX, (float)(centerY - coreSize / 2 + wobble), new Color(255, 255, 255),
            (float)centerX, (float)(centerY + coreSize / 2 + wobble), coreColor
        );
        g2d.setPaint(coreGradient);
        g2d.fill(new Ellipse2D.Double(
            centerX - coreSize / 2,
            centerY - coreSize / 2 + wobble,
            coreSize, coreSize
        ));

        // Core outline (glowing)
        g2d.setColor(new Color(
            255, 255, 255,
            (int)(200 * pulse)
        ));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.draw(new Ellipse2D.Double(
            centerX - coreSize / 2,
            centerY - coreSize / 2 + wobble,
            coreSize, coreSize
        ));

        // Draw organic tentacles/appendages (animated)
        g2d.setColor(organicColor);
        g2d.setStroke(new BasicStroke(2.0f));

        double tentacleWave = Math.sin(progress * Math.PI * 2) * 4;

        // Left tentacle
        Path2D leftTentacle = new Path2D.Double();
        leftTentacle.moveTo(centerX - 12, centerY + 6 + wobble);
        leftTentacle.curveTo(
            centerX - 16, centerY + 8 + wobble + tentacleWave,
            centerX - 18, centerY + 10 + wobble,
            centerX - 20, centerY + 14 + wobble + tentacleWave
        );
        g2d.draw(leftTentacle);

        // Right tentacle
        Path2D rightTentacle = new Path2D.Double();
        rightTentacle.moveTo(centerX + 12, centerY + 6 + wobble);
        rightTentacle.curveTo(
            centerX + 16, centerY + 8 + wobble - tentacleWave,
            centerX + 18, centerY + 10 + wobble,
            centerX + 20, centerY + 14 + wobble - tentacleWave
        );
        g2d.draw(rightTentacle);

        // Draw organic spots/bioluminescent areas
        g2d.setColor(new Color(
            organicColor.getRed(),
            organicColor.getGreen(),
            organicColor.getBlue(),
            (int)(150 * (1 - pulse))
        ));

        for (int i = 0; i < 6; i++) {
            double angle = (i / 6.0) * Math.PI * 2 + progress * Math.PI;
            double spotDist = 6 + i % 2;
            double spotX = centerX + Math.cos(angle) * spotDist;
            double spotY = centerY + Math.sin(angle) * spotDist + wobble;
            double spotSize = 1.5 + (i % 2) * 0.5;

            g2d.fill(new Ellipse2D.Double(spotX - spotSize / 2, spotY - spotSize / 2,
                                           spotSize, spotSize));
        }

        // Draw hull outline
        g2d.setColor(darkHullColor.darker());
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(hull);

        g2d.dispose();
        return image;
    }
}
