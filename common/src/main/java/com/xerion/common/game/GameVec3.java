package com.xerion.common.game;

public record GameVec3(double x, double y, double z) {
    public static final GameVec3 ZERO = new GameVec3(0.0D, 0.0D, 0.0D);

    public double distanceToSqr(GameVec3 other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }
}
