package com.xerion.forge.mixin;

import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldSelectionList.WorldListEntry.class)
public interface WorldListEntryAccessor {
    @Accessor("f_101695_")
    LevelSummary xerion$getSummary();

    @Accessor("f_101698_")
    FaviconTexture xerion$getIcon();
}
