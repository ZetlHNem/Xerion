package com.xerion.forge.runtime;

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
import com.xerion.forge.chams.ChamsShaders;
import com.xerion.forge.chams.ChamsTextures;
import com.xerion.forge.esp.BlockWireEspOverlayRenderer;
import com.xerion.forge.gui.font.MSDFReloadListener;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.xerion.forge.gui.dashboard.KawaseBlurPipeline;
import com.xerion.forge.gui.dashboard.SdfShapeRenderer;
import com.xerion.forge.gui.dashboard.ShadowRenderer;
import com.xerion.forge.gui.dashboard.TextureRenderer;
import com.xerion.forge.gui.dashboard.XerionLogoTexture;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;

import java.io.IOException;

public final class RenderResourceRegistry {
    private RenderResourceRegistry() {
    }

    public static void startEarlyResourcePreload() {
        MSDFRenderer.startEarlyCachePreload();
    }

    public static void registerRuntimeReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new MSDFReloadListener());
        event.registerReloadListener(new BlockWireEspOverlayRenderer.TextureColorReloadListener());
        event.registerReloadListener(new ChamsTextures.ReloadListener());
    }

    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new net.minecraft.client.renderer.ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(XerionForge.MOD_ID, "rounded_rect"),
                        DefaultVertexFormat.POSITION_TEX
                ),
                SdfShapeRenderer::init
        );
        event.registerShader(
                new net.minecraft.client.renderer.ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(XerionForge.MOD_ID, "rounded_texture"),
                        DefaultVertexFormat.POSITION_TEX
                ),
                TextureRenderer::init
        );
        event.registerShader(
                new net.minecraft.client.renderer.ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(XerionForge.MOD_ID, "logo_sdf"),
                        DefaultVertexFormat.POSITION_TEX
                ),
                XerionLogoTexture::init
        );
        event.registerShader(
                new net.minecraft.client.renderer.ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(XerionForge.MOD_ID, "sdf_text"),
                        DefaultVertexFormat.POSITION_TEX
                ),
                MSDFRenderer::init
        );
        event.registerShader(
                new net.minecraft.client.renderer.ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(XerionForge.MOD_ID, "kawase_blur"),
                        DefaultVertexFormat.POSITION_TEX
                ),
                KawaseBlurPipeline::init
        );
        event.registerShader(
                new net.minecraft.client.renderer.ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(XerionForge.MOD_ID, "shadow_rect"),
                        DefaultVertexFormat.POSITION_TEX
                ),
                ShadowRenderer::init
        );
        event.registerShader(
                new net.minecraft.client.renderer.ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(XerionForge.MOD_ID, "chams_entity"),
                        DefaultVertexFormat.NEW_ENTITY
                ),
                ChamsShaders::init
        );
    }
}

