package com.xerion.forge.chams;

import com.xerion.common.visual.ChamsMaterial;

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

import com.xerion.forge.XerionForge;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.EnumMap;
import java.util.Map;

public final class ChamsTextures {
    private static final Map<ChamsMaterial, ResourceLocation> TEXTURES = new EnumMap<>(ChamsMaterial.class);

    private ChamsTextures() {
    }

    public static ResourceLocation texture(ChamsMaterial material) {
        return TEXTURES.computeIfAbsent(material, ChamsTextures::createTexture);
    }

    public static void clear() {
        if (TEXTURES.isEmpty()) {
            return;
        }
        if (RenderSystem.isOnRenderThread()) {
            releaseTextures();
        } else {
            RenderSystem.recordRenderCall(ChamsTextures::releaseTextures);
        }
    }

    private static ResourceLocation createTexture(ChamsMaterial material) {
        NativeImage image = new NativeImage(32, 32, false);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setPixelRGBA(x, y, pixel(material, x, y, image.getWidth(), image.getHeight()));
            }
        }
        DynamicTexture texture = new DynamicTexture(image);
        String name = "chams/" + material.label().toLowerCase(java.util.Locale.ROOT).replace(' ', '_');
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(XerionForge.MOD_ID, name);
        Minecraft.getInstance().getTextureManager().register(id, texture);
        return id;
    }

    private static int pixel(ChamsMaterial material, int x, int y, int w, int h) {
        float u = x / (float) Math.max(1, w - 1);
        float v = y / (float) Math.max(1, h - 1);
        return switch (material) {
            case SHADED, SOLID, GLOW, GLOSSY -> rgba(255, 255, 255, 255);
            case IRIDESCENT -> {
                float band = positiveWave(u * 1.75f - v * 0.85f);
                int r = Math.round(170.0f + 85.0f * positiveWave(u + band * 0.15f));
                int g = Math.round(170.0f + 70.0f * positiveWave(u + 0.33f));
                int b = Math.round(180.0f + 75.0f * positiveWave(u + 0.66f - band * 0.12f));
                yield rgba(clamp(r), clamp(g), clamp(b), 255);
            }
            case WATER_FLOW -> {
                float wave = positiveWave(u * 2.0f + v * 5.5f);
                float streak = positiveWave(u * 8.0f + v * 1.25f);
                int r = Math.round(185.0f + 45.0f * wave);
                int g = Math.round(210.0f + 35.0f * wave + 10.0f * streak);
                int b = Math.round(245.0f + 10.0f * streak);
                yield rgba(clamp(r), clamp(g), clamp(b), 255);
            }
        };
    }

    private static float positiveWave(float value) {
        return 0.5f + 0.5f * (float) Math.sin(value * Math.PI * 2.0);
    }

    private static int rgba(int r, int g, int b, int a) {
        return (a & 0xFF) << 24
                | (b & 0xFF) << 16
                | (g & 0xFF) << 8
                | (r & 0xFF);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static void releaseTextures() {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        for (ResourceLocation id : TEXTURES.values()) {
            textureManager.release(id);
        }
        TEXTURES.clear();
    }

    public static final class ReloadListener extends SimplePreparableReloadListener<Void> {
        @Override
        protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
            return null;
        }

        @Override
        protected void apply(Void unused, ResourceManager resourceManager, ProfilerFiller profiler) {
            ChamsTextures.clear();
        }
    }
}
