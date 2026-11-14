package com.xerion.forge.combat;

import com.xerion.common.config.CombatSettings;
import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.runtime.SocialProfileStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;

/**
 * Port of OpenOpal's Block module auto-block: holds the use key while an enemy
 * is in range and the player is holding a sword or an offhand shield.
 */
public final class AutoBlockService {
    private static boolean forcingUse;

    private AutoBlockService() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        boolean shouldBlock = false;
        if (CombatSettings.autoBlockEnabled() && player != null && minecraft.level != null
                && minecraft.screen == null && !VisualSettings.scaffoldEnabled()
                && !player.isSpectator() && !player.isPassenger()) {
            boolean sword = player.getMainHandItem().getItem() instanceof SwordItem;
            boolean shield = player.getOffhandItem().getItem() instanceof ShieldItem;
            if ((sword || shield)
                    && (!CombatSettings.autoBlockRequireAttackKey() || minecraft.options.keyAttack.isDown())) {
                shouldBlock = hasTargetInRange(player, minecraft.level, CombatSettings.autoBlockRange());
            }
        }
        if (shouldBlock) {
            minecraft.options.keyUse.setDown(true);
            forcingUse = true;
        } else if (forcingUse) {
            minecraft.options.keyUse.setDown(false);
            forcingUse = false;
        }
    }

    public static void clearRuntimeState() {
        if (forcingUse) {
            Minecraft.getInstance().options.keyUse.setDown(false);
        }
        forcingUse = false;
    }

    private static boolean hasTargetInRange(LocalPlayer player, ClientLevel level, double range) {
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || entity == player || entity.isRemoved()
                    || living.getHealth() <= 0.0F) {
                continue;
            }
            if (player.distanceTo(entity) > range) {
                continue;
            }
            if (entity instanceof Player other
                    && (AntiBotService.isBot(player, other) || SocialProfileStore.isProtected(player, other))) {
                continue;
            }
            return true;
        }
        return false;
    }
}
