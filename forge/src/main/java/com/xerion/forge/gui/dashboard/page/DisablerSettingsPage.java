package com.xerion.forge.gui.dashboard;

import com.xerion.common.config.DisablerSettings;

final class DisablerSettingsPage {
    private DisablerSettingsPage() {
    }

    static void populate(WidgetSurface.Frame gui) {
        DisablerSettings.setEnabled(gui.controlledCheckbox("Enabled",
                DisablerSettings.enabled(), DisablerSettings::setEnabled));
        DisablerSettings.setMode(gui.combo("Mode", DisablerSettings.mode(),
                "Heypixel, CubeCraft, MiniBlox", DisablerSettings::setMode));
        DisablerSettings.setOnlyRemoteServer(gui.controlledCheckbox("Only Remote Server",
                DisablerSettings.onlyRemoteServer(), DisablerSettings::setOnlyRemoteServer));
        DisablerSettings.setLogging(gui.controlledCheckbox("Logging",
                DisablerSettings.logging(), DisablerSettings::setLogging));
        DisablerSettings.setDebug(gui.controlledCheckbox("Debug",
                DisablerSettings.debug(), DisablerSettings::setDebug));

        gui.mode("disabler-heypixel-options", DisablerSettings.mode(),
                DisablerSettings.MODE_HEYPIXEL, DisablerSettingsPage::populateHeypixel);
    }

    private static void populateHeypixel(WidgetSurface.Frame gui) {
        DisablerSettings.setHeypixelGrimBadPacketsA(gui.controlledCheckbox("Grim Bad PacketsA",
                DisablerSettings.heypixelGrimBadPacketsA(),
                DisablerSettings::setHeypixelGrimBadPacketsA));
        DisablerSettings.setHeypixelGrimDuplicateRotPlace(gui.controlledCheckbox(
                "Grim Duplicate RotPlace",
                DisablerSettings.heypixelGrimDuplicateRotPlace(),
                DisablerSettings::setHeypixelGrimDuplicateRotPlace));
        DisablerSettings.setHeypixelAcaFastSwitch(gui.controlledCheckbox("ACA Fast Switch",
                DisablerSettings.heypixelAcaFastSwitch(),
                DisablerSettings::setHeypixelAcaFastSwitch));
        DisablerSettings.setHeypixelAcaInventoryFrequency(gui.controlledCheckbox(
                "ACA Inventory Frequency",
                DisablerSettings.heypixelAcaInventoryFrequency(),
                DisablerSettings::setHeypixelAcaInventoryFrequency));
        DisablerSettings.setHeypixelAcaAimStep(gui.controlledCheckbox("ACA Aim Step",
                DisablerSettings.heypixelAcaAimStep(),
                DisablerSettings::setHeypixelAcaAimStep));
        DisablerSettings.setHeypixelAcaPerfectRotation(gui.controlledCheckbox("ACA Perfect Rotation",
                DisablerSettings.heypixelAcaPerfectRotation(),
                DisablerSettings::setHeypixelAcaPerfectRotation));
        DisablerSettings.setHeypixelThemisBlink(gui.controlledCheckbox("Themis Blink",
                DisablerSettings.heypixelThemisBlink(),
                DisablerSettings::setHeypixelThemisBlink));
    }
}
