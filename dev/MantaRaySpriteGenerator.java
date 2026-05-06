package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MantaRaySpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateMantaRaySpriteSheet();
            System.out.println("Successfully generated manta ray sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateMantaRaySpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateMantaRayFrame(frame);

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
        System.out.println("Manta ray sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateMantaRayFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for manta ray (from above)
        Color topColor = new Color(30, 50, 80); // Dark blue-gray top
        Color bellyColor = new Color(180, 200, 220); // Light gray belly
        Color spotColor = new Color(150, 170, 200); // Light blue spots
        Color eyeColor = new Color(20, 30, 50); // Dark eyes

        // Wing flapping animation
        double wingFlap = Math.sin(progress * Math.PI * 2) * 0.15;

        // Draw body shape (diamond/kite shape with wings)
        Path2D body = new Path2D.Double();

        // Head (pointed front)
        double headX = centerX;
        double headY = centerY - 18;

        body.moveTo(headX, headY);

        // Left wing
        body.curveTo(
            centerX - 10, centerY - 15 + wingFlap * 8,
            centerX - 20, centerY - 10 + wingFlap * 15,
            centerX - 25, centerY + wingFlap * 20
        );

        // Left wing back edge
        body.curveTo(
            centerX - 22, centerY + 5 + wingFlap * 15,
            centerX - 15, centerY + 8 + wingFlap * 10,
            centerX - 8, centerY + 10
        );

        // Tail base left
        body.lineTo(centerX - 3, centerY + 12);

        // Tail tip
        body.lineTo(centerX, centerY + 22);

        // Tail base right
        body.lineTo(centerX + 3, centerY + 12);

        // Right wing back edge
        body.curveTo(
            centerX + 15, centerY + 8 + wingFlap * 10,
            centerX + 22, centerY + 5 + wingFlap * 15,
            centerX + 25, centerY + wingFlap * 20
        );

        // Right wing
        body.curveTo(
            centerX + 20, centerY - 10 - wingFlap * 15,
            centerX + 10, centerY - 15 - wingFlap * 8,
            headX, headY
        );

        body.closePath();

        // Draw body with gradient
        GradientPaint bodyGradient = new GradientPaint(
            (float)centerX, (float)headY, topColor,
            (float)centerX, (float)(centerY + 22), topColor.darker()
        );
        g2d.setPaint(bodyGradient);
        g2d.fill(body);

        // Draw central body (lighter)
        Path2D centralBody = new Path2D.Double();
        centralBody.moveTo(centerX, headY + 3);
        centralBody.curveTo(
            centerX - 6, centerY - 10,
            centerX - 8, centerY,
            centerX - 6, centerY + 8
        );
        centralBody.lineTo(centerX, centerY + 15);
        centralBody.curveTo(
            centerX + 8, centerY,
            centerX + 6, centerY - 10,
            centerX, headY + 3
        );
        centralBody.closePath();

        g2d.setColor(bellyColor);
        g2d.fill(centralBody);

        // Draw spots pattern on wings
        g2d.setColor(spotColor);
        // Left wing spots
        g2d.fill(new Ellipse2D.Double(centerX - 15, centerY - 8, 4, 3));
        g2d.fill(new Ellipse2D.Double(centerX - 18, centerY - 3, 3, 2.5));
        g2d.fill(new Ellipse2D.Double(centerX - 20, centerY + 2, 3.5, 3));

        // Right wing spots
        g2d.fill(new Ellipse2D.Double(centerX + 11, centerY - 8, 4, 3));
        g2d.fill(new Ellipse2D.Double(centerX + 15, centerY - 3, 3, 2.5));
        g2d.fill(new Ellipse2D.Double(centerX + 16.5, centerY + 2, 3.5, 3));

        // Draw cephalic fins (horn-like projections at front)
        Path2D leftCephalicFin = new Path2D.Double();
        leftCephalicFin.moveTo(centerX - 3, headY + 2);
        leftCephalicFin.curveTo(
            centerX - 8, headY - 5,
            centerX - 10, headY - 8,
            centerX - 8, headY - 12
        );
        leftCephalicFin.lineTo(centerX - 5, headY - 8);
        leftCephalicFin.closePath();

        Path2D rightCephalicFin = new Path2D.Double();
        rightCephalicFin.moveTo(centerX + 3, headY + 2);
        rightCephalicFin.curveTo(
            centerX + 8, headY - 5,
            centerX + 10, headY - 8,
            centerX + 8, headY - 12
        );
        rightCephalicFin.lineTo(centerX + 5, headY - 8);
        rightCephalicFin.closePath();

        g2d.setColor(topColor.darker());
        g2d.fill(leftCephalicFin);
        g2d.fill(rightCephalicFin);

        // Draw eyes
        g2d.setColor(eyeColor);
        double eyeRadius = 2;
        g2d.fill(new Ellipse2D.Double(centerX - 5 - eyeRadius, headY + 5 - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));
        g2d.fill(new Ellipse2D.Double(centerX + 5 - eyeRadius, headY + 5 - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));

        // Draw mouth (curved line)
        g2d.setColor(bellyColor.darker());
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawArc((int)(centerX - 6), (int)(headY + 4), 12, 8, 180, 180);

        // Draw gill slits (5 on each side)
        g2d.setColor(topColor.darker().darker());
        g2d.setStroke(new BasicStroke(1.0f));
        for (int i = 0; i < 5; i++) {
            double gillY = centerY - 5 + i * 3;
            g2d.drawLine((int)(centerX - 6), (int)gillY,
                        (int)(centerX - 4), (int)(gillY + 2));
            g2d.drawLine((int)(centerX + 6), (int)gillY,
                        (int)(centerX + 4), (int)(gillY + 2));
        }

        // Draw body outline
        g2d.setColor(topColor.darker());
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(body);

        // Draw tail spine
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine((int)centerX, (int)(centerY + 22),
                    (int)centerX, (int)(centerY + 28));

        g2d.dispose();
        return image;
    }
}
