package com.xerion.forge.player;

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
import com.xerion.common.player.FakePlayerRules;
import com.xerion.common.player.PlayerItemRules;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

public final class FakePlayerRegistry {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final double PLACE_REACH = 6.0D;
    private static final int FIRST_DUMMY_ENTITY_ID = -742001;
    private static final double KNOCKBACK_STRENGTH = 0.34D;
    private static final double RETURN_STRENGTH = 0.18D;
    private static final int VISUAL_KNOCKBACK_TICKS = 5;

    private static final Map<Integer, TrainingDummyPlayer> DUMMIES = new LinkedHashMap<>();
    private static final TreeSet<Integer> FREE_DUMMY_INDICES = new TreeSet<>();
    private static ClientLevel dummyLevel;
    private static int nextDummyIndex;

    private FakePlayerRegistry() {
    }

    public static void spawn() {
        place();
    }

    public static void place() {
        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (!VisualSettings.fakePlayerEnabled() || player == null || level == null) {
            return;
        }

        if (dummyLevel != level) {
            clear();
            dummyLevel = level;
        }

        int index = allocateDummyIndex();
        int entityId = FakePlayerRules.entityId(FIRST_DUMMY_ENTITY_ID, index);
        String baseName = VisualSettings.fakePlayerName();
        String name = FakePlayerRules.displayName(baseName, index);
        UUID uuid = UUID.nameUUIDFromBytes(FakePlayerRules.uuidSeed(entityId).getBytes(StandardCharsets.UTF_8));
        Vec3 position = placementPosition(level, player);
        TrainingDummyPlayer spawned = new TrainingDummyPlayer(level, new GameProfile(uuid, name),
                entityId,
                name,
                VisualSettings.fakePlayerHealth(),
                VisualSettings.fakePlayerHitFeedback(),
                VisualSettings.fakePlayerReturnHome());
        spawned.setId(entityId);
        spawned.setUUID(uuid);
        float yaw = FakePlayerRules.spawnYaw(VisualSettings.fakePlayerFacePlayer(), player.getYRot());
        spawned.absMoveTo(position.x, position.y, position.z, yaw, 0.0F);
        spawned.setYHeadRot(yaw);
        spawned.setHomePosition(position);
        spawned.resetHealth();
        spawned.setInvulnerable(false);

        level.addPlayer(entityId, spawned);
        DUMMIES.put(entityId, spawned);
        dummyLevel = level;
    }

    public static void remove() {
        clear();
    }

    public static void remove(int entityId) {
        TrainingDummyPlayer dummy = DUMMIES.remove(entityId);
        if (dummyLevel != null) {
            dummyLevel.removeEntity(entityId, RemovalReason.DISCARDED);
        } else if (dummy != null) {
            dummy.discard();
        }
        releaseDummyIndex(entityId);
    }

    public static void clear() {
        if (dummyLevel != null) {
            for (int entityId : new ArrayList<>(DUMMIES.keySet())) {
                dummyLevel.removeEntity(entityId, RemovalReason.DISCARDED);
            }
        } else {
            for (TrainingDummyPlayer dummy : DUMMIES.values()) {
                dummy.discard();
            }
        }
        DUMMIES.clear();
        FREE_DUMMY_INDICES.clear();
        nextDummyIndex = 0;
        dummyLevel = null;
    }

    public static boolean active() {
        return !DUMMIES.isEmpty() && dummyLevel == MC.level;
    }

    public static String status() {
        return FakePlayerRules.status(active(), DUMMIES.size());
    }

    public static boolean hurtIfDummy(Entity entity) {
        if (!(entity instanceof TrainingDummyPlayer dummy) || !isDummy(entity)) {
            return false;
        }
        dummy.receiveLocalHit(MC.player);
        return true;
    }

    public static boolean isDummy(Entity entity) {
        return entity != null
                && dummyLevel == MC.level
                && DUMMIES.get(entity.getId()) == entity;
    }

