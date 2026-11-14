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
import com.xerion.forge.chams.ChamsProfileResolver;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.xerion.forge.visual.VisualTargetFilter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MobScreenEspOverlayRenderer {
    private static final int HEALTH_BACKGROUND = 0x78000000;
    private static final int HEALTH_BORDER = 0xFF000000;
    private static final int BOX_BORDER = 0xFF000000;
    private static final int ARMOR_OVERFLOW_COLOR = 0xF2B45CFF;
    private static final int HEALTH_OVERFLOW_COLOR = 0xF24DFFF0;
    private static final float NAME_BASE_SIZE = 6.0f;
    private static final float LAYOUT_GAP = 2.0f;
    private static final float HEALTH_THICKNESS = 2.0f;
    private static final float MAIN_BAR_THICKNESS = 2.0f;
    private static final float EQUIPMENT_ICON_SIZE = 12.0f;
    private static final float EQUIPMENT_ICON_SLOT = 14.0f;
    private static final float EQUIPMENT_ICON_TOP_VISUAL_Y_OFFSET = 2.0f;
    private static final float FLAG_ANIMATION_SPEED = 0.05f;
    private static final float FLAG_POSITION_LERP = 0.18f;
    private static final Map<FlagAnimationKey, FlagAnimationState> FLAG_ANIMATIONS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> HURT_TIME_TICKS = new ConcurrentHashMap<>();
    private static long renderTick;

    private static volatile Matrix4f cachedProjectionMatrix;
    private static volatile Matrix4f cachedModelViewMatrix;

    private MobScreenEspOverlayRenderer() {
    }

    public static void captureWorldMatrices(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        cachedProjectionMatrix = new Matrix4f(event.getProjectionMatrix());
        cachedModelViewMatrix = ModelGlowRenderer.cameraRelativeMatrix(event.getPoseStack().last().pose());
    }

    public static void clearRuntimeCache() {
        FLAG_ANIMATIONS.clear();
        HURT_TIME_TICKS.clear();
        cachedProjectionMatrix = null;
        cachedModelViewMatrix = null;
        renderTick = 0L;
    }

    public static void renderWorldNames(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER
                || !anyMobEspEnabled()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.level == null || player == null) {
            return;
        }

        Camera camera = event.getCamera() == null ? minecraft.gameRenderer.getMainCamera() : event.getCamera();
        if (camera == null) {
            return;
        }

        double range = maxEnabledRange();
        if (range <= 0.0D) {
            return;
        }

        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        if (screenW <= 0 || screenH <= 0) {
            return;
        }
        Vec3 cameraPos = camera.getPosition();
        Matrix4f projection = event.getProjectionMatrix();
        Matrix4f modelView = ModelGlowRenderer.cameraRelativeMatrix(event.getPoseStack().last().pose());
        Matrix4f viewProjection = new Matrix4f(projection).mul(modelView);
        float partialTick = event.getPartialTick();

        AABB searchBox = player.getBoundingBox().inflate(range);
        List<LivingEntity> entities = new ArrayList<>(minecraft.level.getEntitiesOfClass(LivingEntity.class, searchBox));
        entities.sort(Comparator.comparingDouble(entity -> -player.distanceToSqr(entity)));

        List<MobWorldDraw> draws = new ArrayList<>();
        List<MobWorldTextDraw> texts = new ArrayList<>();
        Set<FlagAnimationKey> activeFlagKeys = new HashSet<>();
        renderTick++;
        for (LivingEntity entity : entities) {
            ChamsProfileResolver.Profile profile = ChamsProfileResolver.profile(entity);
            if (!validEntity(profile)) {
                continue;
            }
            if (profile == ChamsProfileResolver.Profile.LOCAL && minecraft.options.getCameraType().isFirstPerson()) {
                continue;
            }

            AABB box = interpolatedBoundingBox(entity, partialTick);
            double distance = interpolatedDistance(player, entity, partialTick);
            boolean drawBox = shouldDrawBox(profile, player, entity, distance);
            boolean drawHealth = shouldDrawHealth(profile, player, entity, distance);
            boolean drawName = shouldDrawName(profile, player, entity, distance);
            boolean drawDistance = shouldDrawDistance(profile, player, entity, distance);
            boolean drawArmorBar = shouldDrawMainItemVisual(profile, player, entity, distance, VisualSettings.ESP_ITEM_ARMOR_BAR)
                    && entity.getArmorValue() > 0;
            boolean drawAbsorption = shouldDrawMainItemVisual(profile, player, entity, distance, VisualSettings.ESP_ITEM_ABSORPTION)
                    && entity.getAbsorptionAmount() > 0.0f;
            boolean drawHurtTime = shouldDrawMainItemVisual(profile, player, entity, distance, VisualSettings.ESP_ITEM_HURT_TIME)
                    && hurtTimeVisible(entity);
            List<EspTextLabel> flagLabels = mobFlagLabels(profile, player, entity, distance);
            List<EquipmentLayoutEntry> equipmentEntries = equipmentLayoutEntries(profile, player, entity, distance);
            if (!drawBox && !drawHealth && !drawName && !drawDistance && !drawArmorBar && !drawAbsorption && !drawHurtTime
                    && flagLabels.isEmpty() && equipmentEntries.isEmpty()) {
                continue;
            }

            ScreenBounds bounds = projectBounds(box, cameraPos, viewProjection, screenW, screenH);
            if (bounds == null || !bounds.valid(screenW, screenH)) {
                continue;
            }

            Vec3 anchorWorld = new Vec3(
                    (box.minX + box.maxX) * 0.5D,
                    (box.minY + box.maxY) * 0.5D,
                    (box.minZ + box.maxZ) * 0.5D
            );
            ScreenPoint anchorScreen = projectPoint(anchorWorld.x, anchorWorld.y, anchorWorld.z,
                    cameraPos, viewProjection, screenW, screenH);
            if (anchorScreen == null) {
                continue;
            }

            WorldPoint relative = new WorldPoint(
                    anchorWorld.x - cameraPos.x,
                    anchorWorld.y - cameraPos.y,
                    anchorWorld.z - cameraPos.z
            );
            float scale = fixedBillboardScaleRelative(camera, relative, projection, screenH, 1.0f);
            if (scale <= 0.0f) {
                continue;
            }

            Map<VisualSettings.EspAnchor, Integer> offsets = new EnumMap<>(VisualSettings.EspAnchor.class);
            ScreenBounds localBounds = new ScreenBounds(
                    bounds.minX() - anchorScreen.x(),
                    bounds.minY() - anchorScreen.y(),
                    bounds.maxX() - anchorScreen.x(),
                    bounds.maxY() - anchorScreen.y()
            );
            List<WorldRect> rects = new ArrayList<>();
            if (drawBox) {
                addBoxRects(rects, localBounds, VisualSettings.mobBoxColor());
            }
            if (drawHurtTime) {
                addHurtTimeRects(rects, localBounds, hurtTimeAge(entity));
            }
            if (drawHealth) {
                VisualSettings.EspAnchor healthAnchor = VisualSettings.mobHealthAnchor();
                LayoutBounds healthBounds = place(bounds, offsets, healthAnchor, healthSize(bounds, healthAnchor));
                LayoutBounds localHealthBounds = new LayoutBounds(
                        healthBounds.x() - anchorScreen.x(),
                        healthBounds.y() - anchorScreen.y(),
                        healthBounds.w(),
                        healthBounds.h()
                );
                addHealthRects(rects, localHealthBounds, entity);
                if (VisualSettings.mobHealthNumber()) {
                    addBarNumberText(texts, relative, scale, localHealthBounds, String.valueOf(Math.round(entity.getHealth())),
                            healthAnchor, healthRatio(entity), profileThroughWalls(profile));
                }
            }
            if (drawArmorBar) {
                VisualSettings.EspAnchor armorAnchor = VisualSettings.visualEspItemAnchor(VisualSettings.ESP_ITEM_ARMOR_BAR);
                LayoutBounds armorBounds = place(bounds, offsets, armorAnchor,
                        mainBarSize(bounds, armorAnchor));
                LayoutBounds localArmorBounds = new LayoutBounds(
                        armorBounds.x() - anchorScreen.x(),
                        armorBounds.y() - anchorScreen.y(),
                        armorBounds.w(),
                        armorBounds.h()
                );
                addArmorBarRects(rects, localArmorBounds, armorAnchor, entity, armorBarColor(entity));
                if (VisualSettings.visualEspItemNumber(VisualSettings.ESP_ITEM_ARMOR_BAR)) {
                    addBarNumberText(texts, relative, scale, localArmorBounds, String.valueOf(entity.getArmorValue()),
                            armorAnchor, armorRatio(entity), profileThroughWalls(profile));
                }
            }
            if (drawAbsorption) {
                VisualSettings.EspAnchor absorptionAnchor = VisualSettings.visualEspItemAnchor(VisualSettings.ESP_ITEM_ABSORPTION);
                LayoutBounds absorptionBounds = place(bounds, offsets, absorptionAnchor,
                        mainBarSize(bounds, absorptionAnchor));
                LayoutBounds localAbsorptionBounds = new LayoutBounds(
                        absorptionBounds.x() - anchorScreen.x(),
                        absorptionBounds.y() - anchorScreen.y(),
                        absorptionBounds.w(),
                        absorptionBounds.h()
                );
                addAbsorptionRects(rects, localAbsorptionBounds, absorptionAnchor, entity, absorptionBarColor(entity));
                if (VisualSettings.visualEspItemNumber(VisualSettings.ESP_ITEM_ABSORPTION)) {
                    addBarNumberText(texts, relative, scale, localAbsorptionBounds,
                            String.valueOf(Math.round(entity.getAbsorptionAmount())),
                            absorptionAnchor, absorptionRatio(entity), profileThroughWalls(profile));
                }
            }
            if (!rects.isEmpty()) {
                draws.add(new MobWorldDraw(relative, scale, rects, profileThroughWalls(profile)));
            }
            addMobTextColumns(texts, drawName ? entityName(entity) : "",
                    drawDistance ? mobDistanceLabel(distance) : "", flagLabels, equipmentEntries,
                    bounds, anchorScreen, relative, scale, offsets, entity, activeFlagKeys, profileThroughWalls(profile));
        }

        addFadingFlagTexts(texts, activeFlagKeys);

        if (draws.isEmpty() && texts.isEmpty()) {
            return;
        }

        PoseStack poseStack = new PoseStack();
        poseStack.mulPoseMatrix(modelView);

        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f(projection), VertexSorting.DISTANCE_TO_ORIGIN);
        minecraft.getMainRenderTarget().bindWrite(false);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        try {
            if (!draws.isEmpty()) {
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                renderWorldRects(draws, poseStack, camera, false);
                renderWorldRects(draws, poseStack, camera, true);
            }
            if (!texts.isEmpty()) {
                renderWorldTexts(texts, poseStack, camera, false);
                renderWorldTexts(texts, poseStack, camera, true);
            }
        } finally {
            RenderSystem.depthMask(true);
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

    public static void render(GuiGraphics graphics, float partialTick) {
        renderEquipmentIcons(graphics, partialTick);
        renderOffscreenArrows(graphics, partialTick);
    }

    private static void renderEquipmentIcons(GuiGraphics graphics, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        Matrix4f projection = cachedProjectionMatrix;
        Matrix4f modelView = cachedModelViewMatrix;
        if (player == null || level == null || projection == null || modelView == null || !anyMobEspEnabled()) {
            return;
        }
        double range = maxEnabledRange();
        if (range <= 0.0D) {
            return;
        }
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        Matrix4f viewProjection = new Matrix4f(projection).mul(modelView);
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        List<LivingEntity> entities = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range)));
        entities.sort(Comparator.comparingDouble(entity -> -player.distanceToSqr(entity)));
        for (LivingEntity entity : entities) {
            ChamsProfileResolver.Profile profile = ChamsProfileResolver.profile(entity);
            if (!validEntity(profile)) {
                continue;
            }
            double distance = interpolatedDistance(player, entity, partialTick);
            if (distance > mobRange(profile) || !itemVisible(profile, player, entity)) {
                continue;
            }
            List<EquipmentLayoutEntry> equipmentEntries = equipmentLayoutEntries(profile, player, entity, distance);
            if (equipmentEntries.stream().noneMatch(entry -> entry.icon() && !entry.stacks().isEmpty())) {
                continue;
            }
            AABB box = interpolatedBoundingBox(entity, partialTick);
            ScreenBounds bounds = projectBounds(box, cameraPos, viewProjection, screenW, screenH);
            if (bounds == null || !bounds.valid(screenW, screenH)) {
                continue;
            }
            Map<VisualSettings.EspAnchor, Integer> offsets = new EnumMap<>(VisualSettings.EspAnchor.class);
            reserveGuiOffsetsForBars(offsets, bounds, profile);
            renderEquipmentIcons(graphics, bounds, offsets, profile, player, entity, distance, equipmentEntries);
        }
    }

    private static boolean anyMobEspEnabled() {
        return VisualSettings.mobBoxEnabled()
                || VisualSettings.mobHealthEnabled()
                || VisualSettings.mobNameEnabled()
                || VisualSettings.mobNameDistance()
                || VisualSettings.visualEnemyEspItemMask() != 0L
                || VisualSettings.visualTeamMobBox()
                || VisualSettings.visualTeamMobHealth()
                || VisualSettings.visualTeamMobName()
                || VisualSettings.visualTeamMobDistance()
                || VisualSettings.visualTeamEspItemMask() != 0L
                || VisualSettings.visualLocalMobBox()
                || VisualSettings.visualLocalMobHealth()
                || VisualSettings.visualLocalMobName()
                || VisualSettings.visualLocalMobDistance()
                || VisualSettings.visualLocalEspItemMask() != 0L;
    }

    private static double maxEnabledRange() {
        double range = 0.0D;
        if (VisualSettings.visualEspEnabled()
                && (VisualSettings.mobBoxEnabled()
                || VisualSettings.mobHealthEnabled()
                || VisualSettings.mobNameEnabled()
                || VisualSettings.mobNameDistance()
                || VisualSettings.visualEnemyEspItemMask() != 0L)) {
            range = Math.max(range, VisualSettings.visualEnemyRange());
        }
        if (VisualSettings.visualTeamEnabled()
                && (VisualSettings.visualTeamMobBox()
                || VisualSettings.visualTeamMobHealth()
                || VisualSettings.visualTeamMobName()
                || VisualSettings.visualTeamMobDistance()
                || VisualSettings.visualTeamEspItemMask() != 0L)) {
            range = Math.max(range, VisualSettings.visualTeamRange());
        }
        if (VisualSettings.visualLocalEnabled()
                && (VisualSettings.visualLocalMobBox()
                || VisualSettings.visualLocalMobHealth()
                || VisualSettings.visualLocalMobName()
                || VisualSettings.visualLocalMobDistance()
                || VisualSettings.visualLocalEspItemMask() != 0L)) {
            range = Math.max(range, VisualSettings.visualLocalRange());
        }
        return range;
    }

    private static boolean validEntity(ChamsProfileResolver.Profile profile) {
        return profile != ChamsProfileResolver.Profile.NONE;
    }

    private static boolean shouldDrawBox(ChamsProfileResolver.Profile profile, Player player, LivingEntity entity, double distance) {
        return mobBoxEnabled(profile)
                && distance <= mobRange(profile)
                && featureVisible(profile, player, entity);
    }

    private static boolean shouldDrawHealth(ChamsProfileResolver.Profile profile, Player player, LivingEntity entity, double distance) {
        return mobHealthEnabled(profile)
                && distance <= mobRange(profile)
                && featureVisible(profile, player, entity);
    }

    private static boolean shouldDrawName(ChamsProfileResolver.Profile profile, Player player, LivingEntity entity, double distance) {
        return mobNameEnabled(profile)
                && distance <= mobRange(profile)
                && featureVisible(profile, player, entity);
    }

    private static boolean shouldDrawDistance(ChamsProfileResolver.Profile profile, Player player, LivingEntity entity, double distance) {
        return mobDistanceEnabled(profile)
                && distance <= mobRange(profile)
                && featureVisible(profile, player, entity);
    }

    private static boolean shouldDrawItems(ChamsProfileResolver.Profile profile, LivingEntity entity) {
        return espItemMask(profile) != 0L && entity != null;
    }

    private static boolean shouldDrawMainItemVisual(ChamsProfileResolver.Profile profile, Player player,
                                                    LivingEntity entity, double distance, long item) {
        return (espItemMask(profile) & item) != 0L
                && distance <= mobRange(profile)
                && itemVisible(profile, player, entity);
    }

    private static boolean mobBoxEnabled(ChamsProfileResolver.Profile profile) {
        return switch (profile) {
            case ENEMY -> VisualSettings.mobBoxEnabled();
            case TEAM -> VisualSettings.visualTeamMobBox();
            case LOCAL -> VisualSettings.visualLocalMobBox();
            case NONE -> false;
        };
    }

    private static boolean mobHealthEnabled(ChamsProfileResolver.Profile profile) {
        return switch (profile) {
            case ENEMY -> VisualSettings.mobHealthEnabled();
            case TEAM -> VisualSettings.visualTeamMobHealth();
            case LOCAL -> VisualSettings.visualLocalMobHealth();
            case NONE -> false;
        };
    }

    private static boolean mobNameEnabled(ChamsProfileResolver.Profile profile) {
        return switch (profile) {
            case ENEMY -> VisualSettings.mobNameEnabled();
            case TEAM -> VisualSettings.visualTeamMobName();
            case LOCAL -> VisualSettings.visualLocalMobName();
            case NONE -> false;
        };
    }

    private static boolean mobDistanceEnabled(ChamsProfileResolver.Profile profile) {
        return switch (profile) {
            case ENEMY -> VisualSettings.mobNameDistance();
            case TEAM -> VisualSettings.visualTeamMobDistance();
            case LOCAL -> VisualSettings.visualLocalMobDistance();
            case NONE -> false;
        };
    }

    private static long espItemMask(ChamsProfileResolver.Profile profile) {
        return switch (profile) {
            case ENEMY -> VisualSettings.visualEnemyEspItemMask();
            case TEAM -> VisualSettings.visualTeamEspItemMask();
            case LOCAL -> VisualSettings.visualLocalEspItemMask();
            case NONE -> 0L;
        };
    }

    private static double mobRange(ChamsProfileResolver.Profile profile) {
        return switch (profile) {
            case ENEMY -> VisualSettings.visualEnemyRange();
            case TEAM -> VisualSettings.visualTeamRange();
            case LOCAL -> VisualSettings.visualLocalRange();
            case NONE -> 0.0D;
        };
    }

    private static boolean featureVisible(ChamsProfileResolver.Profile profile, Player player, LivingEntity entity) {
        if (!profileShowInvisible(profile) && entity.isInvisible()) {
            return false;
        }
        return profileThroughWalls(profile) || player.hasLineOfSight(entity);
    }

    private static boolean itemVisible(ChamsProfileResolver.Profile profile, Player player, LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        if (!profileShowInvisible(profile) && entity.isInvisible()) {
            return false;
        }
        return profileThroughWalls(profile) || player.hasLineOfSight(entity) || entity.isInvisible();
    }

    private static boolean profileShowInvisible(ChamsProfileResolver.Profile profile) {
        return switch (profile) {
            case ENEMY -> VisualSettings.visualEnemiesShowInvisible();
            case TEAM -> VisualSettings.visualTeamShowInvisible();
            case LOCAL -> VisualSettings.visualLocalShowInvisible();
            case NONE -> false;
        };
    }

    private static boolean profileThroughWalls(ChamsProfileResolver.Profile profile) {
        return switch (profile) {
            case ENEMY -> VisualSettings.visualEnemiesThroughWalls();
            case TEAM -> VisualSettings.visualTeamThroughWalls();
            case LOCAL -> VisualSettings.visualLocalThroughWalls();
            case NONE -> false;
        };
    }

    private static ScreenBounds projectBounds(AABB box, Vec3 cameraPos, Matrix4f viewProjection, int screenW, int screenH) {
        if (box == null || screenW <= 0 || screenH <= 0) {
            return null;
        }
        double[] xs = {box.minX, box.maxX};
        double[] ys = {box.minY, box.maxY};
        double[] zs = {box.minZ, box.maxZ};
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        boolean any = false;

        for (double x : xs) {
            for (double y : ys) {
                for (double z : zs) {
                    ScreenPoint point = projectPoint(x, y, z, cameraPos, viewProjection, screenW, screenH);
                    if (point == null) {
                        continue;
                    }
                    minX = Math.min(minX, point.x());
                    minY = Math.min(minY, point.y());
                    maxX = Math.max(maxX, point.x());
                    maxY = Math.max(maxY, point.y());
                    any = true;
                }
            }
        }

        if (!any) {
            return null;
        }
        return new ScreenBounds(minX, minY, maxX, maxY);
    }

    private static ScreenPoint projectPoint(double worldX, double worldY, double worldZ, Vec3 cameraPos,
                                            Matrix4f viewProjection, int screenW, int screenH) {
        if (cameraPos == null || viewProjection == null || screenW <= 0 || screenH <= 0
                || !Double.isFinite(worldX) || !Double.isFinite(worldY) || !Double.isFinite(worldZ)
                || !Double.isFinite(cameraPos.x) || !Double.isFinite(cameraPos.y) || !Double.isFinite(cameraPos.z)) {
            return null;
        }
        Vector4f clip = new Vector4f(
                (float) (worldX - cameraPos.x),
                (float) (worldY - cameraPos.y),
                (float) (worldZ - cameraPos.z),
                1.0f
        );
        viewProjection.transform(clip);
        if (!finite(clip) || clip.w <= 0.0001f) {
            return null;
        }

        float ndcX = clip.x / clip.w;
        float ndcY = clip.y / clip.w;
        float screenX = (ndcX * 0.5f + 0.5f) * screenW;
        float screenY = (0.5f - ndcY * 0.5f) * screenH;
        if (!Float.isFinite(screenX) || !Float.isFinite(screenY)) {
            return null;
        }
        return new ScreenPoint(screenX, screenY);
    }

    private static OffscreenProjection projectOffscreenPoint(double worldX, double worldY, double worldZ,
                                                             Vec3 cameraPos, Matrix4f viewProjection,
                                                             int screenW, int screenH) {
        if (cameraPos == null || viewProjection == null || screenW <= 0 || screenH <= 0
                || !Double.isFinite(worldX) || !Double.isFinite(worldY) || !Double.isFinite(worldZ)
                || !Double.isFinite(cameraPos.x) || !Double.isFinite(cameraPos.y) || !Double.isFinite(cameraPos.z)) {
            return null;
        }
        Vector4f clip = new Vector4f(
                (float) (worldX - cameraPos.x),
                (float) (worldY - cameraPos.y),
                (float) (worldZ - cameraPos.z),
                1.0f
        );
        viewProjection.transform(clip);
        if (!finite(clip)) {
            return null;
        }

        boolean behindCamera = clip.w <= 0.0001f;
        float divisor = behindCamera ? Math.max(0.0001f, Math.abs(clip.w)) : clip.w;
        float ndcX = clip.x / divisor;
        float ndcY = clip.y / divisor;
        if (!Float.isFinite(ndcX) || !Float.isFinite(ndcY)) {
            return null;
        }
        if (behindCamera && Math.abs(ndcX) < 0.001f && Math.abs(ndcY) < 0.001f) {
            ndcY = -1.0f;
        }

        float screenX = (ndcX * 0.5f + 0.5f) * screenW;
        float screenY = (0.5f - ndcY * 0.5f) * screenH;
        if (!Float.isFinite(screenX) || !Float.isFinite(screenY)) {
            return null;
        }
        return new OffscreenProjection(new ScreenPoint(screenX, screenY), behindCamera);
    }

    private static void renderOffscreenArrows(GuiGraphics graphics, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (!VisualSettings.visualEspEnabled()
                || !VisualSettings.visualOffscreenArrow()
                || player == null
                || level == null) {
            return;
        }

        Camera camera = minecraft.gameRenderer.getMainCamera();
        Matrix4f projectionMatrix = cachedProjectionMatrix == null
                ? new Matrix4f(RenderSystem.getProjectionMatrix())
                : cachedProjectionMatrix;
        Window window = minecraft.getWindow();
        float screenW = window.getGuiScaledWidth();
        float screenH = window.getGuiScaledHeight();
        float centerX = screenW * 0.5f;
        float centerY = screenH * 0.5f;
        float radius = Math.max(8.0f, Math.min(VisualSettings.visualOffscreenArrowRadius(),
                Math.min(screenW, screenH) * 0.5f - 12.0f));
        float size = Math.max(4.0f, VisualSettings.visualOffscreenArrowSize());
        int color = VisualSettings.visualOffscreenArrowColor();
        float maxRange = VisualSettings.visualEnemyRange();
        if (maxRange <= 0.0f) {
            maxRange = 96.0f;
        }

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(maxRange))) {
            renderOffscreenArrowForEntity(graphics, camera, projectionMatrix, screenW, screenH, centerX, centerY,
                    radius, size, color, player, entity, partialTick);
        }
        for (LivingEntity dummy : FakePlayerRegistry.dummyEntities()) {
            if (player.distanceToSqr(dummy) <= maxRange * maxRange) {
                renderOffscreenArrowForEntity(graphics, camera, projectionMatrix, screenW, screenH, centerX, centerY,
                        radius, size, color, player, dummy, partialTick);
            }
        }
    }

    private static void renderOffscreenArrowForEntity(GuiGraphics graphics, Camera camera, Matrix4f projectionMatrix,
                                                      float screenW, float screenH, float centerX, float centerY,
                                                      float radius, float size, int color, LocalPlayer player,
                                                      LivingEntity entity, float partialTick) {
        if (!VisualTargetFilter.isEnemyVisualTarget(player, entity)) {
            return;
        }
        if (!VisualSettings.visualEnemiesShowInvisible() && entity.isInvisible()) {
            return;
        }
        AABB box = interpolatedBoundingBox(entity, partialTick);
        Vec3 cameraPos = camera.getPosition();
        Matrix4f modelView = new Matrix4f().rotation(camera.rotation());
        Matrix4f viewProjection = new Matrix4f(projectionMatrix).mul(modelView);
        if (isAabbInView(box, cameraPos, viewProjection)) {
            return;
        }

        double targetX = lerp(entity.xOld, entity.getX(), partialTick);
        double targetY = lerp(entity.yOld, entity.getY(), partialTick) + entity.getBbHeight() * 0.5;
        double targetZ = lerp(entity.zOld, entity.getZ(), partialTick);
        double dx = targetX - cameraPos.x;
        double dy = targetY - cameraPos.y;
        double dz = targetZ - cameraPos.z;

        Vector3f look = camera.getLookVector();
        Vector3f left = camera.getLeftVector();
        Vector3f up = camera.getUpVector();
        double viewX = -(dx * left.x() + dy * left.y() + dz * left.z());
        double viewY = dx * up.x() + dy * up.y() + dz * up.z();
        double viewZ = dx * look.x() + dy * look.y() + dz * look.z();

        double angle = Math.atan2(viewY, viewX);
        float dirX = (float) Math.cos(angle);
        float dirY = -(float) Math.sin(angle);
        float x = centerX + dirX * radius;
        float y = centerY + dirY * radius;
        x = Math.max(size + 2.0f, Math.min(screenW - size - 2.0f, x));
        y = Math.max(size + 2.0f, Math.min(screenH - size - 2.0f, y));
        drawHudArrow(graphics, x, y, (float) (Math.PI * 0.5 - angle), size, color);

        String label = offscreenLabel(entity, player.distanceTo(entity), VisualSettings.visualOffscreenArrowDisplayMask());
        if (!label.isEmpty()) {
            drawOffscreenLabel(graphics, label, x, y, dirX, dirY, size, color);
        }
    }

    private static void drawHudArrow(GuiGraphics graphics, float centerX, float centerY, float rotation,
                                     float size, int color) {
        float tipX = 0.0f;
        float tipY = -size;
        float leftX = -size * 0.62f;
        float leftY = size * 0.58f;
        float rightX = size * 0.62f;
        float rightY = size * 0.58f;

        float sin = (float) Math.sin(rotation);
        float cos = (float) Math.cos(rotation);
        float x1 = centerX + tipX * cos - tipY * sin;
        float y1 = centerY + tipX * sin + tipY * cos;
        float x2 = centerX + leftX * cos - leftY * sin;
        float y2 = centerY + leftX * sin + leftY * cos;
        float x3 = centerX + rightX * cos - rightY * sin;
        float y3 = centerY + rightX * sin + rightY * cos;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        try {
            Matrix4f matrix = graphics.pose().last().pose();
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            float alpha = ((color >>> 24) & 0xFF) / 255.0f;
            float red = ((color >>> 16) & 0xFF) / 255.0f;
            float green = ((color >>> 8) & 0xFF) / 255.0f;
            float blue = (color & 0xFF) / 255.0f;
            buffer.vertex(matrix, x1, y1, 0.0f).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrix, x2, y2, 0.0f).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrix, x3, y3, 0.0f).color(red, green, blue, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        } finally {
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }

    private static void drawOffscreenLabel(GuiGraphics graphics, String label, float arrowX, float arrowY,
                                           float dirX, float dirY, float size, int color) {
        float textSize = 6.0f;
        float textW = MSDFRenderer.width(label, textSize);
        float tipX = arrowX + dirX * size;
        float tipY = arrowY + dirY * size;
        float offset = Math.max(8.0f, size * 0.5f);
        float textX = tipX + dirX * offset;
        float textY = tipY + dirY * offset;
        float x = textX - textW * 0.5f;
        float y = textY - textSize * 0.5f;
        MSDFRenderer.drawScreenTextWithShadow(graphics, label, x, y, textSize, color);
    }

    private static String offscreenLabel(LivingEntity entity, double distance, int displayMask) {
        boolean showName = (displayMask & VisualSettings.OFFSCREEN_ARROW_NAME) != 0;
        boolean showDistance = (displayMask & VisualSettings.OFFSCREEN_ARROW_DISTANCE) != 0;
        if (!showName && !showDistance) {
            return "";
        }
        String name = showName ? entityName(entity) : "";
        String distanceText = showDistance ? "[" + Math.round(distance) + "m]" : "";
        return name.isEmpty() ? distanceText : (distanceText.isEmpty() ? name : name + " " + distanceText);
    }

    private static AABB interpolatedBoundingBox(LivingEntity entity, float partialTick) {
        double x = lerp(entity.xOld, entity.getX(), partialTick);
        double y = lerp(entity.yOld, entity.getY(), partialTick);
        double z = lerp(entity.zOld, entity.getZ(), partialTick);
        double halfWidth = entity.getBbWidth() * 0.5D;
        double height = entity.getBbHeight();
        return new AABB(x - halfWidth, y, z - halfWidth, x + halfWidth, y + height, z + halfWidth);
    }

    private static double interpolatedDistance(LivingEntity from, LivingEntity to, float partialTick) {
        double fromX = lerp(from.xOld, from.getX(), partialTick);
        double fromY = lerp(from.yOld, from.getY(), partialTick);
        double fromZ = lerp(from.zOld, from.getZ(), partialTick);
        double toX = lerp(to.xOld, to.getX(), partialTick);
        double toY = lerp(to.yOld, to.getY(), partialTick);
        double toZ = lerp(to.zOld, to.getZ(), partialTick);
        double dx = toX - fromX;
        double dy = toY - fromY;
        double dz = toZ - fromZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static boolean isAabbInView(AABB box, Vec3 cameraPos, Matrix4f viewProjection) {
        if (box == null) {
            return false;
        }
        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        double[] xs = {box.minX, box.maxX};
        double[] ys = {box.minY, box.maxY};
        double[] zs = {box.minZ, box.maxZ};
        for (double x : xs) {
            for (double y : ys) {
                for (double z : zs) {
                    ScreenPoint point = projectPoint(x, y, z, cameraPos, viewProjection, screenW, screenH);
                    if (point != null
                            && point.x() >= 0.0f && point.x() <= screenW
                            && point.y() >= 0.0f && point.y() <= screenH) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static double lerp(double start, double end, float partialTick) {
        return start + (end - start) * partialTick;
    }

    private static void emitWorldRects(BufferBuilder buffer, PoseStack poseStack, Camera camera, MobWorldDraw draw) {
        poseStack.pushPose();
        try {
            poseStack.translate(draw.anchor().x(), draw.anchor().y(), draw.anchor().z());
            poseStack.mulPose(camera.rotation());
            poseStack.scale(-draw.scale(), -draw.scale(), draw.scale());
            Matrix4f matrix = poseStack.last().pose();
            for (WorldRect rect : draw.rects()) {
                emitRect(buffer, matrix, rect);
            }
        } finally {
            poseStack.popPose();
        }
    }

    private static void renderWorldRects(List<MobWorldDraw> draws, PoseStack poseStack, Camera camera, boolean throughWalls) {
        boolean hasMatchingDraw = false;
        for (MobWorldDraw draw : draws) {
            if (draw.throughWalls() == throughWalls) {
                hasMatchingDraw = true;
                break;
            }
        }
        if (!hasMatchingDraw) {
            return;
        }

        RenderSystem.depthMask(false);
        if (throughWalls) {
            RenderSystem.disableDepthTest();
        } else {
            RenderSystem.enableDepthTest();
        }
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (MobWorldDraw draw : draws) {
            if (draw.throughWalls() != throughWalls) {
                continue;
            }
            emitWorldRects(buffer, poseStack, camera, draw);
        }
        BufferUploader.drawWithShader(buffer.end());
    }

    private static void drawWorldText(PoseStack poseStack, MSDFRenderer.WorldTextBatch batch, Camera camera,
                                      MobWorldTextDraw draw) {
        poseStack.pushPose();
        try {
            poseStack.translate(draw.anchor().x(), draw.anchor().y(), draw.anchor().z());
            poseStack.mulPose(camera.rotation());
            poseStack.scale(-draw.scale(), -draw.scale(), draw.scale());
            MSDFRenderer.drawWorldTextWithShadow(batch, poseStack.last().pose(), draw.text(), draw.x(), draw.y(),
                    draw.size(), draw.color());
        } finally {
            poseStack.popPose();
        }
    }

    private static void renderWorldTexts(List<MobWorldTextDraw> texts, PoseStack poseStack, Camera camera, boolean throughWalls) {
        try (MSDFRenderer.WorldTextBatch textBatch = MSDFRenderer.beginWorldTextBatch(throughWalls)) {
            for (MobWorldTextDraw text : texts) {
                if (text.throughWalls() == throughWalls) {
                    drawWorldText(poseStack, textBatch, camera, text);
                }
            }
        }
    }

    private static void addBarNumberText(List<MobWorldTextDraw> texts, WorldPoint relative, float scale,
                                         LayoutBounds bounds, String value, VisualSettings.EspAnchor anchor,
                                         float ratio, boolean throughWalls) {
        if (value == null || value.isBlank()) {
            return;
        }
        float size = 5.0f;
        float textW = MSDFRenderer.width(value, size);
        float clamped = Math.max(0.0f, Math.min(1.0f, ratio));
        float textX;
        float textY;
        if (anchor == VisualSettings.EspAnchor.TOP || anchor == VisualSettings.EspAnchor.BOTTOM) {
            float progressX = bounds.x() + bounds.w() * clamped;
            textX = clamp(progressX - textW * 0.5f, bounds.x() - textW * 0.5f, bounds.x() + bounds.w() - textW * 0.5f);
            textY = bounds.y() + bounds.h() * 0.5f - size * 0.5f;
        } else {
            float progressY = bounds.y() + bounds.h() * (1.0f - clamped);
            textX = bounds.x() + bounds.w() * 0.5f - textW * 0.5f;
            textY = clamp(progressY - size * 0.5f, bounds.y() - size * 0.5f, bounds.y() + bounds.h() - size * 0.5f);
        }
        texts.add(new MobWorldTextDraw(
                relative,
                scale,
                value,
                textX,
                textY,
                size,
                0xF2FFFFFF,
                throughWalls
        ));
    }

    private static void addMobTextColumns(List<MobWorldTextDraw> texts, String nameLabel, String distanceLabel,
                                          List<EspTextLabel> flagLabels, List<EquipmentLayoutEntry> equipmentEntries,
                                          ScreenBounds bounds, ScreenPoint anchorScreen, WorldPoint relative, float scale,
                                          Map<VisualSettings.EspAnchor, Integer> offsets, LivingEntity entity,
                                          Set<FlagAnimationKey> activeFlagKeys, boolean throughWalls) {
        float nameSize = NAME_BASE_SIZE * VisualSettings.mobNameScale();
        float distanceSize = NAME_BASE_SIZE * VisualSettings.mobDistanceScale();
        float flagSize = Math.max(6.0f, NAME_BASE_SIZE);
        float equipmentSize = Math.max(5.0f, NAME_BASE_SIZE * 0.92f);
        for (VisualSettings.EspAnchor anchor : VisualSettings.EspAnchor.values()) {
            List<LayoutColumnEntry> columnEntries = new ArrayList<>();
            if (nameLabel != null && !nameLabel.isBlank() && VisualSettings.mobNameAnchor() == anchor) {
                MobTextLayoutEntry entry = new MobTextLayoutEntry(0L, nameLabel, VisualSettings.mobNameColor(),
                        nameSize, false, VisualSettings.mobNameOffsetX(), VisualSettings.mobNameOffsetY());
                columnEntries.add(LayoutColumnEntry.text(entry, textEntrySize(entry)));
            }
            if (distanceLabel != null && !distanceLabel.isBlank() && VisualSettings.mobDistanceAnchor() == anchor) {
                MobTextLayoutEntry entry = new MobTextLayoutEntry(0L, distanceLabel, VisualSettings.mobDistanceColor(),
                        distanceSize, false, VisualSettings.mobDistanceOffsetX(), VisualSettings.mobDistanceOffsetY());
                columnEntries.add(LayoutColumnEntry.text(entry, textEntrySize(entry)));
            }
            for (EspTextLabel label : flagLabels) {
                if (label == null || label.text() == null || label.text().isBlank()
                        || VisualSettings.visualEspFlagAnchor(label.item()) != anchor) {
                    continue;
                }
                MobTextLayoutEntry entry = new MobTextLayoutEntry(label.item(), label.text(), label.color(),
                        flagSize, true, 0.0f, 0.0f);
                columnEntries.add(LayoutColumnEntry.text(entry, textEntrySize(entry)));
            }
            for (EquipmentLayoutEntry entry : equipmentEntries) {
                if (entry == null || entry.anchor() != anchor) {
                    continue;
                }
                columnEntries.add(LayoutColumnEntry.equipment(entry, equipmentEntrySize(entry, equipmentSize), equipmentSize));
            }
            if (columnEntries.isEmpty()) {
                continue;
            }
            LayoutBounds column = place(bounds, offsets, anchor, columnSize(columnEntries));
            float y = column.y();
            LayoutColumnEntry previous = null;
            for (LayoutColumnEntry entry : columnEntries) {
                if (previous != null) {
                    y += columnEntryGap(previous, entry);
                }
                LayoutSize entrySize = entry.size();
                float x = anchor == VisualSettings.EspAnchor.LEFT ? column.x() + column.w() - entrySize.w() : column.x();
                if (anchor == VisualSettings.EspAnchor.TOP || anchor == VisualSettings.EspAnchor.BOTTOM) {
                    x = column.x() + column.w() * 0.5f - entrySize.w() * 0.5f;
                }
                if (entry.text() != null) {
                    addColumnText(texts, entry.text(), x, y, anchorScreen, relative, scale, entity, activeFlagKeys, throughWalls);
                } else if (entry.equipment() != null && !entry.equipment().icon()) {
                    EquipmentLayoutEntry equipment = entry.equipment();
                    texts.add(new MobWorldTextDraw(
                            relative,
                            scale,
                            equipment.text(),
                            x - anchorScreen.x() + 4.0f,
                            y - anchorScreen.y() + Math.max(0.0f, (entrySize.h() - entry.textSize()) * 0.5f),
                            entry.textSize(),
                            equipment.color(),
                            throughWalls
                    ));
                }
                y += entrySize.h();
                previous = entry;
            }
        }
    }

    private static void addColumnText(List<MobWorldTextDraw> texts, MobTextLayoutEntry label, float x, float y,
                                      ScreenPoint anchorScreen, WorldPoint relative, float scale, LivingEntity entity,
                                      Set<FlagAnimationKey> activeFlagKeys, boolean throughWalls) {
        if (!label.flag()) {
            texts.add(new MobWorldTextDraw(
                    relative,
                    scale,
                    label.text(),
                    x - anchorScreen.x() + label.offsetX() + 4.0f,
                    y - anchorScreen.y() + Math.max(0.0f, (textEntrySize(label).h() - label.size()) * 0.5f) + label.offsetY(),
                    label.size(),
                    label.color(),
                    throughWalls
            ));
            return;
        }
        FlagAnimationKey key = new FlagAnimationKey(entity.getUUID(), label.item());
        activeFlagKeys.add(key);
        FlagAnimationState state = FLAG_ANIMATIONS.computeIfAbsent(key, unused -> new FlagAnimationState());
        state.progress = Math.min(1.0f, state.progress + FLAG_ANIMATION_SPEED);
        state.anchor = relative;
        state.scale = scale;
        state.text = label.text();
        state.color = label.color();
        state.throughWalls = throughWalls;
        state.x = x - anchorScreen.x() + 4.0f;
        state.y = y - anchorScreen.y() + Math.max(0.0f, (textEntrySize(label).h() - label.size()) * 0.5f);
        if (!state.initialized) {
            state.displayY = state.y;
            state.initialized = true;
        }
        state.displayY += (state.y - state.displayY) * FLAG_POSITION_LERP;
        state.size = label.size();
        float eased = easeOutCubic(state.progress);
        texts.add(new MobWorldTextDraw(
                relative,
                scale,
                label.text(),
                state.x,
                state.displayY,
                label.size(),
                applyAlpha(label.color(), eased),
                throughWalls
        ));
    }

    private static void addFadingFlagTexts(List<MobWorldTextDraw> texts, Set<FlagAnimationKey> activeFlagKeys) {
        List<FlagAnimationKey> remove = new ArrayList<>();
        for (Map.Entry<FlagAnimationKey, FlagAnimationState> entry : FLAG_ANIMATIONS.entrySet()) {
            if (activeFlagKeys.contains(entry.getKey())) {
                continue;
            }
            FlagAnimationState state = entry.getValue();
            state.progress = Math.max(0.0f, state.progress - FLAG_ANIMATION_SPEED);
            if (state.progress <= 0.001f || state.anchor == null || state.text == null || state.text.isBlank()) {
                remove.add(entry.getKey());
                continue;
            }
            float eased = easeOutCubic(state.progress);
            texts.add(new MobWorldTextDraw(
                    state.anchor,
                    state.scale,
                    state.text,
                    state.x,
                    state.displayY,
                    state.size,
                    applyAlpha(state.color, eased),
                    state.throughWalls
            ));
        }
        for (FlagAnimationKey key : remove) {
            FLAG_ANIMATIONS.remove(key);
        }
    }

    private static List<EspTextLabel> mobFlagLabels(ChamsProfileResolver.Profile profile, Player viewer,
                                                    LivingEntity entity, double distance) {
        if (!shouldDrawItems(profile, entity)
                || distance > mobRange(profile)
                || !itemVisible(profile, viewer, entity)) {
            return List.of();
        }

        List<EspTextLabel> labels = new ArrayList<>();
        long mask = espItemMask(profile);

        addFlagLabels(labels, mask, viewer, entity);
        return labels;
    }

    private static List<EspTextLabel> mobItemLabels(ChamsProfileResolver.Profile profile, Player viewer,
                                                    LivingEntity entity, double distance) {
        if (!shouldDrawItems(profile, entity)
                || distance > mobRange(profile)
                || !itemVisible(profile, viewer, entity)) {
            return List.of();
        }

        List<EspTextLabel> labels = new ArrayList<>();
        long mask = espItemMask(profile);

        addEquipmentLabels(labels, mask, entity);
        return labels;
    }

    private static List<EquipmentLayoutEntry> equipmentLayoutEntries(ChamsProfileResolver.Profile profile, Player viewer,
                                                                     LivingEntity entity, double distance) {
        if (!shouldDrawItems(profile, entity)
                || distance > mobRange(profile)
                || !itemVisible(profile, viewer, entity)) {
            return List.of();
        }

        List<EquipmentLayoutEntry> entries = new ArrayList<>();
        long mask = espItemMask(profile);
        addEquipmentTextEntry(entries, mask, VisualSettings.ESP_ITEM_HELD_ITEM_TEXT, heldItemLabel(entity));
        addEquipmentIconEntry(entries, mask, VisualSettings.ESP_ITEM_HELD_ITEM_ICON, heldIconStacks(entity));
        addEquipmentTextEntry(entries, mask, VisualSettings.ESP_ITEM_ARMOR_TEXT, armorTextLabel(entity));
        addEquipmentIconEntry(entries, mask, VisualSettings.ESP_ITEM_ARMOR_ICONS, armorIconStacks(entity));
        addEquipmentTextEntry(entries, mask, VisualSettings.ESP_ITEM_DURABILITY, durabilityLabel(entity));
        addEquipmentTextEntry(entries, mask, VisualSettings.ESP_ITEM_ENCHANTS, enchantsLabel(entity));
        addEquipmentIconEntry(entries, mask, VisualSettings.ESP_ITEM_TOTEM, hotbarMatchingIconStacks(entity, Items.TOTEM_OF_UNDYING));
        addEquipmentIconEntry(entries, mask, VisualSettings.ESP_ITEM_SHIELD, hotbarMatchingIconStacks(entity, Items.SHIELD));
        addEquipmentIconEntry(entries, mask, VisualSettings.ESP_ITEM_BOW_CROSSBOW, hotbarBowIconStacks(entity));
        return entries;
    }

    private static void addEquipmentTextEntry(List<EquipmentLayoutEntry> entries, long mask, long item, String text) {
        if ((mask & item) == 0L || text == null || text.isBlank()) {
            return;
        }
        entries.add(new EquipmentLayoutEntry(
                item,
                false,
                text,
                VisualSettings.visualEspItemColor(item),
                VisualSettings.visualEspItemAnchor(item),
                List.of()
        ));
    }

    private static void addEquipmentIconEntry(List<EquipmentLayoutEntry> entries, long mask, long item, List<ItemStack> stacks) {
        if ((mask & item) == 0L || stacks == null || stacks.isEmpty()) {
            return;
        }
        entries.add(new EquipmentLayoutEntry(
                item,
                true,
                "",
                VisualSettings.visualEspItemColor(item),
                VisualSettings.visualEspItemAnchor(item),
                stacks
        ));
    }

    private static void addFlagLabels(List<EspTextLabel> labels, long mask, Player viewer, LivingEntity entity) {
        addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_SNEAKING, entity.isShiftKeyDown() ? "Sneaking" : "", "SNK");
        addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_SPRINTING, entity.isSprinting() ? "Sprinting" : "", "SPR");
        addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_INVISIBLE, entity.isInvisible() ? "Invisible" : "", "INV");
        addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_BURNING, entity.isOnFire() ? "Burning" : "", "FIRE");
        addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_BLOCKING, entity.isBlocking() ? "Blocking" : "", "BLK");
        addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_USING_ITEM, entity.isUsingItem() ? "Using Item" : "", "USE");
        addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_EATING, eatingLabel(entity), "EAT");
        addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_DRINKING, drinkingLabel(entity), "DRK");
        addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_BOW_CHARGING, bowChargingLabel(entity), "BOW");
        addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_CROSSBOW_CHARGED, crossbowChargedLabel(entity), "X-BOW");
        addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_ELYTRA, entity.isFallFlying() ? "Elytra" : "", "ELY");
        addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_SWIMMING, entity.isVisuallySwimming() ? "Swimming" : "", "SWM");
        addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_GLOWING, entity.isCurrentlyGlowing() ? "Glowing" : "", "GLOW");
        addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_POTION, potionLabel(entity), "POT");
        addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_LOW_HEALTH,
                entity.getHealth() <= Math.max(4.0f, entity.getMaxHealth() * 0.25f) ? "Low Health" : "", "LOW");
        addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_BOT, botLabel(entity), "BOT");
        addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_FRIEND_TEAM, friendTeamLabel(viewer, entity), "TEAM");
        if (entity instanceof Player target) {
            addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_PING, pingLabel(target), "PING");
            addFlagIfEnabled(labels, mask, VisualSettings.ESP_ITEM_GAMEMODE, gamemodeLabel(target), "GM");
        }
    }

    private static void addEquipmentLabels(List<EspTextLabel> labels, long mask, LivingEntity entity) {
        addIfEnabled(labels, mask, VisualSettings.ESP_ITEM_HELD_ITEM_TEXT, heldItemLabel(entity));
        addIfEnabled(labels, mask, VisualSettings.ESP_ITEM_ARMOR_TEXT, armorTextLabel(entity));
        addIfEnabled(labels, mask, VisualSettings.ESP_ITEM_DURABILITY, durabilityLabel(entity));
        addIfEnabled(labels, mask, VisualSettings.ESP_ITEM_ENCHANTS, enchantsLabel(entity));
    }


    private static void addFlagIfEnabled(List<EspTextLabel> labels, long mask, long item, String fullLabel, String shortLabel) {
        if ((mask & item) != 0L && fullLabel != null && !fullLabel.isBlank()) {
            String text = VisualSettings.visualEspFlagTextShort(item) ? shortLabel : fullLabel;
            labels.add(new EspTextLabel(item, text, VisualSettings.visualEspItemColor(item)));
        }
    }

    private static void addIfEnabled(List<EspTextLabel> labels, long mask, long item, String label) {
        if ((mask & item) != 0L && label != null && !label.isBlank()) {
            labels.add(new EspTextLabel(item, label, VisualSettings.visualEspItemColor(item)));
        }
    }

    private static String armorLabel(LivingEntity entity) {
        int armor = entity.getArmorValue();
        return armor > 0 ? "Armor " + armor : "";
    }

    private static String absorptionLabel(LivingEntity entity) {
        float absorption = entity.getAbsorptionAmount();
        return absorption > 0.0f ? "Absorb " + Math.round(absorption) : "";
    }

    private static String heldItemLabel(LivingEntity entity) {
        ItemStack stack = entity.getMainHandItem();
        if (stack.isEmpty()) {
            stack = entity.getOffhandItem();
        }
        if (stack.isEmpty()) {
            return "";
        }
        String name = stack.getHoverName().getString();
        return name == null || name.isBlank() ? "" : name;
    }

    private static String heldItemIconLabel(LivingEntity entity) {
        return heldItemLabel(entity).isEmpty() ? "" : "Item Icon";
    }

    private static String armorTextLabel(LivingEntity entity) {
        List<String> pieces = new ArrayList<>();
        for (ItemStack stack : entity.getArmorSlots()) {
            if (!stack.isEmpty()) {
                pieces.add(stack.getHoverName().getString());
            }
        }
        return pieces.isEmpty() ? "" : "Armor " + pieces.size() + "/4";
    }

    private static String armorIconsLabel(LivingEntity entity) {
        return armorTextLabel(entity).isEmpty() ? "" : "Armor Icons";
    }

    private static String durabilityLabel(LivingEntity entity) {
        int minPercent = 101;
        for (ItemStack stack : equipmentStacks(entity)) {
            if (stack.isEmpty() || !stack.isDamageableItem()) {
                continue;
            }
            int max = Math.max(1, stack.getMaxDamage());
            int remaining = Math.max(0, max - stack.getDamageValue());
            minPercent = Math.min(minPercent, Math.round(remaining * 100.0f / max));
        }
        return minPercent <= 100 ? "Dur " + minPercent + "%" : "";
    }

    private static String enchantsLabel(LivingEntity entity) {
        for (ItemStack stack : equipmentStacks(entity)) {
            if (!stack.isEmpty() && stack.isEnchanted()) {
                return "Enchants";
            }
        }
        return "";
    }

    private static String totemLabel(LivingEntity entity) {
        return entity.getMainHandItem().is(Items.TOTEM_OF_UNDYING)
                || entity.getOffhandItem().is(Items.TOTEM_OF_UNDYING)
                ? "Totem" : "";
    }

    private static String shieldLabel(LivingEntity entity) {
        return entity.getMainHandItem().is(Items.SHIELD)
                || entity.getOffhandItem().is(Items.SHIELD)
                ? "Shield" : "";
    }

    private static String bowCrossbowLabel(LivingEntity entity) {
        ItemStack main = entity.getMainHandItem();
        ItemStack off = entity.getOffhandItem();
        if (main.is(Items.BOW) || off.is(Items.BOW)) {
            return "Bow";
        }
        if (main.is(Items.CROSSBOW) || off.is(Items.CROSSBOW)) {
            return "Crossbow";
        }
        return "";
    }

    private static String eatingLabel(LivingEntity entity) {
        return entity.isUsingItem() && entity.getUseItem().getUseAnimation() == UseAnim.EAT ? "Eating" : "";
    }

    private static String drinkingLabel(LivingEntity entity) {
        return entity.isUsingItem() && entity.getUseItem().getUseAnimation() == UseAnim.DRINK ? "Drinking" : "";
    }

    private static String bowChargingLabel(LivingEntity entity) {
        return entity.isUsingItem() && entity.getUseItem().getUseAnimation() == UseAnim.BOW ? "Bow Charging" : "";
    }

    private static String crossbowChargedLabel(LivingEntity entity) {
        return isChargedCrossbow(entity.getMainHandItem()) || isChargedCrossbow(entity.getOffhandItem())
                ? "Crossbow Charged" : "";
    }

    private static boolean isChargedCrossbow(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(stack);
    }

    private static String potionLabel(LivingEntity entity) {
        int effects = entity.getActiveEffects().size();
        if (effects <= 0) {
            return "";
        }
        return effects == 1 ? "Potion" : "Potion x" + effects;
    }

    private static String botLabel(LivingEntity entity) {
        Minecraft minecraft = Minecraft.getInstance();
        return FakePlayerRegistry.isDummy(entity)
                || entity instanceof Player player && AntiBotService.isBot(minecraft.player, player)
                ? "Bot" : "";
    }

    private static String friendTeamLabel(Player viewer, LivingEntity entity) {
        if (!(entity instanceof Player target)) {
            return "";
        }
        return target == viewer || ChamsProfileResolver.profile(entity) == ChamsProfileResolver.Profile.TEAM ? "Team" : "";
    }


    private static List<ItemStack> equipmentStacks(LivingEntity entity) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(entity.getMainHandItem());
        stacks.add(entity.getOffhandItem());
        for (ItemStack stack : entity.getArmorSlots()) {
            stacks.add(stack);
        }
        return stacks;
    }

    private static String pingLabel(Player target) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null) {
            return "";
        }
        var info = minecraft.getConnection().getPlayerInfo(target.getUUID());
        return info == null ? "" : info.getLatency() + "ms";
    }

    private static String gamemodeLabel(Player target) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null) {
            return "";
        }
        var info = minecraft.getConnection().getPlayerInfo(target.getUUID());
        if (info == null || info.getGameMode() == null) {
            return "";
        }
        return switch (info.getGameMode()) {
            case SURVIVAL -> "Survival";
            case CREATIVE -> "Creative";
            case ADVENTURE -> "Adventure";
            case SPECTATOR -> "Spectator";
        };
    }

    private static void addBoxRects(List<WorldRect> rects, ScreenBounds bounds, int color) {
        float x = Math.round(bounds.minX());
        float y = Math.round(bounds.minY());
        float w = Math.max(2.0f, Math.round(bounds.width()));
        float h = Math.max(2.0f, Math.round(bounds.height()));

        addRectBorder(rects, x - 0.5f, y - 0.5f, w + 1.0f, h + 1.0f, 0.5f, BOX_BORDER);
        addRectBorder(rects, x, y, w, h, 1.0f, color);
        if (w > 3.0f && h > 3.0f) {
            addRectBorder(rects, x + 1.0f, y + 1.0f, w - 2.0f, h - 2.0f, 0.5f, BOX_BORDER);
        }
    }

    private static void addHealthRects(List<WorldRect> rects, LayoutBounds bounds, LivingEntity entity) {
        float rawRatio = rawHealthRatio(entity);
        float ratio = Math.max(0.0f, Math.min(1.0f, rawRatio));
        int color = VisualSettings.mobHealthDynamicColor() ? dynamicHealthColor(ratio) : VisualSettings.mobHealthColor();
        if (rawRatio > 1.0f) {
            color = lerpColorRgbKeepingAlpha(color, HEALTH_OVERFLOW_COLOR, overflowAmount(rawRatio));
        }
        VisualSettings.EspAnchor anchor = VisualSettings.mobHealthAnchor();
        float x = bounds.x() + VisualSettings.mobHealthOffsetX();
        float y = bounds.y() + VisualSettings.mobHealthOffsetY();
        addBarRects(rects, new LayoutBounds(x, y, bounds.w(), bounds.h()), anchor, ratio, color, HEALTH_THICKNESS);
    }

    private static void addArmorBarRects(List<WorldRect> rects, LayoutBounds bounds,
                                         VisualSettings.EspAnchor anchor, LivingEntity entity, int baseColor) {
        float rawRatio = rawArmorRatio(entity);
        float ratio = Math.max(0.0f, Math.min(1.0f, rawRatio));
        int color = rawRatio > 1.0f
                ? lerpColorRgbKeepingAlpha(baseColor, ARMOR_OVERFLOW_COLOR, overflowAmount(rawRatio))
                : baseColor;
        addBarRects(rects, bounds, anchor, ratio, color, MAIN_BAR_THICKNESS);
    }

    private static int armorBarColor(LivingEntity entity) {
        if (!VisualSettings.visualEspItemDynamicColor(VisualSettings.ESP_ITEM_ARMOR_BAR)) {
            return VisualSettings.visualEspItemColor(VisualSettings.ESP_ITEM_ARMOR_BAR);
        }
        float ratio = Math.max(0.0f, Math.min(1.0f, entity.getArmorValue() / 20.0f));
        int red = Math.round(255.0f * (1.0f - ratio));
        int green = Math.round(156.0f + 99.0f * ratio);
        int blue = Math.round(255.0f);
        return 0xF2000000 | (red << 16) | (green << 8) | blue;
    }

    private static void addAbsorptionRects(List<WorldRect> rects, LayoutBounds bounds,
                                           VisualSettings.EspAnchor anchor, LivingEntity entity, int color) {
        addBarRects(rects, bounds, anchor, absorptionRatio(entity), color, MAIN_BAR_THICKNESS);
    }

    private static int absorptionBarColor(LivingEntity entity) {
        if (!VisualSettings.visualEspItemDynamicColor(VisualSettings.ESP_ITEM_ABSORPTION)) {
            return VisualSettings.visualEspItemColor(VisualSettings.ESP_ITEM_ABSORPTION);
        }
        float ratio = Math.max(0.0f, Math.min(1.0f, entity.getAbsorptionAmount() / Math.max(1.0f, entity.getMaxHealth())));
        int red = 255;
        int green = Math.round(184.0f + 71.0f * ratio);
        int blue = Math.round(48.0f + 48.0f * ratio);
        return 0xF2000000 | (red << 16) | (green << 8) | blue;
    }

    private static float rawHealthRatio(LivingEntity entity) {
        return entity.getHealth() / Math.max(1.0f, entity.getMaxHealth());
    }

    private static float healthRatio(LivingEntity entity) {
        return Math.max(0.0f, Math.min(1.0f, rawHealthRatio(entity)));
    }

    private static float rawArmorRatio(LivingEntity entity) {
        return entity.getArmorValue() / 20.0f;
    }

    private static float armorRatio(LivingEntity entity) {
        return Math.max(0.0f, Math.min(1.0f, rawArmorRatio(entity)));
    }

    private static float absorptionRatio(LivingEntity entity) {
        return Math.max(0.0f, Math.min(1.0f, entity.getAbsorptionAmount() / Math.max(1.0f, entity.getMaxHealth())));
    }

    private static void addBarRects(List<WorldRect> rects, LayoutBounds bounds,
                                    VisualSettings.EspAnchor anchor, float ratio, int color, float thickness) {
        float clamped = Math.max(0.0f, Math.min(1.0f, ratio));
        float x = bounds.x();
        float y = bounds.y();
        if (anchor == VisualSettings.EspAnchor.TOP || anchor == VisualSettings.EspAnchor.BOTTOM) {
            float w = Math.max(8.0f, bounds.w());
            float h = thickness;
            float fillW = w * clamped;
            rects.add(new WorldRect(x, y, w, h, HEALTH_BACKGROUND));
            rects.add(new WorldRect(x, y, fillW, h, color));
            addRectBorder(rects, x - 0.5f, y - 0.5f, w + 1.0f, h + 1.0f, 0.5f, HEALTH_BORDER);
        } else {
            float w = thickness;
            float h = Math.max(8.0f, bounds.h());
            float fillH = h * clamped;
            rects.add(new WorldRect(x, y, w, h, HEALTH_BACKGROUND));
            rects.add(new WorldRect(x, y + h - fillH, w, fillH, color));
            addRectBorder(rects, x - 0.5f, y - 0.5f, w + 1.0f, h + 1.0f, 0.5f, HEALTH_BORDER);
        }
    }

    private static void addHurtTimeRects(List<WorldRect> rects, ScreenBounds bounds, float hurtAge) {
        float duration = Math.max(1.0f, VisualSettings.visualHurtTimeDisplayTicks());
        float alpha = Math.max(0.12f, Math.min(0.45f, (1.0f - Math.min(1.0f, hurtAge / duration)) * 0.45f));
        int baseColor = VisualSettings.visualEspItemColor(VisualSettings.ESP_ITEM_HURT_TIME);
        int color = ((Math.round(alpha * 255.0f) & 0xFF) << 24) | (baseColor & 0x00FFFFFF);
        addRectBorder(rects, bounds.minX() - 1.0f, bounds.minY() - 1.0f,
                bounds.width() + 2.0f, bounds.height() + 2.0f, 1.0f, color);
    }


    private static boolean hurtTimeVisible(LivingEntity entity) {
        if (entity.hurtTime > 0) {
            HURT_TIME_TICKS.put(entity.getUUID(), renderTick);
            return true;
        }
        Long lastHurtTick = HURT_TIME_TICKS.get(entity.getUUID());
        if (lastHurtTick == null) {
            return false;
        }
        boolean visible = renderTick - lastHurtTick <= Math.round(VisualSettings.visualHurtTimeDisplayTicks());
        if (!visible) {
            HURT_TIME_TICKS.remove(entity.getUUID());
        }
        return visible;
    }

    private static float hurtTimeAge(LivingEntity entity) {
        Long lastHurtTick = HURT_TIME_TICKS.get(entity.getUUID());
        return lastHurtTick == null ? 0.0f : Math.max(0.0f, renderTick - lastHurtTick);
    }

    private static void addRectBorder(List<WorldRect> rects, float x, float y, float w, float h, float thickness, int color) {
        float line = Math.max(0.5f, thickness);
        rects.add(new WorldRect(x, y, w, line, color));
        rects.add(new WorldRect(x, y + h - line, w, line, color));
        rects.add(new WorldRect(x, y, line, h, color));
        rects.add(new WorldRect(x + w - line, y, line, h, color));
    }

    private static void emitRect(BufferBuilder buffer, Matrix4f matrix, WorldRect rect) {
        if (rect.w() <= 0.0f || rect.h() <= 0.0f) {
            return;
        }
        float x0 = rect.x();
        float y0 = rect.y();
        float x1 = rect.x() + rect.w();
        float y1 = rect.y() + rect.h();
        float a = (float) (rect.color() >>> 24) / 255.0f;
        float r = (float) (rect.color() >> 16 & 255) / 255.0f;
        float g = (float) (rect.color() >> 8 & 255) / 255.0f;
        float b = (float) (rect.color() & 255) / 255.0f;
        buffer.vertex(matrix, x0, y1, 0.0f).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y1, 0.0f).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y0, 0.0f).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x0, y0, 0.0f).color(r, g, b, a).endVertex();
    }

    private static LayoutSize healthSize(ScreenBounds bounds, VisualSettings.EspAnchor anchor) {
        if (anchor == VisualSettings.EspAnchor.TOP || anchor == VisualSettings.EspAnchor.BOTTOM) {
            return new LayoutSize(Math.max(8.0f, bounds.width()), HEALTH_THICKNESS);
        }
        return new LayoutSize(HEALTH_THICKNESS, Math.max(8.0f, bounds.height()));
    }

    private static LayoutSize mainBarSize(ScreenBounds bounds, VisualSettings.EspAnchor anchor) {
        if (anchor == VisualSettings.EspAnchor.TOP || anchor == VisualSettings.EspAnchor.BOTTOM) {
            return new LayoutSize(Math.max(8.0f, bounds.width()), MAIN_BAR_THICKNESS);
        }
        return new LayoutSize(MAIN_BAR_THICKNESS, Math.max(8.0f, bounds.height()));
    }

    private static LayoutBounds place(ScreenBounds box, Map<VisualSettings.EspAnchor, Integer> offsets,
                                      VisualSettings.EspAnchor anchor, LayoutSize size) {
        int offset = offsets.getOrDefault(anchor, 0);
        LayoutBounds bounds;
        if (anchor == VisualSettings.EspAnchor.BOTTOM) {
            float x = box.minX() + box.width() * 0.5f - size.w() * 0.5f;
            float y = box.maxY() + LAYOUT_GAP + offset;
            bounds = new LayoutBounds(x, y, size.w(), size.h());
            offsets.put(anchor, offset + Math.round(size.h() + LAYOUT_GAP));
        } else if (anchor == VisualSettings.EspAnchor.LEFT) {
            float x = box.minX() - size.w() - LAYOUT_GAP - offset;
            float y = box.minY();
            bounds = new LayoutBounds(x, y, size.w(), size.h());
            offsets.put(anchor, offset + Math.round(size.w() + LAYOUT_GAP));
        } else if (anchor == VisualSettings.EspAnchor.RIGHT) {
            float x = box.maxX() + LAYOUT_GAP + offset;
            float y = box.minY();
            bounds = new LayoutBounds(x, y, size.w(), size.h());
            offsets.put(anchor, offset + Math.round(size.w() + LAYOUT_GAP));
        } else {
            float x = box.minX() + box.width() * 0.5f - size.w() * 0.5f;
            float y = box.minY() - size.h() - LAYOUT_GAP - offset;
            bounds = new LayoutBounds(x, y, size.w(), size.h());
            offsets.put(anchor, offset + Math.round(size.h() + LAYOUT_GAP));
        }
        return bounds;
    }

    private static void reserveGuiOffsetsForBars(Map<VisualSettings.EspAnchor, Integer> offsets,
                                                 ScreenBounds bounds, ChamsProfileResolver.Profile profile) {
        if (mobHealthEnabled(profile)) {
            reserveGuiOffset(offsets, VisualSettings.mobHealthAnchor(), healthSize(bounds, VisualSettings.mobHealthAnchor()));
        }
        long mask = espItemMask(profile);
        if ((mask & VisualSettings.ESP_ITEM_ARMOR_BAR) != 0L) {
            VisualSettings.EspAnchor anchor = VisualSettings.visualEspItemAnchor(VisualSettings.ESP_ITEM_ARMOR_BAR);
            reserveGuiOffset(offsets, anchor, mainBarSize(bounds, anchor));
        }
        if ((mask & VisualSettings.ESP_ITEM_ABSORPTION) != 0L) {
            VisualSettings.EspAnchor anchor = VisualSettings.visualEspItemAnchor(VisualSettings.ESP_ITEM_ABSORPTION);
            reserveGuiOffset(offsets, anchor, mainBarSize(bounds, anchor));
        }
    }

    private static void reserveGuiOffset(Map<VisualSettings.EspAnchor, Integer> offsets,
                                         VisualSettings.EspAnchor anchor, LayoutSize size) {
        int offset = offsets.getOrDefault(anchor, 0);
        float used = anchor == VisualSettings.EspAnchor.LEFT || anchor == VisualSettings.EspAnchor.RIGHT
                ? size.w() : size.h();
        offsets.put(anchor, offset + Math.round(used + LAYOUT_GAP));
    }

    private static void reserveGuiOffsetsForText(Map<VisualSettings.EspAnchor, Integer> offsets,
                                                 ScreenBounds bounds, ChamsProfileResolver.Profile profile,
                                                 Player viewer, LivingEntity entity, double distance,
                                                 List<EquipmentLayoutEntry> equipmentEntries) {
        float nameSize = NAME_BASE_SIZE * VisualSettings.mobNameScale();
        float distanceSize = NAME_BASE_SIZE * VisualSettings.mobDistanceScale();
        float flagSize = Math.max(6.0f, NAME_BASE_SIZE);
        float equipmentSize = Math.max(5.0f, NAME_BASE_SIZE * 0.92f);
        for (VisualSettings.EspAnchor anchor : VisualSettings.EspAnchor.values()) {
            List<LayoutColumnEntry> columnEntries = new ArrayList<>();
            if (mobNameEnabled(profile) && VisualSettings.mobNameAnchor() == anchor) {
                MobTextLayoutEntry entry = new MobTextLayoutEntry(0L, entityName(entity), VisualSettings.mobNameColor(),
                        nameSize, false, VisualSettings.mobNameOffsetX(), VisualSettings.mobNameOffsetY());
                columnEntries.add(LayoutColumnEntry.text(entry, textEntrySize(entry)));
            }
            if (mobDistanceEnabled(profile) && VisualSettings.mobDistanceAnchor() == anchor) {
                MobTextLayoutEntry entry = new MobTextLayoutEntry(0L, mobDistanceLabel(distance), VisualSettings.mobDistanceColor(),
                        distanceSize, false, VisualSettings.mobDistanceOffsetX(), VisualSettings.mobDistanceOffsetY());
                columnEntries.add(LayoutColumnEntry.text(entry, textEntrySize(entry)));
            }
            for (EspTextLabel label : mobFlagLabels(profile, viewer, entity, distance)) {
                if (VisualSettings.visualEspFlagAnchor(label.item()) != anchor) {
                    continue;
                }
                MobTextLayoutEntry entry = new MobTextLayoutEntry(label.item(), label.text(), label.color(),
                        flagSize, true, 0.0f, 0.0f);
                columnEntries.add(LayoutColumnEntry.text(entry, textEntrySize(entry)));
            }
            for (EquipmentLayoutEntry entry : equipmentEntries) {
                if (entry == null || entry.anchor() != anchor) {
                    continue;
                }
                columnEntries.add(LayoutColumnEntry.equipment(entry, equipmentEntrySize(entry, equipmentSize), equipmentSize));
            }
            if (!columnEntries.isEmpty()) {
                reserveGuiOffset(offsets, anchor, columnSize(columnEntries));
            }
        }
    }

    private static LayoutSize textItemSize(String text, float size) {
        if (text == null || text.isBlank()) {
            return new LayoutSize(0.0f, 0.0f);
        }
        float width = MSDFRenderer.width(text, size) + 6.0f;
        float height = Math.max(size + 2.0f, size * 1.15f);
        return new LayoutSize(width, height);
    }

    private static LayoutSize textEntrySize(MobTextLayoutEntry entry) {
        return textItemSize(entry.text(), entry.size());
    }

    private static LayoutSize columnSize(List<LayoutColumnEntry> entries) {
        float width = 0.0f;
        float height = 0.0f;
        LayoutColumnEntry previous = null;
        for (LayoutColumnEntry entry : entries) {
            width = Math.max(width, entry.size().w());
            height += entry.size().h() + (previous == null ? 0.0f : columnEntryGap(previous, entry));
            previous = entry;
        }
        return new LayoutSize(width, height);
    }

    private static float columnEntryGap(LayoutColumnEntry previous, LayoutColumnEntry current) {
        if (previous.equipment() != null && current.equipment() != null) {
            return equipmentEntryGap(previous.equipment(), current.equipment());
        }
        float smaller = Math.min(previous.size().h(), current.size().h());
        return Math.max(0.0f, Math.min(1.0f, smaller * 0.08f));
    }

    private static LayoutSize equipmentEntrySize(EquipmentLayoutEntry entry, float textSize) {
        if (entry.icon()) {
            int count = Math.max(1, entry.stacks().size());
            float gap = 0.0f;
            if (entry.anchor() == VisualSettings.EspAnchor.TOP
                    || entry.anchor() == VisualSettings.EspAnchor.BOTTOM) {
                return new LayoutSize(count * EQUIPMENT_ICON_SLOT + Math.max(0, count - 1) * gap,
                        EQUIPMENT_ICON_SLOT);
            }
            return new LayoutSize(EQUIPMENT_ICON_SLOT,
                    count * EQUIPMENT_ICON_SLOT + Math.max(0, count - 1) * gap);
        }
        return new LayoutSize(MSDFRenderer.width(entry.text(), textSize) + 6.0f,
                Math.max(textSize + 2.0f, textSize * 1.15f));
    }

    private static void renderEquipmentIcons(GuiGraphics graphics, ScreenBounds bounds,
                                             Map<VisualSettings.EspAnchor, Integer> offsets,
                                             ChamsProfileResolver.Profile profile, Player viewer, LivingEntity entity,
                                             double distance, List<EquipmentLayoutEntry> entries) {
        float nameSize = NAME_BASE_SIZE * VisualSettings.mobNameScale();
        float distanceSize = NAME_BASE_SIZE * VisualSettings.mobDistanceScale();
        float flagSize = Math.max(6.0f, NAME_BASE_SIZE);
        float equipmentSize = Math.max(5.0f, NAME_BASE_SIZE * 0.92f);
        for (VisualSettings.EspAnchor anchor : VisualSettings.EspAnchor.values()) {
            List<LayoutColumnEntry> columnEntries = new ArrayList<>();
            if (mobNameEnabled(profile) && VisualSettings.mobNameAnchor() == anchor) {
                MobTextLayoutEntry entry = new MobTextLayoutEntry(0L, entityName(entity), VisualSettings.mobNameColor(),
                        nameSize, false, VisualSettings.mobNameOffsetX(), VisualSettings.mobNameOffsetY());
                columnEntries.add(LayoutColumnEntry.text(entry, textEntrySize(entry)));
            }
            if (mobDistanceEnabled(profile) && VisualSettings.mobDistanceAnchor() == anchor) {
                MobTextLayoutEntry entry = new MobTextLayoutEntry(0L, mobDistanceLabel(distance), VisualSettings.mobDistanceColor(),
                        distanceSize, false, VisualSettings.mobDistanceOffsetX(), VisualSettings.mobDistanceOffsetY());
                columnEntries.add(LayoutColumnEntry.text(entry, textEntrySize(entry)));
            }
            for (EspTextLabel label : mobFlagLabels(profile, viewer, entity, distance)) {
                if (VisualSettings.visualEspFlagAnchor(label.item()) != anchor) {
                    continue;
                }
                MobTextLayoutEntry entry = new MobTextLayoutEntry(label.item(), label.text(), label.color(),
                        flagSize, true, 0.0f, 0.0f);
                columnEntries.add(LayoutColumnEntry.text(entry, textEntrySize(entry)));
            }
            for (EquipmentLayoutEntry entry : entries) {
                if (entry == null || entry.anchor() != anchor) {
                    continue;
                }
                columnEntries.add(LayoutColumnEntry.equipment(entry, equipmentEntrySize(entry, equipmentSize), equipmentSize));
            }
            if (columnEntries.isEmpty()) {
                continue;
            }
            LayoutBounds column = place(bounds, offsets, anchor, columnSize(columnEntries));
            float y = column.y();
            LayoutColumnEntry previous = null;
            for (LayoutColumnEntry columnEntry : columnEntries) {
                if (previous != null) {
                    y += columnEntryGap(previous, columnEntry);
                }
                LayoutSize entrySize = columnEntry.size();
                float x = anchor == VisualSettings.EspAnchor.LEFT ? column.x() + column.w() - entrySize.w() : column.x();
                if (anchor == VisualSettings.EspAnchor.TOP || anchor == VisualSettings.EspAnchor.BOTTOM) {
                    x = column.x() + column.w() * 0.5f - entrySize.w() * 0.5f;
                }
                if (columnEntry.equipment() != null && columnEntry.equipment().icon()) {
                    renderIconStack(graphics, x, y, entrySize, anchor, columnEntry.equipment().stacks());
                }
                y += entrySize.h();
                previous = columnEntry;
            }
        }
    }

    private static float equipmentEntryGap(EquipmentLayoutEntry previous, EquipmentLayoutEntry current) {
        LayoutSize previousSize = equipmentEntrySize(previous, Math.max(5.0f, NAME_BASE_SIZE * 0.92f));
        LayoutSize currentSize = equipmentEntrySize(current, Math.max(5.0f, NAME_BASE_SIZE * 0.92f));
        float smaller = Math.min(previousSize.h(), currentSize.h());
        return Math.max(0.0f, Math.min(1.0f, smaller * 0.08f));
    }

    private static void renderIconStack(GuiGraphics graphics, float x, float y, LayoutSize slotSize,
                                        VisualSettings.EspAnchor anchor, List<ItemStack> stacks) {
        if (stacks.isEmpty()) {
            return;
        }
        float gap = 0.0f;
        PoseStack pose = graphics.pose();
        pose.pushPose();
        try {
            pose.translate(0.0f, 0.0f, 240.0f);
            float scale = EQUIPMENT_ICON_SIZE / 16.0f;
            float drawX = x + (anchor == VisualSettings.EspAnchor.TOP || anchor == VisualSettings.EspAnchor.BOTTOM
                    ? (EQUIPMENT_ICON_SLOT - EQUIPMENT_ICON_SIZE) * 0.5f
                    : (slotSize.w() - EQUIPMENT_ICON_SIZE) * 0.5f);
            float drawY = y + (anchor == VisualSettings.EspAnchor.TOP || anchor == VisualSettings.EspAnchor.BOTTOM
                    ? (slotSize.h() - EQUIPMENT_ICON_SIZE) * 0.5f
                    : (EQUIPMENT_ICON_SLOT - EQUIPMENT_ICON_SIZE) * 0.5f);
            if (anchor == VisualSettings.EspAnchor.TOP) {
                drawY += EQUIPMENT_ICON_TOP_VISUAL_Y_OFFSET;
            }
            for (ItemStack stack : stacks) {
                if (stack.isEmpty()) {
                    continue;
                }
                pose.pushPose();
                try {
                    pose.translate(Math.round(drawX), Math.round(drawY), 0.0f);
                    pose.scale(scale, scale, 1.0f);
                    graphics.renderItem(stack, 0, 0);
                } finally {
                    pose.popPose();
                }
                if (anchor == VisualSettings.EspAnchor.TOP || anchor == VisualSettings.EspAnchor.BOTTOM) {
                    drawX += EQUIPMENT_ICON_SLOT + gap;
                } else {
                    drawY += EQUIPMENT_ICON_SLOT + gap;
                }
            }
        } finally {
            pose.popPose();
        }
    }

    private static List<ItemStack> heldIconStacks(LivingEntity entity) {
        List<ItemStack> stacks = new ArrayList<>();
        if (!entity.getMainHandItem().isEmpty()) {
            stacks.add(entity.getMainHandItem());
        }
        if (!entity.getOffhandItem().isEmpty()) {
            stacks.add(entity.getOffhandItem());
        }
        return stacks;
    }

    private static List<ItemStack> armorIconStacks(LivingEntity entity) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : entity.getArmorSlots()) {
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    private static List<ItemStack> hotbarMatchingIconStacks(LivingEntity entity, net.minecraft.world.item.Item item) {
        List<ItemStack> stacks = new ArrayList<>();
        if (!(entity instanceof Player player)) {
            return stacks;
        }
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.is(item)) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    private static List<ItemStack> hotbarBowIconStacks(LivingEntity entity) {
        List<ItemStack> stacks = new ArrayList<>();
        if (!(entity instanceof Player player)) {
            return stacks;
        }
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && (stack.is(Items.BOW) || stack.is(Items.CROSSBOW))) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    private static String entityName(LivingEntity entity) {
        Component component = entity.getDisplayName();
        String value = component == null ? "" : component.getString();
        return value == null ? "" : value;
    }

    private static String mobDistanceLabel(double distance) {
        return "[" + Math.round(distance) + "m]";
    }

    private static int dynamicHealthColor(float ratio) {
        int red;
        int green;
        if (ratio >= 0.5f) {
            float t = (ratio - 0.5f) * 2.0f;
            red = Math.round(255.0f * (1.0f - t));
            green = 255;
        } else {
            float t = ratio * 2.0f;
            red = 255;
            green = Math.round(255.0f * t);
        }
        return 0xF2000000 | (red << 16) | (green << 8);
    }

    private static float overflowAmount(float rawRatio) {
        return Math.max(0.0f, Math.min(1.0f, (rawRatio - 1.0f) / 1.0f));
    }

    private static int lerpColorRgbKeepingAlpha(int from, int to, float amount) {
        float t = Math.max(0.0f, Math.min(1.0f, amount));
        int alpha = from & 0xFF000000;
        int fromR = from >> 16 & 0xFF;
        int fromG = from >> 8 & 0xFF;
        int fromB = from & 0xFF;
        int toR = to >> 16 & 0xFF;
        int toG = to >> 8 & 0xFF;
        int toB = to & 0xFF;
        int red = Math.round(fromR + (toR - fromR) * t);
        int green = Math.round(fromG + (toG - fromG) * t);
        int blue = Math.round(fromB + (toB - fromB) * t);
        return alpha | (red << 16) | (green << 8) | blue;
    }

    private static float easeOutCubic(float value) {
        float t = Math.max(0.0f, Math.min(1.0f, value));
        float inverse = 1.0f - t;
        return 1.0f - inverse * inverse * inverse;
    }

    private static int applyAlpha(int color, float alphaMultiplier) {
        float amount = Math.max(0.0f, Math.min(1.0f, alphaMultiplier));
        int alpha = Math.round(((color >>> 24) & 0xFF) * amount);
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float fixedBillboardScaleRelative(Camera camera, WorldPoint anchor, Matrix4f projectionMatrix, float multiplier) {
        return fixedBillboardScaleRelative(camera, anchor, projectionMatrix, Minecraft.getInstance().getWindow().getHeight(), multiplier);
    }

    private static float fixedBillboardScaleRelative(Camera camera, WorldPoint anchor, Matrix4f projectionMatrix,
                                                     int viewportH, float multiplier) {
        Vector3f look = camera.getLookVector();
        double depth = anchor.x() * look.x() + anchor.y() * look.y() + anchor.z() * look.z();
        if (!Double.isFinite(depth) || depth <= 0.01D) {
            return -1.0f;
        }

        float projectionY = Math.abs(projectionMatrix.m11());
        if (!Float.isFinite(projectionY) || projectionY <= 0.0001f || viewportH <= 0) {
            return 0.025f * multiplier;
        }
        float scale = (float) (2.0D * Math.max(0.25D, depth) / (projectionY * viewportH)) * multiplier;
        return Float.isFinite(scale) ? scale : -1.0f;
    }

    private static boolean finite(Vector4f value) {
        return value != null
                && Float.isFinite(value.x)
                && Float.isFinite(value.y)
                && Float.isFinite(value.z)
                && Float.isFinite(value.w);
    }

    private record ScreenPoint(float x, float y) {
    }

    private record OffscreenProjection(ScreenPoint point, boolean behindCamera) {
    }

    private record ScreenBounds(float minX, float minY, float maxX, float maxY) {
        private float width() {
            return maxX - minX;
        }

        private float height() {
            return maxY - minY;
        }

        private boolean valid(int screenW, int screenH) {
            return Float.isFinite(minX)
                    && Float.isFinite(minY)
                    && Float.isFinite(maxX)
                    && Float.isFinite(maxY)
                    && maxX > 0.0f
                    && maxY > 0.0f
                    && minX < screenW
                    && minY < screenH
                    && width() >= 2.0f
                    && height() >= 2.0f
                    && width() < screenW * 2.0f
                    && height() < screenH * 2.0f;
        }
    }

    private record LayoutSize(float w, float h) {
    }

    private record LayoutBounds(float x, float y, float w, float h) {
    }

    private record MobWorldDraw(WorldPoint anchor, float scale, List<WorldRect> rects, boolean throughWalls) {
    }

    private record MobWorldTextDraw(WorldPoint anchor, float scale, String text, float x, float y, float size, int color,
                                    boolean throughWalls) {
    }

    private record EspTextLabel(long item, String text, int color) {
    }

    private record MobTextLayoutEntry(long item, String text, int color, float size, boolean flag,
                                      float offsetX, float offsetY) {
    }

    private record EquipmentLayoutEntry(long item, boolean icon, String text, int color,
                                        VisualSettings.EspAnchor anchor, List<ItemStack> stacks) {
    }

    private record LayoutColumnEntry(MobTextLayoutEntry text, EquipmentLayoutEntry equipment,
                                     LayoutSize size, float textSize) {
        private static LayoutColumnEntry text(MobTextLayoutEntry text, LayoutSize size) {
            return new LayoutColumnEntry(text, null, size, text.size());
        }

        private static LayoutColumnEntry equipment(EquipmentLayoutEntry equipment, LayoutSize size, float textSize) {
            return new LayoutColumnEntry(null, equipment, size, textSize);
        }

        private boolean icon() {
            return equipment != null && equipment.icon();
        }
    }

    private record WorldRect(float x, float y, float w, float h, int color) {
    }

    private record WorldPoint(double x, double y, double z) {
    }

    private record FlagAnimationKey(UUID entityId, long item) {
    }

    private static final class FlagAnimationState {
        private float progress;
        private WorldPoint anchor;
        private float scale;
        private String text;
        private float x;
        private float y;
        private float displayY;
        private float size;
        private int color = 0xF2FFFFFF;
        private boolean throughWalls;
        private boolean initialized;
    }
}
