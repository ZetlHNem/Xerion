package com.xerion.forge.combat;

import com.xerion.common.config.CombatSettings;
import com.xerion.common.movement.ScaffoldRotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.UUID;

public final class TaczRagebotHooks {
    private static final long DOUBLE_TAP_BYPASS_TIMEOUT_MS = 1500L;
    private static final long SERVER_SHOT_AIM_TIMEOUT_MS = 1500L;
    private static final int MAX_PENDING_SERVER_SHOT_AIMS = 8;
    private static final ArrayDeque<ServerShotAim> pendingServerShotAims = new ArrayDeque<>();
    private static UUID activeShooterId;
    private static boolean antiAimActive;
    private static boolean aimRotationAvailable;
    private static float aimYaw;
    private static float aimPitch;
    private static boolean antiAimFakeRotationAvailable;
    private static float antiAimFakeYaw;
    private static float antiAimFakePitch;
    private static boolean antiAimRestorePending;
    private static long antiAimRestoreAtMs;
    private static UUID pendingDoubleTapShooterId;
    private static long pendingDoubleTapTimestamp;
    private static long pendingDoubleTapExpiresAtMs;

    private TaczRagebotHooks() {
    }

    static void setActiveShooter(LivingEntity shooter) {
        activeShooterId = shooter == null ? null : shooter.getUUID();
    }

    static void clearActiveShooter() {
        activeShooterId = null;
    }

    static void setAntiAimActive(boolean active) {
        antiAimActive = active;
        if (!active) {
            aimRotationAvailable = false;
            antiAimFakeRotationAvailable = false;
            antiAimRestorePending = false;
            antiAimRestoreAtMs = 0L;
        }
    }

    static void setAntiAimFakeRotation(float yaw, float pitch) {
        antiAimFakeYaw = yaw;
        antiAimFakePitch = Math.max(-90.0f, Math.min(90.0f, pitch));
        antiAimFakeRotationAvailable = true;
    }

    static void setAimRotation(float yaw, float pitch) {
        aimYaw = yaw;
        aimPitch = Math.max(-90.0f, Math.min(90.0f, pitch));
        aimRotationAvailable = true;
    }

    static void clearAimRotation() {
        aimRotationAvailable = false;
    }

    static boolean antiAimActive() {
        return antiAimActive;
    }

    static void prepareAntiAimShot(LocalPlayer player) {
        if (!antiAimActive || player == null || player.connection == null) {
            return;
        }
        float shotYaw = aimRotationAvailable ? aimYaw : player.getYRot();
        float shotPitch = aimRotationAvailable ? aimPitch : player.getXRot();
        registerServerShotAim(player, shotYaw, shotPitch);
        if (!CombatSettings.ragebotHideShots()) {
            ScaffoldRotation.requestRender(shotYaw, shotPitch, true);
        }
        antiAimRestorePending = true;
        antiAimRestoreAtMs = System.currentTimeMillis() + 25L;
        player.connection.send(new ServerboundMovePlayerPacket.Rot(
                shotYaw, shotPitch, player.onGround()));
    }

    static void registerAntiAimDoubleTapShot(LocalPlayer player) {
        if (!antiAimActive || player == null) {
            return;
        }
        float shotYaw = aimRotationAvailable ? aimYaw : player.getYRot();
        float shotPitch = aimRotationAvailable ? aimPitch : player.getXRot();
        registerServerShotAim(player, shotYaw, shotPitch);
    }

    private static synchronized void registerServerShotAim(LivingEntity shooter, float yaw, float pitch) {
        long now = System.currentTimeMillis();
        removeExpiredServerShotAims(now);
        while (pendingServerShotAims.size() >= MAX_PENDING_SERVER_SHOT_AIMS) {
            pendingServerShotAims.removeFirst();
        }
        pendingServerShotAims.addLast(new ServerShotAim(
                shooter.getUUID(), yaw, Math.max(-90.0f, Math.min(90.0f, pitch)),
                now + SERVER_SHOT_AIM_TIMEOUT_MS));
    }

