package com.xerion.forge.game;

import com.mojang.blaze3d.platform.InputConstants;
import com.xerion.common.game.GameActions;
import com.xerion.common.game.GameBlockPos;
import com.xerion.common.game.GameBlockState;
import com.xerion.common.game.GameBox;
import com.xerion.common.game.GameClient;
import com.xerion.common.game.GameEntity;
import com.xerion.common.game.GameInput;
import com.xerion.common.game.GameInventory;
import com.xerion.common.game.GameItemStack;
import com.xerion.common.game.GameLivingEntity;
import com.xerion.common.game.GameNetwork;
import com.xerion.common.game.GamePlayer;
import com.xerion.common.game.GameRenderer;
import com.xerion.common.game.GameRotation;
import com.xerion.common.game.GameScreen;
import com.xerion.common.game.GameVec3;
import com.xerion.common.game.GameWorld;
import com.xerion.common.platform.PlatformGame;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public final class ForgePlatformGame implements PlatformGame {
    private final Minecraft minecraft;
    private final GameClient client;

    public ForgePlatformGame() {
        this.minecraft = Minecraft.getInstance();
        this.client = new ForgeGameClient();
    }

    @Override
    public GameClient client() {
        return client;
    }

    private final class ForgeGameClient implements GameClient {
        private final GameInput input = new ForgeGameInput();
        private final GameRenderer renderer = new ForgeGameRenderer();
        private final GameNetwork network = new ForgeGameNetwork();
        private final GameActions actions = new ForgeGameActions();

        @Override
        public Optional<GamePlayer> player() {
            return Optional.ofNullable(minecraft.player).map(ForgeGamePlayer::new);
        }

        @Override
        public Optional<GameWorld> world() {
            return Optional.ofNullable(minecraft.level).map(ForgeGameWorld::new);
        }

        @Override
        public Optional<GameScreen> screen() {
            return Optional.ofNullable(minecraft.screen).map(ForgeGameScreen::new);
        }

        @Override
        public GameInput input() {
            return input;
        }

        @Override
        public GameRenderer renderer() {
            return renderer;
        }

        @Override
        public GameNetwork network() {
            return network;
        }

        @Override
        public GameActions actions() {
            return actions;
        }
    }

    private final class ForgeGameWorld implements GameWorld {
        private final ClientLevel level;

        private ForgeGameWorld(ClientLevel level) {
            this.level = level;
        }

        @Override
        public long gameTime() {
            return level.getGameTime();
        }

        @Override
        public int minBuildHeight() {
            return level.getMinBuildHeight();
        }

        @Override
        public int maxBuildHeight() {
            return level.getMaxBuildHeight();
        }

        @Override
        public List<GameEntity> entities() {
            List<GameEntity> entities = new ArrayList<>();
            for (Entity entity : level.entitiesForRendering()) {
                entities.add(wrapEntity(entity));
            }
            return entities;
        }

        @Override
        public Optional<GameEntity> entityById(int entityId) {
            return Optional.ofNullable(level.getEntity(entityId)).map(ForgePlatformGame.this::wrapEntity);
        }

        @Override
        public GameBlockState blockState(GameBlockPos pos) {
            BlockPos blockPos = new BlockPos(pos.x(), pos.y(), pos.z());
            BlockState state = level.getBlockState(blockPos);
            return new GameBlockState(
                    BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(),
                    state.isAir(),
                    !state.getFluidState().isEmpty(),
                    state.canBeReplaced(),
                    state.getDestroySpeed(level, blockPos)
            );
        }
    }

    private GameEntity wrapEntity(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            return new ForgeGameLivingEntity(livingEntity);
        }
        return new ForgeGameEntity(entity);
    }

    private class ForgeGameEntity implements GameEntity {
        protected final Entity entity;

        private ForgeGameEntity(Entity entity) {
            this.entity = entity;
        }

        @Override
        public int id() {
            return entity.getId();
        }

        @Override
        public String typeId() {
            return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        }

        @Override
        public String displayName() {
            return entity.getDisplayName().getString();
        }

        @Override
        public GameVec3 position() {
            return vec(entity.position());
        }

        @Override
        public GameVec3 velocity() {
            return vec(entity.getDeltaMovement());
        }

        @Override
        public GameBox boundingBox() {
            return box(entity.getBoundingBox());
        }

        @Override
        public boolean removed() {
            return entity.isRemoved();
        }

        @Override
        public boolean invisible() {
            return entity.isInvisible();
        }
    }

    private class ForgeGameLivingEntity extends ForgeGameEntity implements GameLivingEntity {
        protected final LivingEntity livingEntity;

        private ForgeGameLivingEntity(LivingEntity livingEntity) {
            super(livingEntity);
            this.livingEntity = livingEntity;
        }

        @Override
        public float health() {
            return livingEntity.getHealth();
        }

        @Override
        public float maxHealth() {
            return livingEntity.getMaxHealth();
        }

        @Override
        public int armorValue() {
            return livingEntity.getArmorValue();
        }

        @Override
        public boolean alive() {
            return livingEntity.isAlive();
        }

        @Override
        public boolean teammate() {
            LocalPlayer player = minecraft.player;
            return player != null && livingEntity.isAlliedTo(player);
        }
    }

    private final class ForgeGamePlayer extends ForgeGameLivingEntity implements GamePlayer {
        private final LocalPlayer player;

        private ForgeGamePlayer(LocalPlayer player) {
            super(player);
            this.player = player;
        }

        @Override
        public GameVec3 eyePosition() {
            return vec(player.getEyePosition());
        }

        @Override
        public GameRotation rotation() {
            return new GameRotation(player.getYRot(), player.getXRot());
        }

        @Override
        public GameInventory inventory() {
            return new ForgeGameInventory(player);
        }

        @Override
        public boolean onGround() {
            return player.onGround();
        }

        @Override
        public boolean sprinting() {
            return player.isSprinting();
        }

        @Override
        public boolean sneaking() {
            return player.isShiftKeyDown();
        }

        @Override
        public float attackCooldown(float partialTicks) {
            return player.getAttackStrengthScale(partialTicks);
        }
    }

    private static final class ForgeGameInventory implements GameInventory {
        private final LocalPlayer player;

        private ForgeGameInventory(LocalPlayer player) {
            this.player = player;
        }

        @Override
        public List<GameItemStack> slots() {
            List<GameItemStack> slots = new ArrayList<>();
            for (ItemStack stack : player.getInventory().items) {
                slots.add(itemStack(stack));
            }
            return slots;
        }

        @Override
        public int selectedHotbarSlot() {
            return player.getInventory().selected;
        }

        @Override
        public Optional<GameItemStack> slot(int index) {
            if (index < 0 || index >= player.getInventory().items.size()) {
                return Optional.empty();
            }
            return Optional.of(itemStack(player.getInventory().items.get(index)));
        }

        @Override
        public Optional<GameItemStack> offhand() {
            if (player.getInventory().offhand.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(itemStack(player.getInventory().offhand.get(0)));
        }
    }

    private final class ForgeGameScreen implements GameScreen {
        private final net.minecraft.client.gui.screens.Screen screen;

        private ForgeGameScreen(net.minecraft.client.gui.screens.Screen screen) {
            this.screen = screen;
        }

        @Override
        public String title() {
            return screen.getTitle().getString();
        }

        @Override
        public boolean pauseScreen() {
            return screen.isPauseScreen();
        }

        @Override
        public boolean containerScreen() {
            return screen instanceof AbstractContainerScreen<?>;
        }
    }

    private final class ForgeGameInput implements GameInput {
        @Override
        public boolean keyDown(int keyCode) {
            return minecraft.getWindow() != null && InputConstants.isKeyDown(minecraft.getWindow().getWindow(), keyCode);
        }

        @Override
        public boolean mouseDown(int button) {
            return minecraft.getWindow() != null && GLFW.glfwGetMouseButton(minecraft.getWindow().getWindow(), button) == GLFW.GLFW_PRESS;
        }

        @Override
        public float forwardImpulse() {
            return minecraft.player != null ? minecraft.player.input.forwardImpulse : 0.0F;
        }

        @Override
        public float leftImpulse() {
            return minecraft.player != null ? minecraft.player.input.leftImpulse : 0.0F;
        }

        @Override
        public boolean jumping() {
            return minecraft.player != null && minecraft.player.input.jumping;
        }

        @Override
        public boolean shiftKeyDown() {
            return minecraft.player != null && minecraft.player.input.shiftKeyDown;
        }
    }

    private final class ForgeGameRenderer implements GameRenderer {
        @Override
        public int windowWidth() {
            return minecraft.getWindow().getGuiScaledWidth();
        }

        @Override
        public int windowHeight() {
            return minecraft.getWindow().getGuiScaledHeight();
        }

        @Override
        public double guiScale() {
            return minecraft.getWindow().getGuiScale();
        }

        @Override
        public GameVec3 cameraPosition() {
            return vec(camera().getPosition());
        }

        @Override
        public GameRotation cameraRotation() {
            Camera camera = camera();
            return new GameRotation(camera.getYRot(), camera.getXRot());
        }

        private Camera camera() {
            return minecraft.gameRenderer.getMainCamera();
        }
    }

    private final class ForgeGameNetwork implements GameNetwork {
        @Override
        public boolean connected() {
            return minecraft.getConnection() != null;
        }

        @Override
        public void sendClientCommand(String command) {
            if (minecraft.player == null || minecraft.player.connection == null || command == null || command.isBlank()) {
                return;
            }
            String normalized = command.startsWith("/") ? command.substring(1) : command;
            minecraft.player.connection.sendCommand(normalized);
        }
    }

    private final class ForgeGameActions implements GameActions {
        @Override
        public void attackEntity(int entityId) {
            if (minecraft.level == null || minecraft.player == null || minecraft.gameMode == null) {
                return;
            }
            Entity entity = minecraft.level.getEntity(entityId);
            if (entity == null) {
                return;
            }
            minecraft.gameMode.attack(minecraft.player, entity);
            minecraft.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        }

        @Override
        public void useSelectedItem() {
            if (minecraft.player == null || minecraft.gameMode == null) {
                return;
            }
            minecraft.gameMode.useItem(minecraft.player, net.minecraft.world.InteractionHand.MAIN_HAND);
        }

        @Override
        public void swapToHotbarSlot(int slot) {
            if (minecraft.player == null || slot < 0 || slot > 8) {
                return;
            }
            minecraft.player.getInventory().selected = slot;
        }

        @Override
        public void clickInventorySlot(int slot, int button, GameActions.ClickType type) {
            if (minecraft.player == null || minecraft.gameMode == null || type == null) {
                return;
            }
            minecraft.gameMode.handleInventoryMouseClick(
                    minecraft.player.containerMenu.containerId,
                    slot,
                    button,
                    clickType(type),
                    minecraft.player
            );
        }

        @Override
        public void rotate(GameRotation rotation) {
            if (minecraft.player == null || rotation == null) {
                return;
            }
            minecraft.player.setYRot(rotation.yaw());
            minecraft.player.setXRot(rotation.pitch());
            minecraft.player.setYHeadRot(rotation.yaw());
        }

        @Override
        public void setMotion(GameVec3 motion) {
            if (minecraft.player == null || motion == null) {
                return;
            }
            minecraft.player.setDeltaMovement(new Vec3(motion.x(), motion.y(), motion.z()));
        }

        private net.minecraft.world.inventory.ClickType clickType(GameActions.ClickType type) {
            return switch (type) {
                case PICKUP -> net.minecraft.world.inventory.ClickType.PICKUP;
                case QUICK_MOVE -> net.minecraft.world.inventory.ClickType.QUICK_MOVE;
                case SWAP -> net.minecraft.world.inventory.ClickType.SWAP;
                case THROW -> net.minecraft.world.inventory.ClickType.THROW;
                case QUICK_CRAFT -> net.minecraft.world.inventory.ClickType.QUICK_CRAFT;
                case PICKUP_ALL -> net.minecraft.world.inventory.ClickType.PICKUP_ALL;
            };
        }
    }

    private static GameVec3 vec(Vec3 vec) {
        return new GameVec3(vec.x, vec.y, vec.z);
    }

    private static GameBox box(AABB box) {
        return new GameBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    private static GameItemStack itemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return new GameItemStack("", 0, 0, 0, 0, Set.of());
        }
        return new GameItemStack(
                BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),
                stack.getCount(),
                stack.getMaxStackSize(),
                stack.getDamageValue(),
                stack.getMaxDamage(),
                Set.of()
        );
    }
}
