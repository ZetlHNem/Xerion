package com.xerion.forge.movement;

import com.xerion.common.movement.MovementAbilityRules;
import com.xerion.common.movement.MovementControlRules;
import com.xerion.common.movement.MovementSurfaceRules;

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
import com.xerion.forge.gui.dashboard.WidgetSurface;
import com.xerion.forge.mixin.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.MovementInputUpdateEvent;

public final class MovementService {
    private boolean keepSprintKeyForced;
    private boolean keepSprintWasOnGround = true;
    private int keepSprintAirTicks;
    private int noFallSpoofTicks;
    private float originalStepHeight = Float.NaN;
    private boolean enhancedJumpConsumed;
    private boolean enhancedJumpWasGrounded;
    private boolean phaseWasActive;
    private Vec3 antiVoidSafePos;
    private int antiVoidCooldownTicks;
    private Vec3 freezePos;

    public void clearWorldRuntime(Minecraft minecraft) {
        antiVoidSafePos = null;
        antiVoidCooldownTicks = 0;
        freezePos = null;
        noFallSpoofTicks = 0;
        keepSprintWasOnGround = true;
        keepSprintAirTicks = 0;
        enhancedJumpConsumed = false;
        enhancedJumpWasGrounded = false;
        LocalPlayer player = minecraft == null ? null : minecraft.player;
        if (phaseWasActive && player != null) {
            player.noPhysics = false;
            player.hurtMarked = true;
        }
        phaseWasActive = false;
        if (player != null && !Float.isNaN(originalStepHeight)
                && MovementAbilityRules.stepHeightChanged(player.maxUpStep(), originalStepHeight)) {
            player.setMaxUpStep(originalStepHeight);
        }
        originalStepHeight = Float.NaN;
    }

    public void startEarlyTick(Minecraft minecraft) {
        if (BypassProtocolService.correctionRecoveryActive()) {
            releaseForcedSafeWalkKey(minecraft);
            releaseForcedSprintKey(minecraft);
            return;
        }
        updateNormalSafeWalk(minecraft);
        applyFreeze(minecraft);
        applyAntiVoid(minecraft);
        applyNoFall(minecraft);
        applyPhase(minecraft);
        applyFastLadder(minecraft);
        applyFastPlace(minecraft);
        applyStep(minecraft);
        applySpider(minecraft);
        applyEnhancedJump(minecraft);
        applyNoSlowSprint(minecraft);
    }

    public void startLateTick(Minecraft minecraft) {
        if (BypassProtocolService.correctionRecoveryActive()) {
            return;
        }
        applyQuickStop(minecraft);
        applyBhopTurn(minecraft);
        applyKeepSprint(minecraft);
        applySafeWalkVelocityCorrection(minecraft, "SETTINGS_START");
    }

    public void endTick(Minecraft minecraft) {
        if (BypassProtocolService.correctionRecoveryActive()) {
            return;
        }
        applyKeepSprintSpeedCorrection(minecraft);
        MovementSpeedService.applyNoSlowSpeedCorrection(minecraft);
        applyEnhancedJump(minecraft);
        applyJesus(minecraft);
        FlightService.applyElytraFly(minecraft);
        FlightService.applyBoatFly(minecraft);
        FlightService.applyEntitySpeed(minecraft);
        FlightService.applyFlight(minecraft);
        MovementSpeedService.applySpeed(minecraft);
        applyPhase(minecraft);
        applyFreeze(minecraft);
        NoFallWatchdogService.tickPostMove();
    }

    public void playerTickStart(Minecraft minecraft) {
        applySafeWalkVelocityCorrection(minecraft, "PLAYER_START");
    }

