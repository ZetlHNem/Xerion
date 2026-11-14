package com.xerion.forge.gui.dashboard;

import com.xerion.forge.misc.*;

import com.xerion.forge.render.*;

import com.xerion.forge.world.*;

import com.xerion.forge.player.*;

import com.xerion.forge.movement.*;

import com.xerion.forge.combat.*;

import com.xerion.forge.*;

import com.xerion.forge.runtime.*;

import com.xerion.forge.event.*;

import com.xerion.forge.util.*;

import com.xerion.common.config.CombatSettings;
import com.xerion.forge.config.VisualSettings;

final class PlayerInventorySettingsPage {
    private PlayerInventorySettingsPage() {
    }

    static void populatePlayer(WidgetSurface.Frame gui) {
        VisualSettings.setAutoEatEnabled(gui.controlledCheckboxWithSettings("Auto Eat",
                VisualSettings.autoEatEnabled(), PlayerInventorySettingsPage::populateAutoEatSettings,
                VisualSettings::setAutoEatEnabled));
        VisualSettings.setAutoFishEnabled(gui.controlledCheckboxWithSettings("Auto Fish",
                VisualSettings.autoFishEnabled(), PlayerInventorySettingsPage::populateAutoFishSettings,
                VisualSettings::setAutoFishEnabled));
        CombatSettings.setAutoExpEnabled(gui.controlledCheckboxWithSettings("Auto Exp",
                CombatSettings.autoExpEnabled(), PlayerInventorySettingsPage::populateAutoExpSettings,
                CombatSettings::setAutoExpEnabled));
        VisualSettings.setInventoryMoveEnabled(gui.controlledCheckboxWithSettings("Inventory Move",
                VisualSettings.inventoryMoveEnabled(), PlayerInventorySettingsPage::populateInventoryMoveSettings,
                VisualSettings::setInventoryMoveEnabled));
        VisualSettings.setFastPlaceEnabled(gui.controlledCheckbox("Fast Place",
                VisualSettings.fastPlaceEnabled(), VisualSettings::setFastPlaceEnabled));
        VisualSettings.setAntiHungerEnabled(gui.controlledCheckbox("Anti Hunger",
                VisualSettings.antiHungerEnabled(), VisualSettings::setAntiHungerEnabled));
        VisualSettings.setAutoToolEnabled(gui.controlledCheckboxWithSettings("Auto Tool",
                VisualSettings.autoToolEnabled(), PlayerInventorySettingsPage::populatePlayerAutoToolSettings,
                VisualSettings::setAutoToolEnabled));
        VisualSettings.setAutoRespawnEnabled(gui.controlledCheckbox("Auto Respawn",
                VisualSettings.autoRespawnEnabled(), VisualSettings::setAutoRespawnEnabled));
    }

    static void populateInventoryMoveSettings(WidgetSurface.Frame gui) {
        VisualSettings.setInventoryMoveMode(gui.combo("Mode", VisualSettings.inventoryMoveMode(),
                "Normal, Heypixel", VisualSettings::setInventoryMoveMode));
    }

    static void populatePlayerAutoToolSettings(WidgetSurface.Frame gui) {
        VisualSettings.setAutoToolMode(gui.combo("Mode", VisualSettings.autoToolMode(),
                "Best, Preserve Slot", VisualSettings::setAutoToolMode));
        VisualSettings.setAutoToolSilent(gui.controlledCheckbox("Silent",
                VisualSettings.autoToolSilent(), VisualSettings::setAutoToolSilent));
        VisualSettings.setAutoToolPreferDurability(gui.controlledCheckbox("Prefer Durability",
                VisualSettings.autoToolPreferDurability(), VisualSettings::setAutoToolPreferDurability));
    }

