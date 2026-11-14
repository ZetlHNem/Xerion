package com.xerion.common.combat;

import com.xerion.common.game.GameBox;
import com.xerion.common.game.GameVec3;

public final class CombatBoxRules {
    private CombatBoxRules() {
    }

    public static GameVec3[] samplePoints(GameBox box) {
        double cx = (box.minX() + box.maxX()) * 0.5D;
        double cz = (box.minZ() + box.maxZ()) * 0.5D;
        double widthX = Math.max(0.02D, box.widthX());
        double widthZ = Math.max(0.02D, box.widthZ());
        double ox = Math.max(0.04D, widthX * 0.36D);
        double oz = Math.max(0.04D, widthZ * 0.36D);
        double height = Math.max(0.02D, box.height());
        double foot = box.minY() + Math.min(0.12D, height * 0.08D);
        double shin = box.minY() + height * 0.22D;
        double mid = box.minY() + height * 0.50D;
        double upper = box.minY() + height * 0.78D;
        double head = box.maxY() - Math.min(0.08D, height * 0.06D);

        return new GameVec3[] {
                new GameVec3(cx, foot, cz),
                new GameVec3(cx - ox, foot, cz),
                new GameVec3(cx + ox, foot, cz),
                new GameVec3(cx, foot, cz - oz),
                new GameVec3(cx, foot, cz + oz),
                new GameVec3(cx, shin, cz),
                new GameVec3(cx - ox, shin, cz),
                new GameVec3(cx + ox, shin, cz),
                new GameVec3(cx, shin, cz - oz),
                new GameVec3(cx, shin, cz + oz),
                new GameVec3(cx, mid, cz),
                new GameVec3(cx, upper, cz),
                new GameVec3(cx, head, cz)
        };
    }
}
