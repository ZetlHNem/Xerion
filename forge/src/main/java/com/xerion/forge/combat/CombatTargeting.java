package com.xerion.forge.combat;

import com.xerion.forge.misc.*;

import com.xerion.forge.render.*;

import com.xerion.forge.world.*;

import com.xerion.forge.player.*;

import com.xerion.forge.movement.*;

import com.xerion.forge.combat.*;

import com.xerion.forge.*;

import com.xerion.forge.runtime.*;

import com.xerion.forge.event.*;

import com.xerion.forge.util.*;

import com.xerion.common.combat.CombatBoxRules;
import com.xerion.common.combat.CombatTargetRules;
import com.xerion.common.config.CombatSettings;
import com.xerion.common.game.GameBox;
import com.xerion.common.game.GameVec3;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public final class CombatTargeting {
    private static final Map<Integer, Boolean> VISIBILITY_CACHE = new HashMap<>();
    private static LocalPlayer visibilityCachePlayer;
    private static int visibilityCacheTick = Integer.MIN_VALUE;

    private CombatTargeting() {
    }

    public static boolean hasVisiblePart(LocalPlayer player, LivingEntity target) {
        if (player == null || target == null) {
            return false;
        }
        if (visibilityCachePlayer != player || visibilityCacheTick != player.tickCount) {
            visibilityCachePlayer = player;
            visibilityCacheTick = player.tickCount;
            VISIBILITY_CACHE.clear();
        }
        Boolean cached = VISIBILITY_CACHE.get(target.getId());
        if (cached != null) {
            return cached;
        }

        boolean visible = computeVisiblePart(player, target.getBoundingBox());
        VISIBILITY_CACHE.put(target.getId(), visible);
        return visible;
    }

    private static boolean computeVisiblePart(LocalPlayer player, AABB box) {
        Vec3 eyes = player.getEyePosition();
        double cx = (box.minX + box.maxX) * 0.5D;
        double cz = (box.minZ + box.maxZ) * 0.5D;
        double widthX = Math.max(0.02D, box.maxX - box.minX);
        double widthZ = Math.max(0.02D, box.maxZ - box.minZ);
        double ox = Math.max(0.04D, widthX * 0.36D);
        double oz = Math.max(0.04D, widthZ * 0.36D);
        double height = Math.max(0.02D, box.maxY - box.minY);
        double foot = box.minY + Math.min(0.12D, height * 0.08D);
        double shin = box.minY + height * 0.22D;
        double mid = box.minY + height * 0.50D;
        double upper = box.minY + height * 0.78D;
        double head = box.maxY - Math.min(0.08D, height * 0.06D);

        return clearLine(player, eyes, cx, mid, cz)
                || clearLine(player, eyes, cx, upper, cz)
                || clearLine(player, eyes, cx, head, cz)
                || clearLine(player, eyes, cx, shin, cz)
                || clearLine(player, eyes, cx, foot, cz)
                || clearLine(player, eyes, cx - ox, shin, cz)
                || clearLine(player, eyes, cx + ox, shin, cz)
                || clearLine(player, eyes, cx, shin, cz - oz)
                || clearLine(player, eyes, cx, shin, cz + oz)
                || clearLine(player, eyes, cx - ox, foot, cz)
                || clearLine(player, eyes, cx + ox, foot, cz)
                || clearLine(player, eyes, cx, foot, cz - oz)
                || clearLine(player, eyes, cx, foot, cz + oz);
    }

    public static Vec3 aimPoint(LocalPlayer player, LivingEntity target, boolean requireVisible) {
        AABB box = target.getBoundingBox();
        if (!requireVisible) {
            return box.getCenter();
        }

        Vec3 eyes = player.getEyePosition();
        Vec3 best = null;
        double bestScore = Double.MAX_VALUE;
        for (Vec3 point : samplePoints(box)) {
            if (!clearLine(player, eyes, point)) {
                continue;
            }
            double score = angularScore(player, eyes, point);
            if (score < bestScore) {
                bestScore = score;
                best = point;
            }
        }
        return best == null ? box.getCenter() : best;
    }

    public static Vec3 ragebotAimPoint(LocalPlayer player, LivingEntity target, boolean requireVisible) {
        AABB box = target.getBoundingBox();
        Vec3[] points = ragebotHitboxPoints(box, CombatSettings.ragebotHitboxesMask());
        if (!requireVisible) {
            return bestAngularPoint(player, player.getEyePosition(), points, false);
        }

        Vec3 eyes = player.getEyePosition();
        Vec3 best = bestAngularPoint(player, eyes, points, true);
        return best == null ? aimPoint(player, target, true) : best;
    }

    public static double distanceToBoxSqr(LocalPlayer player, LivingEntity target) {
        return distanceToBoxSqr(player.getEyePosition(), target.getBoundingBox());
    }

    private static Vec3[] samplePoints(AABB box) {
        GameVec3[] points = CombatBoxRules.samplePoints(new GameBox(
                box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ
        ));
        Vec3[] result = new Vec3[points.length];
        for (int i = 0; i < points.length; i++) {
            GameVec3 point = points[i];
            result[i] = new Vec3(point.x(), point.y(), point.z());
        }
        return result;
    }

    private static Vec3[] ragebotHitboxPoints(AABB box, int mask) {
        if (mask == 0) {
            mask = 31;
        }
        double height = Math.max(0.1D, box.maxY - box.minY);
        double widthX = Math.max(0.05D, box.maxX - box.minX);
        double widthZ = Math.max(0.05D, box.maxZ - box.minZ);
        double cx = (box.minX + box.maxX) * 0.5D;
        double cz = (box.minZ + box.maxZ) * 0.5D;
        java.util.ArrayList<Vec3> points = new java.util.ArrayList<>(9);

        if ((mask & 1) != 0) {
            points.add(new Vec3(cx, box.minY + height * 0.88D, cz));
        }
        if ((mask & 2) != 0) {
            points.add(new Vec3(cx, box.minY + height * 0.68D, cz));
        }
        if ((mask & 4) != 0) {
            points.add(new Vec3(cx, box.minY + height * 0.48D, cz));
        }
        if ((mask & 8) != 0) {
            points.add(new Vec3(cx - widthX * 0.58D, box.minY + height * 0.62D, cz));
            points.add(new Vec3(cx + widthX * 0.58D, box.minY + height * 0.62D, cz));
            points.add(new Vec3(cx, box.minY + height * 0.62D, cz - widthZ * 0.58D));
            points.add(new Vec3(cx, box.minY + height * 0.62D, cz + widthZ * 0.58D));
        }
        if ((mask & 16) != 0) {
            points.add(new Vec3(cx - widthX * 0.22D, box.minY + height * 0.24D, cz));
            points.add(new Vec3(cx + widthX * 0.22D, box.minY + height * 0.24D, cz));
        }
        if (points.isEmpty()) {
            points.add(box.getCenter());
        }
        return points.toArray(Vec3[]::new);
    }

    private static Vec3 bestAngularPoint(LocalPlayer player, Vec3 eyes, Vec3[] points, boolean requireVisible) {
        Vec3 best = null;
        double bestScore = Double.MAX_VALUE;
        for (Vec3 point : points) {
            if (requireVisible && !clearLine(player, eyes, point)) {
                continue;
            }
            double score = angularScore(player, eyes, point);
            if (score < bestScore) {
                bestScore = score;
                best = point;
            }
        }
        return best;
    }

    private static boolean clearLine(LocalPlayer player, Vec3 eyes, Vec3 point) {
        Level level = player.level();
        HitResult result = level.clip(new ClipContext(
                eyes,
                point,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player));
        return result.getType() == HitResult.Type.MISS
                || CombatTargetRules.clearLineByDistance(
                eyes.distanceToSqr(result.getLocation()),
                eyes.distanceToSqr(point));
    }

    private static boolean clearLine(LocalPlayer player, Vec3 eyes, double x, double y, double z) {
        return clearLine(player, eyes, new Vec3(x, y, z));
    }

    private static double distanceToBoxSqr(Vec3 point, AABB box) {
        return CombatTargetRules.distanceToBoxSqr(
                point.x, point.y, point.z,
                box.minX, box.minY, box.minZ,
                box.maxX, box.maxY, box.maxZ
        );
    }

    private static double angularScore(LocalPlayer player, Vec3 eyes, Vec3 point) {
        return CombatTargetRules.angularScore(
                eyes.x, eyes.y, eyes.z,
                point.x, point.y, point.z,
                player.getYRot(), player.getXRot()
        );
    }
}
