package com.xerion.forge.mixin;

import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Consumer;

@Mixin(CreateBuffetWorldScreen.class)
public interface CreateBuffetWorldScreenAccessor {
    @Accessor("f_95743_")
    Screen xerion$getParent();

    @Accessor("f_95744_")
    Consumer<Holder<Biome>> xerion$getApplySettings();

    @Accessor("f_95745_")
    Registry<Biome> xerion$getBiomes();

    @Accessor("f_95747_")
    Holder<Biome> xerion$getBiome();
}
