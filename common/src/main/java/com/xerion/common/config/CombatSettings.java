package com.xerion.common.config;

public final class CombatSettings {
    public static final String BEHAVIOR_DEFAULT = "Default";
    public static final String BEHAVIOR_SMASH = "Smash";
    public static final String BEHAVIOR_RANDOM = "Random";
    public static final String BLOCK_ANIMATION_OFF = "Off";
    public static final String BLOCK_ANIMATION_1_7 = "1.7";
    public static final String BLOCK_ANIMATION_PUSHDOWN = "Pushdown";
    public static final String BLOCK_ANIMATION_SCALE = "Scale";
    public static final String BLOCK_ANIMATION_LEAKED = "Leaked";
    public static final String BLOCK_ANIMATION_NINJA = "Ninja";
    public static final String BLOCK_ANIMATION_DOWN = "Down";
    public static final String AUTOBLOCK_OFF = "Off";
    public static final String AUTOBLOCK_VANILLA = "Vanilla";
    public static final String AUTOBLOCK_FAKE = "Fake";
    public static final String AIM_PRIORITY_CROSSHAIR = "Crosshair";
    public static final String AIM_PRIORITY_DISTANCE = "Distance";
    public static final String AIM_PRIORITY_HITCHANCE = "Hitchance";
    public static final String AIM_PRIORITY_LOW_HEALTH = "Low Health";
    public static final String AUTO_CRYSTAL_PRIORITY_DAMAGE = "Damage";
    public static final String AUTO_CRYSTAL_PRIORITY_DISTANCE = "Distance";
    public static final String AUTO_CRYSTAL_PRIORITY_HEALTH = "Health";
    public static final String TARGET_STRAFE_DIRECTION_AUTO = "Auto";
    public static final String TARGET_STRAFE_DIRECTION_LEFT = "Left";
    public static final String TARGET_STRAFE_DIRECTION_RIGHT = "Right";
    public static final String AUTO_WEAPON_MODE_BEST = "Best";
    public static final String AUTO_WEAPON_MODE_SWORD = "Sword";
    public static final String AUTO_WEAPON_MODE_AXE = "Axe";
    public static final String AUTO_HEAL_MODE_ANY = "Any";
    public static final String AUTO_HEAL_MODE_GOLDEN = "Golden";
    public static final String AUTO_HEAL_MODE_ENCHANTED = "Enchanted";
    public static final String WEB_AURA_MODE_FEET = "Feet";
    public static final String WEB_AURA_MODE_HEAD = "Head";
    public static final String WEB_AURA_MODE_BOTH = "Both";
    public static final String AUTO_TRAP_MODE_FEET = "Feet";
    public static final String AUTO_TRAP_MODE_HEAD = "Head";
    public static final String AUTO_TRAP_MODE_FULL = "Full";
    public static final String SURROUND_MODE_NORMAL = "Normal";
    public static final String SURROUND_MODE_FULL = "Full";
    public static final String HOLE_FILL_MODE_BOTH = "Both";
    public static final String HOLE_FILL_MODE_TARGET = "Target";
    public static final String HOLE_FILL_MODE_SELF = "Self";
    public static final String AUTO_EXP_MODE_ALL = "All";
    public static final String AUTO_EXP_MODE_ARMOR = "Armor";
    public static final String AUTO_EXP_MODE_HELD = "Held";
    public static final String TEAM_MODE_ALL = "All";
    public static final String TEAM_MODE_VANILLA = "Vanilla";
    public static final String TEAM_MODE_SCOREBOARD = "Scoreboard";
    public static final String TEAM_MODE_NAME_COLOR = "Name Color";
    public static final String RAGEBOT_PITCH_OFF = "Off";
    public static final String RAGEBOT_PITCH_UP = "Up";
    public static final String RAGEBOT_PITCH_DOWN = "Down";
    public static final String RAGEBOT_PITCH_ZERO = "Zero";
    public static final String RAGEBOT_PITCH_JITTER = "Jitter";
    public static final String RAGEBOT_PITCH_CUSTOM = "Custom";
    public static final String RAGEBOT_YAW_OFF = "Off";
    public static final String RAGEBOT_YAW_AT_TARGETS = "At Targets";
    public static final String RAGEBOT_YAW_LOCAL_VIEW = "Local View";
    public static final String RAGEBOT_YAW_MODIFIER_DISABLE = "Disable";
    public static final String RAGEBOT_YAW_MODIFIER_CENTER = "Center";
    public static final String RAGEBOT_YAW_MODIFIER_OFFSET = "Offset";
    public static final String RAGEBOT_YAW_MODIFIER_RANDOM = "Random";
    public static final String RAGEBOT_YAW_MODIFIER_SPIN = "Spin";
    public static final String RAGEBOT_YAW_MODIFIER_3_WAY = "3-way";
    public static final String RAGEBOT_YAW_MODIFIER_5_WAY = "5-way";
    public static final String VELOCITY_MODE_SCALE = "Scale";
    public static final String VELOCITY_MODE_JUMP_RESET = "Jump Reset";
    public static final String VELOCITY_MODE_CUBECRAFT = "CubeCraft";
    public static final String VELOCITY_MODE_HEYPIXEL3 = "Heypixel3";
    public static final String CRITICALS_MODE_PACKET = "Packet";
    public static final String CRITICALS_MODE_HEYPIXEL = "Heypixel";

    private static final float RAGEBOT_ANTI_AIM_PITCH_MIN = -90.0f;
    private static final float RAGEBOT_ANTI_AIM_PITCH_MAX = 90.0f;
    private static final float RAGEBOT_ANTI_AIM_YAW_OFFSET_MIN = -180.0f;
    private static final float RAGEBOT_ANTI_AIM_YAW_OFFSET_MAX = 180.0f;

    private static final float REACH_MIN = 3.0f;
    private static final float REACH_MAX = 6.0f;
    private static final float CPS_MIN = 1.0f;
    private static final float CPS_MAX = 20.0f;
    private static final float SWING_CPS_MIN = 1.0f;
    private static final float SWING_CPS_MAX = 20.0f;
    private static final float RANGE_MIN = 3.0f;
    private static final float RANGE_MAX = 15.0f;
    private static final float ROTATION_RANGE_MIN = 3.0f;
    private static final float ROTATION_RANGE_MAX = 8.0f;
    private static final float SWING_RANGE_MIN = 3.0f;
    private static final float SWING_RANGE_MAX = 8.0f;
    private static final float FOV_MIN = 30.0f;
    private static final float FOV_MAX = 360.0f;
    private static final float SMOOTHNESS_MIN = 0.0f;
    private static final float SMOOTHNESS_MAX = 100.0f;
    private static final float RANDOMIZATION_MIN = 0.0f;
    private static final float RANDOMIZATION_MAX = 35.0f;
    private static final float VELOCITY_MIN = 0.0f;
    private static final float VELOCITY_MAX = 1.0f;
    private static final float AIM_ASSIST_STRENGTH_MIN = 1.0f;
    private static final float AIM_ASSIST_STRENGTH_MAX = 100.0f;
    private static final float AIM_ASSIST_SMOOTH_MIN = 0.0f;
    private static final float AIM_ASSIST_SMOOTH_MAX = 100.0f;
    private static final float AIM_ASSIST_RANGE_MIN = 2.0f;
    private static final float AIM_ASSIST_RANGE_MAX = 90.0f;
    private static final float PROJECTILE_AIM_RANGE_MIN = 2.0f;
    private static final float PROJECTILE_AIM_RANGE_MAX = 90.0f;
    private static final float PROJECTILE_AIM_SMOOTH_MIN = 0.0f;
    private static final float PROJECTILE_AIM_SMOOTH_MAX = 100.0f;
    private static final float PROJECTILE_AIM_PREDICTION_MIN = 0.0f;
    private static final float PROJECTILE_AIM_PREDICTION_MAX = 150.0f;
    private static final float GUN_AIM_RANGE_MIN = 2.0f;
    private static final float GUN_AIM_RANGE_MAX = 90.0f;
    private static final float GUN_AIM_SMOOTH_MIN = 0.0f;
    private static final float GUN_AIM_SMOOTH_MAX = 100.0f;
    private static final float GUN_AIM_PREDICTION_MIN = 0.0f;
    private static final float GUN_AIM_PREDICTION_MAX = 150.0f;
    private static final float RAGEBOT_FOV_MIN = 30.0f;
    private static final float RAGEBOT_FOV_MAX = 360.0f;
    private static final float AUTO_PEARL_DISTANCE_MIN = 4.0f;
    private static final float AUTO_PEARL_DISTANCE_MAX = 64.0f;
    private static final float PEARL_AIM_RANGE_MIN = 2.0f;
    private static final float PEARL_AIM_RANGE_MAX = 90.0f;
    private static final float PEARL_AIM_SMOOTH_MIN = 0.0f;
    private static final float PEARL_AIM_SMOOTH_MAX = 100.0f;
    private static final float PEARL_AIM_PREDICTION_MIN = 0.0f;
    private static final float PEARL_AIM_PREDICTION_MAX = 150.0f;
    private static final float AUTO_PEARL_DELAY_MIN = 5.0f;
    private static final float AUTO_PEARL_DELAY_MAX = 60.0f;
    private static final float BOW_SPAM_CHARGE_MIN = 1.0f;
    private static final float BOW_SPAM_CHARGE_MAX = 20.0f;
    private static final float BOW_SPAM_DELAY_MIN = 0.0f;
    private static final float BOW_SPAM_DELAY_MAX = 10.0f;
    private static final float FAST_USE_CHARGE_MIN = 1.0f;
    private static final float FAST_USE_CHARGE_MAX = 20.0f;
    private static final float FAST_USE_DELAY_MIN = 0.0f;
    private static final float FAST_USE_DELAY_MAX = 10.0f;
    private static final float FAST_USE_PACKETS_MIN = 0.0f;
    private static final float FAST_USE_PACKETS_MAX = 25.0f;
    private static final float AUTO_CLICKER_CPS_MIN = 1.0f;
    private static final float AUTO_CLICKER_CPS_MAX = 20.0f;
    private static final float AUTO_HEAL_HEALTH_MIN = 1.0f;
    private static final float AUTO_HEAL_HEALTH_MAX = 20.0f;
    private static final float AUTO_HEAL_DELAY_MIN = 1.0f;
    private static final float AUTO_HEAL_DELAY_MAX = 40.0f;
    private static final float AUTO_TOTEM_HEALTH_MIN = 1.0f;
    private static final float AUTO_TOTEM_HEALTH_MAX = 20.0f;
    private static final float AUTO_EXP_DURABILITY_MIN = 5.0f;
    private static final float AUTO_EXP_DURABILITY_MAX = 100.0f;
    private static final float AUTO_EXP_DELAY_MIN = 1.0f;
    private static final float AUTO_EXP_DELAY_MAX = 20.0f;
    private static final float AUTO_ARMOR_DELAY_MIN = 1.0f;
    private static final float AUTO_ARMOR_DELAY_MAX = 20.0f;
    private static final float BED_AURA_RANGE_MIN = 2.0f;
    private static final float BED_AURA_RANGE_MAX = 7.0f;
    private static final float BED_AURA_DAMAGE_MIN = 1.0f;
    private static final float BED_AURA_DAMAGE_MAX = 20.0f;
    private static final float BED_AURA_SELF_DAMAGE_MIN = 1.0f;
    private static final float BED_AURA_SELF_DAMAGE_MAX = 20.0f;
    private static final float BED_AURA_DELAY_MIN = 0.0f;
    private static final float BED_AURA_DELAY_MAX = 10.0f;
    private static final float ANCHOR_AURA_RANGE_MIN = 2.0f;
    private static final float ANCHOR_AURA_RANGE_MAX = 7.0f;
    private static final float ANCHOR_AURA_DAMAGE_MIN = 1.0f;
    private static final float ANCHOR_AURA_DAMAGE_MAX = 20.0f;
    private static final float ANCHOR_AURA_SELF_DAMAGE_MIN = 1.0f;
    private static final float ANCHOR_AURA_SELF_DAMAGE_MAX = 20.0f;
    private static final float ANCHOR_AURA_DELAY_MIN = 0.0f;
    private static final float ANCHOR_AURA_DELAY_MAX = 10.0f;
    private static final float WEB_AURA_RANGE_MIN = 2.0f;
    private static final float WEB_AURA_RANGE_MAX = 6.0f;
    private static final float WEB_AURA_DELAY_MIN = 1.0f;
    private static final float WEB_AURA_DELAY_MAX = 20.0f;
    private static final float AUTO_TRAP_RANGE_MIN = 2.0f;
    private static final float AUTO_TRAP_RANGE_MAX = 6.0f;
    private static final float AUTO_TRAP_DELAY_MIN = 1.0f;
    private static final float AUTO_TRAP_DELAY_MAX = 20.0f;
    private static final float SURROUND_DELAY_MIN = 1.0f;
    private static final float SURROUND_DELAY_MAX = 20.0f;
    private static final float HOLE_FILL_RANGE_MIN = 2.0f;
    private static final float HOLE_FILL_RANGE_MAX = 8.0f;
    private static final float HOLE_FILL_DELAY_MIN = 1.0f;
    private static final float HOLE_FILL_DELAY_MAX = 20.0f;
    private static final float AUTO_CRYSTAL_RANGE_MIN = 2.0f;
    private static final float AUTO_CRYSTAL_RANGE_MAX = 7.0f;
    private static final float AUTO_CRYSTAL_DAMAGE_MIN = 1.0f;
    private static final float AUTO_CRYSTAL_DAMAGE_MAX = 20.0f;
    private static final float AUTO_CRYSTAL_SELF_DAMAGE_MIN = 1.0f;
    private static final float AUTO_CRYSTAL_SELF_DAMAGE_MAX = 20.0f;
    private static final float AUTO_CRYSTAL_DELAY_MIN = 0.0f;
    private static final float AUTO_CRYSTAL_DELAY_MAX = 10.0f;
    private static final float AUTO_CRYSTAL_FACE_HEALTH_MIN = 1.0f;
    private static final float AUTO_CRYSTAL_FACE_HEALTH_MAX = 20.0f;
    private static final float TARGET_STRAFE_RADIUS_MIN = 1.2f;
    private static final float TARGET_STRAFE_RADIUS_MAX = 5.0f;
    private static final float TARGET_STRAFE_SPEED_MIN = 0.0f;
    private static final float TARGET_STRAFE_SPEED_MAX = 2.0f;
    private static final float HITBOXES_SIZE_MIN = 0.0f;
    private static final float HITBOXES_SIZE_MAX = 1.0f;
    private static final float BACKTRACK_TICKS_MIN = 1.0f;
    private static final float BACKTRACK_TICKS_MAX = 12.0f;

