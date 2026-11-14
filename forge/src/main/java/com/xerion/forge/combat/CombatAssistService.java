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

import com.xerion.common.combat.CombatActionRules;
import com.xerion.common.combat.CombatAimRules;
import com.xerion.common.combat.CombatCompatibilityRules;
import com.xerion.common.combat.RagebotAntiAimRules;
import com.xerion.common.config.BypassSettings;
import com.xerion.common.config.CombatSettings;
import com.xerion.common.movement.ScaffoldRotation;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class CombatAssistService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final double AIM_ASSIST_MAX_DISTANCE = 8.0;
    private static final double PROJECTILE_AIM_MAX_DISTANCE = 80.0;
    private static final double RAGEBOT_MAX_DISTANCE = 48.0;
    private static final float RAGEBOT_REFINE_SHOT_DEGREES = 1.2f;
    private int attackCooldownTicks;
    private int velocityAppliedHurtTime;
    private int aimTargetId = -1;
    private long lastAimNanos;
    private boolean aimInitialized;
    private boolean aimHasTarget;
    private float aimYaw;
    private float aimPitch;
    private float aimTargetYaw;
    private float aimTargetPitch;
    private int projectileTargetId = -1;
    private long lastProjectileNanos;
    private boolean projectileInitialized;
    private boolean projectileHasTarget;
    private float projectileYaw;
    private float projectilePitch;
    private float projectileTargetYaw;
    private float projectileTargetPitch;
    private int gunTargetId = -1;
    private long lastGunNanos;
    private boolean gunInitialized;
    private boolean gunHasTarget;
    private float gunYaw;
    private float gunPitch;
    private float gunTargetYaw;
    private float gunTargetPitch;
    private int ragebotFireCooldownTicks;
    private boolean ragebotForcedUseKey;
    private boolean ragebotForcedAttackKey;
    private boolean ragebotForcedTaczAim;
    private String ragebotNoAmmoStackKey = "";
    private boolean ragebotAntiAimApplied;
    private int ragebotAntiAimTicks;
    private float compatibilityYawSample;
    private float compatibilityPitchSample;
    private int compatibilityRotationTicks;
    private int lastAttackTargetId = -1;
    private long lastAttackNanos;

    public void tick() {
        if (attackCooldownTicks > 0) {
            attackCooldownTicks--;
        }

        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (player == null || level == null || MC.gameMode == null || MC.screen != null || player.isSpectator()) {
            TaczGunBridge.resetDoubleTapCharge();
            TaczGunBridge.setAutoStopSprint(player, false);
            resetRagebotAntiAim();
            return;
        }
        if (BypassProtocolService.correctionRecoveryActive()) {
            TaczGunBridge.resetDoubleTapCharge();
            TaczGunBridge.setAutoStopSprint(player, false);
            resetRagebotAntiAim();
            resetGunAim();
            resetProjectileAim();
            resetAimAssist();
            return;
        }

        updateRagebotAntiAim(player, level);

        if (CombatSettings.killAuraEnabled()) {
            TaczGunBridge.resetDoubleTapCharge();
            TaczGunBridge.setAutoStopSprint(player, false);
            return;
        }

        boolean doubleTapEnabled = CombatSettings.ragebotEnabled()
                && CombatSettings.ragebotDoubleTap() && holdingGun(player);
        boolean doubleTapFullAuto = TaczGunBridge.isFullAuto(player);
        boolean doubleTapWasFiring = doubleTapFullAuto && (MC.options.keyAttack.isDown()
                || CombatSettings.ragebotAutomaticFire() && gunHasTarget);
        TaczGunBridge.updateDoubleTapCharge(player, doubleTapEnabled, doubleTapWasFiring);

        boolean gunActive = false;
        if (CombatSettings.ragebotEnabled()) {
            gunActive = updateGunAimTarget(player, level);
        } else {
            resetGunAim();
        }
        boolean doubleTapFiring = doubleTapFullAuto && (MC.options.keyAttack.isDown()
                || CombatSettings.ragebotAutomaticFire() && gunActive);
        TaczGunBridge.updateDoubleTapCharge(player, doubleTapEnabled, doubleTapFiring);

        boolean projectileActive = false;
        if (!gunActive && CombatSettings.projectileAimEnabled()) {
            projectileActive = updateProjectileAimTarget(player, level);
        } else {
            resetProjectileAim();
        }

        if (!gunActive && !projectileActive && CombatSettings.aimAssistEnabled()) {
            updateAimAssistTarget(player, level);
        } else {
            resetAimAssist();
        }
        if (attackCooldownTicks <= 0 && CombatSettings.triggerBotEnabled()) {
            triggerBotAttack(player);
        }
        if (attackCooldownTicks <= 0 && CombatSettings.autoClickerEnabled() && MC.options.keyAttack.isDown()) {
            crosshairAttack(player);
        }
    }

    public void tickEnd() {
        LocalPlayer player = MC.player;
        if (player == null || !CombatSettings.velocityEnabled()) {
            velocityAppliedHurtTime = 0;
            VelocityPacketState.clear();
            return;
        }
        if (player.hurtTime <= 0) {
            velocityAppliedHurtTime = 0;
            return;
        }
        if (velocityAppliedHurtTime == player.hurtTime) {
            return;
        }

        boolean newHit = velocityAppliedHurtTime <= 0 || player.hurtTime > velocityAppliedHurtTime;

        if (CombatSettings.VELOCITY_MODE_JUMP_RESET.equals(CombatSettings.velocityMode())) {
            if (newHit && player.onGround()) {
                player.jumpFromGround();
            }
            velocityAppliedHurtTime = player.hurtTime;
            return;
        }
        if (!CombatSettings.VELOCITY_MODE_SCALE.equals(CombatSettings.velocityMode())) {
            velocityAppliedHurtTime = player.hurtTime;
            return;
        }
        if (!newHit) {
            velocityAppliedHurtTime = player.hurtTime;
            return;
        }
        if (VelocityPacketState.consumeHandled(player.tickCount)) {
            velocityAppliedHurtTime = player.hurtTime;
            return;
        }

        Vec3 velocity = player.getDeltaMovement();
        double horizontal = CombatActionRules.velocityMultiplier(CombatSettings.velocityHorizontal());
        double vertical = CombatActionRules.velocityMultiplier(CombatSettings.velocityVertical());
        player.setDeltaMovement(velocity.x * horizontal, velocity.y * vertical, velocity.z * horizontal);
        player.hurtMarked = true;
        velocityAppliedHurtTime = player.hurtTime;
    }

    public void renderTick(float partialTick) {
        LocalPlayer player = MC.player;
        if (player == null || MC.level == null || MC.screen != null || player.isSpectator()
                || CombatSettings.killAuraEnabled()) {
            resetProjectileAim();
            resetAimAssist();
            resetGunAim();
            return;
        }

        if (CombatSettings.ragebotEnabled() && gunHasTarget && holdingGun(player)) {
            renderGunAim(player);
            resetProjectileAim();
            resetAimAssist();
            return;
        }
        resetGunAim();

        if (CombatSettings.projectileAimEnabled() && projectileHasTarget && CombatAimMath.projectileInfo(player) != null) {
            renderProjectileAim(player);
            return;
        }
        resetProjectileAim();

        if (!CombatSettings.aimAssistEnabled() || !aimHasTarget) {
            resetAimAssist();
            return;
        }
        renderAimAssist(player);
    }

    private void renderAimAssist(LocalPlayer player) {
        if (!aimInitialized) {
            aimYaw = player.getYRot();
            aimPitch = player.getXRot();
            aimInitialized = true;
            lastAimNanos = System.nanoTime();
        }

        long now = System.nanoTime();
        float dt = CombatActionRules.frameDeltaSeconds(now, lastAimNanos, 0.05f);
        lastAimNanos = now;
        if (dt <= 0.0f) {
            return;
        }

        float currentYaw = player.getYRot();
        float currentPitch = player.getXRot();
        float targetYaw = CombatAimMath.strengthTargetYaw(currentYaw, aimTargetYaw);
        float targetPitch = CombatAimMath.strengthTargetPitch(currentPitch, aimTargetPitch);

        if (CombatActionRules.immediateAim(CombatSettings.aimAssistSmooth())) {
            aimYaw = targetYaw;
            aimPitch = CombatAimRules.clamp(targetPitch, -90.0f, 90.0f);
            player.setYRot(aimYaw);
            player.setXRot(aimPitch);
            return;
        }

        float alpha = CombatAimMath.aimAssistLerpAlpha(dt);
        float maxStep = CombatAimMath.aimAssistMaxStep(dt);
        aimYaw = CombatAimMath.smoothYawStep(currentYaw, targetYaw, alpha, maxStep);
        aimPitch = CombatAimMath.smoothStep(currentPitch, targetPitch, alpha * 0.82f, maxStep * 0.72f);
        aimPitch = CombatAimRules.clamp(aimPitch, -90.0f, 90.0f);
        player.setYRot(aimYaw);
        player.setXRot(aimPitch);
    }

    private void renderProjectileAim(LocalPlayer player) {
        if (!projectileInitialized) {
            projectileYaw = player.getYRot();
            projectilePitch = player.getXRot();
            projectileInitialized = true;
            lastProjectileNanos = System.nanoTime();
        }

        long now = System.nanoTime();
        float dt = CombatActionRules.frameDeltaSeconds(now, lastProjectileNanos, 0.05f);
        lastProjectileNanos = now;
        if (dt <= 0.0f) {
            return;
        }

        if (CombatActionRules.immediateAim(CombatSettings.projectileAimSmooth())) {
            projectileYaw = projectileTargetYaw;
            projectilePitch = CombatAimRules.clamp(projectileTargetPitch, -90.0f, 90.0f);
            player.setYRot(projectileYaw);
            player.setXRot(projectilePitch);
            return;
        }

        float currentYaw = player.getYRot();
        float currentPitch = player.getXRot();
        float alpha = CombatAimMath.projectileAimLerpAlpha(dt);
        float maxStep = CombatAimMath.projectileAimMaxStep(dt);
        projectileYaw = CombatAimMath.smoothYawStep(currentYaw, projectileTargetYaw, alpha, maxStep);
        projectilePitch = CombatAimMath.smoothStep(currentPitch, projectileTargetPitch, alpha * 0.82f, maxStep * 0.72f);
        projectilePitch = CombatAimRules.clamp(projectilePitch, -90.0f, 90.0f);
        player.setYRot(projectileYaw);
        player.setXRot(projectilePitch);
    }

    private void renderGunAim(LocalPlayer player) {
        if (!gunInitialized) {
            gunYaw = player.getYRot();
            gunPitch = player.getXRot();
            gunInitialized = true;
            lastGunNanos = System.nanoTime();
        }

        long now = System.nanoTime();
        float dt = CombatActionRules.frameDeltaSeconds(now, lastGunNanos, 0.05f);
        lastGunNanos = now;
        if (dt <= 0.0f) {
            return;
        }

        if (CombatActionRules.immediateAim(CombatSettings.ragebotSmooth())) {
            gunYaw = gunTargetYaw;
            gunPitch = CombatAimRules.clamp(gunTargetPitch, -90.0f, 90.0f);
            applyGunRotation(player, gunYaw, gunPitch);
            return;
        }

        float currentYaw = player.getYRot();
        float currentPitch = player.getXRot();
        float alpha = CombatAimMath.gunAimLerpAlpha(dt);
        float maxStep = CombatAimMath.gunAimMaxStep(dt);
        gunYaw = CombatAimMath.smoothYawStep(currentYaw, gunTargetYaw, alpha, maxStep);
        gunPitch = CombatAimMath.smoothStep(currentPitch, gunTargetPitch, alpha * 0.86f, maxStep * 0.78f);
        gunPitch = CombatAimRules.clamp(gunPitch, -90.0f, 90.0f);
        applyGunRotation(player, gunYaw, gunPitch);
    }

    private void applyGunRotation(LocalPlayer player, float yaw, float pitch) {
        if (CombatSettings.ragebotSilentAim()) {
            if (!TaczRagebotHooks.antiAimActive()) {
                ScaffoldRotation.requestPacketOnly(yaw, pitch, true);
            }
            return;
        }
        player.setYRot(yaw);
        player.setXRot(pitch);
    }

    private void triggerBotAttack(LocalPlayer player) {
        if (CombatSettings.triggerBotRequireAttackKey() && !MC.options.keyAttack.isDown()) {
            return;
        }
        if (CombatSettings.triggerBotOnlyWeapon() && !triggerBotWeapon(player)) {
            return;
        }
        if (!(MC.hitResult instanceof EntityHitResult entityHit) || !(entityHit.getEntity() instanceof LivingEntity target)) {
            return;
        }
        if (!CombatTargetSelector.validTarget(player, target,
                CombatActionRules.minimumReach(CombatSettings.triggerBotRange(), 3.0f))) {
            return;
        }
        attack(player, target, CombatActionRules.minimumCps(CombatSettings.triggerBotCps()));
    }

    private static boolean triggerBotWeapon(LocalPlayer player) {
        return player.getMainHandItem().getItem() instanceof SwordItem
                || player.getMainHandItem().getItem() instanceof AxeItem;
    }

    private void crosshairAttack(LocalPlayer player) {
        if (!(MC.hitResult instanceof EntityHitResult entityHit) || !(entityHit.getEntity() instanceof LivingEntity target)) {
            return;
        }
        if (MC.hitResult.getType() != HitResult.Type.ENTITY || !CombatTargetSelector.validTarget(player, target, CombatActionRules.minimumReach(CombatSettings.reach(), 3.0f))) {
            return;
        }
        attack(player, target, CombatActionRules.minimumCps(CombatSettings.autoClickerCps()));
    }

    private void attack(LocalPlayer player, LivingEntity target, float cps) {
        if (!CombatSettings.attackDelayEnabled() && player.getAttackStrengthScale(0.0f) < 0.86f) {
            return;
        }
        if (!canCriticalAttack(player, target, CombatActionRules.minimumReach(CombatSettings.reach(), 3.0f))) {
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
        KnockbackService.beforeAttack(player);
        CriticalsService.beforeAttack(player, target);
        MC.gameMode.attack(player, target);
        FakePlayerRegistry.hurtIfDummy(target);
        HitSoundService.play(target);
        player.swing(InteractionHand.MAIN_HAND);
        KnockbackService.afterAttack(player);
        AutoWeaponService.afterAttack(player, weaponSlot);
        ShieldBreakerService.afterAttack(player, shieldSlot);
        lastAttackTargetId = target.getId();
        lastAttackNanos = now;
        attackCooldownTicks = CombatSettings.attackDelayEnabled()
                ? Math.min(compatibleAttackDelay(cps), CombatSettings.attackDelayMaxCooldown())
                : compatibleAttackDelay(cps);
    }

    private void updateAimAssistTarget(LocalPlayer player, ClientLevel level) {
        float aimRadius = CombatSettings.aimAssistRange();
        List<LivingEntity> targets = CombatTargetSelector.aimAssistTargets(player, level, AIM_ASSIST_MAX_DISTANCE, aimRadius);
        if (targets.isEmpty()) {
            resetAimAssist();
            return;
        }

        LivingEntity target = CombatTargetSelector.selectStableAimTarget(player, targets, aimTargetId);
        CombatAimMath.Rotation rotation = CombatAimMath.rotationTo(player, target);
        if (target.getId() != aimTargetId || !aimInitialized) {
            aimTargetId = target.getId();
            aimYaw = player.getYRot();
            aimPitch = player.getXRot();
            aimInitialized = true;
            lastAimNanos = System.nanoTime();
        }
        CombatCompatibilityRules.Rotation filtered = compatibleRotation(player, rotation.yaw(), rotation.pitch());
        aimTargetYaw = filtered.yaw();
        aimTargetPitch = filtered.pitch();
        aimHasTarget = true;
    }

    private boolean updateProjectileAimTarget(LocalPlayer player, ClientLevel level) {
        CombatAimMath.ProjectileInfo info = CombatAimMath.projectileInfo(player);
        if (info == null) {
            resetProjectileAim();
            return false;
        }

        float aimRadius = CombatSettings.projectileAimRange();
        List<LivingEntity> targets = CombatTargetSelector.projectileAimTargets(player, level, PROJECTILE_AIM_MAX_DISTANCE, aimRadius);
        if (targets.isEmpty()) {
            resetProjectileAim();
            return false;
        }

        LivingEntity target = CombatTargetSelector.selectStableProjectileTarget(player, targets, projectileTargetId);
        CombatAimMath.Rotation rotation = CombatAimMath.projectileRotationTo(player, target, info);
        if (rotation == null) {
            resetProjectileAim();
            return false;
        }

        if (target.getId() != projectileTargetId || !projectileInitialized) {
            projectileTargetId = target.getId();
            projectileYaw = player.getYRot();
            projectilePitch = player.getXRot();
            projectileInitialized = true;
            lastProjectileNanos = System.nanoTime();
        }
        CombatCompatibilityRules.Rotation filtered = compatibleRotation(player, rotation.yaw(), rotation.pitch());
        projectileTargetYaw = filtered.yaw();
        projectileTargetPitch = filtered.pitch();
        projectileHasTarget = true;
        return true;
    }

    private boolean updateGunAimTarget(LocalPlayer player, ClientLevel level) {
        if (ragebotFireCooldownTicks > 0) {
            ragebotFireCooldownTicks--;
        }
        if (!holdingGun(player)) {
            resetGunAim();
            return false;
        }
        TaczRagebotHooks.setActiveShooter(player);

        float aimRadius = CombatSettings.ragebotFov();
        List<LivingEntity> targets = CombatTargetSelector.gunAimTargets(player, level,
                RAGEBOT_MAX_DISTANCE, aimRadius);
        if (targets.isEmpty()) {
            resetGunAim();
            return false;
        }

        LivingEntity target = CombatTargetSelector.selectStableGunTarget(player, targets, gunTargetId);
        CombatAimMath.Rotation rotation = CombatAimMath.gunRotationTo(player, target);
        if (rotation == null) {
            resetGunAim();
            return false;
        }

        if (target.getId() != gunTargetId || !gunInitialized) {
            gunTargetId = target.getId();
            gunYaw = player.getYRot();
            gunPitch = player.getXRot();
            gunInitialized = true;
            lastGunNanos = System.nanoTime();
        }
        CombatCompatibilityRules.Rotation filtered = compatibleRotation(player, rotation.yaw(), rotation.pitch());
        gunTargetYaw = filtered.yaw();
        gunTargetPitch = filtered.pitch();
        gunHasTarget = true;
        TaczRagebotHooks.setAimRotation(gunTargetYaw,
                CombatAimRules.clamp(gunTargetPitch, -90.0f, 90.0f));
        if (CombatSettings.ragebotSilentAim() && CombatSettings.ragebotForceSilentAim()
                && !TaczRagebotHooks.antiAimActive()) {
            ScaffoldRotation.requestPacketOnly(gunTargetYaw, CombatAimRules.clamp(gunTargetPitch, -90.0f, 90.0f), true);
        }
        applyRagebotActions(player, target);
        return true;
    }

    private void applyRagebotActions(LocalPlayer player, LivingEntity target) {
        TaczGunBridge.setAutoStopSprint(player, CombatSettings.ragebotAutoStop());
        boolean automaticFire = CombatSettings.ragebotAutomaticFire();
        if (CombatSettings.ragebotQuickScope()) {
            if (TaczGunBridge.setAiming(player, true)) {
                ragebotForcedTaczAim = true;
            }
            MC.options.keyUse.setDown(true);
            ragebotForcedUseKey = true;
        } else if (ragebotForcedTaczAim) {
            TaczGunBridge.setAiming(player, false);
            ragebotForcedTaczAim = false;
        }
        if (!automaticFire) {
            releaseRagebotAttackKey();
            if (!CombatSettings.ragebotQuickScope()) {
                releaseRagebotUseKey();
            }
            return;
        }
        if (ragebotFireCooldownTicks > 0 || MC.gameMode == null) {
            return;
        }
        if (CombatSettings.ragebotQuickScope() && !TaczGunBridge.aimingReady(player)) {
            releaseRagebotAttackKey();
            return;
        }
        InteractionHand hand = gunHand(player);
        if (hand == null) {
            return;
        }
        if (ragebotNoAmmoActive(player)) {
            releaseRagebotAttackKey();
            if (!CombatSettings.ragebotQuickScope()) {
                releaseRagebotUseKey();
            }
            return;
        }
        TaczGunBridge.ReadyState taczReady = TaczGunBridge.readyState(player);
        if (taczReady == TaczGunBridge.ReadyState.WAIT) {
            releaseRagebotAttackKey();
            if (!CombatSettings.ragebotQuickScope()) {
                releaseRagebotUseKey();
            }
            return;
        }
        if (taczReady == TaczGunBridge.ReadyState.WAIT_COOLDOWN) {
            releaseRagebotAttackKey();
            if (!CombatSettings.ragebotQuickScope()) {
                releaseRagebotUseKey();
            }
            return;
        }
        requestRagebotShotRotation();
        if (CombatSettings.ragebotRefineShot() && !ragebotShotAligned(RAGEBOT_REFINE_SHOT_DEGREES)) {
            return;
        }
        TaczGunBridge.ShootAttempt taczShoot = tryRagebotTaczShoot(player);
        if (taczShoot == TaczGunBridge.ShootAttempt.SUCCESS) {
            ragebotNoAmmoStackKey = "";
            markRagebotShot(0);
            return;
        }
        if (taczShoot == TaczGunBridge.ShootAttempt.NO_AMMO) {
            ragebotNoAmmoStackKey = ragebotGunStackKey(player);
            releaseRagebotAttackKey();
            if (!CombatSettings.ragebotQuickScope()) {
                releaseRagebotUseKey();
            }
            return;
        }
        if (taczShoot == TaczGunBridge.ShootAttempt.HANDLED) {
            return;
        }
        MC.options.keyUse.setDown(true);
        ragebotForcedUseKey = true;
        MC.options.keyAttack.setDown(true);
        ragebotForcedAttackKey = true;
        KeyMapping.click(MC.options.keyAttack.getKey());
        KeyMapping.click(MC.options.keyUse.getKey());
        markRagebotShot(CombatSettings.ragebotDoubleTap() ? 1 : 3);
    }

    private void markRagebotShot(int cooldownTicks) {
        ragebotFireCooldownTicks = Math.max(ragebotFireCooldownTicks, cooldownTicks);
    }

    private void requestRagebotShotRotation() {
        if (!CombatSettings.ragebotSilentAim() || TaczRagebotHooks.antiAimActive()) {
            return;
        }
        ScaffoldRotation.requestPacketOnly(ragebotShotYaw(), ragebotShotPitch(), true);
    }

    private void updateRagebotAntiAim(LocalPlayer player, ClientLevel level) {
        String pitchMode = CombatSettings.ragebotAntiAimPitch();
        String yawMode = CombatSettings.ragebotAntiAimYaw();
        boolean active = CombatSettings.ragebotAntiAimEnabled()
                && holdingGun(player)
                && RagebotAntiAimRules.configured(pitchMode, yawMode);
        if (!active) {
            resetRagebotAntiAim();
            return;
        }

        if (!ragebotAntiAimApplied) {
            ragebotAntiAimTicks = 0;
        }
        int antiAimStep = ragebotAntiAimTicks++;
        boolean jitterHigh = (antiAimStep & 1) != 0;
        float targetYaw = CombatSettings.RAGEBOT_YAW_AT_TARGETS.equals(yawMode)
                ? antiAimTargetYaw(player, level)
                : player.getYRot();
        float fakeYaw = RagebotAntiAimRules.yaw(yawMode, player.getYRot(), targetYaw,
                CombatSettings.ragebotAntiAimYawBase(),
                CombatSettings.ragebotAntiAimYawModifier(),
                CombatSettings.ragebotAntiAimYawOffset(), antiAimStep);
        float fakePitch = RagebotAntiAimRules.pitch(pitchMode, player.getXRot(),
                CombatSettings.ragebotAntiAimCustomPitch(), jitterHigh);

        TaczRagebotHooks.setAntiAimActive(true);
        TaczRagebotHooks.setAntiAimFakeRotation(fakeYaw, fakePitch);
        TaczRagebotHooks.flushDelayedAntiAimRestore(player);
        ScaffoldRotation.request(fakeYaw, fakePitch, RagebotAntiAimRules.yawOverride(yawMode));
        ragebotAntiAimApplied = true;
    }

    private static float antiAimTargetYaw(LocalPlayer player, ClientLevel level) {
        LivingEntity target = CombatTargetSelector.closestAntiAimTarget(
                player, level, RAGEBOT_MAX_DISTANCE);
        if (target == null) {
            return player.getYRot();
        }
        Vec3 delta = target.getBoundingBox().getCenter().subtract(player.getEyePosition());
        return (float) Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0f;
    }

    private void resetRagebotAntiAim() {
        TaczRagebotHooks.setAntiAimActive(false);
        if (ragebotAntiAimApplied) {
            ScaffoldRotation.clearRender();
        }
        ragebotAntiAimApplied = false;
        ragebotAntiAimTicks = 0;
    }

    private TaczGunBridge.ShootAttempt tryRagebotTaczShoot(LocalPlayer player) {
        if (!CombatSettings.ragebotSilentAim() || !CombatSettings.ragebotForceSilentAim()) {
            return TaczGunBridge.tryShoot(player);
        }
        float previousYaw = player.getYRot();
        float previousPitch = player.getXRot();
        player.setYRot(gunTargetYaw);
        player.setXRot(CombatAimRules.clamp(gunTargetPitch, -90.0f, 90.0f));
        try {
            return TaczGunBridge.tryShoot(player);
        } finally {
            player.setYRot(previousYaw);
            player.setXRot(previousPitch);
        }
    }

    private boolean ragebotShotAligned(float maxDegrees) {
        return CombatAimRules.aimCircleDistance(ragebotShotYaw(), ragebotShotPitch(),
                gunTargetYaw, CombatAimRules.clamp(gunTargetPitch, -90.0f, 90.0f)) <= maxDegrees;
    }

    private float ragebotShotYaw() {
        return CombatSettings.ragebotForceSilentAim() ? gunTargetYaw : gunYaw;
    }

    private float ragebotShotPitch() {
        return CombatSettings.ragebotForceSilentAim()
                ? CombatAimRules.clamp(gunTargetPitch, -90.0f, 90.0f)
                : CombatAimRules.clamp(gunPitch, -90.0f, 90.0f);
    }

    private void resetAimAssist() {
        aimTargetId = -1;
        lastAimNanos = 0L;
        aimInitialized = false;
        aimHasTarget = false;
    }

    private void resetProjectileAim() {
        projectileTargetId = -1;
        lastProjectileNanos = 0L;
        projectileInitialized = false;
        projectileHasTarget = false;
    }

    private void resetGunAim() {
        gunTargetId = -1;
        lastGunNanos = 0L;
        gunInitialized = false;
        gunHasTarget = false;
        ragebotFireCooldownTicks = 0;
        ragebotNoAmmoStackKey = "";
        TaczRagebotHooks.clearActiveShooter();
        TaczRagebotHooks.clearAimRotation();
        TaczGunBridge.setAutoStopSprint(MC.player, false);
        releaseRagebotAttackKey();
        releaseRagebotUseKey();
        releaseRagebotTaczAim();
    }

    private int compatibleAttackDelay(float cps) {
        if (!BypassSettings.enabled() || !BypassSettings.attackTiming()) {
            return CombatActionRules.attackDelayTicks(cps);
        }
        return CombatCompatibilityRules.attackDelayTicks(cps, BypassSettings.profile(),
                BypassSettings.attackJitterTicks(), ThreadLocalRandom.current().nextDouble());
    }

    private CombatCompatibilityRules.Rotation compatibleRotation(LocalPlayer player, float yaw, float pitch) {
        if (!BypassSettings.enabled() || BypassProtocolService.correctionRecoveryActive()) {
            return new CombatCompatibilityRules.Rotation(yaw, pitch);
        }
        updateCompatibilityRotationSamples();
        float amount = BypassSettings.rotationRandomization()
                ? BypassSettings.rotationRandomizationAmount() : 0.0f;
        return CombatCompatibilityRules.filterRotation(player.getYRot(), player.getXRot(), yaw, pitch,
                MC.options.sensitivity().get(), amount,
                normalizedSample(compatibilityYawSample), normalizedSample(compatibilityPitchSample),
                BypassSettings.rotationQuantization());
    }

    private void updateCompatibilityRotationSamples() {
        if (!BypassSettings.rotationRandomization()) {
            compatibilityYawSample = 0.0f;
            compatibilityPitchSample = 0.0f;
            return;
        }
        if (compatibilityRotationTicks++ % 3 != 0) {
            return;
        }
        compatibilityYawSample = (float) ThreadLocalRandom.current().nextDouble(-1.0D, 1.0D);
        compatibilityPitchSample = (float) ThreadLocalRandom.current().nextDouble(-1.0D, 1.0D);
    }

    private static double normalizedSample(float centeredSample) {
        return Math.max(0.0D, Math.min(1.0D, centeredSample * 0.5D + 0.5D));
    }

    private boolean ragebotNoAmmoActive(LocalPlayer player) {
        if (ragebotNoAmmoStackKey.isEmpty()) {
            return false;
        }
        String currentKey = ragebotGunStackKey(player);
        if (currentKey.equals(ragebotNoAmmoStackKey)) {
            return true;
        }
        ragebotNoAmmoStackKey = "";
        return false;
    }

    private static String ragebotGunStackKey(LocalPlayer player) {
        ItemStack stack = isGunStack(player.getMainHandItem()) ? player.getMainHandItem() : player.getOffhandItem();
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        String tag = stack.getTag() == null ? "" : stack.getTag().toString();
        return id + "|" + tag;
    }

    private void releaseRagebotUseKey() {
        if (!ragebotForcedUseKey) {
            return;
        }
        MC.options.keyUse.setDown(false);
        ragebotForcedUseKey = false;
    }

    private void releaseRagebotAttackKey() {
        if (!ragebotForcedAttackKey) {
            return;
        }
        MC.options.keyAttack.setDown(false);
        ragebotForcedAttackKey = false;
    }

    private void releaseRagebotTaczAim() {
        if (!ragebotForcedTaczAim || MC.player == null) {
            ragebotForcedTaczAim = false;
            return;
        }
        TaczGunBridge.setAiming(MC.player, false);
        ragebotForcedTaczAim = false;
    }

    private static boolean holdingGun(LocalPlayer player) {
        return isGunStack(player.getMainHandItem()) || isGunStack(player.getOffhandItem());
    }

    private static InteractionHand gunHand(LocalPlayer player) {
        if (isGunStack(player.getMainHandItem())) {
            return InteractionHand.MAIN_HAND;
        }
        if (isGunStack(player.getOffhandItem())) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    private static boolean isGunStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().toLowerCase(Locale.ROOT);
        if (CombatSettings.ragebotItemIdEnabled(id) || CombatSettings.gunAimItemIdEnabled(id)) {
            return true;
        }
        if ("minecraft:gunpowder".equals(id)) {
            return false;
        }
        String namespace = id.contains(":") ? id.substring(0, id.indexOf(':')) : "";
        String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        String className = stack.getItem().getClass().getName().toLowerCase(Locale.ROOT);
        if (className.contains("gun") || className.contains("firearm")) {
            return true;
        }
        if (namespace.equals("cgm") || namespace.equals("tacz") || namespace.equals("pointblank")
                || namespace.equals("timeless") || namespace.equals("tac")) {
            return true;
        }
        return path.contains("gun")
                || path.contains("rifle")
                || path.contains("pistol")
                || path.contains("shotgun")
                || path.contains("sniper")
                || path.contains("revolver")
                || path.contains("carbine")
                || path.contains("firearm")
                || path.contains("smg")
                || path.contains("ak47")
                || path.contains("m4a1")
                || path.contains("glock")
                || path.contains("deagle");
    }

    public static boolean canCriticalAttack(LocalPlayer player, LivingEntity target, double range) {
        if (!CombatSettings.criticalsEnabled()) {
            return true;
        }
        if (target == null || player.isInWater() || player.onClimbable() || player.isPassenger()
                || player.isShiftKeyDown() || player.horizontalCollision) {
            return false;
        }
        if (player.fallDistance > 0.0f && !player.onGround()) {
            return CombatTargeting.distanceToBoxSqr(player, target) <= range * range;
        }
        if (player.onGround() && shouldPreJumpForCritical(player, target, range)) {
            player.jumpFromGround();
            player.hurtMarked = true;
        }
        return false;
    }

    private static boolean shouldPreJumpForCritical(LocalPlayer player, LivingEntity target, double range) {
        if (!target.isAlive() || target.isRemoved()) {
            return false;
        }

        Vec3 playerPos = player.position();
        Vec3 targetPos = target.position();
        Vec3 playerMotion = player.getDeltaMovement();
        Vec3 targetMotion = target.getDeltaMovement();
        double currentDistanceSq = CombatTargeting.distanceToBoxSqr(player, target);
        if (!CombatActionRules.criticalStartDistanceAllowed(currentDistanceSq, range)) {
            return false;
        }

        float yawDelta = CombatAimMath.yawDelta(player, target);
        if (!CombatActionRules.criticalYawAllowed(yawDelta)) {
            return false;
        }

        int ticksToFall = CombatActionRules.criticalFallPredictionTicks();
        Vec3 predictedPlayer = playerPos.add(playerMotion.x * ticksToFall, 0.0, playerMotion.z * ticksToFall);
        Vec3 predictedTarget = targetPos.add(targetMotion.x * ticksToFall, 0.0, targetMotion.z * ticksToFall);
        double predictedDistanceSq = predictedPlayer.distanceToSqr(predictedTarget);
        if (!CombatActionRules.criticalPredictedDistanceAllowed(predictedDistanceSq, range)) {
            return false;
        }

        double targetVerticalDelta = Math.abs(target.getBoundingBox().getCenter().y - player.getEyeY());
        return CombatActionRules.criticalVerticalAllowed(targetVerticalDelta);
    }

}
