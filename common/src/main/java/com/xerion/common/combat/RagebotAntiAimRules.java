package com.xerion.common.combat;

import com.xerion.common.config.CombatSettings;

public final class RagebotAntiAimRules {
    private RagebotAntiAimRules() {
    }

    public static float pitch(String mode, float viewPitch, float customPitch, boolean jitterHigh) {
        if (CombatSettings.RAGEBOT_PITCH_UP.equals(mode)) {
            return -89.0f;
        }
        if (CombatSettings.RAGEBOT_PITCH_DOWN.equals(mode)) {
            return 89.0f;
        }
        if (CombatSettings.RAGEBOT_PITCH_ZERO.equals(mode)) {
            return 0.0f;
        }
        if (CombatSettings.RAGEBOT_PITCH_JITTER.equals(mode)) {
            return jitterHigh ? 89.0f : -89.0f;
        }
        if (CombatSettings.RAGEBOT_PITCH_CUSTOM.equals(mode)) {
            return clampPitch(-customPitch);
        }
        return clampPitch(viewPitch);
    }

    public static float yaw(String mode, float localViewYaw, float targetYaw, float offset) {
        if (CombatSettings.RAGEBOT_YAW_AT_TARGETS.equals(mode)) {
            return normalizeYaw(targetYaw + 180.0f - offset);
        }
        if (CombatSettings.RAGEBOT_YAW_LOCAL_VIEW.equals(mode)) {
            return normalizeYaw(localViewYaw + 180.0f - offset);
        }
        return normalizeYaw(localViewYaw);
    }

    public static float yaw(String mode, float localViewYaw, float targetYaw,
                            String modifier, float offset, int step) {
        return yaw(mode, localViewYaw, targetYaw, 0.0f, modifier, offset, step);
    }

    public static float yaw(String mode, float localViewYaw, float targetYaw,
                            float base, String modifier, float offset, int step) {
        float baseYaw = yaw(mode, localViewYaw, targetYaw, 0.0f);
        if (CombatSettings.RAGEBOT_YAW_OFF.equals(mode)) {
            return baseYaw;
        }
        return normalizeYaw(baseYaw - base - modifierOffset(modifier, offset, step));
    }

    static float modifierOffset(String modifier, float offset, int step) {
        if (CombatSettings.RAGEBOT_YAW_MODIFIER_CENTER.equals(modifier)) {
            return (step & 1) == 0 ? -offset * 0.5f : offset * 0.5f;
        }
        if (CombatSettings.RAGEBOT_YAW_MODIFIER_OFFSET.equals(modifier)) {
            return (step & 1) == 0 ? 0.0f : offset;
        }
        if (CombatSettings.RAGEBOT_YAW_MODIFIER_RANDOM.equals(modifier)) {
            int hash = step * 0x45d9f3b;
            hash = (hash ^ (hash >>> 16)) * 0x45d9f3b;
            hash ^= hash >>> 16;
            float unit = (hash & 0x7fffffff) / (float) Integer.MAX_VALUE;
            return (unit * 2.0f - 1.0f) * Math.abs(offset);
        }
        if (CombatSettings.RAGEBOT_YAW_MODIFIER_SPIN.equals(modifier)) {
            int phase = Math.floorMod(step, 40);
            return -Math.abs(offset) + Math.abs(offset) * 2.0f * phase / 39.0f;
        }
        if (CombatSettings.RAGEBOT_YAW_MODIFIER_3_WAY.equals(modifier)) {
            return switch (Math.floorMod(step, 3)) {
                case 0 -> -offset;
                case 1 -> 0.0f;
                default -> offset;
            };
        }
        if (CombatSettings.RAGEBOT_YAW_MODIFIER_5_WAY.equals(modifier)) {
            return switch (Math.floorMod(step, 5)) {
                case 0 -> -offset;
                case 1 -> -offset * 0.5f;
                case 2 -> 0.0f;
                case 3 -> offset * 0.5f;
                default -> offset;
            };
        }
        return 0.0f;
    }

    public static boolean yawOverride(String mode) {
        return !CombatSettings.RAGEBOT_YAW_OFF.equals(mode);
    }

    public static boolean configured(String pitchMode, String yawMode) {
        return !CombatSettings.RAGEBOT_PITCH_OFF.equals(pitchMode)
                || !CombatSettings.RAGEBOT_YAW_OFF.equals(yawMode);
    }

    static float normalizeYaw(float yaw) {
        float normalized = yaw % 360.0f;
        if (normalized >= 180.0f) {
            normalized -= 360.0f;
        }
        if (normalized < -180.0f) {
            normalized += 360.0f;
        }
        return normalized;
    }

    private static float clampPitch(float pitch) {
        return Math.max(-90.0f, Math.min(90.0f, pitch));
    }
}
