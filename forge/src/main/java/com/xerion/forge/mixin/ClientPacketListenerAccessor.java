package com.xerion.forge.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPacketListener.class)
public interface ClientPacketListenerAccessor {
    // net.minecraft.client.multiplayer.ClientPacketListener#connection -> SRG f_104885_
    @Accessor("f_104885_")
    Connection xerion$connection();
}
