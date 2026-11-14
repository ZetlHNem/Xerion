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
import com.xerion.common.config.BypassSettings;
import com.xerion.common.config.DisablerSettings;
import com.xerion.common.config.NoRotateSettings;
import com.xerion.common.config.PingSpoofSettings;
import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

public final class ArrayListHudRenderer {
    private static final int PAD_X = 5;
    private static final int ROW_H = 11;
    private static final int ROW_GAP = 2;
    private static final int MIN_W = 66;
    private static final float TEXT_SIZE = 6.0f;
    private static final float BLUR_STRENGTH = 18.0f;
    private static final float BG_ALPHA = 0.58f;
    private static final String EDIT_PREVIEW_ITEM = "Perview Items";
    private static final List<ModuleEntry> MODULES = List.of(
            new ModuleEntry("Compatibility", BypassSettings::enabled),
            new ModuleEntry("Disabler", DisablerSettings::enabled),
            new ModuleEntry("Ping Spoof", PingSpoofSettings::enabled),
            new ModuleEntry("No Rotate", NoRotateSettings::enabled),
            new ModuleEntry("Kill Aura", CombatSettings::killAuraEnabled),
            new ModuleEntry("Reach", CombatSettings::reachEnabled),
            new ModuleEntry("Criticals", CombatSettings::criticalsEnabled),
            new ModuleEntry("Knockback", CombatSettings::knockbackEnabled),
            new ModuleEntry("WTap", CombatSettings::sprintResetEnabled),
            new ModuleEntry("Anti Bot", CombatSettings::antiBotEnabled),
            new ModuleEntry("Auto Weapon", CombatSettings::autoWeaponEnabled),
            new ModuleEntry("Shield Breaker", CombatSettings::shieldBreakerEnabled),
            new ModuleEntry("Target Strafe", CombatSettings::targetStrafeEnabled),
            new ModuleEntry("Hitboxes", CombatSettings::hitboxesEnabled),
            new ModuleEntry("Backtrack", CombatSettings::backtrackEnabled),
            new ModuleEntry("Fake Lag", CombatSettings::fakeLagEnabled),
            new ModuleEntry("Attack Delay", CombatSettings::attackDelayEnabled),
            new ModuleEntry("Auto Head", CombatSettings::autoHeadEnabled),
            new ModuleEntry("Auto Block", CombatSettings::autoBlockEnabled),
            new ModuleEntry("Velocity", CombatSettings::velocityEnabled),
            new ModuleEntry("Trigger Bot", CombatSettings::triggerBotEnabled),
            new ModuleEntry("Aim Assist", CombatSettings::aimAssistEnabled),
            new ModuleEntry("Projectile Aim", CombatSettings::projectileAimEnabled),
            new ModuleEntry("Ragebot", CombatSettings::ragebotEnabled),
            new ModuleEntry("Anti Aim", CombatSettings::ragebotAntiAimEnabled),
            new ModuleEntry("Auto Pearl", CombatSettings::autoPearlEnabled),
            new ModuleEntry("Pearl Aim", CombatSettings::pearlAimEnabled),
            new ModuleEntry("Fast Use", CombatSettings::fastUseEnabled),
            new ModuleEntry("Auto Clicker", CombatSettings::autoClickerEnabled),
            new ModuleEntry("Auto Gap", CombatSettings::autoHealEnabled),
            new ModuleEntry("Auto Totem", CombatSettings::autoTotemEnabled),
            new ModuleEntry("Auto EXP", CombatSettings::autoExpEnabled),
            new ModuleEntry("Auto Armor", CombatSettings::autoArmorEnabled),
            new ModuleEntry("Bed Aura", CombatSettings::bedAuraEnabled),
            new ModuleEntry("Anchor Aura", CombatSettings::anchorAuraEnabled),
            new ModuleEntry("Web Aura", CombatSettings::webAuraEnabled),
            new ModuleEntry("Auto Trap", CombatSettings::autoTrapEnabled),
            new ModuleEntry("Surround", CombatSettings::surroundEnabled),
            new ModuleEntry("Hole Fill", CombatSettings::holeFillEnabled),
            new ModuleEntry("Auto Crystal", CombatSettings::autoCrystalEnabled),
            new ModuleEntry("Visual ESP", VisualSettings::visualEspEnabled),
            new ModuleEntry("Ore ESP", VisualSettings::oreGlowEnabled),
            new ModuleEntry("Xray", VisualSettings::xrayEnabled),
            new ModuleEntry("Chest ESP", VisualSettings::chestGlowEnabled),
            new ModuleEntry("Spawner ESP", VisualSettings::spawnerEspEnabled),
            new ModuleEntry("Bed ESP", VisualSettings::bedEspEnabled),
            new ModuleEntry("Block Overlay", VisualSettings::blockOverlayEnabled),
            new ModuleEntry("Barrier Blocks", VisualSettings::barrierBlocksEnabled),
            new ModuleEntry("Light Blocks", VisualSettings::lightBlocksEnabled),
            new ModuleEntry("Fullbright", VisualSettings::fullbrightEnabled),
            new ModuleEntry("No Fog", VisualSettings::noFogEnabled),
            new ModuleEntry("No Weather", VisualSettings::noWeatherEnabled),
            new ModuleEntry("Time Changer", VisualSettings::timeChangerEnabled),
            new ModuleEntry("Ambience", VisualSettings::ambienceEnabled),
            new ModuleEntry("Anti Blindness", VisualSettings::antiBlindnessEnabled),
            new ModuleEntry("Freecam", VisualSettings::freecamEnabled),
            new ModuleEntry("Zoom", VisualSettings::zoomEnabled),
            new ModuleEntry("Aspect Ratio", VisualSettings::aspectRatioEnabled),
            new ModuleEntry("View Model", VisualSettings::viewModelEnabled),
            new ModuleEntry("Auto Tool", VisualSettings::autoToolEnabled),
            new ModuleEntry("Auto Mine", VisualSettings::autoMineEnabled),
            new ModuleEntry("Nuker", VisualSettings::nukerEnabled),
            new ModuleEntry("Vein Miner", VisualSettings::veinMinerEnabled),
            new ModuleEntry("Quick Stop", VisualSettings::quickStopEnabled),
            new ModuleEntry("Bhop Turn", VisualSettings::bhopTurnEnabled),
            new ModuleEntry("No Fall", VisualSettings::noFallEnabled),
            new ModuleEntry("Fast Ladder", VisualSettings::fastLadderEnabled),
            new ModuleEntry("No Slow", VisualSettings::noSlowEnabled),
            new ModuleEntry("Safe Walk", VisualSettings::safeWalkEnabled),
            new ModuleEntry("Edge Jump", VisualSettings::edgeJumpEnabled),
            new ModuleEntry("Inventory Move", VisualSettings::inventoryMoveEnabled),
            new ModuleEntry("Pearl Throw", VisualSettings::pearlThrowEnabled),
            new ModuleEntry("Quick Switch", VisualSettings::quickSwitchEnabled),
            new ModuleEntry("Fake Player", VisualSettings::fakePlayerEnabled),
            new ModuleEntry("Sound Control", VisualSettings::soundControlEnabled),
            new ModuleEntry("Hit Sounds", VisualSettings::hitSoundsEnabled),
            new ModuleEntry("Better Chat", VisualSettings::betterChatEnabled),
            new ModuleEntry("Fast Place", VisualSettings::fastPlaceEnabled),
            new ModuleEntry("Anti Hunger", VisualSettings::antiHungerEnabled),
            new ModuleEntry("Auto Respawn", VisualSettings::autoRespawnEnabled),
            new ModuleEntry("Keep Sprint", VisualSettings::keepSprintEnabled),
            new ModuleEntry("Speed", VisualSettings::speedEnabled),
            new ModuleEntry("Flight", VisualSettings::flightEnabled),
            new ModuleEntry("Elytra Fly", VisualSettings::elytraFlyEnabled),
            new ModuleEntry("Boat Fly", VisualSettings::boatFlyEnabled),
            new ModuleEntry("Entity Speed", VisualSettings::entitySpeedEnabled),
            new ModuleEntry("Anti Void", VisualSettings::antiVoidEnabled),
            new ModuleEntry("Freeze", VisualSettings::freezeEnabled),
            new ModuleEntry("Step", VisualSettings::stepEnabled),
            new ModuleEntry("Long Jump", VisualSettings::longJumpEnabled),
            new ModuleEntry("High Jump", VisualSettings::highJumpEnabled),
            new ModuleEntry("Jesus", VisualSettings::jesusEnabled),
            new ModuleEntry("Spider", VisualSettings::spiderEnabled),
            new ModuleEntry("Phase", VisualSettings::phaseEnabled),
            new ModuleEntry("No Clip", VisualSettings::noClipEnabled),
            new ModuleEntry("Blink", VisualSettings::blinkEnabled),
            new ModuleEntry("Stuck", VisualSettings::stuckEnabled),
            new ModuleEntry("Movement Fix", VisualSettings::movementFixEnabled),
            new ModuleEntry("Fast Web", VisualSettings::fastWebEnabled),
            new ModuleEntry("Fast Break", VisualSettings::fastBreakEnabled),
            new ModuleEntry("Breaker", VisualSettings::breakerEnabled),
            new ModuleEntry("Timer", VisualSettings::timerEnabled),
            new ModuleEntry("Anti-Staff", VisualSettings::antiStaffEnabled),
            new ModuleEntry("Auto Hypixel", VisualSettings::autoGGEnabled),
            new ModuleEntry("Auto Eat", VisualSettings::autoEatEnabled),
            new ModuleEntry("Auto Fish", VisualSettings::autoFishEnabled),
            new ModuleEntry("Chest Steal", VisualSettings::chestStealEnabled),
            new ModuleEntry("Inventory Cleaner", VisualSettings::inventoryCleanerEnabled),
            new ModuleEntry("Auto Refill", VisualSettings::autoRefillEnabled),
            new ModuleEntry("Auto Sort", VisualSettings::autoSortEnabled),
            new ModuleEntry("Scaffold", VisualSettings::scaffoldEnabled),
            new ModuleEntry("Anti Packet Kick", VisualSettings::antiPacketKickEnabled),
            new ModuleEntry("Auto Reconnect", VisualSettings::autoReconnectEnabled)
    );
    private static final Map<String, RowAnim> rowAnimations = new HashMap<>();
    private static float animatedX;
    private static float animatedY;
    private static float animatedRight;
    private static float alpha;
    private static boolean animationInitialized;
    private static Arrangement lastArrangement;
    private static float layoutProgress = 1.0f;

