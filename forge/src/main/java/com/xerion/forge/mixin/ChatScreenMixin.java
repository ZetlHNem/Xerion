package com.xerion.forge.mixin;

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
import com.xerion.forge.gui.dashboard.ShapeRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    private static Field xerion$inputField;
    private static Field xerion$suggestionsField;

    @Inject(method = "m_88315_", at = @At("HEAD"), cancellable = true)
    private void xerion$renderBetterChatInput(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
                                                         CallbackInfo ci) {
        if (!VisualSettings.betterChatEnabled()) {
            return;
        }
        ChatScreen screen = (ChatScreen) (Object) this;
        EditBox input = xerion$getField(screen, EditBox.class, true);
        if (input == null) {
            return;
        }

        int x = 1;
        int y = graphics.guiHeight() - 16;
        int w = graphics.guiWidth() - 2;
        int h = 16;
        ShapeRenderer.roundRect(graphics, x, y, w, h, 6, VisualSettings.betterChatBackgroundColor());
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 6, 1, 0x18FFFFFF);

        input.render(graphics, mouseX, mouseY, partialTick);
        CommandSuggestions suggestions = xerion$getField(screen, CommandSuggestions.class, false);
        if (suggestions != null) {
            suggestions.render(graphics, mouseX, mouseY);
        }
        ci.cancel();
    }

    private static <T> T xerion$getField(ChatScreen screen, Class<T> type, boolean input) {
        try {
            Field field = input ? xerion$inputField : xerion$suggestionsField;
            if (field == null) {
                field = xerion$findField(type);
                if (input) {
                    xerion$inputField = field;
                } else {
                    xerion$suggestionsField = field;
                }
            }
            if (field == null) {
                return null;
            }
            Object value = field.get(screen);
            return type.isInstance(value) ? type.cast(value) : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Field xerion$findField(Class<?> type) {
        for (Field field : ChatScreen.class.getDeclaredFields()) {
            if (type.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }
}
