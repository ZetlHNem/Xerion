package com.xerion.forge.movement;

import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.misc.ServerDetector;
import com.xerion.forge.mixin.LocalPlayerAccessor;
import com.xerion.forge.packet.BlockHolder;
import com.xerion.forge.packet.PacketBlockageService;
import com.xerion.forge.simulation.PlayerSimulation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of OpenOpal's Watchdog NoFall: when the predicted fall distance would
 * cause fall damage, the outbound movement packet is spoofed as on-ground and
 * both connection directions are blocked until the movement packet is out.
 */
public final class NoFallWatchdogService {
    private static final BlockHolder OUTBOUND_HOLDER = new BlockHolder(PacketBlockageService.outbound());
    private static final BlockHolder INBOUND_HOLDER = new BlockHolder(PacketBlockageService.inbound());

    private static float fallDistanceBase;
    private static Vec3 prevMotion;
    private static Vec3 nextPos;
    private static boolean blocked;
    private static boolean rewriting;

    private NoFallWatchdogService() {
    }

    public static boolean isActive() {
        return VisualSettings.noFallEnabled()
                && "Watchdog".equals(VisualSettings.noFallMode())
                && Minecraft.getInstance().player != null
                && Minecraft.getInstance().level != null;
    }

    public static void tickPreMove() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!isActive() || player == null) {
            return;
        }
        if (player.fallDistance == 0.0F) {
            fallDistanceBase = player.fallDistance;
        }
        if (nextPos != null) {
            player.setPos(nextPos.x, nextPos.y, nextPos.z);
            nextPos = null;
            return;
        }
        if (ServerDetector.isHypixel() && ServerDetector.isHypixelLobby()) {
            return;
        }
        if (isGoingToFall(player)) {
            return;
        }
        double predicted = fallDifference(player) - (player.getDeltaMovement().y - 0.08D) * 0.98D;
        if (predicted >= maxFallDistance(player)) {
            prevMotion = player.getDeltaMovement();
        }
    }

    public static void tickPostMove() {
        if (!isActive()) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || prevMotion == null) {
            return;
        }
        Vec3 velocity = player.getDeltaMovement().scale(0.5D);
        AABB moved = player.getBoundingBox().move(velocity.x, velocity.y, velocity.z);
        if (isBoxEmpty(player, moved)) {
            nextPos = player.position().add(velocity);
        }
        player.setDeltaMovement(Vec3.ZERO);
    }

    /**
     * @return the movement packet rewritten with onGround=true, or null.
     */
    public static Packet<?> rewriteMovement(Packet<?> packet) {
        if (rewriting || !isActive() || prevMotion == null
                || !(packet instanceof ServerboundMovePlayerPacket move)) {
            return null;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }
        double x = move.getX(player.getX());
        double y = move.getY(player.getY());
        double z = move.getZ(player.getZ());
        double dx = x - player.xo;
        double dy = y - player.yo;
        double dz = z - player.zo;
        boolean moved = dx * dx + dy * dy + dz * dz > 4.0E-8D;

        Vec3 restore = prevMotion;
        prevMotion = null;
        player.setDeltaMovement(restore);
        if (moved) {
            return null;
        }

        int reminder = ((LocalPlayerAccessor) player).xerion$positionReminder();
        if (reminder >= 20) {
            ((LocalPlayerAccessor) player).xerion$setPositionReminder(18);
        }
        fallDistanceBase = player.fallDistance;
        block();
        return spoofGround(move, x, y, z);
    }

    public static void sendRewritten(Packet<?> packet) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null || packet == null) {
            return;
        }
        rewriting = true;
        try {
            PacketBlockageService.runDirectSend(() -> connection.send(packet));
        } finally {
            rewriting = false;
        }
    }

    public static void onMovementSent(Packet<?> packet) {
        if (!isActive() || Minecraft.getInstance().player == null
                || !(packet instanceof ServerboundMovePlayerPacket)) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (nextPos != null) {
            Vec3 target = nextPos;
            nextPos = player.position();
            player.setPos(target.x, target.y, target.z);
        }
        if (blocked) {
            blocked = false;
        } else {
            release();
        }
    }

    public static void clearRuntimeState() {
        release();
        blocked = false;
        nextPos = null;
        prevMotion = null;
        fallDistanceBase = 0.0F;
        rewriting = false;
    }

    private static void block() {
        OUTBOUND_HOLDER.block();
        INBOUND_HOLDER.block();
        blocked = true;
    }

    private static void release() {
        OUTBOUND_HOLDER.release();
        INBOUND_HOLDER.release();
    }

    private static Packet<?> spoofGround(ServerboundMovePlayerPacket move, double x, double y, double z) {
        LocalPlayer player = Minecraft.getInstance().player;
        float yaw = move.getYRot(player.getYRot());
        float pitch = move.getXRot(player.getXRot());
        if (move instanceof ServerboundMovePlayerPacket.PosRot) {
            return new ServerboundMovePlayerPacket.PosRot(x, y, z, yaw, pitch, true);
        }
        if (move instanceof ServerboundMovePlayerPacket.Pos) {
            return new ServerboundMovePlayerPacket.Pos(x, y, z, true);
        }
        if (move instanceof ServerboundMovePlayerPacket.Rot) {
            return new ServerboundMovePlayerPacket.Rot(yaw, pitch, true);
        }
        if (move instanceof ServerboundMovePlayerPacket.StatusOnly) {
            return new ServerboundMovePlayerPacket.StatusOnly(true);
        }
        return null;
    }

    private static boolean isGoingToFall(LocalPlayer player) {
        if (player.onGround()) {
            return true;
        }
        if (isOverVoid(player, player.getBoundingBox())) {
            PlayerSimulation simulation = new PlayerSimulation(player);
            if (!simulation.valid()) {
                return false;
            }
            for (int i = 0; i < 14; i++) {
                simulation.simulateTick();
                if (!isOverVoid(player, simulation.entity().getBoundingBox())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean isOverVoid(Entity entity, AABB box) {
        if (Minecraft.getInstance().level == null) {
            return false;
        }
        AABB column = new AABB(
                box.minX + 0.005D, 0.0D, box.minZ + 0.005D,
                box.maxX - 0.005D, box.maxY, box.maxZ - 0.005D);
        return !Minecraft.getInstance().level.getBlockCollisions(entity, column).iterator().hasNext();
    }

    private static boolean isBoxEmpty(Entity entity, AABB box) {
        return Minecraft.getInstance().level != null
                && !Minecraft.getInstance().level.getBlockCollisions(entity, box).iterator().hasNext();
    }

    private static double fallDifference(LocalPlayer player) {
        if (player.getAbilities().flying) {
            return 0.0D;
        }
        return player.fallDistance - fallDistanceBase;
    }

    private static float maxFallDistance(LocalPlayer player) {
        float distance = 3.0F;
        MobEffectInstance jumpBoost = player.getEffect(MobEffects.JUMP);
        if (jumpBoost != null) {
            distance += jumpBoost.getAmplifier() + 1;
        }
        return distance;
    }
}
