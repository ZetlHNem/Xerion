package com.xerion.forge.config;

import com.xerion.common.config.ConfigValueCodec;
import com.xerion.common.config.SerializedSettingValues;
import com.xerion.common.config.VisualDefaults;
import com.xerion.common.config.VisualEspItems;
import com.xerion.common.config.VisualRanges;
import com.xerion.common.config.WatermarkElementOrder;
import com.xerion.common.config.WatermarkElements;

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

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import org.lwjgl.glfw.GLFW;

public final class VisualSettings {
    public enum EspAnchor {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM
    }

    public static final int ORE_DIAMOND = 1 << 0;
    public static final int ORE_EMERALD = 1 << 1;
    public static final int ORE_GOLD = 1 << 2;
    public static final int ORE_IRON = 1 << 3;
    public static final int ORE_REDSTONE = 1 << 4;
    public static final int ORE_LAPIS = 1 << 5;
    public static final int ORE_COAL = 1 << 6;
    public static final int ORE_COPPER = 1 << 7;
    public static final int ORE_QUARTZ = 1 << 8;
    public static final int ORE_DEBRIS = 1 << 9;
    public static final int ORE_OTHER = 1 << 10;
    public static final int ORE_ALL = ORE_DIAMOND
            | ORE_EMERALD
            | ORE_GOLD
            | ORE_IRON
            | ORE_REDSTONE
            | ORE_LAPIS
            | ORE_COAL
            | ORE_COPPER
            | ORE_QUARTZ
            | ORE_DEBRIS
            | ORE_OTHER;
    public static final int OFFSCREEN_ARROW_NAME = 1 << 0;
    public static final int OFFSCREEN_ARROW_DISTANCE = 1 << 1;
    public static final int OFFSCREEN_ARROW_ALL = OFFSCREEN_ARROW_NAME
            | OFFSCREEN_ARROW_DISTANCE;
    public static final String ESP_FLAG_TEXT_FULL = "Full";
    public static final String ESP_FLAG_TEXT_SHORT = "Short";
    public static final int LOCAL_TRANSPARENCY_DISABLE_FIRST_PERSON = 1;
    public static final int LOCAL_TRANSPARENCY_DISABLE_HOLDING_ITEM = 1 << 1;
    public static final int LOCAL_TRANSPARENCY_DISABLE_USING_ITEM = 1 << 2;
    public static final int LOCAL_TRANSPARENCY_DISABLE_SNEAKING = 1 << 3;
    public static final int LOCAL_TRANSPARENCY_DISABLE_SWIMMING = 1 << 4;
    public static final int VISUAL_ENEMY_TARGET_PLAYERS = 1;
    public static final int VISUAL_ENEMY_TARGET_HOSTILE_MOBS = 1 << 1;
    public static final int VISUAL_ENEMY_TARGET_PASSIVE_MOBS = 1 << 2;
    public static final int VISUAL_ENEMY_TARGET_OTHER_LIVING = 1 << 3;
    public static final int VISUAL_ENEMY_TARGET_ALL = VISUAL_ENEMY_TARGET_PLAYERS
            | VISUAL_ENEMY_TARGET_HOSTILE_MOBS
            | VISUAL_ENEMY_TARGET_PASSIVE_MOBS
            | VISUAL_ENEMY_TARGET_OTHER_LIVING;
    public static final int NUKER_EXTENSION_SWING_HAND = 1;
    public static final int NUKER_EXTENSION_PACKET_MINE = 1 << 1;
    public static final int NUKER_EXTENSION_ROTATE = 1 << 2;
    public static final int NUKER_EXTENSION_SKIP_FEET_LEVEL = 1 << 3;
    public static final int VEIN_MINER_FILTER_ORES = 1;
    public static final int VEIN_MINER_FILTER_TREES = 1 << 1;
    public static final int VEIN_MINER_FILTER_ALL = VEIN_MINER_FILTER_ORES
            | VEIN_MINER_FILTER_TREES;
    public static final int SOUND_CONTROL_MASTER = 1;
    public static final int SOUND_CONTROL_MUSIC = 1 << 1;
    public static final int SOUND_CONTROL_RECORDS = 1 << 2;
    public static final int SOUND_CONTROL_WEATHER = 1 << 3;
    public static final int SOUND_CONTROL_BLOCKS = 1 << 4;
    public static final int SOUND_CONTROL_HOSTILE = 1 << 5;
    public static final int SOUND_CONTROL_NEUTRAL = 1 << 6;
    public static final int SOUND_CONTROL_PLAYERS = 1 << 7;
    public static final int SOUND_CONTROL_AMBIENT = 1 << 8;
    public static final int SOUND_CONTROL_VOICE = 1 << 9;
    public static final int SOUND_CONTROL_ALL = SOUND_CONTROL_MASTER
            | SOUND_CONTROL_MUSIC
            | SOUND_CONTROL_RECORDS
            | SOUND_CONTROL_WEATHER
            | SOUND_CONTROL_BLOCKS
            | SOUND_CONTROL_HOSTILE
            | SOUND_CONTROL_NEUTRAL
            | SOUND_CONTROL_PLAYERS
            | SOUND_CONTROL_AMBIENT
            | SOUND_CONTROL_VOICE;
    public static final int HUD_WIDGET_WATERMARK = 1;
    public static final int HUD_WIDGET_HOTKEYS = 1 << 1;
    public static final int HUD_WIDGET_ARRAY_LIST = 1 << 2;
    public static final int HUD_WIDGET_TARGETS = 1 << 3;
    public static final int HUD_WIDGET_BACKPACK = 1 << 4;
    public static final int HUD_WIDGET_POTION_BUFF = 1 << 5;
    private static final int HUD_WIDGET_LEGACY_ARMOR = 1 << 6;
    public static final int HUD_WIDGET_COORDS = 1 << 7;
    public static final int HUD_WIDGET_SPEED = 1 << 8;
    private static final int HUD_WIDGET_LEGACY_BPS = 1 << 9;
    public static final int HUD_WIDGET_ALL = HUD_WIDGET_WATERMARK
            | HUD_WIDGET_HOTKEYS
            | HUD_WIDGET_ARRAY_LIST
            | HUD_WIDGET_TARGETS
            | HUD_WIDGET_BACKPACK
            | HUD_WIDGET_POTION_BUFF
            | HUD_WIDGET_SPEED;
    public static final int WATERMARK_ELEMENT_LOGO = WatermarkElements.LOGO;
    public static final int WATERMARK_ELEMENT_PLAYER = WatermarkElements.PLAYER;
    public static final int WATERMARK_ELEMENT_CONFIG = WatermarkElements.CONFIG;
    public static final int WATERMARK_ELEMENT_PING = WatermarkElements.PING;
    public static final int WATERMARK_ELEMENT_FPS = WatermarkElements.FPS;
    public static final int WATERMARK_ELEMENT_SERVER = WatermarkElements.SERVER;
    public static final int WATERMARK_ELEMENT_TIME = WatermarkElements.TIME;
    public static final int WATERMARK_ELEMENT_ALL = WatermarkElements.ALL;
    public static final long ESP_ITEM_ARMOR_BAR = VisualEspItems.ARMOR_BAR;
    public static final long ESP_ITEM_ABSORPTION = VisualEspItems.ABSORPTION;
    public static final long ESP_ITEM_HURT_TIME = VisualEspItems.HURT_TIME;
    public static final long ESP_ITEM_SNEAKING = VisualEspItems.SNEAKING;
    public static final long ESP_ITEM_SPRINTING = VisualEspItems.SPRINTING;
    public static final long ESP_ITEM_INVISIBLE = VisualEspItems.INVISIBLE;
    public static final long ESP_ITEM_BURNING = VisualEspItems.BURNING;
    public static final long ESP_ITEM_BLOCKING = VisualEspItems.BLOCKING;
    public static final long ESP_ITEM_USING_ITEM = VisualEspItems.USING_ITEM;
    public static final long ESP_ITEM_EATING = VisualEspItems.EATING;
    public static final long ESP_ITEM_DRINKING = VisualEspItems.DRINKING;
    public static final long ESP_ITEM_BOW_CHARGING = VisualEspItems.BOW_CHARGING;
    public static final long ESP_ITEM_CROSSBOW_CHARGED = VisualEspItems.CROSSBOW_CHARGED;
    public static final long ESP_ITEM_ELYTRA = VisualEspItems.ELYTRA;
    public static final long ESP_ITEM_SWIMMING = VisualEspItems.SWIMMING;
    public static final long ESP_ITEM_GLOWING = VisualEspItems.GLOWING;
    public static final long ESP_ITEM_POTION = VisualEspItems.POTION;
    public static final long ESP_ITEM_LOW_HEALTH = VisualEspItems.LOW_HEALTH;
    public static final long ESP_ITEM_BOT = VisualEspItems.BOT;
    public static final long ESP_ITEM_FRIEND_TEAM = VisualEspItems.FRIEND_TEAM;
    public static final long ESP_ITEM_HELD_ITEM_TEXT = 1L << 21;
    public static final long ESP_ITEM_HELD_ITEM_ICON = 1L << 22;
    public static final long ESP_ITEM_ARMOR_TEXT = 1L << 23;
    public static final long ESP_ITEM_ARMOR_ICONS = 1L << 24;
    public static final long ESP_ITEM_DURABILITY = 1L << 25;
    public static final long ESP_ITEM_ENCHANTS = 1L << 26;
    public static final long ESP_ITEM_TOTEM = 1L << 27;
    public static final long ESP_ITEM_SHIELD = 1L << 28;
    public static final long ESP_ITEM_BOW_CROSSBOW = 1L << 29;
    public static final long ESP_ITEM_TARGET = VisualEspItems.TARGET;
    public static final long ESP_ITEM_THREAT = VisualEspItems.THREAT;
    public static final long ESP_ITEM_ARMOR_VALUE = 1L << 32;
    public static final long ESP_ITEM_PING = 1L << 33;
    public static final long ESP_ITEM_GAMEMODE = 1L << 34;
    public static final long ESP_ITEM_HURT_INDICATOR = 1L << 35;
    public static final long ESP_ITEM_DISTANCE_COLOR = VisualEspItems.DISTANCE_COLOR;

    private static boolean mobBoxEnabled;
    private static float mobBoxRange = 96.0f;
    private static int mobBoxColor = 0xF26E72FF;
    private static String mobBoxStyle = "Normal";
    private static boolean mobBoxShowInvisible;
    private static boolean mobBoxThroughWalls = true;
    private static boolean mobHealthEnabled;
    private static float mobHealthRange = 96.0f;
    private static String mobHealthStyle = "Normal";
    private static String mobHealthColorMode = "Dynamic";
    private static int mobHealthColor = 0xF218C783;
    private static boolean mobHealthNumber;
    private static boolean mobHealthShowInvisible;
    private static boolean mobHealthThroughWalls = true;
    private static EspAnchor mobHealthAnchor = EspAnchor.LEFT;
    private static float mobHealthOffsetX;
    private static float mobHealthOffsetY;
    private static boolean mobNameEnabled;
    private static float mobNameRange = 96.0f;
    private static int mobNameColor = 0xF2FFFFFF;
    private static float mobNameScale = 1.0f;
    private static String mobNameStyle = "Normal";
    private static boolean mobNameDistance;
    private static boolean mobNameShowInvisible;
    private static boolean mobNameThroughWalls = true;
    private static EspAnchor mobNameAnchor = EspAnchor.TOP;
    private static float mobNameOffsetX;
    private static float mobNameOffsetY;
    private static int mobDistanceColor = 0xF2FFFFFF;
    private static float mobDistanceScale = 1.0f;
    private static EspAnchor mobDistanceAnchor = EspAnchor.TOP;
    private static float mobDistanceOffsetX;
    private static float mobDistanceOffsetY;
    private static boolean visualEspEnabled = true;
    private static float visualEnemyRange = 96.0f;
    private static boolean visualEnemiesPlayers = true;
    private static boolean visualEnemiesHostileMobs = true;
    private static boolean visualEnemiesPassiveMobs;
    private static boolean visualEnemiesOtherLiving;
    private static boolean visualEnemiesIgnoreBots = true;
    private static boolean visualEnemiesShowInvisible;
    private static boolean visualEnemiesThroughWalls = true;
    private static boolean visualOffscreenArrow;
    private static float visualOffscreenArrowSize = 12.0f;
    private static float visualOffscreenArrowRadius = 96.0f;
    private static int visualOffscreenArrowDisplayMask = OFFSCREEN_ARROW_ALL;
    private static int visualOffscreenArrowColor = 0xF2FFFFFF;
    private static boolean visualSounds;
    private static int visualSoundsColor = 0xF2FF4F8B;
    private static boolean visualPlayerChams;
    private static String visualPlayerChamsMode = "Shaded";
    private static boolean visualBehindWallsChams;
    private static String visualBehindWallsChamsMode = "Shaded";
    private static boolean visualOnHitChams;
    private static String visualOnHitChamsMode = "Shaded";
    private static boolean visualRagdollsChams;
    private static String visualRagdollsChamsMode = "Shaded";
    private static boolean visualSoulParticles;
    private static String visualSoulParticlesMode = "Shaded";
    private static boolean visualGlow;
    private static int visualPlayerChamsColor = 0xF26E72FF;
    private static int visualBehindWallsChamsColor = 0xF24FE8F2;
    private static int visualOnHitChamsColor = 0xF2FF4F8B;
    private static int visualRagdollsChamsColor = 0xF2B9B7D9;
    private static int visualSoulParticlesColor = 0xF25B6CFF;
    private static int visualGlowColor = 0xF2D6D2FF;
    private static boolean visualTeamEnabled;
    private static float visualTeamRange = 96.0f;
    private static boolean visualTeamShowInvisible;
    private static boolean visualTeamThroughWalls = true;
    private static boolean visualTeamPlayerChams;
    private static String visualTeamPlayerChamsMode = "Shaded";
    private static int visualTeamPlayerChamsColor = 0xF26E72FF;
    private static boolean visualTeamBehindWallsChams;
    private static String visualTeamBehindWallsChamsMode = "Shaded";
    private static int visualTeamBehindWallsChamsColor = 0xF24FE8F2;
    private static boolean visualTeamGlow;
    private static int visualTeamGlowColor = 0xF2D6D2FF;
    private static boolean visualTeamDisableModelRendering;
    private static boolean visualTeamMobBox;
    private static boolean visualTeamMobHealth;
    private static boolean visualTeamMobName;
    private static boolean visualTeamMobDistance;
    private static long visualEnemyEspItemMask;
    private static long visualTeamEspItemMask;
    private static String visualEspItemColors = "";
    private static String visualEspItemColorModes = "";
    private static String visualEspItemNumbers = "";
    private static String visualEspItemAnchors = "";
    private static String visualEspFlagTextModes = "";
    private static String visualEspFlagAnchors = "";
    private static float visualHurtTimeDisplayTicks = 10.0f;
    private static boolean visualLocalEnabled;
    private static float visualLocalRange = 96.0f;
    private static boolean visualLocalShowInvisible = true;
    private static boolean visualLocalThroughWalls = true;
    private static boolean visualLocalPlayerChams;
    private static String visualLocalPlayerChamsMode = "Shaded";
    private static int visualLocalPlayerChamsColor = 0xF26E72FF;
    private static boolean visualLocalTransparencyEnabled;
    private static float visualLocalTransparency = 0.65f;
    private static int visualLocalTransparencyDisableMask = LOCAL_TRANSPARENCY_DISABLE_FIRST_PERSON
            | LOCAL_TRANSPARENCY_DISABLE_USING_ITEM;
    private static boolean visualLocalItem;
    private static String visualLocalItemMode = "Shaded";
    private static int visualLocalItemColor = 0xF2FFB84D;
    private static boolean visualLocalArms;
    private static String visualLocalArmsMode = "Shaded";
    private static int visualLocalArmsColor = 0xF26E72FF;
    private static boolean visualLocalGlow;
    private static int visualLocalGlowColor = 0xF2D6D2FF;
    private static boolean visualLocalMobBox;
    private static boolean visualLocalMobHealth;
    private static boolean visualLocalMobName;
    private static boolean visualLocalMobDistance;
    private static long visualLocalEspItemMask;
    private static boolean oreGlowEnabled;
    private static float oreGlowRange = 48.0f;
    private static float oreGlowOpacity = 0.32f;
    private static boolean oreGlowOutline = true;
    private static boolean oreGlowThroughWalls = true;
    private static boolean oreGlowNames = true;
    private static int oreGlowOreMask;
    private static String oreGlowModOreIds = "";
    private static boolean xrayEnabled;
    private static float xrayOpacity = 0.42f;
    private static boolean xrayExposedOnly;
    private static String xrayBlockIds = "";
    private static boolean chestGlowEnabled;
    private static float chestGlowRange = 48.0f;
    private static float chestGlowOpacity = 0.28f;
    private static boolean chestGlowOutline = true;
    private static boolean chestGlowThroughWalls = true;
    private static boolean chestGlowStorageBlocks = true;
    private static int chestGlowColor = 0xF2FFB84D;
    private static boolean spawnerEspEnabled;
    private static float spawnerEspRange = 48.0f;
    private static boolean spawnerEspShowType = true;
    private static boolean spawnerEspThroughWalls = true;
    private static int spawnerEspColor = 0xF2B85CFF;
    private static boolean bedEspEnabled;
    private static float bedEspRange = 36.0f;
    private static boolean bedEspThroughWalls = true;
    private static int bedEspColor = 0xF2FF5C7A;
    private static boolean blockOverlayEnabled;
    private static boolean blockOverlaySingleSide;
    private static String blockOverlayShapeMode = "Both";
    private static int blockOverlaySideColor = 0x336E72FF;
    private static int blockOverlayLineColor = 0xF26E72FF;
    private static boolean barrierBlocksEnabled;
    private static float barrierBlocksRange = 40.0f;
    private static boolean barrierBlocksThroughWalls = true;
    private static int barrierBlocksColor = 0xF2FF4F8B;
    private static boolean lightBlocksEnabled;
    private static float lightBlocksRange = 40.0f;
    private static boolean lightBlocksShowLevel = true;
    private static boolean lightBlocksAllGlowing;
    private static boolean lightBlocksGradientFace;
    private static int lightBlocksColor = 0xF2FFD166;
    private static boolean fullbrightEnabled;
    private static String fullbrightMode = "Gamma";
    private static float fullbrightStrength = 1.0f;
    private static boolean noFogEnabled;
    private static boolean noFogWater = true;
    private static boolean noFogLava = true;
    private static boolean noFogDistance = true;
    private static boolean noFogPowderSnow = true;
    private static boolean noWeatherEnabled;
    private static boolean noWeatherRain = true;
    private static boolean noWeatherSnow = true;
    private static boolean noWeatherThunder = true;
    private static boolean timeChangerEnabled;
    private static String timeChangerTime = "Day";
    private static boolean ambienceEnabled;
    private static boolean ambiencePumpkinOverlay = true;
    private static boolean ambiencePortalOverlay = true;
    private static boolean ambienceDarknessPulse = true;
    private static boolean antiBlindnessEnabled;
    private static boolean antiBlindnessBlindness = true;
    private static boolean antiBlindnessDarkness = true;
    private static boolean antiBlindnessNausea = true;
    private static boolean freecamEnabled;
    private static float freecamSpeed = 1.8f;
    private static boolean freecamNoClip = true;
    private static boolean freecamFreezePlayer = true;
    private static boolean autoToolEnabled;
    private static String autoToolMode = "Best";
    private static boolean autoToolSilent;
    private static boolean autoToolPreferDurability;
    private static boolean autoMineEnabled;
    private static String autoMineTarget = "Crosshair";
    private static float autoMineRange = 4.5f;
    private static float autoMineDelay = 1.0f;
    private static boolean autoMineSwing = true;
    private static boolean nukerEnabled;
    private static String nukerTarget = "Visible";
    private static String nukerSortMode = "Closest";
    private static String nukerBlockIds = "";
    private static float nukerRange = 3.0f;
    private static float nukerDelay = 0.0f;
    private static int nukerExtensionsMask = NUKER_EXTENSION_SWING_HAND;
    private static boolean veinMinerEnabled;
    private static String veinMinerMode = "Chain";
    private static String veinMinerFormat = "Connected";
    private static float veinMinerMaxBlocks = 32.0f;
    private static int veinMinerFilterMask = VEIN_MINER_FILTER_ORES;
    private static boolean veinMinerOresOnly = true;
    private static boolean veinMinerRotate = true;
    private static int veinMinerColor = 0xF2FF4545;
    private static int veinMinerKeyCode = GLFW.GLFW_KEY_UNKNOWN;
    private static int veinMinerScanCode;
    private static int veinMinerMouseButton = -1;
    private static boolean zoomEnabled;
    private static float zoomFactor = 4.0f;
    private static boolean zoomSmooth = true;
    private static boolean zoomCinematic;
    private static boolean zoomShowHands = true;
    private static boolean aspectRatioEnabled;
    private static String aspectRatioPreset = "16:9";
    private static float aspectRatioCustomValue = 16.0f / 9.0f;
    private static boolean viewModelEnabled;
    private static float viewModelScale = 1.0f;
    private static float viewModelOffsetX;
    private static float viewModelOffsetY;
    private static float viewModelOffsetZ;
    private static boolean quickStopEnabled;
    private static boolean bhopTurnEnabled;
    private static boolean noFallEnabled;
    private static String noFallMode = "Spoof";
    private static boolean noFallNoGround;
    private static boolean fastLadderEnabled;
    private static boolean noSlowEnabled;
    private static String noSlowMode = "Vanilla";
    private static boolean noSlowSlowDown;
    private static boolean noSlowKeepSprinting = true;
    private static boolean safeWalkEnabled;
    private static String safeWalkMode = "Normal";
    private static boolean edgeJumpEnabled;
    private static boolean inventoryMoveEnabled;
    private static String inventoryMoveMode = "Normal";
    private static boolean quickSwitchEnabled;
    private static int quickSwitchKeyCode = GLFW.GLFW_KEY_R;
    private static int quickSwitchScanCode;
    private static int quickSwitchMouseButton = -1;
    private static float quickSwitchSlot = 9.0f;
    private static String quickSwitchMode = "Hold";
    private static boolean quickSwitchRestoreSlot = true;
    private static boolean fakePlayerEnabled;
    private static String fakePlayerName = "Dummy";
    private static float fakePlayerHealth = 20.0f;
    private static boolean fakePlayerFacePlayer = true;
    private static boolean fakePlayerHitFeedback = true;
    private static boolean fakePlayerReturnHome = true;
    private static boolean soundControlEnabled;
    private static int soundControlMuteMask;
    private static boolean hitSoundsEnabled;
    private static String hitSoundsMode = "Orb";
    private static float hitSoundsVolume = 0.75f;
    private static float hitSoundsPitch = 1.0f;
    private static boolean betterChatEnabled;
    private static String dashboardGuiScale = "100%";
    private static int betterChatBackgroundColor = 0xCC171B24;
    private static boolean betterChatHideVanillaBackground = true;
    private static boolean betterChatCompact;
    private static boolean pearlThrowEnabled;
    private static int pearlThrowKeyCode = GLFW.GLFW_KEY_UNKNOWN;
    private static int pearlThrowScanCode;
    private static int pearlThrowMouseButton = GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
    private static float pearlThrowDelay = 2.0f;
    private static String pearlThrowMode = "Silent";
    private static float pearlThrowSwitchDelay = 2.0f;
    private static boolean pearlThrowSwing = true;
    private static boolean fastPlaceEnabled;
    private static boolean antiHungerEnabled;
    private static boolean autoRespawnEnabled;
    private static boolean keepSprintEnabled;
    private static float keepSprintScale = 1.0f;
    private static float keepSprintSlowdown;
    private static boolean keepSprintGroundOnly;
    private static boolean keepSprintPrediction;
    private static boolean keepSprintReachOnly;
    private static boolean speedEnabled;
    private static String speedMode = "Strafe";
    private static float speedGroundMultiplier = 1.0f;
    private static float speedAirMultiplier = 1.0f;
    private static boolean speedAutoJump = true;
    private static boolean speedOnlySprint = true;
    private static boolean flightEnabled;
    private static String flightMode = "Vanilla";
    private static float flightCubeCraftSpeed = 3.5F;
    private static float flightCubeCraftVertical = 0.7F;
    private static float flightCubeCraftAirWalkSpeed = 0.34F;
    private static boolean flightCubeCraftConstantSpeed;
    private static int flightCubeCraftReboostTicks = 30;
    private static boolean flightCubeCraftBoostOnce;
    private static boolean flightCubeCraftNostalgia;
    private static float speedCubeCraftFastSpeed = 1.0F;
    private static boolean speedCubeCraftLagBackCheck = true;
    private static boolean speedCubeCraftLowHopGlide;
    private static boolean timerEnabled;
    private static float timerSpeed = 2.0F;
    private static boolean movementFixEnabled;
    private static String movementFixMode = "Normal";
    private static boolean movementFixPacketOnly;
    private static boolean fastWebEnabled;
    private static boolean antiStaffEnabled;
    private static String antiStaffCommand = "hub";
    private static boolean autoGGEnabled;
    private static String autoGGMessage = "gg";
    private static boolean autoLeaveOnBanEnabled;
    private static boolean fastBreakEnabled;
    private static int fastBreakSpeedPercent = 20;
    private static boolean fastBreakCooldownEnabled = true;
    private static int fastBreakCooldown;
    private static boolean breakerEnabled;
    private static float breakerRange = 4.5F;
    private static boolean breakerBreakSurroundings = true;
    private static String breakerSwingMode = "Client";
    private static float flightHorizontalSpeed = 8.0f;
    private static float flightVerticalSpeed = 4.0f;
    private static float flightGlide;
    private static boolean elytraFlyEnabled;
    private static String elytraFlyMode = "Control";
    private static float elytraFlyHorizontalSpeed = 18.0f;
    private static float elytraFlyVerticalSpeed = 6.0f;
    private static float elytraFlyGlide = 0.1f;
    private static boolean elytraFlyAutoStart = true;
    private static boolean boatFlyEnabled;
    private static String boatFlyMode = "Control";
    private static float boatFlyHorizontalSpeed = 10.0f;
    private static float boatFlyVerticalSpeed = 5.0f;
    private static float boatFlyGlide = 0.0f;
    private static boolean entitySpeedEnabled;
    private static float entitySpeed = 8.0f;
    private static boolean entitySpeedKeepY = true;
    private static boolean antiVoidEnabled;
    private static String antiVoidMode = "Setback";
    private static float antiVoidFallDistance = 6.0f;
    private static boolean freezeEnabled;
    private static boolean stepEnabled;
    private static float stepHeight = 1.0f;
    private static boolean stepOnlyGround = true;
    private static boolean longJumpEnabled;
    private static float longJumpPower = 1.6f;
    private static boolean highJumpEnabled;
    private static float highJumpPower = 1.6f;
    private static boolean jesusEnabled;
    private static String jesusMode = "Solid";
    private static boolean jesusLava;
    private static boolean spiderEnabled;
    private static String spiderMode = "Motion";
    private static float spiderSpeed = 0.22f;
    private static boolean spiderOnlyJump = true;
    private static boolean spiderOnlyMoving = true;
    private static boolean phaseEnabled;
    private static String phaseMode = "Normal";
    private static float phaseHorizontalSpeed = 2.0f;
    private static float phaseVerticalSpeed = 2.0f;
    private static boolean phaseSneakDown = true;
    private static boolean noClipEnabled;
    private static float noClipHorizontalSpeed = 2.0f;
    private static float noClipVerticalSpeed = 2.0f;
    private static boolean noClipSneakDown = true;
    private static boolean blinkEnabled;
    private static String blinkMode = "Normal";
    private static float blinkPulseTicks = 20.0f;
    private static float blinkMaxPackets = 120.0f;
    private static boolean blinkCancelOnHurt = true;
    private static boolean autoEatEnabled;
    private static float autoEatFoodLevel = 14.0f;
    private static float autoEatDelay = 8.0f;
    private static boolean autoEatRestoreSlot = true;
    private static boolean autoEatInventory;
    private static boolean autoFishEnabled;
    private static boolean autoFishAutoCast = true;
    private static float autoFishTicksAutoCast = 12.0f;
    private static float autoFishCatchDelay = 4.0f;
    private static float autoFishThrowDelay = 10.0f;
    private static boolean autoFishAntiBreak = true;
    private static boolean chestStealEnabled;
    private static float chestStealDelay = 2.0f;
    private static boolean chestStealAutoClose = true;
    private static boolean inventoryCleanerEnabled;
    private static float inventoryCleanerDelay = 2.0f;
    private static boolean inventoryCleanerCleanHotbar = true;
    private static final String DEFAULT_INVENTORY_CLEANER_TRASH_ITEMS = String.join("|",
            "minecraft:rotten_flesh",
            "minecraft:poisonous_potato",
            "minecraft:spider_eye",
            "minecraft:wheat_seeds",
            "minecraft:beetroot_seeds",
            "minecraft:melon_seeds",
            "minecraft:pumpkin_seeds",
            "minecraft:dead_bush",
            "minecraft:bowl",
            "minecraft:stick",
            "minecraft:flint",
            "minecraft:feather",
            "minecraft:string",
            "minecraft:bone");
    private static String inventoryCleanerTrashItems = DEFAULT_INVENTORY_CLEANER_TRASH_ITEMS;
    private static boolean autoRefillEnabled;
    private static float autoRefillThreshold = 8.0f;
    private static float autoRefillDelay = 2.0f;
    private static boolean autoRefillCurrentSlotOnly;
    private static boolean autoSortEnabled;
    private static String autoSortMode = "Category";
    private static String autoSortAction = "Instant";
    private static float autoSortDelay = 2.0f;
    private static boolean autoSortIncludeHotbar;
    private static int autoSortKeyCode = GLFW.GLFW_KEY_UNKNOWN;
    private static int autoSortScanCode;
    private static boolean scaffoldEnabled;
    private static String scaffoldMode = "TellyBridge";
    private static boolean scaffoldTower;
    private static boolean scaffoldTelly = true;
    private static boolean scaffoldInteractBeforePlace;
    private static boolean scaffoldDuplicateRotPlace;
    private static String scaffoldSilenceTellyMode = "Telly";
    private static int scaffoldSilenceTellyPlaceTick = 1;
    private static int scaffoldSilenceTellyRotationTick = 1;
    private static boolean scaffoldSilenceTellySnap;
    private static boolean scaffoldSilenceTellyGodBridge;
    private static boolean scaffoldSilenceTellyEagle;
    private static int scaffoldSilenceTellyEagleTick = 1;
    private static int scaffoldSilenceTellyKeepEagleTick = 1;
    private static String scaffoldSilenceTellyJumpMode = "Normal";
    private static boolean scaffoldSilenceTellyNoUptelly = true;
    private static boolean scaffoldSilenceTellySlowUpTelly;
    private static boolean scaffoldSilenceTellyHeypixelUpTelly = true;
    private static boolean scaffoldSilenceTellySafeMode;
    private static boolean scaffoldSilenceTellyTestOnGround;
    private static float scaffoldSilenceTellySafeDistance = 4.5F;
    private static boolean scaffoldSilenceTellyFixRotation = true;
    private static boolean scaffoldSilenceTellyDuplicateRotPlace = true;
    private static boolean scaffoldSilenceTellyBlockFly;
    private static boolean scaffoldSilenceTellyAbuseRotation;
    private static boolean scaffoldSilenceTellyDebug;
    private static boolean stuckEnabled;
    private static String scaffoldSwapMode = "Normal";
    private static boolean scaffoldSwapBack = true;
    private static boolean scaffoldSnap;
    private static String scaffoldRotationMode = "Rise";
    private static String scaffoldRaytraceMode = "Normal";
    private static float scaffoldRotationSpeed = 10.0f;
    private static float scaffoldRotationBackSpeed = 10.0f;
    private static float scaffoldTellyTicks = 1.0f;
    private static boolean scaffoldSafeWalk;
    private static boolean scaffoldSwingHand = true;
    private static boolean antiPacketKickEnabled;
    private static boolean autoReconnectEnabled;
    private static float autoReconnectDelay = 5.0f;
    private static int hudWidgetMask = HUD_WIDGET_HOTKEYS;
    static int watermarkX = 12;
    static int watermarkY = 12;
    private static int watermarkBackgroundColor = 0xB0161A23;
    private static int watermarkTextColor = 0xF2FFFFFF;
    private static int watermarkIconColor = 0xF26E8BFF;
    private static int watermarkElementMask = WATERMARK_ELEMENT_ALL;
    private static String watermarkElementOrder = "1,2,4,8,16,32,64";
    private static boolean watermarkBlur = true;
    private static boolean hotkeyHudEnabled = true;
    static int hotkeyHudX = 36;
    static int hotkeyHudY = 36;
    static int arrayListHudX = 12;
    static int arrayListHudY = 60;
    static int arrayListHudRightOffset = 12;
    static boolean arrayListHudRightAnchored;
    static int targetsHudX = 120;
    static int targetsHudY = 120;
    static int backpackHudX = 12;
    static int backpackHudY = 126;
    static int potionBuffHudX = 12;
    static int potionBuffHudY = 190;
    static int speedHudX = 12;
    static int speedHudY = 222;
    static int musicLyricsHudX = Integer.MIN_VALUE;
    static int musicLyricsHudY = Integer.MIN_VALUE;
    private static boolean hotkeyHudBlur = true;
    private static int hotkeyHudBackgroundColor = 0xD0161A23;
    private static int hotkeyHudItemColor = 0x9E222734;
    private static int hotkeyHudTextColor = 0xF2FFFFFF;
    private static int hotkeyHudAccentColor = 0xF26E8BFF;
    private static boolean hotkeyHudShowTitle = true;
    private static boolean arrayListHudBlur = true;
    private static int arrayListHudBackgroundColor = 0x94222734;
    private static int arrayListHudTextColor = 0xF2FFFFFF;
    private static int arrayListHudAccentColor = 0xF26E8BFF;
    private static boolean arrayListHudAccentBar = true;
    private static boolean targetsHudBlur = true;
    private static int targetsHudBackgroundColor = 0xCC161A23;
    private static int targetsHudFieldColor = 0xB8222734;
    private static int targetsHudTextColor = 0xF2FFFFFF;
    private static int targetsHudAccentColor = 0xF26E8BFF;
    private static boolean targetsHudShowAvatar = true;
    private static boolean targetsHudShowDistance = true;
    private static boolean targetsHudShowHealthText = true;
    private static boolean targetsHudShowHealthBar = true;
    private static boolean backpackHudBlur = true;
    private static int backpackHudBackgroundColor = 0xCC161A23;
    private static int backpackHudSlotColor = 0xA3222734;
    private static int backpackHudTextColor = 0xF2FFFFFF;
    private static int backpackHudAccentColor = 0xF26E8BFF;
    private static boolean backpackHudShowTitle = true;
    private static boolean backpackHudShowCounts = true;
    private static boolean potionBuffHudBlur = true;
    private static int potionBuffHudBackgroundColor = 0x9E222734;
    private static int potionBuffHudTextColor = 0xF2FFFFFF;
    private static int potionBuffHudAccentColor = 0xF26E8BFF;
    private static boolean potionBuffHudShowIcons = true;
    private static boolean potionBuffHudShowDuration = true;
    private static boolean potionBuffHudEffectColors = true;
    private static boolean speedHudBlur = true;
    private static int speedHudBackgroundColor = 0xC7161A23;
    private static int speedHudFieldColor = 0x8C222734;
    private static int speedHudTextColor = 0xF2FFFFFF;
    private static int speedHudAccentColor = 0xF26E8BFF;
    private static boolean speedHudShowChart = true;
    private static boolean speedHudShowValue = true;
    private static boolean notificationsEnabled = true;
    private static String notificationsPosition = "Bottom Right";
    private static float notificationsDuration = 3.5f;
    private static int notificationsMaxVisible = 5;
    private static boolean notificationsBlur = true;
    private static boolean notificationsModuleToggles = true;
    private static boolean notificationsLinkHotkeys = true;
    private static boolean modernUiEnabled = true;

