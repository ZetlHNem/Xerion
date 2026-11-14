package com.xerion.forge.combat;

import com.xerion.common.config.CombatSettings;
import com.xerion.forge.packet.PacketBlockageService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.MovementInputUpdateEvent;

/**
 * Port of OpenOpal's Criticals modes.
 * <ul>
 *     <li>Packet: sends two short downward move packets so the server marks the
 *     next hit as critical.</li>
 *     <li>Heypixel: auto-jumps when a Kill Aura target is close, cancels sprint
 *     while falling and skips the next movement packet.</li>
 * </ul>
 */
public final class CriticalsService {
    private static boolean silentSend;
    private static boolean skipMovementPacket;

    private CriticalsService() {
    }

    public static boolean silentSendActive() {
        return silentSend;
    }

    public static boolean consumeSkipMovement(Packet<?> packet) {
        if (!skipMovementPacket || !(packet instanceof ServerboundMovePlayerPacket)) {
            return false;
        }
        skipMovementPacket = false;
        return true;
    }

    public static void onMovementInput(MovementInputUpdateEvent event, LivingEntity target) {
        if (!CombatSettings.criticalsEnabled()
                || !CombatSettings.CRITICALS_MODE_HEYPIXEL.equals(CombatSettings.criticalsMode())
                || !CombatSettings.criticalsAutoJump()
                || !(event.getEntity() instanceof LocalPlayer player)
                || target == null
                || player.level() == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.keyJump.isDown()
                || !player.onGround()
                || CombatTargeting.distanceToBoxSqr(player, target) > 9.0D
                || hasHeadBlock(player)) {
            return;
        }
        if (event.getInput() != null) {
            event.getInput().jumping = true;
        }
    }

    public static void beforeAttack(LocalPlayer player, LivingEntity target) {
        if (!CombatSettings.criticalsEnabled() || player == null || target == null) {
            return;
        }
        if (CombatSettings.CRITICALS_MODE_HEYPIXEL.equals(CombatSettings.criticalsMode())) {
            heypixelBeforeAttack(player, target);
            return;
        }
        packetBeforeAttack(player);
    }

    public static void clearRuntimeState() {
        silentSend = false;
        skipMovementPacket = false;
    }

    private static void packetBeforeAttack(LocalPlayer player) {
        if (CombatSettings.criticalsGroundOnly() && !player.onGround()) {
            return;
        }
        if (!criticalEligible(player)) {
            return;
        }
        AABB raisedBox = player.getBoundingBox().move(0.0D, 0.0625D, 0.0D);
        if (player.level() == null || !player.level().noCollision(player, raisedBox)) {
            return;
        }
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        sendSilent(new ServerboundMovePlayerPacket.PosRot(x, y + 0.0625D, z, yaw, pitch, false));
        sendSilent(new ServerboundMovePlayerPacket.PosRot(x, y + 0.00125D, z, yaw, pitch, false));
    }

    private static void heypixelBeforeAttack(LocalPlayer player, LivingEntity target) {
        if (!criticalEligible(player) || hasHeadBlock(player)) {
            return;
        }
        if (player.getDeltaMovement().y >= 0.0D
                || player.onGround()
                || CombatTargeting.distanceToBoxSqr(player, target)
                > CombatSettings.criticalsRange() * CombatSettings.criticalsRange()) {
            return;
        }
        if (CombatSettings.criticalsSkipTicks()) {
            skipMovementPacket = true;
        }
        if (player.isSprinting()) {
            player.setSprinting(false);
        }
    }

    private static boolean criticalEligible(LocalPlayer player) {
        return !player.isInWater()
                && !player.onClimbable()
                && !player.isPassenger()
                && !player.hasEffect(MobEffects.BLINDNESS);
    }

    private static boolean hasHeadBlock(LocalPlayer player) {
        return player.level() != null
                && !player.level().noCollision(player, player.getBoundingBox().move(0.0D, 0.5D, 0.0D));
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
}
