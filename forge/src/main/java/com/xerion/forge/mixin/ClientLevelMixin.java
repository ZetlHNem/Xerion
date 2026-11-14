package com.xerion.forge.mixin;

import com.xerion.forge.misc.SkipTickService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientLevel.class)
abstract class ClientLevelMixin {
    @Redirect(method = "m_104639_(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;m_8119_()V"),
            require = 0)
    private void xerion$skipPlayerTick(Entity entity) {
        if (entity == Minecraft.getInstance().player && SkipTickService.consumeSkipTick()) {
            return;
        }
        entity.tick();
    }
}