    private static boolean killAuraEnabled;
    private static boolean reachEnabled;
    private static float reach = 3.6f;
    private static float cpsLimit = 12.0f;
    private static float swingCps = 10.0f;
    private static String attackMode = "Single";
    private static String behaviorMode = BEHAVIOR_DEFAULT;
    private static String killAuraBlockAnimation = BLOCK_ANIMATION_OFF;
    private static String autoblockMode = AUTOBLOCK_OFF;
    private static String weapon = "Best Weapon";
    private static boolean targetPlayers = true;
    private static boolean targetMobs = true;
    private static boolean targetInvisible;
    private static boolean teamsCheck = true;
    private static boolean friendsCheck = true;
    private static String teamMode = TEAM_MODE_ALL;
    private static float range = 4.8f;
    private static float rotationRange = 5.0f;
    private static float swingRange = 5.0f;
    private static float fov = 180.0f;
    private static String priority = "Distance";
    private static boolean silentRotations;
    private static boolean smoothAim = true;
    private static boolean killAuraHeypixelBypass;
    private static boolean rayTrace = true;
    private static boolean throughWalls;
    private static boolean hideFakeSwings = true;
    private static boolean killAuraRequireAttackKey;
    private static boolean killAuraRequireWeapon;
    private static boolean killAuraSmartWeapon;
    private static boolean attackCooldown19 = true;
    private static float smoothness = 36.0f;
    private static float randomization = 12.0f;
    private static boolean criticalsEnabled;
    private static String criticalsMode = CRITICALS_MODE_PACKET;
    private static boolean criticalsGroundOnly;
    private static boolean criticalsAutoJump = true;
    private static boolean criticalsSkipTicks = true;
    private static float criticalsRange = 3.0f;
    private static boolean fakeLagEnabled;
    private static String fakeLagMode = "Dynamic";
    private static float fakeLagRange = 5.0f;
    private static int fakeLagDelayMs = 300;
    private static int fakeLagRecoilMs = 250;
    private static boolean fakeLagFlushEntity = true;
    private static boolean fakeLagFlushBlock = true;
    private static boolean fakeLagFlushAction = true;
    private static boolean fakeLagDebug;
    private static boolean attackDelayEnabled;
    private static int attackDelayMaxCooldown;
    private static boolean autoHeadEnabled;
    private static int autoHeadHealthPercent = 50;
    private static int autoHeadDelayMs = 750;
    private static boolean autoBlockEnabled;
    private static boolean autoBlockRequireAttackKey;
    private static float autoBlockRange = 3.0f;
    private static boolean knockbackEnabled;
    private static boolean sprintResetEnabled;
    private static boolean antiBotEnabled;
    private static boolean antiBotTabListCheck = true;
    private static boolean antiBotDuplicateCheck = true;
    private static boolean antiBotInvalidProfileCheck = true;
    private static boolean antiBotZeroPingCheck;
    private static boolean autoWeaponEnabled;
    private static String autoWeaponMode = AUTO_WEAPON_MODE_BEST;
    private static boolean autoWeaponRestoreSlot = true;
    private static boolean shieldBreakerEnabled;
    private static boolean shieldBreakerRestoreSlot = true;
    private static boolean velocityEnabled;
    private static String velocityMode = VELOCITY_MODE_SCALE;
    private static float velocityHorizontal = 0.0f;
    private static float velocityVertical = 0.0f;
    private static boolean velocityAlternativeBypass = true;
    private static int velocityBufferDelayTicks = 5;
    private static boolean triggerBotEnabled;
    private static float triggerBotCps = 10.0f;
    private static float triggerBotRange = 3.6f;
    private static boolean triggerBotOnlyWeapon = true;
    private static boolean triggerBotRequireAttackKey;
    private static boolean aimAssistEnabled;
    private static float aimAssistStrength = 45.0f;
    private static float aimAssistSmooth = 55.0f;
    private static float aimAssistRange = 28.0f;
    private static String aimAssistPriority = AIM_PRIORITY_CROSSHAIR;
    private static boolean projectileAimEnabled;
    private static float projectileAimRange = 28.0f;
    private static float projectileAimSmooth = 30.0f;
    private static float projectileAimPrediction = 100.0f;
    private static String projectileAimPriority = AIM_PRIORITY_CROSSHAIR;
    private static boolean gunAimEnabled;
    private static float gunAimRange = 36.0f;
    private static float gunAimSmooth = 28.0f;
    private static float gunAimPrediction = 35.0f;
    private static String gunAimPriority = AIM_PRIORITY_CROSSHAIR;
    private static boolean gunAimRequireUseKey;
    private static String gunAimItemIds = "";
    private static boolean ragebotEnabled;
    private static boolean ragebotRefineShot;
    private static boolean ragebotSilentAim;
    private static boolean ragebotForceSilentAim;
    private static boolean ragebotAutomaticFire;
    private static float ragebotFov = 36.0f;
    private static int ragebotHitboxesMask = 7;
    private static boolean ragebotAutoStop;
    private static boolean ragebotQuickScope;
    private static boolean ragebotRemoveRecoil;
    private static boolean ragebotRemoveSpread;
    private static boolean ragebotDoubleTap;
    private static boolean ragebotHideShots;
    private static boolean ragebotAntiAimEnabled;
    private static String ragebotAntiAimPitch = RAGEBOT_PITCH_OFF;
    private static float ragebotAntiAimCustomPitch;
    private static String ragebotAntiAimYaw = RAGEBOT_YAW_OFF;
    private static float ragebotAntiAimYawBase;
    private static String ragebotAntiAimYawModifier = RAGEBOT_YAW_MODIFIER_DISABLE;
    private static float ragebotAntiAimYawOffset;
    private static float ragebotSmooth = 28.0f;
    private static float ragebotPrediction = 35.0f;
    private static String ragebotPriority = AIM_PRIORITY_DISTANCE;
    private static String ragebotItemIds = "";
    private static boolean autoPearlEnabled;
    private static boolean pearlAimEnabled = true;
    private static float autoPearlDistance = 24.0f;
    private static float pearlAimRange = 28.0f;
    private static float pearlAimSmooth = 35.0f;
    private static float pearlAimPrediction = 100.0f;
    private static float autoPearlDelay = 20.0f;
    private static String pearlAimPriority = AIM_PRIORITY_CROSSHAIR;
    private static boolean autoPearlSilentRotate = true;
    private static boolean autoPearlRestoreSlot = true;
    private static boolean bowSpamEnabled;
    private static float bowSpamCharge = 4.0f;
    private static float bowSpamDelay = 1.0f;
    private static boolean bowSpamAutoUse = true;
    private static boolean fastUseEnabled;
    private static float fastUseCharge = 3.0f;
    private static float fastUseDelay = 1.0f;
    private static float fastUsePackets = 12.0f;
    private static boolean fastUseBow = true;
    private static boolean fastUseTrident = true;
    private static boolean fastUseCrossbow = true;
    private static boolean fastUseThrowables = true;
    private static boolean fastUseAutoUse = true;
    private static boolean fastUsePacketBoost = true;
    private static boolean autoClickerEnabled;
    private static float autoClickerCps = 10.0f;
    private static boolean autoHealEnabled;
    private static String autoHealMode = AUTO_HEAL_MODE_ANY;
    private static float autoHealHealth = 12.0f;
    private static float autoHealDelay = 8.0f;
    private static boolean autoHealIncludeAbsorption;
    private static boolean autoHealRestoreSlot = true;
    private static boolean autoTotemEnabled;
    private static float autoTotemHealth = 10.0f;
    private static boolean autoExpEnabled;
    private static String autoExpMode = AUTO_EXP_MODE_ALL;
    private static float autoExpDurability = 80.0f;
    private static float autoExpDelay = 2.0f;
    private static boolean autoExpMendingOnly = true;
    private static boolean autoExpSilentRotate = true;
    private static boolean autoExpRestoreSlot = true;
    private static boolean autoArmorEnabled;
    private static float autoArmorDelay = 4.0f;
    private static boolean autoArmorPreferDurability = true;
    private static boolean autoArmorAllowBindingCurse;
    private static boolean bedAuraEnabled;
    private static boolean bedAuraPlace = true;
    private static boolean bedAuraBreak = true;
    private static float bedAuraRange = 5.0f;
    private static float bedAuraMinDamage = 5.0f;
    private static float bedAuraMaxSelfDamage = 10.0f;
    private static float bedAuraPlaceDelay = 3.0f;
    private static float bedAuraBreakDelay = 2.0f;
    private static boolean bedAuraRayTrace;
    private static boolean bedAuraRotate = true;
    private static boolean bedAuraAntiSuicide = true;
    private static boolean anchorAuraEnabled;
    private static boolean anchorAuraPlace = true;
    private static boolean anchorAuraBreak = true;
    private static float anchorAuraRange = 5.0f;
    private static float anchorAuraMinDamage = 5.0f;
    private static float anchorAuraMaxSelfDamage = 10.0f;
    private static float anchorAuraPlaceDelay = 3.0f;
    private static float anchorAuraBreakDelay = 2.0f;
    private static boolean anchorAuraRayTrace;
    private static boolean anchorAuraRotate = true;
    private static boolean anchorAuraAntiSuicide = true;
    private static boolean webAuraEnabled;
    private static String webAuraMode = WEB_AURA_MODE_BOTH;
    private static float webAuraRange = 4.5f;
    private static float webAuraDelay = 4.0f;
    private static boolean webAuraRayTrace = true;
    private static boolean webAuraRotate = true;
    private static boolean webAuraRestoreSlot = true;
    private static boolean autoTrapEnabled;
    private static String autoTrapMode = AUTO_TRAP_MODE_FULL;
    private static float autoTrapRange = 4.5f;
    private static float autoTrapDelay = 4.0f;
    private static boolean autoTrapRayTrace = true;
    private static boolean autoTrapRotate = true;
    private static boolean autoTrapRestoreSlot = true;
    private static boolean surroundEnabled;
    private static String surroundMode = SURROUND_MODE_NORMAL;
    private static float surroundDelay = 4.0f;
    private static boolean surroundFloor;
    private static boolean surroundRayTrace = true;
    private static boolean surroundRotate = true;
    private static boolean surroundRestoreSlot = true;
    private static boolean holeFillEnabled;
    private static String holeFillMode = HOLE_FILL_MODE_BOTH;
    private static float holeFillRange = 5.0f;
    private static float holeFillDelay = 4.0f;
    private static boolean holeFillRayTrace = true;
    private static boolean holeFillRotate = true;
    private static boolean holeFillRestoreSlot = true;
    private static boolean autoCrystalEnabled;
    private static boolean autoCrystalPlace = true;
    private static boolean autoCrystalBreak = true;
    private static float autoCrystalRange = 6.0f;
    private static float autoCrystalMinDamage = 1.0f;
    private static float autoCrystalMaxSelfDamage = 20.0f;
    private static float autoCrystalDelay = 3.0f;
    private static float autoCrystalPlaceDelay = 3.0f;
    private static float autoCrystalBreakDelay = 2.0f;
    private static boolean autoCrystalRayTrace;
    private static boolean autoCrystalRotate = true;
    private static boolean autoCrystalFacePlace = true;
    private static float autoCrystalFacePlaceHealth = 8.0f;
    private static boolean autoCrystalAntiSuicide = true;
    private static String autoCrystalPriority = AUTO_CRYSTAL_PRIORITY_DAMAGE;
    private static boolean targetStrafeEnabled;
    private static float targetStrafeRadius = 2.6f;
    private static float targetStrafeSpeed = 1.0f;
    private static String targetStrafeDirection = TARGET_STRAFE_DIRECTION_AUTO;
    private static boolean targetStrafeAutoJump = true;
    private static boolean hitboxesEnabled;
    private static float hitboxesSize = 0.25f;
    private static boolean backtrackEnabled;
    private static float backtrackTicks = 4.0f;

