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
import com.xerion.forge.gui.font.MSDFRenderer;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class SpawnerEspOverlayRenderer {
    private static final float LINE_WIDTH = 1.0f;
    private static final float FILL_ALPHA = 0.28f;
    private static final float LABEL_FONT_SIZE = 12.0f;
    private static final int LABEL_COLOR = 0xFFFFFFFF;
    private static final int RESET_DISTANCE_BLOCKS = 4;
    private static final long SCAN_INTERVAL_MILLIS = 500L;
    private static final Set<BlockPos> CACHED_SPAWNERS = new HashSet<>();
    private static final Map<BlockPos, String> CACHED_TYPES = new HashMap<>();
    private static int cachedRange = -1;
    private static int currentRadius;
    private static long lastScanMillis;
    private static BlockPos cachedScanCenter;

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        if (!VisualSettings.spawnerEspEnabled()) {
            clearScanCache();
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        int range = Math.max(1, Math.round(VisualSettings.spawnerEspRange()));
        BlockPos center = minecraft.player.blockPosition();
        updateScanOrigin(center, range);
        scanTick(minecraft.level, center, range);
        pruneInvalidCachedSpawners(minecraft.level);

        Set<BlockPos> spawners = new HashSet<>(CACHED_SPAWNERS);
        if (spawners.isEmpty()) {
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

        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        if (VisualSettings.spawnerEspThroughWalls()) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
        } else {
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
        }
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(LINE_WIDTH);

        int color = VisualSettings.spawnerEspColor();
        try {
            BufferBuilder fillBuffer = Tesselator.getInstance().getBuilder();
            fillBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (BlockPos pos : spawners) {
                emitFilledBox(fillBuffer, matrix, pos, cameraPos, color);
            }
            BufferUploader.drawWithShader(fillBuffer.end());

            BufferBuilder lineBuffer = Tesselator.getInstance().getBuilder();
            lineBuffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            Set<EdgeKey> emittedEdges = new HashSet<>();
            for (BlockPos pos : spawners) {
                emitOutline(lineBuffer, matrix, pos, cameraPos, emittedEdges, color);
            }
            BufferUploader.drawWithShader(lineBuffer.end());

            if (VisualSettings.spawnerEspShowType()) {
                renderLabels(cameraPoseStack, camera, event.getProjectionMatrix(), spawners);
            }
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
        CACHED_SPAWNERS.clear();
        CACHED_TYPES.clear();
        cachedRange = -1;
        cachedScanCenter = null;
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
            collectSpawners(level, center, range, 0);
            currentRadius = Math.min(2, range);
            return;
        }
        if (currentRadius >= range) {
            currentRadius = 0;
            return;
        }
        collectSpawners(level, center, range, currentRadius);
        currentRadius++;
    }

    private static void collectSpawners(Level level, BlockPos center, int range, int radius) {
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
                    if (isSpawner(level, pos)) {
                        CACHED_SPAWNERS.add(pos);
                        CACHED_TYPES.put(pos, spawnerType(level, pos));
                    } else {
                        CACHED_SPAWNERS.remove(pos);
                        CACHED_TYPES.remove(pos);
                    }
                }
            }
        }
    }

    private static void pruneInvalidCachedSpawners(Level level) {
        CACHED_SPAWNERS.removeIf(pos -> {
            if (isSpawner(level, pos)) {
                CACHED_TYPES.put(pos, spawnerType(level, pos));
                return false;
            }
            CACHED_TYPES.remove(pos);
            return true;
        });
    }

    private static void pruneOutOfRange(BlockPos center, int range) {
        int rangeSq = range * range;
        CACHED_SPAWNERS.removeIf(pos -> {
            if (distanceSq(center, pos) <= rangeSq) {
                return false;
            }
            CACHED_TYPES.remove(pos);
            return true;
        });
    }

    private static boolean isSpawner(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(Blocks.SPAWNER) || level.getBlockEntity(pos) instanceof SpawnerBlockEntity;
    }

    private static String spawnerType(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SpawnerBlockEntity spawnerBlockEntity) {
            Entity entity = spawnerBlockEntity.getSpawner().getOrCreateDisplayEntity(level, level.random, pos);
            if (entity != null) {
                return entity.getType().getDescription().getString();
            }
        }
        return "Spawner";
    }

    private static int distanceSq(BlockPos center, BlockPos pos) {
        int dx = pos.getX() - center.getX();
        int dy = pos.getY() - center.getY();
        int dz = pos.getZ() - center.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static void renderLabels(PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, Set<BlockPos> spawners) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        Vec3 cameraPos = camera.getPosition();
        Vec3 playerPos = minecraft.player.position();
        try (MSDFRenderer.WorldTextBatch batch = MSDFRenderer.beginWorldTextBatch(true)) {
            for (BlockPos pos : spawners) {
                WorldPoint anchor = new WorldPoint(
                        pos.getX() + 0.5D - cameraPos.x,
                        pos.getY() + 1.15D - cameraPos.y,
                        pos.getZ() + 0.5D - cameraPos.z
                );
                float scale = fixedBillboardScaleRelative(camera, anchor, projectionMatrix, 1.0f);
                if (scale <= 0.0f) {
                    continue;
                }
                double distance = Math.sqrt(playerPos.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D));
                String spawnerName = minecraft.level.getBlockState(pos).getBlock().getName().getString();
                String text = CACHED_TYPES.getOrDefault(pos, "Spawner")
                        + spawnerName
                        + " [" + String.format(Locale.ROOT, "%.1fm", distance) + "]";
                float textW = MSDFRenderer.width(text, LABEL_FONT_SIZE);

                poseStack.pushPose();
                try {
                    poseStack.translate(anchor.x(), anchor.y(), anchor.z());
                    poseStack.mulPose(camera.rotation());
                    poseStack.scale(-scale, -scale, scale);
                    MSDFRenderer.drawWorldTextWithShadow(batch, poseStack.last().pose(), text, -textW * 0.5f, 0.0f,
                            LABEL_FONT_SIZE, LABEL_COLOR);
                } finally {
                    poseStack.popPose();
                }
            }
        }
    }

    private static float fixedBillboardScaleRelative(Camera camera, WorldPoint anchor, Matrix4f projectionMatrix, float pixelScale) {
        Vector3f look = camera.getLookVector();
        double depth = anchor.x() * look.x() + anchor.y() * look.y() + anchor.z() * look.z();
        if (depth <= 0.01D) {
            return -1.0f;
        }
        float projectionY = Math.abs(projectionMatrix.m11());
        int viewportH = Minecraft.getInstance().getWindow().getHeight();
        if (projectionY <= 0.0001f || viewportH <= 0) {
            return 0.025f * pixelScale;
        }
        return (float) (2.0D * Math.max(0.25D, depth) / (projectionY * viewportH)) * pixelScale;
    }

    private static void emitFilledBox(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, Vec3 cameraPos, int color) {
        AABB box = blockBox(pos);
        for (Direction direction : Direction.values()) {
            emitFaceQuad(buffer, matrix, box, cameraPos, direction, color, FILL_ALPHA);
        }
    }

    private static void emitOutline(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, Vec3 cameraPos,
                                    Set<EdgeKey> emittedEdges, int color) {
        AABB box = blockBox(pos);
        for (Direction direction : Direction.values()) {
            emitFaceEdges(buffer, matrix, box, cameraPos, direction, emittedEdges, color);
        }
    }

    private static AABB blockBox(BlockPos pos) {
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    private static void emitFaceQuad(BufferBuilder buffer, Matrix4f matrix, AABB box, Vec3 cameraPos,
                                     Direction direction, int color, float alpha) {
        float red = ((color >>> 16) & 0xFF) / 255.0f;
        float green = ((color >>> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        float a = ((color >>> 24) & 0xFF) / 255.0f * alpha;
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
                                      Direction direction, Set<EdgeKey> emittedEdges, int color) {
        int x0 = (int) box.minX;
        int y0 = (int) box.minY;
        int z0 = (int) box.minZ;
        int x1 = (int) box.maxX;
        int y1 = (int) box.maxY;
        int z1 = (int) box.maxZ;
        switch (direction) {
            case DOWN -> {
                emitGridLine(buffer, matrix, cameraPos, x0, y0, z0, x1, y0, z0, emittedEdges, color);
                emitGridLine(buffer, matrix, cameraPos, x1, y0, z0, x1, y0, z1, emittedEdges, color);
                emitGridLine(buffer, matrix, cameraPos, x1, y0, z1, x0, y0, z1, emittedEdges, color);
                emitGridLine(buffer, matrix, cameraPos, x0, y0, z1, x0, y0, z0, emittedEdges, color);
            }
            case UP -> {
                emitGridLine(buffer, matrix, cameraPos, x0, y1, z0, x1, y1, z0, emittedEdges, color);
                emitGridLine(buffer, matrix, cameraPos, x1, y1, z0, x1, y1, z1, emittedEdges, color);
                emitGridLine(buffer, matrix, cameraPos, x1, y1, z1, x0, y1, z1, emittedEdges, color);
                emitGridLine(buffer, matrix, cameraPos, x0, y1, z1, x0, y1, z0, emittedEdges, color);
            }
            case NORTH -> {
                emitGridLine(buffer, matrix, cameraPos, x0, y0, z0, x1, y0, z0, emittedEdges, color);
                emitGridLine(buffer, matrix, cameraPos, x1, y0, z0, x1, y1, z0, emittedEdges, color);
                emitGridLine(buffer, matrix, cameraPos, x1, y1, z0, x0, y1, z0, emittedEdges, color);
                emitGridLine(buffer, matrix, cameraPos, x0, y1, z0, x0, y0, z0, emittedEdges, color);
            }
            case SOUTH -> {
                emitGridLine(buffer, matrix, cameraPos, x0, y0, z1, x1, y0, z1, emittedEdges, color);
                emitGridLine(buffer, matrix, cameraPos, x1, y0, z1, x1, y1, z1, emittedEdges, color);
                emitGridLine(buffer, matrix, cameraPos, x1, y1, z1, x0, y1, z1, emittedEdges, color);
                emitGridLine(buffer, matrix, cameraPos, x0, y1, z1, x0, y0, z1, emittedEdges, color);
            }
            case WEST -> {
                emitGridLine(buffer, matrix, cameraPos, x0, y0, z0, x0, y0, z1, emittedEdges, color);
                emitGridLine(buffer, matrix, cameraPos, x0, y0, z1, x0, y1, z1, emittedEdges, color);
                emitGridLine(buffer, matrix, cameraPos, x0, y1, z1, x0, y1, z0, emittedEdges, color);
                emitGridLine(buffer, matrix, cameraPos, x0, y1, z0, x0, y0, z0, emittedEdges, color);
            }
            case EAST -> {
                emitGridLine(buffer, matrix, cameraPos, x1, y0, z0, x1, y0, z1, emittedEdges, color);
                emitGridLine(buffer, matrix, cameraPos, x1, y0, z1, x1, y1, z1, emittedEdges, color);
                emitGridLine(buffer, matrix, cameraPos, x1, y1, z1, x1, y1, z0, emittedEdges, color);
                emitGridLine(buffer, matrix, cameraPos, x1, y1, z0, x1, y0, z0, emittedEdges, color);
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

    private static void emitGridLine(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPos,
                                     int x0, int y0, int z0, int x1, int y1, int z1,
                                     Set<EdgeKey> emittedEdges, int color) {
        EdgeKey edge = EdgeKey.of(x0, y0, z0, x1, y1, z1);
        if (!emittedEdges.add(edge)) {
            return;
        }
        emitLine(buffer, matrix,
                x0 - cameraPos.x, y0 - cameraPos.y, z0 - cameraPos.z,
                x1 - cameraPos.x, y1 - cameraPos.y, z1 - cameraPos.z,
                color);
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

    private record WorldPoint(double x, double y, double z) {
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
