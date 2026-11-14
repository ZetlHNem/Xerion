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

import com.xerion.common.combat.CombatAimRules;
import com.xerion.common.config.CombatSettings;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class KillAuraAimMath {
    private KillAuraAimMath() {
    }

    public static Rotation rotationTo(LocalPlayer player, LivingEntity target) {
        Vec3 eyes = player.getEyePosition();
        Vec3 targetPoint = CombatTargeting.aimPoint(player, target,
                !CombatSettings.throughWalls() && CombatSettings.rayTrace());
        CombatAimRules.Rotation rotation = CombatAimRules.rotationBetween(
                eyes.x,
                eyes.y,
                eyes.z,
                targetPoint.x,
                targetPoint.y,
                targetPoint.z
        );
        return new Rotation(rotation.yaw(), rotation.pitch());
    }

    public static float yawDelta(LocalPlayer player, LivingEntity target) {
        float targetYaw = rotationTo(player, target).yaw();
        return CombatAimRules.yawDelta(player.getYRot(), targetYaw);
    }

    public static boolean insideFov(LocalPlayer player, LivingEntity target, float fov) {
        return CombatAimRules.insideFov(yawDelta(player, target), fov);
    }

    public static float rotationSpeed() {
        return CombatAimRules.rotationSpeed(CombatSettings.smoothness());
    }

    public static float rotateYaw(float current, float target, float step) {
        return CombatAimRules.rotateYaw(current, target, step);
    }

    public static float approach(float current, float target, float step) {
        return CombatAimRules.approach(current, target, step);
    }

    public static float renderLerpAlpha(float dt) {
        return CombatAimRules.renderLerpAlpha(CombatSettings.smoothness(), dt);
    }

    public static float lerpYaw(float current, float target, float alpha) {
        return CombatAimRules.lerpYaw(current, target, alpha);
    }

    public record Rotation(float yaw, float pitch) {
    }
}