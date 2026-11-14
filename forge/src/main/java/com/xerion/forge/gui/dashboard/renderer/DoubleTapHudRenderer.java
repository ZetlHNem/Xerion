package com.xerion.forge.gui.dashboard;

import com.xerion.forge.combat.TaczRagebotHooks;
import com.xerion.forge.gui.font.MSDFRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class DoubleTapHudRenderer {
    private static final int W = 68;
    private static final int H = 13;
    private static final float LABEL_SIZE = 5.0f;
    private static final float STATUS_SIZE = 4.4f;
    private static float alpha;
    private static float displayedProgress;

    private DoubleTapHudRenderer() {
    }

    public static void render(GuiGraphics graphics) {
        boolean active = TaczRagebotHooks.doubleTapActive();
        alpha = AnimationClock.lerp(alpha, active ? 1.0f : 0.0f, 0.24f);
        displayedProgress = AnimationClock.lerp(displayedProgress,
                TaczRagebotHooks.doubleTapChargeProgress(), 0.30f);
        float hudAlpha = UiMath.clamp01(alpha);
        if (hudAlpha <= 0.01f) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        int x = (minecraft.getWindow().getGuiScaledWidth() - W) / 2;
        int y = minecraft.getWindow().getGuiScaledHeight() / 2 + 24;
        boolean ready = TaczRagebotHooks.doubleTapCharged();
        int statusColor = ready ? UiPalette.statusGreen() : UiPalette.statusOrange();

        ShapeRenderer.roundRect(graphics, x, y, W, H, 4,
                UiColor.alpha(UiPalette.surface(), hudAlpha * 0.78f));
        MSDFRenderer.draw(graphics, "DT", x + 5.0f, y + 3.0f, LABEL_SIZE,
                UiColor.alpha(UiPalette.text(), hudAlpha));

        String status = ready ? "READY" : "CHARGING";
        float statusW = MSDFRenderer.width(status, STATUS_SIZE);
        MSDFRenderer.draw(graphics, status, x + W - statusW - 5.0f, y + 3.4f, STATUS_SIZE,
                UiColor.alpha(statusColor, hudAlpha));

        int trackX = x + 4;
        int trackY = y + H - 3;
        int trackW = W - 8;
        ShapeRenderer.roundRect(graphics, trackX, trackY, trackW, 2, 1,
                UiColor.alpha(UiPalette.field(), hudAlpha * 0.90f));
        int fillW = Math.round(trackW * UiMath.clamp01(displayedProgress));
        if (fillW > 0) {
            ShapeRenderer.roundRect(graphics, trackX, trackY, fillW, 2, 1,
                    UiColor.alpha(statusColor, hudAlpha));
        }
    }
}
