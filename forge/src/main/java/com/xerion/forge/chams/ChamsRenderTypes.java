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

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

public final class ChamsRenderTypes extends RenderType {
    private ChamsRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                             boolean affectsCrumbling, boolean sortOnUpload,
                             Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType visible(ResourceLocation texture, ChamsMaterial material) {
        return visible(texture, material, 0xFFFFFFFF);
    }

    public static RenderType visible(ResourceLocation texture, ChamsMaterial material, int color) {
        return createType(texture, material, false, color);
    }

    public static RenderType throughWalls(ResourceLocation texture, ChamsMaterial material) {
        return throughWalls(texture, material, 0xFFFFFFFF);
    }

    public static RenderType throughWalls(ResourceLocation texture, ChamsMaterial material, int color) {
        return createType(texture, material, true, color);
    }

    public static RenderType overlay(ResourceLocation texture, ChamsMaterial material, int color) {
        return createType(texture, material, false, color, true);
    }

    private static RenderType createType(ResourceLocation texture, ChamsMaterial material, boolean throughWalls, int color) {
        return createType(texture, material, throughWalls, color, false);
    }

    private static RenderType createType(ResourceLocation texture, ChamsMaterial material, boolean throughWalls,
                                         int color, boolean overlay) {
        String name = "xerion_chams_" + material.label().toLowerCase(java.util.Locale.ROOT)
                .replace(' ', '_') + "_" + Integer.toHexString(color)
                + (throughWalls ? "_xqz" : (overlay ? "_overlay" : "_visible"));
        return create(name,
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                512,
                true,
                true,
                composite(texture, material, throughWalls, color, overlay));
    }

    private static CompositeState composite(ResourceLocation texture, ChamsMaterial material, boolean throughWalls, int color) {
        return composite(texture, material, throughWalls, color, false);
    }

    private static CompositeState composite(ResourceLocation texture, ChamsMaterial material, boolean throughWalls,
                                            int color, boolean overlay) {
        CompositeState.CompositeStateBuilder builder = CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setShaderState(new RenderStateShard.ShaderStateShard(ChamsShaders::shader))
                .setTexturingState(materialState(material, throughWalls, color, overlay))
                .setTransparencyState(additive(material)
                        ? ADDITIVE_TRANSPARENCY
                        : TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(throughWalls ? GREATER_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(fullBright(material) ? NO_LIGHTMAP : LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(throughWalls || overlay ? COLOR_WRITE : COLOR_DEPTH_WRITE);
        return builder.createCompositeState(false);
    }

    private static RenderStateShard.TexturingStateShard materialState(ChamsMaterial material, boolean throughWalls,
                                                                      int color, boolean overlay) {
        return new RenderStateShard.TexturingStateShard(
                "xerion_chams_material_" + material.ordinal() + (throughWalls ? "_xqz" : ""),
                () -> {
                    ShaderInstance shader = ChamsShaders.shader();
                    if (shader == null) {
                        return;
                    }
                    if (shader.getUniform("uMaterialMode") != null) {
                        shader.getUniform("uMaterialMode").set((float) materialMode(material));
                    }
                    if (shader.getUniform("uThroughWalls") != null) {
                        shader.getUniform("uThroughWalls").set(throughWalls ? 1.0f : 0.0f);
                    }
                    if (shader.getUniform("uShellOffset") != null) {
                        shader.getUniform("uShellOffset").set(overlay ? 0.055f : 0.0f);
                    }
                    if (shader.getUniform("uTint") != null) {
                        shader.getUniform("uTint").set(
                                ((color >>> 16) & 0xFF) / 255.0f,
                                ((color >>> 8) & 0xFF) / 255.0f,
                                (color & 0xFF) / 255.0f,
                                ((color >>> 24) & 0xFF) / 255.0f
                        );
                    }
                    if (shader.getUniform("uTime") != null) {
                        shader.getUniform("uTime").set((System.currentTimeMillis() % 100000L) / 1000.0f);
                    }
                },
                () -> {
                });
    }

    private static int materialMode(ChamsMaterial material) {
        return switch (material) {
            case SHADED -> 0;
            case SOLID -> 1;
            case GLOW -> 3;
            case IRIDESCENT -> 4;
            case WATER_FLOW -> 5;
            case GLOSSY -> 6;
        };
    }

    private static boolean additive(ChamsMaterial material) {
        return false;
    }

    public static boolean fullBright(ChamsMaterial material) {
        return material == ChamsMaterial.SOLID
                || material == ChamsMaterial.GLOW
                || material == ChamsMaterial.IRIDESCENT
                || material == ChamsMaterial.GLOSSY;
    }
}
