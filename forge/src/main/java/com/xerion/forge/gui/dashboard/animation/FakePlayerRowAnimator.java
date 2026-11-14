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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class FakePlayerRowAnimator {
    private final Map<Integer, FakePlayerRowAnimation> animations = new HashMap<>();

    List<FakePlayerRowEntry> updateRows(List<FakePlayerRegistry.DummySnapshot> snapshots,
                                        int firstY, int rowH, int rowGap,
                                        boolean snapVisibleRows) {
        Set<Integer> visibleIds = new HashSet<>();
        List<FakePlayerRowEntry> rows = new ArrayList<>();
        int targetY = firstY;
        for (FakePlayerRegistry.DummySnapshot snapshot : snapshots) {
            int id = snapshot.entityId();
            visibleIds.add(id);
            FakePlayerRowAnimation animation = animations.get(id);
            if (animation == null) {
                animation = new FakePlayerRowAnimation(snapshot, targetY + Math.max(10, rowGap + 8));
                animations.put(id, animation);
            }
            animation.snapshot = snapshot;
            animation.targetY = targetY;
            animation.deleting = false;
            updateAnimation(animation, snapVisibleRows);
            rows.add(new FakePlayerRowEntry(snapshot, animation));
            targetY += rowH + rowGap;
        }

        for (Map.Entry<Integer, FakePlayerRowAnimation> entry : new ArrayList<>(animations.entrySet())) {
            if (visibleIds.contains(entry.getKey())) {
                continue;
            }
            FakePlayerRowAnimation animation = entry.getValue();
            if (!animation.deleting) {
                animation.deleting = true;
                animation.targetY = Math.round(animation.y) - 14;
            }
            updateAnimation(animation, false);
            if (animation.alpha <= 0.02f) {
                animations.remove(entry.getKey());
                continue;
            }
            rows.add(new FakePlayerRowEntry(animation.snapshot, animation));
        }

        rows.sort((left, right) -> Float.compare(left.animation().y, right.animation().y));
        return rows;
    }

    private void updateAnimation(FakePlayerRowAnimation animation, boolean snapY) {
        float targetAlpha = animation.deleting ? 0.0f : 1.0f;
        animation.y = snapY ? animation.targetY : AnimationClock.lerp(animation.y, animation.targetY, UiAnimationTimings.CARD_SIZE);
        animation.alpha = AnimationClock.lerp(animation.alpha, targetAlpha, UiAnimationTimings.POPUP_CLOSE);
        if (Math.abs(animation.y - animation.targetY) < 0.35f) {
            animation.y = animation.targetY;
        }
        if (Math.abs(animation.alpha - targetAlpha) < 0.01f) {
            animation.alpha = targetAlpha;
        }
    }
}
