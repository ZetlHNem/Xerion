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

import com.xerion.forge.config.VisualSettings;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public final class LocalVisualChams {
    private static final ThreadLocal<Boolean> FIRST_PERSON_ARMS = ThreadLocal.withInitial(() -> false);
    private static final ResourceLocation BLOCK_ATLAS = TextureAtlas.LOCATION_BLOCKS;

    private LocalVisualChams() {
    }

    public static boolean localEnabled() {
        return VisualSettings.visualLocalEnabled();
    }

    public static boolean localItemEnabled() {
        return localEnabled() && VisualSettings.visualLocalItem();
    }

    public static boolean localArmsEnabled() {
        return localEnabled() && VisualSettings.visualLocalArms();
    }

    public static ChamsMaterial localItemMaterial() {
        return ChamsMaterial.fromLabel(VisualSettings.visualLocalItemMode());
    }

    public static int localItemColor() {
        return VisualSettings.visualLocalItemColor();
    }

    public static ChamsMaterial localArmsMaterial() {
        return ChamsMaterial.fromLabel(VisualSettings.visualLocalArmsMode());
    }

    public static int localArmsColor() {
        return VisualSettings.visualLocalArmsColor();
    }

    public static int localPlayerColor(Player player, int color) {
        if (!shouldApplyLocalTransparency(player)) {
            return color;
        }
        int rgb = color & 0x00FFFFFF;
        int alpha = localTransparencyAlpha();
        return (alpha << 24) | rgb;
    }

    public static float localTransparencyAlphaFloat() {
        return Math.max(0.0f, Math.min(1.0f, VisualSettings.visualLocalTransparency()));
    }

    public static int localTransparencyAlpha() {
        return Math.round(localTransparencyAlphaFloat() * 255.0f);
    }

    public static boolean shouldApplyLocalTransparency(Player player) {
        if (player == null
                || !localEnabled()
                || !VisualSettings.visualLocalTransparencyEnabled()) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (player != minecraft.player) {
            return false;
        }
        int mask = VisualSettings.visualLocalTransparencyDisableMask();
        if ((mask & VisualSettings.LOCAL_TRANSPARENCY_DISABLE_FIRST_PERSON) != 0
                && minecraft.options.getCameraType().isFirstPerson()) {
            return false;
        }
        if ((mask & VisualSettings.LOCAL_TRANSPARENCY_DISABLE_HOLDING_ITEM) != 0
                && (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty())) {
            return false;
        }
        if ((mask & VisualSettings.LOCAL_TRANSPARENCY_DISABLE_USING_ITEM) != 0
                && player.isUsingItem()) {
            return false;
        }
        if ((mask & VisualSettings.LOCAL_TRANSPARENCY_DISABLE_SNEAKING) != 0
                && player.isShiftKeyDown()) {
            return false;
        }
        return (mask & VisualSettings.LOCAL_TRANSPARENCY_DISABLE_SWIMMING) == 0
                || !player.isVisuallySwimming();
    }

    public static MultiBufferSource itemBuffer(MultiBufferSource delegate) {
        if (!localItemEnabled()) {
            return delegate;
        }
        return new ReplacingBuffer(delegate, BLOCK_ATLAS, localItemMaterial(), localItemColor());
    }

    public static void beginFirstPersonArms() {
        FIRST_PERSON_ARMS.set(true);
    }

    public static void endFirstPersonArms() {
        FIRST_PERSON_ARMS.set(false);
    }

    public static boolean firstPersonArmsActive() {
        return FIRST_PERSON_ARMS.get();
    }

    public static RenderType replaceArmRenderType(RenderType original, ResourceLocation texture) {
        if (!firstPersonArmsActive() || !localArmsEnabled() || texture == null) {
            return original;
        }
        return ChamsRenderTypes.visible(texture, localArmsMaterial(), localArmsColor());
    }

    private record ReplacingBuffer(MultiBufferSource delegate, ResourceLocation texture,
                                   ChamsMaterial material, int color) implements MultiBufferSource {
        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return delegate.getBuffer(ChamsRenderTypes.visible(texture, material, color));
        }
    }
}
