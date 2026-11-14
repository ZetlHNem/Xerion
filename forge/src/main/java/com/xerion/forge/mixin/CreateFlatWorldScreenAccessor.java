package com.xerion.forge.mixin;

import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Consumer;

@Mixin(CreateFlatWorldScreen.class)
public interface CreateFlatWorldScreenAccessor {
    @Accessor("f_95814_")
    CreateWorldScreen xerion$getParent();

    @Accessor("f_95815_")
    Consumer<FlatLevelGeneratorSettings> xerion$getApplySettings();
}
