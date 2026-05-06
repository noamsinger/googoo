package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TeethEatingSpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateTeethEatingSpriteSheet();
            System.out.println("Successfully generated teeth-eating sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateTeethEatingSpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateTeethEatingFrame(frame);

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
        System.out.println("Teeth-eating sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateTeethEatingFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for teeth with red gums
        Color gumsColor = new Color(220, 50, 60); // Bright red gums
        Color darkGumsColor = new Color(180, 30, 40); // Darker red for depth
        Color teethColor = new Color(255, 255, 255); // Pure white teeth
        Color teethShadowColor = new Color(220, 220, 230); // Slight gray for tooth shading
        Color throatColor = new Color(100, 20, 30); // Dark red throat interior

        // Mouth opening/closing animation - snapping bite motion
        double mouthOpen = Math.abs(Math.sin(progress * Math.PI * 2));
        double maxMouthOpen = 25; // Maximum mouth opening
        double currentOpen = mouthOpen * maxMouthOpen;

        // Draw throat/interior (dark red background)
        if (currentOpen > 2) {
            double throatWidth = 30;
            double throatHeight = currentOpen;

            g2d.setColor(throatColor);
            g2d.fill(new Ellipse2D.Double(centerX - throatWidth / 2, centerY - throatHeight / 2,
                                           throatWidth, throatHeight));
        }

        // Draw upper gums
        Path2D upperGums = new Path2D.Double();
        double gumsWidth = 35;
        double upperGumsY = centerY - currentOpen / 2;

        upperGums.moveTo(centerX - gumsWidth / 2, upperGumsY);
        upperGums.curveTo(
            centerX - gumsWidth / 2, upperGumsY - 8,
            centerX - gumsWidth / 3, upperGumsY - 12,
            centerX, upperGumsY - 12
        );
        upperGums.curveTo(
            centerX + gumsWidth / 3, upperGumsY - 12,
            centerX + gumsWidth / 2, upperGumsY - 8,
            centerX + gumsWidth / 2, upperGumsY
        );
        upperGums.lineTo(centerX + gumsWidth / 2 - 2, upperGumsY + 3);
        upperGums.lineTo(centerX - gumsWidth / 2 + 2, upperGumsY + 3);
        upperGums.closePath();

        // Gradient for gums
        GradientPaint upperGumsGradient = new GradientPaint(
            (float)centerX, (float)(upperGumsY - 12), gumsColor,
            (float)centerX, (float)(upperGumsY + 3), darkGumsColor
        );
        g2d.setPaint(upperGumsGradient);
        g2d.fill(upperGums);

        // Draw lower gums
        Path2D lowerGums = new Path2D.Double();
        double lowerGumsY = centerY + currentOpen / 2;

        lowerGums.moveTo(centerX - gumsWidth / 2, lowerGumsY);
        lowerGums.curveTo(
            centerX - gumsWidth / 2, lowerGumsY + 8,
            centerX - gumsWidth / 3, lowerGumsY + 12,
            centerX, lowerGumsY + 12
        );
        lowerGums.curveTo(
            centerX + gumsWidth / 3, lowerGumsY + 12,
            centerX + gumsWidth / 2, lowerGumsY + 8,
            centerX + gumsWidth / 2, lowerGumsY
        );
        lowerGums.lineTo(centerX + gumsWidth / 2 - 2, lowerGumsY - 3);
        lowerGums.lineTo(centerX - gumsWidth / 2 + 2, lowerGumsY - 3);
        lowerGums.closePath();

        // Gradient for lower gums
        GradientPaint lowerGumsGradient = new GradientPaint(
            (float)centerX, (float)(lowerGumsY - 3), darkGumsColor,
            (float)centerX, (float)(lowerGumsY + 12), gumsColor
        );
        g2d.setPaint(lowerGumsGradient);
        g2d.fill(lowerGums);

        // Draw upper teeth
        int numTeeth = 10;
        double teethAreaWidth = gumsWidth - 6;

        for (int i = 0; i < numTeeth; i++) {
            double toothX = centerX - teethAreaWidth / 2 + (i * teethAreaWidth / (numTeeth - 1));
            double toothWidth = 3.5;
            double toothHeight = 6 + (i % 2) * 1.5; // Vary tooth height slightly

            Path2D tooth = new Path2D.Double();
            tooth.moveTo(toothX - toothWidth / 2, upperGumsY);
            // Rounded tooth bottom
            tooth.curveTo(
                toothX - toothWidth / 2, upperGumsY + toothHeight * 0.7,
                toothX - toothWidth / 3, upperGumsY + toothHeight,
                toothX, upperGumsY + toothHeight
            );
            tooth.curveTo(
                toothX + toothWidth / 3, upperGumsY + toothHeight,
                toothX + toothWidth / 2, upperGumsY + toothHeight * 0.7,
                toothX + toothWidth / 2, upperGumsY
            );
            tooth.closePath();

            g2d.setColor(teethColor);
            g2d.fill(tooth);

            // Tooth shading (left side)
            g2d.setColor(teethShadowColor);
            Path2D toothShade = new Path2D.Double();
            toothShade.moveTo(toothX - toothWidth / 2, upperGumsY);
            toothShade.lineTo(toothX - toothWidth / 4, upperGumsY);
            toothShade.curveTo(
                toothX - toothWidth / 4, upperGumsY + toothHeight * 0.7,
                toothX - toothWidth / 6, upperGumsY + toothHeight,
                toothX, upperGumsY + toothHeight
            );
            toothShade.curveTo(
                toothX - toothWidth / 3, upperGumsY + toothHeight,
                toothX - toothWidth / 2, upperGumsY + toothHeight * 0.7,
                toothX - toothWidth / 2, upperGumsY
            );
            toothShade.closePath();
            g2d.fill(toothShade);

            // Tooth outline
            g2d.setColor(new Color(200, 200, 210));
            g2d.setStroke(new BasicStroke(0.8f));
            g2d.draw(tooth);
        }

        // Draw lower teeth
        for (int i = 0; i < numTeeth; i++) {
            double toothX = centerX - teethAreaWidth / 2 + (i * teethAreaWidth / (numTeeth - 1));
            double toothWidth = 3.5;
            double toothHeight = 6 + ((i + 1) % 2) * 1.5; // Vary tooth height (offset from upper)

            Path2D tooth = new Path2D.Double();
            tooth.moveTo(toothX - toothWidth / 2, lowerGumsY);
            // Rounded tooth top
            tooth.curveTo(
                toothX - toothWidth / 2, lowerGumsY - toothHeight * 0.7,
                toothX - toothWidth / 3, lowerGumsY - toothHeight,
                toothX, lowerGumsY - toothHeight
            );
            tooth.curveTo(
                toothX + toothWidth / 3, lowerGumsY - toothHeight,
                toothX + toothWidth / 2, lowerGumsY - toothHeight * 0.7,
                toothX + toothWidth / 2, lowerGumsY
            );
            tooth.closePath();

            g2d.setColor(teethColor);
            g2d.fill(tooth);

            // Tooth shading (left side)
            g2d.setColor(teethShadowColor);
            Path2D toothShade = new Path2D.Double();
            toothShade.moveTo(toothX - toothWidth / 2, lowerGumsY);
            toothShade.lineTo(toothX - toothWidth / 4, lowerGumsY);
            toothShade.curveTo(
                toothX - toothWidth / 4, lowerGumsY - toothHeight * 0.7,
                toothX - toothWidth / 6, lowerGumsY - toothHeight,
                toothX, lowerGumsY - toothHeight
            );
            toothShade.curveTo(
                toothX - toothWidth / 3, lowerGumsY - toothHeight,
                toothX - toothWidth / 2, lowerGumsY - toothHeight * 0.7,
                toothX - toothWidth / 2, lowerGumsY
            );
            toothShade.closePath();
            g2d.fill(toothShade);

            // Tooth outline
            g2d.setColor(new Color(200, 200, 210));
            g2d.setStroke(new BasicStroke(0.8f));
            g2d.draw(tooth);
        }

        // Draw gums outlines for definition
        g2d.setColor(darkGumsColor.darker());
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(upperGums);
        g2d.draw(lowerGums);

        // Add gum texture/blood vessels
        g2d.setColor(new Color(180, 40, 50, 150));
        g2d.setStroke(new BasicStroke(0.8f));
        for (int i = 0; i < 5; i++) {
            double veinX = centerX - 12 + i * 6;
            g2d.drawLine((int)veinX, (int)(upperGumsY - 8), (int)(veinX + 2), (int)(upperGumsY - 2));
            g2d.drawLine((int)veinX, (int)(lowerGumsY + 8), (int)(veinX + 2), (int)(lowerGumsY + 2));
        }

        g2d.dispose();
        return image;
    }
}
