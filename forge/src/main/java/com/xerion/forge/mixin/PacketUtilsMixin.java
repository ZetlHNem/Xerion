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

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketUtils.class)
abstract class PacketUtilsMixin {
    /**
     * Actual client-thread packet dispatch. Blocking already happened on the
     * netty thread, so inbound blockage and the velocity buffer have to be
     * applied here, where cancelling really stops the vanilla handler body.
     */
    @Inject(method = "m_263899_(Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/protocol/Packet;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void xerion$captureInbound(PacketListener listener, Packet<?> packet, CallbackInfo ci) {
        FakeLagService.onInbound(packet);
        if (PacketBlockageService.isInboundBlocked(packet)) {
            ci.cancel();
            return;
        }
        if (VelocityModeService.wantsInbound(packet)) {
            VelocityModeService.handleInbound(packet, listener);
            ci.cancel();
        }
    }

    @Inject(method = "m_131363_(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void xerion$packetIntercept(Packet<?> packet, PacketListener listener,
                                               BlockableEventLoop<?> executor, CallbackInfo ci) {
        if (!AntiPacketKickService.enabled()) {
            return;
        }
        if (!executor.isSameThread()) {
            executor.executeIfPossible(() -> AntiPacketKickService.handle(packet, listener));
            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
        }
        AntiPacketKickService.handle(packet, listener);
        ci.cancel();
    }
}
