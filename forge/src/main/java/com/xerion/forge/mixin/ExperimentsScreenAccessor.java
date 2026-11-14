package com.xerion.forge.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.ExperimentsScreen;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Consumer;

@Mixin(ExperimentsScreen.class)
public interface ExperimentsScreenAccessor {
    @Accessor("f_268615_")
    Screen xerion$getParent();

    @Accessor("f_268626_")
    PackRepository xerion$getPackRepository();

    @Accessor("f_268670_")
    Consumer<PackRepository> xerion$getOutput();
}
