package com.xerion.forge.world;

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

import com.xerion.forge.config.VisualSettings;

import net.minecraft.resources.ResourceLocation;

public final class AmbienceService {
    private static final String PUMPKIN_BLUR_PATH = "textures/misc/pumpkinblur.png";

    private AmbienceService() {
    }

    public static boolean hidePumpkinOverlay(ResourceLocation texture) {
        return VisualSettings.ambienceEnabled()
                && VisualSettings.ambiencePumpkinOverlay()
                && texture != null
                && PUMPKIN_BLUR_PATH.equals(texture.getPath());
    }

    public static boolean hidePortalOverlay() {
        return VisualSettings.ambienceEnabled()
                && VisualSettings.ambiencePortalOverlay();
    }

    public static boolean hideDarknessPulse() {
        return VisualSettings.ambienceEnabled()
                && VisualSettings.ambienceDarknessPulse();
    }
}
