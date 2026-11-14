package com.xerion.forge.mixin;

import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
public interface ServerListOnlineEntryAccessor {
    @Accessor("f_99860_")
    FaviconTexture xerion$getIcon();
}