    private VisualSettings() {
    }

    public static boolean visualEspEnabled() {
        return visualEspEnabled;
    }

    public static void setVisualEspEnabled(boolean enabled) {
        visualEspEnabled = enabled;
    }

    public static float visualEnemyRange() {
        return visualEnemyRange;
    }

    public static float visualEnemyRangeSlider() {
        return ConfigValueCodec.inverse(visualEnemyRange, VisualRanges.VISUAL_PROFILE_MIN_RANGE, VisualRanges.VISUAL_PROFILE_MAX_RANGE);
    }

    public static void setVisualEnemyRangeSlider(float value) {
        visualEnemyRange = ConfigValueCodec.range(value, VisualRanges.VISUAL_PROFILE_MIN_RANGE, VisualRanges.VISUAL_PROFILE_MAX_RANGE);
    }

    public static boolean visualEnemiesPlayers() {
        return visualEnemiesPlayers;
    }

    public static void setVisualEnemiesPlayers(boolean enabled) {
        visualEnemiesPlayers = enabled;
    }

    public static int visualEnemiesTargetMask() {
        int mask = 0;
        if (visualEnemiesPlayers) {
            mask |= VISUAL_ENEMY_TARGET_PLAYERS;
        }
        if (visualEnemiesHostileMobs) {
            mask |= VISUAL_ENEMY_TARGET_HOSTILE_MOBS;
        }
        if (visualEnemiesPassiveMobs) {
            mask |= VISUAL_ENEMY_TARGET_PASSIVE_MOBS;
        }
        if (visualEnemiesOtherLiving) {
            mask |= VISUAL_ENEMY_TARGET_OTHER_LIVING;
        }
        return mask;
    }

    public static void setVisualEnemiesTargetMask(int mask) {
        visualEnemiesPlayers = (mask & VISUAL_ENEMY_TARGET_PLAYERS) != 0;
        visualEnemiesHostileMobs = (mask & VISUAL_ENEMY_TARGET_HOSTILE_MOBS) != 0;
        visualEnemiesPassiveMobs = (mask & VISUAL_ENEMY_TARGET_PASSIVE_MOBS) != 0;
        visualEnemiesOtherLiving = (mask & VISUAL_ENEMY_TARGET_OTHER_LIVING) != 0;
    }

    public static boolean visualEnemiesHostileMobs() {
        return visualEnemiesHostileMobs;
    }

    public static void setVisualEnemiesHostileMobs(boolean enabled) {
        visualEnemiesHostileMobs = enabled;
    }

    public static boolean visualEnemiesPassiveMobs() {
        return visualEnemiesPassiveMobs;
    }

    public static void setVisualEnemiesPassiveMobs(boolean enabled) {
        visualEnemiesPassiveMobs = enabled;
    }

    public static boolean visualEnemiesOtherLiving() {
        return visualEnemiesOtherLiving;
    }

    public static void setVisualEnemiesOtherLiving(boolean enabled) {
        visualEnemiesOtherLiving = enabled;
    }

    public static boolean visualEnemiesIgnoreBots() {
        return visualEnemiesIgnoreBots;
    }

    public static void setVisualEnemiesIgnoreBots(boolean enabled) {
        visualEnemiesIgnoreBots = enabled;
    }

    public static boolean visualEnemiesShowInvisible() {
        return visualEnemiesShowInvisible;
    }

    public static void setVisualEnemiesShowInvisible(boolean enabled) {
        visualEnemiesShowInvisible = enabled;
    }

    public static boolean visualEnemiesThroughWalls() {
        return visualEnemiesThroughWalls;
    }

    public static void setVisualEnemiesThroughWalls(boolean enabled) {
        visualEnemiesThroughWalls = enabled;
    }

    public static boolean visualOffscreenArrow() {
        return visualOffscreenArrow;
    }

    public static void setVisualOffscreenArrow(boolean enabled) {
        visualOffscreenArrow = enabled;
    }

    public static float visualOffscreenArrowSize() {
        return visualOffscreenArrowSize;
    }

    public static float visualOffscreenArrowSizeSlider() {
        return ConfigValueCodec.inverse(visualOffscreenArrowSize, VisualRanges.OFFSCREEN_ARROW_SIZE_MIN, VisualRanges.OFFSCREEN_ARROW_SIZE_MAX);
    }

    public static void setVisualOffscreenArrowSizeSlider(float value) {
        visualOffscreenArrowSize = ConfigValueCodec.range(value, VisualRanges.OFFSCREEN_ARROW_SIZE_MIN, VisualRanges.OFFSCREEN_ARROW_SIZE_MAX);
    }

    public static float visualOffscreenArrowRadius() {
        return visualOffscreenArrowRadius;
    }

    public static float visualOffscreenArrowRadiusSlider() {
        return ConfigValueCodec.inverse(visualOffscreenArrowRadius, VisualRanges.OFFSCREEN_ARROW_RADIUS_MIN, VisualRanges.OFFSCREEN_ARROW_RADIUS_MAX);
    }

    public static void setVisualOffscreenArrowRadiusSlider(float value) {
        visualOffscreenArrowRadius = ConfigValueCodec.range(value, VisualRanges.OFFSCREEN_ARROW_RADIUS_MIN, VisualRanges.OFFSCREEN_ARROW_RADIUS_MAX);
    }

    public static int visualOffscreenArrowDisplayMask() {
        return visualOffscreenArrowDisplayMask;
    }

    public static void setVisualOffscreenArrowDisplayMask(int mask) {
        visualOffscreenArrowDisplayMask = mask & OFFSCREEN_ARROW_ALL;
    }

    public static int visualOffscreenArrowColor() {
        return visualOffscreenArrowColor;
    }

    public static void setVisualOffscreenArrowColor(int color) {
        visualOffscreenArrowColor = color;
    }

    public static boolean visualSounds() {
        return visualSounds;
    }

    public static void setVisualSounds(boolean enabled) {
        visualSounds = enabled;
    }

    public static int visualSoundsColor() {
        return visualSoundsColor;
    }

    public static void setVisualSoundsColor(int color) {
        visualSoundsColor = color;
    }

    public static boolean visualPlayerChams() {
        return visualPlayerChams;
    }

    public static void setVisualPlayerChams(boolean enabled) {
        visualPlayerChams = enabled;
    }

    public static String visualPlayerChamsMode() {
        return visualPlayerChamsMode;
    }

    public static void setVisualPlayerChamsMode(String mode) {
        visualPlayerChamsMode = VisualDefaults.visualModelMode(mode);
    }

    public static int visualPlayerChamsColor() {
        return visualPlayerChamsColor;
    }

    public static void setVisualPlayerChamsColor(int color) {
        visualPlayerChamsColor = color;
    }

    public static boolean visualBehindWallsChams() {
        return visualBehindWallsChams;
    }

    public static void setVisualBehindWallsChams(boolean enabled) {
        visualBehindWallsChams = enabled;
    }

    public static String visualBehindWallsChamsMode() {
        return visualBehindWallsChamsMode;
    }

    public static void setVisualBehindWallsChamsMode(String mode) {
        visualBehindWallsChamsMode = VisualDefaults.visualModelMode(mode);
    }

    public static int visualBehindWallsChamsColor() {
        return visualBehindWallsChamsColor;
    }

    public static void setVisualBehindWallsChamsColor(int color) {
        visualBehindWallsChamsColor = color;
    }

    public static boolean visualOnHitChams() {
        return visualOnHitChams;
    }

    public static void setVisualOnHitChams(boolean enabled) {
        visualOnHitChams = enabled;
    }

    public static String visualOnHitChamsMode() {
        return visualOnHitChamsMode;
    }

    public static void setVisualOnHitChamsMode(String mode) {
        visualOnHitChamsMode = VisualDefaults.visualModelMode(mode);
    }

    public static int visualOnHitChamsColor() {
        return visualOnHitChamsColor;
    }

    public static void setVisualOnHitChamsColor(int color) {
        visualOnHitChamsColor = color;
    }

    public static boolean visualRagdollsChams() {
        return visualRagdollsChams;
    }

    public static void setVisualRagdollsChams(boolean enabled) {
        visualRagdollsChams = enabled;
    }

    public static String visualRagdollsChamsMode() {
        return visualRagdollsChamsMode;
    }

    public static void setVisualRagdollsChamsMode(String mode) {
        visualRagdollsChamsMode = VisualDefaults.visualModelMode(mode);
    }

    public static int visualRagdollsChamsColor() {
        return visualRagdollsChamsColor;
    }

    public static void setVisualRagdollsChamsColor(int color) {
        visualRagdollsChamsColor = color;
    }

    public static boolean visualSoulParticles() {
        return visualSoulParticles;
    }

    public static void setVisualSoulParticles(boolean enabled) {
        visualSoulParticles = enabled;
    }

    public static String visualSoulParticlesMode() {
        return visualSoulParticlesMode;
    }

    public static void setVisualSoulParticlesMode(String mode) {
        visualSoulParticlesMode = VisualDefaults.visualModelMode(mode);
    }

    public static int visualSoulParticlesColor() {
        return visualSoulParticlesColor;
    }

    public static void setVisualSoulParticlesColor(int color) {
        visualSoulParticlesColor = color;
    }

    public static boolean visualGlow() {
        return visualGlow;
    }

    public static void setVisualGlow(boolean enabled) {
        visualGlow = enabled;
    }

    public static int visualGlowColor() {
        return visualGlowColor;
    }

    public static void setVisualGlowColor(int color) {
        visualGlowColor = color;
    }

    public static boolean visualTeamEnabled() {
        return visualTeamEnabled;
    }

    public static void setVisualTeamEnabled(boolean enabled) {
        visualTeamEnabled = enabled;
    }

    public static float visualTeamRange() {
        return visualTeamRange;
    }

    public static float visualTeamRangeSlider() {
        return ConfigValueCodec.inverse(visualTeamRange, VisualRanges.VISUAL_PROFILE_MIN_RANGE, VisualRanges.VISUAL_PROFILE_MAX_RANGE);
    }

    public static void setVisualTeamRangeSlider(float value) {
        visualTeamRange = ConfigValueCodec.range(value, VisualRanges.VISUAL_PROFILE_MIN_RANGE, VisualRanges.VISUAL_PROFILE_MAX_RANGE);
    }

    public static boolean visualTeamShowInvisible() {
        return visualTeamShowInvisible;
    }

    public static void setVisualTeamShowInvisible(boolean enabled) {
        visualTeamShowInvisible = enabled;
    }

    public static boolean visualTeamThroughWalls() {
        return visualTeamThroughWalls;
    }

    public static void setVisualTeamThroughWalls(boolean enabled) {
        visualTeamThroughWalls = enabled;
    }

    public static boolean visualTeamPlayerChams() {
        return visualTeamPlayerChams;
    }

    public static void setVisualTeamPlayerChams(boolean enabled) {
        visualTeamPlayerChams = enabled;
    }

    public static String visualTeamPlayerChamsMode() {
        return visualTeamPlayerChamsMode;
    }

    public static void setVisualTeamPlayerChamsMode(String mode) {
        visualTeamPlayerChamsMode = VisualDefaults.visualModelMode(mode);
    }

    public static int visualTeamPlayerChamsColor() {
        return visualTeamPlayerChamsColor;
    }

    public static void setVisualTeamPlayerChamsColor(int color) {
        visualTeamPlayerChamsColor = color;
    }

    public static boolean visualTeamBehindWallsChams() {
        return visualTeamBehindWallsChams;
    }

    public static void setVisualTeamBehindWallsChams(boolean enabled) {
        visualTeamBehindWallsChams = enabled;
    }

    public static String visualTeamBehindWallsChamsMode() {
        return visualTeamBehindWallsChamsMode;
    }

    public static void setVisualTeamBehindWallsChamsMode(String mode) {
        visualTeamBehindWallsChamsMode = VisualDefaults.visualModelMode(mode);
    }

    public static int visualTeamBehindWallsChamsColor() {
        return visualTeamBehindWallsChamsColor;
    }

    public static void setVisualTeamBehindWallsChamsColor(int color) {
        visualTeamBehindWallsChamsColor = color;
    }

