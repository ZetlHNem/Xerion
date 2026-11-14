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
import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;


public final class TargetsHudRenderer {
    private static final int W = 112;
    private static final int H = 38;
    private static final int PAD = 5;
    private static final int AVATAR = 23;
    private static final float TITLE_SIZE = 5.8f;
    private static final float TEXT_SIZE = 5.0f;
    private static final float ICON_SIZE = 10.0f;
    private static final float BLUR_STRENGTH = 18.0f;
    private static final float BG_ALPHA = 0.80f;
    private static float animatedX;
    private static float animatedY;
    private static float alpha;
    private static float healthProgress;
    private static boolean animationInitialized;
    private static CombatHudTarget.Snapshot lastSnapshot;

    private TargetsHudRenderer() {
    }

    public static void render(GuiGraphics graphics, boolean editMode) {
        boolean enabled = VisualSettings.hudWidgetEnabled(VisualSettings.HUD_WIDGET_TARGETS);
        CombatHudTarget.Snapshot snapshot = CombatHudTarget.snapshot();
        boolean visibleTarget = enabled && (snapshot.present() || editMode);
        if (!visibleTarget && alpha <= 0.01f) {
            return;
        }
        if (snapshot.present()) {
            lastSnapshot = snapshot;
        } else if (editMode) {
            snapshot = CombatHudTarget.Snapshot.empty();
        } else if (lastSnapshot != null) {
            snapshot = lastSnapshot;
        }
        clampToScreen();
        int targetX = VisualSettings.targetsHudX();
        int targetY = VisualSettings.targetsHudY();
        if (!animationInitialized) {
            animatedX = targetX;
            animatedY = targetY;
            animationInitialized = true;
        }
        animatedX = UiMath.lerp(animatedX, targetX, UiAnimationTimings.HUD_MOVE);
        animatedY = UiMath.lerp(animatedY, targetY, UiAnimationTimings.HUD_MOVE);
        alpha = UiMath.lerp(alpha, visibleTarget ? 1.0f : 0.0f, UiAnimationTimings.HUD_FADE);
        float hudAlpha = UiMath.clamp01(alpha);
        if (hudAlpha <= 0.01f) {
            return;
        }

        int x = Math.round(animatedX);
        int y = Math.round(animatedY);
        Window window = Minecraft.getInstance().getWindow();
        if (VisualSettings.targetsHudBlur()) {
            KawaseBlurPipeline.renderSurfaceBackdrop(graphics, window.getGuiScaledWidth(), window.getGuiScaledHeight(),
                    x, y, W, H, 6.0f, hudAlpha, BLUR_STRENGTH);
        }
        ShadowRenderer.shadow(graphics, x, y, W, H, 6, ShadowStyle.card(), hudAlpha * BG_ALPHA);
        ShapeRenderer.roundRect(graphics, x, y, W, H, 6,
                UiColor.alpha(UiPalette.surface(), hudAlpha * BG_ALPHA));
        if (editMode) {
            ShapeRenderer.roundRectBorder(graphics, x, y, W, H, 6, 1,
                    UiColor.alpha(UiPalette.accentSoft(), hudAlpha));
        }

        int avatarX = x + PAD;
        int avatarY = y + PAD;
        boolean showAvatar = VisualSettings.targetsHudShowAvatar();
        if (showAvatar) {
            ShapeRenderer.roundRect(graphics, avatarX, avatarY, AVATAR, AVATAR, 5,
                    UiColor.alpha(UiPalette.field(), hudAlpha * 0.72f));
            drawAvatar(graphics, snapshot, avatarX, avatarY, hudAlpha);
        }

        String name = snapshot.name() == null || snapshot.name().isBlank() ? "Target" : snapshot.name();
        String distance = String.format(java.util.Locale.ROOT, "%.1fm", snapshot.distance());
        float nameX = showAvatar ? avatarX + AVATAR + 6.0f : x + PAD;
        float nameY = y + 6.0f;
        boolean showDistance = VisualSettings.targetsHudShowDistance();
        float maxNameW = W - (nameX - x) - PAD - (showDistance ? MSDFRenderer.width(distance, TEXT_SIZE) + 5.0f : 0.0f);
        String clippedName = clipText(name, maxNameW);
        MSDFRenderer.draw(graphics, clippedName, nameX, nameY, TITLE_SIZE,
                UiColor.alpha(UiPalette.text(), hudAlpha));
        if (showDistance) {
            MSDFRenderer.draw(graphics, distance, x + W - PAD - MSDFRenderer.width(distance, TEXT_SIZE), nameY + 1.0f,
                    TEXT_SIZE, UiColor.alpha(UiPalette.text(), hudAlpha * 0.92f));
        }

        float totalHealth = Math.max(0.0f, snapshot.health() + snapshot.absorption());
        float maxHealth = Math.max(1.0f, snapshot.maxHealth());
        float targetProgress = Math.min(1.0f, totalHealth / maxHealth);
        healthProgress = UiMath.lerp(healthProgress, targetProgress, UiAnimationTimings.HUD_MOVE);
        int barX = Math.round(nameX);
        int barY = y + 21;
        int barW = x + W - PAD - barX;
        int barH = 4;
        if (VisualSettings.targetsHudShowHealthBar()) {
            ShapeRenderer.roundRect(graphics, barX, barY, barW, barH, 3,
                    UiColor.alpha(UiPalette.field(), hudAlpha * 0.70f));
        }
        int fillW = Math.max(0, Math.round(barW * UiMath.clamp01(healthProgress)));
        int healthColor = healthColor(targetProgress);
        if (VisualSettings.targetsHudShowHealthBar() && fillW > 0) {
            ShapeRenderer.roundRect(graphics, barX, barY, fillW, barH, 3,
                    UiColor.alpha(healthColor, hudAlpha));
        }

        String hp = String.format(java.util.Locale.ROOT, "%.0f / %.0f", totalHealth, maxHealth);
        if (VisualSettings.targetsHudShowHealthText()) {
            MSDFRenderer.draw(graphics, hp, barX, y + 29.0f, TEXT_SIZE,
                    UiColor.alpha(UiPalette.text(), hudAlpha * 0.90f));
        }
    }

