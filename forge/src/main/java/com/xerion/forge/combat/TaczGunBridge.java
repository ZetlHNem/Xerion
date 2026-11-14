package com.xerion.forge.combat;

import com.xerion.forge.util.BypassProtocolService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

final class TaczGunBridge {
    private static final long DRAW_GRACE_MS = 20L;
    private static final long DOUBLE_TAP_CHARGE_MS = 500L;
    private static final double DOUBLE_TAP_SHIFT_DISTANCE = 1.25D;
    private static final double DOUBLE_TAP_SHIFT_MIN_SPEED_SQR = 1.0E-4D;
    private static boolean initialized;
    private static boolean available;
    private static Method fromLocalPlayer;
    private static Method chargeShoot;
    private static Method shoot;
    private static Method aim;
    private static Method isAim;
    private static Method getDataHolder;
    private static Method getClientAimingProgress;
    private static Method getClientShootCoolDown;
    private static Method getIGunOrNull;
    private static Method getFireMode;
    private static Method getGunDisplay;
    private static Method getAnimationStateMachine;
    private static Method triggerAnimation;
    private static Method sendToServer;
    private static Constructor<?> clientShootMessageConstructor;
    private static Object networkChannel;
    private static Field clientDrawTimestamp;
    private static Field clientBaseTimestamp;
    private static Field clientShootTimestamp;
    private static Field clientLastShootTimestamp;
    private static Field isShootRecorded;
    private static Field stopSprint;
    private static KeyMapping taczAimKey;
    private static boolean doubleTapCharging;
    private static boolean doubleTapCharged;
    private static long doubleTapReadyAtMs;
    private static long doubleTapChargeStartedAtMs;
    private static int doubleTapPlayerId = -1;
    private static boolean autoStopSprintActive;
    private static boolean resumeSprintAfterAutoStop;

    private TaczGunBridge() {
    }

    static ShootAttempt tryShoot(LocalPlayer player) {
        Object operator = operator(player);
        if (operator == null) {
            return ShootAttempt.UNAVAILABLE;
        }
        try {
            ReadyState readyState = readyState(operator);
            if (readyState != ReadyState.READY) {
                return readyState == ReadyState.UNAVAILABLE ? ShootAttempt.UNAVAILABLE : ShootAttempt.HANDLED;
            }
            Object chargeReady = chargeShoot.invoke(operator, true);
            if (chargeReady instanceof Boolean chargeReadyValue && !chargeReadyValue) {
                return ShootAttempt.HANDLED;
            }
            Object result = shoot.invoke(operator);
            if (result == null) {
                return ShootAttempt.HANDLED;
            }
            String resultName = result.toString();
            if ("SUCCESS".equals(resultName)) {
                return ShootAttempt.SUCCESS;
            }
            if ("NO_AMMO".equals(resultName)) {
                return ShootAttempt.NO_AMMO;
            }
            return ShootAttempt.HANDLED;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            available = false;
            return ShootAttempt.UNAVAILABLE;
        }
    }

    static ReadyState readyState(LocalPlayer player) {
        Object operator = operator(player);
        if (operator == null) {
            return ReadyState.UNAVAILABLE;
        }
        return readyState(operator);
    }

