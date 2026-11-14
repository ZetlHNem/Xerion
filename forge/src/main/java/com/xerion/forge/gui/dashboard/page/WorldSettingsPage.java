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

import com.xerion.common.config.VisualRanges;
import com.xerion.forge.config.VisualSettings;

final class WorldSettingsPage {
    private WorldSettingsPage() {
    }

    static void populateWorldSpecialBlocks(WidgetSurface.Frame gui) {
        VisualSettings.setBlockOverlayEnabled(gui.controlledCheckboxWithSettings("Block Overlay",
                VisualSettings.blockOverlayEnabled(), WorldSettingsPage::populateWorldBlockOverlaySettings,
                VisualSettings::setBlockOverlayEnabled));
        VisualSettings.setBarrierBlocksEnabled(gui.controlledCheckboxWithSettings("Barrier Blocks",
                VisualSettings.barrierBlocksEnabled(), WorldSettingsPage::populateWorldBarrierBlocksSettings,
                VisualSettings::setBarrierBlocksEnabled));
        VisualSettings.setLightBlocksEnabled(gui.controlledCheckboxWithSettings("Light Blocks",
                VisualSettings.lightBlocksEnabled(), WorldSettingsPage::populateWorldLightBlocksSettings,
                VisualSettings::setLightBlocksEnabled));
        VisualSettings.setBedEspEnabled(gui.controlledCheckboxWithSettings("Bed ESP",
                VisualSettings.bedEspEnabled(), WorldSettingsPage::populateWorldBedEspSettings,
                VisualSettings::setBedEspEnabled));
    }

    static void populateWorldRender(WidgetSurface.Frame gui) {
        VisualSettings.setFullbrightEnabled(gui.controlledCheckboxWithSettings("Fullbright",
                VisualSettings.fullbrightEnabled(), WorldSettingsPage::populateWorldFullbrightSettings,
                VisualSettings::setFullbrightEnabled));
        VisualSettings.setNoFogEnabled(gui.controlledCheckboxWithSettings("No Fog",
                VisualSettings.noFogEnabled(), WorldSettingsPage::populateWorldNoFogSettings,
                VisualSettings::setNoFogEnabled));
        VisualSettings.setNoWeatherEnabled(gui.controlledCheckboxWithSettings("No Weather",
                VisualSettings.noWeatherEnabled(), WorldSettingsPage::populateWorldNoWeatherSettings,
                VisualSettings::setNoWeatherEnabled));
        VisualSettings.setTimeChangerEnabled(gui.controlledCheckboxWithSettings("Time Changer",
                VisualSettings.timeChangerEnabled(), WorldSettingsPage::populateWorldTimeChangerSettings,
                VisualSettings::setTimeChangerEnabled));
        VisualSettings.setAmbienceEnabled(gui.controlledCheckboxWithSettings("Ambience",
                VisualSettings.ambienceEnabled(), WorldSettingsPage::populateWorldAmbienceSettings,
                VisualSettings::setAmbienceEnabled));
        VisualSettings.setAntiBlindnessEnabled(gui.controlledCheckboxWithSettings("Anti Blindness",
                VisualSettings.antiBlindnessEnabled(), WorldSettingsPage::populateWorldAntiBlindnessSettings,
                VisualSettings::setAntiBlindnessEnabled));
    }

