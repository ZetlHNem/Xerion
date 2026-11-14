package com.xerion.forge.gui.dashboard;

import com.xerion.common.config.PingSpoofSettings;

final class PingSpoofSettingsPage {
    private PingSpoofSettingsPage() {
    }

    static void populate(WidgetSurface.Frame gui) {
        PingSpoofSettings.setEnabled(gui.controlledCheckbox("Enabled",
                PingSpoofSettings.enabled(), PingSpoofSettings::setEnabled));
        float delay = gui.slider("Delay", PingSpoofSettings.delaySlider(),
                Math.round(PingSpoofSettings.delayMs()) + "ms",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 25_000.0f, "ms"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 25_000.0f),
                PingSpoofSettings::setDelaySlider);
        PingSpoofSettings.setDelaySlider(delay);
    }
}
