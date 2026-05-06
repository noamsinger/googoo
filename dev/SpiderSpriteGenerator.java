package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SpiderSpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateSpiderSpriteSheet();
            System.out.println("Successfully generated spider sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateSpiderSpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateSpiderFrame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_5.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Spider sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateSpiderFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for spider
        Color bodyColor = new Color(150, 50, 50); // Dark red body
        Color legColor = new Color(100, 30, 30); // Darker red legs
        Color eyeColor = new Color(255, 200, 0); // Yellow eyes
        Color accentColor = new Color(200, 80, 80); // Lighter red for markings

        // Leg animation - legs move in walking motion
        double legWave = Math.sin(progress * Math.PI * 2);

        // Draw 8 legs (4 on each side)
        int numLegs = 4; // per side
        double legSpacing = 8;

        for (int side = 0; side < 2; side++) {
            int direction = (side == 0) ? -1 : 1;

            for (int i = 0; i < numLegs; i++) {
                double legStartY = centerY - 10 + i * 6;
                double legStartX = centerX + direction * 8;

                // Alternate leg movement
                double legPhase = legWave * (i % 2 == 0 ? 1 : -1);
                double legAngle = direction * (Math.PI / 3 + legPhase * 0.3);

                // First segment
                double leg1Length = 15;
                double leg1EndX = legStartX + Math.cos(legAngle) * leg1Length;
                double leg1EndY = legStartY + Math.sin(legAngle) * leg1Length;

                g2d.setColor(legColor);
                g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine((int)legStartX, (int)legStartY, (int)leg1EndX, (int)leg1EndY);

                // Second segment (bent)
                double leg2Angle = legAngle + direction * (Math.PI / 4);
                double leg2Length = 12;
                double leg2EndX = leg1EndX + Math.cos(leg2Angle) * leg2Length;
                double leg2EndY = leg1EndY + Math.sin(leg2Angle) * leg2Length;

                g2d.drawLine((int)leg1EndX, (int)leg1EndY, (int)leg2EndX, (int)leg2EndY);

                // Third segment (foot)
                double leg3Angle = leg2Angle + direction * (Math.PI / 6);
                double leg3Length = 8;
                double leg3EndX = leg2EndX + Math.cos(leg3Angle) * leg3Length;
                double leg3EndY = leg2EndY + Math.sin(leg3Angle) * leg3Length;

                g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine((int)leg2EndX, (int)leg2EndY, (int)leg3EndX, (int)leg3EndY);

                // Draw joint dots
                g2d.setColor(legColor.brighter());
                g2d.fill(new Ellipse2D.Double(leg1EndX - 1.5, leg1EndY - 1.5, 3, 3));
                g2d.fill(new Ellipse2D.Double(leg2EndX - 1, leg2EndY - 1, 2, 2));
            }
        }

        // Draw abdomen (larger rear part)
        double abdomenWidth = 18;
        double abdomenHeight = 22;
        double abdomenY = centerY + 2;

        g2d.setColor(bodyColor);
        g2d.fill(new Ellipse2D.Double(centerX - abdomenWidth / 2, abdomenY - abdomenHeight / 2,
                                       abdomenWidth, abdomenHeight));

        // Draw abdomen pattern
        g2d.setColor(accentColor);
        double patternWidth = abdomenWidth * 0.6;
        double patternHeight = abdomenHeight * 0.7;
        g2d.fill(new Ellipse2D.Double(centerX - patternWidth / 2, abdomenY - patternHeight / 2,
                                       patternWidth, patternHeight));

        // Draw spots on abdomen
        g2d.setColor(bodyColor.darker());
        g2d.fill(new Ellipse2D.Double(centerX - 2, abdomenY - 5, 4, 4));
        g2d.fill(new Ellipse2D.Double(centerX - 2, abdomenY + 3, 4, 4));

        // Draw cephalothorax (front body part with head)
        double cephWidth = 14;
        double cephHeight = 16;
        double cephY = centerY - 8;

        g2d.setColor(bodyColor.brighter());
        g2d.fill(new Ellipse2D.Double(centerX - cephWidth / 2, cephY - cephHeight / 2,
                                       cephWidth, cephHeight));

        // Draw eyes (8 eyes arranged in typical spider pattern)
        g2d.setColor(eyeColor);

        // Main front eyes (2 large)
        double mainEyeRadius = 2;
        g2d.fill(new Ellipse2D.Double(centerX - 4 - mainEyeRadius, cephY - 4 - mainEyeRadius,
                                       mainEyeRadius * 2, mainEyeRadius * 2));
        g2d.fill(new Ellipse2D.Double(centerX + 4 - mainEyeRadius, cephY - 4 - mainEyeRadius,
                                       mainEyeRadius * 2, mainEyeRadius * 2));

        // Secondary eyes (4 medium, arranged above)
        double secEyeRadius = 1.5;
        g2d.fill(new Ellipse2D.Double(centerX - 6 - secEyeRadius, cephY - 7 - secEyeRadius,
                                       secEyeRadius * 2, secEyeRadius * 2));
        g2d.fill(new Ellipse2D.Double(centerX + 6 - secEyeRadius, cephY - 7 - secEyeRadius,
                                       secEyeRadius * 2, secEyeRadius * 2));

        // Tertiary eyes (2 small, side)
        double tertEyeRadius = 1;
        g2d.fill(new Ellipse2D.Double(centerX - 8 - tertEyeRadius, cephY - 3 - tertEyeRadius,
                                       tertEyeRadius * 2, tertEyeRadius * 2));
        g2d.fill(new Ellipse2D.Double(centerX + 8 - tertEyeRadius, cephY - 3 - tertEyeRadius,
                                       tertEyeRadius * 2, tertEyeRadius * 2));

        // Rear eyes (2 small, top)
        g2d.fill(new Ellipse2D.Double(centerX - 3 - tertEyeRadius, cephY - 8 - tertEyeRadius,
                                       tertEyeRadius * 2, tertEyeRadius * 2));
        g2d.fill(new Ellipse2D.Double(centerX + 3 - tertEyeRadius, cephY - 8 - tertEyeRadius,
                                       tertEyeRadius * 2, tertEyeRadius * 2));

        // Eye highlights
        g2d.setColor(Color.WHITE);
        g2d.fill(new Ellipse2D.Double(centerX - 4 - 0.5, cephY - 5, 1, 1));
        g2d.fill(new Ellipse2D.Double(centerX + 4 - 0.5, cephY - 5, 1, 1));

        // Draw fangs (chelicerae)
        g2d.setColor(legColor.darker());
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine((int)(centerX - 3), (int)(cephY + 4), (int)(centerX - 4), (int)(cephY + 8));
        g2d.drawLine((int)(centerX + 3), (int)(cephY + 4), (int)(centerX + 4), (int)(cephY + 8));

        g2d.dispose();
        return image;
    }
}
