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
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraftforge.client.event.RenderPlayerEvent;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

public final class PlayerChamsReplacementRenderer {
    private static final int FULL_BRIGHT = LightTexture.FULL_BRIGHT;

    private PlayerChamsReplacementRenderer() {
    }

    public static boolean shouldReplace(RenderPlayerEvent.Pre event) {
        return event.getEntity() instanceof AbstractClientPlayer player
                && ChamsProfileResolver.profile(player).hasAnyChams(player);
    }

    public static void render(RenderPlayerEvent.Pre event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player) || player.isSpectator()) {
            return;
        }

        PlayerRenderer renderer = event.getRenderer();
        PlayerModel<AbstractClientPlayer> model = renderer.getModel();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        float partialTick = event.getPartialTick();

        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
        float netHeadYaw = headYaw - bodyYaw;
        float headPitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());
        float ageInTicks = player.tickCount + partialTick;
        float limbSwing = player.walkAnimation.position(partialTick);
        float limbSwingAmount = Math.min(player.walkAnimation.speed(partialTick), 1.0f);

        applyPlayerModelVisibility(model, player);
        model.attackTime = player.getAttackAnim(partialTick);
        model.riding = player.isPassenger();
        model.young = player.isBaby();
        model.crouching = player.isCrouching();

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf(new AxisAngle4f((float) Math.toRadians(180.0f - bodyYaw), 0.0f, 1.0f, 0.0f)));
        if (player.deathTime > 0) {
            float death = ((float) player.deathTime + partialTick - 1.0f) / 20.0f * 1.6f;
            death = Mth.sqrt(Math.max(0.0f, death));
            if (death > 1.0f) {
                death = 1.0f;
            }
            poseStack.mulPose(new Quaternionf(new AxisAngle4f(death * 90.0f * ((float) Math.PI / 180.0f), 0.0f, 0.0f, 1.0f)));
        }
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(0.0d, -1.501d, 0.0d);

        model.prepareMobModel(player, limbSwing, limbSwingAmount, partialTick);
        model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ChamsProfileResolver.Profile profile = ChamsProfileResolver.profile(player);

        if (profile.throughWallsEnabled()) {
            renderPass(model, poseStack, bufferSource,
                    profile.throughWallsMaterial(),
                    profile.throughWallsColor(),
                    true,
                    event.getPackedLight());
        }
        ChamsMaterial visibleMaterial = profile.visibleMaterial(player);
        if (visibleMaterial != null) {
            renderPass(model, poseStack, bufferSource,
                    visibleMaterial,
                    profile.visibleColor(player),
                    false,
                    event.getPackedLight());
        }

        poseStack.popPose();
    }

    private static void applyPlayerModelVisibility(PlayerModel<AbstractClientPlayer> model, AbstractClientPlayer player) {
        model.setAllVisible(true);
        model.hat.visible = player.isModelPartShown(PlayerModelPart.HAT);
        model.jacket.visible = player.isModelPartShown(PlayerModelPart.JACKET);
        model.leftPants.visible = player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
        model.rightPants.visible = player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
        model.leftSleeve.visible = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        model.rightSleeve.visible = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
        model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
    }

    private static void renderPass(PlayerModel<AbstractClientPlayer> model, PoseStack poseStack,
                                   MultiBufferSource bufferSource, ChamsMaterial material, int color,
                                   boolean throughWalls, int packedLight) {
        float alpha = ((color >>> 24) & 0xFF) / 255.0f;
        if (alpha <= 0.01f) {
            return;
        }

        float red = ((color >>> 16) & 0xFF) / 255.0f;
        float green = ((color >>> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        ResourceLocation texture = ChamsTextures.texture(material);
        RenderType renderType = throughWalls
                ? ChamsRenderTypes.throughWalls(texture, material)
                : ChamsRenderTypes.visible(texture, material);
        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        int light = ChamsRenderTypes.fullBright(material) ? FULL_BRIGHT : packedLight;
        model.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
    }
}
