package com.xerion.forge.simulation;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Port of OpenOpal's PlayerSimulation: clones the local player into a dummy
 * entity and advances its physics to predict landing / falling positions.
 */
public final class PlayerSimulation {
    private final LocalPlayer player;
    private final RemotePlayer simulated;

    public PlayerSimulation(LocalPlayer player) {
        ClientLevel level = Minecraft.getInstance().level;
        this.player = player;
        if (level == null || player == null) {
            this.simulated = null;
            return;
        }
        GameProfile profile = new GameProfile(UUID.randomUUID(), "Xerion Simulation");
        this.simulated = new RemotePlayer(level, profile) {
            @Override
            public boolean isPushable() {
                return false;
            }
        };
        cloneStates();
    }

    public boolean valid() {
        return simulated != null;
    }

    public void simulateTick() {
        simulateTicks(1);
    }

    public void simulateTicks(int ticks) {
        if (player == null || player.input == null) {
            simulateTicks(ticks, 0.0F, 0.0F, 0.0F);
            return;
        }
        simulateTicks(ticks, player.input.forwardImpulse, player.input.leftImpulse, 0.0F);
    }

    public void simulateTicks(int ticks, float forward, float sideways, float upward) {
        if (simulated == null) {
            return;
        }
        for (int i = 0; i < ticks; i++) {
            simulated.travel(new Vec3(sideways, upward, forward));
        }
    }

    public RemotePlayer entity() {
        return simulated;
    }

    private void cloneStates() {
        simulated.setNoGravity(player.isNoGravity());
        simulated.setPos(player.getX(), player.getY(), player.getZ());
        simulated.setBoundingBox(player.getBoundingBox());
        simulated.setDeltaMovement(player.getDeltaMovement());
        simulated.setYRot(player.getYRot());
        simulated.setXRot(player.getXRot());
        simulated.setYHeadRot(player.getYHeadRot());
        simulated.setOnGround(player.onGround());
        simulated.setSprinting(player.isSprinting());
        simulated.setShiftKeyDown(player.isShiftKeyDown());
        simulated.fallDistance = player.fallDistance;
        simulated.setSpeed(player.getSpeed());
        for (MobEffectInstance effect : player.getActiveEffects()) {
            simulated.addEffect(new MobEffectInstance(effect));
        }
    }
}
