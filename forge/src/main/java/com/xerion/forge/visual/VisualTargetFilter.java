package com.xerion.forge.visual;

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

import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.runtime.SocialProfileStore;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

public final class VisualTargetFilter {
    private VisualTargetFilter() {
    }

    public static boolean isEnemyVisualTarget(LocalPlayer localPlayer, LivingEntity entity) {
        if (localPlayer == null || entity == null || entity == localPlayer) {
            return false;
        }
        if (!entity.isAlive() || entity.isRemoved() || entity.isSpectator()) {
            return false;
        }
        if (FakePlayerRegistry.isDummy(entity)) {
            return true;
        }
        if (entity instanceof Player player) {
            return isEnemyPlayer(localPlayer, player);
        }
        if (entity instanceof Monster) {
            return VisualSettings.visualEnemiesHostileMobs();
        }
        if (entity instanceof Animal || entity instanceof WaterAnimal) {
            return VisualSettings.visualEnemiesPassiveMobs();
        }
        if (entity instanceof PathfinderMob) {
            return VisualSettings.visualEnemiesPassiveMobs();
        }
        return VisualSettings.visualEnemiesOtherLiving();
    }

    private static boolean isEnemyPlayer(LocalPlayer localPlayer, Player player) {
        if (!VisualSettings.visualEnemiesPlayers()) {
            return false;
        }
        if (VisualSettings.visualEnemiesIgnoreBots() && AntiBotService.isBot(localPlayer, player)) {
            return false;
        }
        if (SocialProfileStore.isFriend(player)) {
            return false;
        }
        if (SocialProfileStore.isStructuralTeamMate(localPlayer, player)) {
            return false;
        }
        return true;
    }
}
