package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExplosionSpriteGeneratorSimple {
    private static final int FRAME_SIZE = 120;
    private static final int NUM_FRAMES = 32;
    private static final int FRAMES_PER_ROW = 8;
    private static final Random random = new Random(42);

    // Particle system
    private static class Particle {
        double startX, startY;
        double angle;
        double speed;
        double size;
        int birthFrame;
        Color color;

        Particle(double startX, double startY, double angle, double speed, double size, int birthFrame, Color color) {
            this.startX = startX;
            this.startY = startY;
            this.angle = angle;
            this.speed = speed;
            this.size = size;
            this.birthFrame = birthFrame;
            this.color = color;
        }

        void draw(Graphics2D g2d, int currentFrame, double centerX, double centerY, float globalOpacity) {
            if (currentFrame < birthFrame) return;

            int age = currentFrame - birthFrame;
            double maxAge = NUM_FRAMES - birthFrame;
            double ageProgress = age / maxAge;

            // Position based on velocity
            double distance = speed * age;
            double x = centerX + startX + Math.cos(angle) * distance;
            double y = centerY + startY + Math.sin(angle) * distance;

            // Size grows slightly then shrinks
            double particleSize = size * (1.0 + ageProgress * 0.5) * (1.0 - ageProgress * 0.8);

            // Fade out over time
            float opacity = globalOpacity * (float) (1.0 - ageProgress);

            if (opacity > 0 && particleSize > 0) {
                Color particleColor = new Color(
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue(),
                    Math.max(0, Math.min(255, (int) (opacity * 255)))
                );
                g2d.setColor(particleColor);
                g2d.fill(new Ellipse2D.Double(x - particleSize / 2, y - particleSize / 2, particleSize, particleSize));
            }
        }
    }

    public static void main(String[] args) {
        try {
            generateExplosionSpriteSheet();
            System.out.println("Successfully generated explosion sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateExplosionSpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        // Generate particles once for consistency across frames
        List<Particle> particles = generateParticles();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateExplosionFrame(frame, particles);

            int col = frame % FRAMES_PER_ROW;
            int row = frame / FRAMES_PER_ROW;
            int x = col * FRAME_SIZE;
            int y = row * FRAME_SIZE;

            sheetG2d.drawImage(frameImage, x, y, null);
            System.out.println("Generated frame " + frame + "/" + NUM_FRAMES);
        }

        sheetG2d.dispose();

        String outputDir = "src/main/resources/images";
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File outputFile = new File(outputDir, "explosion_sheet.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static List<Particle> generateParticles() {
        List<Particle> particles = new ArrayList<>();

        // Generate spray particles
        int numParticles = 60;
        for (int i = 0; i < numParticles; i++) {
            double angle = (i / (double) numParticles) * Math.PI * 2;
            double speed = 1.5 + random.nextDouble() * 2.5; // pixels per frame
            double startDistance = 15 + random.nextDouble() * 10;
            double startX = Math.cos(angle) * startDistance;
            double startY = Math.sin(angle) * startDistance;
            double size = 3 + random.nextDouble() * 5;

            // Birth frame varies - earlier particles spawn in first few frames
            int birthFrame = (int) (random.nextDouble() * 8);

            // Color variations
            Color color;
            double colorRand = random.nextDouble();
            if (colorRand < 0.3) {
                color = new Color(255, 255, 100); // Bright yellow
            } else if (colorRand < 0.6) {
                color = new Color(255, 150, 50); // Orange
            } else {
                color = new Color(255, 100, 50); // Red-orange
            }

            particles.add(new Particle(startX, startY, angle, speed, size, birthFrame, color));
        }

        return particles;
    }

    private static BufferedImage generateExplosionFrame(int frameIndex, List<Particle> particles) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;
        double progress = frameIndex / (double) (NUM_FRAMES - 1);

        double maxRadius = FRAME_SIZE * 0.35;
        double currentRadius;
        float opacity;

        if (progress < 0.3) {
            // Fast expansion phase
            double phase1Progress = progress / 0.3;
            currentRadius = maxRadius * phase1Progress;
            opacity = (float) (0.4 + 0.6 * phase1Progress);
        } else if (progress < 0.5) {
            // Peak brightness phase
            currentRadius = maxRadius;
            opacity = 1.0f;
        } else {
            // Fading phase
            double phase3Progress = (progress - 0.5) / 0.5;
            currentRadius = maxRadius * (1.0 + phase3Progress * 0.3);
            opacity = (float) (1.0 - phase3Progress);
        }

        // Draw spray particles first (behind core)
        for (Particle particle : particles) {
            particle.draw(g2d, frameIndex, centerX, centerY, opacity);
        }

        // Draw core explosion with multiple layers
        int numLayers = 6;
        for (int layer = 0; layer < numLayers; layer++) {
            double layerProgress = layer / (double) numLayers;
            double layerRadius = currentRadius * (0.4 + layerProgress * 0.6);
            float layerOpacity = opacity * (float) (1.0 - layerProgress * 0.6);

            Color coreColor;
            if (layerProgress < 0.25) {
                // White hot core
                coreColor = new Color(255, 255, 255);
            } else if (layerProgress < 0.5) {
                // Yellow
                double t = (layerProgress - 0.25) / 0.25;
                coreColor = new Color(255, 255, (int) (255 * (1 - t)));
            } else if (layerProgress < 0.75) {
                // Orange
                double t = (layerProgress - 0.5) / 0.25;
                coreColor = new Color(255, (int) (200 - t * 100), 0);
            } else {
                // Red
                double t = (layerProgress - 0.75) / 0.25;
                coreColor = new Color((int) (255 - t * 55), (int) (100 - t * 100), 0);
            }

            // Create smooth radial gradient effect
            int steps = 15;
            for (int step = 0; step < steps; step++) {
                double stepProgress = step / (double) steps;
                double stepRadius = layerRadius * (1.0 - stepProgress);
                float stepOpacity = layerOpacity * (1.0f - (float) Math.pow(stepProgress, 1.5));

                if (stepOpacity > 0) {
                    Color stepColor = new Color(
                        coreColor.getRed(),
                        coreColor.getGreen(),
                        coreColor.getBlue(),
                        Math.max(0, Math.min(255, (int) (stepOpacity * 255)))
                    );
                    g2d.setColor(stepColor);
                    g2d.fill(new Ellipse2D.Double(
                        centerX - stepRadius, centerY - stepRadius,
                        stepRadius * 2, stepRadius * 2
                    ));
                }
            }
        }

        g2d.dispose();
        return image;
    }
}
