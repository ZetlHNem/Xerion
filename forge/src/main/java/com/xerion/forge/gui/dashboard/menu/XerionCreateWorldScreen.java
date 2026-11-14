package com.xerion.forge.gui.dashboard;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.xerion.forge.mixin.CreateWorldScreenInvoker;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModLoader;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class XerionCreateWorldScreen extends Screen {
    private static final int PANEL_MAX_W = 620;
    private static final int PANEL_MAX_H = 330;
    private static final int PANEL_MARGIN = 16;
    private static final int HEADER_H = 58;
    private static final int TAB_H = 30;
    private static final int FOOTER_H = 54;
    private static final int FIELD_H = 28;
    private static final Map<CreateWorldScreen, CreateTab> RETAINED_TABS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private final CreateWorldScreen delegate;
    private final WorldCreationUiState state;
    private final List<AbstractWidget> gameWidgets = new ArrayList<>();
    private final List<AbstractWidget> worldWidgets = new ArrayList<>();
    private final List<AbstractWidget> advancedWidgets = new ArrayList<>();
    private final List<TabButton> tabButtons = new ArrayList<>();
    private CreateTab selectedTab = CreateTab.GAME;
    private EditBox nameBox;
    private EditBox seedBox;
    private SettingButton gameModeButton;
    private SettingButton difficultyButton;
    private SettingButton worldTypeButton;
    private ToggleButton allowCheatsButton;
    private ToggleButton structuresButton;
    private ToggleButton bonusChestButton;
    private CommandButton customizeButton;
    private ActionButton createButton;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int contentX;
    private int contentY;
    private int contentW;
    private int nameFieldY;
    private int seedFieldY;
    private float screenAlpha;

    public XerionCreateWorldScreen(CreateWorldScreen delegate) {
        super(Component.translatable("selectWorld.create"));
        this.delegate = delegate;
        this.state = delegate.getUiState();
        this.selectedTab = RETAINED_TABS.getOrDefault(delegate, CreateTab.GAME);
    }

    public static void openFresh(Minecraft minecraft, Screen lastScreen) {
        if (!VisualSettings.modernUiEnabled()) {
            CreateWorldScreen.openFresh(minecraft, lastScreen);
            return;
        }
        minecraft.setScreen(new XerionLoadingScreen(Component.translatable("createWorld.preparing"),
                () -> startFreshLoad(minecraft, lastScreen)));
    }

    private static void startFreshLoad(Minecraft minecraft, Screen lastScreen) {
        PackRepository repository = new PackRepository(new ServerPacksSource());
        ModLoader.get().postEvent(new AddPackFindersEvent(PackType.SERVER_DATA, repository::addPackFinder));
        WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(
                repository, WorldDataConfiguration.DEFAULT, false, true);
        WorldLoader.InitConfig initConfig = new WorldLoader.InitConfig(
                packConfig, Commands.CommandSelection.INTEGRATED, 2);
        CompletableFuture<WorldCreationContext> future = WorldLoader.load(
                initConfig,
                data -> new WorldLoader.DataLoadOutput<>(
                        new FreshWorldData(
                                new WorldGenSettings(
                                        WorldOptions.defaultWithRandomSeed(),
                                        WorldPresets.createNormalWorldDimensions(data.datapackWorldgen())),
                                data.dataConfiguration()),
                        data.datapackDimensions()),
                (resources, reloadableResources, registries, cookie) -> {
                    resources.close();
                    return new WorldCreationContext(
                            cookie.settings(), registries, reloadableResources, cookie.configuration());
                },
                Util.backgroundExecutor(), minecraft);
        future.whenComplete((context, throwable) -> minecraft.execute(() -> {
            if (throwable != null) {
                Throwable cause = throwable instanceof CompletionException && throwable.getCause() != null
                        ? throwable.getCause() : throwable;
                minecraft.delayCrash(CrashReport.forThrowable(
                        cause, "Couldn't prepare world creation data"));
                return;
            }
            minecraft.setScreen(createFreshDelegate(
                    minecraft, lastScreen, context, Optional.of(WorldPresets.NORMAL), OptionalLong.empty()));
        }));
    }

    private static CreateWorldScreen createFreshDelegate(Minecraft minecraft, Screen lastScreen,
                                                          WorldCreationContext context,
                                                          Optional<ResourceKey<WorldPreset>> preset,
                                                          OptionalLong seed) {
        try {
            Constructor<CreateWorldScreen> constructor = CreateWorldScreen.class.getDeclaredConstructor(
                    Minecraft.class, Screen.class, WorldCreationContext.class, Optional.class, OptionalLong.class);
            constructor.setAccessible(true);
            return constructor.newInstance(minecraft, lastScreen, context, preset, seed);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to create the world creation screen", exception);
        }
    }

    @Override
    protected void init() {
        delegate.init(minecraft, width, height);
        gameWidgets.clear();
        worldWidgets.clear();
        advancedWidgets.clear();
        tabButtons.clear();

        panelW = Math.min(PANEL_MAX_W, Math.max(320, width - PANEL_MARGIN * 2));
        panelH = Math.min(PANEL_MAX_H, Math.max(276, height - PANEL_MARGIN * 2));
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        contentX = panelX + 22;
        contentY = panelY + HEADER_H + TAB_H + 12;
        contentW = panelW - 44;

        buildTabs();
        buildGameTab();
        buildWorldTab();
        buildAdvancedTab();
        buildFooter();
        setSelectedTab(selectedTab);
        setInitialFocus(nameBox);
        syncControlStates();
    }

    private void buildTabs() {
        int x = panelX + 18;
        int y = panelY + HEADER_H;
        int tabW = 96;
        int gap = 4;
        for (CreateTab tab : CreateTab.values()) {
            TabButton button = new TabButton(x, y, tabW, 26, tab, () -> setSelectedTab(tab));
            tabButtons.add(button);
            addRenderableWidget(button);
            x += tabW + gap;
        }
    }

    private void buildGameTab() {
        nameFieldY = contentY + 15;
        nameBox = new AlignedEditBox(font, contentX + 10, nameFieldY,
                contentW - 20, 28, Component.translatable("selectWorld.enterName"), 9);
        nameBox.setBordered(false);
        nameBox.setTextColor(UiPalette.inputText());
        nameBox.setTextColorUneditable(UiPalette.muted());
        nameBox.setValue(state.getName());
        nameBox.setResponder(state::setName);
        register(gameWidgets, nameBox);

        int gap = 10;
        int halfW = (contentW - gap) / 2;
        int optionY = contentY + 55;
        gameModeButton = new SettingButton(contentX, optionY, halfW, 34,
                tr("selectWorld.gameMode"), () -> state.getGameMode().displayName.getString(),
                FontAwesomeIcons.ICON_FA_GAMEPAD, this::cycleGameMode);
        register(gameWidgets, gameModeButton);
        difficultyButton = new SettingButton(contentX + halfW + gap, optionY, halfW, 34,
                tr("options.difficulty"), () -> state.getDifficulty().getDisplayName().getString(),
                FontAwesomeIcons.ICON_FA_SHIELD, this::cycleDifficulty);
        register(gameWidgets, difficultyButton);

        allowCheatsButton = new ToggleButton(contentX, optionY + 44, contentW, 34,
                tr("selectWorld.allowCommands"), FontAwesomeIcons.ICON_FA_WAND_MAGIC,
                state::isAllowCheats, () -> state.setAllowCheats(!state.isAllowCheats()));
        register(gameWidgets, allowCheatsButton);
    }

    private void buildWorldTab() {
        seedFieldY = contentY + 15;
        seedBox = new AlignedEditBox(font, contentX + 10, seedFieldY,
                contentW - 20, 28, Component.translatable("selectWorld.enterSeed"), 9);
        seedBox.setBordered(false);
        seedBox.setTextColor(UiPalette.inputText());
        seedBox.setTextColorUneditable(UiPalette.muted());
        seedBox.setHint(Component.translatable("selectWorld.seedInfo"));
        seedBox.setValue(state.getSeed());
        seedBox.setResponder(state::setSeed);
        register(worldWidgets, seedBox);

        int optionY = contentY + 55;
        worldTypeButton = new SettingButton(contentX, optionY, contentW, 34,
                tr("selectWorld.mapType"), () -> state.getWorldType().describePreset().getString(),
                FontAwesomeIcons.ICON_FA_GLOBE, this::cycleWorldType);
        register(worldWidgets, worldTypeButton);

        int gap = 10;
        int halfW = (contentW - gap) / 2;
        structuresButton = new ToggleButton(contentX, optionY + 44, halfW, 34,
                tr("selectWorld.mapFeatures"), FontAwesomeIcons.ICON_FA_CUBES,
                state::isGenerateStructures,
                () -> state.setGenerateStructures(!state.isGenerateStructures()));
        register(worldWidgets, structuresButton);
        bonusChestButton = new ToggleButton(contentX + halfW + gap, optionY + 44, halfW, 34,
                tr("selectWorld.bonusItems"), FontAwesomeIcons.ICON_FA_BOX,
                state::isBonusChest, () -> state.setBonusChest(!state.isBonusChest()));
        register(worldWidgets, bonusChestButton);

        customizeButton = new CommandButton(contentX, optionY + 88, contentW, 30,
                tr("selectWorld.customizeType"), FontAwesomeIcons.ICON_FA_SLIDERS, this::openPresetEditor);
        register(worldWidgets, customizeButton);
    }

    private void buildAdvancedTab() {
        int gap = 10;
        int halfW = (contentW - gap) / 2;
        int y = contentY + 8;
        register(advancedWidgets, new CommandButton(contentX, y, halfW, 36,
                tr("selectWorld.gameRules"), FontAwesomeIcons.ICON_FA_LIST_CHECK, this::openGameRules));
        register(advancedWidgets, new CommandButton(contentX + halfW + gap, y, halfW, 36,
                tr("selectWorld.experiments"), FontAwesomeIcons.ICON_FA_FLASK, this::openExperiments));
        register(advancedWidgets, new CommandButton(contentX, y + 46, halfW, 36,
                tr("selectWorld.dataPacks"), FontAwesomeIcons.ICON_FA_FILE, this::openDataPacks));
    }

    private void buildFooter() {
        int y = panelY + panelH - 40;
        int right = panelX + panelW - 18;
        createButton = new ActionButton(right - 118, y, 118, 26,
                tr("selectWorld.create"), FontAwesomeIcons.ICON_FA_CHECK, true, this::createWorld);
        addRenderableWidget(createButton);
        addRenderableWidget(new ActionButton(right - 202, y, 78, 26,
                tr("gui.cancel"), FontAwesomeIcons.ICON_FA_XMARK, false, this::onClose));
    }

    private <T extends AbstractWidget> T register(List<AbstractWidget> group, T widget) {
        group.add(widget);
        return addRenderableWidget(widget);
    }

    private void setSelectedTab(CreateTab tab) {
        selectedTab = tab;
        RETAINED_TABS.put(delegate, tab);
        setGroupVisible(gameWidgets, tab == CreateTab.GAME);
        setGroupVisible(worldWidgets, tab == CreateTab.WORLD);
        setGroupVisible(advancedWidgets, tab == CreateTab.ADVANCED);
        for (TabButton button : tabButtons) {
            button.setSelected(button.tab == tab);
        }
        if (tab == CreateTab.GAME && nameBox != null) {
            setInitialFocus(nameBox);
        } else if (tab == CreateTab.WORLD && seedBox != null) {
            setInitialFocus(seedBox);
        }
    }

    private void setGroupVisible(List<AbstractWidget> widgets, boolean visible) {
        for (AbstractWidget widget : widgets) {
            widget.visible = visible;
        }
    }

    private void cycleGameMode() {
        WorldCreationUiState.SelectedGameMode[] modes = {
                WorldCreationUiState.SelectedGameMode.SURVIVAL,
                WorldCreationUiState.SelectedGameMode.HARDCORE,
                WorldCreationUiState.SelectedGameMode.CREATIVE
        };
        int current = 0;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == state.getGameMode()) {
                current = i;
                break;
            }
        }
        state.setGameMode(modes[(current + 1) % modes.length]);
        syncControlStates();
    }

    private void cycleDifficulty() {
        Difficulty[] values = Difficulty.values();
        int index = state.getDifficulty().getId();
        state.setDifficulty(values[(index + 1) % values.length]);
    }

    private void cycleWorldType() {
        List<WorldCreationUiState.WorldTypeEntry> values = state.getNormalPresetList();
        if (values.isEmpty()) {
            return;
        }
        int index = values.indexOf(state.getWorldType());
        state.setWorldType(values.get((Math.max(0, index) + 1) % values.size()));
        syncControlStates();
    }

    private void openPresetEditor() {
        PresetEditor editor = state.getPresetEditor();
        if (editor != null) {
            minecraft.setScreen(editor.createEditScreen(delegate, state.getSettings()));
        }
    }

    private void openGameRules() {
        minecraft.setScreen(new XerionGameRulesScreen(state.getGameRules().copy(), result -> {
            result.ifPresent(state::setGameRules);
            minecraft.setScreen(delegate);
        }));
    }

    private void openExperiments() {
        ((CreateWorldScreenInvoker) (Object) delegate)
                .xerion$openExperiments(state.getSettings().dataConfiguration());
    }

    private void openDataPacks() {
        ((CreateWorldScreenInvoker) (Object) delegate)
                .xerion$openDataPacks(state.getSettings().dataConfiguration());
    }

    private void createWorld() {
        minecraft.setScreen(new XerionLoadingScreen(Component.translatable("createWorld.preparing"),
                () -> ((CreateWorldScreenInvoker) (Object) delegate).xerion$createWorld()));
    }

    private void syncControlStates() {
        if (allowCheatsButton == null) {
            return;
        }
        boolean debug = state.isDebug();
        allowCheatsButton.active = !debug && !state.isHardcore();
        difficultyButton.active = !state.isHardcore();
        structuresButton.active = !debug;
        bonusChestButton.active = !debug && !state.isHardcore();
        worldTypeButton.active = state.getWorldType().preset() != null;
        customizeButton.active = !debug && state.getPresetEditor() != null;
        createButton.active = true;
    }

    @Override
    public void tick() {
        if (nameBox.visible) {
            nameBox.tick();
        }
        if (seedBox.visible) {
            seedBox.tick();
        }
        syncControlStates();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        AnimationClock.beginFrame();
        KawaseBlurPipeline.beginFrame();
        screenAlpha = UiMath.lerp(screenAlpha, 1.0f, UiAnimationTimings.PANEL_FADE);
        float alpha = UiMath.clamp01(screenAlpha);

        XerionTitleScreen.drawBackgroundImage(graphics, width, height, alpha);
        graphics.fill(0, 0, width, height, UiColor.alpha(0xFF070A10, alpha * 0.48f));
        graphics.flush();
        KawaseBlurPipeline.renderSurfaceBackdrop(graphics, width, height,
                panelX, panelY, panelW, panelH, 10.0f, alpha, 24.0f);
        ShadowRenderer.shadow(graphics, panelX, panelY, panelW, panelH, 10,
                ShadowStyle.popup(), alpha);
        ShapeRenderer.roundRect(graphics, panelX, panelY, panelW, panelH, 10,
                UiColor.alpha(UiPalette.shell(), alpha * 0.82f));
        ShapeRenderer.roundRectBorder(graphics, panelX, panelY, panelW, panelH,
                10, 1, UiColor.alpha(UiPalette.shellBorder(), alpha));

        drawHeader(graphics, alpha);
        drawTabContent(graphics, alpha);
        graphics.fill(panelX + 16, panelY + panelH - FOOTER_H,
                panelX + panelW - 16, panelY + panelH - FOOTER_H + 1,
                UiColor.alpha(UiPalette.border(), alpha * 0.82f));
        for (var renderable : renderables) {
            if (renderable instanceof AbstractWidget widget) {
                widget.setAlpha(alpha);
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawHeader(GuiGraphics graphics, float alpha) {
        drawText(graphics, tr("selectWorld.create"), panelX + 20, panelY + 16,
                16.0f, UiPalette.text(), alpha);
        drawText(graphics, state.getTargetFolder(), panelX + 20, panelY + 39,
                7.5f, UiPalette.muted(), alpha);
        graphics.fill(panelX + 16, panelY + HEADER_H + TAB_H - 1,
                panelX + panelW - 16, panelY + HEADER_H + TAB_H,
                UiColor.alpha(UiPalette.border(), alpha * 0.82f));
    }

    private void drawTabContent(GuiGraphics graphics, float alpha) {
        if (selectedTab == CreateTab.GAME) {
            drawText(graphics, tr("selectWorld.enterName"), contentX, contentY,
                    8.0f, UiPalette.muted(), alpha);
            drawInputBackground(graphics, nameBox, contentX, nameFieldY, contentW, alpha);
        } else if (selectedTab == CreateTab.WORLD) {
            drawText(graphics, tr("selectWorld.enterSeed"), contentX, contentY,
                    8.0f, UiPalette.muted(), alpha);
            drawInputBackground(graphics, seedBox, contentX, seedFieldY, contentW, alpha);
        }
    }

    private void drawInputBackground(GuiGraphics graphics, EditBox box,
                                     int x, int y, int w, float alpha) {
        int border = box.isFocused() ? UiPalette.accent() : UiPalette.border();
        ShapeRenderer.roundRect(graphics, x, y, w, FIELD_H, 6,
                UiColor.alpha(UiPalette.field(), alpha * 0.90f));
        ShapeRenderer.roundRectBorder(graphics, x, y, w, FIELD_H, 6, 1,
                UiColor.alpha(border, alpha));
    }

    private void drawText(GuiGraphics graphics, String value, float x, float y,
                          float size, int color, float alpha) {
        int resolved = UiColor.alpha(color, alpha);
        MSDFRenderer.draw(graphics, value, x, y, size, resolved);
    }

    private static String tr(String key) {
        return Component.translatable(key).getString();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == 257 || keyCode == 335) && createButton.active) {
            createWorld();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        delegate.popScreen();
    }

    private enum CreateTab {
        GAME("createWorld.tab.game.title", FontAwesomeIcons.ICON_FA_GAMEPAD),
        WORLD("createWorld.tab.world.title", FontAwesomeIcons.ICON_FA_GLOBE),
        ADVANCED("createWorld.tab.more.title", FontAwesomeIcons.ICON_FA_SLIDERS);

        private final String translationKey;
        private final int icon;

        CreateTab(String translationKey, int icon) {
            this.translationKey = translationKey;
            this.icon = icon;
        }

        private String label() {
            return tr(translationKey);
        }
    }

    private record FreshWorldData(WorldGenSettings settings, WorldDataConfiguration configuration) {
    }

    @FunctionalInterface
    private interface BooleanValue {
        boolean get();
    }

    private static class BaseButton extends AbstractButton {
        protected final int icon;
        protected final Runnable action;
        protected float hoverProgress;

        protected BaseButton(int x, int y, int width, int height, Component message,
                             int icon, Runnable action) {
            super(x, y, width, height, message);
            this.icon = icon;
            this.action = action;
        }

        @Override
        public void onPress() {
            action.run();
        }

        protected float updateHover() {
            hoverProgress = UiMath.lerp(hoverProgress,
                    isHoveredOrFocused() && active ? 1.0f : 0.0f,
                    UiAnimationTimings.CONTROL);
            return UiMath.clamp01(alpha) * (active ? 1.0f : 0.42f);
        }

        protected void drawBase(GuiGraphics graphics, float widgetAlpha, int accent, float activeMix) {
            int normal = UiColor.alpha(UiPalette.surface(), widgetAlpha * 0.78f);
            int hover = UiColor.alpha(accent, widgetAlpha * 0.72f);
            ShapeRenderer.roundRect(graphics, getX(), getY(), getWidth(), getHeight(), 6,
                    UiColor.lerp(normal, hover, activeMix));
            ShapeRenderer.roundRectBorder(graphics, getX(), getY(), getWidth(), getHeight(),
                    6, 1, UiColor.alpha(UiColor.lerp(UiPalette.border(), accent, hoverProgress), widgetAlpha));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    private static final class TabButton extends BaseButton {
        private final CreateTab tab;
        private boolean selected;

        private TabButton(int x, int y, int width, int height, CreateTab tab, Runnable action) {
            super(x, y, width, height, Component.literal(tab.label()), tab.icon, action);
            this.tab = tab;
        }

        private void setSelected(boolean selected) {
            this.selected = selected;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            float widgetAlpha = updateHover();
            drawBase(graphics, widgetAlpha, UiPalette.accent(), selected ? 0.68f : hoverProgress * 0.34f);
            drawCenteredIconText(graphics, this, tab.label(), icon, 8.0f, 8.0f,
                    selected ? UiPalette.accent() : UiPalette.text(), widgetAlpha);
        }
    }

    private static final class SettingButton extends BaseButton {
        private final String label;
        private final java.util.function.Supplier<String> value;

        private SettingButton(int x, int y, int width, int height, String label,
                              java.util.function.Supplier<String> value, int icon, Runnable action) {
            super(x, y, width, height, Component.literal(label), icon, action);
            this.label = label;
            this.value = value;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            float widgetAlpha = updateHover();
            drawBase(graphics, widgetAlpha, UiPalette.accent(), hoverProgress * 0.28f);
            drawLeftIcon(graphics, this, icon, widgetAlpha);
            drawButtonText(graphics, label, getX() + 31,
                    MSDFRenderer.textCenteredY(getY(), getHeight(), 8.0f),
                    8.0f, UiPalette.text(), widgetAlpha);
            String currentValue = value.get();
            float valueW = MSDFRenderer.width(currentValue, 8.0f);
            drawButtonText(graphics, currentValue,
                    Math.max(getX() + 72, getX() + getWidth() - valueW - 20),
                    MSDFRenderer.textCenteredY(getY(), getHeight(), 8.0f),
                    8.0f, UiPalette.accentDark(), widgetAlpha);
            MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_ANGLE_DOWN,
                    getX() + getWidth() - 12,
                    MSDFRenderer.iconCenteredY(FontAwesomeIcons.ICON_FA_ANGLE_DOWN,
                            getY(), getHeight(), 7.0f),
                    7.0f, UiColor.alpha(UiPalette.muted(), widgetAlpha));
        }
    }

    private static final class ToggleButton extends BaseButton {
        private final String label;
        private final BooleanValue value;
        private float toggleProgress;

        private ToggleButton(int x, int y, int width, int height, String label,
                             int icon, BooleanValue value, Runnable action) {
            super(x, y, width, height, Component.literal(label), icon, action);
            this.label = label;
            this.value = value;
            this.toggleProgress = value.get() ? 1.0f : 0.0f;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            float widgetAlpha = updateHover();
            toggleProgress = UiMath.lerp(toggleProgress, value.get() ? 1.0f : 0.0f,
                    UiAnimationTimings.CONTROL);
            drawBase(graphics, widgetAlpha, UiPalette.accent(), hoverProgress * 0.24f);
            drawLeftIcon(graphics, this, icon, widgetAlpha);
            drawButtonText(graphics, label, getX() + 31,
                    MSDFRenderer.textCenteredY(getY(), getHeight(), 8.0f),
                    8.0f, UiPalette.text(), widgetAlpha);
            int switchX = getX() + getWidth() - 31;
            int switchY = getY() + (getHeight() - 14) / 2;
            ShapeRenderer.roundRect(graphics, switchX, switchY, 22, 14, 7,
                    UiColor.lerp(UiPalette.track(), UiPalette.accent(), toggleProgress));
            ShapeRenderer.roundRect(graphics, switchX + 2 + Math.round(toggleProgress * 8.0f),
                    switchY + 2, 10, 10, 5,
                    UiColor.alpha(0xFFFFFFFF, widgetAlpha));
        }
    }

    private static final class CommandButton extends BaseButton {
        private final String label;

        private CommandButton(int x, int y, int width, int height, String label, int icon, Runnable action) {
            super(x, y, width, height, Component.literal(label), icon, action);
            this.label = label;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            float widgetAlpha = updateHover();
            drawBase(graphics, widgetAlpha, UiPalette.accent(), hoverProgress * 0.32f);
            drawCenteredIconText(graphics, this, label, icon, 8.5f, 8.0f,
                    UiPalette.text(), widgetAlpha);
        }
    }

    private static final class ActionButton extends BaseButton {
        private final String label;
        private final boolean primary;

        private ActionButton(int x, int y, int width, int height, String label,
                             int icon, boolean primary, Runnable action) {
            super(x, y, width, height, Component.literal(label), icon, action);
            this.label = label;
            this.primary = primary;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            float widgetAlpha = updateHover();
            drawBase(graphics, widgetAlpha, UiPalette.accent(),
                    primary ? 0.66f + hoverProgress * 0.34f : hoverProgress * 0.32f);
            drawCenteredIconText(graphics, this, label, icon, 8.0f, 8.0f,
                    primary ? 0xFFFFFFFF : UiPalette.text(), widgetAlpha);
        }
    }

    private static void drawLeftIcon(GuiGraphics graphics, AbstractWidget widget,
                                     int icon, float alpha) {
        float iconSize = 8.5f;
        MSDFRenderer.drawIconCodepoint(graphics, icon, widget.getX() + 11,
                MSDFRenderer.iconCenteredY(icon, widget.getY(), widget.getHeight(), iconSize),
                iconSize, UiColor.alpha(UiPalette.accent(), alpha));
    }

    private static void drawCenteredIconText(GuiGraphics graphics, AbstractWidget widget,
                                             String label, int icon, float iconSize,
                                             float textSize, int color, float alpha) {
        float iconW = MSDFRenderer.iconWidth(icon, iconSize);
        float textW = MSDFRenderer.width(label, textSize);
        float startX = widget.getX() + (widget.getWidth() - iconW - 5.0f - textW) * 0.5f;
        MSDFRenderer.drawIconCodepoint(graphics, icon, startX,
                MSDFRenderer.iconCenteredY(icon, widget.getY(), widget.getHeight(), iconSize),
                iconSize, UiColor.alpha(color, alpha));
        MSDFRenderer.draw(graphics, label, startX + iconW + 5.0f,
                MSDFRenderer.textCenteredY(widget.getY(), widget.getHeight(), textSize),
                textSize, UiColor.alpha(color, alpha));
    }

    private static void drawButtonText(GuiGraphics graphics, String text, float x, float y,
                                       float size, int color, float alpha) {
        int resolved = UiColor.alpha(color, alpha);
        MSDFRenderer.draw(graphics, text, x, y, size, resolved);
    }
}
