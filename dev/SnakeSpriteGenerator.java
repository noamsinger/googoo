package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SnakeSpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateSnakeSpriteSheet();
            System.out.println("Successfully generated snake sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateSnakeSpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateSnakeFrame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_1.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Snake sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateSnakeFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for snake
        Color bodyColor = new Color(50, 200, 50); // Bright green body
        Color headColor = new Color(100, 255, 100); // Lighter lime green for head
        Color eyeColor = new Color(255, 200, 0); // Golden yellow eyes
        Color stripeColor = new Color(255, 255, 100); // Yellow stripes

        // Snake body - create a sinuous S-curve
        // The snake is oriented vertically (head at top)
        int numSegments = 12;
        double[] segmentX = new double[numSegments];
        double[] segmentY = new double[numSegments];
        double segmentRadius = 5.0;

        // Create undulating body position
        double snakeLength = 50; // Total length from head to tail
        double waveAmplitude = 8; // How much it wiggles side to side
        double waveFrequency = 2.0; // Number of S-curves along body

        for (int i = 0; i < numSegments; i++) {
            double t = i / (double) (numSegments - 1); // 0 at head, 1 at tail

            // Base position (vertical)
            double baseY = centerY - 20 + t * snakeLength;

            // Add sinuous wave motion
            double wavePhase = progress * Math.PI * 2; // Animate wave
            double wave = Math.sin(t * Math.PI * waveFrequency + wavePhase) * waveAmplitude;

            segmentX[i] = centerX + wave;
            segmentY[i] = baseY;
        }

        // Draw body segments from tail to head (so head is on top)
        for (int i = numSegments - 1; i >= 0; i--) {
            double t = i / (double) (numSegments - 1);

            // Segment size - thicker in middle, thinner at tail
            double radius;
            if (i == 0) {
                // Head is largest
                radius = segmentRadius * 1.8;
            } else if (t > 0.7) {
                // Tail tapers
                radius = segmentRadius * (0.4 + (1.0 - t) * 2.0);
            } else {
                // Body is full thickness
                radius = segmentRadius * (1.0 + (1.0 - t) * 0.3);
            }

            // Draw body segment
            Color segmentColor;
            if (i == 0) {
                // Head color
                segmentColor = headColor;
            } else {
                segmentColor = bodyColor;
            }

            g2d.setColor(segmentColor);
            g2d.fill(new Ellipse2D.Double(segmentX[i] - radius, segmentY[i] - radius,
                                           radius * 2, radius * 2));

            // Add white stripes on body (not head or tail)
            if (i > 0 && i < numSegments - 3 && i % 2 == 1) {
                g2d.setColor(stripeColor);
                double stripeWidth = radius * 1.8;
                double stripeHeight = 2.5;
                g2d.fill(new Ellipse2D.Double(segmentX[i] - stripeWidth / 2, segmentY[i] - stripeHeight / 2,
                                               stripeWidth, stripeHeight));
            }

            // Add shading outline
            g2d.setColor(bodyColor.darker());
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.draw(new Ellipse2D.Double(segmentX[i] - radius, segmentY[i] - radius,
                                           radius * 2, radius * 2));
        }

        // Draw head details (eyes, tongue)
        double headX = segmentX[0];
        double headY = segmentY[0];
        double headRadius = segmentRadius * 1.8;

        // Calculate head direction (tangent to body curve)
        double dirX = segmentX[0] - segmentX[1];
        double dirY = segmentY[0] - segmentY[1];
        double dirLength = Math.sqrt(dirX * dirX + dirY * dirY);
        dirX /= dirLength;
        dirY /= dirLength;

        // Draw two eyes
        double eyeRadius = 2.5;
        double eyeOffset = headRadius * 0.5;

        // Eye positions perpendicular to head direction
        double perpX = -dirY;
        double perpY = dirX;

        // Left eye
        double leftEyeX = headX + perpX * eyeOffset;
        double leftEyeY = headY + perpY * eyeOffset;

        // Right eye
        double rightEyeX = headX - perpX * eyeOffset;
        double rightEyeY = headY - perpY * eyeOffset;

        // Draw eye sockets (darker)
        g2d.setColor(headColor.darker());
        g2d.fill(new Ellipse2D.Double(leftEyeX - eyeRadius, leftEyeY - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));
        g2d.fill(new Ellipse2D.Double(rightEyeX - eyeRadius, rightEyeY - eyeRadius,
                                       eyeRadius * 2, eyeRadius * 2));

        // Draw eye whites
        g2d.setColor(Color.WHITE);
        double whiteRadius = eyeRadius * 0.9;
        g2d.fill(new Ellipse2D.Double(leftEyeX - whiteRadius, leftEyeY - whiteRadius,
                                       whiteRadius * 2, whiteRadius * 2));
        g2d.fill(new Ellipse2D.Double(rightEyeX - whiteRadius, rightEyeY - whiteRadius,
                                       whiteRadius * 2, whiteRadius * 2));

        // Draw eye pupils (blue)
        g2d.setColor(eyeColor);
        double pupilRadius = eyeRadius * 0.5;

        // Pupils look slightly forward
        double pupilOffsetX = dirX * pupilRadius * 0.3;
        double pupilOffsetY = dirY * pupilRadius * 0.3;

        g2d.fill(new Ellipse2D.Double(leftEyeX - pupilRadius + pupilOffsetX,
                                       leftEyeY - pupilRadius + pupilOffsetY,
                                       pupilRadius * 2, pupilRadius * 2));
        g2d.fill(new Ellipse2D.Double(rightEyeX - pupilRadius + pupilOffsetX,
                                       rightEyeY - pupilRadius + pupilOffsetY,
                                       pupilRadius * 2, pupilRadius * 2));

        // Draw eye highlights
        g2d.setColor(Color.WHITE);
        double highlightSize = 1.0;
        g2d.fill(new Ellipse2D.Double(leftEyeX - highlightSize / 2 + pupilOffsetX - pupilRadius * 0.3,
                                       leftEyeY - highlightSize / 2 + pupilOffsetY - pupilRadius * 0.3,
                                       highlightSize, highlightSize));
        g2d.fill(new Ellipse2D.Double(rightEyeX - highlightSize / 2 + pupilOffsetX - pupilRadius * 0.3,
                                       rightEyeY - highlightSize / 2 + pupilOffsetY - pupilRadius * 0.3,
                                       highlightSize, highlightSize));

        // Draw forked tongue (flicks in and out)
        double tongueProgress = Math.sin(progress * Math.PI * 4); // Flick 4 times per cycle
        if (tongueProgress > 0) {
            double tongueLength = 8 * tongueProgress;
            double tongueEndX = headX + dirX * (headRadius + tongueLength);
            double tongueEndY = headY + dirY * (headRadius + tongueLength);

            // Draw tongue base
            g2d.setColor(new Color(255, 100, 100)); // Reddish tongue
            g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine((int) (headX + dirX * headRadius), (int) (headY + dirY * headRadius),
                        (int) tongueEndX, (int) tongueEndY);

            // Draw forked tip
            double forkLength = 3;
            double forkAngle = Math.PI / 6; // 30 degrees

            // Left fork
            double leftForkX = tongueEndX + dirX * forkLength * Math.cos(forkAngle) - perpX * forkLength * Math.sin(forkAngle);
            double leftForkY = tongueEndY + dirY * forkLength * Math.cos(forkAngle) - perpY * forkLength * Math.sin(forkAngle);
            g2d.drawLine((int) tongueEndX, (int) tongueEndY, (int) leftForkX, (int) leftForkY);

            // Right fork
            double rightForkX = tongueEndX + dirX * forkLength * Math.cos(forkAngle) + perpX * forkLength * Math.sin(forkAngle);
            double rightForkY = tongueEndY + dirY * forkLength * Math.cos(forkAngle) + perpY * forkLength * Math.sin(forkAngle);
            g2d.drawLine((int) tongueEndX, (int) tongueEndY, (int) rightForkX, (int) rightForkY);
        }

        g2d.dispose();
        return image;
    }
}
