package com.xerion.common.combat;

import com.xerion.common.config.CombatSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RagebotAntiAimRulesTest {
    @Test
    void resolvesPitchModes() {
        assertEquals(-89.0f, RagebotAntiAimRules.pitch(CombatSettings.RAGEBOT_PITCH_UP, 12.0f, 35.0f, false));
        assertEquals(89.0f, RagebotAntiAimRules.pitch(CombatSettings.RAGEBOT_PITCH_DOWN, 12.0f, 35.0f, false));
        assertEquals(0.0f, RagebotAntiAimRules.pitch(CombatSettings.RAGEBOT_PITCH_ZERO, 12.0f, 35.0f, false));
        assertEquals(-89.0f, RagebotAntiAimRules.pitch(CombatSettings.RAGEBOT_PITCH_JITTER, 12.0f, 35.0f, false));
        assertEquals(89.0f, RagebotAntiAimRules.pitch(CombatSettings.RAGEBOT_PITCH_JITTER, 12.0f, 35.0f, true));
        assertEquals(-35.0f, RagebotAntiAimRules.pitch(CombatSettings.RAGEBOT_PITCH_CUSTOM, 12.0f, 35.0f, false));
        assertEquals(35.0f, RagebotAntiAimRules.pitch(CombatSettings.RAGEBOT_PITCH_CUSTOM, 12.0f, -35.0f, false));
    }

    @Test
    void resolvesAndNormalizesYawModes() {
        assertEquals(-170.0f,
                RagebotAntiAimRules.yaw(CombatSettings.RAGEBOT_YAW_LOCAL_VIEW, 10.0f, 75.0f, 0.0f));
        assertEquals(-135.0f,
                RagebotAntiAimRules.yaw(CombatSettings.RAGEBOT_YAW_AT_TARGETS, 10.0f, 75.0f, 30.0f));
        assertEquals(-75.0f,
                RagebotAntiAimRules.yaw(CombatSettings.RAGEBOT_YAW_AT_TARGETS, 10.0f, 75.0f, -30.0f));
        assertEquals(10.0f,
                RagebotAntiAimRules.yaw(CombatSettings.RAGEBOT_YAW_OFF, 10.0f, 75.0f, 30.0f));
    }

    @Test
    void detectsConfiguredOverrides() {
        assertFalse(RagebotAntiAimRules.configured(
                CombatSettings.RAGEBOT_PITCH_OFF, CombatSettings.RAGEBOT_YAW_OFF));
        assertTrue(RagebotAntiAimRules.configured(
                CombatSettings.RAGEBOT_PITCH_UP, CombatSettings.RAGEBOT_YAW_OFF));
        assertFalse(RagebotAntiAimRules.yawOverride(CombatSettings.RAGEBOT_YAW_OFF));
        assertTrue(RagebotAntiAimRules.yawOverride(CombatSettings.RAGEBOT_YAW_LOCAL_VIEW));
    }

    @Test
    void appliesYawModifiers() {
        assertEquals(0.0f, RagebotAntiAimRules.modifierOffset(
                CombatSettings.RAGEBOT_YAW_MODIFIER_DISABLE, 60.0f, 1));
        assertEquals(-30.0f, RagebotAntiAimRules.modifierOffset(
                CombatSettings.RAGEBOT_YAW_MODIFIER_CENTER, 60.0f, 0));
        assertEquals(30.0f, RagebotAntiAimRules.modifierOffset(
                CombatSettings.RAGEBOT_YAW_MODIFIER_CENTER, 60.0f, 1));
        assertEquals(0.0f, RagebotAntiAimRules.modifierOffset(
                CombatSettings.RAGEBOT_YAW_MODIFIER_OFFSET, 60.0f, 0));
        assertEquals(60.0f, RagebotAntiAimRules.modifierOffset(
                CombatSettings.RAGEBOT_YAW_MODIFIER_OFFSET, 60.0f, 1));
        assertEquals(-60.0f, RagebotAntiAimRules.modifierOffset(
                CombatSettings.RAGEBOT_YAW_MODIFIER_3_WAY, 60.0f, 0));
        assertEquals(0.0f, RagebotAntiAimRules.modifierOffset(
                CombatSettings.RAGEBOT_YAW_MODIFIER_3_WAY, 60.0f, 1));
        assertEquals(60.0f, RagebotAntiAimRules.modifierOffset(
                CombatSettings.RAGEBOT_YAW_MODIFIER_3_WAY, 60.0f, 2));
        assertEquals(-30.0f, RagebotAntiAimRules.modifierOffset(
                CombatSettings.RAGEBOT_YAW_MODIFIER_5_WAY, 60.0f, 1));
        assertEquals(30.0f, RagebotAntiAimRules.modifierOffset(
                CombatSettings.RAGEBOT_YAW_MODIFIER_5_WAY, 60.0f, 3));
    }

    @Test
    void appliesYawBaseBeforeModifier() {
        assertEquals(95.0f, RagebotAntiAimRules.yaw(
                CombatSettings.RAGEBOT_YAW_LOCAL_VIEW, 10.0f, 75.0f,
                95.0f, CombatSettings.RAGEBOT_YAW_MODIFIER_DISABLE, 60.0f, 0));
        assertEquals(65.0f, RagebotAntiAimRules.yaw(
                CombatSettings.RAGEBOT_YAW_LOCAL_VIEW, 10.0f, 75.0f,
                95.0f, CombatSettings.RAGEBOT_YAW_MODIFIER_CENTER, 60.0f, 1));
    }
}
