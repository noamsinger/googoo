package com.game.util;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class ExplosionSpriteGenerator extends Application {
    private static final int FRAME_SIZE = 120; // Size of each frame
    private static final int NUM_FRAMES = 32;
    private static final Random random = new Random(42); // Fixed seed for consistency

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            generateExplosionFrames();
            System.out.println("Successfully generated " + NUM_FRAMES + " explosion frames!");
            Platform.exit();
        } catch (IOException e) {
            System.err.println("Error generating explosion frames: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
        }
    }

    private static void generateExplosionFrames() throws IOException {
        String outputDir = "src/main/resources/images";
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            Image image = generateExplosionFrame(frame);
            saveImage(image, new File(outputDir, "explosion_frame_" + frame + ".png"));
            System.out.println("Generated frame " + frame + "/" + NUM_FRAMES);
        }
    }

    private static Image generateExplosionFrame(int frameIndex) {
        Canvas canvas = new Canvas(FRAME_SIZE, FRAME_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Clear background (transparent)
        gc.clearRect(0, 0, FRAME_SIZE, FRAME_SIZE);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0.0 to 1.0)
        double progress = frameIndex / (double) (NUM_FRAMES - 1);

        // Phase 1: Expansion (0.0 - 0.4)
        // Phase 2: Peak brightness (0.4 - 0.6)
        // Phase 3: Fade and dissipate (0.6 - 1.0)

        double maxRadius = FRAME_SIZE * 0.45;
        double currentRadius;
        double opacity;

        if (progress < 0.4) {
            // Expanding phase
            double phase1Progress = progress / 0.4;
            currentRadius = maxRadius * phase1Progress;
            opacity = 0.3 + 0.7 * phase1Progress;
        } else if (progress < 0.6) {
            // Peak brightness phase
            currentRadius = maxRadius;
            opacity = 1.0;
        } else {
            // Fading phase
            double phase3Progress = (progress - 0.6) / 0.4;
            currentRadius = maxRadius * (1.0 + phase3Progress * 0.5); // Slight expansion while fading
            opacity = 1.0 - phase3Progress;
        }

        // Draw multiple layers for depth
        int numLayers = 8;
        for (int layer = 0; layer < numLayers; layer++) {
            double layerProgress = layer / (double) numLayers;
            double layerRadius = currentRadius * (0.3 + layerProgress * 0.7);
            double layerOpacity = opacity * (1.0 - layerProgress * 0.5);

            // Color shifts from white -> yellow -> orange -> red -> dark red
            Color color;
            if (layerProgress < 0.2) {
                // White core
                color = Color.rgb(255, 255, 255, layerOpacity);
            } else if (layerProgress < 0.4) {
                // Yellow
                double t = (layerProgress - 0.2) / 0.2;
                color = Color.rgb(255, 255, (int) (255 * (1 - t)), layerOpacity);
            } else if (layerProgress < 0.6) {
                // Orange
                double t = (layerProgress - 0.4) / 0.2;
                color = Color.rgb(255, (int) (200 - t * 100), 0, layerOpacity);
            } else if (layerProgress < 0.8) {
                // Red
                double t = (layerProgress - 0.6) / 0.2;
                color = Color.rgb((int) (255 - t * 55), (int) (100 - t * 100), 0, layerOpacity);
            } else {
                // Dark red
                color = Color.rgb(200, 0, 0, layerOpacity * 0.5);
            }

            // Draw with radial gradient for smooth falloff
            RadialGradient gradient = new RadialGradient(
                    0, 0, centerX, centerY, layerRadius,
                    false, CycleMethod.NO_CYCLE,
                    new Stop(0, color),
                    new Stop(0.7, Color.rgb((int) (color.getRed() * 255),
                                            (int) (color.getGreen() * 255),
                                            (int) (color.getBlue() * 255),
                                            color.getOpacity() * 0.5)),
                    new Stop(1, Color.TRANSPARENT)
            );

            gc.setFill(gradient);
            gc.fillOval(centerX - layerRadius, centerY - layerRadius,
                       layerRadius * 2, layerRadius * 2);
        }

        // Add particle effects
        if (progress > 0.3) {
            int numParticles = (int) (20 * (1.0 - progress));
            for (int i = 0; i < numParticles; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double distance = currentRadius * (0.8 + random.nextDouble() * 0.4);
                double particleX = centerX + Math.cos(angle) * distance;
                double particleY = centerY + Math.sin(angle) * distance;
                double particleSize = 2 + random.nextDouble() * 4;
                double particleOpacity = opacity * (0.5 + random.nextDouble() * 0.5);

                gc.setFill(Color.rgb(255, 150, 0, particleOpacity));
                gc.fillOval(particleX - particleSize / 2, particleY - particleSize / 2,
                           particleSize, particleSize);
            }

            // Reset random for next frame consistency
            random.setSeed(42 + frameIndex);
        }

        // Convert to WritableImage
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return canvas.snapshot(params, null);
    }

    private static void saveImage(Image image, File file) throws IOException {
        WritableImage wImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        Canvas canvas = new Canvas(image.getWidth(), image.getHeight());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.drawImage(image, 0, 0);
        wImage = canvas.snapshot(params, wImage);

        BufferedImage bImage = javafx.embed.swing.SwingFXUtils.fromFXImage(wImage, null);
        ImageIO.write(bImage, "png", file);
    }
}
