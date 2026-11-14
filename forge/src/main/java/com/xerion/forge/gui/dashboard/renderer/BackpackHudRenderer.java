package com.xerion.forge.gui.dashboard;

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
import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class BackpackHudRenderer {
    private static final int COLS = 9;
    private static final int ROWS = 3;
    private static final int SLOT = 15;
    private static final int GAP = 2;
    private static final int PAD = 5;
    private static final int TITLE_H = 13;
    private static final int TITLE_GRID_GAP = 1;
    private static final int W = PAD * 2 + COLS * SLOT + (COLS - 1) * GAP;
    private static final int H = TITLE_H + TITLE_GRID_GAP + ROWS * SLOT + (ROWS - 1) * GAP + PAD;
    private static final float TITLE_SIZE = 5.6f;
    private static final float ICON_SIZE = 8.0f;
    private static final float COUNT_SIZE = 4.8f;
    private static final float BLUR_STRENGTH = 18.0f;
    private static final float BG_ALPHA = 0.80f;
    private static final float ITEM_SCALE = (SLOT - 4) / 16.0f;
    private static final int SLOT_COUNT = COLS * ROWS;
    private static final ItemStack[] previousStacks = new ItemStack[SLOT_COUNT];
    private static float animatedX;
    private static float animatedY;
    private static float alpha;
    private static boolean animationInitialized;
    private static HeldStack heldStack;
    private static final List<MoveAnimation> moveAnimations = new ArrayList<>();

    static {
        for (int i = 0; i < previousStacks.length; i++) {
            previousStacks[i] = ItemStack.EMPTY;
        }
    }

    private BackpackHudRenderer() {
    }

    public static void render(GuiGraphics graphics, boolean editMode) {
        boolean enabled = VisualSettings.hudWidgetEnabled(VisualSettings.HUD_WIDGET_BACKPACK);
        ItemStack[] currentStacks = snapshotBackpack();
        ItemStack carried = carriedStack();
        updateTransientItems(currentStacks, carried);
        boolean visibleTarget = enabled && (hasContents(currentStacks) || hasTransientItems() || editMode);
        if (!visibleTarget && alpha <= 0.01f) {
            remember(currentStacks);
            return;
        }
        clampToScreen();
        int targetX = VisualSettings.backpackHudX();
        int targetY = VisualSettings.backpackHudY();
        if (!animationInitialized) {
            animatedX = targetX;
            animatedY = targetY;
            animationInitialized = true;
        }
        animatedX = UiMath.lerp(animatedX, targetX, UiAnimationTimings.HUD_MOVE);
        animatedY = UiMath.lerp(animatedY, targetY, UiAnimationTimings.HUD_MOVE);
        alpha = UiMath.lerp(alpha, visibleTarget ? 1.0f : 0.0f, UiAnimationTimings.HUD_FADE);
        float hudAlpha = UiMath.clamp01(alpha);
        if (hudAlpha <= 0.01f) {
            return;
        }

        int x = Math.round(animatedX);
        int y = Math.round(animatedY);
        Window window = Minecraft.getInstance().getWindow();
        if (VisualSettings.backpackHudBlur()) {
            KawaseBlurPipeline.renderSurfaceBackdrop(graphics, window.getGuiScaledWidth(), window.getGuiScaledHeight(),
                    x, y, W, H, 6.0f, hudAlpha, BLUR_STRENGTH);
        }
        ShadowRenderer.shadow(graphics, x, y, W, H, 6, ShadowStyle.card(), hudAlpha * BG_ALPHA);
        ShapeRenderer.roundRect(graphics, x, y, W, H, 6,
                UiColor.alpha(UiPalette.surface(), hudAlpha * BG_ALPHA));
        if (editMode) {
            ShapeRenderer.roundRectBorder(graphics, x, y, W, H, 6, 1,
                    UiColor.alpha(UiPalette.accentSoft(), hudAlpha));
        }

        if (VisualSettings.backpackHudShowTitle()) {
            float iconX = x + PAD;
            float iconY = y + UiMath.centeredTextY(TITLE_H, ICON_SIZE) + 1.0f;
            MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_BOX_OPEN, iconX, iconY, ICON_SIZE,
                    UiColor.alpha(UiPalette.accent(), hudAlpha));
            float titleX = iconX + MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_BOX_OPEN, ICON_SIZE) + 4.0f;
            MSDFRenderer.draw(graphics, "Backpack", titleX, y + UiMath.centeredTextY(TITLE_H, TITLE_SIZE), TITLE_SIZE,
                    UiColor.alpha(UiPalette.text(), hudAlpha));
        }

        int gridX = x + PAD;
        int gridY = y + TITLE_H + TITLE_GRID_GAP;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int visualSlot = row * COLS + col;
                int slotX = gridX + col * (SLOT + GAP);
                int slotY = gridY + row * (SLOT + GAP);
                ShapeRenderer.roundRect(graphics, slotX, slotY, SLOT, SLOT, 4,
                        UiColor.alpha(UiPalette.field(), hudAlpha * 0.64f));
                ItemStack stack = displayStackForSlot(currentStacks, visualSlot);
                if (stack.isEmpty()) {
                    continue;
                }
                FadingItemRenderer.draw(graphics, stack, slotX + 2, slotY + 2, ITEM_SCALE, hudAlpha);
                if (VisualSettings.backpackHudShowCounts()) {
                    drawCount(graphics, stack, slotX, slotY, hudAlpha);
                }
            }
        }

        drawHeldStack(graphics, gridX, gridY, hudAlpha);
        drawMoveAnimations(graphics, gridX, gridY, hudAlpha);
        remember(currentStacks);
    }

    public static boolean contains(double mouseX, double mouseY) {
        if (!VisualSettings.hudWidgetEnabled(VisualSettings.HUD_WIDGET_BACKPACK)) {
            return false;
        }
        int x = VisualSettings.backpackHudX();
        int y = VisualSettings.backpackHudY();
        return mouseX >= x && mouseX <= x + W && mouseY >= y && mouseY <= y + H;
    }

    public static void moveTo(double mouseX, double mouseY, double offsetX, double offsetY) {
        Minecraft minecraft = Minecraft.getInstance();
        int rawX = (int) Math.round(mouseX - offsetX);
        int rawY = (int) Math.round(mouseY - offsetY);
        HudSnap.Position snapped = HudSnap.snap(rawX, rawY, W, H,
                minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
        VisualSettings.setBackpackHudPosition(snapped.x(), snapped.y());
        animatedX = snapped.x();
        animatedY = snapped.y();
        animationInitialized = true;
    }

    public static double dragOffsetX(double mouseX) {
        return mouseX - VisualSettings.backpackHudX();
    }

    public static double dragOffsetY(double mouseY) {
        return mouseY - VisualSettings.backpackHudY();
    }

    public static int width() {
        return W;
    }

    public static int height() {
        return H;
    }

    private static boolean hasBackpackContents() {
        return hasContents(snapshotBackpack());
    }

    private static boolean hasContents(ItemStack[] stacks) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasTransientItems() {
        return heldStack != null || !moveAnimations.isEmpty();
    }

    private static ItemStack[] snapshotBackpack() {
        ItemStack[] stacks = new ItemStack[SLOT_COUNT];
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            for (int i = 0; i < stacks.length; i++) {
                stacks[i] = ItemStack.EMPTY;
            }
            return stacks;
        }
        Inventory inventory = minecraft.player.getInventory();
        for (int i = 0; i < SLOT_COUNT; i++) {
            stacks[i] = inventory.getItem(i + 9).copy();
        }
        return stacks;
    }

    private static ItemStack carriedStack() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return ItemStack.EMPTY;
        }
        Player player = minecraft.player;
        ItemStack carried = player.containerMenu == null ? ItemStack.EMPTY : player.containerMenu.getCarried();
        if (!carried.isEmpty()) {
            return carried.copy();
        }
        return player.inventoryMenu == null ? ItemStack.EMPTY : player.inventoryMenu.getCarried().copy();
    }

    private static void updateTransientItems(ItemStack[] currentStacks, ItemStack carried) {
        if (Minecraft.getInstance().player == null) {
            heldStack = null;
            moveAnimations.clear();
            return;
        }
        if (heldStack == null) {
            int source = findPickedUpSlot(currentStacks, carried);
            if (source >= 0) {
                heldStack = new HeldStack(source, previousStacks[source]);
            }
        } else {
            int target = findPlacedSlot(currentStacks, carried, heldStack);
            if (target >= 0) {
                HeldStack swapped = swappedHeldStack(target, carried, heldStack);
                if (target != heldStack.sourceSlot) {
                    moveAnimations.add(new MoveAnimation(heldStack.sourceSlot, target,
                            movedStack(heldStack.stack, previousStacks[target], currentStacks[target]),
                            swapped == null ? previousStacks[target] : ItemStack.EMPTY));
                }
                heldStack = swapped;
            } else if (carried.isEmpty()) {
                heldStack = null;
            } else if (!sameItem(carried, heldStack.stack)) {
                heldStack = null;
            } else if (!currentStacks[heldStack.sourceSlot].isEmpty() && sameItem(currentStacks[heldStack.sourceSlot], heldStack.stack)) {
                heldStack = null;
            }
        }

        moveAnimations.removeIf(MoveAnimation::done);
    }

    private static int findPickedUpSlot(ItemStack[] currentStacks, ItemStack carried) {
        if (carried.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack previous = previousStacks[i];
            if (!previous.isEmpty() && currentStacks[i].isEmpty() && sameItem(previous, carried)) {
                return i;
            }
        }
        return -1;
    }

    private static int findPlacedSlot(ItemStack[] currentStacks, ItemStack carried, HeldStack held) {
        int bestSlot = -1;
        int bestIncrease = 0;
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (currentStacks[i].isEmpty() || !sameItem(currentStacks[i], held.stack)) {
                continue;
            }
            if (i == held.sourceSlot && sameItem(previousStacks[i], currentStacks[i])) {
                continue;
            }
            ItemStack previous = previousStacks[i];
            boolean newStack = previous.isEmpty();
            int increase = currentStacks[i].getCount() - (sameItem(previous, currentStacks[i]) ? previous.getCount() : 0);
            boolean replacedSource = i == held.sourceSlot && !sameItem(previous, currentStacks[i]);
            if ((newStack || increase > 0 || replacedSource) && increase >= bestIncrease) {
                bestSlot = i;
                bestIncrease = Math.max(1, increase);
            }
        }
        if (bestSlot >= 0) {
            return bestSlot;
        }
        if (carried.isEmpty()) {
            for (int i = 0; i < SLOT_COUNT; i++) {
                if (i != held.sourceSlot && !currentStacks[i].isEmpty() && sameItem(currentStacks[i], held.stack)
                        && !sameItem(previousStacks[i], currentStacks[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static ItemStack movedStack(ItemStack held, ItemStack previousTarget, ItemStack currentTarget) {
        ItemStack stack = held.copy();
        if (!previousTarget.isEmpty() && sameItem(previousTarget, currentTarget)) {
            int increase = Math.max(1, currentTarget.getCount() - previousTarget.getCount());
            stack.setCount(Math.min(increase, stack.getMaxStackSize()));
        }
        return stack;
    }

    private static ItemStack displayStackForSlot(ItemStack[] currentStacks, int visualSlot) {
        if (heldStack != null && heldStack.sourceSlot == visualSlot && currentStacks[visualSlot].isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (MoveAnimation animation : moveAnimations) {
            if (animation.targetSlot == visualSlot && animation.hidesTargetStack()) {
                return animation.targetBaseStack;
            }
        }
        return currentStacks[visualSlot];
    }

    private static void drawHeldStack(GuiGraphics graphics, int gridX, int gridY, float hudAlpha) {
        if (heldStack == null || !stackAtVisualSlot(heldStack.sourceSlot).isEmpty()) {
            return;
        }
        int slotX = slotX(gridX, heldStack.sourceSlot);
        int slotY = slotY(gridY, heldStack.sourceSlot);
        FadingItemRenderer.draw(graphics, heldStack.stack, slotX + 2, slotY + 2, ITEM_SCALE, hudAlpha);
        if (VisualSettings.backpackHudShowCounts()) {
            drawCount(graphics, heldStack.stack, slotX, slotY, hudAlpha);
        }
    }

    private static void drawMoveAnimations(GuiGraphics graphics, int gridX, int gridY, float hudAlpha) {
        Iterator<MoveAnimation> iterator = moveAnimations.iterator();
        while (iterator.hasNext()) {
            MoveAnimation animation = iterator.next();
            int targetX = slotX(gridX, animation.targetSlot);
            int targetY = slotY(gridY, animation.targetSlot);
            int sourceX = slotX(gridX, animation.sourceSlot);
            int sourceY = slotY(gridY, animation.sourceSlot);
            animation.ensurePosition(sourceX, sourceY, targetX, targetY);
            if (animation.drawsMovingStack()) {
                FadingItemRenderer.draw(graphics, animation.stack, animation.x + 2.0f, animation.y + 2.0f, ITEM_SCALE, hudAlpha);
                if (VisualSettings.backpackHudShowCounts()) {
                    drawCount(graphics, animation.stack, animation.x, animation.y, hudAlpha);
                }
            }
            if (animation.done()) {
                iterator.remove();
            }
        }
    }

    private static int slotX(int gridX, int visualSlot) {
        return gridX + (visualSlot % COLS) * (SLOT + GAP);
    }

    private static int slotY(int gridY, int visualSlot) {
        return gridY + (visualSlot / COLS) * (SLOT + GAP);
    }

    private static void remember(ItemStack[] currentStacks) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            previousStacks[i] = currentStacks[i].copy();
        }
    }

    private static boolean sameItem(ItemStack first, ItemStack second) {
        return !first.isEmpty() && !second.isEmpty() && ItemStack.isSameItemSameTags(first, second);
    }

    private static void drawCount(GuiGraphics graphics, ItemStack stack, float slotX, float slotY, float alpha) {
        if (stack.getCount() <= 1) {
            return;
        }
        String count = stack.getCount() > 99 ? "99+" : String.valueOf(stack.getCount());
        float textW = MSDFRenderer.width(count, COUNT_SIZE);
        float textX = slotX + SLOT - textW - 1.0f;
        float textY = slotY + SLOT - COUNT_SIZE - 1.0f;
        MSDFRenderer.drawScreenTextWithShadow(graphics, count, textX, textY, COUNT_SIZE,
                UiColor.alpha(UiPalette.text(), alpha));
    }

    private static void clampToScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        int x = UiMath.clamp(VisualSettings.backpackHudX(), 0, Math.max(0, screenW - W));
        int y = UiMath.clamp(VisualSettings.backpackHudY(), 0, Math.max(0, screenH - H));
        if (x != VisualSettings.backpackHudX() || y != VisualSettings.backpackHudY()) {
            VisualSettings.setBackpackHudPosition(x, y);
        }
    }

    private static HeldStack swappedHeldStack(int targetSlot, ItemStack carried, HeldStack placed) {
        ItemStack previousTarget = previousStacks[targetSlot];
        if (carried.isEmpty() || previousTarget.isEmpty() || !sameItem(previousTarget, carried)
                || sameItem(previousTarget, placed.stack)) {
            return null;
        }
        return new HeldStack(targetSlot, carried);
    }

    private static ItemStack stackAtVisualSlot(int visualSlot) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || visualSlot < 0 || visualSlot >= SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        return minecraft.player.getInventory().getItem(visualSlot + 9);
    }

    private record HeldStack(int sourceSlot, ItemStack stack) {
        private HeldStack {
            stack = stack.copy();
        }
    }

    private static final class MoveAnimation {
        private static final float SPEED = 0.34f;
        private final int sourceSlot;
        private final int targetSlot;
        private final ItemStack stack;
        private final ItemStack targetBaseStack;
        private float x;
        private float y;
        private boolean initialized;
        private int settleTicks;

        private MoveAnimation(int sourceSlot, int targetSlot, ItemStack stack, ItemStack targetBaseStack) {
            this.sourceSlot = sourceSlot;
            this.targetSlot = targetSlot;
            this.stack = stack.copy();
            this.targetBaseStack = targetBaseStack.copy();
        }

        private void ensurePosition(float sourceX, float sourceY, float targetX, float targetY) {
            if (!initialized) {
                x = sourceX;
                y = sourceY;
                initialized = true;
            }
            x = UiMath.lerp(x, targetX, SPEED);
            y = UiMath.lerp(y, targetY, SPEED);
            if (Math.abs(x - targetX) <= 0.35f && Math.abs(y - targetY) <= 0.35f) {
                x = targetX;
                y = targetY;
                settleTicks++;
            } else {
                settleTicks = 0;
            }
        }

        private boolean hidesTargetStack() {
            return settleTicks <= 0;
        }

        private boolean drawsMovingStack() {
            return settleTicks <= 0;
        }

        private boolean done() {
            return settleTicks > 4;
        }
    }
}
