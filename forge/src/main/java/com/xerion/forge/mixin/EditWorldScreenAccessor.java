package com.xerion.forge.mixin;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EditWorldScreen.class)
public interface EditWorldScreenAccessor {
    @Accessor("f_101247_")
    BooleanConsumer xerion$getCallback();

    @Accessor("f_101249_")
    LevelStorageSource.LevelStorageAccess xerion$getLevelAccess();
}