    public static LivingEntity dummyEntity() {
        if (!active()) {
            return null;
        }
        return DUMMIES.values().stream()
                .filter(dummy -> !dummy.isRemoved())
                .findFirst()
                .orElse(null);
    }

    public static List<LivingEntity> dummyEntities() {
        if (!active()) {
            return List.of();
        }
        return DUMMIES.values().stream()
                .filter(dummy -> !dummy.isRemoved())
                .map(dummy -> (LivingEntity) dummy)
                .toList();
    }

    public static List<DummySnapshot> snapshots() {
        LocalPlayer player = MC.player;
        if (!active() || player == null) {
            return List.of();
        }
        List<DummySnapshot> snapshots = new ArrayList<>();
        for (TrainingDummyPlayer dummy : DUMMIES.values()) {
            if (dummy.isRemoved()) {
                continue;
            }
            double distance = PlayerItemRules.distanceFromSqr(player.distanceToSqr(dummy));
            snapshots.add(new DummySnapshot(dummy.entityId, dummy.displayName, distance, dummy.getHealth(), dummy.configuredHealth));
        }
        return snapshots;
    }

    public static void tick() {
        if (!VisualSettings.fakePlayerEnabled()) {
            clear();
            return;
        }
        if (DUMMIES.isEmpty()) {
            return;
        }
        if (dummyLevel != MC.level) {
            clear();
            return;
        }
        Iterator<Map.Entry<Integer, TrainingDummyPlayer>> iterator = DUMMIES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, TrainingDummyPlayer> entry = iterator.next();
            TrainingDummyPlayer dummy = entry.getValue();
            if (dummy.isRemoved()) {
                releaseDummyIndex(entry.getKey());
                iterator.remove();
                continue;
            }
            dummy.resetHealth();
        }
        if (DUMMIES.isEmpty()) {
            dummyLevel = null;
        }
    }

    private static Vec3 placementPosition(ClientLevel level, LocalPlayer player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(PLACE_REACH));
        HitResult result = level.clip(new ClipContext(
                eye,
                end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));

        if (result instanceof BlockHitResult blockHit && result.getType() == HitResult.Type.BLOCK) {
            Direction direction = blockHit.getDirection();
            Vec3 center = Vec3.atCenterOf(blockHit.getBlockPos().relative(direction));
            if (direction == Direction.UP) {
                center = Vec3.atBottomCenterOf(blockHit.getBlockPos().above());
            }
            return center;
        }

        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        if (FakePlayerRules.needsFallbackLook(forward.lengthSqr())) {
            forward = Vec3.directionFromRotation(0.0F, player.getYRot());
            forward = new Vec3(forward.x, 0.0D, forward.z);
        }
        forward = forward.normalize();
        return player.position().add(forward.scale(2.5D));
    }

    private static int allocateDummyIndex() {
        if (!FREE_DUMMY_INDICES.isEmpty()) {
            return FREE_DUMMY_INDICES.pollFirst();
        }
        return nextDummyIndex++;
    }

    private static void releaseDummyIndex(int entityId) {
        int index = FIRST_DUMMY_ENTITY_ID - entityId;
        if (FakePlayerRules.releasableIndex(index, nextDummyIndex)) {
            FREE_DUMMY_INDICES.add(index);
        }
    }

    public record DummySnapshot(int entityId, String name, double distance, float health, float maxHealth) {
    }

    private static final class TrainingDummyPlayer extends RemotePlayer {
        private final int entityId;
        private final String displayName;
        private final float configuredHealth;
        private final boolean hitFeedback;
        private final boolean returnHome;
        private Vec3 homePosition = Vec3.ZERO;
        private int visualKnockbackTicks;
        private long lastFeedbackGameTime = Long.MIN_VALUE;

        private TrainingDummyPlayer(ClientLevel level, GameProfile profile, int entityId, String displayName,
                                    float configuredHealth, boolean hitFeedback, boolean returnHome) {
            super(level, profile);
            this.entityId = entityId;
            this.displayName = displayName;
            this.configuredHealth = PlayerItemRules.fakePlayerHealth(configuredHealth);
            this.hitFeedback = hitFeedback;
            this.returnHome = returnHome;
            if (getAttribute(Attributes.MAX_HEALTH) != null) {
                getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.configuredHealth);
            }
        }

        @Override
        public boolean hurt(DamageSource source, float amount) {
            receiveLocalHit(source.getEntity());
            return true;
        }

        @Override
        public boolean isInvulnerableTo(DamageSource source) {
            return false;
        }

        @Override
        public void tick() {
            super.tick();
            tickVisualMotion();
            resetHealth();
        }

        private void setHomePosition(Vec3 position) {
            homePosition = position;
        }

        private void receiveLocalHit(Entity attacker) {
            long gameTime = level().getGameTime();
            if (gameTime == lastFeedbackGameTime) {
                resetHealth();
                return;
            }
            lastFeedbackGameTime = gameTime;
            hurtTime = 10;
            hurtDuration = 10;
            deathTime = 0;
            invulnerableTime = 0;
            hurtMarked = true;
            animateHurt(hurtDirection(attacker));
            applyVisualKnockback(attacker);
            if (hitFeedback) {
                playHitFeedback();
            }
            resetHealth();
        }

        private float hurtDirection(Entity attacker) {
            if (attacker == null) {
                return getYRot();
            }
            return PlayerItemRules.hurtDirection(attacker.getX(), attacker.getZ(), getX(), getZ());
        }

        private void applyVisualKnockback(Entity attacker) {
            if (attacker == null) {
                return;
            }
            Vec3 away = position().subtract(attacker.position());
            away = new Vec3(away.x, 0.0D, away.z);
            if (away.lengthSqr() < 1.0E-4D) {
                away = Vec3.directionFromRotation(0.0F, attacker.getYRot());
            }
            away = away.normalize();
            setDeltaMovement(away.x * KNOCKBACK_STRENGTH, 0.08D, away.z * KNOCKBACK_STRENGTH);
            visualKnockbackTicks = VISUAL_KNOCKBACK_TICKS;
            hurtMarked = true;
        }

        private void tickVisualMotion() {
            if (visualKnockbackTicks > 0) {
                visualKnockbackTicks--;
                return;
            }
            if (!returnHome) {
                setDeltaMovement(Vec3.ZERO);
                return;
            }
            Vec3 toHome = homePosition.subtract(position());
            double distanceSq = toHome.lengthSqr();
            if (FakePlayerRules.returnHomeArrived(distanceSq)) {
                setDeltaMovement(Vec3.ZERO);
                absMoveTo(homePosition.x, homePosition.y, homePosition.z, getYRot(), getXRot());
                return;
            }
            Vec3 returnMotion = toHome.scale(RETURN_STRENGTH);
            setDeltaMovement(returnMotion.x, returnMotion.y, returnMotion.z);
            hurtMarked = true;
        }

        private void playHitFeedback() {
            if (!(level() instanceof ClientLevel level)) {
                return;
            }
            level.playLocalSound(
                    getX(),
                    getY(),
                    getZ(),
                    SoundEvents.PLAYER_HURT,
                    SoundSource.PLAYERS,
                    0.85F,
                    0.92F + random.nextFloat() * 0.16F,
                    false
            );
            for (int i = 0; i < 4; i++) {
                level.addParticle(
                        ParticleTypes.DAMAGE_INDICATOR,
                        getRandomX(0.55D),
                        getY(0.65D + random.nextDouble() * 0.35D),
                        getRandomZ(0.55D),
                        0.0D,
                        0.08D + random.nextDouble() * 0.08D,
                        0.0D
                );
            }
        }

        private void resetHealth() {
            if (getAttribute(Attributes.MAX_HEALTH) != null
                    && PlayerItemRules.healthAttributeChanged(getMaxHealth(), configuredHealth)) {
                getAttribute(Attributes.MAX_HEALTH).setBaseValue(configuredHealth);
            }
            setHealth(PlayerItemRules.clampedHealth(configuredHealth, getMaxHealth()));
        }
    }
}