    public static boolean visualTeamGlow() {
        return visualTeamGlow;
    }

    public static void setVisualTeamGlow(boolean enabled) {
        visualTeamGlow = enabled;
    }

    public static int visualTeamGlowColor() {
        return visualTeamGlowColor;
    }

    public static void setVisualTeamGlowColor(int color) {
        visualTeamGlowColor = color;
    }

    public static boolean visualTeamDisableModelRendering() {
        return visualTeamDisableModelRendering;
    }

    public static void setVisualTeamDisableModelRendering(boolean enabled) {
        visualTeamDisableModelRendering = enabled;
    }

    public static boolean visualTeamMobBox() {
        return visualTeamMobBox;
    }

    public static void setVisualTeamMobBox(boolean enabled) {
        visualTeamMobBox = enabled;
    }

    public static boolean visualTeamMobHealth() {
        return visualTeamMobHealth;
    }

    public static void setVisualTeamMobHealth(boolean enabled) {
        visualTeamMobHealth = enabled;
    }

    public static boolean visualTeamMobName() {
        return visualTeamMobName;
    }

    public static void setVisualTeamMobName(boolean enabled) {
        visualTeamMobName = enabled;
    }

    public static boolean visualTeamMobDistance() {
        return visualTeamMobDistance;
    }

    public static void setVisualTeamMobDistance(boolean enabled) {
        visualTeamMobDistance = enabled;
    }

    public static long visualEnemyEspItemMask() {
        return visualEnemyEspItemMask;
    }

    public static void setVisualEnemyEspItemMask(long mask) {
        visualEnemyEspItemMask = mask;
    }

    public static long visualTeamEspItemMask() {
        return visualTeamEspItemMask;
    }

    public static void setVisualTeamEspItemMask(long mask) {
        visualTeamEspItemMask = mask;
    }

    public static boolean visualEnemyEspItemEnabled(long item) {
        return (visualEnemyEspItemMask & item) != 0L;
    }

    public static void setVisualEnemyEspItemEnabled(long item, boolean enabled) {
        visualEnemyEspItemMask = enabled ? visualEnemyEspItemMask | item : visualEnemyEspItemMask & ~item;
    }

    public static boolean visualTeamEspItemEnabled(long item) {
        return (visualTeamEspItemMask & item) != 0L;
    }

    public static void setVisualTeamEspItemEnabled(long item, boolean enabled) {
        visualTeamEspItemMask = enabled ? visualTeamEspItemMask | item : visualTeamEspItemMask & ~item;
    }

    public static boolean visualLocalEnabled() {
        return visualLocalEnabled;
    }

    public static void setVisualLocalEnabled(boolean enabled) {
        visualLocalEnabled = enabled;
    }

    public static float visualLocalRange() {
        return visualLocalRange;
    }

    public static float visualLocalRangeSlider() {
        return ConfigValueCodec.inverse(visualLocalRange, VisualRanges.VISUAL_PROFILE_MIN_RANGE, VisualRanges.VISUAL_PROFILE_MAX_RANGE);
    }

    public static void setVisualLocalRangeSlider(float value) {
        visualLocalRange = ConfigValueCodec.range(value, VisualRanges.VISUAL_PROFILE_MIN_RANGE, VisualRanges.VISUAL_PROFILE_MAX_RANGE);
    }

    public static boolean visualLocalShowInvisible() {
        return visualLocalShowInvisible;
    }

    public static void setVisualLocalShowInvisible(boolean enabled) {
        visualLocalShowInvisible = enabled;
    }

    public static boolean visualLocalThroughWalls() {
        return visualLocalThroughWalls;
    }

    public static void setVisualLocalThroughWalls(boolean enabled) {
        visualLocalThroughWalls = enabled;
    }

    public static boolean visualLocalPlayerChams() {
        return visualLocalPlayerChams;
    }

    public static void setVisualLocalPlayerChams(boolean enabled) {
        visualLocalPlayerChams = enabled;
    }

    public static String visualLocalPlayerChamsMode() {
        return visualLocalPlayerChamsMode;
    }

    public static void setVisualLocalPlayerChamsMode(String mode) {
        visualLocalPlayerChamsMode = VisualDefaults.visualModelMode(mode);
    }

    public static int visualLocalPlayerChamsColor() {
        return visualLocalPlayerChamsColor;
    }

    public static void setVisualLocalPlayerChamsColor(int color) {
        visualLocalPlayerChamsColor = color;
    }

    public static boolean visualLocalTransparencyEnabled() {
        return visualLocalTransparencyEnabled;
    }

    public static void setVisualLocalTransparencyEnabled(boolean enabled) {
        visualLocalTransparencyEnabled = enabled;
    }

    public static float visualLocalTransparency() {
        return visualLocalTransparency;
    }

    public static float visualLocalTransparencySlider() {
        return visualLocalTransparency;
    }

    public static void setVisualLocalTransparencySlider(float value) {
        visualLocalTransparency = Math.max(0.0f, Math.min(1.0f, value));
    }

    public static int visualLocalTransparencyDisableMask() {
        return visualLocalTransparencyDisableMask;
    }

    public static void setVisualLocalTransparencyDisableMask(int mask) {
        visualLocalTransparencyDisableMask = mask;
    }

    public static boolean visualLocalItem() {
        return visualLocalItem;
    }

    public static void setVisualLocalItem(boolean enabled) {
        visualLocalItem = enabled;
    }

    public static String visualLocalItemMode() {
        return visualLocalItemMode;
    }

    public static void setVisualLocalItemMode(String mode) {
        visualLocalItemMode = VisualDefaults.visualModelMode(mode);
    }

    public static int visualLocalItemColor() {
        return visualLocalItemColor;
    }

    public static void setVisualLocalItemColor(int color) {
        visualLocalItemColor = color;
    }

    public static boolean visualLocalArms() {
        return visualLocalArms;
    }

    public static void setVisualLocalArms(boolean enabled) {
        visualLocalArms = enabled;
    }

    public static String visualLocalArmsMode() {
        return visualLocalArmsMode;
    }

    public static void setVisualLocalArmsMode(String mode) {
        visualLocalArmsMode = VisualDefaults.visualModelMode(mode);
    }

    public static int visualLocalArmsColor() {
        return visualLocalArmsColor;
    }

    public static void setVisualLocalArmsColor(int color) {
        visualLocalArmsColor = color;
    }

    public static boolean visualLocalGlow() {
        return visualLocalGlow;
    }

    public static void setVisualLocalGlow(boolean enabled) {
        visualLocalGlow = enabled;
    }

    public static int visualLocalGlowColor() {
        return visualLocalGlowColor;
    }

    public static void setVisualLocalGlowColor(int color) {
        visualLocalGlowColor = color;
    }

    public static boolean visualLocalMobBox() {
        return visualLocalMobBox;
    }

    public static void setVisualLocalMobBox(boolean enabled) {
        visualLocalMobBox = enabled;
    }

    public static boolean visualLocalMobHealth() {
        return visualLocalMobHealth;
    }

    public static void setVisualLocalMobHealth(boolean enabled) {
        visualLocalMobHealth = enabled;
    }

    public static boolean visualLocalMobName() {
        return visualLocalMobName;
    }

    public static void setVisualLocalMobName(boolean enabled) {
        visualLocalMobName = enabled;
    }

    public static boolean visualLocalMobDistance() {
        return visualLocalMobDistance;
    }

    public static void setVisualLocalMobDistance(boolean enabled) {
        visualLocalMobDistance = enabled;
    }

    public static long visualLocalEspItemMask() {
        return visualLocalEspItemMask;
    }

    public static void setVisualLocalEspItemMask(long mask) {
        visualLocalEspItemMask = mask;
    }

    public static boolean visualLocalEspItemEnabled(long item) {
        return (visualLocalEspItemMask & item) != 0L;
    }

    public static void setVisualLocalEspItemEnabled(long item, boolean enabled) {
        visualLocalEspItemMask = enabled ? visualLocalEspItemMask | item : visualLocalEspItemMask & ~item;
    }

    public static String visualEspItemColors() {
        return visualEspItemColors;
    }

    public static void setVisualEspItemColors(String colors) {
        visualEspItemColors = colors == null ? "" : colors;
    }

    public static int visualEspItemColor(long item) {
        return SerializedSettingValues.intValue(visualEspItemColors, item, VisualDefaults.espItemColor(item));
    }

    public static void setVisualEspItemColor(long item, int color) {
        visualEspItemColors = SerializedSettingValues.putIntValue(visualEspItemColors, item, 0xF2000000 | (color & 0x00FFFFFF));
    }

    public static String visualEspItemColorModes() {
        return visualEspItemColorModes;
    }

    public static void setVisualEspItemColorModes(String modes) {
        visualEspItemColorModes = modes == null ? "" : modes;
    }

    public static String visualEspItemNumbers() {
        return visualEspItemNumbers;
    }

    public static void setVisualEspItemNumbers(String numbers) {
        visualEspItemNumbers = numbers == null ? "" : numbers;
    }

    public static boolean visualEspItemNumber(long item) {
        return SerializedSettingValues.booleanValue(visualEspItemNumbers, item, false);
    }

    public static void setVisualEspItemNumber(long item, boolean enabled) {
        visualEspItemNumbers = SerializedSettingValues.putBooleanValue(visualEspItemNumbers, item, enabled);
    }

    public static String visualEspItemColorMode(long item) {
        String fallback = item == ESP_ITEM_ARMOR_BAR || item == ESP_ITEM_ABSORPTION ? "Dynamic" : "Custom";
        String mode = SerializedSettingValues.stringValue(visualEspItemColorModes, item, fallback);
        return "Dynamic".equals(mode) ? "Dynamic" : "Custom";
    }

    public static void setVisualEspItemColorMode(long item, String mode) {
        visualEspItemColorModes = SerializedSettingValues.putStringValue(visualEspItemColorModes, item,
                "Dynamic".equals(mode) ? "Dynamic" : "Custom");
    }

    public static boolean visualEspItemDynamicColor(long item) {
        return "Dynamic".equals(visualEspItemColorMode(item));
    }

    public static String visualEspItemAnchors() {
        return visualEspItemAnchors;
    }

    public static void setVisualEspItemAnchors(String anchors) {
        visualEspItemAnchors = anchors == null ? "" : anchors;
    }

    public static EspAnchor visualEspItemAnchor(long item) {
        String fallback = item == ESP_ITEM_ABSORPTION ? EspAnchor.TOP.name() : EspAnchor.BOTTOM.name();
        String value = SerializedSettingValues.stringValue(visualEspItemAnchors, item, fallback);
        try {
            return EspAnchor.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return EspAnchor.BOTTOM;
        }
    }

    public static void setVisualEspItemAnchor(long item, EspAnchor anchor) {
        visualEspItemAnchors = SerializedSettingValues.putStringValue(visualEspItemAnchors, item,
                (anchor == null ? EspAnchor.BOTTOM : anchor).name());
    }

    public static float visualHurtTimeDisplayTicks() {
        return visualHurtTimeDisplayTicks;
    }

    public static float visualHurtTimeDisplayTicksSlider() {
        return ConfigValueCodec.inverse(visualHurtTimeDisplayTicks, 2.0f, 20.0f);
    }

    public static void setVisualHurtTimeDisplayTicksSlider(float value) {
        visualHurtTimeDisplayTicks = ConfigValueCodec.range(value, 2.0f, 20.0f);
    }

    public static String visualEspFlagTextModes() {
        return visualEspFlagTextModes;
    }

    public static void setVisualEspFlagTextModes(String modes) {
        visualEspFlagTextModes = modes == null ? "" : modes;
    }

    public static String visualEspFlagTextMode(long item) {
        String mode = SerializedSettingValues.stringValue(visualEspFlagTextModes, item, ESP_FLAG_TEXT_FULL);
        return ESP_FLAG_TEXT_SHORT.equals(mode) ? ESP_FLAG_TEXT_SHORT : ESP_FLAG_TEXT_FULL;
    }

    public static void setVisualEspFlagTextMode(long item, String mode) {
        visualEspFlagTextModes = SerializedSettingValues.putStringValue(visualEspFlagTextModes, item,
                ESP_FLAG_TEXT_SHORT.equals(mode) ? ESP_FLAG_TEXT_SHORT : ESP_FLAG_TEXT_FULL);
    }

    public static boolean visualEspFlagTextShort(long item) {
        return ESP_FLAG_TEXT_SHORT.equals(visualEspFlagTextMode(item));
    }

    public static String visualEspFlagAnchors() {
        return visualEspFlagAnchors;
    }

    public static void setVisualEspFlagAnchors(String anchors) {
        visualEspFlagAnchors = anchors == null ? "" : anchors;
    }

    public static EspAnchor visualEspFlagAnchor(long item) {
        String value = SerializedSettingValues.stringValue(visualEspFlagAnchors, item, EspAnchor.RIGHT.name());
        try {
            return EspAnchor.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return EspAnchor.RIGHT;
        }
    }

    public static void setVisualEspFlagAnchor(long item, EspAnchor anchor) {
        visualEspFlagAnchors = SerializedSettingValues.putStringValue(visualEspFlagAnchors, item,
                (anchor == null ? EspAnchor.RIGHT : anchor).name());
    }

    public static boolean espItemIsFlag(long item) {
        return item >= ESP_ITEM_SNEAKING && item <= ESP_ITEM_FRIEND_TEAM
                || item == ESP_ITEM_PING
                || item == ESP_ITEM_GAMEMODE;
    }

    public static boolean mobBoxEnabled() {
        return mobBoxEnabled;
    }

    public static void setMobBoxEnabled(boolean enabled) {
        mobBoxEnabled = enabled;
    }

    public static float mobBoxRange() {
        return mobBoxRange;
    }

    public static float mobBoxRangeSlider() {
        return (mobBoxRange - VisualRanges.MOB_BOX_MIN_RANGE) / (VisualRanges.MOB_BOX_MAX_RANGE - VisualRanges.MOB_BOX_MIN_RANGE);
    }

