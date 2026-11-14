package com.xerion.forge.movement;

import com.xerion.forge.config.VisualSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of OpenOpal's FastWeb: while stuck in cobwebs the slowdown multiplier is
 * raised to 0.88 after a few ticks, with a short exit-recovery step.
 */
public final class FastWebService {
    private static int lastWebTick;
    private static int webCount;
    private static int lastAcceleratedWebTick = -1;
    private static boolean exitRecoveryPending;

    private FastWebService() {
    }

    public static Vec3 modifyStuckMotion(BlockState state, Vec3 motion) {
        if (!VisualSettings.fastWebEnabled()
                || state == null
                || !state.is(Blocks.COBWEB)
                || Minecraft.getInstance().player == null) {
            return motion;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        lastWebTick = player.tickCount;
        webCount++;
        if (useVanillaWebMotion()) {
            return motion;
        }
        if (webCount > 5) {
            lastAcceleratedWebTick = player.tickCount;
            return new Vec3(0.88D, motion.y, 0.88D);
        }
        return motion;
    }

    public static void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!VisualSettings.fastWebEnabled() || player == null) {
            clearRuntimeState();
            return;
        }
        if (lastWebTick < player.tickCount && !isTouchingCobweb(player)) {
            if (lastAcceleratedWebTick >= 0 && lastAcceleratedWebTick == player.tickCount - 1) {
                exitRecoveryPending = true;
            }
            webCount = 0;
        }
        if (exitRecoveryPending) {
            Vec3 velocity = player.getDeltaMovement();
            double transition = 0.25D / 0.88D;
            player.setDeltaMovement(velocity.x * transition, velocity.y, velocity.z * transition);
            player.setSprinting(false);
            exitRecoveryPending = false;
            lastAcceleratedWebTick = -1;
        } else if (webCount > 1) {
            player.setSprinting(false);
        }
    }

    public static void clearRuntimeState() {
        lastWebTick = 0;
        webCount = 0;
        lastAcceleratedWebTick = -1;
        exitRecoveryPending = false;
    }

    private static boolean useVanillaWebMotion() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        return minecraft.options.keyJump.isDown()
                || minecraft.options.keyShift.isDown()
                || (player != null && player.input != null && (player.input.jumping || player.input.shiftKeyDown));
    }

    private static boolean isTouchingCobweb(LocalPlayer player) {
        if (Minecraft.getInstance().level == null) {
            return false;
        }
        AABB box = player.getBoundingBox().deflate(1.0E-4D);
        for (int x = Mth.floor(box.minX); x <= Mth.floor(box.maxX); x++) {
            for (int y = Mth.floor(box.minY); y <= Mth.floor(box.maxY); y++) {
                for (int z = Mth.floor(box.minZ); z <= Mth.floor(box.maxZ); z++) {
                    if (Minecraft.getInstance().level.getBlockState(new BlockPos(x, y, z)).is(Blocks.COBWEB)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
