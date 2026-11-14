package com.xerion.forge.mixin;

import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;

@Mixin(PackSelectionScreen.class)
public interface PackSelectionScreenAccessor {
    @Accessor("f_99973_")
    PackSelectionModel xerion$getModel();

    @Accessor("f_99979_")
    Path xerion$getPackDirectory();
}