    static void populateWorldUtility(WidgetSurface.Frame gui) {
        VisualSettings.setFreecamEnabled(gui.controlledCheckboxWithSettings("Freecam",
                VisualSettings.freecamEnabled(), WorldSettingsPage::populateWorldFreecamSettings,
                VisualSettings::setFreecamEnabled));
        VisualSettings.setZoomEnabled(gui.controlledCheckboxWithSettings("Zoom",
                VisualSettings.zoomEnabled(), WorldSettingsPage::populateWorldZoomSettings,
                VisualSettings::setZoomEnabled));
        VisualSettings.setAspectRatioEnabled(gui.controlledCheckboxWithSettings("Aspect Ratio",
                VisualSettings.aspectRatioEnabled(), WorldSettingsPage::populateWorldAspectRatioSettings,
                VisualSettings::setAspectRatioEnabled));
        VisualSettings.setViewModelEnabled(gui.controlledCheckboxWithSettings("View Model",
                VisualSettings.viewModelEnabled(), WorldSettingsPage::populateWorldViewModelSettings,
                VisualSettings::setViewModelEnabled));
        VisualSettings.setTimerEnabled(gui.controlledCheckboxWithSettings("Timer",
                VisualSettings.timerEnabled(), WorldSettingsPage::populateTimerSettings,
                VisualSettings::setTimerEnabled));
    }

    static void populateTimerSettings(WidgetSurface.Frame gui) {
        float speed = gui.slider("Game Speed", VisualSettings.timerSpeedSlider(),
                String.format(java.util.Locale.ROOT, "%.2fx", VisualSettings.timerSpeed()),
                value -> SliderValueFormat.decimalLabel(value, 0.05f, 10.0f),
                value -> SliderValueFormat.parseRangeSlider(value, 0.05f, 10.0f),
                VisualSettings::setTimerSpeedSlider);
        VisualSettings.setTimerSpeedSlider(speed);
    }

    static void populateWorldSpawnerEspSettings(WidgetSurface.Frame gui) {
        float range = gui.slider("Range", VisualSettings.spawnerEspRangeSlider(),
                Math.round(VisualSettings.spawnerEspRange()) + "m",
                value -> SliderValueFormat.intRangeLabel(value, 16.0f, 64.0f, "m"),
                value -> SliderValueFormat.parseRangeSlider(value, 16.0f, 64.0f),
                VisualSettings::setSpawnerEspRangeSlider);
        VisualSettings.setSpawnerEspRangeSlider(range);
        VisualSettings.setSpawnerEspShowType(gui.controlledCheckbox("Show Type",
                VisualSettings.spawnerEspShowType(), VisualSettings::setSpawnerEspShowType));
        VisualSettings.setSpawnerEspThroughWalls(gui.controlledCheckbox("Through Walls",
                VisualSettings.spawnerEspThroughWalls(), VisualSettings::setSpawnerEspThroughWalls));
    }

    static void populateWorldBlockOverlaySettings(WidgetSurface.Frame gui) {
        VisualSettings.setBlockOverlaySingleSide(gui.controlledCheckbox("Single Side",
                VisualSettings.blockOverlaySingleSide(), VisualSettings::setBlockOverlaySingleSide));
        VisualSettings.setBlockOverlayShapeMode(gui.combo("Shape Mode", VisualSettings.blockOverlayShapeMode(),
                "Both, Lines, Sides"));
        VisualSettings.setBlockOverlaySideColor(gui.colorPicker("Side Color",
                VisualSettings.blockOverlaySideColor(), VisualSettings::setBlockOverlaySideColor));
        VisualSettings.setBlockOverlayLineColor(gui.colorPicker("Line Color",
                VisualSettings.blockOverlayLineColor(), VisualSettings::setBlockOverlayLineColor));
    }

    static void populateWorldBarrierBlocksSettings(WidgetSurface.Frame gui) {
        float range = gui.slider("Range", VisualSettings.barrierBlocksRangeSlider(),
                Math.round(VisualSettings.barrierBlocksRange()) + "m",
                value -> SliderValueFormat.intRangeLabel(value, 8.0f, 64.0f, "m"),
                value -> SliderValueFormat.parseRangeSlider(value, 8.0f, 64.0f),
                VisualSettings::setBarrierBlocksRangeSlider);
        VisualSettings.setBarrierBlocksRangeSlider(range);
        VisualSettings.setBarrierBlocksThroughWalls(gui.controlledCheckbox("Through Walls",
                VisualSettings.barrierBlocksThroughWalls(), VisualSettings::setBarrierBlocksThroughWalls));
        VisualSettings.setBarrierBlocksColor(gui.colorPicker("Color",
                VisualSettings.barrierBlocksColor(), VisualSettings::setBarrierBlocksColor));
    }