    private ArrayListHudRenderer() {
    }

    public static void render(GuiGraphics graphics, boolean editMode) {
        boolean enabled = VisualSettings.hudWidgetEnabled(VisualSettings.HUD_WIDGET_ARRAY_LIST);
        List<String> active = activeModules();
        List<String> display = active.isEmpty() && editMode ? List.of(EDIT_PREVIEW_ITEM) : active;
        boolean visibleTarget = enabled && !display.isEmpty();
        if (!visibleTarget && alpha <= 0.01f && rowAnimations.isEmpty()) {
            return;
        }
        updateRows(display, visibleTarget);
        List<RowAnim> rows = visibleRows();
        Layout layout = layout(rows, true);
        clampToScreen(layout);
        applySavedRightAnchor(layout);
        int targetX = VisualSettings.arrayListHudX();
        int targetY = VisualSettings.arrayListHudY();
        boolean savedRightAnchored = VisualSettings.arrayListHudRightAnchored();
        float targetRight = targetX + layout.width;
        if (!animationInitialized) {
            animatedX = targetX;
            animatedY = targetY;
            animatedRight = targetRight;
            animationInitialized = true;
        }
        if (savedRightAnchored) {
            animatedRight = UiMath.lerp(animatedRight, targetRight, UiAnimationTimings.HUD_MOVE);
            if (Math.abs(animatedRight - targetRight) < 0.35f) {
                animatedRight = targetRight;
            }
            animatedX = animatedRight - layout.width;
        } else {
            animatedX = UiMath.lerp(animatedX, targetX, UiAnimationTimings.HUD_MOVE);
            if (Math.abs(animatedX - targetX) < 0.35f) {
                animatedX = targetX;
            }
            animatedRight = animatedX + layout.width;
        }
        animatedY = UiMath.lerp(animatedY, targetY, UiAnimationTimings.HUD_MOVE);
        alpha = UiMath.lerp(alpha, visibleTarget ? 1.0f : 0.0f, UiAnimationTimings.HUD_FADE);
        float hudAlpha = UiMath.clamp01(alpha);
        if (hudAlpha <= 0.01f && rows.isEmpty()) {
            return;
        }

        int anchorX = Math.round(animatedX);
        int anchorY = Math.round(animatedY);
        Window window = Minecraft.getInstance().getWindow();
        int x = anchorX;
        int y = anchorY;
        Arrangement arrangement = arrangement(anchorY, rows.size(), window.getGuiScaledHeight());
        updateRowPositions(rows, arrangement);
        boolean rightAligned = savedRightAnchored || x + layout.width * 0.5f > window.getGuiScaledWidth() * 0.5f;
        for (int index = 0; index < rows.size(); index++) {
            RowAnim row = rows.get(index);
            float rowAlpha = hudAlpha * UiMath.clamp01(row.alpha);
            if (rowAlpha <= 0.01f) {
                continue;
            }
            int rowW = Math.round(MSDFRenderer.width(row.label, TEXT_SIZE)) + PAD_X * 2;
            int rowX = rightAligned ? x + layout.width - rowW : x;
            int rowY = y + Math.round(row.y);
            if (VisualSettings.arrayListHudBlur()) {
                KawaseBlurPipeline.renderSurfaceBackdrop(graphics, window.getGuiScaledWidth(), window.getGuiScaledHeight(),
                        rowX, rowY, rowW, ROW_H, 4.0f, rowAlpha, BLUR_STRENGTH);
            }
            ShapeRenderer.roundRect(graphics, rowX, rowY, rowW, ROW_H, 4,
                    UiColor.alpha(UiPalette.field(), rowAlpha * BG_ALPHA));
            if (VisualSettings.arrayListHudAccentBar()) {
                int accentX = rightAligned ? rowX + rowW - 3 : rowX + 2;
                ShapeRenderer.roundRect(graphics, accentX, rowY + 2, 1, ROW_H - 4, 1,
                        UiColor.alpha(UiPalette.accent(), rowAlpha));
            }
            float textX = rightAligned
                    ? rowX + rowW - PAD_X - MSDFRenderer.width(row.label, TEXT_SIZE)
                    : rowX + PAD_X;
            MSDFRenderer.draw(graphics, row.label, textX, rowY + UiMath.centeredTextY(ROW_H, TEXT_SIZE),
                    TEXT_SIZE, UiColor.alpha(UiPalette.text(), rowAlpha));
        }
    }

