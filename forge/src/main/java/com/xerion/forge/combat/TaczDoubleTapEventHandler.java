package com.xerion.forge.combat;

import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.event.common.GunShootEvent;
import com.xerion.common.config.CombatSettings;
import com.xerion.forge.util.BypassProtocolService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public final class TaczDoubleTapEventHandler {
    private static final Minecraft MC = Minecraft.getInstance();
    private boolean handlingDoubleTap;

    @SubscribeEvent
    public void onGunShoot(GunShootEvent event) {
        LocalPlayer player = MC.player;
        if (player == null || event.getLogicalSide() != LogicalSide.CLIENT
                || !player.equals(event.getShooter())
                || BypassProtocolService.correctionRecoveryActive()) {
            return;
        }
        TaczRagebotHooks.prepareAntiAimShot(player);
    }

    @SubscribeEvent
    public void onGunFire(GunFireEvent event) {
        LocalPlayer player = MC.player;
        if (player == null || MC.screen != null
                || event.getLogicalSide() != LogicalSide.CLIENT
                || !player.equals(event.getShooter())) {
            return;
        }
        try {
            if (!handlingDoubleTap
                    && !BypassProtocolService.correctionRecoveryActive()
                    && CombatSettings.ragebotEnabled()
                    && CombatSettings.ragebotDoubleTap()
                    && (CombatSettings.ragebotAutomaticFire() || MC.options.keyAttack.isDown())) {
                handlingDoubleTap = true;
                TaczGunBridge.ShootAttempt result = TaczGunBridge.tryDoubleTapShoot(player);
                TaczGunBridge.finishPrimaryShot(player, result == TaczGunBridge.ShootAttempt.SUCCESS);
            }
        } finally {
            handlingDoubleTap = false;
            TaczRagebotHooks.finishAntiAimShot(player);
        }
    }
}
