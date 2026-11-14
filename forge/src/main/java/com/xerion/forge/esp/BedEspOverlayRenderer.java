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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class BedEspOverlayRenderer {
    private static final float LINE_WIDTH = 1.0f;
    private static final float FILL_ALPHA = 0.24f;
    private static final double BED_HEIGHT = 9.0D / 16.0D;
    private static final int RESET_DISTANCE_BLOCKS = 4;
    private static final long SCAN_INTERVAL_MILLIS = 500L;
    private static final Set<BlockPos> CACHED_BEDS = new HashSet<>();
    private static final Map<BlockPos, Direction> CACHED_FACING = new HashMap<>();
    private static BlockPos cachedScanCenter;
    private static int cachedRange = -1;
    private static int currentRadius;
    private static long lastScanMillis;

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        if (!VisualSettings.bedEspEnabled()) {
            clearScanCache();
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        int range = Math.max(1, Math.round(VisualSettings.bedEspRange()));
        BlockPos center = minecraft.player.blockPosition();
        updateScanOrigin(center, range);
        scanTick(minecraft.level, center, range);
        pruneInvalidCachedBeds(minecraft.level);

        Set<BlockPos> beds = new HashSet<>(CACHED_BEDS);
        if (beds.isEmpty()) {
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

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        if (VisualSettings.bedEspThroughWalls()) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
        } else {
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
        }
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(LINE_WIDTH);

        int color = VisualSettings.bedEspColor();
        try {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder fillBuffer = Tesselator.getInstance().getBuilder();
            fillBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (BlockPos foot : beds) {
                Direction facing = CACHED_FACING.getOrDefault(foot, Direction.NORTH);
                emitFilledBox(fillBuffer, matrix, bedBox(foot, facing), cameraPos, color);
            }
            BufferUploader.drawWithShader(fillBuffer.end());

            RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
            BufferBuilder lineBuffer = Tesselator.getInstance().getBuilder();
            lineBuffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            for (BlockPos foot : beds) {
                Direction facing = CACHED_FACING.getOrDefault(foot, Direction.NORTH);
                emitOutline(lineBuffer, matrix, bedBox(foot, facing), cameraPos, color);
            }
            BufferUploader.drawWithShader(lineBuffer.end());
        } finally {
            RenderSystem.setProjectionMatrix(previousProjection, VertexSorting.DISTANCE_TO_ORIGIN);
            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();
            EspRenderState.restoreWorldOverlayState();
        }

    }

    private static void updateScanOrigin(BlockPos center, int range) {
        if (cachedScanCenter == null || cachedRange != range || center.distManhattan(cachedScanCenter) > RESET_DISTANCE_BLOCKS) {
            cachedScanCenter = center;
            cachedRange = range;
            currentRadius = 0;
            pruneOutOfRange(center, range);
        }
    }

    public static void clearScanCache() {
        CACHED_BEDS.clear();
        CACHED_FACING.clear();
        cachedScanCenter = null;
        cachedRange = -1;
        currentRadius = 0;
        lastScanMillis = 0L;
    }

    private static void scanTick(Level level, BlockPos center, int range) {
        long now = System.currentTimeMillis();
        if (lastScanMillis != 0L && now - lastScanMillis < SCAN_INTERVAL_MILLIS) {
            return;
        }
        lastScanMillis = now;

        pruneOutOfRange(center, range);
        if (currentRadius == 0) {
            collectBeds(level, center, range, 0);
            currentRadius = Math.min(2, range);
            return;
        }
        if (currentRadius >= range) {
            currentRadius = 0;
            return;
        }
        collectBeds(level, center, range, currentRadius);
        currentRadius++;
    }

    private static void collectBeds(Level level, BlockPos center, int range, int radius) {
        int outerRadius = radius == 0 ? Math.min(2, range) : Math.min(radius + 1, range);
        int innerSq = radius == 0 ? -1 : radius * radius;
        int outerSq = outerRadius * outerRadius;

        for (int x = center.getX() - outerRadius; x <= center.getX() + outerRadius; x++) {
            for (int y = center.getY() - outerRadius; y <= center.getY() + outerRadius; y++) {
                for (int z = center.getZ() - outerRadius; z <= center.getZ() + outerRadius; z++) {
                    int dx = x - center.getX();
                    int dy = y - center.getY();
                    int dz = z - center.getZ();
                    int distanceSq = dx * dx + dy * dy + dz * dz;
                    if (distanceSq > outerSq || distanceSq <= innerSq) {
                        continue;
                    }

                    BlockPos pos = new BlockPos(x, y, z);
                    BedTarget bed = bedTarget(level, pos);
                    if (bed != null) {
                        CACHED_BEDS.add(bed.foot());
                        CACHED_FACING.put(bed.foot(), bed.facing());
                    } else {
                        CACHED_BEDS.remove(pos);
                        CACHED_FACING.remove(pos);
                    }
                }
            }
        }
    }

    private static void pruneInvalidCachedBeds(Level level) {
        CACHED_BEDS.removeIf(foot -> {
            BedTarget bed = bedTarget(level, foot);
            if (bed == null || !bed.foot().equals(foot)) {
                CACHED_FACING.remove(foot);
                return true;
            }
            CACHED_FACING.put(foot, bed.facing());
            return false;
        });
    }

    private static void pruneOutOfRange(BlockPos center, int range) {
        int rangeSq = range * range;
        CACHED_BEDS.removeIf(pos -> {
            if (distanceSq(center, pos) <= rangeSq) {
                return false;
            }
            CACHED_FACING.remove(pos);
            return true;
        });
    }

    private static BedTarget bedTarget(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BedBlock)) {
            return null;
        }
        Direction facing = state.getValue(BedBlock.FACING);
        BlockPos foot = state.getValue(BedBlock.PART) == BedPart.FOOT
                ? pos.immutable()
                : pos.relative(facing.getOpposite()).immutable();
        BlockState footState = level.getBlockState(foot);
        if (!(footState.getBlock() instanceof BedBlock)
                || footState.getValue(BedBlock.PART) != BedPart.FOOT
                || footState.getValue(BedBlock.FACING) != facing) {
            return null;
        }
        return new BedTarget(foot, facing);
    }

    private static int distanceSq(BlockPos center, BlockPos pos) {
        int dx = pos.getX() - center.getX();
        int dy = pos.getY() - center.getY();
        int dz = pos.getZ() - center.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static AABB bedBox(BlockPos foot, Direction facing) {
        BlockPos head = foot.relative(facing);
        int minX = Math.min(foot.getX(), head.getX());
        int minZ = Math.min(foot.getZ(), head.getZ());
        int maxX = Math.max(foot.getX(), head.getX()) + 1;
        int maxZ = Math.max(foot.getZ(), head.getZ()) + 1;
        return new AABB(minX, foot.getY(), minZ, maxX, foot.getY() + BED_HEIGHT, maxZ);
    }

    private static void emitFilledBox(BufferBuilder buffer, Matrix4f matrix, AABB box, Vec3 cameraPos, int color) {
        for (Direction direction : Direction.values()) {
            emitFaceQuad(buffer, matrix, box, cameraPos, direction, color);
        }
    }

    private static void emitOutline(BufferBuilder buffer, Matrix4f matrix, AABB box, Vec3 cameraPos, int color) {
        double x0 = box.minX - cameraPos.x;
        double y0 = box.minY - cameraPos.y;
        double z0 = box.minZ - cameraPos.z;
        double x1 = box.maxX - cameraPos.x;
        double y1 = box.maxY - cameraPos.y;
        double z1 = box.maxZ - cameraPos.z;

        emitLine(buffer, matrix, x0, y0, z0, x1, y0, z0, color);
        emitLine(buffer, matrix, x1, y0, z0, x1, y0, z1, color);
        emitLine(buffer, matrix, x1, y0, z1, x0, y0, z1, color);
        emitLine(buffer, matrix, x0, y0, z1, x0, y0, z0, color);
        emitLine(buffer, matrix, x0, y1, z0, x1, y1, z0, color);
        emitLine(buffer, matrix, x1, y1, z0, x1, y1, z1, color);
        emitLine(buffer, matrix, x1, y1, z1, x0, y1, z1, color);
        emitLine(buffer, matrix, x0, y1, z1, x0, y1, z0, color);
        emitLine(buffer, matrix, x0, y0, z0, x0, y1, z0, color);
        emitLine(buffer, matrix, x1, y0, z0, x1, y1, z0, color);
        emitLine(buffer, matrix, x1, y0, z1, x1, y1, z1, color);
        emitLine(buffer, matrix, x0, y0, z1, x0, y1, z1, color);
    }

    private static void emitFaceQuad(BufferBuilder buffer, Matrix4f matrix, AABB box, Vec3 cameraPos,
                                     Direction direction, int color) {
        float red = ((color >>> 16) & 0xFF) / 255.0f;
        float green = ((color >>> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        float alpha = ((color >>> 24) & 0xFF) / 255.0f * FILL_ALPHA;
        double x0 = box.minX - cameraPos.x;
        double y0 = box.minY - cameraPos.y;
        double z0 = box.minZ - cameraPos.z;
        double x1 = box.maxX - cameraPos.x;
        double y1 = box.maxY - cameraPos.y;
        double z1 = box.maxZ - cameraPos.z;

        switch (direction) {
            case DOWN -> emitQuad(buffer, matrix, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, red, green, blue, alpha, 0.0f, -1.0f, 0.0f);
            case UP -> emitQuad(buffer, matrix, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, red, green, blue, alpha, 0.0f, 1.0f, 0.0f);
            case NORTH -> emitQuad(buffer, matrix, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, red, green, blue, alpha, 0.0f, 0.0f, -1.0f);
            case SOUTH -> emitQuad(buffer, matrix, x0, y0, z1, x0, y1, z1, x1, y1, z1, x1, y0, z1, red, green, blue, alpha, 0.0f, 0.0f, 1.0f);
            case WEST -> emitQuad(buffer, matrix, x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1, red, green, blue, alpha, -1.0f, 0.0f, 0.0f);
            case EAST -> emitQuad(buffer, matrix, x1, y0, z1, x1, y1, z1, x1, y1, z0, x1, y0, z0, red, green, blue, alpha, 1.0f, 0.0f, 0.0f);
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

    private static void emitLine(BufferBuilder buffer, Matrix4f matrix,
                                 double x0, double y0, double z0,
                                 double x1, double y1, double z1,
                                 int color) {
        float red = ((color >>> 16) & 0xFF) / 255.0f;
        float green = ((color >>> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        Vector3f normal = lineNormal(x0, y0, z0, x1, y1, z1);
        buffer.vertex(matrix, (float) x0, (float) y0, (float) z0).color(red, green, blue, 1.0f).normal(normal.x(), normal.y(), normal.z()).endVertex();
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(red, green, blue, 1.0f).normal(normal.x(), normal.y(), normal.z()).endVertex();
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

    private record BedTarget(BlockPos foot, Direction facing) {
    }
}
