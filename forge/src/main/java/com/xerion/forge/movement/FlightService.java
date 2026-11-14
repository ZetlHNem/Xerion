package com.xerion.forge.movement;

import com.xerion.common.movement.FlightRules;

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

import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.packet.BlockHolder;
import com.xerion.forge.packet.PacketBlockageService;
import com.xerion.forge.util.DisablerService;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public final class FlightService {
    private static final BlockHolder FAST_BLINK = new BlockHolder(PacketBlockageService.outbound());
    private static final int PING_SPOOF_DAMAGE_TIMEOUT_TICKS = 24;
    private static final int PING_SPOOF_MAX_DAMAGE_ATTEMPTS = 4;

    private static int cubeCraftAirWalkTicks;
    private static int cubeCraftFastTicks;
    private static boolean pingSpoofHasBeenHurt;
    private static boolean pingSpoofHasBeenTeleported;
    private static boolean pingSpoofWaitingForDamage;
    private static int pingSpoofReboostTicksLeft;
    private static int pingSpoofDamageTicksLeft;
    private static int pingSpoofDamageAttempts;
    private static String lastFlightMode = "";

    private FlightService() {
    }

    public static void applyFlight(Minecraft minecraft) {
        if (!VisualSettings.flightEnabled()) {
            resetFlightModes();
            return;
        }
        if (VisualSettings.phaseEnabled()
                || elytraFlyActive(minecraft.player)) {
            return;
        }

        LocalPlayer player = minecraft.player;
        if (player == null || player.input == null || player.isPassenger()
                || player.isSpectator() || player.getAbilities().flying) {
            return;
        }
        if (minecraft.screen != null) {
            return;
        }

        String mode = VisualSettings.flightMode();
        if (!mode.equals(lastFlightMode)) {
            resetFlightModes();
            lastFlightMode = mode;
            if ("CubeCraftPingSpoof".equals(mode)) {
                onPingSpoofModeEnable();
            }
        }
        if ("CubeCraftAirWalk".equals(mode)) {
            applyCubeCraftAirWalk(minecraft, player);
            return;
        }
        if ("CubeCraftFast".equals(mode)) {
            applyCubeCraftFast(minecraft, player);
            return;
        }
        if ("CubeCraftPingSpoof".equals(mode)) {
            applyCubeCraftPingSpoof(minecraft, player);
            return;
        }
        FAST_BLINK.release();

        com.xerion.common.movement.MovementMath.MoveInput moveInput = MovementMath.movementInput(player);
        double horizontalSpeed = FlightRules.horizontalSpeed(VisualSettings.flightHorizontalSpeed());
        double verticalSpeed = FlightRules.verticalSpeed(VisualSettings.flightVerticalSpeed());
        double motionX = 0.0D;
        double motionZ = 0.0D;
        if (moveInput.moving() && horizontalSpeed > 1.0E-5D) {
            double[] motion = MovementMath.directionalMotion(player.getYRot(), moveInput.forward(), moveInput.strafe(), horizontalSpeed);
            motionX = motion[0];
            motionZ = motion[1];
        }

        double motionY;
        if (minecraft.options.keyJump.isDown()) {
            motionY = verticalSpeed;
        } else if (minecraft.options.keyShift.isDown()) {
            motionY = -verticalSpeed;
        } else {
            motionY = FlightRules.glideMotion(VisualSettings.flightMode(), VisualSettings.flightGlide(), 0.08D, 0.0D);
        }

        player.fallDistance = 0.0F;
        player.setDeltaMovement(motionX, motionY, motionZ);
        player.hurtMarked = true;
    }

    private static void applyCubeCraftAirWalk(Minecraft minecraft, LocalPlayer player) {
        player.fallDistance = 0.0F;
        if (player.onGround()) {
            return;
        }
        com.xerion.common.movement.MovementMath.MoveInput moveInput = MovementMath.movementInput(player);
        Vec3 velocity = player.getDeltaMovement();
        if (!moveInput.moving()) {
            player.setDeltaMovement(0.0D, velocity.y, 0.0D);
        }
        if (cubeCraftAirWalkTicks++ % 6 != 0) {
            return;
        }
        double motionY = minecraft.options.keyShift.isDown() ? -0.4D
                : minecraft.options.keyJump.isDown() ? 0.42D : 0.2D;
        double horizontal = moveInput.moving()
                ? VisualSettings.flightCubeCraftAirWalkSpeed() + (Math.random() - 0.5D) * 0.01D
                : 0.0D;
        double[] motion = MovementMath.directionalMotion(
                player.getYRot(), moveInput.forward(), moveInput.strafe(), horizontal);
        player.setDeltaMovement(motion[0], motionY, motion[1]);
        player.hurtMarked = true;
    }

    private static void applyCubeCraftFast(Minecraft minecraft, LocalPlayer player) {
        player.fallDistance = 0.0F;
        if (DisablerService.isCubeCraftWaiting()) {
            FAST_BLINK.release();
            return;
        }
        com.xerion.common.movement.MovementMath.MoveInput moveInput = MovementMath.movementInput(player);
        boolean moving = moveInput.moving();
        double targetY = 0.0D;
        if (minecraft.options.keyJump.isDown()) {
            targetY = VisualSettings.flightCubeCraftVertical();
        } else if (minecraft.options.keyShift.isDown()) {
            targetY = -VisualSettings.flightCubeCraftVertical();
        }
        if (cubeCraftFastTicks++ % 6 == 0) {
            FAST_BLINK.block(null, packet -> !isBlinkIgnored(packet));
            Vec3 velocity = player.getDeltaMovement();
            player.setDeltaMovement(0.0D, velocity.y, 0.0D);
            if (moving) {
                applyHorizontal(player, moveInput, VisualSettings.flightCubeCraftSpeed());
            }
        } else if (!moving) {
            Vec3 velocity = player.getDeltaMovement();
            player.setDeltaMovement(0.0D, velocity.y, 0.0D);
        } else {
            FAST_BLINK.release();
        }
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(velocity.x, targetY, velocity.z);
        player.hurtMarked = true;
    }

    private static void applyCubeCraftPingSpoof(Minecraft minecraft, LocalPlayer player) {
        player.fallDistance = 0.0F;
        if (pingSpoofWaitingForDamage) {
            if (tryCompletePingSpoofDamage(player)) {
                return;
            }
            if (pingSpoofDamageTicksLeft-- > 0) {
                return;
            }
            if (pingSpoofDamageAttempts < PING_SPOOF_MAX_DAMAGE_ATTEMPTS) {
                sendPingSpoofBoost(player);
                return;
            }
            VisualSettings.setFlightEnabled(false);
            return;
        }
        if (pingSpoofReboostTicksLeft > 0) {
            pingSpoofReboostTicksLeft--;
            return;
        }
        if (VisualSettings.flightCubeCraftBoostOnce()) {
            VisualSettings.setFlightEnabled(false);
            return;
        }
        sendPingSpoofBoost(player);
        pingSpoofReboostTicksLeft = VisualSettings.flightCubeCraftReboostTicks();
        if (!pingSpoofHasBeenHurt) {
            return;
        }
        double motionY = minecraft.options.keyJump.isDown() ? VisualSettings.flightCubeCraftVertical()
                : minecraft.options.keyShift.isDown() ? -VisualSettings.flightCubeCraftVertical() : 0.0D;
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(velocity.x, motionY, velocity.z);
        if (VisualSettings.flightCubeCraftConstantSpeed()) {
            com.xerion.common.movement.MovementMath.MoveInput moveInput = MovementMath.movementInput(player);
            if (moveInput.moving()) {
                applyHorizontal(player, moveInput, VisualSettings.flightCubeCraftSpeed());
            }
        }
        player.hurtMarked = true;
    }

    private static void sendPingSpoofBoost(LocalPlayer player) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null) {
            return;
        }
        pingSpoofHasBeenHurt = false;
        pingSpoofWaitingForDamage = true;
        pingSpoofDamageTicksLeft = Math.max(PING_SPOOF_DAMAGE_TIMEOUT_TICKS,
                Math.min(40, VisualSettings.flightCubeCraftReboostTicks()));
        pingSpoofDamageAttempts++;
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        sendSilent(new ServerboundMovePlayerPacket.Pos(x, y, z, false));
        sendSilent(new ServerboundMovePlayerPacket.Pos(x, y + 3.25D, z, false));
        sendSilent(new ServerboundMovePlayerPacket.Pos(x, y, z, false));
        sendSilent(new ServerboundMovePlayerPacket.Pos(x, y, z, true));
    }

    private static boolean tryCompletePingSpoofDamage(LocalPlayer player) {
        if (player.hurtTime <= 0 || pingSpoofHasBeenHurt) {
            return false;
        }
        pingSpoofHasBeenHurt = true;
        pingSpoofWaitingForDamage = false;
        pingSpoofDamageAttempts = 0;
        pingSpoofDamageTicksLeft = 0;
        pingSpoofReboostTicksLeft = VisualSettings.flightCubeCraftReboostTicks();
        com.xerion.common.movement.MovementMath.MoveInput moveInput = MovementMath.movementInput(player);
        if (moveInput.moving()) {
            applyHorizontal(player, moveInput, VisualSettings.flightCubeCraftSpeed());
        }
        if (!pingSpoofHasBeenTeleported && VisualSettings.flightCubeCraftNostalgia()) {
            pingSpoofHasBeenTeleported = true;
            player.setPos(player.getX(), player.getY() + 0.42D, player.getZ());
        }
        return true;
    }

    private static void onPingSpoofModeEnable() {
        if (!com.xerion.common.config.PingSpoofSettings.enabled()) {
            com.xerion.common.config.PingSpoofSettings.setEnabled(true);
        }
        if (VisualSettings.speedEnabled()) {
            VisualSettings.setSpeedEnabled(false);
        }
        pingSpoofHasBeenHurt = false;
        pingSpoofHasBeenTeleported = false;
        pingSpoofWaitingForDamage = false;
        pingSpoofReboostTicksLeft = 0;
        pingSpoofDamageTicksLeft = 0;
        pingSpoofDamageAttempts = 0;
    }

    private static void applyHorizontal(LocalPlayer player,
                                        com.xerion.common.movement.MovementMath.MoveInput moveInput, double speed) {
        double[] motion = MovementMath.directionalMotion(
                player.getYRot(), moveInput.forward(), moveInput.strafe(), speed);
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(motion[0], velocity.y, motion[1]);
    }

    private static boolean isBlinkIgnored(Packet<?> packet) {
        return packet instanceof ServerboundKeepAlivePacket
                || packet instanceof ServerboundPongPacket
                || packet instanceof ServerboundChatPacket
                || packet instanceof ServerboundChatCommandPacket;
    }

    private static void sendSilent(Packet<?> packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null) {
            return;
        }
        PacketBlockageService.runDirectSend(() -> minecraft.getConnection().send(packet));
    }

    public static void resetFlightModes() {
        FAST_BLINK.release();
        cubeCraftAirWalkTicks = 0;
        cubeCraftFastTicks = 0;
        pingSpoofHasBeenHurt = false;
        pingSpoofHasBeenTeleported = false;
        pingSpoofWaitingForDamage = false;
        pingSpoofReboostTicksLeft = 0;
        pingSpoofDamageTicksLeft = 0;
        pingSpoofDamageAttempts = 0;
    }

    public static void applyElytraFly(Minecraft minecraft) {
        if (!VisualSettings.elytraFlyEnabled()) {
            return;
        }
        LocalPlayer player = minecraft.player;
        if (player == null || player.input == null || minecraft.getConnection() == null
                || minecraft.screen != null || player.isPassenger() || player.isSpectator()
                || player.getAbilities().flying || VisualSettings.phaseEnabled()) {
            return;
        }
        if (!wearingElytra(player)) {
            return;
        }

        if (!player.isFallFlying()) {
            if (!VisualSettings.elytraFlyAutoStart() || player.onGround() || player.isInWater() || player.isInLava()) {
                return;
            }
            minecraft.getConnection().send(new ServerboundPlayerCommandPacket(
                    player,
                    ServerboundPlayerCommandPacket.Action.START_FALL_FLYING
            ));
            player.startFallFlying();
            return;
        }

        com.xerion.common.movement.MovementMath.MoveInput moveInput = MovementMath.movementInput(player);
        double horizontalSpeed = FlightRules.boostedHorizontalSpeed(VisualSettings.elytraFlyHorizontalSpeed(),
                "Boost".equals(VisualSettings.elytraFlyMode()));
        double verticalSpeed = FlightRules.boostedVerticalSpeed(VisualSettings.elytraFlyVerticalSpeed(),
                "Boost".equals(VisualSettings.elytraFlyMode()));

        double motionX = 0.0D;
        double motionZ = 0.0D;
        if (moveInput.moving() && horizontalSpeed > 1.0E-5D) {
            double[] motion = MovementMath.directionalMotion(player.getYRot(), moveInput.forward(), moveInput.strafe(), horizontalSpeed);
            motionX = motion[0];
            motionZ = motion[1];
        }

        double motionY;
        if (minecraft.options.keyJump.isDown()) {
            motionY = verticalSpeed;
        } else if (minecraft.options.keyShift.isDown()) {
            motionY = -verticalSpeed;
        } else {
            motionY = FlightRules.elytraGlideMotion(VisualSettings.elytraFlyGlide());
        }

        player.fallDistance = 0.0F;
        player.setDeltaMovement(motionX, motionY, motionZ);
        player.hurtMarked = true;
    }

    public static boolean elytraFlyActive(LocalPlayer player) {
        return VisualSettings.elytraFlyEnabled()
                && player != null
                && player.isFallFlying()
                && wearingElytra(player);
    }

    public static void applyBoatFly(Minecraft minecraft) {
        if (!VisualSettings.boatFlyEnabled()) {
            return;
        }
        LocalPlayer player = minecraft.player;
        if (player == null || player.input == null || minecraft.screen != null || player.isSpectator()) {
            return;
        }
        if (!(player.getVehicle() instanceof Boat boat)) {
            return;
        }

        com.xerion.common.movement.MovementMath.MoveInput moveInput = MovementMath.movementInput(player);
        double horizontalSpeed = FlightRules.horizontalSpeed(VisualSettings.boatFlyHorizontalSpeed());
        double verticalSpeed = FlightRules.verticalSpeed(VisualSettings.boatFlyVerticalSpeed());
        double motionX = 0.0D;
        double motionZ = 0.0D;
        if (moveInput.moving() && horizontalSpeed > 1.0E-5D) {
            double[] motion = MovementMath.directionalMotion(player.getYRot(), moveInput.forward(), moveInput.strafe(), horizontalSpeed);
            motionX = motion[0];
            motionZ = motion[1];
        }

        double motionY;
        if (minecraft.options.keyJump.isDown()) {
            motionY = verticalSpeed;
        } else if (minecraft.options.keyShift.isDown()) {
            motionY = -verticalSpeed;
        } else {
            motionY = FlightRules.glideMotion(VisualSettings.boatFlyMode(), VisualSettings.boatFlyGlide(), 0.04D, 0.0D);
        }

        boat.fallDistance = 0.0F;
        boat.setDeltaMovement(motionX, motionY, motionZ);
        boat.hurtMarked = true;
    }

    public static void applyEntitySpeed(Minecraft minecraft) {
        if (!VisualSettings.entitySpeedEnabled()) {
            return;
        }
        LocalPlayer player = minecraft.player;
        if (player == null || player.input == null || minecraft.screen != null || player.isSpectator()) {
            return;
        }
        Entity vehicle = player.getVehicle();
        if (vehicle == null || vehicle == player) {
            return;
        }
        if (VisualSettings.boatFlyEnabled() && vehicle instanceof Boat) {
            return;
        }

        com.xerion.common.movement.MovementMath.MoveInput moveInput = MovementMath.movementInput(player);
        if (!moveInput.moving()) {
            return;
        }

        double speed = FlightRules.entitySpeed(VisualSettings.entitySpeed());
        if (speed <= 1.0E-5D) {
            return;
        }

        Vec3 velocity = vehicle.getDeltaMovement();
        double[] motion = MovementMath.directionalMotion(player.getYRot(), moveInput.forward(), moveInput.strafe(), speed);
        double motionY = VisualSettings.entitySpeedKeepY() ? velocity.y : 0.0D;
        vehicle.setDeltaMovement(motion[0], motionY, motion[1]);
        vehicle.hurtMarked = true;
    }

    private static boolean wearingElytra(LocalPlayer player) {
        return player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA);
    }
}
