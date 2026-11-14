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

import com.xerion.forge.compat.ModCompatibility;
import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.visual.ModelGlowRenderer;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.mojang.blaze3d.platform.NativeImage;
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
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class BlockWireEspOverlayRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Xerion");
    private static final int FALLBACK_COLOR = 0xFFFF0000;
    private static final float LINE_WIDTH = 1.0f;
    private static final int RESET_DISTANCE_BLOCKS = 4;
    private static final long SCAN_INTERVAL_MILLIS = 500L;
    private static final Map<Block, Integer> COLOR_CACHE = new HashMap<>();
    private static final Set<Block> SELECTED_ORE_BLOCKS = new HashSet<>();
    private static final Set<BlockPos> CACHED_ORE_BLOCKS = new HashSet<>();
    private static final Map<BlockPos, Integer> CACHED_BLOCK_COLORS = new HashMap<>();
    private static final Map<BlockPos, ModCompatibility.OreInfo> CACHED_ORE_INFO = new HashMap<>();
    private static final float LABEL_FONT_SIZE = 12.0f;
    private static final int LABEL_COLOR = 0xFFFFFFFF;
    private static int cachedOreMask = Integer.MIN_VALUE;
    private static String cachedOreIds = null;
    private static int cachedRange = -1;
    private static int currentRadius;
    private static long lastScanMillis;
    private static BlockPos cachedScanCenter = null;

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        if (!VisualSettings.oreGlowEnabled()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        int range = Math.max(1, Math.round(VisualSettings.oreGlowRange()));
        Set<Block> selectedOreBlocks = selectedOreBlocks();
        if (selectedOreBlocks.isEmpty()) {
            clearScanCache();
            return;
        }

        BlockPos center = minecraft.player.blockPosition();
        updateScanOrigin(center, range);
        scanTick(minecraft, selectedOreBlocks, center, range);
        pruneInvalidCachedOreBlocks(minecraft, selectedOreBlocks);

        Set<BlockPos> oreBlocks = new HashSet<>(CACHED_ORE_BLOCKS);
        if (oreBlocks.isEmpty()) {
            return;
        }
        Map<BlockPos, Integer> blockColors = new HashMap<>(CACHED_BLOCK_COLORS);

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
        if (VisualSettings.oreGlowThroughWalls()) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
        } else {
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
        }
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(LINE_WIDTH);

        try {
            BufferBuilder fillBuffer = Tesselator.getInstance().getBuilder();
            fillBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            Set<FaceKey> emittedFaces = new HashSet<>();
            for (BlockPos pos : oreBlocks) {
                emitFilledFaces(fillBuffer, matrix, pos, cameraPos, oreBlocks, emittedFaces, blockColors.getOrDefault(pos, FALLBACK_COLOR));
            }
            BufferUploader.drawWithShader(fillBuffer.end());

            if (VisualSettings.oreGlowOutline()) {
                BufferBuilder lineBuffer = Tesselator.getInstance().getBuilder();
                lineBuffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
                Set<EdgeKey> emittedEdges = new HashSet<>();
                for (BlockPos pos : oreBlocks) {
                    emitOutlineEdges(lineBuffer, matrix, pos, cameraPos, emittedEdges, blockColors.getOrDefault(pos, FALLBACK_COLOR));
                }
                BufferUploader.drawWithShader(lineBuffer.end());
            }
            if (VisualSettings.oreGlowNames()) {
                renderOreLabels(cameraPoseStack, camera, event.getProjectionMatrix(), oreBlocks);
            }
        } finally {
            RenderSystem.setProjectionMatrix(previousProjection, VertexSorting.DISTANCE_TO_ORIGIN);
            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();
            EspRenderState.restoreWorldOverlayState();
        }

    }

    private static Set<Block> selectedOreBlocks() {
        int oreMask = VisualSettings.oreGlowOreMask();
        String oreIds = normalizeOreIds(VisualSettings.oreGlowModOreIds());
        if (cachedOreIds != null && cachedOreMask == oreMask && Objects.equals(cachedOreIds, oreIds)) {
            return SELECTED_ORE_BLOCKS;
        }

        SELECTED_ORE_BLOCKS.clear();
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            BlockState state = block.defaultBlockState();
            ModCompatibility.OreInfo oreInfo = ModCompatibility.ore(state);
            if (oreInfo != null && selectedOre(oreInfo, oreMask, oreIds)) {
                SELECTED_ORE_BLOCKS.add(block);
            }
        }
        cachedOreMask = oreMask;
        cachedOreIds = oreIds;
        clearScanCache();
        return SELECTED_ORE_BLOCKS;
    }

    private static String normalizeOreIds(String oreIds) {
        return oreIds == null ? "" : oreIds;
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
        CACHED_ORE_BLOCKS.clear();
        CACHED_BLOCK_COLORS.clear();
        CACHED_ORE_INFO.clear();
        currentRadius = 0;
        lastScanMillis = 0L;
    }

    private static void pruneInvalidCachedOreBlocks(Minecraft minecraft, Set<Block> selectedBlocks) {
        if (minecraft.level == null || CACHED_ORE_BLOCKS.isEmpty()) {
            return;
        }
        CACHED_ORE_BLOCKS.removeIf(pos -> {
            BlockState state = minecraft.level.getBlockState(pos);
            ModCompatibility.OreInfo oreInfo = state.isAir() ? null : ModCompatibility.ore(state);
            boolean valid = oreInfo != null
                    && selectedBlocks.contains(state.getBlock())
                    && selectedOre(oreInfo, cachedOreMask, cachedOreIds);
            if (valid) {
                CACHED_ORE_INFO.put(pos, oreInfo);
                return false;
            }
            CACHED_BLOCK_COLORS.remove(pos);
            CACHED_ORE_INFO.remove(pos);
            return true;
        });
    }

    private static void scanTick(Minecraft minecraft, Set<Block> selectedBlocks, BlockPos center, int range) {
        long now = System.currentTimeMillis();
        if (lastScanMillis != 0L && now - lastScanMillis < SCAN_INTERVAL_MILLIS) {
            return;
        }
        lastScanMillis = now;

        pruneOutOfRange(center, range);
        if (currentRadius == 0) {
            collectSelectedOreBlocks(minecraft, selectedBlocks, center, range, 0);
            currentRadius = Math.min(2, range);
            return;
        }
        if (currentRadius >= range) {
            currentRadius = 0;
            return;
        }
        collectSelectedOreBlocks(minecraft, selectedBlocks, center, range, currentRadius);
        currentRadius++;
    }

    private static void pruneOutOfRange(BlockPos center, int range) {
        int rangeSq = range * range;
        CACHED_ORE_BLOCKS.removeIf(pos -> {
            if (distanceSq(center, pos) <= rangeSq) {
                return false;
            }
            CACHED_BLOCK_COLORS.remove(pos);
            CACHED_ORE_INFO.remove(pos);
            return true;
        });
    }

    private static void collectSelectedOreBlocks(Minecraft minecraft, Set<Block> selectedBlocks, BlockPos center, int range, int radius) {
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
                    BlockState state = minecraft.level.getBlockState(pos);
                    if (state.isAir()) {
                        CACHED_ORE_BLOCKS.remove(pos);
                        CACHED_BLOCK_COLORS.remove(pos);
                        CACHED_ORE_INFO.remove(pos);
                        continue;
                    }
                    if (!selectedBlocks.contains(state.getBlock())) {
                        CACHED_ORE_BLOCKS.remove(pos);
                        CACHED_BLOCK_COLORS.remove(pos);
                        CACHED_ORE_INFO.remove(pos);
                        continue;
                    }
                    CACHED_ORE_BLOCKS.add(pos);
                    CACHED_BLOCK_COLORS.put(pos, cachedAccentColor(minecraft, state));
                    ModCompatibility.OreInfo oreInfo = ModCompatibility.ore(state);
                    if (oreInfo != null) {
                        CACHED_ORE_INFO.put(pos, oreInfo);
                    }
                }
            }
        }
    }

    private static void renderOreLabels(PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, Set<BlockPos> oreBlocks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || oreBlocks.isEmpty()) {
            return;
        }
        Vec3 cameraPos = camera.getPosition();
        Vec3 playerPos = minecraft.player.position();
        Set<BlockPos> visited = new HashSet<>();

        try (MSDFRenderer.WorldTextBatch batch = MSDFRenderer.beginWorldTextBatch(true)) {
            for (BlockPos start : oreBlocks) {
                if (!visited.add(start)) {
                    continue;
                }
                ModCompatibility.OreInfo startInfo = CACHED_ORE_INFO.get(start);
                if (startInfo == null) {
                    continue;
                }
                OreCluster cluster = collectCluster(start, startInfo, oreBlocks, visited, playerPos);
                if (cluster == null) {
                    continue;
                }
                WorldPoint anchor = new WorldPoint(
                        cluster.labelPos().getX() + 0.5D - cameraPos.x,
                        cluster.labelPos().getY() + 1.15D - cameraPos.y,
                        cluster.labelPos().getZ() + 0.5D - cameraPos.z
                );
                float scale = fixedBillboardScaleRelative(camera, anchor, projectionMatrix, 1.0f);
                if (scale <= 0.0f) {
                    continue;
                }
                String text = cluster.name() + " [" + String.format(Locale.ROOT, "%.1fm", Math.sqrt(cluster.distanceSq())) + "]";
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

    private static OreCluster collectCluster(BlockPos start, ModCompatibility.OreInfo startInfo,
                                             Set<BlockPos> oreBlocks, Set<BlockPos> visited, Vec3 playerPos) {
        String typeKey = oreTypeKey(startInfo);
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        BlockPos nearest = start;
        double nearestDistanceSq = blockDistanceSq(playerPos, start);
        String name = oreDisplayName(start, startInfo);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            double distanceSq = blockDistanceSq(playerPos, pos);
            if (distanceSq < nearestDistanceSq) {
                nearestDistanceSq = distanceSq;
                nearest = pos;
            }
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (!oreBlocks.contains(next) || visited.contains(next)) {
                    continue;
                }
                ModCompatibility.OreInfo nextInfo = CACHED_ORE_INFO.get(next);
                if (nextInfo == null || !typeKey.equals(oreTypeKey(nextInfo))) {
                    continue;
                }
                visited.add(next);
                queue.add(next);
            }
        }
        return new OreCluster(nearest, name, nearestDistanceSq);
    }

    private static String oreTypeKey(ModCompatibility.OreInfo oreInfo) {
        if (oreInfo.mask() == VisualSettings.ORE_OTHER) {
            return "id:" + oreInfo.id();
        }
        return "mask:" + oreInfo.mask();
    }

    private static String oreDisplayName(BlockPos pos, ModCompatibility.OreInfo oreInfo) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            String name = minecraft.level.getBlockState(pos).getBlock().getName().getString();
            if (name != null && !name.isBlank()) {
                return name;
            }
        }
        return oreInfo.name();
    }

    private static double blockDistanceSq(Vec3 playerPos, BlockPos pos) {
        double dx = pos.getX() + 0.5D - playerPos.x;
        double dy = pos.getY() + 0.5D - playerPos.y;
        double dz = pos.getZ() + 0.5D - playerPos.z;
        return dx * dx + dy * dy + dz * dz;
    }

    private static int distanceSq(BlockPos center, BlockPos pos) {
        int dx = pos.getX() - center.getX();
        int dy = pos.getY() - center.getY();
        int dz = pos.getZ() - center.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static boolean selectedOre(ModCompatibility.OreInfo oreInfo, int oreMask, String oreIds) {
        return modOreEnabled(oreIds, oreInfo.id());
    }

    private static boolean modOreEnabled(String oreIds, String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        if ("*".equals(oreIds)) {
            return true;
        }
        if (oreIds == null || oreIds.isBlank()) {
            return false;
        }
        return ("|" + oreIds + "|").contains("|" + id + "|");
    }

    private static int cachedAccentColor(Minecraft minecraft, BlockState state) {
        Block block = state.getBlock();
        Integer cached = COLOR_CACHE.get(block);
        if (cached != null) {
            return cached;
        }
        int color = sampleAccentColor(minecraft, state);
        COLOR_CACHE.put(block, color);
        return color;
    }

    private static int sampleAccentColor(Minecraft minecraft, BlockState state) {
        try {
            TextureAtlasSprite sprite = minecraft.getBlockRenderer().getBlockModelShaper().getBlockModel(state).getParticleIcon();
            if (sprite == null || sprite.contents() == null) {
                return FALLBACK_COLOR;
            }
            NativeImage image = sprite.contents().getOriginalImage();
            if (image == null) {
                return FALLBACK_COLOR;
            }
            int w = image.getWidth();
            int h = image.getHeight();
            int bestColor = FALLBACK_COLOR;
            double bestScore = Double.NEGATIVE_INFINITY;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int abgr = image.getPixelRGBA(x, y);
                    int alpha = (abgr >>> 24) & 0xFF;
                    if (alpha <= 16) {
                        continue;
                    }
                    int r = abgr & 0xFF;
                    int g = (abgr >>> 8) & 0xFF;
                    int b = (abgr >>> 16) & 0xFF;
                    int max = Math.max(r, Math.max(g, b));
                    int min = Math.min(r, Math.min(g, b));
                    double saturation = max == 0 ? 0.0D : (max - min) / (double) max;
                    double brightness = max / 255.0D;
                    double opacity = alpha / 255.0D;
                    double score = saturation * 0.78D + brightness * 0.17D + opacity * 0.05D;
                    if (score > bestScore) {
                        bestScore = score;
                        bestColor = 0xFF000000 | (r << 16) | (g << 8) | b;
                    }
                }
            }
            return bestScore == Double.NEGATIVE_INFINITY ? FALLBACK_COLOR : bestColor;
        } catch (Throwable throwable) {
            LOGGER.warn("[ESP][Ore] accent color sample failed for {}", state.getBlock().builtInRegistryHolder().key(), throwable);
            return FALLBACK_COLOR;
        }
    }

    private static void emitFilledFaces(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, Vec3 cameraPos,
                                        Set<BlockPos> oreBlocks, Set<FaceKey> emittedFaces, int color) {
        AABB box = new AABB(
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                pos.getX() + 1,
                pos.getY() + 1,
                pos.getZ() + 1
        );
        for (Direction direction : Direction.values()) {
            if (oreBlocks.contains(pos.relative(direction))) {
                continue;
            }
            FaceKey face = FaceKey.of(pos, direction);
            if (!emittedFaces.add(face)) {
                continue;
            }
            emitFaceQuad(buffer, matrix, box, cameraPos, direction, color, VisualSettings.oreGlowOpacity());
        }
    }

    private static void emitOutlineEdges(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, Vec3 cameraPos,
                                         Set<EdgeKey> emittedEdges, int color) {
        AABB box = new AABB(
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                pos.getX() + 1,
                pos.getY() + 1,
                pos.getZ() + 1
        );
        for (Direction direction : Direction.values()) {
            emitFaceEdges(buffer, matrix, box, cameraPos, direction, emittedEdges, color);
        }
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
            case DOWN -> emitQuad(buffer, matrix,
                    x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0,
                    red, green, blue, a, 0.0f, -1.0f, 0.0f);
            case UP -> emitQuad(buffer, matrix,
                    x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1,
                    red, green, blue, a, 0.0f, 1.0f, 0.0f);
            case NORTH -> emitQuad(buffer, matrix,
                    x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0,
                    red, green, blue, a, 0.0f, 0.0f, -1.0f);
            case SOUTH -> emitQuad(buffer, matrix,
                    x0, y0, z1, x0, y1, z1, x1, y1, z1, x1, y0, z1,
                    red, green, blue, a, 0.0f, 0.0f, 1.0f);
            case WEST -> emitQuad(buffer, matrix,
                    x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1,
                    red, green, blue, a, -1.0f, 0.0f, 0.0f);
            case EAST -> emitQuad(buffer, matrix,
                    x1, y0, z1, x1, y1, z1, x1, y1, z0, x1, y0, z0,
                    red, green, blue, a, 1.0f, 0.0f, 0.0f);
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
        emitLine(
                buffer,
                matrix,
                x0 - cameraPos.x,
                y0 - cameraPos.y,
                z0 - cameraPos.z,
                x1 - cameraPos.x,
                y1 - cameraPos.y,
                z1 - cameraPos.z,
                color
        );
    }

    private static void emitLine(BufferBuilder buffer, Matrix4f matrix,
                                 double x0, double y0, double z0,
                                 double x1, double y1, double z1,
                                 int color) {
        float alpha = ((color >>> 24) & 0xFF) / 255.0f;
        float red = ((color >>> 16) & 0xFF) / 255.0f;
        float green = ((color >>> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        Vector3f normal = lineNormal(x0, y0, z0, x1, y1, z1);
        buffer.vertex(matrix, (float) x0, (float) y0, (float) z0)
                .color(red, green, blue, alpha)
                .normal(normal.x(), normal.y(), normal.z())
                .endVertex();
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z1)
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

    private static float fixedBillboardScaleRelative(Camera camera, WorldPoint anchor, Matrix4f projectionMatrix, float multiplier) {
        Vector3f look = camera.getLookVector();
        double depth = anchor.x() * look.x() + anchor.y() * look.y() + anchor.z() * look.z();
        if (depth <= 0.01D) {
            return -1.0f;
        }

        float projectionY = Math.abs(projectionMatrix.m11());
        int viewportH = Minecraft.getInstance().getWindow().getHeight();
        if (projectionY <= 0.0001f || viewportH <= 0) {
            return 0.025f * multiplier;
        }
        return (float) (2.0D * Math.max(0.25D, depth) / (projectionY * viewportH)) * multiplier;
    }

    public static void clearAccentColorCache() {
        COLOR_CACHE.clear();
    }

    public static final class TextureColorReloadListener extends SimplePreparableReloadListener<Void> {
        @Override
        protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
            return null;
        }

        @Override
        protected void apply(Void unused, ResourceManager resourceManager, ProfilerFiller profiler) {
            clearAccentColorCache();
        }
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

    private record FaceKey(int x, int y, int z, Direction face) {
        private static FaceKey of(BlockPos pos, Direction face) {
            return new FaceKey(pos.getX(), pos.getY(), pos.getZ(), face);
        }
    }

    private record OreCluster(BlockPos labelPos, String name, double distanceSq) {
    }

    private record WorldPoint(double x, double y, double z) {
    }
}
