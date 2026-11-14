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

import com.xerion.common.combat.CombatExplosionRules;
import com.xerion.common.combat.CombatPlacementRules;
import com.xerion.common.config.CombatSettings;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class AutoCrystalTargeting {
    private static final CombatPlacementRules.ScanOffset[] PLACEMENT_OFFSETS =
            CombatPlacementRules.autoCrystalPlacementOffsets();

    private AutoCrystalTargeting() {
    }

    public static LivingEntity findTarget(LocalPlayer player, ClientLevel level) {
        double rangeSq = CombatSettings.autoCrystalRange() * CombatSettings.autoCrystalRange();
        TargetPick best = null;

        for (Player target : level.players()) {
            best = considerTarget(player, target, rangeSq, best);
        }

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity target)
                    || target instanceof Player
                    || !CombatTargetSelector.validPlayerOrDummyTarget(player, target)) {
                continue;
            }
            best = considerTarget(player, target, rangeSq, best);
        }
        return best == null ? null : best.target();
    }

    public static EndCrystal findBreakCrystal(LocalPlayer player, ClientLevel level, LivingEntity target) {
        double rangeSq = CombatSettings.autoCrystalRange() * CombatSettings.autoCrystalRange();
        EndCrystal crystal = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof EndCrystal crystalEntity)
                    || !crystalEntity.isAlive()
                    || player.distanceToSqr(crystalEntity) > rangeSq
                    || !visible(player, level, crystalEntity.position())
                    || !validDamage(player, target, crystalEntity.position())) {
                continue;
            }
            double damage = estimatedDamage(target, crystalEntity.position());
            double selfDamage = estimatedDamage(player, crystalEntity.position());
            double score = crystalScore(player, target, crystalEntity.position(), damage, selfDamage);
            if (score > bestScore) {
                bestScore = score;
                crystal = crystalEntity;
            }
        }
        return crystal;
    }

    public static PlaceCandidate findBestPlacement(LocalPlayer player, ClientLevel level, LivingEntity target) {
        double range = CombatSettings.autoCrystalRange();
        double rangeSq = range * range;
        BlockPos targetPos = target.blockPosition();
        PlaceCandidate best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (CombatPlacementRules.ScanOffset offset : PLACEMENT_OFFSETS) {
            mutable.set(targetPos.getX() + offset.dx(), targetPos.getY() + offset.dy(), targetPos.getZ() + offset.dz());
            Vec3 hit = WorldGeometry.topCenter(mutable);
            if (player.distanceToSqr(hit) > rangeSq || !canPlaceCrystal(level, mutable) || !visible(player, level, hit)) {
                continue;
            }
            Vec3 explosion = new Vec3(mutable.getX() + 0.5D, mutable.getY() + 1.0D, mutable.getZ() + 0.5D);
            if (!validDamage(player, target, explosion)) {
                continue;
            }
            double targetDamage = estimatedDamage(target, explosion);
            double selfDamage = estimatedDamage(player, explosion);
            double score = crystalScore(player, target, explosion, targetDamage, selfDamage);
            if (score > bestScore) {
                bestScore = score;
                best = new PlaceCandidate(mutable.immutable(), hit, score);
            }
        }
        return best;
    }

    public static void rotateTo(LocalPlayer player, Vec3 point) {
        if (!CombatSettings.autoCrystalRotate()) {
            return;
        }
        RotationPacketBridge.requestPacketLookAt(player, point);
    }

    private static TargetPick considerTarget(LocalPlayer player, LivingEntity target, double rangeSq, TargetPick current) {
        if (!CombatTargetSelector.validPlayerOrDummyTarget(player, target)) {
            return current;
        }
        double distance = player.distanceToSqr(target);
        if (distance > rangeSq) {
            return current;
        }
        double score = targetScore(player, target, distance);
        if (current == null || score > current.score()) {
            return new TargetPick(target, score);
        }
        return current;
    }

    private static boolean canPlaceCrystal(ClientLevel level, BlockPos basePos) {
        var base = level.getBlockState(basePos).getBlock();
        if (base != Blocks.OBSIDIAN && base != Blocks.BEDROCK) {
            return false;
        }
        BlockPos above = basePos.above();
        if (!level.isEmptyBlock(above)) {
            return false;
        }
        return level.getEntities((Entity) null, new AABB(above)).isEmpty();
    }

    private static boolean validDamage(LocalPlayer player, LivingEntity target, Vec3 explosion) {
        double targetDamage = estimatedDamage(target, explosion);
        double targetHealth = target.getHealth() + target.getAbsorptionAmount();
        float minDamage = CombatExplosionRules.autoCrystalMinDamage(
                CombatSettings.autoCrystalMinDamage(),
                CombatSettings.autoCrystalFacePlace(),
                targetHealth,
                CombatSettings.autoCrystalFacePlaceHealth()
        );
        double selfDamage = estimatedDamage(player, explosion);
        double selfHealth = player.getHealth() + player.getAbsorptionAmount();
        return CombatExplosionRules.validExplosionDamage(
                targetDamage,
                selfDamage,
                selfHealth,
                minDamage,
                CombatSettings.autoCrystalMaxSelfDamage(),
                CombatSettings.autoCrystalAntiSuicide()
        );
    }

    private static double estimatedDamage(LivingEntity entity, Vec3 explosion) {
        double distance = Math.sqrt(entity.getBoundingBox().getCenter().distanceToSqr(explosion));
        return CombatExplosionRules.estimatedDamage(
                distance,
                CombatExplosionRules.CRYSTAL_EXPLOSION_RADIUS,
                18.0D,
                entity.getArmorValue()
        );
    }

    private static double targetScore(LocalPlayer player, LivingEntity target, double distanceSq) {
        return CombatExplosionRules.autoCrystalTargetScore(
                CombatSettings.autoCrystalPriority(),
                distanceSq,
                target.getHealth() + target.getAbsorptionAmount(),
                CombatSettings.autoCrystalRange()
        );
    }

    private static double crystalScore(LocalPlayer player, LivingEntity target, Vec3 explosion, double targetDamage, double selfDamage) {
        return CombatExplosionRules.autoCrystalCrystalScore(
                CombatSettings.autoCrystalPriority(),
                player.distanceToSqr(explosion),
                target.getHealth() + target.getAbsorptionAmount(),
                targetDamage,
                selfDamage
        );
    }

    private static boolean visible(LocalPlayer player, ClientLevel level, Vec3 point) {
        if (!CombatSettings.autoCrystalRayTrace()) {
            return true;
        }
        return WorldGeometry.visible(player, level, point);
    }

    public record PlaceCandidate(BlockPos pos, Vec3 hit, double score) {
    }

    private record TargetPick(LivingEntity target, double score) {
    }
}
