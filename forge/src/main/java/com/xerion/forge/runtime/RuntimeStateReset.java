package com.xerion.forge.runtime;

import com.xerion.common.movement.ScaffoldRotation;
import com.xerion.forge.packet.PacketBlockageService;

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

public final class RuntimeStateReset {
    private RuntimeStateReset() {
    }

    public static void clearWorldRuntime() {
        AutoMineService.clearRuntimeState();
        NukerService.clearRuntimeState();
        VeinMinerService.clearRuntimeState();
        PearlThrowService.clearRuntimeState();
        HitSoundService.clearRuntimeState();
        BypassProtocolService.clearRuntimeState();
        DisablerService.clearRuntimeState();
        PingSpoofService.clearRuntimeState();
        NoRotateService.clearRuntimeState();
        VelocityModeService.clearRuntimeState();
        CriticalsService.clearRuntimeState();
        PacketBlockageService.reset();
        ScaffoldSilenceTellyService.clearRuntimeState();
        StuckService.clearRuntimeState();
        SkipTickService.reset();
        ServerDetector.reset();
        NoFallWatchdogService.clearRuntimeState();
        NoSlowService.clearRuntimeState();
        FakeLagService.clearRuntimeState();
        FlightService.resetFlightModes();
        SpeedModeService.clearRuntimeState();
        TimerService.reset();
        MovementFixService.clearRuntimeState();
        FastWebService.clearRuntimeState();
        AutoHeadService.clearRuntimeState();
        AutoBlockService.clearRuntimeState();
        AntiStaffService.clearRuntimeState();
        AutoHypixelService.clearRuntimeState();
        FastBreakService.clearRuntimeState();
        BreakerService.clearRuntimeState();
        ScaffoldRotation.clear();
        FullbrightService.reset();
        FreecamService.resetState();
        ZoomState.reset();
    }
}

