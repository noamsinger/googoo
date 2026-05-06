package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CockroachSpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateCockroachSpriteSheet();
            System.out.println("Successfully generated cockroach sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateCockroachSpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateCockroachFrame(frame);

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
        System.out.println("Cockroach sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateCockroachFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for cockroach (from above)
        Color bodyColor = new Color(120, 70, 40); // Brown body
        Color shellColor = new Color(160, 90, 50); // Lighter brown shell
        Color legColor = new Color(80, 50, 30); // Dark brown legs
        Color antennaColor = new Color(100, 60, 35); // Medium brown antennae
        Color highlightColor = new Color(200, 130, 80); // Tan highlights

        // Leg animation - alternating movement
        double legWave = Math.sin(progress * Math.PI * 2);

        // Draw 6 legs (3 on each side, visible from above)
        g2d.setColor(legColor);
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (int side = 0; side < 2; side++) {
            int direction = (side == 0) ? -1 : 1;

            // Front legs
            double frontPhase = legWave;
            double frontLegX = centerX + direction * 6;
            double frontLegY = centerY - 5;
            double frontAngle = direction * (Math.PI / 2.5 + frontPhase * 0.2);
            g2d.drawLine((int)frontLegX, (int)frontLegY,
                        (int)(frontLegX + Math.cos(frontAngle) * 10),
                        (int)(frontLegY + Math.sin(frontAngle) * 10));
            g2d.drawLine((int)(frontLegX + Math.cos(frontAngle) * 10),
                        (int)(frontLegY + Math.sin(frontAngle) * 10),
                        (int)(frontLegX + Math.cos(frontAngle + direction * 0.5) * 18),
                        (int)(frontLegY + Math.sin(frontAngle + direction * 0.5) * 18));

            // Middle legs
            double midPhase = -legWave;
            double midLegX = centerX + direction * 8;
            double midLegY = centerY + 2;
            double midAngle = direction * (Math.PI / 2 + midPhase * 0.2);
            g2d.drawLine((int)midLegX, (int)midLegY,
                        (int)(midLegX + Math.cos(midAngle) * 12),
                        (int)(midLegY + Math.sin(midAngle) * 12));
            g2d.drawLine((int)(midLegX + Math.cos(midAngle) * 12),
                        (int)(midLegY + Math.sin(midAngle) * 12),
                        (int)(midLegX + Math.cos(midAngle + direction * 0.5) * 20),
                        (int)(midLegY + Math.sin(midAngle + direction * 0.5) * 20));

            // Back legs
            double backPhase = legWave;
            double backLegX = centerX + direction * 6;
            double backLegY = centerY + 8;
            double backAngle = direction * (Math.PI / 2.2 + backPhase * 0.2);
            g2d.drawLine((int)backLegX, (int)backLegY,
                        (int)(backLegX + Math.cos(backAngle) * 10),
                        (int)(backLegY + Math.sin(backAngle) * 10));
            g2d.drawLine((int)(backLegX + Math.cos(backAngle) * 10),
                        (int)(backLegY + Math.sin(backAngle) * 10),
                        (int)(backLegX + Math.cos(backAngle + direction * 0.5) * 18),
                        (int)(backLegY + Math.sin(backAngle + direction * 0.5) * 18));
        }

        // Draw main body (oval shell from above)
        double bodyWidth = 18;
        double bodyHeight = 28;

        g2d.setColor(shellColor);
        g2d.fill(new Ellipse2D.Double(centerX - bodyWidth / 2, centerY - bodyHeight / 2 + 2,
                                       bodyWidth, bodyHeight));

        // Shell segments
        g2d.setColor(bodyColor);
        g2d.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < 5; i++) {
            double segY = centerY - 10 + i * 5;
            g2d.drawArc((int)(centerX - bodyWidth / 2), (int)segY,
                       (int)bodyWidth, 8, 0, 180);
        }

        // Highlight on shell
        g2d.setColor(highlightColor);
        double highlightWidth = bodyWidth * 0.6;
        double highlightHeight = bodyHeight * 0.7;
        g2d.fill(new Ellipse2D.Double(centerX - highlightWidth / 2, centerY - highlightHeight / 2 + 3,
                                       highlightWidth, highlightHeight));

        // Draw head (pronotum - front shield)
        double headWidth = 16;
        double headHeight = 12;
        double headY = centerY - 12;

        g2d.setColor(shellColor.darker());
        g2d.fill(new Ellipse2D.Double(centerX - headWidth / 2, headY - headHeight / 2,
                                       headWidth, headHeight));

        // Head highlight
        g2d.setColor(highlightColor.darker());
        double headHighlightWidth = headWidth * 0.7;
        double headHighlightHeight = headHeight * 0.6;
        g2d.fill(new Ellipse2D.Double(centerX - headHighlightWidth / 2, headY - headHighlightHeight / 2,
                                       headHighlightWidth, headHighlightHeight));

        // Draw eyes (small, on sides)
        g2d.setColor(new Color(20, 20, 20));
        double eyeRadius = 1.5;
        g2d.fill(new Ellipse2D.Double(centerX - 6 - eyeRadius, headY - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));
        g2d.fill(new Ellipse2D.Double(centerX + 6 - eyeRadius, headY - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));

        // Draw antennae (long, curved)
        g2d.setColor(antennaColor);
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Left antenna
        double antennaWave = Math.sin(progress * Math.PI * 2) * 3;
        for (int i = 0; i < 5; i++) {
            double t = i / 4.0;
            double startX = centerX - 5 - t * 10;
            double startY = headY - 5 - t * 8 + Math.sin(t * Math.PI + progress * Math.PI * 2) * antennaWave;
            double endX = centerX - 5 - (t + 0.25) * 10;
            double endY = headY - 5 - (t + 0.25) * 8 + Math.sin((t + 0.25) * Math.PI + progress * Math.PI * 2) * antennaWave;
            g2d.drawLine((int)startX, (int)startY, (int)endX, (int)endY);
        }

        // Right antenna
        for (int i = 0; i < 5; i++) {
            double t = i / 4.0;
            double startX = centerX + 5 + t * 10;
            double startY = headY - 5 - t * 8 + Math.sin(t * Math.PI - progress * Math.PI * 2) * antennaWave;
            double endX = centerX + 5 + (t + 0.25) * 10;
            double endY = headY - 5 - (t + 0.25) * 8 + Math.sin((t + 0.25) * Math.PI - progress * Math.PI * 2) * antennaWave;
            g2d.drawLine((int)startX, (int)startY, (int)endX, (int)endY);
        }

        // Draw cerci (rear appendages)
        g2d.setColor(legColor);
        g2d.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double cerciY = centerY + 14;
        g2d.drawLine((int)(centerX - 3), (int)cerciY,
                    (int)(centerX - 5), (int)(cerciY + 5));
        g2d.drawLine((int)(centerX + 3), (int)cerciY,
                    (int)(centerX + 5), (int)(cerciY + 5));

        g2d.dispose();
        return image;
    }
}
