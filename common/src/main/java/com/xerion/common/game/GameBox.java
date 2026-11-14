package com.xerion.common.game;

public record GameBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
    public GameVec3 center() {
        return new GameVec3((minX + maxX) * 0.5D, (minY + maxY) * 0.5D, (minZ + maxZ) * 0.5D);
    }

    public double widthX() {
        return maxX - minX;
    }

    public double height() {
        return maxY - minY;
    }

    public double widthZ() {
        return maxZ - minZ;
    }
}