    static void updateDoubleTapCharge(LocalPlayer player, boolean enabled, boolean firing) {
        if (!enabled || player == null) {
            resetDoubleTapCharge();
            return;
        }
        long now = System.currentTimeMillis();
        if (!doubleTapCharging || doubleTapPlayerId != player.getId()) {
            doubleTapCharging = true;
            doubleTapCharged = false;
            doubleTapPlayerId = player.getId();
            doubleTapChargeStartedAtMs = firing ? 0L : now;
            doubleTapReadyAtMs = firing ? 0L : now + DOUBLE_TAP_CHARGE_MS;
            return;
        }
        ReadyState state = readyState(player);
        if (state == ReadyState.UNAVAILABLE || state == ReadyState.WAIT) {
            doubleTapCharged = false;
            doubleTapChargeStartedAtMs = firing ? 0L : now;
            doubleTapReadyAtMs = firing ? 0L : now + DOUBLE_TAP_CHARGE_MS;
            return;
        }
        if (!doubleTapCharged && doubleTapChargeStartedAtMs > 0L && now >= doubleTapReadyAtMs) {
            doubleTapCharged = true;
        }
        if (firing) {
            if (!doubleTapCharged) {
                doubleTapChargeStartedAtMs = 0L;
                doubleTapReadyAtMs = 0L;
            }
            return;
        }
        if (!doubleTapCharged && doubleTapChargeStartedAtMs == 0L) {
            doubleTapChargeStartedAtMs = now;
            doubleTapReadyAtMs = now + DOUBLE_TAP_CHARGE_MS;
        }
    }

    static void resetDoubleTapCharge() {
        doubleTapCharging = false;
        doubleTapCharged = false;
        doubleTapReadyAtMs = 0L;
        doubleTapChargeStartedAtMs = 0L;
        doubleTapPlayerId = -1;
    }

    static boolean doubleTapActive() {
        return doubleTapCharging;
    }

