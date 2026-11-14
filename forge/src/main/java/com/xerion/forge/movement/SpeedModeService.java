package com.xerion.forge.movement;

import com.xerion.forge.config.VisualSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of OpenOpal's CubeCraft speed modes: CubeCraftFast (ground boost that
 * disables itself on a lag-back) and CubeCraftLowHop (low jump with air-tick
 * motion adjustments).
 */
public final class SpeedModeService {
    private static final double AIR_TICK_ONE_Y_ADD = 0.0568D;
    private static final double HURT_STRAFE_MINIMUM = 0.281D;

    private static int airTicks;
    private static boolean ignoreInitialTeleport = true;

    private SpeedModeService() {
    }

    public static void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            airTicks = 0;
            return;
        }
        airTicks = player.onGround() ? 0 : airTicks + 1;
    }

    public static boolean handles(String mode) {
        return "CubeCraftFast".equals(mode) || "CubeCraftLowHop".equals(mode);
    }

    public static void apply(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null || player.input == null || minecraft.level == null
                || player.isPassenger() || player.isSpectator() || player.getAbilities().flying
                || minecraft.screen != null) {
            return;
        }
        String mode = VisualSettings.speedMode();
        if ("CubeCraftFast".equals(mode)) {
            applyCubeCraftFast(player);
        } else {
            applyCubeCraftLowHop(minecraft, player);
        }
    }

    private static void applyCubeCraftFast(LocalPlayer player) {
        if (!player.onGround()) {
            return;
        }
        com.xerion.common.movement.MovementMath.MoveInput input = MovementMath.movementInput(player);
        if (!input.moving()) {
            return;
        }
        setSpeed(player, input, VisualSettings.speedCubeCraftFastSpeed() / 4.0D);
    }

    private static void applyCubeCraftLowHop(Minecraft minecraft, LocalPlayer player) {
        com.xerion.common.movement.MovementMath.MoveInput input = MovementMath.movementInput(player);
        if (player.onGround()) {
            if (input.moving() && !player.isShiftKeyDown() && !minecraft.options.keyJump.isDown()) {
                player.jumpFromGround();
                double minimum = 0.247D + 0.15D * speedAmplifier(player);
                setSpeed(player, input, Math.max(horizontalSpeed(player.getDeltaMovement()), minimum));
                player.setSprinting(true);
            }
        }
        if (!input.moving()) {
            zeroHorizontal(player);
            return;
        }
        if (player.onGround()) {
            return;
        }
        Vec3 velocity = player.getDeltaMovement();
        switch (airTicks) {
            case 1 -> {
                setSpeed(player, input, horizontalSpeed(velocity));
                player.setDeltaMovement(velocity.x, velocity.y + AIR_TICK_ONE_Y_ADD, velocity.z);
            }
            case 3 -> player.setDeltaMovement(velocity.x * 0.95D, velocity.y - 0.13D, velocity.z * 0.95D);
            case 4 -> player.setDeltaMovement(velocity.x, velocity.y - 0.2D, velocity.z);
            case 7 -> {
                if (VisualSettings.speedCubeCraftLowHopGlide() && isGroundExempt(player)) {
                    player.setDeltaMovement(velocity.x, 0.0D, velocity.z);
                }
            }
            default -> {
            }
        }
        if (isGroundExempt(player)) {
            setSpeed(player, input, horizontalSpeed(player.getDeltaMovement()));
        }
        if (player.hurtTime == 9) {
            setSpeed(player, input, Math.max(horizontalSpeed(player.getDeltaMovement()), HURT_STRAFE_MINIMUM));
        }
        if (speedAmplifier(player) == 2 && isBoostAirTick(airTicks)) {
            Vec3 current = player.getDeltaMovement();
            player.setDeltaMovement(current.x * 1.2D, current.y, current.z * 1.2D);
        }
        player.hurtMarked = true;
    }

    public static void onServerCorrection() {
        if (!VisualSettings.speedEnabled()
                || !"CubeCraftFast".equals(VisualSettings.speedMode())
                || !VisualSettings.speedCubeCraftLagBackCheck()) {
            return;
        }
        if (ignoreInitialTeleport) {
            ignoreInitialTeleport = false;
            return;
        }
        VisualSettings.setSpeedEnabled(false);
    }

    public static void clearRuntimeState() {
        airTicks = 0;
        ignoreInitialTeleport = true;
    }

    private static void setSpeed(LocalPlayer player,
                                 com.xerion.common.movement.MovementMath.MoveInput input, double speed) {
        if (speed <= 1.0E-5D) {
            zeroHorizontal(player);
            return;
        }
        double[] motion = MovementMath.directionalMotion(
                player.getYRot(), input.forward(), input.strafe(), speed);
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(motion[0], velocity.y, motion[1]);
        player.hurtMarked = true;
    }

    private static void zeroHorizontal(LocalPlayer player) {
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(0.0D, velocity.y, 0.0D);
    }

    private static boolean isGroundExempt(LocalPlayer player) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || player.getDeltaMovement().y >= 0.0D) {
            return false;
        }
        for (VoxelShape shape : level.getBlockCollisions(player,
                player.getBoundingBox().move(0.0D, -0.66D, 0.0D))) {
            if (!shape.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBoostAirTick(int ticks) {
        return ticks == 1 || ticks == 2 || ticks == 5 || ticks == 6 || ticks == 8;
    }

    private static int speedAmplifier(LocalPlayer player) {
        MobEffectInstance speed = player.getEffect(MobEffects.MOVEMENT_SPEED);
        return speed == null ? 0 : speed.getAmplifier();
    }

    private static double horizontalSpeed(Vec3 velocity) {
        return Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
    }
}
