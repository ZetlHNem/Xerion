package com.xerion.forge.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LocalPlayer.class)
public interface LocalPlayerAccessor {
    // net.minecraft.client.player.LocalPlayer#positionReminder -> SRG f_108604_
    @Accessor("f_108604_")
    int xerion$positionReminder();

    @Accessor("f_108604_")
    void xerion$setPositionReminder(int value);
}
