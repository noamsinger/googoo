package com.game.core;

public class LevelProgress {
    private int level;
    private int lives;
    private int exp;
    private int shipType;
    private boolean[] ownedShips;
    private int weaponMode;
    private int weapon;
    private boolean[] ownedWeaponModes;
    private boolean[] ownedWeapons;
    private int hitShieldRemaining;
    private double timedShieldRemaining;
    private String shieldType;

    public LevelProgress(int level,
                        int lives, int exp,
                        int shipType, boolean[] ownedShips,
                        int weaponMode, int weapon,
                        boolean[] ownedWeaponModes, boolean[] ownedWeapons,
                        int hitShieldRemaining, double timedShieldRemaining,
                        String shieldType) {
        this.level = level;
        this.lives = lives;
        this.exp = exp;
        this.shipType = shipType;
        this.ownedShips = ownedShips != null ? ownedShips.clone() : new boolean[]{true, false, false, false, false, false, false, false};
        this.weaponMode = weaponMode;
        this.weapon = weapon;
        this.ownedWeaponModes = ownedWeaponModes != null ? ownedWeaponModes.clone() : new boolean[]{true, false, false, false};
        this.ownedWeapons = ownedWeapons != null ? ownedWeapons.clone() : new boolean[]{true, false};
        this.hitShieldRemaining = hitShieldRemaining;
        this.timedShieldRemaining = timedShieldRemaining;
        this.shieldType = shieldType != null ? shieldType : "none";
    }

    public int getLevel() { return level; }
    public int getLives() { return lives; }
    public int getExp() { return exp; }
    public int getShipType() { return shipType; }
    public boolean[] getOwnedShips() { return ownedShips; }
    public int getWeaponMode() { return weaponMode; }
    public int getWeapon() { return weapon; }
    public boolean[] getOwnedWeaponModes() { return ownedWeaponModes; }
    public boolean[] getOwnedWeapons() { return ownedWeapons; }
    public int getHitShieldRemaining() { return hitShieldRemaining; }
    public double getTimedShieldRemaining() { return timedShieldRemaining; }
    public String getShieldType() { return shieldType; }

    public String getKey() {
        return "LEVEL_" + level;
    }

    public static int getLevelFromKey(String key) {
        if (key.startsWith("LEVEL_")) {
            try {
                return Integer.parseInt(key.substring(6));
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        // Legacy key format: LIVES_LEVEL_N
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
}