    private CombatSettings() {
    }

    public static boolean killAuraEnabled() {
        return killAuraEnabled;
    }

    public static void setKillAuraEnabled(boolean enabled) {
        killAuraEnabled = enabled;
    }

    public static boolean reachEnabled() {
        return reachEnabled;
    }

    public static void setReachEnabled(boolean enabled) {
        reachEnabled = enabled;
    }

    public static float reach() {
        return reach;
    }

    public static float reachSlider() {
        return inverse(reach, REACH_MIN, REACH_MAX);
    }

    public static void setReachSlider(float value) {
        reach = range(value, REACH_MIN, REACH_MAX);
    }

    public static float cpsLimit() {
        return cpsLimit;
    }

    public static float cpsLimitSlider() {
        return inverse(cpsLimit, CPS_MIN, CPS_MAX);
    }

    public static void setCpsLimitSlider(float value) {
        cpsLimit = range(value, CPS_MIN, CPS_MAX);
    }

    public static float swingCps() {
        return swingCps;
    }

    public static float swingCpsSlider() {
        return inverse(swingCps, SWING_CPS_MIN, SWING_CPS_MAX);
    }

    public static void setSwingCpsSlider(float value) {
        swingCps = range(value, SWING_CPS_MIN, SWING_CPS_MAX);
    }

    public static String attackMode() {
        return attackMode;
    }

    public static void setAttackMode(String mode) {
        if ("Switch".equals(mode) || "Multi".equals(mode)) {
            attackMode = mode;
        } else {
            attackMode = "Single";
        }
    }

    public static String behaviorMode() {
        return behaviorMode;
    }

    public static void setBehaviorMode(String mode) {
        if (BEHAVIOR_SMASH.equals(mode) || "Keep crit".equals(mode)
                || "\u4fdd\u6301\u91cd\u51fb".equals(mode)
                || "\u6dc7\u6fef\u5bd4\u95b2\u5d85\u5681".equals(mode)) {
            behaviorMode = BEHAVIOR_SMASH;
        } else if (BEHAVIOR_RANDOM.equals(mode)
                || "\u968f\u673a".equals(mode)
                || "\u95c5\u5fd4\u6e80".equals(mode)) {
            behaviorMode = BEHAVIOR_RANDOM;
        } else {
            behaviorMode = BEHAVIOR_DEFAULT;
        }
    }

    public static String killAuraBlockAnimation() {
        return killAuraBlockAnimation;
    }

    public static void setKillAuraBlockAnimation(String value) {
        if (BLOCK_ANIMATION_1_7.equals(value)
                || BLOCK_ANIMATION_PUSHDOWN.equals(value)
                || BLOCK_ANIMATION_SCALE.equals(value)
                || BLOCK_ANIMATION_LEAKED.equals(value)
                || BLOCK_ANIMATION_NINJA.equals(value)
                || BLOCK_ANIMATION_DOWN.equals(value)) {
            killAuraBlockAnimation = value;
        } else {
            killAuraBlockAnimation = BLOCK_ANIMATION_OFF;
        }
    }

    public static String autoblockMode() {
        return autoblockMode;
    }

    public static void setAutoblockMode(String value) {
        if (AUTOBLOCK_VANILLA.equals(value) || AUTOBLOCK_FAKE.equals(value)) {
            autoblockMode = value;
        } else {
            autoblockMode = AUTOBLOCK_OFF;
        }
    }

    public static boolean hideFakeSwings() {
        return hideFakeSwings;
    }

    public static void setHideFakeSwings(boolean enabled) {
        hideFakeSwings = enabled;
    }

    public static boolean killAuraRequireAttackKey() {
        return killAuraRequireAttackKey;
    }

    public static void setKillAuraRequireAttackKey(boolean enabled) {
        killAuraRequireAttackKey = enabled;
    }

    public static boolean killAuraRequireWeapon() {
        return killAuraRequireWeapon;
    }

    public static void setKillAuraRequireWeapon(boolean enabled) {
        killAuraRequireWeapon = enabled;
    }

    public static boolean killAuraSmartWeapon() {
        return killAuraSmartWeapon;
    }

    public static void setKillAuraSmartWeapon(boolean enabled) {
        killAuraSmartWeapon = enabled;
    }

    public static boolean attackCooldown19() {
        return attackCooldown19;
    }

    public static void setAttackCooldown19(boolean enabled) {
        attackCooldown19 = enabled;
    }

    public static String weapon() {
        return weapon;
    }

    public static void setWeapon(String value) {
        if ("Sword".equals(value) || "Axe".equals(value)) {
            weapon = value;
        } else {
            weapon = "Best Weapon";
        }
    }

    public static boolean targetPlayers() {
        return targetPlayers;
    }

    public static void setTargetPlayers(boolean enabled) {
        targetPlayers = enabled;
    }

    public static boolean targetMobs() {
        return targetMobs;
    }

    public static void setTargetMobs(boolean enabled) {
        targetMobs = enabled;
    }

    public static boolean targetInvisible() {
        return targetInvisible;
    }

    public static void setTargetInvisible(boolean enabled) {
        targetInvisible = enabled;
    }

    public static boolean teamsCheck() {
        return teamsCheck;
    }

    public static void setTeamsCheck(boolean enabled) {
        teamsCheck = enabled;
    }

    public static boolean friendsCheck() {
        return friendsCheck;
    }

    public static void setFriendsCheck(boolean enabled) {
        friendsCheck = enabled;
    }

    public static String teamMode() {
        return teamMode;
    }

    public static void setTeamMode(String value) {
        if ("Name color".equals(value)) {
            value = TEAM_MODE_NAME_COLOR;
        }
        if (TEAM_MODE_VANILLA.equals(value)
                || TEAM_MODE_SCOREBOARD.equals(value)
                || TEAM_MODE_NAME_COLOR.equals(value)) {
            teamMode = value;
        } else {
            teamMode = TEAM_MODE_ALL;
        }
    }

    public static float range() {
        return range;
    }

    public static float rangeSlider() {
        return inverse(range, RANGE_MIN, RANGE_MAX);
    }

    public static void setRangeSlider(float value) {
        range = range(value, RANGE_MIN, RANGE_MAX);
    }

    public static float rotationRange() {
        return rotationRange;
    }

    public static float rotationRangeSlider() {
        return inverse(rotationRange, ROTATION_RANGE_MIN, ROTATION_RANGE_MAX);
    }

    public static void setRotationRangeSlider(float value) {
        rotationRange = range(value, ROTATION_RANGE_MIN, ROTATION_RANGE_MAX);
    }

    public static float swingRange() {
        return swingRange;
    }

    public static float swingRangeSlider() {
        return inverse(swingRange, SWING_RANGE_MIN, SWING_RANGE_MAX);
    }

    public static void setSwingRangeSlider(float value) {
        swingRange = range(value, SWING_RANGE_MIN, SWING_RANGE_MAX);
    }

    public static float fov() {
        return fov;
    }

    public static float fovSlider() {
        return inverse(fov, FOV_MIN, FOV_MAX);
    }

    public static void setFovSlider(float value) {
        fov = range(value, FOV_MIN, FOV_MAX);
    }

    public static String priority() {
        return priority;
    }

    public static void setPriority(String value) {
        if ("Health".equals(value) || "Armor".equals(value) || "Angle".equals(value)) {
            priority = value;
        } else {
            priority = "Distance";
        }
    }

    public static boolean silentRotations() {
        return silentRotations;
    }

    public static void setSilentRotations(boolean enabled) {
        silentRotations = enabled;
    }

    public static boolean smoothAim() {
        return smoothAim;
    }

    public static void setSmoothAim(boolean enabled) {
        smoothAim = enabled;
    }

    public static boolean killAuraHeypixelBypass() {
        return killAuraHeypixelBypass;
    }

    public static void setKillAuraHeypixelBypass(boolean enabled) {
        killAuraHeypixelBypass = enabled;
    }

    public static boolean rayTrace() {
        return rayTrace;
    }

    public static void setRayTrace(boolean enabled) {
        rayTrace = enabled;
    }

    public static boolean throughWalls() {
        return throughWalls;
    }

    public static void setThroughWalls(boolean enabled) {
        throughWalls = enabled;
    }

    public static float smoothness() {
        return smoothness;
    }

    public static float smoothnessSlider() {
        return inverse(smoothness, SMOOTHNESS_MIN, SMOOTHNESS_MAX);
    }

    public static void setSmoothnessSlider(float value) {
        smoothness = range(value, SMOOTHNESS_MIN, SMOOTHNESS_MAX);
    }

    public static float randomization() {
        return randomization;
    }

    public static float randomizationSlider() {
        return inverse(randomization, RANDOMIZATION_MIN, RANDOMIZATION_MAX);
    }

    public static void setRandomizationSlider(float value) {
        randomization = range(value, RANDOMIZATION_MIN, RANDOMIZATION_MAX);
    }

    public static boolean criticalsEnabled() {
        return criticalsEnabled;
    }

    public static void setCriticalsEnabled(boolean enabled) {
        criticalsEnabled = enabled;
    }

    public static String criticalsMode() {
        return criticalsMode;
    }

    public static void setCriticalsMode(String mode) {
        criticalsMode = CRITICALS_MODE_HEYPIXEL.equals(mode)
                ? CRITICALS_MODE_HEYPIXEL : CRITICALS_MODE_PACKET;
    }

    public static boolean criticalsGroundOnly() {
        return criticalsGroundOnly;
    }

    public static void setCriticalsGroundOnly(boolean enabled) {
        criticalsGroundOnly = enabled;
    }

    public static boolean criticalsAutoJump() {
        return criticalsAutoJump;
    }

    public static void setCriticalsAutoJump(boolean enabled) {
        criticalsAutoJump = enabled;
    }

    public static boolean criticalsSkipTicks() {
        return criticalsSkipTicks;
    }

    public static void setCriticalsSkipTicks(boolean enabled) {
        criticalsSkipTicks = enabled;
    }

    public static float criticalsRange() {
        return criticalsRange;
    }

    public static float criticalsRangeSlider() {
        return inverse(criticalsRange, 1.0f, 3.2f);
    }

    public static void setCriticalsRangeSlider(float value) {
        criticalsRange = range(value, 1.0f, 3.2f);
    }

    public static boolean fakeLagEnabled() {
        return fakeLagEnabled;
    }

    public static void setFakeLagEnabled(boolean enabled) {
        fakeLagEnabled = enabled;
    }

    public static String fakeLagMode() {
        return fakeLagMode;
    }

    public static void setFakeLagMode(String mode) {
        fakeLagMode = "Constant".equals(mode) ? "Constant" : "Dynamic";
    }

    public static float fakeLagRange() {
        return fakeLagRange;
    }

    public static float fakeLagRangeSlider() {
        return inverse(fakeLagRange, 0.0f, 10.0f);
    }

    public static void setFakeLagRangeSlider(float value) {
        fakeLagRange = range(value, 0.0f, 10.0f);
    }

    public static int fakeLagDelayMs() {
        return fakeLagDelayMs;
    }

    public static float fakeLagDelaySlider() {
        return inverse(fakeLagDelayMs, 0.0f, 1000.0f);
    }

    public static void setFakeLagDelaySlider(float value) {
        fakeLagDelayMs = Math.round(range(value, 0.0f, 1000.0f));
    }

    public static int fakeLagRecoilMs() {
        return fakeLagRecoilMs;
    }

    public static float fakeLagRecoilSlider() {
        return inverse(fakeLagRecoilMs, 0.0f, 1000.0f);
    }

    public static void setFakeLagRecoilSlider(float value) {
        fakeLagRecoilMs = Math.round(range(value, 0.0f, 1000.0f));
    }

    public static boolean fakeLagFlushEntity() {
        return fakeLagFlushEntity;
    }

