package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AlienSpaceshipSpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateAlienSpaceshipSpriteSheet();
            System.out.println("Successfully generated alien spaceship sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateAlienSpaceshipSpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateAlienSpaceshipFrame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_0.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Alien spaceship sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateAlienSpaceshipFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for alien spaceship (classic UFO style)
        Color hullColor = new Color(180, 200, 220); // Light metallic silver
        Color darkHullColor = new Color(100, 120, 140); // Dark metallic
        Color domeColor = new Color(100, 255, 200); // Bright cyan-green dome
        Color darkDomeColor = new Color(50, 180, 120); // Dark cyan-green
        Color glowColor = new Color(255, 100, 255); // Magenta glow
        Color accentColor = new Color(255, 200, 50); // Golden accent

        // Pulsing animation for lights
        double pulse = 0.5 + Math.sin(progress * Math.PI * 2) * 0.5;

        // Slight wobble/tilt animation
        double wobble = Math.sin(progress * Math.PI * 2) * 2;

        // Draw bottom glow (propulsion system)
        for (int i = 3; i > 0; i--) {
            g2d.setColor(new Color(
                glowColor.getRed(),
                glowColor.getGreen(),
                glowColor.getBlue(),
                (int)(50 * pulse / i)
            ));
            double glowSize = 25 + i * 5;
            g2d.fill(new Ellipse2D.Double(
                centerX - glowSize / 2,
                centerY + 8 - glowSize / 2,
                glowSize, glowSize
            ));
        }

        // Draw main hull (disc shape)
        Path2D hull = new Path2D.Double();
        double hullWidth = 30;
        double hullHeight = 10;

        hull.moveTo(centerX, centerY - hullHeight / 2 + wobble);
        // Top edge (curved)
        hull.curveTo(
            centerX - hullWidth / 2, centerY - hullHeight / 2 + wobble,
            centerX - hullWidth / 2, centerY + hullHeight / 2 + wobble,
            centerX, centerY + hullHeight / 2 + wobble
        );
        hull.curveTo(
            centerX + hullWidth / 2, centerY + hullHeight / 2 + wobble,
            centerX + hullWidth / 2, centerY - hullHeight / 2 + wobble,
            centerX, centerY - hullHeight / 2 + wobble
        );
        hull.closePath();

        // Gradient for hull
        GradientPaint hullGradient = new GradientPaint(
            (float)centerX, (float)(centerY - hullHeight / 2 + wobble), hullColor,
            (float)centerX, (float)(centerY + hullHeight / 2 + wobble), darkHullColor
        );
        g2d.setPaint(hullGradient);
        g2d.fill(hull);

        // Draw hull rim (darker edge)
        g2d.setColor(darkHullColor.darker());
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(hull);

        // Draw circular lights around the rim
        int numLights = 8;
        for (int i = 0; i < numLights; i++) {
            double angle = (i / (double) numLights) * Math.PI * 2;
            double lightX = centerX + Math.cos(angle) * (hullWidth / 2 - 3);
            double lightY = centerY + Math.sin(angle) * (hullHeight / 2) + wobble;

            // Alternating light colors with pulse
            Color lightColor;
            if (i % 2 == 0) {
                lightColor = new Color(
                    glowColor.getRed(),
                    glowColor.getGreen(),
                    glowColor.getBlue(),
                    (int)(200 * pulse)
                );
            } else {
                lightColor = new Color(
                    accentColor.getRed(),
                    accentColor.getGreen(),
                    accentColor.getBlue(),
                    (int)(200 * (1 - pulse))
                );
            }

            g2d.setColor(lightColor);
            g2d.fill(new Ellipse2D.Double(lightX - 2, lightY - 1, 4, 2));
        }

        // Draw dome (cockpit)
        double domeWidth = 16;
        double domeHeight = 12;

        Path2D dome = new Path2D.Double();
        dome.moveTo(centerX - domeWidth / 2, centerY - hullHeight / 2 + wobble);
        dome.curveTo(
            centerX - domeWidth / 2, centerY - hullHeight / 2 - domeHeight + wobble,
            centerX + domeWidth / 2, centerY - hullHeight / 2 - domeHeight + wobble,
            centerX + domeWidth / 2, centerY - hullHeight / 2 + wobble
        );
        dome.closePath();

        // Gradient for dome
        GradientPaint domeGradient = new GradientPaint(
            (float)centerX, (float)(centerY - hullHeight / 2 - domeHeight + wobble), domeColor,
            (float)centerX, (float)(centerY - hullHeight / 2 + wobble), darkDomeColor
        );
        g2d.setPaint(domeGradient);
        g2d.fill(dome);

        // Draw dome highlights (glass reflection)
        g2d.setColor(new Color(255, 255, 255, 150));
        Path2D domeHighlight = new Path2D.Double();
        domeHighlight.moveTo(centerX - domeWidth / 4, centerY - hullHeight / 2 + wobble);
        domeHighlight.curveTo(
            centerX - domeWidth / 4, centerY - hullHeight / 2 - domeHeight * 0.7 + wobble,
            centerX + domeWidth / 4, centerY - hullHeight / 2 - domeHeight * 0.7 + wobble,
            centerX + domeWidth / 4, centerY - hullHeight / 2 + wobble
        );
        domeHighlight.closePath();
        g2d.fill(domeHighlight);

        // Draw dome outline
        g2d.setColor(darkDomeColor.darker());
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(dome);

        // Draw antenna on top
        g2d.setColor(accentColor);
        g2d.setStroke(new BasicStroke(1.5f));
        double antennaTop = centerY - hullHeight / 2 - domeHeight + wobble;
        g2d.drawLine(
            (int)centerX, (int)antennaTop,
            (int)centerX, (int)(antennaTop - 4)
        );

        // Antenna tip (pulsing)
        g2d.setColor(new Color(
            glowColor.getRed(),
            glowColor.getGreen(),
            glowColor.getBlue(),
            (int)(255 * pulse)
        ));
        g2d.fill(new Ellipse2D.Double(centerX - 1.5, antennaTop - 6, 3, 3));

        // Draw panel lines on hull
        g2d.setColor(new Color(150, 170, 190, 100));
        g2d.setStroke(new BasicStroke(0.5f));
        for (int i = -2; i <= 2; i++) {
            double panelX = centerX + i * 6;
            g2d.drawLine(
                (int)panelX, (int)(centerY - hullHeight / 2 + wobble + 2),
                (int)panelX, (int)(centerY + hullHeight / 2 + wobble - 2)
            );
        }

        // Draw central power core (pulsing)
        g2d.setColor(new Color(
            glowColor.getRed(),
            glowColor.getGreen(),
            glowColor.getBlue(),
            (int)(150 * pulse)
        ));
        g2d.fill(new Ellipse2D.Double(centerX - 3, centerY + wobble - 2, 6, 4));

        g2d.dispose();
        return image;
    }
}
