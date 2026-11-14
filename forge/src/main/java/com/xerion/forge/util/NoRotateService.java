package com.xerion.forge.util;

import com.xerion.common.config.NoRotateSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.entity.RelativeMovement;

import java.util.Set;

/**
 * Port of OpenOpal's NoRotate: teleport packets that would overwrite the local
 * player's rotation are applied by vanilla first, then the client's previous
 * rotation is restored.
 */
public final class NoRotateService {
    private static boolean armed;
    private static float storedYaw;
    private static float storedPitch;

    private NoRotateService() {
    }

    public static void prepare(ClientboundPlayerPositionPacket packet) {
        if (!NoRotateSettings.enabled() || packet == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft == null ? null : minecraft.player;
        if (player == null) {
            return;
        }
        if (NoRotateSettings.ignoreTeleports() && teleportDistanceSquared(packet, player) >= 100.0D) {
            return;
        }
        Set<RelativeMovement> relatives = packet.getRelativeArguments();
        boolean absoluteYaw = !relatives.contains(RelativeMovement.Y_ROT);
        boolean absolutePitch = !relatives.contains(RelativeMovement.X_ROT);
        if (!absoluteYaw && !absolutePitch) {
            return;
        }
        storedYaw = player.getYRot();
        storedPitch = player.getXRot();
        armed = true;
    }

    public static void restore() {
        if (!armed) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.setYRot(storedYaw);
            player.setXRot(storedPitch);
        }
        armed = false;
    }

    public static void clearRuntimeState() {
        armed = false;
        storedYaw = 0.0F;
        storedPitch = 0.0F;
    }

    private static double teleportDistanceSquared(ClientboundPlayerPositionPacket packet, LocalPlayer player) {
        Set<RelativeMovement> relatives = packet.getRelativeArguments();
        double x = relatives.contains(RelativeMovement.X)
                ? player.getX() + packet.getX()
                : packet.getX();
        double y = relatives.contains(RelativeMovement.Y)
                ? player.getY() + packet.getY()
                : packet.getY();
        double z = relatives.contains(RelativeMovement.Z)
                ? player.getZ() + packet.getZ()
                : packet.getZ();
        double dx = x - player.getX();
        double dy = y - player.getY();
        double dz = z - player.getZ();
        return dx * dx + dy * dy + dz * dz;
    }
}
