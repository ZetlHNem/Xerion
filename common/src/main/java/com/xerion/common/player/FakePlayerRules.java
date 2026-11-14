package com.xerion.common.player;

public final class FakePlayerRules {
    private static final double FALLBACK_LOOK_EPSILON_SQ = 0.001D;
    private static final double RETURN_HOME_EPSILON_SQ = 0.0004D;

    private FakePlayerRules() {
    }

    public static int entityId(int firstEntityId, int index) {
        return firstEntityId - index;
    }

    public static String displayName(String baseName, int index) {
        return index == 0 ? baseName : baseName + " " + (index + 1);
    }

    public static String uuidSeed(int entityId) {
        return "xerion:fake_dummy:" + entityId;
    }

    public static float spawnYaw(boolean facePlayer, float playerYaw) {
        return facePlayer ? playerYaw + 180.0F : playerYaw;
    }

    public static String status(boolean active, int count) {
        if (!active) {
            return "None";
        }
        return count == 1 ? "1 Active" : count + " Active";
    }

    public static boolean releasableIndex(int index, int nextIndex) {
        return index >= 0 && index < nextIndex;
    }

    public static boolean needsFallbackLook(double horizontalLookLengthSq) {
        return horizontalLookLengthSq < FALLBACK_LOOK_EPSILON_SQ;
    }

    public static boolean returnHomeArrived(double distanceSq) {
        return distanceSq <= RETURN_HOME_EPSILON_SQ;
    }
}