    public static void setFakeLagFlushEntity(boolean enabled) {
        fakeLagFlushEntity = enabled;
    }

    public static boolean fakeLagFlushBlock() {
        return fakeLagFlushBlock;
    }

    public static void setFakeLagFlushBlock(boolean enabled) {
        fakeLagFlushBlock = enabled;
    }

    public static boolean fakeLagFlushAction() {
        return fakeLagFlushAction;
    }

    public static void setFakeLagFlushAction(boolean enabled) {
        fakeLagFlushAction = enabled;
    }

    public static boolean fakeLagDebug() {
        return fakeLagDebug;
    }

    public static void setFakeLagDebug(boolean enabled) {
        fakeLagDebug = enabled;
    }

    public static boolean attackDelayEnabled() {
        return attackDelayEnabled;
    }

    public static void setAttackDelayEnabled(boolean enabled) {
        attackDelayEnabled = enabled;
    }

    public static int attackDelayMaxCooldown() {
        return attackDelayMaxCooldown;
    }

    public static float attackDelayMaxCooldownSlider() {
        return inverse(attackDelayMaxCooldown, 0.0f, 9.0f);
    }

    public static void setAttackDelayMaxCooldownSlider(float value) {
        attackDelayMaxCooldown = Math.round(range(value, 0.0f, 9.0f));
    }

    public static boolean autoHeadEnabled() {
        return autoHeadEnabled;
    }

    public static void setAutoHeadEnabled(boolean enabled) {
        autoHeadEnabled = enabled;
    }

    public static int autoHeadHealthPercent() {
        return autoHeadHealthPercent;
    }

    public static float autoHeadHealthSlider() {
        return inverse(autoHeadHealthPercent, 5.0f, 95.0f);
    }

    public static void setAutoHeadHealthSlider(float value) {
        autoHeadHealthPercent = Math.round(range(value, 5.0f, 95.0f));
    }

    public static int autoHeadDelayMs() {
        return autoHeadDelayMs;
    }

    public static float autoHeadDelaySlider() {
        return inverse(autoHeadDelayMs, 0.0f, 3000.0f);
    }

    public static void setAutoHeadDelaySlider(float value) {
        autoHeadDelayMs = Math.round(range(value, 0.0f, 3000.0f));
    }

    public static boolean autoBlockEnabled() {
        return autoBlockEnabled;
    }

    public static void setAutoBlockEnabled(boolean enabled) {
        autoBlockEnabled = enabled;
    }

    public static boolean autoBlockRequireAttackKey() {
        return autoBlockRequireAttackKey;
    }

    public static void setAutoBlockRequireAttackKey(boolean enabled) {
        autoBlockRequireAttackKey = enabled;
    }

    public static float autoBlockRange() {
        return autoBlockRange;
    }

    public static float autoBlockRangeSlider() {
        return inverse(autoBlockRange, 3.0f, 8.0f);
    }

    public static void setAutoBlockRangeSlider(float value) {
        autoBlockRange = range(value, 3.0f, 8.0f);
    }

    public static boolean knockbackEnabled() {
        return knockbackEnabled;
    }

    public static void setKnockbackEnabled(boolean enabled) {
        knockbackEnabled = enabled;
    }

    public static boolean sprintResetEnabled() {
        return sprintResetEnabled;
    }

    public static void setSprintResetEnabled(boolean enabled) {
        sprintResetEnabled = enabled;
    }

    public static boolean antiBotEnabled() {
        return antiBotEnabled;
    }

    public static void setAntiBotEnabled(boolean enabled) {
        antiBotEnabled = enabled;
    }

    public static boolean antiBotTabListCheck() {
        return antiBotTabListCheck;
    }

    public static void setAntiBotTabListCheck(boolean enabled) {
        antiBotTabListCheck = enabled;
    }

    public static boolean antiBotDuplicateCheck() {
        return antiBotDuplicateCheck;
    }

    public static void setAntiBotDuplicateCheck(boolean enabled) {
        antiBotDuplicateCheck = enabled;
    }

    public static boolean antiBotInvalidProfileCheck() {
        return antiBotInvalidProfileCheck;
    }

    public static void setAntiBotInvalidProfileCheck(boolean enabled) {
        antiBotInvalidProfileCheck = enabled;
    }

    public static boolean antiBotZeroPingCheck() {
        return antiBotZeroPingCheck;
    }

    public static void setAntiBotZeroPingCheck(boolean enabled) {
        antiBotZeroPingCheck = enabled;
    }

    public static boolean autoWeaponEnabled() {
        return autoWeaponEnabled;
    }

    public static void setAutoWeaponEnabled(boolean enabled) {
        autoWeaponEnabled = enabled;
    }

    public static String autoWeaponMode() {
        return autoWeaponMode;
    }

    public static void setAutoWeaponMode(String value) {
        if (AUTO_WEAPON_MODE_SWORD.equals(value) || AUTO_WEAPON_MODE_AXE.equals(value)) {
            autoWeaponMode = value;
        } else {
            autoWeaponMode = AUTO_WEAPON_MODE_BEST;
        }
    }

    public static boolean autoWeaponRestoreSlot() {
        return autoWeaponRestoreSlot;
    }

    public static void setAutoWeaponRestoreSlot(boolean enabled) {
        autoWeaponRestoreSlot = enabled;
    }

    public static boolean shieldBreakerEnabled() {
        return shieldBreakerEnabled;
    }

    public static void setShieldBreakerEnabled(boolean enabled) {
        shieldBreakerEnabled = enabled;
    }

    public static boolean shieldBreakerRestoreSlot() {
        return shieldBreakerRestoreSlot;
    }

    public static void setShieldBreakerRestoreSlot(boolean enabled) {
        shieldBreakerRestoreSlot = enabled;
    }

    public static boolean targetStrafeEnabled() {
        return targetStrafeEnabled;
    }

    public static void setTargetStrafeEnabled(boolean enabled) {
        targetStrafeEnabled = enabled;
    }

    public static float targetStrafeRadius() {
        return targetStrafeRadius;
    }

    public static float targetStrafeRadiusSlider() {
        return inverse(targetStrafeRadius, TARGET_STRAFE_RADIUS_MIN, TARGET_STRAFE_RADIUS_MAX);
    }

    public static void setTargetStrafeRadiusSlider(float value) {
        targetStrafeRadius = range(value, TARGET_STRAFE_RADIUS_MIN, TARGET_STRAFE_RADIUS_MAX);
    }

    public static float targetStrafeSpeed() {
        return targetStrafeSpeed;
    }

    public static float targetStrafeSpeedSlider() {
        return inverse(targetStrafeSpeed, TARGET_STRAFE_SPEED_MIN, TARGET_STRAFE_SPEED_MAX);
    }

    public static void setTargetStrafeSpeedSlider(float value) {
        targetStrafeSpeed = range(value, TARGET_STRAFE_SPEED_MIN, TARGET_STRAFE_SPEED_MAX);
    }

    public static String targetStrafeDirection() {
        return targetStrafeDirection;
    }

    public static void setTargetStrafeDirection(String value) {
        if (TARGET_STRAFE_DIRECTION_LEFT.equals(value) || TARGET_STRAFE_DIRECTION_RIGHT.equals(value)) {
            targetStrafeDirection = value;
        } else {
            targetStrafeDirection = TARGET_STRAFE_DIRECTION_AUTO;
        }
    }

    public static boolean targetStrafeAutoJump() {
        return targetStrafeAutoJump;
    }

    public static void setTargetStrafeAutoJump(boolean enabled) {
        targetStrafeAutoJump = enabled;
    }

    public static boolean hitboxesEnabled() {
        return hitboxesEnabled;
    }

    public static void setHitboxesEnabled(boolean enabled) {
        hitboxesEnabled = enabled;
    }

    public static float hitboxesSize() {
        return hitboxesSize;
    }

    public static float hitboxesSizeSlider() {
        return inverse(hitboxesSize, HITBOXES_SIZE_MIN, HITBOXES_SIZE_MAX);
    }

    public static void setHitboxesSizeSlider(float value) {
        hitboxesSize = range(value, HITBOXES_SIZE_MIN, HITBOXES_SIZE_MAX);
    }

    public static boolean backtrackEnabled() {
        return backtrackEnabled;
    }

    public static void setBacktrackEnabled(boolean enabled) {
        backtrackEnabled = enabled;
    }

    public static int backtrackTicks() {
        return Math.round(backtrackTicks);
    }

    public static float backtrackTicksSlider() {
        return inverse(backtrackTicks, BACKTRACK_TICKS_MIN, BACKTRACK_TICKS_MAX);
    }

    public static void setBacktrackTicksSlider(float value) {
        backtrackTicks = range(value, BACKTRACK_TICKS_MIN, BACKTRACK_TICKS_MAX);
    }

    public static boolean velocityEnabled() {
        return velocityEnabled;
    }

    public static void setVelocityEnabled(boolean enabled) {
        velocityEnabled = enabled;
    }

    public static String velocityMode() {
        return velocityMode;
    }

    public static void setVelocityMode(String mode) {
        if (VELOCITY_MODE_JUMP_RESET.equals(mode)
                || VELOCITY_MODE_CUBECRAFT.equals(mode)
                || VELOCITY_MODE_HEYPIXEL3.equals(mode)) {
            velocityMode = mode;
        } else {
            velocityMode = VELOCITY_MODE_SCALE;
        }
    }

    public static boolean velocityAlternativeBypass() {
        return velocityAlternativeBypass;
    }

    public static void setVelocityAlternativeBypass(boolean enabled) {
        velocityAlternativeBypass = enabled;
    }

    public static int velocityBufferDelayTicks() {
        return velocityBufferDelayTicks;
    }

    public static float velocityBufferDelaySlider() {
        return inverse(velocityBufferDelayTicks, 1.0f, 60.0f);
    }

    public static void setVelocityBufferDelaySlider(float value) {
        velocityBufferDelayTicks = Math.round(range(value, 1.0f, 60.0f));
    }

    public static float velocityHorizontal() {
        return velocityHorizontal;
    }

    public static float velocityHorizontalSlider() {
        return inverse(velocityHorizontal, VELOCITY_MIN, VELOCITY_MAX);
    }

    public static void setVelocityHorizontalSlider(float value) {
        velocityHorizontal = range(value, VELOCITY_MIN, VELOCITY_MAX);
    }

    public static float velocityVertical() {
        return velocityVertical;
    }

    public static float velocityVerticalSlider() {
        return inverse(velocityVertical, VELOCITY_MIN, VELOCITY_MAX);
    }

    public static void setVelocityVerticalSlider(float value) {
        velocityVertical = range(value, VELOCITY_MIN, VELOCITY_MAX);
    }

    public static boolean triggerBotEnabled() {
        return triggerBotEnabled;
    }

    public static void setTriggerBotEnabled(boolean enabled) {
        triggerBotEnabled = enabled;
    }

    public static float triggerBotCps() {
        return triggerBotCps;
    }

    public static float triggerBotCpsSlider() {
        return inverse(triggerBotCps, CPS_MIN, CPS_MAX);
    }

    public static void setTriggerBotCpsSlider(float value) {
        triggerBotCps = range(value, CPS_MIN, CPS_MAX);
    }

    public static float triggerBotRange() {
        return triggerBotRange;
    }

    public static float triggerBotRangeSlider() {
        return inverse(triggerBotRange, REACH_MIN, REACH_MAX);
    }

    public static void setTriggerBotRangeSlider(float value) {
        triggerBotRange = range(value, REACH_MIN, REACH_MAX);
    }

    public static boolean triggerBotOnlyWeapon() {
        return triggerBotOnlyWeapon;
    }

    public static void setTriggerBotOnlyWeapon(boolean enabled) {
        triggerBotOnlyWeapon = enabled;
    }

    public static boolean triggerBotRequireAttackKey() {
        return triggerBotRequireAttackKey;
    }

    public static void setTriggerBotRequireAttackKey(boolean enabled) {
        triggerBotRequireAttackKey = enabled;
    }

    public static boolean aimAssistEnabled() {
        return aimAssistEnabled;
    }

    public static void setAimAssistEnabled(boolean enabled) {
        aimAssistEnabled = enabled;
    }

    public static float aimAssistStrength() {
        return aimAssistStrength;
    }

    public static float aimAssistStrengthSlider() {
        return inverse(aimAssistStrength, AIM_ASSIST_STRENGTH_MIN, AIM_ASSIST_STRENGTH_MAX);
    }

    public static void setAimAssistStrengthSlider(float value) {
        aimAssistStrength = range(value, AIM_ASSIST_STRENGTH_MIN, AIM_ASSIST_STRENGTH_MAX);
    }

    public static float aimAssistSmooth() {
        return aimAssistSmooth;
    }

