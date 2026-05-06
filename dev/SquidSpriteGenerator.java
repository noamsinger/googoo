package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SquidSpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateSquidSpriteSheet();
            System.out.println("Successfully generated squid sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateSquidSpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateSquidFrame(frame);

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
        System.out.println("Squid sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateSquidFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for squid
        Color bodyColor = new Color(255, 100, 150); // Pink/magenta body
        Color headColor = new Color(255, 150, 180); // Lighter pink for head
        Color eyeColor = new Color(100, 50, 200); // Purple eyes
        Color tentacleColor = new Color(200, 50, 100); // Dark pink/red tentacles

        // Draw 8 tentacles with wave animation
        // Tentacles fan out from the bottom of the head
        int numTentacles = 8;
        double tentacleLength = 28;
        double tentacleWidth = 3;

        for (int i = 0; i < numTentacles; i++) {
            // Spread tentacles in a fan pattern
            double angleSpread = Math.PI * 0.6; // 108 degrees spread
            double baseAngle = Math.PI / 2.0 - angleSpread / 2.0 + (i / (double)(numTentacles - 1)) * angleSpread;

            // Each tentacle has its own wave phase
            double wavePhase = progress * Math.PI * 2 + (i * Math.PI / 4.0);
            double wave = Math.sin(wavePhase) * 0.15; // Wave amplitude

            // Draw tentacle as a curved path
            Path2D tentacle = new Path2D.Double();
            double startX = centerX - 10 + (i - 3.5) * 2; // Start from bottom of head
            double startY = centerY + 8;

            tentacle.moveTo(startX, startY);

            // Create bezier curve for tentacle
            double angle = baseAngle + wave;
            double endX = startX + Math.cos(angle) * tentacleLength;
            double endY = startY + Math.sin(angle) * tentacleLength;

            // Control point for curve
            double ctrlX = startX + Math.cos(angle) * tentacleLength * 0.5;
            double ctrlY = startY + Math.sin(angle) * tentacleLength * 0.5;

            tentacle.quadTo(ctrlX, ctrlY, endX, endY);

            g2d.setColor(tentacleColor);
            g2d.setStroke(new BasicStroke((float)tentacleWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(tentacle);
        }

        // Draw squid head/mantle (bulbous body)
        double mantleWidth = 24;
        double mantleHeight = 26;

        // Pulsing animation for mantle
        double pulseProgress = Math.sin(progress * Math.PI * 2) * 0.1;
        double currentMantleWidth = mantleWidth * (1.0 + pulseProgress);
        double currentMantleHeight = mantleHeight * (1.0 + pulseProgress);

        // Draw main body as rounded shape
        Path2D body = new Path2D.Double();
        double bodyTop = centerY - 12;
        double bodyBottom = centerY + 8;
        double bodyLeft = centerX - currentMantleWidth / 2;
        double bodyRight = centerX + currentMantleWidth / 2;

        // Create mantle shape (rounded top, slightly wider at middle)
        body.moveTo(centerX, bodyTop - 8); // Top point

        // Right side curve
        body.curveTo(
            bodyRight + 2, bodyTop,
            bodyRight + 2, centerY - 4,
            bodyRight, centerY
        );
        body.curveTo(
            bodyRight - 1, centerY + 4,
            bodyRight - 4, bodyBottom,
            centerX, bodyBottom
        );

        // Left side curve (mirror)
        body.curveTo(
            bodyLeft + 4, bodyBottom,
            bodyLeft + 1, centerY + 4,
            bodyLeft, centerY
        );
        body.curveTo(
            bodyLeft - 2, centerY - 4,
            bodyLeft - 2, bodyTop,
            centerX, bodyTop - 8
        );

        body.closePath();

        // Fill body with gradient
        GradientPaint bodyGradient = new GradientPaint(
            (float)centerX, (float)bodyTop, headColor,
            (float)centerX, (float)bodyBottom, bodyColor
        );
        g2d.setPaint(bodyGradient);
        g2d.fill(body);

        // Draw body outline
        g2d.setColor(bodyColor.darker());
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(body);

        // Draw two large eyes
        double eyeRadius = 4.5;
        double eyeSpacing = 8;

        // Eye sockets (darker)
        g2d.setColor(bodyColor.darker());
        g2d.fill(new Ellipse2D.Double(centerX - eyeSpacing - eyeRadius, centerY - 6 - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));
        g2d.fill(new Ellipse2D.Double(centerX + eyeSpacing - eyeRadius, centerY - 6 - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));

        // Eye whites
        g2d.setColor(Color.WHITE);
        g2d.fill(new Ellipse2D.Double(centerX - eyeSpacing - eyeRadius + 0.5, centerY - 6 - eyeRadius + 0.5,
                                       eyeRadius * 2 - 1, eyeRadius * 2 - 1));
        g2d.fill(new Ellipse2D.Double(centerX + eyeSpacing - eyeRadius + 0.5, centerY - 6 - eyeRadius + 0.5,
                                       eyeRadius * 2 - 1, eyeRadius * 2 - 1));

        // Eye pupils (animated - look around slightly)
        double pupilOffset = Math.sin(progress * Math.PI * 4) * 1.0;
        double pupilRadius = 2.5;

        g2d.setColor(eyeColor);
        g2d.fill(new Ellipse2D.Double(centerX - eyeSpacing - pupilRadius + pupilOffset,
                                       centerY - 6 - pupilRadius,
                                       pupilRadius * 2, pupilRadius * 2));
        g2d.fill(new Ellipse2D.Double(centerX + eyeSpacing - pupilRadius + pupilOffset,
                                       centerY - 6 - pupilRadius,
                                       pupilRadius * 2, pupilRadius * 2));

        // Eye highlights (small white dots)
        g2d.setColor(Color.WHITE);
        double highlightSize = 1.5;
        g2d.fill(new Ellipse2D.Double(centerX - eyeSpacing - 1 + pupilOffset, centerY - 8,
                                       highlightSize, highlightSize));
        g2d.fill(new Ellipse2D.Double(centerX + eyeSpacing - 1 + pupilOffset, centerY - 8,
                                       highlightSize, highlightSize));

        // Draw fins/wings on sides (small)
        double finAngle = Math.sin(progress * Math.PI * 2) * 0.2;

        // Left fin
        Path2D leftFin = new Path2D.Double();
        leftFin.moveTo(centerX - 10, centerY - 4);
        leftFin.curveTo(centerX - 18, centerY - 6 + Math.sin(finAngle) * 3,
                       centerX - 18, centerY + 2 + Math.sin(finAngle) * 3,
                       centerX - 10, centerY + 4);
        leftFin.closePath();

        // Right fin
        Path2D rightFin = new Path2D.Double();
        rightFin.moveTo(centerX + 10, centerY - 4);
        rightFin.curveTo(centerX + 18, centerY - 6 - Math.sin(finAngle) * 3,
                        centerX + 18, centerY + 2 - Math.sin(finAngle) * 3,
                        centerX + 10, centerY + 4);
        rightFin.closePath();

        // Draw fins with semi-transparency
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.fill(leftFin);
        g2d.fill(rightFin);

        g2d.setColor(new Color(255, 140, 0, 150));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.draw(leftFin);
        g2d.draw(rightFin);

        g2d.dispose();
        return image;
    }
}
