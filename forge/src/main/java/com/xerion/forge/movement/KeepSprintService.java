package com.xerion.forge.movement;

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

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.EntityHitResult;

public final class KeepSprintService {
    private static boolean postTickReady;

    private KeepSprintService() {
    }

    public static void beginRuntimeTick() {
        postTickReady = false;
    }

    public static void endRuntimeTick() {
        postTickReady = true;
    }

    public static boolean shouldKeepSprintAfterAttack(Minecraft minecraft, LocalPlayer player) {
        if (!VisualSettings.keepSprintEnabled()) {
            return false;
        }
        if (minecraft == null || player == null) {
            return false;
        }
        if (VisualSettings.keepSprintPrediction() && !postTickReady) {
            return false;
        }
        if (VisualSettings.keepSprintGroundOnly() && !player.onGround()) {
            return false;
        }
        if (VisualSettings.keepSprintReachOnly()) {
            if (!(minecraft.hitResult instanceof EntityHitResult hitResult)) {
                return false;
            }
            return hitResult.getLocation().distanceTo(player.getEyePosition()) > 3.0D;
        }
        return true;
    }
}
