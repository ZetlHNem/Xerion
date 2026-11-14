package com.xerion.forge.gui.dashboard;

import com.xerion.common.config.BypassSettings;

final class BypassSettingsPage {
    private BypassSettingsPage() {
    }

    static void populateMain(WidgetSurface.Frame gui) {
        BypassSettings.setEnabled(gui.controlledCheckbox("Enabled",
                BypassSettings.enabled(), BypassSettings::setEnabled));
        BypassSettings.setProfile(gui.combo("Profile", BypassSettings.profile(),
                "Adaptive, Strict", BypassSettings::setProfile));
    }

    static void populateRotation(WidgetSurface.Frame gui) {
        BypassSettings.setRotationQuantization(gui.controlledCheckbox("Rotation Quantization",
                BypassSettings.rotationQuantization(), BypassSettings::setRotationQuantization));
        BypassSettings.setRotationRandomization(gui.controlledCheckbox("Rotation Randomization",
                BypassSettings.rotationRandomization(), BypassSettings::setRotationRandomization));
        float amount = gui.slider("Randomization Amount",
                BypassSettings.rotationRandomizationAmountSlider(),
                String.format(java.util.Locale.ROOT, "%.2f", BypassSettings.rotationRandomizationAmount()),
                value -> SliderValueFormat.decimalRangeLabel(value, 0.0f, 1.0f, 2),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 1.0f),
                BypassSettings::setRotationRandomizationAmountSlider);
        BypassSettings.setRotationRandomizationAmountSlider(amount);
    }

    static void populateTiming(WidgetSurface.Frame gui) {
        BypassSettings.setAttackTiming(gui.controlledCheckbox("Attack Timing",
                BypassSettings.attackTiming(), BypassSettings::setAttackTiming));
        float jitter = gui.slider("Jitter Ticks", BypassSettings.attackJitterTicksSlider(),
                String.valueOf(BypassSettings.attackJitterTicks()),
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 3.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 3.0f),
                BypassSettings::setAttackJitterTicksSlider);
        BypassSettings.setAttackJitterTicksSlider(jitter);
    }

    static void populateNetwork(WidgetSurface.Frame gui) {
        BypassSettings.setActionDeduplication(gui.controlledCheckbox("State Deduplication",
                BypassSettings.actionDeduplication(), BypassSettings::setActionDeduplication));
        BypassSettings.setPacketDeduplication(gui.controlledCheckbox("Movement Deduplication",
                BypassSettings.packetDeduplication(), BypassSettings::setPacketDeduplication));
        float window = gui.slider("Deduplication Window",
                BypassSettings.actionDeduplicationWindowSlider(),
                BypassSettings.actionDeduplicationWindowMs() + "ms",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 250.0f, "ms"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 250.0f),
                BypassSettings::setActionDeduplicationWindowSlider);
        BypassSettings.setActionDeduplicationWindowSlider(window);
    }

    static void populateMovement(WidgetSurface.Frame gui) {
        BypassSettings.setCorrectionRecovery(gui.controlledCheckbox("Correction Recovery",
                BypassSettings.correctionRecovery(), BypassSettings::setCorrectionRecovery));
        float window = gui.slider("Recovery Window",
                BypassSettings.correctionRecoveryWindowSlider(),
                BypassSettings.correctionRecoveryWindowMs() + "ms",
                value -> SliderValueFormat.intRangeLabel(value, 150.0f, 1000.0f, "ms"),
                value -> SliderValueFormat.parseRangeSlider(value, 150.0f, 1000.0f),
                BypassSettings::setCorrectionRecoveryWindowSlider);
        BypassSettings.setCorrectionRecoveryWindowSlider(window);
    }
}
