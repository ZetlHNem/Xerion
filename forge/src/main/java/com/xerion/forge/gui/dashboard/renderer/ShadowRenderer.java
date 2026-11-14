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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

public final class ShadowRenderer {
    private static ShaderInstance shader;

    private ShadowRenderer() {
    }

    public static void init(ShaderInstance loadedShader) {
        shader = loadedShader;
    }

    static void shadow(GuiGraphics graphics, int x, int y, int w, int h, int radius,
                       ShadowStyle.Style style, float alpha) {
        if (shader == null || style == null || w <= 0 || h <= 0 || alpha <= 0.01f) {
            return;
        }
        float spread = Math.max(0.0f, style.spread());
        if (spread <= 0.0f) {
            return;
        }

        int color = UiColor.alpha(style.color(), alpha);
        float quadX = Math.round(x - spread + style.offsetX());
        float quadY = Math.round(y - spread + style.offsetY());
        float quadW = Math.round(w + spread * 2.0f);
        float quadH = Math.round(h + spread * 2.0f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(() -> shader);

        try {
            shader.getUniform("uRectSize").set((float) w, (float) h);
            shader.getUniform("uQuadSize").set(quadW, quadH);
            shader.getUniform("uColor").set(
                    ((color >> 16) & 0xFF) / 255.0f,
                    ((color >> 8) & 0xFF) / 255.0f,
                    (color & 0xFF) / 255.0f,
                    ((color >> 24) & 0xFF) / 255.0f
            );
            shader.getUniform("uRadius").set(Math.max(0.0f, radius));
            shader.getUniform("uSpread").set(spread);

            drawQuad(graphics.pose().last().pose(), quadX, quadY, quadW, quadH);
        } finally {
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    private static void drawQuad(Matrix4f matrix, float x, float y, float w, float h) {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, x, y + h, 0.0f).uv(0.0f, 1.0f).endVertex();
        buffer.vertex(matrix, x + w, y + h, 0.0f).uv(1.0f, 1.0f).endVertex();
        buffer.vertex(matrix, x + w, y, 0.0f).uv(1.0f, 0.0f).endVertex();
        buffer.vertex(matrix, x, y, 0.0f).uv(0.0f, 0.0f).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }
}
