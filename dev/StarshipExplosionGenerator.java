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

public class StarshipExplosionGenerator {
    private static final int FRAME_SIZE = 300; // Much bigger than enemy explosion
    private static final int NUM_FRAMES = 256;
    private static final int FRAMES_PER_ROW = 16;
    private static final Random random = new Random(123);

    private static class Particle {
        double startX, startY;
        double angle;
        double speed;
        double size;
        int birthFrame;
        Color color;
        double rotation;
        double rotationSpeed;

        Particle(double startX, double startY, double angle, double speed, double size, int birthFrame, Color color) {
            this.startX = startX;
            this.startY = startY;
            this.angle = angle;
            this.speed = speed;
            this.size = size;
            this.birthFrame = birthFrame;
            this.color = color;
            this.rotation = random.nextDouble() * Math.PI * 2;
            this.rotationSpeed = (random.nextDouble() - 0.5) * 0.2;
        }

        void draw(Graphics2D g2d, int currentFrame, double centerX, double centerY, float globalOpacity) {
            if (currentFrame < birthFrame) return;

            int age = currentFrame - birthFrame;
            double maxAge = NUM_FRAMES - birthFrame;
            double ageProgress = age / maxAge;

            double distance = speed * age;
            double x = centerX + startX + Math.cos(angle) * distance;
            double y = centerY + startY + Math.sin(angle) * distance;

            double particleSize = size * (1.0 + ageProgress * 0.3) * (1.0 - ageProgress * 0.7);
            float opacity = globalOpacity * (float) (1.0 - ageProgress);

            if (opacity > 0 && particleSize > 0.5) {
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

    private static class SecondaryExplosion {
        double x, y;
        int birthFrame;
        double maxRadius;

        SecondaryExplosion(double x, double y, int birthFrame, double maxRadius) {
            this.x = x;
            this.y = y;
            this.birthFrame = birthFrame;
            this.maxRadius = maxRadius;
        }

        void draw(Graphics2D g2d, int currentFrame, double centerX, double centerY) {
            if (currentFrame < birthFrame) return;

            int age = currentFrame - birthFrame;
            int duration = 40; // Secondary explosions last 40 frames
            if (age > duration) return;

            double progress = age / (double) duration;
            double radius = maxRadius * progress;
            float opacity = (float) (1.0 - progress);

            // Draw explosion flash
            int layers = 5;
            for (int layer = 0; layer < layers; layer++) {
                double layerProgress = layer / (double) layers;
                double layerRadius = radius * (0.5 + layerProgress * 0.5);
                double layerOpacity = opacity * (1.0 - layerProgress * 0.6);

                Color color;
                if (layerProgress < 0.3) {
                    color = new Color(255, 255, 200);
                } else if (layerProgress < 0.6) {
                    color = new Color(255, 200, 100);
                } else {
                    color = new Color(255, 150, 50);
                }

                int steps = 8;
                for (int step = 0; step < steps; step++) {
                    double stepProgress = step / (double) steps;
                    double stepRadius = layerRadius * (1.0 - stepProgress);
                    double stepOpacity = layerOpacity * (1.0 - Math.pow(stepProgress, 1.5));

                    Color stepColor = new Color(
                        color.getRed(),
                        color.getGreen(),
                        color.getBlue(),
                        Math.max(0, Math.min(255, (int) (stepOpacity * 255)))
                    );
                    g2d.setColor(stepColor);
                    g2d.fill(new Ellipse2D.Double(
                        centerX + x - stepRadius, centerY + y - stepRadius,
                        stepRadius * 2, stepRadius * 2
                    ));
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            generateStarshipExplosionSheet();
            System.out.println("Successfully generated starship explosion sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateStarshipExplosionSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        List<Particle> particles = generateStarshipParticles();
        List<SecondaryExplosion> secondaries = generateSecondaryExplosions();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateStarshipExplosionFrame(frame, particles, secondaries);

            int col = frame % FRAMES_PER_ROW;
            int row = frame / FRAMES_PER_ROW;
            int x = col * FRAME_SIZE;
            int y = row * FRAME_SIZE;

            sheetG2d.drawImage(frameImage, x, y, null);

            if (frame % 16 == 0) {
                System.out.println("Generated frame " + frame + "/" + NUM_FRAMES);
            }
        }

        sheetG2d.dispose();

        String outputDir = "src/main/resources/images";
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File outputFile = new File(outputDir, "starship_explosion_sheet.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("Starship explosion sheet saved: " + outputFile.getAbsolutePath());
    }

    private static List<Particle> generateStarshipParticles() {
        List<Particle> particles = new ArrayList<>();

        // Many more particles, various sizes and colors
        int numParticles = 300;
        for (int i = 0; i < numParticles; i++) {
            double angle = (i / (double) numParticles) * Math.PI * 2 + random.nextDouble() * 0.1;
            double speed = 0.5 + random.nextDouble() * 2.0;
            double startDistance = 20 + random.nextDouble() * 40;
            double startX = Math.cos(angle) * startDistance;
            double startY = Math.sin(angle) * startDistance;
            double size = 4 + random.nextDouble() * 12;

            int birthFrame = (int) (random.nextDouble() * 30);

            // Varied colors
            Color color;
            double colorRand = random.nextDouble();
            if (colorRand < 0.15) {
                color = new Color(255, 255, 255); // White hot
            } else if (colorRand < 0.3) {
                color = new Color(255, 255, 150); // Bright yellow
            } else if (colorRand < 0.5) {
                color = new Color(255, 200, 100); // Yellow-orange
            } else if (colorRand < 0.7) {
                color = new Color(255, 150, 50); // Orange
            } else if (colorRand < 0.85) {
                color = new Color(255, 100, 50); // Red-orange
            } else {
                color = new Color(200, 50, 50); // Dark red
            }

            particles.add(new Particle(startX, startY, angle, speed, size, birthFrame, color));
        }

        return particles;
    }

    private static List<SecondaryExplosion> generateSecondaryExplosions() {
        List<SecondaryExplosion> secondaries = new ArrayList<>();

        // Create secondary explosions at random times and positions
        int numSecondaries = 20;
        for (int i = 0; i < numSecondaries; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = 30 + random.nextDouble() * 60;
            double x = Math.cos(angle) * distance;
            double y = Math.sin(angle) * distance;
            int birthFrame = 15 + (int) (random.nextDouble() * 100);
            double maxRadius = 25 + random.nextDouble() * 35;

            secondaries.add(new SecondaryExplosion(x, y, birthFrame, maxRadius));
        }

        return secondaries;
    }

    private static BufferedImage generateStarshipExplosionFrame(int frameIndex, List<Particle> particles, List<SecondaryExplosion> secondaries) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;
        double progress = frameIndex / (double) (NUM_FRAMES - 1);

        double maxRadius = FRAME_SIZE * 0.4;
        double currentRadius;
        float opacity;

        if (progress < 0.15) {
            // Initial flash
            double phase1Progress = progress / 0.15;
            currentRadius = maxRadius * phase1Progress * 0.6;
            opacity = (float) (0.3 + 0.7 * phase1Progress);
        } else if (progress < 0.35) {
            // Main expansion
            double phase2Progress = (progress - 0.15) / 0.2;
            currentRadius = maxRadius * (0.6 + phase2Progress * 0.4);
            opacity = 1.0f;
        } else if (progress < 0.5) {
            // Peak
            currentRadius = maxRadius;
            opacity = 1.0f;
        } else {
            // Long fade out
            double phase4Progress = (progress - 0.5) / 0.5;
            currentRadius = maxRadius * (1.0 + phase4Progress * 0.3);
            opacity = (float) (1.0 - phase4Progress);
        }

        // Draw spray particles
        for (Particle particle : particles) {
            particle.draw(g2d, frameIndex, centerX, centerY, opacity);
        }

        // Draw secondary explosions
        for (SecondaryExplosion secondary : secondaries) {
            secondary.draw(g2d, frameIndex, centerX, centerY);
        }

        // Color morphing: smooth transition every ~20 frames through red -> yellow -> orange -> white
        double colorCycleProgress = (frameIndex % 80) / 80.0; // 80 frames = 4 colors * 20 frames each
        int colorPhase = (int) (colorCycleProgress * 4); // Which color pair we're transitioning between
        double morphProgress = (colorCycleProgress * 4) - colorPhase; // 0.0 to 1.0 within current transition

        // Define the 4 base colors
        Color[] baseColors = new Color[4];
        baseColors[0] = new Color(255, 50, 50);    // Red
        baseColors[1] = new Color(255, 255, 100);  // Yellow
        baseColors[2] = new Color(255, 150, 50);   // Orange
        baseColors[3] = new Color(255, 255, 255);  // White

        // Get current and next color in cycle
        Color currentCycleColor = baseColors[colorPhase];
        Color nextCycleColor = baseColors[(colorPhase + 1) % 4];

        // Draw main core explosion
        int numLayers = 8;
        for (int layer = 0; layer < numLayers; layer++) {
            double layerProgress = layer / (double) numLayers;
            double layerRadius = currentRadius * (0.3 + layerProgress * 0.7);
            double layerOpacity = opacity * (1.0 - layerProgress * 0.5);

            // Interpolate between current and next color based on morphProgress
            int r = (int) (currentCycleColor.getRed() + morphProgress * (nextCycleColor.getRed() - currentCycleColor.getRed()));
            int g = (int) (currentCycleColor.getGreen() + morphProgress * (nextCycleColor.getGreen() - currentCycleColor.getGreen()));
            int b = (int) (currentCycleColor.getBlue() + morphProgress * (nextCycleColor.getBlue() - currentCycleColor.getBlue()));

            Color coreColor;
            if (layerProgress < 0.3) {
                // Inner core - use morphed color
                coreColor = new Color(r, g, b);
            } else if (layerProgress < 0.6) {
                // Middle layer - blend to darker
                double t = (layerProgress - 0.3) / 0.3;
                coreColor = new Color(
                    (int) (r * (1.0 - t * 0.2)),
                    (int) (g * (1.0 - t * 0.3)),
                    (int) (b * (1.0 - t * 0.5))
                );
            } else {
                // Outer layer - darker still
                double t = (layerProgress - 0.6) / 0.4;
                coreColor = new Color(
                    (int) (r * (0.8 - t * 0.2)),
                    (int) (g * (0.7 - t * 0.5)),
                    Math.max(0, (int) (b * (0.5 - t * 0.5)))
                );
            }

            int steps = 12;
            for (int step = 0; step < steps; step++) {
                double stepProgress = step / (double) steps;
                double stepRadius = layerRadius * (1.0 - stepProgress);
                double stepOpacity = layerOpacity * (1.0 - Math.pow(stepProgress, 1.3));

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
