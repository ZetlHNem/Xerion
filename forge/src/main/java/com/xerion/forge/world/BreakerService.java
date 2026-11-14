package com.xerion.forge.world;

import com.xerion.common.combat.CombatAimRules;
import com.xerion.common.movement.ScaffoldRotation;
import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.misc.ServerDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of OpenOpal's Breaker: silently rotates to and breaks nearby enemy beds,
 * optionally targeting the weakest adjacent block first.
 *
 * <p>The own-bed / team-colour exclusion and Hypixel GameType gating are not
 * ported; only the lobby check is applied.</p>
 */
public final class BreakerService {
    private static final Direction[] SURROUNDING_DIRECTIONS = {
            Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    private static BlockPos lastBreakingPos;
    private static boolean breaking;

    private BreakerService() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (!VisualSettings.breakerEnabled() || player == null || level == null
                || minecraft.gameMode == null || minecraft.screen != null
                || player.isSpectator() || ServerDetector.isHypixelLobby()) {
            reset();
            return;
        }
        BlockPos target = findTarget(player, level, VisualSettings.breakerRange());
        if (target == null) {
            reset();
            return;
        }
        if (!target.equals(lastBreakingPos)) {
            lastBreakingPos = target;
            breaking = false;
        }
        Vec3 center = Vec3.atCenterOf(target);
        rotateTo(player, center);
        Direction face = Direction.UP;
        if (!breaking) {
            minecraft.gameMode.startDestroyBlock(target, face);
            breaking = true;
        }
        if (minecraft.gameMode.continueDestroyBlock(target, face)) {
            if ("Server".equals(VisualSettings.breakerSwingMode())) {
                minecraft.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            } else {
                player.swing(InteractionHand.MAIN_HAND);
            }
        }
    }

    public static void clearRuntimeState() {
        reset();
    }

    private static void reset() {
        lastBreakingPos = null;
        breaking = false;
    }

    private static BlockPos findTarget(LocalPlayer player, ClientLevel level, double range) {
        BlockPos origin = player.blockPosition();
        int radius = (int) Math.ceil(range);
        BlockPos bed = null;
        double bestDistance = Double.MAX_VALUE;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!(state.getBlock() instanceof BedBlock)) {
                        continue;
                    }
                    double distance = player.getEyePosition().distanceTo(Vec3.atCenterOf(pos));
                    if (distance <= range && distance < bestDistance) {
                        bestDistance = distance;
                        bed = pos;
                    }
                }
            }
        }
        if (bed == null) {
            return null;
        }
        if (!VisualSettings.breakerBreakSurroundings()) {
            return bed;
        }
        BlockPos weakest = null;
        float weakestResistance = Float.MAX_VALUE;
        for (Direction direction : SURROUNDING_DIRECTIONS) {
            BlockPos candidate = bed.relative(direction);
            BlockState state = level.getBlockState(candidate);
            if (state.isAir() || state.getBlock() instanceof BedBlock) {
                continue;
            }
            float resistance = Math.max(0.01F, state.getDestroySpeed(level, candidate));
            if (resistance < weakestResistance) {
                weakestResistance = resistance;
                weakest = candidate;
            }
        }
        return weakest != null ? weakest : bed;
    }

    private static void rotateTo(LocalPlayer player, Vec3 target) {
        Vec3 eyes = player.getEyePosition();
        CombatAimRules.Rotation rotation = CombatAimRules.rotationBetween(
                eyes.x, eyes.y, eyes.z, target.x, target.y, target.z);
        ScaffoldRotation.requestPacket(rotation.yaw(), rotation.pitch(), true);
    }
}