    public static float aimAssistSmoothSlider() {
        return inverse(aimAssistSmooth, AIM_ASSIST_SMOOTH_MIN, AIM_ASSIST_SMOOTH_MAX);
    }

    public static void setAimAssistSmoothSlider(float value) {
        aimAssistSmooth = range(value, AIM_ASSIST_SMOOTH_MIN, AIM_ASSIST_SMOOTH_MAX);
    }

    public static float aimAssistRange() {
        return aimAssistRange;
    }

    public static float aimAssistRangeSlider() {
        return inverse(aimAssistRange, AIM_ASSIST_RANGE_MIN, AIM_ASSIST_RANGE_MAX);
    }

    public static void setAimAssistRangeSlider(float value) {
        aimAssistRange = range(value, AIM_ASSIST_RANGE_MIN, AIM_ASSIST_RANGE_MAX);
    }

    public static String aimAssistPriority() {
        return aimAssistPriority;
    }

    public static void setAimAssistPriority(String value) {
        if ("Low health".equals(value)) {
            value = AIM_PRIORITY_LOW_HEALTH;
        }
        if (AIM_PRIORITY_DISTANCE.equals(value) || AIM_PRIORITY_LOW_HEALTH.equals(value)) {
            aimAssistPriority = value;
        } else {
            aimAssistPriority = AIM_PRIORITY_CROSSHAIR;
        }
    }

    public static boolean projectileAimEnabled() {
        return projectileAimEnabled;
    }

    public static void setProjectileAimEnabled(boolean enabled) {
        projectileAimEnabled = enabled;
    }

    public static float projectileAimRange() {
        return projectileAimRange;
    }

    public static float projectileAimRangeSlider() {
        return inverse(projectileAimRange, PROJECTILE_AIM_RANGE_MIN, PROJECTILE_AIM_RANGE_MAX);
    }

    public static void setProjectileAimRangeSlider(float value) {
        projectileAimRange = range(value, PROJECTILE_AIM_RANGE_MIN, PROJECTILE_AIM_RANGE_MAX);
    }

    public static float projectileAimSmooth() {
        return projectileAimSmooth;
    }

    public static float projectileAimSmoothSlider() {
        return inverse(projectileAimSmooth, PROJECTILE_AIM_SMOOTH_MIN, PROJECTILE_AIM_SMOOTH_MAX);
    }

    public static void setProjectileAimSmoothSlider(float value) {
        projectileAimSmooth = range(value, PROJECTILE_AIM_SMOOTH_MIN, PROJECTILE_AIM_SMOOTH_MAX);
    }

    public static float projectileAimPrediction() {
        return projectileAimPrediction;
    }

    public static float projectileAimPredictionSlider() {
        return inverse(projectileAimPrediction, PROJECTILE_AIM_PREDICTION_MIN, PROJECTILE_AIM_PREDICTION_MAX);
    }

    public static void setProjectileAimPredictionSlider(float value) {
        projectileAimPrediction = range(value, PROJECTILE_AIM_PREDICTION_MIN, PROJECTILE_AIM_PREDICTION_MAX);
    }

    public static String projectileAimPriority() {
        return projectileAimPriority;
    }

    public static void setProjectileAimPriority(String value) {
        if ("Low health".equals(value)) {
            value = AIM_PRIORITY_LOW_HEALTH;
        }
        if (AIM_PRIORITY_DISTANCE.equals(value) || AIM_PRIORITY_LOW_HEALTH.equals(value)) {
            projectileAimPriority = value;
        } else {
            projectileAimPriority = AIM_PRIORITY_CROSSHAIR;
        }
    }

    public static boolean gunAimEnabled() {
        return gunAimEnabled;
    }

    public static void setGunAimEnabled(boolean enabled) {
        gunAimEnabled = enabled;
    }

    public static float gunAimRange() {
        return gunAimRange;
    }

    public static float gunAimRangeSlider() {
        return inverse(gunAimRange, GUN_AIM_RANGE_MIN, GUN_AIM_RANGE_MAX);
    }

    public static void setGunAimRangeSlider(float value) {
        gunAimRange = range(value, GUN_AIM_RANGE_MIN, GUN_AIM_RANGE_MAX);
    }

    public static float gunAimSmooth() {
        return gunAimSmooth;
    }

    public static float gunAimSmoothSlider() {
        return inverse(gunAimSmooth, GUN_AIM_SMOOTH_MIN, GUN_AIM_SMOOTH_MAX);
    }

    public static void setGunAimSmoothSlider(float value) {
        gunAimSmooth = range(value, GUN_AIM_SMOOTH_MIN, GUN_AIM_SMOOTH_MAX);
    }

    public static float gunAimPrediction() {
        return gunAimPrediction;
    }

    public static float gunAimPredictionSlider() {
        return inverse(gunAimPrediction, GUN_AIM_PREDICTION_MIN, GUN_AIM_PREDICTION_MAX);
    }

    public static void setGunAimPredictionSlider(float value) {
        gunAimPrediction = range(value, GUN_AIM_PREDICTION_MIN, GUN_AIM_PREDICTION_MAX);
    }

    public static String gunAimPriority() {
        return gunAimPriority;
    }

    public static void setGunAimPriority(String value) {
        if ("Low health".equals(value)) {
            value = AIM_PRIORITY_LOW_HEALTH;
        }
        if (AIM_PRIORITY_DISTANCE.equals(value) || AIM_PRIORITY_LOW_HEALTH.equals(value)) {
            gunAimPriority = value;
        } else {
            gunAimPriority = AIM_PRIORITY_CROSSHAIR;
        }
    }

    public static boolean gunAimRequireUseKey() {
        return gunAimRequireUseKey;
    }

    public static void setGunAimRequireUseKey(boolean enabled) {
        gunAimRequireUseKey = enabled;
    }

    public static String gunAimItemIds() {
        return gunAimItemIds;
    }

    public static void setGunAimItemIds(String ids) {
        gunAimItemIds = ConfigValueCodec.normalizeIdList(ids);
    }

    public static boolean gunAimItemIdEnabled(String id) {
        return ConfigValueCodec.idListContains(gunAimItemIds, id);
    }

    public static boolean ragebotEnabled() {
        return ragebotEnabled;
    }

    public static void setRagebotEnabled(boolean enabled) {
        ragebotEnabled = enabled;
    }

    public static boolean ragebotRefineShot() {
        return ragebotRefineShot;
    }

    public static void setRagebotRefineShot(boolean enabled) {
        ragebotRefineShot = enabled;
    }

    public static boolean ragebotSilentAim() {
        return ragebotSilentAim;
    }

    public static void setRagebotSilentAim(boolean enabled) {
        ragebotSilentAim = enabled;
    }

    public static boolean ragebotForceSilentAim() {
        return ragebotForceSilentAim;
    }

    public static void setRagebotForceSilentAim(boolean enabled) {
        ragebotForceSilentAim = enabled;
    }

    public static boolean ragebotAutomaticFire() {
        return ragebotAutomaticFire;
    }

    public static void setRagebotAutomaticFire(boolean enabled) {
        ragebotAutomaticFire = enabled;
    }

    public static float ragebotFov() {
        return ragebotFov;
    }

    public static float ragebotFovSlider() {
        return inverse(ragebotFov, RAGEBOT_FOV_MIN, RAGEBOT_FOV_MAX);
    }

    public static void setRagebotFovSlider(float value) {
        ragebotFov = range(value, RAGEBOT_FOV_MIN, RAGEBOT_FOV_MAX);
    }

    public static int ragebotHitboxesMask() {
        return ragebotHitboxesMask;
    }

    public static void setRagebotHitboxesMask(int mask) {
        ragebotHitboxesMask = mask & 31;
    }

    public static boolean ragebotAutoStop() {
        return ragebotAutoStop;
    }

    public static void setRagebotAutoStop(boolean enabled) {
        ragebotAutoStop = enabled;
    }

    public static boolean ragebotQuickScope() {
        return ragebotQuickScope;
    }

    public static void setRagebotQuickScope(boolean enabled) {
        ragebotQuickScope = enabled;
    }

    public static boolean ragebotRemoveRecoil() {
        return ragebotRemoveRecoil;
    }

    public static void setRagebotRemoveRecoil(boolean enabled) {
        ragebotRemoveRecoil = enabled;
    }

    public static boolean ragebotRemoveSpread() {
        return ragebotRemoveSpread;
    }

    public static void setRagebotRemoveSpread(boolean enabled) {
        ragebotRemoveSpread = enabled;
    }

    public static boolean ragebotDoubleTap() {
        return ragebotDoubleTap;
    }

    public static void setRagebotDoubleTap(boolean enabled) {
        ragebotDoubleTap = enabled;
    }

    public static boolean ragebotHideShots() {
        return ragebotHideShots;
    }

    public static void setRagebotHideShots(boolean enabled) {
        ragebotHideShots = enabled;
    }

    public static boolean ragebotAntiAimEnabled() {
        return ragebotAntiAimEnabled;
    }

    public static void setRagebotAntiAimEnabled(boolean enabled) {
        ragebotAntiAimEnabled = enabled;
    }

    public static String ragebotAntiAimPitch() {
        return ragebotAntiAimPitch;
    }

    public static void setRagebotAntiAimPitch(String value) {
        if (RAGEBOT_PITCH_UP.equals(value) || RAGEBOT_PITCH_DOWN.equals(value)
                || RAGEBOT_PITCH_ZERO.equals(value) || RAGEBOT_PITCH_JITTER.equals(value)
                || RAGEBOT_PITCH_CUSTOM.equals(value)) {
            ragebotAntiAimPitch = value;
        } else {
            ragebotAntiAimPitch = RAGEBOT_PITCH_OFF;
        }
    }

    public static float ragebotAntiAimCustomPitch() {
        return ragebotAntiAimCustomPitch;
    }

    public static float ragebotAntiAimCustomPitchSlider() {
        return inverse(ragebotAntiAimCustomPitch,
                RAGEBOT_ANTI_AIM_PITCH_MIN, RAGEBOT_ANTI_AIM_PITCH_MAX);
    }

    public static void setRagebotAntiAimCustomPitchSlider(float value) {
        ragebotAntiAimCustomPitch = range(value,
                RAGEBOT_ANTI_AIM_PITCH_MIN, RAGEBOT_ANTI_AIM_PITCH_MAX);
    }

    public static String ragebotAntiAimYaw() {
        if ("Backwards".equals(ragebotAntiAimYaw)) {
            ragebotAntiAimYaw = RAGEBOT_YAW_LOCAL_VIEW;
            ragebotAntiAimYawOffset = 0.0f;
        } else if ("At targets".equals(ragebotAntiAimYaw)) {
            ragebotAntiAimYaw = RAGEBOT_YAW_AT_TARGETS;
        } else if ("Local view".equals(ragebotAntiAimYaw)) {
            ragebotAntiAimYaw = RAGEBOT_YAW_LOCAL_VIEW;
        } else if (!RAGEBOT_YAW_OFF.equals(ragebotAntiAimYaw)
                && !RAGEBOT_YAW_AT_TARGETS.equals(ragebotAntiAimYaw)
                && !RAGEBOT_YAW_LOCAL_VIEW.equals(ragebotAntiAimYaw)) {
            ragebotAntiAimYaw = RAGEBOT_YAW_LOCAL_VIEW;
        }
        return ragebotAntiAimYaw;
    }

    public static void setRagebotAntiAimYaw(String value) {
        if ("At targets".equals(value)) {
            value = RAGEBOT_YAW_AT_TARGETS;
        } else if ("Local view".equals(value)) {
            value = RAGEBOT_YAW_LOCAL_VIEW;
        }
        if (RAGEBOT_YAW_AT_TARGETS.equals(value) || RAGEBOT_YAW_LOCAL_VIEW.equals(value)) {
            ragebotAntiAimYaw = value;
        } else {
            ragebotAntiAimYaw = RAGEBOT_YAW_OFF;
        }
    }

    public static float ragebotAntiAimYawBase() {
        return ragebotAntiAimYawBase;
    }

    public static float ragebotAntiAimYawBaseSlider() {
        return inverse(ragebotAntiAimYawBase,
                RAGEBOT_ANTI_AIM_YAW_OFFSET_MIN, RAGEBOT_ANTI_AIM_YAW_OFFSET_MAX);
    }

    public static void setRagebotAntiAimYawBaseSlider(float value) {
        ragebotAntiAimYawBase = range(value,
                RAGEBOT_ANTI_AIM_YAW_OFFSET_MIN, RAGEBOT_ANTI_AIM_YAW_OFFSET_MAX);
    }

    public static String ragebotAntiAimYawModifier() {
        return ragebotAntiAimYawModifier;
    }

