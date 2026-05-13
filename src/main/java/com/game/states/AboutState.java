package com.game.states;

import com.game.core.Game;
import com.game.util.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AboutState extends GameState {

    private static final int STAR_COUNT = 400;
    private static final int MIN_FLYING_OBJECTS = 2;
    private static final int MAX_FLYING_OBJECTS = 5;

    private final Random random = new Random();
    private final List<Star> stars = new ArrayList<>();
    private final List<FlyingObject> flyingObjects = new ArrayList<>();

    private Font titleFont;
    private Font bodyFont;
    private Font creditFont;

    private enum FadeState { FADE_IN, VISIBLE, FADE_OUT, DONE }

    private class Star {
        double x, y, size, currentOpacity;
        FadeState fadeState;
        double fadeTimer, fadeInDuration, fadeOutDuration, visibleDuration;

        Star(double x, double y, double size) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.currentOpacity = 0.0;
            this.fadeInDuration = 2.0 + random.nextDouble() * 5.0;
            this.fadeOutDuration = 2.0 + random.nextDouble() * 5.0;
            this.visibleDuration = 2.0 + random.nextDouble() * 5.0;
            this.fadeState = FadeState.FADE_IN;
            this.fadeTimer = random.nextDouble() * fadeInDuration;
        }

        void update(double deltaTime) {
            fadeTimer += deltaTime;
            switch (fadeState) {
                case FADE_IN:
                    currentOpacity = fadeTimer / fadeInDuration;
                    if (fadeTimer >= fadeInDuration) {
                        currentOpacity = 1.0;
                        fadeState = FadeState.VISIBLE;
                        fadeTimer = 0.0;
                    }
                    break;
                case VISIBLE:
                    currentOpacity = 1.0;
                    if (fadeTimer >= visibleDuration) {
                        fadeState = FadeState.FADE_OUT;
                        fadeTimer = 0.0;
                    }
                    break;
                case FADE_OUT:
                    currentOpacity = 1.0 - fadeTimer / fadeOutDuration;
                    if (fadeTimer >= fadeOutDuration) {
                        currentOpacity = 0.0;
                        fadeState = FadeState.DONE;
                    }
                    break;
                case DONE:
                    currentOpacity = 0.0;
                    break;
            }
        }

        boolean needsRepositioning() {
            return fadeState == FadeState.DONE;
        }
    }

    private enum SpriteType { ENEMY, GEM, BLACK_HOLE, STARSHIP, HEART }

    private class FlyingObject {
        double x, y, dx, dy, size;
        double rotation;
        double rotationSpeed;
        SpriteType type;
        int subType;
        int frameIndex;
        double frameTimer;
        double frameInterval;
        int frameCount;

        FlyingObject() {
            pickRandomType();
            spawnAtEdge();
            rotation = random.nextDouble() * 360;
            // Between 2 rotations/sec (720 deg/s) and 1 rotation per 4 sec (90 deg/s)
            rotationSpeed = 90 + random.nextDouble() * 630;
            if (random.nextBoolean()) rotationSpeed = -rotationSpeed;
        }

        void pickRandomType() {
            // Weighted selection: each black hole and gem has 10% the weight of other sprites
            // Enemies: 16 at weight 1.0, Gems: 16 at weight 0.1, Black holes: 16 at weight 0.1
            // Starship: 1 at weight 1.0, Heart: 1 at weight 1.0
            double enemyWeight = 1.0;
            double gemWeight = 0.1;
            double blackHoleWeight = 0.1;
            double starshipWeight = 1.0;
            double heartWeight = 1.0;

            double totalWeight = EnemySpriteLoader.getEnemyTypeCount() * enemyWeight
                + GemSpriteLoader.getGemTypeCount() * gemWeight
                + BlackHoleSpriteLoader.getColorSchemeCount() * blackHoleWeight
                + starshipWeight + heartWeight;

            int attempts = 0;
            int pick;
            do {
                pick = weightedPick(totalWeight, enemyWeight, gemWeight, blackHoleWeight, starshipWeight, heartWeight);
                attempts++;
            } while (attempts < 20 && isSpriteVisible(pick));

            assignFromIndex(pick);
            frameIndex = random.nextInt(frameCount);
            frameTimer = 0;
        }

        private int weightedPick(double totalWeight, double enemyWeight, double gemWeight,
                                  double blackHoleWeight, double starshipWeight, double heartWeight) {
            double r = random.nextDouble() * totalWeight;

            double cumulative = EnemySpriteLoader.getEnemyTypeCount() * enemyWeight;
            if (r < cumulative) {
                return (int)(r / enemyWeight);
            }

            r -= cumulative;
            cumulative = GemSpriteLoader.getGemTypeCount() * gemWeight;
            if (r < cumulative) {
                return EnemySpriteLoader.getEnemyTypeCount() + (int)(r / gemWeight);
            }

            r -= cumulative;
            cumulative = BlackHoleSpriteLoader.getColorSchemeCount() * blackHoleWeight;
            if (r < cumulative) {
                return EnemySpriteLoader.getEnemyTypeCount() + GemSpriteLoader.getGemTypeCount() + (int)(r / blackHoleWeight);
            }

            r -= cumulative;
            if (r < starshipWeight) {
                return EnemySpriteLoader.getEnemyTypeCount() + GemSpriteLoader.getGemTypeCount() + BlackHoleSpriteLoader.getColorSchemeCount();
            }

            return EnemySpriteLoader.getEnemyTypeCount() + GemSpriteLoader.getGemTypeCount() + BlackHoleSpriteLoader.getColorSchemeCount() + 1;
        }

        private boolean isSpriteVisible(int index) {
            for (FlyingObject obj : flyingObjects) {
                if (obj == this) continue;
                if (obj.getSpriteIndex() == index) return true;
            }
            return false;
        }

        private int getSpriteIndex() {
            int offset = 0;
            switch (type) {
                case ENEMY: return subType;
                case GEM: return EnemySpriteLoader.getEnemyTypeCount() + subType;
                case BLACK_HOLE: return EnemySpriteLoader.getEnemyTypeCount() + GemSpriteLoader.getGemTypeCount() + subType;
                case STARSHIP: return EnemySpriteLoader.getEnemyTypeCount() + GemSpriteLoader.getGemTypeCount() + BlackHoleSpriteLoader.getColorSchemeCount();
                case HEART: return EnemySpriteLoader.getEnemyTypeCount() + GemSpriteLoader.getGemTypeCount() + BlackHoleSpriteLoader.getColorSchemeCount() + 1;
                default: return -1;
            }
        }

        private void assignFromIndex(int index) {
            int offset = 0;

            // Enemies: 0..15
            if (index < offset + EnemySpriteLoader.getEnemyTypeCount()) {
                type = SpriteType.ENEMY;
                subType = index - offset;
                frameCount = EnemySpriteLoader.getFrameCount();
                frameInterval = 0.1;
                size = 80;
                return;
            }
            offset += EnemySpriteLoader.getEnemyTypeCount();

            // Gems: 16..31
            if (index < offset + GemSpriteLoader.getGemTypeCount()) {
                type = SpriteType.GEM;
                subType = index - offset;
                frameCount = GemSpriteLoader.getFrameCount();
                frameInterval = 0.05;
                size = 80;
                return;
            }
            offset += GemSpriteLoader.getGemTypeCount();

            // Black holes: 32..47
            if (index < offset + BlackHoleSpriteLoader.getColorSchemeCount()) {
                type = SpriteType.BLACK_HOLE;
                subType = index - offset;
                frameCount = BlackHoleSpriteLoader.getFrameCount();
                frameInterval = 0.1;
                size = 120;
                return;
            }
            offset += BlackHoleSpriteLoader.getColorSchemeCount();

            // Starship: 48
            if (index == offset) {
                type = SpriteType.STARSHIP;
                subType = 0;
                frameCount = StarshipSpriteLoader.getFrameCount();
                frameInterval = 0.25;
                size = 80;
                return;
            }
            offset++;

            // Heart: 49
            type = SpriteType.HEART;
            subType = 0;
            frameCount = HeartSpriteLoader.getFrameCount();
            frameInterval = 0.0625;
            size = 80;
        }

        void spawnAtEdge() {
            double w = Game.gameWidth;
            double h = Game.gameHeight;
            double speed = 80 + random.nextDouble() * 120;
            int edge = random.nextInt(4);
            double targetX, targetY;

            switch (edge) {
                case 0: // top
                    x = random.nextDouble() * w;
                    y = -size;
                    targetX = random.nextDouble() * w;
                    targetY = h + size;
                    break;
                case 1: // bottom
                    x = random.nextDouble() * w;
                    y = h + size;
                    targetX = random.nextDouble() * w;
                    targetY = -size;
                    break;
                case 2: // left
                    x = -size;
                    y = random.nextDouble() * h;
                    targetX = w + size;
                    targetY = random.nextDouble() * h;
                    break;
                default: // right
                    x = w + size;
                    y = random.nextDouble() * h;
                    targetX = -size;
                    targetY = random.nextDouble() * h;
                    break;
            }

            double angle = Math.atan2(targetY - y, targetX - x);
            dx = Math.cos(angle) * speed;
            dy = Math.sin(angle) * speed;
        }

        void update(double deltaTime) {
            x += dx * deltaTime;
            y += dy * deltaTime;
            rotation += rotationSpeed * deltaTime;
            frameTimer += deltaTime;
            if (frameTimer >= frameInterval) {
                frameTimer -= frameInterval;
                frameIndex = (frameIndex + 1) % frameCount;
            }
        }

        boolean isOffScreen() {
            double margin = size * 2;
            return x < -margin || x > Game.gameWidth + margin ||
                   y < -margin || y > Game.gameHeight + margin;
        }

        Image getCurrentFrame() {
            switch (type) {
                case ENEMY:
                    return EnemySpriteLoader.getEnemyFrame(subType, frameIndex);
                case GEM:
                    return GemSpriteLoader.getGemFrame(subType, frameIndex);
                case BLACK_HOLE:
                    return BlackHoleSpriteLoader.getBlackHoleFrame(subType, frameIndex);
                case STARSHIP:
                    return StarshipSpriteLoader.getStarshipFrame(frameIndex);
                case HEART:
                    return HeartSpriteLoader.getHeartFrame(frameIndex);
                default:
                    return null;
            }
        }
    }

    public AboutState(GameStateManager gsm) {
        super(gsm);
        init();
    }

    @Override
    public void init() {
        titleFont = Font.font("Arial", FontWeight.BOLD, 36);
        bodyFont = Font.font("Arial", FontWeight.NORMAL, 20);
        creditFont = Font.font("Arial", FontWeight.BOLD, 22);

        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Star(
                random.nextDouble() * Game.gameWidth,
                random.nextDouble() * Game.gameHeight,
                1.0 + random.nextDouble() * 2.5
            ));
        }

        int initialCount = MIN_FLYING_OBJECTS + random.nextInt(MAX_FLYING_OBJECTS - MIN_FLYING_OBJECTS + 1);
        for (int i = 0; i < initialCount; i++) {
            flyingObjects.add(new FlyingObject());
        }
    }

    @Override
    public void update(double deltaTime) {
        for (Star star : stars) {
            star.update(deltaTime);
            if (star.needsRepositioning()) {
                star.x = random.nextDouble() * Game.gameWidth;
                star.y = random.nextDouble() * Game.gameHeight;
                star.size = 1.0 + random.nextDouble() * 2.5;
                star.fadeState = FadeState.FADE_IN;
                star.fadeTimer = 0;
                star.fadeInDuration = 2.0 + random.nextDouble() * 5.0;
                star.fadeOutDuration = 2.0 + random.nextDouble() * 5.0;
                star.visibleDuration = 2.0 + random.nextDouble() * 5.0;
            }
        }

        flyingObjects.removeIf(FlyingObject::isOffScreen);
        for (FlyingObject obj : flyingObjects) {
            obj.update(deltaTime);
        }

        int target = MIN_FLYING_OBJECTS + random.nextInt(MAX_FLYING_OBJECTS - MIN_FLYING_OBJECTS + 1);
        while (flyingObjects.size() < target) {
            flyingObjects.add(new FlyingObject());
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        double w = Game.gameWidth;
        double h = Game.gameHeight;

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, w, h);

        for (Star star : stars) {
            gc.setFill(Color.rgb(255, 255, 255, Math.max(0, Math.min(1, star.currentOpacity))));
            gc.fillOval(star.x - star.size / 2, star.y - star.size / 2, star.size, star.size);
        }

        for (FlyingObject obj : flyingObjects) {
            Image frame = obj.getCurrentFrame();
            if (frame != null) {
                gc.save();
                gc.translate(obj.x, obj.y);
                gc.rotate(obj.rotation);
                gc.drawImage(frame, -obj.size / 2, -obj.size / 2, obj.size, obj.size);
                gc.restore();
            }
        }

        double centerX = w / 2.0;
        double textBlockTop = h * 0.15;

        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        double boxWidth = Math.min(700, w * 0.8);
        double boxHeight = h * 0.72;
        gc.fillRoundRect(centerX - boxWidth / 2, textBlockTop - 20, boxWidth, boxHeight, 20, 20);

        double lineY = textBlockTop + 30;
        double lineSpacing = 30;

        gc.setFont(titleFont);
        gc.setFill(Color.rgb(100, 200, 255));
        String title = "About GooGoo";
        double titleX = TextUtils.centerTextX(title, titleFont, w);
        gc.fillText(title, titleX, lineY);
        lineY += lineSpacing;

        gc.setFont(bodyFont);
        gc.setFill(Color.rgb(180, 180, 180));
        String versionText = "Version " + Game.VERSION;
        gc.fillText(versionText, TextUtils.centerTextX(versionText, bodyFont, w), lineY);
        lineY += lineSpacing * 1.5;

        gc.setFont(bodyFont);
        gc.setFill(Color.WHITE);

        String[] lines = {
            "GooGoo is a remake of a classic Sinclair Spectrum game,",
            "preserving the original spirit.",
            "",
            "A space shooter built with JavaFX, featuring procedurally",
            "generated sprites, intelligent enemy AI, and a dynamic",
            "scoring system.",
            "",
            "Developed using vibe-coding on a MacBook Pro,",
            "with Claude Code and assistance from Gemini.",
        };

        for (String line : lines) {
            if (!line.isEmpty()) {
                double lx = TextUtils.centerTextX(line, bodyFont, w);
                gc.fillText(line, lx, lineY);
            }
            lineY += lineSpacing;
        }

        lineY += lineSpacing;

        gc.setFont(creditFont);
        gc.setFill(Color.rgb(255, 215, 0));

        String devLine = "Developed by: Noam Singer";
        gc.fillText(devLine, TextUtils.centerTextX(devLine, creditFont, w), lineY);
        lineY += lineSpacing * 1.5;

        gc.setFill(Color.rgb(200, 200, 200));
        gc.setFont(bodyFont);
        String successorLine1 = "Spiritual successor to: GooGoo";
        String successorLine2 = "by Noam Singer & Raz Shoham";
        String successorLine3 = "(original Sinclair Spectrum game)";
        gc.fillText(successorLine1, TextUtils.centerTextX(successorLine1, bodyFont, w), lineY);
        lineY += lineSpacing;
        gc.fillText(successorLine2, TextUtils.centerTextX(successorLine2, bodyFont, w), lineY);
        lineY += lineSpacing;
        gc.fillText(successorLine3, TextUtils.centerTextX(successorLine3, bodyFont, w), lineY);
        lineY += lineSpacing * 2;

        gc.setFill(Color.rgb(150, 150, 150));
        Font smallFont = Font.font("Arial", FontWeight.NORMAL, 16);
        gc.setFont(smallFont);
        String backText = "Press ESC or click to return";
        gc.fillText(backText, TextUtils.centerTextX(backText, smallFont, w), lineY);
    }

    @Override
    public void keyPressed(KeyCode key) {
        if (key == KeyCode.ESCAPE) {
            gsm.setState(new MenuState(gsm));
        }
    }

    @Override
    public void keyReleased(KeyCode key) {
    }

    @Override
    public void mouseClicked(double x, double y) {
        gsm.setState(new MenuState(gsm));
    }
}
