package com.xerion.forge.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {
    @Accessor("f_105195_")
    void xerion$setDestroyDelay(int value);

    @Accessor("f_105195_")
    int xerion$destroyDelay();
}