    public static void setRagebotAntiAimYawModifier(String value) {
        if (RAGEBOT_YAW_MODIFIER_CENTER.equals(value)
                || RAGEBOT_YAW_MODIFIER_OFFSET.equals(value)
                || RAGEBOT_YAW_MODIFIER_RANDOM.equals(value)
                || RAGEBOT_YAW_MODIFIER_SPIN.equals(value)
                || RAGEBOT_YAW_MODIFIER_3_WAY.equals(value)
                || RAGEBOT_YAW_MODIFIER_5_WAY.equals(value)) {
            ragebotAntiAimYawModifier = value;
        } else {
            ragebotAntiAimYawModifier = RAGEBOT_YAW_MODIFIER_DISABLE;
        }
    }

    public static float ragebotAntiAimYawOffset() {
        return ragebotAntiAimYawOffset;
    }

    public static float ragebotAntiAimYawOffsetSlider() {
        return inverse(ragebotAntiAimYawOffset,
                RAGEBOT_ANTI_AIM_YAW_OFFSET_MIN, RAGEBOT_ANTI_AIM_YAW_OFFSET_MAX);
    }

    public static void setRagebotAntiAimYawOffsetSlider(float value) {
        ragebotAntiAimYawOffset = range(value,
                RAGEBOT_ANTI_AIM_YAW_OFFSET_MIN, RAGEBOT_ANTI_AIM_YAW_OFFSET_MAX);
    }

    public static float ragebotSmooth() {
        return ragebotSmooth;
    }

    public static float ragebotSmoothSlider() {
        return inverse(ragebotSmooth, GUN_AIM_SMOOTH_MIN, GUN_AIM_SMOOTH_MAX);
    }

    public static void setRagebotSmoothSlider(float value) {
        ragebotSmooth = range(value, GUN_AIM_SMOOTH_MIN, GUN_AIM_SMOOTH_MAX);
    }

    public static float ragebotPrediction() {
        return ragebotPrediction;
    }

    public static float ragebotPredictionSlider() {
        return inverse(ragebotPrediction, GUN_AIM_PREDICTION_MIN, GUN_AIM_PREDICTION_MAX);
    }

    public static void setRagebotPredictionSlider(float value) {
        ragebotPrediction = range(value, GUN_AIM_PREDICTION_MIN, GUN_AIM_PREDICTION_MAX);
    }

    public static String ragebotPriority() {
        return ragebotPriority;
    }

    public static void setRagebotPriority(String value) {
        if ("Low health".equals(value)) {
            value = AIM_PRIORITY_LOW_HEALTH;
        }
        if (AIM_PRIORITY_DISTANCE.equals(value) || AIM_PRIORITY_HITCHANCE.equals(value)
                || AIM_PRIORITY_LOW_HEALTH.equals(value)) {
            ragebotPriority = value;
        } else {
            ragebotPriority = AIM_PRIORITY_DISTANCE;
        }
    }

    public static String ragebotItemIds() {
        return ragebotItemIds;
    }

    public static void setRagebotItemIds(String ids) {
        ragebotItemIds = ConfigValueCodec.normalizeIdList(ids);
    }

    public static boolean ragebotItemIdEnabled(String id) {
        return ConfigValueCodec.idListContains(ragebotItemIds, id);
    }

    public static boolean autoPearlEnabled() {
        return autoPearlEnabled;
    }

    public static void setAutoPearlEnabled(boolean enabled) {
        autoPearlEnabled = enabled;
    }

    public static boolean pearlAimEnabled() {
        return pearlAimEnabled;
    }

    public static void setPearlAimEnabled(boolean enabled) {
        pearlAimEnabled = enabled;
    }

    public static float autoPearlDistance() {
        return autoPearlDistance;
    }

    public static float autoPearlDistanceSlider() {
        return inverse(autoPearlDistance, AUTO_PEARL_DISTANCE_MIN, AUTO_PEARL_DISTANCE_MAX);
    }

    public static void setAutoPearlDistanceSlider(float value) {
        autoPearlDistance = range(value, AUTO_PEARL_DISTANCE_MIN, AUTO_PEARL_DISTANCE_MAX);
    }

    public static float pearlAimRange() {
        return pearlAimRange;
    }

    public static float pearlAimRangeSlider() {
        return inverse(pearlAimRange, PEARL_AIM_RANGE_MIN, PEARL_AIM_RANGE_MAX);
    }

    public static void setPearlAimRangeSlider(float value) {
        pearlAimRange = range(value, PEARL_AIM_RANGE_MIN, PEARL_AIM_RANGE_MAX);
    }

    public static float pearlAimSmooth() {
        return pearlAimSmooth;
    }

    public static float pearlAimSmoothSlider() {
        return inverse(pearlAimSmooth, PEARL_AIM_SMOOTH_MIN, PEARL_AIM_SMOOTH_MAX);
    }

    public static void setPearlAimSmoothSlider(float value) {
        pearlAimSmooth = range(value, PEARL_AIM_SMOOTH_MIN, PEARL_AIM_SMOOTH_MAX);
    }

    public static float pearlAimPrediction() {
        return pearlAimPrediction;
    }

    public static float pearlAimPredictionSlider() {
        return inverse(pearlAimPrediction, PEARL_AIM_PREDICTION_MIN, PEARL_AIM_PREDICTION_MAX);
    }

    public static void setPearlAimPredictionSlider(float value) {
        pearlAimPrediction = range(value, PEARL_AIM_PREDICTION_MIN, PEARL_AIM_PREDICTION_MAX);
    }

    public static int autoPearlDelayTicks() {
        return Math.round(autoPearlDelay);
    }

    public static float autoPearlDelaySlider() {
        return inverse(autoPearlDelay, AUTO_PEARL_DELAY_MIN, AUTO_PEARL_DELAY_MAX);
    }

    public static void setAutoPearlDelaySlider(float value) {
        autoPearlDelay = range(value, AUTO_PEARL_DELAY_MIN, AUTO_PEARL_DELAY_MAX);
    }

    public static String pearlAimPriority() {
        return pearlAimPriority;
    }

    public static void setPearlAimPriority(String value) {
        if ("Low health".equals(value)) {
            value = AIM_PRIORITY_LOW_HEALTH;
        }
        if (AIM_PRIORITY_DISTANCE.equals(value) || AIM_PRIORITY_LOW_HEALTH.equals(value)) {
            pearlAimPriority = value;
        } else {
            pearlAimPriority = AIM_PRIORITY_CROSSHAIR;
        }
    }

    public static boolean autoPearlSilentRotate() {
        return autoPearlSilentRotate;
    }

    public static void setAutoPearlSilentRotate(boolean enabled) {
        autoPearlSilentRotate = enabled;
    }

    public static boolean autoPearlRestoreSlot() {
        return autoPearlRestoreSlot;
    }

    public static void setAutoPearlRestoreSlot(boolean enabled) {
        autoPearlRestoreSlot = enabled;
    }

    public static boolean bowSpamEnabled() {
        return bowSpamEnabled;
    }

    public static void setBowSpamEnabled(boolean enabled) {
        bowSpamEnabled = enabled;
    }

    public static int bowSpamChargeTicks() {
        return Math.round(bowSpamCharge);
    }

    public static float bowSpamChargeSlider() {
        return inverse(bowSpamCharge, BOW_SPAM_CHARGE_MIN, BOW_SPAM_CHARGE_MAX);
    }

    public static void setBowSpamChargeSlider(float value) {
        bowSpamCharge = range(value, BOW_SPAM_CHARGE_MIN, BOW_SPAM_CHARGE_MAX);
    }

    public static int bowSpamDelayTicks() {
        return Math.round(bowSpamDelay);
    }

    public static float bowSpamDelaySlider() {
        return inverse(bowSpamDelay, BOW_SPAM_DELAY_MIN, BOW_SPAM_DELAY_MAX);
    }

    public static void setBowSpamDelaySlider(float value) {
        bowSpamDelay = range(value, BOW_SPAM_DELAY_MIN, BOW_SPAM_DELAY_MAX);
    }

    public static boolean bowSpamAutoUse() {
        return bowSpamAutoUse;
    }

    public static void setBowSpamAutoUse(boolean enabled) {
        bowSpamAutoUse = enabled;
    }

    public static boolean fastUseEnabled() {
        return fastUseEnabled || bowSpamEnabled;
    }

    public static void setFastUseEnabled(boolean enabled) {
        fastUseEnabled = enabled;
        bowSpamEnabled = false;
    }

    public static int fastUseChargeTicks() {
        return Math.round(fastUseCharge);
    }

    public static float fastUseChargeSlider() {
        return inverse(fastUseCharge, FAST_USE_CHARGE_MIN, FAST_USE_CHARGE_MAX);
    }

    public static void setFastUseChargeSlider(float value) {
        fastUseCharge = range(value, FAST_USE_CHARGE_MIN, FAST_USE_CHARGE_MAX);
    }

    public static int fastUseDelayTicks() {
        return Math.round(fastUseDelay);
    }

    public static float fastUseDelaySlider() {
        return inverse(fastUseDelay, FAST_USE_DELAY_MIN, FAST_USE_DELAY_MAX);
    }

    public static void setFastUseDelaySlider(float value) {
        fastUseDelay = range(value, FAST_USE_DELAY_MIN, FAST_USE_DELAY_MAX);
    }

    public static int fastUsePackets() {
        return Math.round(fastUsePackets);
    }

    public static float fastUsePacketsSlider() {
        return inverse(fastUsePackets, FAST_USE_PACKETS_MIN, FAST_USE_PACKETS_MAX);
    }

    public static void setFastUsePacketsSlider(float value) {
        fastUsePackets = range(value, FAST_USE_PACKETS_MIN, FAST_USE_PACKETS_MAX);
    }

    public static boolean fastUseBow() {
        return fastUseBow;
    }

    public static void setFastUseBow(boolean enabled) {
        fastUseBow = enabled;
    }

    public static boolean fastUseTrident() {
        return fastUseTrident;
    }

    public static void setFastUseTrident(boolean enabled) {
        fastUseTrident = enabled;
    }

    public static boolean fastUseCrossbow() {
        return fastUseCrossbow;
    }

    public static void setFastUseCrossbow(boolean enabled) {
        fastUseCrossbow = enabled;
    }

    public static boolean fastUseThrowables() {
        return fastUseThrowables;
    }

    public static void setFastUseThrowables(boolean enabled) {
        fastUseThrowables = enabled;
    }

    public static boolean fastUseAutoUse() {
        return fastUseAutoUse;
    }

    public static void setFastUseAutoUse(boolean enabled) {
        fastUseAutoUse = enabled;
    }

    public static boolean fastUsePacketBoost() {
        return fastUsePacketBoost;
    }

    public static void setFastUsePacketBoost(boolean enabled) {
        fastUsePacketBoost = enabled;
    }

    public static boolean autoClickerEnabled() {
        return autoClickerEnabled;
    }

    public static void setAutoClickerEnabled(boolean enabled) {
        autoClickerEnabled = enabled;
    }

    public static float autoClickerCps() {
        return autoClickerCps;
    }

    public static float autoClickerCpsSlider() {
        return inverse(autoClickerCps, AUTO_CLICKER_CPS_MIN, AUTO_CLICKER_CPS_MAX);
    }

    public static void setAutoClickerCpsSlider(float value) {
        autoClickerCps = range(value, AUTO_CLICKER_CPS_MIN, AUTO_CLICKER_CPS_MAX);
    }

    public static boolean autoHealEnabled() {
        return autoHealEnabled;
    }

    public static void setAutoHealEnabled(boolean enabled) {
        autoHealEnabled = enabled;
    }

    public static String autoHealMode() {
        return autoHealMode;
    }

    public static void setAutoHealMode(String value) {
        if (AUTO_HEAL_MODE_GOLDEN.equals(value) || AUTO_HEAL_MODE_ENCHANTED.equals(value)) {
            autoHealMode = value;
        } else {
            autoHealMode = AUTO_HEAL_MODE_ANY;
        }
    }

    public static float autoHealHealth() {
        return autoHealHealth;
    }

    public static float autoHealHealthSlider() {
        return inverse(autoHealHealth, AUTO_HEAL_HEALTH_MIN, AUTO_HEAL_HEALTH_MAX);
    }

    public static void setAutoHealHealthSlider(float value) {
        autoHealHealth = range(value, AUTO_HEAL_HEALTH_MIN, AUTO_HEAL_HEALTH_MAX);
    }

    public static int autoHealDelayTicks() {
        return Math.round(autoHealDelay);
    }

    public static float autoHealDelaySlider() {
        return inverse(autoHealDelay, AUTO_HEAL_DELAY_MIN, AUTO_HEAL_DELAY_MAX);
    }

    public static void setAutoHealDelaySlider(float value) {
        autoHealDelay = range(value, AUTO_HEAL_DELAY_MIN, AUTO_HEAL_DELAY_MAX);
    }

