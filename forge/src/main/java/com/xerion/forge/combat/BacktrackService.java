package com.xerion.forge.combat;

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

import com.xerion.common.combat.CombatTargetRules;
import com.xerion.common.config.CombatSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class BacktrackService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final int ABSOLUTE_MAX_HISTORY = 16;
    private static final Map<Integer, History> HISTORIES = new HashMap<>();
    private static ClientLevel lastLevel;

    private BacktrackService() {
    }

    public static void tick() {
        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (player == null || level == null || level != lastLevel) {
            clear();
            lastLevel = level;
            return;
        }
        if (!CombatSettings.backtrackEnabled()) {
            clear();
            return;
        }

        long tick = level.getGameTime();
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity target) || target == player || !target.isAlive()
                    || target.isRemoved() || target.isSpectator()) {
                continue;
            }
            HISTORIES.computeIfAbsent(target.getId(), unused -> new History(target))
                    .add(tick, target.getBoundingBox());
        }
        prune(tick);
    }

    public static Iterable<Entry> entries() {
        return () -> new Iterator<>() {
            private final Iterator<History> historyIterator = HISTORIES.values().iterator();
            private Iterator<Entry> entryIterator = java.util.Collections.emptyIterator();

            @Override
            public boolean hasNext() {
                while (!entryIterator.hasNext() && historyIterator.hasNext()) {
                    History history = historyIterator.next();
                    LivingEntity entity = history.entity;
                    if (entity == null || entity.isRemoved() || !entity.isAlive()) {
                        continue;
                    }
                    entryIterator = history.entries.iterator();
                }
                return entryIterator.hasNext();
            }

            @Override
            public Entry next() {
                return entryIterator.next();
            }
        };
    }

    private static void prune(long tick) {
        int maxAge = CombatTargetRules.backtrackMaxAge(CombatSettings.backtrackTicks(), ABSOLUTE_MAX_HISTORY);
        HISTORIES.entrySet().removeIf(entry -> {
            History history = entry.getValue();
            LivingEntity entity = history.entity;
            if (entity == null || entity.isRemoved() || !entity.isAlive()) {
                return true;
            }
            while (!history.entries.isEmpty()
                    && CombatTargetRules.backtrackEntryExpired(tick, history.entries.peekFirst().tick, maxAge)) {
                history.entries.removeFirst();
            }
            return history.entries.isEmpty();
        });
    }

    public static void clear() {
        HISTORIES.clear();
    }

    private static final class History {
        private final LivingEntity entity;
        private final ArrayDeque<Entry> entries = new ArrayDeque<>();

        private History(LivingEntity entity) {
            this.entity = entity;
        }

        private void add(long tick, AABB box) {
            if (!entries.isEmpty() && CombatTargetRules.backtrackSameTick(entries.peekLast().tick, tick)) {
                entries.removeLast();
            }
            entries.addLast(new Entry(entity, box, tick));
            while (CombatTargetRules.backtrackHistoryTooLarge(entries.size(), ABSOLUTE_MAX_HISTORY)) {
                entries.removeFirst();
            }
        }
    }

    public record Entry(LivingEntity entity, AABB box, long tick) {
    }
}
