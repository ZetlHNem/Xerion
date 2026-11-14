package com.xerion.forge.esp;

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
import com.xerion.forge.visual.ModelGlowRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.Set;

public final class XrayOverlayRenderer {
    private static final int XRAY_COLOR = 0xFFFFFFFF;
    private static final int RANGE = 32;
    private static final long SCAN_INTERVAL_MILLIS = 500L;
    private static final Set<BlockPos> CACHED_BLOCKS = new HashSet<>();
    private static BlockPos cachedCenter;
    private static long lastScanMillis;

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || !XrayService.enabled()) {
            clearScanCache();
            return;
        }

        BlockPos center = minecraft.player.blockPosition();
        if (cachedCenter == null || cachedCenter.distManhattan(center) > 8) {
            CACHED_BLOCKS.clear();
            cachedCenter = center;
            lastScanMillis = 0L;
        }
        scanTick(minecraft, center);
        pruneInvalidBlocks(minecraft);

        if (CACHED_BLOCKS.isEmpty() || VisualSettings.xrayOpacity() <= 0.01f) {
            return;
        }

        Camera camera = event.getCamera() == null ? minecraft.gameRenderer.getMainCamera() : event.getCamera();
        if (camera == null) {
            return;
        }

        PoseStack cameraPoseStack = new PoseStack();
        Matrix4f modelView = ModelGlowRenderer.cameraRelativeMatrix(event.getPoseStack().last().pose());
        cameraPoseStack.mulPoseMatrix(modelView);
        Matrix4f matrix = cameraPoseStack.last().pose();
        Vec3 cameraPos = camera.getPosition();

        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f(event.getProjectionMatrix()), VertexSorting.DISTANCE_TO_ORIGIN);
        minecraft.getMainRenderTarget().bindWrite(false);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.depthMask(false);

        try {
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (BlockPos pos : CACHED_BLOCKS) {
                emitBlock(buffer, matrix, pos, cameraPos);
            }
            BufferUploader.drawWithShader(buffer.end());
        } finally {
            RenderSystem.depthMask(true);
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(previousProjection, VertexSorting.DISTANCE_TO_ORIGIN);
        }
    }

    public static void clearScanCache() {
        CACHED_BLOCKS.clear();
        cachedCenter = null;
        lastScanMillis = 0L;
    }

    private static void scanTick(Minecraft minecraft, BlockPos center) {
        long now = System.currentTimeMillis();
        if (now - lastScanMillis < SCAN_INTERVAL_MILLIS) {
            return;
        }
        lastScanMillis = now;
        CACHED_BLOCKS.clear();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int radiusSqr = RANGE * RANGE;
        for (int x = center.getX() - RANGE; x <= center.getX() + RANGE; x++) {
            for (int y = center.getY() - RANGE; y <= center.getY() + RANGE; y++) {
                for (int z = center.getZ() - RANGE; z <= center.getZ() + RANGE; z++) {
                    int dx = x - center.getX();
                    int dy = y - center.getY();
                    int dz = z - center.getZ();
                    if (dx * dx + dy * dy + dz * dz > radiusSqr) {
                        continue;
                    }
                    mutable.set(x, y, z);
                    BlockState state = minecraft.level.getBlockState(mutable);
                    if (XrayService.shouldRenderBlock(minecraft.level, mutable, state)) {
                        CACHED_BLOCKS.add(mutable.immutable());
                    }
                }
            }
        }
    }

    private static void pruneInvalidBlocks(Minecraft minecraft) {
        CACHED_BLOCKS.removeIf(pos -> {
            BlockState state = minecraft.level.getBlockState(pos);
            return !XrayService.shouldRenderBlock(minecraft.level, pos, state);
        });
    }

    private static void emitBlock(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, Vec3 cameraPos) {
        AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        for (Direction direction : Direction.values()) {
            emitFace(buffer, matrix, box, cameraPos, direction);
        }
    }

    private static void emitFace(BufferBuilder buffer, Matrix4f matrix, AABB box, Vec3 cameraPos, Direction direction) {
        float red = ((XRAY_COLOR >>> 16) & 0xFF) / 255.0f;
        float green = ((XRAY_COLOR >>> 8) & 0xFF) / 255.0f;
        float blue = (XRAY_COLOR & 0xFF) / 255.0f;
        float alpha = VisualSettings.xrayOpacity();
        double x0 = box.minX - cameraPos.x;
        double y0 = box.minY - cameraPos.y;
        double z0 = box.minZ - cameraPos.z;
        double x1 = box.maxX - cameraPos.x;
        double y1 = box.maxY - cameraPos.y;
        double z1 = box.maxZ - cameraPos.z;
        switch (direction) {
            case DOWN -> emitQuad(buffer, matrix, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, red, green, blue, alpha);
            case UP -> emitQuad(buffer, matrix, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, red, green, blue, alpha);
            case NORTH -> emitQuad(buffer, matrix, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, red, green, blue, alpha);
            case SOUTH -> emitQuad(buffer, matrix, x0, y0, z1, x0, y1, z1, x1, y1, z1, x1, y0, z1, red, green, blue, alpha);
            case WEST -> emitQuad(buffer, matrix, x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1, red, green, blue, alpha);
            case EAST -> emitQuad(buffer, matrix, x1, y0, z1, x1, y1, z1, x1, y1, z0, x1, y0, z0, red, green, blue, alpha);
        }
    }

    private static void emitQuad(BufferBuilder buffer, Matrix4f matrix,
                                 double x0, double y0, double z0,
                                 double x1, double y1, double z1,
                                 double x2, double y2, double z2,
                                 double x3, double y3, double z3,
                                 float red, float green, float blue, float alpha) {
        buffer.vertex(matrix, (float) x0, (float) y0, (float) z0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, (float) x3, (float) y3, (float) z3).color(red, green, blue, alpha).endVertex();
    }
}
