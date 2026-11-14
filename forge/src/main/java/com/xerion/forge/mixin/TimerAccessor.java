package com.xerion.forge.mixin;

import net.minecraft.client.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Timer.class)
public interface TimerAccessor {
    // f_92521_ is `private final float` in 1.20.1; @Mutable makes Mixin strip ACC_FINAL from the
    // target field when it generates this setter, otherwise the generated putfield throws
    // IllegalAccessError at runtime (writing a final field outside <init>).
    @Mutable
    @Accessor("f_92521_")
    void xerion$setMsPerTick(float value);

    @Accessor("f_92521_")
    float xerion$msPerTick();
}
