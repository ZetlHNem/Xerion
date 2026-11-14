package com.xerion.forge.mixin;

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
import com.xerion.forge.gui.dashboard.ShapeRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Redirect(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"))
    private void xerion$replaceVanillaChatFill(GuiGraphics graphics, int minX, int minY, int maxX, int maxY,
                                                          int color) {
        if (xerion$drawBetterFill(graphics, minX, minY, maxX, maxY, color)) {
            return;
        }
        graphics.fill(minX, minY, maxX, maxY, color);
    }

    @Redirect(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIIII)V"))
    private void xerion$replaceVanillaChatFillWithZ(GuiGraphics graphics, int minX, int minY, int maxX,
                                                               int maxY, int z, int color) {
        if (xerion$drawBetterFill(graphics, minX, minY, maxX, maxY, color)) {
            return;
        }
        graphics.fill(minX, minY, maxX, maxY, z, color);
    }

    @Redirect(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(Lnet/minecraft/client/renderer/RenderType;IIIII)V"))
    private void xerion$replaceVanillaChatRenderTypeFill(GuiGraphics graphics, RenderType renderType,
                                                                    int minX, int minY, int maxX, int maxY,
                                                                    int color) {
        if (xerion$drawBetterFill(graphics, minX, minY, maxX, maxY, color)) {
            return;
        }
        graphics.fill(renderType, minX, minY, maxX, maxY, color);
    }

    @Redirect(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(Lnet/minecraft/client/renderer/RenderType;IIIIII)V"))
    private void xerion$replaceVanillaChatRenderTypeFillWithZ(GuiGraphics graphics, RenderType renderType,
                                                                         int minX, int minY, int maxX, int maxY,
                                                                         int z, int color) {
        if (xerion$drawBetterFill(graphics, minX, minY, maxX, maxY, color)) {
            return;
        }
        graphics.fill(renderType, minX, minY, maxX, maxY, z, color);
    }

    private boolean xerion$drawBetterFill(GuiGraphics graphics, int minX, int minY, int maxX, int maxY,
                                                     int color) {
        if (!VisualSettings.betterChatEnabled()) {
            return false;
        }

        int w = maxX - minX;
        int h = maxY - minY;
        if (w <= 0 || h <= 0) {
            return true;
        }

        if (!VisualSettings.betterChatHideVanillaBackground() || w < 12 || h < 5) {
            return false;
        }

        int x = minX - 2;
        int y = minY - 1;
        int drawW = w + 4;
        int drawH = h + 2;
        ShapeRenderer.roundRect(graphics, x, y, drawW, drawH, 5, VisualSettings.betterChatBackgroundColor());
        ShapeRenderer.roundRectBorder(graphics, x, y, drawW, drawH, 5, 1, 0x55FFFFFF);
        return true;
    }
}