    public static boolean contains(double mouseX, double mouseY) {
        if (!VisualSettings.hudWidgetEnabled(VisualSettings.HUD_WIDGET_ARRAY_LIST)) {
            return false;
        }
        Layout layout = layoutPreview();
        int x = VisualSettings.arrayListHudX();
        int anchorY = VisualSettings.arrayListHudY();
        Minecraft minecraft = Minecraft.getInstance();
        List<RowAnim> rows = visibleRows();
        Arrangement arrangement = arrangement(anchorY, Math.max(1, rows.size()), minecraft.getWindow().getGuiScaledHeight());
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int count = Math.max(1, rows.size());
        for (int i = 0; i < count; i++) {
            int rowY = anchorY + (i < rows.size() ? Math.round(rows.get(i).y) : rowOffset(i, count, arrangement));
            minY = Math.min(minY, rowY);
            maxY = Math.max(maxY, rowY + ROW_H);
        }
        return mouseX >= x && mouseX <= x + layout.width && mouseY >= minY && mouseY <= maxY;
    }

    public static void moveTo(double mouseX, double mouseY, double offsetX, double offsetY) {
        Layout layout = layoutPreview();
        Minecraft minecraft = Minecraft.getInstance();
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        int rawX = (int) Math.round(mouseX - offsetX);
        int rawY = dragAnchorY(mouseY, offsetY, screenH);
        HudSnap.Position snapped = HudSnap.snap(rawX, rawY, layout.width, ROW_H, screenW, screenH);
        VisualSettings.setArrayListHudPosition(snapped.x(), snapped.y());
        boolean rightAnchored = snapped.x() + layout.width * 0.5f > screenW * 0.5f;
        VisualSettings.setArrayListHudRightAnchored(rightAnchored);
        if (rightAnchored) {
            VisualSettings.setArrayListHudRightOffset(Math.max(0, screenW - (snapped.x() + layout.width)));
        }
        animatedX = snapped.x();
        animatedRight = snapped.x() + layout.width;
        animatedY = snapped.y();
        animationInitialized = true;
    }