    static void populateAutoEatSettings(WidgetSurface.Frame gui) {
        float foodLevel = gui.slider("Food Level", VisualSettings.autoEatFoodLevelSlider(),
                VisualSettings.autoEatFoodLevelInt() + "",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 20.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                VisualSettings::setAutoEatFoodLevelSlider);
        VisualSettings.setAutoEatFoodLevelSlider(foodLevel);
        float delay = gui.slider("Delay", VisualSettings.autoEatDelaySlider(),
                VisualSettings.autoEatDelayTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 40.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 40.0f),
                VisualSettings::setAutoEatDelaySlider);
        VisualSettings.setAutoEatDelaySlider(delay);
        VisualSettings.setAutoEatRestoreSlot(gui.controlledCheckbox("Restore Slot",
                VisualSettings.autoEatRestoreSlot(), VisualSettings::setAutoEatRestoreSlot));
        VisualSettings.setAutoEatInventory(gui.controlledCheckbox("Inventory",
                VisualSettings.autoEatInventory(), VisualSettings::setAutoEatInventory));
    }

    static void populateAutoFishSettings(WidgetSurface.Frame gui) {
        VisualSettings.setAutoFishAutoCast(gui.controlledCheckbox("Auto Cast",
                VisualSettings.autoFishAutoCast(), VisualSettings::setAutoFishAutoCast));
        float ticks = gui.slider("Ticks Auto Cast", VisualSettings.autoFishTicksAutoCastSlider(),
                Math.round(VisualSettings.autoFishTicksAutoCast()) + "t",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 20.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                VisualSettings::setAutoFishTicksAutoCastSlider);
        VisualSettings.setAutoFishTicksAutoCastSlider(ticks);
        float catchDelay = gui.slider("Catch Delay", VisualSettings.autoFishCatchDelaySlider(),
                Math.round(VisualSettings.autoFishCatchDelay()) + "t",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 20.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 20.0f),
                VisualSettings::setAutoFishCatchDelaySlider);
        VisualSettings.setAutoFishCatchDelaySlider(catchDelay);
        float throwDelay = gui.slider("Throw Delay", VisualSettings.autoFishThrowDelaySlider(),
                Math.round(VisualSettings.autoFishThrowDelay()) + "t",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 20.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 20.0f),
                VisualSettings::setAutoFishThrowDelaySlider);
        VisualSettings.setAutoFishThrowDelaySlider(throwDelay);
        VisualSettings.setAutoFishAntiBreak(gui.controlledCheckbox("Anti Break",
                VisualSettings.autoFishAntiBreak(), VisualSettings::setAutoFishAntiBreak));
    }

    static void populateAutoExpSettings(WidgetSurface.Frame gui) {
        CombatSettings.setAutoExpMode(gui.combo("Mode", CombatSettings.autoExpMode(),
                "All, Armor, Held", CombatSettings::setAutoExpMode));
        float durability = gui.slider("Durability", CombatSettings.autoExpDurabilitySlider(),
                Math.round(CombatSettings.autoExpDurability()) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 5.0f, 100.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 5.0f, 100.0f),
                CombatSettings::setAutoExpDurabilitySlider);
        CombatSettings.setAutoExpDurabilitySlider(durability);
        float delay = gui.slider("Delay", CombatSettings.autoExpDelaySlider(),
                CombatSettings.autoExpDelayTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 20.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setAutoExpDelaySlider);
        CombatSettings.setAutoExpDelaySlider(delay);
        CombatSettings.setAutoExpMendingOnly(gui.controlledCheckbox("Mending Only",
                CombatSettings.autoExpMendingOnly(), CombatSettings::setAutoExpMendingOnly));
        CombatSettings.setAutoExpSilentRotate(gui.controlledCheckbox("Silent Rotate",
                CombatSettings.autoExpSilentRotate(), CombatSettings::setAutoExpSilentRotate));
        CombatSettings.setAutoExpRestoreSlot(gui.controlledCheckbox("Restore Slot",
                CombatSettings.autoExpRestoreSlot(), CombatSettings::setAutoExpRestoreSlot));
    }

    static void populateAutoRefillSettings(WidgetSurface.Frame gui) {
        float threshold = gui.slider("Threshold", VisualSettings.autoRefillThresholdSlider(),
                Math.round(VisualSettings.autoRefillThreshold()) + "",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 32.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 32.0f),
                VisualSettings::setAutoRefillThresholdSlider);
        VisualSettings.setAutoRefillThresholdSlider(threshold);

        float delay = gui.slider("Delay", VisualSettings.autoRefillDelaySlider(),
                Math.round(VisualSettings.autoRefillDelay()) + "t",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 10.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 10.0f),
                VisualSettings::setAutoRefillDelaySlider);
        VisualSettings.setAutoRefillDelaySlider(delay);

        VisualSettings.setAutoRefillCurrentSlotOnly(gui.controlledCheckbox("Current Slot Only",
                VisualSettings.autoRefillCurrentSlotOnly(), VisualSettings::setAutoRefillCurrentSlotOnly));
    }

    static void populateChestStealSettings(WidgetSurface.Frame gui) {
        float delay = gui.slider("Delay", VisualSettings.chestStealDelaySlider(),
                VisualSettings.chestStealDelayTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 10.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 10.0f),
                VisualSettings::setChestStealDelaySlider);
        VisualSettings.setChestStealDelaySlider(delay);
        VisualSettings.setChestStealAutoClose(gui.controlledCheckbox("Auto Close",
                VisualSettings.chestStealAutoClose(), VisualSettings::setChestStealAutoClose));
    }
}
