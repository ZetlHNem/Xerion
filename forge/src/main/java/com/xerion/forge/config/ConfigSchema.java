package com.xerion.forge.config;

import com.xerion.common.config.CombatSettings;
import com.xerion.common.config.BypassSettings;
import com.xerion.common.config.CommonConfigSchema;
import com.xerion.common.config.DisablerSettings;
import com.xerion.common.config.NoRotateSettings;
import com.xerion.common.config.PingSpoofSettings;
import com.xerion.common.config.UiSettings;

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

import java.util.List;

final class ConfigSchema {
    static final int CURRENT_VERSION = CommonConfigSchema.CURRENT_VERSION;
    static final String VERSION_KEY = CommonConfigSchema.VERSION_KEY;
    static final String PROFILE_EXTENSION = CommonConfigSchema.PROFILE_EXTENSION;
    static final String HOTKEYS_PREFIX = CommonConfigSchema.HOTKEYS_PREFIX;
    static final String FILE_COMMENT = CommonConfigSchema.FILE_COMMENT;

    static final List<Owner> OWNERS = List.of(
            new Owner("combat", CombatSettings.class),
            new Owner("bypass", BypassSettings.class),
            new Owner("bypass", DisablerSettings.class),
            new Owner("bypass", PingSpoofSettings.class),
            new Owner("bypass", NoRotateSettings.class),
            new Owner("visual", VisualSettings.class),
            new Owner("ui", UiSettings.class)
    );

    private ConfigSchema() {
    }

    static String category(Class<?> type) {
        for (Owner owner : OWNERS) {
            if (owner.type() == type) {
                return owner.category();
            }
        }
        return "misc";
    }

    record Owner(String category, Class<?> type) {
    }
}
