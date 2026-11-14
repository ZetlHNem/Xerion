package com.xerion.forge.util;

import com.xerion.common.movement.ScaffoldRotation;

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

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class RotationPacketBridge {
    private RotationPacketBridge() {
    }

    public static void requestPacketLookAt(LocalPlayer player, BlockPos pos) {
        requestPacketLookAt(player, Vec3.atCenterOf(pos));
    }

    public static void requestPacketLookAt(LocalPlayer player, Vec3 point) {
        Vec3 diff = point.subtract(player.getEyePosition(1.0F));
        double horizontal = Math.hypot(diff.x, diff.z);
        float yaw = (float) (Mth.atan2(diff.z, diff.x) * Mth.RAD_TO_DEG) - 90.0f;
        float pitch = (float) -(Mth.atan2(diff.y, horizontal) * Mth.RAD_TO_DEG);
        ScaffoldRotation.requestPacketOnly(yaw, Mth.clamp(pitch, -90.0f, 90.0f), true);
    }
}