    public static double dragOffsetX(double mouseX) {
        return mouseX - VisualSettings.arrayListHudX();
    }

    public static double dragOffsetY(double mouseY) {
        int anchorY = VisualSettings.arrayListHudY();
        List<RowAnim> rows = visibleRows();
        int count = Math.max(1, targetRowCount(rows));
        Minecraft minecraft = Minecraft.getInstance();
        Arrangement arrangement = arrangement(anchorY, count, minecraft.getWindow().getGuiScaledHeight());
        return mouseY - (anchorY + currentVisualMinOffset(rows, count, arrangement));
    }

    public static int width() {
        return anchorLayout().width;
    }

    public static int height() {
        return ROW_H;
    }

    private static List<String> activeModules() {
        List<String> result = new ArrayList<>();
        for (ModuleEntry module : MODULES) {
            if (module.enabled.getAsBoolean()) {
                result.add(module.name);
            }
        }
        result.sort(Comparator.comparingDouble((String name) -> MSDFRenderer.width(name, TEXT_SIZE)).reversed()
                .thenComparing(String::compareToIgnoreCase));
        return result;
    }

    private static void updateRows(List<String> labels, boolean visibleTarget) {
        for (RowAnim row : rowAnimations.values()) {
            row.target = false;
        }
        int index = 0;
        if (visibleTarget) {
            for (String label : labels) {
                RowAnim row = rowAnimations.computeIfAbsent(label, RowAnim::new);
                row.label = label;
                row.target = true;
                row.orderIndex = index;
                if (!row.initialized) {
                    row.y = index * (ROW_H + ROW_GAP) + 5;
                    row.layoutStartY = row.y;
                    row.targetY = row.y;
                    row.initialized = true;
                }
                index++;
            }
        }
        rowAnimations.values().removeIf(row -> {
            row.alpha = UiMath.lerp(row.alpha, row.target ? 1.0f : 0.0f, UiAnimationTimings.HUD_FADE);
            return !row.target && row.alpha <= 0.02f;
        });
    }

