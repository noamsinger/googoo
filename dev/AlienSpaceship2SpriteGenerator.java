package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AlienSpaceship2SpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateAlienSpaceship2SpriteSheet();
            System.out.println("Successfully generated alien spaceship 2 sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateAlienSpaceship2SpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateAlienSpaceship2Frame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_2.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Alien spaceship 2 sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateAlienSpaceship2Frame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for alien spaceship type 2 (triangular fighter design)
        Color hullColor = new Color(220, 100, 50); // Orange hull
        Color darkHullColor = new Color(150, 50, 20); // Dark orange/brown
        Color accentColor = new Color(255, 220, 100); // Golden accent
        Color windowColor = new Color(50, 200, 255); // Cyan windows
        Color engineColor = new Color(0, 255, 200); // Cyan-green engine glow
        Color weaponColor = new Color(255, 50, 100); // Hot pink weapon ports

        // Pulsing animation for engines
        double pulse = 0.5 + Math.sin(progress * Math.PI * 2) * 0.5;

        // Slight rotation/banking animation
        double bank = Math.sin(progress * Math.PI * 2) * 3;

        // Draw engine glow trails
        for (int i = 2; i >= 0; i--) {
            g2d.setColor(new Color(
                engineColor.getRed(),
                engineColor.getGreen(),
                engineColor.getBlue(),
                (int)(80 * pulse / (i + 1))
            ));
            double glowLength = 8 + i * 4;
            double glowWidth = 6 + i * 2;

            // Left engine trail
            g2d.fill(new Ellipse2D.Double(
                centerX - 10 - glowLength / 2,
                centerY + 12 + bank - glowWidth / 2,
                glowLength, glowWidth
            ));

            // Right engine trail
            g2d.fill(new Ellipse2D.Double(
                centerX + 10 - glowLength / 2,
                centerY + 12 - bank - glowWidth / 2,
                glowLength, glowWidth
            ));
        }

        // Draw main hull (triangular/arrow shape)
        Path2D hull = new Path2D.Double();

        // Nose (front)
        hull.moveTo(centerX, centerY - 18);

        // Left side
        hull.lineTo(centerX - 12, centerY + 6 + bank);
        hull.lineTo(centerX - 8, centerY + 12 + bank);

        // Left engine nacelle
        hull.lineTo(centerX - 10, centerY + 12 + bank);
        hull.lineTo(centerX - 10, centerY + 16 + bank);
        hull.lineTo(centerX - 6, centerY + 16 + bank);
        hull.lineTo(centerX - 6, centerY + 12 + bank);

        // Bottom center
        hull.lineTo(centerX, centerY + 10);

        // Right engine nacelle
        hull.lineTo(centerX + 6, centerY + 12 - bank);
        hull.lineTo(centerX + 6, centerY + 16 - bank);
        hull.lineTo(centerX + 10, centerY + 16 - bank);
        hull.lineTo(centerX + 10, centerY + 12 - bank);

        // Right side
        hull.lineTo(centerX + 8, centerY + 12 - bank);
        hull.lineTo(centerX + 12, centerY + 6 - bank);

        hull.closePath();

        // Gradient for hull
        GradientPaint hullGradient = new GradientPaint(
            (float)centerX, (float)(centerY - 18), accentColor,
            (float)centerX, (float)(centerY + 16), darkHullColor
        );
        g2d.setPaint(hullGradient);
        g2d.fill(hull);

        // Draw cockpit/bridge area
        Path2D cockpit = new Path2D.Double();
        cockpit.moveTo(centerX, centerY - 12);
        cockpit.lineTo(centerX - 6, centerY - 2);
        cockpit.lineTo(centerX - 4, centerY + 2);
        cockpit.lineTo(centerX + 4, centerY + 2);
        cockpit.lineTo(centerX + 6, centerY - 2);
        cockpit.closePath();

        g2d.setColor(windowColor);
        g2d.fill(cockpit);

        // Cockpit reflection
        g2d.setColor(new Color(200, 255, 255, 150));
        Path2D reflection = new Path2D.Double();
        reflection.moveTo(centerX, centerY - 12);
        reflection.lineTo(centerX - 3, centerY - 5);
        reflection.lineTo(centerX - 2, centerY - 2);
        reflection.lineTo(centerX + 2, centerY - 2);
        reflection.lineTo(centerX + 3, centerY - 5);
        reflection.closePath();
        g2d.fill(reflection);

        // Draw wing details
        g2d.setColor(accentColor);
        g2d.setStroke(new BasicStroke(1.5f));

        // Left wing lines
        g2d.drawLine((int)(centerX - 8), (int)(centerY - 8),
                    (int)(centerX - 10), (int)(centerY + 4 + bank));
        g2d.drawLine((int)(centerX - 5), (int)(centerY - 4),
                    (int)(centerX - 7), (int)(centerY + 6 + bank));

        // Right wing lines
        g2d.drawLine((int)(centerX + 8), (int)(centerY - 8),
                    (int)(centerX + 10), (int)(centerY + 4 - bank));
        g2d.drawLine((int)(centerX + 5), (int)(centerY - 4),
                    (int)(centerX + 7), (int)(centerY + 6 - bank));

        // Draw weapon ports
        g2d.setColor(weaponColor);
        g2d.fill(new Ellipse2D.Double(centerX - 10, centerY + 2 + bank, 3, 3));
        g2d.fill(new Ellipse2D.Double(centerX + 7, centerY + 2 - bank, 3, 3));

        // Weapon port glow
        g2d.setColor(new Color(
            weaponColor.getRed(),
            weaponColor.getGreen(),
            weaponColor.getBlue(),
            (int)(150 * (1 - pulse))
        ));
        g2d.fill(new Ellipse2D.Double(centerX - 11, centerY + 1 + bank, 5, 5));
        g2d.fill(new Ellipse2D.Double(centerX + 6, centerY + 1 - bank, 5, 5));

        // Draw engines (pulsing)
        g2d.setColor(engineColor);
        // Left engine
        g2d.fill(new Ellipse2D.Double(centerX - 10, centerY + 14 + bank, 4, 3));
        // Right engine
        g2d.fill(new Ellipse2D.Double(centerX + 6, centerY + 14 - bank, 4, 3));

        // Engine core (brighter)
        g2d.setColor(new Color(
            255, 255, 255,
            (int)(255 * pulse)
        ));
        g2d.fill(new Ellipse2D.Double(centerX - 9, centerY + 14.5 + bank, 2, 2));
        g2d.fill(new Ellipse2D.Double(centerX + 7, centerY + 14.5 - bank, 2, 2));

        // Draw center line detail
        g2d.setColor(accentColor);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawLine((int)centerX, (int)(centerY - 16),
                    (int)centerX, (int)(centerY + 8));

        // Draw hull outline
        g2d.setColor(darkHullColor.darker());
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(hull);

        // Draw cockpit outline
        g2d.setColor(windowColor.darker());
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.draw(cockpit);

        // Draw small navigation lights (blinking)
        if (frameIndex % 4 < 2) {
            g2d.setColor(new Color(255, 0, 0));
            g2d.fill(new Ellipse2D.Double(centerX - 11, centerY + bank, 1.5, 1.5));

            g2d.setColor(new Color(0, 255, 0));
            g2d.fill(new Ellipse2D.Double(centerX + 9.5, centerY - bank, 1.5, 1.5));
        }

        g2d.dispose();
        return image;
    }
}
