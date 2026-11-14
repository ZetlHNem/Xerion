package com.xerion.forge.combat;

import com.xerion.common.config.CombatSettings;
import com.xerion.forge.packet.PacketBlockageService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Server-specific Anti Knockback modes ported from OpenOpal:
 * <ul>
 *     <li>CubeCraft: cancels the knockback packet that follows a damage event,
 *     then replays fake movement / block action packets.</li>
 *     <li>Heypixel3: buffers the knockback and every inbound packet for a few
 *     ticks, then flushes them in order.</li>
 * </ul>
 */
public final class VelocityModeService {
    private static final Queue<Packet<?>> DELAYED_PACKETS = new ArrayDeque<>();

    private static boolean silentSend;
    private static String lastMode = "";

    // CubeCraft state.
    private static boolean cubeCraftCanCancel;
    private static int cubeCraftBypassTicks;

    // Heypixel3 buffer state.
    private static int bufferDelayTicks;
    private static int bufferAttackCount = 1;
    private static boolean bufferShouldFlush;
    private static Vec3 pendingVelocity;
    private static int hurtWindowTicks;

    private VelocityModeService() {
    }

    public static boolean wantsInbound(Packet<?> packet) {
        if (packet == null || !CombatSettings.velocityEnabled()) {
            return false;
        }
        String mode = CombatSettings.velocityMode();
        if (CombatSettings.VELOCITY_MODE_HEYPIXEL3.equals(mode)) {
            // The buffer has to intercept every inbound packet while it waits.
            return true;
        }
        if (CombatSettings.VELOCITY_MODE_CUBECRAFT.equals(mode)) {
            return packet instanceof ClientboundDamageEventPacket
                    || packet instanceof ClientboundSetEntityMotionPacket
                    || packet instanceof ClientboundExplodePacket;
        }
        return false;
    }

