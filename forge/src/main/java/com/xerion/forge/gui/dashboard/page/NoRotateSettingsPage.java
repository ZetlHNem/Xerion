package com.xerion.forge.gui.dashboard;

import com.xerion.common.config.NoRotateSettings;

final class NoRotateSettingsPage {
    private NoRotateSettingsPage() {
    }

    static void populate(WidgetSurface.Frame gui) {
        NoRotateSettings.setEnabled(gui.controlledCheckbox("Enabled",
                NoRotateSettings.enabled(), NoRotateSettings::setEnabled));
        NoRotateSettings.setIgnoreTeleports(gui.controlledCheckbox("Ignore Teleports",
                NoRotateSettings.ignoreTeleports(), NoRotateSettings::setIgnoreTeleports));
    }
}
