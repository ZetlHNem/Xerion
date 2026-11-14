package com.xerion.forge.misc;

import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.packet.PacketBlockageService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.MovementInputUpdateEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Port of OpenOpal's Stuck module: freezes the server-side position while the
 * client keeps free view control, buffering pongs and item interactions until
 * a look packet has been sent.
 */
public final class StuckService {
    private static final long MAX_STUCK_MS = 3_500L;

    private static final Queue<Packet<?>> PACKET_QUEUE = new ConcurrentLinkedQueue<>();
    private static Packet<?> interactPacket;
    private static int interactStage;
    private static double frozenX;
    private static double frozenY;
    private static double frozenZ;
    private static long enableTime;
    private static boolean wasEnabled;

    private StuckService() {
    }

    public static boolean isEnabled() {
        return VisualSettings.stuckEnabled();
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        boolean enabled = isEnabled() && player != null && minecraft.getConnection() != null;
        if (enabled && !wasEnabled) {
            PACKET_QUEUE.clear();
            interactPacket = null;
            interactStage = 0;
            enableTime = System.currentTimeMillis();
            frozenX = player.getX();
            frozenY = player.getY();
            frozenZ = player.getZ();
        } else if (!enabled && wasEnabled) {
            flushOnDisable(player);
        }
        wasEnabled = enabled;
        if (!enabled) {
            return;
        }
        if (System.currentTimeMillis() - enableTime > MAX_STUCK_MS) {
            VisualSettings.setStuckEnabled(false);
            flushOnDisable(player);
            wasEnabled = false;
            return;
        }
        player.setDeltaMovement(0.0D, 0.0D, 0.0D);
    }

    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!isEnabled() || !(event.getEntity() instanceof LocalPlayer)) {
            return;
        }
        Input input = event.getInput();
        if (input == null) {
            return;
        }
        input.forwardImpulse = 0.0F;
        input.leftImpulse = 0.0F;
        input.jumping = false;
        input.shiftKeyDown = false;
    }

    public static boolean shouldCancelCorrection() {
        return isEnabled();
    }

    /**
     * @return true when the packet must be cancelled (queued or swallowed).
     */
    public static boolean captureOutbound(Packet<?> packet) {
        if (!isEnabled() || Minecraft.getInstance().player == null) {
            return false;
        }
        if (packet instanceof ServerboundMovePlayerPacket) {
            prepareMovement();
            if (packet instanceof ServerboundMovePlayerPacket.StatusOnly) {
                return true;
            }
            return false;
        }
        if (packet instanceof ServerboundPongPacket) {
            PACKET_QUEUE.add(packet);
            return true;
        }
        if (packet instanceof ServerboundUseItemPacket useItem) {
            LocalPlayer player = Minecraft.getInstance().player;
            ItemStack stack = useItem.getHand() == InteractionHand.OFF_HAND
                    ? player.getOffhandItem()
                    : player.getMainHandItem();
            if (stack.is(Items.ENDER_PEARL) || stack.isEdible() || stack.getItem() instanceof BowItem) {
                return false;
            }
            interactPacket = packet;
            interactStage = 1;
            return true;
        }
        if (packet instanceof ServerboundPlayerActionPacket action
                && action.getAction() == ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM
                && Minecraft.getInstance().player.getUseItem().getItem() instanceof BowItem) {
            interactPacket = packet;
            interactStage = 1;
            return true;
        }
        return false;
    }

    /**
     * @return a replacement packet with the frozen position, or null.
     */
    public static Packet<?> rewriteMovement(Packet<?> packet) {
        if (!isEnabled() || Minecraft.getInstance().player == null) {
            return null;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (packet instanceof ServerboundMovePlayerPacket.PosRot move) {
            return new ServerboundMovePlayerPacket.PosRot(frozenX, frozenY, frozenZ,
                    move.getYRot(player.getYRot()), move.getXRot(player.getXRot()), move.isOnGround());
        }
        if (packet instanceof ServerboundMovePlayerPacket.Pos move) {
            return new ServerboundMovePlayerPacket.Pos(frozenX, frozenY, frozenZ, move.isOnGround());
        }
        return null;
    }

    public static void sendRewritten(Packet<?> packet) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null || packet == null) {
            return;
        }
        PacketBlockageService.runDirectSend(() -> connection.send(packet));
    }

    public static void clearRuntimeState() {
        PACKET_QUEUE.clear();
        interactPacket = null;
        interactStage = 0;
        wasEnabled = false;
    }

    private static void prepareMovement() {
        if (interactStage != 1) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        interactStage = 2;
        sendDirect(new ServerboundMovePlayerPacket.Rot(
                player.getYRot(), player.getXRot(), player.onGround()));
        Packet<?> queued;
        while ((queued = PACKET_QUEUE.poll()) != null) {
            sendDirect(queued);
        }
        if (interactPacket != null) {
            sendDirect(interactPacket);
            interactPacket = null;
        }
    }

    private static void flushOnDisable(LocalPlayer player) {
        if (player != null) {
            sendDirect(new ServerboundMovePlayerPacket.PosRot(
                    player.getX(), player.getY(), player.getZ(),
                    player.getYRot(), player.getXRot(), player.onGround()));
        }
        Packet<?> queued;
        while ((queued = PACKET_QUEUE.poll()) != null) {
            sendDirect(queued);
        }
        interactPacket = null;
        interactStage = 0;
    }

    private static void sendDirect(Packet<?> packet) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null || packet == null) {
            return;
        }
        PacketBlockageService.runDirectSend(() -> connection.send(packet));
    }
}
