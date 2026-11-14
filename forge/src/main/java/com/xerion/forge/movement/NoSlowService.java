package com.xerion.forge.movement;

import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.packet.PacketBlockageService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;

/**
 * Port of OpenOpal's NoSlow mode gating. Xerion removes the 0.2 use-item
 * slowdown through a mixin; this service decides per mode/tick whether that
 * removal is allowed, so GrimJump / GrimFast / Watchdog / Universal no longer
 * behave identically to Vanilla.
 */
public final class NoSlowService {
    public enum Action {
        BLOCKABLE,
        USEABLE,
        BOW,
        NONE
    }

    private static int groundTicks;
    private static int usingTicks;
    private static int lastSelectedSlot = -1;
    private static int slotChangeTick = -10_000;

    private NoSlowService() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (!VisualSettings.noSlowEnabled() || player == null) {
            clearRuntimeState();
            return;
        }
        if (minecraft.screen != null) {
            usingTicks = 0;
            return;
        }
        groundTicks = player.onGround() ? groundTicks + 1 : 0;
        usingTicks = player.isUsingItem() ? usingTicks + 1 : 0;
        int selected = player.getInventory().selected;
        if (lastSelectedSlot != -1 && selected != lastSelectedSlot) {
            slotChangeTick = player.tickCount;
        }
        lastSelectedSlot = selected;

        if (VisualSettings.noSlowKeepSprinting()
                && player.isUsingItem()
                && player.input != null
                && (Math.abs(player.input.forwardImpulse) > 1.0E-5F
                || Math.abs(player.input.leftImpulse) > 1.0E-5F)) {
            player.setSprinting(true);
        }
    }

    public static boolean canCancelSlowdown() {
        if (!VisualSettings.noSlowEnabled()) {
            return false;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        String mode = VisualSettings.noSlowMode();
        return switch (mode) {
            case "Universal" -> !VisualSettings.noSlowSlowDown() && player.isUsingItem();
            case "GrimJump" -> player.isUsingItem() && groundTicks == 1;
            case "GrimFast" -> player.isUsingItem() && usingTicks >= 2;
            case "Watchdog" -> player.isUsingItem()
                    && actionOf(player) != Action.BOW
                    && player.tickCount - slotChangeTick != 1
                    && (actionOf(player) != Action.USEABLE || PacketBlockageService.outbound().isAnyBlockages());
            default -> player.isUsingItem();
        };
    }

    public static void clearRuntimeState() {
        groundTicks = 0;
        usingTicks = 0;
        lastSelectedSlot = -1;
        slotChangeTick = -10_000;
    }

    private static Action actionOf(LocalPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return Action.NONE;
        }
        UseAnim animation = stack.getUseAnimation();
        if (animation == UseAnim.BLOCK) {
            return Action.BLOCKABLE;
        }
        if (animation == UseAnim.NONE) {
            return stack.getItem() instanceof SwordItem ? Action.BLOCKABLE : Action.NONE;
        }
        if (animation == UseAnim.BOW) {
            return Action.BOW;
        }
        return Action.USEABLE;
    }
}
