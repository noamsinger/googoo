package com.game.util;

/**
 * Mathematical utility functions for game calculations.
 */
public class MathUtils {

    /**
     * Normalizes an angle to the range [-π, π].
     *
     * @param angle The angle in radians to normalize
     * @return The normalized angle in the range [-π, π]
     */
    public static double normalizeAngle(double angle) {
        while (angle > Math.PI) {
            angle -= 2 * Math.PI;
        }
        while (angle < -Math.PI) {
            angle += 2 * Math.PI;
        }
        return angle;
    }

    /**
     * Calculates the shortest angular difference between two angles.
     * The result is normalized to [-π, π].
     *
     * @param targetAngle The target angle in radians
     * @param currentAngle The current angle in radians
     * @return The angular difference in radians, normalized to [-π, π]
     */
    public static double angleDifference(double targetAngle, double currentAngle) {
        double diff = targetAngle - currentAngle;
        return normalizeAngle(diff);
    }
}