    public static boolean contains(double mouseX, double mouseY) {
        if (!VisualSettings.hudWidgetEnabled(VisualSettings.HUD_WIDGET_TARGETS)) {
            return false;
        }
        int x = VisualSettings.targetsHudX();
        int y = VisualSettings.targetsHudY();
        return mouseX >= x && mouseX <= x + W && mouseY >= y && mouseY <= y + H;
    }

    public static void moveTo(double mouseX, double mouseY, double offsetX, double offsetY) {
        Minecraft minecraft = Minecraft.getInstance();
        int rawX = (int) Math.round(mouseX - offsetX);
        int rawY = (int) Math.round(mouseY - offsetY);
        HudSnap.Position snapped = HudSnap.snap(rawX, rawY, W, H,
                minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
        VisualSettings.setTargetsHudPosition(snapped.x(), snapped.y());
        animatedX = snapped.x();
        animatedY = snapped.y();
        animationInitialized = true;
    }

    public static double dragOffsetX(double mouseX) {
        return mouseX - VisualSettings.targetsHudX();
    }

    public static double dragOffsetY(double mouseY) {
        return mouseY - VisualSettings.targetsHudY();
    }

    public static int width() {
        return W;
    }

    public static int height() {
        return H;
    }

    private static void clampToScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        int x = UiMath.clamp(VisualSettings.targetsHudX(), 0, Math.max(0, screenW - W));
        int y = UiMath.clamp(VisualSettings.targetsHudY(), 0, Math.max(0, screenH - H));
        if (x != VisualSettings.targetsHudX() || y != VisualSettings.targetsHudY()) {
            VisualSettings.setTargetsHudPosition(x, y);
        }
    }

    private static String clipText(String text, float maxW) {
        if (MSDFRenderer.width(text, TITLE_SIZE) <= maxW) {
            return text;
        }
        String suffix = "...";
        String clipped = text;
        while (!clipped.isEmpty() && MSDFRenderer.width(clipped + suffix, TITLE_SIZE) > maxW) {
            clipped = clipped.substring(0, clipped.length() - 1);
        }
        return clipped.isEmpty() ? suffix : clipped + suffix;
    }

    private static int healthColor(float progress) {
        if (progress > 0.66f) {
        return UiPalette.accent();
        }
        if (progress > 0.33f) {
            return 0xFFF0B84F;
        }
        return 0xFFFF5A7A;
    }

    private static void drawAvatar(GuiGraphics graphics, CombatHudTarget.Snapshot snapshot, int x, int y, float alpha) {
        ResourceLocation skin = snapshot.skin();
        if (skin != null) {
            drawPlayerHead(graphics, skin, x, y, alpha);
            return;
        }
        drawFallbackIcon(graphics, x, y, alpha);
    }

    private static void drawPlayerHead(GuiGraphics graphics, ResourceLocation skin, int x, int y, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        try {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, UiMath.clamp01(alpha));
            graphics.blit(skin, x + 2, y + 2, AVATAR - 4, AVATAR - 4,
                    8.0f, 8.0f, 8, 8, 64, 64);
            graphics.blit(skin, x + 2, y + 2, AVATAR - 4, AVATAR - 4,
                    40.0f, 8.0f, 8, 8, 64, 64);
        } finally {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }
    }

    private static void drawFallbackIcon(GuiGraphics graphics, int x, int y, float alpha) {
        float iconX = x + (AVATAR - MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_CROSSHAIRS, ICON_SIZE)) * 0.5f;
        float iconY = y + UiMath.centeredTextY(AVATAR, ICON_SIZE) + 1.0f;
        MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_CROSSHAIRS, iconX, iconY, ICON_SIZE,
                UiColor.alpha(UiPalette.accent(), alpha));
    }

}