    private static List<RowAnim> visibleRows() {
        List<RowAnim> rows = new ArrayList<>(rowAnimations.values());
        rows.sort((left, right) -> Integer.compare(left.orderIndex, right.orderIndex));
        return rows;
    }

    private static Layout layout(List<RowAnim> rows, boolean includeFadingRows) {
        int width = 0;
        int count = 0;
        for (RowAnim row : rows) {
            if (row.alpha <= 0.01f) {
                continue;
            }
            if (!includeFadingRows && !row.target) {
                continue;
            }
            count++;
            width = Math.max(width, Math.round(MSDFRenderer.width(row.label, TEXT_SIZE)) + PAD_X * 2);
        }
        width = width <= 0 ? MIN_W : width;
        int height = count == 0 ? ROW_H : count * ROW_H + Math.max(0, count - 1) * ROW_GAP;
        return new Layout(width, height);
    }

    private static Layout layoutPreview() {
        List<RowAnim> rows = visibleRows();
        return rows.isEmpty() ? new Layout(MIN_W, ROW_H) : layout(rows, true);
    }

    private static Layout anchorLayout() {
        List<RowAnim> rows = visibleRows();
        Layout current = layout(rows, false);
        return new Layout(current.width, ROW_H);
    }

    private static void updateRowPositions(List<RowAnim> rows, Arrangement arrangement) {
        List<RowAnim> targetRows = rows.stream().filter(row -> row.target).toList();
        int targetCount = targetRows.size();
        boolean arrangementChanged = lastArrangement != arrangement;
        if (arrangementChanged) {
            lastArrangement = arrangement;
            layoutProgress = 0.0f;
            for (int index = 0; index < targetCount; index++) {
                RowAnim row = targetRows.get(index);
                row.layoutStartY = row.y;
                row.targetY = rowOffset(index, targetCount, arrangement);
            }
        }

        if (arrangementChanged || layoutProgress < 1.0f) {
            layoutProgress = UiMath.lerp(layoutProgress, 1.0f, UiAnimationTimings.HUD_MOVE);
            if (Math.abs(layoutProgress - 1.0f) < 0.01f) {
                layoutProgress = 1.0f;
            }
            float t = UiMath.smoothstep(layoutProgress);
            for (int index = 0; index < targetCount; index++) {
                RowAnim row = targetRows.get(index);
                row.y = row.layoutStartY + (row.targetY - row.layoutStartY) * t;
                if (layoutProgress >= 1.0f || Math.abs(row.y - row.targetY) < 0.35f) {
                    row.y = row.targetY;
                    row.layoutStartY = row.targetY;
                }
            }
        } else {
            for (int index = 0; index < targetCount; index++) {
                RowAnim row = targetRows.get(index);
                row.targetY = rowOffset(index, targetCount, arrangement);
                row.y = UiMath.lerp(row.y, row.targetY, UiAnimationTimings.HUD_MOVE);
                if (Math.abs(row.y - row.targetY) < 0.35f) {
                    row.y = row.targetY;
                }
                row.layoutStartY = row.targetY;
            }
        }

        for (RowAnim row : rows) {
            if (!row.target) {
                row.targetY = row.y;
                row.layoutStartY = row.y;
            }
        }
    }

