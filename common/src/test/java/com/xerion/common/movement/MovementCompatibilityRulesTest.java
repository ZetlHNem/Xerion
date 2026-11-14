package com.xerion.common.movement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MovementCompatibilityRulesTest {
    @Test
    void movementScaleUsesSymmetricPercentageRange() {
        assertEquals(0.99D, MovementCompatibilityRules.randomizedScale(1.0f, 0.0D), 1.0E-8D);
        assertEquals(1.0D, MovementCompatibilityRules.randomizedScale(1.0f, 0.5D), 1.0E-8D);
        assertEquals(1.01D, MovementCompatibilityRules.randomizedScale(1.0f, 1.0D), 1.0E-8D);
    }
}
