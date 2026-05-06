package com.game.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BlackHoleSpriteGenerator {
    private static final int SPRITE_SIZE = 120;
    private static final int FRAMES = 16;
    private static final int COLS = 4;
    private static final int ROWS = 4;

    public static void main(String[] args) {
        javafx.application.Platform.startup(() -> {});

        if (args.length > 0) {
            // Generate single color scheme specified by argument
            int colorScheme = Integer.parseInt(args[0]);
            generateBlackHoleSpriteSheet(colorScheme);
        } else {
            // Generate all 16 color schemes
            for (int i = 0; i < 16; i++) {
                generateBlackHoleSpriteSheet(i);
            }
        }

        javafx.application.Platform.exit();
    }

    public static void generateBlackHoleSpriteSheet(int colorScheme) {
        int sheetWidth = SPRITE_SIZE * COLS;
        int sheetHeight = SPRITE_SIZE * ROWS;
        WritableImage spriteSheet = new WritableImage(sheetWidth, sheetHeight);
        PixelWriter pw = spriteSheet.getPixelWriter();

        for (int i = 0; i < FRAMES; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int offsetX = col * SPRITE_SIZE;
            int offsetY = row * SPRITE_SIZE;

            double phase = (i / (double) FRAMES) * 2 * Math.PI;
            WritableImage frame = createBlackHoleFrame(phase, colorScheme);

            // Copy frame to sprite sheet
            for (int y = 0; y < SPRITE_SIZE; y++) {
                for (int x = 0; x < SPRITE_SIZE; x++) {
                    Color pixel = frame.getPixelReader().getColor(x, y);
                    pw.setColor(offsetX + x, offsetY + y, pixel);
                }
            }
        }

        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(spriteSheet, null);

        try {
            File outputFile = new File("src/main/resources/images/blackhole_sheet_" + colorScheme + ".png");
            outputFile.getParentFile().mkdirs();
            ImageIO.write(bufferedImage, "png", outputFile);
            System.out.println("Black hole sprite sheet " + colorScheme + " saved to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving sprite sheet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static WritableImage createBlackHoleFrame(double phase, int colorScheme) {
        WritableImage image = new WritableImage(SPRITE_SIZE, SPRITE_SIZE);
        PixelWriter pw = image.getPixelWriter();

        int centerX = SPRITE_SIZE / 2;
        int centerY = SPRITE_SIZE / 2;
        double maxRadius = SPRITE_SIZE / 2.0 - 5;

        // Draw swirling accretion disk
        for (int y = 0; y < SPRITE_SIZE; y++) {
            for (int x = 0; x < SPRITE_SIZE; x++) {
                double dx = x - centerX;
                double dy = y - centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);
                double angle = Math.atan2(dy, dx);

                if (distance > 2 && distance < maxRadius) {
                    // Create swirling pattern
                    double normalizedDist = distance / maxRadius;
                    double spiral = angle + (1.0 - normalizedDist) * 4 * Math.PI + phase;

                    // Create bands in the accretion disk
                    double band = Math.sin(spiral * 3 + distance * 0.3);

                    // Brightness falls off with distance and varies with spiral
                    double brightness = (1.0 - normalizedDist * normalizedDist) * (0.5 + 0.5 * band);
                    brightness = Math.max(0, Math.min(1, brightness));

                    if (brightness > 0.1) {
                        // 16 combinations: 4 inner arm colors × 4 outer arm colors
                        // Inner arms (center): white, yellow, red, orange
                        // Outer arms (edge): blue, green, purple, gray

                        int innerColorIndex = colorScheme / 4; // 0-3
                        int outerColorIndex = colorScheme % 4; // 0-3

                        double r, g, b;

                        if (normalizedDist < 0.2) {
                            // Inner arms - close to center (half as large)
                            double innerBlend = normalizedDist / 0.2; // 0 at center, 1 at 0.2

                            switch (innerColorIndex) {
                                case 0: // White
                                    r = 1.0;
                                    g = 1.0;
                                    b = 1.0;
                                    break;
                                case 1: // Yellow
                                    r = 1.0;
                                    g = 1.0;
                                    b = 0.2 + 0.5 * innerBlend;
                                    break;
                                case 2: // Red
                                    r = 1.0;
                                    g = 0.2 * innerBlend;
                                    b = 0.1 * innerBlend;
                                    break;
                                case 3: // Orange
                                default:
                                    r = 1.0;
                                    g = 0.5 + 0.3 * innerBlend;
                                    b = 0.1 * innerBlend;
                                    break;
                            }
                        } else {
                            // Outer arms - far from center
                            // Blend from inner color (at 0.2) to outer color (at 1.0)
                            double outerBlend = (normalizedDist - 0.2) / 0.8; // 0 at 0.2, 1 at 1.0

                            // Get inner color at boundary
                            double innerR, innerG, innerB;
                            switch (innerColorIndex) {
                                case 0: // White
                                    innerR = 1.0; innerG = 1.0; innerB = 1.0;
                                    break;
                                case 1: // Yellow
                                    innerR = 1.0; innerG = 1.0; innerB = 0.7;
                                    break;
                                case 2: // Red
                                    innerR = 1.0; innerG = 0.2; innerB = 0.1;
                                    break;
                                case 3: // Orange
                                default:
                                    innerR = 1.0; innerG = 0.8; innerB = 0.1;
                                    break;
                            }

                            // Get outer color
                            double outerR, outerG, outerB;
                            switch (outerColorIndex) {
                                case 0: // Blue
                                    outerR = 0.1; outerG = 0.3; outerB = 1.0;
                                    break;
                                case 1: // Green
                                    outerR = 0.1; outerG = 1.0; outerB = 0.3;
                                    break;
                                case 2: // Purple
                                    outerR = 0.7; outerG = 0.1; outerB = 1.0;
                                    break;
                                case 3: // Gray
                                default:
                                    outerR = 0.5; outerG = 0.5; outerB = 0.6;
                                    break;
                            }

                            // Blend from inner to outer
                            r = innerR * (1.0 - outerBlend) + outerR * outerBlend;
                            g = innerG * (1.0 - outerBlend) + outerG * outerBlend;
                            b = innerB * (1.0 - outerBlend) + outerB * outerBlend;
                        }

                        Color color = Color.color(r, g, b, brightness);
                        pw.setColor(x, y, color);
                    }
                } else if (distance <= 2) {
                    // Event horizon - very dark with slight glow edge
                    if (distance > 1) {
                        double edge = (distance - 1.0);
                        pw.setColor(x, y, Color.color(0.1, 0.05, 0.2, edge));
                    } else {
                        pw.setColor(x, y, Color.color(0, 0, 0, 0.8));
                    }
                }
            }
        }

        // Add some bright spots for stars being consumed
        for (int i = 0; i < 3; i++) {
            double spotAngle = phase + i * Math.PI * 2.0 / 3.0;
            double spotRadius = 25 + 15 * Math.sin(phase * 2 + i);
            int spotX = centerX + (int)(Math.cos(spotAngle) * spotRadius);
            int spotY = centerY + (int)(Math.sin(spotAngle) * spotRadius);

            // Draw bright spot
            for (int dy = -3; dy <= 3; dy++) {
                for (int dx = -3; dx <= 3; dx++) {
                    if (dx * dx + dy * dy <= 9) {
                        int px = spotX + dx;
                        int py = spotY + dy;
                        if (px >= 0 && px < SPRITE_SIZE && py >= 0 && py < SPRITE_SIZE) {
                            double dist = Math.sqrt(dx * dx + dy * dy);
                            double intensity = 1.0 - dist / 3.0;
                            Color current = image.getPixelReader().getColor(px, py);
                            Color bright = Color.color(
                                Math.min(1.0, current.getRed() + intensity * 0.8),
                                Math.min(1.0, current.getGreen() + intensity * 0.8),
                                Math.min(1.0, current.getBlue() + intensity * 0.5),
                                Math.max(current.getOpacity(), intensity)
                            );
                            pw.setColor(px, py, bright);
                        }
                    }
                }
            }
        }

        return image;
    }
}
