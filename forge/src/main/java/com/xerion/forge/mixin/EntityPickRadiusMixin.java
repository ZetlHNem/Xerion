package com.xerion.forge.mixin;

import com.xerion.forge.misc.*;

import com.xerion.forge.render.*;

import com.xerion.forge.world.*;

import com.xerion.forge.player.*;

import com.xerion.forge.movement.*;

import com.xerion.forge.combat.*;

import com.xerion.forge.*;

import com.xerion.forge.runtime.*;

import com.xerion.forge.event.*;

import com.xerion.forge.util.*;

import com.xerion.common.config.CombatSettings;
import com.xerion.forge.runtime.SocialProfileStore;
import com.xerion.common.runtime.HitboxPickContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
abstract class EntityPickRadiusMixin {
    @Inject(method = "m_6143_", at = @At("RETURN"), cancellable = true, require = 0)
    private void xerion$expandPickRadius(CallbackInfoReturnable<Float> cir) {
        if (!HitboxPickContext.active() || !CombatSettings.hitboxesEnabled()) {
            return;
        }

        Entity entity = (Entity) (Object) this;
        if (!(entity instanceof LivingEntity target)) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().screen != null || target == player || !target.isAlive()
                || target.isRemoved() || target.isSpectator()) {
            return;
        }
        if (!targetAllowed(player, target)) {
            return;
        }

        float size = Math.max(0.0f, Math.min(1.0f, CombatSettings.hitboxesSize()));
        cir.setReturnValue(Math.max(cir.getReturnValueF(), size));
    }

    private static boolean targetAllowed(LocalPlayer player, LivingEntity target) {
        if (target.isInvisible() && !CombatSettings.targetInvisible()) {
            return false;
        }
        if (FakePlayerRegistry.isDummy(target)) {
            return CombatSettings.targetPlayers();
        }
        if (target instanceof Player otherPlayer) {
            return CombatSettings.targetPlayers()
                    && !AntiBotService.isBot(player, otherPlayer)
                    && !SocialProfileStore.isProtected(player, otherPlayer);
        }
        return target instanceof Mob && CombatSettings.targetMobs();
    }
}
