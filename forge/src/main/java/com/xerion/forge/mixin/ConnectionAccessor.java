package com.xerion.forge.mixin;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Connection.class)
public interface ConnectionAccessor {
    // net.minecraft.network.Connection#channel -> SRG f_129468_ (production runtime uses SRG names)
    @Accessor("f_129468_")
    Channel xerion$channel();
}
