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
import com.xerion.forge.packet.PacketBlockageService;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
abstract class ConnectionMixin {
    @Inject(method = "m_129512_(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$captureBlinkPacket(Packet<?> packet, CallbackInfo ci) {
        if (StuckService.captureOutbound(packet)) {
            ci.cancel();
            return;
        }
        Packet<?> stuckPacket = StuckService.rewriteMovement(packet);
        if (stuckPacket != null) {
            ci.cancel();
            StuckService.sendRewritten(stuckPacket);
            return;
        }
        Packet<?> noFallPacket = NoFallWatchdogService.rewriteMovement(packet);
        if (noFallPacket != null) {
            ci.cancel();
            NoFallWatchdogService.sendRewritten(noFallPacket);
            return;
        }
        if (FakeLagService.captureOutbound(packet)) {
            ci.cancel();
            return;
        }
        if (CriticalsService.consumeSkipMovement(packet)) {
            ci.cancel();
            return;
        }
        if (BlinkService.capture(packet)) {
            ci.cancel();
            return;
        }
        if (DisablerService.captureOutbound(packet)) {
            ci.cancel();
            return;
        }
        if (PingSpoofService.captureOutbound(packet)) {
            ci.cancel();
            return;
        }
        Packet<?> silenceTellyPacket = ScaffoldSilenceTellyService.rewriteMovement(packet);
        if (silenceTellyPacket != null) {
            ci.cancel();
            ScaffoldSilenceTellyService.sendRewritten(silenceTellyPacket);
            return;
        }
        if (BypassProtocolService.captureOutbound(packet)) {
            ci.cancel();
            return;
        }
        if (!PacketBlockageService.isDirectSendActive() && PacketBlockageService.isOutboundBlocked(packet)) {
            ci.cancel();
            return;
        }
        DisablerService.maybeSendMiniBloxPayload(packet);
    }

    @Inject(method = "m_129512_(Lnet/minecraft/network/protocol/Packet;)V", at = @At("TAIL"), require = 0)
    private void xerion$afterMovementSent(Packet<?> packet, CallbackInfo ci) {
        ScaffoldSilenceTellyService.onMovementSent(packet);
        NoFallWatchdogService.onMovementSent(packet);
    }
}
