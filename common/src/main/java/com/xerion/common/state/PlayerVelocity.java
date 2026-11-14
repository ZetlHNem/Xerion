package com.xerion.common.state;

public record PlayerVelocity(double x, double y, double z) {
    public double horizontalSpeed() {
        return Math.sqrt(x * x + z * z);
    }
}
