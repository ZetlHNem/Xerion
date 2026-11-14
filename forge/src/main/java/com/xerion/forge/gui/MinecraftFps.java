package com.xerion.forge.gui;

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

public final class MinecraftFps {
    private MinecraftFps() {
    }

    public static int read() {
        return Minecraft.getInstance().getFps();
    }
}
