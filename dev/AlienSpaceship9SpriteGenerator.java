package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AlienSpaceship9SpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateAlienSpaceship9SpriteSheet();
            System.out.println("Successfully generated alien spaceship 9 sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateAlienSpaceship9SpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateAlienSpaceship9Frame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_9.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Alien spaceship 9 sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateAlienSpaceship9Frame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for alien spaceship type 9 (crescent/scythe design)
        Color hullColor = new Color(200, 50, 100); // Deep magenta hull
        Color darkHullColor = new Color(120, 20, 60); // Dark magenta
        Color accentColor = new Color(255, 100, 200); // Bright pink accent
        Color edgeColor = new Color(255, 150, 220); // Light pink edge
        Color coreColor = new Color(100, 255, 255); // Cyan core
        Color engineColor = new Color(0, 200, 255); // Bright cyan engine
        Color glowColor = new Color(150, 255, 255); // Cyan glow
        Color weaponColor = new Color(255, 255, 100); // Yellow weapons

        // Pulsing animation
        double pulse = 0.85 + Math.sin(progress * Math.PI * 2) * 0.15;

        // Slight oscillation
        double oscillation = Math.sin(progress * Math.PI * 2) * 2;

        // Draw engine glow trails
        for (int i = 2; i >= 0; i--) {
            g2d.setColor(new Color(
                engineColor.getRed(),
                engineColor.getGreen(),
                engineColor.getBlue(),
                (int)(60 * pulse / (i + 1))
            ));
            double glowSize = 15 + i * 5;

            // Left engine trail
            g2d.fill(new Ellipse2D.Double(
                centerX - 10 - glowSize / 2,
                centerY + 10 + oscillation - glowSize / 2,
                glowSize, glowSize
            ));

            // Right engine trail
            g2d.fill(new Ellipse2D.Double(
                centerX + 10 - glowSize / 2,
                centerY + 10 - oscillation - glowSize / 2,
                glowSize, glowSize
            ));
        }

        // Draw main hull (crescent/scythe shape)
        Path2D hull = new Path2D.Double();

        // Sharp front blade
        hull.moveTo(centerX, centerY - 18 * pulse);

        // Left outer curve
        hull.curveTo(
            centerX - 8, centerY - 14,
            centerX - 16 * pulse, centerY - 6,
            centerX - 18 * pulse, centerY
        );

        hull.curveTo(
            centerX - 16 * pulse, centerY + 4,
            centerX - 12, centerY + 8,
            centerX - 8, centerY + 10
        );

        // Left engine pod
        hull.lineTo(centerX - 6, centerY + 12);
        hull.lineTo(centerX - 10, centerY + 14 + oscillation);
        hull.lineTo(centerX - 8, centerY + 16 + oscillation);
        hull.lineTo(centerX - 4, centerY + 14);

        // Center indent
        hull.curveTo(
            centerX - 2, centerY + 12,
            centerX + 2, centerY + 12,
            centerX + 4, centerY + 14
        );

        // Right engine pod
        hull.lineTo(centerX + 8, centerY + 16 - oscillation);
        hull.lineTo(centerX + 10, centerY + 14 - oscillation);
        hull.lineTo(centerX + 6, centerY + 12);

        // Right outer curve
        hull.lineTo(centerX + 8, centerY + 10);
        hull.curveTo(
            centerX + 12, centerY + 8,
            centerX + 16 * pulse, centerY + 4,
            centerX + 18 * pulse, centerY
        );

        hull.curveTo(
            centerX + 16 * pulse, centerY - 6,
            centerX + 8, centerY - 14,
            centerX, centerY - 18 * pulse
        );

        hull.closePath();

        // Gradient for hull
        GradientPaint hullGradient = new GradientPaint(
            (float)centerX, (float)(centerY - 18 * pulse), accentColor,
            (float)centerX, (float)(centerY + 16), darkHullColor
        );
        g2d.setPaint(hullGradient);
        g2d.fill(hull);

        // Draw inner blade design
        Path2D innerBlade = new Path2D.Double();
        innerBlade.moveTo(centerX, centerY - 14 * pulse);
        innerBlade.curveTo(
            centerX - 6, centerY - 10,
            centerX - 12, centerY - 4,
            centerX - 13, centerY + 2
        );
        innerBlade.curveTo(
            centerX - 10, centerY + 4,
            centerX - 6, centerY + 6,
            centerX, centerY + 8
        );
        innerBlade.curveTo(
            centerX + 6, centerY + 6,
            centerX + 10, centerY + 4,
            centerX + 13, centerY + 2
        );
        innerBlade.curveTo(
            centerX + 12, centerY - 4,
            centerX + 6, centerY - 10,
            centerX, centerY - 14 * pulse
        );
        innerBlade.closePath();

        g2d.setColor(new Color(hullColor.getRed(), hullColor.getGreen(), hullColor.getBlue(), 180));
        g2d.fill(innerBlade);

        // Draw central core
        double coreSize = 8 * pulse;
        GradientPaint coreGradient = new GradientPaint(
            (float)centerX, (float)(centerY - coreSize / 2), glowColor,
            (float)centerX, (float)(centerY + coreSize / 2), coreColor
        );
        g2d.setPaint(coreGradient);
        g2d.fill(new Ellipse2D.Double(
            centerX - coreSize / 2,
            centerY - coreSize / 2,
            coreSize, coreSize
        ));

        // Core glow
        g2d.setColor(new Color(
            glowColor.getRed(),
            glowColor.getGreen(),
            glowColor.getBlue(),
            (int)(150 * pulse)
        ));
        g2d.fill(new Ellipse2D.Double(
            centerX - coreSize,
            centerY - coreSize,
            coreSize * 2, coreSize * 2
        ));

        // Draw blade edge highlights
        g2d.setColor(edgeColor);
        g2d.setStroke(new BasicStroke(2.0f));

        // Left blade edge
        Path2D leftEdge = new Path2D.Double();
        leftEdge.moveTo(centerX, centerY - 18 * pulse);
        leftEdge.curveTo(
            centerX - 8, centerY - 14,
            centerX - 16 * pulse, centerY - 6,
            centerX - 18 * pulse, centerY
        );
        g2d.draw(leftEdge);

        // Right blade edge
        Path2D rightEdge = new Path2D.Double();
        rightEdge.moveTo(centerX, centerY - 18 * pulse);
        rightEdge.curveTo(
            centerX + 8, centerY - 14,
            centerX + 16 * pulse, centerY - 6,
            centerX + 18 * pulse, centerY
        );
        g2d.draw(rightEdge);

        // Draw weapon emitters on blade tips
        g2d.setColor(weaponColor);

        // Left tip
        g2d.fill(new Ellipse2D.Double(centerX - 17 * pulse, centerY - 1, 3, 3));
        g2d.setColor(new Color(weaponColor.getRed(), weaponColor.getGreen(), weaponColor.getBlue(), 150));
        g2d.fill(new Ellipse2D.Double(centerX - 18 * pulse, centerY - 2, 5, 5));

        // Right tip
        g2d.setColor(weaponColor);
        g2d.fill(new Ellipse2D.Double(centerX + 14 * pulse, centerY - 1, 3, 3));
        g2d.setColor(new Color(weaponColor.getRed(), weaponColor.getGreen(), weaponColor.getBlue(), 150));
        g2d.fill(new Ellipse2D.Double(centerX + 13 * pulse, centerY - 2, 5, 5));

        // Draw engines (pulsing)
        double engineSize = 5 * pulse;

        // Left engine
        GradientPaint leftEngineGradient = new GradientPaint(
            (float)(centerX - 9), (float)(centerY + 14 + oscillation), new Color(255, 255, 255),
            (float)(centerX - 9), (float)(centerY + 16 + oscillation), engineColor
        );
        g2d.setPaint(leftEngineGradient);
        g2d.fill(new Ellipse2D.Double(
            centerX - 9 - engineSize / 2,
            centerY + 14 + oscillation,
            engineSize, engineSize
        ));

        // Right engine
        GradientPaint rightEngineGradient = new GradientPaint(
            (float)(centerX + 9), (float)(centerY + 14 - oscillation), new Color(255, 255, 255),
            (float)(centerX + 9), (float)(centerY + 16 - oscillation), engineColor
        );
        g2d.setPaint(rightEngineGradient);
        g2d.fill(new Ellipse2D.Double(
            centerX + 9 - engineSize / 2,
            centerY + 14 - oscillation,
            engineSize, engineSize
        ));

        // Engine cores (bright)
        g2d.setColor(new Color(255, 255, 255, (int)(255 * pulse)));
        g2d.fill(new Ellipse2D.Double(centerX - 10, centerY + 14.5 + oscillation, 2, 2));
        g2d.fill(new Ellipse2D.Double(centerX + 8, centerY + 14.5 - oscillation, 2, 2));

        // Draw energy lines on hull
        g2d.setColor(new Color(coreColor.getRed(), coreColor.getGreen(), coreColor.getBlue(), 180));
        g2d.setStroke(new BasicStroke(1.5f));

        // Left energy line
        Path2D leftEnergy = new Path2D.Double();
        leftEnergy.moveTo(centerX - 2, centerY - 8);
        leftEnergy.curveTo(
            centerX - 6, centerY - 4,
            centerX - 10, centerY + 2,
            centerX - 8, centerY + 8
        );
        g2d.draw(leftEnergy);

        // Right energy line
        Path2D rightEnergy = new Path2D.Double();
        rightEnergy.moveTo(centerX + 2, centerY - 8);
        rightEnergy.curveTo(
            centerX + 6, centerY - 4,
            centerX + 10, centerY + 2,
            centerX + 8, centerY + 8
        );
        g2d.draw(rightEnergy);

        // Draw hull outline
        g2d.setColor(darkHullColor.darker());
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(hull);

        // Draw pulsing energy ring
        double ringSize = 12 + pulse * 4;
        int ringAlpha = (int)(100 * pulse);
        g2d.setColor(new Color(
            coreColor.getRed(),
            coreColor.getGreen(),
            coreColor.getBlue(),
            ringAlpha
        ));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.draw(new Ellipse2D.Double(
            centerX - ringSize,
            centerY - ringSize,
            ringSize * 2, ringSize * 2
        ));

        g2d.dispose();
        return image;
    }
}
