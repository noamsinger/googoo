package com.game.core;

import com.game.states.GameStateManager;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Game extends Application {
    private static final Logger LOGGER = Logger.getLogger(Game.class.getName());

    public final static int WINDOW_WIDTH = 1024;
    public final static int WINDOW_HEIGHT = 768;
    public static int GAME_MAX_WIDTH = 2560;
    public static int GAME_MAX_HEIGHT = 1440;
    public static int gameWidth = WINDOW_WIDTH;  // Start with window dimensions
    public static int gameHeight = WINDOW_HEIGHT;
    public static final String TITLE = "GooGoo Game Remake";

    private static Stage primaryStage;
    private static Canvas canvas;
    private static GameStateManager gsm;
    private static Scene scene;
    private static StackPane root;

    private GraphicsContext gc;

    @Override
    public void start(Stage stage) {
        LOGGER.fine("Starting game application");
        primaryStage = stage;

        // Show stage early to avoid macOS activation timeout
        primaryStage.setTitle(TITLE);
        primaryStage.show();
        primaryStage.toFront();

        adjustMaxResolution();

        canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        LOGGER.fine("Canvas created with size: " + WINDOW_WIDTH + "x" + WINDOW_HEIGHT);

        gsm = new GameStateManager();

        root = new StackPane();
        root.setStyle("-fx-background-color: black;");
        root.getChildren().add(canvas);

        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        scene.setOnKeyPressed(e -> {
            LOGGER.fine("Key pressed: " + e.getCode());
            gsm.keyPressed(e.getCode());
        });
        scene.setOnKeyReleased(e -> gsm.keyReleased(e.getCode()));
        scene.setOnMouseMoved(e -> {
            double canvasX = e.getSceneX() - (scene.getWidth() - canvas.getWidth()) / 2;
            double canvasY = e.getSceneY() - (scene.getHeight() - canvas.getHeight()) / 2;
            if (canvasX >= 0 && canvasX <= canvas.getWidth() && canvasY >= 0 && canvasY <= canvas.getHeight()) {
                gsm.mouseMoved(canvasX, canvasY);
            }
        });
        scene.setOnMouseDragged(e -> {
            double canvasX = e.getSceneX() - (scene.getWidth() - canvas.getWidth()) / 2;
            double canvasY = e.getSceneY() - (scene.getHeight() - canvas.getHeight()) / 2;
            if (canvasX >= 0 && canvasX <= canvas.getWidth() && canvasY >= 0 && canvasY <= canvas.getHeight()) {
                gsm.mouseMoved(canvasX, canvasY);
            }
        });
        scene.setOnMouseClicked(e -> {
            double canvasX = e.getSceneX() - (scene.getWidth() - canvas.getWidth()) / 2;
            double canvasY = e.getSceneY() - (scene.getHeight() - canvas.getHeight()) / 2;
            if (canvasX >= 0 && canvasX <= canvas.getWidth() && canvasY >= 0 && canvasY <= canvas.getHeight()) {
                gsm.mouseClicked(canvasX, canvasY);
            }
        });
        scene.setOnMousePressed(e -> {
            double canvasX = e.getSceneX() - (scene.getWidth() - canvas.getWidth()) / 2;
            double canvasY = e.getSceneY() - (scene.getHeight() - canvas.getHeight()) / 2;
            if (canvasX >= 0 && canvasX <= canvas.getWidth() && canvasY >= 0 && canvasY <= canvas.getHeight()) {
                gsm.mousePressed(canvasX, canvasY);
            }
        });
        scene.setOnMouseReleased(e -> {
            double canvasX = e.getSceneX() - (scene.getWidth() - canvas.getWidth()) / 2;
            double canvasY = e.getSceneY() - (scene.getHeight() - canvas.getHeight()) / 2;
            if (canvasX >= 0 && canvasX <= canvas.getWidth() && canvasY >= 0 && canvasY <= canvas.getHeight()) {
                gsm.mouseReleased(canvasX, canvasY);
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setFullScreenExitHint("");

        primaryStage.setOnCloseRequest(e -> {
            LOGGER.info("Close requested");
            primaryStage.setFullScreen(false);
        });

        new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                gsm.update(deltaTime);
                gsm.render(gc);
            }
        }.start();

        LOGGER.fine("Game loop started");
    }

    private void adjustMaxResolution() {
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        LOGGER.fine("Visual bounds: " + visualBounds.getWidth() + "x" + visualBounds.getHeight());
        LOGGER.fine("Actual screen bounds: " + screenBounds.getWidth() + "x" + screenBounds.getHeight());

        // Use actual screen bounds for fullscreen, not visual bounds
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        double aspectRatio = 16.0 / 9.0;

        // Calculate max fullscreen resolution based on screen height
        int maxHeight = (int) screenHeight;
        int maxWidth = (int) (maxHeight * aspectRatio);

        // Cap at 2560x1440
        if (maxHeight > 1440) {
            maxHeight = 1440;
        }
        if (maxWidth > 2560) {
            maxWidth = 2560;
        }

        GAME_MAX_WIDTH = maxWidth;
        GAME_MAX_HEIGHT = maxHeight;

        LOGGER.fine("Max game resolution set to: " + GAME_MAX_WIDTH + "x" + GAME_MAX_HEIGHT);
    }

    public static void main(String[] args) {
        // LoggingConfigurator has already suppressed JavaFX warnings
        // and configured the console handler (via java.util.logging.config.class)

        // Initialize log level based on debug mode before app starts
        GameSettings settings = GameSettings.getInstance();
        Level logLevel = settings.isDebugMode() ? Level.FINE : Level.INFO;

        // Set level for all game package loggers
        Logger.getLogger("com.game").setLevel(logLevel);

        launch(args);
    }

    public static void setFullscreen(boolean fullscreen) {
        if (primaryStage != null && canvas != null) {
            LOGGER.fine("setFullscreen called with: " + fullscreen);
            LOGGER.fine("Current canvas size: " + canvas.getWidth() + "x" + canvas.getHeight());
            LOGGER.fine("Current GAME dimensions: " + gameWidth + "x" + gameHeight);

            // Update game dimensions immediately (synchronously)
            if (fullscreen) {
                GameSettings settings = GameSettings.getInstance();
                GameSettings.ResolutionMode resMode = settings.getResolutionMode();

                // Determine resolution based on settings
                if (resMode == GameSettings.ResolutionMode.AUTO) {
                    gameWidth = GAME_MAX_WIDTH;
                    gameHeight = GAME_MAX_HEIGHT;
                } else {
                    gameWidth = resMode.getWidth();
                    gameHeight = resMode.getHeight();
                }
                LOGGER.fine("Game dimensions updated to fullscreen: " + gameWidth + "x" + gameHeight + " (mode: " + resMode + ")");
            } else {
                gameWidth = WINDOW_WIDTH;
                gameHeight = WINDOW_HEIGHT;
                LOGGER.fine("Game dimensions updated to windowed: " + gameWidth + "x" + gameHeight);
            }

            // Then update UI on JavaFX thread
            Platform.runLater(() -> {
                if (fullscreen) {
                    LOGGER.fine("Updating canvas and entering fullscreen mode");

                    // Make resizable temporarily to allow resize
                    primaryStage.setResizable(true);

                    canvas.setWidth(gameWidth);
                    canvas.setHeight(gameHeight);
                    root.setPrefSize(gameWidth, gameHeight);
                    root.setMinSize(gameWidth, gameHeight);
                    root.setMaxSize(gameWidth, gameHeight);

                    // Force stage size
                    primaryStage.setWidth(gameWidth);
                    primaryStage.setHeight(gameHeight);

                    LOGGER.fine("Canvas resized to: " + gameWidth + "x" + gameHeight);
                    primaryStage.setFullScreen(true);
                    primaryStage.setResizable(false);
                    LOGGER.fine("Fullscreen mode activated");
                } else {
                    LOGGER.fine("Updating canvas and entering windowed mode");
                    primaryStage.setResizable(true);

                    canvas.setWidth(WINDOW_WIDTH);
                    canvas.setHeight(WINDOW_HEIGHT);
                    root.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
                    root.setMinSize(WINDOW_WIDTH, WINDOW_HEIGHT);
                    root.setMaxSize(WINDOW_WIDTH, WINDOW_HEIGHT);

                    primaryStage.setWidth(WINDOW_WIDTH);
                    primaryStage.setHeight(WINDOW_HEIGHT);

                    LOGGER.fine("Canvas resized to: " + WINDOW_WIDTH + "x" + WINDOW_HEIGHT);
                    primaryStage.setFullScreen(false);
                    primaryStage.setResizable(false);
                    LOGGER.fine("Windowed mode activated");
                }
            });
        } else {
            LOGGER.warning("Cannot setFullscreen - primaryStage or canvas is null");
        }
    }
}
