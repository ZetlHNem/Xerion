package com.xerion.forge.mixin;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.world.level.WorldDataConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CreateWorldScreen.class)
public interface CreateWorldScreenInvoker {
    @Invoker("m_100972_")
    void xerion$createWorld();

    @Invoker("m_269545_")
    void xerion$openExperiments(WorldDataConfiguration configuration);

    @Invoker("m_267734_")
    void xerion$openDataPacks(WorldDataConfiguration configuration);
}
