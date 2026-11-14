package com.xerion.forge.combat;

import com.xerion.common.config.CombatSettings;
import com.xerion.forge.packet.PacketBlockageService;
import com.xerion.forge.runtime.SocialProfileStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Port of OpenOpal's FakeLag: while an enemy is nearby, outgoing packets are
 * queued for a short delay and released on a timer or on any latency-critical
 * event (correction, damage, knockback, interaction, ...).
 */
public final class FakeLagService {
    private static final Logger LOGGER = LoggerFactory.getLogger("xerion-fakelag");
    private static final int MAX_QUEUED_MOVEMENT_PACKETS = 6;

    private record QueuedPacket(Packet<?> packet, long timestamp, Vec3 position) {
    }

    private static final Queue<QueuedPacket> QUEUE = new ConcurrentLinkedQueue<>();

    private static long lastFlush;
    private static long nextDelay;
    private static long lastDebugLog;
    private static boolean flushing;
    private static boolean enemyNearby;
    private static boolean wasEnabled;

    private FakeLagService() {
    }

    public static boolean isEnabled() {
        return CombatSettings.fakeLagEnabled();
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        boolean enabled = isEnabled() && player != null && minecraft.level != null;
        if (enabled && !wasEnabled) {
            QUEUE.clear();
            lastFlush = 0L;
            nextDelay = randomDelay();
            enemyNearby = false;
        } else if (!enabled && wasEnabled) {
            flush("disable");
        }
        wasEnabled = enabled;
        if (!enabled) {
            return;
        }
        enemyNearby = findEnemy(player, CombatSettings.fakeLagRange()) != null;
        QueuedPacket head = QUEUE.peek();
        if (head != null && System.currentTimeMillis() - head.timestamp() >= nextDelay) {
            flush("delay");
            nextDelay = randomDelay();
        } else if (head != null) {
            debugHeartbeat();
        }
    }

