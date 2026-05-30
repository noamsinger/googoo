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

public class InstructionsState extends GameState {

    private static final int STAR_COUNT = 400;
    private static final int MIN_FLYING_OBJECTS = 2;
    private static final int MAX_FLYING_OBJECTS = 4;
    private static final double SCROLL_SPEED = 200.0;

    private final Random random = new Random();
    private final List<Star> stars = new ArrayList<>();
    private final List<FlyingObject> flyingObjects = new ArrayList<>();

    private Font titleFont;
    private Font headingFont;
    private Font bodyFont;
    private double scrollOffset = 0;
    private double maxScroll = 0;

    private enum FadeState {FADE_IN, VISIBLE, FADE_OUT, DONE}

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

    private enum SpriteType {ENEMY, GEM, BLACK_HOLE, STARSHIP, HEART}

    private class FlyingObject {
        double x, y, dx, dy, size;
        double rotation, rotationSpeed;
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
            rotationSpeed = 90 + random.nextDouble() * 630;
            if (random.nextBoolean()) rotationSpeed = -rotationSpeed;
        }

        void pickRandomType() {
            SpriteType[] types = SpriteType.values();
            type = types[random.nextInt(types.length)];
            switch (type) {
                case ENEMY:
                    subType = random.nextInt(EnemySpriteLoader.getEnemyTypeCount());
                    frameCount = EnemySpriteLoader.getFrameCount();
                    frameInterval = 0.1;
                    size = 80;
                    break;
                case GEM:
                    subType = random.nextInt(GemSpriteLoader.getGemTypeCount());
                    frameCount = GemSpriteLoader.getFrameCount();
                    frameInterval = 0.05;
                    size = 80;
                    break;
                case BLACK_HOLE:
                    subType = random.nextInt(BlackHoleSpriteLoader.getColorSchemeCount());
                    frameCount = BlackHoleSpriteLoader.getFrameCount();
                    frameInterval = 0.1;
                    size = 120;
                    break;
                case STARSHIP:
                    subType = 0;
                    frameCount = StarshipSpriteLoader.getFrameCount();
                    frameInterval = 0.25;
                    size = 80;
                    break;
                case HEART:
                    subType = 0;
                    frameCount = HeartSpriteLoader.getFrameCount();
                    frameInterval = 0.0625;
                    size = 80;
                    break;
            }
            frameIndex = random.nextInt(frameCount);
            frameTimer = 0;
        }

