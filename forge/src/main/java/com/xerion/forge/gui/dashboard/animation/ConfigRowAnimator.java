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

import com.xerion.forge.config.ProfileConfigStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ConfigRowAnimator {
    private final Map<String, ConfigRowAnimation> animations = new HashMap<>();
    private int lastDashboardY = Integer.MIN_VALUE;

    List<ConfigRowEntry> updateRows(List<ProfileConfigStore.ProfileInfo> profiles,
                                    int firstY, int rowH, int rowGap,
                                    boolean snapVisibleRows, int dashboardY, boolean dragging,
                                    Map<String, Float> configIconAnimations,
                                    Map<String, Float> configNameEditAnimations) {
        boolean snapY = syncToDashboard(dashboardY, dragging);
        Set<String> visibleNames = new HashSet<>();
        List<ConfigRowEntry> rows = new ArrayList<>();
        int targetY = firstY;
        for (ProfileConfigStore.ProfileInfo profile : profiles) {
            String name = profile.name();
            visibleNames.add(name);
            ConfigRowAnimation animation = animations.get(name);
            if (animation == null) {
                animation = new ConfigRowAnimation(profile, targetY + Math.max(10, rowGap + 8));
                animations.put(name, animation);
                if (snapVisibleRows) {
                    animation.y = targetY;
                    animation.alpha = 1.0f;
                }
            }
            animation.profile = profile;
            animation.targetY = targetY;
            animation.deleting = false;
            updateAnimation(animation, snapY || snapVisibleRows);
            rows.add(new ConfigRowEntry(profile, animation));
            targetY += rowH + rowGap;
        }

        for (Map.Entry<String, ConfigRowAnimation> entry : new ArrayList<>(animations.entrySet())) {
            if (visibleNames.contains(entry.getKey())) {
                continue;
            }
            ConfigRowAnimation animation = entry.getValue();
            if (!animation.deleting) {
                animation.deleting = true;
                animation.targetY = Math.round(animation.y) - 14;
            }
            updateAnimation(animation, snapY);
            if (animation.alpha <= 0.02f) {
                animations.remove(entry.getKey());
                configIconAnimations.remove("config-icon/" + entry.getKey());
                configNameEditAnimations.remove("config-name-edit/" + entry.getKey());
                continue;
            }
            rows.add(new ConfigRowEntry(animation.profile, animation));
        }

        rows.sort((left, right) -> Float.compare(left.animation().y, right.animation().y));
        return rows;
    }

    private boolean syncToDashboard(int dashboardY, boolean dragging) {
        if (lastDashboardY == Integer.MIN_VALUE) {
            lastDashboardY = dashboardY;
            return true;
        }
        int deltaY = dashboardY - lastDashboardY;
        lastDashboardY = dashboardY;
        if (deltaY == 0) {
            return dragging;
        }
        for (ConfigRowAnimation animation : animations.values()) {
            animation.y += deltaY;
            animation.targetY += deltaY;
        }
        return true;
    }

    private void updateAnimation(ConfigRowAnimation animation, boolean snapY) {
        float targetAlpha = animation.deleting ? 0.0f : 1.0f;
        float targetY = animation.targetY;
        animation.y = snapY ? targetY : AnimationClock.lerp(animation.y, targetY, UiAnimationTimings.CARD_SIZE);
        animation.alpha = AnimationClock.lerp(animation.alpha, targetAlpha, UiAnimationTimings.POPUP_CLOSE);
        if (Math.abs(animation.y - targetY) < 0.35f) {
            animation.y = targetY;
        }
        if (Math.abs(animation.alpha - targetAlpha) < 0.01f) {
            animation.alpha = targetAlpha;
        }
    }
}
