package com.xerion.forge.movement;

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

import com.xerion.forge.config.VisualSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FungusBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class ScaffoldService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final double REACH = 4.5D;
    private static final List<net.minecraft.world.level.block.Block> BLACKLISTED_BLOCKS = List.of(
            Blocks.AIR,
            Blocks.WATER,
            Blocks.LAVA,
            Blocks.ENCHANTING_TABLE,
            Blocks.GLASS_PANE,
            Blocks.IRON_BARS,
            Blocks.SNOW,
            Blocks.COAL_ORE,
            Blocks.DIAMOND_ORE,
            Blocks.EMERALD_ORE,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.TORCH,
            Blocks.ANVIL,
            Blocks.NOTE_BLOCK,
            Blocks.JUKEBOX,
            Blocks.TNT,
            Blocks.GOLD_ORE,
            Blocks.IRON_ORE,
            Blocks.LAPIS_ORE,
            Blocks.STONE_PRESSURE_PLATE,
            Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Blocks.STONE_BUTTON,
            Blocks.LEVER,
            Blocks.TALL_GRASS,
            Blocks.TRIPWIRE,
            Blocks.TRIPWIRE_HOOK,
            Blocks.RAIL,
            Blocks.CORNFLOWER,
            Blocks.RED_MUSHROOM,
            Blocks.BROWN_MUSHROOM,
            Blocks.VINE,
            Blocks.SUNFLOWER,
            Blocks.LADDER,
            Blocks.FURNACE,
            Blocks.SAND,
            Blocks.CACTUS,
            Blocks.DISPENSER,
            Blocks.DROPPER,
            Blocks.CRAFTING_TABLE,
            Blocks.COBWEB,
            Blocks.PUMPKIN,
            Blocks.COBBLESTONE_WALL,
            Blocks.OAK_FENCE,
            Blocks.REDSTONE_TORCH,
            Blocks.FLOWER_POT
    );

    private int airTicks;
    private int yLevel;
    private BlockPos blockPos;
    private Direction direction;
    private ScaffoldPlacementMath.Rot rotation;
    private int blockSlot = -1;
    private int previousSlot = -1;
    private boolean shouldSwapBack;
    private boolean forcingShift;

    // Heypixel / Hypixel mode state.
    private int heyAirTick;
    private float heyBaseYaw;
    private float heyForwardYaw;
    private ScaffoldPlacementMath.Rot heyPacketRotation;
    private int heyPacketRotationTicks;
    private int heyDuplicateRotNonce;

    public void tick() {
        if (!VisualSettings.scaffoldEnabled()) {
            reset();
            return;
        }

        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (player == null || level == null || MC.gameMode == null || MC.getConnection() == null) {
            reset();
            return;
        }
        if (MC.screen != null || player.isPassenger() || player.isSpectator() || player.getAbilities().flying) {
            reset();
            return;
        }

        if ("SilenceTelly".equals(VisualSettings.scaffoldMode())) {
            ScaffoldSilenceTellyService.tick();
            return;
        }
        ScaffoldSilenceTellyService.dismissIfNeeded();

        updateGodBridgeSafeWalk(level, player);
        blockSlot = findBlockSlot(player);
        if (blockSlot < 0) {
            restoreShiftKey();
            ScaffoldRotation.clear();
            return;
        }

        if (player.onGround()) {
            airTicks = 0;
            yLevel = Mth.floor(player.getY()) - 1;
        } else {
            airTicks++;
        }

        String mode = VisualSettings.scaffoldMode();
        if ("Heypixel".equals(mode) || "Hypixel".equals(mode)) {
            handleHeypixel(level, player, "Hypixel".equals(mode));
            return;
        }

        getBlockInfo(level, player);

        if ("TellyBridge".equals(mode)) {
            handleTellyBridge(level, player);
        } else {
            // GodBridge plus the core-only anti-cheat modes (Vanilla, Bloxd,
            // AntiGamingChair, Watchdog) share the straight placement path.
            handleGodBridge(level, player);
        }
    }

    private void reset() {
        ScaffoldSilenceTellyService.dismissIfNeeded();
        airTicks = 0;
        yLevel = 0;
        blockPos = null;
        direction = null;
        rotation = null;
        blockSlot = -1;
        heyAirTick = 0;
        heyBaseYaw = 0.0F;
        heyForwardYaw = 0.0F;
        heyPacketRotation = null;
        heyPacketRotationTicks = 0;
        heyDuplicateRotNonce = 0;
        if (shouldSwapBack && MC.player != null && previousSlot >= 0) {
            HotbarSlotActions.select(MC.player, previousSlot);
        }
        previousSlot = -1;
        shouldSwapBack = false;
        restoreShiftKey();
        ScaffoldRotation.clear();
    }

    private void handleTellyBridge(ClientLevel level, LocalPlayer player) {
        if (player.onGround()) {
            if (rotation != null) {
                ScaffoldPlacementMath.Rot back = ScaffoldPlacementMath.smoothRotation(rotation,
                        new ScaffoldPlacementMath.Rot(player.getYRot(), rotation.pitch()),
                        VisualSettings.scaffoldRotationBackSpeed());
                rotation = back;
                ScaffoldRotation.request(back.yaw(), back.pitch(), false);
            } else {
                ScaffoldRotation.clear();
            }
            return;
        }

        ScaffoldPlacementMath.Rot target = ScaffoldPlacementMath.placementRotation(level, player, rotation, blockPos,
                direction, REACH, onAir(level, player));
        double speed = VisualSettings.scaffoldRotationSpeed();
        if ("Hypixel".equals(VisualSettings.scaffoldRotationMode())) {
            speed = airTicks <= 1 ? 7.055D : 1.944D;
        }
        rotation = ScaffoldPlacementMath.smoothRotation(rotation, target, speed);
        if (rotation != null) {
            ScaffoldRotation.request(rotation.yaw(), rotation.pitch(), true);
        }

        if (airTicks > VisualSettings.scaffoldTellyTicks()) {
            place(level, player);
        }
    }

    private void handleGodBridge(ClientLevel level, LocalPlayer player) {
        if (onAir(level, player) || !VisualSettings.scaffoldSnap() || rotation == null) {
            ScaffoldPlacementMath.Rot target = ScaffoldPlacementMath.placementRotation(level, player, rotation, blockPos,
                    direction, REACH, onAir(level, player));
            double speed = "Hypixel".equals(VisualSettings.scaffoldRotationMode())
                    ? 1.944D
                    : VisualSettings.scaffoldRotationSpeed();
            rotation = ScaffoldPlacementMath.smoothRotation(rotation, target, speed);
        }
        if (rotation != null) {
            ScaffoldRotation.request(rotation.yaw(), rotation.pitch(), true);
        } else {
            ScaffoldRotation.clear();
        }
        place(level, player);
    }

    private void place(ClientLevel level, LocalPlayer player) {
        if (!onAir(level, player) || blockPos == null || direction == null || rotation == null) {
            return;
        }
        if (!ScaffoldPlacementMath.overBlock(level, player, rotation, blockPos, direction, REACH)) {
            return;
        }

        int selectedSlot = player.getInventory().selected;
        if (selectedSlot != blockSlot) {
            if ("None".equals(VisualSettings.scaffoldSwapMode())) {
                return;
            }
            if (previousSlot == -1) {
                previousSlot = selectedSlot;
            }
            HotbarSlotActions.select(player, blockSlot);
            if ("Normal".equals(VisualSettings.scaffoldSwapMode()) && VisualSettings.scaffoldSwapBack()) {
                shouldSwapBack = true;
            }
        }

        if (!isValidStack(player.getInventory().getSelected())) {
            silentSwapBack(player);
            return;
        }

        BlockHitResult hit = new BlockHitResult(ScaffoldPlacementMath.hitVec(blockPos, direction), direction, blockPos, false);
        if (VisualSettings.scaffoldInteractBeforePlace()) {
            MC.gameMode.useItem(player, InteractionHand.MAIN_HAND);
        }
        InteractionResult result = MC.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
        if (result.consumesAction()) {
            handleTower(player, hit);
            if (VisualSettings.scaffoldSwingHand()) {
                player.swing(InteractionHand.MAIN_HAND);
            } else {
                MC.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            }
        }
        silentSwapBack(player);
    }

    public void tickEnd() {
        if (!VisualSettings.scaffoldEnabled() || !"Bloxd".equals(VisualSettings.scaffoldMode())) {
            return;
        }
        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (player == null || level == null || player.isPassenger() || player.isSpectator()) {
            return;
        }
        if (!level.noCollision(player, player.getBoundingBox().deflate(0.005D, 0.0D, 0.005D))) {
            player.setPos(player.getX(), player.getY() + 2.0D, player.getZ());
        }
    }

    private void handleTower(LocalPlayer player, BlockHitResult hit) {
        if (!VisualSettings.scaffoldTower()
                || player.hasEffect(MobEffects.JUMP)
                || MC.options.keyUse.isDown()
                || !MC.options.keyJump.isDown()) {
            return;
        }
        switch (VisualSettings.scaffoldMode()) {
            case "Vanilla", "Watchdog" -> player.jumpFromGround();
            case "AntiGamingChair" -> {
                Vec3 velocity = player.getDeltaMovement();
                player.setDeltaMovement(velocity.x, 0.42D, velocity.z);
                player.hurtMarked = true;
            }
            case "Bloxd" -> MC.getConnection().send(new ServerboundUseItemOnPacket(
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(hit.getLocation(), hit.getDirection(), hit.getBlockPos().above(), hit.isInside()),
                    0));
            default -> {
            }
        }
    }

    private int getYLevel(LocalPlayer player) {
        if (!MC.options.keyJump.isDown() && isMoving(player) && player.fallDistance <= 0.25F
                && "TellyBridge".equals(VisualSettings.scaffoldMode())) {
            return yLevel;
        }
        return Mth.floor(player.getY()) - 1;
    }

    private void handleHeypixel(ClientLevel level, LocalPlayer player, boolean hypixel) {
        boolean telly = hypixel || VisualSettings.scaffoldTelly();
        int tellyTick = hypixel ? 5 : VisualSettings.scaffoldTellyTicks();
        float rotateSpeed = hypixel ? 180.0F : VisualSettings.scaffoldRotationSpeed();
        float rotateBackSpeed = hypixel ? 180.0F : VisualSettings.scaffoldRotationBackSpeed();

        if (player.onGround()) {
            heyAirTick = 0;
            yLevel = Mth.floor(player.getY()) - 1;
            blockPos = null;
            direction = null;
            heyBaseYaw = player.getYRot();
            if (telly) {
                heyForwardYaw = hypixel && isMoving(player) ? movementYaw(player) : heyBaseYaw;
                applyHeypixelRotation(new ScaffoldPlacementMath.Rot(heyForwardYaw, player.getXRot()), rotateBackSpeed);
            }
            return;
        }

        tickHeypixelPacketRotation();

        if (hypixel && isMoving(player)) {
            heyForwardYaw = movementYaw(player);
        }

        boolean upTelly = telly
                && !player.horizontalCollision
                && isMoving(player)
                && (MC.options.keyJump.isDown() || player.getDeltaMovement().y > -0.08D);
        int dynamicTellyTick = upTelly
                ? Math.max(1, tellyTick - (isDiagonalYaw(heyForwardYaw) ? 3 : 2))
                : tellyTick;

        getBlockInfo(level, player, upTelly ? 1 : 0);

        if (telly && heyAirTick < dynamicTellyTick) {
            applyHeypixelRotation(new ScaffoldPlacementMath.Rot(heyForwardYaw, player.getXRot()), rotateBackSpeed);
            if (upTelly && isDiagonalYaw(heyForwardYaw) && heyAirTick >= 1
                    && blockPos != null && direction != null) {
                placeHeypixel(level, player);
            }
            heyAirTick++;
            return;
        }

        if (!telly && blockPos == null) {
            applyHeypixelRotation(new ScaffoldPlacementMath.Rot(
                    Mth.wrapDegrees(heyBaseYaw - 180.0F), 89.64F), rotateSpeed);
        }
        if (telly || onAir(level, player) || !VisualSettings.scaffoldSnap()) {
            placeHeypixel(level, player);
        } else {
            place(level, player);
        }
        heyAirTick++;
    }

    private void placeHeypixel(ClientLevel level, LocalPlayer player) {
        if (blockPos == null || direction == null) {
            return;
        }
        ScaffoldPlacementMath.Rot target = ScaffoldPlacementMath.placementRotation(
                level, player, rotation, blockPos, direction, REACH, onAir(level, player));
        if (target == null) {
            return;
        }
        target = applyDuplicateRotJitter(level, player, target);
        rotation = target;
        requestHeypixelPacketRotation(target);
        place(level, player);
    }

    private void applyHeypixelRotation(ScaffoldPlacementMath.Rot target, float speed) {
        if (target == null) {
            return;
        }
        ScaffoldPlacementMath.Rot from = rotation != null
                ? rotation
                : new ScaffoldPlacementMath.Rot(MC.player.getYRot(), MC.player.getXRot());
        ScaffoldPlacementMath.Rot smoothed = ScaffoldPlacementMath.smoothRotation(from, target, speed);
        if (smoothed == null) {
            smoothed = target;
        }
        rotation = smoothed;
        requestHeypixelPacketRotation(smoothed);
    }

    private void requestHeypixelPacketRotation(ScaffoldPlacementMath.Rot rot) {
        if (rot == null || MC.player == null) {
            return;
        }
        ScaffoldRotation.requestPacket(rot.yaw(), rot.pitch(), true);
        heyPacketRotation = rot;
        heyPacketRotationTicks = 2;
    }

    private void tickHeypixelPacketRotation() {
        if (heyPacketRotation != null && heyPacketRotationTicks > 0) {
            ScaffoldRotation.requestPacket(heyPacketRotation.yaw(), heyPacketRotation.pitch(), true);
            heyPacketRotationTicks--;
        }
    }

    private ScaffoldPlacementMath.Rot applyDuplicateRotJitter(ClientLevel level, LocalPlayer player,
                                                              ScaffoldPlacementMath.Rot base) {
        if (!VisualSettings.scaffoldDuplicateRotPlace() || base == null) {
            return base;
        }
        heyDuplicateRotNonce++;
        int index = Math.floorMod(heyDuplicateRotNonce, 6);
        float yawJitter = switch (index) {
            case 0 -> 0.032F;
            case 1 -> -0.037F;
            case 2 -> 0.021F;
            case 3 -> -0.026F;
            case 4 -> 0.014F;
            default -> -0.018F;
        };
        float pitchJitter = index % 2 == 0 ? 0.012F : -0.009F;
        ScaffoldPlacementMath.Rot jittered = new ScaffoldPlacementMath.Rot(
                Mth.wrapDegrees(base.yaw() + yawJitter),
                Mth.clamp(base.pitch() + pitchJitter, -89.9F, 89.9F));
        return ScaffoldPlacementMath.overBlock(level, player, jittered, blockPos, direction, REACH)
                ? jittered
                : base;
    }

    private static boolean isDiagonalYaw(float yaw) {
        double radians = Math.toRadians(Mth.wrapDegrees(yaw));
        return Math.abs(Math.sin(radians * 2.0D)) > 0.58D;
    }

    private static float movementYaw(LocalPlayer player) {
        if (player.input == null) {
            return player.getYRot();
        }
        float forward = player.input.forwardImpulse;
        float sideways = player.input.leftImpulse;
        float yaw = player.getYRot();
        if (forward < 0.0F) {
            yaw += 180.0F;
        }
        float factor = 1.0F;
        if (forward < 0.0F) {
            factor = -0.5F;
        } else if (forward > 0.0F) {
            factor = 0.5F;
        }
        if (sideways > 0.0F) {
            yaw -= 90.0F * factor;
        }
        if (sideways < 0.0F) {
            yaw += 90.0F * factor;
        }
        return yaw;
    }

    private void getBlockInfo(ClientLevel level, LocalPlayer player) {
        getBlockInfo(level, player, 0);
    }

    private void getBlockInfo(ClientLevel level, LocalPlayer player, int yTopBoost) {
        blockPos = null;
        direction = null;

        int baseY = getYLevel(player) + yTopBoost;
        Vec3 baseVec = player.getEyePosition();
        BlockPos base = BlockPos.containing(baseVec.x, baseY, baseVec.z);
        int baseX = base.getX();
        int baseZ = base.getZ();

        if (!onAir(level, player)) {
            return;
        }
        if (checkBlock(level, player, baseVec, base, baseY)) {
            return;
        }

        for (int d = 1; d <= 6; d++) {
            if (checkBlock(level, player, baseVec, new BlockPos(baseX, baseY - d, baseZ), baseY)) {
                return;
            }

            for (int x = 0; x <= d; x++) {
                for (int z = 0; z <= d - x; z++) {
                    int y = d - x - z;
                    for (int rev1 = 0; rev1 <= 1; rev1++) {
                        for (int rev2 = 0; rev2 <= 1; rev2++) {
                            BlockPos pos = new BlockPos(
                                    baseX + (rev1 == 0 ? x : -x),
                                    baseY - y,
                                    baseZ + (rev2 == 0 ? z : -z)
                            );
                            if (checkBlock(level, player, baseVec, pos, baseY)) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean checkBlock(ClientLevel level, LocalPlayer player, Vec3 baseVec, BlockPos pos, int maxY) {
        if (!onAir(level, player) || pos.getY() > maxY || !level.getBlockState(pos).canBeReplaced()) {
            return false;
        }

        for (Direction dir : Direction.values()) {
            BlockPos baseBlockPos = pos.relative(dir);
            if (!isSolidAndNonInteractive(level.getBlockState(baseBlockPos), level, baseBlockPos)) {
                continue;
            }

            Vec3 normal = Vec3.atLowerCornerOf(dir.getNormal());
            Vec3 relevant = Vec3.atBottomCenterOf(pos).relative(dir, 0.5).subtract(baseVec);
            Direction placeDirection = dir.getOpposite();
            if (relevant.lengthSqr() > REACH * REACH || relevant.dot(normal) < 0.0D) {
                continue;
            }
            if (placeDirection == Direction.UP && isMoving(player) && !MC.options.keyJump.isDown()) {
                continue;
            }

            blockPos = baseBlockPos;
            direction = placeDirection;
            return true;
        }

        return false;
    }

    private boolean isSolidAndNonInteractive(BlockState state, Level level, BlockPos pos) {
        return !state.getCollisionShape(level, pos).isEmpty() && state.getMenuProvider(level, pos) == null;
    }

    private boolean onAir(ClientLevel level, LocalPlayer player) {
        Vec3 baseVec = player.getEyePosition();
        BlockPos base = BlockPos.containing(baseVec.x, getYLevel(player), baseVec.z);
        return level.getBlockState(base).canBeReplaced();
    }

    private int findBlockSlot(LocalPlayer player) {
        if ("None".equals(VisualSettings.scaffoldSwapMode())) {
            return isValidStack(player.getInventory().getSelected()) ? player.getInventory().selected : -1;
        }
        int bestSlot = -1;
        int bestCount = -1;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!isValidStack(stack)) {
                continue;
            }
            if (stack.getCount() > bestCount) {
                bestSlot = slot;
                bestCount = stack.getCount();
            }
        }
        return bestSlot;
    }

    private boolean isValidStack(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        if (stack.getItem() instanceof StandingAndWallBlockItem) {
            return false;
        }

        String name = stack.getDisplayName().getString();
        if (name.contains("Click") || name.contains("点击")) {
            return false;
        }

        var block = blockItem.getBlock();
        if (block instanceof FlowerBlock || block instanceof BushBlock || block instanceof FungusBlock || block instanceof CropBlock) {
            return false;
        }

        return !(block instanceof SlabBlock) && !BLACKLISTED_BLOCKS.contains(block);
    }

    private void silentSwapBack(LocalPlayer player) {
        if ("Silent".equals(VisualSettings.scaffoldSwapMode()) && previousSlot >= 0) {
            HotbarSlotActions.select(player, previousSlot);
            previousSlot = -1;
        }
    }

    private void updateGodBridgeSafeWalk(ClientLevel level, LocalPlayer player) {
        String mode = VisualSettings.scaffoldMode();
        boolean supported = "GodBridge".equals(mode)
                || "Heypixel".equals(mode) && !VisualSettings.scaffoldTelly();
        if (!supported || !VisualSettings.scaffoldSafeWalk()) {
            restoreShiftKey();
            return;
        }
        boolean shouldSneak = player.onGround() && isOnBlockEdge(level, player, 0.3D);
        MC.options.keyShift.setDown(shouldSneak || MenuMovementService.keyDown(MC, MC.options.keyShift));
        forcingShift = shouldSneak;
    }

    private void restoreShiftKey() {
        if (!forcingShift) {
            return;
        }
        MC.options.keyShift.setDown(MenuMovementService.keyDown(MC, MC.options.keyShift));
        forcingShift = false;
    }

    private boolean isOnBlockEdge(ClientLevel level, LocalPlayer player, double distance) {
        return noGround(level, player, distance, 0.0D)
                || noGround(level, player, -distance, 0.0D)
                || noGround(level, player, 0.0D, distance)
                || noGround(level, player, 0.0D, -distance);
    }

    private boolean noGround(ClientLevel level, LocalPlayer player, double x, double z) {
        return level.noCollision(player, player.getBoundingBox().move(x, -0.08D, z));
    }

    private boolean isMoving(LocalPlayer player) {
        return player.input != null
                && (Math.abs(player.input.forwardImpulse) > 1.0E-5F || Math.abs(player.input.leftImpulse) > 1.0E-5F);
    }

}
