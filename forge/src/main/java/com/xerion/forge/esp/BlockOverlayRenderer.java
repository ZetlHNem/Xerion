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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public final class BlockOverlayRenderer {
    private static final float LINE_WIDTH = 1.0f;

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        if (!VisualSettings.blockOverlayEnabled()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        BlockHitResult hit = crosshairBlock(minecraft);
        if (hit == null) {
            return;
        }
        BlockPos targetPos = hit.getBlockPos();
        Direction targetFace = hit.getDirection();

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

        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(LINE_WIDTH);

        String mode = VisualSettings.blockOverlayShapeMode();
        boolean drawSides = !"Lines".equalsIgnoreCase(mode);
        boolean drawLines = !"Sides".equalsIgnoreCase(mode);
        boolean singleSide = VisualSettings.blockOverlaySingleSide();
        try {
            if (drawSides) {
                BufferBuilder fillBuffer = Tesselator.getInstance().getBuilder();
                fillBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                emitFilledBox(fillBuffer, matrix, targetPos, cameraPos, VisualSettings.blockOverlaySideColor(),
                        singleSide ? targetFace : null);
                BufferUploader.drawWithShader(fillBuffer.end());
            }

            if (drawLines) {
                BufferBuilder lineBuffer = Tesselator.getInstance().getBuilder();
                lineBuffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
                emitOutline(lineBuffer, matrix, targetPos, cameraPos, VisualSettings.blockOverlayLineColor(),
                        singleSide ? targetFace : null);
                BufferUploader.drawWithShader(lineBuffer.end());
            }
        } finally {
            RenderSystem.setProjectionMatrix(previousProjection, VertexSorting.DISTANCE_TO_ORIGIN);
            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();
            EspRenderState.restoreWorldOverlayState();
        }

    }

    private static BlockHitResult crosshairBlock(Minecraft minecraft) {
        if (minecraft.hitResult instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = minecraft.level.getBlockState(pos);
            return state.isAir() ? null : blockHit;
        }
        return null;
    }

    private static void emitFilledBox(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, Vec3 cameraPos, int color,
                                      Direction onlyFace) {
        AABB box = blockBox(pos);
        for (Direction direction : Direction.values()) {
            if (onlyFace != null && direction != onlyFace) {
                continue;
            }
            emitFaceQuad(buffer, matrix, box, cameraPos, direction, color);
        }
    }

    private static void emitOutline(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, Vec3 cameraPos, int color,
                                    Direction onlyFace) {
        AABB box = blockBox(pos);
        for (Direction direction : Direction.values()) {
            if (onlyFace != null && direction != onlyFace) {
                continue;
            }
            emitFaceEdges(buffer, matrix, box, cameraPos, direction, color);
        }
    }

    private static AABB blockBox(BlockPos pos) {
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    private static void emitFaceQuad(BufferBuilder buffer, Matrix4f matrix, AABB box, Vec3 cameraPos,
                                     Direction direction, int color) {
        float red = ((color >>> 16) & 0xFF) / 255.0f;
        float green = ((color >>> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        float a = ((color >>> 24) & 0xFF) / 255.0f;
        double x0 = box.minX - cameraPos.x;
        double y0 = box.minY - cameraPos.y;
        double z0 = box.minZ - cameraPos.z;
        double x1 = box.maxX - cameraPos.x;
        double y1 = box.maxY - cameraPos.y;
        double z1 = box.maxZ - cameraPos.z;
        switch (direction) {
            case DOWN -> emitQuad(buffer, matrix, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, red, green, blue, a, 0.0f, -1.0f, 0.0f);
            case UP -> emitQuad(buffer, matrix, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, red, green, blue, a, 0.0f, 1.0f, 0.0f);
            case NORTH -> emitQuad(buffer, matrix, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, red, green, blue, a, 0.0f, 0.0f, -1.0f);
            case SOUTH -> emitQuad(buffer, matrix, x0, y0, z1, x0, y1, z1, x1, y1, z1, x1, y0, z1, red, green, blue, a, 0.0f, 0.0f, 1.0f);
            case WEST -> emitQuad(buffer, matrix, x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1, red, green, blue, a, -1.0f, 0.0f, 0.0f);
            case EAST -> emitQuad(buffer, matrix, x1, y0, z1, x1, y1, z1, x1, y1, z0, x1, y0, z0, red, green, blue, a, 1.0f, 0.0f, 0.0f);
        }
    }

    private static void emitFaceEdges(BufferBuilder buffer, Matrix4f matrix, AABB box, Vec3 cameraPos,
                                      Direction direction, int color) {
        int x0 = (int) box.minX;
        int y0 = (int) box.minY;
        int z0 = (int) box.minZ;
        int x1 = (int) box.maxX;
        int y1 = (int) box.maxY;
        int z1 = (int) box.maxZ;
        switch (direction) {
            case DOWN -> {
                emitLine(buffer, matrix, cameraPos, x0, y0, z0, x1, y0, z0, color);
                emitLine(buffer, matrix, cameraPos, x1, y0, z0, x1, y0, z1, color);
                emitLine(buffer, matrix, cameraPos, x1, y0, z1, x0, y0, z1, color);
                emitLine(buffer, matrix, cameraPos, x0, y0, z1, x0, y0, z0, color);
            }
            case UP -> {
                emitLine(buffer, matrix, cameraPos, x0, y1, z0, x1, y1, z0, color);
                emitLine(buffer, matrix, cameraPos, x1, y1, z0, x1, y1, z1, color);
                emitLine(buffer, matrix, cameraPos, x1, y1, z1, x0, y1, z1, color);
                emitLine(buffer, matrix, cameraPos, x0, y1, z1, x0, y1, z0, color);
            }
            case NORTH -> {
                emitLine(buffer, matrix, cameraPos, x0, y0, z0, x1, y0, z0, color);
                emitLine(buffer, matrix, cameraPos, x1, y0, z0, x1, y1, z0, color);
                emitLine(buffer, matrix, cameraPos, x1, y1, z0, x0, y1, z0, color);
                emitLine(buffer, matrix, cameraPos, x0, y1, z0, x0, y0, z0, color);
            }
            case SOUTH -> {
                emitLine(buffer, matrix, cameraPos, x0, y0, z1, x1, y0, z1, color);
                emitLine(buffer, matrix, cameraPos, x1, y0, z1, x1, y1, z1, color);
                emitLine(buffer, matrix, cameraPos, x1, y1, z1, x0, y1, z1, color);
                emitLine(buffer, matrix, cameraPos, x0, y1, z1, x0, y0, z1, color);
            }
            case WEST -> {
                emitLine(buffer, matrix, cameraPos, x0, y0, z0, x0, y0, z1, color);
                emitLine(buffer, matrix, cameraPos, x0, y0, z1, x0, y1, z1, color);
                emitLine(buffer, matrix, cameraPos, x0, y1, z1, x0, y1, z0, color);
                emitLine(buffer, matrix, cameraPos, x0, y1, z0, x0, y0, z0, color);
            }
            case EAST -> {
                emitLine(buffer, matrix, cameraPos, x1, y0, z0, x1, y0, z1, color);
                emitLine(buffer, matrix, cameraPos, x1, y0, z1, x1, y1, z1, color);
                emitLine(buffer, matrix, cameraPos, x1, y1, z1, x1, y1, z0, color);
                emitLine(buffer, matrix, cameraPos, x1, y1, z0, x1, y0, z0, color);
            }
        }
    }

    private static void emitQuad(BufferBuilder buffer, Matrix4f matrix,
                                 double x0, double y0, double z0,
                                 double x1, double y1, double z1,
                                 double x2, double y2, double z2,
                                 double x3, double y3, double z3,
                                 float red, float green, float blue, float alpha,
                                 float nx, float ny, float nz) {
        buffer.vertex(matrix, (float) x0, (float) y0, (float) z0).color(red, green, blue, alpha).normal(nx, ny, nz).endVertex();
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(red, green, blue, alpha).normal(nx, ny, nz).endVertex();
        buffer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(red, green, blue, alpha).normal(nx, ny, nz).endVertex();
        buffer.vertex(matrix, (float) x3, (float) y3, (float) z3).color(red, green, blue, alpha).normal(nx, ny, nz).endVertex();
    }

    private static void emitLine(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPos,
                                 int x0, int y0, int z0, int x1, int y1, int z1,
                                 int color) {
        float red = ((color >>> 16) & 0xFF) / 255.0f;
        float green = ((color >>> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        Vector3f normal = lineNormal(x1 - x0, y1 - y0, z1 - z0);
        buffer.vertex(matrix, (float) (x0 - cameraPos.x), (float) (y0 - cameraPos.y), (float) (z0 - cameraPos.z)).color(red, green, blue, 1.0f).normal(normal.x(), normal.y(), normal.z()).endVertex();
        buffer.vertex(matrix, (float) (x1 - cameraPos.x), (float) (y1 - cameraPos.y), (float) (z1 - cameraPos.z)).color(red, green, blue, 1.0f).normal(normal.x(), normal.y(), normal.z()).endVertex();
    }

    private static Vector3f lineNormal(double dx, double dy, double dz) {
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= 0.00001D) {
            return new Vector3f(0.0f, 1.0f, 0.0f);
        }
        return new Vector3f((float) (dx / length), (float) (dy / length), (float) (dz / length));
    }
}
