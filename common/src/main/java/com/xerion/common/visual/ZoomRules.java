package com.xerion.common.visual;

public final class ZoomRules {
    private ZoomRules() {
    }

    public static boolean rendering(boolean active, float renderedFactor) {
        return active || renderedFactor > 1.01F;
    }

    public static float targetFactor(boolean active, float configuredFactor) {
        return active ? Math.max(1.0F, configuredFactor) : 1.0F;
    }

    public static float nextRenderedFactor(float renderedFactor, float targetFactor, boolean smooth) {
        if (!smooth) {
            return targetFactor;
        }
        float next = renderedFactor + (targetFactor - renderedFactor) * 0.28F;
        if (Math.abs(targetFactor - next) < 0.01F) {
            return targetFactor;
        }
        return next;
    }

    public static double applyFov(double originalFov, float renderedFactor) {
        return originalFov / Math.max(1.0F, renderedFactor);
    }
}
