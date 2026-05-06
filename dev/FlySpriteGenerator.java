package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FlySpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateFlySpriteSheet();
            System.out.println("Successfully generated fly sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateFlySpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateFlyFrame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_8.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Fly sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateFlyFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for fly (from above)
        Color bodyColor = new Color(40, 60, 100); // Dark blue-gray body
        Color headColor = new Color(60, 80, 120); // Lighter blue-gray head
        Color wingColor = new Color(200, 220, 240, 120); // Translucent wings
        Color eyeColor = new Color(150, 50, 50); // Dark red compound eyes
        Color stripesColor = new Color(80, 100, 140); // Light blue stripes

        // Wing flapping animation - very fast
        double wingFlap = Math.sin(progress * Math.PI * 8); // Very fast flapping
        double wingSpread = 0.8 + Math.abs(wingFlap) * 0.2; // Wings spread more when up

        // Draw wings (behind body) - two wings visible from above
        // Left wing
        Path2D leftWing = new Path2D.Double();
        double wingBaseX = centerX - 5;
        double wingBaseY = centerY;

        leftWing.moveTo(wingBaseX, wingBaseY);
        leftWing.curveTo(
            wingBaseX - 15 * wingSpread, centerY - 12,
            wingBaseX - 18 * wingSpread, centerY - 5,
            wingBaseX - 15 * wingSpread, centerY + 8
        );
        leftWing.lineTo(wingBaseX, centerY + 3);
        leftWing.closePath();

        // Right wing (mirror)
        Path2D rightWing = new Path2D.Double();
        double rightWingBaseX = centerX + 5;

        rightWing.moveTo(rightWingBaseX, wingBaseY);
        rightWing.curveTo(
            rightWingBaseX + 15 * wingSpread, centerY - 12,
            rightWingBaseX + 18 * wingSpread, centerY - 5,
            rightWingBaseX + 15 * wingSpread, centerY + 8
        );
        rightWing.lineTo(rightWingBaseX, centerY + 3);
        rightWing.closePath();

        g2d.setColor(wingColor);
        g2d.fill(leftWing);
        g2d.fill(rightWing);

        // Wing veins
        g2d.setColor(new Color(150, 170, 190, 100));
        g2d.setStroke(new BasicStroke(0.8f));
        // Left wing veins
        g2d.drawLine((int)wingBaseX, (int)wingBaseY,
                    (int)(wingBaseX - 12 * wingSpread), (int)(centerY - 8));
        g2d.drawLine((int)wingBaseX, (int)wingBaseY,
                    (int)(wingBaseX - 15 * wingSpread), (int)centerY);
        g2d.drawLine((int)wingBaseX, (int)wingBaseY,
                    (int)(wingBaseX - 12 * wingSpread), (int)(centerY + 5));

        // Right wing veins
        g2d.drawLine((int)rightWingBaseX, (int)wingBaseY,
                    (int)(rightWingBaseX + 12 * wingSpread), (int)(centerY - 8));
        g2d.drawLine((int)rightWingBaseX, (int)wingBaseY,
                    (int)(rightWingBaseX + 15 * wingSpread), (int)centerY);
        g2d.drawLine((int)rightWingBaseX, (int)wingBaseY,
                    (int)(rightWingBaseX + 12 * wingSpread), (int)(centerY + 5));

        // Draw 6 legs (3 on each side, visible from above)
        g2d.setColor(bodyColor.darker());
        g2d.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (int side = 0; side < 2; side++) {
            int direction = (side == 0) ? -1 : 1;

            // Front legs
            double frontLegX = centerX + direction * 4;
            double frontLegY = centerY - 3;
            g2d.drawLine((int)frontLegX, (int)frontLegY,
                        (int)(frontLegX + direction * 8), (int)(frontLegY - 5));
            g2d.drawLine((int)(frontLegX + direction * 8), (int)(frontLegY - 5),
                        (int)(frontLegX + direction * 12), (int)(frontLegY - 3));

            // Middle legs
            double midLegX = centerX + direction * 5;
            double midLegY = centerY + 2;
            g2d.drawLine((int)midLegX, (int)midLegY,
                        (int)(midLegX + direction * 10), (int)midLegY);
            g2d.drawLine((int)(midLegX + direction * 10), (int)midLegY,
                        (int)(midLegX + direction * 13), (int)(midLegY + 2));

            // Back legs
            double backLegX = centerX + direction * 4;
            double backLegY = centerY + 6;
            g2d.drawLine((int)backLegX, (int)backLegY,
                        (int)(backLegX + direction * 8), (int)(backLegY + 5));
            g2d.drawLine((int)(backLegX + direction * 8), (int)(backLegY + 5),
                        (int)(backLegX + direction * 11), (int)(backLegY + 3));
        }

        // Draw thorax (middle body segment)
        double thoraxWidth = 10;
        double thoraxHeight = 12;

        g2d.setColor(bodyColor);
        g2d.fill(new Ellipse2D.Double(centerX - thoraxWidth / 2, centerY - thoraxHeight / 2,
                                       thoraxWidth, thoraxHeight));

        // Draw thorax stripes
        g2d.setColor(stripesColor);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawLine((int)(centerX - 4), (int)(centerY - 2),
                    (int)(centerX + 4), (int)(centerY - 2));
        g2d.drawLine((int)(centerX - 4), (int)(centerY + 2),
                    (int)(centerX + 4), (int)(centerY + 2));

        // Draw abdomen (rear body segment) - oval
        double abdomenWidth = 8;
        double abdomenHeight = 10;
        double abdomenY = centerY + 10;

        g2d.setColor(bodyColor.darker());
        g2d.fill(new Ellipse2D.Double(centerX - abdomenWidth / 2, abdomenY - abdomenHeight / 2,
                                       abdomenWidth, abdomenHeight));

        // Abdomen stripes
        g2d.setColor(stripesColor.darker());
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine((int)(centerX - 3), (int)(abdomenY - 2),
                    (int)(centerX + 3), (int)(abdomenY - 2));
        g2d.drawLine((int)(centerX - 3), (int)(abdomenY + 2),
                    (int)(centerX + 3), (int)(abdomenY + 2));

        // Draw head (front, smaller)
        double headWidth = 8;
        double headHeight = 7;
        double headY = centerY - 8;

        g2d.setColor(headColor);
        g2d.fill(new Ellipse2D.Double(centerX - headWidth / 2, headY - headHeight / 2,
                                       headWidth, headHeight));

        // Draw compound eyes (large, visible from above)
        double eyeRadius = 3;

        g2d.setColor(eyeColor);
        g2d.fill(new Ellipse2D.Double(centerX - 5 - eyeRadius, headY - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));
        g2d.fill(new Ellipse2D.Double(centerX + 5 - eyeRadius, headY - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));

        // Eye facets (compound eye detail)
        g2d.setColor(new Color(100, 30, 30));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                double facetX = centerX - 5 - 1.5 + i;
                double facetY = headY - 1.5 + j;
                g2d.fill(new Ellipse2D.Double(facetX, facetY, 0.5, 0.5));

                facetX = centerX + 5 - 1.5 + i;
                g2d.fill(new Ellipse2D.Double(facetX, facetY, 0.5, 0.5));
            }
        }

        // Draw antennae (short, visible from above)
        g2d.setColor(bodyColor.darker());
        g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine((int)(centerX - 3), (int)(headY - 3),
                    (int)(centerX - 5), (int)(headY - 6));
        g2d.drawLine((int)(centerX + 3), (int)(headY - 3),
                    (int)(centerX + 5), (int)(headY - 6));

        g2d.dispose();
        return image;
    }
}
