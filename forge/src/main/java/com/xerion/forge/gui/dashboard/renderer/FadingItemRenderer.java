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

import com.xerion.forge.mixin.RenderTypeCompositeAccessor;
import com.xerion.forge.mixin.RenderTypeCompositeStateAccessor;
import com.xerion.forge.mixin.TextureStateShardAccessor;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

final class FadingItemRenderer {
    private static final int GUI_LIGHT = 15728880;

    private FadingItemRenderer() {
    }

    static boolean draw(GuiGraphics graphics, ItemStack stack, float x, float y, float scale, float alpha) {
        if (stack.isEmpty() || scale <= 0.0f || alpha <= 0.01f) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, minecraft.level, minecraft.player, 0);
        boolean flatLighting = !model.usesBlockLight();

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        try {
            poseStack.translate(x + 8.0f * scale, y + 8.0f * scale, 150.0f);
            poseStack.mulPoseMatrix(new Matrix4f().scaling(1.0f, -1.0f, 1.0f));
            poseStack.scale(16.0f * scale, 16.0f * scale, 16.0f * scale);
            if (flatLighting) {
                Lighting.setupForFlatItems();
            }
            itemRenderer.render(stack, ItemDisplayContext.GUI, false, poseStack,
                    new AlphaBufferSource(graphics.bufferSource(), alpha),
                    GUI_LIGHT, OverlayTexture.NO_OVERLAY, model);
            graphics.flush();
            return true;
        } catch (RuntimeException ignored) {
            return false;
        } finally {
            if (flatLighting) {
                Lighting.setupFor3DItems();
            }
            poseStack.popPose();
        }
    }

    private static final class AlphaBufferSource implements MultiBufferSource {
        private final MultiBufferSource delegate;
        private final float alpha;

        private AlphaBufferSource(MultiBufferSource delegate, float alpha) {
            this.delegate = delegate;
            this.alpha = Math.max(0.0f, Math.min(alpha, 1.0f));
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            RenderType targetType = alpha < 0.999f ? translucentRenderType(renderType) : renderType;
            return new AlphaVertexConsumer(delegate.getBuffer(targetType), alpha);
        }

        private static RenderType translucentRenderType(RenderType renderType) {
            if (renderType == null || isGlint(renderType)) {
                return renderType;
            }
            ResourceLocation texture = texture(renderType);
            if (texture != null) {
                return RenderType.entityTranslucent(texture);
            }
            if (canUseItemAtlasTransparency(renderType)) {
                return Sheets.translucentItemSheet();
            }
            return renderType;
        }

        private static boolean canUseItemAtlasTransparency(RenderType renderType) {
            return renderType == RenderType.solid()
                    || renderType == RenderType.cutout()
                    || renderType == RenderType.cutoutMipped()
                    || renderType == Sheets.translucentItemSheet();
        }

        private static boolean isGlint(RenderType renderType) {
            return renderType.toString().toLowerCase().contains("glint");
        }

        private static ResourceLocation texture(RenderType renderType) {
            try {
                RenderType.CompositeState state =
                        ((RenderTypeCompositeAccessor) (Object) renderType).xerion$getState();
                RenderStateShard.EmptyTextureStateShard textureState =
                        ((RenderTypeCompositeStateAccessor) (Object) state).xerion$getTextureState();
                if (textureState instanceof RenderStateShard.TextureStateShard) {
                    return ((TextureStateShardAccessor) (Object) textureState)
                            .xerion$getTexture()
                            .orElse(null);
                }
            } catch (ClassCastException | LinkageError ignored) {
            }
            return null;
        }
    }

    private static final class AlphaVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float alpha;

        private AlphaVertexConsumer(VertexConsumer delegate, float alpha) {
            this.delegate = delegate;
            this.alpha = Math.max(0.0f, Math.min(alpha, 1.0f));
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            delegate.color(red, green, blue, scaledAlpha(alpha));
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            delegate.uv(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            delegate.overlayCoords(u, v);
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            delegate.uv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            delegate.endVertex();
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            delegate.defaultColor(red, green, blue, scaledAlpha(alpha));
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }

        @Override
        public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue,
                                float alpha, int packedLight, int packedOverlay, boolean readExistingColor) {
            delegate.putBulkData(pose, quad, red, green, blue, alpha * this.alpha,
                    packedLight, packedOverlay, readExistingColor);
        }

        @Override
        public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightness, float red, float green,
                                float blue, float alpha, int[] packedLight, int packedOverlay,
                                boolean readExistingColor) {
            delegate.putBulkData(pose, quad, brightness, red, green, blue, alpha * this.alpha,
                    packedLight, packedOverlay, readExistingColor);
        }

        @Override
        public VertexConsumer vertex(Matrix4f matrix, float x, float y, float z) {
            delegate.vertex(matrix, x, y, z);
            return this;
        }

        @Override
        public VertexConsumer normal(Matrix3f matrix, float x, float y, float z) {
            delegate.normal(matrix, x, y, z);
            return this;
        }

        private int scaledAlpha(int value) {
            return Math.max(0, Math.min(255, Math.round(value * alpha)));
        }
    }
}
