package com.xerion.forge.util;

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

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;

public final class HotbarSlotActions {
    private HotbarSlotActions() {
    }

    public static void select(LocalPlayer player, int slot) {
        Minecraft minecraft = Minecraft.getInstance();
        if (slot < 0 || slot > 8 || player.getInventory().selected == slot || minecraft.getConnection() == null) {
            return;
        }
        player.getInventory().selected = slot;
        minecraft.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
    }

    public static void spoofServerSlot(int slot) {
        Minecraft minecraft = Minecraft.getInstance();
        if (slot < 0 || slot > 8 || minecraft.getConnection() == null) {
            return;
        }
        minecraft.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
    }
}
