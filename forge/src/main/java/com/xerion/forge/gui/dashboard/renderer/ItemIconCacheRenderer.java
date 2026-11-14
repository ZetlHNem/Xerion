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

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

final class ItemIconCacheRenderer {
    private static final int TARGET_SIZE = 64;
    private static final int CACHE_LIMIT = 256;

    private final LinkedHashMap<String, RenderTarget> cache = new LinkedHashMap<>();

    boolean draw(GuiGraphics graphics, ItemStack stack, int x, int y, float size, float alpha) {
        if (stack.isEmpty() || alpha <= 0.01f) {
            return false;
        }
        RenderTarget target = target(graphics, stack);
        if (target == null) {
            return false;
        }
        return TextureRenderer.roundedTexture(graphics, target.getColorTextureId(),
                0.0f, 1.0f, 1.0f, 0.0f, x, y, size, size, 6.0f, alpha);
    }

    private RenderTarget target(GuiGraphics graphics, ItemStack stack) {
        String key = key(stack);
        RenderTarget cached = cache.remove(key);
        if (cached != null) {
            cache.put(key, cached);
            return cached;
        }

        RenderTarget target = new TextureTarget(TARGET_SIZE, TARGET_SIZE, true, Minecraft.ON_OSX);
        target.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        if (!render(graphics, stack, target)) {
            target.destroyBuffers();
            return null;
        }
        cache.put(key, target);
        trim();
        return target;
    }

    private boolean render(GuiGraphics graphics, ItemStack stack, RenderTarget target) {
        Minecraft minecraft = Minecraft.getInstance();
        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        int previousViewportW = minecraft.getWindow().getWidth();
        int previousViewportH = minecraft.getWindow().getHeight();
        try {
            graphics.flush();
            target.bindWrite(true);
            RenderSystem.viewport(0, 0, TARGET_SIZE, TARGET_SIZE);
            RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
            target.clear(Minecraft.ON_OSX);
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0f, TARGET_SIZE, TARGET_SIZE,
                    0.0f, 1000.0f, -1000.0f), VertexSorting.ORTHOGRAPHIC_Z);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            GuiGraphics targetGraphics = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());
            targetGraphics.pose().pushPose();
            try {
                targetGraphics.pose().translate(0.0f, 0.0f, 250.0f);
                targetGraphics.pose().scale(TARGET_SIZE / 16.0f, TARGET_SIZE / 16.0f, 1.0f);
                targetGraphics.renderItem(stack, 0, 0);
            } finally {
                targetGraphics.pose().popPose();
            }
            targetGraphics.flush();
            return true;
        } catch (RuntimeException ignored) {
            return false;
        } finally {
            minecraft.getMainRenderTarget().bindWrite(false);
            RenderSystem.viewport(0, 0, previousViewportW, previousViewportH);
            RenderSystem.setProjectionMatrix(previousProjection, VertexSorting.ORTHOGRAPHIC_Z);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private void trim() {
        Iterator<Map.Entry<String, RenderTarget>> iterator = cache.entrySet().iterator();
        while (cache.size() > CACHE_LIMIT && iterator.hasNext()) {
            RenderTarget target = iterator.next().getValue();
            iterator.remove();
            if (target != null) {
                target.destroyBuffers();
            }
        }
    }

    private static String key(ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        int damage = stack.isDamageableItem() ? stack.getDamageValue() : 0;
        int tagHash = stack.hasTag() ? stack.getTag().hashCode() : 0;
        return id + "|" + damage + "|" + tagHash + "|" + stack.hasFoil();
    }
}
