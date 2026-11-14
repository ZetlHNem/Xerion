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

public final class SdfShapeRenderer {
    private static final float FEATHER = 0.1f;

    private static ShaderInstance shader;

    private SdfShapeRenderer() {
    }

    public static void init(ShaderInstance loadedShader) {
        shader = loadedShader;
    }

    public static boolean roundRect(GuiGraphics graphics, float x, float y, float w, float h, float radius, int color) {
        return roundRect(graphics, x, y, w, h, radius, 0.0f, color);
    }

    public static boolean roundRectBorder(GuiGraphics graphics, float x, float y, float w, float h, float radius,
                                          float strokeWidth, int color) {
        return roundRect(graphics, x, y, w, h, radius, Math.max(0.0f, strokeWidth), color);
    }

    private static boolean roundRect(GuiGraphics graphics, float x, float y, float w, float h, float radius,
                                     float strokeWidth, int color) {
        if (shader == null || w <= 0.0f || h <= 0.0f) {
            return false;
        }

        float clampedRadius = Math.max(0.0f, Math.min(radius, Math.min(w, h) * 0.5f));
        float clampedStroke = Math.max(0.0f, Math.min(strokeWidth, Math.min(w, h) * 0.5f));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(() -> shader);

        try {
            shader.getUniform("uSize").set(w, h);
            shader.getUniform("uColor").set(
                    ((color >> 16) & 0xFF) / 255.0f,
                    ((color >> 8) & 0xFF) / 255.0f,
                    (color & 0xFF) / 255.0f,
                    ((color >> 24) & 0xFF) / 255.0f
            );
            shader.getUniform("uRadius").set(clampedRadius);
            shader.getUniform("uFeather").set(FEATHER);
            shader.getUniform("uStrokeWidth").set(clampedStroke);

            drawQuad(graphics.pose().last().pose(), x, y, w, h);
        } finally {
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
        return true;
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