    /**
     * @return true when the packet must be cancelled (queued).
     */
    public static boolean captureOutbound(Packet<?> packet) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (flushing || !isEnabled() || player == null || minecraft.level == null
                || player.isDeadOrDying() || player.isInWater() || minecraft.screen != null) {
            return false;
        }
        if (isLatencyCritical(packet)) {
            flushIfQueued("latency critical");
            return false;
        }
        String flushReason = sendFlushReason(packet);
        if (flushReason != null) {
            flushIfQueued(flushReason);
            return false;
        }
        if (System.currentTimeMillis() - lastFlush < CombatSettings.fakeLagRecoilMs()) {
            return false;
        }
        if (player.isUsingItem() && isConsumable(player.getUseItem())) {
            return false;
        }
        if ("Dynamic".equals(CombatSettings.fakeLagMode()) && !shouldLagDynamically(player, minecraft.level)) {
            flushIfQueued("dynamic stop");
            return false;
        }
        if (packet instanceof ServerboundMovePlayerPacket move && shouldFlushBeforeQueueMove(move)) {
            flushIfQueued("movement cap");
            return false;
        }
        boolean wasEmpty = QUEUE.isEmpty();
        QUEUE.add(new QueuedPacket(packet, System.currentTimeMillis(), movePosition(packet, player)));
        if (wasEmpty) {
            debug("queue start delay=" + nextDelay + "ms mode=" + CombatSettings.fakeLagMode());
        } else {
            debugHeartbeat();
        }
        return true;
    }

    public static void onInbound(Packet<?> packet) {
        if (flushing || !isEnabled() || Minecraft.getInstance().player == null) {
            return;
        }
        String flushReason = null;
        if (packet instanceof ClientboundPlayerPositionPacket) {
            flushReason = "position correction";
        } else if (packet instanceof ClientboundSetHealthPacket) {
            flushReason = "health update";
        } else if (packet instanceof ClientboundSetEntityMotionPacket motion
                && motion.getId() == Minecraft.getInstance().player.getId()
                && (motion.getXa() != 0 || motion.getYa() != 0 || motion.getZa() != 0)) {
            flushReason = "velocity";
        } else if (packet instanceof ClientboundExplodePacket explosion
                && (explosion.getKnockbackX() != 0.0F
                || explosion.getKnockbackY() != 0.0F
                || explosion.getKnockbackZ() != 0.0F)) {
            flushReason = "explosion";
        }
        if (flushReason != null) {
            flushIfQueued(flushReason);
        }
    }

    public static void clearRuntimeState() {
        while (!QUEUE.isEmpty()) {
            QUEUE.poll();
        }
        flushing = false;
        enemyNearby = false;
        wasEnabled = false;
        lastFlush = 0L;
        nextDelay = 0L;
        lastDebugLog = 0L;
    }

    private static boolean isLatencyCritical(Packet<?> packet) {
        return packet instanceof ServerboundPongPacket
                || packet instanceof ServerboundKeepAlivePacket
                || packet instanceof ServerboundContainerClickPacket
                || packet instanceof ServerboundContainerClosePacket;
    }

    private static String sendFlushReason(Packet<?> packet) {
        if (CombatSettings.fakeLagFlushEntity()
                && (packet instanceof ServerboundInteractPacket || packet instanceof ServerboundSwingPacket)) {
            return "entity interact";
        }
        if (CombatSettings.fakeLagFlushBlock()
                && (packet instanceof ServerboundUseItemOnPacket || packet instanceof ServerboundSignUpdatePacket)) {
            return "block interact";
        }
        if (CombatSettings.fakeLagFlushAction() && packet instanceof ServerboundPlayerActionPacket) {
            return "action";
        }
        return null;
    }

    private static boolean shouldLagDynamically(LocalPlayer player, ClientLevel level) {
        if (!enemyNearby) {
            return false;
        }
        double maxRange = CombatSettings.fakeLagRange();
        Entity nearest = findEnemy(player, maxRange);
        if (nearest == null || player.distanceTo(nearest) < 2.25D) {
            return false;
        }
        Vec3 serverPosition = firstQueuedPosition();
        if (serverPosition == null) {
            return true;
        }
        Vec3 clientPosition = player.position();
        AABB playerBox = player.getBoundingBox().move(serverPosition.subtract(clientPosition));
        boolean intersects = false;
        double serverDistance = Double.MAX_VALUE;
        double clientDistance = Double.MAX_VALUE;
        for (Entity entity : level.entitiesForRendering()) {
            if (!isValidEnemy(player, entity)) {
                continue;
            }
            Vec3 entityPosition = entity.position();
            double toServer = entityPosition.distanceTo(serverPosition);
            double toClient = entityPosition.distanceTo(clientPosition);
            if (toServer > maxRange) {
                continue;
            }
            intersects |= entity.getBoundingBox().intersects(playerBox);
            serverDistance = Math.min(serverDistance, toServer);
            clientDistance = Math.min(clientDistance, toClient);
        }
        if (serverDistance == Double.MAX_VALUE) {
            return false;
        }
        return serverDistance >= clientDistance && !intersects;
    }

    private static boolean shouldFlushBeforeQueueMove(ServerboundMovePlayerPacket move) {
        if (QUEUE.isEmpty() || !move.hasPosition()) {
            return false;
        }
        Vec3 first = firstQueuedPosition();
        LocalPlayer player = Minecraft.getInstance().player;
        if (first != null && player != null) {
            Vec3 current = new Vec3(move.getX(player.getX()), move.getY(player.getY()), move.getZ(player.getZ()));
            if (first.distanceTo(current) > 0.6D) {
                return true;
            }
        }
        int movementPackets = 0;
        for (QueuedPacket queued : QUEUE) {
            if (queued.packet() instanceof ServerboundMovePlayerPacket) {
                movementPackets++;
            }
        }
        return movementPackets >= MAX_QUEUED_MOVEMENT_PACKETS;
    }

    private static Vec3 movePosition(Packet<?> packet, LocalPlayer player) {
        if (!(packet instanceof ServerboundMovePlayerPacket move) || !move.hasPosition()) {
            return null;
        }
        return new Vec3(move.getX(player.getX()), move.getY(player.getY()), move.getZ(player.getZ()));
    }

    private static Vec3 firstQueuedPosition() {
        for (QueuedPacket queued : QUEUE) {
            if (queued.position() != null) {
                return queued.position();
            }
        }
        return null;
    }

    private static Entity findEnemy(LocalPlayer player, double range) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        Entity nearest = null;
        double best = range;
        for (Entity entity : level.entitiesForRendering()) {
            if (!isValidEnemy(player, entity)) {
                continue;
            }
            double distance = player.distanceTo(entity);
            if (distance < best) {
                best = distance;
                nearest = entity;
            }
        }
        return nearest;
    }

    private static boolean isValidEnemy(LocalPlayer player, Entity entity) {
        if (entity == player || !(entity instanceof LivingEntity living) || entity.isRemoved()
                || living.getHealth() <= 0.0F) {
            return false;
        }
        if (entity instanceof Player other) {
            if (AntiBotService.isBot(player, other) || SocialProfileStore.isProtected(player, other)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isConsumable(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        UseAnim animation = stack.getUseAnimation();
        return animation == UseAnim.EAT || animation == UseAnim.DRINK || stack.isEdible();
    }

    private static long randomDelay() {
        int base = CombatSettings.fakeLagDelayMs();
        return Math.max(0L, base + (long) ((Math.random() - 0.5D) * base * 0.4D));
    }

    private static void flushIfQueued(String reason) {
        if (!QUEUE.isEmpty()) {
            flush(reason);
            lastFlush = System.currentTimeMillis();
        }
    }

    private static void flush(String reason) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            QUEUE.clear();
            return;
        }
        int queued = QUEUE.size();
        flushing = true;
        try {
            QueuedPacket queuedPacket;
            while ((queuedPacket = QUEUE.poll()) != null) {
                Packet<?> packet = queuedPacket.packet();
                PacketBlockageService.runDirectSend(() -> connection.send(packet));
            }
        } finally {
            flushing = false;
        }
        if (queued > 0) {
            debug("flush reason=" + reason + " packets=" + queued);
        }
    }

    private static void debugHeartbeat() {
        if (!CombatSettings.fakeLagDebug()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastDebugLog < 1000L) {
            return;
        }
        QueuedPacket head = QUEUE.peek();
        long oldest = head == null ? 0L : Math.max(0L, now - head.timestamp());
        debug("queued=" + QUEUE.size() + " oldest=" + oldest + "ms target=" + nextDelay
                + "ms enemy=" + enemyNearby);
    }

    private static void debug(String message) {
        if (!CombatSettings.fakeLagDebug()) {
            return;
        }
        lastDebugLog = System.currentTimeMillis();
        LOGGER.info("[FakeLag] {}", message);
    }
}
