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

import com.xerion.forge.config.VisualSettings;

final class MovementSettingsPage {
    private MovementSettingsPage() {
    }

    static void populateMovementFlight(WidgetSurface.Frame gui) {
        VisualSettings.setFlightEnabled(gui.controlledCheckboxWithSettings("Flight",
                VisualSettings.flightEnabled(), MovementSettingsPage::populateFlightSettings,
                VisualSettings::setFlightEnabled));
        VisualSettings.setElytraFlyEnabled(gui.controlledCheckboxWithSettings("Elytra Fly",
                VisualSettings.elytraFlyEnabled(), MovementSettingsPage::populateElytraFlySettings,
                VisualSettings::setElytraFlyEnabled));
        VisualSettings.setBoatFlyEnabled(gui.controlledCheckboxWithSettings("Boat Fly",
                VisualSettings.boatFlyEnabled(), MovementSettingsPage::populateBoatFlySettings,
                VisualSettings::setBoatFlyEnabled));
    }

    static void populateMovementVehicle(WidgetSurface.Frame gui) {
        VisualSettings.setEntitySpeedEnabled(gui.controlledCheckboxWithSettings("Entity Speed",
                VisualSettings.entitySpeedEnabled(), MovementSettingsPage::populateEntitySpeedSettings,
                VisualSettings::setEntitySpeedEnabled));
    }

    static void populateMovementEnvironment(WidgetSurface.Frame gui) {
        VisualSettings.setStepEnabled(gui.controlledCheckboxWithSettings("Step",
                VisualSettings.stepEnabled(), MovementSettingsPage::populateStepSettings,
                VisualSettings::setStepEnabled));
        VisualSettings.setLongJumpEnabled(gui.controlledCheckboxWithSettings("Long Jump",
                VisualSettings.longJumpEnabled(), MovementSettingsPage::populateLongJumpSettings,
                VisualSettings::setLongJumpEnabled));
        VisualSettings.setHighJumpEnabled(gui.controlledCheckboxWithSettings("High Jump",
                VisualSettings.highJumpEnabled(), MovementSettingsPage::populateHighJumpSettings,
                VisualSettings::setHighJumpEnabled));
        VisualSettings.setJesusEnabled(gui.controlledCheckboxWithSettings("Jesus",
                VisualSettings.jesusEnabled(), MovementSettingsPage::populateJesusSettings,
                VisualSettings::setJesusEnabled));
        VisualSettings.setSpiderEnabled(gui.controlledCheckboxWithSettings("Spider",
                VisualSettings.spiderEnabled(), MovementSettingsPage::populateSpiderSettings,
                VisualSettings::setSpiderEnabled));
        VisualSettings.setNoFallEnabled(gui.controlledCheckboxWithSettings("No Fall",
                VisualSettings.noFallEnabled(), MovementSettingsPage::populateNoFallSettings,
                VisualSettings::setNoFallEnabled));
        VisualSettings.setAntiVoidEnabled(gui.controlledCheckboxWithSettings("Anti Void",
                VisualSettings.antiVoidEnabled(), MovementSettingsPage::populateAntiVoidSettings,
                VisualSettings::setAntiVoidEnabled));
    }

    static void populateMovementExploit(WidgetSurface.Frame gui) {
        VisualSettings.setPhaseEnabled(gui.controlledCheckboxWithSettings("Phase",
                VisualSettings.phaseEnabled(), MovementSettingsPage::populatePhaseSettings,
                VisualSettings::setPhaseEnabled));
        VisualSettings.setBlinkEnabled(gui.controlledCheckboxWithSettings("Blink",
                VisualSettings.blinkEnabled(), MovementSettingsPage::populateBlinkSettings,
                VisualSettings::setBlinkEnabled));
        VisualSettings.setStuckEnabled(gui.controlledCheckbox("Stuck",
                VisualSettings.stuckEnabled(), VisualSettings::setStuckEnabled));
        VisualSettings.setMovementFixEnabled(gui.controlledCheckboxWithSettings("Movement Fix",
                VisualSettings.movementFixEnabled(), MovementSettingsPage::populateMovementFixSettings,
                VisualSettings::setMovementFixEnabled));
        VisualSettings.setFastWebEnabled(gui.controlledCheckboxWithSettings("Fast Web",
                VisualSettings.fastWebEnabled(), MovementSettingsPage::populateFastWebSettings,
                VisualSettings::setFastWebEnabled));
    }

