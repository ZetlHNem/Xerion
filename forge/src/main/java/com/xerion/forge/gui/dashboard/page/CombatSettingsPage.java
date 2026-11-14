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

final class CombatSettingsPage {
    private CombatSettingsPage() {
    }

    static void populateAimbot(WidgetSurface.Frame gui) {
        CombatSettings.setKillAuraEnabled(gui.controlledCheckboxWithSettings("Enabled",
                CombatSettings.killAuraEnabled(), CombatSettingsPage::populateKillAuraSettings,
                CombatSettings::setKillAuraEnabled));
        float fov = gui.slider("FOV", CombatSettings.fovSlider(),
                String.valueOf(Math.round(CombatSettings.fov())),
                value -> SliderValueFormat.intRangeLabel(value, 30.0f, 360.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 30.0f, 360.0f),
                CombatSettings::setFovSlider);
        CombatSettings.setFovSlider(fov);
        float cps = gui.slider("CPS Limit", CombatSettings.cpsLimitSlider(),
                String.valueOf(Math.round(CombatSettings.cpsLimit())),
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 20.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setCpsLimitSlider);
        CombatSettings.setCpsLimitSlider(cps);
        float range = gui.slider("Range", CombatSettings.rangeSlider(),
                String.format(java.util.Locale.ROOT, "%.1fm", CombatSettings.range()),
                value -> SliderValueFormat.decimalRangeLabel(value, 3.0f, 15.0f, 1) + "m",
                value -> SliderValueFormat.parseRangeSlider(value, 3.0f, 15.0f),
                CombatSettings::setRangeSlider);
        CombatSettings.setRangeSlider(range);
        CombatSettings.setAttackMode(gui.combo("Attack Mode", CombatSettings.attackMode(),
                "Single, Switch, Multi", CombatSettings::setAttackMode));
        CombatSettings.setBehaviorMode(gui.combo("Behavior", CombatSettings.behaviorMode(),
                "Default, Smash, Random", CombatSettings::setBehaviorMode));
        CombatSettings.setKillAuraBlockAnimation(gui.combo("Block Animation",
                CombatSettings.killAuraBlockAnimation(),
                "Off, 1.7, Pushdown, Scale, Leaked, Ninja, Down",
                CombatSettings::setKillAuraBlockAnimation));
        CombatSettings.setWeapon(gui.combo("Weapon", CombatSettings.weapon(),
                "Best Weapon, Sword, Axe", CombatSettings::setWeapon));
        CombatSettings.setPriority(gui.combo("Priority", CombatSettings.priority(),
                "Distance, Health, Armor, Angle", CombatSettings::setPriority));
        CombatSettings.setAutoblockMode(gui.combo("Autoblock", CombatSettings.autoblockMode(),
                "Off, Vanilla, Fake", CombatSettings::setAutoblockMode));
        CombatSettings.setKillAuraHeypixelBypass(gui.controlledCheckbox("Heypixel Bypass",
                CombatSettings.killAuraHeypixelBypass(), CombatSettings::setKillAuraHeypixelBypass));
        gui.settingsRow("Targets", CombatSettingsPage::populateKillAuraTargets);
        gui.settingsRow("Rotation", CombatSettingsPage::populateKillAuraRotation);
    }

