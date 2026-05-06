package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SharkSpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateSharkSpriteSheet();
            System.out.println("Successfully generated shark sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateSharkSpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateSharkFrame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_11.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Shark sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateSharkFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for shark (viewed from above)
        Color topBodyColor = new Color(60, 120, 180); // Blue-gray top
        Color bellyColor = new Color(180, 200, 220); // Light gray belly
        Color finColor = new Color(50, 100, 150); // Darker blue fins
        Color eyeColor = new Color(20, 20, 20); // Dark eyes

        // Hard tail thrashing animation
        double tailSwish = Math.sin(progress * Math.PI * 2) * 0.8;

        // Draw main body (streamlined shape viewed from above)
        Path2D body = new Path2D.Double();

        // Head (pointed nose)
        double noseX = centerX;
        double noseY = centerY - 20;

        body.moveTo(noseX, noseY);

        // Left side of body
        body.curveTo(
            centerX - 8, centerY - 15,
            centerX - 12, centerY - 5,
            centerX - 10, centerY + 5
        );

        // Left side toward tail (more dramatic curve)
        body.curveTo(
            centerX - 8, centerY + 12,
            centerX - 5 + tailSwish * 2, centerY + 18,
            centerX - 3 + tailSwish * 8, centerY + 22
        );

        // Tail notch
        body.lineTo(centerX + tailSwish * 8, centerY + 20);

        // Right side of tail
        body.lineTo(centerX + 3 + tailSwish * 8, centerY + 22);

        // Right side of body (more dramatic curve)
        body.curveTo(
            centerX + 5 + tailSwish * 2, centerY + 18,
            centerX + 8, centerY + 12,
            centerX + 10, centerY + 5
        );

        // Right side toward head
        body.curveTo(
            centerX + 12, centerY - 5,
            centerX + 8, centerY - 15,
            noseX, noseY
        );

        body.closePath();

        // Draw body with gradient
        GradientPaint bodyGradient = new GradientPaint(
            (float)centerX, (float)(centerY - 20), topBodyColor,
            (float)centerX, (float)(centerY + 22), topBodyColor.darker()
        );
        g2d.setPaint(bodyGradient);
        g2d.fill(body);

        // Draw belly (lighter color in middle)
        Path2D belly = new Path2D.Double();
        belly.moveTo(centerX, centerY - 15);
        belly.curveTo(
            centerX - 5, centerY - 10,
            centerX - 6, centerY,
            centerX - 5, centerY + 10
        );
        belly.curveTo(
            centerX - 3, centerY + 15,
            centerX, centerY + 17,
            centerX, centerY + 17
        );
        belly.curveTo(
            centerX, centerY + 17,
            centerX + 3, centerY + 15,
            centerX + 5, centerY + 10
        );
        belly.curveTo(
            centerX + 6, centerY,
            centerX + 5, centerY - 10,
            centerX, centerY - 15
        );
        belly.closePath();

        g2d.setColor(bellyColor);
        g2d.fill(belly);

        // Draw dorsal fin (top fin)
        Path2D dorsalFin = new Path2D.Double();
        dorsalFin.moveTo(centerX - 2, centerY - 2);
        dorsalFin.lineTo(centerX, centerY - 10);
        dorsalFin.lineTo(centerX + 2, centerY + 2);
        dorsalFin.closePath();

        g2d.setColor(finColor);
        g2d.fill(dorsalFin);

        // Draw outline for depth
        g2d.setColor(finColor.darker());
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.draw(dorsalFin);

        // Draw pectoral fins (side fins)
        Path2D leftPectoralFin = new Path2D.Double();
        leftPectoralFin.moveTo(centerX - 8, centerY - 3);
        leftPectoralFin.curveTo(
            centerX - 18, centerY - 8,
            centerX - 20, centerY - 5,
            centerX - 18, centerY
        );
        leftPectoralFin.lineTo(centerX - 10, centerY + 2);
        leftPectoralFin.closePath();

        Path2D rightPectoralFin = new Path2D.Double();
        rightPectoralFin.moveTo(centerX + 8, centerY - 3);
        rightPectoralFin.curveTo(
            centerX + 18, centerY - 8,
            centerX + 20, centerY - 5,
            centerX + 18, centerY
        );
        rightPectoralFin.lineTo(centerX + 10, centerY + 2);
        rightPectoralFin.closePath();

        g2d.setColor(finColor);
        g2d.fill(leftPectoralFin);
        g2d.fill(rightPectoralFin);

        g2d.setColor(finColor.darker());
        g2d.draw(leftPectoralFin);
        g2d.draw(rightPectoralFin);

        // Draw tail fin with more dramatic thrashing
        Path2D tailFin = new Path2D.Double();
        double tailCenterX = centerX + tailSwish * 8;
        double tailY = centerY + 22;

        tailFin.moveTo(tailCenterX - 3, tailY);
        tailFin.lineTo(tailCenterX - 8 + tailSwish * 5, tailY + 8);
        tailFin.lineTo(tailCenterX, tailY + 6);
        tailFin.lineTo(tailCenterX + 8 + tailSwish * 5, tailY + 8);
        tailFin.lineTo(tailCenterX + 3, tailY);
        tailFin.closePath();

        g2d.setColor(finColor);
        g2d.fill(tailFin);

        g2d.setColor(finColor.darker());
        g2d.draw(tailFin);

        // Draw eyes
        g2d.setColor(eyeColor);
        double eyeRadius = 2;
        g2d.fill(new Ellipse2D.Double(centerX - 5 - eyeRadius, centerY - 12 - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));
        g2d.fill(new Ellipse2D.Double(centerX + 5 - eyeRadius, centerY - 12 - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));

        // Draw gills
        g2d.setColor(topBodyColor.darker());
        g2d.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < 3; i++) {
            double gillY = centerY - 8 + i * 3;
            g2d.drawLine((int)(centerX - 8), (int)gillY, (int)(centerX - 6), (int)(gillY + 2));
            g2d.drawLine((int)(centerX + 8), (int)gillY, (int)(centerX + 6), (int)(gillY + 2));
        }

        // Draw body outline
        g2d.setColor(topBodyColor.darker());
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(body);

        g2d.dispose();
        return image;
    }
}