    private static Arrangement arrangement(int anchorY, int count, int screenH) {
        float anchorCenter = anchorY + ROW_H * 0.5f;
        if (anchorCenter < screenH / 3.0f) {
            return Arrangement.DOWN;
        }
        if (anchorCenter > screenH * 2.0f / 3.0f) {
            return Arrangement.UP;
        }
        return Arrangement.CENTER;
    }

    private static int rowOffset(int index, int count, Arrangement arrangement) {
        int step = ROW_H + ROW_GAP;
        return switch (arrangement) {
            case DOWN -> index * step;
            case UP -> -index * step;
            case CENTER -> {
                if (index == 0) {
                    yield 0;
                }
                int distance = (index + 1) / 2;
                yield (index % 2 == 1 ? distance : -distance) * step;
            }
        };
    }

    private static int dragAnchorY(double mouseY, double visualOffsetY, int screenH) {
        int count = Math.max(1, targetRowCount(visibleRows()));
        int anchorY = VisualSettings.arrayListHudY();
        for (int i = 0; i < 4; i++) {
            Arrangement arrangement = arrangement(anchorY, count, screenH);
            anchorY = (int) Math.round(mouseY - visualOffsetY - minRowOffset(count, arrangement));
        }
        return anchorY;
    }

    private static int targetRowCount(List<RowAnim> rows) {
        int count = 0;
        for (RowAnim row : rows) {
            if (row.target) {
                count++;
            }
        }
        return count;
    }

    private static int currentVisualMinOffset(List<RowAnim> rows, int count, Arrangement arrangement) {
        int min = Integer.MAX_VALUE;
        for (RowAnim row : rows) {
            if (row.target) {
                min = Math.min(min, Math.round(row.y));
            }
        }
        return min == Integer.MAX_VALUE ? minRowOffset(count, arrangement) : min;
    }

    private static int minRowOffset(int count, Arrangement arrangement) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            min = Math.min(min, rowOffset(i, count, arrangement));
        }
        return min == Integer.MAX_VALUE ? 0 : min;
    }

    private static void clampToScreen(Layout layout) {
        Minecraft minecraft = Minecraft.getInstance();
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        int x = VisualSettings.arrayListHudRightAnchored()
                ? screenW - VisualSettings.arrayListHudRightOffset() - layout.width
                : VisualSettings.arrayListHudX();
        x = UiMath.clamp(x, 0, Math.max(0, screenW - layout.width));
        int y = UiMath.clamp(VisualSettings.arrayListHudY(), 0, Math.max(0, screenH - ROW_H));
        if (x != VisualSettings.arrayListHudX() || y != VisualSettings.arrayListHudY()) {
            VisualSettings.setArrayListHudPosition(x, y);
        }
    }

    private static void applySavedRightAnchor(Layout layout) {
        if (!VisualSettings.arrayListHudRightAnchored()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int targetX = UiMath.clamp(screenW - VisualSettings.arrayListHudRightOffset() - layout.width,
                0, Math.max(0, screenW - layout.width));
        if (targetX != VisualSettings.arrayListHudX()) {
            VisualSettings.setArrayListHudPosition(targetX, VisualSettings.arrayListHudY());
        }
    }

    private record ModuleEntry(String name, BooleanSupplier enabled) {
    }

    private record Layout(int width, int height) {
    }

    private enum Arrangement {
        DOWN,
        UP,
        CENTER
    }

    private static final class RowAnim {
        private String label;
        private float y;
        private float layoutStartY;
        private float targetY;
        private float alpha;
        private boolean target;
        private boolean initialized;
        private int orderIndex;

        private RowAnim(String label) {
            this.label = label;
        }
    }
}
