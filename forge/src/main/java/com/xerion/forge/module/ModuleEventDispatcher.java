package com.xerion.forge.module;

import com.xerion.common.XerionCore;
import com.xerion.common.event.events.TickEvent;
import com.xerion.forge.combat.*;
import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.event.RuntimeStateBridge;
import com.xerion.forge.misc.*;
import com.xerion.forge.movement.*;
import com.xerion.forge.player.*;
import com.xerion.forge.render.*;
import com.xerion.forge.service.RuntimeResourceLifecycle;
import com.xerion.forge.util.DisablerService;
import com.xerion.forge.util.PingSpoofService;
import com.xerion.forge.visual.WorldEffectRenderer;
import com.xerion.forge.world.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public final class ModuleEventDispatcher {
    private final XerionCore core;
    private final WorldEffectRenderer worldEffectRenderer = new WorldEffectRenderer();
    private final ScaffoldService ScaffoldService = new ScaffoldService();
    private final KillAuraService KillAuraService = new KillAuraService();
    private final PlayerRenderPipeline playerRenderPipeline = new PlayerRenderPipeline(KillAuraService);
    private final TargetStrafeService TargetStrafeService = new TargetStrafeService(KillAuraService);
    private final CombatAssistService CombatAssistService = new CombatAssistService();
    private final AutoPearlService AutoPearlService = new AutoPearlService();
    private final FastUseService FastUseService = new FastUseService();
    private final AutoExpService AutoExpService = new AutoExpService();
    private final AutoHealService AutoHealService = new AutoHealService();
    private final AutoEatService AutoEatService = new AutoEatService();
    private final AutoFishService AutoFishService = new AutoFishService();
    private final ChestStealService ChestStealService = new ChestStealService();
    private final InventoryCleanerService InventoryCleanerService = new InventoryCleanerService();
    private final AutoRefillService AutoRefillService = new AutoRefillService();
    private final AutoSortService AutoSortService = new AutoSortService();
    private final AutoToolService AutoToolService = new AutoToolService();
    private final AutoMineService AutoMineService = new AutoMineService();
    private final NukerService NukerService = new NukerService();
    private final VeinMinerService VeinMinerService = new VeinMinerService();
    private final WorldRenderPipeline worldRenderPipeline =
            new WorldRenderPipeline(VeinMinerService, worldEffectRenderer);
    private final AutoTotemService AutoTotemService = new AutoTotemService();
    private final AutoArmorService AutoArmorService = new AutoArmorService();
    private final BedAuraService BedAuraService = new BedAuraService();
    private final AnchorAuraService AnchorAuraService = new AnchorAuraService();
    private final FreecamService FreecamService = new FreecamService();
    private final WebAuraService WebAuraService = new WebAuraService();
    private final AutoTrapService AutoTrapService = new AutoTrapService();
    private final SurroundService SurroundService = new SurroundService();
    private final HoleFillService HoleFillService = new HoleFillService();
    private final AutoCrystalService AutoCrystalService = new AutoCrystalService();
    private final MovementService movementService = new MovementService();

    public ModuleEventDispatcher(XerionCore core) {
        this.core = core;
    }

    public void clearWorldRuntime(Minecraft minecraft) {
        RuntimeResourceLifecycle.clearWorldRuntimeResources();
        worldEffectRenderer.clear();
        movementService.clearWorldRuntime(minecraft);
    }

    public void renderTick(float renderTickTime) {
        KillAuraService.renderTick(renderTickTime);
        CombatAssistService.renderTick(renderTickTime);
        AutoPearlService.renderTick(renderTickTime);
    }

    public void playSound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound != null && VisualSettings.soundControlMutes(sound.getSource())) {
            event.setSound(null);
            return;
        }
        worldEffectRenderer.trackSound(sound);
    }

    public void playerTickStart(Minecraft minecraft) {
        movementService.playerTickStart(minecraft);
    }

    public void attackEntity(AttackEntityEvent event) {
        if (event.getEntity() instanceof LocalPlayer
                && event.getTarget() instanceof LivingEntity target) {
            HitSoundService.play(target);
        }
    }

    public void movementInputUpdate(MovementInputUpdateEvent event) {
        movementService.movementInputUpdate(event);
        CriticalsService.onMovementInput(event, KillAuraService.activeTarget());
        ScaffoldSilenceTellyService.onMovementInput(event);
        StuckService.onMovementInput(event);
        MovementFixService.onMovementInput(event);
    }

    public void renderPlayerPre(RenderPlayerEvent.Pre event) {
        playerRenderPipeline.beforeRender(event);
    }

    public void renderPlayerPost(RenderPlayerEvent.Post event) {
        playerRenderPipeline.afterRender(event);
    }

    public void renderLevelStage(RenderLevelStageEvent event) {
        worldRenderPipeline.render(event);
    }

    public void runtimeTickStart(Minecraft minecraft) {
        ServerDetector.tick();
        KeepSprintService.beginRuntimeTick();
        MenuMovementService.updateKeyMappings(minecraft);
        EdgeJumpService.tick(minecraft);
        BlinkService.tick();
        StuckService.tick();
        AutoReconnectService.tick();
        QuickSwitchService.tick();
        NoSlowService.tick();
        FakeLagService.tick();
        SpeedModeService.tick();
        TimerService.tick();
        FastWebService.tick();
        FastBreakService.tick();
        BreakerService.tick();
        AutoHeadService.tick();
        AutoBlockService.tick();
        AntiStaffService.tick();
        movementService.startEarlyTick(minecraft);
        FreecamService.tick();
        ZoomState.tick();
        FullbrightService.tick();
        movementService.startLateTick(minecraft);
        DisablerService.tick();
        PingSpoofService.tick();
        ScaffoldService.tick();
        KillAuraService.tick();
        CombatAssistService.tick();
        VelocityModeService.tick();
        AutoPearlService.tick();
        AutoExpService.tick();
        FastUseService.tick();
        AutoHealService.tick();
        AutoEatService.tick();
        PearlThrowService.tick();
        AutoFishService.tick();
        ChestStealService.tick();
        InventoryCleanerService.tick();
        AutoRefillService.tick();
        AutoSortService.tick();
        AutoToolService.tick();
        AutoMineService.tick();
        NukerService.tick();
        VeinMinerService.tick();
        AutoTotemService.tick();
        AutoArmorService.tick();
        BedAuraService.tick();
        AnchorAuraService.tick();
        WebAuraService.tick();
        AutoTrapService.tick();
        SurroundService.tick();
        HoleFillService.tick();
        AutoCrystalService.tick();
        FakePlayerRegistry.tick();
        BacktrackService.tick();
        core.eventBus().post(new TickEvent.RuntimeStart());
        RuntimeStateBridge.update(core.runtimeState(), minecraft);
    }

    public void runtimeTickEnd(Minecraft minecraft) {
        KeepSprintService.endRuntimeTick();
        core.eventBus().post(new TickEvent.RuntimeEnd());
        movementService.endTick(minecraft);
        ScaffoldService.tickEnd();
        TargetStrafeService.tickEnd();
        CombatAssistService.tickEnd();
        worldEffectRenderer.tick(minecraft);
        RuntimeStateBridge.update(core.runtimeState(), minecraft);
    }
}

