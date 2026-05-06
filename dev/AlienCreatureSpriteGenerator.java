package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AlienCreatureSpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateAlienCreatureSpriteSheet();
            System.out.println("Successfully generated alien creature sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateAlienCreatureSpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateAlienCreatureFrame(frame);

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
        System.out.println("Alien creature sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateAlienCreatureFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for alien creature (reptilian/insectoid)
        Color bodyColor = new Color(120, 255, 150); // Bright green body
        Color darkBodyColor = new Color(60, 180, 90); // Dark green
        Color spotColor = new Color(255, 200, 100); // Orange spots
        Color eyeColor = new Color(255, 50, 50); // Bright red eyes
        Color pupilColor = new Color(255, 255, 0); // Yellow pupils
        Color clawColor = new Color(200, 100, 250); // Purple claws

        // Breathing/pulsing animation
        double pulse = 0.9 + Math.sin(progress * Math.PI * 2) * 0.1;

        // Draw main body (oval with alien shape)
        double bodyWidth = 20 * pulse;
        double bodyHeight = 24 * pulse;

        Path2D body = new Path2D.Double();
        // Head area (narrower)
        body.moveTo(centerX, centerY - bodyHeight / 2);
        body.curveTo(
            centerX - bodyWidth * 0.4, centerY - bodyHeight / 2,
            centerX - bodyWidth * 0.5, centerY - bodyHeight / 4,
            centerX - bodyWidth * 0.5, centerY
        );
        // Lower body (wider)
        body.curveTo(
            centerX - bodyWidth * 0.6, centerY + bodyHeight / 4,
            centerX - bodyWidth * 0.5, centerY + bodyHeight / 2,
            centerX, centerY + bodyHeight / 2
        );
        // Right side (mirror)
        body.curveTo(
            centerX + bodyWidth * 0.5, centerY + bodyHeight / 2,
            centerX + bodyWidth * 0.6, centerY + bodyHeight / 4,
            centerX + bodyWidth * 0.5, centerY
        );
        body.curveTo(
            centerX + bodyWidth * 0.5, centerY - bodyHeight / 4,
            centerX + bodyWidth * 0.4, centerY - bodyHeight / 2,
            centerX, centerY - bodyHeight / 2
        );
        body.closePath();

        // Gradient for body
        GradientPaint bodyGradient = new GradientPaint(
            (float)centerX, (float)(centerY - bodyHeight / 2), bodyColor,
            (float)centerX, (float)(centerY + bodyHeight / 2), darkBodyColor
        );
        g2d.setPaint(bodyGradient);
        g2d.fill(body);

        // Draw spots/markings
        g2d.setColor(spotColor);
        for (int i = 0; i < 6; i++) {
            double spotY = centerY - bodyHeight / 4 + i * (bodyHeight / 2 / 5);
            double spotSize = 2 + (i % 2) * 1.5;
            g2d.fill(new Ellipse2D.Double(centerX - 6 - spotSize / 2, spotY - spotSize / 2,
                                           spotSize, spotSize));
            g2d.fill(new Ellipse2D.Double(centerX + 6 - spotSize / 2, spotY - spotSize / 2,
                                           spotSize, spotSize));
        }

        // Draw spine/ridge on back
        g2d.setColor(darkBodyColor.darker());
        g2d.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < 5; i++) {
            double spineY = centerY - bodyHeight / 3 + i * (bodyHeight / 2 / 4);
            double spineHeight = 3 + Math.sin(progress * Math.PI * 2 + i) * 1;
            g2d.drawLine((int)centerX, (int)spineY, (int)centerX, (int)(spineY - spineHeight));
        }

        // Draw large alien eyes
        double eyeWidth = 6;
        double eyeHeight = 8;
        double eyeOffsetX = 7;
        double eyeOffsetY = -6;

        // Left eye
        Path2D leftEye = new Path2D.Double();
        leftEye.moveTo(centerX - eyeOffsetX, eyeOffsetY + centerY - eyeHeight / 2);
        leftEye.curveTo(
            centerX - eyeOffsetX - eyeWidth / 2, eyeOffsetY + centerY - eyeHeight / 2,
            centerX - eyeOffsetX - eyeWidth / 2, eyeOffsetY + centerY + eyeHeight / 2,
            centerX - eyeOffsetX, eyeOffsetY + centerY + eyeHeight / 2
        );
        leftEye.curveTo(
            centerX - eyeOffsetX + eyeWidth / 2, eyeOffsetY + centerY + eyeHeight / 2,
            centerX - eyeOffsetX + eyeWidth / 2, eyeOffsetY + centerY - eyeHeight / 2,
            centerX - eyeOffsetX, eyeOffsetY + centerY - eyeHeight / 2
        );
        leftEye.closePath();

        g2d.setColor(eyeColor);
        g2d.fill(leftEye);

        // Right eye
        Path2D rightEye = new Path2D.Double();
        rightEye.moveTo(centerX + eyeOffsetX, eyeOffsetY + centerY - eyeHeight / 2);
        rightEye.curveTo(
            centerX + eyeOffsetX - eyeWidth / 2, eyeOffsetY + centerY - eyeHeight / 2,
            centerX + eyeOffsetX - eyeWidth / 2, eyeOffsetY + centerY + eyeHeight / 2,
            centerX + eyeOffsetX, eyeOffsetY + centerY + eyeHeight / 2
        );
        rightEye.curveTo(
            centerX + eyeOffsetX + eyeWidth / 2, eyeOffsetY + centerY + eyeHeight / 2,
            centerX + eyeOffsetX + eyeWidth / 2, eyeOffsetY + centerY - eyeHeight / 2,
            centerX + eyeOffsetX, eyeOffsetY + centerY - eyeHeight / 2
        );
        rightEye.closePath();

        g2d.setColor(eyeColor);
        g2d.fill(rightEye);

        // Draw pupils (vertical slits)
        g2d.setColor(pupilColor);
        double pupilWidth = 2;
        double pupilHeight = 5;
        g2d.fillRect((int)(centerX - eyeOffsetX - pupilWidth / 2), (int)(eyeOffsetY + centerY - pupilHeight / 2),
                    (int)pupilWidth, (int)pupilHeight);
        g2d.fillRect((int)(centerX + eyeOffsetX - pupilWidth / 2), (int)(eyeOffsetY + centerY - pupilHeight / 2),
                    (int)pupilWidth, (int)pupilHeight);

        // Draw eye highlights
        g2d.setColor(new Color(255, 200, 200));
        g2d.fill(new Ellipse2D.Double(centerX - eyeOffsetX - 2, eyeOffsetY + centerY - 3, 1.5, 1.5));
        g2d.fill(new Ellipse2D.Double(centerX + eyeOffsetX - 2, eyeOffsetY + centerY - 3, 1.5, 1.5));

        // Draw small mouth
        g2d.setColor(darkBodyColor.darker());
        g2d.setStroke(new BasicStroke(1.5f));
        Path2D mouth = new Path2D.Double();
        mouth.moveTo(centerX - 3, centerY + 2);
        mouth.curveTo(centerX - 1, centerY + 4, centerX + 1, centerY + 4, centerX + 3, centerY + 2);
        g2d.draw(mouth);

        // Draw antennae (animated)
        double antennaAngle = Math.sin(progress * Math.PI * 2) * 0.3;

        // Left antenna
        g2d.setColor(clawColor);
        g2d.setStroke(new BasicStroke(1.5f));
        double leftAntennaBaseX = centerX - 8;
        double leftAntennaBaseY = centerY - bodyHeight / 2 + 2;
        double leftAntennaTipX = leftAntennaBaseX - 5 * Math.cos(antennaAngle);
        double leftAntennaTipY = leftAntennaBaseY - 8;

        g2d.drawLine((int)leftAntennaBaseX, (int)leftAntennaBaseY,
                    (int)leftAntennaTipX, (int)leftAntennaTipY);
        g2d.fill(new Ellipse2D.Double(leftAntennaTipX - 1.5, leftAntennaTipY - 1.5, 3, 3));

        // Right antenna
        double rightAntennaBaseX = centerX + 8;
        double rightAntennaBaseY = centerY - bodyHeight / 2 + 2;
        double rightAntennaTipX = rightAntennaBaseX + 5 * Math.cos(antennaAngle);
        double rightAntennaTipY = rightAntennaBaseY - 8;

        g2d.drawLine((int)rightAntennaBaseX, (int)rightAntennaBaseY,
                    (int)rightAntennaTipX, (int)rightAntennaTipY);
        g2d.fill(new Ellipse2D.Double(rightAntennaTipX - 1.5, rightAntennaTipY - 1.5, 3, 3));

        // Draw clawed arms (animated)
        double armSwing = Math.sin(progress * Math.PI * 2) * 0.2;

        // Left arm
        g2d.setColor(clawColor);
        g2d.setStroke(new BasicStroke(2.0f));
        double leftArmBaseX = centerX - bodyWidth * 0.4;
        double leftArmBaseY = centerY;
        double leftArmEndX = leftArmBaseX - 8;
        double leftArmEndY = leftArmBaseY + 5 + armSwing * 5;

        g2d.drawLine((int)leftArmBaseX, (int)leftArmBaseY,
                    (int)leftArmEndX, (int)leftArmEndY);

        // Left claw
        g2d.drawLine((int)leftArmEndX, (int)leftArmEndY,
                    (int)(leftArmEndX - 3), (int)(leftArmEndY + 3));
        g2d.drawLine((int)leftArmEndX, (int)leftArmEndY,
                    (int)(leftArmEndX - 3), (int)(leftArmEndY - 3));

        // Right arm
        double rightArmBaseX = centerX + bodyWidth * 0.4;
        double rightArmBaseY = centerY;
        double rightArmEndX = rightArmBaseX + 8;
        double rightArmEndY = rightArmBaseY + 5 - armSwing * 5;

        g2d.drawLine((int)rightArmBaseX, (int)rightArmBaseY,
                    (int)rightArmEndX, (int)rightArmEndY);

        // Right claw
        g2d.drawLine((int)rightArmEndX, (int)rightArmEndY,
                    (int)(rightArmEndX + 3), (int)(rightArmEndY + 3));
        g2d.drawLine((int)rightArmEndX, (int)rightArmEndY,
                    (int)(rightArmEndX + 3), (int)(rightArmEndY - 3));

        // Draw body outline
        g2d.setColor(darkBodyColor.darker());
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(body);

        g2d.dispose();
        return image;
    }
}
