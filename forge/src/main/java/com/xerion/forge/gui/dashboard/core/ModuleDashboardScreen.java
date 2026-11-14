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

import com.xerion.common.XerionCore;
import com.xerion.common.module.Module;
import com.xerion.forge.config.ProfileConfigStore;
import com.xerion.common.config.CombatSettings;
import com.xerion.common.config.UiSettings;
import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.event.ForgeModEventBridge;
import com.xerion.forge.visual.ModelGlowRenderer;
import com.xerion.forge.runtime.SocialProfileStore;
import com.xerion.common.visual.ChamsRenderContext;
import com.xerion.forge.gui.MinecraftFps;
import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.xerion.forge.mixin.LevelRendererAccessor;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ModuleDashboardScreen extends Screen {
    private static final int PANEL_W = UiMetrics.Screen.WIDTH;
    private static final int PANEL_H = UiMetrics.Screen.HEIGHT;
    private static final int RAIL_W = UiMetrics.Screen.RAIL_WIDTH;
    private static final int TAB_SIZE = UiMetrics.Screen.TAB_SIZE;
    private static final int TAB_GAP = UiMetrics.Screen.TAB_GAP;
    private static final int TAB_TOP_PADDING = UiMetrics.Screen.TAB_TOP_PADDING;
    private static final int TAB_BOTTOM_PADDING = UiMetrics.Screen.TAB_BOTTOM_PADDING;
    private static final float SIDEBAR_TOP_VISIBLE_TABS = 6.5f;
    private static final int SIDEBAR_FADE_H = 26;
    private static final int SIDEBAR_TAB_CLIP_PAD = 2;
    private static final int CARD_GAP = UiMetrics.Screen.CARD_GAP;
    private static final int SCROLL_STEP = UiMetrics.Screen.SCROLL_STEP;
    private static final int CONFIG_MENU_W = UiMetrics.HotkeyPopup.POPUP_W;
    private static final int CONFIG_MENU_H = UiMetrics.HotkeyPopup.POPUP_PAD * 2
            + UiMetrics.HotkeyPopup.ROW_H * 4
            + UiMetrics.HotkeyPopup.DIVIDER_GAP;
    private static final int ORE_WINDOW_W = 520;
    private static final int ORE_WINDOW_H = 360;
    private static final int ORE_WINDOW_TITLE_H = 32;
    private static final int ORE_WINDOW_SEARCH_H = 24;
    private static final int ORE_CARD_W = 76;
    private static final int ORE_CARD_H = 104;
    private static final int ORE_CARD_GAP = 16;
    private static final int MODAL_CLOSE_SIZE = 20;
    private static final int MODAL_CLOSE_RIGHT_INSET = 10;
    private static final int MODAL_CLOSE_TOP_INSET = 8;
    private static final int FAKE_PLAYER_WINDOW_W = 430;
    private static final int FAKE_PLAYER_WINDOW_H = 320;
    private static final int FAKE_PLAYER_WINDOW_TITLE_H = 32;
    private static final int FAKE_PLAYER_ROW_H = 34;
    private static final int ESP_ITEMS_POPUP_W = 324;
    private static final int ESP_ITEMS_GAP = 6;
    private static final int ESP_ITEMS_BUTTON_H = 22;
    private static final int ESP_ITEMS_BUTTON_PAD_X = 18;
    private static final float ESP_ITEMS_BUTTON_TEXT_SIZE = 10.0f;
    private static final long FRIEND_ROW_FADE_OUT_MS = 520L;
    private static final int MAIN_SCROLLBAR_X_OFFSET = 8;
    static final int SCROLLBAR_RIGHT_INSET = ScrollbarControl.RIGHT_INSET;
    static final int SCROLLBAR_TRACK_W = ScrollbarControl.TRACK_W;
    static final int SCROLLBAR_TRACK_SLOT_W = 8;
    static final int SCROLLBAR_VERTICAL_INSET = ScrollbarControl.VERTICAL_INSET;
    static final int SCROLLBAR_THUMB_MIN_H = ScrollbarControl.THUMB_MIN_H;
    private static final String CLIENT_VERSION = "1.0.5";
    private static final int PAGE_BG = 0x00000000;
    private static int SHELL = UiPalette.shell();
    private static int SHELL_BORDER = UiPalette.shellBorder();
    private static int SIDEBAR = UiPalette.sidebar();
    private static int SURFACE = UiPalette.surface();
    private static int FIELD = UiPalette.field();
    private static int TRACK = UiPalette.track();
    private static int BORDER = UiPalette.border();
    private static int TEXT = UiPalette.text();
    private static int MUTED = UiPalette.muted();
    private static int SUBTLE = UiPalette.subtle();
    private static int ACCENT = UiPalette.accent();
    private static int ACCENT_DARK = UiPalette.accentDark();
    private static int ACCENT_SOFT = UiPalette.accentSoft();
    private static int GREEN = UiPalette.statusGreen();
    private static int RED = UiPalette.statusRed();
    private static int ORANGE = UiPalette.statusOrange();
    private static int PREVIEW_BG = UiPalette.previewBg();
    private static int PREVIEW_BORDER = UiPalette.previewBorder();
    private static int PREVIEW_TEXT = TEXT;
    private static int PREVIEW_MUTED = MUTED;
    private static int PREVIEW_ACCENT = ACCENT_DARK;
    private static int PREVIEW_ACCENT_SOFT = ACCENT_SOFT;
    private static SidebarTab retainedSelectedTab = SidebarTab.COMBAT;
    private static int retainedSidebarScrollY;
    private static boolean retainedMusicHomeVisible = true;
    private static boolean retainedMusicExpandedDetails;
    private static final Deque<Integer> retainedMusicNavigationScroll = new ArrayDeque<>();
    private static final Map<SidebarTab, Integer> retainedContentScroll = new EnumMap<>(SidebarTab.class);

    private final XerionCore core;
    private final WidgetSurface dashboardSurface = new WidgetSurface();
    private final SelectionCatalog selectionWindowData = new SelectionCatalog();
    private final Map<SidebarTab, Float> tabButtonAnimations = new EnumMap<>(SidebarTab.class);
    private final Map<String, Float> cardHeightAnimations = new HashMap<>();
    private final Map<String, Float> previewItemAnimations = new HashMap<>();
    private final Map<String, Float> configIconAnimations = new HashMap<>();
    private final Map<String, Float> configNameEditAnimations = new HashMap<>();
    private final Map<String, Integer> friendRowPositions = new HashMap<>();
    private final Map<String, FriendRowFadeState> closingFriendRows = new HashMap<>();
    private final ConfigRowAnimator configRowAnimator = new ConfigRowAnimator();
    private final FakePlayerRowAnimator fakePlayerRowAnimator = new FakePlayerRowAnimator();
    private boolean listeningVeinMinerKey;
    private boolean listeningAutoSortKey;
    private boolean listeningPearlThrowKey;
    private boolean listeningQuickSwitchKey;
    private String friendNameDraft = "";
    private String musicSearchText = "";
    private boolean musicHomeVisible = retainedMusicHomeVisible;
    private boolean musicExpandedDetails = retainedMusicExpandedDetails;
    private int musicListX;
    private int musicListY;
    private int musicListW;
    private int musicListH;
    private int musicResultScrollY;
    private float musicAnimatedResultScrollY;
    private int musicMaxResultScrollY;
    private int musicResultContentH;
    private float musicResultScrollbarAlpha;
    private int musicRenderedPage = -1;
    private float musicPageTransitionProgress = 1.0f;
    private final ScrollbarControl.DragState musicResultScrollbarDrag = new ScrollbarControl.DragState();
    private float musicLyricsAnimatedLine;
    private long musicLyricsSongId = -1L;
    private long musicLyricsTimelineRevision = -1L;
    private boolean musicProgressDragging;
    private long musicPreviewPositionMs;
    private float musicAnimatedProgress = -1.0f;
    private long musicProgressSongId = -1L;
    private long musicProgressLastRenderNanos;
    private int musicPlayerX;
    private int musicPlayerY;
    private int musicPlayerW;
    private int musicPlayerH;
    private int musicProgressTrackX;
    private int musicProgressTrackY;
    private int musicProgressTrackW;
    private boolean musicPlaylistOpen;
    private boolean musicVolumePopupOpen;
    private boolean musicVolumeDragging;
    private int musicVolumeButtonX;
    private int musicVolumeButtonY;
    private int musicVolumeButtonSize;
    private int musicVolumePopupX;
    private int musicVolumePopupY;
    private int musicVolumePopupW;
    private int musicVolumePopupH;
    private int musicVolumeTrackX;
    private int musicVolumeTrackY;
    private int musicVolumeTrackH;
    private float musicAnimatedVolume = -1.0f;
    private int dashboardX;
    private int dashboardY;
    private int dashboardW;
    private int dashboardH;
    private double guiScale = 1.0;
    private float dashboardScale = 1.0f;
    private boolean clickPending;
    private int clickButton;
    private double clickX;
    private double clickY;
    private boolean mouseDown;
    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;
    private SidebarTab selectedTab = retainedSelectedTab;
    private int sidebarScrollY = retainedSidebarScrollY;
    private float animatedSidebarScrollY = retainedSidebarScrollY;
    private float sidebarTopFadeAlpha;
    private float sidebarBottomFadeAlpha;
    private int maxSidebarScrollY;
    private int contentScrollY;
    private float animatedContentScrollY;
    private int maxContentScrollY;
    private int contentBoundsX;
    private int contentBoundsY;
    private int contentBoundsW;
    private int contentBoundsH;
    private float contentScrollbarReserve;
    private float contentScrollbarAlpha;
    private final ScrollbarControl.DragState contentScrollbarDrag = new ScrollbarControl.DragState();
    private double renderMouseX;
    private double renderMouseY;
    private boolean previewEspPopupOpen;
    private boolean previewEspPopupRendered;
    private float previewEspPopupAlpha;
    private boolean previewModelDragging;
    private float previewModelYaw;
    private static PreviewModelMode lastPreviewModelMode = PreviewModelMode.ENEMIES;
    private PreviewModelMode previewModelMode = lastPreviewModelMode;
    private int previewPopupX;
    private int previewPopupY;
    private int previewToolbarItemsX;
    private int previewToolbarItemsY;
    private int previewToolbarItemsW;
    private int previewToolbarItemsH;
    private int previewPopupW;
    private PreviewBounds previewEspBoxBounds;
    private final Map<PreviewSettings, PreviewBounds> previewComponentBounds = new EnumMap<>(PreviewSettings.class);
    private final Map<Long, PreviewBounds> previewFlagBounds = new HashMap<>();
    private final Map<Long, PreviewBounds> previewMainItemBounds = new HashMap<>();
    private final Map<PreviewSettings, AnimatedPreviewBounds> previewComponentAnimations = new EnumMap<>(PreviewSettings.class);
    private final Map<Long, AnimatedPreviewBounds> previewFlagAnimations = new HashMap<>();
    private final Map<Long, AnimatedPreviewBounds> previewMainItemAnimations = new HashMap<>();
    private final PreviewDragGhost previewDragGhost = new PreviewDragGhost();
    private PreviewDragTarget previewDragTarget = PreviewDragTarget.NONE;
    private long previewDragFlagItem;
    private long previewDragMainItem;
    private VisualSettings.EspAnchor previewDragAnchor = VisualSettings.EspAnchor.LEFT;
    private float previewHealthGhostHorizontal;
    private double previewDragGrabCenterOffsetX;
    private double previewDragGrabCenterOffsetY;
    private double previewDragLastX;
    private double previewDragLastY;
    private boolean hotkeyHudDragging;
    private double hotkeyHudDragOffsetX;
    private double hotkeyHudDragOffsetY;
    private boolean arrayListHudDragging;
    private double arrayListHudDragOffsetX;
    private double arrayListHudDragOffsetY;
    private boolean targetsHudDragging;
    private double targetsHudDragOffsetX;
    private double targetsHudDragOffsetY;
    private boolean backpackHudDragging;
    private double backpackHudDragOffsetX;
    private double backpackHudDragOffsetY;
    private boolean potionBuffHudDragging;
    private double potionBuffHudDragOffsetX;
    private double potionBuffHudDragOffsetY;
    private boolean speedHudDragging;
    private double speedHudDragOffsetX;
    private double speedHudDragOffsetY;
    private boolean watermarkHudDragging;
    private double watermarkHudDragOffsetX;
    private double watermarkHudDragOffsetY;
    private boolean musicLyricsHudDragging;
    private double musicLyricsHudDragOffsetX;
    private double musicLyricsHudDragOffsetY;
    private final Map<String, HudPopupPosition> hudSettingsPopupPositions = new HashMap<>();
    private int pendingWatermarkElementDrag;
    private long watermarkElementDragStartedAt;
    private double watermarkElementDragStartX;
    private int demoButtonClicks;
    private String movementSpeedModeSelection;
    private SidebarTab previousTab;
    private SidebarTab renderingContentTab;
    private String currentCardTitle = "";
    private float previousContentScrollY;
    private float tabTransitionProgress = 1.0f;
    private boolean pendingScrollRestore;
    private float animatedDashboardX;
    private float animatedDashboardY;
    private float dashboardAnimationAlpha;
    private float contentRenderAlpha = 1.0f;
    private float dashboardRenderOffsetX;
    private float dashboardRenderOffsetY;
    private boolean dashboardAnimationInitialized;
    private String configName = "default";
    private String configStatus = "Ready";
    private String configMenuProfile;
    private int configMenuX;
    private int configMenuY;
    private int configMenuButtonX;
    private int configMenuButtonY;
    private int configMenuButtonSize;
    private boolean configMenuOpenedThisFrame;
    private boolean configSearchOpen;
    private String configSearchText = "";
    private float configSearchWidth;
    private boolean snapConfigRowsOnNextDraw;
    private String editingConfigName;
    private String editingConfigText = "";
    private boolean configRenameFocusPending;
    private String lastConfigNameClick;
    private long lastConfigNameClickMillis;
    private int editingConfigNameX;
    private int editingConfigNameY;
    private int editingConfigNameW;
    private int editingConfigNameH;
    private boolean oreWindowOpen;
    private boolean suppressOreWindowClickThisFrame;
    private boolean oreWindowDragging;
    private int oreWindowX = Integer.MIN_VALUE;
    private int oreWindowY = Integer.MIN_VALUE;
    private double oreWindowDragOffsetX;
    private double oreWindowDragOffsetY;
    private float oreWindowAlpha;
    private int oreWindowScrollY;
    private float oreWindowAnimatedScrollY;
    private int oreWindowMaxScrollY;
    private float oreWindowScrollbarReserve;
    private float oreWindowScrollbarAlpha;
    private final ScrollbarControl.DragState oreWindowScrollbarDrag = new ScrollbarControl.DragState();
    private SelectionWindowMode selectionWindowMode = SelectionWindowMode.ORE;
    private String selectionSearchText = "";
    private boolean selectionSearchFocused;
    private int selectionSearchCursor;
    private int selectionSearchAnchor;
    private char pendingSelectionSearchHighSurrogate;
    private int selectionSearchX;
    private int selectionSearchY;
    private int selectionSearchW;
    private int selectionSearchH;
    private final Map<String, Boolean> oreCardSelectedStates = new HashMap<>();
    private boolean fakePlayerWindowOpen;
    private boolean fakePlayerWindowDragging;
    private boolean suppressFakePlayerWindowClickThisFrame;
    private int fakePlayerWindowX = Integer.MIN_VALUE;
    private int fakePlayerWindowY = Integer.MIN_VALUE;
    private double fakePlayerWindowDragOffsetX;
    private double fakePlayerWindowDragOffsetY;
    private float fakePlayerWindowAlpha;
    private int fakePlayerWindowScrollY;
    private float fakePlayerWindowAnimatedScrollY;
    private int fakePlayerWindowMaxScrollY;
    private float fakePlayerWindowScrollbarReserve;
    private float fakePlayerWindowScrollbarAlpha;
    private final ScrollbarControl.DragState fakePlayerWindowScrollbarDrag = new ScrollbarControl.DragState();

    public ModuleDashboardScreen(Component title, XerionCore core) {
        super(title);
        this.core = core;
    }

    public static void warmupHotkeys(XerionCore core) {
        ModuleDashboardScreen screen = new ModuleDashboardScreen(Component.literal("WidgetSurfaceWarmup"), core);
        Window window = Minecraft.getInstance().getWindow();
        screen.guiScale = window.getGuiScale();
        screen.dashboardScale = VisualSettings.dashboardGuiScaleFactor();
        screen.dashboardW = PANEL_W;
        screen.dashboardH = PANEL_H;
        screen.dashboardX = (window.getWidth() - screen.dashboardW) / 2;
        screen.dashboardY = (window.getHeight() - screen.dashboardH) / 2;
        screen.dashboardSurface.setHotkeyPreregistering(true);
        int contentX = screen.dashboardX + 86;
        int contentY = screen.dashboardY + 58;
        int contentW = screen.dashboardW - 100;
        int gap = 14;
        int colW = (contentW - gap) / 2;
        int leftX = contentX;
        int rightX = leftX + colW + gap;
        screen.preregisterHotkeyPage(SidebarTab.COMBAT, leftX, contentY, rightX, colW);
        screen.preregisterHotkeyPage(SidebarTab.RAGEBOT, leftX, contentY, rightX, colW);
        screen.preregisterHotkeyPage(SidebarTab.BYPASS, leftX, contentY, rightX, colW);
        screen.preregisterHotkeyPage(SidebarTab.WHITELIST, leftX, contentY, rightX, colW);
        screen.preregisterHotkeyPage(SidebarTab.MOVEMENT, leftX, contentY, rightX, colW);
        screen.preregisterHotkeyPage(SidebarTab.WORLD, leftX, contentY, rightX, colW);
        screen.preregisterHotkeyPage(SidebarTab.VISUAL, leftX, contentY, rightX, colW);
        screen.preregisterHotkeyPage(SidebarTab.PLAYER, leftX, contentY, rightX, colW);
        screen.preregisterHotkeyPage(SidebarTab.MISC, leftX, contentY, rightX, colW);
        screen.preregisterHotkeyPage(SidebarTab.MUSIC, leftX, contentY, rightX, colW);
        screen.dashboardSurface.setHotkeyPreregistering(false);
        WidgetSurface.reconcileHotkeyRuntimeMetas();
    }

    private void preregisterHotkeyPage(SidebarTab tab, int leftX, int y, int rightX, int colW) {
        SidebarTab previousTab = renderingContentTab;
        renderingContentTab = tab;
        try {
            switch (tab) {
                case COMBAT -> {
                    int leftY = y;
                    leftY += preregisterCard("KILL AURA", leftX, leftY, colW, this::measureAimbotCard) + CARD_GAP;
                    leftY += preregisterCard("MELEE", leftX, leftY, colW, this::measureMeleeCard) + CARD_GAP;
                    leftY += preregisterCard("DEFENSE", leftX, leftY, colW, this::measureDefenseCard) + CARD_GAP;
                    preregisterCard("TERRAIN", leftX, leftY, colW, this::measureTerrainCard);
                    int rightY = y;
                    rightY += preregisterCard("ASSIST", rightX, rightY, colW, this::measureCombatAssistCard) + CARD_GAP;
                    rightY += preregisterCard("AUTO CRYSTAL", rightX, rightY, colW, this::measureAutoCrystalCard) + CARD_GAP;
                    rightY += preregisterCard("BED AURA", rightX, rightY, colW, this::measureBedAuraCard) + CARD_GAP;
                    preregisterCard("ANCHOR AURA", rightX, rightY, colW, this::measureAnchorAuraCard);
                }
                case RAGEBOT -> {
                    int leftY = y;
                    leftY += preregisterCard("MAIN", leftX, leftY, colW, this::measureRagebotMainCard) + CARD_GAP;
                    preregisterCard("SELECTION", leftX, leftY, colW, this::measureRagebotSelectionCard);
                    int rightY = y;
                    rightY += preregisterCard("OTHER", rightX, rightY, colW, this::measureRagebotOtherCard) + CARD_GAP;
                    preregisterCard("ANTI AIM", rightX, rightY, colW, this::measureRagebotAntiAimCard);
                }
                case BYPASS -> {
                    int leftY = y;
                    leftY += preregisterCard("MAIN", leftX, leftY, colW, this::measureBypassMainCard) + CARD_GAP;
                    leftY += preregisterCard("ROTATION", leftX, leftY, colW, this::measureBypassRotationCard) + CARD_GAP;
                    leftY += preregisterCard("TIMING", leftX, leftY, colW, this::measureBypassTimingCard) + CARD_GAP;
                    leftY += preregisterCard("DISABLER", leftX, leftY, colW, this::measureDisablerCard) + CARD_GAP;
                    leftY += preregisterCard("PING SPOOF", leftX, leftY, colW, this::measurePingSpoofCard) + CARD_GAP;
                    preregisterCard("NO ROTATE", leftX, leftY, colW, this::measureNoRotateCard);
                    int rightY = y;
                    rightY += preregisterCard("NETWORK", rightX, rightY, colW, this::measureBypassNetworkCard) + CARD_GAP;
                    preregisterCard("RECOVERY", rightX, rightY, colW, this::measureBypassMovementCard);
                }
                case WHITELIST -> {
                    int leftY = y;
                    leftY += preregisterCard("WHITELIST", leftX, leftY, colW, this::measureSocialCard) + CARD_GAP;
                    preregisterCard("FRIEND ACTIONS", leftX, leftY, colW, this::measureFriendActionsCard);
                    preregisterCard("FRIEND LIST", rightX, y, colW, this::measureFriendsListCard);
                }
                case MOVEMENT -> {
                    int leftY = y;
                    leftY += preregisterMovementGroup("GENERAL", leftX, leftY, colW,
                            this::populateMovementGeneral) + CARD_GAP;
                    leftY += preregisterCard("SCAFFOLD", leftX, leftY, colW, this::measureScaffoldCard) + CARD_GAP;
                    preregisterMovementGroup("EXPLOIT", leftX, leftY, colW,
                            MovementSettingsPage::populateMovementExploit);
                    int rightY = y;
                    rightY += preregisterMovementGroup("FLIGHT", rightX, rightY, colW,
                            MovementSettingsPage::populateMovementFlight) + CARD_GAP;
                    rightY += preregisterMovementGroup("VEHICLE", rightX, rightY, colW,
                            MovementSettingsPage::populateMovementVehicle) + CARD_GAP;
                    rightY += preregisterMovementGroup("ENVIRONMENT", rightX, rightY, colW,
                            MovementSettingsPage::populateMovementEnvironment) + CARD_GAP;
                    preregisterCard("EDGE", rightX, rightY, colW, this::measureMovementUtilityCard);
                }
                case WORLD -> {
                    int leftY = y;
                    leftY += preregisterCard("RESOURCE ESP", leftX, leftY, colW,
                            this::measureWorldResourceEspCard) + CARD_GAP;
                    leftY += preregisterCard("BUILD / MINE", leftX, leftY, colW,
                            this::measureWorldBuildMineCard) + CARD_GAP;
                    preregisterCard("SPECIAL BLOCKS", leftX, leftY, colW,
                            this::measureWorldSpecialBlocksCard);
                    int rightY = y;
                    rightY += preregisterCard("WORLD RENDER", rightX, rightY, colW,
                            this::measureWorldRenderCard) + CARD_GAP;
                    preregisterCard("WORLD UTILITY", rightX, rightY, colW, this::measureWorldUtilityCard);
                }
                case VISUAL -> {
                    int leftY = y;
                    leftY += preregisterCard("GENERAL", leftX, leftY, colW,
                            this::measureVisualGeneralStableCard) + CARD_GAP;
                    preregisterCard("MODEL", leftX, leftY, colW, this::measureVisualModelCard);
                }
                case PLAYER -> {
                    preregisterCard("PLAYER", leftX, y, colW, this::measurePlayerCard);
                    preregisterCard("INVENTORY", rightX, y, colW, this::measureInventoryCard);
                }
                case MISC -> {
                    preregisterCard("Features", leftX, y, colW, this::measureMiscCard);
                    preregisterCard("SETTINGS", rightX, y, colW, this::measureSettingsCard);
                }
                case MUSIC -> {
                }
                case CONFIG, USER -> {
                }
            }
        } finally {
            renderingContentTab = previousTab;
        }
    }

    private int preregisterCard(String title, int x, int y, int w, CardMeasurer measurer) {
        currentCardTitle = cardDisplayTitle(title);
        return measurer.measure(x, y, w);
    }

    private int preregisterMovementGroup(String title, int x, int y, int w,
                                         java.util.function.Consumer<WidgetSurface.Frame> populator) {
        currentCardTitle = cardDisplayTitle(title);
        return measureMovementGroupCard(x, y, w, populator);
    }

    @Override
    protected void init() {
        WidgetSurface.resetHotkeyPressedState();
        ProfileConfigStore.ensureDefaultProfile();
        String lastLoadedProfile = ProfileConfigStore.lastLoadedProfile();
        if (lastLoadedProfile != null && !lastLoadedProfile.isBlank()) {
            configName = lastLoadedProfile;
        }
        Window window = Minecraft.getInstance().getWindow();
        guiScale = window.getGuiScale();
        dashboardScale = VisualSettings.dashboardGuiScaleFactor();
        dashboardW = PANEL_W;
        dashboardH = PANEL_H;
        dashboardX = (window.getWidth() - dashboardW) / 2;
        dashboardY = (window.getHeight() - dashboardH) / 2;
        animatedDashboardX = dashboardX;
        animatedDashboardY = dashboardY - 16.0f;
        dashboardAnimationAlpha = 0.0f;
        dashboardRenderOffsetX = 0.0f;
        dashboardRenderOffsetY = 0.0f;
        dashboardAnimationInitialized = true;
        sidebarScrollY = Math.max(0, retainedSidebarScrollY);
        animatedSidebarScrollY = sidebarScrollY;
        contentScrollY = retainedScroll(selectedTab);
        animatedContentScrollY = contentScrollY;
        pendingScrollRestore = true;
        snapConfigRowsOnNextDraw = selectedTab == SidebarTab.CONFIG;
        if (oreWindowX == Integer.MIN_VALUE || oreWindowY == Integer.MIN_VALUE) {
            oreWindowX = (window.getWidth() - ORE_WINDOW_W) / 2;
            oreWindowY = (window.getHeight() - ORE_WINDOW_H) / 2;
            clampOreWindowPosition();
        }
        if (fakePlayerWindowX == Integer.MIN_VALUE || fakePlayerWindowY == Integer.MIN_VALUE) {
            fakePlayerWindowX = (window.getWidth() - FAKE_PLAYER_WINDOW_W) / 2;
            fakePlayerWindowY = (window.getHeight() - FAKE_PLAYER_WINDOW_H) / 2;
            clampFakePlayerWindowPosition();
        }
        WindowsImeBridge.install(this::commitImeText, this::imeCaretPosition);
    }

    private boolean commitImeText(String value) {
        if (Minecraft.getInstance().screen != this || value == null || value.isEmpty()) {
            return false;
        }
        if (oreWindowOpen) {
            if (!selectionSearchFocused) {
                return false;
            }
            pendingSelectionSearchHighSurrogate = 0;
            replaceSelectionSearchText(value);
            return true;
        }
        return dashboardSurface.insertImeText(value);
    }

    private WindowsImeBridge.ClientPoint imeCaretPosition() {
        int logicalX;
        int logicalY;
        if (oreWindowOpen && selectionSearchFocused) {
            logicalX = selectionSearchX + 10
                    + Math.min(TextPainter.unicodeWidth(selectionSearchText, 10.0f), selectionSearchW - 20);
            logicalY = selectionSearchY + selectionSearchH;
        } else if (dashboardSurface.hasFocusedTextInput()) {
            logicalX = dashboardSurface.focusedTextInputCaretX();
            logicalY = dashboardSurface.focusedTextInputCaretY();
        } else {
            return null;
        }
        Window window = Minecraft.getInstance().getWindow();
        int clientX = clamp((int) Math.round(toDashboardScale(logicalX, true)), 0, window.getWidth());
        int clientY = clamp((int) Math.round(toDashboardScale(logicalY, false)), 0, window.getHeight());
        return new WindowsImeBridge.ClientPoint(clientX, clientY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        refreshTheme();
        AnimationClock.beginFrame();
        Window window = Minecraft.getInstance().getWindow();
        guiScale = window.getGuiScale();
        dashboardScale = VisualSettings.dashboardGuiScaleFactor();
        renderMouseX = dashboardMouseX(mouseX);
        renderMouseY = dashboardMouseY(mouseY);
        updateUiAnimationTimings();
        boolean modalOreWindow = oreWindowOpen || oreWindowAlpha > 0.02f;
        boolean modalFakePlayerWindow = fakePlayerWindowOpen || fakePlayerWindowAlpha > 0.02f;
        boolean modalWindow = modalOreWindow || modalFakePlayerWindow;
        dashboardSurface.begin(renderMouseX, renderMouseY, modalWindow ? false : mouseDown, modalWindow ? false : clickPending,
                clickButton, clickX, clickY);
        handlePreviewPopupClickOutside(modalWindow);
        boolean mouseInsidePopup = dashboardSurface.insideAnyPopup(renderMouseX, renderMouseY);
        boolean clickInsidePopup = dashboardSurface.hasClick() && dashboardSurface.insideAnyPopup(dashboardSurface.clickX(), dashboardSurface.clickY());
        dashboardSurface.setInteractionLocked(modalWindow
                || (dashboardSurface.hasClick() && !clickInsidePopup && previewPopupBlocksPoint(dashboardSurface.clickX(), dashboardSurface.clickY()))
                || (!mouseInsidePopup && previewPopupBlocksPoint(renderMouseX, renderMouseY)));
        dashboardSurface.setRenderOffset(dashboardRenderOffsetX, dashboardRenderOffsetY);
        dashboardSurface.setRenderScale(dashboardScale);
        registerHudSettingsPopups();
        KawaseBlurPipeline.beginFrame();
        if (minecraft.level == null) {
            XerionTitleScreen.drawBackgroundImage(graphics, width, height, 1.0f);
            graphics.flush();
        } else {
            graphics.fill(0, 0, width, height, PAGE_BG);
        }
        KawaseBlurPipeline.render(graphics, width, height, dashboardAnimationAlpha);
        renderScaledPanelBackdrop(graphics, dashboardX + dashboardRenderOffsetX, dashboardY + dashboardRenderOffsetY,
                dashboardW, dashboardH, 16.0f, dashboardAnimationAlpha);
        graphics.pose().pushPose();
        graphics.pose().scale((float) (1.0 / guiScale), (float) (1.0 / guiScale), 1.0f);
        applyDashboardScale(graphics);
        graphics.pose().translate(dashboardRenderOffsetX, dashboardRenderOffsetY, 0.0f);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, dashboardAnimationAlpha);
        try {
            drawShell(graphics);
            drawSidebar(graphics);
            drawTopBar(graphics);
            drawContent(graphics);
            drawStatusBar(graphics);
        } finally {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            ShapeRenderer.resetGlobalAlpha();
            graphics.pose().popPose();
        }
        KawaseBlurPipeline.beginOverlayStage();
        WatermarkHudRenderer.render(graphics, true);
        HotkeyHudRenderer.render(graphics, true);
        ArrayListHudRenderer.render(graphics, true);
        TargetsHudRenderer.render(graphics, true);
        BackpackHudRenderer.render(graphics, true);
        PotionBuffHudRenderer.render(graphics, true);
        SpeedHudRenderer.render(graphics, true);
        MusicLyricsHudRenderer.render(graphics);
        if (watermarkHudDragging || hotkeyHudDragging || arrayListHudDragging || targetsHudDragging
                || backpackHudDragging || potionBuffHudDragging || speedHudDragging
                || musicLyricsHudDragging) {
            Window hudWindow = Minecraft.getInstance().getWindow();
            int widgetW = watermarkHudDragging ? WatermarkHudRenderer.width()
                    : hotkeyHudDragging ? HotkeyHudRenderer.width()
                    : arrayListHudDragging ? ArrayListHudRenderer.width()
                    : backpackHudDragging ? BackpackHudRenderer.width()
                    : potionBuffHudDragging ? PotionBuffHudRenderer.width()
                    : speedHudDragging ? SpeedHudRenderer.width()
                    : musicLyricsHudDragging ? MusicLyricsHudRenderer.width()
                    : TargetsHudRenderer.width();
            int widgetH = watermarkHudDragging ? WatermarkHudRenderer.height()
                    : hotkeyHudDragging ? HotkeyHudRenderer.height()
                    : arrayListHudDragging ? ArrayListHudRenderer.height()
                    : backpackHudDragging ? BackpackHudRenderer.height()
                    : potionBuffHudDragging ? PotionBuffHudRenderer.height()
                    : speedHudDragging ? SpeedHudRenderer.height()
                    : musicLyricsHudDragging ? MusicLyricsHudRenderer.height()
                    : TargetsHudRenderer.height();
            HudSnap.renderGuides(graphics, hudWindow.getGuiScaledWidth(), hudWindow.getGuiScaledHeight(),
                    widgetW, widgetH);
        }
        graphics.pose().pushPose();
        graphics.pose().scale((float) (1.0 / guiScale), (float) (1.0 / guiScale), 1.0f);
        applyDashboardScale(graphics);
        graphics.pose().translate(dashboardRenderOffsetX, dashboardRenderOffsetY, 0.0f);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, dashboardAnimationAlpha);
        dashboardSurface.setInteractionLocked(modalWindow);
        try {
            KawaseBlurPipeline.beginOverlayStage();
            drawPreviewPopupOverlay(graphics);
            drawMusicVolumePopup(graphics);
            dashboardSurface.renderPopup(graphics);
            drawOreListWindow(graphics);
            drawFakePlayerListWindow(graphics);
            dashboardSurface.setInteractionLocked(false);
            dashboardSurface.setLayoutAnimationsSuspended(false);
            if (!modalWindow && !tabTransitionInputLocked() && dashboardSurface.hasClick() && !dashboardSurface.clickConsumed()
                    && isDashboardBlankDragArea(dashboardSurface.clickX(), dashboardSurface.clickY())) {
                dragging = true;
                dragOffsetX = dashboardSurface.clickX() - dashboardX;
                dragOffsetY = dashboardSurface.clickY() - dashboardY;
                dashboardSurface.consumeClick();
            }
            clickPending = false;
        } finally {
            dashboardSurface.setInteractionLocked(false);
            dashboardSurface.setLayoutAnimationsSuspended(false);
            clickPending = false;
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            ShapeRenderer.resetGlobalAlpha();
            graphics.pose().popPose();
        }
        WindowsImeBridge.updateInputState(
                oreWindowOpen ? selectionSearchFocused : dashboardSurface.hasFocusedTextInput());
        super.render(graphics, mouseX, mouseY, partialTick);
        KawaseBlurPipeline.beginOverlayStage();
        NotificationHudRenderer.render(graphics, VisualSettings.notificationsEnabled());
        drawNativeImeOverlay(graphics);
    }

    private void drawNativeImeOverlay(GuiGraphics graphics) {
        if (!Minecraft.getInstance().getWindow().isFullscreen()) {
            return;
        }
        String composition = WindowsImeBridge.composition();
        String[] candidates = WindowsImeBridge.candidates();
        if ((composition == null || composition.isEmpty())
                && (candidates == null || candidates.length == 0)) {
            return;
        }
        WindowsImeBridge.ClientPoint caret = imeCaretPosition();
        if (caret == null) {
            return;
        }
        int x = clamp((int) Math.round(caret.x() / Math.max(1.0, guiScale)), 6, Math.max(6, width - 96));
        int y = (int) Math.round(caret.y() / Math.max(1.0, guiScale)) + 3;
        StringBuilder candidateText = new StringBuilder();
        if (candidates != null) {
            for (int i = 0; i < Math.min(6, candidates.length); i++) {
                if (candidateText.length() > 0) {
                    candidateText.append("   ");
                }
                candidateText.append(i + 1).append('.').append(' ').append(candidates[i]);
            }
        }
        int compositionW = composition == null ? 0 : TextPainter.width(composition, 11.0f);
        int candidatesW = TextPainter.width(candidateText.toString(), 10.0f);
        int boxW = Math.max(88, Math.min(width - x - 8, Math.max(compositionW, candidatesW) + 18));
        int boxH = candidateText.length() == 0 ? 28 : 48;
        if (y + boxH > height - 6) {
            y = Math.max(6, y - boxH - 30);
        }
        ShapeRenderer.roundRect(graphics, x, y, boxW, boxH, 5,
                themeColor(0xFFF8F9FC, 0xFF202124));
        ShapeRenderer.roundRectBorder(graphics, x, y, boxW, boxH, 5, 1, BORDER);
        if (composition != null && !composition.isEmpty()) {
            TextPainter.drawClipped(graphics, composition, x + 9, y + 7,
                    boxW - 18, 11.0f, TEXT, 1.0f);
            int prefixEnd = Math.min(composition.length(), WindowsImeBridge.compositionCaret());
            int caretX = x + 9 + TextPainter.width(composition.substring(0, prefixEnd), 11.0f);
            graphics.vLine(caretX, y + 6, y + 20, ACCENT_DARK);
        }
        if (candidateText.length() > 0) {
            TextPainter.drawClipped(graphics, candidateText.toString(), x + 9, y + boxH - 18,
                    boxW - 18, 10.0f, MUTED, 1.0f);
        }
    }

    private static void refreshTheme() {
        SHELL = UiPalette.shell();
        SHELL_BORDER = UiPalette.shellBorder();
        SIDEBAR = UiPalette.sidebar();
        SURFACE = UiPalette.surface();
        FIELD = UiPalette.field();
        TRACK = UiPalette.track();
        BORDER = UiPalette.border();
        TEXT = UiPalette.text();
        MUTED = UiPalette.muted();
        SUBTLE = UiPalette.subtle();
        ACCENT = UiPalette.accent();
        ACCENT_DARK = UiPalette.accentDark();
        ACCENT_SOFT = UiPalette.accentSoft();
        GREEN = UiPalette.statusGreen();
        RED = UiPalette.statusRed();
        ORANGE = UiPalette.statusOrange();
        PREVIEW_BG = UiPalette.previewBg();
        PREVIEW_BORDER = UiPalette.previewBorder();
        PREVIEW_TEXT = TEXT;
        PREVIEW_MUTED = MUTED;
        PREVIEW_ACCENT = ACCENT_DARK;
        PREVIEW_ACCENT_SOFT = ACCENT_SOFT;
    }

    private void updateUiAnimationTimings() {
        if (!dashboardAnimationInitialized) {
            animatedDashboardX = dashboardX;
            animatedDashboardY = dashboardY - 16.0f;
            dashboardAnimationAlpha = 0.0f;
            dashboardAnimationInitialized = true;
        }
        if (dragging) {
            animatedDashboardX = dashboardX;
            animatedDashboardY = dashboardY;
        } else {
            animatedDashboardX = lerp(animatedDashboardX, dashboardX, UiAnimationTimings.PANEL_MOVE);
            animatedDashboardY = lerp(animatedDashboardY, dashboardY, UiAnimationTimings.PANEL_MOVE);
        }
        dashboardAnimationAlpha = lerp(dashboardAnimationAlpha, 1.0f, UiAnimationTimings.PANEL_FADE);
        dashboardRenderOffsetX = animatedDashboardX - dashboardX;
        dashboardRenderOffsetY = animatedDashboardY - dashboardY;
    }

    private double dashboardMouseX(double guiMouseX) {
        refreshDashboardScale();
        return fromDashboardScale(guiMouseX * guiScale, true);
    }

    private double dashboardMouseY(double guiMouseY) {
        refreshDashboardScale();
        return fromDashboardScale(guiMouseY * guiScale, false);
    }

    private void refreshDashboardScale() {
        dashboardScale = VisualSettings.dashboardGuiScaleFactor();
    }

    private double fromDashboardScale(double physicalCoord, boolean xAxis) {
        Window window = Minecraft.getInstance().getWindow();
        double origin = (xAxis ? window.getWidth() : window.getHeight()) * 0.5;
        return origin + (physicalCoord - origin) / Math.max(0.01f, dashboardScale);
    }

    private double toDashboardScale(double logicalCoord, boolean xAxis) {
        Window window = Minecraft.getInstance().getWindow();
        double origin = (xAxis ? window.getWidth() : window.getHeight()) * 0.5;
        return origin + (logicalCoord - origin) * dashboardScale;
    }

    private double toGuiX(double logicalX) {
        return toDashboardScale(logicalX, true) / guiScale;
    }

    private double toGuiY(double logicalY) {
        return toDashboardScale(logicalY, false) / guiScale;
    }

    private void applyDashboardScale(GuiGraphics graphics) {
        if (Math.abs(dashboardScale - 1.0f) < 0.001f) {
            return;
        }
        Window window = Minecraft.getInstance().getWindow();
        float originX = window.getWidth() * 0.5f;
        float originY = window.getHeight() * 0.5f;
        graphics.pose().translate(originX, originY, 0.0f);
        graphics.pose().scale(dashboardScale, dashboardScale, 1.0f);
        graphics.pose().translate(-originX, -originY, 0.0f);
    }

    private void renderScaledPanelBackdrop(GuiGraphics graphics, double x, double y, double w, double h,
                                           float radius, float alpha) {
        double scaledX = toDashboardScale(x, true);
        double scaledY = toDashboardScale(y, false);
        KawaseBlurPipeline.renderSurfaceBackdrop(
                graphics,
                width,
                height,
                (float) (scaledX / guiScale),
                (float) (scaledY / guiScale),
                (float) (w * dashboardScale / guiScale),
                (float) (h * dashboardScale / guiScale),
                (float) (radius * dashboardScale / guiScale),
                alpha
        );
    }

    private void registerHudSettingsPopups() {
        registerHudSettingsPopup("watermark", VisualSettings.HUD_WIDGET_WATERMARK,
                VisualSettings.watermarkX(), VisualSettings.watermarkY(),
                WatermarkHudRenderer.width(), WatermarkHudRenderer.height(), this::populateWatermarkSettings);
        registerHudSettingsPopup("hotkeys", VisualSettings.HUD_WIDGET_HOTKEYS,
                VisualSettings.hotkeyHudX(), VisualSettings.hotkeyHudY(),
                HotkeyHudRenderer.width(), HotkeyHudRenderer.height(), this::populateHotkeyHudSettings);
        registerHudSettingsPopup("array-list", VisualSettings.HUD_WIDGET_ARRAY_LIST,
                VisualSettings.arrayListHudX(), VisualSettings.arrayListHudY(),
                ArrayListHudRenderer.width(), ArrayListHudRenderer.height(), this::populateArrayListHudSettings);
        registerHudSettingsPopup("targets", VisualSettings.HUD_WIDGET_TARGETS,
                VisualSettings.targetsHudX(), VisualSettings.targetsHudY(),
                TargetsHudRenderer.width(), TargetsHudRenderer.height(), this::populateTargetsHudSettings);
        registerHudSettingsPopup("backpack", VisualSettings.HUD_WIDGET_BACKPACK,
                VisualSettings.backpackHudX(), VisualSettings.backpackHudY(),
                BackpackHudRenderer.width(), BackpackHudRenderer.height(), this::populateBackpackHudSettings);
        registerHudSettingsPopup("potion-buff", VisualSettings.HUD_WIDGET_POTION_BUFF,
                VisualSettings.potionBuffHudX(), VisualSettings.potionBuffHudY(),
                PotionBuffHudRenderer.width(), PotionBuffHudRenderer.height(), this::populatePotionBuffHudSettings);
        registerHudSettingsPopup("speed", VisualSettings.HUD_WIDGET_SPEED,
                VisualSettings.speedHudX(), VisualSettings.speedHudY(),
                SpeedHudRenderer.width(), SpeedHudRenderer.height(), this::populateSpeedHudSettings);
    }

    private void registerHudSettingsPopup(String key, int widgetMask, int hudX, int hudY, int hudW, int hudH,
                                          WidgetSurface.PopupContent content) {
        if (!VisualSettings.hudWidgetEnabled(widgetMask)) {
            return;
        }
        String id = "hud/" + key;
        int triggerX = (int) Math.round(dashboardMouseX(hudX));
        int triggerY = (int) Math.round(dashboardMouseY(hudY));
        int triggerW = Math.max(1, (int) Math.ceil(hudW * guiScale / dashboardScale));
        int triggerH = Math.max(1, (int) Math.ceil(hudH * guiScale / dashboardScale));
        if (dashboardSurface.hasClick()
                && !dashboardSurface.clickConsumed()
                && dashboardSurface.clickButton() == 1
                && contains(dashboardSurface.clickX(), dashboardSurface.clickY(), triggerX, triggerY, triggerW, triggerH)) {
            hudSettingsPopupPositions.put(id, new HudPopupPosition(
                    (int) Math.round(dashboardSurface.clickX()),
                    (int) Math.round(dashboardSurface.clickY())));
        }
        HudPopupPosition position = hudSettingsPopupPositions.getOrDefault(id,
                new HudPopupPosition(triggerX + triggerW + 8, triggerY));
        dashboardSurface.settingsPopup(id, triggerX, triggerY, triggerW, triggerH,
                position.x(), position.y(), content);
    }

    private void drawShell(GuiGraphics graphics) {
        ShadowRenderer.shadow(graphics, dashboardX, dashboardY, dashboardW, dashboardH, 16,
                ShadowStyle.shell(), dashboardAnimationAlpha);
        ShapeRenderer.roundRect(graphics, dashboardX, dashboardY, dashboardW, dashboardH, 16, SHELL);
        ShapeRenderer.roundRectBorder(graphics, dashboardX, dashboardY, dashboardW, dashboardH, 16, 1, SHELL_BORDER);
    }

    private void drawSidebar(GuiGraphics graphics) {
        int x = dashboardX + 14;
        int y = dashboardY + 14;
        int h = dashboardH - 28;
        ShapeRenderer.roundRect(graphics, x, y, RAIL_W, h, 18, SIDEBAR);
        ShapeRenderer.roundRectBorder(graphics, x, y, RAIL_W, h, 18, 1, SHELL_BORDER);


        int tabX = x + (RAIL_W - TAB_SIZE) / 2;
        int topViewportX = x;
        int topViewportY = y + TAB_TOP_PADDING;
        int topViewportH = sidebarTopViewportH();
        maxSidebarScrollY = Math.max(0, sidebarTopContentH() - topViewportH);
        sidebarScrollY = clamp(sidebarScrollY, 0, maxSidebarScrollY);
        animatedSidebarScrollY = updateAnimatedScroll(animatedSidebarScrollY, sidebarScrollY, maxSidebarScrollY);
        int nextBottomY = y + h - TAB_BOTTOM_PADDING - TAB_SIZE;

        enableContentScissor(topViewportX, topViewportY - SIDEBAR_TAB_CLIP_PAD,
                RAIL_W, topViewportH + SIDEBAR_TAB_CLIP_PAD * 2);
        int nextTopY = Math.round(topViewportY - animatedSidebarScrollY);
        for (SidebarTab tab : SidebarTab.values()) {
            if (!tab.bottomAligned) {
                drawTabButton(graphics, tabX, nextTopY, tab,
                        topViewportX, topViewportY, RAIL_W, topViewportH);
                nextTopY += TAB_SIZE + TAB_GAP;
            }
        }
        RenderSystem.disableScissor();
        drawSidebarFade(graphics, tabX, topViewportY - SIDEBAR_TAB_CLIP_PAD,
                TAB_SIZE, topViewportH + SIDEBAR_TAB_CLIP_PAD * 2);

        for (SidebarTab tab : SidebarTab.values()) {
            if (tab.bottomAligned) {
                drawTabButton(graphics, tabX, nextBottomY, tab);
                nextBottomY -= TAB_SIZE + TAB_GAP;
            }
        }
    }

    private void drawTabButton(GuiGraphics graphics, int x, int y, SidebarTab tab) {
        drawTabButton(graphics, x, y, tab, Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0);
    }

    private void drawTabButton(GuiGraphics graphics, int x, int y, SidebarTab tab,
                               int clipX, int clipY, int clipW, int clipH) {
        boolean clipped = clipW > 0 && clipH > 0;
        boolean mouseInsideClip = !clipped || contains(renderMouseX, renderMouseY, clipX, clipY, clipW, clipH);
        boolean intersectsClip = !clipped || x < clipX + clipW && x + TAB_SIZE > clipX
                && y < clipY + clipH && y + TAB_SIZE > clipY;
        if (!intersectsClip) {
            return;
        }
        boolean active = selectedTab == tab;
        boolean hovered = mouseInsideClip && isHovering(x, y, TAB_SIZE, TAB_SIZE);
        float focus = updateTabButtonAnimation(tab, active ? 1.0f : hovered ? 0.32f : 0.0f);
        int background = lerpColor(themeColor(0xFFF8F9FD, SURFACE), ACCENT_SOFT, focus);
        int border = lerpColor(BORDER, active ? ACCENT : themeColor(0xFFD4D9E8, BORDER), focus);
        int color = lerpColor(MUTED, ACCENT_DARK, focus);
        float iconSize = 14.0f;
        float iconX = x + TAB_SIZE * 0.5f - MSDFRenderer.iconWidth(tab.icon, iconSize) * 0.5f;
        float iconY = y + 10.0f;

        ShapeRenderer.roundRect(graphics, x, y, TAB_SIZE, TAB_SIZE, 12, background);
        ShapeRenderer.roundRectBorder(graphics, x, y, TAB_SIZE, TAB_SIZE, 12, 1, border);
        MSDFRenderer.drawIconCodepoint(graphics, tab.icon, iconX, iconY, iconSize, color);
        if (mouseInsideClip && dashboardSurface.button("tab/" + tab.name(), x, y, TAB_SIZE, TAB_SIZE)) {
            selectTab(tab);
        }
    }

    private int sidebarTopViewportH() {
        int fullTabs = (int) SIDEBAR_TOP_VISIBLE_TABS;
        float partial = SIDEBAR_TOP_VISIBLE_TABS - fullTabs;
        return Math.round(fullTabs * TAB_SIZE + fullTabs * TAB_GAP + partial * TAB_SIZE) + 1;
    }

    private int sidebarTopContentH() {
        int count = 0;
        for (SidebarTab tab : SidebarTab.values()) {
            if (!tab.bottomAligned) {
                count++;
            }
        }
        if (count <= 0) {
            return 0;
        }
        return count * TAB_SIZE + (count - 1) * TAB_GAP;
    }

    private boolean sidebarTopViewportContains(double x, double y) {
        int railX = dashboardX + 14;
        int railY = dashboardY + 14;
        return contains(x, y, railX, railY + TAB_TOP_PADDING, RAIL_W, sidebarTopViewportH());
    }

    private void drawSidebarFade(GuiGraphics graphics, int x, int y, int w, int h) {
        if (maxSidebarScrollY <= 0) {
            sidebarTopFadeAlpha = lerp(sidebarTopFadeAlpha, 0.0f, UiAnimationTimings.CONTROL);
            sidebarBottomFadeAlpha = lerp(sidebarBottomFadeAlpha, 0.0f, UiAnimationTimings.CONTROL);
            return;
        }
        float topTarget = animatedSidebarScrollY > 0.5f ? 1.0f : 0.0f;
        float bottomTarget = animatedSidebarScrollY < maxSidebarScrollY - 0.5f ? 1.0f : 0.0f;
        sidebarTopFadeAlpha = updateSidebarFadeAlpha(sidebarTopFadeAlpha, topTarget);
        sidebarBottomFadeAlpha = updateSidebarFadeAlpha(sidebarBottomFadeAlpha, bottomTarget);
        int fadeH = Math.min(SIDEBAR_FADE_H, Math.max(1, h / 3));
        int clear = alphaColor(SIDEBAR, 0.0f);
        if (sidebarTopFadeAlpha > 0.01f) {
            graphics.fillGradient(x, y - 1, x + w, y + fadeH,
                    alphaColor(SIDEBAR, sidebarTopFadeAlpha), clear);
        }
        if (sidebarBottomFadeAlpha > 0.01f) {
            graphics.fillGradient(x, y + h - fadeH, x + w, y + h + 1,
                    clear, alphaColor(SIDEBAR, sidebarBottomFadeAlpha));
        }
    }

    private float updateSidebarFadeAlpha(float current, float target) {
        float next = lerp(current, target, UiAnimationTimings.CONTROL);
        if (Math.abs(next - target) < 0.01f) {
            next = target;
        }
        return clampFloat(next, 0.0f, 1.0f);
    }

    private float updateTabButtonAnimation(SidebarTab tab, float target) {
        float current = tabButtonAnimations.getOrDefault(tab, target);
        float next = lerp(current, target, UiAnimationTimings.TAB_BUTTON);
        if (Math.abs(next - target) < 0.001f) {
            next = target;
        }
        tabButtonAnimations.put(tab, next);
        return next;
    }

    private float updateConfigIconAnimation(String name, float target) {
        String key = "config-icon/" + name;
        float current = configIconAnimations.getOrDefault(key, target);
        float next = lerp(current, target, UiAnimationTimings.CONTROL);
        if (Math.abs(next - target) < 0.001f) {
            next = target;
        }
        configIconAnimations.put(key, next);
        return next;
    }

    private float updateConfigNameEditAnimation(String name, float target) {
        String key = "config-name-edit/" + name;
        float current = configNameEditAnimations.getOrDefault(key, target);
        float next = lerp(current, target, UiAnimationTimings.CONTROL);
        if (Math.abs(next - target) < 0.001f) {
            next = target;
        }
        configNameEditAnimations.put(key, next);
        return next;
    }

    private void selectTab(SidebarTab tab) {
        if (selectedTab == tab) {
            return;
        }
        rememberScroll(selectedTab);
        previousTab = selectedTab;
        previousContentScrollY = animatedContentScrollY;
        selectedTab = tab;
        retainedSelectedTab = tab;
        contentScrollY = retainedScroll(tab);
        animatedContentScrollY = contentScrollY;
        pendingScrollRestore = true;
        tabTransitionProgress = 0.0f;
        previewEspPopupOpen = false;
        if (tab == SidebarTab.CONFIG) {
            snapConfigRowsOnNextDraw = true;
        }
    }

    private void drawTopBar(GuiGraphics graphics) {
        int x = dashboardX + 86;
        int y = dashboardY + 14;
        int w = dashboardW - 100;
        int h = 36;
        int centerY = y + h / 2;
        int textY = Math.round(MSDFRenderer.textCenteredY(y, h, UiMetrics.Control.FONT_SIZE));
        ShapeRenderer.roundRect(graphics, x, y, w, h, 15, SURFACE);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 15, 1, BORDER);

        text(graphics, selectedTab.title, x + 14, textY, TEXT);
        int dotX = x + 24 + textWidth(selectedTab.title);
        ShapeRenderer.roundRect(graphics, dotX, centerY - 2, 5, 5, 3, ACCENT);
        text(graphics, "v" + CLIENT_VERSION + " - Connected", dotX + 13, textY, MUTED);

        int dotsX = x + w - 58;
        int dotsY = centerY - 4;
        ShapeRenderer.roundRect(graphics, dotsX, dotsY, 8, 8, 4, RED);
        ShapeRenderer.roundRect(graphics, dotsX + 18, dotsY, 8, 8, 4, ORANGE);
        ShapeRenderer.roundRect(graphics, dotsX + 36, dotsY, 8, 8, 4, GREEN);
    }

    private void drawContent(GuiGraphics graphics) {
        int x = dashboardX + 86;
        int y = dashboardY + 58;
        int w = dashboardW - 100;
        int bottom = dashboardY + dashboardH - 36;
        int gap = 14;
        int viewportH = bottom - y;
        contentBoundsX = x;
        contentBoundsY = y;
        contentBoundsW = w;
        contentBoundsH = viewportH;
        boolean hasContentScrollbar = maxContentScrollY > 0;
        contentScrollbarReserve = ScrollbarControl.updateReserve(contentScrollbarReserve, hasContentScrollbar);
        ScrollbarControl.Layout scrollbarLayout = ScrollbarControl.layout(x, y, w, y, viewportH, w, contentScrollbarReserve);
        int contentW = scrollbarLayout.contentW();
        int colW = (contentW - gap) / 2;
        int leftX = x;
        int rightX = leftX + colW + gap;
        int scrollbarX = scrollbarLayout.trackX() + MAIN_SCROLLBAR_X_OFFSET;
        if (!pendingScrollRestore) {
            contentScrollY = clamp(contentScrollY, 0, maxContentScrollY);
            animatedContentScrollY = updateAnimatedScroll(animatedContentScrollY, contentScrollY, maxContentScrollY);
        }

        float transition = updateTabTransition();
        float eased = smoothstep(transition);
        int currentOffsetY = Math.round(-30.0f * (1.0f - eased));
        int previousOffsetY = Math.round(-30.0f * eased);

        dashboardSurface.setInteractionClip(x, y, w, viewportH);
        dashboardSurface.setRenderClip(x, y, w, viewportH);
        enableContentScissor(x, y, w, viewportH);
        int stableContentY = y;
        int displayScrollY = Math.round(animatedContentScrollY);
        int contentY = y - displayScrollY + currentOffsetY;
        boolean contentInteractionLocked = tabTransitionInputLocked() || oreWindowOpen || oreWindowAlpha > 0.02f;
        dashboardSurface.setInteractionLocked(contentInteractionLocked);
        if (previousTab != null && transition < 1.0f) {
            int previousY = y - Math.round(previousContentScrollY) + previousOffsetY;
            drawAnimatedContentPage(graphics, previousTab, leftX, previousY, stableContentY, rightX, colW,
                    contentW, 1.0f - eased);
        }
        int contentHeight = drawAnimatedContentPage(graphics, selectedTab, leftX, contentY, stableContentY,
                rightX, colW, contentW, eased);
        dashboardSurface.setInteractionLocked(oreWindowOpen || oreWindowAlpha > 0.02f);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, dashboardAnimationAlpha);
        ShapeRenderer.setGlobalAlpha(dashboardAnimationAlpha);
        WidgetSurface.setRenderAlpha(dashboardAnimationAlpha);
        RenderSystem.disableScissor();
        dashboardSurface.clearRenderClip();
        dashboardSurface.clearInteractionClip();

        maxContentScrollY = Math.max(0, contentHeight - viewportH);
        contentScrollY = clamp(contentScrollY, 0, maxContentScrollY);
        if (pendingScrollRestore) {
            animatedContentScrollY = contentScrollY;
            pendingScrollRestore = false;
        } else {
            animatedContentScrollY = clampFloat(animatedContentScrollY, 0.0f, maxContentScrollY);
        }
        rememberScroll(selectedTab);
        contentScrollbarAlpha = ScrollbarControl.updateAlpha(contentScrollbarAlpha, maxContentScrollY > 0);
        if (contentScrollbarAlpha > 0.01f) {
            float progress = maxContentScrollY <= 0 ? 0.0f : animatedContentScrollY / (float) maxContentScrollY;
            ScrollbarControl.draw(graphics, scrollbarX, y + SCROLLBAR_VERTICAL_INSET,
                    viewportH - SCROLLBAR_VERTICAL_INSET * 2,
                    progress, viewportH, contentHeight, contentScrollbarAlpha * dashboardAnimationAlpha);
        }
    }

    private static int retainedScroll(SidebarTab tab) {
        return Math.max(0, retainedContentScroll.getOrDefault(tab, 0));
    }

    private void rememberScroll(SidebarTab tab) {
        if (tab != null) {
            retainedContentScroll.put(tab, Math.max(0, contentScrollY));
        }
    }

    private float updateAnimatedScroll(float current, int target, int maxScroll) {
        if (maxScroll <= 0) {
            return 0.0f;
        }
        float clampedCurrent = clampFloat(current, 0.0f, maxScroll);
        float next = lerp(clampedCurrent, target, UiAnimationTimings.CONTENT_SCROLL);
        if (Math.abs(next - target) < 0.35f) {
            next = target;
        }
        return clampFloat(next, 0.0f, maxScroll);
    }

    private int drawAnimatedContentPage(GuiGraphics graphics, SidebarTab tab, int leftX, int y, int stableY,
                                        int rightX, int colW, int fullW, float alpha) {
        SidebarTab previousRenderingTab = renderingContentTab;
        float previousContentAlpha = contentRenderAlpha;
        renderingContentTab = tab;
        float clampedAlpha = clampFloat(alpha * dashboardAnimationAlpha, 0.0f, 1.0f);
        contentRenderAlpha = clampedAlpha;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, clampedAlpha);
        ShapeRenderer.setGlobalAlpha(clampedAlpha);
        WidgetSurface.setRenderAlpha(clampedAlpha);
        try {
            return switch (tab) {
                case COMBAT -> drawCombatPage(graphics, leftX, y, stableY, rightX, colW);
                case RAGEBOT -> drawRagebotPage(graphics, leftX, y, stableY, rightX, colW);
                case BYPASS -> drawBypassPage(graphics, leftX, y, stableY, rightX, colW);
                case WHITELIST -> drawWhitelistPage(graphics, leftX, y, stableY, rightX, colW);
                case MOVEMENT -> drawMovementPage(graphics, leftX, y, stableY, rightX, colW);
                case WORLD -> drawWorldPage(graphics, leftX, y, stableY, rightX, colW);
                case VISUAL -> drawVisualPage(graphics, leftX, y, stableY, rightX, colW);
                case PLAYER -> drawPlayerPage(graphics, leftX, y, stableY, rightX, colW);
                case MISC -> drawMiscPage(graphics, leftX, y, stableY, rightX, colW);
                case MUSIC -> drawMusicPage(graphics, leftX, y, stableY, rightX, colW);
                case CONFIG -> drawConfigPage(graphics, leftX, y, stableY, rightX, colW);
                case USER -> drawUserPage(graphics, leftX, y, stableY, fullW);
            };
        } finally {
            renderingContentTab = previousRenderingTab;
            contentRenderAlpha = previousContentAlpha;
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, dashboardAnimationAlpha);
            ShapeRenderer.setGlobalAlpha(dashboardAnimationAlpha);
            WidgetSurface.setRenderAlpha(dashboardAnimationAlpha);
        }
    }

    private float updateTabTransition() {
        if (previousTab == null) {
            tabTransitionProgress = 1.0f;
            return 1.0f;
        }
        tabTransitionProgress = lerp(tabTransitionProgress, 1.0f, UiAnimationTimings.TAB_TRANSITION);
        if (tabTransitionProgress > 0.985f) {
            tabTransitionProgress = 1.0f;
            previousTab = null;
        }
        return tabTransitionProgress;
    }

    private boolean tabTransitionInputLocked() {
        return previousTab != null || tabTransitionProgress < 1.0f;
    }

    private int drawCombatPage(GuiGraphics graphics, int leftX, int y, int stableY, int rightX, int colW) {
        int killAuraTargetH = measureAimbotCard(leftX, stableY, colW);
        int meleeTargetStableY = stableY + killAuraTargetH + CARD_GAP;
        int meleeTargetH = measureMeleeCard(leftX, meleeTargetStableY, colW);
        int defenseTargetStableY = meleeTargetStableY + meleeTargetH + CARD_GAP;
        int defenseTargetH = measureDefenseCard(leftX, defenseTargetStableY, colW);
        int terrainTargetStableY = defenseTargetStableY + defenseTargetH + CARD_GAP;

        int killAuraH = drawAimbotCard(graphics, leftX, y, stableY, colW);
        int meleeY = y + killAuraH + CARD_GAP;
        int meleeStableY = meleeTargetStableY;
        int meleeH = drawMeleeCard(graphics, leftX, meleeY, meleeStableY, colW);
        int defenseY = meleeY + meleeH + CARD_GAP;
        int defenseStableY = defenseTargetStableY;
        int defenseH = drawDefenseCard(graphics, leftX, defenseY, defenseStableY, colW);
        int terrainY = defenseY + defenseH + CARD_GAP;
        int terrainStableY = terrainTargetStableY;
        int terrainH = drawTerrainCard(graphics, leftX, terrainY, terrainStableY, colW);
        int leftH = killAuraH + CARD_GAP + meleeH + CARD_GAP + defenseH + CARD_GAP
                + terrainH;

        int assistTargetH = measureCombatAssistCard(rightX, stableY, colW);
        int crystalTargetStableY = stableY + assistTargetH + CARD_GAP;
        int crystalTargetH = measureAutoCrystalCard(rightX, crystalTargetStableY, colW);
        int bedTargetStableY = crystalTargetStableY + crystalTargetH + CARD_GAP;
        int bedTargetH = measureBedAuraCard(rightX, bedTargetStableY, colW);
        int anchorTargetStableY = bedTargetStableY + bedTargetH + CARD_GAP;

        int assistH = drawCombatAssistCard(graphics, rightX, y, stableY, colW);
        int crystalY = y + assistH + CARD_GAP;
        int crystalStableY = crystalTargetStableY;
        int crystalH = drawAutoCrystalCard(graphics, rightX, crystalY, crystalStableY, colW);
        int bedY = crystalY + crystalH + CARD_GAP;
        int bedStableY = bedTargetStableY;
        int bedH = drawBedAuraCard(graphics, rightX, bedY, bedStableY, colW);
        int anchorH = drawAnchorAuraCard(graphics, rightX, bedY + bedH + CARD_GAP, anchorTargetStableY, colW);
        int rightH = assistH + CARD_GAP + crystalH + CARD_GAP + bedH + CARD_GAP
                + anchorH;
        return Math.max(leftH, rightH);
    }

    private int drawWhitelistPage(GuiGraphics graphics, int leftX, int y, int stableY, int rightX, int colW) {
        int leftH = drawSocialCard(graphics, leftX, y, stableY, colW);
        int actionsY = y + leftH + CARD_GAP;
        int actionsStableY = stableY + measureSocialCard(leftX, stableY, colW) + CARD_GAP;
        int actionsH = drawFriendActionsCard(graphics, leftX, actionsY, actionsStableY, colW);
        int rightH = drawFriendsListCard(graphics, rightX, y, stableY, colW);
        leftH += CARD_GAP + actionsH;
        return Math.max(leftH, rightH);
    }

    private int drawRagebotPage(GuiGraphics graphics, int leftX, int y, int stableY, int rightX, int colW) {
        int mainTargetH = measureRagebotMainCard(leftX, stableY, colW);
        int selectionStableY = stableY + mainTargetH + CARD_GAP;
        int mainH = drawRagebotMainCard(graphics, leftX, y, stableY, colW);
        int selectionH = drawRagebotSelectionCard(graphics, leftX, y + mainH + CARD_GAP, selectionStableY, colW);
        int leftH = mainH + CARD_GAP + selectionH;

        int otherTargetH = measureRagebotOtherCard(rightX, stableY, colW);
        int antiAimStableY = stableY + otherTargetH + CARD_GAP;
        int otherH = drawRagebotOtherCard(graphics, rightX, y, stableY, colW);
        int antiAimH = drawRagebotAntiAimCard(graphics, rightX, y + otherH + CARD_GAP, antiAimStableY, colW);
        int rightH = otherH + CARD_GAP + antiAimH;
        return Math.max(leftH, rightH);
    }

    private int drawBypassPage(GuiGraphics graphics, int leftX, int y, int stableY, int rightX, int colW) {
        int mainTargetH = measureBypassMainCard(leftX, stableY, colW);
        int rotationStableY = stableY + mainTargetH + CARD_GAP;
        int rotationTargetH = measureBypassRotationCard(leftX, rotationStableY, colW);
        int timingStableY = rotationStableY + rotationTargetH + CARD_GAP;

        int mainH = drawBypassCard(graphics, "MAIN", leftX, y, stableY, colW,
                this::measureBypassMainCard, BypassSettingsPage::populateMain);
        int rotationY = y + mainH + CARD_GAP;
        int rotationH = drawBypassCard(graphics, "ROTATION", leftX, rotationY, rotationStableY, colW,
                this::measureBypassRotationCard, BypassSettingsPage::populateRotation);
        int timingY = rotationY + rotationH + CARD_GAP;
        int timingH = drawBypassCard(graphics, "TIMING", leftX, timingY,
                timingStableY, colW, this::measureBypassTimingCard, BypassSettingsPage::populateTiming);
        int disablerY = timingY + timingH + CARD_GAP;
        int disablerStableY = timingStableY + timingH + CARD_GAP;
        int disablerH = drawBypassCard(graphics, "DISABLER", leftX, disablerY,
                disablerStableY, colW, this::measureDisablerCard, DisablerSettingsPage::populate);
        int pingY = disablerY + disablerH + CARD_GAP;
        int pingStableY = disablerStableY + disablerH + CARD_GAP;
        int pingH = drawBypassCard(graphics, "PING SPOOF", leftX, pingY,
                pingStableY, colW, this::measurePingSpoofCard, PingSpoofSettingsPage::populate);
        int noRotateY = pingY + pingH + CARD_GAP;
        int noRotateStableY = pingStableY + pingH + CARD_GAP;
        int noRotateH = drawBypassCard(graphics, "NO ROTATE", leftX, noRotateY,
                noRotateStableY, colW, this::measureNoRotateCard, NoRotateSettingsPage::populate);
        int leftH = mainH + CARD_GAP + rotationH + CARD_GAP + timingH + CARD_GAP
                + disablerH + CARD_GAP + pingH + CARD_GAP + noRotateH;

        int networkTargetH = measureBypassNetworkCard(rightX, stableY, colW);
        int movementStableY = stableY + networkTargetH + CARD_GAP;
        int networkH = drawBypassCard(graphics, "NETWORK", rightX, y, stableY, colW,
                this::measureBypassNetworkCard, BypassSettingsPage::populateNetwork);
        int movementH = drawBypassCard(graphics, "RECOVERY", rightX, y + networkH + CARD_GAP,
                movementStableY, colW, this::measureBypassMovementCard, BypassSettingsPage::populateMovement);
        return Math.max(leftH, networkH + CARD_GAP + movementH);
    }

    private int drawBypassCard(GuiGraphics graphics, String title, int x, int y, int stableY, int w,
                               CardMeasurer measurer, java.util.function.Consumer<WidgetSurface.Frame> populator) {
        int h = measurer.measure(x, stableY, w);
        h = drawCard(graphics, title, x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populator.accept(gui);
        return h;
    }

    private int drawMovementPage(GuiGraphics graphics, int leftX, int y, int stableY, int rightX, int colW) {
        int leftY = y;
        int leftStableY = stableY;
        int generalH = drawMovementGroupCard(graphics, "GENERAL", leftX, leftY, leftStableY, colW, this::populateMovementGeneral);
        leftY += generalH + CARD_GAP;
        leftStableY += measureMovementGroupCard(leftX, leftStableY, colW, this::populateMovementGeneral) + CARD_GAP;
        int scaffoldH = drawScaffoldCard(graphics, leftX, leftY, leftStableY, colW);
        leftY += scaffoldH + CARD_GAP;
        leftStableY += measureScaffoldCard(leftX, leftStableY, colW) + CARD_GAP;
        int exploitH = drawMovementGroupCard(graphics, "EXPLOIT", leftX, leftY, leftStableY, colW,
                MovementSettingsPage::populateMovementExploit);
        leftY += exploitH + CARD_GAP;
        leftStableY += measureMovementGroupCard(leftX, leftStableY, colW,
                MovementSettingsPage::populateMovementExploit) + CARD_GAP;
        int leftH = generalH + CARD_GAP + scaffoldH + CARD_GAP + exploitH;

        int rightY = y;
        int rightStableY = stableY;
        int flightH = drawMovementGroupCard(graphics, "FLIGHT", rightX, rightY, rightStableY, colW,
                MovementSettingsPage::populateMovementFlight);
        rightY += flightH + CARD_GAP;
        rightStableY += measureMovementGroupCard(rightX, rightStableY, colW,
                MovementSettingsPage::populateMovementFlight) + CARD_GAP;
        int vehicleH = drawMovementGroupCard(graphics, "VEHICLE", rightX, rightY, rightStableY, colW,
                MovementSettingsPage::populateMovementVehicle);
        rightY += vehicleH + CARD_GAP;
        rightStableY += measureMovementGroupCard(rightX, rightStableY, colW,
                MovementSettingsPage::populateMovementVehicle) + CARD_GAP;
        int environmentH = drawMovementGroupCard(graphics, "ENVIRONMENT", rightX, rightY, rightStableY, colW,
                MovementSettingsPage::populateMovementEnvironment);
        rightY += environmentH + CARD_GAP;
        rightStableY += measureMovementGroupCard(rightX, rightStableY, colW,
                MovementSettingsPage::populateMovementEnvironment) + CARD_GAP;
        int utilityH = drawMovementUtilityCard(graphics, rightX, rightY, rightStableY, colW);
        int rightH = flightH + CARD_GAP + vehicleH + CARD_GAP + environmentH + CARD_GAP + utilityH;
        return Math.max(leftH, rightH);
    }

    private int drawWorldPage(GuiGraphics graphics, int leftX, int y, int stableY, int rightX, int colW) {
        int resourceStableY = stableY;
        int resourceH = drawWorldResourceEspCard(graphics, leftX, y, resourceStableY, colW);
        int buildStableY = resourceStableY + measureWorldResourceEspCard(leftX, resourceStableY, colW) + CARD_GAP;
        int buildY = y + resourceH + CARD_GAP;
        int buildH = drawWorldBuildMineCard(graphics, leftX, buildY, buildStableY, colW);
        int blockStableY = buildStableY + measureWorldBuildMineCard(leftX, buildStableY, colW) + CARD_GAP;
        int blockY = buildY + buildH + CARD_GAP;
        int blockH = drawWorldSpecialBlocksCard(graphics, leftX, blockY, blockStableY, colW);
        int leftH = resourceH + CARD_GAP + buildH + CARD_GAP + blockH;

        int renderStableY = stableY;
        int renderH = drawWorldRenderCard(graphics, rightX, y, renderStableY, colW);
        int utilityStableY = renderStableY + measureWorldRenderCard(rightX, renderStableY, colW) + CARD_GAP;
        int utilityY = y + renderH + CARD_GAP;
        int utilityH = drawWorldUtilityCard(graphics, rightX, utilityY, utilityStableY, colW);
        int rightH = renderH + CARD_GAP + utilityH;
        return Math.max(leftH, rightH);
    }

    private int drawVisualPage(GuiGraphics graphics, int leftX, int y, int stableY, int rightX, int colW) {
        int generalStableH = measureVisualGeneralStableCard(leftX, stableY, colW);
        int generalH = drawVisualGeneralCard(graphics, leftX, y, stableY, colW);
        int modelStableY = stableY + generalStableH + CARD_GAP;
        int modelH = drawVisualModelCard(graphics, leftX, y + generalH + CARD_GAP, modelStableY, colW);
        int leftH = generalH + CARD_GAP + modelH;
        int rightH = drawPlayerPreviewCard(graphics, rightX, y, stableY, colW);
        return Math.max(leftH, rightH);
    }

    private int drawMiscPage(GuiGraphics graphics, int leftX, int y, int stableY, int rightX, int colW) {
        int leftH = drawMiscCard(graphics, leftX, y, stableY, colW);
        int rightH = drawSettingsCard(graphics, rightX, y, stableY, colW);
        return Math.max(leftH, rightH);
    }

    private int drawMusicPage(GuiGraphics graphics, int leftX, int y, int stableY, int rightX, int colW) {
        int pageW = rightX + colW - leftX;
        int pageH = Math.max(350, contentBoundsH);
        int headerH = 46;
        int playerH = 58;
        int bodyY = y + headerH;
        int bodyH = pageH - headerH - playerH;
        int listW = Math.round(pageW * 0.62f);
        int detailX = leftX + listW;
        int detailW = pageW - listW;
        int playerY = y + pageH - playerH;

        ShapeRenderer.roundRect(graphics, leftX, y, pageW, pageH, 8, SURFACE);
        ShapeRenderer.roundRectBorder(graphics, leftX, y, pageW, pageH, 8, 1,
                alphaColor(BORDER, contentRenderAlpha));
        graphics.fill(leftX, bodyY, leftX + pageW, bodyY + 1, alphaColor(BORDER, contentRenderAlpha));

        if (musicHomeVisible) {
            MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_MUSIC,
                    leftX + 14, y + 15, 15.0f, alphaColor(ACCENT, contentRenderAlpha));
        } else {
            if (drawMusicIconButton(graphics, "back-level", leftX + 8, y + 11, 24,
                    FontAwesomeIcons.ICON_FA_ARROW_LEFT, false, false)) {
                navigateMusicBack();
            }
            if (drawMusicIconButton(graphics, "go-home", leftX + 36, y + 11, 24,
                    FontAwesomeIcons.ICON_FA_HOUSE, false, false)) {
                navigateMusicHome();
            }
        }
        int musicTitleX = leftX + (musicHomeVisible ? 38 : 68);
        int searchButtonSize = 26;
        int searchGroupW = clamp(Math.round(pageW * 0.34f), 190, 250);
        int searchGroupCenterX = leftX + Math.round(pageW * 0.67f);
        int searchGroupX = clamp(searchGroupCenterX - searchGroupW / 2,
                leftX + 144, leftX + pageW - searchGroupW - 12);
        int searchButtonX = searchGroupX + searchGroupW - searchButtonSize;
        int searchX = searchGroupX;
        int searchW = Math.max(100, searchGroupW - searchButtonSize - 7);
        int musicTitleW = Math.max(32, searchX - musicTitleX - 10);
        drawMusicClippedText(graphics,
                musicHomeVisible ? "NETEASE MUSIC" : NeteaseMusicService.resultTitle(),
                musicTitleX, y + 15, musicTitleW, 11.0f, TEXT);

        WidgetSurface.Frame gui = dashboardSurface.overlay(graphics,
                contentScope() + "/netease", dashboardX, dashboardY);
        musicSearchText = gui.inputText("Netease Search", musicSearchText,
                searchX, y + 10, searchW, 26, value -> musicSearchText = value,
                false, 2, true);
        if (drawMusicIconButton(graphics, "search", searchButtonX, y + 10, searchButtonSize,
                FontAwesomeIcons.ICON_FA_MAGNIFYING_GLASS, true, false)) {
            boolean rememberCurrent = !musicHomeVisible;
            if (!musicSearchText.trim().isEmpty()) {
                if (rememberCurrent) {
                    retainedMusicNavigationScroll.addFirst(musicResultScrollY);
                }
                musicHomeVisible = false;
                musicExpandedDetails = false;
                musicResultScrollY = 0;
                musicAnimatedResultScrollY = 0.0f;
                NeteaseMusicService.search(musicSearchText, rememberCurrent);
            } else {
                NeteaseMusicService.search(musicSearchText, false);
            }
        }

        updateMusicPageTransition();
        float previousMusicAlpha = contentRenderAlpha;
        float musicPageAlpha = smoothstep(musicPageTransitionProgress);
        contentRenderAlpha = previousMusicAlpha * musicPageAlpha;
        ShapeRenderer.setGlobalAlpha(contentRenderAlpha);
        WidgetSurface.setRenderAlpha(contentRenderAlpha);
        if (musicPageTransitionProgress < 0.99f) {
            dashboardSurface.setInteractionLocked(true);
        }
        graphics.pose().pushPose();
        graphics.pose().translate(12.0f * (1.0f - musicPageAlpha), 0.0f, 0.0f);
        NeteaseMusicService.Song current = NeteaseMusicService.currentSong();
        try {
            if (musicHomeVisible) {
                NeteaseMusicService.requestFeaturedPlaylists();
                drawMusicHome(graphics, leftX, bodyY, pageW, bodyH);
            } else if (musicExpandedDetails) {
                musicListW = 0;
                musicListH = 0;
                drawMusicExpandedDetails(graphics, current, leftX, bodyY, pageW, bodyH);
            } else {
                graphics.fill(detailX, bodyY, detailX + 1, playerY,
                        alphaColor(BORDER, contentRenderAlpha));

                drawMusicSearchWorkspace(graphics, current, leftX, bodyY, listW, playerY);

                drawMusicDetails(graphics, current, detailX, bodyY, detailW, bodyH);
            }
        } finally {
            graphics.pose().popPose();
            contentRenderAlpha = previousMusicAlpha;
            ShapeRenderer.setGlobalAlpha(previousMusicAlpha);
            WidgetSurface.setRenderAlpha(previousMusicAlpha);
        }
        musicPlayerX = leftX;
        musicPlayerY = playerY;
        musicPlayerW = pageW;
        musicPlayerH = playerH;
        drawMusicPlayerBar(graphics, current, leftX, playerY, pageW, playerH);
        return pageH;
    }

    private void drawMusicSearchWorkspace(GuiGraphics graphics, NeteaseMusicService.Song current,
                                          int leftX, int bodyY, int listW, int playerY) {
        int tableX = leftX + 12;
        int tableW = listW - 24;
        NeteaseMusicService.SearchCategory category = NeteaseMusicService.searchCategory();
        boolean searchContext = NeteaseMusicService.hasActiveSearch();
        if (searchContext) {
            drawMusicSearchTabs(graphics, tableX, bodyY + 7, tableW, category);
        }

        int tableHeaderY = bodyY + (searchContext ? 39 : 10);
        int numberW = 24;
        int coverColumnW = 34;
        int valueW = 48;
        int scrollbarReserve = 14;
        int columnGap = 7;
        int availableTextW = Math.max(150,
                tableW - numberW - coverColumnW - valueW - scrollbarReserve - columnGap * 2);
        int secondaryW = Math.max(62, Math.round(availableTextW * 0.34f));
        int titleW = Math.max(88, availableTextW - secondaryW);
        int titleX = tableX + numberW + coverColumnW;
        int secondaryX = titleX + titleW + columnGap;
        int valueX = tableX + tableW - scrollbarReserve - valueW;

        String titleHeader = category == NeteaseMusicService.SearchCategory.SONGS ? "TITLE"
                : category == NeteaseMusicService.SearchCategory.PLAYLISTS ? "PLAYLIST" : "USER";
        String secondaryHeader = category == NeteaseMusicService.SearchCategory.SONGS ? "ARTIST"
                : category == NeteaseMusicService.SearchCategory.PLAYLISTS ? "TRACKS" : "BIO";
        String valueHeader = category == NeteaseMusicService.SearchCategory.SONGS ? "TIME"
                : category == NeteaseMusicService.SearchCategory.PLAYLISTS ? "PLAYS" : "FOLLOWS";
        unicodeText(graphics, "#", tableX + 7, tableHeaderY + 3, 9.0f, MUTED);
        unicodeText(graphics, titleHeader, titleX, tableHeaderY + 3, 9.0f, MUTED);
        unicodeText(graphics, secondaryHeader, secondaryX, tableHeaderY + 3, 9.0f, MUTED);
        unicodeText(graphics, valueHeader, valueX, tableHeaderY + 3, 9.0f, MUTED);

        List<?> results = switch (category) {
            case SONGS -> NeteaseMusicService.searchResults();
            case PLAYLISTS -> NeteaseMusicService.playlistSearchResults();
            case USERS -> NeteaseMusicService.userSearchResults();
        };
        int listY = tableHeaderY + 22;
        int rowH = 36;
        int listH = Math.max(1, playerY - listY - 8);
        musicListX = tableX;
        musicListY = listY;
        musicListW = tableW;
        musicListH = listH;
        musicResultContentH = Math.max(listH, results.size() * rowH);
        musicMaxResultScrollY = Math.max(0, musicResultContentH - listH);
        musicResultScrollY = clamp(musicResultScrollY, 0, musicMaxResultScrollY);
        if (!results.isEmpty() && musicMaxResultScrollY > 0
                && musicResultScrollY >= musicMaxResultScrollY - rowH * 2) {
            NeteaseMusicService.loadMoreSearchResults();
        }
        musicAnimatedResultScrollY = updateAnimatedScroll(
                musicAnimatedResultScrollY, musicResultScrollY, musicMaxResultScrollY);
        musicResultScrollbarAlpha = ScrollbarControl.updateAlpha(
                musicResultScrollbarAlpha, musicMaxResultScrollY > 0);

        boolean listVisible = enableIntersectedContentScissor(
                contentBoundsX, contentBoundsY, contentBoundsW, contentBoundsH,
                musicListX, musicListY, musicListW, musicListH);
        try {
            if (!listVisible) {
                return;
            }
            int animatedScroll = Math.round(musicAnimatedResultScrollY);
            int listBottom = listY + listH;
            boolean mouseInsideList = contains(renderMouseX, renderMouseY,
                    musicListX, musicListY, musicListW, musicListH);
            for (int i = 0; i < results.size(); i++) {
                Object item = results.get(i);
                long itemId;
                String primary;
                String secondary;
                String value;
                int textureId;
                int fallbackIcon;
                boolean circularImage;
                boolean vip = false;
                boolean selected = false;
                switch (category) {
                    case SONGS -> {
                        NeteaseMusicService.Song song = (NeteaseMusicService.Song) item;
                        itemId = song.id();
                        primary = song.name();
                        secondary = song.artist();
                        value = formatMusicDuration(song.durationMs());
                        textureId = NeteaseMusicService.coverTextureId(song);
                        fallbackIcon = FontAwesomeIcons.ICON_FA_MUSIC;
                        circularImage = false;
                        vip = song.vipRequired();
                        selected = current != null && current.id() == song.id();
                    }
                    case PLAYLISTS -> {
                        NeteaseMusicService.Playlist playlist = (NeteaseMusicService.Playlist) item;
                        itemId = playlist.id();
                        primary = playlist.name();
                        secondary = playlist.trackCount() + " tracks";
                        value = formatMusicCount(playlist.playCount());
                        textureId = NeteaseMusicService.playlistCoverTextureId(playlist);
                        fallbackIcon = FontAwesomeIcons.ICON_FA_LIST;
                        circularImage = false;
                    }
                    case USERS -> {
                        NeteaseMusicService.UserProfile user = (NeteaseMusicService.UserProfile) item;
                        itemId = user.id();
                        primary = user.nickname();
                        secondary = user.signature().isBlank() ? user.playlistCount() + " playlists" : user.signature();
                        value = formatMusicCount(user.followers());
                        textureId = NeteaseMusicService.userAvatarTextureId(user);
                        fallbackIcon = FontAwesomeIcons.ICON_FA_USER;
                        circularImage = true;
                    }
                    default -> throw new IllegalStateException("Unsupported search category");
                }

                int rowY = listY + i * rowH - animatedScroll;
                int rowBottom = rowY + rowH - 2;
                if (rowBottom <= listY || rowY >= listBottom) {
                    continue;
                }
                boolean hovered = mouseInsideList && contains(renderMouseX, renderMouseY,
                        tableX, rowY, tableW - scrollbarReserve, rowH);
                String animationId = "music-" + category.name().toLowerCase(java.util.Locale.ROOT)
                        + "-row-" + itemId;
                float hover = previewItemAnimation(animationId, "hover", hovered ? 1.0f : 0.0f);
                int rowFill = selected
                        ? alphaColor(ACCENT_SOFT, contentRenderAlpha * 0.58f)
                        : alphaColor(ACCENT_SOFT, hover * contentRenderAlpha * 0.24f);
                if (selected || hover > 0.01f) {
                    ShapeRenderer.roundRect(graphics, tableX, rowY,
                            tableW - scrollbarReserve, rowH - 2, 4, rowFill);
                }
                int rowText = selected ? ACCENT_DARK : TEXT;
                unicodeText(graphics, String.format(java.util.Locale.ROOT, "%02d", i + 1),
                        tableX + 6, rowY + 11, 9.0f, selected ? ACCENT_DARK : MUTED);

                int coverSize = 26;
                int coverX = tableX + numberW;
                int coverY = rowY + 4;
                drawMusicSearchImage(graphics, textureId, fallbackIcon,
                        coverX, coverY, coverSize, circularImage);

                int primaryX = titleX;
                int primaryW = titleW - 3;
                if (vip) {
                    unicodeText(graphics, "VIP", primaryX, rowY + 11, 7.0f, ACCENT_DARK);
                    primaryX += 20;
                    primaryW = Math.max(1, primaryW - 20);
                }
                drawMusicClippedText(graphics, primary, primaryX, rowY + 10,
                        primaryW, 10.0f, rowText);
                drawMusicClippedText(graphics, secondary, secondaryX, rowY + 10,
                        secondaryW - 3, 9.0f, MUTED);
                drawMusicClippedText(graphics, value, valueX, rowY + 10,
                        valueW, 9.0f, MUTED);

                int buttonY = Math.max(rowY, listY);
                int buttonBottom = Math.min(rowBottom, listBottom);
                if (buttonBottom > buttonY && dashboardSurface.button(
                        "music/search/" + category.name() + "/" + itemId,
                        tableX, buttonY, tableW - scrollbarReserve, buttonBottom - buttonY)) {
                    switch (category) {
                        case SONGS -> NeteaseMusicService.play((NeteaseMusicService.Song) item);
                        case PLAYLISTS -> {
                            retainedMusicNavigationScroll.addFirst(musicResultScrollY);
                            musicResultScrollY = 0;
                            musicAnimatedResultScrollY = 0.0f;
                            NeteaseMusicService.openPlaylist((NeteaseMusicService.Playlist) item);
                        }
                        case USERS -> {
                            retainedMusicNavigationScroll.addFirst(musicResultScrollY);
                            musicResultScrollY = 0;
                            musicAnimatedResultScrollY = 0.0f;
                            NeteaseMusicService.openUser((NeteaseMusicService.UserProfile) item);
                        }
                    }
                }
            }
            if (results.isEmpty()) {
                unicodeText(graphics, NeteaseMusicService.searchStatus(category), titleX,
                        listY + 14, 10.0f, MUTED);
            }
            if (musicResultScrollbarAlpha > 0.01f) {
                float progress = musicMaxResultScrollY <= 0
                        ? 0.0f : musicAnimatedResultScrollY / (float) musicMaxResultScrollY;
                ScrollbarControl.draw(graphics,
                        musicResultScrollbarTrackX(), musicListY + ScrollbarControl.VERTICAL_INSET,
                        Math.max(1, musicListH - ScrollbarControl.VERTICAL_INSET * 2),
                        progress, musicListH, musicResultContentH,
                        musicResultScrollbarAlpha * contentRenderAlpha);
            }
        } finally {
            enableContentScissor(contentBoundsX, contentBoundsY, contentBoundsW, contentBoundsH);
        }
    }

    private void drawMusicSearchTabs(GuiGraphics graphics, int x, int y, int w,
                                     NeteaseMusicService.SearchCategory selected) {
        int groupW = Math.min(w, 216);
        int groupH = 24;
        int itemW = groupW / NeteaseMusicService.SearchCategory.values().length;
        ShapeRenderer.roundRect(graphics, x, y, groupW, groupH, 5,
                alphaColor(themeColor(0xFFF0F1F3, 0xFF252629), contentRenderAlpha));
        NeteaseMusicService.SearchCategory[] values = NeteaseMusicService.SearchCategory.values();
        for (int i = 0; i < values.length; i++) {
            NeteaseMusicService.SearchCategory category = values[i];
            int itemX = x + i * itemW;
            boolean active = category == selected;
            boolean hovered = contains(renderMouseX, renderMouseY, itemX, y, itemW, groupH);
            float hover = previewItemAnimation("music-search-tab-" + category.name(), "hover",
                    hovered ? 1.0f : 0.0f);
            if (active || hover > 0.01f) {
                ShapeRenderer.roundRect(graphics, itemX + 2, y + 2, itemW - 4, groupH - 4, 4,
                        active ? alphaColor(ACCENT_SOFT, contentRenderAlpha * 0.82f)
                                : alphaColor(ACCENT_SOFT, hover * contentRenderAlpha * 0.34f));
            }
            String label = category.label();
            int textW = TextPainter.unicodeWidth(label, 9.0f);
            unicodeText(graphics, label, itemX + (itemW - textW) / 2, y + 7, 9.0f,
                    active ? ACCENT_DARK : MUTED);
            if (dashboardSurface.button("music/search-tab/" + category.name(),
                    itemX, y, itemW, groupH) && !active) {
                NeteaseMusicService.setSearchCategory(category);
                musicResultScrollY = 0;
                musicAnimatedResultScrollY = 0.0f;
            }
        }
    }

    private void drawMusicSearchImage(GuiGraphics graphics, int textureId, int fallbackIcon,
                                      int x, int y, int size, boolean circular) {
        int radius = circular ? size / 2 : 4;
        if (textureId > 0) {
            TextureRenderer.roundedTexture(graphics, textureId, x, y, size, size,
                    radius, contentRenderAlpha);
            return;
        }
        ShapeRenderer.roundRect(graphics, x, y, size, size, radius,
                alphaColor(themeColor(0xFFECECEC, 0xFF2C2C2C), contentRenderAlpha));
        float iconSize = 8.0f;
        float iconX = x + size * 0.5f
                - MSDFRenderer.iconWidth(fallbackIcon, iconSize) * 0.5f;
        MSDFRenderer.drawIconCodepoint(graphics, fallbackIcon, iconX, y + 8,
                iconSize, alphaColor(ACCENT, contentRenderAlpha));
    }

    private static String formatMusicCount(long value) {
        if (value < 1_000L) {
            return Long.toString(Math.max(0L, value));
        }
        if (value < 1_000_000L) {
            return String.format(java.util.Locale.ROOT, "%.1fK", value / 1_000.0D);
        }
        return String.format(java.util.Locale.ROOT, "%.1fM", value / 1_000_000.0D);
    }

    private void navigateMusicBack() {
        if (musicExpandedDetails) {
            musicExpandedDetails = false;
            return;
        }
        musicResultScrollY = 0;
        musicAnimatedResultScrollY = 0.0f;
        if (NeteaseMusicService.navigateBack()) {
            musicHomeVisible = false;
            musicSearchText = NeteaseMusicService.activeSearchQuery();
            Integer previousScroll = retainedMusicNavigationScroll.pollFirst();
            musicResultScrollY = previousScroll == null ? 0 : Math.max(0, previousScroll);
            musicAnimatedResultScrollY = musicResultScrollY;
            return;
        }
        navigateMusicHome();
    }

    private void navigateMusicHome() {
        musicHomeVisible = true;
        musicExpandedDetails = false;
        musicResultScrollY = 0;
        musicAnimatedResultScrollY = 0.0f;
        retainedMusicNavigationScroll.clear();
        NeteaseMusicService.clearNavigationHistory();
    }

    private void updateMusicPageTransition() {
        int targetPage = musicHomeVisible ? 0 : musicExpandedDetails ? 2 : 1;
        if (musicRenderedPage < 0) {
            musicRenderedPage = targetPage;
            musicPageTransitionProgress = 1.0f;
            return;
        }
        if (targetPage != musicRenderedPage) {
            musicRenderedPage = targetPage;
            musicPageTransitionProgress = 0.0f;
        }
        musicPageTransitionProgress = lerp(
                musicPageTransitionProgress, 1.0f, UiAnimationTimings.TAB_TRANSITION);
        if (musicPageTransitionProgress > 0.985f) {
            musicPageTransitionProgress = 1.0f;
        }
    }

    private void drawMusicHome(GuiGraphics graphics, int x, int y, int w, int h) {
        List<NeteaseMusicService.Playlist> playlists = NeteaseMusicService.featuredPlaylists();
        int innerX = x + 14;
        int innerW = Math.max(1, w - 28);
        unicodeText(graphics, "HOT PLAYLISTS", innerX, y + 12, 10.0f, TEXT);
        if (drawMusicIconButton(graphics, "refresh-playlists", x + w - 39, y + 5, 24,
                FontAwesomeIcons.ICON_FA_ARROWS_ROTATE, false, false)) {
            musicResultScrollY = 0;
            musicAnimatedResultScrollY = 0.0f;
            NeteaseMusicService.refreshFeaturedPlaylists();
        }

        int gridY = y + 33;
        int gridH = Math.max(1, h - 39);
        musicListX = innerX;
        musicListY = gridY;
        musicListW = innerW;
        musicListH = gridH;
        if (playlists.isEmpty()) {
            musicResultContentH = gridH;
            musicMaxResultScrollY = 0;
            musicResultScrollY = 0;
            musicAnimatedResultScrollY = 0.0f;
            String statusText = NeteaseMusicService.featuredPlaylistsStatus();
            int statusW = TextPainter.msdfWidth(statusText, 10.0f);
            unicodeText(graphics, statusText,
                    innerX + Math.max(0, (innerW - statusW) / 2), y + Math.max(34, h / 2),
                    10.0f, MUTED);
            return;
        }

        int scrollbarReserve = 14;
        int cardAreaW = Math.max(1, innerW - scrollbarReserve);
        int columns = clamp(cardAreaW / 94, 4, 6);
        int columnGap = 10;
        int cardW = Math.max(56, (cardAreaW - columnGap * (columns - 1)) / columns);
        int rowGap = 8;
        int rowH = Math.max(82, Math.min(112, (gridH - rowGap) / 2));
        int coverSize = Math.max(42, Math.min(cardW - 8, rowH - 29));
        int rowCount = (playlists.size() + columns - 1) / columns;
        musicResultContentH = Math.max(gridH, rowCount * (rowH + rowGap) - rowGap);
        musicMaxResultScrollY = Math.max(0, musicResultContentH - gridH);
        musicResultScrollY = clamp(musicResultScrollY, 0, musicMaxResultScrollY);
        musicAnimatedResultScrollY = updateAnimatedScroll(
                musicAnimatedResultScrollY, musicResultScrollY, musicMaxResultScrollY);
        musicResultScrollbarAlpha = ScrollbarControl.updateAlpha(
                musicResultScrollbarAlpha, musicMaxResultScrollY > 0);
        int animatedScroll = Math.round(musicAnimatedResultScrollY);
        int gridBottom = gridY + gridH;
        boolean mouseInsideGrid = contains(renderMouseX, renderMouseY,
                musicListX, musicListY, musicListW, musicListH);

        enableContentScissor(innerX, gridY, innerW, gridH);
        try {
            for (int i = 0; i < playlists.size(); i++) {
                NeteaseMusicService.Playlist playlist = playlists.get(i);
                int column = i % columns;
                int row = i / columns;
                int cardX = innerX + column * (cardW + columnGap);
                int cardY = gridY + row * (rowH + rowGap) - animatedScroll;
                if (cardY + rowH <= gridY || cardY >= gridBottom) {
                    continue;
                }
                boolean hovered = mouseInsideGrid && contains(renderMouseX, renderMouseY,
                        cardX, cardY, cardW, rowH);
                float hover = previewItemAnimation("music-playlist-" + playlist.id(), "hover",
                        hovered ? 1.0f : 0.0f);
                if (hover > 0.01f) {
                    ShapeRenderer.roundRect(graphics, cardX, cardY, cardW, rowH, 6,
                            alphaColor(ACCENT_SOFT, hover * contentRenderAlpha * 0.42f));
                }

                int coverX = cardX + (cardW - coverSize) / 2;
                int coverY = cardY + 3;
                int textureId = NeteaseMusicService.playlistCoverTextureId(playlist);
                if (textureId > 0) {
                    TextureRenderer.roundedTexture(graphics, textureId,
                            coverX, coverY, coverSize, coverSize, 6, contentRenderAlpha);
                } else {
                    ShapeRenderer.roundRect(graphics, coverX, coverY, coverSize, coverSize, 6,
                            alphaColor(themeColor(0xFFF0F0F0, 0xFF282828), contentRenderAlpha));
                    float iconSize = Math.max(12.0f, coverSize * 0.24f);
                    float iconX = coverX + (coverSize
                            - MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_MUSIC, iconSize)) * 0.5f;
                    MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_MUSIC,
                            iconX, coverY + (coverSize - iconSize) * 0.5f - 1.0f,
                            iconSize, alphaColor(ACCENT, contentRenderAlpha));
                }

                int textY = coverY + coverSize + 5;
                drawMusicClippedText(graphics, playlist.name(), cardX + 4, textY,
                        cardW - 8, 8.5f, TEXT);
                drawMusicClippedText(graphics, playlist.trackCount() + " tracks", cardX + 4,
                        textY + 13, cardW - 8, 7.0f, MUTED);
                int buttonY = Math.max(cardY, gridY);
                int buttonBottom = Math.min(cardY + rowH, gridBottom);
                if (buttonBottom > buttonY
                        && dashboardSurface.button("music/home/playlist/" + playlist.id(),
                        cardX, buttonY, cardW, buttonBottom - buttonY)) {
                    musicHomeVisible = false;
                    musicExpandedDetails = false;
                    musicResultScrollY = 0;
                    musicAnimatedResultScrollY = 0.0f;
                    NeteaseMusicService.openPlaylist(playlist, false);
                }
            }
            if (musicResultScrollbarAlpha > 0.01f) {
                float progress = musicMaxResultScrollY <= 0
                        ? 0.0f : musicAnimatedResultScrollY / (float) musicMaxResultScrollY;
                ScrollbarControl.draw(graphics,
                        musicResultScrollbarTrackX(), musicListY + ScrollbarControl.VERTICAL_INSET,
                        Math.max(1, musicListH - ScrollbarControl.VERTICAL_INSET * 2),
                        progress, musicListH, musicResultContentH,
                        musicResultScrollbarAlpha * contentRenderAlpha);
            }
        } finally {
            restoreContentScissor();
        }
    }

    private void drawMusicDetails(GuiGraphics graphics, NeteaseMusicService.Song song,
                                  int x, int y, int w, int h) {
        int coverSize = Math.max(68, Math.min(84, Math.min(w / 3, h - 128)));
        int coverX = x + 14;
        int coverY = y + 16;
        int textureId = NeteaseMusicService.coverTextureId(song);
        if (textureId > 0) {
            TextureRenderer.roundedTexture(graphics, textureId,
                    coverX, coverY, coverSize, coverSize, 7, contentRenderAlpha);
        } else {
            ShapeRenderer.roundRect(graphics, coverX, coverY, coverSize, coverSize, 7,
                    alphaColor(themeColor(0xFFF1F1F1, 0xFF242424), contentRenderAlpha));
            ShapeRenderer.roundRectBorder(graphics, coverX, coverY, coverSize, coverSize, 7, 1,
                    alphaColor(BORDER, contentRenderAlpha));
            float iconSize = 30.0f;
            float iconX = coverX + coverSize * 0.5f
                    - MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_MUSIC, iconSize) * 0.5f;
            MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_MUSIC,
                    iconX, coverY + coverSize * 0.5f - 15.0f, iconSize,
                    alphaColor(ACCENT, contentRenderAlpha));
        }

        if (song != null && drawMusicIconButton(graphics, "expand-details",
                x + w - 34, y + 10, 24,
                FontAwesomeIcons.ICON_FA_UP_RIGHT_AND_DOWN_LEFT_FROM_CENTER, false, false)) {
            musicExpandedDetails = true;
        }

        int textX = coverX + coverSize + 10;
        int textW = Math.max(40, x + w - textX - 40);
        int titleY = coverY + 8;
        drawMusicClippedText(graphics, song == null ? "Choose a song" : song.name(),
                textX, titleY, textW, 12.0f, TEXT);
        drawMusicClippedText(graphics, song == null ? "NetEase Cloud Music" : song.artist(),
                textX, titleY + 21, textW, 9.0f, MUTED);
        drawMusicClippedText(graphics, song == null ? NeteaseMusicService.status() : song.album(),
                textX, titleY + 38, textW, 9.0f, MUTED);

        int lyricsX = x + 12;
        int lyricsY = y + coverSize + 30;
        int lyricsW = Math.max(1, w - 24);
        int lyricsH = Math.max(1, h - coverSize - 42);
        drawMusicLyrics(graphics, song, lyricsX, lyricsY, lyricsW, lyricsH);
    }

    private void drawMusicExpandedDetails(GuiGraphics graphics, NeteaseMusicService.Song song,
                                          int x, int y, int w, int h) {
        if (drawMusicIconButton(graphics, "collapse-details",
                x + w - 36, y + 10, 24,
                FontAwesomeIcons.ICON_FA_DOWN_LEFT_AND_UP_RIGHT_TO_CENTER, false, false)) {
            musicExpandedDetails = false;
        }

        int leftW = Math.max(150, Math.round(w * 0.42f));
        int dividerX = x + leftW;
        graphics.fill(dividerX, y + 12, dividerX + 1, y + h - 12,
                alphaColor(BORDER, contentRenderAlpha));

        int coverSize = Math.max(104, Math.min(leftW - 48, h - 72));
        int coverX = x + (leftW - coverSize) / 2;
        int coverY = y + Math.max(20, (h - coverSize) / 2);
        drawMusicRecord(graphics, song, coverX, coverY, coverSize);

        int rightX = dividerX + 20;
        int rightW = Math.max(1, x + w - rightX - 48);
        drawMusicClippedText(graphics, song == null ? "Choose a song" : song.name(),
                rightX, y + 18, rightW, 14.0f, TEXT);
        drawMusicClippedText(graphics, song == null ? "NetEase Cloud Music" : song.artist(),
                rightX, y + 44, rightW, 9.0f, MUTED);
        drawMusicClippedText(graphics, song == null ? NeteaseMusicService.status() : song.album(),
                rightX, y + 61, rightW, 9.0f, MUTED);

        int lyricsY = y + 84;
        int lyricsH = Math.max(1, h - 94);
        drawMusicLyrics(graphics, song, rightX, lyricsY, rightW, lyricsH,
                14.0f, 11.0f, 22.0f);
    }

    private void drawMusicRecord(GuiGraphics graphics, NeteaseMusicService.Song song,
                                 int x, int y, int size) {
        int radius = size / 2;
        ShadowRenderer.shadow(graphics, x, y, size, size, radius,
                ShadowStyle.card(), contentRenderAlpha * 0.72f);
        float rotationDegrees = song == null ? 0.0f
                : (NeteaseMusicService.elapsedMilliseconds() % 30_000L) * (360.0f / 30_000.0f);
        float centerX = x + size * 0.5f;
        float centerY = y + size * 0.5f;
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 0.0f);
        graphics.pose().mulPose(new Quaternionf().rotationZ((float) Math.toRadians(rotationDegrees)));
        graphics.pose().translate(-centerX, -centerY, 0.0f);
        try {
            ShapeRenderer.roundRect(graphics, x, y, size, size, radius,
                    alphaColor(0xFF111318, contentRenderAlpha));
            ShapeRenderer.roundRectBorder(graphics, x, y, size, size, radius, 1,
                    alphaColor(themeColor(0xFF282B31, 0xFF343840), contentRenderAlpha));

            int[] ringInsets = {8, 15, 23, 31};
            for (int inset : ringInsets) {
                int ringSize = size - inset * 2;
                if (ringSize <= 0) {
                    continue;
                }
                ShapeRenderer.roundRectBorder(graphics, x + inset, y + inset,
                        ringSize, ringSize, ringSize / 2, 1,
                        alphaColor(0xFF3A3D45, contentRenderAlpha * 0.34f));
            }

            int labelSize = Math.max(52, Math.round(size * 0.58f));
            int labelX = x + (size - labelSize) / 2;
            int labelY = y + (size - labelSize) / 2;
            int textureId = NeteaseMusicService.coverTextureId(song);
            if (textureId > 0) {
                TextureRenderer.roundedTexture(graphics, textureId,
                        labelX, labelY, labelSize, labelSize, labelSize / 2, contentRenderAlpha);
            } else {
                ShapeRenderer.roundRect(graphics, labelX, labelY, labelSize, labelSize,
                        labelSize / 2, alphaColor(ACCENT_SOFT, contentRenderAlpha));
                float iconSize = Math.max(20.0f, labelSize * 0.24f);
                float iconX = labelX + labelSize * 0.5f
                        - MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_MUSIC, iconSize) * 0.5f;
                MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_MUSIC,
                        iconX, labelY + labelSize * 0.5f - iconSize * 0.5f - 1.0f,
                        iconSize, alphaColor(ACCENT_DARK, contentRenderAlpha));
            }
            ShapeRenderer.roundRectBorder(graphics, labelX, labelY, labelSize, labelSize,
                    labelSize / 2, 1, alphaColor(BORDER, contentRenderAlpha * 0.72f));

            int holeSize = Math.max(5, Math.round(size * 0.035f));
            int holeX = x + (size - holeSize) / 2;
            int holeY = y + (size - holeSize) / 2;
            ShapeRenderer.roundRect(graphics, holeX, holeY, holeSize, holeSize,
                    holeSize / 2, alphaColor(themeColor(0xFFF4F5F8, 0xFF111318), contentRenderAlpha));
            ShapeRenderer.roundRectBorder(graphics, holeX, holeY, holeSize, holeSize,
                    holeSize / 2, 1, alphaColor(BORDER, contentRenderAlpha));
        } finally {
            graphics.pose().popPose();
        }
    }

    private void drawMusicLyrics(GuiGraphics graphics, NeteaseMusicService.Song song,
                                 int x, int y, int w, int h) {
        drawMusicLyrics(graphics, song, x, y, w, h, 10.0f, 9.0f, 17.0f);
    }

    private void drawMusicLyrics(GuiGraphics graphics, NeteaseMusicService.Song song,
                                 int x, int y, int w, int h,
                                 float activeSize, float inactiveSize, float lineHeight) {
        List<NeteaseMusicService.LyricLine> lyrics = NeteaseMusicService.lyrics(song);
        if (song == null || lyrics.isEmpty()) {
            String emptyText = song == null ? "选择歌曲" : NeteaseMusicService.lyricMessage(song);
            int emptyW = TextPainter.msdfWidth(emptyText, inactiveSize);
            drawMusicClippedText(graphics, emptyText,
                    x + Math.max(0, (w - emptyW) / 2),
                    y + Math.max(0, (h - Math.round(inactiveSize)) / 2),
                    w, inactiveSize, MUTED);
            return;
        }

        long songId = song.id();
        if (musicLyricsSongId != songId) {
            musicLyricsSongId = songId;
            musicLyricsAnimatedLine = 0.0f;
            musicLyricsTimelineRevision = -1L;
        }
        int activeLine = NeteaseMusicService.currentLyricIndex(song);
        float targetLine = activeLine < 0 ? 0.0f : activeLine;
        long timelineRevision = NeteaseMusicService.lyricTimelineRevision();
        if (timelineRevision != musicLyricsTimelineRevision
                || Math.abs(targetLine - musicLyricsAnimatedLine) > 3.0f) {
            musicLyricsTimelineRevision = timelineRevision;
            musicLyricsAnimatedLine = targetLine;
        } else {
            musicLyricsAnimatedLine = lerp(musicLyricsAnimatedLine, targetLine, UiAnimationTimings.CONTROL);
        }

        enableContentScissor(x, y, w, h);
        try {
            float centerY = y + h * 0.5f;
            for (int i = 0; i < lyrics.size(); i++) {
                float distance = i - musicLyricsAnimatedLine;
                float lineY = centerY + distance * lineHeight - activeSize * 0.5f;
                if (lineY < y - lineHeight || lineY > y + h) {
                    continue;
                }
                float emphasis = Math.max(0.0f, 1.0f - Math.abs(distance) * 0.24f);
                int color = i == activeLine ? ACCENT_DARK : lerpColor(MUTED, TEXT, emphasis * 0.72f);
                float size = i == activeLine ? activeSize : inactiveSize;
                String line = lyrics.get(i).text();
                int lineW = Math.min(w, TextPainter.msdfWidth(line, size));
                int lineX = x + Math.max(0, (w - lineW) / 2);
                TextPainter.drawMsdfClipped(graphics, line, lineX, Math.round(lineY),
                        w, size, color, contentRenderAlpha * Math.max(0.28f, emphasis));
            }
        } finally {
            restoreContentScissor();
        }
    }

    private void drawMusicPlayerBar(GuiGraphics graphics, NeteaseMusicService.Song song,
                                    int x, int y, int w, int h) {
        musicProgressTrackX = x;
        musicProgressTrackY = y;
        musicProgressTrackW = Math.max(1, w);
        long durationMs = song == null ? 0L : Math.max(0L, song.durationMs());
        long positionMs = musicProgressDragging
                ? musicPreviewPositionMs : NeteaseMusicService.elapsedMilliseconds();
        positionMs = Math.max(0L, Math.min(positionMs, durationMs > 0L ? durationMs : positionMs));
        float targetProgress = durationMs <= 0L ? 0.0f : positionMs / (float) durationMs;
        long now = System.nanoTime();
        long songId = song == null ? -1L : song.id();
        boolean resetProgressAnimation = musicAnimatedProgress < 0.0f
                || songId != musicProgressSongId
                || musicProgressLastRenderNanos == 0L
                || now - musicProgressLastRenderNanos > 250_000_000L;
        if (resetProgressAnimation) {
            musicAnimatedProgress = targetProgress;
        } else {
            musicAnimatedProgress = lerp(
                    musicAnimatedProgress, targetProgress, UiAnimationTimings.CONTROL);
            if (Math.abs(musicAnimatedProgress - targetProgress) < 0.00005f) {
                musicAnimatedProgress = targetProgress;
            }
        }
        musicProgressSongId = songId;
        musicProgressLastRenderNanos = now;
        float progress = clampFloat(musicAnimatedProgress, 0.0f, 1.0f);
        boolean progressHovered = contains(renderMouseX, renderMouseY,
                musicProgressTrackX, musicProgressTrackY - 5,
                musicProgressTrackW, 12);
        graphics.fill(musicProgressTrackX, musicProgressTrackY,
                musicProgressTrackX + musicProgressTrackW, musicProgressTrackY + 2,
                alphaColor(themeColor(0xFFD9DDE5, 0xFF454952), contentRenderAlpha));
        if (progress > 0.0f) {
            graphics.fill(musicProgressTrackX, musicProgressTrackY,
                    musicProgressTrackX + Math.max(2, Math.round(musicProgressTrackW * progress)),
                    musicProgressTrackY + 2, alphaColor(ACCENT, contentRenderAlpha));
        }
        int knobX = clamp(musicProgressTrackX + Math.round(musicProgressTrackW * progress),
                musicProgressTrackX + 3, musicProgressTrackX + musicProgressTrackW - 3);
        if (progressHovered || musicProgressDragging) {
            ShapeRenderer.roundRect(graphics, knobX - 3, musicProgressTrackY - 3, 7, 7, 4,
                    alphaColor(ACCENT, contentRenderAlpha));
        }
        String currentTime = formatMusicClock(positionMs);
        String totalTime = formatMusicDuration(durationMs);
        String timeText = currentTime + " / " + totalTime;
        int timeW = TextPainter.msdfWidth(timeText, 9.0f);
        unicodeText(graphics, timeText, x + w - timeW - 14, y + 24, 9.0f, MUTED);

        int cover = 38;
        int coverX = x + 12;
        int coverY = y + (h - cover) / 2;
        int textureId = NeteaseMusicService.coverTextureId(song);
        if (textureId > 0) {
            TextureRenderer.roundedTexture(graphics, textureId,
                    coverX, coverY, cover, cover, 5, contentRenderAlpha);
        } else {
            ShapeRenderer.roundRect(graphics, coverX, coverY, cover, cover, 5,
                    alphaColor(themeColor(0xFFF0F0F0, 0xFF282828), contentRenderAlpha));
            MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_MUSIC,
                    coverX + 12, coverY + 10, 14.0f, alphaColor(ACCENT, contentRenderAlpha));
        }
        if (song != null && dashboardSurface.button(
                "music/player/cover", coverX, coverY, cover, cover)) {
            musicHomeVisible = false;
            musicExpandedDetails = true;
        }
        drawMusicClippedText(graphics, song == null ? "No song selected" : song.name(),
                coverX + cover + 10, y + 14, 150, 10.0f, TEXT);
        String playbackStatus = NeteaseMusicService.status();
        String playerSubtitle = song == null ? ""
                : ("VIP required".equals(playbackStatus) || "No playable URL".equals(playbackStatus)
                ? playbackStatus : song.artist());
        drawMusicClippedText(graphics, playerSubtitle,
                coverX + cover + 10, y + 32, 150, 8.0f, MUTED);

        int centerX = x + w / 2;
        int modeX = centerX - 92;
        int modeIcon = switch (NeteaseMusicService.playMode()) {
            case SINGLE -> FontAwesomeIcons.ICON_FA_REPEAT;
            case SHUFFLE -> FontAwesomeIcons.ICON_FA_SHUFFLE;
            case ORDER -> FontAwesomeIcons.ICON_FA_ARROW_RIGHT;
        };
        if (drawMusicIconButton(graphics, "mode", modeX, y + 17, 24, modeIcon, false, false)) {
            NeteaseMusicService.cyclePlayMode();
        }
        if (drawMusicIconButton(graphics, "previous", centerX - 58, y + 17, 24,
                FontAwesomeIcons.ICON_FA_BACKWARD_STEP, false, false)) {
            NeteaseMusicService.previous();
        }
        if (drawMusicIconButton(graphics, "toggle", centerX - 17, y + 12, 34,
                NeteaseMusicService.playing() ? FontAwesomeIcons.ICON_FA_PAUSE : FontAwesomeIcons.ICON_FA_PLAY,
                true, false)) {
            NeteaseMusicService.togglePlay();
        }
        if (drawMusicIconButton(graphics, "next", centerX + 34, y + 17, 24,
                FontAwesomeIcons.ICON_FA_FORWARD_STEP, false, false)) {
            NeteaseMusicService.next();
        }
        musicVolumeButtonX = centerX + 68;
        musicVolumeButtonY = y + 17;
        musicVolumeButtonSize = 24;
        musicVolumePopupW = 40;
        musicVolumePopupH = 118;
        musicVolumePopupX = musicVolumeButtonX + (musicVolumeButtonSize - musicVolumePopupW) / 2;
        musicVolumePopupY = musicVolumeButtonY - musicVolumePopupH + 3;
        boolean volumeButtonHovered = contains(renderMouseX, renderMouseY,
                musicVolumeButtonX, musicVolumeButtonY, musicVolumeButtonSize, musicVolumeButtonSize);
        boolean volumePopupHovered = musicVolumePopupOpen && contains(renderMouseX, renderMouseY,
                musicVolumePopupX, musicVolumePopupY, musicVolumePopupW,
                musicVolumeButtonY + musicVolumeButtonSize - musicVolumePopupY);
        musicVolumePopupOpen = musicVolumeDragging || volumeButtonHovered || volumePopupHovered;
        drawMusicIconButton(graphics, "volume", musicVolumeButtonX, musicVolumeButtonY,
                musicVolumeButtonSize,
                NeteaseMusicService.volume() <= 0.001f
                        ? FontAwesomeIcons.ICON_FA_VOLUME_MUTE
                        : FontAwesomeIcons.ICON_FA_VOLUME_HIGH, false, musicVolumePopupOpen);
        if (drawMusicIconButton(graphics, "offscreen-lyrics", centerX + 96, y + 17, 24,
                FontAwesomeIcons.ICON_FA_CLOSED_CAPTIONING,
                false, MusicLyricsHudRenderer.enabled())) {
            MusicLyricsHudRenderer.setEnabled(!MusicLyricsHudRenderer.enabled());
        }
    }

    private void drawMusicVolumePopup(GuiGraphics graphics) {
        if (selectedTab != SidebarTab.MUSIC) {
            musicVolumePopupOpen = false;
            musicVolumeDragging = false;
        }
        float popupAlpha = previewItemAnimation("music-volume-popup", "visible",
                musicVolumePopupOpen ? 1.0f : 0.0f);
        if (popupAlpha <= 0.01f) {
            return;
        }
        if (musicVolumeDragging) {
            updateMusicVolumeDrag(renderMouseY);
        }
        float alpha = popupAlpha * contentRenderAlpha;
        renderScaledPanelBackdrop(graphics,
                musicVolumePopupX + dashboardRenderOffsetX,
                musicVolumePopupY + dashboardRenderOffsetY,
                musicVolumePopupW, musicVolumePopupH, 7.0f, alpha);
        ShadowRenderer.shadow(graphics, musicVolumePopupX, musicVolumePopupY,
                musicVolumePopupW, musicVolumePopupH, 7,
                ShadowStyle.popup(), alpha);
        ShapeRenderer.roundRect(graphics, musicVolumePopupX, musicVolumePopupY,
                musicVolumePopupW, musicVolumePopupH, 7,
                alphaColor(themeColor(0xB8FFFFFF, SURFACE), alpha));
        ShapeRenderer.roundRectBorder(graphics, musicVolumePopupX, musicVolumePopupY,
                musicVolumePopupW, musicVolumePopupH, 7, 1,
                alphaColor(BORDER, alpha));
        musicVolumeTrackX = musicVolumePopupX + musicVolumePopupW / 2;
        musicVolumeTrackY = musicVolumePopupY + 13;
        musicVolumeTrackH = 76;
        float targetVolume = NeteaseMusicService.volume();
        if (musicAnimatedVolume < 0.0f) {
            musicAnimatedVolume = targetVolume;
        } else {
            musicAnimatedVolume = lerp(musicAnimatedVolume, targetVolume, UiAnimationTimings.CONTROL);
            if (Math.abs(musicAnimatedVolume - targetVolume) < 0.0005f) {
                musicAnimatedVolume = targetVolume;
            }
        }
        float volume = clampFloat(musicAnimatedVolume, 0.0f, 1.0f);
        ShapeRenderer.roundRect(graphics, musicVolumeTrackX - 2, musicVolumeTrackY,
                5, musicVolumeTrackH, 3,
                alphaColor(themeColor(0xFFE1E4EA, 0xFF454952), alpha));
        int knobY = musicVolumeTrackY + musicVolumeTrackH
                - Math.round(musicVolumeTrackH * volume);
        if (volume > 0.0f) {
            ShapeRenderer.roundRect(graphics, musicVolumeTrackX - 2, knobY,
                    5, musicVolumeTrackY + musicVolumeTrackH - knobY, 3,
                    alphaColor(ACCENT, alpha));
        }
        ShapeRenderer.roundRect(graphics, musicVolumeTrackX - 4, knobY - 4,
                9, 9, 5, alphaColor(themeColor(0xFFFFFFFF, 0xFFF4F5F8), alpha));
        ShapeRenderer.roundRectBorder(graphics, musicVolumeTrackX - 4, knobY - 4,
                9, 9, 5, 1, alphaColor(lerpColor(BORDER, ACCENT, 0.55f), alpha));

        String percentLabel = Math.round(volume * 100.0f) + "%";
        int labelW = TextPainter.msdfWidth(percentLabel, 8.0f);
        TextPainter.drawMsdfClipped(graphics, percentLabel,
                musicVolumePopupX + Math.max(2, (musicVolumePopupW - labelW) / 2),
                musicVolumePopupY + musicVolumePopupH - 17,
                Math.max(1, musicVolumePopupW - 4), 8.0f, TEXT, alpha);
    }

    private void drawMusicPlaylistPopup(GuiGraphics graphics, int pageX, int pageY,
                                        int pageW, int playerOffset) {
        if (!musicPlaylistOpen) {
            return;
        }
        int popupW = Math.min(220, pageW - 20);
        int popupH = Math.min(224, Math.max(100, playerOffset - 16));
        int popupX = pageX + pageW - popupW - 10;
        int popupY = pageY + playerOffset - popupH - 8;
        if (dashboardSurface.hasClick() && !contains(dashboardSurface.clickX(), dashboardSurface.clickY(),
                popupX, popupY, popupW, popupH)
                && !contains(dashboardSurface.clickX(), dashboardSurface.clickY(),
                musicProgressTrackX, musicProgressTrackY - 6, musicProgressTrackW, 14)) {
            musicPlaylistOpen = false;
            return;
        }

        ShapeRenderer.roundRect(graphics, popupX, popupY, popupW, popupH, 7,
                alphaColor(themeColor(0xFFFDFDFE, 0xFF202229), contentRenderAlpha));
        ShapeRenderer.roundRectBorder(graphics, popupX, popupY, popupW, popupH, 7, 1,
                alphaColor(BORDER, contentRenderAlpha));
        unicodeText(graphics, "播放列表", popupX + 12, popupY + 10, 10.0f, TEXT);
        String count = NeteaseMusicService.searchResults().size() + " 首歌曲";
        drawMusicClippedText(graphics, count, popupX + 12, popupY + 27,
                popupW - 48, 8.0f, MUTED);
        if (drawMusicIconButton(graphics, "playlist-close", popupX + popupW - 30, popupY + 8,
                20, FontAwesomeIcons.ICON_FA_XMARK, false, false)) {
            musicPlaylistOpen = false;
            return;
        }

        List<NeteaseMusicService.Song> playlist = NeteaseMusicService.searchResults();
        int rowY = popupY + 45;
        int rowH = 28;
        int visibleRows = Math.max(0, (popupH - 51) / rowH);
        for (int i = 0; i < Math.min(visibleRows, playlist.size()); i++) {
            NeteaseMusicService.Song playlistSong = playlist.get(i);
            boolean selected = NeteaseMusicService.currentSong() != null
                    && NeteaseMusicService.currentSong().id() == playlistSong.id();
            int rowColor = selected ? alphaColor(ACCENT_SOFT, contentRenderAlpha * 0.58f)
                    : alphaColor(themeColor(0xFFF4F5F8, 0xFF2A2D35), 0.0f);
            if (selected) {
                ShapeRenderer.roundRect(graphics, popupX + 7, rowY + i * rowH,
                        popupW - 14, rowH - 2, 4, rowColor);
            }
            String prefix = String.format(java.util.Locale.ROOT, "%02d", i + 1);
            unicodeText(graphics, prefix, popupX + 12, rowY + i * rowH + 8, 8.0f,
                    selected ? ACCENT_DARK : MUTED);
            drawMusicClippedText(graphics, playlistSong.name(), popupX + 38,
                    rowY + i * rowH + 8, popupW - 50, 9.0f,
                    selected ? ACCENT_DARK : TEXT);
            if (dashboardSurface.button("music/playlist/song/" + playlistSong.id(),
                    popupX + 7, rowY + i * rowH, popupW - 14, rowH - 2)) {
                NeteaseMusicService.play(playlistSong);
                musicPlaylistOpen = false;
            }
        }
        if (playlist.isEmpty()) {
            drawMusicClippedText(graphics, "暂无歌曲", popupX + 12, rowY + 12,
                    popupW - 24, 9.0f, MUTED);
        }
    }

    private boolean drawMusicIconButton(GuiGraphics graphics, String id, int x, int y, int size,
                                        int icon, boolean primary, boolean activeState) {
        boolean hovered = contains(renderMouseX, renderMouseY, x, y, size, size);
        float hover = previewItemAnimation("music-control-" + id, "hover", hovered ? 1.0f : 0.0f);
        boolean pressed = hovered && mouseDown;
        float press = previewItemAnimation("music-control-" + id, "active", pressed ? 1.0f : 0.0f);
        float emphasis = Math.max(activeState ? 1.0f : 0.0f,
                lerp(hover * 0.5f, 1.0f, press));
        if (primary) {
            int fill = lerpColor(ACCENT, ACCENT_DARK, hover * 0.45f);
            ShapeRenderer.roundRect(graphics, x, y, size, size, size / 2, fill);
        }
        float iconSize = primary ? 12.0f : 11.0f;
        float iconX = x + size * 0.5f - MSDFRenderer.iconWidth(icon, iconSize) * 0.5f;
        float iconY = y + size * 0.5f - iconSize * 0.5f;
        int emphasisColor = ACCENT;
        int iconColor = primary ? 0xFFFFFFFF : lerpColor(TEXT, emphasisColor, emphasis);
        MSDFRenderer.drawIconCodepoint(graphics, icon, iconX, iconY, iconSize,
                alphaColor(iconColor, contentRenderAlpha));
        return dashboardSurface.button("music/control/" + id, x, y, size, size);
    }

    private void drawMusicClippedText(GuiGraphics graphics, String value, int x, int y,
                                      int maxW, float size, int color) {
        if (value == null || value.isEmpty() || maxW <= 0) {
            return;
        }
        TextPainter.drawMsdfClipped(graphics, value, x, y, maxW,
                size, color, contentRenderAlpha);
    }

    private static String formatMusicDuration(long durationMs) {
        if (durationMs <= 0L) {
            return "--:--";
        }
        long totalSeconds = durationMs / 1000L;
        return String.format(java.util.Locale.ROOT, "%d:%02d", totalSeconds / 60L, totalSeconds % 60L);
    }

    private static String formatMusicClock(long positionMs) {
        long totalSeconds = Math.max(0L, positionMs) / 1000L;
        return String.format(java.util.Locale.ROOT, "%d:%02d", totalSeconds / 60L, totalSeconds % 60L);
    }

    private boolean startMusicProgressDrag(double scaledX, double scaledY) {
        if (selectedTab != SidebarTab.MUSIC || NeteaseMusicService.currentSong() == null
                || musicProgressTrackW <= 0 || !contains(scaledX, scaledY,
                musicProgressTrackX, musicProgressTrackY - 6,
                musicProgressTrackW, 14)) {
            return false;
        }
        musicProgressDragging = true;
        updateMusicProgressDrag(scaledX);
        return true;
    }

    private boolean startMusicVolumeDrag(double scaledX, double scaledY) {
        if (!musicVolumePopupOpen || musicVolumeTrackH <= 0
                || !contains(scaledX, scaledY, musicVolumeTrackX - 9, musicVolumeTrackY - 6,
                18, musicVolumeTrackH + 12)) {
            return false;
        }
        musicVolumeDragging = true;
        updateMusicVolumeDrag(scaledY);
        return true;
    }

    private boolean updateMusicVolumeDrag(double scaledY) {
        if (!musicVolumeDragging) {
            return false;
        }
        double ratio = 1.0 - (scaledY - musicVolumeTrackY) / Math.max(1.0, musicVolumeTrackH);
        NeteaseMusicService.setVolume((float) Math.max(0.0, Math.min(1.0, ratio)));
        return true;
    }

    private boolean updateMusicProgressDrag(double scaledX) {
        if (!musicProgressDragging) {
            return false;
        }
        NeteaseMusicService.Song song = NeteaseMusicService.currentSong();
        if (song == null || song.durationMs() <= 0L) {
            return true;
        }
        double ratio = (scaledX - musicProgressTrackX) / Math.max(1.0, musicProgressTrackW);
        ratio = Math.max(0.0, Math.min(1.0, ratio));
        musicPreviewPositionMs = Math.round(song.durationMs() * ratio);
        return true;
    }

    private void commitMusicProgressDrag() {
        if (!musicProgressDragging) {
            return;
        }
        musicProgressDragging = false;
        NeteaseMusicService.seekToMillis(musicPreviewPositionMs);
    }

    private int drawPlayerPage(GuiGraphics graphics, int leftX, int y, int stableY, int rightX, int colW) {
        int leftH = drawPlayerCard(graphics, leftX, y, stableY, colW);
        int rightH = drawInventoryCard(graphics, rightX, y, stableY, colW);
        return Math.max(leftH, rightH);
    }

    private int drawUserPage(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int gap = CARD_GAP;
        int colW = (w - gap) / 2;
        int rightX = x + colW + gap;
        int profileH = drawUserProfileCard(graphics, x, y, stableY, colW);
        int runtimeH = drawUserRuntimeCard(graphics, rightX, y, stableY, colW);
        return Math.max(profileH, runtimeH);
    }

    private int drawUserProfileCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureUserProfileCard(x, stableY, w);
        h = drawCard(graphics, "PROFILE", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateUserProfile(gui);
        return h;
    }

    private int drawUserRuntimeCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureUserRuntimeCard(x, stableY, w);
        h = drawCard(graphics, "RUNTIME", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateUserRuntime(gui);
        return h;
    }

    private int drawConfigPage(GuiGraphics graphics, int leftX, int y, int stableY, int rightX, int colW) {
        int fullW = rightX + colW - leftX;
        int toolbarH = 38;
        int listGap = 12;
        int listStartY = stableY + toolbarH + listGap;
        int rowScrollY = y - stableY;
        int rowH = 58;
        int rowGap = 8;
        List<ProfileConfigStore.ProfileInfo> profiles = filteredConfigProfiles();
        WidgetSurface.Frame gui = dashboardSurface.overlay(graphics, contentScope(), dashboardX, dashboardY);
        configMenuOpenedThisFrame = false;

        drawConfigToolbar(graphics, gui, leftX, stableY, fullW, toolbarH, profiles.size());

        int rowClipH = Math.max(0, contentBoundsY + contentBoundsH - listStartY);
        boolean snapConfigRows = snapConfigRowsOnNextDraw;
        List<ConfigRowEntry> animatedRows = configRowAnimator.updateRows(profiles,
                listStartY + rowScrollY, rowH, rowGap, snapConfigRows, dashboardY, dragging,
                configIconAnimations, configNameEditAnimations);
        snapConfigRowsOnNextDraw = false;
        RenderSystem.disableScissor();
        dashboardSurface.setInteractionClip(leftX, listStartY, fullW, rowClipH);
        dashboardSurface.setRenderClip(leftX, listStartY, fullW, rowClipH);
        if (rowClipH > 0) {
            enableContentScissor(leftX, listStartY, fullW, rowClipH);
        }
        if (profiles.isEmpty() && animatedRows.isEmpty()) {
            drawEmptyConfigRow(graphics, leftX, listStartY + rowScrollY, fullW, rowH);
        } else {
            for (ConfigRowEntry row : animatedRows) {
                int drawY = Math.round(row.animation().y);
                if (drawY + rowH > listStartY && drawY < listStartY + rowClipH) {
                    drawConfigProfileRow(graphics, gui, row.profile(), leftX, drawY, fullW, rowH,
                            row.animation().alpha, !row.animation().deleting);
                }
            }
        }
        RenderSystem.disableScissor();
        dashboardSurface.setInteractionClip(contentBoundsX, contentBoundsY, contentBoundsW, contentBoundsH);
        dashboardSurface.setRenderClip(contentBoundsX, contentBoundsY, contentBoundsW, contentBoundsH);
        enableContentScissor(contentBoundsX, contentBoundsY, contentBoundsW, contentBoundsH);

        handleConfigMenuOutsideClick();
        submitConfigMenu(gui);
        int visualRowCount = Math.max(profiles.isEmpty() ? 1 : profiles.size(), animatedRows.size());
        return toolbarH + listGap + visualRowCount * rowH
                + Math.max(0, visualRowCount - 1) * rowGap;
    }

    private void drawConfigToolbar(GuiGraphics graphics, WidgetSurface.Frame gui, int x, int y, int w, int h, int profileCount) {
        int toolbar = themeColor(0xEAF8F9FD, 0xC5161822);
        ShapeRenderer.roundRect(graphics, x, y, w, h, 13, toolbar);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 13, 1, themeColor(0xFFDDE2EE, 0x553E4656));

        int createW = 94;
        gui.primeButtonState("button", "Create", "Create", x + 8, y + 7, true);
        if (gui.button("Create", x + 8, y + 7, createW, 24, FontAwesomeIcons.ICON_FA_LAYER_GROUP, true)) {
            configName = ProfileConfigStore.nextAvailableName("Config");
            saveConfig(configName);
        }
        gui.primeButtonState("iconButton", "Refresh", "Refresh", x + createW + 18, y + 7, false);
        if (gui.iconButton("Refresh", x + createW + 18, y + 7, 24, FontAwesomeIcons.ICON_FA_REFRESH)) {
            configStatus = "Refreshed (" + profileCount + ")";
        }

        int searchButtonSize = 24;
        int searchButtonX = x + w - searchButtonSize - 8;
        int searchY = y + 7;
        gui.primeButtonState("iconButton", "Search", "Search", searchButtonX, searchY, configSearchOpen);
        configSearchWidth = lerp(configSearchWidth, configSearchOpen ? 176.0f : 0.0f, UiAnimationTimings.CONTROL);
        if (configSearchWidth > 1.0f) {
            int inputW = Math.round(configSearchWidth);
            int inputX = searchButtonX - 8 - inputW;
            String nextSearch = gui.inputText("Config Search", configSearchText, inputX, searchY, inputW, searchButtonSize,
                    value -> configSearchText = value);
            configSearchText = nextSearch;
        }
        if (gui.iconButton("Search", searchButtonX, searchY, searchButtonSize,
                FontAwesomeIcons.ICON_FA_MAGNIFYING_GLASS, configSearchOpen)) {
            configSearchOpen = !configSearchOpen;
            if (!configSearchOpen) {
                configSearchText = "";
            }
            closeConfigMenu();
        }
    }

    private List<ProfileConfigStore.ProfileInfo> filteredConfigProfiles() {
        List<ProfileConfigStore.ProfileInfo> profiles = ProfileConfigStore.listProfiles();
        String query = configSearchOpen ? configSearchText.trim().toLowerCase() : "";
        if (query.isEmpty()) {
            return profiles;
        }
        return profiles.stream()
                .filter(profile -> profile.name().toLowerCase().contains(query))
                .toList();
    }

    private void drawConfigProfileRow(GuiGraphics graphics, WidgetSurface.Frame gui, ProfileConfigStore.ProfileInfo profile,
                                      int x, int y, int w, int h, float rowAlpha, boolean interactive) {
        float previousContentAlpha = contentRenderAlpha;
        contentRenderAlpha = clampFloat(previousContentAlpha * rowAlpha, 0.0f, 1.0f);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, contentRenderAlpha);
        ShapeRenderer.setGlobalAlpha(contentRenderAlpha);
        WidgetSurface.setRenderAlpha(contentRenderAlpha);
        try {
            drawConfigProfileRowContent(graphics, gui, profile, x, y, w, h, interactive);
        } finally {
            contentRenderAlpha = previousContentAlpha;
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, previousContentAlpha);
            ShapeRenderer.setGlobalAlpha(previousContentAlpha);
            WidgetSurface.setRenderAlpha(previousContentAlpha);
        }
    }

    private void drawConfigProfileRowContent(GuiGraphics graphics, WidgetSurface.Frame gui,
                                             ProfileConfigStore.ProfileInfo profile,
                                             int x, int y, int w, int h, boolean interactive) {
        String name = profile.name();
        boolean selected = name.equals(configName);
        drawConfigProfileRowBackground(graphics, profile, x, y, w, h);

        int loadW = 85;
        int loadH = 26;
        int loadX = x + w - loadW - 10;
        int loadY = y + 16;
        int dotsX = loadX - 30;
        int actionX = dotsX;
        int contentX = x + 8;
        int contentW = Math.max(80, actionX - contentX - 10);

        int iconBox = 30;
        boolean iconHovered = isHovering(x + 12, y + 14, iconBox, iconBox);
        float iconFocus = updateConfigIconAnimation(name, selected ? 1.0f : iconHovered ? 0.36f : 0.0f);
        int iconBoxFill = lerpColor(themeColor(0xFFF0F3F9, 0x80303A4B), ACCENT_SOFT, iconFocus);
        int iconColor = lerpColor(MUTED, ACCENT_DARK, iconFocus);
        ShapeRenderer.roundRect(graphics, x + 12, y + 14, iconBox, iconBox, 9,
                iconBoxFill);
        MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_FOLDER_OPEN, x + 20, y + 22,
                12.0f, alphaColor(iconColor, contentRenderAlpha));

        int textX = x + 52;
        int textMaxW = Math.max(80, actionX - textX - 10);
        int nameFieldW = Math.min(textMaxW, Math.max(52, textWidth(name) + 50));
        int nameFieldY = y + 8;
        int nameFieldH = 22;
        boolean editingThisName = name.equals(editingConfigName);
        float editProgress = updateConfigNameEditAnimation(name, editingThisName ? 1.0f : 0.0f);
        float textAlpha = 1.0f - editProgress;
        if (textAlpha > 0.02f) {
            float previousContentAlpha = contentRenderAlpha;
            contentRenderAlpha = clampFloat(previousContentAlpha * textAlpha, 0.0f, 1.0f);
            text(graphics, ConfigTextFormatter.ellipsize(name, 12.0f, textMaxW), textX, y + 12, 12.0f, TEXT);
            contentRenderAlpha = previousContentAlpha;
        }
        if (editProgress > 0.02f) {
            editingConfigNameX = textX;
            editingConfigNameY = nameFieldY;
            editingConfigNameW = nameFieldW;
            editingConfigNameH = nameFieldH;
            float previousContentAlpha = contentRenderAlpha;
            contentRenderAlpha = clampFloat(previousContentAlpha * editProgress, 0.0f, 1.0f);
            ShapeRenderer.setGlobalAlpha(contentRenderAlpha);
            WidgetSurface.setRenderAlpha(contentRenderAlpha);
            try {
                editingConfigText = gui.inputText("Config Rename " + name, editingConfigText,
                        textX, nameFieldY, nameFieldW, nameFieldH,
                        value -> editingConfigText = value, configRenameFocusPending);
                configRenameFocusPending = false;
            } finally {
                contentRenderAlpha = previousContentAlpha;
                ShapeRenderer.setGlobalAlpha(previousContentAlpha);
                WidgetSurface.setRenderAlpha(previousContentAlpha);
            }
        }
        if (!editingThisName && interactive
                && dashboardSurface.button("config/name/" + name, textX, nameFieldY, nameFieldW, nameFieldH)
                && handleConfigNameDoubleClick(name)) {
            startConfigRename(name, textX, nameFieldY, nameFieldW, nameFieldH);
        }
        String meta = "Modified: " + ConfigTextFormatter.modified(profile.modifiedMillis())
                + "   Author: " + ConfigTextFormatter.author();
        text(graphics, ConfigTextFormatter.ellipsize(meta, 10.0f, textMaxW), textX, y + 32, 9.0f, MUTED);

        boolean protectedDefault = ProfileConfigStore.isProtectedDefault(name);
        boolean saveAction = selected && !protectedDefault;
        String actionLabel = saveAction ? "Save" : "Load";
        int actionIcon = saveAction ? FontAwesomeIcons.ICON_FA_FLOPPY_DISK : FontAwesomeIcons.ICON_FA_PLAY;
        gui.primeFloatingButtonState("button", "config-action/" + name, selected);
        boolean actionClicked = gui.floatingButton("config-action/" + name, actionLabel, loadX, loadY, loadW, loadH,
                actionIcon, selected);
        if (interactive && actionClicked) {
            if (saveAction) {
                saveConfig(name);
            } else {
                configName = name;
                loadConfig(name);
            }
            closeConfigMenu();
        }

        boolean menuOpen = name.equals(configMenuProfile);
        gui.primeFloatingButtonState("iconButton", "config-menu/" + name, menuOpen);
        boolean menuClicked = gui.floatingIconButton("config-menu/" + name, dotsX, y + 17, 24,
                FontAwesomeIcons.ICON_FA_ELLIPSIS, menuOpen);
        if (interactive && menuClicked) {
            if (name.equals(configMenuProfile)) {
                closeConfigMenu();
            } else {
                configMenuProfile = name;
                configMenuX = dotsX + 28;
                configMenuY = y + 17;
                configMenuButtonX = dotsX;
                configMenuButtonY = y + 17;
                configMenuButtonSize = 24;
                configMenuOpenedThisFrame = true;
            }
        }
    }

    private void drawConfigProfileRowBackground(GuiGraphics graphics, ProfileConfigStore.ProfileInfo profile,
                                                int x, int y, int w, int h) {
        drawConfigProfileRowBackground(graphics, profile, x, y, w, h, 1.0f);
    }

    private void drawConfigProfileRowBackground(GuiGraphics graphics, ProfileConfigStore.ProfileInfo profile,
                                                int x, int y, int w, int h, float rowAlpha) {
        float previousContentAlpha = contentRenderAlpha;
        contentRenderAlpha = clampFloat(previousContentAlpha * rowAlpha, 0.0f, 1.0f);
        ShapeRenderer.setGlobalAlpha(contentRenderAlpha);
        int fill = themeColor(0xCCFFFFFF, 0x8A1A1E29);
        int border = themeColor(0xFFE4E8F1, 0x443E4656);
        ShapeRenderer.roundRect(graphics, x, y, w, h, 11, fill);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 11, 1, border);
        contentRenderAlpha = previousContentAlpha;
        ShapeRenderer.setGlobalAlpha(previousContentAlpha);
    }

    private void drawEmptyConfigRow(GuiGraphics graphics, int x, int y, int w, int h) {
        ShapeRenderer.roundRect(graphics, x, y, w, h, 11, themeColor(0x99FFFFFF, 0x66212634));
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 11, 1, themeColor(0xFFE4E8F1, 0x443E4656));
        text(graphics, "No Configs Yet", x + 14, y + 16, 12.0f, TEXT);
        text(graphics, "Click Create to save the current runtime state.", x + 14, y + 34, 9.0f, MUTED);
    }

    private void submitConfigMenu(WidgetSurface.Frame gui) {
        if (configMenuProfile == null) {
            return;
        }
        String profile = configMenuProfile;
        gui.menuPopup("config/" + profile, configMenuX, configMenuY, menu -> {
            menu.item("Share", FontAwesomeIcons.ICON_FA_SHARE, () -> {
                configStatus = "Share is not available";
                closeConfigMenu();
            });
            menu.item("Duplicate", FontAwesomeIcons.ICON_FA_COPY, () -> {
                duplicateConfig(profile);
                closeConfigMenu();
            });
            menu.item("Collections", FontAwesomeIcons.ICON_FA_LIST, () -> {
                configStatus = "Collections is not available";
                closeConfigMenu();
            });
            menu.danger("Delete", FontAwesomeIcons.ICON_FA_TRASH, () -> {
                deleteConfig(profile);
                if (profile.equals(configName)) {
                    configName = "default";
                }
                closeConfigMenu();
            });
        });
    }

    private void handleConfigMenuOutsideClick() {
        if (configMenuProfile == null || !dashboardSurface.hasClick()) {
            return;
        }
        boolean insideMenu = contains(dashboardSurface.clickX(), dashboardSurface.clickY(), configMenuX, configMenuY,
                CONFIG_MENU_W, CONFIG_MENU_H);
        boolean insideMenuButton = configMenuButtonSize > 0
                && contains(dashboardSurface.clickX(), dashboardSurface.clickY(), configMenuButtonX, configMenuButtonY,
                configMenuButtonSize, configMenuButtonSize);
        if (insideMenuButton && !configMenuOpenedThisFrame) {
            closeConfigMenu();
            if (!dashboardSurface.clickConsumed()) {
                dashboardSurface.consumeClick();
            }
            return;
        }
        if (!insideMenu && !insideMenuButton) {
            closeConfigMenu();
            if (!dashboardSurface.clickConsumed()) {
                dashboardSurface.consumeClick();
            }
        }
    }

    private void closeConfigMenu() {
        configMenuProfile = null;
        configMenuButtonSize = 0;
    }

    private boolean handleConfigNameDoubleClick(String name) {
        long now = System.currentTimeMillis();
        boolean doubleClick = name.equals(lastConfigNameClick) && now - lastConfigNameClickMillis <= 350L;
        lastConfigNameClick = name;
        lastConfigNameClickMillis = now;
        return doubleClick;
    }

    private void startConfigRename(String name, int x, int y, int w, int h) {
        if (ProfileConfigStore.isProtectedDefault(name)) {
            configStatus = "Default config is protected";
            NotificationCenter.warning("Config", "Default config is protected");
            return;
        }
        editingConfigName = name;
        editingConfigText = name;
        configRenameFocusPending = true;
        editingConfigNameX = x;
        editingConfigNameY = y;
        editingConfigNameW = w;
        editingConfigNameH = h;
        closeConfigMenu();
    }

    private void commitConfigRename() {
        if (editingConfigName == null) {
            return;
        }
        String source = editingConfigName;
        String target = ProfileConfigStore.sanitizeName(editingConfigText);
        clearConfigRename();
        if (ProfileConfigStore.isProtectedDefault(source) || ProfileConfigStore.isProtectedDefault(target)) {
            configStatus = "Default config is protected";
            NotificationCenter.warning("Config", "Default config is protected");
            return;
        }
        if (source.equals(target)) {
            return;
        }
        try {
            String renamed = ProfileConfigStore.rename(source, target);
            if (source.equals(configName)) {
                configName = renamed;
            }
            configStatus = "Renamed " + source + " to " + renamed;
            NotificationCenter.success("Config Renamed", source + " -> " + renamed);
        } catch (Exception exception) {
            configStatus = "Rename Failed";
            NotificationCenter.error("Config Rename Failed", source);
        }
    }

    private void cancelConfigRename() {
        clearConfigRename();
    }

    private void clearConfigRename() {
        editingConfigName = null;
        editingConfigText = "";
        configRenameFocusPending = false;
        lastConfigNameClick = null;
        lastConfigNameClickMillis = 0L;
        dashboardSurface.clearTextInputFocusNow();
        editingConfigNameX = 0;
        editingConfigNameY = 0;
        editingConfigNameW = 0;
        editingConfigNameH = 0;
    }

    private void duplicateConfig(String name) {
        try {
            String duplicated = ProfileConfigStore.duplicate(name);
            configStatus = "Duplicated " + duplicated;
            NotificationCenter.success("Config Duplicated", duplicated);
        } catch (Exception exception) {
            configStatus = "Duplicate Failed";
            NotificationCenter.error("Config Duplicate Failed", name);
        }
    }

    private int drawConfigActionsCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureConfigActionsCard(x, stableY, w);
        h = drawCard(graphics, "CONFIG", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateConfigActions(gui);
        return h;
    }

    private int drawConfigListCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureConfigListCard(x, stableY, w);
        h = drawCard(graphics, "PROFILES", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateConfigList(gui);
        return h;
    }

    private void populateConfigActions(WidgetSurface.Frame gui) {
        configName = ProfileConfigStore.sanitizeName(gui.inputText("Name", configName));
        gui.infoRow("Current", configName);
        gui.infoRow("Entries", String.valueOf(ProfileConfigStore.entryCount()));
        gui.infoRow("Status", configStatus);
        gui.divider();
        if (gui.button("Save", 108, 22)) {
            saveConfig(configName);
        }
        if (gui.button("Load", 108, 22)) {
            loadConfig(configName);
        }
        if (gui.button("Delete", 108, 22)) {
            deleteConfig(configName);
        }
    }

    private void populateConfigList(WidgetSurface.Frame gui) {
        java.util.List<String> configs = ProfileConfigStore.listConfigs();
        gui.infoRow("Directory", ProfileConfigStore.directory().getFileName().toString());
        gui.infoRow("Profiles", String.valueOf(configs.size()));
        gui.divider();
        if (configs.isEmpty()) {
            gui.infoRow("No Profiles", "Save One");
            return;
        }
        for (String name : configs) {
            if (gui.button(name, 150, 22)) {
                configName = name;
                loadConfig(name);
            }
        }
    }

    private void saveConfig(String name) {
        if (ProfileConfigStore.isProtectedDefault(name)) {
            configStatus = "Default config is protected";
            NotificationCenter.warning("Config", "Default config is protected");
            return;
        }
        try {
            int count = ProfileConfigStore.save(name);
            configStatus = "Saved " + ProfileConfigStore.sanitizeName(name) + " (" + count + ")";
            NotificationCenter.success("Config Saved", ProfileConfigStore.sanitizeName(name));
        } catch (Exception exception) {
            configStatus = "Save Failed";
            NotificationCenter.error("Config Save Failed", ProfileConfigStore.sanitizeName(name));
        }
    }

    private void loadConfig(String name) {
        try {
            int count = ProfileConfigStore.load(name);
            dashboardSurface.resetControlValuesAfterConfigLoad();
            warmupHotkeys(core);
            configName = ProfileConfigStore.sanitizeName(name);
            configStatus = "Loaded " + ProfileConfigStore.sanitizeName(name) + " (" + count + ")";
            NotificationCenter.success("Config Loaded", ProfileConfigStore.sanitizeName(name));
        } catch (Exception exception) {
            configStatus = "Load Failed";
            NotificationCenter.error("Config Load Failed", ProfileConfigStore.sanitizeName(name));
        }
    }

    private void deleteConfig(String name) {
        if (ProfileConfigStore.isProtectedDefault(name)) {
            configStatus = "Default config is protected";
            NotificationCenter.warning("Config", "Default config is protected");
            return;
        }
        try {
            boolean deleted = ProfileConfigStore.delete(name);
            configStatus = deleted ? "Deleted " + ProfileConfigStore.sanitizeName(name) : "Not found";
            if (deleted) {
                NotificationCenter.success("Config Deleted", ProfileConfigStore.sanitizeName(name));
            } else {
                NotificationCenter.warning("Config Not Found", ProfileConfigStore.sanitizeName(name));
            }
        } catch (Exception exception) {
            configStatus = "Delete Failed";
            NotificationCenter.error("Config Delete Failed", ProfileConfigStore.sanitizeName(name));
        }
    }

    private void populateUserProfile(WidgetSurface.Frame gui) {
        gui.infoRow("Local Profile", "Connected");
        gui.infoRow("Session", "Active");
        gui.infoRow("UI Profile", "Default", false);
    }

    private void populateUserRuntime(WidgetSurface.Frame gui) {
        gui.infoRow("Build", CLIENT_VERSION);
        gui.infoRow("Loader", "Forge");
        gui.infoRow("Minecraft", "1.20.1", false);
    }

    private int drawAimbotCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureAimbotCard(x, stableY, w);
        h = drawCard(graphics, "KILL AURA", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        CombatSettingsPage.populateAimbot(gui);
        return h;
    }

    private int drawKillAuraTargetsCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureKillAuraTargetsCard(x, stableY, w);
        h = drawCard(graphics, "TARGETS", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        CombatSettingsPage.populateKillAuraTargets(gui);
        return h;
    }

    private int drawKillAuraRotationCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureKillAuraRotationCard(x, stableY, w);
        h = drawCard(graphics, "ROTATION", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        CombatSettingsPage.populateKillAuraRotation(gui);
        return h;
    }

    private int drawCombatModulesCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureCombatModulesCard(x, stableY, w);
        h = drawCard(graphics, "COMBAT", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateCombatModules(gui);
        return h;
    }

    private int drawMeleeCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureMeleeCard(x, stableY, w);
        h = drawCard(graphics, "MELEE", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateMelee(gui);
        return h;
    }

    private void populateMelee(WidgetSurface.Frame gui) {
        CombatSettings.setCriticalsEnabled(gui.controlledCheckboxWithSettings("Criticals",
                CombatSettings.criticalsEnabled(), CombatSettingsPage::populateCriticalsSettings,
                CombatSettings::setCriticalsEnabled));
        CombatSettings.setAttackDelayEnabled(gui.controlledCheckboxWithSettings("Attack Delay",
                CombatSettings.attackDelayEnabled(), CombatSettingsPage::populateAttackDelaySettings,
                CombatSettings::setAttackDelayEnabled));
        CombatSettings.setAutoHeadEnabled(gui.controlledCheckboxWithSettings("Auto Head",
                CombatSettings.autoHeadEnabled(), CombatSettingsPage::populateAutoHeadSettings,
                CombatSettings::setAutoHeadEnabled));
        CombatSettings.setAutoBlockEnabled(gui.controlledCheckboxWithSettings("Auto Block",
                CombatSettings.autoBlockEnabled(), CombatSettingsPage::populateAutoBlockSettings,
                CombatSettings::setAutoBlockEnabled));
        CombatSettings.setKnockbackEnabled(gui.controlledCheckbox("Knockback", CombatSettings.knockbackEnabled(), CombatSettings::setKnockbackEnabled));
        CombatSettings.setSprintResetEnabled(gui.controlledCheckbox("WTap", CombatSettings.sprintResetEnabled(), CombatSettings::setSprintResetEnabled));
        CombatSettings.setAutoWeaponEnabled(gui.controlledCheckboxWithSettings("Auto Weapon",
                CombatSettings.autoWeaponEnabled(), CombatSettingsPage::populateAutoWeaponSettings,
                CombatSettings::setAutoWeaponEnabled));
        CombatSettings.setShieldBreakerEnabled(gui.controlledCheckboxWithSettings("Shield Breaker",
                CombatSettings.shieldBreakerEnabled(), CombatSettingsPage::populateShieldBreakerSettings,
                CombatSettings::setShieldBreakerEnabled));
        CombatSettings.setTargetStrafeEnabled(gui.controlledCheckboxWithSettings("Target Strafe",
                CombatSettings.targetStrafeEnabled(), CombatSettingsPage::populateTargetStrafeSettings,
                CombatSettings::setTargetStrafeEnabled));
        CombatSettings.setHitboxesEnabled(gui.controlledCheckboxWithSettings("Hitboxes",
                CombatSettings.hitboxesEnabled(), CombatSettingsPage::populateHitboxesSettings,
                CombatSettings::setHitboxesEnabled));
        CombatSettings.setBacktrackEnabled(gui.controlledCheckboxWithSettings("Backtrack",
                CombatSettings.backtrackEnabled(), CombatSettingsPage::populateBacktrackSettings,
                CombatSettings::setBacktrackEnabled));
        CombatSettings.setAntiBotEnabled(gui.controlledCheckboxWithSettings("Anti Bot",
                CombatSettings.antiBotEnabled(), this::populateAntiBotSettings,
                CombatSettings::setAntiBotEnabled));
        CombatSettings.setFakeLagEnabled(gui.controlledCheckboxWithSettings("Fake Lag",
                CombatSettings.fakeLagEnabled(), CombatSettingsPage::populateFakeLagSettings,
                CombatSettings::setFakeLagEnabled));
    }

    private int drawDefenseCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureDefenseCard(x, stableY, w);
        h = drawCard(graphics, "DEFENSE", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        CombatSettingsPage.populateDefense(gui);
        return h;
    }

    private int drawTerrainCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureTerrainCard(x, stableY, w);
        h = drawCard(graphics, "TERRAIN", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        CombatSettingsPage.populateTerrain(gui);
        return h;
    }

    private void populateCombatModules(WidgetSurface.Frame gui) {
        CombatSettings.setCriticalsEnabled(gui.controlledCheckboxWithSettings("Criticals",
                CombatSettings.criticalsEnabled(), CombatSettingsPage::populateCriticalsSettings,
                CombatSettings::setCriticalsEnabled));
        CombatSettings.setKnockbackEnabled(gui.controlledCheckbox("Knockback", CombatSettings.knockbackEnabled(), CombatSettings::setKnockbackEnabled));
        CombatSettings.setSprintResetEnabled(gui.controlledCheckbox("WTap", CombatSettings.sprintResetEnabled(), CombatSettings::setSprintResetEnabled));
        CombatSettings.setAntiBotEnabled(gui.controlledCheckboxWithSettings("Anti Bot",
                CombatSettings.antiBotEnabled(), this::populateAntiBotSettings,
                CombatSettings::setAntiBotEnabled));
        CombatSettings.setAutoWeaponEnabled(gui.controlledCheckboxWithSettings("Auto Weapon",
                CombatSettings.autoWeaponEnabled(), CombatSettingsPage::populateAutoWeaponSettings,
                CombatSettings::setAutoWeaponEnabled));
        CombatSettings.setShieldBreakerEnabled(gui.controlledCheckboxWithSettings("Shield Breaker",
                CombatSettings.shieldBreakerEnabled(), CombatSettingsPage::populateShieldBreakerSettings,
                CombatSettings::setShieldBreakerEnabled));
        CombatSettings.setTargetStrafeEnabled(gui.controlledCheckboxWithSettings("Target Strafe",
                CombatSettings.targetStrafeEnabled(), CombatSettingsPage::populateTargetStrafeSettings,
                CombatSettings::setTargetStrafeEnabled));
        CombatSettings.setHitboxesEnabled(gui.controlledCheckboxWithSettings("Hitboxes",
                CombatSettings.hitboxesEnabled(), CombatSettingsPage::populateHitboxesSettings,
                CombatSettings::setHitboxesEnabled));
        CombatSettings.setBacktrackEnabled(gui.controlledCheckboxWithSettings("Backtrack",
                CombatSettings.backtrackEnabled(), CombatSettingsPage::populateBacktrackSettings,
                CombatSettings::setBacktrackEnabled));
        CombatSettings.setWebAuraEnabled(gui.controlledCheckboxWithSettings("Web Aura",
                CombatSettings.webAuraEnabled(), CombatSettingsPage::populateWebAuraSettings,
                CombatSettings::setWebAuraEnabled));
        CombatSettings.setAutoTrapEnabled(gui.controlledCheckboxWithSettings("Auto Trap",
                CombatSettings.autoTrapEnabled(), CombatSettingsPage::populateAutoTrapSettings,
                CombatSettings::setAutoTrapEnabled));
        CombatSettings.setSurroundEnabled(gui.controlledCheckboxWithSettings("Surround",
                CombatSettings.surroundEnabled(), CombatSettingsPage::populateSurroundSettings,
                CombatSettings::setSurroundEnabled));
        CombatSettings.setHoleFillEnabled(gui.controlledCheckboxWithSettings("Hole Fill",
                CombatSettings.holeFillEnabled(), CombatSettingsPage::populateHoleFillSettings,
                CombatSettings::setHoleFillEnabled));
        CombatSettings.setAutoHealEnabled(gui.controlledCheckboxWithSettings("Auto Gap",
                CombatSettings.autoHealEnabled(), CombatSettingsPage::populateAutoHealSettings,
                CombatSettings::setAutoHealEnabled));
        CombatSettings.setAutoTotemEnabled(gui.controlledCheckboxWithSettings("Auto Totem",
                CombatSettings.autoTotemEnabled(), CombatSettingsPage::populateAutoTotemSettings,
                CombatSettings::setAutoTotemEnabled));
        CombatSettings.setAutoArmorEnabled(gui.controlledCheckboxWithSettings("Auto Armor",
                CombatSettings.autoArmorEnabled(), CombatSettingsPage::populateAutoArmorSettings,
                CombatSettings::setAutoArmorEnabled));
        CombatSettings.setVelocityEnabled(gui.controlledCheckboxWithSettings("Anti Knockback",
                CombatSettings.velocityEnabled(), CombatSettingsPage::populateVelocitySettings,
                CombatSettings::setVelocityEnabled));
        CombatSettings.setFakeLagEnabled(gui.controlledCheckboxWithSettings("Fake Lag",
                CombatSettings.fakeLagEnabled(), CombatSettingsPage::populateFakeLagSettings,
                CombatSettings::setFakeLagEnabled));
    }

    private void populateAntiBotSettings(WidgetSurface.Frame gui) {
        CombatSettings.setAntiBotTabListCheck(gui.controlledCheckbox("Tab List",
                CombatSettings.antiBotTabListCheck(), CombatSettings::setAntiBotTabListCheck));
        CombatSettings.setAntiBotDuplicateCheck(gui.controlledCheckbox("Duplicate Profile",
                CombatSettings.antiBotDuplicateCheck(), CombatSettings::setAntiBotDuplicateCheck));
        CombatSettings.setAntiBotInvalidProfileCheck(gui.controlledCheckbox("Invalid Profile",
                CombatSettings.antiBotInvalidProfileCheck(), CombatSettings::setAntiBotInvalidProfileCheck));
        CombatSettings.setAntiBotZeroPingCheck(gui.controlledCheckbox("Zero Ping",
                CombatSettings.antiBotZeroPingCheck(), CombatSettings::setAntiBotZeroPingCheck));
        gui.divider();
        gui.infoRow("Detected", String.valueOf(AntiBotService.detectedCount()));
        Player target = crosshairPlayer();
        gui.infoRow("Crosshair", target == null ? "None" : AntiBotService.statusFor(target));
    }

    private int drawCombatAssistCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureCombatAssistCard(x, stableY, w);
        h = drawCard(graphics, "ASSIST", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateCombatAssist(gui);
        return h;
    }

    private void populateCombatAssist(WidgetSurface.Frame gui) {
        CombatSettings.setTriggerBotEnabled(gui.controlledCheckboxWithSettings("Trigger Bot",
                CombatSettings.triggerBotEnabled(), CombatSettingsPage::populateTriggerBotSettings,
                CombatSettings::setTriggerBotEnabled));
        CombatSettings.setAimAssistEnabled(gui.controlledCheckboxWithSettings("Aim Assist",
                CombatSettings.aimAssistEnabled(), CombatSettingsPage::populateAimAssistSettings,
                CombatSettings::setAimAssistEnabled));
        CombatSettings.setProjectileAimEnabled(gui.controlledCheckboxWithSettings("Projectile Aim",
                CombatSettings.projectileAimEnabled(), CombatSettingsPage::populateProjectileAimSettings,
                CombatSettings::setProjectileAimEnabled));
        CombatSettings.setAutoPearlEnabled(gui.controlledCheckboxWithSettings("Auto Pearl",
                CombatSettings.autoPearlEnabled(), CombatSettingsPage::populateAutoPearlSettings,
                CombatSettings::setAutoPearlEnabled));
        CombatSettings.setFastUseEnabled(gui.controlledCheckboxWithSettings("Fast Use",
                CombatSettings.fastUseEnabled(), CombatSettingsPage::populateFastUseSettings,
                CombatSettings::setFastUseEnabled));
        CombatSettings.setAutoClickerEnabled(gui.controlledCheckboxWithSettings("Auto Clicker",
                CombatSettings.autoClickerEnabled(), CombatSettingsPage::populateAutoClickerSettings,
                CombatSettings::setAutoClickerEnabled));
    }

    private int drawRagebotMainCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureRagebotMainCard(x, stableY, w);
        h = drawCard(graphics, "MAIN", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        CombatSettingsPage.populateRagebotMain(gui);
        return h;
    }

    private int drawRagebotSelectionCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureRagebotSelectionCard(x, stableY, w);
        h = drawCard(graphics, "SELECTION", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        CombatSettingsPage.populateRagebotSelection(gui);
        return h;
    }

    private int drawRagebotOtherCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureRagebotOtherCard(x, stableY, w);
        h = drawCard(graphics, "OTHER", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        CombatSettingsPage.populateRagebotOther(gui);
        return h;
    }

    private int drawRagebotAntiAimCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureRagebotAntiAimCard(x, stableY, w);
        h = drawCard(graphics, "ANTI AIM", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        CombatSettingsPage.populateRagebotAntiAim(gui);
        return h;
    }

    private int drawSocialCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureSocialCard(x, stableY, w);
        h = drawCard(graphics, "WHITELIST", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateSocial(gui);
        return h;
    }

    private void populateSocial(WidgetSurface.Frame gui) {
        CombatSettings.setTeamsCheck(gui.controlledCheckbox("Teams Check",
                CombatSettings.teamsCheck(), CombatSettings::setTeamsCheck));
        CombatSettings.setTeamMode(gui.combo("Team Mode", CombatSettings.teamMode(),
                "All, Vanilla, Scoreboard, Name Color", CombatSettings::setTeamMode));
        CombatSettings.setFriendsCheck(gui.controlledCheckbox("Friends Check",
                CombatSettings.friendsCheck(), CombatSettings::setFriendsCheck));
        gui.infoRow("Current Team", SocialProfileStore.localTeamLabel(Minecraft.getInstance().player), false);
    }

    private int drawFriendActionsCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureFriendActionsCard(x, stableY, w);
        h = drawCard(graphics, "FRIEND ACTIONS", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateFriendActions(gui);
        return h;
    }

    private int drawFriendsListCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureFriendsListCard(x, stableY, w);
        h = drawCard(graphics, "FRIEND LIST", x, y, w, h);
        drawCardHeaderValue(graphics, String.valueOf(SocialProfileStore.friendCount()), x, y, w);
        enableCardContentScissor(x, y, w, h);
        WidgetSurface.Frame gui = beginClippedCardGui(graphics, x, y, stableY, w, h);
        try {
            populateFriendsList(gui);
        } finally {
            restoreContentScissor();
        }
        return h;
    }

    private void populateFriendActions(WidgetSurface.Frame gui) {
        String name = gui.inputText("Friend Name", friendNameDraft, value -> friendNameDraft = value);
        int editActions = gui.actionButtonPair(
                "add-friend", "Add Friend", FontAwesomeIcons.ICON_FA_USER_PLUS, true,
                "remove-name", "Remove Name", FontAwesomeIcons.ICON_FA_USER_MINUS, false,
                24);
        if ((editActions & 1) != 0) {
            if (SocialProfileStore.addFriend(name)) {
                clearClosingFriendRow(name);
            }
            friendNameDraft = "";
            gui.setLastInputText("");
        }
        if ((editActions & 2) != 0) {
            markFriendRowClosing(name);
            SocialProfileStore.removeFriend(name);
            friendNameDraft = "";
            gui.setLastInputText("");
        }
        Player target = crosshairPlayer();
        String targetLabel = target == null ? "No Target" : "Toggle Target";
        gui.infoRow("Crosshair", target == null ? "None" : friendStatus(target), false);
        int bulkActions = gui.actionButtonPair(
                "toggle-crosshair", targetLabel, FontAwesomeIcons.ICON_FA_USER_CHECK, target != null,
                "clear-friends", "Clear Friends", FontAwesomeIcons.ICON_FA_TRASH, false,
                24);
        if ((bulkActions & 1) != 0 && target != null) {
            if (SocialProfileStore.isFriend(target)) {
                markFriendRowClosing(target.getGameProfile().getName());
            } else {
                clearClosingFriendRow(target.getGameProfile().getName());
            }
            SocialProfileStore.toggleFriend(target);
        }
        if ((bulkActions & 2) != 0) {
            markAllFriendRowsClosing();
            SocialProfileStore.clearFriends();
        }
    }

    private void populateFriendsList(WidgetSurface.Frame gui) {
        pruneClosingFriendRows();
        List<String> friends = new ArrayList<>(SocialProfileStore.friendNames());
        int friendCount = friends.size();
        if (friendCount <= 0 && closingFriendRows.isEmpty()) {
            gui.emptyState("No Friends Saved", "Add a name or toggle crosshair", 58, false);
            return;
        }
        int shown = 0;
        for (String friend : friends) {
            if (shown >= 12) {
                break;
            }
            boolean removeClicked = gui.compactActionRow("remove-friend/" + friend, friend, "Remove",
                    FontAwesomeIcons.ICON_FA_USER_MINUS, 28, true, false);
            if (gui.rendering()) {
                friendRowPositions.put(friend, gui.lastRowY());
                if (removeClicked) {
                    markFriendRowClosing(friend, gui.lastRowY());
                    SocialProfileStore.removeFriend(friend);
                    shown++;
                    break;
                }
            }
            shown++;
        }
        int remaining = friendCount - shown;
        if (remaining > 0) {
            gui.infoRow("More", "+" + remaining, false);
        }
        renderClosingFriendRows(gui);
    }

    private void markFriendRowClosing(String name) {
        String displayName = displayFriendName(name);
        Integer rowY = friendRowPositions.get(displayName);
        markFriendRowClosing(displayName, rowY == null ? Integer.MIN_VALUE : rowY);
    }

    private void markFriendRowClosing(String name, int rowY) {
        String displayName = displayFriendName(name);
        if (displayName == null || displayName.isBlank()) {
            return;
        }
        if (!SocialProfileStore.isFriendName(displayName) && !SocialProfileStore.isFriendName(name)) {
            return;
        }
        if (rowY == Integer.MIN_VALUE) {
            return;
        }
        closingFriendRows.put(displayName, new FriendRowFadeState(rowY, System.currentTimeMillis()));
    }

    private void markAllFriendRowsClosing() {
        for (String friend : SocialProfileStore.friendNames()) {
            Integer rowY = friendRowPositions.get(friend);
            if (rowY != null) {
                markFriendRowClosing(friend, rowY);
            }
        }
    }

    private void clearClosingFriendRow(String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        closingFriendRows.keySet().removeIf(friend -> friend.equalsIgnoreCase(name));
    }

    private void pruneClosingFriendRows() {
        long now = System.currentTimeMillis();
        String scope = cardScope("FRIEND LIST");
        closingFriendRows.entrySet().removeIf(entry -> {
            boolean remove = SocialProfileStore.isFriendName(entry.getKey())
                    || now - entry.getValue().startedAt >= FRIEND_ROW_FADE_OUT_MS;
            if (remove) {
                dashboardSurface.clearCompactActionRowAnimation(scope, "remove-friend/" + entry.getKey());
            }
            return remove;
        });
    }

    private void renderClosingFriendRows(WidgetSurface.Frame gui) {
        if (!gui.rendering()) {
            return;
        }
        for (Map.Entry<String, FriendRowFadeState> entry : new ArrayList<>(closingFriendRows.entrySet())) {
            String friend = entry.getKey();
            if (SocialProfileStore.isFriendName(friend)) {
                continue;
            }
            gui.compactActionRowAt("remove-friend/" + friend, friend, "Remove",
                    FontAwesomeIcons.ICON_FA_USER_MINUS, entry.getValue().rowY, 28, false, false);
        }
    }

    private String displayFriendName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        for (String friend : SocialProfileStore.friendNames()) {
            if (friend.equalsIgnoreCase(name)) {
                return friend;
            }
        }
        return name;
    }

    private static final class FriendRowFadeState {
        final int rowY;
        final long startedAt;

        FriendRowFadeState(int rowY, long startedAt) {
            this.rowY = rowY;
            this.startedAt = startedAt;
        }
    }

    private Player crosshairPlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof Player player
                && !FakePlayerRegistry.isDummy(player)) {
            return player;
        }
        return null;
    }

    private String friendStatus(Player player) {
        if (SocialProfileStore.isFriend(player)) {
            return player.getGameProfile().getName() + " / Friend";
        }
        return player.getGameProfile().getName();
    }

    private int drawAutoCrystalCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureAutoCrystalCard(x, stableY, w);
        h = drawCard(graphics, "AUTO CRYSTAL", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateAutoCrystal(gui);
        return h;
    }

    private int drawBedAuraCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureBedAuraCard(x, stableY, w);
        h = drawCard(graphics, "BED AURA", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateBedAura(gui);
        return h;
    }

    private void populateBedAura(WidgetSurface.Frame gui) {
        CombatSettings.setBedAuraEnabled(gui.controlledCheckboxWithSettings("Enabled",
                CombatSettings.bedAuraEnabled(), this::populateBedAuraGeneralSettings,
                CombatSettings::setBedAuraEnabled));
        CombatSettings.setBedAuraPlace(gui.controlledCheckboxWithSettings("Place",
                CombatSettings.bedAuraPlace(), this::populateBedAuraPlaceSettings,
                CombatSettings::setBedAuraPlace));
        CombatSettings.setBedAuraBreak(gui.controlledCheckboxWithSettings("Break",
                CombatSettings.bedAuraBreak(), this::populateBedAuraBreakSettings,
                CombatSettings::setBedAuraBreak));
        float range = gui.slider("Range", CombatSettings.bedAuraRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", CombatSettings.bedAuraRange()),
                value -> SliderValueFormat.decimalRangeLabel(value, 2.0f, 7.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 2.0f, 7.0f),
                CombatSettings::setBedAuraRangeSlider);
        CombatSettings.setBedAuraRangeSlider(range);
        float minDamage = gui.slider("Min Damage", CombatSettings.bedAuraMinDamageSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", CombatSettings.bedAuraMinDamage()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 20.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setBedAuraMinDamageSlider);
        CombatSettings.setBedAuraMinDamageSlider(minDamage);
        float selfDamage = gui.slider("Max Self", CombatSettings.bedAuraMaxSelfDamageSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", CombatSettings.bedAuraMaxSelfDamage()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 20.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setBedAuraMaxSelfDamageSlider);
        CombatSettings.setBedAuraMaxSelfDamageSlider(selfDamage);
    }

    private void populateBedAuraGeneralSettings(WidgetSurface.Frame gui) {
        CombatSettings.setBedAuraRayTrace(gui.controlledCheckbox("Ray Trace",
                CombatSettings.bedAuraRayTrace(), CombatSettings::setBedAuraRayTrace));
        CombatSettings.setBedAuraRotate(gui.controlledCheckbox("Silent Rotate",
                CombatSettings.bedAuraRotate(), CombatSettings::setBedAuraRotate));
        CombatSettings.setBedAuraAntiSuicide(gui.controlledCheckbox("Anti Suicide",
                CombatSettings.bedAuraAntiSuicide(), CombatSettings::setBedAuraAntiSuicide));
    }

    private void populateBedAuraPlaceSettings(WidgetSurface.Frame gui) {
        float delay = gui.slider("Place Delay", CombatSettings.bedAuraPlaceDelaySlider(),
                CombatSettings.bedAuraPlaceDelayTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 10.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 10.0f),
                CombatSettings::setBedAuraPlaceDelaySlider);
        CombatSettings.setBedAuraPlaceDelaySlider(delay);
    }

    private void populateBedAuraBreakSettings(WidgetSurface.Frame gui) {
        float delay = gui.slider("Break Delay", CombatSettings.bedAuraBreakDelaySlider(),
                CombatSettings.bedAuraBreakDelayTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 10.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 10.0f),
                CombatSettings::setBedAuraBreakDelaySlider);
        CombatSettings.setBedAuraBreakDelaySlider(delay);
    }

    private int drawAnchorAuraCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureAnchorAuraCard(x, stableY, w);
        h = drawCard(graphics, "ANCHOR AURA", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateAnchorAura(gui);
        return h;
    }

    private void populateAnchorAura(WidgetSurface.Frame gui) {
        CombatSettings.setAnchorAuraEnabled(gui.controlledCheckboxWithSettings("Enabled",
                CombatSettings.anchorAuraEnabled(), this::populateAnchorAuraGeneralSettings,
                CombatSettings::setAnchorAuraEnabled));
        CombatSettings.setAnchorAuraPlace(gui.controlledCheckboxWithSettings("Place",
                CombatSettings.anchorAuraPlace(), this::populateAnchorAuraPlaceSettings,
                CombatSettings::setAnchorAuraPlace));
        CombatSettings.setAnchorAuraBreak(gui.controlledCheckboxWithSettings("Break",
                CombatSettings.anchorAuraBreak(), this::populateAnchorAuraBreakSettings,
                CombatSettings::setAnchorAuraBreak));
        float range = gui.slider("Range", CombatSettings.anchorAuraRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", CombatSettings.anchorAuraRange()),
                value -> SliderValueFormat.decimalRangeLabel(value, 2.0f, 7.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 2.0f, 7.0f),
                CombatSettings::setAnchorAuraRangeSlider);
        CombatSettings.setAnchorAuraRangeSlider(range);
        float minDamage = gui.slider("Min Damage", CombatSettings.anchorAuraMinDamageSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", CombatSettings.anchorAuraMinDamage()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 20.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setAnchorAuraMinDamageSlider);
        CombatSettings.setAnchorAuraMinDamageSlider(minDamage);
        float selfDamage = gui.slider("Max Self", CombatSettings.anchorAuraMaxSelfDamageSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", CombatSettings.anchorAuraMaxSelfDamage()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 20.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setAnchorAuraMaxSelfDamageSlider);
        CombatSettings.setAnchorAuraMaxSelfDamageSlider(selfDamage);
    }

    private void populateAnchorAuraGeneralSettings(WidgetSurface.Frame gui) {
        CombatSettings.setAnchorAuraRayTrace(gui.controlledCheckbox("Ray Trace",
                CombatSettings.anchorAuraRayTrace(), CombatSettings::setAnchorAuraRayTrace));
        CombatSettings.setAnchorAuraRotate(gui.controlledCheckbox("Silent Rotate",
                CombatSettings.anchorAuraRotate(), CombatSettings::setAnchorAuraRotate));
        CombatSettings.setAnchorAuraAntiSuicide(gui.controlledCheckbox("Anti Suicide",
                CombatSettings.anchorAuraAntiSuicide(), CombatSettings::setAnchorAuraAntiSuicide));
    }

    private void populateAnchorAuraPlaceSettings(WidgetSurface.Frame gui) {
        float delay = gui.slider("Place Delay", CombatSettings.anchorAuraPlaceDelaySlider(),
                CombatSettings.anchorAuraPlaceDelayTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 10.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 10.0f),
                CombatSettings::setAnchorAuraPlaceDelaySlider);
        CombatSettings.setAnchorAuraPlaceDelaySlider(delay);
    }

    private void populateAnchorAuraBreakSettings(WidgetSurface.Frame gui) {
        float delay = gui.slider("Break Delay", CombatSettings.anchorAuraBreakDelaySlider(),
                CombatSettings.anchorAuraBreakDelayTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 10.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 10.0f),
                CombatSettings::setAnchorAuraBreakDelaySlider);
        CombatSettings.setAnchorAuraBreakDelaySlider(delay);
    }

    private void populateAutoCrystal(WidgetSurface.Frame gui) {
        CombatSettings.setAutoCrystalEnabled(gui.controlledCheckboxWithSettings("Enabled",
                CombatSettings.autoCrystalEnabled(), this::populateAutoCrystalGeneralSettings,
                CombatSettings::setAutoCrystalEnabled));
        CombatSettings.setAutoCrystalPlace(gui.controlledCheckboxWithSettings("Place",
                CombatSettings.autoCrystalPlace(), this::populateAutoCrystalPlaceSettings,
                CombatSettings::setAutoCrystalPlace));
        CombatSettings.setAutoCrystalBreak(gui.controlledCheckboxWithSettings("Break",
                CombatSettings.autoCrystalBreak(), this::populateAutoCrystalBreakSettings,
                CombatSettings::setAutoCrystalBreak));
        CombatSettings.setAutoCrystalPriority(gui.combo("Priority", CombatSettings.autoCrystalPriority(),
                "Damage, Distance, Health", CombatSettings::setAutoCrystalPriority));
        float range = gui.slider("Range", CombatSettings.autoCrystalRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", CombatSettings.autoCrystalRange()),
                value -> SliderValueFormat.decimalRangeLabel(value, 2.0f, 7.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 2.0f, 7.0f),
                CombatSettings::setAutoCrystalRangeSlider);
        CombatSettings.setAutoCrystalRangeSlider(range);
        float minDamage = gui.slider("Min Damage", CombatSettings.autoCrystalMinDamageSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", CombatSettings.autoCrystalMinDamage()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 20.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setAutoCrystalMinDamageSlider);
        CombatSettings.setAutoCrystalMinDamageSlider(minDamage);
        float selfDamage = gui.slider("Max Self", CombatSettings.autoCrystalMaxSelfDamageSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", CombatSettings.autoCrystalMaxSelfDamage()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 20.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setAutoCrystalMaxSelfDamageSlider);
        CombatSettings.setAutoCrystalMaxSelfDamageSlider(selfDamage);
        CombatSettings.setAutoCrystalFacePlace(gui.controlledCheckboxWithSettings("Face Place",
                CombatSettings.autoCrystalFacePlace(), this::populateAutoCrystalFacePlaceSettings,
                CombatSettings::setAutoCrystalFacePlace));
    }

    private void populateAutoCrystalGeneralSettings(WidgetSurface.Frame gui) {
        CombatSettings.setAutoCrystalRayTrace(gui.controlledCheckbox("Ray Trace",
                CombatSettings.autoCrystalRayTrace(), CombatSettings::setAutoCrystalRayTrace));
        CombatSettings.setAutoCrystalRotate(gui.controlledCheckbox("Silent Rotate",
                CombatSettings.autoCrystalRotate(), CombatSettings::setAutoCrystalRotate));
        CombatSettings.setAutoCrystalAntiSuicide(gui.controlledCheckbox("Anti Suicide",
                CombatSettings.autoCrystalAntiSuicide(), CombatSettings::setAutoCrystalAntiSuicide));
    }

    private void populateAutoCrystalPlaceSettings(WidgetSurface.Frame gui) {
        float placeDelay = gui.slider("Place Delay", CombatSettings.autoCrystalPlaceDelaySlider(),
                Math.round(CombatSettings.autoCrystalPlaceDelay()) + "t",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 10.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 10.0f),
                CombatSettings::setAutoCrystalPlaceDelaySlider);
        CombatSettings.setAutoCrystalPlaceDelaySlider(placeDelay);
    }

    private void populateAutoCrystalBreakSettings(WidgetSurface.Frame gui) {
        float breakDelay = gui.slider("Break Delay", CombatSettings.autoCrystalBreakDelaySlider(),
                Math.round(CombatSettings.autoCrystalBreakDelay()) + "t",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 10.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 10.0f),
                CombatSettings::setAutoCrystalBreakDelaySlider);
        CombatSettings.setAutoCrystalBreakDelaySlider(breakDelay);
    }

    private void populateAutoCrystalFacePlaceSettings(WidgetSurface.Frame gui) {
        float faceHealth = gui.slider("Face HP", CombatSettings.autoCrystalFacePlaceHealthSlider(),
                String.format(java.util.Locale.ROOT, "%.1f", CombatSettings.autoCrystalFacePlaceHealth()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 20.0f, 1),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 20.0f),
                CombatSettings::setAutoCrystalFacePlaceHealthSlider);
        CombatSettings.setAutoCrystalFacePlaceHealthSlider(faceHealth);
    }

    private void populateAutoCrystalSettings(WidgetSurface.Frame gui) {
        populateAutoCrystalGeneralSettings(gui);
        gui.divider();
        populateAutoCrystalPlaceSettings(gui);
        populateAutoCrystalBreakSettings(gui);
        populateAutoCrystalFacePlaceSettings(gui);
    }

    private int drawVisualGeneralCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureVisualGeneralCard(x, stableY, w);
        h = drawCard(graphics, "GENERAL", x, y, w, h);
        enableCardContentScissor(x, y, w, h);
        WidgetSurface.Frame gui = beginClippedCardGui(graphics, x, y, stableY, w, h);
        try {
            populateVisualGeneral(gui);
        } finally {
            restoreContentScissor();
        }
        return h;
    }

    private int drawVisualModelCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureVisualModelCard(x, stableY, w);
        h = drawCard(graphics, "MODEL", x, y, w, h);
        enableCardContentScissor(x, y, w, h);
        WidgetSurface.Frame gui = beginClippedCardGui(graphics, x, y, stableY, w, h);
        try {
            populateVisualModel(gui);
        } finally {
            restoreContentScissor();
        }
        return h;
    }

    private void populateVisualGeneral(WidgetSurface.Frame gui) {
        String mode = previewModelMode.name();
        gui.mode("visual-general-mode", mode, PreviewModelMode.ENEMIES.name(), this::populateEnemyVisualGeneral);
        gui.mode("visual-general-mode", mode, PreviewModelMode.TEAM.name(), block ->
                VisualSettings.setVisualTeamEnabled(block.controlledCheckboxWithSettings("Enable",
                        VisualSettings.visualTeamEnabled(), this::populateTeamVisualGeneralSettings,
                        VisualSettings::setVisualTeamEnabled)));
        gui.mode("visual-general-mode", mode, PreviewModelMode.LOCAL.name(), block ->
                VisualSettings.setVisualLocalEnabled(block.controlledCheckboxWithSettings("Enable",
                        VisualSettings.visualLocalEnabled(), this::populateLocalVisualGeneralSettings,
                        VisualSettings::setVisualLocalEnabled)));
    }

    private void populateVisualModel(WidgetSurface.Frame gui) {
        String mode = previewModelMode.name();
        gui.mode("visual-model-mode", mode, PreviewModelMode.ENEMIES.name(), this::populateEnemyVisualModel);
        gui.mode("visual-model-mode", mode, PreviewModelMode.TEAM.name(), this::populateTeamVisualModel);
        gui.mode("visual-model-mode", mode, PreviewModelMode.LOCAL.name(), this::populateLocalVisualModel);
    }

    private void populateEnemyVisualGeneral(WidgetSurface.Frame gui) {
        VisualSettings.setVisualEspEnabled(gui.controlledCheckboxWithSettings("Enable",
                VisualSettings.visualEspEnabled(), this::populateEnemyVisualFilterSettings,
                VisualSettings::setVisualEspEnabled));
        VisualSettings.setVisualOffscreenArrow(gui.controlledCheckboxWithSettings("Offscreen Arrow",
                VisualSettings.visualOffscreenArrow(), this::populateOffscreenArrowSettings,
                VisualSettings::setVisualOffscreenArrow));
        VisualSettings.setVisualSounds(gui.controlledCheckboxWithColor("Sounds",
                VisualSettings.visualSounds(), VisualSettings.visualSoundsColor(),
                VisualSettings::setVisualSounds, VisualSettings::setVisualSoundsColor));
    }

    private void populateEnemyVisualFilterSettings(WidgetSurface.Frame gui) {
        float range = gui.slider("Range", VisualSettings.visualEnemyRangeSlider(),
                Math.round(VisualSettings.visualEnemyRange()) + "m",
                value -> SliderValueFormat.intRangeLabel(value, 16.0f, 128.0f, "m"),
                value -> SliderValueFormat.parseRangeSlider(value, 16.0f, 128.0f),
                VisualSettings::setVisualEnemyRangeSlider);
        VisualSettings.setVisualEnemyRangeSlider(range);
        int targetMask = gui.multiSelect("Targets", VisualSettings.visualEnemiesTargetMask(),
                "Players, Hostile Mobs, Passive Mobs, Other Living",
                VisualSettings::setVisualEnemiesTargetMask);
        VisualSettings.setVisualEnemiesTargetMask(targetMask);
        VisualSettings.setVisualEnemiesShowInvisible(gui.controlledCheckbox("Show Invisible",
                VisualSettings.visualEnemiesShowInvisible(), VisualSettings::setVisualEnemiesShowInvisible));
        VisualSettings.setVisualEnemiesThroughWalls(gui.controlledCheckbox("Through Walls",
                VisualSettings.visualEnemiesThroughWalls(), VisualSettings::setVisualEnemiesThroughWalls));
        VisualSettings.setVisualEnemiesIgnoreBots(gui.controlledCheckbox("Ignore Bots",
                VisualSettings.visualEnemiesIgnoreBots(), VisualSettings::setVisualEnemiesIgnoreBots));
    }

    private void populateTeamVisualGeneralSettings(WidgetSurface.Frame gui) {
        float range = gui.slider("Range", VisualSettings.visualTeamRangeSlider(),
                Math.round(VisualSettings.visualTeamRange()) + "m",
                value -> SliderValueFormat.intRangeLabel(value, 16.0f, 128.0f, "m"),
                value -> SliderValueFormat.parseRangeSlider(value, 16.0f, 128.0f),
                VisualSettings::setVisualTeamRangeSlider);
        VisualSettings.setVisualTeamRangeSlider(range);
        VisualSettings.setVisualTeamShowInvisible(gui.controlledCheckbox("Show Invisible",
                VisualSettings.visualTeamShowInvisible(), VisualSettings::setVisualTeamShowInvisible));
        VisualSettings.setVisualTeamThroughWalls(gui.controlledCheckbox("Through Walls",
                VisualSettings.visualTeamThroughWalls(), VisualSettings::setVisualTeamThroughWalls));
    }

    private void populateLocalVisualGeneralSettings(WidgetSurface.Frame gui) {
        float range = gui.slider("Range", VisualSettings.visualLocalRangeSlider(),
                Math.round(VisualSettings.visualLocalRange()) + "m",
                value -> SliderValueFormat.intRangeLabel(value, 16.0f, 128.0f, "m"),
                value -> SliderValueFormat.parseRangeSlider(value, 16.0f, 128.0f),
                VisualSettings::setVisualLocalRangeSlider);
        VisualSettings.setVisualLocalRangeSlider(range);
        VisualSettings.setVisualLocalShowInvisible(gui.controlledCheckbox("Show Invisible",
                VisualSettings.visualLocalShowInvisible(), VisualSettings::setVisualLocalShowInvisible));
        VisualSettings.setVisualLocalThroughWalls(gui.controlledCheckbox("Through Walls",
                VisualSettings.visualLocalThroughWalls(), VisualSettings::setVisualLocalThroughWalls));
    }

    private void populateEnemyVisualModel(WidgetSurface.Frame gui) {
        VisualSettings.setVisualPlayerChams(gui.controlledCheckboxWithSettings("Player",
                VisualSettings.visualPlayerChams(), this::populatePlayerChamsSettings,
                VisualSettings::setVisualPlayerChams));
        VisualSettings.setVisualBehindWallsChams(gui.controlledCheckboxWithSettings("Behind Walls",
                VisualSettings.visualBehindWallsChams(), this::populateBehindWallsChamsSettings,
                VisualSettings::setVisualBehindWallsChams));
        VisualSettings.setVisualOnHitChams(gui.controlledCheckboxWithSettings("On Hit",
                VisualSettings.visualOnHitChams(), this::populateOnHitChamsSettings,
                VisualSettings::setVisualOnHitChams));
        VisualSettings.setVisualRagdollsChams(gui.controlledCheckboxWithSettings("Ragdolls",
                VisualSettings.visualRagdollsChams(), this::populateRagdollsChamsSettings,
                VisualSettings::setVisualRagdollsChams));
        VisualSettings.setVisualSoulParticles(gui.controlledCheckboxWithColor("Soul Particles",
                VisualSettings.visualSoulParticles(), VisualSettings.visualSoulParticlesColor(),
                VisualSettings::setVisualSoulParticles, VisualSettings::setVisualSoulParticlesColor));
        VisualSettings.setVisualGlow(gui.controlledCheckboxWithColor("Glow", "Enemy Glow",
                VisualSettings.visualGlow(), VisualSettings.visualGlowColor(),
                VisualSettings::setVisualGlow, VisualSettings::setVisualGlowColor, false));
    }

    private void populateTeamVisualModel(WidgetSurface.Frame gui) {
        VisualSettings.setVisualTeamPlayerChams(gui.controlledCheckboxWithSettings("Player",
                VisualSettings.visualTeamPlayerChams(), this::populateTeamPlayerChamsSettings,
                VisualSettings::setVisualTeamPlayerChams));
        VisualSettings.setVisualTeamBehindWallsChams(gui.controlledCheckboxWithSettings("Behind Walls",
                VisualSettings.visualTeamBehindWallsChams(), this::populateTeamBehindWallsChamsSettings,
                VisualSettings::setVisualTeamBehindWallsChams));
        VisualSettings.setVisualTeamGlow(gui.controlledCheckboxWithColor("Glow", "Team Glow",
                VisualSettings.visualTeamGlow(), VisualSettings.visualTeamGlowColor(),
                VisualSettings::setVisualTeamGlow, VisualSettings::setVisualTeamGlowColor, false));
        VisualSettings.setVisualTeamDisableModelRendering(gui.controlledCheckbox("Disable Model Rendering",
                VisualSettings.visualTeamDisableModelRendering(), VisualSettings::setVisualTeamDisableModelRendering));
    }

    private void populateLocalVisualModel(WidgetSurface.Frame gui) {
        VisualSettings.setVisualLocalPlayerChams(gui.controlledCheckboxWithSettings("Player",
                VisualSettings.visualLocalPlayerChams(), this::populateLocalPlayerChamsSettings,
                VisualSettings::setVisualLocalPlayerChams));
        VisualSettings.setVisualLocalTransparencyEnabled(gui.controlledCheckboxWithSettings("Transparency",
                VisualSettings.visualLocalTransparencyEnabled(), this::populateLocalTransparencySettings,
                VisualSettings::setVisualLocalTransparencyEnabled));
        VisualSettings.setVisualLocalItem(gui.controlledCheckboxWithSettings("Item",
                VisualSettings.visualLocalItem(), this::populateLocalItemSettings,
                VisualSettings::setVisualLocalItem));
        VisualSettings.setVisualLocalArms(gui.controlledCheckboxWithSettings("Arms",
                VisualSettings.visualLocalArms(), this::populateLocalArmsSettings,
                VisualSettings::setVisualLocalArms));
        VisualSettings.setVisualLocalGlow(gui.controlledCheckboxWithColor("Glow", "Local Glow",
                VisualSettings.visualLocalGlow(), VisualSettings.visualLocalGlowColor(),
                VisualSettings::setVisualLocalGlow, VisualSettings::setVisualLocalGlowColor, false));
    }

    private void populateOffscreenArrowSettings(WidgetSurface.Frame gui) {
        float size = gui.slider("Size", VisualSettings.visualOffscreenArrowSizeSlider(),
                Math.round(VisualSettings.visualOffscreenArrowSize()) + "px",
                value -> SliderValueFormat.intRangeLabel(value, 6.0f, 24.0f, "px"),
                value -> SliderValueFormat.parseRangeSlider(value, 6.0f, 24.0f),
                VisualSettings::setVisualOffscreenArrowSizeSlider);
        VisualSettings.setVisualOffscreenArrowSizeSlider(size);
        float radius = gui.slider("Radius", VisualSettings.visualOffscreenArrowRadiusSlider(),
                Math.round(VisualSettings.visualOffscreenArrowRadius()) + "px",
                value -> SliderValueFormat.intRangeLabel(value, 40.0f, 260.0f, "px"),
                value -> SliderValueFormat.parseRangeSlider(value, 40.0f, 260.0f),
                VisualSettings::setVisualOffscreenArrowRadiusSlider);
        VisualSettings.setVisualOffscreenArrowRadiusSlider(radius);
        int displayMask = gui.multiSelect("Display", VisualSettings.visualOffscreenArrowDisplayMask(),
                "Name, Distance", VisualSettings::setVisualOffscreenArrowDisplayMask);
        VisualSettings.setVisualOffscreenArrowDisplayMask(displayMask);
        VisualSettings.setVisualOffscreenArrowColor(gui.colorPicker("Color",
                VisualSettings.visualOffscreenArrowColor(), VisualSettings::setVisualOffscreenArrowColor));
    }

    private void populatePlayerChamsSettings(WidgetSurface.Frame gui) {
        populateChamsMaterialSettings(gui, VisualSettings.visualPlayerChamsMode(),
                VisualSettings::setVisualPlayerChamsMode, VisualSettings.visualPlayerChamsColor(),
                VisualSettings::setVisualPlayerChamsColor);
    }

    private void populateBehindWallsChamsSettings(WidgetSurface.Frame gui) {
        populateChamsMaterialSettings(gui, VisualSettings.visualBehindWallsChamsMode(),
                VisualSettings::setVisualBehindWallsChamsMode, VisualSettings.visualBehindWallsChamsColor(),
                VisualSettings::setVisualBehindWallsChamsColor);
    }

    private void populateTeamPlayerChamsSettings(WidgetSurface.Frame gui) {
        populateChamsMaterialSettings(gui, VisualSettings.visualTeamPlayerChamsMode(),
                VisualSettings::setVisualTeamPlayerChamsMode, VisualSettings.visualTeamPlayerChamsColor(),
                VisualSettings::setVisualTeamPlayerChamsColor);
    }

    private void populateTeamBehindWallsChamsSettings(WidgetSurface.Frame gui) {
        populateChamsMaterialSettings(gui, VisualSettings.visualTeamBehindWallsChamsMode(),
                VisualSettings::setVisualTeamBehindWallsChamsMode, VisualSettings.visualTeamBehindWallsChamsColor(),
                VisualSettings::setVisualTeamBehindWallsChamsColor);
    }

    private void populateLocalPlayerChamsSettings(WidgetSurface.Frame gui) {
        populateChamsMaterialSettings(gui, VisualSettings.visualLocalPlayerChamsMode(),
                VisualSettings::setVisualLocalPlayerChamsMode, VisualSettings.visualLocalPlayerChamsColor(),
                VisualSettings::setVisualLocalPlayerChamsColor);
    }

    private void populateLocalItemSettings(WidgetSurface.Frame gui) {
        populateChamsMaterialSettings(gui, VisualSettings.visualLocalItemMode(),
                VisualSettings::setVisualLocalItemMode, VisualSettings.visualLocalItemColor(),
                VisualSettings::setVisualLocalItemColor);
    }

    private void populateLocalArmsSettings(WidgetSurface.Frame gui) {
        populateChamsMaterialSettings(gui, VisualSettings.visualLocalArmsMode(),
                VisualSettings::setVisualLocalArmsMode, VisualSettings.visualLocalArmsColor(),
                VisualSettings::setVisualLocalArmsColor);
    }

    private void populateLocalTransparencySettings(WidgetSurface.Frame gui) {
        float transparency = gui.slider("Amount", VisualSettings.visualLocalTransparencySlider(),
                Math.round(VisualSettings.visualLocalTransparency() * 100.0f) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 100.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 100.0f),
                VisualSettings::setVisualLocalTransparencySlider);
        VisualSettings.setVisualLocalTransparencySlider(transparency);
        int disableMask = gui.multiSelect("Disable When", VisualSettings.visualLocalTransparencyDisableMask(),
                "First Person, Holding Item, Using Item, Sneaking, Swimming",
                VisualSettings::setVisualLocalTransparencyDisableMask);
        VisualSettings.setVisualLocalTransparencyDisableMask(disableMask);
    }

    private void populateOnHitChamsSettings(WidgetSurface.Frame gui) {
        populateChamsMaterialSettings(gui, VisualSettings.visualOnHitChamsMode(),
                VisualSettings::setVisualOnHitChamsMode, VisualSettings.visualOnHitChamsColor(),
                VisualSettings::setVisualOnHitChamsColor);
    }

    private void populateRagdollsChamsSettings(WidgetSurface.Frame gui) {
        populateChamsMaterialSettings(gui, VisualSettings.visualRagdollsChamsMode(),
                VisualSettings::setVisualRagdollsChamsMode, VisualSettings.visualRagdollsChamsColor(),
                VisualSettings::setVisualRagdollsChamsColor);
    }

    private void populateSoulParticlesSettings(WidgetSurface.Frame gui) {
        populateChamsMaterialSettings(gui, VisualSettings.visualSoulParticlesMode(),
                VisualSettings::setVisualSoulParticlesMode, VisualSettings.visualSoulParticlesColor(),
                VisualSettings::setVisualSoulParticlesColor);
    }

    private void populateChamsMaterialSettings(WidgetSurface.Frame gui, String mode, Consumer<String> modeSetter,
                                               int color, Consumer<Integer> colorSetter) {
        modeSetter.accept(gui.combo("Material", mode,
                "Shaded, Solid, Glow, Iridescent, Water Flow, Glossy", modeSetter));
        colorSetter.accept(gui.colorPicker("Color", color, colorSetter));
    }

    private int drawMovementGroupCard(GuiGraphics graphics, String title, int x, int y, int stableY, int w,
                                      java.util.function.Consumer<WidgetSurface.Frame> populator) {
        int h = measureMovementGroupCard(x, stableY, w, populator);
        h = drawCard(graphics, title, x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populator.accept(gui);
        return h;
    }

    private int measureMovementGroupCard(int x, int y, int w, java.util.function.Consumer<WidgetSurface.Frame> populator) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populator.accept(gui);
        return gui.cardHeight();
    }

    private int drawMovementCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureMovementCard(x, stableY, w);
        h = drawCard(graphics, "MOVEMENT", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateMovement(gui);
        return h;
    }

    private void populateMovement(WidgetSurface.Frame gui) {
        populateMovementGeneral(gui);
        gui.divider();
        MovementSettingsPage.populateMovementFlight(gui);
        gui.divider();
        MovementSettingsPage.populateMovementVehicle(gui);
        gui.divider();
        MovementSettingsPage.populateMovementEnvironment(gui);
        gui.divider();
        MovementSettingsPage.populateMovementExploit(gui);
    }

    private void populateMovementGeneral(WidgetSurface.Frame gui) {
        VisualSettings.setQuickStopEnabled(gui.controlledCheckbox("Quick Stop", VisualSettings.quickStopEnabled(), VisualSettings::setQuickStopEnabled));
        VisualSettings.setBhopTurnEnabled(gui.controlledCheckbox("Bhop Turn", VisualSettings.bhopTurnEnabled(), VisualSettings::setBhopTurnEnabled));
        VisualSettings.setNoSlowEnabled(gui.controlledCheckboxWithSettings("No Slow",
                VisualSettings.noSlowEnabled(), MovementSettingsPage::populateNoSlowSettings,
                VisualSettings::setNoSlowEnabled));
        VisualSettings.setFastLadderEnabled(gui.controlledCheckbox("Fast Ladder", VisualSettings.fastLadderEnabled(), VisualSettings::setFastLadderEnabled));
        VisualSettings.setFreezeEnabled(gui.controlledCheckbox("Freeze",
                VisualSettings.freezeEnabled(), VisualSettings::setFreezeEnabled));
        setMovementSpeedEnabled(gui.controlledCheckboxWithSettings("Speed",
                movementSpeedEnabled(), this::populateMovementSpeedSettings,
                this::setMovementSpeedEnabled));
    }

    private void populateMovementSpeedSettings(WidgetSurface.Frame gui) {
        setMovementSpeedMode(gui.combo("Mode", movementSpeedMode(),
                "Keep Sprint, Strafe, BHop, Vanilla, CubeCraftFast, CubeCraftLowHop",
                this::setMovementSpeedMode));
        String mode = movementSpeedMode();
        gui.visible("speed-keep-sprint-settings", "Keep Sprint".equals(mode),
                MovementSettingsPage::populateKeepSprintSettings);
        gui.visible("speed-full-settings",
                !"Keep Sprint".equals(mode)
                        && !"CubeCraftFast".equals(mode)
                        && !"CubeCraftLowHop".equals(mode),
                MovementSettingsPage::populateFullSpeedSettings);
        gui.visible("speed-cubecraft-settings",
                "CubeCraftFast".equals(mode) || "CubeCraftLowHop".equals(mode),
                MovementSettingsPage::populateCubeCraftSpeedSettings);
    }

    private boolean movementSpeedEnabled() {
        return VisualSettings.speedEnabled() || VisualSettings.keepSprintEnabled();
    }

    private String movementSpeedMode() {
        if (movementSpeedModeSelection == null) {
            movementSpeedModeSelection = VisualSettings.keepSprintEnabled() && !VisualSettings.speedEnabled()
                    ? "Keep Sprint"
                    : VisualSettings.speedMode();
        }
        return movementSpeedModeSelection;
    }

    private void setMovementSpeedEnabled(boolean enabled) {
        if (!enabled) {
            VisualSettings.setSpeedEnabled(false);
            VisualSettings.setKeepSprintEnabled(false);
            return;
        }
        if ("Keep Sprint".equals(movementSpeedMode())) {
            VisualSettings.setSpeedEnabled(false);
            VisualSettings.setKeepSprintEnabled(true);
        } else {
            VisualSettings.setKeepSprintEnabled(false);
            VisualSettings.setSpeedEnabled(true);
        }
    }

    private void setMovementSpeedMode(String mode) {
        movementSpeedModeSelection = "Keep Sprint".equals(mode) ? "Keep Sprint" : mode;
        boolean enabled = movementSpeedEnabled();
        if ("Keep Sprint".equals(mode)) {
            VisualSettings.setSpeedEnabled(false);
            VisualSettings.setKeepSprintEnabled(enabled);
            return;
        }
        VisualSettings.setSpeedMode(mode);
        VisualSettings.setKeepSprintEnabled(false);
        VisualSettings.setSpeedEnabled(enabled);
    }

    private int drawScaffoldCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureScaffoldCard(x, stableY, w);
        h = drawCard(graphics, "SCAFFOLD", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateScaffold(gui);
        return h;
    }

    private void populateScaffold(WidgetSurface.Frame gui) {
        VisualSettings.setScaffoldEnabled(gui.controlledCheckboxWithSettings("Enabled",
                VisualSettings.scaffoldEnabled(), this::populateScaffoldSettings,
                VisualSettings::setScaffoldEnabled));
        String mode = VisualSettings.scaffoldMode();
        VisualSettings.setScaffoldMode(gui.combo("Mode", mode,
                "TellyBridge, GodBridge, Heypixel, Hypixel, SilenceTelly, Vanilla, Bloxd, AntiGamingChair, Watchdog",
                VisualSettings::setScaffoldMode));
        mode = VisualSettings.scaffoldMode();
        VisualSettings.setScaffoldSwapMode(gui.combo("Swap Mode", VisualSettings.scaffoldSwapMode(),
                "None, Normal, Silent", VisualSettings::setScaffoldSwapMode));
        gui.mode("bridge-mode", mode, "TellyBridge", block -> {
            float tellyTicks = block.slider("Telly Ticks", VisualSettings.scaffoldTellyTicksSlider(),
                    VisualSettings.scaffoldTellyTicks() + "t",
                    value -> SliderValueFormat.intRangeLabel(value, 0.0f, 6.0f, "t"),
                    value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 6.0f),
                    VisualSettings::setScaffoldTellyTicksSlider);
            VisualSettings.setScaffoldTellyTicksSlider(tellyTicks);
        });
        gui.mode("bridge-mode", mode, "GodBridge", block -> {
            VisualSettings.setScaffoldSafeWalk(block.controlledCheckbox("Safe Walk",
                    VisualSettings.scaffoldSafeWalk(), VisualSettings::setScaffoldSafeWalk));
        });
        gui.mode("bridge-mode", mode, "Heypixel", block -> {
            VisualSettings.setScaffoldTelly(block.controlledCheckbox("Telly",
                    VisualSettings.scaffoldTelly(), VisualSettings::setScaffoldTelly));
            float tellyTicks = block.slider("Telly Ticks", VisualSettings.scaffoldTellyTicksSlider(),
                    VisualSettings.scaffoldTellyTicks() + "t",
                    value -> SliderValueFormat.intRangeLabel(value, 0.0f, 6.0f, "t"),
                    value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 6.0f),
                    VisualSettings::setScaffoldTellyTicksSlider);
            VisualSettings.setScaffoldTellyTicksSlider(tellyTicks);
            VisualSettings.setScaffoldSafeWalk(block.controlledCheckbox("Safe Walk",
                    VisualSettings.scaffoldSafeWalk(), VisualSettings::setScaffoldSafeWalk));
            VisualSettings.setScaffoldInteractBeforePlace(block.controlledCheckbox("Interact Before Place",
                    VisualSettings.scaffoldInteractBeforePlace(),
                    VisualSettings::setScaffoldInteractBeforePlace));
            VisualSettings.setScaffoldDuplicateRotPlace(block.controlledCheckbox("Duplicate Rot Place",
                    VisualSettings.scaffoldDuplicateRotPlace(),
                    VisualSettings::setScaffoldDuplicateRotPlace));
        });
        gui.mode("bridge-mode", mode, "Hypixel", block -> {
            VisualSettings.setScaffoldInteractBeforePlace(block.controlledCheckbox("Interact Before Place",
                    VisualSettings.scaffoldInteractBeforePlace(),
                    VisualSettings::setScaffoldInteractBeforePlace));
            VisualSettings.setScaffoldDuplicateRotPlace(block.controlledCheckbox("Duplicate Rot Place",
                    VisualSettings.scaffoldDuplicateRotPlace(),
                    VisualSettings::setScaffoldDuplicateRotPlace));
        });
        gui.mode("bridge-mode", mode, "SilenceTelly", block -> {
            VisualSettings.setScaffoldSilenceTellyMode(block.combo("Silence Mode",
                    VisualSettings.scaffoldSilenceTellyMode(), "Normal, Snap, Telly",
                    VisualSettings::setScaffoldSilenceTellyMode));
            float placeTick = block.slider("Place Tick", VisualSettings.scaffoldSilenceTellyPlaceTickSlider(),
                    VisualSettings.scaffoldSilenceTellyPlaceTick() + "t",
                    value -> SliderValueFormat.intRangeLabel(value, 1.0f, 5.0f, "t"),
                    value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 5.0f),
                    VisualSettings::setScaffoldSilenceTellyPlaceTickSlider);
            VisualSettings.setScaffoldSilenceTellyPlaceTickSlider(placeTick);
            float rotationTick = block.slider("Rotation Tick", VisualSettings.scaffoldSilenceTellyRotationTickSlider(),
                    VisualSettings.scaffoldSilenceTellyRotationTick() + "t",
                    value -> SliderValueFormat.intRangeLabel(value, 1.0f, 5.0f, "t"),
                    value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 5.0f),
                    VisualSettings::setScaffoldSilenceTellyRotationTickSlider);
            VisualSettings.setScaffoldSilenceTellyRotationTickSlider(rotationTick);
            VisualSettings.setScaffoldSilenceTellySnap(block.controlledCheckbox("Snap",
                    VisualSettings.scaffoldSilenceTellySnap(), VisualSettings::setScaffoldSilenceTellySnap));
            VisualSettings.setScaffoldSilenceTellyGodBridge(block.controlledCheckbox("God Bridge",
                    VisualSettings.scaffoldSilenceTellyGodBridge(), VisualSettings::setScaffoldSilenceTellyGodBridge));
            VisualSettings.setScaffoldSilenceTellyNoUptelly(block.controlledCheckbox("No UpTelly",
                    VisualSettings.scaffoldSilenceTellyNoUptelly(), VisualSettings::setScaffoldSilenceTellyNoUptelly));
            VisualSettings.setScaffoldSilenceTellySlowUpTelly(block.controlledCheckbox("Slow UpTelly",
                    VisualSettings.scaffoldSilenceTellySlowUpTelly(), VisualSettings::setScaffoldSilenceTellySlowUpTelly));
            VisualSettings.setScaffoldSilenceTellyHeypixelUpTelly(block.controlledCheckbox("Heypixel UpTelly",
                    VisualSettings.scaffoldSilenceTellyHeypixelUpTelly(),
                    VisualSettings::setScaffoldSilenceTellyHeypixelUpTelly));
            VisualSettings.setScaffoldSilenceTellyEagle(block.controlledCheckbox("Eagle",
                    VisualSettings.scaffoldSilenceTellyEagle(), VisualSettings::setScaffoldSilenceTellyEagle));
            float eagleTick = block.slider("Eagle Tick", VisualSettings.scaffoldSilenceTellyEagleTickSlider(),
                    VisualSettings.scaffoldSilenceTellyEagleTick() + "t",
                    value -> SliderValueFormat.intRangeLabel(value, 1.0f, 5.0f, "t"),
                    value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 5.0f),
                    VisualSettings::setScaffoldSilenceTellyEagleTickSlider);
            VisualSettings.setScaffoldSilenceTellyEagleTickSlider(eagleTick);
            float keepEagle = block.slider("Keep Eagle", VisualSettings.scaffoldSilenceTellyKeepEagleTickSlider(),
                    VisualSettings.scaffoldSilenceTellyKeepEagleTick() + "t",
                    value -> SliderValueFormat.intRangeLabel(value, 1.0f, 5.0f, "t"),
                    value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 5.0f),
                    VisualSettings::setScaffoldSilenceTellyKeepEagleTickSlider);
            VisualSettings.setScaffoldSilenceTellyKeepEagleTickSlider(keepEagle);
            VisualSettings.setScaffoldSilenceTellyJumpMode(block.combo("Jump Mode",
                    VisualSettings.scaffoldSilenceTellyJumpMode(), "None, Normal, Parkour",
                    VisualSettings::setScaffoldSilenceTellyJumpMode));
            VisualSettings.setScaffoldSilenceTellySafeMode(block.controlledCheckbox("Safe Mode",
                    VisualSettings.scaffoldSilenceTellySafeMode(), VisualSettings::setScaffoldSilenceTellySafeMode));
            VisualSettings.setScaffoldSilenceTellyTestOnGround(block.controlledCheckbox("Test On Ground",
                    VisualSettings.scaffoldSilenceTellyTestOnGround(),
                    VisualSettings::setScaffoldSilenceTellyTestOnGround));
            float safeDistance = block.slider("Safe Distance", VisualSettings.scaffoldSilenceTellySafeDistanceSlider(),
                    String.format(java.util.Locale.ROOT, "%.1f", VisualSettings.scaffoldSilenceTellySafeDistance()),
                    value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 5.0f, 1),
                    value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 5.0f),
                    VisualSettings::setScaffoldSilenceTellySafeDistanceSlider);
            VisualSettings.setScaffoldSilenceTellySafeDistanceSlider(safeDistance);
            VisualSettings.setScaffoldSilenceTellyFixRotation(block.controlledCheckbox("Fix Rotation",
                    VisualSettings.scaffoldSilenceTellyFixRotation(),
                    VisualSettings::setScaffoldSilenceTellyFixRotation));
            VisualSettings.setScaffoldSilenceTellyDuplicateRotPlace(block.controlledCheckbox("Duplicate Rot Place",
                    VisualSettings.scaffoldSilenceTellyDuplicateRotPlace(),
                    VisualSettings::setScaffoldSilenceTellyDuplicateRotPlace));
            VisualSettings.setScaffoldSilenceTellyBlockFly(block.controlledCheckbox("Block Fly",
                    VisualSettings.scaffoldSilenceTellyBlockFly(),
                    VisualSettings::setScaffoldSilenceTellyBlockFly));
            VisualSettings.setScaffoldSilenceTellyAbuseRotation(block.controlledCheckbox("Abuse Rotation",
                    VisualSettings.scaffoldSilenceTellyAbuseRotation(),
                    VisualSettings::setScaffoldSilenceTellyAbuseRotation));
            VisualSettings.setScaffoldSilenceTellyDebug(block.controlledCheckbox("Debug",
                    VisualSettings.scaffoldSilenceTellyDebug(),
                    VisualSettings::setScaffoldSilenceTellyDebug));
        });
        boolean towerMode = "Vanilla".equals(mode) || "Bloxd".equals(mode)
                || "AntiGamingChair".equals(mode) || "Watchdog".equals(mode);
        gui.visible("scaffold-tower", towerMode, block -> VisualSettings.setScaffoldTower(
                block.controlledCheckbox("Tower", VisualSettings.scaffoldTower(),
                        VisualSettings::setScaffoldTower)));
    }

    private void populateScaffoldSettings(WidgetSurface.Frame gui) {
        String mode = VisualSettings.scaffoldMode();
        gui.mode("swap-mode-settings", VisualSettings.scaffoldSwapMode(), "Normal", block -> {
            VisualSettings.setScaffoldSwapBack(block.controlledCheckbox("Swap Back",
                    VisualSettings.scaffoldSwapBack(), VisualSettings::setScaffoldSwapBack));
        });
        gui.mode("bridge-settings", mode, "GodBridge", block -> {
            VisualSettings.setScaffoldSnap(block.controlledCheckbox("Snap",
                    VisualSettings.scaffoldSnap(), VisualSettings::setScaffoldSnap));
        });
        gui.mode("bridge-settings", mode, "Heypixel", block -> {
            VisualSettings.setScaffoldSnap(block.controlledCheckbox("Snap",
                    VisualSettings.scaffoldSnap(), VisualSettings::setScaffoldSnap));
        });
        VisualSettings.setScaffoldRotationMode(gui.combo("Rotation Mode", VisualSettings.scaffoldRotationMode(),
                "Rise, Hypixel", VisualSettings::setScaffoldRotationMode));
        gui.mode("rotation-mode-settings", VisualSettings.scaffoldRotationMode(), "Rise", block -> {
            float rotateSpeed = block.slider("Rotation Speed", VisualSettings.scaffoldRotationSpeedSlider(),
                    VisualSettings.scaffoldRotationSpeed() + "",
                    value -> SliderValueFormat.intRangeLabel(value, 1.0f, 10.0f, ""),
                    value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 10.0f),
                    VisualSettings::setScaffoldRotationSpeedSlider);
            VisualSettings.setScaffoldRotationSpeedSlider(rotateSpeed);
        });
        VisualSettings.setScaffoldRaytraceMode(gui.combo("Raytrace", VisualSettings.scaffoldRaytraceMode(),
                "Normal, Strict", VisualSettings::setScaffoldRaytraceMode));
        gui.mode("bridge-back-rotation-settings", mode, "TellyBridge", block -> {
            float rotateBackSpeed = block.slider("Rotation Back Speed", VisualSettings.scaffoldRotationBackSpeedSlider(),
                    VisualSettings.scaffoldRotationBackSpeed() + "",
                    value -> SliderValueFormat.intRangeLabel(value, 1.0f, 10.0f, ""),
                    value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 10.0f),
                    VisualSettings::setScaffoldRotationBackSpeedSlider);
            VisualSettings.setScaffoldRotationBackSpeedSlider(rotateBackSpeed);
        });
        VisualSettings.setScaffoldSwingHand(gui.controlledCheckbox("Swing Hand",
                VisualSettings.scaffoldSwingHand(), VisualSettings::setScaffoldSwingHand));
    }

    private int drawMovementUtilityCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureMovementUtilityCard(x, stableY, w);
        h = drawCard(graphics, "EDGE", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateMovementUtility(gui);
        return h;
    }

    private void populateMovementUtility(WidgetSurface.Frame gui) {
        VisualSettings.setSafeWalkEnabled(gui.controlledCheckboxWithSettings("Safe Walk",
                VisualSettings.safeWalkEnabled(), this::populateSafeWalkSettings,
                VisualSettings::setSafeWalkEnabled));
        VisualSettings.setEdgeJumpEnabled(gui.controlledCheckbox("Edge Jump", VisualSettings.edgeJumpEnabled(), VisualSettings::setEdgeJumpEnabled));
    }

    private void populateSafeWalkSettings(WidgetSurface.Frame gui) {
        VisualSettings.setSafeWalkMode(gui.combo("Mode", VisualSettings.safeWalkMode(),
                "Normal, Freeze", VisualSettings::setSafeWalkMode));
    }

    private int drawWorldResourceEspCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureWorldResourceEspCard(x, stableY, w);
        h = drawCard(graphics, "RESOURCE ESP", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateWorldResourceEsp(gui);
        return h;
    }

    private void populateWorldResourceEsp(WidgetSurface.Frame gui) {
        VisualSettings.setOreGlowEnabled(gui.controlledCheckboxWithSettings("Ore Glow",
                VisualSettings.oreGlowEnabled(), this::populateOreGlowSettings,
                VisualSettings::setOreGlowEnabled));
        VisualSettings.setChestGlowEnabled(gui.controlledCheckboxWithSettings("Container Glow",
                VisualSettings.chestGlowEnabled(), this::populateChestGlowSettings,
                VisualSettings::setChestGlowEnabled));
        VisualSettings.setXrayEnabled(gui.controlledCheckboxWithSettings("X-Ray",
                VisualSettings.xrayEnabled(), this::populateWorldXraySettings,
                VisualSettings::setXrayEnabled));
        VisualSettings.setSpawnerEspEnabled(gui.controlledCheckboxWithSettings("Spawner ESP",
                VisualSettings.spawnerEspEnabled(), WorldSettingsPage::populateWorldSpawnerEspSettings,
                VisualSettings::setSpawnerEspEnabled));
    }

    private int drawWorldSpecialBlocksCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureWorldSpecialBlocksCard(x, stableY, w);
        h = drawCard(graphics, "SPECIAL BLOCKS", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        WorldSettingsPage.populateWorldSpecialBlocks(gui);
        return h;
    }

    private int drawWorldRenderCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureWorldRenderCard(x, stableY, w);
        h = drawCard(graphics, "WORLD RENDER", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        WorldSettingsPage.populateWorldRender(gui);
        return h;
    }

    private int drawWorldUtilityCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureWorldUtilityCard(x, stableY, w);
        h = drawCard(graphics, "WORLD UTILITY", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        WorldSettingsPage.populateWorldUtility(gui);
        return h;
    }

    private int drawWorldBuildMineCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureWorldBuildMineCard(x, stableY, w);
        h = drawCard(graphics, "BUILD / MINE", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateWorldBuildMine(gui);
        return h;
    }

    private void populateWorldBuildMine(WidgetSurface.Frame gui) {
        VisualSettings.setAutoMineEnabled(gui.controlledCheckboxWithSettings("Auto Mine",
                VisualSettings.autoMineEnabled(), WorldSettingsPage::populateWorldAutoMineSettings,
                VisualSettings::setAutoMineEnabled));
        VisualSettings.setNukerEnabled(gui.controlledCheckboxWithSettings("Nuker",
                VisualSettings.nukerEnabled(), this::populateWorldNukerSettings,
                VisualSettings::setNukerEnabled));
        VisualSettings.setVeinMinerEnabled(gui.controlledCheckboxWithSettings("Vein Miner",
                VisualSettings.veinMinerEnabled(), this::populateWorldVeinMinerSettings,
                VisualSettings::setVeinMinerEnabled));
        VisualSettings.setFastBreakEnabled(gui.controlledCheckboxWithSettings("Fast Break",
                VisualSettings.fastBreakEnabled(), WorldSettingsPage::populateFastBreakSettings,
                VisualSettings::setFastBreakEnabled));
        VisualSettings.setBreakerEnabled(gui.controlledCheckboxWithSettings("Breaker",
                VisualSettings.breakerEnabled(), WorldSettingsPage::populateBreakerSettings,
                VisualSettings::setBreakerEnabled));
    }

    private void populateWorldXraySettings(WidgetSurface.Frame gui) {
        if (gui.button("Block List", gui.contentWidth(), 24)) {
            openSelectionWindow(SelectionWindowMode.XRAY);
        }
        VisualSettings.setXrayExposedOnly(gui.controlledCheckbox("Exposed Only",
                VisualSettings.xrayExposedOnly(), VisualSettings::setXrayExposedOnly));
    }

    private void populateWorldNukerSettings(WidgetSurface.Frame gui) {
        if (gui.button("Whitelist", gui.contentWidth(), 24)) {
            openSelectionWindow(SelectionWindowMode.NUKER);
        }
        VisualSettings.setNukerTarget(gui.combo("Target", VisualSettings.nukerTarget(),
                "Whitelist, Visible, Same Block", VisualSettings::setNukerTarget));
        float range = gui.slider("Range", VisualSettings.nukerRangeSlider(),
                String.format(java.util.Locale.ROOT, "%.1fm", VisualSettings.nukerRange()),
                value -> SliderValueFormat.decimalRangeLabel(value, 1.0f, 6.0f, 1) + "m",
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 6.0f),
                VisualSettings::setNukerRangeSlider);
        VisualSettings.setNukerRangeSlider(range);
        float delay = gui.slider("Delay", VisualSettings.nukerDelaySlider(),
                VisualSettings.nukerDelayTicks() + "t",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 10.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 10.0f),
                VisualSettings::setNukerDelaySlider);
        VisualSettings.setNukerDelaySlider(delay);
        VisualSettings.setNukerSortMode(gui.combo("Sort", VisualSettings.nukerSortMode(),
                "Closest, Farthest, Top Down, Bottom Up", VisualSettings::setNukerSortMode));
        int extensions = gui.multiSelect("Extensions", VisualSettings.nukerExtensionsMask(),
                "Swing Hand, Packet Mine, Rotate, Skip Feet Level", VisualSettings::setNukerExtensionsMask);
        VisualSettings.setNukerExtensionsMask(extensions);
    }

    private void populateWorldVeinMinerSettings(WidgetSurface.Frame gui) {
        VisualSettings.setVeinMinerMode(gui.combo("Mode", VisualSettings.veinMinerMode(),
                "Basic, Chain", VisualSettings::setVeinMinerMode));
        gui.visible("vein-miner-chain-format", "Chain".equals(VisualSettings.veinMinerMode()), visibleGui ->
                VisualSettings.setVeinMinerFormat(visibleGui.combo("Format", VisualSettings.veinMinerFormat(),
                        "Connected, Tunnel, Small Vein, Sphere, Plane, Stair", VisualSettings::setVeinMinerFormat)));
        float maxBlocks = gui.slider("Max Blocks", VisualSettings.veinMinerMaxBlocksSlider(),
                VisualSettings.veinMinerMaxBlocks() + "",
                value -> SliderValueFormat.intRangeLabel(value, 4.0f, 128.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 4.0f, 128.0f),
                VisualSettings::setVeinMinerMaxBlocksSlider);
        VisualSettings.setVeinMinerMaxBlocksSlider(maxBlocks);
        gui.visible("vein-miner-basic-rotate", "Basic".equals(VisualSettings.veinMinerMode()), visibleGui ->
                VisualSettings.setVeinMinerRotate(visibleGui.checkbox("Rotate", VisualSettings.veinMinerRotate())));
        VisualSettings.setVeinMinerColor(gui.colorPicker("Color",
                VisualSettings.veinMinerColor(), VisualSettings::setVeinMinerColor));
        String keyLabel = listeningVeinMinerKey ? "Key: press a key" : "Key: " + ForgeModEventBridge.veinMinerKeyName();
        if (gui.button(keyLabel, gui.contentWidth(), 24)) {
            listeningVeinMinerKey = true;
        }
    }

    private int drawPlayerCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measurePlayerCard(x, stableY, w);
        h = drawCard(graphics, "PLAYER", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        PlayerInventorySettingsPage.populatePlayer(gui);
        return h;
    }

    private int drawInventoryCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureInventoryCard(x, stableY, w);
        h = drawCard(graphics, "INVENTORY", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateInventory(gui);
        return h;
    }

    private void populateInventory(WidgetSurface.Frame gui) {
        VisualSettings.setChestStealEnabled(gui.controlledCheckboxWithSettings("Chest Steal",
                VisualSettings.chestStealEnabled(), PlayerInventorySettingsPage::populateChestStealSettings,
                VisualSettings::setChestStealEnabled));
        VisualSettings.setInventoryCleanerEnabled(gui.controlledCheckboxWithSettings("Inventory Cleaner",
                VisualSettings.inventoryCleanerEnabled(), this::populateInventoryCleanerSettings,
                VisualSettings::setInventoryCleanerEnabled));
        VisualSettings.setAutoRefillEnabled(gui.controlledCheckboxWithSettings("Auto Refill",
                VisualSettings.autoRefillEnabled(), PlayerInventorySettingsPage::populateAutoRefillSettings,
                VisualSettings::setAutoRefillEnabled));
        VisualSettings.setAutoSortEnabled(gui.controlledCheckboxWithSettings("Auto Sort",
                VisualSettings.autoSortEnabled(), this::populateAutoSortSettings,
                VisualSettings::setAutoSortEnabled));
        VisualSettings.setPearlThrowEnabled(gui.controlledCheckboxWithSettings("Pearl Throw",
                VisualSettings.pearlThrowEnabled(), this::populatePearlThrowSettings,
                VisualSettings::setPearlThrowEnabled));
    }

    private void populatePearlThrowSettings(WidgetSurface.Frame gui) {
        String keyLabel = listeningPearlThrowKey ? "Key: press key/mouse" : "Key: " + ForgeModEventBridge.pearlThrowKeyName();
        if (gui.button(keyLabel, gui.contentWidth(), 24)) {
            listeningPearlThrowKey = true;
        }
        float throwDelay = gui.slider("Throw Delay", VisualSettings.pearlThrowDelaySlider(),
                Math.round(VisualSettings.pearlThrowDelay()) + "t",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 20.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 20.0f),
                VisualSettings::setPearlThrowDelaySlider);
        VisualSettings.setPearlThrowDelaySlider(throwDelay);
        VisualSettings.setPearlThrowMode(gui.combo("Mode", VisualSettings.pearlThrowMode(),
                "Silent, Switch", VisualSettings::setPearlThrowMode));
        gui.visible("pearl-throw-switch-delay", "Switch".equals(VisualSettings.pearlThrowMode()), visibleGui -> {
            float switchDelay = visibleGui.slider("Switch Delay", VisualSettings.pearlThrowSwitchDelaySlider(),
                    Math.round(VisualSettings.pearlThrowSwitchDelay()) + "t",
                    value -> SliderValueFormat.intRangeLabel(value, 0.0f, 10.0f, "t"),
                    value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 10.0f),
                    VisualSettings::setPearlThrowSwitchDelaySlider);
            VisualSettings.setPearlThrowSwitchDelaySlider(switchDelay);
        });
        VisualSettings.setPearlThrowSwing(gui.controlledCheckbox("Swing Hand",
                VisualSettings.pearlThrowSwing(), VisualSettings::setPearlThrowSwing));
    }

    private void populateAutoSortSettings(WidgetSurface.Frame gui) {
        VisualSettings.setAutoSortMode(gui.combo("Mode", VisualSettings.autoSortMode(),
                "Category, Name, Count", VisualSettings::setAutoSortMode));
        VisualSettings.setAutoSortAction(gui.combo("Action", VisualSettings.autoSortAction(),
                "Instant, Delayed", VisualSettings::setAutoSortAction));
        gui.visible("auto-sort-delay", "Delayed".equals(VisualSettings.autoSortAction()), visibleGui -> {
            float delay = visibleGui.slider("Delay", VisualSettings.autoSortDelaySlider(),
                    Math.round(VisualSettings.autoSortDelay()) + "t",
                    value -> SliderValueFormat.intRangeLabel(value, 0.0f, 10.0f, "t"),
                    value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 10.0f),
                    VisualSettings::setAutoSortDelaySlider);
            VisualSettings.setAutoSortDelaySlider(delay);
        });
        VisualSettings.setAutoSortIncludeHotbar(gui.controlledCheckbox("Hotbar",
                VisualSettings.autoSortIncludeHotbar(), VisualSettings::setAutoSortIncludeHotbar));
        String keyLabel = listeningAutoSortKey ? "Key: press a key" : "Key: " + ForgeModEventBridge.autoSortKeyName();
        if (gui.button(keyLabel, gui.contentWidth(), 24)) {
            listeningAutoSortKey = true;
        }
    }

    private void populateInventoryCleanerSettings(WidgetSurface.Frame gui) {
        float delay = gui.slider("Delay", VisualSettings.inventoryCleanerDelaySlider(),
                Math.round(VisualSettings.inventoryCleanerDelay()) + "t",
                value -> SliderValueFormat.intRangeLabel(value, 0.0f, 10.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 0.0f, 10.0f),
                VisualSettings::setInventoryCleanerDelaySlider);
        VisualSettings.setInventoryCleanerDelaySlider(delay);
        VisualSettings.setInventoryCleanerCleanHotbar(gui.controlledCheckbox("Clean Hotbar",
                VisualSettings.inventoryCleanerCleanHotbar(), VisualSettings::setInventoryCleanerCleanHotbar));
        if (gui.button("Trash Items", gui.contentWidth(), 24)) {
            openSelectionWindow(SelectionWindowMode.INVENTORY_CLEANER);
        }
    }

    private int drawPlayerPreviewCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measurePlayerPreviewCard(stableY);
        drawPreviewModelModeButtons(graphics, x + 12, y + 8, w - 24);

        int previewX = x + 12;
        int previewY = y + 38;
        int previewW = w - 24;
        int previewH = h - 48;
        int modelCenterX = previewX + previewW / 2;
        int modelScale = Math.round(clamp(Math.round(previewH * 0.40f), 88, 130) * 0.92f);
        int layoutBoxH = Math.round(modelScale * 2.28f);
        int boxH = Math.round(modelScale * 1.92f) + 4;
        int boxW = Math.round(modelScale * 0.78f);
        int modelBottomY = previewY + (previewH - layoutBoxH) / 2 - 10 + layoutBoxH - 3;
        int boxY = modelBottomY + 3 - boxH;
        drawPreviewEntity(graphics, modelCenterX, modelBottomY, modelScale);

        int boxX = modelCenterX - boxW / 2;
        drawPreviewEspOverlay(graphics, boxX, boxY, boxW, boxH);

        if (previewEspPopupOpen) {
            previewPopupW = Math.min(ESP_ITEMS_POPUP_W, w - 18);
            int anchorX = previewToolbarItemsW > 0 ? previewToolbarItemsX : x + 8;
            int anchorY = previewToolbarItemsH > 0 ? previewToolbarItemsY + previewToolbarItemsH + 6 : y + 42;
            previewPopupX = clamp(anchorX + previewToolbarItemsW - previewPopupW, x + 8, x + w - previewPopupW - 8);
            previewPopupY = anchorY;
        }

        return h;
    }

    private void drawPreviewModelModeButtons(GuiGraphics graphics, int x, int y, int availableW) {
        PreviewModelMode[] modes = {PreviewModelMode.ENEMIES, PreviewModelMode.TEAM, PreviewModelMode.LOCAL};
        int gap = 6;
        int toolbarW = 28;
        float totalW = toolbarW;
        for (PreviewModelMode mode : modes) {
            totalW += previewModelModeButtonWidth(mode) + gap;
        }
        float cursorX = x + Math.max(0.0f, (availableW - totalW) * 0.5f);
        for (PreviewModelMode mode : modes) {
            float buttonW = previewModelModeButtonWidth(mode);
            drawPreviewModelModeButton(graphics, mode, cursorX, y, buttonW, 28);
            cursorX += buttonW + gap;
        }
        drawPreviewToolbar(graphics, Math.round(cursorX), y);
    }

    private float previewModelModeButtonWidth(PreviewModelMode mode) {
        float background = previewModelModeBackground(mode);
        float iconSize = 13.0f;
        float iconW = MSDFRenderer.iconWidth(mode.icon(), iconSize);
        float iconOnlyW = Math.max(30.0f, iconW + 17.0f);
        float fullW = Math.max(54.0f, 10.0f + iconW + 6.0f + MSDFRenderer.width(mode.label(), 11.0f) + 11.0f);
        return iconOnlyW + (fullW - iconOnlyW) * smoothstep(background);
    }

    private float previewModelModeBackground(PreviewModelMode mode) {
        float amount = previewModeAnimation(mode, "selected",
                previewModelMode == mode ? 1.0f : 0.0f);
        return stagedPreviewModeBackground(amount);
    }

    private void drawPreviewModelModeButton(GuiGraphics graphics, PreviewModelMode mode,
                                            float x, int y, float w, int h) {
        boolean active = previewModelMode == mode;
        boolean hovered = isHovering(Math.round(x), y, Math.round(w), h);
        if (dashboardSurface.button("preview-model-mode/" + mode.label(), Math.round(x), y, Math.round(w), h)) {
            previewModelMode = mode;
            lastPreviewModelMode = mode;
        }
        float amount = previewModeAnimation(mode, "selected",
                active ? 1.0f : 0.0f);
        float bg = stagedPreviewModeBackground(amount);
        float text = stagedPreviewModeIncomingText(amount);
        float hoverAnim = previewItemAnimation("model-mode-" + mode.label(), "hover", hovered ? 1.0f : 0.0f);

        int iconColor = lerpColor(PREVIEW_MUTED, PREVIEW_ACCENT, Math.max(bg, text));
        if (bg > 0.01f) {
            ShapeRenderer.roundRect(graphics, Math.round(x), y, Math.round(w), h, 8,
                    alphaColor(PREVIEW_ACCENT_SOFT, bg));
        } else if (hoverAnim > 0.01f) {
            ShapeRenderer.roundRect(graphics, Math.round(x), y, Math.round(w), h, 8,
                    alphaColor(themeColor(0xFFEFF1F7, 0xFF222734), hoverAnim * 0.65f));
        }

        float iconSize = 13.0f;
        float iconW = MSDFRenderer.iconWidth(mode.icon(), iconSize);
        float centeredIconX = x + (w - iconW) * 0.5f;
        float expandedIconX = x + 10.0f;
        float iconX = centeredIconX + (expandedIconX - centeredIconX) * bg;
        MSDFRenderer.drawIconCodepoint(graphics, mode.icon(), iconX, y + (h - iconSize) * 0.5f,
                iconSize, alphaColor(iconColor, contentRenderAlpha));

        if (text > 0.02f) {
            int textColor = alphaColor(PREVIEW_ACCENT, contentRenderAlpha * text);
            int textX = Math.round(x + 10.0f + iconW + 6.0f);
            MSDFRenderer.draw(graphics, mode.label(), textX, y + 7, 11.0f, textColor);
        }
    }

    private float previewModeAnimation(PreviewModelMode mode, String channel, float target) {
        String key = "model-mode/" + mode.label() + "/" + channel;
        float current = previewItemAnimations.getOrDefault(key, target);
        float next = lerp(current, target, 0.03f);
        if (Math.abs(next - target) < 0.005f) {
            next = target;
        }
        previewItemAnimations.put(key, next);
        return next;
    }

    private static float stagedPreviewModeBackground(float amount) {
        return smoothstep((amount - 0.20f) / 0.70f);
    }

    private static float stagedPreviewModeIncomingText(float amount) {
        return smoothstep((amount - 0.50f) / 0.50f);
    }

    private int measurePlayerPreviewCard(int stableY) {
        return Math.max(350, contentBoundsH - 2);
    }

    private void drawPreviewPopupOverlay(GuiGraphics graphics) {
        boolean targetOpen = previewEspPopupOpen && selectedTab == SidebarTab.VISUAL;
        previewEspPopupAlpha = lerp(previewEspPopupAlpha, targetOpen ? 1.0f : 0.0f, UiAnimationTimings.PREVIEW_POPUP);
        if (targetOpen && previewEspPopupAlpha > 0.985f) {
            previewEspPopupAlpha = 1.0f;
        } else if (!targetOpen && previewEspPopupAlpha < 0.015f) {
            previewEspPopupAlpha = 0.0f;
            previewEspPopupRendered = false;
        }
        if (!targetOpen && previewEspPopupAlpha <= 0.0f) {
            return;
        }
        int popupH = espPopupHeight(previewPopupW);
        int popupY = Math.max(previewPopupY, dashboardY + 58);
        previewEspPopupRendered = true;
        float previousContentAlpha = contentRenderAlpha;
        contentRenderAlpha = clampFloat(previousContentAlpha * smoothstep(previewEspPopupAlpha), 0.0f, 1.0f);
        ShapeRenderer.setGlobalAlpha(contentRenderAlpha);
        drawEspItemsPopup(graphics, previewPopupX, popupY, previewPopupW);
        if (dashboardSurface.hasClick()
                && !dashboardSurface.clickConsumed()
                && !dashboardSurface.insideAnyPopup(dashboardSurface.clickX(), dashboardSurface.clickY())
                && contains(dashboardSurface.clickX(), dashboardSurface.clickY(), previewPopupX, popupY, previewPopupW, popupH)) {
            dashboardSurface.consumeClick();
        }
        ShapeRenderer.setGlobalAlpha(dashboardAnimationAlpha);
        contentRenderAlpha = previousContentAlpha;
    }

    private void handlePreviewPopupClickOutside(boolean modalOreWindow) {
        if (modalOreWindow
                || selectedTab != SidebarTab.VISUAL
                || !previewEspPopupOpen
                || !dashboardSurface.hasClick()
                || dashboardSurface.clickConsumed()
                || dashboardSurface.insideAnyPopup(dashboardSurface.clickX(), dashboardSurface.clickY())
                || previewPopupBlocksPoint(dashboardSurface.clickX(), dashboardSurface.clickY())) {
            return;
        }
        previewEspPopupOpen = false;
        dashboardSurface.consumeClick();
    }

    private boolean previewPopupBlocksPoint(double x, double y) {
        if (selectedTab != SidebarTab.VISUAL || previewEspPopupAlpha <= 0.02f || previewPopupW <= 0) {
            return false;
        }
        int popupH = espPopupHeight(previewPopupW);
        int popupY = Math.max(previewPopupY, dashboardY + 58);
        return contains(x, y, previewPopupX, popupY, previewPopupW, popupH);
    }

    private boolean hudContains(double mouseX, double mouseY) {
        return (VisualSettings.hudWidgetEnabled(VisualSettings.HUD_WIDGET_WATERMARK)
                && WatermarkHudRenderer.containsVisible(mouseX, mouseY))
                || HotkeyHudRenderer.contains(mouseX, mouseY)
                || ArrayListHudRenderer.contains(mouseX, mouseY)
                || TargetsHudRenderer.contains(mouseX, mouseY)
                || BackpackHudRenderer.contains(mouseX, mouseY)
                || PotionBuffHudRenderer.contains(mouseX, mouseY)
                || SpeedHudRenderer.contains(mouseX, mouseY)
                || MusicLyricsHudRenderer.contains(mouseX, mouseY);
    }

    private void clearHudDragging() {
        watermarkHudDragging = false;
        hotkeyHudDragging = false;
        arrayListHudDragging = false;
        targetsHudDragging = false;
        backpackHudDragging = false;
        potionBuffHudDragging = false;
        speedHudDragging = false;
        musicLyricsHudDragging = false;
        pendingWatermarkElementDrag = 0;
        WatermarkHudRenderer.clearDraggedElement();
    }

    private void drawPreviewToolbar(GuiGraphics graphics, int x, int y) {
        int mainW = 28;
        int mainH = 28;
        previewToolbarItemsX = x;
        previewToolbarItemsY = y;
        previewToolbarItemsW = mainW;
        previewToolbarItemsH = mainH;
        float iconSize = 13.0f;
        float iconW = MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_LIST, iconSize);
        float iconX = x + (mainW - iconW) * 0.5f;
        float iconY = y + (mainH - iconSize) * 0.5f - 0.5f;
        boolean mainHovered = isHovering(x, y, mainW, mainH);
        if (dashboardSurface.button("preview-toolbar/items", x, y, mainW, mainH)) {
            previewEspPopupOpen = !previewEspPopupOpen;
            if (previewEspPopupOpen && !previewEspPopupRendered && previewEspPopupAlpha <= 0.0f) {
                previewEspPopupAlpha = 0.0f;
            }
        }
        float openAnim = previewItemAnimation("toolbar-items", "open", previewEspPopupOpen ? 1.0f : 0.0f);
        float hoverAnim = previewItemAnimation("toolbar-items", "hover", mainHovered ? 1.0f : 0.0f);
        int iconColor = lerpColor(lerpColor(PREVIEW_MUTED, PREVIEW_ACCENT, hoverAnim * 0.55f), PREVIEW_ACCENT, openAnim);
        if (openAnim > 0.01f) {
            ShapeRenderer.roundRect(graphics, x, y, mainW, mainH, 8,
                    alphaColor(PREVIEW_ACCENT_SOFT, openAnim));
        } else if (hoverAnim > 0.01f) {
            ShapeRenderer.roundRect(graphics, x, y, mainW, mainH, 8,
                    alphaColor(themeColor(0xFFEFF1F7, 0xFF222734), hoverAnim * 0.65f));
        }
        MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_LIST, iconX, iconY, iconSize,
                iconColor);
    }

    private void drawPreviewGlow(GuiGraphics graphics, int centerX, int y, int h) {
        int glowW = 150;
        ShapeRenderer.roundRect(graphics, centerX - glowW / 2, y + 22, glowW, Math.max(120, h - 18), 32,
                themeColor(0x55EFF1FB, 0x44181E2A));
    }

    private void drawPreviewEntity(GuiGraphics graphics, int centerX, int bottomY, int scale) {
        Minecraft minecraft = Minecraft.getInstance();
        LivingEntity player = minecraft.player;
        if (player == null) {
            drawPreviewFallbackEntity(graphics, centerX, bottomY, scale);
            return;
        }

        renderPreviewEntityFacingMouse(graphics, centerX, bottomY, scale, player);
        graphics.flush();
        RenderSystem.clearDepth(1.0D);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private void renderPreviewEntityFacingMouse(GuiGraphics graphics, int centerX, int bottomY, int scale,
                                                LivingEntity entity) {
        float yawAngle = previewModelYaw;

        Quaternionf modelRotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf cameraTilt = new Quaternionf();

        float bodyYawOld = entity.yBodyRotO;
        float bodyYaw = entity.yBodyRot;
        float yawOld = entity.yRotO;
        float yaw = entity.getYRot();
        float pitchOld = entity.xRotO;
        float pitch = entity.getXRot();
        float headYawOld = entity.yHeadRotO;
        float headYaw = entity.yHeadRot;

        float previewYaw = 180.0f - yawAngle * 40.0f;

        entity.yBodyRot = previewYaw;
        entity.yBodyRotO = previewYaw;
        entity.setYRot(previewYaw);
        entity.yRotO = previewYaw;
        entity.setXRot(0.0f);
        entity.xRotO = 0.0f;
        entity.yHeadRot = previewYaw;
        entity.yHeadRotO = previewYaw;

        try {
            ChamsRenderContext.beginPreviewRender();
            try {
                renderPreviewEntityPass(graphics, centerX, bottomY, scale, modelRotation, cameraTilt, entity,
                        graphics.bufferSource(), graphics::flush);
                renderPreviewEntityOutline(graphics, centerX, bottomY, scale, modelRotation, cameraTilt, entity);
            } finally {
                ChamsRenderContext.endPreviewRender();
            }
        } finally {
            entity.yBodyRotO = bodyYawOld;
            entity.yBodyRot = bodyYaw;
            entity.yRotO = yawOld;
            entity.setYRot(yaw);
            entity.xRotO = pitchOld;
            entity.setXRot(pitch);
            entity.yHeadRotO = headYawOld;
            entity.yHeadRot = headYaw;
        }
    }

    private void renderPreviewEntityOutline(GuiGraphics graphics, int centerX, int bottomY, int scale,
                                            Quaternionf modelRotation, Quaternionf cameraTilt, LivingEntity entity) {
        if (!VisualSettings.visualEspEnabled() || !VisualSettings.visualGlow()) {
            return;
        }

        int color = VisualSettings.visualGlowColor();
        if (((color >>> 24) & 0xFF) <= 0) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.levelRenderer instanceof LevelRendererAccessor accessor)) {
            return;
        }

        PostChain entityEffect = accessor.xerion$getEntityEffect();
        RenderTarget entityTarget = accessor.xerion$getEntityTarget();
        if (entityEffect == null || entityTarget == null) {
            return;
        }

        OutlineBufferSource outlineBuffer = minecraft.renderBuffers().outlineBufferSource();
        outlineBuffer.setColor((color >>> 16) & 0xFF, (color >>> 8) & 0xFF, color & 0xFF, 255);
        ModelGlowRenderer.setEntityOutlineOpacity(entityEffect,
                ModelGlowRenderer.mappedEntityOutlineOpacity((color >>> 24) & 0xFF));
        entityTarget.clear(Minecraft.ON_OSX);
        entityTarget.bindWrite(false);

        ChamsRenderContext.beginOutlineMaskRender();
        try {
            renderPreviewEntityPass(graphics, centerX, bottomY, scale, modelRotation, cameraTilt, entity,
                    outlineBuffer, outlineBuffer::endOutlineBatch);
            minecraft.renderBuffers().bufferSource().endBatch();
            entityEffect.process(0.0f);
        } finally {
            ChamsRenderContext.endOutlineMaskRender();
            ModelGlowRenderer.setEntityOutlineOpacity(entityEffect, 1.0f);
            minecraft.getMainRenderTarget().bindWrite(false);
        }
    }

    private void renderPreviewEntityPass(GuiGraphics graphics, int centerX, int bottomY, int scale,
                                         Quaternionf modelRotation, Quaternionf cameraTilt, LivingEntity entity,
                                         MultiBufferSource bufferSource, Runnable flushBuffers) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        EntityRenderDispatcher dispatcher = null;
        try {
            poseStack.translate(centerX, bottomY, 50.0D);
            poseStack.scale((float) scale, (float) scale, (float) -scale);
            poseStack.mulPose(modelRotation);
            RenderSystem.setShaderLights(
                    new Vector3f(0.0f, 0.45f, 1.0f).normalize(),
                    new Vector3f(0.0f, 0.15f, 1.0f).normalize());

            dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            Quaternionf cameraOrientation = new Quaternionf(cameraTilt).conjugate();
            dispatcher.overrideCameraOrientation(cameraOrientation);
            dispatcher.setRenderShadow(false);
            EntityRenderDispatcher renderDispatcher = dispatcher;
            RenderSystem.runAsFancy(() ->
                    renderDispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, poseStack, bufferSource,
                            LightTexture.FULL_BRIGHT));
            flushBuffers.run();
        } finally {
            if (dispatcher != null) {
                dispatcher.setRenderShadow(true);
            }
            poseStack.popPose();
            Lighting.setupFor3DItems();
        }
    }

    private void drawPreviewFallbackEntity(GuiGraphics graphics, int centerX, int bottomY, int scale) {
        int head = Math.max(22, Math.round(scale * 0.36f));
        int bodyW = Math.max(38, Math.round(scale * 0.58f));
        int bodyH = Math.max(76, Math.round(scale * 1.12f));
        ShapeRenderer.roundRect(graphics, centerX - head / 2, bottomY - bodyH - head - 8, head, head, head / 2, 0xFF343B50);
        ShapeRenderer.roundRect(graphics, centerX - bodyW / 2, bottomY - bodyH - 4, bodyW, bodyH, 12, 0xFF2B3245);
        ShapeRenderer.roundRectBorder(graphics, centerX - bodyW / 2, bottomY - bodyH - 4, bodyW, bodyH, 12, 1, 0xFF4B536A);
        MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_PERSON_RIFLE,
                centerX - MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_PERSON_RIFLE, 54.0f) * 0.5f,
                bottomY - bodyH + 4, 54.0f, themeColor(0xFFB6BDCD, SUBTLE));
    }

    private void drawPreviewEspOverlay(GuiGraphics graphics, int boxX, int boxY, int boxW, int boxH) {
        PreviewBounds nextBoxBounds = new PreviewBounds(boxX, boxY, boxW, boxH);
        syncPreviewAnimationsToBox(nextBoxBounds);
        previewEspBoxBounds = nextBoxBounds;
        previewComponentBounds.clear();
        previewFlagBounds.clear();
        previewMainItemBounds.clear();
        if (!previewEspEnabled()) {
            return;
        }
        int clipPadX = 120;
        int clipPadTop = 60;
        int clipPadBottom = 60;
        enableContentScissor(boxX - clipPadX, boxY - clipPadTop,
                boxW + clipPadX * 2, boxH + clipPadTop + clipPadBottom);
        try {
            drawPreviewEspOverlayClipped(graphics, boxX, boxY, boxW, boxH);
        } finally {
            restoreContentScissor();
        }
    }

    private void drawPreviewEspOverlayClipped(GuiGraphics graphics, int boxX, int boxY, int boxW, int boxH) {
        PreviewEspLayout previewLayout = new PreviewEspLayout(boxX, boxY, boxW, boxH);
        int boxColor = withAlpha(VisualSettings.mobBoxColor(), 0xEE);
        if (previewMobBoxEnabled()) {
            drawPreviewBox(graphics, boxX, boxY, boxW, boxH, boxColor, true);
            openPreviewSettingsOnRightClick("preview-settings/mob-box", boxX, boxY, boxW, boxH,
                    this::populateMobBoxSettings);
        }

        if (previewMobHealthEnabled()) {
            PreviewBounds bounds = animatedPreviewComponentBounds(PreviewSettings.MOB_HEALTH,
                    previewLayout.place(VisualSettings.mobHealthAnchor(),
                            previewHealthSize(boxW, boxH, true, VisualSettings.mobHealthAnchor())));
            previewComponentBounds.put(PreviewSettings.MOB_HEALTH, bounds);
            drawPreviewHover(graphics, bounds.x(), bounds.y(), bounds.w(), bounds.h());
            drawPreviewHealthBar(graphics, bounds, VisualSettings.mobHealthAnchor(), 1.0f);
            if (VisualSettings.mobHealthNumber()) {
                drawPreviewBarNumber(graphics, bounds, VisualSettings.mobHealthAnchor(), 0.72f, "14");
            }
            if (previewDragTarget == PreviewDragTarget.HEALTH) {
                PreviewSize shape = previewHealthSize(boxW, boxH, true, previewDragAnchor);
                PreviewBounds ghost = animatedPreviewDragBounds(previewMouseBounds(shape.w(), shape.h()));
                previewHealthGhostHorizontal = lerp(previewHealthGhostHorizontal,
                        isHorizontalAnchor(previewDragAnchor) ? 1.0f : 0.0f, UiAnimationTimings.PREVIEW_GHOST);
                drawPreviewBarGhost(graphics, ghost, previewHealthGhostHorizontal, 0.72f, GREEN, 0.58f);
            }
            openPreviewSettingsOnRightClick("preview-settings/mob-health", bounds.x(), bounds.y(), bounds.w(), bounds.h(),
                    this::populateMobHealthSettings);
        }

        drawPreviewMainItems(graphics, previewLayout, boxX, boxY, boxW, boxH);
        drawPreviewTextColumns(graphics, previewLayout);
        drawPreviewEquipmentItems(graphics, previewLayout);
    }

    private void drawPreviewMainItems(GuiGraphics graphics, PreviewEspLayout previewLayout,
                                      int boxX, int boxY, int boxW, int boxH) {
        if (previewEspItemEnabled(VisualSettings.ESP_ITEM_HURT_TIME)) {
            int color = VisualSettings.visualEspItemColor(VisualSettings.ESP_ITEM_HURT_TIME);
            drawRectBorder(graphics, boxX - 2, boxY - 2, boxW + 4, boxH + 4, alphaColor(color, 0.75f));
        }
        if (previewEspItemEnabled(VisualSettings.ESP_ITEM_ARMOR_BAR)) {
            VisualSettings.EspAnchor anchor = VisualSettings.visualEspItemAnchor(VisualSettings.ESP_ITEM_ARMOR_BAR);
            PreviewBounds bounds = animatedPreviewMainItemBounds(VisualSettings.ESP_ITEM_ARMOR_BAR,
                    previewLayout.place(anchor, previewMainBarSize(boxW, boxH, anchor)));
            previewMainItemBounds.put(VisualSettings.ESP_ITEM_ARMOR_BAR, bounds);
            drawPreviewHover(graphics, bounds.x(), bounds.y(), bounds.w(), bounds.h());
            drawPreviewStatBar(graphics, bounds, 0.78f, previewArmorColor());
            if (VisualSettings.visualEspItemNumber(VisualSettings.ESP_ITEM_ARMOR_BAR)) {
                drawPreviewBarNumber(graphics, bounds, anchor, 0.78f, "16");
            }
            if (previewDragTarget == PreviewDragTarget.MAIN_ITEM
                    && previewDragMainItem == VisualSettings.ESP_ITEM_ARMOR_BAR) {
                drawPreviewMainBarGhost(graphics, boxW, boxH, 0.78f, previewArmorColor());
            }
            openPreviewSettingsOnRightClick("preview-settings/armor-bar", bounds.x(), bounds.y(), bounds.w(), bounds.h(),
                    this::populateArmorBarItemSettings);
        }
        if (previewEspItemEnabled(VisualSettings.ESP_ITEM_ABSORPTION)) {
            VisualSettings.EspAnchor anchor = VisualSettings.visualEspItemAnchor(VisualSettings.ESP_ITEM_ABSORPTION);
            PreviewBounds bounds = animatedPreviewMainItemBounds(VisualSettings.ESP_ITEM_ABSORPTION,
                    previewLayout.place(anchor, previewMainBarSize(boxW, boxH, anchor)));
            previewMainItemBounds.put(VisualSettings.ESP_ITEM_ABSORPTION, bounds);
            drawPreviewHover(graphics, bounds.x(), bounds.y(), bounds.w(), bounds.h());
            drawPreviewStatBar(graphics, bounds, 0.45f, previewAbsorptionColor());
            if (VisualSettings.visualEspItemNumber(VisualSettings.ESP_ITEM_ABSORPTION)) {
                drawPreviewBarNumber(graphics, bounds, anchor, 0.45f, "8");
            }
            if (previewDragTarget == PreviewDragTarget.MAIN_ITEM
                    && previewDragMainItem == VisualSettings.ESP_ITEM_ABSORPTION) {
                drawPreviewMainBarGhost(graphics, boxW, boxH, 0.45f, previewAbsorptionColor());
            }
            openPreviewSettingsOnRightClick("preview-settings/absorption", bounds.x(), bounds.y(), bounds.w(), bounds.h(),
                    this::populateAbsorptionItemSettings);
        }
    }

    private void drawPreviewMainBarGhost(GuiGraphics graphics, int boxW, int boxH, float ratio, int color) {
        PreviewSize shape = previewMainBarSize(boxW, boxH, previewDragAnchor);
        PreviewBounds ghost = animatedPreviewDragBounds(previewMouseBounds(shape.w(), shape.h()));
        previewHealthGhostHorizontal = lerp(previewHealthGhostHorizontal,
                isHorizontalAnchor(previewDragAnchor) ? 1.0f : 0.0f, UiAnimationTimings.PREVIEW_GHOST);
        drawPreviewBarGhost(graphics, ghost, previewHealthGhostHorizontal, ratio, color, 0.58f);
    }

    private void drawPreviewTextColumns(GuiGraphics graphics, PreviewEspLayout previewLayout) {
        float flagSize = 10.0f;
        float labelSize = 12.0f * VisualSettings.mobNameScale();
        float distanceSize = 12.0f * VisualSettings.mobDistanceScale();
        for (VisualSettings.EspAnchor anchor : VisualSettings.EspAnchor.values()) {
            List<PreviewColumnItem> items = new ArrayList<>();
            if (previewMobNameEnabled() && VisualSettings.mobNameAnchor() == anchor) {
                String label = previewEntityLabel();
                items.add(PreviewColumnItem.text(new PreviewTextItem(PreviewSettings.MOB_NAME, 0L, label,
                        VisualSettings.mobNameColor(), labelSize)));
            }
            if (previewMobDistanceEnabled() && VisualSettings.mobDistanceAnchor() == anchor) {
                items.add(PreviewColumnItem.text(new PreviewTextItem(PreviewSettings.MOB_DISTANCE, 0L, "[18m]",
                        VisualSettings.mobDistanceColor(), distanceSize)));
            }
            for (EspItem item : previewFlagItems()) {
                if (!item.active() || VisualSettings.visualEspFlagAnchor(item.item()) != anchor) {
                    continue;
                }
                String label = VisualSettings.visualEspFlagTextShort(item.item()) ? shortFlagPreview(item.item()) : item.label();
                items.add(PreviewColumnItem.text(new PreviewTextItem(null, item.item(), label,
                        VisualSettings.visualEspItemColor(item.item()), flagSize)));
            }
            for (EspItem item : previewEquipmentItems()) {
                if (!item.active() || VisualSettings.visualEspItemAnchor(item.item()) != anchor) {
                    continue;
                }
                items.add(PreviewColumnItem.equipment(item));
            }
            if (items.isEmpty()) {
                continue;
            }
            int columnW = 0;
            int columnH = 0;
            PreviewColumnItem previous = null;
            for (PreviewColumnItem item : items) {
                PreviewSize itemSize = previewColumnItemSize(item);
                columnW = Math.max(columnW, itemSize.w());
                columnH += itemSize.h() + (previous == null ? 0 : previewColumnGap(previous, item));
                previous = item;
            }
            PreviewBounds column = previewLayout.place(anchor, new PreviewSize(columnW, columnH));
            int y = column.y();
            previous = null;
            for (PreviewColumnItem columnItem : items) {
                if (previous != null) {
                    y += previewColumnGap(previous, columnItem);
                }
                PreviewSize itemSize = previewColumnItemSize(columnItem);
                int itemW = itemSize.w();
                int itemH = itemSize.h();
                int x = anchor == VisualSettings.EspAnchor.LEFT
                        ? column.x() + column.w() - itemW
                        : column.x();
                if (anchor == VisualSettings.EspAnchor.TOP || anchor == VisualSettings.EspAnchor.BOTTOM) {
                    x = Math.round(column.x() + column.w() * 0.5f - itemW * 0.5f);
                }
                PreviewBounds target = new PreviewBounds(x, y, itemW, itemH);
                if (columnItem.equipment() != null) {
                    EspItem item = columnItem.equipment();
                    PreviewBounds bounds = animatedPreviewMainItemBounds(item.item(), target);
                    previewMainItemBounds.put(item.item(), bounds);
                    drawPreviewHover(graphics, bounds.x(), bounds.y(), bounds.w(), bounds.h());
                    if (previewEquipmentIsIcon(item.item())) {
                        drawPreviewEquipmentIcons(graphics, item.item(), bounds, 1.0f);
                    } else {
                        drawPreviewLabel(graphics, previewEquipmentLabel(item.item()), bounds, 9.0f,
                                VisualSettings.visualEspItemColor(item.item()), 1.0f);
                    }
                    if (previewDragTarget == PreviewDragTarget.MAIN_ITEM && previewDragMainItem == item.item()) {
                        PreviewBounds ghost = animatedPreviewDragBounds(previewMouseBounds(bounds.w(), bounds.h()));
                        if (previewEquipmentIsIcon(item.item())) {
                            ShapeRenderer.roundRect(graphics, ghost.x() - 2, ghost.y() - 2, ghost.w() + 4, ghost.h() + 4, 7,
                                    alphaColor(ACCENT_SOFT, 0.45f));
                            ShapeRenderer.roundRectBorder(graphics, ghost.x() - 2, ghost.y() - 2, ghost.w() + 4, ghost.h() + 4, 7, 1,
                                    alphaColor(ACCENT, 0.58f));
                            drawPreviewEquipmentIcons(graphics, item.item(), ghost, 0.58f);
                        } else {
                            drawPreviewLabelGhost(graphics, previewEquipmentLabel(item.item()), ghost, 9.0f,
                                    VisualSettings.visualEspItemColor(item.item()), 0.58f);
                        }
                    }
                    openPreviewSettingsOnRightClick("preview-settings/equipment/" + item.item(),
                            bounds.x(), bounds.y(), bounds.w(), bounds.h(), itemSettingsContent(item));
                } else {
                    PreviewTextItem item = columnItem.text();
                    float textW = MSDFRenderer.width(item.label(), item.size());
                    PreviewBounds bounds = item.setting() == null
                            ? animatedPreviewFlagBounds(item.flagItem(), target)
                            : animatedPreviewComponentBounds(item.setting(), target);
                    if (item.setting() == null) {
                        previewFlagBounds.put(item.flagItem(), bounds);
                    } else {
                        previewComponentBounds.put(item.setting(), bounds);
                    }
                    drawPreviewHover(graphics, bounds.x(), bounds.y(), bounds.w(), bounds.h());
                    if (item.setting() == PreviewSettings.MOB_NAME || item.setting() == PreviewSettings.MOB_DISTANCE) {
                        drawPreviewName(graphics, item.label(), textW, item.size(), bounds, item.color(), 1.0f);
                    } else {
                        drawPreviewLabel(graphics, item.label(), bounds, item.size(), item.color(), 1.0f);
                    }
                    if (item.setting() == PreviewSettings.MOB_NAME && previewDragTarget == PreviewDragTarget.NAME) {
                        PreviewBounds ghost = animatedPreviewDragBounds(previewMouseBounds(bounds.w(), bounds.h()));
                        drawPreviewNameGhost(graphics, item.label(), textW, item.size(), item.color(), ghost, 0.58f);
                    } else if (item.setting() == PreviewSettings.MOB_DISTANCE && previewDragTarget == PreviewDragTarget.DISTANCE) {
                        PreviewBounds ghost = animatedPreviewDragBounds(previewMouseBounds(bounds.w(), bounds.h()));
                        drawPreviewNameGhost(graphics, item.label(), textW, item.size(), item.color(), ghost, 0.58f);
                    } else if (previewDragTarget == PreviewDragTarget.FLAG && previewDragFlagItem == item.flagItem()) {
                        PreviewBounds ghost = animatedPreviewDragBounds(previewMouseBounds(bounds.w(), bounds.h()));
                        drawPreviewLabelGhost(graphics, item.label(), ghost, item.size(), item.color(), 0.58f);
                    }
                    if (item.setting() == PreviewSettings.MOB_NAME) {
                        openPreviewSettingsOnRightClick("preview-settings/mob-name", bounds.x(), bounds.y(),
                                bounds.w(), bounds.h(), this::populateMobNameSettings);
                    } else if (item.setting() == PreviewSettings.MOB_DISTANCE) {
                        openPreviewSettingsOnRightClick("preview-settings/mob-distance", bounds.x(), bounds.y(),
                                bounds.w(), bounds.h(), this::populateMobDistanceSettings);
                    } else {
                        EspItem source = previewItemForFlag(item.flagItem());
                        if (source != null) {
                            openPreviewSettingsOnRightClick("preview-settings/flag/" + item.flagItem(),
                                    bounds.x(), bounds.y(), bounds.w(), bounds.h(), itemSettingsContent(source));
                        }
                    }
                }
                y += itemH;
                previous = columnItem;
            }
        }
    }

    private void drawPreviewEquipmentItems(GuiGraphics graphics, PreviewEspLayout previewLayout) {
        // Equipment is laid out together with text/flags so each anchor uses one shared column.
    }

    private int previewEquipmentGap(long previous, long current) {
        PreviewSize previousSize = previewEquipmentSize(previous, 9.0f);
        PreviewSize currentSize = previewEquipmentSize(current, 9.0f);
        int smaller = Math.min(previousSize.h(), currentSize.h());
        return Math.max(0, Math.min(1, Math.round(smaller * 0.08f)));
    }

    private PreviewSize previewColumnItemSize(PreviewColumnItem item) {
        if (item.equipment() != null) {
            return previewEquipmentSize(item.equipment().item(), 9.0f);
        }
        PreviewTextItem text = item.text();
        return new PreviewSize(Math.round(MSDFRenderer.width(text.label(), text.size())) + 6,
                Math.max(Math.round(text.size() + 2.0f), Math.round(text.size() * 1.15f)));
    }

    private int previewColumnGap(PreviewColumnItem previous, PreviewColumnItem current) {
        if (previous.equipment() != null && current.equipment() != null) {
            return previewEquipmentGap(previous.equipment().item(), current.equipment().item());
        }
        PreviewSize previousSize = previewColumnItemSize(previous);
        PreviewSize currentSize = previewColumnItemSize(current);
        int smaller = Math.min(previousSize.h(), currentSize.h());
        return Math.max(0, Math.min(1, Math.round(smaller * 0.08f)));
    }

    private EspItem previewItemForFlag(long flagItem) {
        for (EspItem item : previewFlagItems()) {
            if (item.item() == flagItem) {
                return item;
            }
        }
        return null;
    }

    private PreviewSize previewMainBarSize(int boxW, int boxH, VisualSettings.EspAnchor anchor) {
        if (isHorizontalAnchor(anchor)) {
            return new PreviewSize(Math.max(24, boxW), 6);
        }
        return new PreviewSize(6, Math.max(24, boxH));
    }

    private PreviewBounds animatedPreviewMainItemBounds(long item, PreviewBounds target) {
        AnimatedPreviewBounds animation = previewMainItemAnimations.computeIfAbsent(item,
                unused -> new AnimatedPreviewBounds(target));
        animation.update(target, UiAnimationTimings.PREVIEW_BOUNDS);
        return animation.bounds();
    }

    private PreviewBounds animatedPreviewFlagBounds(long item, PreviewBounds target) {
        AnimatedPreviewBounds animation = previewFlagAnimations.computeIfAbsent(item,
                unused -> new AnimatedPreviewBounds(target));
        animation.update(target, UiAnimationTimings.PREVIEW_BOUNDS);
        return animation.bounds();
    }

    private boolean previewEspEnabled() {
        return switch (previewModelMode) {
            case ENEMIES -> VisualSettings.visualEspEnabled();
            case TEAM -> VisualSettings.visualTeamEnabled();
            case LOCAL -> VisualSettings.visualLocalEnabled();
        };
    }

    private boolean previewMobBoxEnabled() {
        return switch (previewModelMode) {
            case ENEMIES -> VisualSettings.mobBoxEnabled();
            case TEAM -> VisualSettings.visualTeamMobBox();
            case LOCAL -> VisualSettings.visualLocalMobBox();
        };
    }

    private boolean previewMobHealthEnabled() {
        return switch (previewModelMode) {
            case ENEMIES -> VisualSettings.mobHealthEnabled();
            case TEAM -> VisualSettings.visualTeamMobHealth();
            case LOCAL -> VisualSettings.visualLocalMobHealth();
        };
    }

    private boolean previewMobNameEnabled() {
        return switch (previewModelMode) {
            case ENEMIES -> VisualSettings.mobNameEnabled();
            case TEAM -> VisualSettings.visualTeamMobName();
            case LOCAL -> VisualSettings.visualLocalMobName();
        };
    }

    private boolean previewMobDistanceEnabled() {
        return switch (previewModelMode) {
            case ENEMIES -> VisualSettings.mobNameDistance();
            case TEAM -> VisualSettings.visualTeamMobDistance();
            case LOCAL -> VisualSettings.visualLocalMobDistance();
        };
    }

    private boolean previewEspItemEnabled(long item) {
        return switch (previewModelMode) {
            case ENEMIES -> VisualSettings.visualEnemyEspItemEnabled(item);
            case TEAM -> VisualSettings.visualTeamEspItemEnabled(item);
            case LOCAL -> VisualSettings.visualLocalEspItemEnabled(item);
        };
    }

    private EspItem[] previewEquipmentItems() {
        return EspItemCatalog.equipmentItems(previewModelMode);
    }

    private boolean previewEquipmentIsIcon(long item) {
        return item == VisualSettings.ESP_ITEM_HELD_ITEM_ICON
                || item == VisualSettings.ESP_ITEM_ARMOR_ICONS
                || item == VisualSettings.ESP_ITEM_TOTEM
                || item == VisualSettings.ESP_ITEM_SHIELD
                || item == VisualSettings.ESP_ITEM_BOW_CROSSBOW;
    }

    private PreviewSize previewEquipmentSize(long item, float textSize) {
        if (item == VisualSettings.ESP_ITEM_HELD_ITEM_ICON
                || item == VisualSettings.ESP_ITEM_TOTEM
                || item == VisualSettings.ESP_ITEM_SHIELD
                || item == VisualSettings.ESP_ITEM_BOW_CROSSBOW) {
            return new PreviewSize(16, 16);
        }
        if (item == VisualSettings.ESP_ITEM_ARMOR_ICONS) {
            return new PreviewSize(64, 16);
        }
        String label = previewEquipmentLabel(item);
        return new PreviewSize(Math.round(MSDFRenderer.width(label, textSize)) + 6,
                Math.max(Math.round(textSize + 2.0f), Math.round(textSize * 1.15f)));
    }

    private void drawPreviewEquipmentIcons(GuiGraphics graphics, long item, PreviewBounds bounds, float alpha) {
        if (item == VisualSettings.ESP_ITEM_HELD_ITEM_ICON) {
            drawFadingItem(graphics, new ItemStack(Items.DIAMOND_SWORD), bounds.x() + 1, bounds.y() + 1, 0.9f, alpha);
            return;
        }
        if (item == VisualSettings.ESP_ITEM_TOTEM) {
            drawFadingItem(graphics, new ItemStack(Items.TOTEM_OF_UNDYING), bounds.x() + 1, bounds.y() + 1, 0.9f, alpha);
            return;
        }
        if (item == VisualSettings.ESP_ITEM_SHIELD) {
            drawFadingItem(graphics, new ItemStack(Items.SHIELD), bounds.x() + 1, bounds.y() + 1, 0.9f, alpha);
            return;
        }
        if (item == VisualSettings.ESP_ITEM_BOW_CROSSBOW) {
            drawFadingItem(graphics, new ItemStack(Items.BOW), bounds.x() + 1, bounds.y() + 1, 0.9f, alpha);
            return;
        }
        if (item == VisualSettings.ESP_ITEM_ARMOR_ICONS) {
            ItemStack[] armor = {
                    new ItemStack(Items.DIAMOND_BOOTS),
                    new ItemStack(Items.DIAMOND_LEGGINGS),
                    new ItemStack(Items.DIAMOND_CHESTPLATE),
                    new ItemStack(Items.DIAMOND_HELMET)
            };
            int x = bounds.x();
            for (ItemStack stack : armor) {
                drawFadingItem(graphics, stack, x + 1, bounds.y() + 1, 0.9f, alpha);
                x += 16;
            }
        }
    }

    private String previewEquipmentLabel(long item) {
        if (item == VisualSettings.ESP_ITEM_HELD_ITEM_TEXT) return "Diamond Sword";
        if (item == VisualSettings.ESP_ITEM_HELD_ITEM_ICON) return "Item Icon";
        if (item == VisualSettings.ESP_ITEM_ARMOR_TEXT) return "Armor 4/4";
        if (item == VisualSettings.ESP_ITEM_ARMOR_ICONS) return "Armor Icons";
        if (item == VisualSettings.ESP_ITEM_DURABILITY) return "Dur 82%";
        if (item == VisualSettings.ESP_ITEM_ENCHANTS) return "Enchants";
        if (item == VisualSettings.ESP_ITEM_TOTEM) return "Totem";
        if (item == VisualSettings.ESP_ITEM_SHIELD) return "Shield";
        if (item == VisualSettings.ESP_ITEM_BOW_CROSSBOW) return "Bow";
        return "Equipment";
    }

    private EspItem[] previewFlagItems() {
        return EspItemCatalog.flagItems(previewModelMode);
    }

    private String shortFlagPreview(long item) {
        if (item == VisualSettings.ESP_ITEM_SNEAKING) return "SNK";
        if (item == VisualSettings.ESP_ITEM_SPRINTING) return "SPR";
        if (item == VisualSettings.ESP_ITEM_INVISIBLE) return "INV";
        if (item == VisualSettings.ESP_ITEM_BURNING) return "FIRE";
        if (item == VisualSettings.ESP_ITEM_BLOCKING) return "BLK";
        if (item == VisualSettings.ESP_ITEM_USING_ITEM) return "USE";
        if (item == VisualSettings.ESP_ITEM_EATING) return "EAT";
        if (item == VisualSettings.ESP_ITEM_DRINKING) return "DRK";
        if (item == VisualSettings.ESP_ITEM_BOW_CHARGING) return "BOW";
        if (item == VisualSettings.ESP_ITEM_CROSSBOW_CHARGED) return "X-BOW";
        if (item == VisualSettings.ESP_ITEM_ELYTRA) return "ELY";
        if (item == VisualSettings.ESP_ITEM_SWIMMING) return "SWM";
        if (item == VisualSettings.ESP_ITEM_GLOWING) return "GLOW";
        if (item == VisualSettings.ESP_ITEM_POTION) return "POT";
        if (item == VisualSettings.ESP_ITEM_LOW_HEALTH) return "LOW";
        if (item == VisualSettings.ESP_ITEM_BOT) return "BOT";
        if (item == VisualSettings.ESP_ITEM_FRIEND_TEAM) return "TEAM";
        if (item == VisualSettings.ESP_ITEM_PING) return "PING";
        if (item == VisualSettings.ESP_ITEM_GAMEMODE) return "GM";
        return "FLAG";
    }

    private String previewEntityLabel() {
        return switch (previewModelMode) {
            case ENEMIES -> "Enemy";
            case TEAM -> "Team";
            case LOCAL -> "Local";
        };
    }

    private void drawPreviewHover(GuiGraphics graphics, int x, int y, int w, int h) {
        if (previewEspPopupOpen || previewEspPopupAlpha > 0.02f || !isHovering(x, y, w, h)) {
            return;
        }
        ShapeRenderer.roundRect(graphics, x, y, w, h, 6, 0x66E9EAFF);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 6, 1, 0x996E72FF);
    }

    private void drawPreviewBox(GuiGraphics graphics, int x, int y, int w, int h, int color, boolean style2D) {
        if (!style2D) {
            drawPreview3DBox(graphics, x, y, w, h, color);
            return;
        }
        drawRectBorder(graphics, x, y, w, h, 0xFF000000);
        drawRectBorder(graphics, x + 1, y + 1, w - 2, h - 2, color);
        drawRectBorder(graphics, x + 2, y + 2, w - 4, h - 4, color);
        drawRectBorder(graphics, x + 3, y + 3, w - 6, h - 6, 0xFF000000);
    }

    private void drawPreview3DBox(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        drawPreview3DBoxLayer(graphics, x + 2, y + 2, w, h, 0x66000000);
        drawPreview3DBoxLayer(graphics, x - 1, y - 1, w + 2, h + 2, 0xF2FFFFFF);
        drawPreview3DBoxLayer(graphics, x, y, w, h, color);
    }

    private void drawPreview3DBoxLayer(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        int depthX = clamp(Math.round(w * 0.11f), 4, 8);
        int depthY = clamp(Math.round(h * 0.055f), 4, 7);

        int frontLeft = x;
        int frontRight = x + w;
        int frontTop = y + depthY;
        int frontBottom = y + h;

        int backLeft = x + depthX;
        int backRight = x + w + depthX;
        int backTop = y;
        int backBottom = y + h - depthY;

        drawRectBorder(graphics, frontLeft, frontTop, w, h - depthY, color);

        drawLine(graphics, frontLeft, frontTop, backLeft, backTop, color);
        drawLine(graphics, frontRight, frontTop, backRight, backTop, color);
        drawLine(graphics, frontRight, frontBottom, backRight, backBottom, color);

        drawLine(graphics, backLeft, backTop, backRight, backTop, color);
        drawLine(graphics, backRight, backTop, backRight, backBottom, color);
        drawLine(graphics, backLeft, backTop, backLeft, backTop + Math.max(8, h / 6), color);
    }

    private void drawPreviewName(GuiGraphics graphics, String label, float labelW, float labelSize,
                                 PreviewBounds bounds, int color, float alpha) {
        int labelX = Math.round(bounds.x() + (bounds.w() - labelW) * 0.5f);
        int labelY = Math.round(bounds.y() + (bounds.h() - labelSize) * 0.5f - 4.0f);
        MSDFRenderer.draw(graphics, label, labelX + 1, labelY + 1, labelSize,
                alphaColor(0xB0000000, alpha * contentRenderAlpha));
        MSDFRenderer.draw(graphics, label, labelX, labelY, labelSize,
                alphaColor(color, alpha * contentRenderAlpha));
    }

    private void drawPreviewNameGhost(GuiGraphics graphics, String label, float labelW, float labelSize,
                                      int color, PreviewBounds bounds, float alpha) {
        ShapeRenderer.roundRect(graphics, bounds.x() - 2, bounds.y() - 2, bounds.w() + 4, bounds.h() + 4, 7,
                alphaColor(ACCENT_SOFT, alpha * 0.78f));
        ShapeRenderer.roundRectBorder(graphics, bounds.x() - 2, bounds.y() - 2, bounds.w() + 4, bounds.h() + 4, 7, 1,
                alphaColor(ACCENT, alpha));
        drawPreviewName(graphics, label, labelW, labelSize, bounds, color, alpha);
    }

    private void drawPreviewLabel(GuiGraphics graphics, String label, PreviewBounds bounds, float size, int color, float alpha) {
        float labelW = MSDFRenderer.width(label, size);
        int labelX = Math.round(bounds.x() + (bounds.w() - labelW) * 0.5f);
        int labelY = Math.round(bounds.y() + (bounds.h() - size) * 0.5f - 3.0f);
        MSDFRenderer.draw(graphics, label, labelX + 1, labelY + 1, size,
                alphaColor(0xB0000000, alpha * contentRenderAlpha));
        MSDFRenderer.draw(graphics, label, labelX, labelY, size,
                alphaColor(color, alpha * contentRenderAlpha));
    }

    private void drawPreviewLabelGhost(GuiGraphics graphics, String label, PreviewBounds bounds,
                                       float size, int color, float alpha) {
        ShapeRenderer.roundRect(graphics, bounds.x() - 2, bounds.y() - 2, bounds.w() + 4, bounds.h() + 4, 7,
                alphaColor(ACCENT_SOFT, alpha * 0.78f));
        ShapeRenderer.roundRectBorder(graphics, bounds.x() - 2, bounds.y() - 2, bounds.w() + 4, bounds.h() + 4, 7, 1,
                alphaColor(ACCENT, alpha));
        drawPreviewLabel(graphics, label, bounds, size, color, alpha);
    }

    private void drawPreviewStatBar(GuiGraphics graphics, PreviewBounds bounds, float ratio, int color) {
        int x = bounds.x();
        int y = bounds.y();
        int w = bounds.w();
        int h = bounds.h();
        float clamped = Math.max(0.0f, Math.min(1.0f, ratio));
        drawRectBorder(graphics, x, y, w, h, 0xFF000000);
        graphics.fill(x + 1, y + 1, x + w, y + h, 0x78000000);
        if (h > w) {
            int fillH = Math.round(Math.max(0, h - 2) * clamped);
            graphics.fill(x + 1, y + h - 1 - fillH, x + w - 1, y + h - 1, color);
        } else {
            int fillW = Math.round(Math.max(0, w - 2) * clamped);
            graphics.fill(x + 1, y + 1, x + 1 + fillW, y + h - 1, color);
        }
    }

    private void drawPreviewBarNumber(GuiGraphics graphics, PreviewBounds bounds,
                                      VisualSettings.EspAnchor anchor, float ratio, String value) {
        float size = 7.0f;
        float textW = MSDFRenderer.width(value, size);
        float clamped = Math.max(0.0f, Math.min(1.0f, ratio));
        int x;
        int y;
        if (anchor == VisualSettings.EspAnchor.TOP || anchor == VisualSettings.EspAnchor.BOTTOM) {
            float progressX = bounds.x() + bounds.w() * clamped;
            x = Math.round(clampFloat(progressX - textW * 0.5f,
                    bounds.x() - textW * 0.5f,
                    bounds.x() + bounds.w() - textW * 0.5f));
            y = Math.round(bounds.y() + bounds.h() * 0.5f - size * 0.5f - 2.0f);
        } else {
            float progressY = bounds.y() + bounds.h() * (1.0f - clamped);
            x = Math.round(bounds.x() + bounds.w() * 0.5f - textW * 0.5f);
            y = Math.round(clampFloat(progressY - size * 0.5f - 2.0f,
                    bounds.y() - size * 0.5f - 2.0f,
                    bounds.y() + bounds.h() - size * 0.5f - 2.0f));
        }
        MSDFRenderer.draw(graphics, value, x + 1, y + 1, size, alphaColor(0xB0000000, contentRenderAlpha));
        MSDFRenderer.draw(graphics, value, x, y, size, alphaColor(0xF2FFFFFF, contentRenderAlpha));
    }

    private int previewArmorColor() {
        if (!VisualSettings.visualEspItemDynamicColor(VisualSettings.ESP_ITEM_ARMOR_BAR)) {
            return VisualSettings.visualEspItemColor(VisualSettings.ESP_ITEM_ARMOR_BAR);
        }
        return 0xF2559CFF;
    }

    private int previewAbsorptionColor() {
        if (!VisualSettings.visualEspItemDynamicColor(VisualSettings.ESP_ITEM_ABSORPTION)) {
            return VisualSettings.visualEspItemColor(VisualSettings.ESP_ITEM_ABSORPTION);
        }
        return 0xF2FFD64D;
    }

    private void drawPreviewHealthBar(GuiGraphics graphics, PreviewBounds bounds,
                                      VisualSettings.EspAnchor anchor, float alpha) {
        drawPreviewStatBar(graphics, bounds, 0.72f, alphaColor(GREEN, alpha));
    }

    private void drawPreviewBarGhost(GuiGraphics graphics, PreviewBounds bounds, float horizontal,
                                     float ratio, int color, float alpha) {
        ShapeRenderer.roundRect(graphics, bounds.x() - 2, bounds.y() - 2, bounds.w() + 4, bounds.h() + 4, 7,
                alphaColor(ACCENT_SOFT, alpha * 0.75f));
        ShapeRenderer.roundRectBorder(graphics, bounds.x() - 2, bounds.y() - 2, bounds.w() + 4, bounds.h() + 4, 7, 1,
                alphaColor(ACCENT, alpha));

        float verticalAlpha = alpha * (1.0f - smoothstep(horizontal));
        float horizontalAlpha = alpha * smoothstep(horizontal);
        if (verticalAlpha > 0.02f) {
            int barW = 6;
            int x = Math.round(bounds.x() + bounds.w() * 0.5f - barW * 0.5f);
            int y = bounds.y();
            int h = Math.max(8, bounds.h());
            int fillH = Math.round((h - 2) * ratio);
            int fillY = y + h - 1 - fillH;
            drawRectBorder(graphics, x, y, barW, h, alphaColor(0xFF000000, verticalAlpha));
            graphics.fill(x + 1, y + 1, x + barW, y + h, alphaColor(0x78000000, verticalAlpha));
            graphics.fill(x + 1, fillY, x + barW, y + h, alphaColor(color, verticalAlpha));
        }
        if (horizontalAlpha > 0.02f) {
            int x = bounds.x();
            int y = Math.round(bounds.y() + bounds.h() * 0.5f - 3.0f);
            int w = Math.max(8, bounds.w());
            int fillW = Math.round((w - 2) * ratio);
            drawRectBorder(graphics, x, y, w, 6, alphaColor(0xFF000000, horizontalAlpha));
            graphics.fill(x + 1, y + 1, x + w, y + 6, alphaColor(0x78000000, horizontalAlpha));
            graphics.fill(x + 1, y + 1, x + 1 + fillW, y + 6, alphaColor(color, horizontalAlpha));
        }
    }

    private PreviewSize previewHealthSize(int boxW, int boxH, boolean style2D, VisualSettings.EspAnchor anchor) {
        if (isHorizontalAnchor(anchor)) {
            return new PreviewSize(boxW, 6);
        }
        int h = style2D ? boxH : boxH - Math.max(6, Math.round(boxH * 0.12f));
        return new PreviewSize(6, h);
    }

    private PreviewBounds animatedPreviewComponentBounds(PreviewSettings component, PreviewBounds target) {
        AnimatedPreviewBounds animation = previewComponentAnimations.computeIfAbsent(component,
                unused -> new AnimatedPreviewBounds(target));
        animation.update(target, UiAnimationTimings.PREVIEW_BOUNDS);
        return animation.bounds();
    }

    private void syncPreviewAnimationsToBox(PreviewBounds nextBoxBounds) {
        if (previewEspBoxBounds == null
                || previewComponentAnimations.isEmpty() && previewMainItemAnimations.isEmpty() && previewFlagAnimations.isEmpty()) {
            return;
        }
        int deltaX = nextBoxBounds.x() - previewEspBoxBounds.x();
        int deltaY = nextBoxBounds.y() - previewEspBoxBounds.y();
        if (deltaX == 0 && deltaY == 0) {
            return;
        }
        translatePreviewAnimations(deltaX, deltaY);
    }

    private PreviewBounds animatedPreviewDragBounds(PreviewBounds target) {
        return previewDragGhost.update(target, UiAnimationTimings.PREVIEW_GHOST);
    }

    private void openPreviewSettingsOnRightClick(String id, int triggerX, int triggerY, int triggerW, int triggerH,
                                                 WidgetSurface.PopupContent content) {
        if ((previewEspPopupOpen || previewEspPopupAlpha > 0.02f) && !id.startsWith("preview-item-settings/")) {
            return;
        }
        int popupX = triggerX + triggerW + 8;
        int popupY = triggerY;
        dashboardSurface.settingsPopup(id, triggerX, triggerY, triggerW, triggerH, popupX, popupY, content);
    }

    private WidgetSurface.PopupContent settingsContent(PreviewSettings settings) {
        return switch (settings) {
            case MOB_BOX -> this::populateMobBoxSettings;
            case MOB_HEALTH -> this::populateMobHealthSettings;
            case MOB_NAME -> this::populateMobNameSettings;
            case MOB_DISTANCE -> this::populateMobDistanceSettings;
            case ORE_GLOW -> this::populateOreGlowSettings;
            case OFFSCREEN_ARROW -> this::populateOffscreenArrowSettings;
            case PLAYER_CHAMS -> this::populatePlayerChamsSettings;
            case BEHIND_WALLS_CHAMS -> this::populateBehindWallsChamsSettings;
            case ON_HIT_CHAMS -> this::populateOnHitChamsSettings;
            case RAGDOLLS_CHAMS -> this::populateRagdollsChamsSettings;
            case SOUL_PARTICLES -> this::populateSoulParticlesSettings;
            case TEAM_PLAYER_CHAMS -> this::populateTeamPlayerChamsSettings;
            case TEAM_BEHIND_WALLS_CHAMS -> this::populateTeamBehindWallsChamsSettings;
            case LOCAL_PLAYER_CHAMS -> this::populateLocalPlayerChamsSettings;
            case LOCAL_TRANSPARENCY -> this::populateLocalTransparencySettings;
            case LOCAL_ITEM -> this::populateLocalItemSettings;
            case LOCAL_ARMS -> this::populateLocalArmsSettings;
            case NONE -> null;
            case SOUNDS, GLOW, TEAM_GLOW, LOCAL_GLOW -> null;
        };
    }

    private void drawEspItemsPopup(GuiGraphics graphics, int x, int y, int w) {
        int h = espPopupHeight(w);
        drawPopupBackdrop(graphics, x, y, w, h, 11);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 11, 1, BORDER);
        text(graphics, previewModelMode.label() + " Items", x + 12, y + 12, 11.0f, PREVIEW_TEXT);

        int sectionY = y + 34;
        EspItemGroup[] groups = previewItemGroups();
        for (int i = 0; i < groups.length; i++) {
            sectionY = drawEspSection(graphics, x + 8, sectionY, w - 16, groups[i].title(), groups[i].items());
            sectionY += i + 1 < groups.length ? ESP_ITEMS_GAP : 0;
        }
    }

    private void drawPopupBackdrop(GuiGraphics graphics, int x, int y, int w, int h, int radius) {
        renderScaledPanelBackdrop(graphics, x, y, w, h, radius, contentRenderAlpha);
        ShadowRenderer.shadow(graphics, x, y, w, h, radius, ShadowStyle.popup(), contentRenderAlpha);
        ShapeRenderer.roundRect(graphics, x, y, w, h, radius, themeColor(0xB8FFFFFF, SURFACE));
    }

    private int drawEspSection(GuiGraphics graphics, int x, int y, int w, String title, EspItem[] items) {
        int h = espSectionHeight(w, items);
        ShapeRenderer.roundRect(graphics, x, y, w, h, 8, themeColor(0xFFF8F9FD, PREVIEW_BG));
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 8, 1, PREVIEW_BORDER);
        text(graphics, title, x + 10, y + 8, 11.0f, PREVIEW_MUTED);

        int cursorX = x + 10;
        int cursorY = y + 26;
        int maxX = x + w - 10;
        for (EspItem item : items) {
            int itemW = Math.round(MSDFRenderer.width(item.label(), ESP_ITEMS_BUTTON_TEXT_SIZE)) + ESP_ITEMS_BUTTON_PAD_X;
            if (cursorX + itemW > maxX) {
                cursorX = x + 10;
                cursorY += ESP_ITEMS_BUTTON_H + ESP_ITEMS_GAP;
            }
            drawEspItemButton(graphics, item, cursorX, cursorY, itemW, ESP_ITEMS_BUTTON_H);
            cursorX += itemW + ESP_ITEMS_GAP;
        }
        return y + h;
    }

    private void drawEspItemButton(GuiGraphics graphics, EspItem item, int x, int y, int w, int h) {
        boolean active = item.active();
        boolean hovered = isHovering(x, y, w, h);
        if (dashboardSurface.button("preview-esp-item/" + item.label(), x, y, w, h)) {
            item.toggle();
            active = item.active();
        }
        WidgetSurface.PopupContent settings = itemSettingsContent(item);
        if (settings != null) {
            openPreviewSettingsOnRightClick("preview-item-settings/" + item.label(), x, y, w, h, settings);
        }
        float activeAnim = previewItemAnimation(item.label(), "active", active ? 1.0f : 0.0f);
        float hoverAnim = previewItemAnimation(item.label(), "hover", hovered ? 1.0f : 0.0f);
        int inactiveFill = lerpColor(themeColor(0xFFF4F5FA, FIELD), themeColor(0xFFFFFFFF, SURFACE), hoverAnim);
        int fill = lerpColor(inactiveFill, ACCENT_SOFT, activeAnim);
        int border = lerpColor(BORDER, ACCENT, activeAnim);
        int textColor = lerpColor(PREVIEW_TEXT, PREVIEW_ACCENT, activeAnim);
        ShapeRenderer.roundRect(graphics, x, y, w, h, 5, fill);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 5, 1, border);
        text(graphics, item.label(), x + 9, y + 6, ESP_ITEMS_BUTTON_TEXT_SIZE, textColor);
    }

    private float previewItemAnimation(String itemId, String channel, float target) {
        String key = itemId + "/" + channel;
        float current = previewItemAnimations.getOrDefault(key, target);
        float next = lerp(current, target, UiAnimationTimings.PREVIEW_ITEM);
        if (Math.abs(next - target) < 0.01f) {
            next = target;
        }
        previewItemAnimations.put(key, next);
        return smoothstep(next);
    }

    private WidgetSurface.PopupContent itemSettingsContent(EspItem item) {
        if (item.item() != 0L) {
            if (item.item() == VisualSettings.ESP_ITEM_ARMOR_BAR) {
                return this::populateArmorBarItemSettings;
            }
            if (item.item() == VisualSettings.ESP_ITEM_ABSORPTION) {
                return this::populateAbsorptionItemSettings;
            }
            if (item.item() == VisualSettings.ESP_ITEM_HURT_TIME) {
                return this::populateHurtTimeItemSettings;
            }
            return gui -> populateEspItemSettings(gui, item);
        }
        return settingsContent(item.settings());
    }

    private int espPopupHeight(int w) {
        int h = 34 + 8;
        EspItemGroup[] groups = previewItemGroups();
        for (int i = 0; i < groups.length; i++) {
            h += espSectionHeight(w - 16, groups[i].items());
            if (i + 1 < groups.length) {
                h += ESP_ITEMS_GAP;
            }
        }
        return h;
    }

    private EspItemGroup[] previewItemGroups() {
        return EspItemCatalog.groups(previewModelMode);
    }

    private int espSectionHeight(int w, EspItem[] items) {
        int cursorX = 10;
        int rows = 1;
        int maxW = Math.max(1, w - 20);
        for (EspItem item : items) {
            int itemW = Math.round(MSDFRenderer.width(item.label(), ESP_ITEMS_BUTTON_TEXT_SIZE)) + ESP_ITEMS_BUTTON_PAD_X;
            if (cursorX + itemW > maxW) {
                rows++;
                cursorX = 10;
            }
            cursorX += itemW + ESP_ITEMS_GAP;
        }
        return 36 + rows * ESP_ITEMS_BUTTON_H + Math.max(0, rows - 1) * ESP_ITEMS_GAP;
    }

    private void populateMobBoxSettings(WidgetSurface.Frame gui) {
        VisualSettings.setMobBoxColor(gui.colorPicker("Color", VisualSettings.mobBoxColor(), VisualSettings::setMobBoxColor));
    }

    private void populateMobHealthSettings(WidgetSurface.Frame gui) {
        String colorMode = gui.combo("Color Mode", VisualSettings.mobHealthColorMode(), "Dynamic, Custom", VisualSettings::setMobHealthColorMode);
        VisualSettings.setMobHealthColorMode(colorMode);
        if ("Custom".equals(colorMode)) {
            VisualSettings.setMobHealthColor(gui.colorPicker("Color", VisualSettings.mobHealthColor(), VisualSettings::setMobHealthColor));
        }
        VisualSettings.setMobHealthNumber(gui.controlledCheckbox("Number", VisualSettings.mobHealthNumber(), VisualSettings::setMobHealthNumber));
    }

    private void populateMobNameSettings(WidgetSurface.Frame gui) {
        float scale = gui.slider("Scale", VisualSettings.mobNameScaleSlider(), Math.round(VisualSettings.mobNameScale() * 100.0f) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 60.0f, 160.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 60.0f, 160.0f),
                VisualSettings::setMobNameScaleSlider);
        VisualSettings.setMobNameScaleSlider(scale);
        VisualSettings.setMobNameColor(gui.colorPicker("Color", VisualSettings.mobNameColor(), VisualSettings::setMobNameColor));
    }

    private void populateMobDistanceSettings(WidgetSurface.Frame gui) {
        float scale = gui.slider("Scale", VisualSettings.mobDistanceScaleSlider(), Math.round(VisualSettings.mobDistanceScale() * 100.0f) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 60.0f, 160.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 60.0f, 160.0f),
                VisualSettings::setMobDistanceScaleSlider);
        VisualSettings.setMobDistanceScaleSlider(scale);
        VisualSettings.setMobDistanceColor(gui.colorPicker("Color", VisualSettings.mobDistanceColor(), VisualSettings::setMobDistanceColor));
    }

    private void populateArmorBarItemSettings(WidgetSurface.Frame gui) {
        String colorMode = gui.combo("Color Mode", VisualSettings.visualEspItemColorMode(VisualSettings.ESP_ITEM_ARMOR_BAR),
                "Dynamic, Custom",
                value -> VisualSettings.setVisualEspItemColorMode(VisualSettings.ESP_ITEM_ARMOR_BAR, value));
        VisualSettings.setVisualEspItemColorMode(VisualSettings.ESP_ITEM_ARMOR_BAR, colorMode);
        if ("Custom".equals(colorMode)) {
            VisualSettings.setVisualEspItemColor(VisualSettings.ESP_ITEM_ARMOR_BAR, gui.colorPicker("Color",
                    VisualSettings.visualEspItemColor(VisualSettings.ESP_ITEM_ARMOR_BAR),
                    color -> VisualSettings.setVisualEspItemColor(VisualSettings.ESP_ITEM_ARMOR_BAR, color)));
        }
        VisualSettings.setVisualEspItemNumber(VisualSettings.ESP_ITEM_ARMOR_BAR,
                gui.controlledCheckbox("Number", VisualSettings.visualEspItemNumber(VisualSettings.ESP_ITEM_ARMOR_BAR),
                        value -> VisualSettings.setVisualEspItemNumber(VisualSettings.ESP_ITEM_ARMOR_BAR, value)));
    }

    private void populateAbsorptionItemSettings(WidgetSurface.Frame gui) {
        String colorMode = gui.combo("Color Mode", VisualSettings.visualEspItemColorMode(VisualSettings.ESP_ITEM_ABSORPTION),
                "Dynamic, Custom",
                value -> VisualSettings.setVisualEspItemColorMode(VisualSettings.ESP_ITEM_ABSORPTION, value));
        VisualSettings.setVisualEspItemColorMode(VisualSettings.ESP_ITEM_ABSORPTION, colorMode);
        if ("Custom".equals(colorMode)) {
            VisualSettings.setVisualEspItemColor(VisualSettings.ESP_ITEM_ABSORPTION, gui.colorPicker("Color",
                    VisualSettings.visualEspItemColor(VisualSettings.ESP_ITEM_ABSORPTION),
                    color -> VisualSettings.setVisualEspItemColor(VisualSettings.ESP_ITEM_ABSORPTION, color)));
        }
        VisualSettings.setVisualEspItemNumber(VisualSettings.ESP_ITEM_ABSORPTION,
                gui.controlledCheckbox("Number", VisualSettings.visualEspItemNumber(VisualSettings.ESP_ITEM_ABSORPTION),
                        value -> VisualSettings.setVisualEspItemNumber(VisualSettings.ESP_ITEM_ABSORPTION, value)));
    }

    private void populateHurtTimeItemSettings(WidgetSurface.Frame gui) {
        float duration = gui.slider("Display Time", VisualSettings.visualHurtTimeDisplayTicksSlider(),
                Math.round(VisualSettings.visualHurtTimeDisplayTicks()) + "t",
                value -> SliderValueFormat.intRangeLabel(value, 2.0f, 20.0f, "t"),
                value -> SliderValueFormat.parseRangeSlider(value, 2.0f, 20.0f),
                VisualSettings::setVisualHurtTimeDisplayTicksSlider);
        VisualSettings.setVisualHurtTimeDisplayTicksSlider(duration);
        VisualSettings.setVisualEspItemColor(VisualSettings.ESP_ITEM_HURT_TIME, gui.colorPicker("Color",
                VisualSettings.visualEspItemColor(VisualSettings.ESP_ITEM_HURT_TIME),
                color -> VisualSettings.setVisualEspItemColor(VisualSettings.ESP_ITEM_HURT_TIME, color)));
    }

    private void populateOreGlowSettings(WidgetSurface.Frame gui) {
        if (gui.button("Mineral List", gui.contentWidth(), 24)) {
            openSelectionWindow(SelectionWindowMode.ORE);
        }
        float range = gui.slider("Range", VisualSettings.oreGlowRangeSlider(), Math.round(VisualSettings.oreGlowRange()) + "m",
                value -> SliderValueFormat.intRangeLabel(value, 16.0f, 64.0f, "m"),
                value -> SliderValueFormat.parseRangeSlider(value, 16.0f, 64.0f),
                VisualSettings::setOreGlowRangeSlider);
        VisualSettings.setOreGlowRangeSlider(range);
        float opacity = gui.slider("Opacity", VisualSettings.oreGlowOpacitySlider(), Math.round(VisualSettings.oreGlowOpacity() * 100.0f) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 12.0f, 55.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 12.0f, 55.0f),
                VisualSettings::setOreGlowOpacitySlider);
        VisualSettings.setOreGlowOpacitySlider(opacity);
        VisualSettings.setOreGlowOutline(gui.controlledCheckbox("Outline", VisualSettings.oreGlowOutline(), VisualSettings::setOreGlowOutline));
        VisualSettings.setOreGlowThroughWalls(gui.controlledCheckbox("Through Walls", VisualSettings.oreGlowThroughWalls(), VisualSettings::setOreGlowThroughWalls));
        VisualSettings.setOreGlowNames(gui.controlledCheckbox("Names", VisualSettings.oreGlowNames(), VisualSettings::setOreGlowNames));
    }

    private void populateChestGlowSettings(WidgetSurface.Frame gui) {
        VisualSettings.setChestGlowColor(gui.colorPicker("Color", VisualSettings.chestGlowColor(), VisualSettings::setChestGlowColor));
        float range = gui.slider("Range", VisualSettings.chestGlowRangeSlider(), Math.round(VisualSettings.chestGlowRange()) + "m",
                value -> SliderValueFormat.intRangeLabel(value, 16.0f, 64.0f, "m"),
                value -> SliderValueFormat.parseRangeSlider(value, 16.0f, 64.0f),
                VisualSettings::setChestGlowRangeSlider);
        VisualSettings.setChestGlowRangeSlider(range);
        float opacity = gui.slider("Opacity", VisualSettings.chestGlowOpacitySlider(),
                Math.round(VisualSettings.chestGlowOpacity() * 100.0f) + "%",
                value -> SliderValueFormat.intRangeLabel(value, 10.0f, 50.0f, "%"),
                value -> SliderValueFormat.parseRangeSlider(value, 10.0f, 50.0f),
                VisualSettings::setChestGlowOpacitySlider);
        VisualSettings.setChestGlowOpacitySlider(opacity);
        VisualSettings.setChestGlowOutline(gui.controlledCheckbox("Outline", VisualSettings.chestGlowOutline(), VisualSettings::setChestGlowOutline));
        VisualSettings.setChestGlowThroughWalls(gui.controlledCheckbox("Through Walls", VisualSettings.chestGlowThroughWalls(), VisualSettings::setChestGlowThroughWalls));
        VisualSettings.setChestGlowStorageBlocks(gui.controlledCheckbox("Storage Blocks", VisualSettings.chestGlowStorageBlocks(), VisualSettings::setChestGlowStorageBlocks));
    }

    private void populateEspItemSettings(WidgetSurface.Frame gui, EspItem item) {
        if (VisualSettings.espItemIsFlag(item.item())) {
            String mode = gui.combo("Text", VisualSettings.visualEspFlagTextMode(item.item()),
                    "Full, Short",
                    value -> VisualSettings.setVisualEspFlagTextMode(item.item(), value));
            VisualSettings.setVisualEspFlagTextMode(item.item(), mode);
        }
        VisualSettings.setVisualEspItemColor(item.item(), gui.colorPicker("Color",
                VisualSettings.visualEspItemColor(item.item()),
                color -> VisualSettings.setVisualEspItemColor(item.item(), color)));
    }

    private void openSelectionWindow(SelectionWindowMode mode) {
        boolean sameMode = selectionWindowMode == mode;
        boolean sameWindow = oreWindowOpen && sameMode;
        selectionWindowMode = mode;
        selectionWindowData.invalidate(mode);
        oreWindowOpen = !sameWindow;
        selectionSearchFocused = false;
        if (!sameMode) {
            oreWindowScrollY = 0;
            oreWindowAnimatedScrollY = 0.0f;
            selectionSearchText = "";
        }
        suppressOreWindowClickThisFrame = true;
        clickPending = false;
        dashboardSurface.consumeClick();
    }

    private void drawOreListWindow(GuiGraphics graphics) {
        drawSelectionWindow(graphics);
    }


    private void openFakePlayerWindow() {
        boolean opening = !fakePlayerWindowOpen;
        fakePlayerWindowOpen = opening;
        if (opening) {
            fakePlayerWindowScrollY = clamp(fakePlayerWindowScrollY, 0, fakePlayerWindowMaxScrollY);
            fakePlayerWindowAnimatedScrollY = fakePlayerWindowScrollY;
        }
        suppressFakePlayerWindowClickThisFrame = true;
        clickPending = false;
        dashboardSurface.consumeClick();
    }

    private void drawFakePlayerListWindow(GuiGraphics graphics) {
        boolean allowWindowClick = !suppressFakePlayerWindowClickThisFrame;
        fakePlayerWindowAlpha = lerp(fakePlayerWindowAlpha, fakePlayerWindowOpen ? 1.0f : 0.0f, UiAnimationTimings.POPUP_OPEN);
        if (!fakePlayerWindowOpen && fakePlayerWindowAlpha < 0.01f) {
            fakePlayerWindowAlpha = 0.0f;
            suppressFakePlayerWindowClickThisFrame = false;
            return;
        }
        clampFakePlayerWindowPosition();
        float previousAlpha = contentRenderAlpha;
        float alpha = dashboardAnimationAlpha * smoothstep(fakePlayerWindowAlpha);
        contentRenderAlpha = alpha;
        ShapeRenderer.setGlobalAlpha(alpha);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        try {

        renderScaledPanelBackdrop(graphics, fakePlayerWindowX + dashboardRenderOffsetX,
                fakePlayerWindowY + dashboardRenderOffsetY, FAKE_PLAYER_WINDOW_W, FAKE_PLAYER_WINDOW_H, 12.0f, alpha);
        ShadowRenderer.shadow(graphics, fakePlayerWindowX, fakePlayerWindowY,
                FAKE_PLAYER_WINDOW_W, FAKE_PLAYER_WINDOW_H, 12, ShadowStyle.popup(), alpha);
        ShapeRenderer.roundRect(graphics, fakePlayerWindowX, fakePlayerWindowY,
                FAKE_PLAYER_WINDOW_W, FAKE_PLAYER_WINDOW_H, 12,
                UiPalette.dark() ? 0xDD171B24 : 0xEAF8FAFF);
        ShapeRenderer.roundRectBorder(graphics, fakePlayerWindowX, fakePlayerWindowY,
                FAKE_PLAYER_WINDOW_W, FAKE_PLAYER_WINDOW_H, 12, 1, BORDER);

        String title = "Fake Players";
        int titleW = textWidth(title);
        text(graphics, title, fakePlayerWindowX + (FAKE_PLAYER_WINDOW_W - titleW) / 2,
                fakePlayerWindowY + 12, TEXT);

        int closeSize = MODAL_CLOSE_SIZE;
        int closeX = fakePlayerWindowCloseX();
        int closeY = fakePlayerWindowCloseY();
        drawModalCloseButton(graphics, "fake-player-list/close", closeX, closeY, closeSize, alpha);
        if (allowWindowClick && clickPending && clickButton == 0
                && contains(clickX, clickY, closeX, closeY, closeSize, closeSize)) {
            fakePlayerWindowOpen = false;
            fakePlayerWindowDragging = false;
            clickPending = false;
        }

        List<FakePlayerRegistry.DummySnapshot> snapshots = FakePlayerRegistry.snapshots();
        int contentX = fakePlayerWindowX + 14;
        int contentY = fakePlayerWindowY + FAKE_PLAYER_WINDOW_TITLE_H + 10;
        int contentW = FAKE_PLAYER_WINDOW_W - 28;
        int contentH = FAKE_PLAYER_WINDOW_H - FAKE_PLAYER_WINDOW_TITLE_H - 58;
        ShapeRenderer.roundRect(graphics, contentX, contentY, contentW, contentH, 9,
                themeColor(0x33FFFFFF, 0x33101622));
        ShapeRenderer.roundRectBorder(graphics, contentX, contentY, contentW, contentH, 9, 1,
                alphaColor(BORDER, alpha));

        int rowGap = 6;
        int listPadTop = 8;
        int listPadBottom = 8;
        int listH = listPadTop
                + (snapshots.isEmpty() ? 0 : snapshots.size() * FAKE_PLAYER_ROW_H + (snapshots.size() - 1) * rowGap)
                + listPadBottom;
        fakePlayerWindowMaxScrollY = Math.max(0, listH - contentH);
        fakePlayerWindowScrollY = clamp(fakePlayerWindowScrollY, 0, fakePlayerWindowMaxScrollY);
        fakePlayerWindowAnimatedScrollY = clampFloat(
                lerp(fakePlayerWindowAnimatedScrollY, fakePlayerWindowScrollY, UiAnimationTimings.CONTENT_SCROLL),
                0.0f,
                fakePlayerWindowMaxScrollY);
        List<FakePlayerRowEntry> animatedRows = fakePlayerRowAnimator.updateRows(snapshots,
                contentY + listPadTop - Math.round(fakePlayerWindowAnimatedScrollY),
                FAKE_PLAYER_ROW_H,
                rowGap,
                fakePlayerWindowDragging);

        enableContentScissor(contentX + 1, contentY + 1, contentW - 2, contentH - 2);
        try {
            if (snapshots.isEmpty() && animatedRows.isEmpty()) {
                String empty = "No Fake Players";
                int emptyW = textWidth(empty);
                text(graphics, empty, contentX + (contentW - emptyW) / 2, contentY + 24, MUTED);
            } else {
                for (FakePlayerRowEntry row : animatedRows) {
                    int rowY = Math.round(row.animation().y);
                    if (rowY + FAKE_PLAYER_ROW_H >= contentY && rowY <= contentY + contentH) {
                        drawFakePlayerRow(graphics, row.snapshot(), contentX + 8, rowY, contentW - 16,
                                FAKE_PLAYER_ROW_H, allowWindowClick && !row.animation().deleting,
                                alpha, row.animation().alpha);
                    }
                }
            }
        } finally {
            RenderSystem.disableScissor();
        }

        fakePlayerWindowScrollbarAlpha = ScrollbarControl.updateAlpha(fakePlayerWindowScrollbarAlpha,
                fakePlayerWindowMaxScrollY > 0);
        if (fakePlayerWindowScrollbarAlpha > 0.01f) {
            float progress = fakePlayerWindowMaxScrollY <= 0
                    ? 0.0f
                    : fakePlayerWindowAnimatedScrollY / (float) fakePlayerWindowMaxScrollY;
            ScrollbarControl.Layout scrollbarLayout = ScrollbarControl.layout(fakePlayerWindowX, fakePlayerWindowY,
                    FAKE_PLAYER_WINDOW_W, contentY, contentH, contentW, SCROLLBAR_RIGHT_INSET);
            ScrollbarControl.draw(graphics, scrollbarLayout.trackX(), scrollbarLayout.trackY(), scrollbarLayout.trackH(),
                    progress, contentH, listH, fakePlayerWindowScrollbarAlpha * alpha);
        }

        int bottomY = fakePlayerWindowY + FAKE_PLAYER_WINDOW_H - 38;
        if (drawFakePlayerWindowButton(graphics, "fake-player/place", "Place", contentX, bottomY, 90, 24,
                allowWindowClick, alpha)) {
            FakePlayerRegistry.place();
        }
        if (drawFakePlayerWindowButton(graphics, "fake-player/clear", "Clear All",
                contentX + contentW - 96, bottomY, 96, 24, allowWindowClick, alpha)) {
            FakePlayerRegistry.clear();
        }

        } finally {
        contentRenderAlpha = previousAlpha;
        ShapeRenderer.setGlobalAlpha(dashboardAnimationAlpha);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, dashboardAnimationAlpha);
        suppressFakePlayerWindowClickThisFrame = false;
        }
    }







    private void drawFakePlayerRow(GuiGraphics graphics, FakePlayerRegistry.DummySnapshot snapshot,
                                   int x, int y, int w, int h, boolean allowWindowClick, float alpha,
                                   float rowAlpha) {
        float previousAlpha = contentRenderAlpha;
        contentRenderAlpha *= rowAlpha;
        ShapeRenderer.setGlobalAlpha(contentRenderAlpha);
        boolean hovered = contains(renderMouseX, renderMouseY, x, y, w, h);
        float hover = previewItemAnimation("fake-player/row/" + snapshot.entityId(), "hover",
                hovered ? 1.0f : 0.0f);
        int fill = lerpColor(themeColor(0xFFFFFFFF, 0xFF202632),
                themeColor(0xFFF4F7FF, 0xFF2B3342), hover);
        ShapeRenderer.roundRect(graphics, x, y, w, h, 8, fill);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 8, 1,
                alphaColor(lerpColor(BORDER, ACCENT, hover), alpha));

        String name = snapshot.name();
        String meta = "#" + Math.abs(snapshot.entityId()) + "  "
                + String.format(java.util.Locale.ROOT, "%.1fm", snapshot.distance())
                + "  " + Math.round(snapshot.health()) + "/" + Math.round(snapshot.maxHealth()) + " HP";
        text(graphics, name, x + 10, y + 6, 10.0f, TEXT);
        text(graphics, meta, x + 10, y + 20, 8.0f, MUTED);

        int removeW = 62;
        if (drawFakePlayerWindowButton(graphics, "fake-player/remove/" + snapshot.entityId(), "Remove",
                x + w - removeW - 8, y + 5, removeW, 24, allowWindowClick, alpha)) {
            FakePlayerRegistry.remove(snapshot.entityId());
        }
        contentRenderAlpha = previousAlpha;
        ShapeRenderer.setGlobalAlpha(previousAlpha);
    }

    private boolean drawFakePlayerWindowButton(GuiGraphics graphics, String id, String label, int x, int y,
                                               int w, int h, boolean allowWindowClick, float alpha) {
        boolean hovered = contains(renderMouseX, renderMouseY, x, y, w, h);
        boolean active = hovered && mouseDown;
        float hover = previewItemAnimation(id, "hover", hovered ? 1.0f : 0.0f);
        float press = previewItemAnimation(id, "press", active ? 1.0f : 0.0f);
        int fill = lerpColor(themeColor(0xFFF7F8FC, FIELD), ACCENT_SOFT, hover);
        int border = lerpColor(BORDER, ACCENT, hover);
        ShapeRenderer.roundRect(graphics, x, y + Math.round(press), w, h, 8, fill);
        ShapeRenderer.roundRectBorder(graphics, x, y + Math.round(press), w, h, 8, 1,
                alphaColor(border, alpha));
        int labelW = Math.round(MSDFRenderer.width(label, 9.0f));
        MSDFRenderer.draw(graphics, label, x + (w - labelW) / 2, y + 8 + Math.round(press), 9.0f,
                alphaColor(TEXT, contentRenderAlpha));
        if (allowWindowClick && clickPending && clickButton == 0 && contains(clickX, clickY, x, y, w, h)) {
            fakePlayerWindowDragging = false;
            clickPending = false;
            return true;
        }
        return false;
    }

    private void drawSelectionWindow(GuiGraphics graphics) {
        boolean allowWindowClick = !suppressOreWindowClickThisFrame;
        oreWindowAlpha = lerp(oreWindowAlpha, oreWindowOpen ? 1.0f : 0.0f, UiAnimationTimings.POPUP_OPEN);
        if (!oreWindowOpen && oreWindowAlpha < 0.01f) {
            oreWindowAlpha = 0.0f;
            suppressOreWindowClickThisFrame = false;
            selectionSearchFocused = false;
            return;
        }
        clampOreWindowPosition();
        float previousAlpha = contentRenderAlpha;
        float alpha = dashboardAnimationAlpha * smoothstep(oreWindowAlpha);
        contentRenderAlpha = alpha;
        ShapeRenderer.setGlobalAlpha(alpha);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        try {

        renderScaledPanelBackdrop(graphics, oreWindowX + dashboardRenderOffsetX, oreWindowY + dashboardRenderOffsetY,
                ORE_WINDOW_W, ORE_WINDOW_H, 12.0f, alpha);
        ShadowRenderer.shadow(graphics, oreWindowX, oreWindowY, ORE_WINDOW_W, ORE_WINDOW_H, 12,
                ShadowStyle.popup(), alpha);
        ShapeRenderer.roundRect(graphics, oreWindowX, oreWindowY, ORE_WINDOW_W, ORE_WINDOW_H, 12,
                UiPalette.dark() ? 0xDD171B24 : 0xEAF8FAFF);
        ShapeRenderer.roundRectBorder(graphics, oreWindowX, oreWindowY, ORE_WINDOW_W, ORE_WINDOW_H, 12, 1, BORDER);

        String title = selectionWindowMode.title();
        int titleW = textWidth(title);
        text(graphics, title, oreWindowX + (ORE_WINDOW_W - titleW) / 2, oreWindowY + 12, TEXT);

        int closeSize = MODAL_CLOSE_SIZE;
        int closeX = selectionWindowCloseX();
        int closeY = selectionWindowCloseY();
        drawModalCloseButton(graphics, "ore-list/close", closeX, closeY, closeSize, alpha);
        if (allowWindowClick && clickPending && clickButton == 0
                && contains(clickX, clickY, closeX, closeY, closeSize, closeSize)) {
            oreWindowOpen = false;
            oreWindowDragging = false;
            selectionSearchFocused = false;
            clickPending = false;
        }

        int columns = 5;
        List<SelectionWindowEntry> entries = selectionWindowData.filteredEntries(selectionWindowMode, selectionSearchText);
        int gridW = columns * ORE_CARD_W + (columns - 1) * ORE_CARD_GAP;
        int startX = oreWindowX + (ORE_WINDOW_W - gridW) / 2;
        int contentX = oreWindowX + 12;
        selectionSearchX = contentX;
        selectionSearchY = oreWindowY + ORE_WINDOW_TITLE_H + 8;
        selectionSearchW = ORE_WINDOW_W - 24;
        selectionSearchH = ORE_WINDOW_SEARCH_H;
        drawSelectionSearchField(graphics, allowWindowClick, alpha);

        int contentY = selectionSearchY + selectionSearchH + 10;
        int contentW = ORE_WINDOW_W - 24;
        int contentH = ORE_WINDOW_H - (contentY - oreWindowY) - 14;
        int rows = entries.isEmpty() ? 0 : (entries.size() + columns - 1) / columns;
        int gridPadTop = 8;
        int gridPadBottom = 16;
        int gridH = gridPadTop + (rows == 0 ? 0 : rows * ORE_CARD_H + (rows - 1) * ORE_CARD_GAP) + gridPadBottom;
        oreWindowMaxScrollY = Math.max(0, gridH - contentH);
        oreWindowScrollY = clamp(oreWindowScrollY, 0, oreWindowMaxScrollY);
        oreWindowAnimatedScrollY = clampFloat(
                lerp(oreWindowAnimatedScrollY, oreWindowScrollY, UiAnimationTimings.CONTENT_SCROLL),
                0.0f,
                oreWindowMaxScrollY);
        int startY = contentY + gridPadTop - Math.round(oreWindowAnimatedScrollY);
        enableContentScissor(contentX, contentY, contentW, contentH);
        try {
            if (entries.isEmpty()) {
                String empty = selectionSearchText.isBlank() ? "No Entries" : "No Results";
                int emptyW = textWidth(empty);
                text(graphics, empty, contentX + (contentW - emptyW) / 2, contentY + 24, MUTED);
            }
            for (int i = 0; i < entries.size(); i++) {
                SelectionWindowEntry entry = entries.get(i);
                int col = i % columns;
                int row = i / columns;
                int cardX = startX + col * (ORE_CARD_W + ORE_CARD_GAP);
                int cardY = startY + row * (ORE_CARD_H + ORE_CARD_GAP);
                if (cardY + ORE_CARD_H < contentY || cardY > contentY + contentH) {
                    continue;
                }
                drawSelectionCard(graphics, entry, cardX, cardY, ORE_CARD_W, ORE_CARD_H,
                        contentX, contentY, contentW, contentH,
                        selectionWindowData.selected(selectionWindowMode, entry), i, alpha);
            }
        } finally {
            RenderSystem.disableScissor();
        }

        oreWindowScrollbarAlpha = ScrollbarControl.updateAlpha(oreWindowScrollbarAlpha, oreWindowMaxScrollY > 0);
        if (oreWindowScrollbarAlpha > 0.01f) {
            float progress = oreWindowMaxScrollY <= 0 ? 0.0f : oreWindowAnimatedScrollY / (float) oreWindowMaxScrollY;
            ScrollbarControl.Layout scrollbarLayout = ScrollbarControl.layout(oreWindowX, oreWindowY, ORE_WINDOW_W,
                    contentY, contentH, contentW, SCROLLBAR_RIGHT_INSET);
            ScrollbarControl.draw(graphics, scrollbarLayout.trackX(), scrollbarLayout.trackY(), scrollbarLayout.trackH(),
                    progress, contentH, gridH, oreWindowScrollbarAlpha * alpha);
        }

        } finally {
        contentRenderAlpha = previousAlpha;
        ShapeRenderer.setGlobalAlpha(dashboardAnimationAlpha);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, dashboardAnimationAlpha);
        suppressOreWindowClickThisFrame = false;
        }
    }

    private void drawSelectionSearchField(GuiGraphics graphics, boolean allowWindowClick, float alpha) {
        boolean hovered = contains(renderMouseX, renderMouseY, selectionSearchX, selectionSearchY,
                selectionSearchW, selectionSearchH);
        if (allowWindowClick && clickPending && clickButton == 0
                && contains(clickX, clickY, selectionSearchX, selectionSearchY, selectionSearchW, selectionSearchH)) {
            selectionSearchFocused = true;
            selectionSearchCursor = selectionSearchText.length();
            selectionSearchAnchor = selectionSearchCursor;
            oreWindowDragging = false;
            clickPending = false;
        } else if (allowWindowClick && clickPending && clickButton == 0
                && contains(clickX, clickY, oreWindowX, oreWindowY, ORE_WINDOW_W, ORE_WINDOW_H)
                && !contains(clickX, clickY, selectionSearchX, selectionSearchY, selectionSearchW, selectionSearchH)) {
            selectionSearchFocused = false;
        }

        float focus = previewItemAnimation("selection-search/" + selectionWindowMode.name(), "focus",
                selectionSearchFocused || hovered ? 1.0f : 0.0f);
        int border = lerpColor(BORDER, selectionSearchFocused ? ACCENT : themeColor(0xFFC9D0E4, BORDER), focus);
        ShapeRenderer.roundRect(graphics, selectionSearchX, selectionSearchY, selectionSearchW, selectionSearchH,
                8, themeColor(0xFFF7F8FC, FIELD));
        ShapeRenderer.roundRectBorder(graphics, selectionSearchX, selectionSearchY, selectionSearchW, selectionSearchH,
                8, 1, border);
        String value = selectionSearchText;
        int textColor = value.isBlank() && !selectionSearchFocused ? MUTED : TEXT;
        String shown = value.isBlank() && !selectionSearchFocused ? "Search..." : value;
        int textX = selectionSearchX + 10;
        int textY = selectionSearchY + 5;
        int textMaxW = selectionSearchW - 20;
        if (!selectionSearchFocused) {
            TextPainter.drawUnicodeClipped(graphics, shown, textX, textY, textMaxW,
                    10.0f, textColor, alpha * contentRenderAlpha);
            return;
        }

        normalizeSelectionSearchState();
        int visibleStart = 0;
        while (visibleStart < selectionSearchCursor
                && TextPainter.unicodeWidth(value.substring(visibleStart, selectionSearchCursor), 10.0f) > textMaxW - 2) {
            visibleStart = WidgetSurface.nextCodePointIndex(value, visibleStart);
        }
        int visibleEnd = visibleStart;
        while (visibleEnd < value.length()) {
            int next = WidgetSurface.nextCodePointIndex(value, visibleEnd);
            if (TextPainter.unicodeWidth(value.substring(visibleStart, next), 10.0f) > textMaxW - 2) {
                break;
            }
            visibleEnd = next;
        }
        int selectionStart = Math.max(visibleStart, Math.min(selectionSearchCursor, selectionSearchAnchor));
        int selectionEnd = Math.min(visibleEnd, Math.max(selectionSearchCursor, selectionSearchAnchor));
        if (selectionEnd > selectionStart) {
            int selectionX = textX + TextPainter.unicodeWidth(value.substring(visibleStart, selectionStart), 10.0f);
            int selectionW = Math.max(1, TextPainter.unicodeWidth(value.substring(selectionStart, selectionEnd), 10.0f));
            graphics.fill(selectionX, selectionSearchY + 5, selectionX + selectionW,
                    selectionSearchY + selectionSearchH - 5,
                    alphaColor(ACCENT, alpha * contentRenderAlpha * 0.28f));
        }
        TextPainter.drawUnicodeClipped(graphics, value.substring(visibleStart, visibleEnd),
                textX, textY, textMaxW, 10.0f, textColor, alpha * contentRenderAlpha);
        if (((System.currentTimeMillis() / 500L) & 1L) == 0L) {
            int cursorX = textX + TextPainter.unicodeWidth(value.substring(visibleStart, selectionSearchCursor), 10.0f);
            graphics.vLine(cursorX + 1, selectionSearchY + 6, selectionSearchY + selectionSearchH - 6,
                    alphaColor(ACCENT_DARK, alpha * contentRenderAlpha));
        }
    }

    private void normalizeSelectionSearchState() {
        selectionSearchCursor = WidgetSurface.normalizeTextIndex(selectionSearchText, selectionSearchCursor);
        selectionSearchAnchor = WidgetSurface.normalizeTextIndex(selectionSearchText, selectionSearchAnchor);
    }

    private boolean selectionSearchHasSelection() {
        return selectionSearchCursor != selectionSearchAnchor;
    }

    private int selectionSearchSelectionStart() {
        return Math.min(selectionSearchCursor, selectionSearchAnchor);
    }

    private int selectionSearchSelectionEnd() {
        return Math.max(selectionSearchCursor, selectionSearchAnchor);
    }

    private void moveSelectionSearchCursor(int cursor, boolean selecting) {
        selectionSearchCursor = WidgetSurface.normalizeTextIndex(selectionSearchText, cursor);
        if (!selecting) {
            selectionSearchAnchor = selectionSearchCursor;
        }
    }

    private void copySelectionSearchText() {
        if (selectionSearchHasSelection()) {
            Minecraft.getInstance().keyboardHandler.setClipboard(selectionSearchText.substring(
                    selectionSearchSelectionStart(), selectionSearchSelectionEnd()));
        }
    }

    private boolean deleteSelectionSearchSelection() {
        if (!selectionSearchHasSelection()) {
            return false;
        }
        deleteSelectionSearchRange(selectionSearchSelectionStart(), selectionSearchSelectionEnd());
        return true;
    }

    private void deleteSelectionSearchRange(int start, int end) {
        int safeStart = WidgetSurface.normalizeTextIndex(selectionSearchText, Math.min(start, end));
        int safeEnd = WidgetSurface.normalizeTextIndex(selectionSearchText, Math.max(start, end));
        selectionSearchText = selectionSearchText.substring(0, safeStart)
                + selectionSearchText.substring(safeEnd);
        moveSelectionSearchCursor(safeStart, false);
        oreWindowScrollY = 0;
    }

    private void replaceSelectionSearchText(String insertedText) {
        String clean = WidgetSurface.sanitizeTextInput(insertedText == null ? "" : insertedText);
        normalizeSelectionSearchState();
        int start = selectionSearchSelectionStart();
        int end = selectionSearchSelectionEnd();
        int retainedCodePoints = selectionSearchText.codePointCount(0, start)
                + selectionSearchText.codePointCount(end, selectionSearchText.length());
        clean = WidgetSurface.limitCodePoints(clean, Math.max(0, 64 - retainedCodePoints));
        if (clean.isEmpty()) {
            return;
        }
        selectionSearchText = selectionSearchText.substring(0, start)
                + clean + selectionSearchText.substring(end);
        moveSelectionSearchCursor(start + clean.length(), false);
        oreWindowScrollY = 0;
    }

    private void drawModalCloseButton(GuiGraphics graphics, String id, int x, int y, int size, float alpha) {
        boolean hovered = contains(renderMouseX, renderMouseY, x, y, size, size);
        boolean active = hovered && mouseDown;
        float hoverAnim = previewItemAnimation(id, "hover", hovered ? 1.0f : 0.0f);
        float activeAnim = previewItemAnimation(id, "active", active ? 1.0f : 0.0f);
        CloseButtonPainter.render(graphics, x, y, size, hoverAnim, activeAnim, alpha);
    }

    private void drawSelectionCard(GuiGraphics graphics, SelectionWindowEntry entry, int x, int y, int w, int h,
                                   int contentX, int contentY, int contentW, int contentH,
                                   boolean selected, int index, float windowAlpha) {
        boolean mouseInsideContent = contains(renderMouseX, renderMouseY, contentX, contentY, contentW, contentH);
        boolean hovered = mouseInsideContent && contains(renderMouseX, renderMouseY, x, y, w, h);
        String animKey = "selection-card/" + selectionWindowMode.name() + "/" + entry.id();
        float hover = previewItemAnimation(animKey, "hover", hovered ? 1.0f : 0.0f);
        float selectedAnim = oreCardSelectedAnimation(animKey, selected);
        int lift = Math.round(3.0f * hover);
        y -= lift;
        int normalFill = lerpColor(themeColor(0xFFFFFFFF, 0xFF252B37),
                themeColor(0xFFF4F7FF, 0xFF303746), hover);
        int selectedFill = lerpColor(themeColor(0xFFEAFBF1, 0xFF173626),
                themeColor(0xFFD8F7E5, 0xFF1E4931), hover);
        int fill = lerpColor(normalFill, selectedFill, selectedAnim);
        int normalBorder = lerpColor(BORDER, ACCENT, hover);
        int selectedBorder = lerpColor(themeColor(0xFF6FE6A3, 0xFF3EBE76), GREEN, hover);
        int border = lerpColor(normalBorder, selectedBorder, selectedAnim);
        if (hover > 0.01f) {
            ShadowRenderer.shadow(graphics, x, y + lift, w, h, 8, ShadowStyle.popup(), contentRenderAlpha * hover * 0.22f);
        }
        ShapeRenderer.roundRect(graphics, x, y, w, h, 8, fill);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 8, 1, border);

        int iconSize = 48;
        int iconX = x + (w - iconSize) / 2;
        int iconY = y + 12;
        drawFadingItem(graphics, entry.stack(), iconX, iconY, 3.0f, windowAlpha);
        ShapeRenderer.roundRectBorder(graphics, iconX - 1, iconY - 1, iconSize + 2, iconSize + 2, 7,
                1, alphaColor(border, windowAlpha * contentRenderAlpha));
        String name = entry.label();
        float textSize = 10.0f;
        int textColor = themeColor(0xFF000000, 0xFFFFFFFF);
        boolean textClipVisible = enableIntersectedContentScissor(
                contentX, contentY, contentW, contentH,
                x + 7, y + h - 24, w - 14, 18);
        try {
            if (textClipVisible) {
                drawScrollingCardText(graphics, animKey + "/label", name, x + 7, y + h - 22,
                        w - 14, textSize, alphaColor(textColor, contentRenderAlpha));
            }
        } finally {
            enableContentScissor(contentX, contentY, contentW, contentH);
        }

        boolean clickInsideContent = contains(clickX, clickY, contentX, contentY, contentW, contentH);
        if (!suppressOreWindowClickThisFrame && clickPending && clickButton == 0
                && clickInsideContent && contains(clickX, clickY, x, y + lift, w, h)) {
            oreWindowDragging = false;
            selectionWindowData.toggle(selectionWindowMode, entry, !selected);
            clickPending = false;
        }
    }

    private void drawScrollingCardText(GuiGraphics graphics, String id, String text, int x, int y,
                                       int maxW, float size, int color) {
        int textW = Math.round(MSDFRenderer.width(text, size));
        if (textW <= maxW) {
            MSDFRenderer.draw(graphics, text, x + Math.max(0, (maxW - textW) / 2), y, size, color);
            return;
        }

        int clipPad = 1;
        float overflow = textW - maxW;
        float pause = 0.28f;
        float cycleMs = Math.max(3200.0f, overflow * 150.0f);
        float phase = (System.currentTimeMillis() % (long) cycleMs) / cycleMs;
        float offset;
        float travel = (1.0f - pause * 2.0f) * 0.5f;
        if (phase < pause * 0.5f) {
            offset = 0.0f;
        } else if (phase < pause * 0.5f + travel) {
            float t = (phase - pause * 0.5f) / travel;
            offset = overflow * smoothstep(t);
        } else if (phase < pause * 1.5f + travel) {
            offset = overflow;
        } else {
            float t = (phase - pause * 1.5f - travel) / travel;
            offset = overflow * (1.0f - smoothstep(t));
        }
        MSDFRenderer.draw(graphics, text, x + clipPad - offset, y, size, color);
    }

    private void drawFadingItem(GuiGraphics graphics, ItemStack stack, int x, int y, float scale, float alpha) {
        if (stack.isEmpty() || alpha <= 0.01f) {
            return;
        }
        float effectiveAlpha = Math.max(0.0f, Math.min(alpha * contentRenderAlpha, 1.0f));
        FadingItemRenderer.draw(graphics, stack, x, y, scale, effectiveAlpha);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, contentRenderAlpha);
    }

    private float oreCardSelectedAnimation(String itemId, boolean selected) {
        String key = itemId + "/selected";
        float target = selected ? 1.0f : 0.0f;
        Boolean previousSelected = oreCardSelectedStates.put(itemId, selected);
        float current;
        if (previousSelected == null) {
            current = target;
        } else if (previousSelected != selected) {
            current = selected ? 0.0f : 1.0f;
        } else {
            current = previewItemAnimations.getOrDefault(key, target);
        }
        float next = lerp(current, target, 0.10f);
        if (Math.abs(next - target) < 0.006f) {
            next = target;
        }
        previewItemAnimations.put(key, next);
        return smoothstep(next);
    }

    private int drawMiscCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureMiscCard(x, stableY, w);
        h = drawCard(graphics, "Features", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateMisc(gui);
        return h;
    }

    private void populateMisc(WidgetSurface.Frame gui) {
        VisualSettings.setAntiPacketKickEnabled(gui.controlledCheckbox("Anti Packet Kick",
                VisualSettings.antiPacketKickEnabled(), VisualSettings::setAntiPacketKickEnabled));
        VisualSettings.setAutoReconnectEnabled(gui.controlledCheckboxWithSettings("Auto Reconnect",
                VisualSettings.autoReconnectEnabled(), this::populateAutoReconnectSettings,
                VisualSettings::setAutoReconnectEnabled));
        VisualSettings.setAntiStaffEnabled(gui.controlledCheckboxWithSettings("Anti-Staff",
                VisualSettings.antiStaffEnabled(), this::populateAntiStaffSettings,
                VisualSettings::setAntiStaffEnabled));
        VisualSettings.setAutoGGEnabled(gui.controlledCheckboxWithSettings("Auto Hypixel",
                VisualSettings.autoGGEnabled(), this::populateAutoHypixelSettings,
                VisualSettings::setAutoGGEnabled));
        VisualSettings.setQuickSwitchEnabled(gui.controlledCheckboxWithSettings("Quick Switch",
                VisualSettings.quickSwitchEnabled(), this::populateQuickSwitchSettings,
                VisualSettings::setQuickSwitchEnabled));
        VisualSettings.setFakePlayerEnabled(gui.controlledCheckboxWithSettings("Fake Player",
                VisualSettings.fakePlayerEnabled(), this::populateFakePlayerSettings, enabled -> {
                VisualSettings.setFakePlayerEnabled(enabled);
                if (!enabled) {
                    FakePlayerRegistry.clear();
                }
            }));
        VisualSettings.setSoundControlEnabled(gui.controlledCheckboxWithSettings("Sound Control",
                VisualSettings.soundControlEnabled(), this::populateSoundControlSettings,
                VisualSettings::setSoundControlEnabled));
        VisualSettings.setHitSoundsEnabled(gui.controlledCheckboxWithSettings("Hit Sounds",
                VisualSettings.hitSoundsEnabled(), this::populateHitSoundsSettings,
                VisualSettings::setHitSoundsEnabled));
        VisualSettings.setBetterChatEnabled(gui.controlledCheckboxWithSettings("Better Chat",
                VisualSettings.betterChatEnabled(), this::populateBetterChatSettings,
                VisualSettings::setBetterChatEnabled));
    }

    private void populateAntiStaffSettings(WidgetSurface.Frame gui) {
        VisualSettings.setAntiStaffCommand(gui.inputText("Command",
                VisualSettings.antiStaffCommand(), VisualSettings::setAntiStaffCommand));
    }

    private void populateAutoHypixelSettings(WidgetSurface.Frame gui) {
        VisualSettings.setAutoGGMessage(gui.inputText("Message",
                VisualSettings.autoGGMessage(), VisualSettings::setAutoGGMessage));
        VisualSettings.setAutoLeaveOnBanEnabled(gui.controlledCheckbox("Leave On Ban",
                VisualSettings.autoLeaveOnBanEnabled(), VisualSettings::setAutoLeaveOnBanEnabled));
    }

    private void populateAutoReconnectSettings(WidgetSurface.Frame gui) {
        float delay = gui.slider("Delay", VisualSettings.autoReconnectDelaySlider(),
                Math.round(VisualSettings.autoReconnectDelay()) + "s",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 30.0f, "s"),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 30.0f),
                VisualSettings::setAutoReconnectDelaySlider);
        VisualSettings.setAutoReconnectDelaySlider(delay);
    }

    private void populateQuickSwitchSettings(WidgetSurface.Frame gui) {
        String keyLabel = listeningQuickSwitchKey ? "Key: press key/mouse" : "Key: " + ForgeModEventBridge.quickSwitchKeyName();
        if (gui.button(keyLabel, gui.contentWidth(), 24)) {
            listeningQuickSwitchKey = true;
        }
        float slot = gui.slider("Slot", VisualSettings.quickSwitchSlotSlider(),
                Math.round(VisualSettings.quickSwitchSlot()) + "",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 9.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 9.0f),
                VisualSettings::setQuickSwitchSlotSlider);
        VisualSettings.setQuickSwitchSlotSlider(slot);
        VisualSettings.setQuickSwitchMode(gui.combo("Mode", VisualSettings.quickSwitchMode(),
                "Hold, Toggle", VisualSettings::setQuickSwitchMode));
        VisualSettings.setQuickSwitchRestoreSlot(gui.controlledCheckbox("Restore Slot",
                VisualSettings.quickSwitchRestoreSlot(), VisualSettings::setQuickSwitchRestoreSlot));
    }

    private void populateFakePlayerSettings(WidgetSurface.Frame gui) {
        VisualSettings.setFakePlayerName(gui.inputText("Name", VisualSettings.fakePlayerName(),
                VisualSettings::setFakePlayerName));
        float health = gui.slider("Health", VisualSettings.fakePlayerHealthSlider(),
                Math.round(VisualSettings.fakePlayerHealth()) + "",
                value -> SliderValueFormat.intRangeLabel(value, 1.0f, 40.0f, ""),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 40.0f),
                VisualSettings::setFakePlayerHealthSlider);
        VisualSettings.setFakePlayerHealthSlider(health);
        VisualSettings.setFakePlayerFacePlayer(gui.controlledCheckbox("Face Player",
                VisualSettings.fakePlayerFacePlayer(), VisualSettings::setFakePlayerFacePlayer));
        VisualSettings.setFakePlayerHitFeedback(gui.controlledCheckbox("Hit Feedback",
                VisualSettings.fakePlayerHitFeedback(), VisualSettings::setFakePlayerHitFeedback));
        VisualSettings.setFakePlayerReturnHome(gui.controlledCheckbox("Return Home",
                VisualSettings.fakePlayerReturnHome(), VisualSettings::setFakePlayerReturnHome));
        if (gui.actionButton("Place", gui.contentWidth(), 24)) {
            FakePlayerRegistry.place();
        }
        if (gui.actionButton("List", gui.contentWidth(), 24)) {
            openFakePlayerWindow();
        }
    }


    private void populateSoundControlSettings(WidgetSurface.Frame gui) {
        int muteMask = gui.multiSelect("Mute",
                VisualSettings.soundControlMuteMask(),
                "Master, Music, Records, Weather, Blocks, Hostile, Neutral, Players, Ambient, Voice",
                VisualSettings::setSoundControlMuteMask);
        VisualSettings.setSoundControlMuteMask(muteMask);
    }

    private void populateHitSoundsSettings(WidgetSurface.Frame gui) {
        String mode = gui.combo("Sound", VisualSettings.hitSoundsMode(),
                "Orb, Pling, Bell, Click, Pop, Crit",
                VisualSettings::setHitSoundsMode);
        VisualSettings.setHitSoundsMode(mode);
        float volume = gui.slider("Volume", VisualSettings.hitSoundsVolumeSlider(),
                SliderValueFormat.decimalRangeLabel(VisualSettings.hitSoundsVolumeSlider(), 0.1f, 2.0f, 2),
                value -> SliderValueFormat.decimalRangeLabel(value, 0.1f, 2.0f, 2),
                value -> SliderValueFormat.parseRangeSlider(value, 0.1f, 2.0f),
                VisualSettings::setHitSoundsVolumeSlider);
        VisualSettings.setHitSoundsVolumeSlider(volume);
        float pitch = gui.slider("Pitch", VisualSettings.hitSoundsPitchSlider(),
                SliderValueFormat.decimalRangeLabel(VisualSettings.hitSoundsPitchSlider(), 0.5f, 2.0f, 2),
                value -> SliderValueFormat.decimalRangeLabel(value, 0.5f, 2.0f, 2),
                value -> SliderValueFormat.parseRangeSlider(value, 0.5f, 2.0f),
                VisualSettings::setHitSoundsPitchSlider);
        VisualSettings.setHitSoundsPitchSlider(pitch);
    }

    private void populateBetterChatSettings(WidgetSurface.Frame gui) {
        VisualSettings.setBetterChatBackgroundColor(gui.colorPicker("Background",
                VisualSettings.betterChatBackgroundColor(),
                VisualSettings::setBetterChatBackgroundColor));
    }

    private void populateWatermarkSettings(WidgetSurface.Frame gui) {
        int elements = gui.multiSelect("Elements", VisualSettings.watermarkElementMask(),
                "Logo, Player, Config, Ping, FPS, Server, Time",
                VisualSettings::setWatermarkElementMask);
        VisualSettings.setWatermarkElementMask(elements);
        VisualSettings.setWatermarkBlur(gui.controlledCheckbox("Blur",
                VisualSettings.watermarkBlur(), VisualSettings::setWatermarkBlur));
    }

    private void populateHotkeyHudSettings(WidgetSurface.Frame gui) {
        VisualSettings.setHotkeyHudShowTitle(gui.controlledCheckbox("Show Title",
                VisualSettings.hotkeyHudShowTitle(), VisualSettings::setHotkeyHudShowTitle));
        VisualSettings.setHotkeyHudBlur(gui.controlledCheckbox("Blur",
                VisualSettings.hotkeyHudBlur(), VisualSettings::setHotkeyHudBlur));
    }

    private void populateArrayListHudSettings(WidgetSurface.Frame gui) {
        VisualSettings.setArrayListHudBlur(gui.controlledCheckbox("Blur",
                VisualSettings.arrayListHudBlur(), VisualSettings::setArrayListHudBlur));
        VisualSettings.setArrayListHudAccentBar(gui.controlledCheckbox("Accent Bar",
                VisualSettings.arrayListHudAccentBar(), VisualSettings::setArrayListHudAccentBar));
    }

    private void populateTargetsHudSettings(WidgetSurface.Frame gui) {
        VisualSettings.setTargetsHudShowAvatar(gui.controlledCheckbox("Avatar",
                VisualSettings.targetsHudShowAvatar(), VisualSettings::setTargetsHudShowAvatar));
        VisualSettings.setTargetsHudShowDistance(gui.controlledCheckbox("Distance",
                VisualSettings.targetsHudShowDistance(), VisualSettings::setTargetsHudShowDistance));
        VisualSettings.setTargetsHudShowHealthBar(gui.controlledCheckbox("Health Bar",
                VisualSettings.targetsHudShowHealthBar(), VisualSettings::setTargetsHudShowHealthBar));
        VisualSettings.setTargetsHudShowHealthText(gui.controlledCheckbox("Health Text",
                VisualSettings.targetsHudShowHealthText(), VisualSettings::setTargetsHudShowHealthText));
        VisualSettings.setTargetsHudBlur(gui.controlledCheckbox("Blur",
                VisualSettings.targetsHudBlur(), VisualSettings::setTargetsHudBlur));
    }

    private void populateBackpackHudSettings(WidgetSurface.Frame gui) {
        VisualSettings.setBackpackHudShowTitle(gui.controlledCheckbox("Show Title",
                VisualSettings.backpackHudShowTitle(), VisualSettings::setBackpackHudShowTitle));
        VisualSettings.setBackpackHudShowCounts(gui.controlledCheckbox("Item Counts",
                VisualSettings.backpackHudShowCounts(), VisualSettings::setBackpackHudShowCounts));
        VisualSettings.setBackpackHudBlur(gui.controlledCheckbox("Blur",
                VisualSettings.backpackHudBlur(), VisualSettings::setBackpackHudBlur));
    }

    private void populatePotionBuffHudSettings(WidgetSurface.Frame gui) {
        VisualSettings.setPotionBuffHudShowIcons(gui.controlledCheckbox("Icons",
                VisualSettings.potionBuffHudShowIcons(), VisualSettings::setPotionBuffHudShowIcons));
        VisualSettings.setPotionBuffHudShowDuration(gui.controlledCheckbox("Duration",
                VisualSettings.potionBuffHudShowDuration(), VisualSettings::setPotionBuffHudShowDuration));
        VisualSettings.setPotionBuffHudEffectColors(gui.controlledCheckbox("Effect Colors",
                VisualSettings.potionBuffHudEffectColors(), VisualSettings::setPotionBuffHudEffectColors));
        VisualSettings.setPotionBuffHudBlur(gui.controlledCheckbox("Blur",
                VisualSettings.potionBuffHudBlur(), VisualSettings::setPotionBuffHudBlur));
    }

    private void populateSpeedHudSettings(WidgetSurface.Frame gui) {
        VisualSettings.setSpeedHudShowValue(gui.controlledCheckbox("Value",
                VisualSettings.speedHudShowValue(), VisualSettings::setSpeedHudShowValue));
        VisualSettings.setSpeedHudShowChart(gui.controlledCheckbox("Chart",
                VisualSettings.speedHudShowChart(), VisualSettings::setSpeedHudShowChart));
        VisualSettings.setSpeedHudBlur(gui.controlledCheckbox("Blur",
                VisualSettings.speedHudBlur(), VisualSettings::setSpeedHudBlur));
    }

    private int drawSettingsCard(GuiGraphics graphics, int x, int y, int stableY, int w) {
        int h = measureSettingsCard(x, stableY, w);
        h = drawCard(graphics, "SETTINGS", x, y, w, h);
        WidgetSurface.Frame gui = beginCardGui(graphics, x, y, stableY, w);
        populateSettings(gui);
        return h;
    }

    private void populateSettings(WidgetSurface.Frame gui) {
        String guiScaleValue = gui.syncedCombo("GUI Scale", VisualSettings.dashboardGuiScale(),
                "75%, 100%, 125%, 150%, 175%, 200%",
                VisualSettings::setDashboardGuiScale);
        VisualSettings.setDashboardGuiScale(guiScaleValue);
        String theme = gui.syncedCombo("Theme", UiSettings.theme(), "Light, Dark, Auto", UiSettings::setTheme);
        UiSettings.setTheme(theme);
        int hudWidgets = gui.multiSelect("HUD Widgets", hudWidgetUiMask(),
                "Watermark, Hotkeys, Array List, Targets, Backpack, Potion Buff, Speed",
                mask -> VisualSettings.setHudWidgetMask(hudWidgetSettingsMask(mask)));
        VisualSettings.setHudWidgetMask(hudWidgetSettingsMask(hudWidgets));
        UiSettings.setFullscreenBlurEnabled(gui.controlledCheckbox("FullScreen Blur", UiSettings.fullscreenBlurEnabled(), UiSettings::setFullscreenBlurEnabled));
        VisualSettings.setNotificationsEnabled(gui.controlledCheckboxWithSettings("Notifications",
                VisualSettings.notificationsEnabled(), this::populateNotificationSettings,
                VisualSettings::setNotificationsEnabled));
        VisualSettings.setModernUiEnabled(gui.controlledCheckbox("Modern UI",
                VisualSettings.modernUiEnabled(), VisualSettings::setModernUiEnabled));
    }

    private void populateNotificationSettings(WidgetSurface.Frame gui) {
        String position = gui.syncedCombo("Position", VisualSettings.notificationsPosition(),
                "Bottom Right, Bottom Left, Top Right, Top Left",
                VisualSettings::setNotificationsPosition);
        VisualSettings.setNotificationsPosition(position);
        float duration = gui.slider("Duration", VisualSettings.notificationsDurationSlider(),
                String.format(java.util.Locale.ROOT, "%.1fs", VisualSettings.notificationsDuration()),
                value -> String.format(java.util.Locale.ROOT, "%.1fs", 1.0f + clampFloat(value, 0.0f, 1.0f) * 7.0f),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 8.0f),
                VisualSettings::setNotificationsDurationSlider);
        VisualSettings.setNotificationsDurationSlider(duration);
        float maxVisible = gui.slider("Max Visible", VisualSettings.notificationsMaxVisibleSlider(),
                VisualSettings.notificationsMaxVisible() + "",
                value -> String.valueOf(Math.round(1.0f + clampFloat(value, 0.0f, 1.0f) * 7.0f)),
                value -> SliderValueFormat.parseRangeSlider(value, 1.0f, 8.0f),
                VisualSettings::setNotificationsMaxVisibleSlider);
        VisualSettings.setNotificationsMaxVisibleSlider(maxVisible);
        VisualSettings.setNotificationsBlur(gui.controlledCheckbox("Blur",
                VisualSettings.notificationsBlur(), VisualSettings::setNotificationsBlur));
        VisualSettings.setNotificationsLinkHotkeys(gui.controlledCheckbox("Link Hotkeys",
                VisualSettings.notificationsLinkHotkeys(), VisualSettings::setNotificationsLinkHotkeys));
        VisualSettings.setNotificationsModuleToggles(gui.controlledCheckbox("Module Toggles",
                VisualSettings.notificationsModuleToggles(), VisualSettings::setNotificationsModuleToggles));
    }

    private int hudWidgetUiMask() {
        int settings = VisualSettings.hudWidgetMask();
        int ui = 0;
        if ((settings & VisualSettings.HUD_WIDGET_WATERMARK) != 0) {
            ui |= 1;
        }
        if ((settings & VisualSettings.HUD_WIDGET_HOTKEYS) != 0) {
            ui |= 1 << 1;
        }
        if ((settings & VisualSettings.HUD_WIDGET_ARRAY_LIST) != 0) {
            ui |= 1 << 2;
        }
        if ((settings & VisualSettings.HUD_WIDGET_TARGETS) != 0) {
            ui |= 1 << 3;
        }
        if ((settings & VisualSettings.HUD_WIDGET_BACKPACK) != 0) {
            ui |= 1 << 4;
        }
        if ((settings & VisualSettings.HUD_WIDGET_POTION_BUFF) != 0) {
            ui |= 1 << 5;
        }
        if ((settings & VisualSettings.HUD_WIDGET_SPEED) != 0) {
            ui |= 1 << 6;
        }
        return ui;
    }

    private int hudWidgetSettingsMask(int ui) {
        int settings = 0;
        if ((ui & 1) != 0) {
            settings |= VisualSettings.HUD_WIDGET_WATERMARK;
        }
        if ((ui & (1 << 1)) != 0) {
            settings |= VisualSettings.HUD_WIDGET_HOTKEYS;
        }
        if ((ui & (1 << 2)) != 0) {
            settings |= VisualSettings.HUD_WIDGET_ARRAY_LIST;
        }
        if ((ui & (1 << 3)) != 0) {
            settings |= VisualSettings.HUD_WIDGET_TARGETS;
        }
        if ((ui & (1 << 4)) != 0) {
            settings |= VisualSettings.HUD_WIDGET_BACKPACK;
        }
        if ((ui & (1 << 5)) != 0) {
            settings |= VisualSettings.HUD_WIDGET_POTION_BUFF;
        }
        if ((ui & (1 << 6)) != 0) {
            settings |= VisualSettings.HUD_WIDGET_SPEED;
        }
        return settings;
    }

    private int drawCard(GuiGraphics graphics, String title, int x, int y, int w, int targetH) {
        String displayTitle = cardDisplayTitle(title);
        currentCardTitle = displayTitle;
        int h = animatedCardHeight(displayTitle, targetH);
        int headerCenterY = y + 19;
        int accentY = headerCenterY - 7;
        int titleY = Math.round(MSDFRenderer.textCenteredY(y, 38, UiMetrics.Control.FONT_SIZE));
        ShapeRenderer.roundRect(graphics, x, y, w, h, 13, SURFACE);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 13, 1, BORDER);
        ShapeRenderer.roundRect(graphics, x + 10, accentY, 3, 14, 2, ACCENT);
        text(graphics, displayTitle, x + 21, titleY, TEXT);
        if (!"FRIEND ACTIONS".equals(displayTitle) && !"FRIEND LIST".equals(displayTitle)) {
            graphics.hLine(x + 12, x + w - 12, y + 34, BORDER);
        }
        return h;
    }

    private void drawCardHeaderValue(GuiGraphics graphics, String value, int x, int y, int w) {
        if (value == null || value.isBlank()) {
            return;
        }
        int valueW = textWidth(value);
        int valueY = Math.round(MSDFRenderer.textCenteredY(y, 38, UiMetrics.Control.FONT_SIZE));
        text(graphics, value, x + w - 18 - valueW, valueY, MUTED);
    }

    private int animatedCardHeight(String displayTitle, int targetH) {
        String id = contentScope() + "/" + displayTitle;
        float current = cardHeightAnimations.getOrDefault(id, (float) targetH);
        float speed = "FRIEND LIST".equals(displayTitle) ? UiAnimationTimings.CONTROL : UiAnimationTimings.CARD_SIZE;
        float next = lerp(current, targetH, speed);
        if (Math.abs(next - targetH) < 0.5f) {
            next = targetH;
        }
        cardHeightAnimations.put(id, next);
        return Math.max(40, Math.round(next));
    }

    private static String cardDisplayTitle(String title) {
        if (title == null || title.isBlank()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(title.length());
        boolean uppercaseNext = true;
        for (int i = 0; i < title.length(); i++) {
            char c = title.charAt(i);
            if (Character.isLetter(c)) {
                builder.append(uppercaseNext ? Character.toUpperCase(c) : Character.toLowerCase(c));
                uppercaseNext = false;
            } else {
                builder.append(c);
                uppercaseNext = !Character.isDigit(c);
            }
        }
        return builder.toString();
    }

    private WidgetSurface.Frame beginCardGui(GuiGraphics graphics, int x, int y, int stableY, int w) {
        return dashboardSurface.beginCard(graphics, cardScope(currentCardTitle), currentCardTitle, dashboardX, dashboardY, x, y, w, stableY);
    }

    private WidgetSurface.Frame beginCardGui(GuiGraphics graphics, int x, int y, int w) {
        return dashboardSurface.beginCard(graphics, cardScope(currentCardTitle), currentCardTitle, dashboardX, dashboardY, x, y, w, y);
    }

    private WidgetSurface.Frame beginClippedCardGui(GuiGraphics graphics, int x, int y, int stableY, int w, int h) {
        int clipX = x + 1;
        int clipY = y + 35;
        int clipW = Math.max(1, w - 2);
        int clipH = Math.max(1, h - 36);
        return dashboardSurface.beginCard(graphics, cardScope(currentCardTitle), currentCardTitle,
                dashboardX, dashboardY, x, y, w, stableY, clipX, clipY, clipW, clipH);
    }

    private WidgetSurface.Frame measureCardGui(int x, int y, int w) {
        return dashboardSurface.measureCard(cardScope(currentCardTitle), dashboardX, dashboardY, x, y, w);
    }

    private String contentScope() {
        return (renderingContentTab != null ? renderingContentTab : selectedTab).name();
    }

    private String cardScope(String cardTitle) {
        String cleanCard = cardTitle == null || cardTitle.isBlank() ? "CARD" : cardTitle.trim();
        return contentScope() + "/" + cleanCard;
    }

    private int measureUserProfileCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateUserProfile(gui);
        return gui.cardHeight();
    }

    private int measureUserRuntimeCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateUserRuntime(gui);
        return gui.cardHeight();
    }

    private int measureConfigActionsCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateConfigActions(gui);
        return gui.cardHeight();
    }

    private int measureConfigListCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateConfigList(gui);
        return gui.cardHeight();
    }

    private int measureAimbotCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        CombatSettingsPage.populateAimbot(gui);
        return gui.cardHeight();
    }

    private int measureCombatModulesCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateCombatModules(gui);
        return gui.cardHeight();
    }

    private int measureMeleeCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateMelee(gui);
        return gui.cardHeight();
    }

    private int measureDefenseCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        CombatSettingsPage.populateDefense(gui);
        return gui.cardHeight();
    }

    private int measureTerrainCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        CombatSettingsPage.populateTerrain(gui);
        return gui.cardHeight();
    }

    private int measureAutoCrystalCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateAutoCrystal(gui);
        return gui.cardHeight();
    }

    private int measureBedAuraCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateBedAura(gui);
        return gui.cardHeight();
    }

    private int measureAnchorAuraCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateAnchorAura(gui);
        return gui.cardHeight();
    }

    private int measureKillAuraTargetsCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        CombatSettingsPage.populateKillAuraTargets(gui);
        return gui.cardHeight();
    }

    private int measureKillAuraRotationCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        CombatSettingsPage.populateKillAuraRotation(gui);
        return gui.cardHeight();
    }

    private int measureCombatAssistCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateCombatAssist(gui);
        return gui.cardHeight();
    }

    private int measureRagebotMainCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        CombatSettingsPage.populateRagebotMain(gui);
        return gui.cardHeight();
    }

    private int measureRagebotSelectionCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        CombatSettingsPage.populateRagebotSelection(gui);
        return gui.cardHeight();
    }

    private int measureRagebotOtherCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        CombatSettingsPage.populateRagebotOther(gui);
        return gui.cardHeight();
    }

    private int measureRagebotAntiAimCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        CombatSettingsPage.populateRagebotAntiAim(gui);
        return gui.cardHeight();
    }

    private int measureBypassMainCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        BypassSettingsPage.populateMain(gui);
        return gui.cardHeight();
    }

    private int measureBypassRotationCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        BypassSettingsPage.populateRotation(gui);
        return gui.cardHeight();
    }

    private int measureBypassTimingCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        BypassSettingsPage.populateTiming(gui);
        return gui.cardHeight();
    }

    private int measureDisablerCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        DisablerSettingsPage.populate(gui);
        return gui.cardHeight();
    }

    private int measurePingSpoofCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        PingSpoofSettingsPage.populate(gui);
        return gui.cardHeight();
    }

    private int measureNoRotateCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        NoRotateSettingsPage.populate(gui);
        return gui.cardHeight();
    }

    private int measureBypassNetworkCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        BypassSettingsPage.populateNetwork(gui);
        return gui.cardHeight();
    }

    private int measureBypassMovementCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        BypassSettingsPage.populateMovement(gui);
        return gui.cardHeight();
    }

    private int measureSocialCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateSocial(gui);
        return gui.cardHeight();
    }

    private int measureFriendActionsCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateFriendActions(gui);
        return gui.cardHeight();
    }

    private int measureFriendsListCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateFriendsList(gui);
        return gui.cardHeight();
    }

    private int measureScaffoldCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateScaffold(gui);
        return gui.cardHeight();
    }

    private int measureWorldResourceEspCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateWorldResourceEsp(gui);
        return gui.cardHeight();
    }

    private int measureWorldSpecialBlocksCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        WorldSettingsPage.populateWorldSpecialBlocks(gui);
        return gui.cardHeight();
    }

    private int measureVisualGeneralCard(int x, int y, int w) {
        return measureActiveVisualGeneralCard(x, y, w);
    }

    private int measureActiveVisualGeneralCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateVisualGeneral(gui);
        return gui.cardHeight();
    }

    private int measureVisualGeneralStableCard(int x, int y, int w) {
        int max = 0;
        WidgetSurface.Frame enemies = measureCardGui(x, y, w);
        populateEnemyVisualGeneral(enemies);
        max = Math.max(max, enemies.cardHeight());

        WidgetSurface.Frame team = measureCardGui(x, y, w);
        VisualSettings.setVisualTeamEnabled(team.controlledCheckbox("Enable",
                VisualSettings.visualTeamEnabled(), VisualSettings::setVisualTeamEnabled));
        max = Math.max(max, team.cardHeight());

        WidgetSurface.Frame local = measureCardGui(x, y, w);
        VisualSettings.setVisualLocalEnabled(local.controlledCheckbox("Enable",
                VisualSettings.visualLocalEnabled(), VisualSettings::setVisualLocalEnabled));
        max = Math.max(max, local.cardHeight());
        return max;
    }

    private int measureVisualModelCard(int x, int y, int w) {
        return measureActiveVisualModelCard(x, y, w);
    }

    private int measureActiveVisualModelCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateVisualModel(gui);
        return gui.cardHeight();
    }

    private int measureMovementCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateMovement(gui);
        return gui.cardHeight();
    }

    private int measureMovementUtilityCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateMovementUtility(gui);
        return gui.cardHeight();
    }

    private int measureWorldRenderCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        WorldSettingsPage.populateWorldRender(gui);
        return gui.cardHeight();
    }

    private int measureWorldUtilityCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        WorldSettingsPage.populateWorldUtility(gui);
        return gui.cardHeight();
    }

    private int measureWorldBuildMineCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateWorldBuildMine(gui);
        return gui.cardHeight();
    }

    private int measurePlayerCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        PlayerInventorySettingsPage.populatePlayer(gui);
        return gui.cardHeight();
    }

    private int measureInventoryCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateInventory(gui);
        return gui.cardHeight();
    }

    private int measureMiscCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateMisc(gui);
        return gui.cardHeight();
    }

    private int measureSettingsCard(int x, int y, int w) {
        WidgetSurface.Frame gui = measureCardGui(x, y, w);
        populateSettings(gui);
        return gui.cardHeight();
    }

    private void drawStatusBar(GuiGraphics graphics) {
        int x = dashboardX + 86;
        int h = 22;
        int y = dashboardY + dashboardH - h - 8;
        int w = dashboardW - 100;
        int centerY = y + h / 2;
        int textY = Math.round(MSDFRenderer.textCenteredY(y, h, UiMetrics.Control.FONT_SIZE));
        int dividerTop = centerY - 5;
        int dividerBottom = centerY + 5;
        ShapeRenderer.roundRect(graphics, x, y, w, h, 11, themeColor(0xFFF7F8FC, FIELD));
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 11, 1, BORDER);
        text(graphics, "STATUS: ACTIVE", x + 12, textY, MUTED);
        graphics.vLine(x + 112, dividerTop, dividerBottom, BORDER);
        text(graphics, "FPS: " + MinecraftFps.read(), x + 124, textY, MUTED);
        graphics.vLine(x + 194, dividerTop, dividerBottom, BORDER);
        text(graphics, "PING: " + dashboardPingLabel(), x + 206, textY, MUTED);
        String status = "PROTECTED";
        int statusTextW = textWidth(status);
        int statusX = x + w - 14 - statusTextW - 12;
        ShapeRenderer.roundRect(graphics, statusX, centerY - 3, 6, 6, 3, GREEN);
        text(graphics, status, statusX + 12, textY, 0xFF10A66A);
    }

    private static String dashboardPingLabel() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.hasSingleplayerServer() || minecraft.player == null || minecraft.getConnection() == null) {
            return "--";
        }
        var playerInfo = minecraft.getConnection().getPlayerInfo(minecraft.player.getUUID());
        return playerInfo == null ? "--" : Math.max(0, playerInfo.getLatency()) + "ms";
    }

    private void enableContentScissor(int x, int y, int w, int h) {
        Window window = Minecraft.getInstance().getWindow();
        double scaledX = toDashboardScale(x + dashboardRenderOffsetX, true);
        double scaledY = toDashboardScale(y + dashboardRenderOffsetY, false);
        int scissorX = (int) Math.floor(scaledX);
        int scissorY = (int) Math.floor(scaledY);
        int scissorW = Math.max(1, (int) Math.ceil(w * dashboardScale));
        int scissorH = Math.max(1, (int) Math.ceil(h * dashboardScale));
        RenderSystem.enableScissor(scissorX, window.getHeight() - scissorY - scissorH, scissorW, scissorH);
    }

    private boolean enableIntersectedContentScissor(int ax, int ay, int aw, int ah,
                                                   int bx, int by, int bw, int bh) {
        int x = Math.max(ax, bx);
        int y = Math.max(ay, by);
        int right = Math.min(ax + aw, bx + bw);
        int bottom = Math.min(ay + ah, by + bh);
        int w = right - x;
        int h = bottom - y;
        if (w <= 0 || h <= 0) {
            return false;
        }
        enableContentScissor(x, y, w, h);
        return true;
    }

    private void enableCardContentScissor(int x, int y, int w, int h) {
        int clipX = x + 1;
        int clipY = y + 35;
        int clipW = Math.max(1, w - 2);
        int clipH = Math.max(1, h - 36);
        enableContentScissor(clipX, clipY, clipW, clipH);
    }

    private void restoreContentScissor() {
        if (contentBoundsW > 0 && contentBoundsH > 0) {
            enableContentScissor(contentBoundsX, contentBoundsY, contentBoundsW, contentBoundsH);
        } else {
            RenderSystem.disableScissor();
        }
    }

    private void text(GuiGraphics graphics, String value, int x, int y, int color) {
        MSDFRenderer.draw(graphics, value, x, y, alphaColor(color, contentRenderAlpha));
    }

    private void text(GuiGraphics graphics, String value, int x, int y, float size, int color) {
        MSDFRenderer.draw(graphics, value, x, y, size, alphaColor(color, contentRenderAlpha));
    }

    private void unicodeText(GuiGraphics graphics, String value, int x, int y, float size, int color) {
        TextPainter.drawMsdf(graphics, value, x, y, size, color, contentRenderAlpha);
    }

    private void drawShadowedText(GuiGraphics graphics, String value, int x, int y, float size, int color) {
        MSDFRenderer.draw(graphics, value, x + 1, y + 1, size, alphaColor(0xB0000000, contentRenderAlpha));
        MSDFRenderer.draw(graphics, value, x, y, size, alphaColor(color, contentRenderAlpha));
    }

    private void drawRectBorder(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        ShapeDraw.rectBorder(graphics, x, y, w, h, color);
    }

    private void drawLine(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        ShapeDraw.pixelLine(graphics, x0, y0, x1, y1, color);
    }

    private boolean isHovering(int x, int y, int w, int h) {
        if (previewDragging()) {
            return false;
        }
        return contains(renderMouseX, renderMouseY, x, y, w, h);
    }

    private boolean previewDragging() {
        return previewDragTarget != PreviewDragTarget.NONE;
    }

    private static boolean contains(double px, double py, int x, int y, int w, int h) {
        return UiMath.contains(px, py, x, y, w, h);
    }

    private static int withAlpha(int color, int alpha) {
        return UiColor.withAlpha(color, alpha);
    }

    private static int alphaColor(int color, float alpha) {
        return UiColor.alpha(color, alpha);
    }

    private static int lerpColor(int from, int to, float value) {
        return UiColor.lerp(from, to, value);
    }

    private static int themeColor(int light, int dark) {
        return UiPalette.dark() ? dark : light;
    }

    private int textWidth(String value) {
        return MSDFRenderer.width(value);
    }

    private boolean startPreviewDrag(double scaledX, double scaledY) {
        if (previewEspPopupOpen || previewEspPopupAlpha > 0.02f || dashboardSurface.insideAnyPopup(scaledX, scaledY)) {
            return false;
        }
        PreviewBounds startBounds;
        PreviewBounds nameBounds = previewComponentBounds.get(PreviewSettings.MOB_NAME);
        PreviewBounds distanceBounds = previewComponentBounds.get(PreviewSettings.MOB_DISTANCE);
        PreviewBounds healthBounds = previewComponentBounds.get(PreviewSettings.MOB_HEALTH);
        Map.Entry<Long, PreviewBounds> mainEntry = previewMainItemBounds.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().contains(scaledX, scaledY))
                .findFirst()
                .orElse(null);
        Map.Entry<Long, PreviewBounds> flagEntry = previewFlagBounds.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().contains(scaledX, scaledY))
                .findFirst()
                .orElse(null);
        if (mainEntry != null) {
            previewDragTarget = PreviewDragTarget.MAIN_ITEM;
            previewDragMainItem = mainEntry.getKey();
            previewDragAnchor = VisualSettings.visualEspItemAnchor(previewDragMainItem);
            startBounds = mainEntry.getValue();
        } else if (flagEntry != null) {
            previewDragTarget = PreviewDragTarget.FLAG;
            previewDragFlagItem = flagEntry.getKey();
            previewDragAnchor = VisualSettings.visualEspFlagAnchor(previewDragFlagItem);
            startBounds = flagEntry.getValue();
        } else if (nameBounds != null && nameBounds.contains(scaledX, scaledY)) {
            previewDragTarget = PreviewDragTarget.NAME;
            previewDragAnchor = VisualSettings.mobNameAnchor();
            startBounds = nameBounds;
        } else if (distanceBounds != null && distanceBounds.contains(scaledX, scaledY)) {
            previewDragTarget = PreviewDragTarget.DISTANCE;
            previewDragAnchor = VisualSettings.mobDistanceAnchor();
            startBounds = distanceBounds;
        } else if (healthBounds != null && healthBounds.contains(scaledX, scaledY)) {
            previewDragTarget = PreviewDragTarget.HEALTH;
            previewDragAnchor = VisualSettings.mobHealthAnchor();
            previewHealthGhostHorizontal = isHorizontalAnchor(previewDragAnchor) ? 1.0f : 0.0f;
            startBounds = healthBounds;
        } else {
            return false;
        }
        previewDragGhost.reset();
        previewDragGrabCenterOffsetX = scaledX - (startBounds.x() + startBounds.w() * 0.5);
        previewDragGrabCenterOffsetY = scaledY - (startBounds.y() + startBounds.h() * 0.5);
        previewDragLastX = scaledX;
        previewDragLastY = scaledY;
        return true;
    }

    private boolean updatePreviewDrag(double scaledX, double scaledY) {
        if (previewDragTarget == PreviewDragTarget.NONE) {
            return false;
        }
        previewDragLastX = scaledX;
        previewDragLastY = scaledY;
        VisualSettings.EspAnchor nextAnchor = snapPreviewAnchor(scaledX, scaledY);
        if (previewDragUsesBarGhost()
                && isHorizontalAnchor(previewDragAnchor) != isHorizontalAnchor(nextAnchor)) {
            previewDragGrabCenterOffsetX = 0.0;
            previewDragGrabCenterOffsetY = 0.0;
            previewDragGhost.reset();
        }
        previewDragAnchor = nextAnchor;
        return true;
    }

    private boolean previewDragUsesBarGhost() {
        return previewDragTarget == PreviewDragTarget.HEALTH
                || previewDragTarget == PreviewDragTarget.MAIN_ITEM
                && (previewDragMainItem == VisualSettings.ESP_ITEM_ARMOR_BAR
                || previewDragMainItem == VisualSettings.ESP_ITEM_ABSORPTION);
    }

    private boolean startPreviewModelDrag(double scaledX, double scaledY) {
        if (selectedTab != SidebarTab.VISUAL
                || previewEspPopupOpen
                || previewEspPopupAlpha > 0.02f
                || dashboardSurface.insideAnyPopup(scaledX, scaledY)
                || !previewModelHovered(scaledX, scaledY)) {
            return false;
        }
        previewModelDragging = true;
        return true;
    }

    private boolean previewModelHovered(double scaledX, double scaledY) {
        if (previewEspBoxBounds == null) {
            return false;
        }
        double insetX = Math.max(10.0, previewEspBoxBounds.w() * 0.20);
        double insetY = Math.max(8.0, previewEspBoxBounds.h() * 0.12);
        int x = (int) Math.round(previewEspBoxBounds.x() + insetX);
        int y = (int) Math.round(previewEspBoxBounds.y() + insetY);
        int w = (int) Math.round(Math.max(16.0, previewEspBoxBounds.w() - insetX * 2.0));
        int h = (int) Math.round(Math.max(20.0, previewEspBoxBounds.h() - insetY * 2.0));
        return contains(scaledX, scaledY, x, y, w, h);
    }

    private boolean previewElementHovered(double scaledX, double scaledY) {
        if (previewEspBoxBounds != null && previewEspBoxBounds.contains(scaledX, scaledY)) {
            return true;
        }
        for (PreviewBounds bounds : previewComponentBounds.values()) {
            if (bounds != null && bounds.contains(scaledX, scaledY)) {
                return true;
            }
        }
        return false;
    }

    private boolean startContentScrollbarDrag(double scaledX, double scaledY) {
        if (maxContentScrollY <= 0) {
            return false;
        }
        int x = dashboardX + 86;
        int y = dashboardY + 58;
        int w = dashboardW - 100;
        int viewportH = dashboardY + dashboardH - 36 - y;
        ScrollbarControl.Layout layout = ScrollbarControl.layout(x, y, w, y, viewportH, w, contentScrollbarReserve);
        ScrollbarControl.Layout hitLayout = new ScrollbarControl.Layout(layout.contentW(),
                layout.trackX() + MAIN_SCROLLBAR_X_OFFSET, layout.trackY(), layout.trackH());
        if (!ScrollbarControl.beginDrag(contentScrollbarDrag, scaledX, scaledY, hitLayout,
                viewportH, viewportH + maxContentScrollY, contentScrollY, maxContentScrollY)) {
            return false;
        }
        closeConfigMenu();
        dashboardSurface.closePopupsForScroll();
        return true;
    }

    private boolean updateContentScrollbarDrag(double scaledY) {
        int x = dashboardX + 86;
        int y = dashboardY + 58;
        int w = dashboardW - 100;
        int viewportH = dashboardY + dashboardH - 36 - y;
        ScrollbarControl.Layout layout = ScrollbarControl.layout(x, y, w, y, viewportH, w, contentScrollbarReserve);
        ScrollbarControl.Layout hitLayout = new ScrollbarControl.Layout(layout.contentW(),
                layout.trackX() + MAIN_SCROLLBAR_X_OFFSET, layout.trackY(), layout.trackH());
        int[] nextScroll = {contentScrollY};
        if (!ScrollbarControl.updateDrag(contentScrollbarDrag, scaledY, hitLayout,
                viewportH, viewportH + maxContentScrollY, maxContentScrollY, nextScroll)) {
            return false;
        }
        contentScrollY = nextScroll[0];
        animatedContentScrollY = contentScrollY;
        rememberScroll(selectedTab);
        contentScrollbarAlpha = Math.max(contentScrollbarAlpha, 1.0f);
        return true;
    }

    private int musicResultScrollbarTrackX() {
        return musicListX + musicListW - 12;
    }

    private ScrollbarControl.Layout musicResultScrollbarLayout() {
        return new ScrollbarControl.Layout(
                Math.max(0, musicListW - 14),
                musicResultScrollbarTrackX(),
                musicListY + ScrollbarControl.VERTICAL_INSET,
                Math.max(1, musicListH - ScrollbarControl.VERTICAL_INSET * 2));
    }

    private boolean startMusicResultScrollbarDrag(double scaledX, double scaledY) {
        if (selectedTab != SidebarTab.MUSIC || musicMaxResultScrollY <= 0) {
            return false;
        }
        return ScrollbarControl.beginDrag(
                musicResultScrollbarDrag, scaledX, scaledY, musicResultScrollbarLayout(),
                musicListH, musicResultContentH, musicResultScrollY, musicMaxResultScrollY);
    }

    private boolean updateMusicResultScrollbarDrag(double scaledY) {
        int[] nextScroll = {musicResultScrollY};
        if (!ScrollbarControl.updateDrag(
                musicResultScrollbarDrag, scaledY, musicResultScrollbarLayout(),
                musicListH, musicResultContentH, musicMaxResultScrollY, nextScroll)) {
            return false;
        }
        musicResultScrollY = nextScroll[0];
        musicAnimatedResultScrollY = musicResultScrollY;
        musicResultScrollbarAlpha = 1.0f;
        return true;
    }

    private boolean startOreWindowScrollbarDrag(double scaledX, double scaledY) {
        if (oreWindowMaxScrollY <= 0) {
            return false;
        }
        int contentY = oreWindowContentY();
        int contentW = ORE_WINDOW_W - 24;
        int contentH = oreWindowContentH(contentY);
        ScrollbarControl.Layout layout = ScrollbarControl.layout(oreWindowX, oreWindowY, ORE_WINDOW_W,
                contentY, contentH, contentW, SCROLLBAR_RIGHT_INSET);
        if (!ScrollbarControl.beginDrag(oreWindowScrollbarDrag, scaledX, scaledY, layout,
                contentH, contentH + oreWindowMaxScrollY, oreWindowScrollY, oreWindowMaxScrollY)) {
            return false;
        }
        oreWindowDragging = false;
        return true;
    }

    private boolean updateOreWindowScrollbarDrag(double scaledY) {
        int contentY = oreWindowContentY();
        int contentW = ORE_WINDOW_W - 24;
        int contentH = oreWindowContentH(contentY);
        ScrollbarControl.Layout layout = ScrollbarControl.layout(oreWindowX, oreWindowY, ORE_WINDOW_W,
                contentY, contentH, contentW, SCROLLBAR_RIGHT_INSET);
        int[] nextScroll = {oreWindowScrollY};
        if (!ScrollbarControl.updateDrag(oreWindowScrollbarDrag, scaledY, layout,
                contentH, contentH + oreWindowMaxScrollY, oreWindowMaxScrollY, nextScroll)) {
            return false;
        }
        oreWindowScrollY = nextScroll[0];
        oreWindowAnimatedScrollY = oreWindowScrollY;
        oreWindowScrollbarAlpha = Math.max(oreWindowScrollbarAlpha, 1.0f);
        return true;
    }

    private boolean startFakePlayerWindowScrollbarDrag(double scaledX, double scaledY) {
        if (fakePlayerWindowMaxScrollY <= 0) {
            return false;
        }
        int contentY = fakePlayerWindowY + FAKE_PLAYER_WINDOW_TITLE_H + 10;
        int contentW = FAKE_PLAYER_WINDOW_W - 28;
        int contentH = FAKE_PLAYER_WINDOW_H - FAKE_PLAYER_WINDOW_TITLE_H - 58;
        ScrollbarControl.Layout layout = ScrollbarControl.layout(fakePlayerWindowX, fakePlayerWindowY,
                FAKE_PLAYER_WINDOW_W, contentY, contentH, contentW, SCROLLBAR_RIGHT_INSET);
        if (!ScrollbarControl.beginDrag(fakePlayerWindowScrollbarDrag, scaledX, scaledY, layout,
                contentH, contentH + fakePlayerWindowMaxScrollY,
                fakePlayerWindowScrollY, fakePlayerWindowMaxScrollY)) {
            return false;
        }
        fakePlayerWindowDragging = false;
        return true;
    }

    private boolean updateFakePlayerWindowScrollbarDrag(double scaledY) {
        int contentY = fakePlayerWindowY + FAKE_PLAYER_WINDOW_TITLE_H + 10;
        int contentW = FAKE_PLAYER_WINDOW_W - 28;
        int contentH = FAKE_PLAYER_WINDOW_H - FAKE_PLAYER_WINDOW_TITLE_H - 58;
        ScrollbarControl.Layout layout = ScrollbarControl.layout(fakePlayerWindowX, fakePlayerWindowY,
                FAKE_PLAYER_WINDOW_W, contentY, contentH, contentW, SCROLLBAR_RIGHT_INSET);
        int[] nextScroll = {fakePlayerWindowScrollY};
        if (!ScrollbarControl.updateDrag(fakePlayerWindowScrollbarDrag, scaledY, layout,
                contentH, contentH + fakePlayerWindowMaxScrollY, fakePlayerWindowMaxScrollY, nextScroll)) {
            return false;
        }
        fakePlayerWindowScrollY = nextScroll[0];
        fakePlayerWindowAnimatedScrollY = fakePlayerWindowScrollY;
        fakePlayerWindowScrollbarAlpha = Math.max(fakePlayerWindowScrollbarAlpha, 1.0f);
        return true;
    }

    private boolean handleSelectionWindowMouseClicked(int button) {
        if (!oreWindowOpen) {
            return false;
        }
        if (button == 0 && startOreWindowScrollbarDrag(clickX, clickY)) {
            clickPending = false;
            return true;
        }
        if (contains(clickX, clickY, oreWindowX, oreWindowY, ORE_WINDOW_W, ORE_WINDOW_H)) {
            if (button == 0 && selectionWindowTitleContains(clickX, clickY)) {
                oreWindowDragging = true;
                oreWindowDragOffsetX = clickX - oreWindowX;
                oreWindowDragOffsetY = clickY - oreWindowY;
                selectionSearchFocused = false;
                clickPending = false;
                return true;
            }
            oreWindowDragging = false;
            clickButton = button;
            clickPending = true;
            return true;
        }
        oreWindowDragging = false;
        clickPending = false;
        return true;
    }

    private boolean handleFakePlayerWindowMouseClicked(int button) {
        if (!fakePlayerWindowOpen) {
            return false;
        }
        if (button == 0 && startFakePlayerWindowScrollbarDrag(clickX, clickY)) {
            clickPending = false;
            return true;
        }
        if (contains(clickX, clickY, fakePlayerWindowX, fakePlayerWindowY,
                FAKE_PLAYER_WINDOW_W, FAKE_PLAYER_WINDOW_H)) {
            if (button == 0 && fakePlayerWindowTitleContains(clickX, clickY)) {
                fakePlayerWindowDragging = true;
                fakePlayerWindowDragOffsetX = clickX - fakePlayerWindowX;
                fakePlayerWindowDragOffsetY = clickY - fakePlayerWindowY;
                clickPending = false;
                return true;
            }
            fakePlayerWindowDragging = false;
            clickButton = button;
            clickPending = true;
            return true;
        }
        fakePlayerWindowOpen = false;
        fakePlayerWindowDragging = false;
        clickPending = false;
        return true;
    }

    private boolean selectionWindowTitleContains(double scaledX, double scaledY) {
        return contains(scaledX, scaledY, oreWindowX, oreWindowY, ORE_WINDOW_W, ORE_WINDOW_TITLE_H)
                && !contains(scaledX, scaledY, selectionWindowCloseX(), selectionWindowCloseY(),
                MODAL_CLOSE_SIZE, MODAL_CLOSE_SIZE);
    }

    private boolean fakePlayerWindowTitleContains(double scaledX, double scaledY) {
        return contains(scaledX, scaledY, fakePlayerWindowX, fakePlayerWindowY,
                FAKE_PLAYER_WINDOW_W, FAKE_PLAYER_WINDOW_TITLE_H)
                && !contains(scaledX, scaledY, fakePlayerWindowCloseX(), fakePlayerWindowCloseY(),
                MODAL_CLOSE_SIZE, MODAL_CLOSE_SIZE);
    }

    private int selectionWindowCloseX() {
        return oreWindowX + ORE_WINDOW_W - MODAL_CLOSE_SIZE - MODAL_CLOSE_RIGHT_INSET;
    }

    private int selectionWindowCloseY() {
        return oreWindowY + MODAL_CLOSE_TOP_INSET;
    }

    private int fakePlayerWindowCloseX() {
        return fakePlayerWindowX + FAKE_PLAYER_WINDOW_W - MODAL_CLOSE_SIZE - MODAL_CLOSE_RIGHT_INSET;
    }

    private int fakePlayerWindowCloseY() {
        return fakePlayerWindowY + MODAL_CLOSE_TOP_INSET;
    }



    private int oreWindowContentY() {
        return oreWindowY + ORE_WINDOW_TITLE_H + 8 + ORE_WINDOW_SEARCH_H + 10;
    }

    private int oreWindowContentH(int contentY) {
        return ORE_WINDOW_H - (contentY - oreWindowY) - 14;
    }

    private void commitPreviewDrag() {
        if (previewDragTarget == PreviewDragTarget.NAME) {
            VisualSettings.setMobNameAnchor(previewDragAnchor);
        } else if (previewDragTarget == PreviewDragTarget.DISTANCE) {
            VisualSettings.setMobDistanceAnchor(previewDragAnchor);
        } else if (previewDragTarget == PreviewDragTarget.HEALTH) {
            VisualSettings.setMobHealthAnchor(previewDragAnchor);
        } else if (previewDragTarget == PreviewDragTarget.MAIN_ITEM) {
            VisualSettings.setVisualEspItemAnchor(previewDragMainItem, previewDragAnchor);
        } else if (previewDragTarget == PreviewDragTarget.FLAG) {
            VisualSettings.setVisualEspFlagAnchor(previewDragFlagItem, previewDragAnchor);
        }
        previewDragTarget = PreviewDragTarget.NONE;
        previewDragFlagItem = 0L;
        previewDragMainItem = 0L;
        previewDragGhost.reset();
    }

    private VisualSettings.EspAnchor snapPreviewAnchor(double scaledX, double scaledY) {
        if (previewEspBoxBounds == null) {
            return previewDragAnchor;
        }
        double centerX = previewEspBoxBounds.x() + previewEspBoxBounds.w() * 0.5;
        double centerY = previewEspBoxBounds.y() + previewEspBoxBounds.h() * 0.5;
        double dx = scaledX - centerX;
        double dy = scaledY - centerY;
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx >= 0.0 ? VisualSettings.EspAnchor.RIGHT : VisualSettings.EspAnchor.LEFT;
        }
        return dy >= 0.0 ? VisualSettings.EspAnchor.BOTTOM : VisualSettings.EspAnchor.TOP;
    }

    private static boolean isHorizontalAnchor(VisualSettings.EspAnchor anchor) {
        return anchor == VisualSettings.EspAnchor.TOP || anchor == VisualSettings.EspAnchor.BOTTOM;
    }

    private PreviewBounds previewMouseBounds(int w, int h) {
        int x = Math.round((float) (previewDragLastX - previewDragGrabCenterOffsetX - w * 0.5));
        int y = Math.round((float) (previewDragLastY - previewDragGrabCenterOffsetY - h * 0.5));
        return new PreviewBounds(x, y, w, h);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (listeningVeinMinerKey) {
            VisualSettings.bindVeinMinerMouse(button);
            listeningVeinMinerKey = false;
            return true;
        }
        if (listeningPearlThrowKey) {
            VisualSettings.bindPearlThrowMouse(button);
            listeningPearlThrowKey = false;
            return true;
        }
        if (listeningQuickSwitchKey) {
            VisualSettings.bindQuickSwitchMouse(button);
            listeningQuickSwitchKey = false;
            return true;
        }
        if (listeningAutoSortKey) {
            listeningAutoSortKey = false;
            return true;
        }
        if (dashboardSurface.mouseClickedForHotkey(button)) {
            return true;
        }
        if (button == 0 || button == 1) {
            mouseDown = true;
            clickX = dashboardMouseX(mouseX);
            clickY = dashboardMouseY(mouseY);
            if (handleSelectionWindowMouseClicked(button) || handleFakePlayerWindowMouseClicked(button)) {
                clearHudDragging();
                return true;
            }
            if (dashboardSurface.hasOpenSettingsPopup()) {
                clickButton = button;
                clickPending = true;
                clearHudDragging();
                return true;
            }
            if (button == 1 && hudContains(mouseX, mouseY)) {
                clickButton = button;
                clickPending = true;
                clearHudDragging();
                return true;
            }
            if (button == 0
                    && VisualSettings.hudWidgetEnabled(VisualSettings.HUD_WIDGET_WATERMARK)
                    && WatermarkHudRenderer.containsVisible(mouseX, mouseY)) {
                int element = WatermarkHudRenderer.elementAt(mouseX, mouseY);
                if (element != 0) {
                    pendingWatermarkElementDrag = element;
                    watermarkElementDragStartedAt = System.currentTimeMillis();
                    watermarkElementDragStartX = mouseX;
                    WatermarkHudRenderer.beginDraggedElement(element, mouseX);
                    return true;
                }
                watermarkHudDragging = true;
                watermarkHudDragOffsetX = WatermarkHudRenderer.dragOffsetX(mouseX);
                watermarkHudDragOffsetY = WatermarkHudRenderer.dragOffsetY(mouseY);
                return true;
            }
            if (button == 0 && HotkeyHudRenderer.contains(mouseX, mouseY)) {
                hotkeyHudDragging = true;
                hotkeyHudDragOffsetX = HotkeyHudRenderer.dragOffsetX(mouseX);
                hotkeyHudDragOffsetY = HotkeyHudRenderer.dragOffsetY(mouseY);
                return true;
            }
            if (button == 0 && ArrayListHudRenderer.contains(mouseX, mouseY)) {
                arrayListHudDragging = true;
                arrayListHudDragOffsetX = ArrayListHudRenderer.dragOffsetX(mouseX);
                arrayListHudDragOffsetY = ArrayListHudRenderer.dragOffsetY(mouseY);
                return true;
            }
            if (button == 0 && TargetsHudRenderer.contains(mouseX, mouseY)) {
                targetsHudDragging = true;
                targetsHudDragOffsetX = TargetsHudRenderer.dragOffsetX(mouseX);
                targetsHudDragOffsetY = TargetsHudRenderer.dragOffsetY(mouseY);
                return true;
            }
            if (button == 0 && BackpackHudRenderer.contains(mouseX, mouseY)) {
                backpackHudDragging = true;
                backpackHudDragOffsetX = BackpackHudRenderer.dragOffsetX(mouseX);
                backpackHudDragOffsetY = BackpackHudRenderer.dragOffsetY(mouseY);
                return true;
            }
            if (button == 0 && PotionBuffHudRenderer.contains(mouseX, mouseY)) {
                potionBuffHudDragging = true;
                potionBuffHudDragOffsetX = PotionBuffHudRenderer.dragOffsetX(mouseX);
                potionBuffHudDragOffsetY = PotionBuffHudRenderer.dragOffsetY(mouseY);
                return true;
            }
            if (button == 0 && SpeedHudRenderer.contains(mouseX, mouseY)) {
                speedHudDragging = true;
                speedHudDragOffsetX = SpeedHudRenderer.dragOffsetX(mouseX);
                speedHudDragOffsetY = SpeedHudRenderer.dragOffsetY(mouseY);
                return true;
            }
            if (button == 0 && MusicLyricsHudRenderer.contains(mouseX, mouseY)) {
                musicLyricsHudDragging = true;
                musicLyricsHudDragOffsetX = MusicLyricsHudRenderer.dragOffsetX(mouseX);
                musicLyricsHudDragOffsetY = MusicLyricsHudRenderer.dragOffsetY(mouseY);
                return true;
            }
            if (previewEspPopupOpen || previewEspPopupAlpha > 0.02f) {
                if (dashboardSurface.hasOpenSettingsPopup() && !dashboardSurface.insideAnyPopup(clickX, clickY)) {
                    clickButton = button;
                    clickPending = true;
                    return true;
                }
                if (selectedTab == SidebarTab.VISUAL && previewPopupBlocksPoint(clickX, clickY)) {
                    clickButton = button;
                    clickPending = true;
                } else if (dashboardSurface.insideAnyPopup(clickX, clickY)) {
                    clickButton = button;
                    clickPending = true;
                } else if (selectedTab == SidebarTab.VISUAL && !dashboardSurface.insideAnyPopup(clickX, clickY)) {
                    previewEspPopupOpen = false;
                    clickPending = false;
                } else {
                    clickPending = false;
                }
                return true;
            }
            if (button == 0 && editingConfigName != null
                    && !contains(clickX, clickY, editingConfigNameX, editingConfigNameY,
                    editingConfigNameW, editingConfigNameH)) {
                cancelConfigRename();
            }
            if (button == 0 && startMusicResultScrollbarDrag(clickX, clickY)) {
                clickPending = false;
                return true;
            }
            if (button == 0 && startMusicProgressDrag(clickX, clickY)) {
                clickPending = false;
                return true;
            }
            if (button == 0 && startMusicVolumeDrag(clickX, clickY)) {
                clickPending = false;
                return true;
            }
            if (button == 0 && startContentScrollbarDrag(clickX, clickY)) {
                clickPending = false;
                return true;
            }
            if (button == 0 && dashboardSurface.hasOpenSettingsPopup() && !dashboardSurface.insideAnyPopup(clickX, clickY)) {
                clickButton = button;
                clickPending = true;
                return true;
            }
            if (button == 0 && startPreviewDrag(clickX, clickY)) {
                clickPending = false;
                return true;
            }
            if (button == 0 && startPreviewModelDrag(clickX, clickY)) {
                clickPending = false;
                return true;
            }
            clickButton = button;
            clickPending = true;
            if (clickX >= dashboardX && clickX <= dashboardX + dashboardW && clickY >= dashboardY && clickY <= dashboardY + dashboardH) {
                return true;
            }
        }
        WidgetSurface.globalMousePressed(button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningVeinMinerKey) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                VisualSettings.clearVeinMinerKey();
            } else if (keyCode != GLFW.GLFW_KEY_UNKNOWN) {
                VisualSettings.bindVeinMinerKey(keyCode, scanCode);
            }
            listeningVeinMinerKey = false;
            return true;
        }
        if (listeningAutoSortKey) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                VisualSettings.clearAutoSortKey();
            } else if (keyCode != GLFW.GLFW_KEY_UNKNOWN) {
                VisualSettings.bindAutoSortKey(keyCode, scanCode);
            }
            listeningAutoSortKey = false;
            return true;
        }
        if (listeningPearlThrowKey) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                VisualSettings.clearPearlThrowKey();
            } else if (keyCode != GLFW.GLFW_KEY_UNKNOWN) {
                VisualSettings.bindPearlThrowKey(keyCode, scanCode);
            }
            listeningPearlThrowKey = false;
            return true;
        }
        if (listeningQuickSwitchKey) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                VisualSettings.clearQuickSwitchKey();
            } else if (keyCode != GLFW.GLFW_KEY_UNKNOWN) {
                VisualSettings.bindQuickSwitchKey(keyCode, scanCode);
            }
            listeningQuickSwitchKey = false;
            return true;
        }
        if (com.xerion.forge.event.ForgeModEventBridge.isOpenClickGuiKey(keyCode, scanCode)) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (oreWindowOpen) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                oreWindowOpen = false;
                selectionSearchFocused = false;
                return true;
            }
            if (selectionSearchFocused) {
                normalizeSelectionSearchState();
                boolean control = (modifiers & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SUPER)) != 0;
                boolean selecting = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
                if (control && keyCode == GLFW.GLFW_KEY_A) {
                    selectionSearchAnchor = 0;
                    selectionSearchCursor = selectionSearchText.length();
                    return true;
                }
                if (control && keyCode == GLFW.GLFW_KEY_C) {
                    copySelectionSearchText();
                    return true;
                }
                if (control && keyCode == GLFW.GLFW_KEY_X) {
                    copySelectionSearchText();
                    deleteSelectionSearchSelection();
                    return true;
                }
                if (control && keyCode == GLFW.GLFW_KEY_V) {
                    replaceSelectionSearchText(Minecraft.getInstance().keyboardHandler.getClipboard());
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                    if (!deleteSelectionSearchSelection() && selectionSearchCursor > 0) {
                        int start = control
                                ? WidgetSurface.previousWordIndex(selectionSearchText, selectionSearchCursor)
                                : WidgetSurface.previousCodePointIndex(selectionSearchText, selectionSearchCursor);
                        deleteSelectionSearchRange(start, selectionSearchCursor);
                    }
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_DELETE) {
                    if (!deleteSelectionSearchSelection() && selectionSearchCursor < selectionSearchText.length()) {
                        int end = control
                                ? WidgetSurface.nextWordIndex(selectionSearchText, selectionSearchCursor)
                                : WidgetSurface.nextCodePointIndex(selectionSearchText, selectionSearchCursor);
                        deleteSelectionSearchRange(selectionSearchCursor, end);
                    }
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_LEFT) {
                    int next = !selecting && selectionSearchHasSelection()
                            ? selectionSearchSelectionStart()
                            : control
                            ? WidgetSurface.previousWordIndex(selectionSearchText, selectionSearchCursor)
                            : WidgetSurface.previousCodePointIndex(selectionSearchText, selectionSearchCursor);
                    moveSelectionSearchCursor(next, selecting);
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                    int next = !selecting && selectionSearchHasSelection()
                            ? selectionSearchSelectionEnd()
                            : control
                            ? WidgetSurface.nextWordIndex(selectionSearchText, selectionSearchCursor)
                            : WidgetSurface.nextCodePointIndex(selectionSearchText, selectionSearchCursor);
                    moveSelectionSearchCursor(next, selecting);
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_HOME) {
                    moveSelectionSearchCursor(0, selecting);
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_END) {
                    moveSelectionSearchCursor(selectionSearchText.length(), selecting);
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    selectionSearchFocused = false;
                    return true;
                }
            }
            return true;
        }
        if (fakePlayerWindowOpen) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                fakePlayerWindowOpen = false;
            }
            return true;
        }
        if (editingConfigName != null) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                commitConfigRename();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                cancelConfigRename();
                return true;
            }
        }
        if (dashboardSurface.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (oreWindowOpen) {
            if (selectionSearchFocused && !Character.isISOControl(codePoint)) {
                if (Character.isHighSurrogate(codePoint)) {
                    pendingSelectionSearchHighSurrogate = codePoint;
                } else if (Character.isLowSurrogate(codePoint)) {
                    if (pendingSelectionSearchHighSurrogate != 0) {
                        replaceSelectionSearchText(new String(new char[] {
                                pendingSelectionSearchHighSurrogate, codePoint
                        }));
                        pendingSelectionSearchHighSurrogate = 0;
                    }
                } else {
                    pendingSelectionSearchHighSurrogate = 0;
                    replaceSelectionSearchText(String.valueOf(codePoint));
                }
            }
            return true;
        }
        if (fakePlayerWindowOpen) {
            return true;
        }
        if (dashboardSurface.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (oreWindowOpen) {
            return true;
        }
        if (fakePlayerWindowOpen) {
            return true;
        }
        if (dashboardSurface.keyReleased(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        double scaledMouseX = dashboardMouseX(mouseX);
        double scaledMouseY = dashboardMouseY(mouseY);
        boolean overVolumeButton = contains(scaledMouseX, scaledMouseY,
                musicVolumeButtonX, musicVolumeButtonY, musicVolumeButtonSize, musicVolumeButtonSize);
        boolean overVolumePopup = musicVolumePopupOpen && contains(scaledMouseX, scaledMouseY,
                musicVolumePopupX, musicVolumePopupY, musicVolumePopupW,
                musicVolumeButtonY + musicVolumeButtonSize - musicVolumePopupY);
        if (selectedTab == SidebarTab.MUSIC && (overVolumeButton || overVolumePopup)) {
            NeteaseMusicService.setVolume(NeteaseMusicService.volume() + (float) delta * 0.05f);
            musicVolumePopupOpen = true;
            return true;
        }
        if (dashboardSurface.mouseScrolled(scaledMouseX, scaledMouseY, delta)) {
            return true;
        }
        if (oreWindowOpen) {
            if (contains(scaledMouseX, scaledMouseY, oreWindowX, oreWindowY, ORE_WINDOW_W, ORE_WINDOW_H)) {
                oreWindowScrollY = clamp(oreWindowScrollY - (int) Math.round(delta * SCROLL_STEP),
                        0, oreWindowMaxScrollY);
            }
            return true;
        }
        if (fakePlayerWindowOpen) {
            if (contains(scaledMouseX, scaledMouseY, fakePlayerWindowX, fakePlayerWindowY,
                    FAKE_PLAYER_WINDOW_W, FAKE_PLAYER_WINDOW_H)) {
                fakePlayerWindowScrollY = clamp(fakePlayerWindowScrollY - (int) Math.round(delta * SCROLL_STEP),
                        0, fakePlayerWindowMaxScrollY);
            }
            return true;
        }
        if (selectedTab == SidebarTab.MUSIC
                && contains(scaledMouseX, scaledMouseY,
                musicListX, musicListY, musicListW, musicListH)) {
            musicResultScrollY = clamp(
                    musicResultScrollY - (int) Math.round(delta * SCROLL_STEP),
                    0, musicMaxResultScrollY);
            return true;
        }
        if (sidebarTopViewportContains(scaledMouseX, scaledMouseY)) {
            int previousScrollY = sidebarScrollY;
            sidebarScrollY = clamp(sidebarScrollY - (int) Math.round(delta * SCROLL_STEP),
                    0, maxSidebarScrollY);
            if (sidebarScrollY != previousScrollY) {
                retainedSidebarScrollY = sidebarScrollY;
                dashboardSurface.closePopupsForScroll();
                closeConfigMenu();
                previewEspPopupOpen = false;
            }
            return true;
        }
        if (scaledMouseX >= contentBoundsX
                && scaledMouseX <= contentBoundsX + contentBoundsW
                && scaledMouseY >= contentBoundsY
                && scaledMouseY <= contentBoundsY + contentBoundsH) {
            int previousScrollY = contentScrollY;
            contentScrollY = clamp(contentScrollY - (int) Math.round(delta * SCROLL_STEP), 0, maxContentScrollY);
            rememberScroll(selectedTab);
            if (contentScrollY != previousScrollY) {
                cancelConfigRename();
                dashboardSurface.closePopupsForScroll();
                closeConfigMenu();
                previewEspPopupOpen = false;
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        WindowsImeBridge.uninstall();
        retainedSidebarScrollY = Math.max(0, sidebarScrollY);
        retainedMusicHomeVisible = musicHomeVisible;
        retainedMusicExpandedDetails = musicExpandedDetails;
        rememberScroll(selectedTab);
        listeningVeinMinerKey = false;
        listeningAutoSortKey = false;
        listeningPearlThrowKey = false;
        listeningQuickSwitchKey = false;
        WidgetSurface.releaseActiveHoldHotkeys();
        WidgetSurface.resetHotkeyPressedState();
        WidgetSurface.clearGameMovementInputBlock();
        watermarkHudDragging = false;
        hotkeyHudDragging = false;
        arrayListHudDragging = false;
        targetsHudDragging = false;
        backpackHudDragging = false;
        potionBuffHudDragging = false;
        speedHudDragging = false;
        musicLyricsHudDragging = false;
        HudSnap.clearGuides();
        pendingWatermarkElementDrag = 0;
        WatermarkHudRenderer.clearDraggedElement();
        super.removed();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        double scaledMouseX = dashboardMouseX(mouseX);
        double scaledMouseY = dashboardMouseY(mouseY);
        if (dashboardSurface.hasOpenSettingsPopup()) {
            clearHudDragging();
        }
        if (oreWindowOpen) {
            if (button == 0 && updateOreWindowScrollbarDrag(scaledMouseY)) {
                return true;
            }
            if (button == 0 && oreWindowDragging) {
                oreWindowX = (int) Math.round(scaledMouseX - oreWindowDragOffsetX);
                oreWindowY = (int) Math.round(scaledMouseY - oreWindowDragOffsetY);
                clampOreWindowPosition();
            }
            return true;
        }
        if (fakePlayerWindowOpen) {
            if (button == 0 && updateFakePlayerWindowScrollbarDrag(scaledMouseY)) {
                return true;
            }
            if (button == 0 && fakePlayerWindowDragging) {
                fakePlayerWindowX = (int) Math.round(scaledMouseX - fakePlayerWindowDragOffsetX);
                fakePlayerWindowY = (int) Math.round(scaledMouseY - fakePlayerWindowDragOffsetY);
                clampFakePlayerWindowPosition();
            }
            return true;
        }
        if (button == 0 && updateMusicProgressDrag(scaledMouseX)) {
            return true;
        }
        if (button == 0 && updateMusicVolumeDrag(scaledMouseY)) {
            return true;
        }
        if (button == 0 && updateMusicResultScrollbarDrag(scaledMouseY)) {
            return true;
        }
        if (button == 0 && updateContentScrollbarDrag(scaledMouseY)) {
            return true;
        }
        if (oreWindowDragging && button == 0) {
            oreWindowX = (int) Math.round(scaledMouseX - oreWindowDragOffsetX);
            oreWindowY = (int) Math.round(scaledMouseY - oreWindowDragOffsetY);
            clampOreWindowPosition();
            return true;
        }
        if (button == 0 && updatePreviewDrag(scaledMouseX, scaledMouseY)) {
            return true;
        }
        if (previewModelDragging && button == 0) {
            previewModelYaw += (float) (dragX * guiScale / dashboardScale * 0.035f);
            return true;
        }
        if (pendingWatermarkElementDrag != 0 && button == 0) {
            long heldMs = System.currentTimeMillis() - watermarkElementDragStartedAt;
            if (heldMs >= 220L || Math.abs(mouseX - watermarkElementDragStartX) >= 4.0) {
                WatermarkHudRenderer.reorderElementAt(pendingWatermarkElementDrag, mouseX);
            }
            return true;
        }
        if (hotkeyHudDragging && button == 0) {
            HotkeyHudRenderer.moveTo(mouseX, mouseY, hotkeyHudDragOffsetX, hotkeyHudDragOffsetY);
            return true;
        }
        if (arrayListHudDragging && button == 0) {
            ArrayListHudRenderer.moveTo(mouseX, mouseY, arrayListHudDragOffsetX, arrayListHudDragOffsetY);
            return true;
        }
        if (targetsHudDragging && button == 0) {
            TargetsHudRenderer.moveTo(mouseX, mouseY, targetsHudDragOffsetX, targetsHudDragOffsetY);
            return true;
        }
        if (backpackHudDragging && button == 0) {
            BackpackHudRenderer.moveTo(mouseX, mouseY, backpackHudDragOffsetX, backpackHudDragOffsetY);
            return true;
        }
        if (potionBuffHudDragging && button == 0) {
            PotionBuffHudRenderer.moveTo(mouseX, mouseY, potionBuffHudDragOffsetX, potionBuffHudDragOffsetY);
            return true;
        }
        if (speedHudDragging && button == 0) {
            SpeedHudRenderer.moveTo(mouseX, mouseY, speedHudDragOffsetX, speedHudDragOffsetY);
            return true;
        }
        if (musicLyricsHudDragging && button == 0) {
            MusicLyricsHudRenderer.moveTo(
                    mouseX, mouseY, musicLyricsHudDragOffsetX, musicLyricsHudDragOffsetY);
            return true;
        }
        if (watermarkHudDragging && button == 0) {
            WatermarkHudRenderer.moveTo(mouseX, mouseY, watermarkHudDragOffsetX, watermarkHudDragOffsetY);
            return true;
        }
        if (dragging && button == 0) {
            dashboardX = (int) Math.round(scaledMouseX - dragOffsetX);
            dashboardY = (int) Math.round(scaledMouseY - dragOffsetY);
            clampDashboardPosition();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        WidgetSurface.globalMouseReleased(button);
        if (button == 0) {
            commitMusicProgressDrag();
            musicVolumeDragging = false;
            if (previewDragTarget != PreviewDragTarget.NONE) {
                commitPreviewDrag();
            }
            if (oreWindowOpen || oreWindowAlpha > 0.02f
                    || fakePlayerWindowOpen || fakePlayerWindowAlpha > 0.02f) {
                mouseDown = false;
                dragging = false;
                oreWindowDragging = false;
                fakePlayerWindowDragging = false;
                oreWindowScrollbarDrag.stop();
                fakePlayerWindowScrollbarDrag.stop();
                musicResultScrollbarDrag.stop();
                contentScrollbarDrag.stop();
                hotkeyHudDragging = false;
                arrayListHudDragging = false;
                targetsHudDragging = false;
                backpackHudDragging = false;
                potionBuffHudDragging = false;
                speedHudDragging = false;
                musicLyricsHudDragging = false;
                watermarkHudDragging = false;
                HudSnap.clearGuides();
                pendingWatermarkElementDrag = 0;
                WatermarkHudRenderer.clearDraggedElement();
                previewModelDragging = false;
                previewDragGhost.reset();
                return true;
            }
            mouseDown = false;
            dragging = false;
            oreWindowDragging = false;
            fakePlayerWindowDragging = false;
            oreWindowScrollbarDrag.stop();
            fakePlayerWindowScrollbarDrag.stop();
            musicResultScrollbarDrag.stop();
            contentScrollbarDrag.stop();
            hotkeyHudDragging = false;
            arrayListHudDragging = false;
            targetsHudDragging = false;
            backpackHudDragging = false;
            potionBuffHudDragging = false;
            speedHudDragging = false;
            musicLyricsHudDragging = false;
            watermarkHudDragging = false;
            HudSnap.clearGuides();
            pendingWatermarkElementDrag = 0;
            WatermarkHudRenderer.clearDraggedElement();
            previewModelDragging = false;
            previewDragGhost.reset();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isDashboardBlankDragArea(double mouseX, double mouseY) {
        if (!contains(mouseX, mouseY, dashboardX, dashboardY, dashboardW, dashboardH)) {
            return false;
        }
        if (dashboardSurface.insideAnyPopup(mouseX, mouseY)) {
            return false;
        }
        if (WatermarkHudRenderer.contains(toGuiX(mouseX), toGuiY(mouseY))) {
            return false;
        }
        if (HotkeyHudRenderer.contains(toGuiX(mouseX), toGuiY(mouseY))) {
            return false;
        }
        if (ArrayListHudRenderer.contains(toGuiX(mouseX), toGuiY(mouseY))) {
            return false;
        }
        if (TargetsHudRenderer.contains(toGuiX(mouseX), toGuiY(mouseY))) {
            return false;
        }
        if (BackpackHudRenderer.contains(toGuiX(mouseX), toGuiY(mouseY))) {
            return false;
        }
        if (PotionBuffHudRenderer.contains(toGuiX(mouseX), toGuiY(mouseY))) {
            return false;
        }
        if (SpeedHudRenderer.contains(toGuiX(mouseX), toGuiY(mouseY))) {
            return false;
        }
        return !MusicLyricsHudRenderer.contains(toGuiX(mouseX), toGuiY(mouseY));
    }

    private void clampDashboardPosition() {
        Window window = Minecraft.getInstance().getWindow();
        int visibleMargin = 40;
        double originX = window.getWidth() * 0.5;
        double originY = window.getHeight() * 0.5;
        double minX = originX + (visibleMargin - dashboardW * dashboardScale - originX) / dashboardScale;
        double maxX = originX + (window.getWidth() - visibleMargin - originX) / dashboardScale;
        double minY = originY + (visibleMargin - dashboardH * dashboardScale - originY) / dashboardScale;
        double maxY = originY + (window.getHeight() - visibleMargin - originY) / dashboardScale;
        dashboardX = (int) Math.round(Math.max(minX, Math.min(maxX, dashboardX)));
        dashboardY = (int) Math.round(Math.max(minY, Math.min(maxY, dashboardY)));
    }

    private void clampOreWindowPosition() {
        Window window = Minecraft.getInstance().getWindow();
        int visibleMargin = 34;
        oreWindowX = Math.max(visibleMargin - ORE_WINDOW_W, Math.min(window.getWidth() - visibleMargin, oreWindowX));
        oreWindowY = Math.max(visibleMargin - ORE_WINDOW_TITLE_H, Math.min(window.getHeight() - visibleMargin, oreWindowY));
    }

    private void clampFakePlayerWindowPosition() {
        Window window = Minecraft.getInstance().getWindow();
        int visibleMargin = 34;
        fakePlayerWindowX = Math.max(visibleMargin - FAKE_PLAYER_WINDOW_W,
                Math.min(window.getWidth() - visibleMargin, fakePlayerWindowX));
        fakePlayerWindowY = Math.max(visibleMargin - FAKE_PLAYER_WINDOW_TITLE_H,
                Math.min(window.getHeight() - visibleMargin, fakePlayerWindowY));
    }

    private void translatePreviewAnimations(int deltaX, int deltaY) {
        if (deltaX == 0 && deltaY == 0) {
            return;
        }
        for (AnimatedPreviewBounds animation : previewComponentAnimations.values()) {
            animation.translate(deltaX, deltaY);
        }
        for (AnimatedPreviewBounds animation : previewMainItemAnimations.values()) {
            animation.translate(deltaX, deltaY);
        }
        for (AnimatedPreviewBounds animation : previewFlagAnimations.values()) {
            animation.translate(deltaX, deltaY);
        }
    }

    private static int clamp(int value, int min, int max) {
        return UiMath.clamp(value, min, max);
    }

    private static float clampFloat(float value, float min, float max) {
        return UiMath.clamp(value, min, max);
    }

    private static float lerp(float from, float to, float speed) {
        return AnimationClock.lerp(from, to, speed);
    }

    private static float smoothstep(float value) {
        return UiMath.smoothstep(value);
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        // Floating window only.
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}





