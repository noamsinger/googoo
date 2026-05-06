package com.game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class F15SpriteGenerator {
    private static final int FRAME_SIZE = 80;
    private static final int NUM_FRAMES = 16;
    private static final int FRAMES_PER_ROW = 4; // 4x4 grid like original

    public static void main(String[] args) {
        try {
            generateF15SpriteSheet();
            System.out.println("Successfully generated F-15 sprite sheet!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateF15SpriteSheet() throws IOException {
        int rows = (NUM_FRAMES + FRAMES_PER_ROW - 1) / FRAMES_PER_ROW;
        int sheetWidth = FRAME_SIZE * FRAMES_PER_ROW;
        int sheetHeight = FRAME_SIZE * rows;

        BufferedImage spriteSheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sheetG2d = spriteSheet.createGraphics();

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            BufferedImage frameImage = generateF15Frame(frame);

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

        File outputFile = new File(outputDir, "enemy_sheet_13.png");
        ImageIO.write(spriteSheet, "PNG", outputFile);
        System.out.println("F-15 sprite sheet saved: " + outputFile.getAbsolutePath());
    }

    private static BufferedImage generateF15Frame(int frameIndex) {
        BufferedImage image = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = FRAME_SIZE / 2.0;
        double centerY = FRAME_SIZE / 2.0;

        // Animation progress (0 to 1)
        double progress = frameIndex / (double) NUM_FRAMES;

        // Rich color scheme for F-15 (military gray)
        Color bodyColor = new Color(140, 150, 160); // Gray body
        Color darkColor = new Color(80, 90, 100); // Dark gray details
        Color cockpitColor = new Color(50, 100, 150); // Blue cockpit
        Color accentColor = new Color(180, 180, 200); // Light highlights
        Color exhaustColor = new Color(255, 150, 50); // Orange exhaust glow

        // Bank/roll animation (slight tilt)
        double roll = Math.sin(progress * Math.PI * 2) * 0.1;

        // Draw exhaust glow from engines
        double exhaustGlow = 3 + Math.sin(progress * Math.PI * 8) * 2;
        g2d.setColor(new Color(255, 200, 100, 100));
        g2d.fill(new Ellipse2D.Double(centerX - 7 - exhaustGlow / 2, centerY + 12 - exhaustGlow / 2,
                                       exhaustGlow, exhaustGlow));
        g2d.fill(new Ellipse2D.Double(centerX + 7 - exhaustGlow / 2, centerY + 12 - exhaustGlow / 2,
                                       exhaustGlow, exhaustGlow));

        // Draw main wings (swept back, delta-like)
        Path2D leftWing = new Path2D.Double();
        leftWing.moveTo(centerX - 4, centerY);
        leftWing.lineTo(centerX - 22, centerY + 8 + roll * 5);
        leftWing.lineTo(centerX - 18, centerY + 12 + roll * 5);
        leftWing.lineTo(centerX - 4, centerY + 8);
        leftWing.closePath();

        Path2D rightWing = new Path2D.Double();
        rightWing.moveTo(centerX + 4, centerY);
        rightWing.lineTo(centerX + 22, centerY + 8 - roll * 5);
        rightWing.lineTo(centerX + 18, centerY + 12 - roll * 5);
        rightWing.lineTo(centerX + 4, centerY + 8);
        rightWing.closePath();

        g2d.setColor(bodyColor);
        g2d.fill(leftWing);
        g2d.fill(rightWing);

        // Wing details
        g2d.setColor(darkColor);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.draw(leftWing);
        g2d.draw(rightWing);

        // Draw tail fins (vertical stabilizers)
        Path2D leftTail = new Path2D.Double();
        leftTail.moveTo(centerX - 6, centerY + 8);
        leftTail.lineTo(centerX - 8, centerY + 2);
        leftTail.lineTo(centerX - 4, centerY + 8);
        leftTail.closePath();

        Path2D rightTail = new Path2D.Double();
        rightTail.moveTo(centerX + 6, centerY + 8);
        rightTail.lineTo(centerX + 8, centerY + 2);
        rightTail.lineTo(centerX + 4, centerY + 8);
        rightTail.closePath();

        g2d.setColor(darkColor);
        g2d.fill(leftTail);
        g2d.fill(rightTail);

        // Draw fuselage (main body)
        Path2D fuselage = new Path2D.Double();
        double noseY = centerY - 18;

        fuselage.moveTo(centerX, noseY);
        // Left side
        fuselage.curveTo(
            centerX - 3, centerY - 15,
            centerX - 5, centerY - 5,
            centerX - 5, centerY + 5
        );
        fuselage.lineTo(centerX - 4, centerY + 12);
        fuselage.lineTo(centerX - 2, centerY + 14);

        // Exhaust notch
        fuselage.lineTo(centerX, centerY + 13);

        // Right side (mirror)
        fuselage.lineTo(centerX + 2, centerY + 14);
        fuselage.lineTo(centerX + 4, centerY + 12);
        fuselage.lineTo(centerX + 5, centerY + 5);
        fuselage.curveTo(
            centerX + 5, centerY - 5,
            centerX + 3, centerY - 15,
            centerX, noseY
        );
        fuselage.closePath();

        g2d.setColor(bodyColor);
        g2d.fill(fuselage);

        // Fuselage highlight
        g2d.setColor(accentColor);
        Path2D highlight = new Path2D.Double();
        highlight.moveTo(centerX, noseY);
        highlight.curveTo(
            centerX - 1, centerY - 15,
            centerX - 2, centerY - 5,
            centerX - 2, centerY + 5
        );
        highlight.lineTo(centerX - 1, centerY + 10);
        highlight.lineTo(centerX, centerY + 11);
        highlight.closePath();
        g2d.fill(highlight);

        // Draw cockpit canopy
        Path2D cockpit = new Path2D.Double();
        cockpit.moveTo(centerX, centerY - 12);
        cockpit.curveTo(
            centerX - 2.5, centerY - 10,
            centerX - 3, centerY - 5,
            centerX - 2, centerY
        );
        cockpit.lineTo(centerX + 2, centerY);
        cockpit.curveTo(
            centerX + 3, centerY - 5,
            centerX + 2.5, centerY - 10,
            centerX, centerY - 12
        );
        cockpit.closePath();

        g2d.setColor(cockpitColor);
        g2d.fill(cockpit);

        // Cockpit reflection
        g2d.setColor(new Color(150, 200, 255, 150));
        g2d.fill(new Ellipse2D.Double(centerX - 1.5, centerY - 9, 3, 4));

        // Draw air intakes (under wings)
        g2d.setColor(darkColor.darker());
        g2d.fillRect((int)(centerX - 7), (int)(centerY + 2), 3, 6);
        g2d.fillRect((int)(centerX + 4), (int)(centerY + 2), 3, 6);

        // Draw engine exhausts
        g2d.setColor(new Color(40, 40, 50));
        g2d.fill(new Ellipse2D.Double(centerX - 8, centerY + 11, 2.5, 2.5));
        g2d.fill(new Ellipse2D.Double(centerX + 5.5, centerY + 11, 2.5, 2.5));

        // Exhaust glow inside
        g2d.setColor(exhaustColor);
        g2d.fill(new Ellipse2D.Double(centerX - 7.5, centerY + 11.5, 1.5, 1.5));
        g2d.fill(new Ellipse2D.Double(centerX + 6, centerY + 11.5, 1.5, 1.5));

        // Draw weapons/missiles under wings (optional detail)
        g2d.setColor(new Color(100, 100, 120));
        g2d.fillRect((int)(centerX - 15), (int)(centerY + 5), 4, 2);
        g2d.fillRect((int)(centerX + 11), (int)(centerY + 5), 4, 2);

        // Fuselage outline
        g2d.setColor(darkColor);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.draw(fuselage);

        // Cockpit outline
        g2d.setStroke(new BasicStroke(0.8f));
        g2d.draw(cockpit);

        // Panel lines
        g2d.setColor(new Color(100, 110, 120));
        g2d.setStroke(new BasicStroke(0.5f));
        g2d.drawLine((int)(centerX - 2), (int)(centerY - 5), (int)(centerX - 2), (int)(centerY + 8));
        g2d.drawLine((int)(centerX + 2), (int)(centerY - 5), (int)(centerX + 2), (int)(centerY + 8));

        g2d.dispose();
        return image;
    }
}