    public static void handleInbound(Packet<?> packet, PacketListener listener) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null || minecraft.level == null) {
            handleVanilla(packet, listener);
            return;
        }
        String mode = CombatSettings.velocityMode();
        boolean consumed = CombatSettings.VELOCITY_MODE_HEYPIXEL3.equals(mode)
                ? handleHeypixel3(packet)
                : handleCubeCraft(packet);
        if (!consumed) {
            handleVanilla(packet, listener);
        }
    }

    public static boolean silentSendActive() {
        return silentSend;
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft == null ? null : minecraft.player;
        String mode = CombatSettings.velocityMode();
        if (!CombatSettings.velocityEnabled() || player == null
                || (!CombatSettings.VELOCITY_MODE_CUBECRAFT.equals(mode)
                && !CombatSettings.VELOCITY_MODE_HEYPIXEL3.equals(mode))) {
            if (!mode.equals(lastMode) || (!CombatSettings.velocityEnabled() && !lastMode.isEmpty())) {
                clearRuntimeState();
            }
            lastMode = mode;
            return;
        }
        if (!mode.equals(lastMode)) {
            clearRuntimeState();
            lastMode = mode;
        }

        if (CombatSettings.VELOCITY_MODE_CUBECRAFT.equals(mode)) {
            tickCubeCraft(player);
        } else {
            tickHeypixel3(player);
        }
    }

    public static void clearRuntimeState() {
        DELAYED_PACKETS.clear();
        cubeCraftCanCancel = false;
        cubeCraftBypassTicks = 0;
        bufferDelayTicks = 0;
        bufferAttackCount = 1;
        bufferShouldFlush = false;
        pendingVelocity = null;
        hurtWindowTicks = 0;
        silentSend = false;
    }

    // ------------------------------------------------------------------
    // CubeCraft
    // ------------------------------------------------------------------

    private static boolean handleCubeCraft(Packet<?> packet) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (packet instanceof ClientboundDamageEventPacket damage
                && damage.entityId() == player.getId()) {
            cubeCraftCanCancel = true;
            return false;
        }
        if (packet instanceof ClientboundSetEntityMotionPacket motion
                && motion.getId() == player.getId()
                && cubeCraftCanCancel) {
            cubeCraftCanCancel = false;
            cubeCraftBypassTicks = 1;
            return true;
        }
        if (packet instanceof ClientboundExplodePacket && cubeCraftCanCancel) {
            cubeCraftCanCancel = false;
            cubeCraftBypassTicks = 1;
            return true;
        }
        return false;
    }

    private static void tickCubeCraft(LocalPlayer player) {
        if (cubeCraftBypassTicks > 0 && --cubeCraftBypassTicks == 0) {
            sendCubeCraftBypassPackets(player);
        }
    }

    private static void sendCubeCraftBypassPackets(LocalPlayer player) {
        int repeats = CombatSettings.velocityAlternativeBypass() ? 4 : 1;
        for (int i = 0; i < repeats; i++) {
            sendSilent(new ServerboundMovePlayerPacket.PosRot(
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    player.getYRot(),
                    player.getXRot(),
                    player.onGround()));
        }
        BlockPos position = player.blockPosition();
        Direction facing = player.getDirection().getOpposite();
        sendSilent(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                position,
                facing));
    }

    // ------------------------------------------------------------------
    // Heypixel3 (buffer)
    // ------------------------------------------------------------------

    private static boolean handleHeypixel3(Packet<?> packet) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (packet instanceof ClientboundDamageEventPacket damage
                && damage.entityId() == player.getId()) {
            hurtWindowTicks = 3;
            return false;
        }

        if (packet instanceof ClientboundSetEntityMotionPacket motion
                && motion.getId() == player.getId()) {
            if (hurtWindowTicks <= 0) {
                return false;
            }
            if (bufferDelayTicks > 0 || bufferShouldFlush) {
                if (bufferDelayTicks > 0 && canTriggerAttackCountNow(player)) {
                    bufferAttackCount = 1;
                    bufferAttackCount--;
                    bufferDelayTicks = 0;
                    pendingVelocity = null;
                    bufferShouldFlush = true;
                }
                hurtWindowTicks = 0;
                return true;
            }
            if (bufferAttackCount > 0 && canTriggerAttackCountNow(player)) {
                bufferAttackCount--;
                hurtWindowTicks = 0;
                return true;
            }
            bufferDelayTicks = CombatSettings.velocityBufferDelayTicks();
            bufferShouldFlush = false;
            DELAYED_PACKETS.clear();
            pendingVelocity = velocityOf(motion);
            hurtWindowTicks = 0;
            return true;
        }

        if (bufferDelayTicks > 0) {
            if (packet instanceof ClientboundPlayerPositionPacket) {
                bufferDelayTicks = 0;
                bufferShouldFlush = true;
                return false;
            }
            DELAYED_PACKETS.add(packet);
            return true;
        }
        return false;
    }

    private static void tickHeypixel3(LocalPlayer player) {
        if (bufferDelayTicks > 0) {
            bufferDelayTicks--;
            if (bufferDelayTicks == 0) {
                bufferShouldFlush = true;
            }
        }
        if (hurtWindowTicks > 0) {
            hurtWindowTicks--;
        }
        if (bufferShouldFlush) {
            flushDelayedPackets(player);
            bufferShouldFlush = false;
        }
    }

    private static void flushDelayedPackets(LocalPlayer player) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            DELAYED_PACKETS.clear();
            bufferDelayTicks = 0;
            bufferShouldFlush = false;
            pendingVelocity = null;
            hurtWindowTicks = 0;
            return;
        }
        if (pendingVelocity != null) {
            player.setDeltaMovement(pendingVelocity);
            player.hurtMarked = true;
            pendingVelocity = null;
        }
        Packet<?> queued;
        while ((queued = DELAYED_PACKETS.poll()) != null) {
            handleVanilla(queued, connection);
        }
        bufferDelayTicks = 0;
    }

    private static boolean canTriggerAttackCountNow(LocalPlayer player) {
        return player.isSprinting()
                && !player.isShiftKeyDown()
                && player.input != null
                && player.input.forwardImpulse > 0.0F
                && !player.isUsingItem();
    }

    private static Vec3 velocityOf(ClientboundSetEntityMotionPacket motion) {
        return new Vec3(motion.getXa() / 8000.0D, motion.getYa() / 8000.0D, motion.getZa() / 8000.0D);
    }

    private static void sendSilent(Packet<?> packet) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return;
        }
        silentSend = true;
        try {
            PacketBlockageService.runDirectSend(() -> connection.send(packet));
        } finally {
            silentSend = false;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void handleVanilla(Packet<?> packet, PacketListener listener) {
        ((Packet) packet).handle(listener);
    }
}
