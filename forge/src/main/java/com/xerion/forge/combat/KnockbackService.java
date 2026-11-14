package com.xerion.forge.combat;

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
import com.xerion.common.config.BypassSettings;
import com.xerion.common.combat.CombatCompatibilityRules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public final class KnockbackService {
    private static ServerboundPlayerCommandPacket.Action lastAction;
    private static long lastActionNanos;

    private KnockbackService() {
    }

    public static void beforeAttack(LocalPlayer player) {
        if (player == null || Minecraft.getInstance().getConnection() == null) {
            return;
        }

        boolean knockback = CombatSettings.knockbackEnabled();
        boolean sprintReset = CombatSettings.sprintResetEnabled();
        if (!knockback && !sprintReset) {
            return;
        }

        if (knockback && !player.isSprinting()) {
            send(player, ServerboundPlayerCommandPacket.Action.START_SPRINTING);
            player.setSprinting(true);
        }

        if (!sprintReset) {
            return;
        }

        send(player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING);
        send(player, ServerboundPlayerCommandPacket.Action.START_SPRINTING);
        player.setSprinting(true);
    }

    public static void afterAttack(LocalPlayer player) {
        if (!CombatSettings.knockbackEnabled() || player == null || Minecraft.getInstance().getConnection() == null) {
            return;
        }
        send(player, ServerboundPlayerCommandPacket.Action.START_SPRINTING);
        player.setSprinting(true);
    }

    private static void send(LocalPlayer player, ServerboundPlayerCommandPacket.Action action) {
        long now = System.nanoTime();
        if (BypassSettings.enabled() && BypassSettings.actionDeduplication()
                && action == lastAction
                && CombatCompatibilityRules.duplicateAction(now, lastActionNanos,
                BypassSettings.actionDeduplicationWindowMs())) {
            return;
        }
        Minecraft.getInstance().getConnection().send(new ServerboundPlayerCommandPacket(
                player,
                action
        ));
        lastAction = action;
        lastActionNanos = now;
    }
}
