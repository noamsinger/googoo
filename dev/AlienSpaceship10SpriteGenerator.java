package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AlienSpaceship10SpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateAlienSpaceship10SpriteSheet();
            System.out.println("Successfully generated alien spaceship 10 sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateAlienSpaceship10SpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateAlienSpaceship10Frame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_10.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Alien spaceship 10 sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateAlienSpaceship10Frame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for alien spaceship type 10 (stealth angular design)
        Color hullColor = new Color(40, 40, 80); // Dark blue-gray hull
        Color darkHullColor = new Color(20, 20, 40); // Very dark blue-gray
        Color accentColor = new Color(100, 150, 255); // Light blue accent
        Color edgeColor = new Color(150, 200, 255); // Cyan edge highlights
        Color engineColor = new Color(255, 100, 50); // Orange engine glow
        Color coreColor = new Color(255, 200, 0); // Yellow core
        Color weaponColor = new Color(0, 255, 150); // Green weapon lights

        // Pulsing animation
        double pulse = 0.85 + Math.sin(progress * Math.PI * 2) * 0.15;

        // Subtle rotation
        double rotation = progress * Math.PI * 0.5;

        // Draw engine glow trails
        for (int i = 2; i >= 0; i--) {
            g2d.setColor(new Color(
                engineColor.getRed(),
                engineColor.getGreen(),
                engineColor.getBlue(),
                (int)(60 * pulse / (i + 1))
            ));
            double glowSize = 12 + i * 4;
            g2d.fill(new Ellipse2D.Double(
                centerX - glowSize / 2,
                centerY + 14 - glowSize / 2,
                glowSize, glowSize
            ));
        }

        // Draw main hull (angular stealth shape)
        Path2D hull = new Path2D.Double();

        // Front point (sharp nose)
        hull.moveTo(centerX, centerY - 18 * pulse);

        // Left front edge
        hull.lineTo(centerX - 8, centerY - 10);
        hull.lineTo(centerX - 14 * pulse, centerY - 2);

        // Left mid wing
        hull.lineTo(centerX - 16 * pulse, centerY + 2);
        hull.lineTo(centerX - 12, centerY + 8);

        // Left engine nacelle
        hull.lineTo(centerX - 8, centerY + 10);
        hull.lineTo(centerX - 6, centerY + 14);

        // Bottom center
        hull.lineTo(centerX, centerY + 12);

        // Right engine nacelle
        hull.lineTo(centerX + 6, centerY + 14);
        hull.lineTo(centerX + 8, centerY + 10);

        // Right mid wing
        hull.lineTo(centerX + 12, centerY + 8);
        hull.lineTo(centerX + 16 * pulse, centerY + 2);

        // Right front edge
        hull.lineTo(centerX + 14 * pulse, centerY - 2);
        hull.lineTo(centerX + 8, centerY - 10);

        hull.closePath();

        // Gradient for hull (dark stealth)
        GradientPaint hullGradient = new GradientPaint(
            (float)centerX, (float)(centerY - 18 * pulse), accentColor,
            (float)centerX, (float)(centerY + 14), darkHullColor
        );
        g2d.setPaint(hullGradient);
        g2d.fill(hull);

        // Draw angular panel lines
        g2d.setColor(edgeColor);
        g2d.setStroke(new BasicStroke(1.0f));

        // Center spine
        g2d.drawLine((int)centerX, (int)(centerY - 16 * pulse),
                    (int)centerX, (int)(centerY + 10));

        // Left panel lines
        g2d.drawLine((int)(centerX - 8), (int)(centerY - 10),
                    (int)(centerX - 4), (int)(centerY + 4));
        g2d.drawLine((int)(centerX - 14 * pulse), (int)(centerY - 2),
                    (int)(centerX - 8), (int)(centerY + 6));

        // Right panel lines
        g2d.drawLine((int)(centerX + 8), (int)(centerY - 10),
                    (int)(centerX + 4), (int)(centerY + 4));
        g2d.drawLine((int)(centerX + 14 * pulse), (int)(centerY - 2),
                    (int)(centerX + 8), (int)(centerY + 6));

        // Draw cockpit area (angular window)
        Path2D cockpit = new Path2D.Double();
        cockpit.moveTo(centerX, centerY - 12);
        cockpit.lineTo(centerX - 5, centerY - 4);
        cockpit.lineTo(centerX - 3, centerY + 2);
        cockpit.lineTo(centerX + 3, centerY + 2);
        cockpit.lineTo(centerX + 5, centerY - 4);
        cockpit.closePath();

        GradientPaint cockpitGradient = new GradientPaint(
            (float)centerX, (float)(centerY - 12), new Color(100, 200, 255),
            (float)centerX, (float)(centerY + 2), new Color(20, 80, 150)
        );
        g2d.setPaint(cockpitGradient);
        g2d.fill(cockpit);

        // Cockpit reflection
        g2d.setColor(new Color(200, 255, 255, 180));
        Path2D reflection = new Path2D.Double();
        reflection.moveTo(centerX - 1, centerY - 10);
        reflection.lineTo(centerX - 3, centerY - 6);
        reflection.lineTo(centerX - 2, centerY - 2);
        reflection.lineTo(centerX + 2, centerY - 2);
        reflection.lineTo(centerX + 3, centerY - 6);
        reflection.lineTo(centerX + 1, centerY - 10);
        reflection.closePath();
        g2d.fill(reflection);

        // Draw weapon mounts (green lights)
        g2d.setColor(weaponColor);
        g2d.fill(new Ellipse2D.Double(centerX - 14, centerY, 3, 3));
        g2d.fill(new Ellipse2D.Double(centerX + 11, centerY, 3, 3));

        // Weapon glow
        g2d.setColor(new Color(
            weaponColor.getRed(),
            weaponColor.getGreen(),
            weaponColor.getBlue(),
            (int)(120 * pulse)
        ));
        g2d.fill(new Ellipse2D.Double(centerX - 15, centerY - 1, 5, 5));
        g2d.fill(new Ellipse2D.Double(centerX + 10, centerY - 1, 5, 5));

        // Draw engines (pulsing)
        double engineSize = 6 * pulse;

        // Left engine
        GradientPaint leftEngineGradient = new GradientPaint(
            (float)(centerX - 7), (float)(centerY + 12), coreColor,
            (float)(centerX - 7), (float)(centerY + 14), engineColor
        );
        g2d.setPaint(leftEngineGradient);
        g2d.fill(new Ellipse2D.Double(
            centerX - 7 - engineSize / 2,
            centerY + 12,
            engineSize, engineSize
        ));

        // Right engine
        GradientPaint rightEngineGradient = new GradientPaint(
            (float)(centerX + 7), (float)(centerY + 12), coreColor,
            (float)(centerX + 7), (float)(centerY + 14), engineColor
        );
        g2d.setPaint(rightEngineGradient);
        g2d.fill(new Ellipse2D.Double(
            centerX + 7 - engineSize / 2,
            centerY + 12,
            engineSize, engineSize
        ));

        // Engine core (bright)
        g2d.setColor(new Color(255, 255, 255, (int)(255 * pulse)));
        g2d.fill(new Ellipse2D.Double(centerX - 8, centerY + 13, 2, 2));
        g2d.fill(new Ellipse2D.Double(centerX + 6, centerY + 13, 2, 2));

        // Draw edge highlights for angular effect
        g2d.setColor(new Color(edgeColor.getRed(), edgeColor.getGreen(), edgeColor.getBlue(), 200));
        g2d.setStroke(new BasicStroke(1.5f));

        // Top edges
        g2d.drawLine((int)centerX, (int)(centerY - 18 * pulse),
                    (int)(centerX - 8), (int)(centerY - 10));
        g2d.drawLine((int)centerX, (int)(centerY - 18 * pulse),
                    (int)(centerX + 8), (int)(centerY - 10));

        // Side angular edges
        g2d.drawLine((int)(centerX - 14 * pulse), (int)(centerY - 2),
                    (int)(centerX - 16 * pulse), (int)(centerY + 2));
        g2d.drawLine((int)(centerX + 14 * pulse), (int)(centerY - 2),
                    (int)(centerX + 16 * pulse), (int)(centerY + 2));

        // Draw navigation lights (blinking)
        if (frameIndex % 8 < 4) {
            g2d.setColor(new Color(255, 0, 0));
            g2d.fill(new Ellipse2D.Double(centerX - 15, centerY - 2, 2, 2));

            g2d.setColor(new Color(0, 255, 0));
            g2d.fill(new Ellipse2D.Double(centerX + 13, centerY - 2, 2, 2));
        }

        // Draw hull outline
        g2d.setColor(darkHullColor.darker());
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(hull);

        g2d.dispose();
        return image;
    }
}
