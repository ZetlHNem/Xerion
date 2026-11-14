package com.xerion.forge.mixin;

import com.xerion.forge.combat.TaczRagebotHooks;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "com.tacz.guns.item.ModernKineticGunItem", remap = false)
abstract class TaczAbstractGunItemMixin {
    @ModifyVariable(method = "doBulletSpread", at = @At("HEAD"), argsOnly = true, ordinal = 1, require = 0)
    private float xerion$removeRagebotSpread(float spread, ShooterDataHolder data, ItemStack stack, LivingEntity shooter,
                                             Projectile projectile, int bulletIndex, float velocity,
                                             float originalSpread, float pitch, float yaw) {
        return TaczRagebotHooks.removeSpreadActive(shooter) ? 0.0f : spread;
    }
}
