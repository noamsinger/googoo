package com.game.states;

import com.game.core.Game;
import com.game.core.GameSettings;
import com.game.core.ProgressManager;
import com.game.core.LevelProgress;
import com.game.util.BlackHoleSpriteLoader;
import com.game.util.EnemySpriteLoader;
import com.game.util.ExplosionSpriteLoader;
import com.game.util.StarshipExplosionSpriteLoader;
import com.game.util.StarshipSpriteLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayState extends GameState {
    private static final Logger LOGGER = Logger.getLogger(PlayState.class.getName());

    // Reference resolution for physics calculations (1920x1080)
    private static final double REFERENCE_WIDTH = 1920.0;
    private static final double REFERENCE_HEIGHT = 1080.0;

    // Resolution scaling factors
    private double scaleX;
    private double scaleY;

    private List<Star> stars;
    private Random random;
    private Spaceship spaceship;
    private double targetX;
    private double targetY;
    private boolean mousePressed;
    private double mouseX;
    private double mouseY;
    private List<SmokeCloud> smokeClouds;
    private double gameTime; // Track total game time
    private List<BlackHole> blackHoles;
    private List<Enemy> enemies;
    private List<EnemyExplosion> enemyExplosions;
    private List<Gem> gems;
    private int score = 0;
    private double scoreColorTimer = 0.0; // Timer for color morphing
    private int gemsCollectedSinceLastHeart = 0;
    private Heart heart = null;

    // Game mode enum
    private enum GameMode {
        SHIELD,        // Shield percentage mode (1-10% damage per hit)
        LIVES,         // Lives mode with 5-second immunity after losing a life
        LIVE_FOREVER   // Invincible mode - no damage, no game over
    }

    // Game mode and lives
    private GameMode gameMode = GameMode.SHIELD; // Default mode
    private int remainingLives = 3;
    private boolean isImmune = false; // Immunity after losing a life in 3-life mode
    private double immunityTimer = 0.0;
    private double immunityDuration = 5.0; // 5 seconds of immunity

    // Starship explosion
    private StarshipExplosion starshipExplosion = null;
    private boolean gameOver = false;

    // Keyboard controls
    private boolean keyUpPressed = false;
    private boolean keyDownPressed = false;
    private boolean keyLeftPressed = false;
    private boolean keyRightPressed = false;
    private boolean keyShiftPressed = false;

    // Level system
    private int currentLevel;
    private double levelTimer = 0.0;
    private double levelDuration = 60.0; // 60 seconds per level
    private double levelMessageTimer = 0.0;
    private double levelMessageDuration = 5.0; // Show message for 5 seconds
    private boolean showLevelMessage = false;

    // Debug message display
    private boolean showDebugMessage = false;
    private double debugMessageTimer = 0.0;
    private double debugMessageDuration = 5.0; // Show message for 5 seconds
    private String debugMessageText = "";

    public PlayState(GameStateManager gsm) {
        super(gsm);
        init();
    }

    @Override
    public void init() {
        // Initialize debug mode from settings
        GameSettings settings = GameSettings.getInstance();
        boolean debugMode = settings.isDebugMode();

        // Set logger level based on debug mode
        if (debugMode) {
            LOGGER.setLevel(Level.FINE);
        } else {
            LOGGER.setLevel(Level.INFO);
        }

        LOGGER.fine("PlayState init - Setting fullscreen to true");
        LOGGER.fine("Before setFullscreen - gameWidth=" + Game.gameWidth + ", gameHeight=" + Game.gameHeight);

        Game.setFullscreen(true);

        LOGGER.fine("After setFullscreen - gameWidth=" + Game.gameWidth + ", gameHeight=" + Game.gameHeight);

        // Calculate resolution scaling factors
        scaleX = Game.gameWidth / REFERENCE_WIDTH;
        scaleY = Game.gameHeight / REFERENCE_HEIGHT;
        LOGGER.fine("Resolution scaling factors - scaleX=" + scaleX + ", scaleY=" + scaleY);

        random = new Random();
        stars = new ArrayList<>();

        for (int i = 0; i < 400; i++) {
            stars.add(createStar());
        }

        LOGGER.fine("Created " + stars.size() + " stars");

        // Initialize spaceship at center
        spaceship = new Spaceship(Game.gameWidth / 2.0, Game.gameHeight / 2.0);
        targetX = spaceship.x;
        targetY = spaceship.y;
        mousePressed = false;

        smokeClouds = new ArrayList<>();
        gameTime = 0.0;
        enemyExplosions = new ArrayList<>();
        gameOver = false;
        starshipExplosion = null;

        // Load explosion sprites
        ExplosionSpriteLoader.loadExplosionSprites();
        StarshipExplosionSpriteLoader.loadExplosionSprites();

        // Set game mode from settings
        GameSettings.GameType settingsGameType = settings.getGameType();
        switch (settingsGameType) {
            case SHIELD:
                gameMode = GameMode.SHIELD;
                break;
            case LIVES:
                gameMode = GameMode.LIVES;
                break;
            case LIVE_FOREVER:
                gameMode = GameMode.LIVE_FOREVER;
                break;
            default:
                gameMode = GameMode.SHIELD;
        }
        remainingLives = 3;
        isImmune = false;
        immunityTimer = 0.0;
        LOGGER.info("Game mode set to: " + gameMode + " (from settings: " + settingsGameType + ")");

        // Initialize level from settings (reuse settings variable from above)
        currentLevel = settings.getResolvedStartingLevel();

        // Check if we should restore progress for this level
        ProgressManager progressManager = ProgressManager.getInstance();
        LevelProgress levelProgress = progressManager.getLevelProgress(currentLevel, settingsGameType);

        if (levelProgress != null) {
            // Restore saved progress
            score = levelProgress.getStartingScore();
            remainingLives = levelProgress.getStartingLives();
            spaceship.shieldPercentage = levelProgress.getStartingShield();
            LOGGER.info("Restored progress for level " + currentLevel + ": score=" + score +
                       ", lives=" + remainingLives + ", shield=" + spaceship.shieldPercentage);
        } else {
            // Starting fresh - record initial state for this level
            score = 0;
            remainingLives = 3;
            spaceship.shieldPercentage = 100.0;
            progressManager.recordLevelProgress(currentLevel, settingsGameType,
                                               remainingLives, spaceship.shieldPercentage, score);
            LOGGER.info("Starting fresh at level " + currentLevel);
        }

        // Show starting level message
        showLevelMessage = true;
        levelMessageTimer = 0.0;

        // Initialize with black holes based on starting level
        blackHoles = new ArrayList<>();
        // Add black hole pairs for even levels up to current level
        int blackHolePairsToSpawn = (currentLevel / 2);
        for (int i = 0; i < blackHolePairsToSpawn; i++) {
            addBlackHolePair();
        }

        // Initialize enemies based on current level
        // Start with enemy 0, add one enemy every 2 levels
        // Level 1: enemy 0
        // Level 3: enemies 0, 1
        // Level 5: enemies 0, 1, 2
        // etc.
        enemies = new ArrayList<>();
        int enemiesToSpawn = Math.min(16, 1 + (currentLevel / 2));
        for (int i = 0; i < enemiesToSpawn; i++) {
            addEnemy(i);
        }

        // Initialize gems list
        gems = new ArrayList<>();
        spawnInitialGems();

        LOGGER.info("Starting at level " + currentLevel + " with " + blackHoles.size() / 2 + " black hole pairs and " + enemies.size() + " enemies");
    }

    private Star createStar() {
        double x = random.nextDouble() * Game.gameWidth;
        double y = random.nextDouble() * Game.gameHeight;
        double size = 1 + random.nextDouble() * 2.5;

        return new Star(x, y, size);
    }

    private void addBlackHolePair() {
        int colorScheme = (blackHoles.size() / 2) % 16; // Cycle through 16 color schemes
        double minPairDistance = Game.gameWidth / 2.0;
        double margin = 100 * Math.sqrt(scaleX * scaleY); // Scale margin

        // Generate random position for first black hole
        double x1 = margin + random.nextDouble() * (Game.gameWidth - 2 * margin);
        double y1 = margin + random.nextDouble() * (Game.gameHeight - 2 * margin);
        blackHoles.add(new BlackHole(x1, y1, colorScheme));

        // Generate position for second black hole ensuring it's at least half screen width away
        double x2, y2;
        int attempts = 0;
        do {
            x2 = margin + random.nextDouble() * (Game.gameWidth - 2 * margin);
            y2 = margin + random.nextDouble() * (Game.gameHeight - 2 * margin);
            double distance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
            if (distance >= minPairDistance) {
                break;
            }
            attempts++;
        } while (attempts < 100);

        blackHoles.add(new BlackHole(x2, y2, colorScheme));
    }

    private void addEnemy(int enemyType) {
        double minDistance = Game.gameHeight;
        double margin = 100 * Math.sqrt(scaleX * scaleY); // Scale margin

        // Generate random position at least screen height away from spaceship
        double x, y;
        int attempts = 0;
        do {
            x = margin + random.nextDouble() * (Game.gameWidth - 2 * margin);
            y = margin + random.nextDouble() * (Game.gameHeight - 2 * margin);
            double dx = x - spaceship.x;
            double dy = y - spaceship.y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance >= minDistance) {
                break;
            }
            attempts++;
        } while (attempts < 100);

        enemies.add(new Enemy(x, y, enemyType));
    }

    @Override
    public void update(double deltaTime) {
        gameTime += deltaTime;
        scoreColorTimer += deltaTime;
        if (scoreColorTimer >= 10.0) {
            scoreColorTimer -= 10.0; // Reset after 10 seconds
        }

        // Update starship explosion if active
        if (starshipExplosion != null) {
            starshipExplosion.update(deltaTime);
            if (starshipExplosion.isComplete()) {
                // Explosion complete - game is now fully over
                gameOver = true;
            }
            // Continue updating world even during explosion
        }

        // Update immunity timer in 3-life mode
        if (gameMode == GameMode.LIVES && isImmune) {
            immunityTimer += deltaTime;
            if (immunityTimer >= immunityDuration) {
                isImmune = false;
                immunityTimer = 0.0;
                LOGGER.info("Immunity period ended");
            }
        }

        // Don't update level progression if game is over
        if (!gameOver && starshipExplosion == null) {
            // Update level timer
            levelTimer += deltaTime;
        if (levelTimer >= levelDuration) {
            levelTimer -= levelDuration;
            currentLevel++;
            showLevelMessage = true;
            levelMessageTimer = 0.0;

            // Record progress for new level
            GameSettings settings = GameSettings.getInstance();
            ProgressManager progressManager = ProgressManager.getInstance();
            progressManager.recordLevelProgress(currentLevel, settings.getGameType(),
                                               remainingLives, spaceship.shieldPercentage, score);
            LOGGER.info("Recorded progress for level " + currentLevel + ": score=" + score +
                       ", lives=" + remainingLives + ", shield=" + spaceship.shieldPercentage);

            // On even levels, add a black hole pair
            if (currentLevel % 2 == 0) {
                addBlackHolePair();
            }

            // On odd levels (3, 5, 7, etc.), add a new enemy if we haven't reached max (16)
            if (currentLevel % 2 == 1 && currentLevel >= 3 && enemies.size() < 16) {
                int nextEnemyType = enemies.size(); // This will be 1 at level 3, 2 at level 5, etc.
                addEnemy(nextEnemyType);
            }

            LOGGER.info("Advanced to level " + currentLevel + ", black hole pairs: " + (blackHoles.size() / 2) + ", enemies: " + enemies.size());
        }

        // Update level message timer
        if (showLevelMessage) {
            levelMessageTimer += deltaTime;
            if (levelMessageTimer >= levelMessageDuration) {
                showLevelMessage = false;
            }
        }

        // Update debug message timer
        if (showDebugMessage) {
            debugMessageTimer += deltaTime;
            if (debugMessageTimer >= debugMessageDuration) {
                showDebugMessage = false;
            }
        }
        } // End of game-over check for level progression

        for (int i = 0; i < stars.size(); i++) {
            Star star = stars.get(i);
            star.update(deltaTime);

            // Reposition star after fade cycle completes
            if (star.needsRepositioning()) {
                stars.set(i, createStar());
            }
        }

        // Update target continuously while mouse is pressed (only if not game over)
        if (!gameOver && starshipExplosion == null && mousePressed) {
            targetX = mouseX;
            targetY = mouseY;
        }

        // Update spaceship (only if not game over)
        if (!gameOver && starshipExplosion == null) {
            spaceship.update(deltaTime, targetX, targetY, mousePressed, keyUpPressed, keyDownPressed, keyLeftPressed, keyRightPressed);

            // Check if spaceship changed target due to boundary avoidance
            if (spaceship.targetChanged) {
                targetX = spaceship.newTargetX;
                targetY = spaceship.newTargetY;
                spaceship.targetChanged = false;
            }

            // Check for black hole collisions and wormhole effect
            if (!spaceship.inWormhole && !spaceship.wormholeImmune) {
                for (int i = 0; i < blackHoles.size(); i += 2) {
                    BlackHole first = blackHoles.get(i);
                    BlackHole second = blackHoles.get(i + 1);

                    // Check distance to first black hole
                    double dx1 = spaceship.x - first.x;
                    double dy1 = spaceship.y - first.y;
                    double dist1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
                    double blackHoleRadius = 60.0 * Math.sqrt(scaleX * scaleY); // Half of 120 pixel sprite size, scaled
                    double captureRadius = blackHoleRadius * 0.5;

                    if (dist1 < captureRadius) {
                        // Enter wormhole at first black hole, exit at second
                        spaceship.enterWormhole(first.x, first.y, second.x, second.y);
                        // Trigger exit animation on the second (exit) black hole
                        second.triggerExitAnimation();
                        break;
                    }

                    // Check distance to second black hole
                    double dx2 = spaceship.x - second.x;
                    double dy2 = spaceship.y - second.y;
                    double dist2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);

                    if (dist2 < captureRadius) {
                        // Enter wormhole at second black hole, exit at first
                        spaceship.enterWormhole(second.x, second.y, first.x, first.y);
                        // Trigger exit animation on the first (exit) black hole
                        first.triggerExitAnimation();
                        break;
                    }
                }
            }
        }

        // Update smoke clouds and remove expired ones
        for (int i = smokeClouds.size() - 1; i >= 0; i--) {
            SmokeCloud cloud = smokeClouds.get(i);
            cloud.update(deltaTime);
            if (cloud.isExpired()) {
                smokeClouds.remove(i);
            }
        }

        // Update black holes - each pair interacts with its partner
        for (int i = 0; i < blackHoles.size(); i += 2) {
            BlackHole first = blackHoles.get(i);
            BlackHole second = blackHoles.get(i + 1);
            first.update(deltaTime, second);
            second.update(deltaTime, first);
        }

        // Update enemies
        for (Enemy enemy : enemies) {
            enemy.update(deltaTime, spaceship.x, spaceship.y);
        }

        // Update gems
        for (int i = gems.size() - 1; i >= 0; i--) {
            Gem gem = gems.get(i);
            gem.update(deltaTime);

            // Check collision with spaceship
            double dx = gem.x - spaceship.x;
            double dy = gem.y - spaceship.y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            double collisionDistance = 30; // Collision radius

            if (distance < collisionDistance) {
                // Gem collected!
                score++;
                gemsCollectedSinceLastHeart++;
                gems.remove(i);

                // Spawn heart after 4 gems collected if no heart is visible
                if (gemsCollectedSinceLastHeart >= 4 && heart == null) {
                    spawnHeart();
                    gemsCollectedSinceLastHeart = 0;
                }
            }
        }

        // Maintain 4-8 gems on screen
        maintainGems();

        // Update heart if present
        if (heart != null) {
            heart.update(deltaTime);

            // Check if heart has vanished
            if (heart.hasVanished()) {
                heart = null;
            } else {
                // Check collision with spaceship
                double dx = heart.x - spaceship.x;
                double dy = heart.y - spaceship.y;
                double distance = Math.sqrt(dx * dx + dy * dy);
                double collisionDistance = 30; // Collision radius

                if (distance < collisionDistance) {
                    // Heart collected!
                    collectHeart();
                    heart = null;
                }
            }
        }

        // Update enemy explosions
        for (int i = enemyExplosions.size() - 1; i >= 0; i--) {
            EnemyExplosion explosion = enemyExplosions.get(i);
            explosion.update(deltaTime);
            if (explosion.isComplete()) {
                // Respawn the enemy at a safe distance
                Enemy enemy = explosion.enemy;
                double minDistance = Game.gameHeight;
                double margin = 100 * Math.sqrt(scaleX * scaleY);

                // Find position at least screen height away from spaceship
                double x, y;
                int attempts = 0;
                do {
                    x = margin + random.nextDouble() * (Game.gameWidth - 2 * margin);
                    y = margin + random.nextDouble() * (Game.gameHeight - 2 * margin);
                    double dx = x - spaceship.x;
                    double dy = y - spaceship.y;
                    double distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance >= minDistance) {
                        break;
                    }
                    attempts++;
                } while (attempts < 100);

                enemy.x = x;
                enemy.y = y;
                enemy.angle = random.nextDouble() * 2 * Math.PI;

                // Add enemy back to active list
                enemies.add(enemy);
                enemyExplosions.remove(i);
            }
        }

        // Check for collisions between spaceship and enemies
        if (!spaceship.inWormhole && !gameOver && starshipExplosion == null) {
            double spaceshipRadius = spaceship.size / 2.0;
            double enemyRadius = 20.0 * Math.sqrt(scaleX * scaleY); // Approximate enemy size

            // In 3-life mode, skip collision detection if immune
            // In live-forever mode, always skip collision damage
            boolean canCollide = (gameMode != GameMode.LIVE_FOREVER) &&
                                 (gameMode != GameMode.LIVES || !isImmune);

            if (canCollide) {
                for (int i = enemies.size() - 1; i >= 0; i--) {
                    Enemy enemy = enemies.get(i);
                    double dx = spaceship.x - enemy.x;
                    double dy = spaceship.y - enemy.y;
                    double distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance < spaceshipRadius + enemyRadius) {
                        // Collision detected!

                        if (gameMode == GameMode.SHIELD) {
                            // Shield mode: calculate energy-based damage
                            // Calculate velocity vectors
                            double spaceshipVx = Math.cos(spaceship.angle) * spaceship.speed;
                            double spaceshipVy = Math.sin(spaceship.angle) * spaceship.speed;
                            double enemyVx = Math.cos(enemy.angle) * enemy.speed;
                            double enemyVy = Math.sin(enemy.angle) * enemy.speed;

                            // Calculate relative velocity (how fast they're approaching each other)
                            double relativeVx = spaceshipVx - enemyVx;
                            double relativeVy = spaceshipVy - enemyVy;
                            double relativeSpeed = Math.sqrt(relativeVx * relativeVx + relativeVy * relativeVy);

                            // Maximum possible relative speed (both at max speed in opposite directions)
                            double maxRelativeSpeed = spaceship.maxSpeed + spaceship.maxSpeed * 0.7; // Enemy can be up to 0.7 of max speed

                            // Calculate impact energy as percentage (0 to 1)
                            double impactEnergy = relativeSpeed / maxRelativeSpeed;

                            // Scale to 1-10% damage (minimum 1%, maximum 10%)
                            double damage = Math.max(1.0, impactEnergy * 10.0);

                            LOGGER.fine(String.format("Collision: relativeSpeed=%.1f, maxRelativeSpeed=%.1f, damage=%.1f%%",
                                    relativeSpeed, maxRelativeSpeed, damage));

                            // Shield mode: reduce shield by calculated damage
                            spaceship.shieldPercentage -= damage;
                            LOGGER.fine(String.format("Shield reduced by %.1f%%, remaining: %.1f%%", damage, spaceship.shieldPercentage));
                            if (spaceship.shieldPercentage <= 0) {
                                spaceship.shieldPercentage = 0;
                                // Game over - trigger starship explosion
                                starshipExplosion = new StarshipExplosion(spaceship.x, spaceship.y);
                                LOGGER.info("Game Over - Shield depleted!");
                            }
                        } else if (gameMode == GameMode.LIVES) {
                            // 3-life mode: reduce lives by 1 and start immunity
                            remainingLives--;
                            LOGGER.info("Life lost! Remaining lives: " + remainingLives);
                            if (remainingLives <= 0) {
                                remainingLives = 0;
                                // Game over - trigger starship explosion
                                starshipExplosion = new StarshipExplosion(spaceship.x, spaceship.y);
                                LOGGER.info("Game Over - All lives lost!");
                            } else {
                                // Start immunity period
                                isImmune = true;
                                immunityTimer = 0.0;
                                LOGGER.info("Immunity period started (5 seconds)");
                            }
                        }

                        // Remove enemy from active list and create explosion
                        enemies.remove(i);
                        enemyExplosions.add(new EnemyExplosion(enemy.x, enemy.y, enemy));
                    }
                }
            } else if (gameMode == GameMode.LIVE_FOREVER) {
                // In live-forever mode, still explode enemies on contact but no damage
                for (int i = enemies.size() - 1; i >= 0; i--) {
                    Enemy enemy = enemies.get(i);
                    double dx = spaceship.x - enemy.x;
                    double dy = spaceship.y - enemy.y;
                    double distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance < spaceshipRadius + enemyRadius) {
                        // Collision detected - explode enemy but no damage to spaceship
                        LOGGER.fine("Live Forever mode: Enemy collision, no damage taken");
                        enemies.remove(i);
                        enemyExplosions.add(new EnemyExplosion(enemy.x, enemy.y, enemy));
                    }
                }
            }
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.rgb(5, 5, 15));
        gc.fillRect(0, 0, Game.gameWidth, Game.gameHeight);

        // Render black holes first (behind everything)
        for (BlackHole blackHole : blackHoles) {
            blackHole.render(gc);
        }

        for (Star star : stars) {
            gc.setFill(Color.rgb(255, 255, 255, star.currentOpacity));
            gc.fillOval(star.x - star.size / 2, star.y - star.size / 2, star.size, star.size);
        }

        // Render smoke clouds behind spaceship
        for (SmokeCloud cloud : smokeClouds) {
            cloud.render(gc);
        }

        // Render spaceship (unless exploding or game over)
        if (starshipExplosion == null && !gameOver) {
            spaceship.render(gc);

            // Render immunity/protection shield circle
            if ((gameMode == GameMode.LIVES && isImmune) || gameMode == GameMode.LIVE_FOREVER) {
                double circleRadius = spaceship.size * 1.5; // Circle 1.5x the spaceship size

                // Calculate pulsing effect
                double pulseProgress;
                Color circleColor;

                if (gameMode == GameMode.LIVE_FOREVER) {
                    // Live Forever: constant golden shield, gentle pulse
                    pulseProgress = (gameTime % 2.0) / 2.0; // Slower pulse (2 second cycle)
                    double pulseScale = 0.95 + Math.sin(pulseProgress * Math.PI * 2) * 0.05; // Scale between 0.95 and 1.05
                    double actualRadius = circleRadius * pulseScale;
                    circleColor = Color.rgb(255, 215, 0, 0.7); // Gold with 70% opacity

                    gc.setStroke(circleColor);
                    gc.setLineWidth(3.0);
                    gc.strokeOval(
                        spaceship.x - actualRadius,
                        spaceship.y - actualRadius,
                        actualRadius * 2,
                        actualRadius * 2
                    );
                } else {
                    // 3-Lives immunity: cyan shield, faster pulse
                    pulseProgress = (immunityTimer % 0.5) / 0.5; // Pulse every 0.5 seconds
                    double pulseScale = 0.9 + Math.sin(pulseProgress * Math.PI * 2) * 0.1; // Scale between 0.9 and 1.1
                    double actualRadius = circleRadius * pulseScale;
                    circleColor = Color.rgb(0, 255, 255, 0.6); // Cyan with 60% opacity

                    gc.setStroke(circleColor);
                    gc.setLineWidth(3.0);
                    gc.strokeOval(
                        spaceship.x - actualRadius,
                        spaceship.y - actualRadius,
                        actualRadius * 2,
                        actualRadius * 2
                    );
                }
            }
        }

        // Render starship explosion if active
        if (starshipExplosion != null) {
            starshipExplosion.render(gc);
        }

        // Render enemies
        for (Enemy enemy : enemies) {
            enemy.render(gc);
        }

        // Render gems
        for (Gem gem : gems) {
            gem.render(gc);
        }

        // Render heart if present
        if (heart != null) {
            heart.render(gc);
        }

        // Render enemy explosions
        for (EnemyExplosion explosion : enemyExplosions) {
            explosion.render(gc);
        }

        // Render level message if active
        if (showLevelMessage) {
            // Calculate fade effect for last 1 second
            double opacity = 1.0;
            if (levelMessageTimer > levelMessageDuration - 1.0) {
                opacity = (levelMessageDuration - levelMessageTimer);
            }

            gc.setFill(Color.rgb(255, 0, 0, opacity));
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 48));
            String levelText = "Level " + currentLevel;

            // Position at bottom left corner
            double textX = 50;
            double textY = Game.gameHeight - 50;
            gc.fillText(levelText, textX, textY);
        }

        // Render debug message if active (center of screen)
        if (showDebugMessage) {
            // Calculate fade effect for first and last 0.5 seconds
            double opacity = 1.0;
            if (debugMessageTimer < 0.5) {
                // Fade in
                opacity = debugMessageTimer / 0.5;
            } else if (debugMessageTimer > debugMessageDuration - 0.5) {
                // Fade out
                opacity = (debugMessageDuration - debugMessageTimer) / 0.5;
            }

            gc.setFill(Color.rgb(0, 255, 0, opacity));
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 36));

            // Get text dimensions to center it
            javafx.scene.text.Text text = new javafx.scene.text.Text(debugMessageText);
            text.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 36));
            double textWidth = text.getLayoutBounds().getWidth();

            // Center on screen
            double centerX = (Game.gameWidth - textWidth) / 2;
            double centerY = Game.gameHeight / 2;
            gc.fillText(debugMessageText, centerX, centerY);
        }

        // Render debug info if debug mode is active (top right corner)
        if (GameSettings.getInstance().isDebugMode()) {
            gc.setFill(Color.rgb(0, 255, 255)); // Cyan
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.NORMAL, 24));

            String modeText = "Mode: " + spaceship.navigationMode.name();
            String speedText = String.format("Speed: %.1f", spaceship.speed);

            // Position at top left corner (moved from top right)
            double textX = 50;
            double textY = 30;
            gc.fillText(modeText, textX, textY);
            gc.fillText(speedText, textX, textY + 30);
        }

        // Render score at top right corner with color morphing
        // Color cycle: red -> magenta -> blue -> cyan -> green -> yellow -> red (10 seconds total)
        double colorProgress = (scoreColorTimer / 10.0) * 6.0; // 0 to 6 for 6 color transitions
        int colorPhase = (int) colorProgress;
        double phaseProgress = colorProgress - colorPhase;

        // Clamp colorPhase to 0-5 to avoid jumping to default case
        if (colorPhase >= 6) {
            colorPhase = 5;
            phaseProgress = 1.0; // Complete the last transition
        }

        Color scoreColor;
        switch (colorPhase) {
            case 0: // Red to Magenta (255,0,0) -> (255,0,255)
                scoreColor = Color.rgb(255, 0, (int)(255 * phaseProgress));
                break;
            case 1: // Magenta to Blue (255,0,255) -> (0,0,255)
                scoreColor = Color.rgb((int)(255 * (1 - phaseProgress)), 0, 255);
                break;
            case 2: // Blue to Cyan (0,0,255) -> (0,255,255)
                scoreColor = Color.rgb(0, (int)(255 * phaseProgress), 255);
                break;
            case 3: // Cyan to Green (0,255,255) -> (0,255,0)
                scoreColor = Color.rgb(0, 255, (int)(255 * (1 - phaseProgress)));
                break;
            case 4: // Green to Yellow (0,255,0) -> (255,255,0)
                scoreColor = Color.rgb((int)(255 * phaseProgress), 255, 0);
                break;
            case 5: // Yellow to Red (255,255,0) -> (255,0,0)
                scoreColor = Color.rgb(255, (int)(255 * (1 - phaseProgress)), 0);
                break;
            default:
                scoreColor = Color.rgb(255, 0, 0); // Red as fallback
                break;
        }

        gc.setFill(scoreColor);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.NORMAL, 48)); // 2x size (was 24)
        String scoreText = "Score: " + score;
        double scoreTextX = Game.gameWidth - 250; // Adjusted for larger text
        double scoreTextY = 80; // Lower position (was 30)
        gc.fillText(scoreText, scoreTextX, scoreTextY);

        // Render shield/lives/mode display (lower-right corner)
        String statusText;
        double statusValue;
        double maxValue;

        if (gameMode == GameMode.LIVES) {
            // Lives mode (unlimited lives with hearts)
            statusText = String.format("Remaining %d %s", remainingLives, remainingLives == 1 ? "life" : "lives");
            statusValue = remainingLives;
            maxValue = Math.max(5, remainingLives); // Dynamic max based on current lives
        } else if (gameMode == GameMode.LIVE_FOREVER) {
            // Live forever mode
            statusText = "Live Forever";
            statusValue = 100; // Always full for color purposes
            maxValue = 100;
        } else {
            // Shield mode
            statusText = String.format("Shield %d%%", (int) spaceship.shieldPercentage);
            statusValue = spaceship.shieldPercentage;
            maxValue = 100;
        }

        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 32));

        // Calculate color based on percentage (green -> yellow -> orange -> red)
        Color statusColor;
        double percentage = (statusValue / maxValue) * 100.0;

        if (percentage >= 75) {
            // 100% to 75%: Green to Yellow-Green
            double t = (100 - percentage) / 25.0; // 0 to 1
            int red = (int) (0 + t * 200);
            int green = 255;
            statusColor = Color.rgb(red, green, 0);
        } else if (percentage >= 50) {
            // 75% to 50%: Yellow to Orange
            double t = (75 - percentage) / 25.0; // 0 to 1
            int red = (int) (200 + t * 55);
            int green = (int) (255 - t * 100);
            statusColor = Color.rgb(red, green, 0);
        } else if (percentage >= 25) {
            // 50% to 25%: Orange to Red-Orange
            double t = (50 - percentage) / 25.0; // 0 to 1
            int red = 255;
            int green = (int) (155 - t * 105);
            statusColor = Color.rgb(red, green, 0);
        } else {
            // 25% to 0%: Red-Orange to Pure Red
            double t = (25 - percentage) / 25.0; // 0 to 1
            int red = 255;
            int green = (int) (50 - t * 50);
            statusColor = Color.rgb(red, green, 0);
        }

        gc.setFill(statusColor);

        // Get text dimensions for positioning
        javafx.scene.text.Text text = new javafx.scene.text.Text(statusText);
        text.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 32));
        double textWidth = text.getLayoutBounds().getWidth();

        // Position at lower-right corner with margin
        double statusTextX = Game.gameWidth - textWidth - 50;
        double statusTextY = Game.gameHeight - 30;
        gc.fillText(statusText, statusTextX, statusTextY);
    }

    @Override
    public void keyPressed(KeyCode key) {
        LOGGER.fine("PlayState keyPressed: " + key);
        if (key == KeyCode.ESCAPE) {
            LOGGER.fine("ESC pressed - returning to menu");
            Game.setFullscreen(false);
            LOGGER.fine("After setFullscreen(false) - WINDOW_WIDTH=" + Game.WINDOW_WIDTH + ", WINDOW_HEIGHT=" + Game.WINDOW_HEIGHT);
            gsm.setState(new MenuState(gsm));
            LOGGER.fine("MenuState set");
        } else if (key == KeyCode.D && keyShiftPressed) {
            // Toggle debug mode with Shift+D
            GameSettings settings = GameSettings.getInstance();
            boolean newDebugMode = !settings.isDebugMode();
            settings.setDebugMode(newDebugMode);

            showDebugMessage = true;
            debugMessageTimer = 0.0;
            debugMessageText = newDebugMode ? "Debug ON" : "Debug OFF";

            // Set logger level based on debug mode
            if (newDebugMode) {
                LOGGER.setLevel(Level.FINE);
                LOGGER.info("Debug mode enabled");
            } else {
                LOGGER.setLevel(Level.INFO);
                LOGGER.info("Debug mode disabled");
            }
        } else if (key == KeyCode.L && keyShiftPressed) {
            // Cycle through game modes with Shift+L: SHIELD -> LIVES -> LIVE_FOREVER -> SHIELD
            if (gameMode == GameMode.SHIELD) {
                gameMode = GameMode.LIVES;
                remainingLives = 3;
                isImmune = false;
                immunityTimer = 0.0;
                debugMessageText = "Lives Mode";
            } else if (gameMode == GameMode.LIVES) {
                gameMode = GameMode.LIVE_FOREVER;
                debugMessageText = "Live Forever Mode";
            } else {
                gameMode = GameMode.SHIELD;
                spaceship.shieldPercentage = 100.0;
                debugMessageText = "Shield Mode";
            }

            showDebugMessage = true;
            debugMessageTimer = 0.0;
            LOGGER.info("Game mode changed to: " + gameMode);
        } else if (key == KeyCode.SHIFT) {
            keyShiftPressed = true;
        } else if (key == KeyCode.UP) {
            keyUpPressed = true;
        } else if (key == KeyCode.DOWN) {
            keyDownPressed = true;
        } else if (key == KeyCode.LEFT) {
            keyLeftPressed = true;
        } else if (key == KeyCode.RIGHT) {
            keyRightPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyCode key) {
        if (key == KeyCode.SHIFT) {
            keyShiftPressed = false;
        } else if (key == KeyCode.UP) {
            keyUpPressed = false;
        } else if (key == KeyCode.DOWN) {
            keyDownPressed = false;
        } else if (key == KeyCode.LEFT) {
            keyLeftPressed = false;
        } else if (key == KeyCode.RIGHT) {
            keyRightPressed = false;
        }
    }

    @Override
    public void mouseClicked(double x, double y) {
        targetX = x;
        targetY = y;
        LOGGER.fine("Target set to: " + x + ", " + y);
    }

    @Override
    public void mouseMoved(double x, double y) {
        mouseX = x;
        mouseY = y;
    }

    @Override
    public void mousePressed(double x, double y) {
        mousePressed = true;
        mouseX = x;
        mouseY = y;
        targetX = x;
        targetY = y;
    }

    @Override
    public void mouseReleased(double x, double y) {
        mousePressed = false;
    }

    private enum FadeState { FADE_IN, VISIBLE, FADE_OUT, DONE }

    private class Star {
        double x, y;
        double size;
        double currentOpacity;

        // Fade state
        FadeState fadeState;
        double fadeTimer;
        double fadeInDuration;
        double fadeOutDuration;
        double visibleDuration;

        Star(double x, double y, double size) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.currentOpacity = 0.0; // Start invisible

            // Random fade timing between 2-7 seconds
            this.fadeInDuration = 2.0 + Math.random() * 5.0;
            this.fadeOutDuration = 2.0 + Math.random() * 5.0;
            this.visibleDuration = 2.0 + Math.random() * 5.0;
            this.fadeState = FadeState.FADE_IN;
            this.fadeTimer = 0.0;
        }

        void update(double deltaTime) {
            // Update fade state
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

    private enum NavigationMode {
        TRACKING,            // Following mouse target
        BOUNDARY_AVOIDANCE,  // Avoiding edge with random target
        BALLISTIC            // Flying straight in current direction (includes keyboard control)
    }

    private class Spaceship {
        double x, y;
        double angle; // In radians, 0 = right, PI/2 = down

        // Base values at reference resolution (1920x1080)
        private static final double BASE_SPEED = 200.0;
        private static final double BASE_MIN_SPEED = 50.0;
        private static final double BASE_MAX_SPEED = 400.0;
        private static final double BASE_ACCELERATION_RATE = 100.0;
        private static final double BASE_DECELERATION_RATE = 400.0;
        private static final double BASE_SIZE = 30.0;

        // Scaled values for current resolution
        double speed;
        double minSpeed;
        double maxSpeed;
        double accelerationRate;
        double decelerationRate;
        double rotationSpeed = 2.0; // radians per second (not scaled)
        double size;
        double smokeTimer = 0.0;
        double smokeInterval = 0.05; // Emit smoke every 0.05 seconds (doubled rate)
        Image sprite;
        double animationTimer = 0.0;
        int currentFrame = 0;

        // For tracking target changes
        double lastTargetX = Double.NaN;
        double lastTargetY = Double.NaN;
        NavigationMode navigationMode = NavigationMode.TRACKING; // Current navigation state

        // For communicating target changes back to PlayState
        boolean targetChanged = false;
        double newTargetX;
        double newTargetY;

        // Wormhole effect
        boolean inWormhole = false;
        double wormholeTimer = 0.0;
        double wormholeDuration = 0.5; // 0.5 seconds for shrink, 0.5 for expand
        boolean wormholeShrinking = true;
        double normalSize;
        double wormholeTargetX;
        double wormholeTargetY;
        boolean wormholeImmune = false; // Immune to wormhole after exiting
        double immuneBlackHoleX;
        double immuneBlackHoleY;
        double wormholeSourceX; // Source black hole position during shrinking
        double wormholeSourceY;
        double wormholeStartX; // Starting position when entering wormhole
        double wormholeStartY;

        // Shield system
        double shieldPercentage = 100.0;

        Spaceship(double x, double y) {
            this.x = x;
            this.y = y;
            this.angle = 0;
            this.sprite = StarshipSpriteLoader.createStarshipSprite();

            // Scale physics values based on resolution
            double scale = Math.sqrt(scaleX * scaleY); // Use geometric mean for speed scaling
            this.minSpeed = BASE_MIN_SPEED * scale;
            this.maxSpeed = BASE_MAX_SPEED * scale;
            this.speed = this.maxSpeed * 0.6; // Start at 60% of max speed
            this.accelerationRate = BASE_ACCELERATION_RATE * scale;
            this.decelerationRate = BASE_DECELERATION_RATE * scale;
            this.size = BASE_SIZE * scale;
            this.normalSize = this.size;
        }

        void update(double deltaTime, double targetX, double targetY, boolean mousePressed,
                    boolean keyUp, boolean keyDown, boolean keyLeft, boolean keyRight) {
            // Handle wormhole effect
            if (inWormhole) {
                wormholeTimer += deltaTime;

                if (wormholeShrinking) {
                    // Shrinking phase (0 to 0.5 seconds)
                    double progress = wormholeTimer / wormholeDuration;
                    size = normalSize * (1.0 - progress);

                    // Move spaceship toward the source black hole center
                    x = wormholeStartX + (wormholeSourceX - wormholeStartX) * progress;
                    y = wormholeStartY + (wormholeSourceY - wormholeStartY) * progress;

                    if (wormholeTimer >= wormholeDuration) {
                        // Switch to expanding phase at target location
                        wormholeShrinking = false;
                        wormholeTimer = 0.0;
                        x = wormholeTargetX;
                        y = wormholeTargetY;
                        size = 0.0;
                    }
                } else {
                    // Expanding phase (0 to 0.5 seconds)
                    double progress = wormholeTimer / wormholeDuration;
                    size = normalSize * progress;

                    if (wormholeTimer >= wormholeDuration) {
                        // Wormhole complete
                        inWormhole = false;
                        size = normalSize;
                        // Set immunity to the black hole we just exited from
                        wormholeImmune = true;
                        immuneBlackHoleX = x;
                        immuneBlackHoleY = y;
                        // Enter cruise mode - continue in current direction without tracking
                        navigationMode = NavigationMode.BALLISTIC;
                    }
                }

                // Don't process normal movement during wormhole
                return;
            }

            // Check if we're far enough from immune black hole to clear immunity
            if (wormholeImmune) {
                double dx = x - immuneBlackHoleX;
                double dy = y - immuneBlackHoleY;
                double dist = Math.sqrt(dx * dx + dy * dy);
                double blackHoleRadius = 60.0 * Math.sqrt(scaleX * scaleY); // Scale radius

                if (dist > blackHoleRadius) {
                    // Far enough away, clear immunity
                    wormholeImmune = false;
                }
            }

            // Keyboard controls take precedence over mouse controls
            boolean usingKeyboard = keyUp || keyDown || keyLeft || keyRight;

            if (usingKeyboard) {
                // Switch to ballistic mode (keyboard direct control)
                navigationMode = NavigationMode.BALLISTIC;

                // Reset target to current position to prevent unwanted mouse tracking
                targetX = x;
                targetY = y;
                lastTargetX = x;
                lastTargetY = y;

                // Keyboard rotation
                if (keyLeft) {
                    angle -= rotationSpeed * deltaTime;
                }
                if (keyRight) {
                    angle += rotationSpeed * deltaTime;
                }

                // Keyboard acceleration/deceleration
                if (keyUp) {
                    speed += accelerationRate * deltaTime;
                } else if (keyDown) {
                    speed -= decelerationRate * deltaTime;
                }
            } else {
                // Mouse control
                // Only switch to tracking if mouse is actively pressed (not just moved)
                if (mousePressed) {
                    navigationMode = NavigationMode.TRACKING; // Switch to tracking mode
                }

                // Update last target for tracking changes
                lastTargetX = targetX;
                lastTargetY = targetY;

                // Calculate distance to target
                double dx = targetX - x;
                double dy = targetY - y;
                double distanceToTarget = Math.sqrt(dx * dx + dy * dy);
                double targetAngle = Math.atan2(dy, dx);

                // Calculate angle difference
                double angleDiff = targetAngle - angle;
                // Normalize to -PI to PI
                while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
                while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

                // Calculate alignment (-1 = opposite direction, 0 = perpendicular, 1 = aligned)
                double alignment = Math.cos(angleDiff);

                // Check if target is reached (within reasonable distance)
                double reachedThreshold = 30.0 * Math.sqrt(scaleX * scaleY); // Scale threshold
                boolean targetReached = distanceToTarget < reachedThreshold;

                // Switch to ballistic mode if target is reached
                if (targetReached && navigationMode == NavigationMode.TRACKING) {
                    navigationMode = NavigationMode.BALLISTIC;
                } else if (targetReached && navigationMode == NavigationMode.BOUNDARY_AVOIDANCE) {
                    navigationMode = NavigationMode.BALLISTIC;
                }

                // Determine which angle to rotate toward
                double desiredAngle;
                if (navigationMode == NavigationMode.BALLISTIC) {
                    // Maintain current angle in ballistic mode
                    desiredAngle = angle;
                } else {
                    // Track target in all other modes (TRACKING, BOUNDARY_AVOIDANCE)
                    desiredAngle = targetAngle;
                }

                // Rotate toward desired angle
                if (desiredAngle != angle) {
                    double rotationDiff = desiredAngle - angle;
                    // Normalize to -PI to PI
                    while (rotationDiff > Math.PI) rotationDiff -= 2 * Math.PI;
                    while (rotationDiff < -Math.PI) rotationDiff += 2 * Math.PI;

                    double maxRotation = rotationSpeed * deltaTime;

                    // If within 30 degrees (PI/6 radians), scale rotation linearly
                    double thirtyDegrees = Math.PI / 6.0;
                    if (Math.abs(rotationDiff) < thirtyDegrees) {
                        // Scale rotation based on angle difference
                        double rotationScale = Math.abs(rotationDiff) / thirtyDegrees;
                        maxRotation *= rotationScale;
                    }

                    if (Math.abs(rotationDiff) < maxRotation) {
                        angle = desiredAngle;
                    } else {
                        angle += Math.signum(rotationDiff) * maxRotation;
                    }
                }

                // Adjust speed based on mouse press and alignment
                if (mousePressed) {
                    if (alignment > 0) {
                        // Pointing toward target - accelerate
                        speed += accelerationRate * deltaTime * alignment;
                    }
                    // If alignment <= 0 (pointing away), don't change speed
                }
            }

            // Boundary avoidance - applies to ALL navigation modes (keyboard and mouse)
            if (navigationMode != NavigationMode.BOUNDARY_AVOIDANCE) {
                // Calculate velocity
                double vx = Math.cos(angle) * speed;
                double vy = Math.sin(angle) * speed;

                // Base values at reference resolution, then scale
                double baseMargin = 100.0 * Math.sqrt(scaleX * scaleY);
                double speedFactor = speed / maxSpeed; // 0 to 1
                double margin = baseMargin + (speedFactor * 100.0 * Math.sqrt(scaleX * scaleY)); // Scale the variable part too

                double distToLeftEdge = x;
                double distToRightEdge = Game.gameWidth - x;
                double distToTopEdge = y;
                double distToBottomEdge = Game.gameHeight - y;

                boolean nearBoundary = false;

                // Check each edge with angle consideration
                if (distToLeftEdge < margin && vx < 0) {
                    double angleFactor = Math.abs(Math.cos(angle));
                    double effectiveMargin = margin * angleFactor;
                    if (distToLeftEdge < effectiveMargin) {
                        nearBoundary = true;
                    }
                } else if (distToRightEdge < margin && vx > 0) {
                    double angleFactor = Math.abs(Math.cos(angle));
                    double effectiveMargin = margin * angleFactor;
                    if (distToRightEdge < effectiveMargin) {
                        nearBoundary = true;
                    }
                }

                if (distToTopEdge < margin && vy < 0) {
                    double angleFactor = Math.abs(Math.sin(angle));
                    double effectiveMargin = margin * angleFactor;
                    if (distToTopEdge < effectiveMargin) {
                        nearBoundary = true;
                    }
                } else if (distToBottomEdge < margin && vy > 0) {
                    double angleFactor = Math.abs(Math.sin(angle));
                    double effectiveMargin = margin * angleFactor;
                    if (distToBottomEdge < effectiveMargin) {
                        nearBoundary = true;
                    }
                }

                // If near boundary, pick a random target at least screen-height away
                if (nearBoundary) {
                    double minDistance = Game.gameHeight;
                    double newX, newY;
                    int attempts = 0;
                    double targetMargin = 100 * Math.sqrt(scaleX * scaleY); // Scale margin for random target

                    do {
                        // Pick random point within screen bounds with scaled margin
                        newX = targetMargin + Math.random() * (Game.gameWidth - 2 * targetMargin);
                        newY = targetMargin + Math.random() * (Game.gameHeight - 2 * targetMargin);
                        double distance = Math.sqrt((newX - x) * (newX - x) + (newY - y) * (newY - y));

                        if (distance >= minDistance) {
                            break;
                        }
                        attempts++;
                    } while (attempts < 100);

                    // Signal target change to PlayState
                    this.targetChanged = true;
                    this.newTargetX = newX;
                    this.newTargetY = newY;

                    // Update local tracking
                    targetX = newX;
                    targetY = newY;
                    lastTargetX = newX;
                    lastTargetY = newY;
                    navigationMode = NavigationMode.BOUNDARY_AVOIDANCE;
                }
            }

            // Auto-steer during boundary avoidance (works for both keyboard and mouse modes)
            if (navigationMode == NavigationMode.BOUNDARY_AVOIDANCE) {
                // Calculate target angle
                double dx = targetX - x;
                double dy = targetY - y;
                double distanceToTarget = Math.sqrt(dx * dx + dy * dy);
                double targetAngle = Math.atan2(dy, dx);

                // Rotate toward target
                double rotationDiff = targetAngle - angle;
                // Normalize to -PI to PI
                while (rotationDiff > Math.PI) rotationDiff -= 2 * Math.PI;
                while (rotationDiff < -Math.PI) rotationDiff += 2 * Math.PI;

                double maxRotation = rotationSpeed * deltaTime;

                // If within 30 degrees (PI/6 radians), scale rotation linearly
                double thirtyDegrees = Math.PI / 6.0;
                if (Math.abs(rotationDiff) < thirtyDegrees) {
                    // Scale rotation based on angle difference
                    double rotationScale = Math.abs(rotationDiff) / thirtyDegrees;
                    maxRotation *= rotationScale;
                }

                if (Math.abs(rotationDiff) < maxRotation) {
                    angle = targetAngle;
                } else {
                    angle += Math.signum(rotationDiff) * maxRotation;
                }

                // Check if we reached the safe target
                double reachedThreshold = 30.0 * Math.sqrt(scaleX * scaleY);
                if (distanceToTarget < reachedThreshold) {
                    // Switch back to ballistic mode
                    navigationMode = NavigationMode.BALLISTIC;
                }
            }

            // Clamp speed to min/max
            speed = Math.max(minSpeed, Math.min(maxSpeed, speed));

            // Move in the direction of the nose
            double vx = Math.cos(angle) * speed * deltaTime;
            double vy = Math.sin(angle) * speed * deltaTime;

            x += vx;
            y += vy;

            // Boundary bounce - reflect angle when hitting edges
            boolean hitBoundary = false;

            if (x < 0) {
                x = 0;
                // Hit left edge - reflect horizontal component of angle
                // angle is reflected across vertical axis
                angle = Math.PI - angle;
                hitBoundary = true;
            } else if (x > Game.gameWidth) {
                x = Game.gameWidth;
                // Hit right edge - reflect horizontal component of angle
                angle = Math.PI - angle;
                hitBoundary = true;
            }

            if (y < 0) {
                y = 0;
                // Hit top edge - reflect vertical component of angle
                // angle is reflected across horizontal axis
                angle = -angle;
                hitBoundary = true;
            } else if (y > Game.gameHeight) {
                y = Game.gameHeight;
                // Hit bottom edge - reflect vertical component of angle
                angle = -angle;
                hitBoundary = true;
            }

            // Normalize angle and reduce speed on boundary hit
            if (hitBoundary) {
                while (angle > Math.PI) angle -= 2 * Math.PI;
                while (angle < -Math.PI) angle += 2 * Math.PI;

                // Reduce speed by 20% when hitting boundary, but not below minimum
                speed = Math.max(minSpeed, speed * 0.8);
            }

            // Emit smoke clouds
            smokeTimer += deltaTime;
            if (smokeTimer >= smokeInterval) {
                smokeTimer -= smokeInterval;
                emitSmoke();
            }

            // Animate sprite frames (cycle through light states)
            animationTimer += deltaTime;
            if (animationTimer >= 0.25) { // Change frame every 0.25 seconds
                animationTimer -= 0.25;
                currentFrame = (currentFrame + 1) % StarshipSpriteLoader.getFrameCount();
            }
        }

        void emitSmoke() {
            // Emit smoke from the back of the spaceship
            double backX = x - Math.cos(angle) * size * 0.3;
            double backY = y - Math.sin(angle) * size * 0.3;
            smokeClouds.add(new SmokeCloud(backX, backY, gameTime));
        }

        void enterWormhole(double sourceX, double sourceY, double targetX, double targetY) {
            inWormhole = true;
            wormholeTimer = 0.0;
            wormholeShrinking = true;
            wormholeSourceX = sourceX;
            wormholeSourceY = sourceY;
            wormholeTargetX = targetX;
            wormholeTargetY = targetY;
            wormholeStartX = x;
            wormholeStartY = y;
        }

        void render(GraphicsContext gc) {
            gc.save();
            gc.translate(x, y);
            gc.rotate(Math.toDegrees(angle));

            // Get current animation frame
            Image currentFrameImage = StarshipSpriteLoader.getStarshipFrame(currentFrame);

            // Calculate scale: resolution scale * wormhole effect scale
            double resolutionScale = Math.sqrt(scaleX * scaleY);
            double wormholeScale = size / normalSize;
            double totalScale = resolutionScale * wormholeScale;

            // Draw sprite centered with scaling
            double spriteWidth = currentFrameImage.getWidth() * totalScale;
            double spriteHeight = currentFrameImage.getHeight() * totalScale;
            gc.drawImage(currentFrameImage, -spriteWidth / 2, -spriteHeight / 2, spriteWidth, spriteHeight);

            gc.restore();
        }
    }

    private class SmokeCloud {
        double x, y;
        double lifetime;
        double maxLifetime; // Variable lifetime based on creation time
        double initialSize;
        double size;
        double opacity;

        SmokeCloud(double x, double y, double creationTime) {
            this.x = x;
            this.y = y;
            this.lifetime = 0.0;
            this.maxLifetime = 0.4 + Math.sin(creationTime * 2.0 * Math.PI) * 0.2;

            // Scale smoke size based on resolution
            double scale = Math.sqrt(scaleX * scaleY);
            this.initialSize = 8.0 * scale;
            this.size = initialSize;
            this.opacity = 0.8; // Start with bright/dense
        }

        void update(double deltaTime) {
            lifetime += deltaTime;

            double halfLife = maxLifetime / 2.0;

            if (lifetime <= halfLife) {
                // First half: grow 4x and reduce density to half
                double phase1Ratio = lifetime / halfLife;
                size = initialSize + (initialSize * 3.0) * phase1Ratio; // Grow from 1x to 4x
                opacity = 0.8 - (0.4 * phase1Ratio); // Reduce from 0.8 to 0.4
            } else {
                // Second half: keep size, fade to zero
                double phase2Ratio = (lifetime - halfLife) / halfLife;
                size = initialSize * 4.0; // Stay at 4x size
                opacity = 0.4 * (1.0 - phase2Ratio); // Fade from 0.4 to 0
            }
        }

        boolean isExpired() {
            return lifetime >= maxLifetime;
        }

        void render(GraphicsContext gc) {
            gc.setFill(Color.rgb(180, 180, 180, opacity));
            gc.fillOval(x - size / 2, y - size / 2, size, size);
        }
    }

    private class BlackHole {
        double x, y;
        double vx, vy; // Velocity
        double speed; // Current speed magnitude
        double angle; // Current movement angle

        // Base values at reference resolution (1920x1080)
        private static final double BASE_MIN_SPEED = 10.0;
        private static final double BASE_MAX_SPEED = 50.0;
        private static final double BASE_ACCELERATION = 2.0;

        // Scaled values for current resolution
        double minSpeed;
        double maxSpeed;
        double acceleration;
        double rotationRate = 0.3; // radians per second (not scaled)
        double animationTimer = 0.0;
        int currentFrame = 0;
        int colorScheme; // Which color scheme sprite to use
        double directionChangeTimer = 0.0;
        double speedChangeTimer = 0.0;

        // Fade-in effect
        double fadeInTimer = 0.0;
        double fadeInDuration = 2.0; // 2 seconds fade-in
        double opacity = 0.0;

        // Wormhole exit animation
        boolean isExitPortal = false;
        double exitAnimationTimer = 0.0;
        double exitAnimationDuration = 1.0; // 1 second animation
        double normalScale = 1.0;
        double currentScale = 1.0;

        Random rng = new Random();

        BlackHole(double x, double y, int colorScheme) {
            this.x = x;
            this.y = y;
            this.colorScheme = colorScheme;

            // Scale physics values based on resolution
            double scale = Math.sqrt(scaleX * scaleY); // Use geometric mean for speed scaling
            this.minSpeed = BASE_MIN_SPEED * scale;
            this.maxSpeed = BASE_MAX_SPEED * scale;
            this.acceleration = BASE_ACCELERATION * scale;

            this.speed = minSpeed + rng.nextDouble() * (maxSpeed - minSpeed);
            this.angle = rng.nextDouble() * 2 * Math.PI;
            this.vx = Math.cos(angle) * speed;
            this.vy = Math.sin(angle) * speed;
        }

        void update(double deltaTime, BlackHole other) {
            // Update fade-in
            if (fadeInTimer < fadeInDuration) {
                fadeInTimer += deltaTime;
                opacity = Math.min(1.0, fadeInTimer / fadeInDuration);
            } else {
                opacity = 1.0;
            }

            // Update exit portal animation
            if (isExitPortal) {
                exitAnimationTimer += deltaTime;

                if (exitAnimationTimer < exitAnimationDuration / 2.0) {
                    // First half: expand to 3x
                    double progress = exitAnimationTimer / (exitAnimationDuration / 2.0);
                    currentScale = normalScale + (3.0 - normalScale) * progress;
                } else if (exitAnimationTimer < exitAnimationDuration) {
                    // Second half: shrink back to normal
                    double progress = (exitAnimationTimer - exitAnimationDuration / 2.0) / (exitAnimationDuration / 2.0);
                    currentScale = 3.0 - (3.0 - normalScale) * progress;
                } else {
                    // Animation complete
                    currentScale = normalScale;
                    isExitPortal = false;
                    exitAnimationTimer = 0.0;
                }
            } else {
                currentScale = normalScale;
            }

            // Animate sprite frames
            animationTimer += deltaTime;
            if (animationTimer >= 0.1) { // Change frame every 0.1 seconds
                animationTimer -= 0.1;
                currentFrame = (currentFrame + 1) % BlackHoleSpriteLoader.getFrameCount();
            }

            double centerX = Game.gameWidth / 2.0;
            double centerY = Game.gameHeight / 2.0;
            double targetAngle = angle; // Start with current angle
            double targetSpeed = speed;

            // PRIORITY 1: Boundary avoidance
            double edgeMargin = 150.0 * Math.sqrt(scaleX * scaleY); // Scale edge margin
            double distToEdgeX = Math.min(x, Game.gameWidth - x);
            double distToEdgeY = Math.min(y, Game.gameHeight - y);
            double minDistToEdge = Math.min(distToEdgeX, distToEdgeY);

            boolean nearBoundary = minDistToEdge < edgeMargin;
            boolean pastBoundary = x < 0 || x > Game.gameWidth || y < 0 || y > Game.gameHeight;

            if (pastBoundary || nearBoundary) {
                // Calculate angle toward center
                double toCenterAngle = Math.atan2(centerY - y, centerX - x);

                if (pastBoundary) {
                    // Must turn toward center immediately
                    targetAngle = toCenterAngle;
                } else {
                    // Near boundary - steer toward center and slow down
                    double edgeFactor = 1.0 - (minDistToEdge / edgeMargin);
                    targetAngle = toCenterAngle;

                    // Slow down when approaching edge
                    targetSpeed = speed * (1.0 - edgeFactor * 0.7); // Reduce speed up to 70%
                    targetSpeed = Math.max(minSpeed, targetSpeed);
                }
            } else {
                // PRIORITY 2: Maintain distance from other black hole
                double dx = x - other.x;
                double dy = y - other.y;
                double distanceToOther = Math.sqrt(dx * dx + dy * dy);
                double halfScreenHeight = Game.gameHeight / 2.0;

                if (distanceToOther < halfScreenHeight) {
                    double myDistToCenter = Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY));
                    double otherDistToCenter = Math.sqrt((other.x - centerX) * (other.x - centerX) + (other.y - centerY) * (other.y - centerY));

                    // If I'm closer to center, steer away from the other
                    if (myDistToCenter < otherDistToCenter) {
                        double awayAngle = Math.atan2(y - other.y, x - other.x);
                        targetAngle = awayAngle;
                    }
                } else {
                    // PRIORITY 3: Random movement (only when not avoiding boundaries or other black hole)
                    // Slowly change speed
                    speedChangeTimer += deltaTime;
                    if (speedChangeTimer >= 2.0) {
                        speedChangeTimer -= 2.0;
                        if (rng.nextBoolean()) {
                            targetSpeed = Math.min(maxSpeed, speed + acceleration * 2.0);
                        } else {
                            targetSpeed = Math.max(minSpeed, speed - acceleration * 2.0);
                        }
                    }

                    // Slowly change direction
                    directionChangeTimer += deltaTime;
                    if (directionChangeTimer >= 3.0) {
                        directionChangeTimer -= 3.0;
                        double angleChange = (rng.nextDouble() - 0.5) * Math.PI * 0.3; // Random change
                        targetAngle = angle + angleChange;
                    } else {
                        targetAngle = angle; // Keep current angle
                    }
                }
            }

            // Apply angle change with 30 degrees per second limit
            double maxAngleChange = Math.toRadians(30.0) * deltaTime; // 30 degrees per second
            double angleDiff = targetAngle - angle;

            // Normalize angle difference to -PI to PI
            while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
            while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

            // Clamp angle change to max rate
            if (Math.abs(angleDiff) <= maxAngleChange) {
                angle = targetAngle;
            } else {
                angle += Math.signum(angleDiff) * maxAngleChange;
            }

            // Apply speed change gradually
            speed += (targetSpeed - speed) * Math.min(1.0, deltaTime * 2.0);
            speed = Math.max(minSpeed, Math.min(maxSpeed, speed));

            // Update velocity based on angle and speed
            vx = Math.cos(angle) * speed;
            vy = Math.sin(angle) * speed;

            // Update position
            x += vx * deltaTime;
            y += vy * deltaTime;

            // Hard clamp to boundaries with margin
            double clampMargin = 60 * Math.sqrt(scaleX * scaleY); // Scale clamp margin
            x = Math.max(clampMargin, Math.min(Game.gameWidth - clampMargin, x));
            y = Math.max(clampMargin, Math.min(Game.gameHeight - clampMargin, y));
        }

        void triggerExitAnimation() {
            isExitPortal = true;
            exitAnimationTimer = 0.0;
        }

        void render(GraphicsContext gc) {
            Image frame = BlackHoleSpriteLoader.getBlackHoleFrame(colorScheme, currentFrame);
            if (frame != null) {
                // Scale sprite based on resolution and exit animation
                double resolutionScale = Math.sqrt(scaleX * scaleY);
                double totalScale = resolutionScale * currentScale;
                double width = frame.getWidth() * totalScale;
                double height = frame.getHeight() * totalScale;

                // Apply opacity for fade-in effect
                gc.save();
                gc.setGlobalAlpha(opacity);
                gc.drawImage(frame, x - width / 2, y - height / 2, width, height);
                gc.restore();
            }
        }
    }

    private class Enemy {
        double x, y;
        double speed;
        double angle; // Current movement direction
        int enemyType;
        double animationTimer = 0.0;
        int currentFrame = 0;
        double previousSpaceshipSpeed; // Track spaceship speed to detect acceleration/deceleration
        double speedOscillationTimer = 0.0; // For skill 0x02 speed oscillation
        double fadeInTimer = 0.0; // Fade in effect
        double fadeInDuration = 1.0; // 1 second fade in

        // Blind steering when spaceship is in wormhole
        double blindTargetX;
        double blindTargetY;
        boolean isBlindSteering = false;
        double blindSteeringTimer = 0.0;
        double blindSteeringDuration = 2.0; // Will be randomized when blind steering starts

        // Skill 0x04: Prediction time randomization
        double predictionTime = 1.0; // Seconds to predict ahead (0.1 to 1.5)
        double predictionUpdateTimer = 0.0; // Time since last prediction update
        double predictionUpdateInterval = 10.0; // Update every 10 seconds

        Random rng = new Random();

        Enemy(double x, double y, int enemyType) {
            this.x = x;
            this.y = y;
            this.enemyType = enemyType;
            this.previousSpaceshipSpeed = spaceship.speed;
            this.speed = spaceship.speed * 0.5; // Start at 0.5 of spaceship's current speed
            this.angle = rng.nextDouble() * 2 * Math.PI;

            // Initialize random blind target
            pickRandomBlindTarget();

            // Initialize random prediction time for skill 0x04
            if ((enemyType & 0x04) != 0) {
                predictionTime = 0.1 + rng.nextDouble() * 1.4; // 0.1 to 1.5 seconds
            }
        }

        void pickRandomBlindTarget() {
            blindTargetX = 100 + rng.nextDouble() * (Game.gameWidth - 200);
            blindTargetY = 100 + rng.nextDouble() * (Game.gameHeight - 200);
        }

        void update(double deltaTime, double spaceshipX, double spaceshipY) {
            // Update fade in
            if (fadeInTimer < fadeInDuration) {
                fadeInTimer += deltaTime;
            }

            // Animate sprite frames
            animationTimer += deltaTime;
            if (animationTimer >= 0.1) {
                animationTimer -= 0.1;
                currentFrame = (currentFrame + 1) % com.game.util.EnemySpriteLoader.getFrameCount();
            }

            // Check if spaceship is in wormhole or just exited
            if (spaceship.inWormhole && !isBlindSteering) {
                // Spaceship just entered wormhole - start blind steering
                isBlindSteering = true;
                blindSteeringTimer = 0.0;
                // Randomize recovery time between 0.0-2.0 seconds for this enemy
                blindSteeringDuration = rng.nextDouble() * 2.0;
                pickRandomBlindTarget();
            } else if (!spaceship.inWormhole && isBlindSteering) {
                // Spaceship exited wormhole - continue blind steering until this enemy's timer expires
                blindSteeringTimer += deltaTime;
                if (blindSteeringTimer >= blindSteeringDuration) {
                    // End blind steering for this enemy
                    isBlindSteering = false;
                    blindSteeringTimer = 0.0;
                }
            } else if (spaceship.inWormhole && isBlindSteering) {
                // Still in wormhole, keep timer at 0
                blindSteeringTimer = 0.0;
            }

            // Determine target speed based on spaceship
            double baseTargetSpeed = spaceship.maxSpeed * 0.5;

            // Skill 0x02: Speed oscillates between 0.3 of max plane speed and 0.9 of plane's max speed
            if ((enemyType & 0x02) != 0) {
                speedOscillationTimer += deltaTime;
                // Oscillate with 4 second period (0 to 1 range)
                double oscillation = (Math.sin(speedOscillationTimer * Math.PI * 0.5) + 1.0) / 2.0;
                // Map between 0.3 of max speed and 0.9 of max speed
                double minSpeed = spaceship.maxSpeed * 0.3;
                double maxSpeed = spaceship.maxSpeed * 0.9;
                baseTargetSpeed = minSpeed + oscillation * (maxSpeed - minSpeed);
            }

            // Detect if spaceship is accelerating or decelerating
            boolean spaceshipAccelerating = spaceship.speed > previousSpaceshipSpeed;
            boolean spaceshipDecelerating = spaceship.speed < previousSpaceshipSpeed;

            if (spaceshipAccelerating) {
                // Accelerate at half the spaceship's rate
                double spaceshipAccelRate = (spaceship.speed - previousSpaceshipSpeed) / deltaTime;
                speed += (spaceshipAccelRate * 0.5) * deltaTime;

                // Clamp to target speed (don't exceed 0.5 of spaceship speed, unless skill 0x02)
                if ((enemyType & 0x02) == 0 && speed > baseTargetSpeed) {
                    speed = baseTargetSpeed;
                }
            } else if (spaceshipDecelerating) {
                // Instantly match spaceship speed
                speed = spaceship.speed;
            } else {
                // Spaceship speed unchanged - gradually converge to target speed
                speed += (baseTargetSpeed - speed) * Math.min(1.0, deltaTime * 2.0);
            }

            // Update previous spaceship speed for next frame
            previousSpaceshipSpeed = spaceship.speed;

            // Determine target position
            double targetX, targetY;

            if (isBlindSteering) {
                // Use random blind target
                targetX = blindTargetX;
                targetY = blindTargetY;

                // Check if reached blind target, pick a new one
                double dx = targetX - x;
                double dy = targetY - y;
                double distToBlindTarget = Math.sqrt(dx * dx + dy * dy);
                if (distToBlindTarget < 50) {
                    pickRandomBlindTarget();
                }
            } else {
                // Normal tracking
                targetX = spaceshipX;
                targetY = spaceshipY;

                // Skill 0x04: Predict spaceship position (0.1 to 1.5 seconds ahead, including rolling state)
                if ((enemyType & 0x04) != 0) {
                    // Update prediction time every 10 seconds
                    predictionUpdateTimer += deltaTime;
                    if (predictionUpdateTimer >= predictionUpdateInterval) {
                        predictionUpdateTimer = 0.0;
                        predictionTime = 0.1 + rng.nextDouble() * 1.4; // Random 0.1 to 1.5 seconds
                    }

                    // Calculate base predicted position from current velocity
                    double predictedX = spaceshipX + Math.cos(spaceship.angle) * spaceship.speed * predictionTime;
                    double predictedY = spaceshipY + Math.sin(spaceship.angle) * spaceship.speed * predictionTime;

                    // Factor in rolling state - rolling affects lateral movement
                    // Rolling causes lateral drift perpendicular to forward direction
                    if (keyLeftPressed) {
                        // Drift perpendicular (left side) - 90 degrees CCW from angle
                        double rollInfluence = spaceship.speed * 0.3; // Rolling drift is 30% of forward speed
                        predictedX += Math.cos(spaceship.angle - Math.PI / 2) * rollInfluence * predictionTime;
                        predictedY += Math.sin(spaceship.angle - Math.PI / 2) * rollInfluence * predictionTime;
                    } else if (keyRightPressed) {
                        // Drift perpendicular (right side) - 90 degrees CW from angle
                        double rollInfluence = spaceship.speed * 0.3;
                        predictedX += Math.cos(spaceship.angle + Math.PI / 2) * rollInfluence * predictionTime;
                        predictedY += Math.sin(spaceship.angle + Math.PI / 2) * rollInfluence * predictionTime;
                    }

                    targetX = predictedX;
                    targetY = predictedY;
                }
            }

            // Calculate angle to target
            double dx = targetX - x;
            double dy = targetY - y;
            double targetAngle = Math.atan2(dy, dx);

            // Determine rotation speed based on skill 0x08
            double rotationSpeed;
            if ((enemyType & 0x08) != 0) {
                // Skill 0x08: Rotate 3 times faster than the plane
                rotationSpeed = spaceship.rotationSpeed * 3.0;
            } else {
                // Normal: 3 times slower than the spaceship's rotation speed
                rotationSpeed = spaceship.rotationSpeed / 3.0;
            }

            // Slowly turn toward target
            double angleDiff = targetAngle - angle;
            // Normalize to -PI to PI
            while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
            while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

            // Apply rotation
            double maxRotation = rotationSpeed * deltaTime;
            if (Math.abs(angleDiff) < maxRotation) {
                angle = targetAngle;
            } else {
                angle += Math.signum(angleDiff) * maxRotation;
            }

            // Calculate movement angle (with wiggle if applicable)
            double moveAngle = angle;

            // Skill 0x01 != 0: Wiggle - add sinusoidal variation to direction (45 degrees)
            // Affects enemies: 1, 3, 5, 7, 9, 11, 13, 15
            if ((enemyType & 0x01) != 0) {
                double wiggle = Math.toRadians(45) * Math.sin(gameTime * Math.PI);
                moveAngle += wiggle;
            }

            // Move in current direction
            double vx = Math.cos(moveAngle) * speed * deltaTime;
            double vy = Math.sin(moveAngle) * speed * deltaTime;

            x += vx;
            y += vy;

            // No wrapping - enemies can go off screen and come back
            // They stay invisible while off screen but continue moving
        }

        void render(GraphicsContext gc) {
            // Only render if on screen
            if (x >= -40 && x <= Game.gameWidth + 40 && y >= -40 && y <= Game.gameHeight + 40) {
                Image frame = com.game.util.EnemySpriteLoader.getEnemyFrame(enemyType, currentFrame);
                if (frame != null) {
                    // Scale sprite based on resolution
                    double scale = Math.sqrt(scaleX * scaleY);
                    double width = frame.getWidth() * scale;
                    double height = frame.getHeight() * scale;

                    // Calculate fade in opacity
                    double opacity = Math.min(1.0, fadeInTimer / fadeInDuration);

                    gc.save();
                    gc.setGlobalAlpha(opacity);
                    gc.translate(x, y);
                    // Rotate with -90 degrees offset so top of sprite (front) points in movement direction
                    gc.rotate(Math.toDegrees(angle) + 90);
                    gc.drawImage(frame, -width / 2, -height / 2, width, height);
                    gc.restore();
                }
            }
        }
    }

    // Enemy explosion animation
    private class EnemyExplosion {
        double x, y;
        Enemy enemy; // Reference to the enemy being respawned
        double timer = 0.0;
        double duration = 2.0; // 2 seconds for 32 frames at ~16 fps
        int currentFrame = 0;

        EnemyExplosion(double x, double y, Enemy enemy) {
            this.x = x;
            this.y = y;
            this.enemy = enemy;
        }

        void update(double deltaTime) {
            timer += deltaTime;
            // Calculate frame based on timer (32 frames over 2 seconds)
            currentFrame = (int) ((timer / duration) * ExplosionSpriteLoader.getFrameCount());
            if (currentFrame >= ExplosionSpriteLoader.getFrameCount()) {
                currentFrame = ExplosionSpriteLoader.getFrameCount() - 1;
            }
        }

        boolean isComplete() {
            return timer >= duration;
        }

        void render(GraphicsContext gc) {
            Image frame = ExplosionSpriteLoader.getExplosionFrame(currentFrame);
            if (frame != null) {
                // Scale sprite based on resolution
                double scale = Math.sqrt(scaleX * scaleY);
                double width = frame.getWidth() * scale;
                double height = frame.getHeight() * scale;

                gc.drawImage(frame, x - width / 2, y - height / 2, width, height);
            }
        }
    }

    // Starship explosion animation
    private class StarshipExplosion {
        double x, y;
        double timer = 0.0;
        double duration = 10.0; // 10 seconds for 256 frames at 25.6 fps
        int currentFrame = 0;

        StarshipExplosion(double x, double y) {
            this.x = x;
            this.y = y;
            LOGGER.info("Starship explosion started at (" + x + ", " + y + ")");
        }

        void update(double deltaTime) {
            timer += deltaTime;
            // Calculate frame based on timer (256 frames over 10 seconds)
            currentFrame = (int) ((timer / duration) * StarshipExplosionSpriteLoader.getFrameCount());
            if (currentFrame >= StarshipExplosionSpriteLoader.getFrameCount()) {
                currentFrame = StarshipExplosionSpriteLoader.getFrameCount() - 1;
            }
        }

        boolean isComplete() {
            return timer >= duration;
        }

        void render(GraphicsContext gc) {
            Image frame = StarshipExplosionSpriteLoader.getExplosionFrame(currentFrame);
            if (frame != null) {
                // Scale sprite based on resolution
                double scale = Math.sqrt(scaleX * scaleY);
                double width = frame.getWidth() * scale;
                double height = frame.getHeight() * scale;

                gc.drawImage(frame, x - width / 2, y - height / 2, width, height);
            }
        }
    }

    // Gem spawning and maintenance methods
    private void spawnInitialGems() {
        int numGems = 4 + random.nextInt(5); // 4-8 gems
        for (int i = 0; i < numGems; i++) {
            spawnGem();
        }
    }

    private void maintainGems() {
        // Maintain between 4-8 gems
        if (gems.size() < 4) {
            spawnGem();
        } else if (gems.size() > 8) {
            // Remove excess gems (shouldn't happen, but just in case)
            while (gems.size() > 8) {
                gems.remove(gems.size() - 1);
            }
        } else if (gems.size() < 8 && random.nextDouble() < 0.01) {
            // 1% chance per frame to spawn a new gem if below max
            spawnGem();
        }
    }

    private void spawnGem() {
        double minDistanceFromShip = Game.gameHeight; // At least screen height from ship
        double minDistanceFromGems = Game.gameWidth / 4.0; // 1/4 screen width from other gems
        double margin = 50;

        int maxAttempts = 100;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            double x = margin + random.nextDouble() * (Game.gameWidth - 2 * margin);
            double y = margin + random.nextDouble() * (Game.gameHeight - 2 * margin);

            // Check distance from spaceship
            double dx = x - spaceship.x;
            double dy = y - spaceship.y;
            double distanceFromShip = Math.sqrt(dx * dx + dy * dy);

            if (distanceFromShip < minDistanceFromShip) {
                continue; // Too close to ship
            }

            // Check distance from other gems
            boolean tooCloseToGem = false;
            for (Gem gem : gems) {
                double gdx = x - gem.x;
                double gdy = y - gem.y;
                double distanceFromGem = Math.sqrt(gdx * gdx + gdy * gdy);
                if (distanceFromGem < minDistanceFromGems) {
                    tooCloseToGem = true;
                    break;
                }
            }

            if (!tooCloseToGem) {
                // Valid position found
                int gemType = random.nextInt(16); // 0-15 for 16 different gem types
                gems.add(new Gem(x, y, gemType));
                return;
            }
        }

        // If we couldn't find a valid position after max attempts, spawn anyway
        double x = margin + random.nextDouble() * (Game.gameWidth - 2 * margin);
        double y = margin + random.nextDouble() * (Game.gameHeight - 2 * margin);
        int gemType = random.nextInt(16);
        gems.add(new Gem(x, y, gemType));
    }

    private class Gem {
        double x, y;
        int gemType;
        double animationTimer = 0.0;
        int currentFrame = 0;
        double fadeInTimer = 0.0; // Fade in effect
        double fadeInDuration = 1.0; // 1 second fade in

        Gem(double x, double y, int gemType) {
            this.x = x;
            this.y = y;
            this.gemType = gemType;

            // Load gem sprites if not already loaded
            if (gemSpritesCache == null) {
                loadGemSprites();
            }
        }

        void update(double deltaTime) {
            // Update fade in
            if (fadeInTimer < fadeInDuration) {
                fadeInTimer += deltaTime;
            }

            // Animate sprite frames
            animationTimer += deltaTime;
            if (animationTimer >= 0.05) { // 20 FPS animation
                animationTimer -= 0.05;
                currentFrame = (currentFrame + 1) % 16;
            }
        }

        void render(GraphicsContext gc) {
            if (gemSpritesCache != null) {
                Image frame = gemSpritesCache[gemType * 16 + currentFrame];
                if (frame != null) {
                    double scale = Math.sqrt(scaleX * scaleY) * 0.8; // Slightly smaller than enemies
                    double width = frame.getWidth() * scale;
                    double height = frame.getHeight() * scale;

                    // Calculate fade in opacity
                    double opacity = Math.min(1.0, fadeInTimer / fadeInDuration);

                    gc.save();
                    gc.setGlobalAlpha(opacity);
                    gc.drawImage(frame, x - width / 2, y - height / 2, width, height);
                    gc.restore();
                }
            }
        }
    }

    // Gem sprite cache at class level
    private Image[] gemSpritesCache = null;

    private void loadGemSprites() {
        gemSpritesCache = new Image[16 * 16]; // 16 gem types * 16 frames each
        for (int type = 0; type < 16; type++) {
            String path = "images/gem_sheet_" + type + ".png";
            try {
                Image sheet = new Image(getClass().getClassLoader().getResourceAsStream(path));
                int frameSize = 80;
                int framesPerRow = 4;

                for (int frame = 0; frame < 16; frame++) {
                    int col = frame % framesPerRow;
                    int row = frame / framesPerRow;
                    int x = col * frameSize;
                    int y = row * frameSize;

                    javafx.scene.image.PixelReader reader = sheet.getPixelReader();
                    javafx.scene.image.WritableImage frameImage = new javafx.scene.image.WritableImage(
                        reader, x, y, frameSize, frameSize
                    );
                    gemSpritesCache[type * 16 + frame] = frameImage;
                }
            } catch (Exception e) {
                LOGGER.warning("Failed to load gem sprite: " + path);
            }
        }
    }

    // Heart spawning and collection methods
    private void spawnHeart() {
        double minDistanceFromShip = Game.gameHeight; // At least screen height from ship
        double margin = 50;

        int maxAttempts = 100;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            double x = margin + random.nextDouble() * (Game.gameWidth - 2 * margin);
            double y = margin + random.nextDouble() * (Game.gameHeight - 2 * margin);

            // Check distance from spaceship
            double dx = x - spaceship.x;
            double dy = y - spaceship.y;
            double distanceFromShip = Math.sqrt(dx * dx + dy * dy);

            if (distanceFromShip >= minDistanceFromShip) {
                // Valid position found
                heart = new Heart(x, y);
                return;
            }
        }

        // If we couldn't find a valid position after max attempts, spawn anyway
        double x = margin + random.nextDouble() * (Game.gameWidth - 2 * margin);
        double y = margin + random.nextDouble() * (Game.gameHeight - 2 * margin);
        heart = new Heart(x, y);
    }

    private void collectHeart() {
        if (heart == null) return;

        double heartPercentage = heart.getPercentage(); // 100% to 10%

        if (gameMode == GameMode.LIVES) {
            // Add a life (unlimited)
            remainingLives = remainingLives + 1;
            LOGGER.info("Heart collected! Lives: " + remainingLives);
        } else if (gameMode == GameMode.SHIELD) {
            // Fill shield based on heart percentage
            // heartPercentage ranges from 100 (just spawned) to 10 (about to vanish)
            // Formula: newShield = currentShield + (heartPercentage / 100) * (100 - currentShield)
            double fillAmount = (heartPercentage / 100.0) * (100.0 - spaceship.shieldPercentage);
            spaceship.shieldPercentage = Math.min(100.0, spaceship.shieldPercentage + fillAmount);
            LOGGER.info("Heart collected! Shield: " + spaceship.shieldPercentage + "%");
        }
        // LIVE_FOREVER mode: no effect
    }

    private class Heart {
        double x, y;
        double timer = 0.0;
        double duration = 30.0; // 30 seconds to vanish
        double fadeInTimer = 0.0; // Fade in effect
        double fadeInDuration = 1.0; // 1 second fade in
        double animationTimer = 0.0;
        int currentFrame = 0;

        Heart(double x, double y) {
            this.x = x;
            this.y = y;

            // Load heart sprites if not already loaded
            if (heartSpritesCache == null) {
                loadHeartSprites();
            }
        }

        void update(double deltaTime) {
            timer += deltaTime;

            // Update fade in
            if (fadeInTimer < fadeInDuration) {
                fadeInTimer += deltaTime;
            }

            // Animate sprite frames
            animationTimer += deltaTime;
            if (animationTimer >= 0.0625) { // 16 FPS animation
                animationTimer -= 0.0625;
                currentFrame = (currentFrame + 1) % 16;
            }
        }

        boolean hasVanished() {
            return timer >= duration;
        }

        double getPercentage() {
            // Linear interpolation from 100% to 10% over 30 seconds
            double progress = timer / duration;
            return 100.0 - (progress * 90.0); // 100% -> 10%
        }

        void render(GraphicsContext gc) {
            if (heartSpritesCache != null && currentFrame < heartSpritesCache.length && heartSpritesCache[currentFrame] != null) {
                Image frame = heartSpritesCache[currentFrame];
                double scale = Math.sqrt(scaleX * scaleY) * 0.8;
                double width = frame.getWidth() * scale;
                double height = frame.getHeight() * scale;

                // Calculate total opacity (fade in + dimming)
                double fadeInOpacity = Math.min(1.0, fadeInTimer / fadeInDuration);
                double dimmingOpacity = getPercentage() / 100.0; // 1.0 to 0.1
                double opacity = fadeInOpacity * dimmingOpacity;

                gc.save();
                gc.setGlobalAlpha(opacity);
                gc.drawImage(frame, x - width / 2, y - height / 2, width, height);
                gc.restore();
            }
        }
    }

    // Heart sprite cache at class level
    private Image[] heartSpritesCache = null;

    private void loadHeartSprites() {
        String path = "images/heart_sheet.png";
        try {
            Image sheet = new Image(getClass().getClassLoader().getResourceAsStream(path));
            int frameSize = 80;
            int framesPerRow = 4;
            int numFrames = 16;

            heartSpritesCache = new Image[numFrames];
            for (int frame = 0; frame < numFrames; frame++) {
                int col = frame % framesPerRow;
                int row = frame / framesPerRow;
                int frameX = col * frameSize;
                int frameY = row * frameSize;

                javafx.scene.image.PixelReader reader = sheet.getPixelReader();
                javafx.scene.image.WritableImage frameImage = new javafx.scene.image.WritableImage(
                    reader, frameX, frameY, frameSize, frameSize
                );
                heartSpritesCache[frame] = frameImage;
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to load heart sprite: " + path);
        }
    }
}
