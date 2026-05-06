package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MissileSpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateMissileSpriteSheet();
            System.out.println("Successfully generated missile sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateMissileSpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateMissileFrame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_12.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Missile sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateMissileFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for missile
        Color bodyColor = new Color(180, 180, 200); // Light gray body
        Color noseColor = new Color(220, 50, 50); // Red nose cone
        Color finColor = new Color(100, 100, 120); // Dark gray fins
        Color stripeColor = new Color(200, 180, 50); // Yellow warning stripe
        Color exhaustColor = new Color(255, 150, 50); // Orange exhaust

        // Slight rotation animation
        double wobble = Math.sin(progress * Math.PI * 4) * 0.05;

        // Draw exhaust flame (at rear)
        double exhaustLength = 10 + Math.sin(progress * Math.PI * 8) * 5;
        Path2D exhaust = new Path2D.Double();
        exhaust.moveTo(centerX - 3, centerY + 15);
        exhaust.lineTo(centerX, centerY + 15 + exhaustLength);
        exhaust.lineTo(centerX + 3, centerY + 15);
        exhaust.closePath();

        // Gradient for exhaust
        GradientPaint exhaustGradient = new GradientPaint(
            (float)centerX, (float)(centerY + 15), new Color(255, 255, 150),
            (float)centerX, (float)(centerY + 15 + exhaustLength), exhaustColor
        );
        g2d.setPaint(exhaustGradient);
        g2d.fill(exhaust);

        // Draw exhaust glow
        g2d.setColor(new Color(255, 200, 100, 100));
        g2d.fill(new Ellipse2D.Double(centerX - 6, centerY + 15, 12, exhaustLength));

        // Draw main body (cylindrical)
        double bodyWidth = 8;
        double bodyLength = 28;

        g2d.setColor(bodyColor);
        g2d.fillRect((int)(centerX - bodyWidth / 2), (int)(centerY - bodyLength / 2),
                    (int)bodyWidth, (int)bodyLength);

        // Body shine/highlight
        g2d.setColor(new Color(220, 220, 240));
        g2d.fillRect((int)(centerX - bodyWidth / 2 + 1), (int)(centerY - bodyLength / 2),
                    2, (int)bodyLength);

        // Warning stripes
        g2d.setColor(stripeColor);
        for (int i = 0; i < 3; i++) {
            double stripeY = centerY - 5 + i * 5;
            g2d.fillRect((int)(centerX - bodyWidth / 2), (int)stripeY, (int)bodyWidth, 2);
        }

        // Draw nose cone (pointed)
        Path2D nose = new Path2D.Double();
        nose.moveTo(centerX - bodyWidth / 2, centerY - bodyLength / 2);
        nose.lineTo(centerX, centerY - bodyLength / 2 - 8);
        nose.lineTo(centerX + bodyWidth / 2, centerY - bodyLength / 2);
        nose.closePath();

        g2d.setColor(noseColor);
        g2d.fill(nose);

        // Nose highlight
        g2d.setColor(new Color(255, 150, 150));
        Path2D noseHighlight = new Path2D.Double();
        noseHighlight.moveTo(centerX - 2, centerY - bodyLength / 2);
        noseHighlight.lineTo(centerX, centerY - bodyLength / 2 - 8);
        noseHighlight.lineTo(centerX + 2, centerY - bodyLength / 2);
        noseHighlight.closePath();
        g2d.fill(noseHighlight);

        // Draw fins (4 fins in cross pattern)
        // Top fin
        Path2D topFin = new Path2D.Double();
        topFin.moveTo(centerX - 2, centerY + 8);
        topFin.lineTo(centerX, centerY + 2);
        topFin.lineTo(centerX + 2, centerY + 8);
        topFin.closePath();

        // Bottom fin
        Path2D bottomFin = new Path2D.Double();
        bottomFin.moveTo(centerX - 2, centerY + 8);
        bottomFin.lineTo(centerX, centerY + 14);
        bottomFin.lineTo(centerX + 2, centerY + 8);
        bottomFin.closePath();

        // Left fin
        Path2D leftFin = new Path2D.Double();
        leftFin.moveTo(centerX - bodyWidth / 2, centerY + 8);
        leftFin.lineTo(centerX - bodyWidth / 2 - 6, centerY + 8);
        leftFin.lineTo(centerX - bodyWidth / 2, centerY + 12);
        leftFin.closePath();

        // Right fin
        Path2D rightFin = new Path2D.Double();
        rightFin.moveTo(centerX + bodyWidth / 2, centerY + 8);
        rightFin.lineTo(centerX + bodyWidth / 2 + 6, centerY + 8);
        rightFin.lineTo(centerX + bodyWidth / 2, centerY + 12);
        rightFin.closePath();

        g2d.setColor(finColor);
        g2d.fill(topFin);
        g2d.fill(bottomFin);
        g2d.fill(leftFin);
        g2d.fill(rightFin);

        // Fin outlines
        g2d.setColor(finColor.darker());
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.draw(topFin);
        g2d.draw(bottomFin);
        g2d.draw(leftFin);
        g2d.draw(rightFin);

        // Body outline
        g2d.setColor(bodyColor.darker());
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRect((int)(centerX - bodyWidth / 2), (int)(centerY - bodyLength / 2),
                    (int)bodyWidth, (int)bodyLength);

        // Nose outline
        g2d.draw(nose);

        // Add rivets/details
        g2d.setColor(finColor);
        for (int i = 0; i < 4; i++) {
            double rivetY = centerY - 8 + i * 6;
            g2d.fill(new Ellipse2D.Double(centerX - bodyWidth / 2 - 1, rivetY - 0.5, 1, 1));
            g2d.fill(new Ellipse2D.Double(centerX + bodyWidth / 2, rivetY - 0.5, 1, 1));
        }

        g2d.dispose();
        return image;
    }
}
