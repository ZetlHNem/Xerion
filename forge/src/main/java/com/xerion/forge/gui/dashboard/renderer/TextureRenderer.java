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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class TextureRenderer {
    private static final float FEATHER = 0.75f;

    private static ShaderInstance shader;

    private TextureRenderer() {
    }

    public static void init(ShaderInstance loadedShader) {
        shader = loadedShader;
    }

    public static boolean roundedSprite(GuiGraphics graphics, ResourceLocation atlas, TextureAtlasSprite sprite,
                                        float x, float y, float w, float h, float radius, float alpha) {
        if (shader == null || sprite == null || w <= 0.0f || h <= 0.0f || alpha <= 0.01f) {
            return false;
        }

        int textureId = Minecraft.getInstance().getTextureManager().getTexture(atlas).getId();
        return roundedTexture(graphics, textureId, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(),
                x, y, w, h, radius, alpha);
    }

    public static boolean roundedTexture(GuiGraphics graphics, int textureId,
                                         float x, float y, float w, float h, float radius, float alpha) {
        return roundedTexture(graphics, textureId, 0.0f, 0.0f, 1.0f, 1.0f, x, y, w, h, radius, alpha);
    }

    public static boolean roundedTexture(GuiGraphics graphics, int textureId, float u0, float v0, float u1, float v1,
                                         float x, float y, float w, float h, float radius, float alpha) {
        if (shader == null || textureId <= 0 || w <= 0.0f || h <= 0.0f || alpha <= 0.01f) {
            return false;
        }
        float clampedRadius = Math.max(0.0f, Math.min(radius, Math.min(w, h) * 0.5f));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(() -> shader);

        try {
            shader.setSampler("uTexture", textureId);
            shader.getUniform("uSize").set(w, h);
            shader.getUniform("uUvRect").set(u0, v0, u1, v1);
            shader.getUniform("uRadius").set(clampedRadius);
            shader.getUniform("uFeather").set(FEATHER);
            shader.getUniform("uAlpha").set(Math.max(0.0f, Math.min(alpha, 1.0f)));

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
