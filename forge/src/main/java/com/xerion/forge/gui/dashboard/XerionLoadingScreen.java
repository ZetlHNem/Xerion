package com.xerion.forge.gui.dashboard;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.CrashReport;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class XerionLoadingScreen extends Screen {
    private final CompletableFuture<?> future;
    private final Runnable completion;
    private final Runnable deferredStart;
    private float alpha;
    private boolean handled;
    private boolean rendered;
    private boolean started;

    public XerionLoadingScreen(Component message) {
        this(message, null, null, null);
    }

    public XerionLoadingScreen(Component message, Runnable deferredStart) {
        this(message, null, null, deferredStart);
    }

    public XerionLoadingScreen(Component message, CompletableFuture<?> future, Runnable completion) {
        this(message, future, completion, null);
    }

    private XerionLoadingScreen(Component message, CompletableFuture<?> future,
                                Runnable completion, Runnable deferredStart) {
        super(message);
        this.future = future;
        this.completion = completion;
        this.deferredStart = deferredStart;
    }

    @Override
    public void tick() {
        if (!started && deferredStart != null && rendered) {
            started = true;
            deferredStart.run();
            return;
        }
        if (handled || future == null || !future.isDone()) {
            return;
        }
        handled = true;
        try {
            completion.run();
        } catch (CompletionException exception) {
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            minecraft.delayCrash(CrashReport.forThrowable(cause, "Couldn't prepare world creation data"));
        } catch (Throwable throwable) {
            minecraft.delayCrash(CrashReport.forThrowable(throwable, "Couldn't prepare world creation data"));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        AnimationClock.beginFrame();
        alpha = UiMath.lerp(alpha, 1.0f, UiAnimationTimings.PANEL_FADE);
        float renderAlpha = UiMath.clamp01(alpha);
        XerionTitleScreen.drawBackgroundImage(graphics, width, height, renderAlpha);
        graphics.fill(0, 0, width, height, UiColor.alpha(0xFF070A10, renderAlpha * 0.48f));

        int centerX = width / 2;
        int centerY = height / 2;
        TextPainter.draw(graphics, "Xerion", centerX - TextPainter.width("Xerion", 18.0f) / 2,
                centerY - 30, 18.0f, UiPalette.text(), renderAlpha);
        String message = getTitle().getString();
        int messageWidth = TextPainter.width(message, 9.0f);
        TextPainter.draw(graphics, message, centerX - messageWidth / 2,
                centerY + 1, 9.0f, UiPalette.muted(), renderAlpha);

        int trackW = Math.min(240, Math.max(150, width / 3));
        int trackX = centerX - trackW / 2;
        int trackY = centerY + 28;
        ShapeRenderer.roundRect(graphics, trackX, trackY, trackW, 3, 2,
                UiColor.alpha(UiPalette.track(), renderAlpha * 0.86f));
        float phase = (System.currentTimeMillis() % 1400L) / 1400.0f;
        int segmentW = Math.max(28, trackW / 4);
        int segmentX = trackX + Math.round((trackW - segmentW) * phase);
        ShapeRenderer.roundRect(graphics, segmentX, trackY, segmentW, 3, 2,
                UiColor.alpha(UiPalette.accent(), renderAlpha));
        rendered = true;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
