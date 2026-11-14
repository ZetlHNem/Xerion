package com.xerion.forge.render;

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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class VeinMinerPreviewRenderer {
    private static final float LINE_WIDTH = 1.5f;

    private VeinMinerPreviewRenderer() {
    }

    public static void render(RenderLevelStageEvent event, boolean keyWasDown, List<BlockPos> selectedTargets, BlockPos seedPos) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER
                || !VisualSettings.veinMinerEnabled()
                || !keyWasDown
                || (selectedTargets.isEmpty() && seedPos == null)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        Camera camera = event.getCamera() == null ? minecraft.gameRenderer.getMainCamera() : event.getCamera();
        if (camera == null) {
            return;
        }

        PoseStack cameraPoseStack = new PoseStack();
        cameraPoseStack.mulPoseMatrix(ModelGlowRenderer.cameraRelativeMatrix(event.getPoseStack().last().pose()));
        Matrix4f matrix = cameraPoseStack.last().pose();
        Vec3 cameraPos = camera.getPosition();

        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f(event.getProjectionMatrix()), VertexSorting.DISTANCE_TO_ORIGIN);
        minecraft.getMainRenderTarget().bindWrite(false);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.lineWidth(LINE_WIDTH);

        try {
            Set<BlockPos> targets = new HashSet<>(selectedTargets);
            if (seedPos != null) {
                targets.add(seedPos);
            }
            if (targets.isEmpty()) {
                return;
            }
            int color = VisualSettings.veinMinerColor();
            BufferBuilder lineBuffer = Tesselator.getInstance().getBuilder();
            lineBuffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            Map<EdgeKey, Boolean> emittedEdges = new HashMap<>();
            for (BlockPos pos : targets) {
                emitVisibleEdges(lineBuffer, matrix, cameraPos, pos, targets, emittedEdges, color);
            }
            BufferUploader.drawWithShader(lineBuffer.end());
        } finally {
            RenderSystem.lineWidth(1.0f);
            RenderSystem.depthMask(true);
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(previousProjection, VertexSorting.DISTANCE_TO_ORIGIN);
            minecraft.getMainRenderTarget().bindWrite(false);
        }
    }

    private static void emitVisibleEdges(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPos,
                                         BlockPos pos, Set<BlockPos> blocks, Map<EdgeKey, Boolean> emittedEdges, int color) {
        for (Direction direction : Direction.values()) {
            if (!blocks.contains(pos.relative(direction))) {
                emitFaceEdges(buffer, matrix, cameraPos, pos, direction, blocks, emittedEdges, color);
            }
        }
    }

    private static void emitFaceEdges(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPos,
                                      BlockPos pos, Direction direction, Set<BlockPos> blocks,
                                      Map<EdgeKey, Boolean> emittedEdges, int color) {
        int x0 = pos.getX();
        int y0 = pos.getY();
        int z0 = pos.getZ();
        int x1 = pos.getX() + 1;
        int y1 = pos.getY() + 1;
        int z1 = pos.getZ() + 1;
        switch (direction) {
            case DOWN -> {
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.NORTH, x0, y0, z0, x1, y0, z0, blocks, emittedEdges, color);
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.EAST, x1, y0, z0, x1, y0, z1, blocks, emittedEdges, color);
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.SOUTH, x1, y0, z1, x0, y0, z1, blocks, emittedEdges, color);
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.WEST, x0, y0, z1, x0, y0, z0, blocks, emittedEdges, color);
            }
            case UP -> {
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.NORTH, x0, y1, z0, x1, y1, z0, blocks, emittedEdges, color);
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.EAST, x1, y1, z0, x1, y1, z1, blocks, emittedEdges, color);
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.SOUTH, x1, y1, z1, x0, y1, z1, blocks, emittedEdges, color);
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.WEST, x0, y1, z1, x0, y1, z0, blocks, emittedEdges, color);
            }
            case NORTH -> {
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.DOWN, x0, y0, z0, x1, y0, z0, blocks, emittedEdges, color);
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.EAST, x1, y0, z0, x1, y1, z0, blocks, emittedEdges, color);
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.UP, x1, y1, z0, x0, y1, z0, blocks, emittedEdges, color);
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.WEST, x0, y1, z0, x0, y0, z0, blocks, emittedEdges, color);
            }
            case SOUTH -> {
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.DOWN, x0, y0, z1, x1, y0, z1, blocks, emittedEdges, color);
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.EAST, x1, y0, z1, x1, y1, z1, blocks, emittedEdges, color);
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.UP, x1, y1, z1, x0, y1, z1, blocks, emittedEdges, color);
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.WEST, x0, y1, z1, x0, y0, z1, blocks, emittedEdges, color);
            }
            case WEST -> {
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.DOWN, x0, y0, z0, x0, y0, z1, blocks, emittedEdges, color);
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.SOUTH, x0, y0, z1, x0, y1, z1, blocks, emittedEdges, color);
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.UP, x0, y1, z1, x0, y1, z0, blocks, emittedEdges, color);
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.NORTH, x0, y1, z0, x0, y0, z0, blocks, emittedEdges, color);
            }
            case EAST -> {
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.DOWN, x1, y0, z0, x1, y0, z1, blocks, emittedEdges, color);
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.SOUTH, x1, y0, z1, x1, y1, z1, blocks, emittedEdges, color);
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.UP, x1, y1, z1, x1, y1, z0, blocks, emittedEdges, color);
                emitFaceEdge(buffer, matrix, cameraPos, pos, direction, Direction.NORTH, x1, y1, z0, x1, y0, z0, blocks, emittedEdges, color);
            }
        }
    }

    private static void emitFaceEdge(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPos,
                                     BlockPos pos, Direction faceDirection, Direction edgeSide,
                                     int x0, int y0, int z0, int x1, int y1, int z1,
                                     Set<BlockPos> blocks, Map<EdgeKey, Boolean> emittedEdges, int color) {
        boolean exposed = !hasCoplanarSelectedFace(pos, faceDirection, edgeSide, blocks);
        emitGridLine(buffer, matrix, cameraPos, x0, y0, z0, x1, y1, z1, emittedEdges, color, exposed);
    }

    private static boolean hasCoplanarSelectedFace(BlockPos pos, Direction faceDirection,
                                                   Direction edgeSide, Set<BlockPos> blocks) {
        BlockPos neighbor = pos.relative(edgeSide);
        return blocks.contains(neighbor) && !blocks.contains(neighbor.relative(faceDirection));
    }

    private static void emitGridLine(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPos,
                                     int x0, int y0, int z0, int x1, int y1, int z1,
                                     Map<EdgeKey, Boolean> emittedEdges, int color, boolean exposed) {
        EdgeKey edge = EdgeKey.of(x0, y0, z0, x1, y1, z1);
        Boolean previousExposed = emittedEdges.get(edge);
        if (previousExposed != null && (previousExposed || !exposed)) {
            return;
        }
        emittedEdges.put(edge, exposed);
        float alpha = ((color >>> 24) & 0xFF) / 255.0f * (exposed ? 1.0f : 0.35f);
        float red = ((color >>> 16) & 0xFF) / 255.0f;
        float green = ((color >>> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        Vector3f normal = lineNormal(x0, y0, z0, x1, y1, z1);
        buffer.vertex(matrix, (float) (x0 - cameraPos.x), (float) (y0 - cameraPos.y), (float) (z0 - cameraPos.z))
                .color(red, green, blue, alpha)
                .normal(normal.x(), normal.y(), normal.z())
                .endVertex();
        buffer.vertex(matrix, (float) (x1 - cameraPos.x), (float) (y1 - cameraPos.y), (float) (z1 - cameraPos.z))
                .color(red, green, blue, alpha)
                .normal(normal.x(), normal.y(), normal.z())
                .endVertex();
    }

    private static Vector3f lineNormal(double x0, double y0, double z0, double x1, double y1, double z1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double dz = z1 - z0;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= 0.00001D) {
            return new Vector3f(0.0f, 1.0f, 0.0f);
        }
        return new Vector3f((float) (dx / length), (float) (dy / length), (float) (dz / length));
    }

    private record EdgeKey(int ax, int ay, int az, int bx, int by, int bz) {
        private static EdgeKey of(int x0, int y0, int z0, int x1, int y1, int z1) {
            if (before(x1, y1, z1, x0, y0, z0)) {
                return new EdgeKey(x1, y1, z1, x0, y0, z0);
            }
            return new EdgeKey(x0, y0, z0, x1, y1, z1);
        }

        private static boolean before(int ax, int ay, int az, int bx, int by, int bz) {
            if (ax != bx) {
                return ax < bx;
            }
            if (ay != by) {
                return ay < by;
            }
            return az < bz;
        }
    }
}