    public static void setMobBoxRangeSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        mobBoxRange = VisualRanges.MOB_BOX_MIN_RANGE + (VisualRanges.MOB_BOX_MAX_RANGE - VisualRanges.MOB_BOX_MIN_RANGE) * clamped;
    }

    public static int mobBoxColor() {
        return mobBoxColor;
    }

    public static void setMobBoxColor(int color) {
        mobBoxColor = 0xF2000000 | (color & 0x00FFFFFF);
    }

    public static String mobBoxStyle() {
        return mobBoxStyle;
    }

    public static void setMobBoxStyle(String style) {
        mobBoxStyle = "Normal";
    }

    public static boolean mobBox2D() {
        return true;
    }

    public static boolean mobBoxShowInvisible() {
        return mobBoxShowInvisible;
    }

    public static void setMobBoxShowInvisible(boolean enabled) {
        mobBoxShowInvisible = enabled;
    }

    public static boolean mobBoxThroughWalls() {
        return mobBoxThroughWalls;
    }

    public static void setMobBoxThroughWalls(boolean enabled) {
        mobBoxThroughWalls = enabled;
    }

    public static boolean mobHealthEnabled() {
        return mobHealthEnabled;
    }

    public static void setMobHealthEnabled(boolean enabled) {
        mobHealthEnabled = enabled;
    }

    public static float mobHealthRange() {
        return mobHealthRange;
    }

    public static float mobHealthRangeSlider() {
        return (mobHealthRange - VisualRanges.MOB_HEALTH_MIN_RANGE) / (VisualRanges.MOB_HEALTH_MAX_RANGE - VisualRanges.MOB_HEALTH_MIN_RANGE);
    }

    public static void setMobHealthRangeSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        mobHealthRange = VisualRanges.MOB_HEALTH_MIN_RANGE + (VisualRanges.MOB_HEALTH_MAX_RANGE - VisualRanges.MOB_HEALTH_MIN_RANGE) * clamped;
    }

    public static String mobHealthStyle() {
        return mobHealthStyle;
    }

    public static void setMobHealthStyle(String style) {
        mobHealthStyle = "Normal";
    }

    public static boolean mobHealth2D() {
        return true;
    }

    public static String mobHealthColorMode() {
        return mobHealthColorMode;
    }

    public static void setMobHealthColorMode(String mode) {
        mobHealthColorMode = "Custom".equals(mode) ? "Custom" : "Dynamic";
    }

    public static boolean mobHealthDynamicColor() {
        return "Dynamic".equals(mobHealthColorMode);
    }

    public static int mobHealthColor() {
        return mobHealthColor;
    }

    public static void setMobHealthColor(int color) {
        mobHealthColor = 0xF2000000 | (color & 0x00FFFFFF);
    }

    public static boolean mobHealthNumber() {
        return mobHealthNumber;
    }

    public static void setMobHealthNumber(boolean enabled) {
        mobHealthNumber = enabled;
    }

    public static boolean mobHealthShowInvisible() {
        return mobHealthShowInvisible;
    }

    public static void setMobHealthShowInvisible(boolean enabled) {
        mobHealthShowInvisible = enabled;
    }

    public static boolean mobHealthThroughWalls() {
        return mobHealthThroughWalls;
    }

    public static void setMobHealthThroughWalls(boolean enabled) {
        mobHealthThroughWalls = enabled;
    }

    public static EspAnchor mobHealthAnchor() {
        return mobHealthAnchor;
    }

    public static void setMobHealthAnchor(EspAnchor anchor) {
        mobHealthAnchor = anchor == null ? EspAnchor.LEFT : anchor;
        mobHealthOffsetX = 0.0f;
        mobHealthOffsetY = 0.0f;
    }

    public static boolean mobHealthHorizontal() {
        return mobHealthAnchor == EspAnchor.TOP || mobHealthAnchor == EspAnchor.BOTTOM;
    }

    public static float mobHealthOffsetX() {
        return mobHealthOffsetX;
    }

    public static float mobHealthOffsetY() {
        return mobHealthOffsetY;
    }

    public static void setMobHealthOffset(float x, float y) {
        mobHealthOffsetX = ConfigValueCodec.clampOffset(x);
        mobHealthOffsetY = ConfigValueCodec.clampOffset(y);
    }

    public static boolean mobNameEnabled() {
        return mobNameEnabled;
    }

    public static void setMobNameEnabled(boolean enabled) {
        mobNameEnabled = enabled;
    }

    public static float mobNameRange() {
        return mobNameRange;
    }

    public static float mobNameRangeSlider() {
        return (mobNameRange - VisualRanges.MOB_NAME_MIN_RANGE) / (VisualRanges.MOB_NAME_MAX_RANGE - VisualRanges.MOB_NAME_MIN_RANGE);
    }

    public static void setMobNameRangeSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        mobNameRange = VisualRanges.MOB_NAME_MIN_RANGE + (VisualRanges.MOB_NAME_MAX_RANGE - VisualRanges.MOB_NAME_MIN_RANGE) * clamped;
    }

    public static int mobNameColor() {
        return mobNameColor;
    }

    public static void setMobNameColor(int color) {
        mobNameColor = 0xF2000000 | (color & 0x00FFFFFF);
    }

    public static float mobNameScale() {
        return mobNameScale;
    }

    public static float mobNameScaleSlider() {
        return (mobNameScale - VisualRanges.MOB_NAME_MIN_SCALE) / (VisualRanges.MOB_NAME_MAX_SCALE - VisualRanges.MOB_NAME_MIN_SCALE);
    }

    public static void setMobNameScaleSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        mobNameScale = VisualRanges.MOB_NAME_MIN_SCALE + (VisualRanges.MOB_NAME_MAX_SCALE - VisualRanges.MOB_NAME_MIN_SCALE) * clamped;
    }

    public static String mobNameStyle() {
        return mobNameStyle;
    }

    public static void setMobNameStyle(String style) {
        mobNameStyle = "Normal";
    }

    public static boolean mobName2D() {
        return true;
    }

    public static boolean mobNameDistance() {
        return mobNameDistance;
    }

    public static void setMobNameDistance(boolean enabled) {
        mobNameDistance = enabled;
    }

    public static boolean mobNameShowInvisible() {
        return mobNameShowInvisible;
    }

    public static void setMobNameShowInvisible(boolean enabled) {
        mobNameShowInvisible = enabled;
    }

    public static boolean mobNameThroughWalls() {
        return mobNameThroughWalls;
    }

    public static void setMobNameThroughWalls(boolean enabled) {
        mobNameThroughWalls = enabled;
    }

    public static EspAnchor mobNameAnchor() {
        return mobNameAnchor;
    }

    public static void setMobNameAnchor(EspAnchor anchor) {
        mobNameAnchor = anchor == null ? EspAnchor.TOP : anchor;
        mobNameOffsetX = 0.0f;
        mobNameOffsetY = 0.0f;
    }

    public static float mobNameOffsetX() {
        return mobNameOffsetX;
    }

    public static float mobNameOffsetY() {
        return mobNameOffsetY;
    }

    public static void setMobNameOffset(float x, float y) {
        mobNameOffsetX = ConfigValueCodec.clampOffset(x);
        mobNameOffsetY = ConfigValueCodec.clampOffset(y);
    }

    public static int mobDistanceColor() {
        return mobDistanceColor;
    }

    public static void setMobDistanceColor(int color) {
        mobDistanceColor = 0xF2000000 | (color & 0x00FFFFFF);
    }

    public static float mobDistanceScale() {
        return mobDistanceScale;
    }

    public static float mobDistanceScaleSlider() {
        return (mobDistanceScale - VisualRanges.MOB_NAME_MIN_SCALE) / (VisualRanges.MOB_NAME_MAX_SCALE - VisualRanges.MOB_NAME_MIN_SCALE);
    }

    public static void setMobDistanceScaleSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        mobDistanceScale = VisualRanges.MOB_NAME_MIN_SCALE + (VisualRanges.MOB_NAME_MAX_SCALE - VisualRanges.MOB_NAME_MIN_SCALE) * clamped;
    }

    public static EspAnchor mobDistanceAnchor() {
        return mobDistanceAnchor;
    }

    public static void setMobDistanceAnchor(EspAnchor anchor) {
        mobDistanceAnchor = anchor == null ? EspAnchor.TOP : anchor;
        mobDistanceOffsetX = 0.0f;
        mobDistanceOffsetY = 0.0f;
    }

    public static float mobDistanceOffsetX() {
        return mobDistanceOffsetX;
    }

    public static float mobDistanceOffsetY() {
        return mobDistanceOffsetY;
    }

    public static void setMobDistanceOffset(float x, float y) {
        mobDistanceOffsetX = ConfigValueCodec.clampOffset(x);
        mobDistanceOffsetY = ConfigValueCodec.clampOffset(y);
    }

    public static boolean oreGlowEnabled() {
        return oreGlowEnabled;
    }

    public static void setOreGlowEnabled(boolean enabled) {
        oreGlowEnabled = enabled;
    }

    public static float oreGlowRange() {
        return oreGlowRange;
    }

    public static float oreGlowRangeSlider() {
        return (oreGlowRange - VisualRanges.ORE_GLOW_MIN_RANGE) / (VisualRanges.ORE_GLOW_MAX_RANGE - VisualRanges.ORE_GLOW_MIN_RANGE);
    }

    public static void setOreGlowRangeSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        oreGlowRange = VisualRanges.ORE_GLOW_MIN_RANGE + (VisualRanges.ORE_GLOW_MAX_RANGE - VisualRanges.ORE_GLOW_MIN_RANGE) * clamped;
    }

    public static float oreGlowOpacity() {
        return oreGlowOpacity;
    }

    public static float oreGlowOpacitySlider() {
        return (oreGlowOpacity - VisualRanges.ORE_GLOW_MIN_OPACITY) / (VisualRanges.ORE_GLOW_MAX_OPACITY - VisualRanges.ORE_GLOW_MIN_OPACITY);
    }

    public static void setOreGlowOpacitySlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        oreGlowOpacity = VisualRanges.ORE_GLOW_MIN_OPACITY + (VisualRanges.ORE_GLOW_MAX_OPACITY - VisualRanges.ORE_GLOW_MIN_OPACITY) * clamped;
    }

    public static boolean oreGlowOutline() {
        return oreGlowOutline;
    }

    public static void setOreGlowOutline(boolean enabled) {
        oreGlowOutline = enabled;
    }

    public static boolean oreGlowThroughWalls() {
        return oreGlowThroughWalls;
    }

    public static void setOreGlowThroughWalls(boolean enabled) {
        oreGlowThroughWalls = enabled;
    }

    public static boolean oreGlowNames() {
        return oreGlowNames;
    }

    public static void setOreGlowNames(boolean enabled) {
        oreGlowNames = enabled;
    }

    public static int oreGlowOreMask() {
        return oreGlowOreMask;
    }

    public static void setOreGlowOreMask(int mask) {
        oreGlowOreMask = mask & ORE_ALL;
    }

    public static boolean oreGlowOreEnabled(int oreMask) {
        return (oreGlowOreMask & oreMask) != 0;
    }

    public static String oreGlowModOreIds() {
        return oreGlowModOreIds;
    }

    public static void setOreGlowModOreIds(String ids) {
        oreGlowModOreIds = ids == null ? "" : ids;
    }

    public static boolean oreGlowModOreEnabled(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        if ("*".equals(oreGlowModOreIds)) {
            return true;
        }
        if (oreGlowModOreIds.isBlank()) {
            return false;
        }
        return ("|" + oreGlowModOreIds + "|").contains("|" + id + "|");
    }

    public static String xrayBlockIds() {
        return xrayBlockIds;
    }

    public static boolean xrayEnabled() {
        return xrayEnabled;
    }

    public static void setXrayEnabled(boolean enabled) {
        if (xrayEnabled == enabled) {
            return;
        }
        xrayEnabled = enabled;
        refreshLevelRenderer();
    }

    public static float xrayOpacity() {
        return xrayOpacity;
    }

    public static float xrayOpacitySlider() {
        return ConfigValueCodec.inverse(xrayOpacity, 0.10f, 1.0f);
    }

    public static void setXrayOpacitySlider(float value) {
        float next = ConfigValueCodec.range(value, 0.10f, 1.0f);
        if (Math.abs(xrayOpacity - next) < 0.0001f) {
            return;
        }
        xrayOpacity = next;
        refreshLevelRenderer();
    }

    public static boolean xrayExposedOnly() {
        return xrayExposedOnly;
    }

    public static void setXrayExposedOnly(boolean enabled) {
        if (xrayExposedOnly == enabled) {
            return;
        }
        xrayExposedOnly = enabled;
        refreshLevelRenderer();
    }

    private static void refreshLevelRenderer() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.levelRenderer != null) {
            minecraft.levelRenderer.allChanged();
        }
    }

    public static void setXrayBlockIds(String ids) {
        String next = ConfigValueCodec.normalizeIdList(ids);
        if (xrayBlockIds.equals(next)) {
            return;
        }
        xrayBlockIds = next;
        refreshLevelRenderer();
    }

    public static boolean xrayBlockEnabled(String id) {
        return ConfigValueCodec.idListContains(xrayBlockIds, id);
    }

    public static boolean chestGlowEnabled() {
        return chestGlowEnabled;
    }

    public static void setChestGlowEnabled(boolean enabled) {
        chestGlowEnabled = enabled;
    }

    public static float chestGlowRange() {
        return chestGlowRange;
    }

    public static float chestGlowRangeSlider() {
        return ConfigValueCodec.inverse(chestGlowRange, VisualRanges.CHEST_GLOW_MIN_RANGE, VisualRanges.CHEST_GLOW_MAX_RANGE);
    }

    public static void setChestGlowRangeSlider(float value) {
        chestGlowRange = ConfigValueCodec.range(value, VisualRanges.CHEST_GLOW_MIN_RANGE, VisualRanges.CHEST_GLOW_MAX_RANGE);
    }

    public static float chestGlowOpacity() {
        return chestGlowOpacity;
    }

    public static float chestGlowOpacitySlider() {
        return ConfigValueCodec.inverse(chestGlowOpacity, VisualRanges.CHEST_GLOW_MIN_OPACITY, VisualRanges.CHEST_GLOW_MAX_OPACITY);
    }

    public static void setChestGlowOpacitySlider(float value) {
        chestGlowOpacity = ConfigValueCodec.range(value, VisualRanges.CHEST_GLOW_MIN_OPACITY, VisualRanges.CHEST_GLOW_MAX_OPACITY);
    }

    public static boolean chestGlowOutline() {
        return chestGlowOutline;
    }

    public static void setChestGlowOutline(boolean enabled) {
        chestGlowOutline = enabled;
    }

    public static boolean chestGlowThroughWalls() {
        return chestGlowThroughWalls;
    }

    public static void setChestGlowThroughWalls(boolean enabled) {
        chestGlowThroughWalls = enabled;
    }

    public static boolean chestGlowStorageBlocks() {
        return chestGlowStorageBlocks;
    }

    public static void setChestGlowStorageBlocks(boolean enabled) {
        chestGlowStorageBlocks = enabled;
    }

    public static int chestGlowColor() {
        return chestGlowColor;
    }

    public static void setChestGlowColor(int color) {
        chestGlowColor = color;
    }

    public static boolean spawnerEspEnabled() {
        return spawnerEspEnabled;
    }

    public static void setSpawnerEspEnabled(boolean enabled) {
        spawnerEspEnabled = enabled;
    }

    public static float spawnerEspRange() {
        return spawnerEspRange;
    }

    public static float spawnerEspRangeSlider() {
        return ConfigValueCodec.inverse(spawnerEspRange, VisualRanges.SPAWNER_ESP_MIN_RANGE, VisualRanges.SPAWNER_ESP_MAX_RANGE);
    }

    public static void setSpawnerEspRangeSlider(float value) {
        spawnerEspRange = ConfigValueCodec.range(value, VisualRanges.SPAWNER_ESP_MIN_RANGE, VisualRanges.SPAWNER_ESP_MAX_RANGE);
    }

    public static boolean spawnerEspShowType() {
        return spawnerEspShowType;
    }

    public static void setSpawnerEspShowType(boolean enabled) {
        spawnerEspShowType = enabled;
    }

    public static boolean spawnerEspThroughWalls() {
        return spawnerEspThroughWalls;
    }

    public static void setSpawnerEspThroughWalls(boolean enabled) {
        spawnerEspThroughWalls = enabled;
    }

    public static int spawnerEspColor() {
        return spawnerEspColor;
    }

    public static void setSpawnerEspColor(int color) {
        spawnerEspColor = color;
    }

    public static boolean bedEspEnabled() {
        return bedEspEnabled;
    }

    public static void setBedEspEnabled(boolean enabled) {
        bedEspEnabled = enabled;
    }

    public static float bedEspRange() {
        return bedEspRange;
    }

    public static float bedEspRangeSlider() {
        return ConfigValueCodec.inverse(bedEspRange, VisualRanges.BED_ESP_MIN_RANGE, VisualRanges.BED_ESP_MAX_RANGE);
    }

    public static void setBedEspRangeSlider(float value) {
        bedEspRange = ConfigValueCodec.range(value, VisualRanges.BED_ESP_MIN_RANGE, VisualRanges.BED_ESP_MAX_RANGE);
    }

    public static boolean bedEspThroughWalls() {
        return bedEspThroughWalls;
    }

    public static void setBedEspThroughWalls(boolean enabled) {
        bedEspThroughWalls = enabled;
    }

    public static int bedEspColor() {
        return bedEspColor;
    }

    public static void setBedEspColor(int color) {
        bedEspColor = color;
    }

    public static boolean blockOverlayEnabled() {
        return blockOverlayEnabled;
    }

    public static void setBlockOverlayEnabled(boolean enabled) {
        blockOverlayEnabled = enabled;
    }

    public static boolean blockOverlaySingleSide() {
        return blockOverlaySingleSide;
    }

    public static void setBlockOverlaySingleSide(boolean enabled) {
        blockOverlaySingleSide = enabled;
    }

    public static String blockOverlayShapeMode() {
        return blockOverlayShapeMode;
    }

    public static void setBlockOverlayShapeMode(String mode) {
        blockOverlayShapeMode = mode == null || mode.isBlank() ? "Both" : mode;
    }

    public static int blockOverlaySideColor() {
        return blockOverlaySideColor;
    }

    public static void setBlockOverlaySideColor(int color) {
        blockOverlaySideColor = color;
    }

    public static int blockOverlayLineColor() {
        return blockOverlayLineColor;
    }

    public static void setBlockOverlayLineColor(int color) {
        blockOverlayLineColor = color;
    }

    public static boolean barrierBlocksEnabled() {
        return barrierBlocksEnabled;
    }

    public static void setBarrierBlocksEnabled(boolean enabled) {
        barrierBlocksEnabled = enabled;
    }

    public static float barrierBlocksRange() {
        return barrierBlocksRange;
    }

    public static float barrierBlocksRangeSlider() {
        return ConfigValueCodec.inverse(barrierBlocksRange, VisualRanges.BARRIER_BLOCKS_MIN_RANGE, VisualRanges.BARRIER_BLOCKS_MAX_RANGE);
    }

    public static void setBarrierBlocksRangeSlider(float value) {
        barrierBlocksRange = ConfigValueCodec.range(value, VisualRanges.BARRIER_BLOCKS_MIN_RANGE, VisualRanges.BARRIER_BLOCKS_MAX_RANGE);
    }

    public static boolean barrierBlocksThroughWalls() {
        return barrierBlocksThroughWalls;
    }

    public static void setBarrierBlocksThroughWalls(boolean enabled) {
        barrierBlocksThroughWalls = enabled;
    }

    public static int barrierBlocksColor() {
        return barrierBlocksColor;
    }

    public static void setBarrierBlocksColor(int color) {
        barrierBlocksColor = color;
    }

    public static boolean lightBlocksEnabled() {
        return lightBlocksEnabled;
    }

    public static void setLightBlocksEnabled(boolean enabled) {
        lightBlocksEnabled = enabled;
    }

    public static float lightBlocksRange() {
        return lightBlocksRange;
    }

    public static float lightBlocksRangeSlider() {
        return ConfigValueCodec.inverse(lightBlocksRange, VisualRanges.LIGHT_BLOCKS_MIN_RANGE, VisualRanges.LIGHT_BLOCKS_MAX_RANGE);
    }

    public static void setLightBlocksRangeSlider(float value) {
        lightBlocksRange = ConfigValueCodec.range(value, VisualRanges.LIGHT_BLOCKS_MIN_RANGE, VisualRanges.LIGHT_BLOCKS_MAX_RANGE);
    }

    public static boolean lightBlocksShowLevel() {
        return lightBlocksShowLevel;
    }

    public static void setLightBlocksShowLevel(boolean enabled) {
        lightBlocksShowLevel = enabled;
    }

    public static boolean lightBlocksAllGlowing() {
        return lightBlocksAllGlowing;
    }

    public static void setLightBlocksAllGlowing(boolean enabled) {
        lightBlocksAllGlowing = enabled;
    }

    public static boolean lightBlocksGradientFace() {
        return lightBlocksGradientFace;
    }

    public static void setLightBlocksGradientFace(boolean enabled) {
        lightBlocksGradientFace = enabled;
    }

    public static int lightBlocksColor() {
        return lightBlocksColor;
    }

    public static void setLightBlocksColor(int color) {
        lightBlocksColor = color;
    }

    public static boolean fullbrightEnabled() {
        return fullbrightEnabled;
    }

    public static void setFullbrightEnabled(boolean enabled) {
        if (fullbrightEnabled && !enabled) {
            FullbrightService.reset();
        }
        fullbrightEnabled = enabled;
    }

    public static String fullbrightMode() {
        return fullbrightMode;
    }

    public static void setFullbrightMode(String mode) {
        if (!fullbrightMode.equals(mode)) {
            FullbrightService.reset();
        }
        fullbrightMode = "Night Vision".equals(mode) ? "Night Vision" : "Gamma";
    }

    public static float fullbrightStrength() {
        return fullbrightStrength;
    }

    public static float fullbrightStrengthSlider() {
        return fullbrightStrength;
    }

    public static void setFullbrightStrengthSlider(float value) {
        fullbrightStrength = Math.max(0.0f, Math.min(1.0f, value));
    }

    public static boolean noFogEnabled() {
        return noFogEnabled;
    }

    public static void setNoFogEnabled(boolean enabled) {
        noFogEnabled = enabled;
    }

    public static boolean noFogWater() {
        return noFogWater;
    }

    public static void setNoFogWater(boolean enabled) {
        noFogWater = enabled;
    }

    public static boolean noFogLava() {
        return noFogLava;
    }

    public static void setNoFogLava(boolean enabled) {
        noFogLava = enabled;
    }

    public static boolean noFogDistance() {
        return noFogDistance;
    }

    public static void setNoFogDistance(boolean enabled) {
        noFogDistance = enabled;
    }

    public static boolean noFogPowderSnow() {
        return noFogPowderSnow;
    }

    public static void setNoFogPowderSnow(boolean enabled) {
        noFogPowderSnow = enabled;
    }

    public static boolean noWeatherEnabled() {
        return noWeatherEnabled;
    }

    public static void setNoWeatherEnabled(boolean enabled) {
        noWeatherEnabled = enabled;
    }

    public static boolean noWeatherRain() {
        return noWeatherRain;
    }

    public static void setNoWeatherRain(boolean enabled) {
        noWeatherRain = enabled;
    }

    public static boolean noWeatherSnow() {
        return noWeatherSnow;
    }

    public static void setNoWeatherSnow(boolean enabled) {
        noWeatherSnow = enabled;
    }

    public static boolean noWeatherThunder() {
        return noWeatherThunder;
    }

    public static void setNoWeatherThunder(boolean enabled) {
        noWeatherThunder = enabled;
    }

    public static boolean timeChangerEnabled() {
        return timeChangerEnabled;
    }

    public static void setTimeChangerEnabled(boolean enabled) {
        timeChangerEnabled = enabled;
    }

    public static String timeChangerTime() {
        return timeChangerTime;
    }

    public static void setTimeChangerTime(String time) {
        if ("Client".equals(time)) {
            timeChangerTime = "World";
            return;
        }
        if ("World".equals(time)
                || "Night".equals(time)
                || "Sunset".equals(time)
                || "Midnight".equals(time)) {
            timeChangerTime = time;
            return;
        }
        timeChangerTime = "Day";
    }

    public static boolean ambienceEnabled() {
        return ambienceEnabled;
    }

    public static void setAmbienceEnabled(boolean enabled) {
        ambienceEnabled = enabled;
    }

    public static boolean ambiencePumpkinOverlay() {
        return ambiencePumpkinOverlay;
    }

    public static void setAmbiencePumpkinOverlay(boolean enabled) {
        ambiencePumpkinOverlay = enabled;
    }

    public static boolean ambiencePortalOverlay() {
        return ambiencePortalOverlay;
    }

    public static void setAmbiencePortalOverlay(boolean enabled) {
        ambiencePortalOverlay = enabled;
    }

    public static boolean ambienceDarknessPulse() {
        return ambienceDarknessPulse;
    }

    public static void setAmbienceDarknessPulse(boolean enabled) {
        ambienceDarknessPulse = enabled;
    }

    public static boolean antiBlindnessEnabled() {
        return antiBlindnessEnabled;
    }

    public static void setAntiBlindnessEnabled(boolean enabled) {
        antiBlindnessEnabled = enabled;
    }

    public static boolean antiBlindnessBlindness() {
        return antiBlindnessBlindness;
    }

    public static void setAntiBlindnessBlindness(boolean enabled) {
        antiBlindnessBlindness = enabled;
    }

    public static boolean antiBlindnessDarkness() {
        return antiBlindnessDarkness;
    }

    public static void setAntiBlindnessDarkness(boolean enabled) {
        antiBlindnessDarkness = enabled;
    }

    public static boolean antiBlindnessNausea() {
        return antiBlindnessNausea;
    }

    public static void setAntiBlindnessNausea(boolean enabled) {
        antiBlindnessNausea = enabled;
    }

    public static boolean freecamEnabled() {
        return freecamEnabled;
    }

    public static void setFreecamEnabled(boolean enabled) {
        if (freecamEnabled && !enabled) {
            FreecamService.resetState();
        }
        freecamEnabled = enabled;
    }

    public static float freecamSpeed() {
        return freecamSpeed;
    }

    public static float freecamSpeedSlider() {
        return ConfigValueCodec.inverse(freecamSpeed, VisualRanges.FREECAM_MIN_SPEED, VisualRanges.FREECAM_MAX_SPEED);
    }

    public static void setFreecamSpeedSlider(float value) {
        freecamSpeed = ConfigValueCodec.range(value, VisualRanges.FREECAM_MIN_SPEED, VisualRanges.FREECAM_MAX_SPEED);
    }

    public static boolean freecamNoClip() {
        return freecamNoClip;
    }

    public static void setFreecamNoClip(boolean enabled) {
        freecamNoClip = enabled;
    }

    public static boolean freecamFreezePlayer() {
        return freecamFreezePlayer;
    }

    public static void setFreecamFreezePlayer(boolean enabled) {
        if (!freecamFreezePlayer && enabled) {
            FreecamService.resetFreezeAnchor();
        }
        freecamFreezePlayer = enabled;
    }

    public static boolean autoToolEnabled() {
        return autoToolEnabled;
    }

    public static void setAutoToolEnabled(boolean enabled) {
        if (autoToolEnabled && !enabled) {
            AutoToolService.reset();
        }
        autoToolEnabled = enabled;
    }

    public static String autoToolMode() {
        return autoToolMode;
    }

    public static void setAutoToolMode(String mode) {
        autoToolMode = "Preserve Slot".equals(mode) ? "Preserve Slot" : "Best";
    }

    public static boolean autoToolSilent() {
        return autoToolSilent;
    }

    public static void setAutoToolSilent(boolean enabled) {
        if (autoToolSilent && !enabled) {
            AutoToolService.reset();
        }
        autoToolSilent = enabled;
    }

    public static boolean autoToolPreferDurability() {
        return autoToolPreferDurability;
    }

    public static void setAutoToolPreferDurability(boolean enabled) {
        autoToolPreferDurability = enabled;
    }

    public static boolean autoMineEnabled() {
        return autoMineEnabled;
    }

    public static void setAutoMineEnabled(boolean enabled) {
        if (autoMineEnabled && !enabled) {
            AutoMineService.reset();
        }
        autoMineEnabled = enabled;
    }

    public static float autoMineRange() {
        return autoMineRange;
    }

    public static String autoMineTarget() {
        return autoMineTarget;
    }

    public static void setAutoMineTarget(String target) {
        String normalized = "Locked Block".equals(target) ? "Locked Block" : "Crosshair";
        if (!autoMineTarget.equals(normalized)) {
            AutoMineService.reset();
        }
        autoMineTarget = normalized;
    }

    public static float autoMineRangeSlider() {
        return (autoMineRange - VisualRanges.AUTO_MINE_RANGE_MIN) / (VisualRanges.AUTO_MINE_RANGE_MAX - VisualRanges.AUTO_MINE_RANGE_MIN);
    }

    public static void setAutoMineRangeSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        autoMineRange = VisualRanges.AUTO_MINE_RANGE_MIN + (VisualRanges.AUTO_MINE_RANGE_MAX - VisualRanges.AUTO_MINE_RANGE_MIN) * clamped;
    }

    public static int autoMineDelayTicks() {
        return Math.round(autoMineDelay);
    }

    public static float autoMineDelaySlider() {
        return (autoMineDelay - VisualRanges.AUTO_MINE_DELAY_MIN) / (VisualRanges.AUTO_MINE_DELAY_MAX - VisualRanges.AUTO_MINE_DELAY_MIN);
    }

    public static void setAutoMineDelaySlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        autoMineDelay = VisualRanges.AUTO_MINE_DELAY_MIN + (VisualRanges.AUTO_MINE_DELAY_MAX - VisualRanges.AUTO_MINE_DELAY_MIN) * clamped;
    }

    public static boolean autoMineSwing() {
        return autoMineSwing;
    }

    public static void setAutoMineSwing(boolean enabled) {
        autoMineSwing = enabled;
    }

    public static boolean nukerEnabled() {
        return nukerEnabled;
    }

    public static void setNukerEnabled(boolean enabled) {
        if (nukerEnabled && !enabled) {
            NukerService.reset();
        }
        nukerEnabled = enabled;
    }

    public static String nukerTarget() {
        return nukerTarget;
    }

    public static void setNukerTarget(String target) {
        String normalized = switch (target) {
            case "Whitelist", "Same Block" -> target;
            default -> "Visible";
        };
        if (!nukerTarget.equals(normalized)) {
            NukerService.reset();
        }
        nukerTarget = normalized;
    }

    public static String nukerSortMode() {
        return nukerSortMode;
    }

    public static void setNukerSortMode(String mode) {
        nukerSortMode = switch (mode) {
            case "Farthest", "Top Down", "Bottom Up" -> mode;
            default -> "Closest";
        };
    }

    public static String nukerBlockIds() {
        return nukerBlockIds;
    }

    public static void setNukerBlockIds(String ids) {
        nukerBlockIds = ConfigValueCodec.normalizeIdList(ids);
    }

    public static boolean nukerBlockEnabled(String id) {
        return ConfigValueCodec.idListContains(nukerBlockIds, id);
    }

    public static float nukerRange() {
        return nukerRange;
    }

    public static float nukerRangeSlider() {
        return (nukerRange - VisualRanges.NUKER_RANGE_MIN) / (VisualRanges.NUKER_RANGE_MAX - VisualRanges.NUKER_RANGE_MIN);
    }

    public static void setNukerRangeSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        nukerRange = VisualRanges.NUKER_RANGE_MIN + (VisualRanges.NUKER_RANGE_MAX - VisualRanges.NUKER_RANGE_MIN) * clamped;
    }

    public static int nukerDelayTicks() {
        return Math.round(nukerDelay);
    }

    public static float nukerDelaySlider() {
        return (nukerDelay - VisualRanges.AUTO_MINE_DELAY_MIN) / (VisualRanges.AUTO_MINE_DELAY_MAX - VisualRanges.AUTO_MINE_DELAY_MIN);
    }

    public static void setNukerDelaySlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        nukerDelay = VisualRanges.AUTO_MINE_DELAY_MIN + (VisualRanges.AUTO_MINE_DELAY_MAX - VisualRanges.AUTO_MINE_DELAY_MIN) * clamped;
    }

    public static boolean nukerSwing() {
        return nukerExtensionEnabled(NUKER_EXTENSION_SWING_HAND);
    }

    public static boolean nukerPacketMine() {
        return nukerExtensionEnabled(NUKER_EXTENSION_PACKET_MINE);
    }

    public static boolean nukerRotate() {
        return nukerExtensionEnabled(NUKER_EXTENSION_ROTATE);
    }

    public static int nukerExtensionsMask() {
        return nukerExtensionsMask;
    }

    public static void setNukerExtensionsMask(int mask) {
        nukerExtensionsMask = mask & (NUKER_EXTENSION_SWING_HAND
                | NUKER_EXTENSION_PACKET_MINE
                | NUKER_EXTENSION_ROTATE
                | NUKER_EXTENSION_SKIP_FEET_LEVEL);
    }

    public static boolean nukerExtensionEnabled(int mask) {
        return (nukerExtensionsMask & mask) != 0;
    }

    public static void setNukerExtensionEnabled(int mask, boolean enabled) {
        if (enabled) {
            nukerExtensionsMask |= mask;
        } else {
            nukerExtensionsMask &= ~mask;
        }
    }
    public static boolean veinMinerEnabled() {
        return veinMinerEnabled;
    }

    public static void setVeinMinerEnabled(boolean enabled) {
        if (veinMinerEnabled && !enabled) {
            VeinMinerService.reset();
        }
        veinMinerEnabled = enabled;
    }

    public static String veinMinerMode() {
        return veinMinerMode;
    }

    public static void setVeinMinerMode(String mode) {
        String next = "Chain".equals(mode) ? "Chain" : "Basic";
        if (!veinMinerMode.equals(next)) {
            VeinMinerService.reset();
        }
        veinMinerMode = next;
    }

    public static String veinMinerFormat() {
        return veinMinerFormat;
    }

    public static void setVeinMinerFormat(String format) {
        if ("Small vein".equals(format)) {
            format = "Small Vein";
        }
        String next = switch (format) {
            case "Tunnel", "Small Vein", "Sphere", "Plane", "Stair" -> format;
            default -> "Connected";
        };
        if (!veinMinerFormat.equals(next)) {
            VeinMinerService.reset();
        }
        veinMinerFormat = next;
    }

    public static int veinMinerMaxBlocks() {
        return Math.round(veinMinerMaxBlocks);
    }

    public static float veinMinerMaxBlocksSlider() {
        return ConfigValueCodec.inverse(veinMinerMaxBlocks, VisualRanges.VEIN_MINER_MAX_BLOCKS_MIN, VisualRanges.VEIN_MINER_MAX_BLOCKS_MAX);
    }

    public static void setVeinMinerMaxBlocksSlider(float value) {
        float next = ConfigValueCodec.range(value, VisualRanges.VEIN_MINER_MAX_BLOCKS_MIN, VisualRanges.VEIN_MINER_MAX_BLOCKS_MAX);
        if (Math.round(veinMinerMaxBlocks) != Math.round(next)) {
            VeinMinerService.reset();
        }
        veinMinerMaxBlocks = next;
    }

    public static int veinMinerFilterMask() {
        return veinMinerFilterMask;
    }

    public static void setVeinMinerFilterMask(int mask) {
        int next = mask & VEIN_MINER_FILTER_ALL;
        if (veinMinerFilterMask != next) {
            VeinMinerService.reset();
        }
        veinMinerFilterMask = next;
        veinMinerOresOnly = (next & VEIN_MINER_FILTER_ORES) != 0
                && (next & ~VEIN_MINER_FILTER_ORES) == 0;
    }

    public static boolean veinMinerOresOnly() {
        return veinMinerOresOnly;
    }

    public static void setVeinMinerOresOnly(boolean enabled) {
        if (veinMinerOresOnly != enabled) {
            VeinMinerService.reset();
        }
        veinMinerOresOnly = enabled;
        veinMinerFilterMask = enabled ? VEIN_MINER_FILTER_ORES : VEIN_MINER_FILTER_ALL;
    }

    public static boolean veinMinerRotate() {
        return veinMinerRotate;
    }

    public static void setVeinMinerRotate(boolean enabled) {
        veinMinerRotate = enabled;
    }

    public static int veinMinerColor() {
        return veinMinerColor;
    }

    public static void setVeinMinerColor(int color) {
        veinMinerColor = color;
    }

    public static int veinMinerKeyCode() {
        return veinMinerKeyCode;
    }

    public static void setVeinMinerKeyCode(int keyCode) {
        veinMinerKeyCode = keyCode;
    }

    public static int veinMinerScanCode() {
        return veinMinerScanCode;
    }

    public static void setVeinMinerScanCode(int scanCode) {
        veinMinerScanCode = scanCode;
    }

    public static int veinMinerMouseButton() {
        return veinMinerMouseButton;
    }

    public static void setVeinMinerMouseButton(int mouseButton) {
        veinMinerMouseButton = mouseButton;
    }

    public static void bindVeinMinerKey(int keyCode, int scanCode) {
        veinMinerKeyCode = keyCode;
        veinMinerScanCode = scanCode;
        veinMinerMouseButton = -1;
    }

    public static void bindVeinMinerMouse(int mouseButton) {
        veinMinerKeyCode = GLFW.GLFW_KEY_UNKNOWN;
        veinMinerScanCode = 0;
        veinMinerMouseButton = mouseButton;
    }

    public static void clearVeinMinerKey() {
        veinMinerKeyCode = GLFW.GLFW_KEY_UNKNOWN;
        veinMinerScanCode = 0;
        veinMinerMouseButton = -1;
    }

    public static boolean zoomEnabled() {
        return zoomEnabled;
    }

    public static void setZoomEnabled(boolean enabled) {
        if (zoomEnabled && !enabled) {
            ZoomState.stop();
        }
        zoomEnabled = enabled;
    }

    public static float zoomFactor() {
        return zoomFactor;
    }

    public static float zoomFactorSlider() {
        return ConfigValueCodec.inverse(zoomFactor, VisualRanges.ZOOM_MIN_FACTOR, VisualRanges.ZOOM_MAX_FACTOR);
    }

    public static void setZoomFactorSlider(float value) {
        zoomFactor = ConfigValueCodec.range(value, VisualRanges.ZOOM_MIN_FACTOR, VisualRanges.ZOOM_MAX_FACTOR);
    }

    public static boolean zoomSmooth() {
        return zoomSmooth;
    }

    public static void setZoomSmooth(boolean enabled) {
        zoomSmooth = enabled;
    }

    public static boolean zoomCinematic() {
        return zoomCinematic;
    }

    public static void setZoomCinematic(boolean enabled) {
        zoomCinematic = enabled;
    }

    public static boolean zoomShowHands() {
        return zoomShowHands;
    }

    public static void setZoomShowHands(boolean enabled) {
        zoomShowHands = enabled;
    }

    public static boolean aspectRatioEnabled() {
        return aspectRatioEnabled;
    }

    public static void setAspectRatioEnabled(boolean enabled) {
        aspectRatioEnabled = enabled;
    }

    public static float aspectRatioValue() {
        float value = switch (aspectRatioPreset) {
            case "16:9" -> 16.0f / 9.0f;
            case "4:3" -> 4.0f / 3.0f;
            case "16:10" -> 16.0f / 10.0f;
            case "21:9" -> 21.0f / 9.0f;
            case "32:9" -> 32.0f / 9.0f;
            default -> aspectRatioCustomValue;
        };
        return Math.max(VisualRanges.ASPECT_RATIO_MIN,
                Math.min(VisualRanges.ASPECT_RATIO_MAX, value));
    }

    public static float aspectRatioSlider() {
        return ConfigValueCodec.inverse(aspectRatioCustomValue, VisualRanges.ASPECT_RATIO_MIN, VisualRanges.ASPECT_RATIO_MAX);
    }

    public static void setAspectRatioSlider(float value) {
        aspectRatioPreset = "Custom";
        aspectRatioCustomValue = ConfigValueCodec.range(value, VisualRanges.ASPECT_RATIO_MIN, VisualRanges.ASPECT_RATIO_MAX);
    }

    public static String aspectRatioPreset() {
        return switch (aspectRatioPreset) {
            case "4:3", "16:10", "21:9", "32:9", "Custom" -> aspectRatioPreset;
            default -> "16:9";
        };
    }

    public static void setAspectRatioPreset(String preset) {
        aspectRatioPreset = switch (preset) {
            case "4:3", "16:10", "21:9", "32:9", "Custom" -> preset;
            default -> "16:9";
        };
        if (!"Custom".equals(aspectRatioPreset)) {
            aspectRatioCustomValue = aspectRatioValue();
        }
    }

    public static boolean viewModelEnabled() {
        return viewModelEnabled;
    }

    public static void setViewModelEnabled(boolean enabled) {
        viewModelEnabled = enabled;
    }

    public static float viewModelScale() {
        return Math.max(VisualRanges.VIEW_MODEL_SCALE_MIN,
                Math.min(VisualRanges.VIEW_MODEL_SCALE_MAX, viewModelScale));
    }

    public static float viewModelScaleSlider() {
        return ConfigValueCodec.inverse(viewModelScale(), VisualRanges.VIEW_MODEL_SCALE_MIN, VisualRanges.VIEW_MODEL_SCALE_MAX);
    }

    public static void setViewModelScaleSlider(float value) {
        viewModelScale = ConfigValueCodec.range(value, VisualRanges.VIEW_MODEL_SCALE_MIN, VisualRanges.VIEW_MODEL_SCALE_MAX);
    }

    public static float viewModelOffsetX() {
        return Math.max(VisualRanges.VIEW_MODEL_OFFSET_MIN,
                Math.min(VisualRanges.VIEW_MODEL_OFFSET_MAX, viewModelOffsetX));
    }

    public static float viewModelOffsetXSlider() {
        return ConfigValueCodec.inverse(viewModelOffsetX(), VisualRanges.VIEW_MODEL_OFFSET_MIN, VisualRanges.VIEW_MODEL_OFFSET_MAX);
    }

    public static void setViewModelOffsetXSlider(float value) {
        viewModelOffsetX = ConfigValueCodec.range(value, VisualRanges.VIEW_MODEL_OFFSET_MIN, VisualRanges.VIEW_MODEL_OFFSET_MAX);
    }

    public static float viewModelOffsetY() {
        return Math.max(VisualRanges.VIEW_MODEL_OFFSET_MIN,
                Math.min(VisualRanges.VIEW_MODEL_OFFSET_MAX, viewModelOffsetY));
    }

    public static float viewModelOffsetYSlider() {
        return ConfigValueCodec.inverse(viewModelOffsetY(), VisualRanges.VIEW_MODEL_OFFSET_MIN, VisualRanges.VIEW_MODEL_OFFSET_MAX);
    }

    public static void setViewModelOffsetYSlider(float value) {
        viewModelOffsetY = ConfigValueCodec.range(value, VisualRanges.VIEW_MODEL_OFFSET_MIN, VisualRanges.VIEW_MODEL_OFFSET_MAX);
    }

    public static float viewModelOffsetZ() {
        return Math.max(VisualRanges.VIEW_MODEL_OFFSET_MIN,
                Math.min(VisualRanges.VIEW_MODEL_OFFSET_MAX, viewModelOffsetZ));
    }

    public static float viewModelOffsetZSlider() {
        return ConfigValueCodec.inverse(viewModelOffsetZ(), VisualRanges.VIEW_MODEL_OFFSET_MIN, VisualRanges.VIEW_MODEL_OFFSET_MAX);
    }

    public static void setViewModelOffsetZSlider(float value) {
        viewModelOffsetZ = ConfigValueCodec.range(value, VisualRanges.VIEW_MODEL_OFFSET_MIN, VisualRanges.VIEW_MODEL_OFFSET_MAX);
    }

    public static boolean quickStopEnabled() {
        return quickStopEnabled;
    }

    public static void setQuickStopEnabled(boolean enabled) {
        quickStopEnabled = enabled;
    }

    public static boolean bhopTurnEnabled() {
        return bhopTurnEnabled;
    }

    public static void setBhopTurnEnabled(boolean enabled) {
        bhopTurnEnabled = enabled;
    }

    public static boolean noFallEnabled() {
        return noFallEnabled;
    }

    public static void setNoFallEnabled(boolean enabled) {
        noFallEnabled = enabled;
    }

    public static String noFallMode() {
        return noFallMode;
    }

    public static void setNoFallMode(String mode) {
        noFallMode = "Watchdog".equals(mode) ? "Watchdog" : "Spoof";
    }

    public static boolean noFallNoGround() {
        return noFallNoGround;
    }

    public static void setNoFallNoGround(boolean enabled) {
        noFallNoGround = enabled;
    }

    public static boolean fastLadderEnabled() {
        return fastLadderEnabled;
    }

    public static void setFastLadderEnabled(boolean enabled) {
        fastLadderEnabled = enabled;
    }

    public static boolean noSlowEnabled() {
        return noSlowEnabled;
    }

    public static void setNoSlowEnabled(boolean enabled) {
        noSlowEnabled = enabled;
    }

    public static String noSlowMode() {
        return noSlowMode;
    }

    public static void setNoSlowMode(String mode) {
        if ("GrimJump".equals(mode) || "GrimFast".equals(mode)
                || "Watchdog".equals(mode) || "Universal".equals(mode)) {
            noSlowMode = mode;
        } else {
            noSlowMode = "Vanilla";
        }
    }

    public static boolean noSlowSlowDown() {
        return noSlowSlowDown;
    }

    public static void setNoSlowSlowDown(boolean enabled) {
        noSlowSlowDown = enabled;
    }

    public static boolean noSlowKeepSprinting() {
        return noSlowKeepSprinting;
    }

    public static void setNoSlowKeepSprinting(boolean enabled) {
        noSlowKeepSprinting = enabled;
    }

    public static boolean noSlowCancelSlowdown() {
        if (!noSlowEnabled) {
            return false;
        }
        if ("Universal".equals(noSlowMode) && noSlowSlowDown) {
            return false;
        }
        return true;
    }

    public static boolean safeWalkEnabled() {
        return safeWalkEnabled;
    }

    public static void setSafeWalkEnabled(boolean enabled) {
        safeWalkEnabled = enabled;
    }

    public static String safeWalkMode() {
        return safeWalkMode;
    }

    public static void setSafeWalkMode(String mode) {
        safeWalkMode = "Freeze".equals(mode) ? "Freeze" : "Normal";
    }

    public static boolean edgeJumpEnabled() {
        return edgeJumpEnabled;
    }

    public static void setEdgeJumpEnabled(boolean enabled) {
        edgeJumpEnabled = enabled;
    }

    public static boolean inventoryMoveEnabled() {
        return inventoryMoveEnabled;
    }

    public static void setInventoryMoveEnabled(boolean enabled) {
        inventoryMoveEnabled = enabled;
    }

    public static String inventoryMoveMode() {
        return inventoryMoveMode;
    }

    public static void setInventoryMoveMode(String mode) {
        inventoryMoveMode = "Heypixel".equals(mode) ? "Heypixel" : "Normal";
    }

    public static boolean pearlThrowEnabled() {
        return pearlThrowEnabled;
    }

    public static void setPearlThrowEnabled(boolean enabled) {
        pearlThrowEnabled = enabled;
    }

    public static int pearlThrowKeyCode() {
        return pearlThrowKeyCode;
    }

    public static int pearlThrowScanCode() {
        return pearlThrowScanCode;
    }

    public static int pearlThrowMouseButton() {
        return pearlThrowMouseButton;
    }

    public static void bindPearlThrowKey(int keyCode, int scanCode) {
        pearlThrowKeyCode = keyCode;
        pearlThrowScanCode = scanCode;
        pearlThrowMouseButton = -1;
    }

    public static void bindPearlThrowMouse(int mouseButton) {
        pearlThrowMouseButton = mouseButton;
        pearlThrowKeyCode = GLFW.GLFW_KEY_UNKNOWN;
        pearlThrowScanCode = 0;
    }

    public static void clearPearlThrowKey() {
        pearlThrowKeyCode = GLFW.GLFW_KEY_UNKNOWN;
        pearlThrowScanCode = 0;
        pearlThrowMouseButton = -1;
    }

    public static float pearlThrowDelay() {
        return pearlThrowDelay;
    }

    public static float pearlThrowDelaySlider() {
        return ConfigValueCodec.inverse(pearlThrowDelay, 0.0f, 20.0f);
    }

    public static void setPearlThrowDelaySlider(float value) {
        pearlThrowDelay = ConfigValueCodec.range(value, 0.0f, 20.0f);
    }

    public static String pearlThrowMode() {
        return pearlThrowMode;
    }

    public static void setPearlThrowMode(String mode) {
        pearlThrowMode = "Switch".equals(mode) ? "Switch" : "Silent";
    }

    public static float pearlThrowSwitchDelay() {
        return pearlThrowSwitchDelay;
    }

    public static float pearlThrowSwitchDelaySlider() {
        return ConfigValueCodec.inverse(pearlThrowSwitchDelay, 0.0f, 10.0f);
    }

    public static void setPearlThrowSwitchDelaySlider(float value) {
        pearlThrowSwitchDelay = ConfigValueCodec.range(value, 0.0f, 10.0f);
    }

    public static boolean pearlThrowSwing() {
        return pearlThrowSwing;
    }

    public static void setPearlThrowSwing(boolean enabled) {
        pearlThrowSwing = enabled;
    }

    public static boolean quickSwitchEnabled() {
        return quickSwitchEnabled;
    }

    public static void setQuickSwitchEnabled(boolean enabled) {
        quickSwitchEnabled = enabled;
    }

    public static int quickSwitchKeyCode() {
        return quickSwitchKeyCode;
    }

    public static int quickSwitchScanCode() {
        return quickSwitchScanCode;
    }

    public static int quickSwitchMouseButton() {
        return quickSwitchMouseButton;
    }

    public static void bindQuickSwitchKey(int keyCode, int scanCode) {
        quickSwitchKeyCode = keyCode;
        quickSwitchScanCode = scanCode;
        quickSwitchMouseButton = -1;
    }

    public static void bindQuickSwitchMouse(int mouseButton) {
        quickSwitchMouseButton = mouseButton;
        quickSwitchKeyCode = GLFW.GLFW_KEY_UNKNOWN;
        quickSwitchScanCode = 0;
    }

    public static void clearQuickSwitchKey() {
        quickSwitchKeyCode = GLFW.GLFW_KEY_UNKNOWN;
        quickSwitchScanCode = 0;
        quickSwitchMouseButton = -1;
    }

    public static float quickSwitchSlot() {
        return quickSwitchSlot;
    }

    public static int quickSwitchSlotIndex() {
        return Math.max(0, Math.min(8, Math.round(quickSwitchSlot) - 1));
    }

    public static float quickSwitchSlotSlider() {
        return ConfigValueCodec.inverse(quickSwitchSlot, 1.0f, 9.0f);
    }

    public static void setQuickSwitchSlotSlider(float value) {
        quickSwitchSlot = ConfigValueCodec.range(value, 1.0f, 9.0f);
    }

    public static String quickSwitchMode() {
        return quickSwitchMode;
    }

    public static void setQuickSwitchMode(String mode) {
        quickSwitchMode = "Toggle".equals(mode) ? "Toggle" : "Hold";
    }

    public static boolean quickSwitchRestoreSlot() {
        return quickSwitchRestoreSlot;
    }

    public static void setQuickSwitchRestoreSlot(boolean enabled) {
        quickSwitchRestoreSlot = enabled;
    }

    public static boolean fakePlayerEnabled() {
        return fakePlayerEnabled;
    }

    public static void setFakePlayerEnabled(boolean enabled) {
        fakePlayerEnabled = enabled;
    }

    public static String fakePlayerName() {
        return fakePlayerName;
    }

    public static void setFakePlayerName(String name) {
        if (name == null) {
            fakePlayerName = "Dummy";
            return;
        }
        String clean = name.strip();
        if (clean.isBlank()) {
            fakePlayerName = "Dummy";
        } else {
            fakePlayerName = clean.length() > 16 ? clean.substring(0, 16) : clean;
        }
    }

    public static float fakePlayerHealth() {
        return fakePlayerHealth;
    }

    public static float fakePlayerHealthSlider() {
        return ConfigValueCodec.inverse(fakePlayerHealth, 1.0f, 40.0f);
    }

    public static void setFakePlayerHealthSlider(float value) {
        fakePlayerHealth = ConfigValueCodec.range(value, 1.0f, 40.0f);
    }

    public static boolean fakePlayerFacePlayer() {
        return fakePlayerFacePlayer;
    }

    public static void setFakePlayerFacePlayer(boolean enabled) {
        fakePlayerFacePlayer = enabled;
    }

    public static boolean fakePlayerHitFeedback() {
        return fakePlayerHitFeedback;
    }

    public static void setFakePlayerHitFeedback(boolean enabled) {
        fakePlayerHitFeedback = enabled;
    }

    public static boolean fakePlayerReturnHome() {
        return fakePlayerReturnHome;
    }

    public static void setFakePlayerReturnHome(boolean enabled) {
        fakePlayerReturnHome = enabled;
    }

    public static boolean soundControlEnabled() {
        return soundControlEnabled;
    }

    public static void setSoundControlEnabled(boolean enabled) {
        soundControlEnabled = enabled;
    }

    public static int soundControlMuteMask() {
        return soundControlMuteMask & SOUND_CONTROL_ALL;
    }

    public static void setSoundControlMuteMask(int mask) {
        soundControlMuteMask = mask & SOUND_CONTROL_ALL;
    }

    public static boolean soundControlMutes(SoundSource source) {
        if (!soundControlEnabled) {
            return false;
        }
        int mask = soundControlMuteMask();
        if ((mask & SOUND_CONTROL_MASTER) != 0) {
            return true;
        }
        return switch (source) {
            case MUSIC -> (mask & SOUND_CONTROL_MUSIC) != 0;
            case RECORDS -> (mask & SOUND_CONTROL_RECORDS) != 0;
            case WEATHER -> (mask & SOUND_CONTROL_WEATHER) != 0;
            case BLOCKS -> (mask & SOUND_CONTROL_BLOCKS) != 0;
            case HOSTILE -> (mask & SOUND_CONTROL_HOSTILE) != 0;
            case NEUTRAL -> (mask & SOUND_CONTROL_NEUTRAL) != 0;
            case PLAYERS -> (mask & SOUND_CONTROL_PLAYERS) != 0;
            case AMBIENT -> (mask & SOUND_CONTROL_AMBIENT) != 0;
            case VOICE -> (mask & SOUND_CONTROL_VOICE) != 0;
            default -> false;
        };
    }

    public static boolean hitSoundsEnabled() {
        return hitSoundsEnabled;
    }

    public static void setHitSoundsEnabled(boolean enabled) {
        hitSoundsEnabled = enabled;
    }

    public static String hitSoundsMode() {
        return hitSoundsMode;
    }

    public static void setHitSoundsMode(String mode) {
        hitSoundsMode = switch (mode) {
            case "Pling", "Bell", "Click", "Pop", "Crit" -> mode;
            default -> "Orb";
        };
    }

    public static float hitSoundsVolume() {
        return hitSoundsVolume;
    }

    public static float hitSoundsVolumeSlider() {
        return ConfigValueCodec.inverse(hitSoundsVolume, 0.1f, 2.0f);
    }

    public static void setHitSoundsVolumeSlider(float value) {
        hitSoundsVolume = ConfigValueCodec.range(value, 0.1f, 2.0f);
    }

    public static float hitSoundsPitch() {
        return hitSoundsPitch;
    }

    public static float hitSoundsPitchSlider() {
        return ConfigValueCodec.inverse(hitSoundsPitch, 0.5f, 2.0f);
    }

    public static void setHitSoundsPitchSlider(float value) {
        hitSoundsPitch = ConfigValueCodec.range(value, 0.5f, 2.0f);
    }

    public static boolean betterChatEnabled() {
        return betterChatEnabled;
    }

    public static void setBetterChatEnabled(boolean enabled) {
        betterChatEnabled = enabled;
    }

    public static String dashboardGuiScale() {
        return dashboardGuiScale;
    }

    public static void setDashboardGuiScale(String value) {
        String normalized = value == null ? "" : value.trim();
        if ("75%".equals(normalized) || "100%".equals(normalized) || "125%".equals(normalized)
                || "150%".equals(normalized) || "175%".equals(normalized) || "200%".equals(normalized)) {
            dashboardGuiScale = normalized;
        } else {
            dashboardGuiScale = "100%";
        }
    }

    public static float dashboardGuiScaleFactor() {
        return switch (dashboardGuiScale) {
            case "75%" -> 0.75f;
            case "125%" -> 1.25f;
            case "150%" -> 1.5f;
            case "175%" -> 1.75f;
            case "200%" -> 2.0f;
            default -> 1.0f;
        };
    }

    public static int betterChatBackgroundColor() {
        return betterChatBackgroundColor;
    }

    public static void setBetterChatBackgroundColor(int color) {
        betterChatBackgroundColor = color;
    }

    public static boolean betterChatHideVanillaBackground() {
        return betterChatHideVanillaBackground;
    }

    public static void setBetterChatHideVanillaBackground(boolean enabled) {
        betterChatHideVanillaBackground = enabled;
    }

    public static boolean betterChatCompact() {
        return betterChatCompact;
    }

    public static void setBetterChatCompact(boolean enabled) {
        betterChatCompact = enabled;
    }

    public static boolean fastPlaceEnabled() {
        return fastPlaceEnabled;
    }

    public static void setFastPlaceEnabled(boolean enabled) {
        fastPlaceEnabled = enabled;
    }

    public static boolean antiHungerEnabled() {
        return antiHungerEnabled;
    }

    public static void setAntiHungerEnabled(boolean enabled) {
        antiHungerEnabled = enabled;
    }

    public static boolean autoRespawnEnabled() {
        return autoRespawnEnabled;
    }

    public static void setAutoRespawnEnabled(boolean enabled) {
        autoRespawnEnabled = enabled;
    }

    public static boolean keepSprintEnabled() {
        return keepSprintEnabled;
    }

    public static void setKeepSprintEnabled(boolean enabled) {
        keepSprintEnabled = enabled;
    }

    public static float keepSprintScale() {
        return keepSprintScale;
    }

    public static float keepSprintScaleSlider() {
        return (keepSprintScale - VisualRanges.KEEP_SPRINT_SCALE_MIN) / (VisualRanges.KEEP_SPRINT_SCALE_MAX - VisualRanges.KEEP_SPRINT_SCALE_MIN);
    }

    public static void setKeepSprintScaleSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        keepSprintScale = VisualRanges.KEEP_SPRINT_SCALE_MIN + (VisualRanges.KEEP_SPRINT_SCALE_MAX - VisualRanges.KEEP_SPRINT_SCALE_MIN) * clamped;
    }

    public static float keepSprintSlowdown() {
        return keepSprintSlowdown;
    }

    public static float keepSprintSlowdownSlider() {
        return (keepSprintSlowdown - VisualRanges.KEEP_SPRINT_SLOWDOWN_MIN) / (VisualRanges.KEEP_SPRINT_SLOWDOWN_MAX - VisualRanges.KEEP_SPRINT_SLOWDOWN_MIN);
    }

    public static void setKeepSprintSlowdownSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        keepSprintSlowdown = VisualRanges.KEEP_SPRINT_SLOWDOWN_MIN + (VisualRanges.KEEP_SPRINT_SLOWDOWN_MAX - VisualRanges.KEEP_SPRINT_SLOWDOWN_MIN) * clamped;
    }

    public static boolean keepSprintGroundOnly() {
        return keepSprintGroundOnly;
    }

    public static void setKeepSprintGroundOnly(boolean enabled) {
        keepSprintGroundOnly = enabled;
    }

    public static boolean keepSprintPrediction() {
        return keepSprintPrediction;
    }

    public static void setKeepSprintPrediction(boolean enabled) {
        keepSprintPrediction = enabled;
    }

    public static boolean keepSprintReachOnly() {
        return keepSprintReachOnly;
    }

    public static void setKeepSprintReachOnly(boolean enabled) {
        keepSprintReachOnly = enabled;
    }

    public static boolean speedEnabled() {
        return speedEnabled;
    }

    public static void setSpeedEnabled(boolean enabled) {
        speedEnabled = enabled;
    }

    public static String speedMode() {
        return speedMode;
    }

    public static void setSpeedMode(String mode) {
        speedMode = switch (mode) {
            case "Vanilla", "BHop", "CubeCraftFast", "CubeCraftLowHop" -> mode;
            default -> "Strafe";
        };
    }

    public static float speedGroundMultiplier() {
        return speedGroundMultiplier;
    }

    public static float speedGroundMultiplierSlider() {
        return (speedGroundMultiplier - VisualRanges.SPEED_MULTIPLIER_MIN) / (VisualRanges.SPEED_MULTIPLIER_MAX - VisualRanges.SPEED_MULTIPLIER_MIN);
    }

    public static void setSpeedGroundMultiplierSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        speedGroundMultiplier = VisualRanges.SPEED_MULTIPLIER_MIN + (VisualRanges.SPEED_MULTIPLIER_MAX - VisualRanges.SPEED_MULTIPLIER_MIN) * clamped;
    }

    public static float speedAirMultiplier() {
        return speedAirMultiplier;
    }

    public static float speedAirMultiplierSlider() {
        return (speedAirMultiplier - VisualRanges.SPEED_MULTIPLIER_MIN) / (VisualRanges.SPEED_MULTIPLIER_MAX - VisualRanges.SPEED_MULTIPLIER_MIN);
    }

    public static void setSpeedAirMultiplierSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        speedAirMultiplier = VisualRanges.SPEED_MULTIPLIER_MIN + (VisualRanges.SPEED_MULTIPLIER_MAX - VisualRanges.SPEED_MULTIPLIER_MIN) * clamped;
    }

    public static boolean speedAutoJump() {
        return speedAutoJump;
    }

    public static void setSpeedAutoJump(boolean enabled) {
        speedAutoJump = enabled;
    }

    public static boolean speedOnlySprint() {
        return speedOnlySprint;
    }

    public static void setSpeedOnlySprint(boolean enabled) {
        speedOnlySprint = enabled;
    }

    public static boolean flightEnabled() {
        return flightEnabled;
    }

    public static void setFlightEnabled(boolean enabled) {
        flightEnabled = enabled;
    }

    public static String flightMode() {
        return flightMode;
    }

    public static void setFlightMode(String mode) {
        switch (mode == null ? "" : mode) {
            case "Glide", "CubeCraftAirWalk", "CubeCraftFast", "CubeCraftPingSpoof" -> flightMode = mode;
            default -> flightMode = "Vanilla";
        }
    }

    public static float flightCubeCraftSpeed() {
        return flightCubeCraftSpeed;
    }

    public static float flightCubeCraftSpeedSlider() {
        return ConfigValueCodec.inverse(flightCubeCraftSpeed, 0.1F, 10.0F);
    }

    public static void setFlightCubeCraftSpeedSlider(float value) {
        flightCubeCraftSpeed = ConfigValueCodec.range(value, 0.1F, 10.0F);
    }

    public static float flightCubeCraftVertical() {
        return flightCubeCraftVertical;
    }

    public static float flightCubeCraftVerticalSlider() {
        return ConfigValueCodec.inverse(flightCubeCraftVertical, 0.1F, 5.0F);
    }

    public static void setFlightCubeCraftVerticalSlider(float value) {
        flightCubeCraftVertical = ConfigValueCodec.range(value, 0.1F, 5.0F);
    }

    public static float flightCubeCraftAirWalkSpeed() {
        return flightCubeCraftAirWalkSpeed;
    }

    public static float flightCubeCraftAirWalkSpeedSlider() {
        return ConfigValueCodec.inverse(flightCubeCraftAirWalkSpeed, 0.1F, 1.0F);
    }

    public static void setFlightCubeCraftAirWalkSpeedSlider(float value) {
        flightCubeCraftAirWalkSpeed = ConfigValueCodec.range(value, 0.1F, 1.0F);
    }

    public static boolean flightCubeCraftConstantSpeed() {
        return flightCubeCraftConstantSpeed;
    }

    public static void setFlightCubeCraftConstantSpeed(boolean enabled) {
        flightCubeCraftConstantSpeed = enabled;
    }

    public static int flightCubeCraftReboostTicks() {
        return flightCubeCraftReboostTicks;
    }

    public static float flightCubeCraftReboostSlider() {
        return ConfigValueCodec.inverse(flightCubeCraftReboostTicks, 10.0F, 50.0F);
    }

    public static void setFlightCubeCraftReboostSlider(float value) {
        flightCubeCraftReboostTicks = Math.round(ConfigValueCodec.range(value, 10.0F, 50.0F));
    }

    public static boolean flightCubeCraftBoostOnce() {
        return flightCubeCraftBoostOnce;
    }

    public static void setFlightCubeCraftBoostOnce(boolean enabled) {
        flightCubeCraftBoostOnce = enabled;
    }

    public static boolean flightCubeCraftNostalgia() {
        return flightCubeCraftNostalgia;
    }

    public static void setFlightCubeCraftNostalgia(boolean enabled) {
        flightCubeCraftNostalgia = enabled;
    }

    public static float speedCubeCraftFastSpeed() {
        return speedCubeCraftFastSpeed;
    }

    public static float speedCubeCraftFastSpeedSlider() {
        return ConfigValueCodec.inverse(speedCubeCraftFastSpeed, 1.0F, 5.0F);
    }

    public static void setSpeedCubeCraftFastSpeedSlider(float value) {
        speedCubeCraftFastSpeed = ConfigValueCodec.range(value, 1.0F, 5.0F);
    }

    public static boolean speedCubeCraftLagBackCheck() {
        return speedCubeCraftLagBackCheck;
    }

    public static void setSpeedCubeCraftLagBackCheck(boolean enabled) {
        speedCubeCraftLagBackCheck = enabled;
    }

    public static boolean speedCubeCraftLowHopGlide() {
        return speedCubeCraftLowHopGlide;
    }

    public static void setSpeedCubeCraftLowHopGlide(boolean enabled) {
        speedCubeCraftLowHopGlide = enabled;
    }

    public static boolean timerEnabled() {
        return timerEnabled;
    }

    public static void setTimerEnabled(boolean enabled) {
        timerEnabled = enabled;
    }

    public static float timerSpeed() {
        return timerSpeed;
    }

    public static float timerSpeedSlider() {
        return ConfigValueCodec.inverse(timerSpeed, 0.05F, 10.0F);
    }

    public static void setTimerSpeedSlider(float value) {
        timerSpeed = ConfigValueCodec.range(value, 0.05F, 10.0F);
    }

    public static boolean movementFixEnabled() {
        return movementFixEnabled;
    }

    public static void setMovementFixEnabled(boolean enabled) {
        movementFixEnabled = enabled;
    }

    public static String movementFixMode() {
        return movementFixMode;
    }

    public static void setMovementFixMode(String mode) {
        movementFixMode = "SprintOnly".equals(mode) ? "SprintOnly" : "Normal";
    }

    public static boolean movementFixPacketOnly() {
        return movementFixPacketOnly;
    }

    public static void setMovementFixPacketOnly(boolean enabled) {
        movementFixPacketOnly = enabled;
    }

    public static boolean fastWebEnabled() {
        return fastWebEnabled;
    }

    public static void setFastWebEnabled(boolean enabled) {
        fastWebEnabled = enabled;
    }

    public static boolean antiStaffEnabled() {
        return antiStaffEnabled;
    }

    public static void setAntiStaffEnabled(boolean enabled) {
        antiStaffEnabled = enabled;
    }

    public static String antiStaffCommand() {
        return antiStaffCommand;
    }

    public static void setAntiStaffCommand(String command) {
        antiStaffCommand = command == null || command.isBlank() ? "hub" : command.trim();
    }

    public static boolean autoGGEnabled() {
        return autoGGEnabled;
    }

    public static void setAutoGGEnabled(boolean enabled) {
        autoGGEnabled = enabled;
    }

    public static String autoGGMessage() {
        return autoGGMessage;
    }

    public static void setAutoGGMessage(String message) {
        autoGGMessage = message == null || message.isBlank() ? "gg" : message.trim();
    }

    public static boolean autoLeaveOnBanEnabled() {
        return autoLeaveOnBanEnabled;
    }

    public static void setAutoLeaveOnBanEnabled(boolean enabled) {
        autoLeaveOnBanEnabled = enabled;
    }

    public static boolean fastBreakEnabled() {
        return fastBreakEnabled;
    }

    public static void setFastBreakEnabled(boolean enabled) {
        fastBreakEnabled = enabled;
    }

    public static int fastBreakSpeedPercent() {
        return fastBreakSpeedPercent;
    }

    public static float fastBreakSpeedSlider() {
        return ConfigValueCodec.inverse(fastBreakSpeedPercent, 1.0F, 99.0F);
    }

    public static void setFastBreakSpeedSlider(float value) {
        fastBreakSpeedPercent = Math.round(ConfigValueCodec.range(value, 1.0F, 99.0F));
    }

    public static boolean fastBreakCooldownEnabled() {
        return fastBreakCooldownEnabled;
    }

    public static void setFastBreakCooldownEnabled(boolean enabled) {
        fastBreakCooldownEnabled = enabled;
    }

    public static int fastBreakCooldown() {
        return fastBreakCooldown;
    }

    public static float fastBreakCooldownSlider() {
        return ConfigValueCodec.inverse(fastBreakCooldown, 0.0F, 5.0F);
    }

    public static void setFastBreakCooldownSlider(float value) {
        fastBreakCooldown = Math.round(ConfigValueCodec.range(value, 0.0F, 5.0F));
    }

    public static boolean breakerEnabled() {
        return breakerEnabled;
    }

    public static void setBreakerEnabled(boolean enabled) {
        breakerEnabled = enabled;
    }

    public static float breakerRange() {
        return breakerRange;
    }

    public static float breakerRangeSlider() {
        return ConfigValueCodec.inverse(breakerRange, 0.5F, 6.0F);
    }

    public static void setBreakerRangeSlider(float value) {
        breakerRange = ConfigValueCodec.range(value, 0.5F, 6.0F);
    }

    public static boolean breakerBreakSurroundings() {
        return breakerBreakSurroundings;
    }

    public static void setBreakerBreakSurroundings(boolean enabled) {
        breakerBreakSurroundings = enabled;
    }

    public static String breakerSwingMode() {
        return breakerSwingMode;
    }

    public static void setBreakerSwingMode(String mode) {
        breakerSwingMode = "Server".equals(mode) ? "Server" : "Client";
    }

    public static float flightHorizontalSpeed() {
        return flightHorizontalSpeed;
    }

    public static float flightHorizontalSpeedSlider() {
        return (flightHorizontalSpeed - VisualRanges.FLIGHT_HORIZONTAL_MIN) / (VisualRanges.FLIGHT_HORIZONTAL_MAX - VisualRanges.FLIGHT_HORIZONTAL_MIN);
    }

    public static void setFlightHorizontalSpeedSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        flightHorizontalSpeed = VisualRanges.FLIGHT_HORIZONTAL_MIN + (VisualRanges.FLIGHT_HORIZONTAL_MAX - VisualRanges.FLIGHT_HORIZONTAL_MIN) * clamped;
    }

    public static float flightVerticalSpeed() {
        return flightVerticalSpeed;
    }

    public static float flightVerticalSpeedSlider() {
        return (flightVerticalSpeed - VisualRanges.FLIGHT_VERTICAL_MIN) / (VisualRanges.FLIGHT_VERTICAL_MAX - VisualRanges.FLIGHT_VERTICAL_MIN);
    }

    public static void setFlightVerticalSpeedSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        flightVerticalSpeed = VisualRanges.FLIGHT_VERTICAL_MIN + (VisualRanges.FLIGHT_VERTICAL_MAX - VisualRanges.FLIGHT_VERTICAL_MIN) * clamped;
    }

    public static float flightGlide() {
        return flightGlide;
    }

    public static float flightGlideSlider() {
        return (flightGlide - VisualRanges.FLIGHT_GLIDE_MIN) / (VisualRanges.FLIGHT_GLIDE_MAX - VisualRanges.FLIGHT_GLIDE_MIN);
    }

    public static void setFlightGlideSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        flightGlide = VisualRanges.FLIGHT_GLIDE_MIN + (VisualRanges.FLIGHT_GLIDE_MAX - VisualRanges.FLIGHT_GLIDE_MIN) * clamped;
    }

    public static boolean elytraFlyEnabled() {
        return elytraFlyEnabled;
    }

    public static void setElytraFlyEnabled(boolean enabled) {
        elytraFlyEnabled = enabled;
    }

    public static String elytraFlyMode() {
        return elytraFlyMode;
    }

    public static void setElytraFlyMode(String mode) {
        elytraFlyMode = "Boost".equals(mode) ? "Boost" : "Control";
    }

    public static float elytraFlyHorizontalSpeed() {
        return elytraFlyHorizontalSpeed;
    }

    public static float elytraFlyHorizontalSpeedSlider() {
        return (elytraFlyHorizontalSpeed - VisualRanges.ELYTRA_FLY_HORIZONTAL_MIN) / (VisualRanges.ELYTRA_FLY_HORIZONTAL_MAX - VisualRanges.ELYTRA_FLY_HORIZONTAL_MIN);
    }

    public static void setElytraFlyHorizontalSpeedSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        elytraFlyHorizontalSpeed = VisualRanges.ELYTRA_FLY_HORIZONTAL_MIN + (VisualRanges.ELYTRA_FLY_HORIZONTAL_MAX - VisualRanges.ELYTRA_FLY_HORIZONTAL_MIN) * clamped;
    }

    public static float elytraFlyVerticalSpeed() {
        return elytraFlyVerticalSpeed;
    }

    public static float elytraFlyVerticalSpeedSlider() {
        return (elytraFlyVerticalSpeed - VisualRanges.ELYTRA_FLY_VERTICAL_MIN) / (VisualRanges.ELYTRA_FLY_VERTICAL_MAX - VisualRanges.ELYTRA_FLY_VERTICAL_MIN);
    }

    public static void setElytraFlyVerticalSpeedSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        elytraFlyVerticalSpeed = VisualRanges.ELYTRA_FLY_VERTICAL_MIN + (VisualRanges.ELYTRA_FLY_VERTICAL_MAX - VisualRanges.ELYTRA_FLY_VERTICAL_MIN) * clamped;
    }

    public static float elytraFlyGlide() {
        return elytraFlyGlide;
    }

    public static float elytraFlyGlideSlider() {
        return (elytraFlyGlide - VisualRanges.ELYTRA_FLY_GLIDE_MIN) / (VisualRanges.ELYTRA_FLY_GLIDE_MAX - VisualRanges.ELYTRA_FLY_GLIDE_MIN);
    }

    public static void setElytraFlyGlideSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        elytraFlyGlide = VisualRanges.ELYTRA_FLY_GLIDE_MIN + (VisualRanges.ELYTRA_FLY_GLIDE_MAX - VisualRanges.ELYTRA_FLY_GLIDE_MIN) * clamped;
    }

    public static boolean elytraFlyAutoStart() {
        return elytraFlyAutoStart;
    }

    public static void setElytraFlyAutoStart(boolean enabled) {
        elytraFlyAutoStart = enabled;
    }

    public static boolean boatFlyEnabled() {
        return boatFlyEnabled;
    }

    public static void setBoatFlyEnabled(boolean enabled) {
        boatFlyEnabled = enabled;
    }

    public static String boatFlyMode() {
        return boatFlyMode;
    }

    public static void setBoatFlyMode(String mode) {
        boatFlyMode = "Glide".equals(mode) ? "Glide" : "Control";
    }

    public static float boatFlyHorizontalSpeed() {
        return boatFlyHorizontalSpeed;
    }

    public static float boatFlyHorizontalSpeedSlider() {
        return (boatFlyHorizontalSpeed - VisualRanges.BOAT_FLY_HORIZONTAL_MIN) / (VisualRanges.BOAT_FLY_HORIZONTAL_MAX - VisualRanges.BOAT_FLY_HORIZONTAL_MIN);
    }

    public static void setBoatFlyHorizontalSpeedSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        boatFlyHorizontalSpeed = VisualRanges.BOAT_FLY_HORIZONTAL_MIN + (VisualRanges.BOAT_FLY_HORIZONTAL_MAX - VisualRanges.BOAT_FLY_HORIZONTAL_MIN) * clamped;
    }

    public static float boatFlyVerticalSpeed() {
        return boatFlyVerticalSpeed;
    }

    public static float boatFlyVerticalSpeedSlider() {
        return (boatFlyVerticalSpeed - VisualRanges.BOAT_FLY_VERTICAL_MIN) / (VisualRanges.BOAT_FLY_VERTICAL_MAX - VisualRanges.BOAT_FLY_VERTICAL_MIN);
    }

    public static void setBoatFlyVerticalSpeedSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        boatFlyVerticalSpeed = VisualRanges.BOAT_FLY_VERTICAL_MIN + (VisualRanges.BOAT_FLY_VERTICAL_MAX - VisualRanges.BOAT_FLY_VERTICAL_MIN) * clamped;
    }

    public static float boatFlyGlide() {
        return boatFlyGlide;
    }

    public static float boatFlyGlideSlider() {
        return (boatFlyGlide - VisualRanges.BOAT_FLY_GLIDE_MIN) / (VisualRanges.BOAT_FLY_GLIDE_MAX - VisualRanges.BOAT_FLY_GLIDE_MIN);
    }

    public static void setBoatFlyGlideSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        boatFlyGlide = VisualRanges.BOAT_FLY_GLIDE_MIN + (VisualRanges.BOAT_FLY_GLIDE_MAX - VisualRanges.BOAT_FLY_GLIDE_MIN) * clamped;
    }

    public static boolean entitySpeedEnabled() {
        return entitySpeedEnabled;
    }

    public static void setEntitySpeedEnabled(boolean enabled) {
        entitySpeedEnabled = enabled;
    }

    public static float entitySpeed() {
        return entitySpeed;
    }

    public static float entitySpeedSlider() {
        return (entitySpeed - VisualRanges.ENTITY_SPEED_MIN) / (VisualRanges.ENTITY_SPEED_MAX - VisualRanges.ENTITY_SPEED_MIN);
    }

    public static void setEntitySpeedSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        entitySpeed = VisualRanges.ENTITY_SPEED_MIN + (VisualRanges.ENTITY_SPEED_MAX - VisualRanges.ENTITY_SPEED_MIN) * clamped;
    }

    public static boolean entitySpeedKeepY() {
        return entitySpeedKeepY;
    }

    public static void setEntitySpeedKeepY(boolean enabled) {
        entitySpeedKeepY = enabled;
    }

    public static boolean antiVoidEnabled() {
        return antiVoidEnabled;
    }

    public static void setAntiVoidEnabled(boolean enabled) {
        antiVoidEnabled = enabled;
    }

    public static String antiVoidMode() {
        return antiVoidMode;
    }

    public static void setAntiVoidMode(String mode) {
        antiVoidMode = "Freeze".equals(mode) ? "Freeze" : "Setback";
    }

    public static float antiVoidFallDistance() {
        return antiVoidFallDistance;
    }

    public static float antiVoidFallDistanceSlider() {
        return (antiVoidFallDistance - VisualRanges.ANTI_VOID_FALL_MIN) / (VisualRanges.ANTI_VOID_FALL_MAX - VisualRanges.ANTI_VOID_FALL_MIN);
    }

    public static void setAntiVoidFallDistanceSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        antiVoidFallDistance = VisualRanges.ANTI_VOID_FALL_MIN + (VisualRanges.ANTI_VOID_FALL_MAX - VisualRanges.ANTI_VOID_FALL_MIN) * clamped;
    }

    public static boolean freezeEnabled() {
        return freezeEnabled;
    }

    public static void setFreezeEnabled(boolean enabled) {
        freezeEnabled = enabled;
    }

    public static boolean stepEnabled() {
        return stepEnabled;
    }

    public static void setStepEnabled(boolean enabled) {
        stepEnabled = enabled;
    }

    public static float stepHeight() {
        return stepHeight;
    }

    public static float stepHeightSlider() {
        return (stepHeight - VisualRanges.STEP_HEIGHT_MIN) / (VisualRanges.STEP_HEIGHT_MAX - VisualRanges.STEP_HEIGHT_MIN);
    }

    public static void setStepHeightSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        stepHeight = VisualRanges.STEP_HEIGHT_MIN + (VisualRanges.STEP_HEIGHT_MAX - VisualRanges.STEP_HEIGHT_MIN) * clamped;
    }

    public static boolean stepOnlyGround() {
        return stepOnlyGround;
    }

    public static void setStepOnlyGround(boolean enabled) {
        stepOnlyGround = enabled;
    }

    public static boolean longJumpEnabled() {
        return longJumpEnabled;
    }

    public static void setLongJumpEnabled(boolean enabled) {
        longJumpEnabled = enabled;
    }

    public static float longJumpPower() {
        return longJumpPower;
    }

    public static float longJumpPowerSlider() {
        return (longJumpPower - VisualRanges.LONG_JUMP_POWER_MIN) / (VisualRanges.LONG_JUMP_POWER_MAX - VisualRanges.LONG_JUMP_POWER_MIN);
    }

    public static void setLongJumpPowerSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        longJumpPower = VisualRanges.LONG_JUMP_POWER_MIN + (VisualRanges.LONG_JUMP_POWER_MAX - VisualRanges.LONG_JUMP_POWER_MIN) * clamped;
    }

    public static boolean highJumpEnabled() {
        return highJumpEnabled;
    }

    public static void setHighJumpEnabled(boolean enabled) {
        highJumpEnabled = enabled;
    }

    public static float highJumpPower() {
        return highJumpPower;
    }

    public static float highJumpPowerSlider() {
        return (highJumpPower - VisualRanges.HIGH_JUMP_POWER_MIN) / (VisualRanges.HIGH_JUMP_POWER_MAX - VisualRanges.HIGH_JUMP_POWER_MIN);
    }

    public static void setHighJumpPowerSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        highJumpPower = VisualRanges.HIGH_JUMP_POWER_MIN + (VisualRanges.HIGH_JUMP_POWER_MAX - VisualRanges.HIGH_JUMP_POWER_MIN) * clamped;
    }

    public static boolean jesusEnabled() {
        return jesusEnabled;
    }

    public static void setJesusEnabled(boolean enabled) {
        jesusEnabled = enabled;
    }

    public static String jesusMode() {
        return jesusMode;
    }

    public static void setJesusMode(String mode) {
        jesusMode = "Bounce".equals(mode) ? "Bounce" : "Solid";
    }

    public static boolean jesusLava() {
        return jesusLava;
    }

    public static void setJesusLava(boolean enabled) {
        jesusLava = enabled;
    }

    public static boolean spiderEnabled() {
        return spiderEnabled;
    }

    public static void setSpiderEnabled(boolean enabled) {
        spiderEnabled = enabled;
    }

    public static String spiderMode() {
        return spiderMode;
    }

    public static void setSpiderMode(String mode) {
        spiderMode = "Legit".equals(mode) ? "Legit" : "Motion";
    }

    public static float spiderSpeed() {
        return spiderSpeed;
    }

    public static float spiderSpeedSlider() {
        return (spiderSpeed - VisualRanges.SPIDER_SPEED_MIN) / (VisualRanges.SPIDER_SPEED_MAX - VisualRanges.SPIDER_SPEED_MIN);
    }

    public static void setSpiderSpeedSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        spiderSpeed = VisualRanges.SPIDER_SPEED_MIN + (VisualRanges.SPIDER_SPEED_MAX - VisualRanges.SPIDER_SPEED_MIN) * clamped;
    }

    public static boolean spiderOnlyJump() {
        return spiderOnlyJump;
    }

    public static void setSpiderOnlyJump(boolean enabled) {
        spiderOnlyJump = enabled;
    }

    public static boolean spiderOnlyMoving() {
        return spiderOnlyMoving;
    }

    public static void setSpiderOnlyMoving(boolean enabled) {
        spiderOnlyMoving = enabled;
    }

    public static boolean phaseEnabled() {
        return phaseEnabled;
    }

    public static void setPhaseEnabled(boolean enabled) {
        phaseEnabled = enabled;
    }

    public static String phaseMode() {
        return phaseMode;
    }

    public static void setPhaseMode(String mode) {
        phaseMode = "Clip".equals(mode) ? "Clip" : "Normal";
    }

    public static float phaseHorizontalSpeed() {
        return phaseHorizontalSpeed;
    }

    public static float phaseHorizontalSpeedSlider() {
        return (phaseHorizontalSpeed - VisualRanges.PHASE_HORIZONTAL_MIN) / (VisualRanges.PHASE_HORIZONTAL_MAX - VisualRanges.PHASE_HORIZONTAL_MIN);
    }

    public static void setPhaseHorizontalSpeedSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        phaseHorizontalSpeed = VisualRanges.PHASE_HORIZONTAL_MIN + (VisualRanges.PHASE_HORIZONTAL_MAX - VisualRanges.PHASE_HORIZONTAL_MIN) * clamped;
    }

    public static float phaseVerticalSpeed() {
        return phaseVerticalSpeed;
    }

    public static float phaseVerticalSpeedSlider() {
        return (phaseVerticalSpeed - VisualRanges.PHASE_VERTICAL_MIN) / (VisualRanges.PHASE_VERTICAL_MAX - VisualRanges.PHASE_VERTICAL_MIN);
    }

    public static void setPhaseVerticalSpeedSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        phaseVerticalSpeed = VisualRanges.PHASE_VERTICAL_MIN + (VisualRanges.PHASE_VERTICAL_MAX - VisualRanges.PHASE_VERTICAL_MIN) * clamped;
    }

    public static boolean phaseSneakDown() {
        return phaseSneakDown;
    }

    public static void setPhaseSneakDown(boolean enabled) {
        phaseSneakDown = enabled;
    }

    public static boolean noClipEnabled() {
        return false;
    }

    public static void setNoClipEnabled(boolean enabled) {
        noClipEnabled = false;
    }

    public static float noClipHorizontalSpeed() {
        return noClipHorizontalSpeed;
    }

    public static float noClipHorizontalSpeedSlider() {
        return (noClipHorizontalSpeed - VisualRanges.NO_CLIP_HORIZONTAL_MIN) / (VisualRanges.NO_CLIP_HORIZONTAL_MAX - VisualRanges.NO_CLIP_HORIZONTAL_MIN);
    }

    public static void setNoClipHorizontalSpeedSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        noClipHorizontalSpeed = VisualRanges.NO_CLIP_HORIZONTAL_MIN + (VisualRanges.NO_CLIP_HORIZONTAL_MAX - VisualRanges.NO_CLIP_HORIZONTAL_MIN) * clamped;
    }

    public static float noClipVerticalSpeed() {
        return noClipVerticalSpeed;
    }

    public static float noClipVerticalSpeedSlider() {
        return (noClipVerticalSpeed - VisualRanges.NO_CLIP_VERTICAL_MIN) / (VisualRanges.NO_CLIP_VERTICAL_MAX - VisualRanges.NO_CLIP_VERTICAL_MIN);
    }

    public static void setNoClipVerticalSpeedSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        noClipVerticalSpeed = VisualRanges.NO_CLIP_VERTICAL_MIN + (VisualRanges.NO_CLIP_VERTICAL_MAX - VisualRanges.NO_CLIP_VERTICAL_MIN) * clamped;
    }

    public static boolean noClipSneakDown() {
        return noClipSneakDown;
    }

    public static void setNoClipSneakDown(boolean enabled) {
        noClipSneakDown = enabled;
    }

    public static boolean blinkEnabled() {
        return blinkEnabled;
    }

    public static void setBlinkEnabled(boolean enabled) {
        blinkEnabled = enabled;
    }

    public static String blinkMode() {
        return blinkMode;
    }

    public static void setBlinkMode(String mode) {
        blinkMode = "Pulse".equals(mode) ? "Pulse" : "Normal";
    }

    public static int blinkPulseTicks() {
        return Math.round(blinkPulseTicks);
    }

    public static float blinkPulseTicksSlider() {
        return (blinkPulseTicks - VisualRanges.BLINK_PULSE_TICKS_MIN) / (VisualRanges.BLINK_PULSE_TICKS_MAX - VisualRanges.BLINK_PULSE_TICKS_MIN);
    }

    public static void setBlinkPulseTicksSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        blinkPulseTicks = VisualRanges.BLINK_PULSE_TICKS_MIN + (VisualRanges.BLINK_PULSE_TICKS_MAX - VisualRanges.BLINK_PULSE_TICKS_MIN) * clamped;
    }

    public static int blinkMaxPackets() {
        return Math.round(blinkMaxPackets);
    }

    public static float blinkMaxPacketsSlider() {
        return (blinkMaxPackets - VisualRanges.BLINK_MAX_PACKETS_MIN) / (VisualRanges.BLINK_MAX_PACKETS_MAX - VisualRanges.BLINK_MAX_PACKETS_MIN);
    }

    public static void setBlinkMaxPacketsSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        blinkMaxPackets = VisualRanges.BLINK_MAX_PACKETS_MIN + (VisualRanges.BLINK_MAX_PACKETS_MAX - VisualRanges.BLINK_MAX_PACKETS_MIN) * clamped;
    }

    public static boolean blinkCancelOnHurt() {
        return blinkCancelOnHurt;
    }

    public static void setBlinkCancelOnHurt(boolean enabled) {
        blinkCancelOnHurt = enabled;
    }

    public static boolean autoEatEnabled() {
        return autoEatEnabled;
    }

    public static void setAutoEatEnabled(boolean enabled) {
        autoEatEnabled = enabled;
    }

    public static float autoEatFoodLevel() {
        return autoEatFoodLevel;
    }

    public static int autoEatFoodLevelInt() {
        return Math.round(autoEatFoodLevel);
    }

    public static float autoEatFoodLevelSlider() {
        return (autoEatFoodLevel - VisualRanges.AUTO_EAT_FOOD_MIN) / (VisualRanges.AUTO_EAT_FOOD_MAX - VisualRanges.AUTO_EAT_FOOD_MIN);
    }

    public static void setAutoEatFoodLevelSlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        autoEatFoodLevel = VisualRanges.AUTO_EAT_FOOD_MIN + (VisualRanges.AUTO_EAT_FOOD_MAX - VisualRanges.AUTO_EAT_FOOD_MIN) * clamped;
    }

    public static int autoEatDelayTicks() {
        return Math.round(autoEatDelay);
    }

    public static float autoEatDelaySlider() {
        return (autoEatDelay - VisualRanges.AUTO_EAT_DELAY_MIN) / (VisualRanges.AUTO_EAT_DELAY_MAX - VisualRanges.AUTO_EAT_DELAY_MIN);
    }

    public static void setAutoEatDelaySlider(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        autoEatDelay = VisualRanges.AUTO_EAT_DELAY_MIN + (VisualRanges.AUTO_EAT_DELAY_MAX - VisualRanges.AUTO_EAT_DELAY_MIN) * clamped;
    }

    public static boolean autoEatRestoreSlot() {
        return autoEatRestoreSlot;
    }

    public static void setAutoEatRestoreSlot(boolean enabled) {
        autoEatRestoreSlot = enabled;
    }

    public static boolean autoEatInventory() {
        return autoEatInventory;
    }

    public static void setAutoEatInventory(boolean enabled) {
        autoEatInventory = enabled;
    }

    public static boolean autoFishEnabled() {
        return autoFishEnabled;
    }

    public static void setAutoFishEnabled(boolean enabled) {
        autoFishEnabled = enabled;
    }

    public static boolean autoFishAutoCast() {
        return autoFishAutoCast;
    }

    public static void setAutoFishAutoCast(boolean enabled) {
        autoFishAutoCast = enabled;
    }

    public static float autoFishTicksAutoCast() {
        return autoFishTicksAutoCast;
    }

    public static float autoFishTicksAutoCastSlider() {
        return ConfigValueCodec.inverse(autoFishTicksAutoCast, VisualRanges.AUTO_FISH_TICKS_AUTO_CAST_MIN, VisualRanges.AUTO_FISH_TICKS_AUTO_CAST_MAX);
    }

    public static void setAutoFishTicksAutoCastSlider(float value) {
        autoFishTicksAutoCast = ConfigValueCodec.range(value, VisualRanges.AUTO_FISH_TICKS_AUTO_CAST_MIN, VisualRanges.AUTO_FISH_TICKS_AUTO_CAST_MAX);
    }

    public static float autoFishCatchDelay() {
        return autoFishCatchDelay;
    }

    public static float autoFishCatchDelaySlider() {
        return ConfigValueCodec.inverse(autoFishCatchDelay, VisualRanges.AUTO_FISH_CATCH_DELAY_MIN, VisualRanges.AUTO_FISH_CATCH_DELAY_MAX);
    }

    public static void setAutoFishCatchDelaySlider(float value) {
        autoFishCatchDelay = ConfigValueCodec.range(value, VisualRanges.AUTO_FISH_CATCH_DELAY_MIN, VisualRanges.AUTO_FISH_CATCH_DELAY_MAX);
    }

    public static float autoFishThrowDelay() {
        return autoFishThrowDelay;
    }

    public static float autoFishThrowDelaySlider() {
        return ConfigValueCodec.inverse(autoFishThrowDelay, VisualRanges.AUTO_FISH_THROW_DELAY_MIN, VisualRanges.AUTO_FISH_THROW_DELAY_MAX);
    }

    public static void setAutoFishThrowDelaySlider(float value) {
        autoFishThrowDelay = ConfigValueCodec.range(value, VisualRanges.AUTO_FISH_THROW_DELAY_MIN, VisualRanges.AUTO_FISH_THROW_DELAY_MAX);
    }

    public static boolean autoFishAntiBreak() {
        return autoFishAntiBreak;
    }

    public static void setAutoFishAntiBreak(boolean enabled) {
        autoFishAntiBreak = enabled;
    }

    public static boolean chestStealEnabled() {
        return chestStealEnabled;
    }

    public static void setChestStealEnabled(boolean enabled) {
        chestStealEnabled = enabled;
    }

    public static int chestStealDelayTicks() {
        return Math.round(chestStealDelay);
    }

    public static float chestStealDelaySlider() {
        return ConfigValueCodec.inverse(chestStealDelay, VisualRanges.CHEST_STEAL_DELAY_MIN, VisualRanges.CHEST_STEAL_DELAY_MAX);
    }

    public static void setChestStealDelaySlider(float value) {
        chestStealDelay = ConfigValueCodec.range(value, VisualRanges.CHEST_STEAL_DELAY_MIN, VisualRanges.CHEST_STEAL_DELAY_MAX);
    }

    public static boolean chestStealAutoClose() {
        return chestStealAutoClose;
    }

    public static void setChestStealAutoClose(boolean enabled) {
        chestStealAutoClose = enabled;
    }

    public static boolean inventoryCleanerEnabled() {
        return inventoryCleanerEnabled;
    }

    public static void setInventoryCleanerEnabled(boolean enabled) {
        inventoryCleanerEnabled = enabled;
    }

    public static float inventoryCleanerDelay() {
        return inventoryCleanerDelay;
    }

    public static float inventoryCleanerDelaySlider() {
        return ConfigValueCodec.inverse(inventoryCleanerDelay, 0.0f, 10.0f);
    }

    public static void setInventoryCleanerDelaySlider(float value) {
        inventoryCleanerDelay = ConfigValueCodec.range(value, 0.0f, 10.0f);
    }

    public static boolean inventoryCleanerCleanHotbar() {
        return inventoryCleanerCleanHotbar;
    }

    public static void setInventoryCleanerCleanHotbar(boolean enabled) {
        inventoryCleanerCleanHotbar = enabled;
    }

    public static String inventoryCleanerTrashItems() {
        return inventoryCleanerTrashItems;
    }

    public static void setInventoryCleanerTrashItems(String value) {
        inventoryCleanerTrashItems = value == null ? "" : value;
    }

    public static boolean autoRefillEnabled() {
        return autoRefillEnabled;
    }

    public static void setAutoRefillEnabled(boolean enabled) {
        autoRefillEnabled = enabled;
    }

    public static float autoRefillThreshold() {
        return autoRefillThreshold;
    }

    public static float autoRefillThresholdSlider() {
        return ConfigValueCodec.inverse(autoRefillThreshold, 1.0f, 32.0f);
    }

    public static void setAutoRefillThresholdSlider(float value) {
        autoRefillThreshold = ConfigValueCodec.range(value, 1.0f, 32.0f);
    }

    public static float autoRefillDelay() {
        return autoRefillDelay;
    }

    public static float autoRefillDelaySlider() {
        return ConfigValueCodec.inverse(autoRefillDelay, 0.0f, 10.0f);
    }

    public static void setAutoRefillDelaySlider(float value) {
        autoRefillDelay = ConfigValueCodec.range(value, 0.0f, 10.0f);
    }

    public static boolean autoRefillCurrentSlotOnly() {
        return autoRefillCurrentSlotOnly;
    }

    public static void setAutoRefillCurrentSlotOnly(boolean enabled) {
        autoRefillCurrentSlotOnly = enabled;
    }

    public static boolean autoSortEnabled() {
        return autoSortEnabled;
    }

    public static void setAutoSortEnabled(boolean enabled) {
        autoSortEnabled = enabled;
    }

    public static String autoSortMode() {
        return autoSortMode;
    }

    public static void setAutoSortMode(String mode) {
        autoSortMode = switch (mode) {
            case "Name", "Count" -> mode;
            default -> "Category";
        };
    }

    public static String autoSortAction() {
        return autoSortAction;
    }

    public static void setAutoSortAction(String action) {
        autoSortAction = "Delayed".equals(action) ? "Delayed" : "Instant";
    }

    public static float autoSortDelay() {
        return autoSortDelay;
    }

    public static float autoSortDelaySlider() {
        return ConfigValueCodec.inverse(autoSortDelay, 0.0f, 10.0f);
    }

    public static void setAutoSortDelaySlider(float value) {
        autoSortDelay = ConfigValueCodec.range(value, 0.0f, 10.0f);
    }

    public static boolean autoSortKeepHotbar() {
        return !autoSortIncludeHotbar;
    }

    public static void setAutoSortKeepHotbar(boolean enabled) {
        autoSortIncludeHotbar = !enabled;
    }

    public static boolean autoSortIncludeHotbar() {
        return autoSortIncludeHotbar;
    }

    public static void setAutoSortIncludeHotbar(boolean enabled) {
        autoSortIncludeHotbar = enabled;
    }

    public static int autoSortKeyCode() {
        return autoSortKeyCode;
    }

    public static void setAutoSortKeyCode(int keyCode) {
        autoSortKeyCode = keyCode;
    }

    public static int autoSortScanCode() {
        return autoSortScanCode;
    }

    public static void setAutoSortScanCode(int scanCode) {
        autoSortScanCode = scanCode;
    }

    public static void bindAutoSortKey(int keyCode, int scanCode) {
        autoSortKeyCode = keyCode;
        autoSortScanCode = scanCode;
    }

    public static void clearAutoSortKey() {
        autoSortKeyCode = GLFW.GLFW_KEY_UNKNOWN;
        autoSortScanCode = 0;
    }

    public static boolean scaffoldEnabled() {
        return scaffoldEnabled;
    }

    public static void setScaffoldEnabled(boolean enabled) {
        scaffoldEnabled = enabled;
    }

    public static String scaffoldMode() {
        return scaffoldMode;
    }

    public static void setScaffoldMode(String mode) {
        switch (mode == null ? "" : mode) {
            case "GodBridge", "Heypixel", "Hypixel", "Vanilla", "Bloxd", "AntiGamingChair",
                 "Watchdog", "SilenceTelly" -> scaffoldMode = mode;
            default -> scaffoldMode = "TellyBridge";
        }
    }

    public static boolean scaffoldTower() {
        return scaffoldTower;
    }

    public static void setScaffoldTower(boolean enabled) {
        scaffoldTower = enabled;
    }

    public static boolean scaffoldTelly() {
        return scaffoldTelly;
    }

    public static void setScaffoldTelly(boolean enabled) {
        scaffoldTelly = enabled;
    }

    public static boolean scaffoldInteractBeforePlace() {
        return scaffoldInteractBeforePlace;
    }

    public static void setScaffoldInteractBeforePlace(boolean enabled) {
        scaffoldInteractBeforePlace = enabled;
    }

    public static boolean scaffoldDuplicateRotPlace() {
        return scaffoldDuplicateRotPlace;
    }

    public static void setScaffoldDuplicateRotPlace(boolean enabled) {
        scaffoldDuplicateRotPlace = enabled;
    }

    public static String scaffoldSilenceTellyMode() {
        return scaffoldSilenceTellyMode;
    }

    public static void setScaffoldSilenceTellyMode(String mode) {
        scaffoldSilenceTellyMode = switch (mode == null ? "" : mode) {
            case "Snap", "Telly" -> mode;
            default -> "Normal";
        };
    }

    public static int scaffoldSilenceTellyPlaceTick() {
        return scaffoldSilenceTellyPlaceTick;
    }

    public static float scaffoldSilenceTellyPlaceTickSlider() {
        return ConfigValueCodec.inverse(scaffoldSilenceTellyPlaceTick, 1.0F, 5.0F);
    }

    public static void setScaffoldSilenceTellyPlaceTickSlider(float value) {
        scaffoldSilenceTellyPlaceTick = Math.round(ConfigValueCodec.range(value, 1.0F, 5.0F));
    }

    public static int scaffoldSilenceTellyRotationTick() {
        return scaffoldSilenceTellyRotationTick;
    }

    public static float scaffoldSilenceTellyRotationTickSlider() {
        return ConfigValueCodec.inverse(scaffoldSilenceTellyRotationTick, 1.0F, 5.0F);
    }

    public static void setScaffoldSilenceTellyRotationTickSlider(float value) {
        scaffoldSilenceTellyRotationTick = Math.round(ConfigValueCodec.range(value, 1.0F, 5.0F));
    }

    public static boolean scaffoldSilenceTellySnap() {
        return scaffoldSilenceTellySnap;
    }

    public static void setScaffoldSilenceTellySnap(boolean enabled) {
        scaffoldSilenceTellySnap = enabled;
    }

    public static boolean scaffoldSilenceTellyGodBridge() {
        return scaffoldSilenceTellyGodBridge;
    }

    public static void setScaffoldSilenceTellyGodBridge(boolean enabled) {
        scaffoldSilenceTellyGodBridge = enabled;
    }

    public static boolean scaffoldSilenceTellyEagle() {
        return scaffoldSilenceTellyEagle;
    }

    public static void setScaffoldSilenceTellyEagle(boolean enabled) {
        scaffoldSilenceTellyEagle = enabled;
    }

    public static int scaffoldSilenceTellyEagleTick() {
        return scaffoldSilenceTellyEagleTick;
    }

    public static void setScaffoldSilenceTellyEagleTick(int value) {
        scaffoldSilenceTellyEagleTick = Math.max(0, Math.min(5, value));
    }

    public static float scaffoldSilenceTellyEagleTickSlider() {
        return ConfigValueCodec.inverse(scaffoldSilenceTellyEagleTick, 1.0F, 5.0F);
    }

    public static void setScaffoldSilenceTellyEagleTickSlider(float value) {
        scaffoldSilenceTellyEagleTick = Math.round(ConfigValueCodec.range(value, 1.0F, 5.0F));
    }

    public static int scaffoldSilenceTellyKeepEagleTick() {
        return scaffoldSilenceTellyKeepEagleTick;
    }

    public static void setScaffoldSilenceTellyKeepEagleTick(int value) {
        scaffoldSilenceTellyKeepEagleTick = Math.max(0, Math.min(10, value));
    }

    public static float scaffoldSilenceTellyKeepEagleTickSlider() {
        return ConfigValueCodec.inverse(scaffoldSilenceTellyKeepEagleTick, 1.0F, 5.0F);
    }

    public static void setScaffoldSilenceTellyKeepEagleTickSlider(float value) {
        scaffoldSilenceTellyKeepEagleTick = Math.round(ConfigValueCodec.range(value, 1.0F, 5.0F));
    }

    public static String scaffoldSilenceTellyJumpMode() {
        return scaffoldSilenceTellyJumpMode;
    }

    public static void setScaffoldSilenceTellyJumpMode(String mode) {
        scaffoldSilenceTellyJumpMode = switch (mode == null ? "" : mode) {
            case "None", "Parkour" -> mode;
            default -> "Normal";
        };
    }

    public static boolean scaffoldSilenceTellyNoUptelly() {
        return scaffoldSilenceTellyNoUptelly;
    }

    public static void setScaffoldSilenceTellyNoUptelly(boolean enabled) {
        scaffoldSilenceTellyNoUptelly = enabled;
    }

    public static boolean scaffoldSilenceTellySlowUpTelly() {
        return scaffoldSilenceTellySlowUpTelly;
    }

    public static void setScaffoldSilenceTellySlowUpTelly(boolean enabled) {
        scaffoldSilenceTellySlowUpTelly = enabled;
    }

    public static boolean scaffoldSilenceTellyHeypixelUpTelly() {
        return scaffoldSilenceTellyHeypixelUpTelly;
    }

    public static void setScaffoldSilenceTellyHeypixelUpTelly(boolean enabled) {
        scaffoldSilenceTellyHeypixelUpTelly = enabled;
    }

    public static boolean scaffoldSilenceTellySafeMode() {
        return scaffoldSilenceTellySafeMode;
    }

    public static void setScaffoldSilenceTellySafeMode(boolean enabled) {
        scaffoldSilenceTellySafeMode = enabled;
    }

    public static boolean scaffoldSilenceTellyTestOnGround() {
        return scaffoldSilenceTellyTestOnGround;
    }

    public static void setScaffoldSilenceTellyTestOnGround(boolean enabled) {
        scaffoldSilenceTellyTestOnGround = enabled;
    }

    public static float scaffoldSilenceTellySafeDistance() {
        return scaffoldSilenceTellySafeDistance;
    }

    public static float scaffoldSilenceTellySafeDistanceSlider() {
        return ConfigValueCodec.inverse(scaffoldSilenceTellySafeDistance, 1.0F, 5.0F);
    }

    public static void setScaffoldSilenceTellySafeDistanceSlider(float value) {
        scaffoldSilenceTellySafeDistance = ConfigValueCodec.range(value, 1.0F, 5.0F);
    }

    public static boolean scaffoldSilenceTellyFixRotation() {
        return scaffoldSilenceTellyFixRotation;
    }

    public static void setScaffoldSilenceTellyFixRotation(boolean enabled) {
        scaffoldSilenceTellyFixRotation = enabled;
    }

    public static boolean scaffoldSilenceTellyDuplicateRotPlace() {
        return scaffoldSilenceTellyDuplicateRotPlace;
    }

    public static void setScaffoldSilenceTellyDuplicateRotPlace(boolean enabled) {
        scaffoldSilenceTellyDuplicateRotPlace = enabled;
    }

    public static boolean scaffoldSilenceTellyBlockFly() {
        return scaffoldSilenceTellyBlockFly;
    }

    public static void setScaffoldSilenceTellyBlockFly(boolean enabled) {
        scaffoldSilenceTellyBlockFly = enabled;
    }

    public static boolean scaffoldSilenceTellyAbuseRotation() {
        return scaffoldSilenceTellyAbuseRotation;
    }

    public static void setScaffoldSilenceTellyAbuseRotation(boolean enabled) {
        scaffoldSilenceTellyAbuseRotation = enabled;
    }

    public static boolean scaffoldSilenceTellyDebug() {
        return scaffoldSilenceTellyDebug;
    }

    public static void setScaffoldSilenceTellyDebug(boolean enabled) {
        scaffoldSilenceTellyDebug = enabled;
    }

    public static boolean stuckEnabled() {
        return stuckEnabled;
    }

    public static void setStuckEnabled(boolean enabled) {
        stuckEnabled = enabled;
    }

    public static String scaffoldSwapMode() {
        return scaffoldSwapMode;
    }

    public static void setScaffoldSwapMode(String mode) {
        scaffoldSwapMode = switch (mode) {
            case "None", "Silent" -> mode;
            default -> "Normal";
        };
    }

    public static boolean scaffoldSwapBack() {
        return scaffoldSwapBack;
    }

    public static void setScaffoldSwapBack(boolean enabled) {
        scaffoldSwapBack = enabled;
    }

    public static boolean scaffoldSnap() {
        return scaffoldSnap;
    }

    public static void setScaffoldSnap(boolean enabled) {
        scaffoldSnap = enabled;
    }

    public static String scaffoldRotationMode() {
        return scaffoldRotationMode;
    }

    public static void setScaffoldRotationMode(String mode) {
        scaffoldRotationMode = "Hypixel".equals(mode) ? "Hypixel" : "Rise";
    }

    public static String scaffoldRaytraceMode() {
        return scaffoldRaytraceMode;
    }

    public static void setScaffoldRaytraceMode(String mode) {
        scaffoldRaytraceMode = "Strict".equals(mode) ? "Strict" : "Normal";
    }

    public static int scaffoldRotationSpeed() {
        return Math.round(scaffoldRotationSpeed);
    }

    public static float scaffoldRotationSpeedSlider() {
        return (scaffoldRotationSpeed - 1.0f) / 9.0f;
    }

    public static void setScaffoldRotationSpeedSlider(float value) {
        scaffoldRotationSpeed = 1.0f + Math.max(0.0f, Math.min(1.0f, value)) * 9.0f;
    }

    public static int scaffoldRotationBackSpeed() {
        return Math.round(scaffoldRotationBackSpeed);
    }

    public static float scaffoldRotationBackSpeedSlider() {
        return (scaffoldRotationBackSpeed - 1.0f) / 9.0f;
    }

    public static void setScaffoldRotationBackSpeedSlider(float value) {
        scaffoldRotationBackSpeed = 1.0f + Math.max(0.0f, Math.min(1.0f, value)) * 9.0f;
    }

    public static int scaffoldTellyTicks() {
        return Math.round(scaffoldTellyTicks);
    }

    public static float scaffoldTellyTicksSlider() {
        return scaffoldTellyTicks / 6.0f;
    }

    public static void setScaffoldTellyTicksSlider(float value) {
        scaffoldTellyTicks = Math.max(0.0f, Math.min(1.0f, value)) * 6.0f;
    }

    public static boolean scaffoldSafeWalk() {
        return scaffoldSafeWalk;
    }

    public static void setScaffoldSafeWalk(boolean enabled) {
        scaffoldSafeWalk = enabled;
    }

    public static boolean scaffoldSwingHand() {
        return scaffoldSwingHand;
    }

    public static void setScaffoldSwingHand(boolean enabled) {
        scaffoldSwingHand = enabled;
    }

    public static boolean antiPacketKickEnabled() {
        return antiPacketKickEnabled;
    }

    public static void setAntiPacketKickEnabled(boolean enabled) {
        antiPacketKickEnabled = enabled;
    }

    public static boolean autoReconnectEnabled() {
        return autoReconnectEnabled;
    }

    public static void setAutoReconnectEnabled(boolean enabled) {
        autoReconnectEnabled = enabled;
    }

    public static float autoReconnectDelay() {
        return autoReconnectDelay;
    }

    public static float autoReconnectDelaySlider() {
        return ConfigValueCodec.inverse(autoReconnectDelay, VisualRanges.AUTO_RECONNECT_DELAY_MIN, VisualRanges.AUTO_RECONNECT_DELAY_MAX);
    }

    public static void setAutoReconnectDelaySlider(float value) {
        autoReconnectDelay = ConfigValueCodec.range(value, VisualRanges.AUTO_RECONNECT_DELAY_MIN, VisualRanges.AUTO_RECONNECT_DELAY_MAX);
    }

    public static boolean hotkeyHudEnabled() {
        return (hudWidgetMask & HUD_WIDGET_HOTKEYS) != 0;
    }

    public static void setHotkeyHudEnabled(boolean enabled) {
        hotkeyHudEnabled = enabled;
        if (enabled) {
            hudWidgetMask |= HUD_WIDGET_HOTKEYS;
        } else {
            hudWidgetMask &= ~HUD_WIDGET_HOTKEYS;
        }
    }

    public static int hudWidgetMask() {
        return hudWidgetMask & HUD_WIDGET_ALL & ~HUD_WIDGET_COORDS;
    }

    public static void setHudWidgetMask(int mask) {
        mask &= ~HUD_WIDGET_LEGACY_ARMOR;
        mask &= ~HUD_WIDGET_COORDS;
        if ((mask & HUD_WIDGET_LEGACY_BPS) != 0) {
            mask |= HUD_WIDGET_SPEED;
        }
        mask &= ~HUD_WIDGET_LEGACY_BPS;
        hudWidgetMask = mask & HUD_WIDGET_ALL;
        hotkeyHudEnabled = (hudWidgetMask & HUD_WIDGET_HOTKEYS) != 0;
    }

    public static boolean hudWidgetEnabled(int widget) {
        return (hudWidgetMask & widget) != 0;
    }

    public static void setHudWidgetEnabled(int widget, boolean enabled) {
        if (enabled) {
            setHudWidgetMask(hudWidgetMask | widget);
        } else {
            setHudWidgetMask(hudWidgetMask & ~widget);
        }
    }

    public static boolean notificationsEnabled() {
        return notificationsEnabled;
    }

    public static void setNotificationsEnabled(boolean enabled) {
        notificationsEnabled = enabled;
    }

    public static String notificationsPosition() {
        return notificationsPosition;
    }

    public static void setNotificationsPosition(String position) {
        if (position == null || position.isBlank()) {
            notificationsPosition = "Bottom Right";
            return;
        }
        notificationsPosition = position;
    }

    public static float notificationsDuration() {
        return notificationsDuration;
    }

    public static float notificationsDurationSlider() {
        return ConfigValueCodec.inverse(notificationsDuration, 1.0f, 8.0f);
    }

    public static void setNotificationsDurationSlider(float value) {
        notificationsDuration = ConfigValueCodec.range(value, 1.0f, 8.0f);
    }

    public static int notificationsMaxVisible() {
        return notificationsMaxVisible;
    }

    public static float notificationsMaxVisibleSlider() {
        return ConfigValueCodec.inverse(notificationsMaxVisible, 1.0f, 8.0f);
    }

    public static void setNotificationsMaxVisibleSlider(float value) {
        notificationsMaxVisible = Math.max(1, Math.min(8, Math.round(ConfigValueCodec.range(value, 1.0f, 8.0f))));
    }

    public static boolean notificationsBlur() {
        return notificationsBlur;
    }

    public static void setNotificationsBlur(boolean enabled) {
        notificationsBlur = enabled;
    }

    public static boolean notificationsModuleToggles() {
        return notificationsModuleToggles;
    }

    public static void setNotificationsModuleToggles(boolean enabled) {
        notificationsModuleToggles = enabled;
    }

    public static boolean notificationsLinkHotkeys() {
        return notificationsLinkHotkeys;
    }

    public static void setNotificationsLinkHotkeys(boolean enabled) {
        notificationsLinkHotkeys = enabled;
    }

    public static boolean modernUiEnabled() {
        return modernUiEnabled;
    }

    public static void setModernUiEnabled(boolean enabled) {
        modernUiEnabled = enabled;
    }

    public static int watermarkX() {
        return HudPositionSettings.watermarkX();
    }

    public static int watermarkY() {
        return HudPositionSettings.watermarkY();
    }

    public static void setWatermarkPosition(int x, int y) {
        HudPositionSettings.setWatermarkPosition(x, y);
    }

    public static void setWatermarkX(int x) {
        HudPositionSettings.setWatermarkPosition(x, watermarkY());
    }

    public static void setWatermarkY(int y) {
        HudPositionSettings.setWatermarkPosition(watermarkX(), y);
    }

    public static int watermarkBackgroundColor() {
        return watermarkBackgroundColor;
    }

    public static void setWatermarkBackgroundColor(int color) {
        watermarkBackgroundColor = color;
    }

    public static int watermarkTextColor() {
        return watermarkTextColor;
    }

    public static void setWatermarkTextColor(int color) {
        watermarkTextColor = color;
    }

    public static int watermarkIconColor() {
        return watermarkIconColor;
    }

    public static void setWatermarkIconColor(int color) {
        watermarkIconColor = color;
    }

    public static int watermarkElementMask() {
        return watermarkElementMask & WATERMARK_ELEMENT_ALL;
    }

    public static void setWatermarkElementMask(int mask) {
        watermarkElementMask = mask & WATERMARK_ELEMENT_ALL;
    }

    public static boolean watermarkElementEnabled(int element) {
        return (watermarkElementMask & element) != 0;
    }

    public static boolean watermarkBlur() {
        return watermarkBlur;
    }

    public static void setWatermarkBlur(boolean enabled) {
        watermarkBlur = enabled;
    }

    public static int[] watermarkElementOrder() {
        int[] order = WatermarkElementOrder.normalize(watermarkElementOrder);
        setWatermarkElementOrder(order);
        return order;
    }

    public static void setWatermarkElementOrder(int[] order) {
        watermarkElementOrder = WatermarkElementOrder.serialize(order);
    }

    public static String watermarkElementOrderSerialized() {
        watermarkElementOrder = WatermarkElementOrder.serialize(WatermarkElementOrder.normalize(watermarkElementOrder));
        return watermarkElementOrder;
    }

    public static void setWatermarkElementOrderSerialized(String order) {
        watermarkElementOrder = WatermarkElementOrder.serialize(WatermarkElementOrder.normalize(order));
    }

    public static int hotkeyHudX() {
        return HudPositionSettings.hotkeyHudX();
    }

    public static int hotkeyHudY() {
        return HudPositionSettings.hotkeyHudY();
    }

    public static void setHotkeyHudPosition(int x, int y) {
        HudPositionSettings.setHotkeyHudPosition(x, y);
    }

    public static void setHotkeyHudX(int x) {
        HudPositionSettings.setHotkeyHudPosition(x, hotkeyHudY());
    }

    public static void setHotkeyHudY(int y) {
        HudPositionSettings.setHotkeyHudPosition(hotkeyHudX(), y);
    }

    public static int arrayListHudX() {
        return HudPositionSettings.arrayListHudX();
    }

    public static int arrayListHudY() {
        return HudPositionSettings.arrayListHudY();
    }

    public static void setArrayListHudPosition(int x, int y) {
        HudPositionSettings.setArrayListHudPosition(x, y);
    }

    public static void setArrayListHudX(int x) {
        HudPositionSettings.setArrayListHudPosition(x, arrayListHudY());
    }

    public static void setArrayListHudY(int y) {
        HudPositionSettings.setArrayListHudPosition(arrayListHudX(), y);
    }

    public static int arrayListHudRightOffset() {
        return HudPositionSettings.arrayListHudRightOffset();
    }

    public static void setArrayListHudRightOffset(int offset) {
        HudPositionSettings.setArrayListHudRightOffset(offset);
    }

    public static boolean arrayListHudRightAnchored() {
        return HudPositionSettings.arrayListHudRightAnchored();
    }

    public static void setArrayListHudRightAnchored(boolean anchored) {
        HudPositionSettings.setArrayListHudRightAnchored(anchored);
    }

    public static int targetsHudX() {
        return HudPositionSettings.targetsHudX();
    }

    public static int targetsHudY() {
        return HudPositionSettings.targetsHudY();
    }

    public static void setTargetsHudPosition(int x, int y) {
        HudPositionSettings.setTargetsHudPosition(x, y);
    }

    public static void setTargetsHudX(int x) {
        HudPositionSettings.setTargetsHudPosition(x, targetsHudY());
    }

    public static void setTargetsHudY(int y) {
        HudPositionSettings.setTargetsHudPosition(targetsHudX(), y);
    }

    public static int backpackHudX() {
        return HudPositionSettings.backpackHudX();
    }

    public static int backpackHudY() {
        return HudPositionSettings.backpackHudY();
    }

    public static void setBackpackHudPosition(int x, int y) {
        HudPositionSettings.setBackpackHudPosition(x, y);
    }

    public static void setBackpackHudX(int x) {
        HudPositionSettings.setBackpackHudPosition(x, backpackHudY());
    }

    public static void setBackpackHudY(int y) {
        HudPositionSettings.setBackpackHudPosition(backpackHudX(), y);
    }

    public static int potionBuffHudX() {
        return HudPositionSettings.potionBuffHudX();
    }

    public static int potionBuffHudY() {
        return HudPositionSettings.potionBuffHudY();
    }

    public static void setPotionBuffHudPosition(int x, int y) {
        HudPositionSettings.setPotionBuffHudPosition(x, y);
    }

    public static void setPotionBuffHudX(int x) {
        HudPositionSettings.setPotionBuffHudPosition(x, potionBuffHudY());
    }

    public static void setPotionBuffHudY(int y) {
        HudPositionSettings.setPotionBuffHudPosition(potionBuffHudX(), y);
    }

    public static int speedHudX() {
        return HudPositionSettings.speedHudX();
    }

    public static int speedHudY() {
        return HudPositionSettings.speedHudY();
    }

    public static void setSpeedHudPosition(int x, int y) {
        HudPositionSettings.setSpeedHudPosition(x, y);
    }

    public static void setSpeedHudX(int x) {
        HudPositionSettings.setSpeedHudPosition(x, speedHudY());
    }

    public static void setSpeedHudY(int y) {
        HudPositionSettings.setSpeedHudPosition(speedHudX(), y);
    }

    public static int musicLyricsHudX() {
        return HudPositionSettings.musicLyricsHudX();
    }

    public static int musicLyricsHudY() {
        return HudPositionSettings.musicLyricsHudY();
    }

    public static void setMusicLyricsHudPosition(int x, int y) {
        HudPositionSettings.setMusicLyricsHudPosition(x, y);
    }

    public static boolean hotkeyHudBlur() {
        return hotkeyHudBlur;
    }

    public static void setHotkeyHudBlur(boolean enabled) {
        hotkeyHudBlur = enabled;
    }

    public static int hotkeyHudBackgroundColor() {
        return hotkeyHudBackgroundColor;
    }

    public static void setHotkeyHudBackgroundColor(int color) {
        hotkeyHudBackgroundColor = color;
    }

    public static int hotkeyHudItemColor() {
        return hotkeyHudItemColor;
    }

    public static void setHotkeyHudItemColor(int color) {
        hotkeyHudItemColor = color;
    }

    public static int hotkeyHudTextColor() {
        return hotkeyHudTextColor;
    }

    public static void setHotkeyHudTextColor(int color) {
        hotkeyHudTextColor = color;
    }

    public static int hotkeyHudAccentColor() {
        return hotkeyHudAccentColor;
    }

    public static void setHotkeyHudAccentColor(int color) {
        hotkeyHudAccentColor = color;
    }

    public static boolean hotkeyHudShowTitle() {
        return hotkeyHudShowTitle;
    }

    public static void setHotkeyHudShowTitle(boolean enabled) {
        hotkeyHudShowTitle = enabled;
    }

    public static boolean arrayListHudBlur() {
        return arrayListHudBlur;
    }

    public static void setArrayListHudBlur(boolean enabled) {
        arrayListHudBlur = enabled;
    }

    public static int arrayListHudBackgroundColor() {
        return arrayListHudBackgroundColor;
    }

    public static void setArrayListHudBackgroundColor(int color) {
        arrayListHudBackgroundColor = color;
    }

    public static int arrayListHudTextColor() {
        return arrayListHudTextColor;
    }

    public static void setArrayListHudTextColor(int color) {
        arrayListHudTextColor = color;
    }

    public static int arrayListHudAccentColor() {
        return arrayListHudAccentColor;
    }

    public static void setArrayListHudAccentColor(int color) {
        arrayListHudAccentColor = color;
    }

    public static boolean arrayListHudAccentBar() {
        return arrayListHudAccentBar;
    }

    public static void setArrayListHudAccentBar(boolean enabled) {
        arrayListHudAccentBar = enabled;
    }

    public static boolean targetsHudBlur() {
        return targetsHudBlur;
    }

    public static void setTargetsHudBlur(boolean enabled) {
        targetsHudBlur = enabled;
    }

    public static int targetsHudBackgroundColor() {
        return targetsHudBackgroundColor;
    }

    public static void setTargetsHudBackgroundColor(int color) {
        targetsHudBackgroundColor = color;
    }

    public static int targetsHudFieldColor() {
        return targetsHudFieldColor;
    }

    public static void setTargetsHudFieldColor(int color) {
        targetsHudFieldColor = color;
    }

    public static int targetsHudTextColor() {
        return targetsHudTextColor;
    }

    public static void setTargetsHudTextColor(int color) {
        targetsHudTextColor = color;
    }

    public static int targetsHudAccentColor() {
        return targetsHudAccentColor;
    }

    public static void setTargetsHudAccentColor(int color) {
        targetsHudAccentColor = color;
    }

    public static boolean targetsHudShowAvatar() {
        return targetsHudShowAvatar;
    }

    public static void setTargetsHudShowAvatar(boolean enabled) {
        targetsHudShowAvatar = enabled;
    }

    public static boolean targetsHudShowDistance() {
        return targetsHudShowDistance;
    }

    public static void setTargetsHudShowDistance(boolean enabled) {
        targetsHudShowDistance = enabled;
    }

    public static boolean targetsHudShowHealthText() {
        return targetsHudShowHealthText;
    }

    public static void setTargetsHudShowHealthText(boolean enabled) {
        targetsHudShowHealthText = enabled;
    }

    public static boolean targetsHudShowHealthBar() {
        return targetsHudShowHealthBar;
    }

    public static void setTargetsHudShowHealthBar(boolean enabled) {
        targetsHudShowHealthBar = enabled;
    }

    public static boolean backpackHudBlur() {
        return backpackHudBlur;
    }

    public static void setBackpackHudBlur(boolean enabled) {
        backpackHudBlur = enabled;
    }

    public static int backpackHudBackgroundColor() {
        return backpackHudBackgroundColor;
    }

    public static void setBackpackHudBackgroundColor(int color) {
        backpackHudBackgroundColor = color;
    }

    public static int backpackHudSlotColor() {
        return backpackHudSlotColor;
    }

    public static void setBackpackHudSlotColor(int color) {
        backpackHudSlotColor = color;
    }

    public static int backpackHudTextColor() {
        return backpackHudTextColor;
    }

    public static void setBackpackHudTextColor(int color) {
        backpackHudTextColor = color;
    }

    public static int backpackHudAccentColor() {
        return backpackHudAccentColor;
    }

    public static void setBackpackHudAccentColor(int color) {
        backpackHudAccentColor = color;
    }

    public static boolean backpackHudShowTitle() {
        return backpackHudShowTitle;
    }

    public static void setBackpackHudShowTitle(boolean enabled) {
        backpackHudShowTitle = enabled;
    }

    public static boolean backpackHudShowCounts() {
        return backpackHudShowCounts;
    }

    public static void setBackpackHudShowCounts(boolean enabled) {
        backpackHudShowCounts = enabled;
    }

    public static boolean potionBuffHudBlur() {
        return potionBuffHudBlur;
    }

    public static void setPotionBuffHudBlur(boolean enabled) {
        potionBuffHudBlur = enabled;
    }

    public static int potionBuffHudBackgroundColor() {
        return potionBuffHudBackgroundColor;
    }

    public static void setPotionBuffHudBackgroundColor(int color) {
        potionBuffHudBackgroundColor = color;
    }

    public static int potionBuffHudTextColor() {
        return potionBuffHudTextColor;
    }

    public static void setPotionBuffHudTextColor(int color) {
        potionBuffHudTextColor = color;
    }

    public static int potionBuffHudAccentColor() {
        return potionBuffHudAccentColor;
    }

    public static void setPotionBuffHudAccentColor(int color) {
        potionBuffHudAccentColor = color;
    }

    public static boolean potionBuffHudShowIcons() {
        return potionBuffHudShowIcons;
    }

    public static void setPotionBuffHudShowIcons(boolean enabled) {
        potionBuffHudShowIcons = enabled;
    }

    public static boolean potionBuffHudShowDuration() {
        return potionBuffHudShowDuration;
    }

    public static void setPotionBuffHudShowDuration(boolean enabled) {
        potionBuffHudShowDuration = enabled;
    }

    public static boolean potionBuffHudEffectColors() {
        return potionBuffHudEffectColors;
    }

    public static void setPotionBuffHudEffectColors(boolean enabled) {
        potionBuffHudEffectColors = enabled;
    }

    public static boolean speedHudBlur() {
        return speedHudBlur;
    }

    public static void setSpeedHudBlur(boolean enabled) {
        speedHudBlur = enabled;
    }

    public static int speedHudBackgroundColor() {
        return speedHudBackgroundColor;
    }

    public static void setSpeedHudBackgroundColor(int color) {
        speedHudBackgroundColor = color;
    }

    public static int speedHudFieldColor() {
        return speedHudFieldColor;
    }

    public static void setSpeedHudFieldColor(int color) {
        speedHudFieldColor = color;
    }

    public static int speedHudTextColor() {
        return speedHudTextColor;
    }

    public static void setSpeedHudTextColor(int color) {
        speedHudTextColor = color;
    }

    public static int speedHudAccentColor() {
        return speedHudAccentColor;
    }

    public static void setSpeedHudAccentColor(int color) {
        speedHudAccentColor = color;
    }

    public static boolean speedHudShowChart() {
        return speedHudShowChart;
    }

    public static void setSpeedHudShowChart(boolean enabled) {
        speedHudShowChart = enabled;
    }

    public static boolean speedHudShowValue() {
        return speedHudShowValue;
    }

    public static void setSpeedHudShowValue(boolean enabled) {
        speedHudShowValue = enabled;
    }

}


