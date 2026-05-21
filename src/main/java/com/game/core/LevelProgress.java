package com.game.core;

import java.util.HashMap;
import java.util.Map;

public class LevelProgress {
    private int level;
    private GameSettings.GameType gameType;
    private int startingLives;
    private double startingShield;
    private int startingExp;
    private int hitShieldRemaining;
    private double timedShieldRemaining;
    private int shipType;
    private boolean[] ownedShips;

    public LevelProgress(int level, GameSettings.GameType gameType,
                        int startingLives, double startingShield, int startingExp,
                        int hitShieldRemaining, double timedShieldRemaining,
                        int shipType, boolean[] ownedShips) {
        this.level = level;
        this.gameType = gameType;
        this.startingLives = startingLives;
        this.startingShield = startingShield;
        this.startingExp = startingExp;
        this.hitShieldRemaining = hitShieldRemaining;
        this.timedShieldRemaining = timedShieldRemaining;
        this.shipType = shipType;
        this.ownedShips = ownedShips != null ? ownedShips : new boolean[]{true, false, false, false, false, false, false, false};
    }

    public int getLevel() {
        return level;
    }

    public GameSettings.GameType getGameType() {
        return gameType;
    }

    public int getStartingLives() {
        return startingLives;
    }

    public double getStartingShield() {
        return startingShield;
    }

    public int getStartingExp() {
        return startingExp;
    }

    public void setStartingLives(int lives) {
        this.startingLives = lives;
    }

    public void setStartingShield(double shield) {
        this.startingShield = shield;
    }

    public void setStartingExp(int exp) {
        this.startingExp = exp;
    }

    public int getHitShieldRemaining() { return hitShieldRemaining; }
    public void setHitShieldRemaining(int hits) { this.hitShieldRemaining = hits; }
    public double getTimedShieldRemaining() { return timedShieldRemaining; }
    public void setTimedShieldRemaining(double seconds) { this.timedShieldRemaining = seconds; }

    public int getShipType() { return shipType; }
    public void setShipType(int type) { this.shipType = type; }
    public boolean[] getOwnedShips() { return ownedShips; }
    public void setOwnedShips(boolean[] ships) { this.ownedShips = ships; }

    public String getKey() {
        return gameType.name() + "_LEVEL_" + level;
    }

    public static int getLevelFromKey(String key) {
        String[] parts = key.split("_LEVEL_");
        if (parts.length == 2) {
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }

    public static GameSettings.GameType getGameTypeFromKey(String key) {
        String[] parts = key.split("_LEVEL_");
        if (parts.length == 2) {
            try {
                return GameSettings.GameType.valueOf(parts[0]);
            } catch (IllegalArgumentException e) {
                return GameSettings.GameType.SHIELD;
            }
        }
        return GameSettings.GameType.SHIELD;
    }
}
