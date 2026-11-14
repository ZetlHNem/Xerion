package com.xerion.forge.visual;

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
import com.xerion.forge.gui.font.MSDFRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public final class WorldEffectRenderer {
    private static final int SOUND_MARKER_LIFETIME_TICKS = 48;
    private static final int SOUND_MARKER_MAX = 64;
    private static final float SOUND_MARKER_FONT_SIZE = 10.0f;
    private static final float SOUND_MARKER_SCREEN_SCALE = 1.0f;
    private static final int SOUND_RING_SEGMENTS = 48;
    private static final float SOUND_RING_MIN_RADIUS = 0.18f;
    private static final float SOUND_RING_MAX_RADIUS = 1.15f;
    private static final float SOUND_RING_MIN_THICKNESS = 0.035f;
    private static final float SOUND_RING_MAX_THICKNESS = 0.075f;
    private static final int SOUL_PARTICLE_LIFETIME_TICKS = 34;
    private static final int SOUL_PARTICLE_BURST_COUNT = 14;
    private static final int SOUL_PARTICLE_MAX = 192;
    private static final float SOUL_PARTICLE_MIN_SIZE = 0.045f;
    private static final float SOUL_PARTICLE_MAX_SIZE = 0.095f;
    private static final double SOUL_PARTICLE_RANGE = 96.0D;

    private final List<SoundMarker> soundMarkers = new ArrayList<>();
    private final List<SoulParticle> soulParticles = new ArrayList<>();
    private final Map<Integer, Integer> soulParticleSpawnTicks = new HashMap<>();
    private final Random soulParticleRandom = new Random();

    public void clear() {
        synchronized (soundMarkers) {
            soundMarkers.clear();
        }
        synchronized (soulParticles) {
            soulParticles.clear();
        }
        soulParticleSpawnTicks.clear();
    }

    public void tick(Minecraft minecraft) {
        updateSoundMarkers(minecraft);
        updateSoulParticles(minecraft);
    }

    public boolean hasSoundMarkers() {
        return !soundMarkers.isEmpty();
    }

    public boolean hasSoulParticles() {
        return !soulParticles.isEmpty();
    }

    public void trackSound(SoundInstance sound) {
        if (!VisualSettings.visualEspEnabled() || !VisualSettings.visualSounds()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || sound == null || !shouldTrackSound(sound)) {
            return;
        }

        double x = sound.getX();
        double y = sound.getY();
        double z = sound.getZ();
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            return;
        }
        if (player.distanceToSqr(x, y, z) > 128.0D * 128.0D) {
            return;
        }

        synchronized (soundMarkers) {
            addOrRefreshSoundMarker(x, y, z, soundLabel(sound));
        }
    }

    public void renderSoundMarkers(Minecraft minecraft, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix) {
        if (soundMarkers.isEmpty() || minecraft.player == null) {
            return;
        }
        List<SoundMarker> markers;
        synchronized (soundMarkers) {
            markers = new ArrayList<>(soundMarkers);
        }
        if (markers.isEmpty()) {
            return;
        }

        RenderSystem.disableDepthTest();
        try {
            Vec3 cameraPos = camera.getPosition();
            int baseColor = VisualSettings.visualSoundsColor();
            int baseAlpha = (baseColor >>> 24) & 0xFF;
            int rgb = baseColor & 0x00FFFFFF;

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder ringBuffer = Tesselator.getInstance().getBuilder();
            ringBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            Matrix4f matrix = poseStack.last().pose();
            for (SoundMarker marker : markers) {
                float progress = soundMarkerProgress(marker);
                int alpha = soundMarkerAlpha(baseAlpha, progress);
                if (alpha <= 2) {
                    continue;
                }
                emitSoundRing(ringBuffer, matrix, marker, cameraPos, rgb, alpha, progress);
            }
            BufferUploader.drawWithShader(ringBuffer.end());

            try (MSDFRenderer.WorldTextBatch batch = MSDFRenderer.beginWorldTextBatch(true)) {
                for (SoundMarker marker : markers) {
                    float progress = soundMarkerProgress(marker);
                    int alpha = soundMarkerAlpha(baseAlpha, progress);
                    if (alpha <= 2) {
                        continue;
                    }

                    double distance = minecraft.player.distanceToSqr(marker.x, marker.y, marker.z);
                    String text = marker.label + " " + Math.round(Math.sqrt(distance)) + "m";
                    WorldPoint anchor = new WorldPoint(
                            marker.x - cameraPos.x,
                            marker.y - cameraPos.y + 0.35D,
                            marker.z - cameraPos.z
                    );
                    float scale = fixedBillboardScaleRelative(camera, anchor, projectionMatrix, SOUND_MARKER_SCREEN_SCALE);
                    float textWidth = MSDFRenderer.width(text, SOUND_MARKER_FONT_SIZE);

                    poseStack.pushPose();
                    try {
                        poseStack.translate(anchor.x(), anchor.y(), anchor.z());
                        poseStack.mulPose(camera.rotation());
                        poseStack.scale(-scale, -scale, scale);
                        MSDFRenderer.drawWorldTextWithShadow(batch, poseStack.last().pose(), text, -textWidth * 0.5f, 0.0f,
                                SOUND_MARKER_FONT_SIZE, (alpha << 24) | rgb);
                    } finally {
                        poseStack.popPose();
                    }
                }
            }
        } finally {
            RenderSystem.enableDepthTest();
        }
    }

    public void renderSoulParticles(PoseStack poseStack, Camera camera) {
        List<SoulParticle> particles;
        synchronized (soulParticles) {
            particles = new ArrayList<>(soulParticles);
        }
        if (particles.isEmpty()) {
            return;
        }

        RenderSystem.disableDepthTest();
        try {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            Matrix4f matrix = poseStack.last().pose();
            Vec3 cameraPos = camera.getPosition();
            Vector3f right = new Vector3f(camera.getLeftVector()).mul(-1.0f);
            Vector3f up = new Vector3f(camera.getUpVector());
            int baseColor = VisualSettings.visualSoulParticlesColor();
            int baseAlpha = (baseColor >>> 24) & 0xFF;
            int rgb = baseColor & 0x00FFFFFF;

            for (SoulParticle particle : particles) {
                float progress = Math.max(0.0f, Math.min(1.0f, particle.ticksLeft / (float) particle.lifetime));
                float eased = progress * progress * (3.0f - 2.0f * progress);
                int alpha = Math.round(baseAlpha * eased);
                if (alpha <= 2) {
                    continue;
                }
                float pulse = 0.85f + 0.15f * (float) Math.sin((particle.age + particle.seed) * 0.45f);
                float size = particle.size * (0.75f + (1.0f - progress) * 0.65f) * pulse;
                emitSoulParticle(buffer, matrix, cameraPos, right, up, particle, rgb, alpha, size);
            }

            BufferUploader.drawWithShader(buffer.end());
        } finally {
            RenderSystem.enableDepthTest();
        }
    }

    private void updateSoundMarkers(Minecraft minecraft) {
        if (!VisualSettings.visualSounds() || minecraft.level == null || minecraft.player == null) {
            synchronized (soundMarkers) {
                soundMarkers.clear();
            }
            return;
        }
        synchronized (soundMarkers) {
            for (int i = soundMarkers.size() - 1; i >= 0; i--) {
                SoundMarker marker = soundMarkers.get(i);
                if (marker.ticksLeft <= 1) {
                    soundMarkers.remove(i);
                } else {
                    marker.ticksLeft--;
                }
            }
        }
    }

    private void updateSoulParticles(Minecraft minecraft) {
        if (!VisualSettings.visualEspEnabled()
                || !VisualSettings.visualSoulParticles()
                || minecraft.level == null
                || minecraft.player == null) {
            synchronized (soulParticles) {
                soulParticles.clear();
            }
            soulParticleSpawnTicks.clear();
            return;
        }

        synchronized (soulParticles) {
            for (int i = soulParticles.size() - 1; i >= 0; i--) {
                SoulParticle particle = soulParticles.get(i);
                if (particle.ticksLeft <= 1) {
                    soulParticles.remove(i);
                    continue;
                }
                particle.ticksLeft--;
                particle.age++;
                particle.x += particle.vx;
                particle.y += particle.vy;
                particle.z += particle.vz;
                particle.vx *= 0.955D;
                particle.vz *= 0.955D;
                particle.vy = particle.vy * 0.965D + 0.0014D;
            }
        }

        int now = minecraft.player.tickCount;
        soulParticleSpawnTicks.entrySet().removeIf(entry -> now - entry.getValue() > SOUL_PARTICLE_LIFETIME_TICKS * 3);
        double rangeSq = SOUL_PARTICLE_RANGE * SOUL_PARTICLE_RANGE;
        for (LivingEntity entity : minecraft.level.getEntitiesOfClass(LivingEntity.class,
                minecraft.player.getBoundingBox().inflate(SOUL_PARTICLE_RANGE))) {
            if (entity == minecraft.player || entity.isSpectator() || minecraft.player.distanceToSqr(entity) > rangeSq) {
                continue;
            }
            if (entity.deathTime <= 0 && entity.isAlive()) {
                continue;
            }
            int entityId = entity.getId();
            if (soulParticleSpawnTicks.containsKey(entityId)) {
                continue;
            }
            soulParticleSpawnTicks.put(entityId, now);
            spawnSoulParticleBurst(entity);
        }

        for (LivingEntity dummy : FakePlayerRegistry.dummyEntities()) {
            if (dummy != minecraft.player
                    && minecraft.player.distanceToSqr(dummy) <= rangeSq
                    && (dummy.deathTime > 0 || !dummy.isAlive())
                    && !soulParticleSpawnTicks.containsKey(dummy.getId())) {
                soulParticleSpawnTicks.put(dummy.getId(), now);
                spawnSoulParticleBurst(dummy);
            }
        }
    }

    private void spawnSoulParticleBurst(LivingEntity entity) {
        AABB box = entity.getBoundingBox();
        double centerX = (box.minX + box.maxX) * 0.5D;
        double centerY = box.minY + Math.max(0.35D, entity.getBbHeight() * 0.58D);
        double centerZ = (box.minZ + box.maxZ) * 0.5D;
        synchronized (soulParticles) {
            while (soulParticles.size() > SOUL_PARTICLE_MAX - SOUL_PARTICLE_BURST_COUNT) {
                soulParticles.remove(0);
            }
            for (int i = 0; i < SOUL_PARTICLE_BURST_COUNT; i++) {
                double angle = soulParticleRandom.nextDouble() * Math.PI * 2.0D;
                double radius = 0.10D + soulParticleRandom.nextDouble() * Math.max(0.18D, entity.getBbWidth() * 0.38D);
                double speed = 0.014D + soulParticleRandom.nextDouble() * 0.028D;
                double vx = Math.cos(angle) * speed;
                double vz = Math.sin(angle) * speed;
                double vy = 0.018D + soulParticleRandom.nextDouble() * 0.035D;
                float size = SOUL_PARTICLE_MIN_SIZE
                        + soulParticleRandom.nextFloat() * (SOUL_PARTICLE_MAX_SIZE - SOUL_PARTICLE_MIN_SIZE);
                int lifetime = SOUL_PARTICLE_LIFETIME_TICKS - 6 + soulParticleRandom.nextInt(13);
                soulParticles.add(new SoulParticle(
                        centerX + Math.cos(angle) * radius,
                        centerY + soulParticleRandom.nextDouble() * Math.max(0.35D, entity.getBbHeight() * 0.35D),
                        centerZ + Math.sin(angle) * radius,
                        vx,
                        vy,
                        vz,
                        size,
                        Math.max(12, lifetime)
                ));
            }
        }
    }

    private void addOrRefreshSoundMarker(double x, double y, double z, String label) {
        for (SoundMarker marker : soundMarkers) {
            if (!marker.label.equals(label)) {
                continue;
            }
            double dx = marker.x - x;
            double dy = marker.y - y;
            double dz = marker.z - z;
            if (dx * dx + dy * dy + dz * dz <= 1.0D) {
                marker.x = (marker.x * 0.65D) + (x * 0.35D);
                marker.y = (marker.y * 0.65D) + (y * 0.35D);
                marker.z = (marker.z * 0.65D) + (z * 0.35D);
                marker.ticksLeft = SOUND_MARKER_LIFETIME_TICKS;
                return;
            }
        }

        soundMarkers.add(new SoundMarker(x, y, z, label, SOUND_MARKER_LIFETIME_TICKS));
        while (soundMarkers.size() > SOUND_MARKER_MAX) {
            soundMarkers.remove(0);
        }
    }

    private static boolean shouldTrackSound(SoundInstance sound) {
        if (sound.isRelative() || sound.getAttenuation() == SoundInstance.Attenuation.NONE) {
            return false;
        }
        SoundSource source = sound.getSource();
        if (source != SoundSource.HOSTILE && source != SoundSource.NEUTRAL) {
            return false;
        }
        ResourceLocation location = sound.getLocation();
        if (location == null) {
            return false;
        }
        String path = location.getPath();
        return path != null && path.startsWith("entity.");
    }

    private static String soundLabel(SoundInstance sound) {
        ResourceLocation location = sound.getLocation();
        String path = location == null ? "sound" : location.getPath();
        if (path == null || path.isBlank()) {
            return "Sound";
        }

        String[] parts = path.split("[./_]");
        StringBuilder builder = new StringBuilder();
        int added = 0;
        for (int i = parts.length - 1; i >= 0 && added < 2; i--) {
            String part = parts[i];
            if (part == null || part.isBlank() || part.length() <= 1 && Character.isDigit(part.charAt(0))) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.insert(0, ' ');
            }
            builder.insert(0, prettyWord(part));
            added++;
        }
        return builder.isEmpty() ? "Sound" : builder.toString();
    }

    private static String prettyWord(String word) {
        if (word == null || word.isBlank()) {
            return "";
        }
        if (word.length() == 1) {
            return word.toUpperCase(Locale.ROOT);
        }
        return word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1).toLowerCase(Locale.ROOT);
    }

    private static float soundMarkerProgress(SoundMarker marker) {
        return Math.max(0.0f, Math.min(1.0f, marker.ticksLeft / (float) SOUND_MARKER_LIFETIME_TICKS));
    }

    private static int soundMarkerAlpha(int baseAlpha, float progress) {
        float eased = progress * progress * (3.0f - 2.0f * progress);
        return Math.max(0, Math.min(255, Math.round(baseAlpha * eased)));
    }

    private static void emitSoundRing(BufferBuilder buffer, Matrix4f matrix, SoundMarker marker, Vec3 cameraPos,
                                      int rgb, int alpha, float progress) {
        float age = 1.0f - progress;
        float radius = SOUND_RING_MIN_RADIUS + (SOUND_RING_MAX_RADIUS - SOUND_RING_MIN_RADIUS) * easeOutCubic(age);
        float thickness = SOUND_RING_MIN_THICKNESS + (SOUND_RING_MAX_THICKNESS - SOUND_RING_MIN_THICKNESS) * age;
        float outerRadius = radius + thickness;
        float innerRadius = Math.max(0.01f, radius - thickness);
        float red = ((rgb >>> 16) & 0xFF) / 255.0f;
        float green = ((rgb >>> 8) & 0xFF) / 255.0f;
        float blue = (rgb & 0xFF) / 255.0f;
        float centerAlpha = (alpha / 255.0f) * 0.48f;
        float x = (float) (marker.x - cameraPos.x);
        float y = (float) (marker.y - cameraPos.y + 0.025D);
        float z = (float) (marker.z - cameraPos.z);

        for (int i = 0; i < SOUND_RING_SEGMENTS; i++) {
            double a0 = (Math.PI * 2.0D * i) / SOUND_RING_SEGMENTS;
            double a1 = (Math.PI * 2.0D * (i + 1)) / SOUND_RING_SEGMENTS;
            float cos0 = (float) Math.cos(a0);
            float sin0 = (float) Math.sin(a0);
            float cos1 = (float) Math.cos(a1);
            float sin1 = (float) Math.sin(a1);
            buffer.vertex(matrix, x + cos0 * outerRadius, y, z + sin0 * outerRadius).color(red, green, blue, 0.0f).endVertex();
            buffer.vertex(matrix, x + cos1 * outerRadius, y, z + sin1 * outerRadius).color(red, green, blue, 0.0f).endVertex();
            buffer.vertex(matrix, x + cos1 * radius, y, z + sin1 * radius).color(red, green, blue, centerAlpha).endVertex();
            buffer.vertex(matrix, x + cos0 * radius, y, z + sin0 * radius).color(red, green, blue, centerAlpha).endVertex();

            buffer.vertex(matrix, x + cos0 * radius, y, z + sin0 * radius).color(red, green, blue, centerAlpha).endVertex();
            buffer.vertex(matrix, x + cos1 * radius, y, z + sin1 * radius).color(red, green, blue, centerAlpha).endVertex();
            buffer.vertex(matrix, x + cos1 * innerRadius, y, z + sin1 * innerRadius).color(red, green, blue, 0.0f).endVertex();
            buffer.vertex(matrix, x + cos0 * innerRadius, y, z + sin0 * innerRadius).color(red, green, blue, 0.0f).endVertex();
        }
    }

    private static void emitSoulParticle(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPos,
                                         Vector3f right, Vector3f up, SoulParticle particle,
                                         int rgb, int alpha, float size) {
        float red = ((rgb >>> 16) & 0xFF) / 255.0f;
        float green = ((rgb >>> 8) & 0xFF) / 255.0f;
        float blue = (rgb & 0xFF) / 255.0f;
        float centerAlpha = (alpha / 255.0f) * 0.72f;
        float edgeAlpha = 0.0f;
        float x = (float) (particle.x - cameraPos.x);
        float y = (float) (particle.y - cameraPos.y);
        float z = (float) (particle.z - cameraPos.z);
        float rx = right.x() * size;
        float ry = right.y() * size;
        float rz = right.z() * size;
        float ux = up.x() * size;
        float uy = up.y() * size;
        float uz = up.z() * size;

        buffer.vertex(matrix, x - rx - ux, y - ry - uy, z - rz - uz).color(red, green, blue, edgeAlpha).endVertex();
        buffer.vertex(matrix, x + rx - ux, y + ry - uy, z + rz - uz).color(red, green, blue, edgeAlpha).endVertex();
        buffer.vertex(matrix, x + rx + ux, y + ry + uy, z + rz + uz).color(red, green, blue, centerAlpha).endVertex();
        buffer.vertex(matrix, x - rx + ux, y - ry + uy, z - rz + uz).color(red, green, blue, centerAlpha).endVertex();

        float inner = size * 0.48f;
        rx = right.x() * inner;
        ry = right.y() * inner;
        rz = right.z() * inner;
        ux = up.x() * inner;
        uy = up.y() * inner;
        uz = up.z() * inner;
        float coreAlpha = (alpha / 255.0f) * 0.95f;
        buffer.vertex(matrix, x - rx - ux, y - ry - uy, z - rz - uz).color(red, green, blue, centerAlpha).endVertex();
        buffer.vertex(matrix, x + rx - ux, y + ry - uy, z + rz - uz).color(red, green, blue, centerAlpha).endVertex();
        buffer.vertex(matrix, x + rx + ux, y + ry + uy, z + rz + uz).color(red, green, blue, coreAlpha).endVertex();
        buffer.vertex(matrix, x - rx + ux, y - ry + uy, z - rz + uz).color(red, green, blue, coreAlpha).endVertex();
    }

    private static float easeOutCubic(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        float inv = 1.0f - clamped;
        return 1.0f - inv * inv * inv;
    }

    private static float fixedBillboardScaleRelative(Camera camera, WorldPoint anchor, Matrix4f projectionMatrix, float multiplier) {
        Vector3f look = camera.getLookVector();
        double depth = anchor.x() * look.x() + anchor.y() * look.y() + anchor.z() * look.z();
        if (depth <= 0.01) {
            depth = Math.sqrt(anchor.x() * anchor.x() + anchor.y() * anchor.y() + anchor.z() * anchor.z());
        }

        float projectionY = Math.abs(projectionMatrix.m11());
        int viewportH = Minecraft.getInstance().getWindow().getHeight();
        if (projectionY <= 0.0001f || viewportH <= 0) {
            return 0.025f * multiplier;
        }
        return (float) (2.0 * Math.max(0.25, depth) / (projectionY * viewportH)) * multiplier;
    }

    private record WorldPoint(double x, double y, double z) {
    }

    private static final class SoundMarker {
        private double x;
        private double y;
        private double z;
        private final String label;
        private int ticksLeft;

        private SoundMarker(double x, double y, double z, String label, int ticksLeft) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.label = label;
            this.ticksLeft = ticksLeft;
        }
    }

    private static final class SoulParticle {
        private double x;
        private double y;
        private double z;
        private double vx;
        private double vy;
        private double vz;
        private final float size;
        private final int lifetime;
        private final int seed;
        private int ticksLeft;
        private int age;

        private SoulParticle(double x, double y, double z, double vx, double vy, double vz, float size, int lifetime) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            this.size = size;
            this.lifetime = lifetime;
            this.ticksLeft = lifetime;
            this.seed = (int) ((Double.doubleToLongBits(x) * 31L
                    + Double.doubleToLongBits(y) * 17L
                    + Double.doubleToLongBits(z)) & 0x7FFFFFFFL);
        }
    }
}
