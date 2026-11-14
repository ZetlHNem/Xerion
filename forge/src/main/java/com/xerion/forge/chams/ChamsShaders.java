package com.xerion.forge.chams;

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

import net.minecraft.client.renderer.ShaderInstance;

public final class ChamsShaders {
    private static ShaderInstance shader;

    private ChamsShaders() {
    }

    public static void init(ShaderInstance loadedShader) {
        shader = loadedShader;
    }

    public static ShaderInstance shader() {
        return shader;
    }
}
