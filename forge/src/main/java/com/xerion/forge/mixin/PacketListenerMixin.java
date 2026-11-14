package com.xerion.forge.mixin;

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

import com.xerion.common.config.CombatSettings;
import com.xerion.forge.combat.VelocityPacketState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
abstract class PacketListenerMixin {
    @Inject(method = "m_8048_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$scaleLocalPlayerVelocity(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
        if (!CombatSettings.velocityEnabled()) {
            return;
        }
        if (!CombatSettings.VELOCITY_MODE_SCALE.equals(CombatSettings.velocityMode())) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || packet.getId() != player.getId()) {
            return;
        }

        double horizontal = Mth.clamp(CombatSettings.velocityHorizontal(), 0.0f, 1.0f);
        double vertical = Mth.clamp(CombatSettings.velocityVertical(), 0.0f, 1.0f);
        if (horizontal >= 0.999D && vertical >= 0.999D) {
            return;
        }

        Vec3 current = player.getDeltaMovement();
        double x = packet.getXa() / 8000.0D;
        double y = packet.getYa() / 8000.0D;
        double z = packet.getZa() / 8000.0D;
        player.setDeltaMovement(
                horizontal <= 0.0001D ? current.x : x * horizontal,
                vertical <= 0.0001D ? current.y : y * vertical,
                horizontal <= 0.0001D ? current.z : z * horizontal
        );
        player.hurtMarked = true;
        VelocityPacketState.markHandled(player.tickCount);
        ci.cancel();
    }

    @Inject(method = "m_5682_(Lnet/minecraft/network/protocol/game/ClientboundPlayerPositionPacket;)V",
            at = @At("HEAD"), require = 0)
    private void xerion$trackServerCorrection(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        BypassProtocolService.onServerCorrection(packet);
        DisablerService.onServerCorrection(packet);
        NoRotateService.prepare(packet);
        SpeedModeService.onServerCorrection();
    }

    @Inject(method = "m_5682_(Lnet/minecraft/network/protocol/game/ClientboundPlayerPositionPacket;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$cancelStuckCorrection(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        if (StuckService.shouldCancelCorrection()) {
            ci.cancel();
        }
    }

    @Inject(method = "m_5682_(Lnet/minecraft/network/protocol/game/ClientboundPlayerPositionPacket;)V",
            at = @At("TAIL"), require = 0)
    private void xerion$restoreServerCorrectionRotation(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        NoRotateService.restore();
    }

    @Inject(method = "m_5980_", at = @At("HEAD"), require = 0)
    private void xerion$trackDisablerInventoryOpen(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
        DisablerService.onInventoryOpened();
    }
}
