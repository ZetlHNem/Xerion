package com.xerion.common.combat;

import com.xerion.common.config.BypassSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatCompatibilityRulesTest {
    @Test
    void quantizesRotationToMouseSensitivityStep() {
        double step = CombatCompatibilityRules.rotationStep(0.5D);
        CombatCompatibilityRules.Rotation rotation = CombatCompatibilityRules.filterRotation(
                10.0f, 5.0f, 13.17f, 7.04f, 0.5D, 0.0f, 0.5D, 0.5D, true);

        assertEquals(Math.round((rotation.yaw() - 10.0f) / step) * step,
                rotation.yaw() - 10.0f, 1.0E-5D);
        assertEquals(Math.round((rotation.pitch() - 5.0f) / step) * step,
                rotation.pitch() - 5.0f, 1.0E-5D);
    }

    @Test
    void strictTimingAlwaysAddsAtLeastOneTick() {
        assertEquals(3, CombatCompatibilityRules.attackDelayTicks(
                10.0f, BypassSettings.PROFILE_STRICT, 2, 0.0D));
        assertEquals(2, CombatCompatibilityRules.attackDelayTicks(
                10.0f, BypassSettings.PROFILE_ADAPTIVE, 2, 0.0D));
    }

    @Test
    void detectsOnlyActionsInsideConfiguredWindow() {
        long previous = 1_000_000_000L;
        assertTrue(CombatCompatibilityRules.duplicateAction(previous + 50_000_000L, previous, 75));
        assertFalse(CombatCompatibilityRules.duplicateAction(previous + 75_000_000L, previous, 75));
    }

}
