package com.xerion.forge.combat;

import com.xerion.common.combat.TargetStrafeRules;

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

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class TargetStrafeService {
    private static final Minecraft MC = Minecraft.getInstance();
    private final KillAuraService KillAuraService;
    private int autoDirection = 1;

    public TargetStrafeService(KillAuraService KillAuraService) {
        this.KillAuraService = KillAuraService;
    }

    public void tickEnd() {
        if (!CombatSettings.targetStrafeEnabled()) {
            return;
        }

        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (player == null || level == null || MC.screen != null || player.isPassenger()
                || player.isSpectator() || player.getAbilities().flying || player.input == null) {
            return;
        }
        if (!moving(player)) {
            return;
        }

        LivingEntity target = target(player, level);
        if (target == null) {
            return;
        }

        if (CombatSettings.targetStrafeAutoJump() && player.onGround() && !MC.options.keyJump.isDown()) {
            player.jumpFromGround();
        }

        applyStrafe(player, target);
    }

    private LivingEntity target(LocalPlayer player, ClientLevel level) {
        LivingEntity killAuraTarget = KillAuraService.activeTarget();
        double maxRange = Math.max(CombatSettings.range(), CombatSettings.targetStrafeRadius() + 2.0D);
        if (killAuraTarget != null && KillAuraService.validTarget(player, killAuraTarget, maxRange)) {
            return killAuraTarget;
        }

        List<LivingEntity> targets = KillAuraService.targets(player, level, maxRange);
        return targets.isEmpty() ? null : targets.get(0);
    }

    private void applyStrafe(LocalPlayer player, LivingEntity target) {
        Vec3 playerPos = player.position();
        Vec3 targetPos = target.position();
        TargetStrafeRules.Motion motion = TargetStrafeRules.motion(
                playerPos.x,
                playerPos.z,
                targetPos.x,
                targetPos.z,
                direction(player),
                CombatSettings.targetStrafeRadius(),
                CombatSettings.targetStrafeSpeed());
        if (!motion.active()) {
            return;
        }

        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(motion.x(), velocity.y, motion.z());
        player.hurtMarked = true;
    }

    private int direction(LocalPlayer player) {
        String setting = CombatSettings.targetStrafeDirection();
        int manual = TargetStrafeRules.manualDirection(
                setting,
                CombatSettings.TARGET_STRAFE_DIRECTION_LEFT,
                CombatSettings.TARGET_STRAFE_DIRECTION_RIGHT,
                0);
        if (manual != 0) {
            return manual;
        }
        autoDirection = TargetStrafeRules.autoDirection(player.input.leftImpulse, autoDirection);
        return autoDirection;
    }

    private static boolean moving(LocalPlayer player) {
        return TargetStrafeRules.moving(player.input.forwardImpulse, player.input.leftImpulse);
    }
}