    static void populateMovementFixSettings(WidgetSurface.Frame gui) {
        VisualSettings.setMovementFixMode(gui.combo("Mode", VisualSettings.movementFixMode(),
                "Normal, SprintOnly", VisualSettings::setMovementFixMode));
        gui.visible("movement-fix-sprint-only", "SprintOnly".equals(VisualSettings.movementFixMode()),
                block -> VisualSettings.setMovementFixPacketOnly(block.controlledCheckbox("Packet Only",
                        VisualSettings.movementFixPacketOnly(), VisualSettings::setMovementFixPacketOnly)));
    }

    static void populateFastWebSettings(WidgetSurface.Frame gui) {
        gui.infoRow("Mode", "OpenZen FastWeb");
    }

    static void populateKeepSprintSettings(WidgetSurface.Frame gui) {
        float speed = gui.slider("Speed", VisualSettings.keepSprintScaleSlider(),
                String.format(java.util.Locale.ROOT, "%.2f", VisualSettings.keepSprintScale()),
                value -> SliderValueFormat.decimalLabel(value, 0.0f, 2.0f),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 2.0f),
                VisualSettings::setKeepSprintScaleSlider);
        VisualSettings.setKeepSprintScaleSlider(speed);
        float slowdown = gui.slider("Slowdown", VisualSettings.keepSprintSlowdownSlider(),
                Math.round(VisualSettings.keepSprintSlowdown()) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 100.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 100.0f),
                VisualSettings::setKeepSprintSlowdownSlider);
        VisualSettings.setKeepSprintSlowdownSlider(slowdown);
        VisualSettings.setKeepSprintGroundOnly(gui.controlledCheckbox("Ground Only",
                VisualSettings.keepSprintGroundOnly(), VisualSettings::setKeepSprintGroundOnly));
        VisualSettings.setKeepSprintPrediction(gui.controlledCheckbox("Prediction",
                VisualSettings.keepSprintPrediction(), VisualSettings::setKeepSprintPrediction));
        VisualSettings.setKeepSprintReachOnly(gui.controlledCheckbox("Reach Only",
                VisualSettings.keepSprintReachOnly(), VisualSettings::setKeepSprintReachOnly));
    }

    static void populateCubeCraftSpeedSettings(WidgetSurface.Frame gui) {
        boolean fast = "CubeCraftFast".equals(VisualSettings.speedMode());
        gui.visible("speed-cubecraft-fast", fast, block -> {
            float speed = block.slider("Speed", VisualSettings.speedCubeCraftFastSpeedSlider(),
                    String.format(java.util.Locale.ROOT, "%.1f", VisualSettings.speedCubeCraftFastSpeed()),
                    value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 5.0f, 1),
                    value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 5.0f),
                    VisualSettings::setSpeedCubeCraftFastSpeedSlider);
            VisualSettings.setSpeedCubeCraftFastSpeedSlider(speed);
            VisualSettings.setSpeedCubeCraftLagBackCheck(block.controlledCheckbox("LagBack Check",
                    VisualSettings.speedCubeCraftLagBackCheck(),
                    VisualSettings::setSpeedCubeCraftLagBackCheck));
        });
        gui.visible("speed-cubecraft-lowhop", !fast, block ->
                VisualSettings.setSpeedCubeCraftLowHopGlide(block.controlledCheckbox("Glide",
                        VisualSettings.speedCubeCraftLowHopGlide(),
                        VisualSettings::setSpeedCubeCraftLowHopGlide)));
    }

    static void populateFullSpeedSettings(WidgetSurface.Frame gui) {
        float ground = gui.slider("Ground", VisualSettings.speedGroundMultiplierSlider(),
                String.format(java.util.Locale.ROOT, "%.2f", VisualSettings.speedGroundMultiplier()),
                value -> SliderValueFormat.decimalLabel(value, 0.0f, 2.0f),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 2.0f),
                VisualSettings::setSpeedGroundMultiplierSlider);
        VisualSettings.setSpeedGroundMultiplierSlider(ground);
        float air = gui.slider("Air", VisualSettings.speedAirMultiplierSlider(),
                String.format(java.util.Locale.ROOT, "%.2f", VisualSettings.speedAirMultiplier()),
                value -> SliderValueFormat.decimalLabel(value, 0.0f, 2.0f),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 2.0f),
                VisualSettings::setSpeedAirMultiplierSlider);
        VisualSettings.setSpeedAirMultiplierSlider(air);
        VisualSettings.setSpeedAutoJump(gui.controlledCheckbox("Auto Jump",
                VisualSettings.speedAutoJump(), VisualSettings::setSpeedAutoJump));
        VisualSettings.setSpeedOnlySprint(gui.controlledCheckbox("Only Sprint",
                VisualSettings.speedOnlySprint(), VisualSettings::setSpeedOnlySprint));
    }

    static void populateAntiVoidSettings(WidgetSurface.Frame gui) {
        VisualSettings.setAntiVoidMode(gui.combo("Mode", VisualSettings.antiVoidMode(),
                "Setback, Freeze", VisualSettings::setAntiVoidMode));
        float fall = gui.slider("Fall Distance", VisualSettings.antiVoidFallDistanceSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", VisualSettings.antiVoidFallDistance()),
                value -> SliderValueFormat.decimalRangeLabel(value, 2.0f, 20.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 2.0f, 20.0f),
                VisualSettings::setAntiVoidFallDistanceSlider);
        VisualSettings.setAntiVoidFallDistanceSlider(fall);
    }

    static void populateFlightSettings(WidgetSurface.Frame gui) {
        VisualSettings.setFlightMode(gui.combo("Mode", VisualSettings.flightMode(),
                "Vanilla, Glide, CubeCraftAirWalk, CubeCraftFast, CubeCraftPingSpoof",
                VisualSettings::setFlightMode));
        float horizontal = gui.slider("Horizontal", VisualSettings.flightHorizontalSpeedSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", VisualSettings.flightHorizontalSpeed()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 20.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                VisualSettings::setFlightHorizontalSpeedSlider);
        VisualSettings.setFlightHorizontalSpeedSlider(horizontal);
        float vertical = gui.slider("Vertical", VisualSettings.flightVerticalSpeedSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", VisualSettings.flightVerticalSpeed()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 12.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 12.0f),
                VisualSettings::setFlightVerticalSpeedSlider);
        VisualSettings.setFlightVerticalSpeedSlider(vertical);
        gui.mode("flight-mode", VisualSettings.flightMode(), "Glide", block -> {
            float glide = block.slider("Glide", VisualSettings.flightGlideSlider(),
                    String.format(java.util.Locale.ROOT, "%.2f", VisualSettings.flightGlide()),
                    value -> SliderValueFormat.decimalLabel(value, 0.0f, 1.0f),
                    value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 1.0f),
                    VisualSettings::setFlightGlideSlider);
            VisualSettings.setFlightGlideSlider(glide);
        });
        gui.mode("flight-mode", VisualSettings.flightMode(), "CubeCraftAirWalk", block -> {
            float speed = block.slider("Air Walk Speed", VisualSettings.flightCubeCraftAirWalkSpeedSlider(),
                    String.format(java.util.Locale.ROOT, "%.2f", VisualSettings.flightCubeCraftAirWalkSpeed()),
                    value -> SliderValueFormat.decimalLabel(value, 0.1f, 1.0f),
                    value -> SliderValueFormat.parseRangeSlider(value, 0.1f, 1.0f),
                    VisualSettings::setFlightCubeCraftAirWalkSpeedSlider);
            VisualSettings.setFlightCubeCraftAirWalkSpeedSlider(speed);
        });
        gui.mode("flight-mode", VisualSettings.flightMode(), "CubeCraftFast", block -> {
            float ccHorizontal = block.slider("Horizontal", VisualSettings.flightCubeCraftSpeedSlider(),
                    String.format(java.util.Locale.ROOT, "%.1f", VisualSettings.flightCubeCraftSpeed()),
                    value -> SliderValueFormat.decimalRangeLabel(value, 0.1f, 10.0f, 1),
                    value -> SliderValueFormat.parseRangeSlider(value, 0.1f, 10.0f),
                    VisualSettings::setFlightCubeCraftSpeedSlider);
            VisualSettings.setFlightCubeCraftSpeedSlider(ccHorizontal);
            float ccVertical = block.slider("Vertical", VisualSettings.flightCubeCraftVerticalSlider(),
                    String.format(java.util.Locale.ROOT, "%.1f", VisualSettings.flightCubeCraftVertical()),
                    value -> SliderValueFormat.decimalRangeLabel(value, 0.1f, 5.0f, 1),
                    value -> SliderValueFormat.parseRangeSlider(value, 0.1f, 5.0f),
                    VisualSettings::setFlightCubeCraftVerticalSlider);
            VisualSettings.setFlightCubeCraftVerticalSlider(ccVertical);
        });
        gui.mode("flight-mode", VisualSettings.flightMode(), "CubeCraftPingSpoof", block -> {
            float psHorizontal = block.slider("Horizontal", VisualSettings.flightCubeCraftSpeedSlider(),
                    String.format(java.util.Locale.ROOT, "%.1f", VisualSettings.flightCubeCraftSpeed()),
                    value -> SliderValueFormat.decimalRangeLabel(value, 0.1f, 10.0f, 1),
                    value -> SliderValueFormat.parseRangeSlider(value, 0.1f, 10.0f),
                    VisualSettings::setFlightCubeCraftSpeedSlider);
            VisualSettings.setFlightCubeCraftSpeedSlider(psHorizontal);
            float psVertical = block.slider("Vertical", VisualSettings.flightCubeCraftVerticalSlider(),
                    String.format(java.util.Locale.ROOT, "%.1f", VisualSettings.flightCubeCraftVertical()),
                    value -> SliderValueFormat.decimalRangeLabel(value, 0.1f, 5.0f, 1),
                    value -> SliderValueFormat.parseRangeSlider(value, 0.1f, 5.0f),
                    VisualSettings::setFlightCubeCraftVerticalSlider);
            VisualSettings.setFlightCubeCraftVerticalSlider(psVertical);
            float reboost = block.slider("Reboost Ticks", VisualSettings.flightCubeCraftReboostSlider(),
                    VisualSettings.flightCubeCraftReboostTicks() + "t",
                    value -> SliderValueFormat.intRangeLabel(value, 10.0f, 50.0f, "t"),
                    value -> SliderValueFormat.parseRangeSlider(value, 10.0f, 50.0f),
                    VisualSettings::setFlightCubeCraftReboostSlider);
            VisualSettings.setFlightCubeCraftReboostSlider(reboost);
            VisualSettings.setFlightCubeCraftConstantSpeed(block.controlledCheckbox("Constant Speed",
                    VisualSettings.flightCubeCraftConstantSpeed(),
                    VisualSettings::setFlightCubeCraftConstantSpeed));
            VisualSettings.setFlightCubeCraftBoostOnce(block.controlledCheckbox("Boost Once",
                    VisualSettings.flightCubeCraftBoostOnce(), VisualSettings::setFlightCubeCraftBoostOnce));
            VisualSettings.setFlightCubeCraftNostalgia(block.controlledCheckbox("Nostalgia",
                    VisualSettings.flightCubeCraftNostalgia(), VisualSettings::setFlightCubeCraftNostalgia));
        });
    }

    static void populateElytraFlySettings(WidgetSurface.Frame gui) {
        VisualSettings.setElytraFlyMode(gui.combo("Mode", VisualSettings.elytraFlyMode(),
                "Control, Boost", VisualSettings::setElytraFlyMode));
        float horizontal = gui.slider("Horizontal", VisualSettings.elytraFlyHorizontalSpeedSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", VisualSettings.elytraFlyHorizontalSpeed()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 40.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 40.0f),
                VisualSettings::setElytraFlyHorizontalSpeedSlider);
        VisualSettings.setElytraFlyHorizontalSpeedSlider(horizontal);
        float vertical = gui.slider("Vertical", VisualSettings.elytraFlyVerticalSpeedSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", VisualSettings.elytraFlyVerticalSpeed()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 20.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                VisualSettings::setElytraFlyVerticalSpeedSlider);
        VisualSettings.setElytraFlyVerticalSpeedSlider(vertical);
        float glide = gui.slider("Glide", VisualSettings.elytraFlyGlideSlider(),
                String.format(java.util.Locale.ROOT, "%.2f", VisualSettings.elytraFlyGlide()),
                value -> SliderValueFormat.decimalLabel(value, 0.0f, 1.0f),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 1.0f),
                VisualSettings::setElytraFlyGlideSlider);
        VisualSettings.setElytraFlyGlideSlider(glide);
        VisualSettings.setElytraFlyAutoStart(gui.controlledCheckbox("Auto Start",
                VisualSettings.elytraFlyAutoStart(), VisualSettings::setElytraFlyAutoStart));
    }

    static void populateBoatFlySettings(WidgetSurface.Frame gui) {
        VisualSettings.setBoatFlyMode(gui.combo("Mode", VisualSettings.boatFlyMode(),
                "Control, Glide", VisualSettings::setBoatFlyMode));
        float horizontal = gui.slider("Horizontal", VisualSettings.boatFlyHorizontalSpeedSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", VisualSettings.boatFlyHorizontalSpeed()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 30.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 30.0f),
                VisualSettings::setBoatFlyHorizontalSpeedSlider);
        VisualSettings.setBoatFlyHorizontalSpeedSlider(horizontal);
        float vertical = gui.slider("Vertical", VisualSettings.boatFlyVerticalSpeedSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", VisualSettings.boatFlyVerticalSpeed()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 20.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                VisualSettings::setBoatFlyVerticalSpeedSlider);
        VisualSettings.setBoatFlyVerticalSpeedSlider(vertical);
        gui.mode("boat-fly-mode", VisualSettings.boatFlyMode(), "Glide", block -> {
            float glide = block.slider("Glide", VisualSettings.boatFlyGlideSlider(),
                    String.format(java.util.Locale.ROOT, "%.2f", VisualSettings.boatFlyGlide()),
                    value -> SliderValueFormat.decimalLabel(value, 0.0f, 1.0f),
                    value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 1.0f),
                    VisualSettings::setBoatFlyGlideSlider);
            VisualSettings.setBoatFlyGlideSlider(glide);
        });
    }

    static void populateEntitySpeedSettings(WidgetSurface.Frame gui) {
        float speed = gui.slider("Speed", VisualSettings.entitySpeedSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", VisualSettings.entitySpeed()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 30.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 30.0f),
                VisualSettings::setEntitySpeedSlider);
        VisualSettings.setEntitySpeedSlider(speed);
        VisualSettings.setEntitySpeedKeepY(gui.controlledCheckbox("Keep Y",
                VisualSettings.entitySpeedKeepY(), VisualSettings::setEntitySpeedKeepY));
    }

    static void populateStepSettings(WidgetSurface.Frame gui) {
        float height = gui.slider("Height", VisualSettings.stepHeightSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", VisualSettings.stepHeight()),
                value -> SliderValueFormat.decimalRangeLabel(value, 0.6f, 2.5f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 0.6f, 2.5f),
                VisualSettings::setStepHeightSlider);
        VisualSettings.setStepHeightSlider(height);
        VisualSettings.setStepOnlyGround(gui.controlledCheckbox("Only Ground",
                VisualSettings.stepOnlyGround(), VisualSettings::setStepOnlyGround));
    }

    static void populateLongJumpSettings(WidgetSurface.Frame gui) {
        float power = gui.slider("Power", VisualSettings.longJumpPowerSlider(),
                String.format(java.util.Locale.ROOT, "%.2f", VisualSettings.longJumpPower()),
                value -> SliderValueFormat.decimalRangeLabel(value, 0.5f, 4.0f, 2),
                value -> SliderValueFormat.parseRangeSlider(value, 0.5f, 4.0f),
                VisualSettings::setLongJumpPowerSlider);
        VisualSettings.setLongJumpPowerSlider(power);
    }

    static void populateHighJumpSettings(WidgetSurface.Frame gui) {
        float power = gui.slider("Power", VisualSettings.highJumpPowerSlider(),
                String.format(java.util.Locale.ROOT, "%.2f", VisualSettings.highJumpPower()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 4.0f, 2),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 4.0f),
                VisualSettings::setHighJumpPowerSlider);
        VisualSettings.setHighJumpPowerSlider(power);
    }

    static void populateJesusSettings(WidgetSurface.Frame gui) {
        VisualSettings.setJesusMode(gui.combo("Mode", VisualSettings.jesusMode(),
                "Solid, Bounce", VisualSettings::setJesusMode));
        VisualSettings.setJesusLava(gui.controlledCheckbox("Lava",
                VisualSettings.jesusLava(), VisualSettings::setJesusLava));
    }

    static void populateSpiderSettings(WidgetSurface.Frame gui) {
        VisualSettings.setSpiderMode(gui.combo("Mode", VisualSettings.spiderMode(),
                "Motion, Legit", VisualSettings::setSpiderMode));
        float speed = gui.slider("Speed", VisualSettings.spiderSpeedSlider(),
                String.format(java.util.Locale.ROOT, "%.2f", VisualSettings.spiderSpeed()),
                value -> SliderValueFormat.decimalRangeLabel(value, 0.05f, 0.6f, 2),
                value -> SliderValueFormat.parseRangeSlider(value, 0.05f, 0.6f),
                VisualSettings::setSpiderSpeedSlider);
        VisualSettings.setSpiderSpeedSlider(speed);
        VisualSettings.setSpiderOnlyJump(gui.controlledCheckbox("Only Jump",
                VisualSettings.spiderOnlyJump(), VisualSettings::setSpiderOnlyJump));
        VisualSettings.setSpiderOnlyMoving(gui.controlledCheckbox("Only Moving",
                VisualSettings.spiderOnlyMoving(), VisualSettings::setSpiderOnlyMoving));
    }

    static void populatePhaseSettings(WidgetSurface.Frame gui) {
        VisualSettings.setPhaseSneakDown(gui.controlledCheckbox("Sneak Down",
                VisualSettings.phaseSneakDown(), VisualSettings::setPhaseSneakDown));
    }

    static void populateNoFallSettings(WidgetSurface.Frame gui) {
        VisualSettings.setNoFallMode(gui.combo("Mode", VisualSettings.noFallMode(),
                "Spoof, Watchdog", VisualSettings::setNoFallMode));
        gui.visible("no-fall-spoof-settings", "Spoof".equals(VisualSettings.noFallMode()),
                inner -> VisualSettings.setNoFallNoGround(inner.controlledCheckbox("No Ground",
                        VisualSettings.noFallNoGround(), VisualSettings::setNoFallNoGround)));
    }

    static void populateNoSlowSettings(WidgetSurface.Frame gui) {
        VisualSettings.setNoSlowMode(gui.combo("Mode", VisualSettings.noSlowMode(),
                "Vanilla, GrimJump, GrimFast, Watchdog, Universal", VisualSettings::setNoSlowMode));
        VisualSettings.setNoSlowKeepSprinting(gui.controlledCheckbox("Keep Sprinting",
                VisualSettings.noSlowKeepSprinting(), VisualSettings::setNoSlowKeepSprinting));
        gui.visible("no-slow-universal-settings", "Universal".equals(VisualSettings.noSlowMode()),
                inner -> VisualSettings.setNoSlowSlowDown(inner.controlledCheckbox("Slow Down",
                        VisualSettings.noSlowSlowDown(), VisualSettings::setNoSlowSlowDown)));
    }

    static void populateBlinkSettings(WidgetSurface.Frame gui) {
        VisualSettings.setBlinkMode(gui.combo("Mode", VisualSettings.blinkMode(),
                "Normal, Pulse", VisualSettings::setBlinkMode));
        float pulse = gui.slider("Pulse Ticks", VisualSettings.blinkPulseTicksSlider(),
                VisualSettings.blinkPulseTicks() + "",
                value -> SliderValueFormat.intRangeLabel(value, 2.0f, 80.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 2.0f, 80.0f),
                VisualSettings::setBlinkPulseTicksSlider);
        VisualSettings.setBlinkPulseTicksSlider(pulse);
        float maxPackets = gui.slider("Max Packets", VisualSettings.blinkMaxPacketsSlider(),
                VisualSettings.blinkMaxPackets() + "",
                value -> SliderValueFormat.intRangeLabel(value, 10.0f, 300.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 10.0f, 300.0f),
                VisualSettings::setBlinkMaxPacketsSlider);
        VisualSettings.setBlinkMaxPacketsSlider(maxPackets);
        VisualSettings.setBlinkCancelOnHurt(gui.controlledCheckbox("Cancel On Hurt",
                VisualSettings.blinkCancelOnHurt(), VisualSettings::setBlinkCancelOnHurt));
    }
}