    static void populateWorldLightBlocksSettings(WidgetSurface.Frame gui) {
        float range = gui.slider("Range", VisualSettings.lightBlocksRangeSlider(),
                Math.round(VisualSettings.lightBlocksRange()) + "m",
                value -> SliderValueFormat.intRangeLabel(value, 8.0f, 64.0f, "m"),
                value -> SliderValueFormat.parseRangeSlider(value, 8.0f, 64.0f),
                VisualSettings::setLightBlocksRangeSlider);
        VisualSettings.setLightBlocksRangeSlider(range);
        VisualSettings.setLightBlocksShowLevel(gui.controlledCheckbox("Show Level",
                VisualSettings.lightBlocksShowLevel(), VisualSettings::setLightBlocksShowLevel));
        VisualSettings.setLightBlocksAllGlowing(gui.controlledCheckbox("All Glowing",
                VisualSettings.lightBlocksAllGlowing(), VisualSettings::setLightBlocksAllGlowing));
        VisualSettings.setLightBlocksGradientFace(gui.controlledCheckbox("Gradient Face",
                VisualSettings.lightBlocksGradientFace(), VisualSettings::setLightBlocksGradientFace));
        VisualSettings.setLightBlocksColor(gui.colorPicker("Color",
                VisualSettings.lightBlocksColor(), VisualSettings::setLightBlocksColor));
    }

    static void populateWorldBedEspSettings(WidgetSurface.Frame gui) {
        float range = gui.slider("Range", VisualSettings.bedEspRangeSlider(),
                Math.round(VisualSettings.bedEspRange()) + "m",
                value -> SliderValueFormat.intRangeLabel(value, 8.0f, 64.0f, "m"),
                value -> SliderValueFormat.parseRangeSlider(value, 8.0f, 64.0f),
                VisualSettings::setBedEspRangeSlider);
        VisualSettings.setBedEspRangeSlider(range);
        VisualSettings.setBedEspThroughWalls(gui.controlledCheckbox("Through Walls",
                VisualSettings.bedEspThroughWalls(), VisualSettings::setBedEspThroughWalls));
        VisualSettings.setBedEspColor(gui.colorPicker("Color",
                VisualSettings.bedEspColor(), VisualSettings::setBedEspColor));
    }