        void spawnAtEdge() {
            double w = Game.gameWidth;
            double h = Game.gameHeight;
            double speed = 60 + random.nextDouble() * 100;
            int edge = random.nextInt(4);
            double targetX, targetY;
            switch (edge) {
                case 0:
                    x = random.nextDouble() * w;
                    y = -size;
                    targetX = random.nextDouble() * w;
                    targetY = h + size;
                    break;
                case 1:
                    x = random.nextDouble() * w;
                    y = h + size;
                    targetX = random.nextDouble() * w;
                    targetY = -size;
                    break;
                case 2:
                    x = -size;
                    y = random.nextDouble() * h;
                    targetX = w + size;
                    targetY = random.nextDouble() * h;
                    break;
                default:
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

    private static final String[] INSTRUCTIONS = {
            "=== OBJECTIVE ===",
            "",
            "Navigate your starship through space, collect gems",
            "to earn EXP, avoid enemies, survive as long",
            "as possible to reach higher levels, and have fun!",
            "",
            "",
            "=== CONTROLS ===",
            "",
            "Mouse Steering:",
            "  Right-click / Right-drag: Set navigation target",
            "",
            "Ballistic Mode (Arrow Keys):",
            "  Left / Right: Roll and turn",
            "  Up: Accelerate",
            "  Down: Decelerate",
            "",
            "Combat:",
            "  Right-click or SPACE: Fire bullet / torpedo",
            "",
            "Shop:",
            "  S: Open mid-game shop (pauses the game)",
            "",
            "Other:",
            "  ESC: Return to menu",
            "",
            "",
            "=== GAME ELEMENTS ===",
            "",
            "Gems:",
            "  Colorful spinning collectibles scattered in space.",
            "  Fly over them to collect and gain +1 EXP.",
            "  New gems spawn when collected.",
            "",
            "Enemies:",
            "  16 unique enemy types with different behaviors:",
            "  - Wiggle: Unpredictable sinusoidal movement",
            "  - Fast Rotation: Quick turning speed",
            "  - Prediction: Leads your position when chasing",
            "  - Random Speed: Varies velocity unpredictably",
            "  Higher levels introduce tougher enemy combinations.",
            "",
            "Black Holes:",
            "  Warp zones that teleport your ship to a pairing wormhole." +
                    " Use them strategically to escape danger.",
            "",
            "Hearts:",
            "  Appear after collecting 4 gems.",
            "  Restores shield or grants an extra life.",
            "",
            "Bullets:",
            "  Fire at enemies to destroy them on contact.",
            "",
            "Torpedoes:",
            "  Homing projectile. Buy in the shop. Lasts 5 seconds.",
            "",
            "",
            "=== GAME MODES ===",
            "",
            "Lives Mode:",
            "  Start with 3 lives.",
            "  Each collision costs one life.",
            "  5 seconds of immunity after each hit.",
            "  Game over when all lives are lost.",
            "",
            "",
            "=== SHOP (press S) ===",
            "",
            "Spend EXP to upgrade during gameplay:",
            "",
            "  Starships:",
            "    Ship 1: free (default)",
            "    Ship 2: 200 EXP  — 2x speed, 2x accel",
            "    Ship 3: 500 EXP  — 5x turn (tracking), 2x (keys)",
            "    Ship 4: 1000 EXP — burst acceleration, 4x max speed",
            "",
            "  Fire Modes:",
            "    Manual: free (default)",
            "    Semi-Auto: 100 EXP  — click, 0.3s rate",
            "    Auto: 200 EXP       — auto-fire 0.3s",
            "    Vulkan: 400 EXP     — auto-fire 0.1s",
            "",
            "  Weapons:",
            "    Bullet: free (default)",
            "    Torpedo: 600 EXP — homing, 5s life",
            "",
            "  Shields:",
            "    Hit Shield: 40 EXP  — +5 hits protection",
            "    Timed Shield: 80 EXP — +60s protection",
            "",
            "  Lives: 10 EXP each",
            "",
            "",
            "=== LEVEL PROGRESSION ===",
            "",
            "Every 60 seconds you advance to the next level.",
            "Each new level adds more enemies and black holes.",
            "Your progress is saved automatically.",
            "",
            "",
            "=== TIPS ===",
            "",
            "- Use black holes to escape when surrounded",
            "- Enemies get faster and smarter at higher levels",
            "- Collect gems quickly for hearts to appear",
            "- Upgrade weapons and shields in the shop early",
            "- Right-drag for continuous navigation control",
            "",
            "",
            "[Use UP/DOWN arrows or scroll to navigate]",
            "[Press ESC to return to menu]",
    };

    public InstructionsState(GameStateManager gsm) {
        super(gsm);
        init();
    }

    @Override
    public void init() {
        titleFont = Font.font("Arial", FontWeight.BOLD, 36);
        headingFont = Font.font("Arial", FontWeight.BOLD, 22);
        bodyFont = Font.font("Arial", FontWeight.NORMAL, 18);

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

        double lineHeight = 26;
        double totalTextHeight = INSTRUCTIONS.length * lineHeight;
        double visibleHeight = Game.gameHeight * 0.7;
        maxScroll = Math.max(0, totalTextHeight - visibleHeight);
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
        double boxWidth = Math.min(700, w * 0.8);
        double boxTop = h * 0.08;
        double boxHeight = h * 0.84;

        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRoundRect(centerX - boxWidth / 2, boxTop, boxWidth, boxHeight, 20, 20);

        // Title (fixed, not scrolled)
        double titleY = boxTop + 40;
        gc.setFont(titleFont);
        gc.setFill(Color.rgb(100, 200, 255));
        String title = "Instructions";
        gc.fillText(title, TextUtils.centerTextX(title, titleFont, w), titleY);

        // Scrollable content area
        double contentTop = titleY + 30;
        double contentBottom = boxTop + boxHeight - 20;
        double contentHeight = contentBottom - contentTop;
        double lineHeight = 26;

        gc.save();
        gc.beginPath();
        gc.rect(centerX - boxWidth / 2 + 10, contentTop, boxWidth - 20, contentHeight);
        gc.clip();

        double textY = contentTop + lineHeight - scrollOffset;

        for (String line : INSTRUCTIONS) {
            if (textY + lineHeight > contentTop - lineHeight && textY < contentBottom + lineHeight) {
                if (line.startsWith("===") && line.endsWith("===")) {
                    gc.setFont(headingFont);
                    gc.setFill(Color.rgb(255, 215, 0));
                    String heading = line.replace("=", "").trim();
                    gc.fillText(heading, TextUtils.centerTextX(heading, headingFont, w), textY);
                } else if (!line.isEmpty()) {
                    gc.setFont(bodyFont);
                    if (line.startsWith("[")) {
                        gc.setFill(Color.rgb(150, 150, 150));
                    } else if (line.startsWith("  ")) {
                        gc.setFill(Color.rgb(200, 200, 200));
                    } else {
                        gc.setFill(Color.WHITE);
                    }
                    double lx = centerX - boxWidth / 2 + 40;
                    gc.fillText(line, lx, textY);
                }
            }
            textY += lineHeight;
        }

        gc.restore();

        // Scroll indicators
        if (scrollOffset > 0) {
            gc.setFill(Color.rgb(255, 255, 255, 0.5));
            gc.fillText("▲", centerX - 5, contentTop + 10);
        }
        if (scrollOffset < maxScroll) {
            gc.setFill(Color.rgb(255, 255, 255, 0.5));
            gc.fillText("▼", centerX - 5, contentBottom - 5);
        }
    }

    @Override
    public void keyPressed(KeyCode key) {
        if (key == KeyCode.ESCAPE) {
            gsm.setState(new MenuState(gsm));
        } else if (key == KeyCode.UP) {
            scrollOffset = Math.max(0, scrollOffset - 40);
        } else if (key == KeyCode.DOWN) {
            scrollOffset = Math.min(maxScroll, scrollOffset + 40);
        }
    }

    @Override
    public void keyReleased(KeyCode key) {
    }

    @Override
    public void mouseScrolled(double deltaY) {
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - deltaY));
    }

    @Override
    public void mouseClicked(double x, double y) {
        gsm.setState(new MenuState(gsm));
    }
}
