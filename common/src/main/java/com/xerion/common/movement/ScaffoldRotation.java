package com.xerion.common.movement;

public final class ScaffoldRotation {
    private static final long RENDER_ROTATION_TIMEOUT_MS = 350L;

    private static boolean active;
    private static boolean yawActive;
    private static float yaw;
    private static float pitch;
    private static boolean consumeAfterSend;
    private static boolean renderActive;
    private static boolean renderYawActive;
    private static float renderYaw;
    private static float renderLastYaw;
    private static float renderPitch;
    private static float renderLastPitch;
    private static long renderUpdatedAt;

    private ScaffoldRotation() {
    }

    public static void request(float targetYaw, float targetPitch, boolean applyYaw) {
        request(targetYaw, targetPitch, applyYaw, true);
    }

    public static void request(float targetYaw, float targetPitch, boolean applyYaw, boolean packetRotation) {
        if (packetRotation) {
            requestPacket(targetYaw, targetPitch, applyYaw);
        }

        updateRender(targetYaw, targetPitch, applyYaw);
    }

    public static void requestPacket(float targetYaw, float targetPitch, boolean applyYaw) {
        active = true;
        yawActive = applyYaw;
        yaw = targetYaw;
        pitch = Math.max(-90.0f, Math.min(90.0f, targetPitch));
        consumeAfterSend = true;
    }

    public static void requestPacketOnly(float targetYaw, float targetPitch, boolean applyYaw) {
        requestPacket(targetYaw, targetPitch, applyYaw);
    }

    public static void requestRender(float targetYaw, float targetPitch, boolean applyYaw) {
        updateRender(targetYaw, targetPitch, applyYaw);
    }

    private static void updateRender(float targetYaw, float targetPitch, boolean applyYaw) {
        float clampedPitch = Math.max(-90.0f, Math.min(90.0f, targetPitch));

        renderLastYaw = renderActive ? renderYaw : targetYaw;
        renderLastPitch = renderActive ? renderPitch : clampedPitch;
        renderYaw = targetYaw;
        renderPitch = clampedPitch;
        renderYawActive = applyYaw;
        renderActive = true;
        renderUpdatedAt = System.currentTimeMillis();
    }

    public static void clearRender() {
        renderActive = false;
        renderYawActive = false;
    }

    public static boolean hasRenderOverride() {
        return renderActive;
    }

    public static void clear() {
        clearPacket();
        renderActive = false;
        renderYawActive = false;
    }

    private static void clearPacket() {
        active = false;
        yawActive = false;
        consumeAfterSend = false;
    }

    public static boolean active() {
        return active;
    }

    public static boolean yawActive() {
        return yawActive;
    }

    public static float yaw() {
        return yaw;
    }

    public static float pitch() {
        return pitch;
    }

    public static boolean renderActive() {
        if (!renderActive) {
            return false;
        }
        if (System.currentTimeMillis() - renderUpdatedAt > RENDER_ROTATION_TIMEOUT_MS) {
            renderActive = false;
            renderYawActive = false;
            return false;
        }
        return true;
    }

    public static boolean renderYawActive() {
        return renderYawActive;
    }

    public static float renderYaw() {
        return renderYaw;
    }

    public static float renderLastYaw() {
        return renderLastYaw;
    }

    public static float renderPitch() {
        return renderPitch;
    }

    public static float renderLastPitch() {
        return renderLastPitch;
    }

    public static void consumeSend() {
        if (consumeAfterSend) {
            clearPacket();
        }
    }
}
