package com.game.states;

import com.game.core.Game;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayState extends GameState {
    private List<Star> stars;
    private Random random;

    public PlayState(GameStateManager gsm) {
        super(gsm);
        init();
    }

    @Override
    public void init() {
        random = new Random();
        stars = new ArrayList<>();

        for (int i = 0; i < 200; i++) {
            stars.add(new Star(
                random.nextDouble() * Game.WIDTH,
                random.nextDouble() * Game.HEIGHT,
                random.nextDouble() * 2 + 0.5,
                random.nextDouble() * 1.5 + 0.5
            ));
        }
    }

    @Override
    public void update(double deltaTime) {
        for (Star star : stars) {
            star.update(deltaTime);

            if (star.y > Game.HEIGHT) {
                star.y = 0;
                star.x = random.nextDouble() * Game.WIDTH;
            }
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.rgb(5, 5, 15));
        gc.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);

        for (Star star : stars) {
            gc.setFill(Color.rgb(255, 255, 255, star.opacity));
            gc.fillOval(star.x, star.y, star.size, star.size);
        }
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

    private static class Star {
        double x, y;
        double speed;
        double size;
        double opacity;

        Star(double x, double y, double size, double speed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speed = speed;
            this.opacity = Math.random() * 0.5 + 0.5;
        }

        void update(double deltaTime) {
            y += speed * 100 * deltaTime;
        }
    }
}
