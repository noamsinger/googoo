package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AlienSpaceship6SpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateAlienSpaceship6SpriteSheet();
            System.out.println("Successfully generated alien spaceship 6 sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateAlienSpaceship6SpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateAlienSpaceship6Frame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_6.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Alien spaceship 6 sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateAlienSpaceship6Frame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for alien spaceship type 6 (crystalline/geometric design)
        Color crystalColor = new Color(100, 255, 255); // Cyan crystal
        Color darkCrystalColor = new Color(50, 150, 180); // Dark cyan
        Color coreColor = new Color(255, 100, 255); // Magenta core
        Color energyColor = new Color(255, 255, 150); // Light yellow energy
        Color accentColor = new Color(150, 200, 255); // Light blue accent
        Color gemColor = new Color(255, 50, 150); // Hot pink gems

        // Pulsing animation
        double pulse = 0.8 + Math.sin(progress * Math.PI * 2) * 0.2;

        // Rotation animation
        double rotation = progress * Math.PI * 2;

        // Draw energy field
        for (int i = 2; i >= 0; i--) {
            g2d.setColor(new Color(
                energyColor.getRed(),
                energyColor.getGreen(),
                energyColor.getBlue(),
                (int)(60 * pulse / (i + 1))
            ));
            double fieldSize = 30 + i * 6;
            g2d.fill(new Ellipse2D.Double(
                centerX - fieldSize / 2,
                centerY - fieldSize / 2,
                fieldSize, fieldSize
            ));
        }

        // Draw main crystalline body (diamond/hexagonal shape)
        Path2D mainBody = new Path2D.Double();

        // Create hexagonal shape with rotation
        int sides = 6;
        double bodyRadius = 16 * pulse;

        for (int i = 0; i <= sides; i++) {
            double angle = (i / (double) sides) * Math.PI * 2 + rotation;
            double x = centerX + Math.cos(angle) * bodyRadius;
            double y = centerY + Math.sin(angle) * bodyRadius;

            if (i == 0) {
                mainBody.moveTo(x, y);
            } else {
                mainBody.lineTo(x, y);
            }
        }

        // Gradient for main body
        GradientPaint bodyGradient = new GradientPaint(
            (float)(centerX - bodyRadius), (float)(centerY - bodyRadius), accentColor,
            (float)(centerX + bodyRadius), (float)(centerY + bodyRadius), darkCrystalColor
        );
        g2d.setPaint(bodyGradient);
        g2d.fill(mainBody);

        // Draw geometric facets (inner triangles)
        g2d.setColor(crystalColor);
        for (int i = 0; i < sides; i++) {
            double angle1 = (i / (double) sides) * Math.PI * 2 + rotation;
            double angle2 = ((i + 1) / (double) sides) * Math.PI * 2 + rotation;

            double x1 = centerX + Math.cos(angle1) * bodyRadius;
            double y1 = centerY + Math.sin(angle1) * bodyRadius;
            double x2 = centerX + Math.cos(angle2) * bodyRadius;
            double y2 = centerY + Math.sin(angle2) * bodyRadius;

            Path2D facet = new Path2D.Double();
            facet.moveTo(centerX, centerY);
            facet.lineTo(x1, y1);
            facet.lineTo(x2, y2);
            facet.closePath();

            if (i % 2 == 0) {
                g2d.setColor(new Color(
                    crystalColor.getRed(),
                    crystalColor.getGreen(),
                    crystalColor.getBlue(),
                    100
                ));
                g2d.fill(facet);
            }
        }

        // Draw central core (pulsing energy sphere)
        double coreSize = 8 * pulse;
        GradientPaint coreGradient = new GradientPaint(
            (float)(centerX - coreSize / 2), (float)(centerY - coreSize / 2), new Color(255, 255, 255),
            (float)(centerX + coreSize / 2), (float)(centerY + coreSize / 2), coreColor
        );
        g2d.setPaint(coreGradient);
        g2d.fill(new Ellipse2D.Double(
            centerX - coreSize / 2,
            centerY - coreSize / 2,
            coreSize, coreSize
        ));

        // Draw energy spikes at vertices
        g2d.setColor(new Color(
            energyColor.getRed(),
            energyColor.getGreen(),
            energyColor.getBlue(),
            (int)(200 * pulse)
        ));

        for (int i = 0; i < sides; i++) {
            double angle = (i / (double) sides) * Math.PI * 2 + rotation;
            double baseX = centerX + Math.cos(angle) * bodyRadius;
            double baseY = centerY + Math.sin(angle) * bodyRadius;
            double tipX = centerX + Math.cos(angle) * (bodyRadius + 6);
            double tipY = centerY + Math.sin(angle) * (bodyRadius + 6);

            Path2D spike = new Path2D.Double();
            double spikeWidth = 2;
            double perpAngle = angle + Math.PI / 2;

            spike.moveTo(
                baseX + Math.cos(perpAngle) * spikeWidth,
                baseY + Math.sin(perpAngle) * spikeWidth
            );
            spike.lineTo(tipX, tipY);
            spike.lineTo(
                baseX - Math.cos(perpAngle) * spikeWidth,
                baseY - Math.sin(perpAngle) * spikeWidth
            );
            spike.closePath();

            g2d.fill(spike);
        }

        // Draw crystal gems at alternating vertices
        g2d.setColor(gemColor);
        for (int i = 0; i < sides; i += 2) {
            double angle = (i / (double) sides) * Math.PI * 2 + rotation;
            double gemX = centerX + Math.cos(angle) * bodyRadius * 0.7;
            double gemY = centerY + Math.sin(angle) * bodyRadius * 0.7;
            double gemSize = 3 * pulse;

            g2d.fill(new Ellipse2D.Double(
                gemX - gemSize / 2,
                gemY - gemSize / 2,
                gemSize, gemSize
            ));

            // Gem glow
            g2d.setColor(new Color(
                gemColor.getRed(),
                gemColor.getGreen(),
                gemColor.getBlue(),
                (int)(150 * pulse)
            ));
            g2d.fill(new Ellipse2D.Double(
                gemX - gemSize,
                gemY - gemSize,
                gemSize * 2, gemSize * 2
            ));
            g2d.setColor(gemColor);
        }

        // Draw edge highlights for crystal effect
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.setStroke(new BasicStroke(1.5f));

        for (int i = 0; i < sides; i++) {
            double angle = (i / (double) sides) * Math.PI * 2 + rotation;
            double x = centerX + Math.cos(angle) * bodyRadius;
            double y = centerY + Math.sin(angle) * bodyRadius;

            if (i % 2 == 0) {
                g2d.drawLine((int)centerX, (int)centerY, (int)x, (int)y);
            }
        }

        // Draw outer glow rings (animated)
        g2d.setStroke(new BasicStroke(1.0f));
        for (int ring = 0; ring < 2; ring++) {
            double ringProgress = (progress + ring * 0.5) % 1.0;
            double ringSize = bodyRadius + ringProgress * 10;
            int ringAlpha = (int)((1.0 - ringProgress) * 150);

            g2d.setColor(new Color(
                crystalColor.getRed(),
                crystalColor.getGreen(),
                crystalColor.getBlue(),
                ringAlpha
            ));

            g2d.draw(new Ellipse2D.Double(
                centerX - ringSize,
                centerY - ringSize,
                ringSize * 2, ringSize * 2
            ));
        }

        // Draw main body outline
        g2d.setColor(darkCrystalColor.darker());
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(mainBody);

        g2d.dispose();
        return image;
    }
}
