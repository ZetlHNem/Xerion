package com.xerion.forge.combat;

import com.xerion.common.movement.ScaffoldRotation;

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

import com.xerion.common.combat.CombatActionRules;
import com.xerion.common.combat.CombatAimRules;
import com.xerion.common.combat.CombatCompatibilityRules;
import com.xerion.common.config.BypassSettings;
import com.xerion.common.config.CombatSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class KillAuraService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final int MULTI_TARGET_LIMIT = 3;
    private static volatile boolean visualBlockTargetActive;

    private int cooldownTicks;
    private int swingCooldownTicks;
    private int switchIndex;
    private int lastTargetId = -1;
    private boolean randomNextSmash;
    private float currentYaw;
    private float currentPitch;
    private float randomYawOffset;
    private float randomPitchOffset;
    private float compatibilityYawOffset;
    private float compatibilityPitchOffset;
    private int compatibilityRotationTicks;
    private int lastAttackTargetId = -1;
    private long lastAttackNanos;
    private long lastHeypixelAttackTime;
    private boolean hasRenderTarget;
    private boolean renderInitialized;
    private boolean renderReturning;
    private boolean renderOverrideActive;
    private LivingEntity activeTarget;
    private float renderLastYaw;
    private float renderLastPitch;
    private float renderYaw;
    private float renderPitch;
    private float renderTargetYaw;
    private float renderTargetPitch;
    private long lastRenderNanos;
    private boolean autoblockUseDown;

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
        if (swingCooldownTicks > 0) {
            swingCooldownTicks--;
        }
        if (!CombatSettings.killAuraEnabled()) {
            resetIdleState();
            beginReturnToPlayer();
            return;
        }
        if (BypassProtocolService.correctionRecoveryActive()) {
            resetIdleState();
            beginReturnToPlayer();
            return;
        }

        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (player == null || level == null || MC.gameMode == null) {
            resetIdleState();
            beginReturnToPlayer();
            return;
        }
        if (MC.screen != null || player.isPassenger() || player.isSpectator() || !validWeapon(player)
                || CombatSettings.killAuraRequireAttackKey() && !MC.options.keyAttack.isDown()) {
            resetIdleState();
            beginReturnToPlayer();
            return;
        }

        List<LivingEntity> targets = targets(player, level, CombatSettings.range());
        if (targets.isEmpty()) {
            activeTarget = null;
            CombatHudTarget.clear();
            visualBlockTargetActive = false;
            updateAutoblock(false);
            if (rotateTowardDistantTarget(player, level)) {
                return;
            }
            swingTick(player, level);
            beginReturnToPlayer();
            return;
        }
        activeTarget = targets.get(0);
        CombatHudTarget.update(activeTarget);
        visualBlockTargetActive = true;
        boolean sweepAttack = shouldSweepAttack(player, targets);
        updateAutoblock(true);

        if (CombatSettings.killAuraHeypixelBypass()
                ? !heypixelAttackReady()
                : cooldownTicks > 0 || attackStrengthBlocked(player, sweepAttack)) {
            rotateToward(player, targets.get(0));
            return;
        }

        String mode = CombatSettings.attackMode();
        if (sweepAttack) {
            attack(player, targets.get(0), true);
        } else if (CombatActionRules.isMultiMode(mode)) {
            int limit = CombatActionRules.cappedTargetCount(MULTI_TARGET_LIMIT, targets.size());
            for (int i = 0; i < limit; i++) {
                attack(player, targets.get(i), false);
            }
        } else if (CombatActionRules.isSwitchMode(mode)) {
            switchIndex = CombatActionRules.normalizedSwitchIndex(switchIndex, targets.size());
            attack(player, targets.get(switchIndex), false);
            switchIndex = CombatActionRules.nextSwitchIndex(switchIndex, targets.size());
        } else {
            attack(player, targets.get(0), false);
        }

        cooldownTicks = CombatSettings.attackDelayEnabled()
                ? Math.min(attackDelayTicks(), CombatSettings.attackDelayMaxCooldown())
                : attackDelayTicks();
        chooseNextRandomBehavior();
    }

    private void resetIdleState() {
        switchIndex = 0;
        lastTargetId = -1;
        activeTarget = null;
        CombatHudTarget.clear();
        visualBlockTargetActive = false;
        randomNextSmash = false;
        swingCooldownTicks = 0;
        lastHeypixelAttackTime = 0L;
        resetCompatibilityState();
        updateAutoblock(false);
    }

    private boolean attackStrengthBlocked(LocalPlayer player, boolean sweepAttack) {
        if (CombatSettings.attackDelayEnabled()) {
            return false;
        }
        return CombatSettings.attackCooldown19()
                && player.getAttackStrengthScale(0.0f) < requiredAttackStrength(player, sweepAttack);
    }

    private boolean rotateTowardDistantTarget(LocalPlayer player, ClientLevel level) {
        if (CombatSettings.rotationRange() <= CombatSettings.range()) {
            return false;
        }
        LivingEntity candidate = KillAuraTargetSelector.nearestVisibleTarget(
                player, level, CombatSettings.rotationRange());
        if (candidate == null) {
            return false;
        }
        rotateToward(player, candidate);
        return true;
    }

    private void swingTick(LocalPlayer player, ClientLevel level) {
        if (player.isUsingItem()) {
            return;
        }
        double closest = KillAuraTargetSelector.closestTargetDistance(player, level, CombatSettings.swingRange());
        if (closest > CombatSettings.swingRange()) {
            swingCooldownTicks = 0;
            return;
        }
        if (swingCooldownTicks > 0) {
            return;
        }
        swingCooldownTicks = CombatActionRules.attackDelayTicks(CombatSettings.swingCps());
        if (!CombatSettings.hideFakeSwings()) {
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private void updateAutoblock(boolean targetActive) {
        LocalPlayer player = MC.player;
        boolean continuingOwnUse = autoblockUseDown && player != null && player.isUsingItem();
        boolean shouldPress = targetActive
                && CombatSettings.AUTOBLOCK_VANILLA.equals(CombatSettings.autoblockMode())
                && player != null
                && (!player.isUsingItem() || continuingOwnUse)
                && !player.isSpectator()
                && !player.isPassenger()
                && MC.screen == null
                && player.getMainHandItem().getItem() instanceof SwordItem;
        if (shouldPress) {
            MC.options.keyUse.setDown(true);
            autoblockUseDown = true;
            return;
        }
        if (autoblockUseDown) {
            MC.options.keyUse.setDown(false);
        }
        autoblockUseDown = false;
    }

    public void renderTick(float partialTick) {
        LocalPlayer player = MC.player;
        if (player == null) {
            renderInitialized = false;
            renderReturning = false;
            renderOverrideActive = false;
            return;
        }
        if (!hasRenderTarget && !renderReturning) {
            return;
        }

        long now = System.nanoTime();
        float dt = CombatActionRules.frameDeltaSeconds(now, lastRenderNanos, 0.08f);
        lastRenderNanos = now;

        if (!renderInitialized) {
            renderYaw = player.getYRot();
            renderPitch = player.getXRot();
            renderLastYaw = renderYaw;
            renderLastPitch = renderPitch;
            renderInitialized = true;
        }

        if (renderReturning) {
            renderTargetYaw = player.getYRot();
            renderTargetPitch = player.getXRot();
        }

        float alpha = KillAuraAimMath.renderLerpAlpha(dt);
        renderLastYaw = renderYaw;
        renderLastPitch = renderPitch;
        renderYaw = KillAuraAimMath.lerpYaw(renderYaw, renderTargetYaw, alpha);
        renderPitch = CombatAimRules.lerp(alpha, renderPitch, renderTargetPitch);
        renderPitch = CombatAimRules.clamp(renderPitch, -90.0f, 90.0f);
        renderOverrideActive = true;

        if (hasRenderTarget && !CombatSettings.silentRotations()) {
            player.setYRot(renderYaw);
            player.setXRot(renderPitch);
        }

        if (renderReturning && CombatActionRules.renderReturnComplete(
                renderTargetYaw, renderYaw, renderTargetPitch, renderPitch, 0.65f)) {
            renderReturning = false;
            renderInitialized = false;
            renderOverrideActive = false;
        }
    }

    private void beginReturnToPlayer() {
        hasRenderTarget = false;
        activeTarget = null;
        CombatHudTarget.clear();
        lastTargetId = -1;
        randomYawOffset = 0.0f;
        randomPitchOffset = 0.0f;
        if (renderInitialized || renderOverrideActive || ScaffoldRotation.hasRenderOverride()) {
            renderReturning = true;
        }
    }

    public boolean renderOverrideActive() {
        return renderOverrideActive;
    }

    public float renderYaw() {
        return renderYaw;
    }

    public float renderLastYaw() {
        return renderLastYaw;
    }

    public float renderPitch() {
        return renderPitch;
    }

    public float renderLastPitch() {
        return renderLastPitch;
    }

    public LivingEntity activeTarget() {
        return validActiveTarget() ? activeTarget : null;
    }

    public static boolean visualBlockTargetActive() {
        return visualBlockTargetActive;
    }

    private boolean validActiveTarget() {
        LocalPlayer player = MC.player;
        return player != null && activeTarget != null && validTarget(player, activeTarget);
    }

    private static boolean validWeapon(LocalPlayer player) {
        Item item = player.getMainHandItem().getItem();
        if (CombatSettings.killAuraRequireWeapon()
                && !(item instanceof SwordItem || item instanceof AxeItem || item instanceof PickaxeItem)) {
            return false;
        }
        String weapon = CombatSettings.weapon();
        if ("Best Weapon".equals(weapon)) {
            return true;
        }
        if ("Sword".equals(weapon) ? item instanceof SwordItem : item instanceof AxeItem) {
            return true;
        }
        return CombatSettings.autoWeaponEnabled() && AutoWeaponService.hasWeaponSlot(player);
    }

    public static List<LivingEntity> targets(LocalPlayer player, ClientLevel level, double range) {
        return KillAuraTargetSelector.targets(player, level, range);
    }

    public static boolean validTarget(LocalPlayer player, LivingEntity target) {
        return validTarget(player, target, CombatSettings.range());
    }

    public static boolean validTarget(LocalPlayer player, LivingEntity target, double range) {
        return KillAuraTargetSelector.validTarget(player, target, range);
    }

    private void attack(LocalPlayer player, LivingEntity target, boolean sweepAttack) {
        rotateToward(player, target);
        if (!sweepAttack && !CombatAssistService.canCriticalAttack(player, target, CombatSettings.range())) {
            return;
        }
        long now = System.nanoTime();
        if (BypassSettings.enabled() && BypassSettings.actionDeduplication()
                && target.getId() == lastAttackTargetId
                && CombatCompatibilityRules.duplicateAction(now, lastAttackNanos,
                BypassSettings.actionDeduplicationWindowMs())) {
            return;
        }
        int shieldSlot = ShieldBreakerService.beforeAttack(player, target);
        int weaponSlot = shieldSlot < 0 ? AutoWeaponService.beforeAttack(player) : -1;
        if (!sweepAttack) {
            KnockbackService.beforeAttack(player);
        }
        CriticalsService.beforeAttack(player, target);
        MC.gameMode.attack(player, target);
        FakePlayerRegistry.hurtIfDummy(target);
        HitSoundService.play(target);
        KillAuraBlockAnimation.markAttack();
        player.swing(InteractionHand.MAIN_HAND);
        if (!sweepAttack) {
            KnockbackService.afterAttack(player);
        }
        AutoWeaponService.afterAttack(player, weaponSlot);
        ShieldBreakerService.afterAttack(player, shieldSlot);
        lastAttackTargetId = target.getId();
        lastAttackNanos = now;
        if (CombatSettings.killAuraHeypixelBypass()) {
            lastHeypixelAttackTime = System.currentTimeMillis();
        }
    }

    private void rotateToward(LocalPlayer player, LivingEntity target) {
        KillAuraAimMath.Rotation rotation = KillAuraAimMath.rotationTo(player, target);
        float yaw = rotation.yaw();
        float pitch = rotation.pitch();

        if (target.getId() != lastTargetId) {
            lastTargetId = target.getId();
            currentYaw = player.getYRot();
            currentPitch = player.getXRot();
            renderYaw = player.getYRot();
            renderPitch = player.getXRot();
            renderLastYaw = renderYaw;
            renderLastPitch = renderPitch;
            renderInitialized = true;
            lastRenderNanos = System.nanoTime();
            randomYawOffset = 0.0f;
            randomPitchOffset = 0.0f;
            compatibilityYawOffset = 0.0f;
            compatibilityPitchOffset = 0.0f;
            compatibilityRotationTicks = 0;
        }

        float previousYaw = currentYaw;
        float previousPitch = currentPitch;

        float jitter = rotationJitter();
        if (jitter > 0.01f) {
            randomYawOffset = KillAuraAimMath.approach(randomYawOffset,
                    (float) ThreadLocalRandom.current().nextDouble(-jitter, jitter),
                    0.06f);
            randomPitchOffset = KillAuraAimMath.approach(randomPitchOffset,
                    (float) ThreadLocalRandom.current().nextDouble(-jitter, jitter),
                    0.045f);
            yaw += randomYawOffset;
            pitch += randomPitchOffset;
        }

        if (CombatSettings.killAuraHeypixelBypass()) {
            float speed = KillAuraAimMath.rotationSpeed();
            float[] moved = heypixelRotate(currentYaw, currentPitch, yaw, pitch, speed);
            yaw = moved[0];
            pitch = moved[1];
            currentYaw = yaw;
            currentPitch = pitch;
        } else if (CombatSettings.smoothAim()) {
            float speed = KillAuraAimMath.rotationSpeed();
            currentYaw = KillAuraAimMath.rotateYaw(currentYaw, yaw, speed);
            currentPitch = KillAuraAimMath.approach(currentPitch, pitch, speed * 0.72f);
            yaw = currentYaw;
            pitch = currentPitch;
        } else {
            currentYaw = yaw;
            currentPitch = pitch;
        }

        if (BypassSettings.enabled() && !BypassProtocolService.correctionRecoveryActive()) {
            updateCompatibilityRotationOffsets();
            CombatCompatibilityRules.Rotation filtered = CombatCompatibilityRules.filterRotation(
                    previousYaw, previousPitch, yaw, pitch,
                    MC.options.sensitivity().get(),
                    BypassSettings.rotationRandomization() ? BypassSettings.rotationRandomizationAmount() : 0.0f,
                    normalizedSample(compatibilityYawOffset), normalizedSample(compatibilityPitchOffset),
                    BypassSettings.rotationQuantization());
            yaw = filtered.yaw();
            pitch = filtered.pitch();
            currentYaw = yaw;
            currentPitch = pitch;
        }

        pitch = CombatAimRules.clamp(pitch, -90.0f, 90.0f);
        renderTargetYaw = yaw;
        renderTargetPitch = pitch;
        hasRenderTarget = true;
        renderReturning = false;
        ScaffoldRotation.requestPacketOnly(yaw, pitch, true);

        if (CombatSettings.silentRotations()) {
            return;
        }

        if (!renderInitialized) {
            player.setYRot(yaw);
            player.setXRot(pitch);
        }
    }

    private boolean heypixelAttackReady() {
        if (lastHeypixelAttackTime == 0L) {
            return true;
        }
        int cps = Math.max(1, Math.round(CombatSettings.cpsLimit()));
        double baseDelay = 1000.0D / cps;
        double delay = baseDelay + (ThreadLocalRandom.current().nextDouble() - 0.5D) * baseDelay * 0.4D;
        return System.currentTimeMillis() - lastHeypixelAttackTime >= delay;
    }

    private static float[] heypixelRotate(float currentYaw, float currentPitch,
                                          float targetYaw, float targetPitch, float speed) {
        float deltaYaw = Mth.wrapDegrees(targetYaw - currentYaw);
        float deltaPitch = targetPitch - currentPitch;
        double distance = Math.sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch);
        if (distance <= 1.0E-6D) {
            return new float[]{currentYaw, currentPitch};
        }
        double distributionYaw = Math.abs(deltaYaw / distance);
        double distributionPitch = Math.abs(deltaPitch / distance);
        float maxYaw = (float) (speed * distributionYaw);
        float maxPitch = (float) (speed * distributionPitch);
        float moveYaw = Mth.clamp(deltaYaw, -maxYaw, maxYaw);
        float movePitch = Mth.clamp(deltaPitch, -maxPitch, maxPitch);
        float yaw = currentYaw + moveYaw;
        float pitch = Mth.clamp(currentPitch + movePitch, -90.0F, 90.0F);
        return patchConstantRotation(yaw, pitch, currentYaw, currentPitch);
    }

    private static float[] patchConstantRotation(float yaw, float pitch,
                                                 float fromYaw, float fromPitch) {
        double sensitivity = MC.options.sensitivity().get() * 0.6D + 0.2D;
        double multiplier = (sensitivity * sensitivity * sensitivity) * 8.0D;
        double divisor = multiplier * 0.15D;
        if (divisor <= 0.0D) {
            return new float[]{yaw, pitch};
        }
        float adjustedYaw = fromYaw + (float) (Math.round((yaw - fromYaw) / divisor) * divisor);
        float adjustedPitch = fromPitch + (float) (Math.round((pitch - fromPitch) / divisor) * divisor);
        return new float[]{adjustedYaw, adjustedPitch};
    }

    private static int attackDelayTicks() {
        if (BypassSettings.enabled() && BypassSettings.attackTiming()) {
            return CombatCompatibilityRules.attackDelayTicks(CombatSettings.cpsLimit(),
                    BypassSettings.profile(), BypassSettings.attackJitterTicks(),
                    ThreadLocalRandom.current().nextDouble());
        }
        return CombatActionRules.attackDelayTicks(CombatSettings.cpsLimit());
    }

    private void updateCompatibilityRotationOffsets() {
        if (!BypassSettings.rotationRandomization()) {
            compatibilityYawOffset = 0.0f;
            compatibilityPitchOffset = 0.0f;
            return;
        }
        if (compatibilityRotationTicks++ % 3 != 0) {
            return;
        }
        compatibilityYawOffset = (float) ThreadLocalRandom.current().nextDouble(-1.0D, 1.0D);
        compatibilityPitchOffset = (float) ThreadLocalRandom.current().nextDouble(-1.0D, 1.0D);
    }

    private static double normalizedSample(float centeredSample) {
        return Math.max(0.0D, Math.min(1.0D, centeredSample * 0.5D + 0.5D));
    }

    private void resetCompatibilityState() {
        compatibilityYawOffset = 0.0f;
        compatibilityPitchOffset = 0.0f;
        compatibilityRotationTicks = 0;
        lastAttackTargetId = -1;
        lastAttackNanos = 0L;
    }

    private float requiredAttackStrength(LocalPlayer player, boolean sweepAttack) {
        return CombatActionRules.requiredAttackStrength(
                sweepAttack,
                CombatSettings.behaviorMode(),
                randomNextSmash
        );
    }

    private static boolean shouldSweepAttack(LocalPlayer player, List<LivingEntity> targets) {
        return CombatActionRules.shouldSweepAttack(
                targets.size(),
                player.isShiftKeyDown(),
                player.getMainHandItem().getItem() instanceof SwordItem
        );
    }

    private void chooseNextRandomBehavior() {
        if (CombatSettings.BEHAVIOR_RANDOM.equals(CombatSettings.behaviorMode())) {
            randomNextSmash = ThreadLocalRandom.current().nextBoolean();
        } else {
            randomNextSmash = false;
        }
    }

    private static float rotationJitter() {
        return CombatActionRules.rotationJitter(CombatSettings.randomization());
    }
}