    static boolean isFullAuto(LocalPlayer player) {
        if (player == null || !init() || getFireMode == null) {
            return false;
        }
        ItemStack stack = gunStack(player);
        if (stack.isEmpty()) {
            return false;
        }
        try {
            Object gun = getIGunOrNull.invoke(null, stack);
            Object mode = gun == null ? null : getFireMode.invoke(gun, stack);
            return mode != null && "AUTO".equals(mode.toString());
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    static void finishPrimaryShot(LocalPlayer player, boolean doubleTapConsumed) {
        if (!doubleTapCharging || doubleTapConsumed || isFullAuto(player)) {
            return;
        }
        long now = System.currentTimeMillis();
        doubleTapCharged = false;
        doubleTapChargeStartedAtMs = now;
        doubleTapReadyAtMs = now + DOUBLE_TAP_CHARGE_MS;
    }

    static boolean doubleTapCharged() {
        return doubleTapCharging && doubleTapCharged;
    }

    static float doubleTapChargeProgress() {
        if (!doubleTapCharging) {
            return 0.0f;
        }
        if (doubleTapCharged) {
            return 1.0f;
        }
        if (doubleTapChargeStartedAtMs <= 0L || doubleTapReadyAtMs <= doubleTapChargeStartedAtMs) {
            return 0.0f;
        }
        long duration = Math.max(1L, doubleTapReadyAtMs - doubleTapChargeStartedAtMs);
        return Math.max(0.0f, Math.min(1.0f,
                (System.currentTimeMillis() - doubleTapChargeStartedAtMs) / (float) duration));
    }

    static boolean primeDoubleTap(LocalPlayer player) {
        Object operator = operator(player);
        if (operator == null) {
            return false;
        }
        try {
            Object dataHolder = getDataHolder.invoke(operator);
            if (dataHolder == null) {
                return false;
            }
            clientLastShootTimestamp.setLong(dataHolder, clientShootTimestamp.getLong(dataHolder));
            clientShootTimestamp.setLong(dataHolder, -1L);
            isShootRecorded.setBoolean(dataHolder, true);
            return true;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            available = false;
            return false;
        }
    }

    static ShootAttempt tryDoubleTapShoot(LocalPlayer player) {
        Object operator = operator(player);
        if (operator == null || networkChannel == null || clientShootMessageConstructor == null) {
            return ShootAttempt.UNAVAILABLE;
        }
        if (!doubleTapCharging || !doubleTapCharged || doubleTapPlayerId != player.getId()) {
            return ShootAttempt.HANDLED;
        }
        try {
            Object dataHolder = getDataHolder.invoke(operator);
            if (dataHolder == null) {
                return ShootAttempt.HANDLED;
            }
            long drawTimestamp = clientDrawTimestamp.getLong(dataHolder);
            if (drawTimestamp > 0L && System.currentTimeMillis() + DRAW_GRACE_MS < drawTimestamp) {
                return ShootAttempt.HANDLED;
            }
            Object cooldownValue = getClientShootCoolDown.invoke(operator);
            if (!(cooldownValue instanceof Number cooldownNumber)) {
                return ShootAttempt.UNAVAILABLE;
            }
            long cooldown = cooldownNumber.longValue();
            if (cooldown < 50L) {
                return tryShoot(player);
            }
            long baseTimestamp = clientBaseTimestamp.getLong(dataHolder);
            if (baseTimestamp < 0L) {
                return ShootAttempt.HANDLED;
            }
            long now = System.currentTimeMillis();
            long shift = Math.min(285L, Math.max(55L, cooldown + 8L));
            long shiftedNow = now + shift;
            long packetTimestamp = shiftedNow - baseTimestamp;
            clientLastShootTimestamp.setLong(dataHolder, clientShootTimestamp.getLong(dataHolder));
            clientShootTimestamp.setLong(dataHolder, shiftedNow);
            isShootRecorded.setBoolean(dataHolder, true);
            consumeDoubleTap(now, shiftedNow, cooldown);
            applyDoubleTapShift(player);
            TaczRagebotHooks.registerDoubleTapCooldownBypass(player, packetTimestamp);
            Object message = clientShootMessageConstructor.newInstance(packetTimestamp, 0.0f);
            TaczRagebotHooks.registerAntiAimDoubleTapShot(player);
            sendToServer.invoke(networkChannel, message);
            clientShootTimestamp.setLong(dataHolder, now);
            isShootRecorded.setBoolean(dataHolder, true);
            triggerDoubleTapAnimation(player);
            return ShootAttempt.SUCCESS;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            available = false;
            return ShootAttempt.UNAVAILABLE;
        }
    }

    private static void consumeDoubleTap(long now, long shiftedNow, long cooldown) {
        doubleTapCharged = false;
        doubleTapChargeStartedAtMs = now;
        doubleTapReadyAtMs = Math.max(now + DOUBLE_TAP_CHARGE_MS,
                shiftedNow + Math.max(50L, cooldown));
    }

    private static void triggerDoubleTapAnimation(LocalPlayer player) {
        if (getGunDisplay == null || getAnimationStateMachine == null || triggerAnimation == null) {
            return;
        }
        try {
            Object displayOptional = getGunDisplay.invoke(null, gunStack(player));
            if (!(displayOptional instanceof Optional<?> optional) || optional.isEmpty()) {
                return;
            }
            Object stateMachine = getAnimationStateMachine.invoke(optional.get());
            if (stateMachine != null) {
                triggerAnimation.invoke(stateMachine, "shoot");
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // The real server-side shot remains valid even if a gun pack has no client animation.
        }
    }

    private static void applyDoubleTapShift(LocalPlayer player) {
        if (player == null || player.connection == null || BypassProtocolService.correctionRecoveryActive()) {
            return;
        }
        Vec3 velocity = player.getDeltaMovement();
        Vec3 direction = new Vec3(velocity.x, 0.0D, velocity.z);
        if (direction.lengthSqr() < DOUBLE_TAP_SHIFT_MIN_SPEED_SQR) {
            return;
        }
        Vec3 shift = direction.normalize().scale(DOUBLE_TAP_SHIFT_DISTANCE);
        double oldX = player.getX();
        double oldY = player.getY();
        double oldZ = player.getZ();
        player.move(MoverType.SELF, shift);
        if (Math.abs(player.getX() - oldX) < 1.0E-5D
                && Math.abs(player.getY() - oldY) < 1.0E-5D
                && Math.abs(player.getZ() - oldZ) < 1.0E-5D) {
            return;
        }
        player.connection.send(new ServerboundMovePlayerPacket.Pos(
                player.getX(), player.getY(), player.getZ(), player.onGround()));
    }

    static boolean setAiming(LocalPlayer player, boolean aiming) {
        Object operator = operator(player);
        if (operator == null || aim == null) {
            return false;
        }
        setTaczAimKeyDown(aiming);
        try {
            if (isAim != null) {
                Object current = isAim.invoke(operator);
                if (current instanceof Boolean currentValue && currentValue == aiming) {
                    return true;
                }
            }
            aim.invoke(operator, aiming);
            return true;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            available = false;
            return false;
        }
    }

    static boolean aimingReady(LocalPlayer player) {
        Object operator = operator(player);
        if (operator == null || isAim == null || getClientAimingProgress == null) {
            return true;
        }
        try {
            Object aiming = isAim.invoke(operator);
            if (!(aiming instanceof Boolean aimingValue) || !aimingValue) {
                return false;
            }
            Object progress = getClientAimingProgress.invoke(operator, 1.0f);
            return progress instanceof Number progressValue && progressValue.floatValue() >= 0.92f;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            available = false;
            return true;
        }
    }

    private static void setTaczAimKeyDown(boolean down) {
        if (taczAimKey != null) {
            taczAimKey.setDown(down);
        }
    }

    static void setAutoStopSprint(LocalPlayer player, boolean stop) {
        if (!init()) {
            return;
        }
        if (stop) {
            autoStopSprintActive = true;
            resumeSprintAfterAutoStop |= player != null && (player.isSprinting()
                    || Minecraft.getInstance().options.keySprint.isDown());
        }
        if (stopSprint != null) {
            try {
                stopSprint.setBoolean(null, stop);
            } catch (IllegalAccessException | LinkageError ignored) {
                stopSprint = null;
            }
        }
        if (!stop) {
            restoreSprintAfterAutoStop(player);
            return;
        }
        if (player == null || !player.isSprinting()) {
            return;
        }
        player.setSprinting(false);
        if (player.connection != null) {
            player.connection.send(new ServerboundPlayerCommandPacket(
                    player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
        }
    }

    private static void restoreSprintAfterAutoStop(LocalPlayer player) {
        if (!autoStopSprintActive) {
            return;
        }
        boolean restore = resumeSprintAfterAutoStop
                && player != null
                && player.input != null
                && player.input.forwardImpulse > 0.0f;
        autoStopSprintActive = false;
        resumeSprintAfterAutoStop = false;
        if (!restore) {
            return;
        }
        player.setSprinting(true);
        if (player.connection != null) {
            player.connection.send(new ServerboundPlayerCommandPacket(
                    player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
        }
    }

    private static ItemStack gunStack(LocalPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (isTaczGun(mainHand)) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        return isTaczGun(offHand) ? offHand : ItemStack.EMPTY;
    }

    private static boolean isTaczGun(ItemStack stack) {
        if (stack == null || stack.isEmpty() || getIGunOrNull == null) {
            return false;
        }
        try {
            return getIGunOrNull.invoke(null, stack) != null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private static Object operator(LocalPlayer player) {
        if (player == null || !init()) {
            return null;
        }
        try {
            return fromLocalPlayer.invoke(null, player);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            available = false;
            return null;
        }
    }

    private static ReadyState readyState(Object operator) {
        if (operator == null || getClientShootCoolDown == null) {
            return ReadyState.UNAVAILABLE;
        }
        try {
            Object dataHolder = getDataHolder.invoke(operator);
            if (dataHolder != null) {
                long drawTimestamp = clientDrawTimestamp.getLong(dataHolder);
                if (drawTimestamp > 0L && System.currentTimeMillis() + DRAW_GRACE_MS < drawTimestamp) {
                    return ReadyState.WAIT;
                }
            }
            Object cooldown = getClientShootCoolDown.invoke(operator);
            if (cooldown instanceof Number number && number.longValue() >= 50L) {
                return ReadyState.WAIT_COOLDOWN;
            }
            return ReadyState.READY;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            available = false;
            return ReadyState.UNAVAILABLE;
        }
    }

    private static boolean init() {
        if (initialized) {
            return available;
        }
        initialized = true;
        try {
            Class<?> operatorClass = Class.forName("com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator");
            fromLocalPlayer = operatorClass.getMethod("fromLocalPlayer", LocalPlayer.class);
            chargeShoot = operatorClass.getMethod("chargeShoot", boolean.class);
            shoot = operatorClass.getMethod("shoot");
            aim = operatorClass.getMethod("aim", boolean.class);
            isAim = operatorClass.getMethod("isAim");
            getDataHolder = operatorClass.getMethod("getDataHolder");
            getClientAimingProgress = operatorClass.getMethod("getClientAimingProgress", float.class);
            getClientShootCoolDown = operatorClass.getMethod("getClientShootCoolDown");
            Class<?> dataHolderClass = Class.forName("com.tacz.guns.client.gameplay.LocalPlayerDataHolder");
            clientDrawTimestamp = dataHolderClass.getField("clientDrawTimestamp");
            clientBaseTimestamp = dataHolderClass.getField("clientBaseTimestamp");
            clientShootTimestamp = dataHolderClass.getField("clientShootTimestamp");
            clientLastShootTimestamp = dataHolderClass.getField("clientLastShootTimestamp");
            isShootRecorded = dataHolderClass.getField("isShootRecorded");
            Class<?> itemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
            Class<?> gunClass = Class.forName("com.tacz.guns.api.item.IGun");
            getIGunOrNull = gunClass.getMethod("getIGunOrNull", itemStackClass);
            getFireMode = gunClass.getMethod("getFireMode", itemStackClass);
            Class<?> timelessApiClass = Class.forName("com.tacz.guns.api.TimelessAPI");
            getGunDisplay = timelessApiClass.getMethod("getGunDisplay", itemStackClass);
            Class<?> gunDisplayClass = Class.forName("com.tacz.guns.client.resource.GunDisplayInstance");
            getAnimationStateMachine = gunDisplayClass.getMethod("getAnimationStateMachine");
            Class<?> animationStateMachineClass = Class.forName(
                    "com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine");
            triggerAnimation = animationStateMachineClass.getMethod("trigger", String.class);
            Class<?> networkHandlerClass = Class.forName("com.tacz.guns.network.NetworkHandler");
            networkChannel = networkHandlerClass.getField("CHANNEL").get(null);
            sendToServer = networkChannel.getClass().getMethod("sendToServer", Object.class);
            Class<?> clientShootMessageClass = Class.forName("com.tacz.guns.network.message.ClientMessagePlayerShoot");
            clientShootMessageConstructor = clientShootMessageClass.getConstructor(long.class, float.class);
            try {
                stopSprint = Class.forName("com.tacz.guns.client.gameplay.LocalPlayerSprint")
                        .getField("stopSprint");
            } catch (ReflectiveOperationException | LinkageError ignored) {
                stopSprint = null;
            }
            try {
                Object key = Class.forName("com.tacz.guns.client.input.AimKey")
                        .getField("AIM_KEY")
                        .get(null);
                taczAimKey = key instanceof KeyMapping keyMapping ? keyMapping : null;
            } catch (ReflectiveOperationException | LinkageError ignored) {
                taczAimKey = null;
            }
            available = true;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            available = false;
        }
        return available;
    }

    enum ShootAttempt {
        UNAVAILABLE,
        HANDLED,
        NO_AMMO,
        SUCCESS
    }

    enum ReadyState {
        UNAVAILABLE,
        WAIT,
        WAIT_COOLDOWN,
        READY
    }
}