    static void populateKillAuraSettings(WidgetSurface.Frame gui) {
        CombatSettings.setRayTrace(gui.controlledCheckbox("Ray Trace",
                CombatSettings.rayTrace(), CombatSettings::setRayTrace));
        CombatSettings.setThroughWalls(gui.controlledCheckbox("Through Walls",
                CombatSettings.throughWalls(), CombatSettings::setThroughWalls));
        CombatSettings.setKillAuraRequireAttackKey(gui.controlledCheckbox("Require Attack Key",
                CombatSettings.killAuraRequireAttackKey(), CombatSettings::setKillAuraRequireAttackKey));
        CombatSettings.setKillAuraRequireWeapon(gui.controlledCheckbox("Require Weapon",
                CombatSettings.killAuraRequireWeapon(), CombatSettings::setKillAuraRequireWeapon));
        CombatSettings.setKillAuraSmartWeapon(gui.controlledCheckbox("Smart Weapon",
                CombatSettings.killAuraSmartWeapon(), CombatSettings::setKillAuraSmartWeapon));
        CombatSettings.setAttackCooldown19(gui.controlledCheckbox("1.9+ Attack Cooldown",
                CombatSettings.attackCooldown19(), CombatSettings::setAttackCooldown19));
        CombatSettings.setHideFakeSwings(gui.controlledCheckbox("Hide Fake Swings",
                CombatSettings.hideFakeSwings(), CombatSettings::setHideFakeSwings));
        float swingCps = gui.slider("Swing CPS", CombatSettings.swingCpsSlider(),
                String.valueOf(Math.round(CombatSettings.swingCps())),
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 20.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setSwingCpsSlider);
        CombatSettings.setSwingCpsSlider(swingCps);
        float swingRange = gui.slider("Swing Range", CombatSettings.swingRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.1fm", CombatSettings.swingRange()),
                value -> SliderValueFormat.decimalRangeLabel(value, 3.0f, 8.0f, 1) + "m",
                value -> SliderValueFormat.parseRangeSlider(value, 3.0f, 8.0f),
                CombatSettings::setSwingRangeSlider);
        CombatSettings.setSwingRangeSlider(swingRange);
        float randomization = gui.slider("Randomization", CombatSettings.randomizationSlider(),
                Math.round(CombatSettings.randomization()) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 35.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 35.0f),
                CombatSettings::setRandomizationSlider);
        CombatSettings.setRandomizationSlider(randomization);
    }

    static void populateKillAuraTargets(WidgetSurface.Frame gui) {
        CombatSettings.setTargetPlayers(gui.controlledCheckbox("Players",
                CombatSettings.targetPlayers(), CombatSettings::setTargetPlayers));
        CombatSettings.setTargetMobs(gui.controlledCheckbox("Mobs",
                CombatSettings.targetMobs(), CombatSettings::setTargetMobs));
        CombatSettings.setTargetInvisible(gui.controlledCheckbox("Invisible Entities",
                CombatSettings.targetInvisible(), CombatSettings::setTargetInvisible));
    }

    static void populateKillAuraRotation(WidgetSurface.Frame gui) {
        CombatSettings.setSilentRotations(gui.controlledCheckbox("Silent Rotations",
                CombatSettings.silentRotations(), CombatSettings::setSilentRotations));
        CombatSettings.setSmoothAim(gui.controlledCheckbox("Smooth Aim",
                CombatSettings.smoothAim(), CombatSettings::setSmoothAim));
        float rotationRange = gui.slider("Rotation Range", CombatSettings.rotationRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.1fm", CombatSettings.rotationRange()),
                value -> SliderValueFormat.decimalRangeLabel(value, 3.0f, 8.0f, 1) + "m",
                value -> SliderValueFormat.parseRangeSlider(value, 3.0f, 8.0f),
                CombatSettings::setRotationRangeSlider);
        CombatSettings.setRotationRangeSlider(rotationRange);
        float smoothness = gui.slider("Smoothness", CombatSettings.smoothnessSlider(),
                Math.round(CombatSettings.smoothness()) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 100.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 100.0f),
                CombatSettings::setSmoothnessSlider);
        CombatSettings.setSmoothnessSlider(smoothness);
    }

    static void populateDefense(WidgetSurface.Frame gui) {
        CombatSettings.setAutoHealEnabled(gui.controlledCheckboxWithSettings("Auto Gap",
                CombatSettings.autoHealEnabled(), CombatSettingsPage::populateAutoHealSettings,
                CombatSettings::setAutoHealEnabled));
        CombatSettings.setAutoTotemEnabled(gui.controlledCheckboxWithSettings("Auto Totem",
                CombatSettings.autoTotemEnabled(), CombatSettingsPage::populateAutoTotemSettings,
                CombatSettings::setAutoTotemEnabled));
        CombatSettings.setAutoArmorEnabled(gui.controlledCheckboxWithSettings("Auto Armor",
                CombatSettings.autoArmorEnabled(), CombatSettingsPage::populateAutoArmorSettings,
                CombatSettings::setAutoArmorEnabled));
        CombatSettings.setVelocityEnabled(gui.controlledCheckboxWithSettings("Anti Knockback",
                CombatSettings.velocityEnabled(), CombatSettingsPage::populateVelocitySettings,
                CombatSettings::setVelocityEnabled));
    }

    static void populateTerrain(WidgetSurface.Frame gui) {
        CombatSettings.setWebAuraEnabled(gui.controlledCheckboxWithSettings("Web Aura",
                CombatSettings.webAuraEnabled(), CombatSettingsPage::populateWebAuraSettings,
                CombatSettings::setWebAuraEnabled));
        CombatSettings.setAutoTrapEnabled(gui.controlledCheckboxWithSettings("Auto Trap",
                CombatSettings.autoTrapEnabled(), CombatSettingsPage::populateAutoTrapSettings,
                CombatSettings::setAutoTrapEnabled));
        CombatSettings.setSurroundEnabled(gui.controlledCheckboxWithSettings("Surround",
                CombatSettings.surroundEnabled(), CombatSettingsPage::populateSurroundSettings,
                CombatSettings::setSurroundEnabled));
        CombatSettings.setHoleFillEnabled(gui.controlledCheckboxWithSettings("Hole Fill",
                CombatSettings.holeFillEnabled(), CombatSettingsPage::populateHoleFillSettings,
                CombatSettings::setHoleFillEnabled));
    }

    static void populateAutoWeaponSettings(WidgetSurface.Frame gui) {
        CombatSettings.setAutoWeaponMode(gui.combo("Mode", CombatSettings.autoWeaponMode(),
                "Best, Sword, Axe", CombatSettings::setAutoWeaponMode));
        CombatSettings.setAutoWeaponRestoreSlot(gui.controlledCheckbox("Restore Slot",
                CombatSettings.autoWeaponRestoreSlot(), CombatSettings::setAutoWeaponRestoreSlot));
    }

    static void populateShieldBreakerSettings(WidgetSurface.Frame gui) {
        CombatSettings.setShieldBreakerRestoreSlot(gui.controlledCheckbox("Restore Slot",
                CombatSettings.shieldBreakerRestoreSlot(), CombatSettings::setShieldBreakerRestoreSlot));
    }

    static void populateTargetStrafeSettings(WidgetSurface.Frame gui) {
        float radius = gui.slider("Radius", CombatSettings.targetStrafeRadiusSlider(),
                String.format(java.util.Locale.ROOT, "%.1fm", CombatSettings.targetStrafeRadius()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.2f, 5.0f, 1) + "m",
                value -> SliderValueFormat.parseRangeSlider(value, 1.2f, 5.0f),
                CombatSettings::setTargetStrafeRadiusSlider);
        CombatSettings.setTargetStrafeRadiusSlider(radius);
        float speed = gui.slider("Speed", CombatSettings.targetStrafeSpeedSlider(),
                String.format(java.util.Locale.ROOT, "%.2f", CombatSettings.targetStrafeSpeed()),
                value -> SliderValueFormat.decimalLabel(value, 0.0f, 2.0f),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 2.0f),
                CombatSettings::setTargetStrafeSpeedSlider);
        CombatSettings.setTargetStrafeSpeedSlider(speed);
        CombatSettings.setTargetStrafeDirection(gui.combo("Direction", CombatSettings.targetStrafeDirection(),
                "Auto, Left, Right", CombatSettings::setTargetStrafeDirection));
        CombatSettings.setTargetStrafeAutoJump(gui.controlledCheckbox("Auto Jump",
                CombatSettings.targetStrafeAutoJump(), CombatSettings::setTargetStrafeAutoJump));
    }

    static void populateHitboxesSettings(WidgetSurface.Frame gui) {
        float size = gui.slider("Size", CombatSettings.hitboxesSizeSlider(),
                String.format(java.util.Locale.ROOT, "%.2f", CombatSettings.hitboxesSize()),
                value -> SliderValueFormat.decimalRangeLabel(value, 0.0f, 1.0f, 2),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 1.0f),
                CombatSettings::setHitboxesSizeSlider);
        CombatSettings.setHitboxesSizeSlider(size);
    }

    static void populateBacktrackSettings(WidgetSurface.Frame gui) {
        float ticks = gui.slider("Ticks", CombatSettings.backtrackTicksSlider(),
                CombatSettings.backtrackTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 12.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 12.0f),
                CombatSettings::setBacktrackTicksSlider);
        CombatSettings.setBacktrackTicksSlider(ticks);
    }

    static void populateWebAuraSettings(WidgetSurface.Frame gui) {
        CombatSettings.setWebAuraMode(gui.combo("Mode", CombatSettings.webAuraMode(),
                "Both, Feet, Head", CombatSettings::setWebAuraMode));
        float range = gui.slider("Range", CombatSettings.webAuraRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", CombatSettings.webAuraRange()),
                value -> SliderValueFormat.decimalRangeLabel(value, 2.0f, 6.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 2.0f, 6.0f),
                CombatSettings::setWebAuraRangeSlider);
        CombatSettings.setWebAuraRangeSlider(range);
        float delay = gui.slider("Delay", CombatSettings.webAuraDelaySlider(),
                CombatSettings.webAuraDelayTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 20.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setWebAuraDelaySlider);
        CombatSettings.setWebAuraDelaySlider(delay);
        CombatSettings.setWebAuraRayTrace(gui.controlledCheckbox("Ray Trace",
                CombatSettings.webAuraRayTrace(), CombatSettings::setWebAuraRayTrace));
        CombatSettings.setWebAuraRotate(gui.controlledCheckbox("Silent Rotate",
                CombatSettings.webAuraRotate(), CombatSettings::setWebAuraRotate));
        CombatSettings.setWebAuraRestoreSlot(gui.controlledCheckbox("Restore Slot",
                CombatSettings.webAuraRestoreSlot(), CombatSettings::setWebAuraRestoreSlot));
    }

    static void populateAutoTrapSettings(WidgetSurface.Frame gui) {
        CombatSettings.setAutoTrapMode(gui.combo("Mode", CombatSettings.autoTrapMode(),
                "Full, Feet, Head", CombatSettings::setAutoTrapMode));
        float range = gui.slider("Range", CombatSettings.autoTrapRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", CombatSettings.autoTrapRange()),
                value -> SliderValueFormat.decimalRangeLabel(value, 2.0f, 6.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 2.0f, 6.0f),
                CombatSettings::setAutoTrapRangeSlider);
        CombatSettings.setAutoTrapRangeSlider(range);
        float delay = gui.slider("Delay", CombatSettings.autoTrapDelaySlider(),
                CombatSettings.autoTrapDelayTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 20.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setAutoTrapDelaySlider);
        CombatSettings.setAutoTrapDelaySlider(delay);
        CombatSettings.setAutoTrapRayTrace(gui.controlledCheckbox("Ray Trace",
                CombatSettings.autoTrapRayTrace(), CombatSettings::setAutoTrapRayTrace));
        CombatSettings.setAutoTrapRotate(gui.controlledCheckbox("Silent Rotate",
                CombatSettings.autoTrapRotate(), CombatSettings::setAutoTrapRotate));
        CombatSettings.setAutoTrapRestoreSlot(gui.controlledCheckbox("Restore Slot",
                CombatSettings.autoTrapRestoreSlot(), CombatSettings::setAutoTrapRestoreSlot));
    }

    static void populateSurroundSettings(WidgetSurface.Frame gui) {
        CombatSettings.setSurroundMode(gui.combo("Mode", CombatSettings.surroundMode(),
                "Normal, Full", CombatSettings::setSurroundMode));
        float delay = gui.slider("Delay", CombatSettings.surroundDelaySlider(),
                CombatSettings.surroundDelayTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 20.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setSurroundDelaySlider);
        CombatSettings.setSurroundDelaySlider(delay);
        CombatSettings.setSurroundFloor(gui.controlledCheckbox("Floor",
                CombatSettings.surroundFloor(), CombatSettings::setSurroundFloor));
        CombatSettings.setSurroundRayTrace(gui.controlledCheckbox("Ray Trace",
                CombatSettings.surroundRayTrace(), CombatSettings::setSurroundRayTrace));
        CombatSettings.setSurroundRotate(gui.controlledCheckbox("Silent Rotate",
                CombatSettings.surroundRotate(), CombatSettings::setSurroundRotate));
        CombatSettings.setSurroundRestoreSlot(gui.controlledCheckbox("Restore Slot",
                CombatSettings.surroundRestoreSlot(), CombatSettings::setSurroundRestoreSlot));
    }

    static void populateHoleFillSettings(WidgetSurface.Frame gui) {
        CombatSettings.setHoleFillMode(gui.combo("Mode", CombatSettings.holeFillMode(),
                "Both, Target, Self", CombatSettings::setHoleFillMode));
        float range = gui.slider("Range", CombatSettings.holeFillRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", CombatSettings.holeFillRange()),
                value -> SliderValueFormat.decimalRangeLabel(value, 2.0f, 8.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 2.0f, 8.0f),
                CombatSettings::setHoleFillRangeSlider);
        CombatSettings.setHoleFillRangeSlider(range);
        float delay = gui.slider("Delay", CombatSettings.holeFillDelaySlider(),
                CombatSettings.holeFillDelayTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 20.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setHoleFillDelaySlider);
        CombatSettings.setHoleFillDelaySlider(delay);
        CombatSettings.setHoleFillRayTrace(gui.controlledCheckbox("Ray Trace",
                CombatSettings.holeFillRayTrace(), CombatSettings::setHoleFillRayTrace));
        CombatSettings.setHoleFillRotate(gui.controlledCheckbox("Silent Rotate",
                CombatSettings.holeFillRotate(), CombatSettings::setHoleFillRotate));
        CombatSettings.setHoleFillRestoreSlot(gui.controlledCheckbox("Restore Slot",
                CombatSettings.holeFillRestoreSlot(), CombatSettings::setHoleFillRestoreSlot));
    }

    static void populateVelocitySettings(WidgetSurface.Frame gui) {
        CombatSettings.setVelocityMode(gui.combo("Mode", CombatSettings.velocityMode(),
                "Scale, Jump Reset, CubeCraft, Heypixel3", CombatSettings::setVelocityMode));
        String mode = CombatSettings.velocityMode();
        gui.visible("velocity-scale-settings", CombatSettings.VELOCITY_MODE_SCALE.equals(mode),
                CombatSettingsPage::populateVelocityScaleSettings);
        gui.visible("velocity-cubecraft-settings", CombatSettings.VELOCITY_MODE_CUBECRAFT.equals(mode),
                CombatSettingsPage::populateVelocityCubeCraftSettings);
        gui.visible("velocity-heypixel3-settings", CombatSettings.VELOCITY_MODE_HEYPIXEL3.equals(mode),
                CombatSettingsPage::populateVelocityHeypixel3Settings);
    }

    private static void populateVelocityScaleSettings(WidgetSurface.Frame gui) {
        float horizontal = gui.slider("Horizontal", CombatSettings.velocityHorizontalSlider(),
                Math.round(CombatSettings.velocityHorizontal()) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 100.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 100.0f),
                CombatSettings::setVelocityHorizontalSlider);
        CombatSettings.setVelocityHorizontalSlider(horizontal);
        float vertical = gui.slider("Vertical", CombatSettings.velocityVerticalSlider(),
                Math.round(CombatSettings.velocityVertical()) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 100.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 100.0f),
                CombatSettings::setVelocityVerticalSlider);
        CombatSettings.setVelocityVerticalSlider(vertical);
    }

    private static void populateVelocityCubeCraftSettings(WidgetSurface.Frame gui) {
        CombatSettings.setVelocityAlternativeBypass(gui.controlledCheckbox("Alternative Bypass",
                CombatSettings.velocityAlternativeBypass(),
                CombatSettings::setVelocityAlternativeBypass));
    }

    private static void populateVelocityHeypixel3Settings(WidgetSurface.Frame gui) {
        float delay = gui.slider("Delay SPacket Ticks", CombatSettings.velocityBufferDelaySlider(),
                CombatSettings.velocityBufferDelayTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 60.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 60.0f),
                CombatSettings::setVelocityBufferDelaySlider);
        CombatSettings.setVelocityBufferDelaySlider(delay);
    }

    static void populateAttackDelaySettings(WidgetSurface.Frame gui) {
        float cooldown = gui.slider("Max Cooldown", CombatSettings.attackDelayMaxCooldownSlider(),
                CombatSettings.attackDelayMaxCooldown() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 9.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 9.0f),
                CombatSettings::setAttackDelayMaxCooldownSlider);
        CombatSettings.setAttackDelayMaxCooldownSlider(cooldown);
    }

    static void populateAutoHeadSettings(WidgetSurface.Frame gui) {
        float delay = gui.slider("Heal Delay", CombatSettings.autoHeadDelaySlider(),
                CombatSettings.autoHeadDelayMs() + "ms",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 3000.0f, "ms"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 3000.0f),
                CombatSettings::setAutoHeadDelaySlider);
        CombatSettings.setAutoHeadDelaySlider(delay);
        float health = gui.slider("Health", CombatSettings.autoHeadHealthSlider(),
                CombatSettings.autoHeadHealthPercent() + "%",
                value -> SliderValueFormat.intRangeLabel(value, 5.0f, 95.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 5.0f, 95.0f),
                CombatSettings::setAutoHeadHealthSlider);
        CombatSettings.setAutoHeadHealthSlider(health);
    }

    static void populateAutoBlockSettings(WidgetSurface.Frame gui) {
        CombatSettings.setAutoBlockRequireAttackKey(gui.controlledCheckbox("Require Attack Key",
                CombatSettings.autoBlockRequireAttackKey(), CombatSettings::setAutoBlockRequireAttackKey));
        float range = gui.slider("Block Range", CombatSettings.autoBlockRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", CombatSettings.autoBlockRange()),
                value -> SliderValueFormat.decimalRangeLabel(value, 3.0f, 8.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 3.0f, 8.0f),
                CombatSettings::setAutoBlockRangeSlider);
        CombatSettings.setAutoBlockRangeSlider(range);
    }

    static void populateFakeLagSettings(WidgetSurface.Frame gui) {
        CombatSettings.setFakeLagMode(gui.combo("Mode", CombatSettings.fakeLagMode(),
                "Dynamic, Constant", CombatSettings::setFakeLagMode));
        float range = gui.slider("Range", CombatSettings.fakeLagRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", CombatSettings.fakeLagRange()),
                value -> SliderValueFormat.decimalRangeLabel(value, 0.0f, 10.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 10.0f),
                CombatSettings::setFakeLagRangeSlider);
        CombatSettings.setFakeLagRangeSlider(range);
        float delay = gui.slider("Delay", CombatSettings.fakeLagDelaySlider(),
                CombatSettings.fakeLagDelayMs() + "ms",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 1000.0f, "ms"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 1000.0f),
                CombatSettings::setFakeLagDelaySlider);
        CombatSettings.setFakeLagDelaySlider(delay);
        float recoil = gui.slider("Recoil Time", CombatSettings.fakeLagRecoilSlider(),
                CombatSettings.fakeLagRecoilMs() + "ms",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 1000.0f, "ms"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 1000.0f),
                CombatSettings::setFakeLagRecoilSlider);
        CombatSettings.setFakeLagRecoilSlider(recoil);
        CombatSettings.setFakeLagFlushEntity(gui.controlledCheckbox("Flush Entity Interact",
                CombatSettings.fakeLagFlushEntity(), CombatSettings::setFakeLagFlushEntity));
        CombatSettings.setFakeLagFlushBlock(gui.controlledCheckbox("Flush Block Interact",
                CombatSettings.fakeLagFlushBlock(), CombatSettings::setFakeLagFlushBlock));
        CombatSettings.setFakeLagFlushAction(gui.controlledCheckbox("Flush Action",
                CombatSettings.fakeLagFlushAction(), CombatSettings::setFakeLagFlushAction));
        CombatSettings.setFakeLagDebug(gui.controlledCheckbox("Debug",
                CombatSettings.fakeLagDebug(), CombatSettings::setFakeLagDebug));
    }

    static void populateCriticalsSettings(WidgetSurface.Frame gui) {
        CombatSettings.setCriticalsMode(gui.combo("Mode", CombatSettings.criticalsMode(),
                "Packet, Heypixel", CombatSettings::setCriticalsMode));
        String mode = CombatSettings.criticalsMode();
        gui.visible("criticals-packet-settings", CombatSettings.CRITICALS_MODE_PACKET.equals(mode),
                CombatSettingsPage::populateCriticalsPacketSettings);
        gui.visible("criticals-heypixel-settings", CombatSettings.CRITICALS_MODE_HEYPIXEL.equals(mode),
                CombatSettingsPage::populateCriticalsHeypixelSettings);
    }

    private static void populateCriticalsPacketSettings(WidgetSurface.Frame gui) {
        CombatSettings.setCriticalsGroundOnly(gui.controlledCheckbox("Ground Only",
                CombatSettings.criticalsGroundOnly(), CombatSettings::setCriticalsGroundOnly));
    }

    private static void populateCriticalsHeypixelSettings(WidgetSurface.Frame gui) {
        float range = gui.slider("Critical Range", CombatSettings.criticalsRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", CombatSettings.criticalsRange()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 3.2f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 3.2f),
                CombatSettings::setCriticalsRangeSlider);
        CombatSettings.setCriticalsRangeSlider(range);
        CombatSettings.setCriticalsAutoJump(gui.controlledCheckbox("Auto Jump",
                CombatSettings.criticalsAutoJump(), CombatSettings::setCriticalsAutoJump));
        CombatSettings.setCriticalsSkipTicks(gui.controlledCheckbox("Skip Ticks",
                CombatSettings.criticalsSkipTicks(), CombatSettings::setCriticalsSkipTicks));
    }

    static void populateAutoTotemSettings(WidgetSurface.Frame gui) {
        float health = gui.slider("Health", CombatSettings.autoTotemHealthSlider(),
                Math.round(CombatSettings.autoTotemHealth()) + " HP",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 20.0f, " HP"),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setAutoTotemHealthSlider);
        CombatSettings.setAutoTotemHealthSlider(health);
    }

    static void populateAutoHealSettings(WidgetSurface.Frame gui) {
        float health = gui.slider("Health", CombatSettings.autoHealHealthSlider(),
                Math.round(CombatSettings.autoHealHealth()) + " HP",
                value -> SliderValueFormat.intRangeLabel(value, 4.0f, 20.0f, " HP"),
                value -> SliderValueFormat.parseRangeSlider(value, 4.0f, 20.0f),
                CombatSettings::setAutoHealHealthSlider);
        CombatSettings.setAutoHealHealthSlider(health);
        float delay = gui.slider("Delay", CombatSettings.autoHealDelaySlider(),
                CombatSettings.autoHealDelayTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 20.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setAutoHealDelaySlider);
        CombatSettings.setAutoHealDelaySlider(delay);
        CombatSettings.setAutoHealMode(gui.combo("Mode", CombatSettings.autoHealMode(),
                "Any, Golden Apple, Potion", CombatSettings::setAutoHealMode));
        CombatSettings.setAutoHealIncludeAbsorption(gui.controlledCheckbox("Absorption",
                CombatSettings.autoHealIncludeAbsorption(), CombatSettings::setAutoHealIncludeAbsorption));
        CombatSettings.setAutoHealRestoreSlot(gui.controlledCheckbox("Restore Slot",
                CombatSettings.autoHealRestoreSlot(), CombatSettings::setAutoHealRestoreSlot));
    }

    static void populateAutoArmorSettings(WidgetSurface.Frame gui) {
        float delay = gui.slider("Delay", CombatSettings.autoArmorDelaySlider(),
                CombatSettings.autoArmorDelayTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 20.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setAutoArmorDelaySlider);
        CombatSettings.setAutoArmorDelaySlider(delay);
        CombatSettings.setAutoArmorPreferDurability(gui.controlledCheckbox("Prefer Durability",
                CombatSettings.autoArmorPreferDurability(), CombatSettings::setAutoArmorPreferDurability));
        CombatSettings.setAutoArmorAllowBindingCurse(gui.controlledCheckbox("Binding Curse",
                CombatSettings.autoArmorAllowBindingCurse(), CombatSettings::setAutoArmorAllowBindingCurse));
    }

    static void populateAimAssistSettings(WidgetSurface.Frame gui) {
        CombatSettings.setAimAssistPriority(gui.combo("Priority", CombatSettings.aimAssistPriority(),
                "Crosshair, Distance, Low Health", CombatSettings::setAimAssistPriority));
        float strength = gui.slider("Strength", CombatSettings.aimAssistStrengthSlider(),
                Math.round(CombatSettings.aimAssistStrength()) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 100.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 100.0f),
                CombatSettings::setAimAssistStrengthSlider);
        CombatSettings.setAimAssistStrengthSlider(strength);
        float smooth = gui.slider("Smooth", CombatSettings.aimAssistSmoothSlider(),
                Math.round(CombatSettings.aimAssistSmooth()) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 100.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 100.0f),
                CombatSettings::setAimAssistSmoothSlider);
        CombatSettings.setAimAssistSmoothSlider(smooth);
        float range = gui.slider("Range", CombatSettings.aimAssistRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.0f", CombatSettings.aimAssistRange()),
                value -> SliderValueFormat.intRangeLabel(value, 2.0f, 90.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 2.0f, 90.0f),
                CombatSettings::setAimAssistRangeSlider);
        CombatSettings.setAimAssistRangeSlider(range);
    }

    static void populateProjectileAimSettings(WidgetSurface.Frame gui) {
        CombatSettings.setProjectileAimPriority(gui.combo("Priority", CombatSettings.projectileAimPriority(),
                "Crosshair, Distance, Low Health", CombatSettings::setProjectileAimPriority));
        float range = gui.slider("Range", CombatSettings.projectileAimRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.0f", CombatSettings.projectileAimRange()),
                value -> SliderValueFormat.intRangeLabel(value, 2.0f, 90.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 2.0f, 90.0f),
                CombatSettings::setProjectileAimRangeSlider);
        CombatSettings.setProjectileAimRangeSlider(range);
        float smooth = gui.slider("Smooth", CombatSettings.projectileAimSmoothSlider(),
                Math.round(CombatSettings.projectileAimSmooth()) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 100.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 100.0f),
                CombatSettings::setProjectileAimSmoothSlider);
        CombatSettings.setProjectileAimSmoothSlider(smooth);
        float prediction = gui.slider("Prediction", CombatSettings.projectileAimPredictionSlider(),
                Math.round(CombatSettings.projectileAimPrediction()) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 150.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 150.0f),
                CombatSettings::setProjectileAimPredictionSlider);
        CombatSettings.setProjectileAimPredictionSlider(prediction);
    }

    static void populateGunAimSettings(WidgetSurface.Frame gui) {
        CombatSettings.setGunAimPriority(gui.combo("Priority", CombatSettings.gunAimPriority(),
                "Crosshair, Distance, Low Health", CombatSettings::setGunAimPriority));
        float range = gui.slider("Range", CombatSettings.gunAimRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.0f", CombatSettings.gunAimRange()),
                value -> SliderValueFormat.intRangeLabel(value, 2.0f, 90.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 2.0f, 90.0f),
                CombatSettings::setGunAimRangeSlider);
        CombatSettings.setGunAimRangeSlider(range);
        float smooth = gui.slider("Smooth", CombatSettings.gunAimSmoothSlider(),
                Math.round(CombatSettings.gunAimSmooth()) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 100.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 100.0f),
                CombatSettings::setGunAimSmoothSlider);
        CombatSettings.setGunAimSmoothSlider(smooth);
        float prediction = gui.slider("Prediction", CombatSettings.gunAimPredictionSlider(),
                Math.round(CombatSettings.gunAimPrediction()) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 150.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 150.0f),
                CombatSettings::setGunAimPredictionSlider);
        CombatSettings.setGunAimPredictionSlider(prediction);
        CombatSettings.setGunAimRequireUseKey(gui.controlledCheckbox("Require Fire Key",
                CombatSettings.gunAimRequireUseKey(), CombatSettings::setGunAimRequireUseKey));
        CombatSettings.setGunAimItemIds(gui.inputText("Item IDs",
                CombatSettings.gunAimItemIds(), CombatSettings::setGunAimItemIds));
    }

    static void populateRagebotMain(WidgetSurface.Frame gui) {
        CombatSettings.setRagebotEnabled(gui.controlledCheckboxWithSettings("Enable",
                CombatSettings.ragebotEnabled(), CombatSettingsPage::populateRagebotEnableSettings,
                CombatSettings::setRagebotEnabled));
        CombatSettings.setRagebotSilentAim(gui.controlledCheckboxWithSettings("Silent Aim",
                CombatSettings.ragebotSilentAim(), CombatSettingsPage::populateRagebotSilentAimSettings,
                CombatSettings::setRagebotSilentAim));
        CombatSettings.setRagebotAutomaticFire(gui.controlledCheckbox("Automatic Fire",
                CombatSettings.ragebotAutomaticFire(), CombatSettings::setRagebotAutomaticFire));
        CombatSettings.setRagebotDoubleTap(gui.controlledCheckbox("Double Tap",
                CombatSettings.ragebotDoubleTap(), CombatSettings::setRagebotDoubleTap));
        CombatSettings.setRagebotHideShots(gui.controlledCheckbox("Hide Shots",
                CombatSettings.ragebotHideShots(), CombatSettings::setRagebotHideShots));
        float fov = gui.slider("Field Of View", CombatSettings.ragebotFovSlider(),
                String.format(java.util.Locale.ROOT, "%.0f", CombatSettings.ragebotFov()),
                value -> SliderValueFormat.intRangeLabel(value, 30.0f, 360.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 30.0f, 360.0f),
                CombatSettings::setRagebotFovSlider);
        CombatSettings.setRagebotFovSlider(fov);
    }

    private static void populateRagebotEnableSettings(WidgetSurface.Frame gui) {
        CombatSettings.setRagebotRefineShot(gui.controlledCheckbox("Refine Shot",
                CombatSettings.ragebotRefineShot(), CombatSettings::setRagebotRefineShot));
    }

    private static void populateRagebotSilentAimSettings(WidgetSurface.Frame gui) {
        CombatSettings.setRagebotForceSilentAim(gui.controlledCheckbox("Force Silent Aim",
                CombatSettings.ragebotForceSilentAim(), CombatSettings::setRagebotForceSilentAim));
    }

    static void populateRagebotSelection(WidgetSurface.Frame gui) {
        CombatSettings.setRagebotPriority(gui.combo("Target", CombatSettings.ragebotPriority(),
                "Distance, Hitchance, Low Health", CombatSettings::setRagebotPriority));
        int hitboxes = gui.multiSelect("Hitboxes", CombatSettings.ragebotHitboxesMask(),
                "Head, Chest, Stomach, Arms, Legs", CombatSettings::setRagebotHitboxesMask);
        CombatSettings.setRagebotHitboxesMask(hitboxes);
        CombatSettings.setRagebotAutoStop(gui.controlledCheckbox("Auto Stop",
                CombatSettings.ragebotAutoStop(), CombatSettings::setRagebotAutoStop));
        CombatSettings.setRagebotQuickScope(gui.controlledCheckbox("Auto Scope",
                CombatSettings.ragebotQuickScope(), CombatSettings::setRagebotQuickScope));
    }

    static void populateRagebotOther(WidgetSurface.Frame gui) {
        CombatSettings.setRagebotRemoveRecoil(gui.controlledCheckbox("Remove Recoil",
                CombatSettings.ragebotRemoveRecoil(), CombatSettings::setRagebotRemoveRecoil));
        CombatSettings.setRagebotRemoveSpread(gui.controlledCheckbox("Remove Spread",
                CombatSettings.ragebotRemoveSpread(), CombatSettings::setRagebotRemoveSpread));
    }

    static void populateRagebotAntiAim(WidgetSurface.Frame gui) {
        CombatSettings.setRagebotAntiAimEnabled(gui.controlledCheckbox("Enable",
                CombatSettings.ragebotAntiAimEnabled(), CombatSettings::setRagebotAntiAimEnabled));
        CombatSettings.setRagebotAntiAimPitch(gui.combo("Pitch", CombatSettings.ragebotAntiAimPitch(),
                "Off, Up, Down, Zero, Jitter, Custom", CombatSettings::setRagebotAntiAimPitch));
        gui.mode("anti-aim-pitch-custom", CombatSettings.ragebotAntiAimPitch(),
                CombatSettings.RAGEBOT_PITCH_CUSTOM, custom -> {
                    float pitch = custom.slider("Custom Pitch", CombatSettings.ragebotAntiAimCustomPitchSlider(),
                            String.valueOf(Math.round(CombatSettings.ragebotAntiAimCustomPitch())),
                            value -> SliderValueFormat.intRangeLabel(value, -90.0f, 90.0f, ""),
                            value -> SliderValueFormat.parseRangeSlider(value, -90.0f, 90.0f),
                            CombatSettings::setRagebotAntiAimCustomPitchSlider);
                    CombatSettings.setRagebotAntiAimCustomPitchSlider(pitch);
                });
        CombatSettings.setRagebotAntiAimYaw(gui.combo("Yaw", CombatSettings.ragebotAntiAimYaw(),
                "Off, At Targets, Local View", CombatSettings::setRagebotAntiAimYaw));
        float yawBase = gui.slider("Base", CombatSettings.ragebotAntiAimYawBaseSlider(),
                String.valueOf(Math.round(CombatSettings.ragebotAntiAimYawBase())),
                value -> SliderValueFormat.intRangeLabel(value, -180.0f, 180.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, -180.0f, 180.0f),
                CombatSettings::setRagebotAntiAimYawBaseSlider);
        CombatSettings.setRagebotAntiAimYawBaseSlider(yawBase);
        CombatSettings.setRagebotAntiAimYawModifier(gui.combo("Modifier",
                CombatSettings.ragebotAntiAimYawModifier(),
                "Disable, Center, Offset, Random, Spin, 3-way, 5-way",
                CombatSettings::setRagebotAntiAimYawModifier));
        gui.visible("anti-aim-yaw-offset",
                !CombatSettings.RAGEBOT_YAW_MODIFIER_DISABLE.equals(
                        CombatSettings.ragebotAntiAimYawModifier()), offset -> {
                    float yawOffset = offset.slider("Offset", CombatSettings.ragebotAntiAimYawOffsetSlider(),
                            String.valueOf(Math.round(CombatSettings.ragebotAntiAimYawOffset())),
                            value -> SliderValueFormat.intRangeLabel(value, -180.0f, 180.0f, ""),
                            value -> SliderValueFormat.parseRangeSlider(value, -180.0f, 180.0f),
                            CombatSettings::setRagebotAntiAimYawOffsetSlider);
                    CombatSettings.setRagebotAntiAimYawOffsetSlider(yawOffset);
                });
    }

    static void populateAutoPearlSettings(WidgetSurface.Frame gui) {
        CombatSettings.setPearlAimEnabled(gui.controlledCheckbox("Pearl Aim",
                CombatSettings.pearlAimEnabled(), CombatSettings::setPearlAimEnabled));
        CombatSettings.setPearlAimPriority(gui.combo("Priority", CombatSettings.pearlAimPriority(),
                "Crosshair, Distance, Low Health", CombatSettings::setPearlAimPriority));
        float distance = gui.slider("Distance", CombatSettings.autoPearlDistanceSlider(),
                String.format(java.util.Locale.ROOT, "%.0fm", CombatSettings.autoPearlDistance()),
                value -> SliderValueFormat.intRangeLabel(value, 4.0f, 64.0f, "m"),
                value -> SliderValueFormat.parseRangeSlider(value, 4.0f, 64.0f),
                CombatSettings::setAutoPearlDistanceSlider);
        CombatSettings.setAutoPearlDistanceSlider(distance);
        float range = gui.slider("Aim Range", CombatSettings.pearlAimRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.0f", CombatSettings.pearlAimRange()),
                value -> SliderValueFormat.intRangeLabel(value, 2.0f, 90.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 2.0f, 90.0f),
                CombatSettings::setPearlAimRangeSlider);
        CombatSettings.setPearlAimRangeSlider(range);
        float smooth = gui.slider("Smooth", CombatSettings.pearlAimSmoothSlider(),
                Math.round(CombatSettings.pearlAimSmooth()) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 100.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 100.0f),
                CombatSettings::setPearlAimSmoothSlider);
        CombatSettings.setPearlAimSmoothSlider(smooth);
        float prediction = gui.slider("Prediction", CombatSettings.pearlAimPredictionSlider(),
                Math.round(CombatSettings.pearlAimPrediction()) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 150.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 150.0f),
                CombatSettings::setPearlAimPredictionSlider);
        CombatSettings.setPearlAimPredictionSlider(prediction);
        float delay = gui.slider("Delay", CombatSettings.autoPearlDelaySlider(),
                CombatSettings.autoPearlDelayTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 5.0f, 60.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 5.0f, 60.0f),
                CombatSettings::setAutoPearlDelaySlider);
        CombatSettings.setAutoPearlDelaySlider(delay);
        CombatSettings.setAutoPearlSilentRotate(gui.controlledCheckbox("Silent Rotate",
                CombatSettings.autoPearlSilentRotate(), CombatSettings::setAutoPearlSilentRotate));
        CombatSettings.setAutoPearlRestoreSlot(gui.controlledCheckbox("Restore Slot",
                CombatSettings.autoPearlRestoreSlot(), CombatSettings::setAutoPearlRestoreSlot));
    }

    static void populateTriggerBotSettings(WidgetSurface.Frame gui) {
        float cps = gui.slider("CPS", CombatSettings.triggerBotCpsSlider(),
                String.valueOf(Math.round(CombatSettings.triggerBotCps())),
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 20.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setTriggerBotCpsSlider);
        CombatSettings.setTriggerBotCpsSlider(cps);
        float range = gui.slider("Range", CombatSettings.triggerBotRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.1fm", CombatSettings.triggerBotRange()),
                value -> SliderValueFormat.decimalRangeLabel(value, 3.0f, 6.0f, 1) + "m",
                value -> SliderValueFormat.parseRangeSlider(value, 3.0f, 6.0f),
                CombatSettings::setTriggerBotRangeSlider);
        CombatSettings.setTriggerBotRangeSlider(range);
        CombatSettings.setTriggerBotOnlyWeapon(gui.controlledCheckbox("Only Weapon",
                CombatSettings.triggerBotOnlyWeapon(), CombatSettings::setTriggerBotOnlyWeapon));
        CombatSettings.setTriggerBotRequireAttackKey(gui.controlledCheckbox("Require Attack Key",
                CombatSettings.triggerBotRequireAttackKey(), CombatSettings::setTriggerBotRequireAttackKey));
    }

    static void populateFastUseSettings(WidgetSurface.Frame gui) {
        float charge = gui.slider("Charge", CombatSettings.fastUseChargeSlider(),
                CombatSettings.fastUseChargeTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 20.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setFastUseChargeSlider);
        CombatSettings.setFastUseChargeSlider(charge);
        float delay = gui.slider("Delay", CombatSettings.fastUseDelaySlider(),
                CombatSettings.fastUseDelayTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 10.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 10.0f),
                CombatSettings::setFastUseDelaySlider);
        CombatSettings.setFastUseDelaySlider(delay);
        float packets = gui.slider("Packets", CombatSettings.fastUsePacketsSlider(),
                CombatSettings.fastUsePackets() + "",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 25.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 25.0f),
                CombatSettings::setFastUsePacketsSlider);
        CombatSettings.setFastUsePacketsSlider(packets);
        CombatSettings.setFastUsePacketBoost(gui.controlledCheckbox("Packet Boost",
                CombatSettings.fastUsePacketBoost(), CombatSettings::setFastUsePacketBoost));
        CombatSettings.setFastUseAutoUse(gui.controlledCheckbox("Auto Use",
                CombatSettings.fastUseAutoUse(), CombatSettings::setFastUseAutoUse));
        int itemsMask = fastUseItemsMask();
        int nextItemsMask = gui.multiSelect("Items", itemsMask, "Bow, Trident, Crossbow, Throwables");
        CombatSettings.setFastUseBow((nextItemsMask & 1) != 0);
        CombatSettings.setFastUseTrident((nextItemsMask & (1 << 1)) != 0);
        CombatSettings.setFastUseCrossbow((nextItemsMask & (1 << 2)) != 0);
        CombatSettings.setFastUseThrowables((nextItemsMask & (1 << 3)) != 0);
    }

    private static int fastUseItemsMask() {
        int mask = 0;
        if (CombatSettings.fastUseBow()) {
            mask |= 1;
        }
        if (CombatSettings.fastUseTrident()) {
            mask |= 1 << 1;
        }
        if (CombatSettings.fastUseCrossbow()) {
            mask |= 1 << 2;
        }
        if (CombatSettings.fastUseThrowables()) {
            mask |= 1 << 3;
        }
        return mask;
    }

    static void populateAutoClickerSettings(WidgetSurface.Frame gui) {
        float cps = gui.slider("CPS", CombatSettings.autoClickerCpsSlider(),
                String.valueOf(Math.round(CombatSettings.autoClickerCps())),
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 20.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setAutoClickerCpsSlider);
        CombatSettings.setAutoClickerCpsSlider(cps);
    }
}
