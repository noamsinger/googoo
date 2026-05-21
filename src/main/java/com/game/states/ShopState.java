package com.game.states;

import com.game.core.Game;
import com.game.util.StarshipSpriteLoader;
import com.game.util.TextUtils;
import com.game.util.WeaponSpriteLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShopState extends GameState {

    // Color constants for shop items
    private static final Color COLOR_ACTIVE = Color.YELLOW;
    private static final Color COLOR_OWNED = Color.rgb(0, 220, 0);
    private static final Color COLOR_BUY_AFFORDABLE = Color.rgb(180, 100, 255);
    private static final Color COLOR_BUY_UNAFFORDABLE = Color.rgb(60, 40, 80);
    private static final Color COLOR_ACTION = Color.WHITE;
    private static final Color COLOR_DISABLED = Color.rgb(100, 100, 100);
    private static final Color COLOR_BUTTON_BUY = Color.rgb(140, 60, 220);
    private static final Color COLOR_BUTTON_SELECT = Color.rgb(0, 160, 0);

    private static final int STAR_COUNT = 300;
    private final Random random = new Random();
    private final List<Star> stars = new ArrayList<>();
    private final PlayState playState;

    private Font titleFont;
    private Font itemFont;
    private Font detailFont;
    private Font hintFont;
    private int currentSelection = 0;

    private List<ShopItem> allItems;

    private enum Column { STARSHIPS, WEAPONS, SHIELDS, ACTIONS }

    private static class ShopItem {
        final String name;
        final String description;
        final int cost;
        final Runnable action;
        final java.util.function.BooleanSupplier canBuy;
        final Column column;
        final String colorHint; // "yellow", "green", "purple", "blue", "white", ""

        ShopItem(String name, String description, int cost, Runnable action,
                 java.util.function.BooleanSupplier canBuy, Column column, String colorHint) {
            this.name = name;
            this.description = description;
            this.cost = cost;
            this.action = action;
            this.canBuy = canBuy;
            this.column = column;
            this.colorHint = colorHint;
        }
    }

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

    public ShopState(GameStateManager gsm, PlayState playState) {
        super(gsm);
        this.playState = playState;
        init();
    }

    @Override
    public void init() {
        titleFont = Font.font("Arial", FontWeight.BOLD, 42);
        itemFont = Font.font("Arial", FontWeight.NORMAL, 24);
        detailFont = Font.font("Arial", FontWeight.NORMAL, 15);
        hintFont = Font.font("Arial", FontWeight.NORMAL, 16);

        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Star(
                random.nextDouble() * Game.gameWidth,
                random.nextDouble() * Game.gameHeight,
                1.0 + random.nextDouble() * 2.5
            ));
        }

        buildItems();
    }

    private void buildItems() {
        allItems = new ArrayList<>();

        // Starships column
        boolean[] owned = playState.getOwnedShips();
        int currentShip = playState.getShipType();
        String[] shipNames = {"Ship 1", "Ship 2", "Ship 3", "Ship 4"};
        String[] shipDescs = {"Standard", "2x speed, 2x accel", "5x turn (track), 2x (keys)", "5x turn + 4x burst"};
        int[] shipCosts = {0, 50, 80, 150};

        for (int i = 0; i < 4; i++) {
            final int shipIdx = i;
            final int shipNum = i + 1;
            boolean isOwned = owned[i];
            boolean isEquipped = (currentShip == shipNum);

            if (isEquipped) {
                allItems.add(new ShopItem(shipNames[i], shipDescs[i], 0,
                    () -> {}, () -> false, Column.STARSHIPS, "yellow"));
            } else if (isOwned) {
                allItems.add(new ShopItem(shipNames[i] + "  Select", shipDescs[i], 0,
                    () -> playState.setShipType(shipNum),
                    () -> true, Column.STARSHIPS, "green"));
            } else {
                allItems.add(new ShopItem(shipNames[i] + "  Buy " + shipCosts[i], shipDescs[i], shipCosts[i],
                    () -> { playState.spendExp(shipCosts[shipIdx]); playState.buyShip(shipNum); playState.setShipType(shipNum); },
                    () -> playState.getExp() >= shipCosts[shipIdx], Column.STARSHIPS, "purple"));
            }
        }

        // Weapons column - Fire Modes first, then Weapons
        boolean[] ownedModes = playState.getOwnedWeaponModes();
        int currentMode = playState.getActiveWeaponMode();
        boolean[] ownedWeapons = playState.getOwnedWeapons();
        int currentWeapon = playState.getActiveWeapon();

        // Fire modes (0-3): Manual, Semi-Auto, Auto, Vulkan
        String[] modeNames = {"Manual", "Semi-Auto", "Auto", "Vulkan"};
        String[] modeDescs = {"Click to fire", "Click, 0.3s rate", "Auto-fire 0.3s", "Auto-fire 0.1s"};
        int[] modeCosts = {0, 30, 75, 150};

        for (int i = 0; i < 4; i++) {
            final int mIdx = i;
            boolean isOwned = ownedModes[i];
            boolean isActive = (currentMode == i);

            if (isActive) {
                allItems.add(new ShopItem(modeNames[i], modeDescs[i], 0,
                    () -> {}, () -> false, Column.WEAPONS, "yellow"));
            } else if (isOwned) {
                allItems.add(new ShopItem(modeNames[i], modeDescs[i], 0,
                    () -> playState.setActiveWeaponMode(mIdx),
                    () -> true, Column.WEAPONS, "green"));
            } else {
                allItems.add(new ShopItem(modeNames[i], modeDescs[i], modeCosts[i],
                    () -> { playState.spendExp(modeCosts[mIdx]); playState.buyWeaponMode(mIdx); playState.setActiveWeaponMode(mIdx); },
                    () -> playState.getExp() >= modeCosts[mIdx], Column.WEAPONS, "purple"));
            }
        }

        // Weapons: Bullet (0) and Torpedo (1)
        String[] weaponNames = {"Bullet", "Torpedo"};
        String[] weaponDescs = {"Standard projectile", "Homing, 5s life"};
        int[] weaponCosts = {0, 80};

        for (int i = 0; i < 2; i++) {
            final int wIdx = i;
            boolean isOwned = ownedWeapons[i];
            boolean isActive = (currentWeapon == i);

            if (isActive) {
                allItems.add(new ShopItem(weaponNames[i], weaponDescs[i], 0,
                    () -> {}, () -> false, Column.WEAPONS, "yellow"));
            } else if (isOwned) {
                allItems.add(new ShopItem(weaponNames[i], weaponDescs[i], 0,
                    () -> playState.setActiveWeapon(wIdx),
                    () -> true, Column.WEAPONS, "green"));
            } else {
                allItems.add(new ShopItem(weaponNames[i], weaponDescs[i], weaponCosts[i],
                    () -> { playState.spendExp(weaponCosts[wIdx]); playState.buyWeapon(wIdx); playState.setActiveWeapon(wIdx); },
                    () -> playState.getExp() >= weaponCosts[wIdx], Column.WEAPONS, "purple"));
            }
        }

        // Shields column
        String activeShield = playState.getActiveShieldType();
        int hitRemaining = playState.getHitShieldRemaining();
        double timedRemaining = playState.getTimedShieldRemaining();

        // Hit Shield
        if (hitRemaining > 0) {
            if (activeShield.equals("hit")) {
                allItems.add(new ShopItem("Hit Shield  Buy +5", hitRemaining + " hits remaining", 40,
                    () -> { playState.spendExp(40); playState.addHitShield(5); },
                    () -> playState.getExp() >= 40, Column.SHIELDS, "yellow"));
            } else {
                allItems.add(new ShopItem("Hit Shield  Select", hitRemaining + " hits remaining", 0,
                    () -> playState.setActiveShieldType("hit"),
                    () -> true, Column.SHIELDS, "green"));
            }
        } else {
            allItems.add(new ShopItem("Hit Shield  Buy 40", "+5 hits protection", 40,
                () -> { playState.spendExp(40); playState.addHitShield(5); playState.setActiveShieldType("hit"); },
                () -> playState.getExp() >= 40, Column.SHIELDS, "purple"));
        }

        // Timed Shield
        if (timedRemaining > 0) {
            if (activeShield.equals("timed")) {
                allItems.add(new ShopItem("Timed Shield  Buy +60s", (int)timedRemaining + "s remaining", 60,
                    () -> { playState.spendExp(60); playState.addTimedShield(60.0); },
                    () -> playState.getExp() >= 60, Column.SHIELDS, "yellow"));
            } else {
                allItems.add(new ShopItem("Timed Shield  Select", (int)timedRemaining + "s remaining", 0,
                    () -> playState.setActiveShieldType("timed"),
                    () -> true, Column.SHIELDS, "green"));
            }
        } else {
            allItems.add(new ShopItem("Timed Shield  Buy 60", "+60s protection", 60,
                () -> { playState.spendExp(60); playState.addTimedShield(60.0); playState.setActiveShieldType("timed"); },
                () -> playState.getExp() >= 60, Column.SHIELDS, "purple"));
        }

        // Extra life / shield restore
        if (playState.getGameMode() == PlayState.GameMode.LIVES) {
            allItems.add(new ShopItem("Extra Life  Buy 10", "+1 life", 10,
                () -> { playState.spendExp(10); playState.addLife(); },
                () -> playState.getExp() >= 10, Column.SHIELDS, "purple"));
        } else {
            allItems.add(new ShopItem("Shield +5%  Buy 5", "Restore 5%", 5,
                () -> { playState.spendExp(5); playState.addShieldPercentage(5.0); },
                () -> playState.getExp() >= 5 && playState.getShieldPercentage() < 100.0, Column.SHIELDS, "purple"));
        }

        // Actions (bottom)
        allItems.add(new ShopItem("Continue", "", 0, () -> gsm.popState(), () -> true, Column.ACTIONS, "white"));
        allItems.add(new ShopItem("Restart Level", "", 0, () -> {
            gsm.popState();
            gsm.setState(new PlayState(gsm));
        }, () -> true, Column.ACTIONS, "white"));
    }

    @Override
    public void update(double deltaTime) {
        double fastDelta = deltaTime * 10;
        for (Star star : stars) {
            star.update(fastDelta);
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
    }

    @Override
    public void render(GraphicsContext gc) {
        double w = Game.gameWidth;
        double h = Game.gameHeight;

        gc.setFill(Color.rgb(0, 0, 10, 0.85));
        gc.fillRect(0, 0, w, h);

        for (Star star : stars) {
            gc.setFill(Color.rgb(255, 255, 255, Math.max(0, Math.min(1, star.currentOpacity))));
            gc.fillOval(star.x - star.size / 2, star.y - star.size / 2, star.size, star.size);
        }

        // Title
        gc.setFont(titleFont);
        gc.setFill(Color.rgb(100, 200, 255));
        String title = "Shop";
        gc.fillText(title, TextUtils.centerTextX(title, titleFont, w), h * 0.12);

        // EXP display
        gc.setFont(itemFont);
        gc.setFill(Color.rgb(255, 215, 0));
        String expText = "EXP: " + playState.getExp();
        gc.fillText(expText, TextUtils.centerTextX(expText, itemFont, w), h * 0.20);

        // Status line
        gc.setFont(detailFont);
        gc.setFill(Color.rgb(180, 180, 180));
        String status;
        if (playState.getGameMode() == PlayState.GameMode.LIVES) {
            status = "Lives: " + playState.getRemainingLives();
            if (playState.getHitShieldRemaining() > 0) {
                status += " | Hit Shield: " + playState.getHitShieldRemaining() + " hits";
            }
            if (playState.getTimedShieldRemaining() > 0) {
                status += " | Timed Shield: " + (int) playState.getTimedShieldRemaining() + "s";
            }
        } else {
            status = String.format("Shield: %.0f%%", playState.getShieldPercentage());
        }
        gc.fillText(status, TextUtils.centerTextX(status, detailFont, w), h * 0.26);

        // 3 columns + actions at bottom
        double col1X = w * 0.05;
        double col2X = w * 0.37;
        double col3X = w * 0.65;
        double startY = h * 0.35;
        double spacing = 48;

        // Column headers
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.setFill(Color.rgb(150, 150, 200));
        gc.fillText("Starships", col1X, startY - 15);
        gc.fillText("Weapons", col2X, startY - 15);
        gc.fillText("Shields", col3X, startY - 15);

        int[] colIdx = {0, 0, 0};

        for (int i = 0; i < allItems.size(); i++) {
            ShopItem item = allItems.get(i);
            double[] pos = getItemPosition(i);
            double itemX = pos[0];
            double itemY = pos[1];

            boolean affordable = item.canBuy.getAsBoolean();
            String label = item.name;

            // Color based on colorHint using constants
            Color itemColor;
            switch (item.colorHint) {
                case "yellow": itemColor = COLOR_ACTIVE; break;
                case "green": itemColor = COLOR_OWNED; break;
                case "purple": itemColor = affordable ? COLOR_BUY_AFFORDABLE : COLOR_BUY_UNAFFORDABLE; break;
                case "blue": itemColor = affordable ? COLOR_BUY_AFFORDABLE : COLOR_BUY_UNAFFORDABLE; break;
                case "white": itemColor = COLOR_ACTION; break;
                default: itemColor = COLOR_DISABLED; break;
            }

            gc.setFont(itemFont);
            double textWidth = TextUtils.measureTextWidth(label, itemFont);
            double textHeight = TextUtils.measureTextHeight(label, itemFont);

            if (i == currentSelection) {
                gc.setFill(Color.rgb(255, 215, 0, 0.2));
                gc.fillRoundRect(itemX - 5, itemY - textHeight + 5, textWidth + 10, textHeight + 10, 8, 8);
                gc.setFill(affordable ? COLOR_ACTIVE : Color.rgb(150, 80, 80));
                gc.fillText(">", itemX - 18, itemY);
            }

            gc.setFill(itemColor);
            gc.fillText(label, itemX, itemY);

            // Draw Buy/Select button
            if (item.colorHint.equals("purple") && item.cost > 0) {
                String btnText = "Buy " + item.cost;
                Font btnFont = Font.font("Arial", FontWeight.BOLD, 14);
                gc.setFont(btnFont);
                double btnW = TextUtils.measureTextWidth(btnText, btnFont) + 16;
                double btnH = 20;
                double btnX = itemX + textWidth + 15;
                double btnY = itemY - btnH + 4;
                gc.setStroke(affordable ? COLOR_BUTTON_BUY : COLOR_BUY_UNAFFORDABLE);
                gc.setLineWidth(1.5);
                gc.strokeRoundRect(btnX, btnY, btnW, btnH, 10, 10);
                gc.setFill(affordable ? COLOR_BUY_AFFORDABLE : COLOR_BUY_UNAFFORDABLE);
                gc.fillText(btnText, btnX + 8, itemY - 2);
            } else if (item.colorHint.equals("green") && item.canBuy.getAsBoolean()) {
                String btnText = "Select";
                Font btnFont = Font.font("Arial", FontWeight.BOLD, 14);
                gc.setFont(btnFont);
                double btnW = TextUtils.measureTextWidth(btnText, btnFont) + 16;
                double btnH = 20;
                double btnX = itemX + textWidth + 15;
                double btnY = itemY - btnH + 4;
                gc.setStroke(COLOR_BUTTON_SELECT);
                gc.setLineWidth(1.5);
                gc.strokeRoundRect(btnX, btnY, btnW, btnH, 10, 10);
                gc.setFill(COLOR_OWNED);
                gc.fillText(btnText, btnX + 8, itemY - 2);
            }

            // Draw starship sprite next to starship items
            if (item.column == Column.STARSHIPS) {
                int shipIdx = -1;
                for (int s = 0; s < 4; s++) {
                    if (item.name.startsWith("Ship " + (s + 1))) { shipIdx = s + 1; break; }
                }
                if (shipIdx > 0) {
                    Image shipSprite = StarshipSpriteLoader.getStarshipFrame(shipIdx, 0);
                    if (shipSprite != null) {
                        double spriteSize = 32;
                        double spriteX = itemX - spriteSize - 5;
                        gc.drawImage(shipSprite, spriteX, itemY - spriteSize + 5, spriteSize, spriteSize);
                    }
                }
            }

            // Draw weapon sprite next to Bullet and Torpedo
            if (item.column == Column.WEAPONS) {
                int weaponSpriteIdx = -1;
                if (item.name.equals("Bullet")) weaponSpriteIdx = 1;
                else if (item.name.equals("Torpedo")) weaponSpriteIdx = 2;
                if (weaponSpriteIdx > 0) {
                    Image weaponSprite = WeaponSpriteLoader.getWeaponFrame(weaponSpriteIdx, 0);
                    if (weaponSprite != null) {
                        double spriteSize = 28;
                        double spriteX = itemX - spriteSize - 5;
                        gc.drawImage(weaponSprite, spriteX, itemY - spriteSize + 5, spriteSize, spriteSize);
                    }
                }
            }

            if (!item.description.isEmpty()) {
                gc.setFont(detailFont);
                gc.setFill(Color.rgb(150, 150, 150));
                gc.fillText(item.description, itemX, itemY + 16);
            }
        }

        // Hint
        gc.setFill(Color.rgb(120, 120, 120));
        gc.setFont(hintFont);
        String hint = "ESC to continue | ENTER to select | UP/DOWN to navigate";
        gc.fillText(hint, TextUtils.centerTextX(hint, hintFont, w), h * 0.93);
    }

    private double[] getItemPosition(int index) {
        double w = Game.gameWidth;
        double h = Game.gameHeight;
        double col1X = w * 0.05;
        double col2X = w * 0.37;
        double col3X = w * 0.65;
        double startY = h * 0.35;
        double spacing = 48;

        int[] colIdx = {0, 0, 0, 0};
        for (int i = 0; i <= index && i < allItems.size(); i++) {
            if (i == index) {
                Column col = allItems.get(i).column;
                switch (col) {
                    case STARSHIPS: return new double[]{col1X, startY + colIdx[0] * spacing};
                    case WEAPONS: return new double[]{col2X, startY + colIdx[1] * spacing};
                    case SHIELDS: return new double[]{col3X, startY + colIdx[2] * spacing};
                    case ACTIONS: return new double[]{w * 0.37, h * 0.85 + colIdx[3] * 35};
                }
            }
            switch (allItems.get(i).column) {
                case STARSHIPS: colIdx[0]++; break;
                case WEAPONS: colIdx[1]++; break;
                case SHIELDS: colIdx[2]++; break;
                case ACTIONS: colIdx[3]++; break;
            }
        }
        return new double[]{col1X, startY};
    }

    @Override
    public void keyPressed(KeyCode key) {
        if (key == KeyCode.ESCAPE) {
            gsm.popState();
        } else if (key == KeyCode.UP) {
            currentSelection--;
            if (currentSelection < 0) currentSelection = 0;
        } else if (key == KeyCode.DOWN) {
            currentSelection++;
            if (currentSelection >= allItems.size()) currentSelection = allItems.size() - 1;
        } else if (key == KeyCode.ENTER) {
            selectItem();
        }
    }

    @Override
    public void keyReleased(KeyCode key) {
    }

    @Override
    public void mouseClicked(double x, double y) {
        for (int i = 0; i < allItems.size(); i++) {
            if (isInItemBounds(i, x, y)) {
                currentSelection = i;
                selectItem();
                return;
            }
        }
    }

    @Override
    public void mouseMoved(double x, double y) {
        for (int i = 0; i < allItems.size(); i++) {
            if (isInItemBounds(i, x, y)) {
                currentSelection = i;
                break;
            }
        }
    }

    private boolean isInItemBounds(int index, double x, double y) {
        ShopItem item = allItems.get(index);
        String label = item.cost > 0 ? item.name + "  [" + item.cost + " EXP]" : item.name;
        double[] pos = getItemPosition(index);
        double itemX = pos[0];
        double itemY = pos[1];
        double textWidth = TextUtils.measureTextWidth(label, itemFont);
        double textHeight = TextUtils.measureTextHeight(label, itemFont);

        double left = itemX - 10;
        double right = left + textWidth + 20;
        double top = itemY - textHeight + 5;
        double bottom = top + textHeight + 10;

        return x >= left && x <= right && y >= top && y <= bottom;
    }

    private void selectItem() {
        ShopItem item = allItems.get(currentSelection);
        if (item.canBuy.getAsBoolean()) {
            item.action.run();
            buildItems();
            if (currentSelection >= allItems.size()) {
                currentSelection = allItems.size() - 1;
            }
        }
    }
}
