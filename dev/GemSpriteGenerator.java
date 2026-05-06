package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GemSpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid
    private static final int NUM_GEMS = 16;

    // Vivid color schemes for 16 different gems
    private static final Color[][] GEM_COLORS = {
        // 0: Ruby - Red
        {new Color(220, 20, 60), new Color(180, 0, 40), new Color(255, 100, 130)},
        // 1: Sapphire - Blue
        {new Color(15, 82, 186), new Color(8, 37, 103), new Color(100, 149, 237)},
        // 2: Emerald - Green
        {new Color(0, 201, 87), new Color(0, 120, 50), new Color(80, 255, 150)},
        // 3: Amethyst - Purple
        {new Color(153, 50, 204), new Color(75, 0, 130), new Color(218, 112, 214)},
        // 4: Topaz - Orange
        {new Color(255, 140, 0), new Color(180, 80, 0), new Color(255, 200, 100)},
        // 5: Aquamarine - Cyan
        {new Color(0, 255, 255), new Color(0, 139, 139), new Color(175, 238, 238)},
        // 6: Citrine - Yellow
        {new Color(255, 215, 0), new Color(184, 134, 11), new Color(255, 255, 150)},
        // 7: Garnet - Dark Red
        {new Color(128, 0, 32), new Color(80, 0, 20), new Color(200, 50, 100)},
        // 8: Peridot - Lime Green
        {new Color(154, 205, 50), new Color(85, 107, 47), new Color(220, 255, 150)},
        // 9: Tanzanite - Blue-Purple
        {new Color(64, 50, 148), new Color(35, 25, 90), new Color(138, 120, 220)},
        // 10: Tourmaline - Pink
        {new Color(255, 20, 147), new Color(199, 21, 133), new Color(255, 182, 193)},
        // 11: Opal - Rainbow (White with colors)
        {new Color(240, 240, 255), new Color(180, 180, 200), new Color(255, 255, 255)},
        // 12: Jade - Deep Green
        {new Color(0, 168, 107), new Color(0, 100, 60), new Color(100, 220, 160)},
        // 13: Amber - Golden
        {new Color(255, 191, 0), new Color(204, 119, 0), new Color(255, 230, 100)},
        // 14: Turquoise - Teal
        {new Color(64, 224, 208), new Color(32, 178, 170), new Color(175, 238, 238)},
        // 15: Diamond - White/Clear
        {new Color(230, 230, 250), new Color(176, 196, 222), new Color(255, 255, 255)}
    };

    private static final String[] GEM_NAMES = {
        "Ruby", "Sapphire", "Emerald", "Amethyst", "Topaz", "Aquamarine",
        "Citrine", "Garnet", "Peridot", "Tanzanite", "Tourmaline", "Opal",
        "Jade", "Amber", "Turquoise", "Diamond"
    };

    public static void main(String[] args) {
        try {
            for (int gemIndex = 0; gemIndex < NUM_GEMS; gemIndex++) {
                generateGemSpriteSheet(gemIndex);
                System.out.println("Successfully generated " + GEM_NAMES[gemIndex] + " sprite sheet!");
            }
            System.out.println("All 16 gem sprite sheets completed!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateGemSpriteSheet(int gemIndex) throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateGemFrame(frame, gemIndex);

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

        File outputFile = new File(outputDir, "gem_sheet_" + gemIndex + ".png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
    }

    private static BufferedImage generateGemFrame(int frameIndex, int gemIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Get colors for this gem
        Color baseColor = GEM_COLORS[gemIndex][0];
        Color darkColor = GEM_COLORS[gemIndex][1];
        Color lightColor = GEM_COLORS[gemIndex][2];

        // Rotation animation
        double rotation = progress * Math.PI * 2;

        // Floating/bobbing animation
        double bob = Math.sin(progress * Math.PI * 2) * 3;

        // Pulsing glow
        double glow = 0.5 + Math.sin(progress * Math.PI * 2) * 0.5;

        // Apply rotation transform
        g2d.rotate(rotation, centerX, centerY);

        // Gem size
        double gemSize = 20;

        // Draw glow effect
        for (int i = 3; i > 0; i--) {
            g2d.setColor(new Color(
                lightColor.getRed(),
                lightColor.getGreen(),
                lightColor.getBlue(),
                (int)(30 * glow / i)
            ));
            double glowSize = gemSize + i * 8;
            Path2D glowGem = createOctagonGem(centerX, centerY + bob, glowSize);
            g2d.fill(glowGem);
        }

        // Draw main gem body (octagonal cut)
        Path2D gem = createOctagonGem(centerX, centerY + bob, gemSize);

        // Gradient fill
        GradientPaint gemGradient = new GradientPaint(
            (float)(centerX - gemSize), (float)(centerY - gemSize + bob), lightColor,
            (float)(centerX + gemSize), (float)(centerY + gemSize + bob), darkColor
        );
        g2d.setPaint(gemGradient);
        g2d.fill(gem);

        // Draw facets (internal structure)
        g2d.setColor(new Color(
            lightColor.getRed(),
            lightColor.getGreen(),
            lightColor.getBlue(),
            150
        ));
        g2d.setStroke(new BasicStroke(1.5f));

        // Top facets
        g2d.drawLine((int)centerX, (int)(centerY - gemSize + bob),
                    (int)(centerX - gemSize * 0.7), (int)(centerY + bob));
        g2d.drawLine((int)centerX, (int)(centerY - gemSize + bob),
                    (int)(centerX + gemSize * 0.7), (int)(centerY + bob));
        g2d.drawLine((int)centerX, (int)(centerY - gemSize + bob),
                    (int)centerX, (int)(centerY + gemSize * 0.5 + bob));

        // Side facets
        g2d.drawLine((int)(centerX - gemSize * 0.7), (int)(centerY - gemSize * 0.3 + bob),
                    (int)centerX, (int)(centerY + gemSize * 0.5 + bob));
        g2d.drawLine((int)(centerX + gemSize * 0.7), (int)(centerY - gemSize * 0.3 + bob),
                    (int)centerX, (int)(centerY + gemSize * 0.5 + bob));

        // Bottom facets
        g2d.drawLine((int)(centerX - gemSize * 0.5), (int)(centerY + gemSize * 0.7 + bob),
                    (int)centerX, (int)(centerY + gemSize * 0.5 + bob));
        g2d.drawLine((int)(centerX + gemSize * 0.5), (int)(centerY + gemSize * 0.7 + bob),
                    (int)centerX, (int)(centerY + gemSize * 0.5 + bob));

        // Draw top highlight facet (bright spot)
        Path2D topFacet = new Path2D.Double();
        topFacet.moveTo(centerX, centerY - gemSize + bob);
        topFacet.lineTo(centerX - gemSize * 0.4, centerY - gemSize * 0.4 + bob);
        topFacet.lineTo(centerX, centerY + bob);
        topFacet.lineTo(centerX + gemSize * 0.4, centerY - gemSize * 0.4 + bob);
        topFacet.closePath();

        g2d.setColor(new Color(
            lightColor.getRed(),
            lightColor.getGreen(),
            lightColor.getBlue(),
            180
        ));
        g2d.fill(topFacet);

        // Draw dark bottom facets for depth
        Path2D bottomFacet = new Path2D.Double();
        bottomFacet.moveTo(centerX, centerY + gemSize * 0.5 + bob);
        bottomFacet.lineTo(centerX - gemSize * 0.5, centerY + gemSize * 0.7 + bob);
        bottomFacet.lineTo(centerX, centerY + gemSize + bob);
        bottomFacet.lineTo(centerX + gemSize * 0.5, centerY + gemSize * 0.7 + bob);
        bottomFacet.closePath();

        g2d.setColor(new Color(
            darkColor.getRed(),
            darkColor.getGreen(),
            darkColor.getBlue(),
            200
        ));
        g2d.fill(bottomFacet);

        // Draw outline
        g2d.setColor(darkColor.darker());
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(gem);

        // Add bright shine spots
        g2d.setColor(new Color(255, 255, 255, 220));
        double shineX = centerX - gemSize * 0.3;
        double shineY = centerY - gemSize * 0.6 + bob;
        g2d.fillOval((int)(shineX - 2), (int)(shineY - 2), 5, 4);

        // Smaller shine
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.fillOval((int)(centerX + gemSize * 0.2), (int)(centerY - gemSize * 0.3 + bob), 3, 2);

        // Special rainbow effect for Opal (gem 11)
        if (gemIndex == 11) {
            g2d.setStroke(new BasicStroke(1.0f));
            Color[] rainbowColors = {
                new Color(255, 100, 100, 100),
                new Color(255, 200, 100, 100),
                new Color(100, 255, 100, 100),
                new Color(100, 200, 255, 100),
                new Color(200, 100, 255, 100)
            };

            for (int i = 0; i < 5; i++) {
                g2d.setColor(rainbowColors[i]);
                double offset = (i - 2) * 3 + Math.sin(progress * Math.PI * 2 + i) * 2;
                g2d.drawLine((int)(centerX + offset - 5), (int)(centerY + bob),
                           (int)(centerX + offset + 5), (int)(centerY + bob));
            }
        }

        g2d.dispose();
        return image;
    }

    private static Path2D createOctagonGem(double centerX, double centerY, double size) {
        Path2D gem = new Path2D.Double();

        // Create an octagon (8-sided gem cut)
        int sides = 8;
        for (int i = 0; i < sides; i++) {
            double angle = (i / (double) sides) * Math.PI * 2 - Math.PI / 2;
            double x = centerX + Math.cos(angle) * size;
            double y = centerY + Math.sin(angle) * size;

            if (i == 0) {
                gem.moveTo(x, y);
            } else {
                gem.lineTo(x, y);
            }
        }
        gem.closePath();

        return gem;
    }
}
