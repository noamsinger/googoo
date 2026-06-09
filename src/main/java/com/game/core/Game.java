package com.game.core;

import com.game.states.GameStateManager;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Game extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Game.class);

    public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 768;
    public static int gameMaxWidth = 2560;
    public static int gameMaxHeight = 1440;
    public static int gameWidth = WINDOW_WIDTH;  // Start with window dimensions
    public static int gameHeight = WINDOW_HEIGHT;
    public static final String TITLE = "GooGoo Game Remake";
    public static final String VERSION = "2.1.1";

    private static Stage primaryStage;
    private static Canvas canvas;
    private static GameStateManager gsm;
    private static Scene scene;
    private static StackPane root;

    private GraphicsContext gc;

    @Override
    public void start(Stage stage) {
        LOGGER.debug("Starting game application");
        primaryStage = stage;

        // Show stage early to avoid macOS activation timeout
        primaryStage.setTitle(TITLE);
        primaryStage.show();
        primaryStage.toFront();

        adjustMaxResolution();

        canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        LOGGER.debug("Canvas created with size: {}x{}", WINDOW_WIDTH, WINDOW_HEIGHT);

        gsm = new GameStateManager();

        root = new StackPane();
        root.setStyle("-fx-background-color: black;");
        root.getChildren().add(canvas);

        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        scene.setOnKeyPressed(e -> {
            LOGGER.debug("Key pressed: {}", e.getCode());
            gsm.keyPressed(e.getCode());
        });
        scene.setOnKeyReleased(e -> gsm.keyReleased(e.getCode()));
        scene.setOnMouseMoved(e -> handleMouseEvent(e, (x, y) -> gsm.mouseMoved(x, y)));
        scene.setOnMouseDragged(e -> handleMouseEvent(e, (x, y) -> gsm.mouseMoved(x, y)));
        scene.setOnMouseClicked(e -> handleMouseEvent(e, (x, y) -> {
            gsm.mouseClicked(x, y);
        }));
        scene.setOnMousePressed(e -> handleMouseEvent(e, (x, y) -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                gsm.mouseRightPressed(x, y);
            } else {
                gsm.mousePressed(x, y);
            }
        }));
        scene.setOnMouseReleased(e -> handleMouseEvent(e, (x, y) -> gsm.mouseReleased(x, y)));
        scene.setOnScroll(e -> {
            gsm.mouseScrolled(e.getDeltaY());
        });

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);

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

        LOGGER.debug("Game loop started");
    }

    private void adjustMaxResolution() {
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        LOGGER.debug("Visual bounds: {}x{}", visualBounds.getWidth(), visualBounds.getHeight());
        LOGGER.debug("Actual screen bounds: {}x{}", screenBounds.getWidth(), screenBounds.getHeight());

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

        gameMaxWidth = maxWidth;
        gameMaxHeight = maxHeight;

        LOGGER.debug("Max game resolution set to: {}x{}", gameMaxWidth, gameMaxHeight);
    }

    public static void main(String[] args) {
        for (String arg : args) {
            if ("--debug".equals(arg)) {
                LoggingConfigurator.enableDebug();
                break;
            }
        }

        launch(args);
    }

    public static void setFullscreen(boolean fullscreen) {
        if (primaryStage != null && canvas != null) {
            LOGGER.debug("setFullscreen called with: {}", fullscreen);
            LOGGER.debug("Current canvas size: {}x{}", canvas.getWidth(), canvas.getHeight());
            LOGGER.debug("Current GAME dimensions: {}x{}", gameWidth, gameHeight);

            // Update game dimensions immediately (synchronously)
            if (fullscreen) {
                GameSettings settings = GameSettings.getInstance();
                GameSettings.ResolutionMode resMode = settings.getResolutionMode();

                // Determine resolution based on settings
                if (resMode == GameSettings.ResolutionMode.AUTO) {
                    gameWidth = gameMaxWidth;
                    gameHeight = gameMaxHeight;
                } else {
                    gameWidth = resMode.getWidth();
                    gameHeight = resMode.getHeight();
                }
                LOGGER.debug("Game dimensions updated to fullscreen: {}x{} (mode: {})", gameWidth, gameHeight, resMode);
            } else {
                gameWidth = WINDOW_WIDTH;
                gameHeight = WINDOW_HEIGHT;
                LOGGER.debug("Game dimensions updated to windowed: {}x{}", gameWidth, gameHeight);
            }

            // Then update UI on JavaFX thread
            Platform.runLater(() -> {
                if (fullscreen) {
                LOGGER.debug("Updating canvas and entering fullscreen mode");

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

                    LOGGER.debug("Canvas resized to: {}x{}", gameWidth, gameHeight);
                    primaryStage.setFullScreen(true);
                    primaryStage.setResizable(false);
                    LOGGER.debug("Fullscreen mode activated");
                } else {
                LOGGER.debug("Updating canvas and entering windowed mode");
                    primaryStage.setResizable(true);

                    canvas.setWidth(WINDOW_WIDTH);
                    canvas.setHeight(WINDOW_HEIGHT);
                    root.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
                    root.setMinSize(WINDOW_WIDTH, WINDOW_HEIGHT);
                    root.setMaxSize(WINDOW_WIDTH, WINDOW_HEIGHT);

                    primaryStage.setWidth(WINDOW_WIDTH);
                    primaryStage.setHeight(WINDOW_HEIGHT);

                    LOGGER.debug("Canvas resized to: {}x{}", WINDOW_WIDTH, WINDOW_HEIGHT);
                    primaryStage.setFullScreen(false);
                    primaryStage.setResizable(false);
                    LOGGER.debug("Windowed mode activated");
                }
            });
        } else {
            LOGGER.warn("Cannot setFullscreen - primaryStage or canvas is null");
        }
    }

    /**
     * Helper method to convert scene coordinates to canvas coordinates and dispatch to handler.
     * Only dispatches if the coordinates are within canvas bounds.
     */
    private static void handleMouseEvent(javafx.scene.input.MouseEvent e, MouseEventHandler handler) {
        double canvasX = e.getSceneX() - (scene.getWidth() - canvas.getWidth()) / 2;
        double canvasY = e.getSceneY() - (scene.getHeight() - canvas.getHeight()) / 2;
        if (canvasX >= 0 && canvasX <= canvas.getWidth() && canvasY >= 0 && canvasY <= canvas.getHeight()) {
            handler.handle(canvasX, canvasY);
        }
    }

    @FunctionalInterface
    private interface MouseEventHandler {
        void handle(double x, double y);
    }
}
