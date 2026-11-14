package com.xerion.forge.mixin;

import com.xerion.forge.gui.dashboard.XerionCreateWorldScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldSelectionList.class)
public abstract class WorldSelectionListMixin {
    @Redirect(
            method = "m_233213_",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;m_232896_(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/Screen;)V"),
            require = 0
    )
    private void xerion$openCreateWorldAsync(Minecraft minecraft, Screen lastScreen) {
        XerionCreateWorldScreen.openFresh(minecraft, lastScreen);
    }
}
