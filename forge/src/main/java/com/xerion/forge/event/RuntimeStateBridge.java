package com.xerion.forge.event;

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

import com.xerion.common.state.RuntimeState;
import com.xerion.common.state.PlayerPosition;
import com.xerion.common.state.PlayerVelocity;
import net.minecraft.client.Minecraft;

public final class RuntimeStateBridge {
    private RuntimeStateBridge() {
    }

    public static void update(RuntimeState state, Minecraft minecraft) {
        boolean inWorld = minecraft.level != null && minecraft.player != null;
        state.setInWorld(inWorld);

        if (minecraft.player == null) {
            state.clearPlayerPosition();
            state.clearPlayerVelocity();
            return;
        }

        state.setPlayerPosition(new PlayerPosition(
                minecraft.player.getX(),
                minecraft.player.getY(),
                minecraft.player.getZ()
        ));
        state.setPlayerVelocity(new PlayerVelocity(
                minecraft.player.getDeltaMovement().x,
                minecraft.player.getDeltaMovement().y,
                minecraft.player.getDeltaMovement().z
        ));
    }
}