    static void populateWorldFullbrightSettings(WidgetSurface.Frame gui) {
        VisualSettings.setFullbrightMode(gui.combo("Mode", VisualSettings.fullbrightMode(),
                "Gamma, Night Vision", VisualSettings::setFullbrightMode));
        gui.visible("fullbright-gamma-strength", "Gamma".equals(VisualSettings.fullbrightMode()), visibleGui -> {
            float strength = visibleGui.slider("Strength", VisualSettings.fullbrightStrengthSlider(),
                    Math.round(VisualSettings.fullbrightStrength() * 100.0f) + "%",
                    value -> SliderValueFormat.intRangeLabel(value, 0.0f, 100.0f, "%"),
                    value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 100.0f),
                    VisualSettings::setFullbrightStrengthSlider);
            VisualSettings.setFullbrightStrengthSlider(strength);
        });
    }

    static void populateWorldNoFogSettings(WidgetSurface.Frame gui) {
        VisualSettings.setNoFogWater(gui.controlledCheckbox("Water Fog",
                VisualSettings.noFogWater(), VisualSettings::setNoFogWater));
        VisualSettings.setNoFogLava(gui.controlledCheckbox("Lava Fog",
                VisualSettings.noFogLava(), VisualSettings::setNoFogLava));
        VisualSettings.setNoFogDistance(gui.controlledCheckbox("Distance Fog",
                VisualSettings.noFogDistance(), VisualSettings::setNoFogDistance));
        VisualSettings.setNoFogPowderSnow(gui.controlledCheckbox("Powder Snow Fog",
                VisualSettings.noFogPowderSnow(), VisualSettings::setNoFogPowderSnow));
    }

    static void populateWorldNoWeatherSettings(WidgetSurface.Frame gui) {
        VisualSettings.setNoWeatherRain(gui.controlledCheckbox("Rain",
                VisualSettings.noWeatherRain(), VisualSettings::setNoWeatherRain));
        VisualSettings.setNoWeatherSnow(gui.controlledCheckbox("Snow",
                VisualSettings.noWeatherSnow(), VisualSettings::setNoWeatherSnow));
        VisualSettings.setNoWeatherThunder(gui.controlledCheckbox("Thunder",
                VisualSettings.noWeatherThunder(), VisualSettings::setNoWeatherThunder));
    }

    static void populateWorldTimeChangerSettings(WidgetSurface.Frame gui) {
        VisualSettings.setTimeChangerTime(gui.combo("World Time", VisualSettings.timeChangerTime(),
                "World, Day, Night, Sunset, Midnight", VisualSettings::setTimeChangerTime));
    }

    static void populateWorldAmbienceSettings(WidgetSurface.Frame gui) {
        VisualSettings.setAmbiencePumpkinOverlay(gui.controlledCheckbox("Pumpkin Overlay",
                VisualSettings.ambiencePumpkinOverlay(), VisualSettings::setAmbiencePumpkinOverlay));
        VisualSettings.setAmbiencePortalOverlay(gui.controlledCheckbox("Portal Overlay",
                VisualSettings.ambiencePortalOverlay(), VisualSettings::setAmbiencePortalOverlay));
        VisualSettings.setAmbienceDarknessPulse(gui.controlledCheckbox("Darkness Pulse",
                VisualSettings.ambienceDarknessPulse(), VisualSettings::setAmbienceDarknessPulse));
    }

    static void populateWorldAntiBlindnessSettings(WidgetSurface.Frame gui) {
        VisualSettings.setAntiBlindnessBlindness(gui.controlledCheckbox("Blindness",
                VisualSettings.antiBlindnessBlindness(), VisualSettings::setAntiBlindnessBlindness));
        VisualSettings.setAntiBlindnessDarkness(gui.controlledCheckbox("Darkness",
                VisualSettings.antiBlindnessDarkness(), VisualSettings::setAntiBlindnessDarkness));
        VisualSettings.setAntiBlindnessNausea(gui.controlledCheckbox("Nausea",
                VisualSettings.antiBlindnessNausea(), VisualSettings::setAntiBlindnessNausea));
    }

    static void populateWorldFreecamSettings(WidgetSurface.Frame gui) {
        float speed = gui.slider("Speed", VisualSettings.freecamSpeedSlider(),
                String.format(java.util.Locale.ROOT, "%.1fx", VisualSettings.freecamSpeed()),
                value -> String.format(java.util.Locale.ROOT, "%.1fx", value),
                value -> SliderValueFormat.parseRangeSlider(value, 0.2f, 5.0f),
                VisualSettings::setFreecamSpeedSlider);
        VisualSettings.setFreecamSpeedSlider(speed);
        VisualSettings.setFreecamNoClip(gui.controlledCheckbox("No Clip",
                VisualSettings.freecamNoClip(), VisualSettings::setFreecamNoClip));
        VisualSettings.setFreecamFreezePlayer(gui.controlledCheckbox("Freeze Player",
                VisualSettings.freecamFreezePlayer(), VisualSettings::setFreecamFreezePlayer));
    }

    static void populateWorldZoomSettings(WidgetSurface.Frame gui) {
        float zoom = gui.slider("Zoom", VisualSettings.zoomFactorSlider(),
                String.format(java.util.Locale.ROOT, "%.1fx", VisualSettings.zoomFactor()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.2f, 12.0f, 1) + "x",
                value -> SliderValueFormat.parseRangeSlider(value, 1.2f, 12.0f),
                VisualSettings::setZoomFactorSlider);
        VisualSettings.setZoomFactorSlider(zoom);
        VisualSettings.setZoomSmooth(gui.controlledCheckbox("Smooth",
                VisualSettings.zoomSmooth(), VisualSettings::setZoomSmooth));
        VisualSettings.setZoomCinematic(gui.controlledCheckbox("Cinematic",
                VisualSettings.zoomCinematic(), VisualSettings::setZoomCinematic));
        VisualSettings.setZoomShowHands(gui.controlledCheckbox("Show Hands",
                VisualSettings.zoomShowHands(), VisualSettings::setZoomShowHands));
    }

    static void populateWorldAspectRatioSettings(WidgetSurface.Frame gui) {
        VisualSettings.setAspectRatioPreset(gui.combo("Preset", VisualSettings.aspectRatioPreset(),
                "16:9, 16:10, 4:3, 21:9, 32:9, Custom", VisualSettings::setAspectRatioPreset));
        boolean customRatio = "Custom".equals(VisualSettings.aspectRatioPreset());
        gui.visible("aspect-ratio-custom-ratio", customRatio, visibleGui -> {
            float ratio = visibleGui.slider("Ratio", VisualSettings.aspectRatioSlider(),
                    String.format(java.util.Locale.ROOT, "%.2f:1", VisualSettings.aspectRatioValue()),
                    value -> SliderValueFormat.decimalRangeLabel(value, VisualRanges.ASPECT_RATIO_MIN,
                            VisualRanges.ASPECT_RATIO_MAX, 2) + ":1",
                    value -> SliderValueFormat.parseRangeSlider(value, VisualRanges.ASPECT_RATIO_MIN,
                            VisualRanges.ASPECT_RATIO_MAX),
                    VisualSettings::setAspectRatioSlider);
            if (customRatio && visibleGui.rendering()) {
                VisualSettings.setAspectRatioSlider(ratio);
            }
        });
    }

    static void populateWorldViewModelSettings(WidgetSurface.Frame gui) {
        float scale = gui.slider("Scale", VisualSettings.viewModelScaleSlider(),
                String.format(java.util.Locale.ROOT, "%.2fx", VisualSettings.viewModelScale()),
                value -> SliderValueFormat.decimalRangeLabel(value, VisualRanges.VIEW_MODEL_SCALE_MIN,
                        VisualRanges.VIEW_MODEL_SCALE_MAX, 2) + "x",
                value -> SliderValueFormat.parseRangeSlider(value, VisualRanges.VIEW_MODEL_SCALE_MIN,
                        VisualRanges.VIEW_MODEL_SCALE_MAX),
                VisualSettings::setViewModelScaleSlider);
        VisualSettings.setViewModelScaleSlider(scale);
        float offsetX = gui.slider("Offset X", VisualSettings.viewModelOffsetXSlider(),
                String.format(java.util.Locale.ROOT, "%.2f", VisualSettings.viewModelOffsetX()),
                value -> SliderValueFormat.decimalRangeLabel(value, VisualRanges.VIEW_MODEL_OFFSET_MIN,
                        VisualRanges.VIEW_MODEL_OFFSET_MAX, 2),
                value -> SliderValueFormat.parseRangeSlider(value, VisualRanges.VIEW_MODEL_OFFSET_MIN,
                        VisualRanges.VIEW_MODEL_OFFSET_MAX),
                VisualSettings::setViewModelOffsetXSlider);
        VisualSettings.setViewModelOffsetXSlider(offsetX);
        float offsetY = gui.slider("Offset Y", VisualSettings.viewModelOffsetYSlider(),
                String.format(java.util.Locale.ROOT, "%.2f", VisualSettings.viewModelOffsetY()),
                value -> SliderValueFormat.decimalRangeLabel(value, VisualRanges.VIEW_MODEL_OFFSET_MIN,
                        VisualRanges.VIEW_MODEL_OFFSET_MAX, 2),
                value -> SliderValueFormat.parseRangeSlider(value, VisualRanges.VIEW_MODEL_OFFSET_MIN,
                        VisualRanges.VIEW_MODEL_OFFSET_MAX),
                VisualSettings::setViewModelOffsetYSlider);
        VisualSettings.setViewModelOffsetYSlider(offsetY);
        float offsetZ = gui.slider("Offset Z", VisualSettings.viewModelOffsetZSlider(),
                String.format(java.util.Locale.ROOT, "%.2f", VisualSettings.viewModelOffsetZ()),
                value -> SliderValueFormat.decimalRangeLabel(value, VisualRanges.VIEW_MODEL_OFFSET_MIN,
                        VisualRanges.VIEW_MODEL_OFFSET_MAX, 2),
                value -> SliderValueFormat.parseRangeSlider(value, VisualRanges.VIEW_MODEL_OFFSET_MIN,
                        VisualRanges.VIEW_MODEL_OFFSET_MAX),
                VisualSettings::setViewModelOffsetZSlider);
        VisualSettings.setViewModelOffsetZSlider(offsetZ);
    }

    static void populateBreakerSettings(WidgetSurface.Frame gui) {
        float range = gui.slider("Range", VisualSettings.breakerRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", VisualSettings.breakerRange()),
                value -> SliderValueFormat.decimalRangeLabel(value, 0.5f, 6.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 0.5f, 6.0f),
                VisualSettings::setBreakerRangeSlider);
        VisualSettings.setBreakerRangeSlider(range);
        VisualSettings.setBreakerBreakSurroundings(gui.controlledCheckbox("Break Surroundings",
                VisualSettings.breakerBreakSurroundings(), VisualSettings::setBreakerBreakSurroundings));
        VisualSettings.setBreakerSwingMode(gui.combo("Swing Mode", VisualSettings.breakerSwingMode(),
                "Client, Server", VisualSettings::setBreakerSwingMode));
    }

    static void populateFastBreakSettings(WidgetSurface.Frame gui) {
        float speed = gui.slider("Speed", VisualSettings.fastBreakSpeedSlider(),
                VisualSettings.fastBreakSpeedPercent() + "%",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 99.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 99.0f),
                VisualSettings::setFastBreakSpeedSlider);
        VisualSettings.setFastBreakSpeedSlider(speed);
        VisualSettings.setFastBreakCooldownEnabled(gui.controlledCheckbox("Cooldown Override",
                VisualSettings.fastBreakCooldownEnabled(), VisualSettings::setFastBreakCooldownEnabled));
        float cooldown = gui.slider("Cooldown", VisualSettings.fastBreakCooldownSlider(),
                VisualSettings.fastBreakCooldown() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 5.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 5.0f),
                VisualSettings::setFastBreakCooldownSlider);
        VisualSettings.setFastBreakCooldownSlider(cooldown);
    }

    static void populateWorldAutoMineSettings(WidgetSurface.Frame gui) {
        VisualSettings.setAutoMineTarget(gui.combo("Target", VisualSettings.autoMineTarget(),
                "Crosshair, Locked Block", VisualSettings::setAutoMineTarget));
        float range = gui.slider("Range", VisualSettings.autoMineRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.1fm", VisualSettings.autoMineRange()),
                value -> SliderValueFormat.decimalRangeLabel(value, 3.0f, 6.0f, 1) + "m",
                value -> SliderValueFormat.parseRangeSlider(value, 3.0f, 6.0f),
                VisualSettings::setAutoMineRangeSlider);
        VisualSettings.setAutoMineRangeSlider(range);
        float delay = gui.slider("Delay", VisualSettings.autoMineDelaySlider(),
                VisualSettings.autoMineDelayTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 10.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 10.0f),
                VisualSettings::setAutoMineDelaySlider);
        VisualSettings.setAutoMineDelaySlider(delay);
        VisualSettings.setAutoMineSwing(gui.controlledCheckbox("Swing",
                VisualSettings.autoMineSwing(), VisualSettings::setAutoMineSwing));
    }
}