    private void applyNoFall(Minecraft minecraft) {
        if (!VisualSettings.noFallEnabled()) {
            noFallSpoofTicks = 0;
            return;
        }

        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.getConnection() == null || player.isPassenger()
                || player.isSpectator() || player.getAbilities().flying) {
            noFallSpoofTicks = 0;
            return;
        }
        if ("Watchdog".equals(VisualSettings.noFallMode())) {
            NoFallWatchdogService.tickPreMove();
            noFallSpoofTicks = 0;
            return;
        }
        if (VisualSettings.noFallNoGround()) {
            minecraft.getConnection().send(new ServerboundMovePlayerPacket.Pos(
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    false
            ));
            return;
        }
        if (player.onGround()) {
            noFallSpoofTicks = 0;
            return;
        }
        if (noFallSpoofTicks > 0) {
            noFallSpoofTicks--;
            return;
        }
        if (!MovementAbilityRules.shouldSpoofNoFall(player.fallDistance, player.getDeltaMovement().y)) {
            return;
        }

        minecraft.getConnection().send(new ServerboundMovePlayerPacket.Pos(
                player.getX(),
                player.getY(),
                player.getZ(),
                true
        ));
        player.fallDistance = 0.0F;
        noFallSpoofTicks = 3;
    }

    private void applyFreeze(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (!VisualSettings.freezeEnabled() || player == null || player.isPassenger()
                || player.isSpectator() || player.getAbilities().flying) {
            freezePos = null;
            return;
        }
        if (minecraft.screen != null) {
            return;
        }
        if (freezePos == null) {
            freezePos = player.position();
        }

        player.setPos(freezePos.x, freezePos.y, freezePos.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        player.hurtMarked = true;
        if (minecraft.getConnection() != null) {
            minecraft.getConnection().send(new ServerboundMovePlayerPacket.Pos(
                    freezePos.x,
                    freezePos.y,
                    freezePos.z,
                    player.onGround()
            ));
        }
    }

    private void applyAntiVoid(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (!VisualSettings.antiVoidEnabled() || player == null || level == null || player.isPassenger()
                || player.isSpectator() || player.getAbilities().flying) {
            antiVoidSafePos = null;
            antiVoidCooldownTicks = 0;
            return;
        }
        if (antiVoidCooldownTicks > 0) {
            antiVoidCooldownTicks--;
        }

        boolean voidBelow = !hasCollisionBelow(level, player);
        if (player.onGround() && !voidBelow) {
            antiVoidSafePos = player.position();
            return;
        }
        if (!MovementAbilityRules.shouldTriggerAntiVoid(
                voidBelow,
                player.fallDistance,
                VisualSettings.antiVoidFallDistance(),
                player.getDeltaMovement().y,
                antiVoidCooldownTicks)) {
            return;
        }

        if (MovementAbilityRules.antiVoidSetback(VisualSettings.antiVoidMode(), antiVoidSafePos != null)) {
            player.setPos(antiVoidSafePos.x, antiVoidSafePos.y, antiVoidSafePos.z);
            player.setDeltaMovement(Vec3.ZERO);
            player.fallDistance = 0.0F;
            if (minecraft.getConnection() != null) {
                minecraft.getConnection().send(new ServerboundMovePlayerPacket.Pos(
                        antiVoidSafePos.x,
                        antiVoidSafePos.y,
                        antiVoidSafePos.z,
                        true
                ));
            }
        } else {
            player.setDeltaMovement(0.0D, 0.0D, 0.0D);
            player.fallDistance = 0.0F;
        }
        player.hurtMarked = true;
        antiVoidCooldownTicks = 10;
    }

    private static boolean hasCollisionBelow(ClientLevel level, LocalPlayer player) {
        int minBuildY = level.getMinBuildHeight();
        AABB box = player.getBoundingBox().inflate(-0.05D, 0.0D, -0.05D);
        int minX = (int) Math.floor(box.minX);
        int maxX = (int) Math.floor(box.maxX);
        int minZ = (int) Math.floor(box.minZ);
        int maxZ = (int) Math.floor(box.maxZ);
        int startY = (int) Math.floor(player.getY() - 0.01D);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = startY; y >= minBuildY; y--) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.getCollisionShape(level, pos).isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void applyFastLadder(Minecraft minecraft) {
        if (!VisualSettings.fastLadderEnabled()) {
            return;
        }

        LocalPlayer player = minecraft.player;
        if (player == null || player.isPassenger() || player.isSpectator()
                || player.getAbilities().flying || !player.onClimbable()) {
            return;
        }
        if (minecraft.screen != null) {
            return;
        }

        Vec3 velocity = player.getDeltaMovement();
        double targetY;
        if (minecraft.options.keyShift.isDown()) {
            targetY = -0.22D;
        } else if (minecraft.options.keyJump.isDown()) {
            targetY = 0.30D;
        } else if (minecraft.options.keyUp.isDown()
                || minecraft.options.keyDown.isDown()
                || minecraft.options.keyLeft.isDown()
                || minecraft.options.keyRight.isDown()) {
            targetY = 0.18D;
        } else {
            return;
        }

        player.setDeltaMovement(velocity.x, targetY, velocity.z);
        player.hurtMarked = true;
    }

    private void applyFastPlace(Minecraft minecraft) {
        if (!VisualSettings.fastPlaceEnabled()) {
            return;
        }
        if (minecraft.player == null || minecraft.gameMode == null || minecraft.screen != null) {
            return;
        }
        ((MinecraftAccessor) minecraft).xerion$setRightClickDelay(0);
    }

    private void applyStep(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            originalStepHeight = Float.NaN;
            return;
        }

        if (Float.isNaN(originalStepHeight)) {
            originalStepHeight = player.maxUpStep();
        }

        boolean active = VisualSettings.stepEnabled()
                && minecraft.screen == null
                && !player.isPassenger()
                && !player.isSpectator()
                && !player.getAbilities().flying
                && (!VisualSettings.stepOnlyGround() || player.onGround());
        if (!active) {
            if (MovementAbilityRules.stepHeightChanged(player.maxUpStep(), originalStepHeight)) {
                player.setMaxUpStep(originalStepHeight);
            }
            return;
        }

        float targetHeight = MovementAbilityRules.stepHeight(VisualSettings.stepHeight());
        if (MovementAbilityRules.stepHeightChanged(player.maxUpStep(), targetHeight)) {
            player.setMaxUpStep(targetHeight);
        }
    }

    private void applySpider(Minecraft minecraft) {
        if (!VisualSettings.spiderEnabled() || VisualSettings.flightEnabled()
                || VisualSettings.phaseEnabled()) {
            return;
        }

        LocalPlayer player = minecraft.player;
        if (player == null || player.input == null || minecraft.screen != null
                || player.isPassenger() || player.isSpectator() || player.getAbilities().flying
                || player.isInWater() || player.isInLava() || player.onClimbable()) {
            return;
        }
        if (!player.horizontalCollision) {
            return;
        }
        if (VisualSettings.spiderOnlyJump() && !minecraft.options.keyJump.isDown()) {
            return;
        }
        if (VisualSettings.spiderOnlyMoving() && !MovementMath.movementInput(player).moving()) {
            return;
        }

        Vec3 velocity = player.getDeltaMovement();
        double speed = MovementAbilityRules.spiderSpeed(VisualSettings.spiderSpeed(), VisualSettings.spiderMode());
        if (velocity.y >= speed) {
            return;
        }
        player.setDeltaMovement(velocity.x, speed, velocity.z);
        player.fallDistance = 0.0F;
        player.hurtMarked = true;
    }

    private void applyEnhancedJump(Minecraft minecraft) {
        boolean enabled = VisualSettings.longJumpEnabled() || VisualSettings.highJumpEnabled();
        LocalPlayer player = minecraft.player;
        if (!enabled || player == null || player.input == null) {
            enhancedJumpConsumed = false;
            enhancedJumpWasGrounded = player != null && player.onGround();
            return;
        }
        if (minecraft.screen != null && WidgetSurface.blocksGameMovementInput()) {
            enhancedJumpConsumed = false;
            enhancedJumpWasGrounded = player.onGround();
            return;
        }
        if (player.isPassenger() || player.isSpectator()
                || player.getAbilities().flying || VisualSettings.flightEnabled()) {
            enhancedJumpConsumed = false;
            enhancedJumpWasGrounded = player.onGround();
            return;
        }

        boolean grounded = player.onGround();
        if (grounded) {
            enhancedJumpConsumed = false;
            enhancedJumpWasGrounded = true;
            return;
        }
        boolean justJumped = enhancedJumpWasGrounded && player.getDeltaMovement().y > 0.05D;
        enhancedJumpWasGrounded = false;
        if (enhancedJumpConsumed || !justJumped) {
            return;
        }

        com.xerion.common.movement.MovementMath.MoveInput moveInput = MovementMath.movementInput(player);
        boolean applyLong = VisualSettings.longJumpEnabled() && moveInput.moving();
        boolean applyHigh = VisualSettings.highJumpEnabled();
        if (!applyLong && !applyHigh) {
            return;
        }

        Vec3 velocity = player.getDeltaMovement();
        double motionX = velocity.x;
        double motionY = velocity.y;
        double motionZ = velocity.z;

        if (applyHigh) {
            motionY = MovementAbilityRules.highJumpMotion(motionY, VisualSettings.highJumpPower());
        }
        if (applyLong) {
            double longPower = MovementAbilityRules.longJumpPower(VisualSettings.longJumpPower());
            double horizontalSpeed = MovementSpeedService.longJumpHorizontalSpeed(longPower);
            double[] motion = MovementMath.directionalMotion(player.getYRot(), moveInput.forward(), moveInput.strafe(), horizontalSpeed);
            motionX = motion[0];
            motionZ = motion[1];
        }

        player.fallDistance = 0.0F;
        player.setDeltaMovement(motionX, motionY, motionZ);
        player.hurtMarked = true;
        enhancedJumpConsumed = true;
    }

    public void movementInputUpdate(MovementInputUpdateEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) {
            return;
        }

        MenuMovementService.applyToInput(Minecraft.getInstance(), event.getInput());
        applyScaffoldTellyJump(Minecraft.getInstance(), player, event.getInput());
        applyNoSlowGrimJump(Minecraft.getInstance(), player, event.getInput());
        if (VisualSettings.flightEnabled()
                && "CubeCraftAirWalk".equals(VisualSettings.flightMode())
                && event.getInput() != null) {
            event.getInput().shiftKeyDown = false;
        }
        boolean active = safeWalkTickActive(Minecraft.getInstance(), player);
        if (active) {
            applySafeWalkInputLock(player, event.getInput());
        }
    }

    private static void applyScaffoldTellyJump(Minecraft minecraft, LocalPlayer player, net.minecraft.client.player.Input input) {
        if (!VisualSettings.scaffoldEnabled()
                || !"TellyBridge".equals(VisualSettings.scaffoldMode())
                || minecraft.screen != null
                || player == null
                || input == null
                || player.isPassenger()
                || player.isSpectator()
                || player.getAbilities().flying
                || !player.onGround()
                || minecraft.options.keyJump.isDown()) {
            return;
        }
        if (Math.abs(input.forwardImpulse) > 1.0E-5F || Math.abs(input.leftImpulse) > 1.0E-5F) {
            input.jumping = true;
        }
    }

    private static void applyNoSlowGrimJump(Minecraft minecraft, LocalPlayer player,
                                            net.minecraft.client.player.Input input) {
        if (!VisualSettings.noSlowEnabled()
                || !"GrimJump".equals(VisualSettings.noSlowMode())
                || minecraft.screen != null
                || player == null
                || input == null
                || !player.onGround()
                || !player.isUsingItem()) {
            return;
        }
        if (Math.abs(input.forwardImpulse) > 1.0E-5F || Math.abs(input.leftImpulse) > 1.0E-5F) {
            input.jumping = true;
        }
    }

    private void applyNoSlowSprint(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (!VisualSettings.noSlowEnabled()
                || !"GrimJump".equals(VisualSettings.noSlowMode())
                || !VisualSettings.noSlowKeepSprinting()
                || player == null
                || player.input == null
                || !player.isUsingItem()) {
            return;
        }
        boolean moving = Math.abs(player.input.forwardImpulse) > 1.0E-5F
                || Math.abs(player.input.leftImpulse) > 1.0E-5F;
        if (moving && !player.isSprinting()) {
            player.setSprinting(true);
        }
    }

    private void applyQuickStop(Minecraft minecraft) {
        if (!VisualSettings.quickStopEnabled()) {
            return;
        }
        LocalPlayer player = minecraft.player;
        if (player == null || player.isPassenger() || !player.onGround()) {
            return;
        }
        if (minecraft.screen != null) {
            return;
        }
        if (minecraft.options.keyUp.isDown()
                || minecraft.options.keyDown.isDown()
                || minecraft.options.keyLeft.isDown()
                || minecraft.options.keyRight.isDown()
                || minecraft.options.keyJump.isDown()) {
            return;
        }

        Vec3 velocity = player.getDeltaMovement();
        if (!MovementSurfaceRules.shouldQuickStop(velocity.x, velocity.z)) {
            return;
        }
        player.setDeltaMovement(0.0, velocity.y, 0.0);
        player.hurtMarked = true;
    }

    private void applyBhopTurn(Minecraft minecraft) {
        if (!VisualSettings.bhopTurnEnabled()) {
            return;
        }
        LocalPlayer player = minecraft.player;
        if (player == null || player.isPassenger() || player.onGround()) {
            return;
        }
        if (player.getAbilities().flying) {
            return;
        }
        if (minecraft.screen != null || !minecraft.options.keyJump.isDown()) {
            return;
        }

        float forward = 0.0f;
        float strafe = 0.0f;
        if (minecraft.options.keyUp.isDown()) {
            forward += 1.0f;
        }
        if (minecraft.options.keyDown.isDown()) {
            forward -= 1.0f;
        }
        if (minecraft.options.keyLeft.isDown()) {
            strafe += 1.0f;
        }
        if (minecraft.options.keyRight.isDown()) {
            strafe -= 1.0f;
        }

        Vec3 velocity = player.getDeltaMovement();
        boolean keepSprintEnabled = VisualSettings.keepSprintEnabled();
        double keepSprintAirTargetSpeed = keepSprintEnabled
                ? MovementSpeedService.keepSprintAirTargetSpeed(keepSprintAirTicks)
                : 0.0D;
        MovementControlRules.HorizontalMotion motion = MovementControlRules.bhopTurnMotion(
                player.getYRot(),
                forward,
                strafe,
                velocity.x,
                velocity.z,
                keepSprintEnabled,
                keepSprintAirTargetSpeed
        );
        if (motion == null) {
            return;
        }

        player.setDeltaMovement(motion.x(), velocity.y, motion.z());
        player.hurtMarked = true;
    }

    private void applyKeepSprint(Minecraft minecraft) {
        if (!VisualSettings.keepSprintEnabled()) {
            keepSprintWasOnGround = true;
            keepSprintAirTicks = 0;
            releaseForcedSprintKey(minecraft);
            return;
        }

        LocalPlayer player = minecraft.player;
        if (player == null || player.isPassenger()) {
            releaseForcedSprintKey(minecraft);
            return;
        }
        if (player.getAbilities().flying) {
            releaseForcedSprintKey(minecraft);
            return;
        }

        boolean hasMoveInput = minecraft.options.keyUp.isDown()
                || minecraft.options.keyDown.isDown()
                || minecraft.options.keyLeft.isDown()
                || minecraft.options.keyRight.isDown();
        boolean canForceSprint = hasMoveInput
                && minecraft.screen == null
                && !player.isShiftKeyDown()
                && !player.isUsingItem()
                && !player.horizontalCollision;

        if (!canForceSprint) {
            releaseForcedSprintKey(minecraft);
            return;
        }

        player.setSprinting(true);
        if (!keepSprintKeyForced && minecraft.getConnection() != null) {
            minecraft.getConnection().send(new ServerboundPlayerCommandPacket(
                    player,
                    ServerboundPlayerCommandPacket.Action.START_SPRINTING
            ));
        }
        minecraft.options.keySprint.setDown(true);
        keepSprintKeyForced = true;
    }

    private void applyKeepSprintSpeedCorrection(Minecraft minecraft) {
        if (!VisualSettings.keepSprintEnabled()) {
            keepSprintWasOnGround = true;
            keepSprintAirTicks = 0;
            return;
        }
        if (VisualSettings.speedEnabled()) {
            keepSprintWasOnGround = true;
            keepSprintAirTicks = 0;
            return;
        }

        LocalPlayer player = minecraft.player;
        if (player == null || player.isPassenger()) {
            keepSprintWasOnGround = true;
            keepSprintAirTicks = 0;
            return;
        }
        if (player.getAbilities().flying) {
            keepSprintWasOnGround = true;
            keepSprintAirTicks = 0;
            return;
        }
        if (minecraft.screen != null || player.isShiftKeyDown() || player.isUsingItem() || player.horizontalCollision) {
            return;
        }

        Vec3 velocity = player.getDeltaMovement();
        double horizontalSpeed = MovementControlRules.horizontalSpeed(velocity.x, velocity.z);
        if (horizontalSpeed < MovementControlRules.MIN_HORIZONTAL_SPEED) {
            return;
        }

        float forward = player.input == null ? 0.0f : player.input.forwardImpulse;
        float strafe = player.input == null ? 0.0f : player.input.leftImpulse;
        if (!MovementControlRules.hasMoveInput(forward, strafe)) {
            if (player.onGround()) {
                keepSprintWasOnGround = true;
                keepSprintAirTicks = 0;
            }
            return;
        }

        if (!player.onGround()) {
            MovementControlRules.AirState airState = MovementControlRules.nextAirState(
                    keepSprintWasOnGround,
                    keepSprintAirTicks
            );
            keepSprintWasOnGround = airState.wasOnGround();
            keepSprintAirTicks = airState.airTicks();

            double targetHorizontalSpeed = MovementSpeedService.keepSprintAirTargetSpeed(keepSprintAirTicks);
            MovementControlRules.HorizontalMotion motion = MovementControlRules.directionalHorizontalMotion(
                    player.getYRot(),
                    forward,
                    strafe,
                    targetHorizontalSpeed
            );
            if (motion != null) {
                player.setDeltaMovement(motion.x(), velocity.y, motion.z());
                player.hurtMarked = true;
            }
            return;
        }

        keepSprintWasOnGround = true;
        keepSprintAirTicks = 0;

        double targetHorizontalSpeed = MovementSpeedService.keepSprintGroundTargetSpeed();
        if (!MovementControlRules.shouldAdjustGroundSpeed(horizontalSpeed, targetHorizontalSpeed)) {
            return;
        }

        MovementControlRules.HorizontalMotion motion = MovementControlRules.scaleHorizontalMotion(
                velocity.x,
                velocity.z,
                horizontalSpeed,
                targetHorizontalSpeed
        );
        if (motion == null) {
            return;
        }
        player.setDeltaMovement(motion.x(), velocity.y, motion.z());
        player.hurtMarked = true;
    }

    private void applyPhase(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        boolean active = VisualSettings.phaseEnabled()
                && player != null
                && player.input != null
                && minecraft.screen == null
                && !player.isPassenger()
                && !player.isSpectator()
                && !player.getAbilities().flying;
        if (!active) {
            if (phaseWasActive && player != null) {
                player.noPhysics = false;
                player.hurtMarked = true;
            }
            phaseWasActive = false;
            return;
        }

        phaseWasActive = true;
        player.noPhysics = false;
        player.fallDistance = 0.0F;
        player.hurtMarked = true;
    }

    private void applyJesus(Minecraft minecraft) {
        if (!VisualSettings.jesusEnabled() || VisualSettings.flightEnabled()
                || VisualSettings.phaseEnabled()) {
            return;
        }

        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || player.isPassenger() || player.isSpectator()
                || player.getAbilities().flying || minecraft.screen != null) {
            return;
        }
        if (minecraft.options.keyShift.isDown()) {
            return;
        }

        boolean fluidBelow = jesusFluidBelow(level, player);
        boolean inFluid = player.isInWater() || VisualSettings.jesusLava() && player.isInLava();
        if (!fluidBelow && !inFluid) {
            return;
        }

        Vec3 velocity = player.getDeltaMovement();
        double motionY = MovementSurfaceRules.jesusMotionY(
                minecraft.options.keyJump.isDown(),
                inFluid,
                VisualSettings.jesusMode(),
                player.tickCount,
                velocity.y);

        player.fallDistance = 0.0F;
        player.setDeltaMovement(velocity.x, motionY, velocity.z);
        player.hurtMarked = true;
    }

    private static boolean jesusFluidBelow(ClientLevel level, LocalPlayer player) {
        AABB box = player.getBoundingBox();
        double y = box.minY - 0.035D;
        double inset = 0.05D;
        return isJesusFluidAt(level, (box.minX + box.maxX) * 0.5D, y, (box.minZ + box.maxZ) * 0.5D)
                || isJesusFluidAt(level, box.minX + inset, y, box.minZ + inset)
                || isJesusFluidAt(level, box.minX + inset, y, box.maxZ - inset)
                || isJesusFluidAt(level, box.maxX - inset, y, box.minZ + inset)
                || isJesusFluidAt(level, box.maxX - inset, y, box.maxZ - inset);
    }

    private static boolean isJesusFluidAt(ClientLevel level, double x, double y, double z) {
        var fluid = level.getFluidState(BlockPos.containing(x, y, z));
        return fluid.is(FluidTags.WATER) || VisualSettings.jesusLava() && fluid.is(FluidTags.LAVA);
    }

    private void applySafeWalkVelocityCorrection(Minecraft minecraft, String source) {
        LocalPlayer player = minecraft.player;
        boolean active = safeWalkTickActive(minecraft, player);
        if (player == null) {
            return;
        }

        Vec3 velocity = player.getDeltaMovement();
        if (!active || velocity.x == 0.0D && velocity.z == 0.0D) {
            return;
        }

        Vec3 adjusted = SafeWalkSupport.clipVelocity(player, velocity);
        if (adjusted.x != velocity.x || adjusted.z != velocity.z) {
            player.setDeltaMovement(adjusted.x, velocity.y, adjusted.z);
            player.hurtMarked = true;
        }
    }

    private static boolean safeWalkTickActive(Minecraft minecraft, LocalPlayer player) {
        return VisualSettings.safeWalkEnabled()
                && "Freeze".equals(VisualSettings.safeWalkMode())
                && player != null
                && minecraft.screen == null
                && !minecraft.options.keyJump.isDown()
                && player.onGround()
                && !player.isPassenger()
                && !player.isSpectator()
                && !player.getAbilities().flying;
    }

    private static void applySafeWalkInputLock(LocalPlayer player, net.minecraft.client.player.Input input) {
        SafeWalkSupport.applyInputLock(player, input);
    }

    private boolean safeWalkSneakForced;

    private void updateNormalSafeWalk(Minecraft minecraft) {
        LocalPlayer player = minecraft == null ? null : minecraft.player;
        boolean normal = VisualSettings.safeWalkEnabled()
                && "Normal".equals(VisualSettings.safeWalkMode())
                && player != null
                && minecraft.screen == null
                && player.onGround()
                && !player.isPassenger()
                && !player.isSpectator()
                && !player.getAbilities().flying;

        if (normal) {
            minecraft.options.keyShift.setDown(SafeWalkSupport.isOnOpenOpalEdge(player));
            safeWalkSneakForced = true;
            return;
        }

        if (safeWalkSneakForced) {
            releaseForcedSafeWalkKey(minecraft);
        }
    }

    private void releaseForcedSafeWalkKey(Minecraft minecraft) {
        if (!safeWalkSneakForced || minecraft == null) {
            return;
        }
        minecraft.options.keyShift.setDown(MenuMovementService.keyDown(minecraft, minecraft.options.keyShift));
        safeWalkSneakForced = false;
    }

    private void releaseForcedSprintKey(Minecraft minecraft) {
        if (!keepSprintKeyForced) {
            return;
        }
        LocalPlayer player = minecraft.player;
        if (player != null && minecraft.getConnection() != null) {
            minecraft.getConnection().send(new ServerboundPlayerCommandPacket(
                    player,
                    ServerboundPlayerCommandPacket.Action.STOP_SPRINTING
            ));
        }
        minecraft.options.keySprint.setDown(false);
        keepSprintKeyForced = false;
    }

}