    public static boolean autoHealIncludeAbsorption() {
        return autoHealIncludeAbsorption;
    }

    public static void setAutoHealIncludeAbsorption(boolean enabled) {
        autoHealIncludeAbsorption = enabled;
    }

    public static boolean autoHealRestoreSlot() {
        return autoHealRestoreSlot;
    }

    public static void setAutoHealRestoreSlot(boolean enabled) {
        autoHealRestoreSlot = enabled;
    }

    public static boolean autoTotemEnabled() {
        return autoTotemEnabled;
    }

    public static void setAutoTotemEnabled(boolean enabled) {
        autoTotemEnabled = enabled;
    }

    public static float autoTotemHealth() {
        return autoTotemHealth;
    }

    public static float autoTotemHealthSlider() {
        return inverse(autoTotemHealth, AUTO_TOTEM_HEALTH_MIN, AUTO_TOTEM_HEALTH_MAX);
    }

    public static void setAutoTotemHealthSlider(float value) {
        autoTotemHealth = range(value, AUTO_TOTEM_HEALTH_MIN, AUTO_TOTEM_HEALTH_MAX);
    }

    public static boolean autoExpEnabled() {
        return autoExpEnabled;
    }

    public static void setAutoExpEnabled(boolean enabled) {
        autoExpEnabled = enabled;
    }

    public static String autoExpMode() {
        return autoExpMode;
    }

    public static void setAutoExpMode(String value) {
        if (AUTO_EXP_MODE_ARMOR.equals(value) || AUTO_EXP_MODE_HELD.equals(value)) {
            autoExpMode = value;
        } else {
            autoExpMode = AUTO_EXP_MODE_ALL;
        }
    }

    public static float autoExpDurability() {
        return autoExpDurability;
    }

    public static float autoExpDurabilitySlider() {
        return inverse(autoExpDurability, AUTO_EXP_DURABILITY_MIN, AUTO_EXP_DURABILITY_MAX);
    }

    public static void setAutoExpDurabilitySlider(float value) {
        autoExpDurability = range(value, AUTO_EXP_DURABILITY_MIN, AUTO_EXP_DURABILITY_MAX);
    }

    public static int autoExpDelayTicks() {
        return Math.round(autoExpDelay);
    }

    public static float autoExpDelaySlider() {
        return inverse(autoExpDelay, AUTO_EXP_DELAY_MIN, AUTO_EXP_DELAY_MAX);
    }

    public static void setAutoExpDelaySlider(float value) {
        autoExpDelay = range(value, AUTO_EXP_DELAY_MIN, AUTO_EXP_DELAY_MAX);
    }

    public static boolean autoExpMendingOnly() {
        return autoExpMendingOnly;
    }

    public static void setAutoExpMendingOnly(boolean enabled) {
        autoExpMendingOnly = enabled;
    }

    public static boolean autoExpSilentRotate() {
        return autoExpSilentRotate;
    }

    public static void setAutoExpSilentRotate(boolean enabled) {
        autoExpSilentRotate = enabled;
    }

    public static boolean autoExpRestoreSlot() {
        return autoExpRestoreSlot;
    }

    public static void setAutoExpRestoreSlot(boolean enabled) {
        autoExpRestoreSlot = enabled;
    }

    public static boolean autoArmorEnabled() {
        return autoArmorEnabled;
    }

    public static void setAutoArmorEnabled(boolean enabled) {
        autoArmorEnabled = enabled;
    }

    public static int autoArmorDelayTicks() {
        return Math.round(autoArmorDelay);
    }

    public static float autoArmorDelaySlider() {
        return inverse(autoArmorDelay, AUTO_ARMOR_DELAY_MIN, AUTO_ARMOR_DELAY_MAX);
    }

    public static void setAutoArmorDelaySlider(float value) {
        autoArmorDelay = range(value, AUTO_ARMOR_DELAY_MIN, AUTO_ARMOR_DELAY_MAX);
    }

    public static boolean autoArmorPreferDurability() {
        return autoArmorPreferDurability;
    }

    public static void setAutoArmorPreferDurability(boolean enabled) {
        autoArmorPreferDurability = enabled;
    }

    public static boolean autoArmorAllowBindingCurse() {
        return autoArmorAllowBindingCurse;
    }

    public static void setAutoArmorAllowBindingCurse(boolean enabled) {
        autoArmorAllowBindingCurse = enabled;
    }

    public static boolean bedAuraEnabled() {
        return bedAuraEnabled;
    }

    public static void setBedAuraEnabled(boolean enabled) {
        bedAuraEnabled = enabled;
    }

    public static boolean bedAuraPlace() {
        return bedAuraPlace;
    }

    public static void setBedAuraPlace(boolean enabled) {
        bedAuraPlace = enabled;
    }

    public static boolean bedAuraBreak() {
        return bedAuraBreak;
    }

    public static void setBedAuraBreak(boolean enabled) {
        bedAuraBreak = enabled;
    }

    public static float bedAuraRange() {
        return bedAuraRange;
    }

    public static float bedAuraRangeSlider() {
        return inverse(bedAuraRange, BED_AURA_RANGE_MIN, BED_AURA_RANGE_MAX);
    }

    public static void setBedAuraRangeSlider(float value) {
        bedAuraRange = range(value, BED_AURA_RANGE_MIN, BED_AURA_RANGE_MAX);
    }

    public static float bedAuraMinDamage() {
        return bedAuraMinDamage;
    }

    public static float bedAuraMinDamageSlider() {
        return inverse(bedAuraMinDamage, BED_AURA_DAMAGE_MIN, BED_AURA_DAMAGE_MAX);
    }

    public static void setBedAuraMinDamageSlider(float value) {
        bedAuraMinDamage = range(value, BED_AURA_DAMAGE_MIN, BED_AURA_DAMAGE_MAX);
    }

    public static float bedAuraMaxSelfDamage() {
        return bedAuraMaxSelfDamage;
    }

    public static float bedAuraMaxSelfDamageSlider() {
        return inverse(bedAuraMaxSelfDamage, BED_AURA_SELF_DAMAGE_MIN, BED_AURA_SELF_DAMAGE_MAX);
    }

    public static void setBedAuraMaxSelfDamageSlider(float value) {
        bedAuraMaxSelfDamage = range(value, BED_AURA_SELF_DAMAGE_MIN, BED_AURA_SELF_DAMAGE_MAX);
    }

    public static int bedAuraPlaceDelayTicks() {
        return Math.round(bedAuraPlaceDelay);
    }

    public static float bedAuraPlaceDelaySlider() {
        return inverse(bedAuraPlaceDelay, BED_AURA_DELAY_MIN, BED_AURA_DELAY_MAX);
    }

    public static void setBedAuraPlaceDelaySlider(float value) {
        bedAuraPlaceDelay = range(value, BED_AURA_DELAY_MIN, BED_AURA_DELAY_MAX);
    }

    public static int bedAuraBreakDelayTicks() {
        return Math.round(bedAuraBreakDelay);
    }

    public static float bedAuraBreakDelaySlider() {
        return inverse(bedAuraBreakDelay, BED_AURA_DELAY_MIN, BED_AURA_DELAY_MAX);
    }

    public static void setBedAuraBreakDelaySlider(float value) {
        bedAuraBreakDelay = range(value, BED_AURA_DELAY_MIN, BED_AURA_DELAY_MAX);
    }

    public static boolean bedAuraRayTrace() {
        return bedAuraRayTrace;
    }

    public static void setBedAuraRayTrace(boolean enabled) {
        bedAuraRayTrace = enabled;
    }

    public static boolean bedAuraRotate() {
        return bedAuraRotate;
    }

    public static void setBedAuraRotate(boolean enabled) {
        bedAuraRotate = enabled;
    }

    public static boolean bedAuraAntiSuicide() {
        return bedAuraAntiSuicide;
    }

    public static void setBedAuraAntiSuicide(boolean enabled) {
        bedAuraAntiSuicide = enabled;
    }

    public static boolean anchorAuraEnabled() {
        return anchorAuraEnabled;
    }

    public static void setAnchorAuraEnabled(boolean enabled) {
        anchorAuraEnabled = enabled;
    }

    public static boolean anchorAuraPlace() {
        return anchorAuraPlace;
    }

    public static void setAnchorAuraPlace(boolean enabled) {
        anchorAuraPlace = enabled;
    }

    public static boolean anchorAuraBreak() {
        return anchorAuraBreak;
    }

    public static void setAnchorAuraBreak(boolean enabled) {
        anchorAuraBreak = enabled;
    }

    public static float anchorAuraRange() {
        return anchorAuraRange;
    }

    public static float anchorAuraRangeSlider() {
        return inverse(anchorAuraRange, ANCHOR_AURA_RANGE_MIN, ANCHOR_AURA_RANGE_MAX);
    }

    public static void setAnchorAuraRangeSlider(float value) {
        anchorAuraRange = range(value, ANCHOR_AURA_RANGE_MIN, ANCHOR_AURA_RANGE_MAX);
    }

    public static float anchorAuraMinDamage() {
        return anchorAuraMinDamage;
    }

    public static float anchorAuraMinDamageSlider() {
        return inverse(anchorAuraMinDamage, ANCHOR_AURA_DAMAGE_MIN, ANCHOR_AURA_DAMAGE_MAX);
    }

    public static void setAnchorAuraMinDamageSlider(float value) {
        anchorAuraMinDamage = range(value, ANCHOR_AURA_DAMAGE_MIN, ANCHOR_AURA_DAMAGE_MAX);
    }

    public static float anchorAuraMaxSelfDamage() {
        return anchorAuraMaxSelfDamage;
    }

    public static float anchorAuraMaxSelfDamageSlider() {
        return inverse(anchorAuraMaxSelfDamage, ANCHOR_AURA_SELF_DAMAGE_MIN, ANCHOR_AURA_SELF_DAMAGE_MAX);
    }

    public static void setAnchorAuraMaxSelfDamageSlider(float value) {
        anchorAuraMaxSelfDamage = range(value, ANCHOR_AURA_SELF_DAMAGE_MIN, ANCHOR_AURA_SELF_DAMAGE_MAX);
    }

    public static int anchorAuraPlaceDelayTicks() {
        return Math.round(anchorAuraPlaceDelay);
    }

    public static float anchorAuraPlaceDelaySlider() {
        return inverse(anchorAuraPlaceDelay, ANCHOR_AURA_DELAY_MIN, ANCHOR_AURA_DELAY_MAX);
    }

    public static void setAnchorAuraPlaceDelaySlider(float value) {
        anchorAuraPlaceDelay = range(value, ANCHOR_AURA_DELAY_MIN, ANCHOR_AURA_DELAY_MAX);
    }

    public static int anchorAuraBreakDelayTicks() {
        return Math.round(anchorAuraBreakDelay);
    }

    public static float anchorAuraBreakDelaySlider() {
        return inverse(anchorAuraBreakDelay, ANCHOR_AURA_DELAY_MIN, ANCHOR_AURA_DELAY_MAX);
    }

    public static void setAnchorAuraBreakDelaySlider(float value) {
        anchorAuraBreakDelay = range(value, ANCHOR_AURA_DELAY_MIN, ANCHOR_AURA_DELAY_MAX);
    }

    public static boolean anchorAuraRayTrace() {
        return anchorAuraRayTrace;
    }

    public static void setAnchorAuraRayTrace(boolean enabled) {
        anchorAuraRayTrace = enabled;
    }

    public static boolean anchorAuraRotate() {
        return anchorAuraRotate;
    }

    public static void setAnchorAuraRotate(boolean enabled) {
        anchorAuraRotate = enabled;
    }

    public static boolean anchorAuraAntiSuicide() {
        return anchorAuraAntiSuicide;
    }

    public static void setAnchorAuraAntiSuicide(boolean enabled) {
        anchorAuraAntiSuicide = enabled;
    }

    public static boolean webAuraEnabled() {
        return webAuraEnabled;
    }

    public static void setWebAuraEnabled(boolean enabled) {
        webAuraEnabled = enabled;
    }

    public static String webAuraMode() {
        return webAuraMode;
    }

    public static void setWebAuraMode(String value) {
        if (WEB_AURA_MODE_FEET.equals(value) || WEB_AURA_MODE_HEAD.equals(value)) {
            webAuraMode = value;
        } else {
            webAuraMode = WEB_AURA_MODE_BOTH;
        }
    }

    public static float webAuraRange() {
        return webAuraRange;
    }

    public static float webAuraRangeSlider() {
        return inverse(webAuraRange, WEB_AURA_RANGE_MIN, WEB_AURA_RANGE_MAX);
    }

    public static void setWebAuraRangeSlider(float value) {
        webAuraRange = range(value, WEB_AURA_RANGE_MIN, WEB_AURA_RANGE_MAX);
    }