    public static synchronized ServerShotAim beginServerShotAim(LivingEntity shooter) {
        long now = System.currentTimeMillis();
        removeExpiredServerShotAims(now);
        if (shooter == null || shooter.level().isClientSide) {
            return null;
        }
        Iterator<ServerShotAim> iterator = pendingServerShotAims.iterator();
        while (iterator.hasNext()) {
            ServerShotAim shotAim = iterator.next();
            if (shotAim.shooterId().equals(shooter.getUUID())) {
                iterator.remove();
                return shotAim;
            }
        }
        return null;
    }

    private static void removeExpiredServerShotAims(long now) {
        pendingServerShotAims.removeIf(shotAim -> now > shotAim.expiresAtMs());
    }

    static void finishAntiAimShot(LocalPlayer player) {
        if (!antiAimActive || !CombatSettings.ragebotHideShots()
                || !antiAimFakeRotationAvailable || player == null || player.connection == null) {
            return;
        }
        sendAntiAimRestore(player);
    }

    static void flushDelayedAntiAimRestore(LocalPlayer player) {
        if (!antiAimRestorePending || System.currentTimeMillis() < antiAimRestoreAtMs
                || !antiAimActive || !antiAimFakeRotationAvailable
                || player == null || player.connection == null) {
            return;
        }
        sendAntiAimRestore(player);
    }

    private static void sendAntiAimRestore(LocalPlayer player) {
        ScaffoldRotation.requestRender(antiAimFakeYaw, antiAimFakePitch, true);
        player.connection.send(new ServerboundMovePlayerPacket.Rot(
                antiAimFakeYaw, antiAimFakePitch, player.onGround()));
        antiAimRestorePending = false;
        antiAimRestoreAtMs = 0L;
    }

    static synchronized void registerDoubleTapCooldownBypass(LivingEntity shooter, long timestamp) {
        if (shooter == null) {
            return;
        }
        pendingDoubleTapShooterId = shooter.getUUID();
        pendingDoubleTapTimestamp = timestamp;
        pendingDoubleTapExpiresAtMs = System.currentTimeMillis() + DOUBLE_TAP_BYPASS_TIMEOUT_MS;
    }

    public static synchronized boolean consumeDoubleTapCooldownBypass(LivingEntity shooter, long timestamp) {
        boolean matches = shooter != null
                && !shooter.level().isClientSide
                && pendingDoubleTapShooterId != null
                && pendingDoubleTapShooterId.equals(shooter.getUUID())
                && Math.abs(pendingDoubleTapTimestamp - timestamp) <= 2L
                && System.currentTimeMillis() <= pendingDoubleTapExpiresAtMs;
        if (matches || System.currentTimeMillis() > pendingDoubleTapExpiresAtMs) {
            pendingDoubleTapShooterId = null;
            pendingDoubleTapTimestamp = 0L;
            pendingDoubleTapExpiresAtMs = 0L;
        }
        return matches;
    }

    public static boolean doubleTapActive() {
        return TaczGunBridge.doubleTapActive();
    }

    public static boolean doubleTapCharged() {
        return TaczGunBridge.doubleTapCharged();
    }

    public static float doubleTapChargeProgress() {
        return TaczGunBridge.doubleTapChargeProgress();
    }

    public static boolean removeRecoilActive() {
        return CombatSettings.ragebotEnabled()
                && CombatSettings.ragebotRemoveRecoil()
                && Minecraft.getInstance().player != null;
    }

    public static boolean removeRecoilActive(LivingEntity shooter) {
        return CombatSettings.ragebotEnabled()
                && CombatSettings.ragebotRemoveRecoil()
                && isLocalShooter(shooter);
    }

    public static boolean removeSpreadActive(LivingEntity shooter) {
        return CombatSettings.ragebotEnabled()
                && CombatSettings.ragebotRemoveSpread()
                && isLocalShooter(shooter);
    }

    private static boolean isLocalShooter(LivingEntity shooter) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || shooter == null) {
            return false;
        }
        return player.equals(shooter) || player.getUUID().equals(shooter.getUUID())
                || activeShooterId != null && activeShooterId.equals(shooter.getUUID());
    }

    public record ServerShotAim(UUID shooterId, float yaw, float pitch, long expiresAtMs) {
    }
}