    public static int webAuraDelayTicks() {
        return Math.round(webAuraDelay);
    }

    public static float webAuraDelaySlider() {
        return inverse(webAuraDelay, WEB_AURA_DELAY_MIN, WEB_AURA_DELAY_MAX);
    }

    public static void setWebAuraDelaySlider(float value) {
        webAuraDelay = range(value, WEB_AURA_DELAY_MIN, WEB_AURA_DELAY_MAX);
    }

    public static boolean webAuraRayTrace() {
        return webAuraRayTrace;
    }

    public static void setWebAuraRayTrace(boolean enabled) {
        webAuraRayTrace = enabled;
    }

    public static boolean webAuraRotate() {
        return webAuraRotate;
    }

    public static void setWebAuraRotate(boolean enabled) {
        webAuraRotate = enabled;
    }

    public static boolean webAuraRestoreSlot() {
        return webAuraRestoreSlot;
    }

    public static void setWebAuraRestoreSlot(boolean enabled) {
        webAuraRestoreSlot = enabled;
    }

    public static boolean autoTrapEnabled() {
        return autoTrapEnabled;
    }

    public static void setAutoTrapEnabled(boolean enabled) {
        autoTrapEnabled = enabled;
    }

    public static String autoTrapMode() {
        return autoTrapMode;
    }

    public static void setAutoTrapMode(String value) {
        if (AUTO_TRAP_MODE_FEET.equals(value) || AUTO_TRAP_MODE_HEAD.equals(value)) {
            autoTrapMode = value;
        } else {
            autoTrapMode = AUTO_TRAP_MODE_FULL;
        }
    }

    public static float autoTrapRange() {
        return autoTrapRange;
    }

    public static float autoTrapRangeSlider() {
        return inverse(autoTrapRange, AUTO_TRAP_RANGE_MIN, AUTO_TRAP_RANGE_MAX);
    }

    public static void setAutoTrapRangeSlider(float value) {
        autoTrapRange = range(value, AUTO_TRAP_RANGE_MIN, AUTO_TRAP_RANGE_MAX);
    }

    public static int autoTrapDelayTicks() {
        return Math.round(autoTrapDelay);
    }

    public static float autoTrapDelaySlider() {
        return inverse(autoTrapDelay, AUTO_TRAP_DELAY_MIN, AUTO_TRAP_DELAY_MAX);
    }

    public static void setAutoTrapDelaySlider(float value) {
        autoTrapDelay = range(value, AUTO_TRAP_DELAY_MIN, AUTO_TRAP_DELAY_MAX);
    }

    public static boolean autoTrapRayTrace() {
        return autoTrapRayTrace;
    }

    public static void setAutoTrapRayTrace(boolean enabled) {
        autoTrapRayTrace = enabled;
    }

    public static boolean autoTrapRotate() {
        return autoTrapRotate;
    }

    public static void setAutoTrapRotate(boolean enabled) {
        autoTrapRotate = enabled;
    }

    public static boolean autoTrapRestoreSlot() {
        return autoTrapRestoreSlot;
    }

    public static void setAutoTrapRestoreSlot(boolean enabled) {
        autoTrapRestoreSlot = enabled;
    }

    public static boolean surroundEnabled() {
        return surroundEnabled;
    }

    public static void setSurroundEnabled(boolean enabled) {
        surroundEnabled = enabled;
    }

    public static String surroundMode() {
        return surroundMode;
    }

    public static void setSurroundMode(String value) {
        surroundMode = SURROUND_MODE_FULL.equals(value) ? SURROUND_MODE_FULL : SURROUND_MODE_NORMAL;
    }

    public static int surroundDelayTicks() {
        return Math.round(surroundDelay);
    }

    public static float surroundDelaySlider() {
        return inverse(surroundDelay, SURROUND_DELAY_MIN, SURROUND_DELAY_MAX);
    }

    public static void setSurroundDelaySlider(float value) {
        surroundDelay = range(value, SURROUND_DELAY_MIN, SURROUND_DELAY_MAX);
    }

    public static boolean surroundFloor() {
        return surroundFloor;
    }

    public static void setSurroundFloor(boolean enabled) {
        surroundFloor = enabled;
    }

    public static boolean surroundRayTrace() {
        return surroundRayTrace;
    }

    public static void setSurroundRayTrace(boolean enabled) {
        surroundRayTrace = enabled;
    }

    public static boolean surroundRotate() {
        return surroundRotate;
    }

    public static void setSurroundRotate(boolean enabled) {
        surroundRotate = enabled;
    }

    public static boolean surroundRestoreSlot() {
        return surroundRestoreSlot;
    }

    public static void setSurroundRestoreSlot(boolean enabled) {
        surroundRestoreSlot = enabled;
    }

    public static boolean holeFillEnabled() {
        return holeFillEnabled;
    }

    public static void setHoleFillEnabled(boolean enabled) {
        holeFillEnabled = enabled;
    }

    public static String holeFillMode() {
        return holeFillMode;
    }

    public static void setHoleFillMode(String value) {
        if (HOLE_FILL_MODE_TARGET.equals(value) || HOLE_FILL_MODE_SELF.equals(value)) {
            holeFillMode = value;
        } else {
            holeFillMode = HOLE_FILL_MODE_BOTH;
        }
    }

    public static float holeFillRange() {
        return holeFillRange;
    }

    public static float holeFillRangeSlider() {
        return inverse(holeFillRange, HOLE_FILL_RANGE_MIN, HOLE_FILL_RANGE_MAX);
    }

    public static void setHoleFillRangeSlider(float value) {
        holeFillRange = range(value, HOLE_FILL_RANGE_MIN, HOLE_FILL_RANGE_MAX);
    }

    public static int holeFillDelayTicks() {
        return Math.round(holeFillDelay);
    }

    public static float holeFillDelaySlider() {
        return inverse(holeFillDelay, HOLE_FILL_DELAY_MIN, HOLE_FILL_DELAY_MAX);
    }

    public static void setHoleFillDelaySlider(float value) {
        holeFillDelay = range(value, HOLE_FILL_DELAY_MIN, HOLE_FILL_DELAY_MAX);
    }

    public static boolean holeFillRayTrace() {
        return holeFillRayTrace;
    }

    public static void setHoleFillRayTrace(boolean enabled) {
        holeFillRayTrace = enabled;
    }

    public static boolean holeFillRotate() {
        return holeFillRotate;
    }

    public static void setHoleFillRotate(boolean enabled) {
        holeFillRotate = enabled;
    }

    public static boolean holeFillRestoreSlot() {
        return holeFillRestoreSlot;
    }

    public static void setHoleFillRestoreSlot(boolean enabled) {
        holeFillRestoreSlot = enabled;
    }

    public static boolean autoCrystalEnabled() {
        return autoCrystalEnabled;
    }

    public static void setAutoCrystalEnabled(boolean enabled) {
        autoCrystalEnabled = enabled;
    }

    public static boolean autoCrystalPlace() {
        return autoCrystalPlace;
    }

    public static void setAutoCrystalPlace(boolean enabled) {
        autoCrystalPlace = enabled;
    }

    public static boolean autoCrystalBreak() {
        return autoCrystalBreak;
    }

    public static void setAutoCrystalBreak(boolean enabled) {
        autoCrystalBreak = enabled;
    }

    public static float autoCrystalRange() {
        return autoCrystalRange;
    }

    public static float autoCrystalRangeSlider() {
        return inverse(autoCrystalRange, AUTO_CRYSTAL_RANGE_MIN, AUTO_CRYSTAL_RANGE_MAX);
    }

    public static void setAutoCrystalRangeSlider(float value) {
        autoCrystalRange = range(value, AUTO_CRYSTAL_RANGE_MIN, AUTO_CRYSTAL_RANGE_MAX);
    }

    public static float autoCrystalMinDamage() {
        return autoCrystalMinDamage;
    }

    public static float autoCrystalMinDamageSlider() {
        return inverse(autoCrystalMinDamage, AUTO_CRYSTAL_DAMAGE_MIN, AUTO_CRYSTAL_DAMAGE_MAX);
    }

    public static void setAutoCrystalMinDamageSlider(float value) {
        autoCrystalMinDamage = range(value, AUTO_CRYSTAL_DAMAGE_MIN, AUTO_CRYSTAL_DAMAGE_MAX);
    }

    public static float autoCrystalMaxSelfDamage() {
        return autoCrystalMaxSelfDamage;
    }

    public static float autoCrystalMaxSelfDamageSlider() {
        return inverse(autoCrystalMaxSelfDamage, AUTO_CRYSTAL_SELF_DAMAGE_MIN, AUTO_CRYSTAL_SELF_DAMAGE_MAX);
    }

    public static void setAutoCrystalMaxSelfDamageSlider(float value) {
        autoCrystalMaxSelfDamage = range(value, AUTO_CRYSTAL_SELF_DAMAGE_MIN, AUTO_CRYSTAL_SELF_DAMAGE_MAX);
    }

    public static float autoCrystalDelay() {
        return autoCrystalDelay;
    }

    public static float autoCrystalDelaySlider() {
        return inverse(autoCrystalDelay, AUTO_CRYSTAL_DELAY_MIN, AUTO_CRYSTAL_DELAY_MAX);
    }

    public static void setAutoCrystalDelaySlider(float value) {
        autoCrystalDelay = range(value, AUTO_CRYSTAL_DELAY_MIN, AUTO_CRYSTAL_DELAY_MAX);
    }

    public static float autoCrystalPlaceDelay() {
        return autoCrystalPlaceDelay;
    }

    public static float autoCrystalPlaceDelaySlider() {
        return inverse(autoCrystalPlaceDelay, AUTO_CRYSTAL_DELAY_MIN, AUTO_CRYSTAL_DELAY_MAX);
    }

    public static void setAutoCrystalPlaceDelaySlider(float value) {
        autoCrystalPlaceDelay = range(value, AUTO_CRYSTAL_DELAY_MIN, AUTO_CRYSTAL_DELAY_MAX);
        autoCrystalDelay = autoCrystalPlaceDelay;
    }

    public static float autoCrystalBreakDelay() {
        return autoCrystalBreakDelay;
    }

    public static float autoCrystalBreakDelaySlider() {
        return inverse(autoCrystalBreakDelay, AUTO_CRYSTAL_DELAY_MIN, AUTO_CRYSTAL_DELAY_MAX);
    }

    public static void setAutoCrystalBreakDelaySlider(float value) {
        autoCrystalBreakDelay = range(value, AUTO_CRYSTAL_DELAY_MIN, AUTO_CRYSTAL_DELAY_MAX);
    }

    public static boolean autoCrystalRayTrace() {
        return autoCrystalRayTrace;
    }

    public static void setAutoCrystalRayTrace(boolean enabled) {
        autoCrystalRayTrace = enabled;
    }

    public static boolean autoCrystalRotate() {
        return autoCrystalRotate;
    }

    public static void setAutoCrystalRotate(boolean enabled) {
        autoCrystalRotate = enabled;
    }

    public static boolean autoCrystalFacePlace() {
        return autoCrystalFacePlace;
    }

    public static void setAutoCrystalFacePlace(boolean enabled) {
        autoCrystalFacePlace = enabled;
    }

    public static float autoCrystalFacePlaceHealth() {
        return autoCrystalFacePlaceHealth;
    }

    public static float autoCrystalFacePlaceHealthSlider() {
        return inverse(autoCrystalFacePlaceHealth, AUTO_CRYSTAL_FACE_HEALTH_MIN, AUTO_CRYSTAL_FACE_HEALTH_MAX);
    }

    public static void setAutoCrystalFacePlaceHealthSlider(float value) {
        autoCrystalFacePlaceHealth = range(value, AUTO_CRYSTAL_FACE_HEALTH_MIN, AUTO_CRYSTAL_FACE_HEALTH_MAX);
    }

    public static boolean autoCrystalAntiSuicide() {
        return autoCrystalAntiSuicide;
    }

    public static void setAutoCrystalAntiSuicide(boolean enabled) {
        autoCrystalAntiSuicide = enabled;
    }

    public static String autoCrystalPriority() {
        return autoCrystalPriority;
    }

    public static void setAutoCrystalPriority(String value) {
        if (AUTO_CRYSTAL_PRIORITY_DISTANCE.equals(value) || AUTO_CRYSTAL_PRIORITY_HEALTH.equals(value)) {
            autoCrystalPriority = value;
        } else {
            autoCrystalPriority = AUTO_CRYSTAL_PRIORITY_DAMAGE;
        }
    }

    private static float range(float slider, float min, float max) {
        float clamped = Math.max(0.0f, Math.min(1.0f, slider));
        return min + (max - min) * clamped;
    }

    private static float inverse(float value, float min, float max) {
        return Math.max(0.0f, Math.min(1.0f, (value - min) / (max - min)));
    }
}
