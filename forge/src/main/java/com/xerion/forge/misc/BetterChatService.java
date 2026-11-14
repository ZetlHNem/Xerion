package com.xerion.forge.misc;

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
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BetterChatService {
    private static final int MAX_HISTORY = 100;
    private static final int CHAT_X = 6;
    private static final int CHAT_FOCUSED_BOTTOM_OFFSET = 29;
    private static final int CHAT_UNFOCUSED_BOTTOM_OFFSET = 11;
    private static final int CHAT_WIDTH = 324;
    private static final int INPUT_MARGIN_X = 6;
    private static final int INPUT_HEIGHT = 16;
    private static final int INPUT_TARGET_OFFSET = 22;
    private static final int PANEL_RADIUS = 5;
    private static final int PANEL_BORDER_COLOR = 0x14FFFFFF;
    private static final int LINE_HEIGHT = 10;
    private static final int LINE_GAP = 2;
    private static final long MESSAGE_LIFETIME_MS = 12000L;
    private static final long MESSAGE_EXIT_MS = 160L;
    private static final int MESSAGE_EXIT_DISTANCE = 18;
    private static final List<Entry> MESSAGES = new ArrayList<>();
    private static final Map<String, Float> LINE_Y = new HashMap<>();
    private static final Map<String, Float> LINE_ALPHA = new HashMap<>();
    private static long nextMessageId;
    private static float containerTop = Float.NaN;
    private static float containerBottom = Float.NaN;
    private static float containerAlpha;
    private static float inputY = Float.NaN;
    private static Field chatInputField;
    private static Field chatSuggestionsField;

    private BetterChatService() {
    }

    public static void add(Component message) {
        if (message == null || message.getString().isBlank()) {
            return;
        }
        MESSAGES.add(new Entry(nextMessageId++, message, Util.getMillis()));
        while (MESSAGES.size() > MAX_HISTORY) {
            MESSAGES.remove(0);
        }
    }

    public static void clearRuntimeState() {
        MESSAGES.clear();
        LINE_Y.clear();
        LINE_ALPHA.clear();
        containerTop = Float.NaN;
        containerBottom = Float.NaN;
        containerAlpha = 0.0F;
        inputY = Float.NaN;
    }

    public static void render(GuiGraphics graphics) {
        if (!VisualSettings.betterChatEnabled()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            inputY = Float.NaN;
            return;
        }
        boolean focused = minecraft.screen instanceof ChatScreen;
        if (minecraft.options.hideGui) {
            inputY = Float.NaN;
            return;
        }
        if (MESSAGES.isEmpty()) {
            renderClosingInput(graphics, focused);
            return;
        }

        Font font = minecraft.font;
        long now = Util.getMillis();
        int maxLines = focused ? 20 : 10;
        int bottomOffset = focused ? CHAT_FOCUSED_BOTTOM_OFFSET : CHAT_UNFOCUSED_BOTTOM_OFFSET;
        int bottom = graphics.guiHeight() - bottomOffset;
        int y = bottom;
        int drawnLines = 0;
        List<RenderLine> lines = new ArrayList<>();

        for (int i = MESSAGES.size() - 1; i >= 0 && drawnLines < maxLines; i--) {
            Entry entry = MESSAGES.get(i);
            long age = now - entry.timeMs;
            if (!focused && age > MESSAGE_LIFETIME_MS + MESSAGE_EXIT_MS) {
                continue;
            }
            float exitProgress = focused ? 0.0F : messageExitProgress(age);
            float alpha = focused ? 1.0F : 1.0F - exitProgress;
            if (alpha <= 0.02F) {
                continue;
            }

            List<FormattedCharSequence> split = font.split(entry.message, CHAT_WIDTH - 10);
            for (int lineIndex = split.size() - 1; lineIndex >= 0 && drawnLines < maxLines; lineIndex--) {
                FormattedCharSequence line = split.get(lineIndex);
                y -= LINE_HEIGHT;
                int targetY = y + Math.round(exitProgress * MESSAGE_EXIT_DISTANCE);
                lines.add(new RenderLine(entry.id + ":" + lineIndex, line, targetY, alpha));
                y -= LINE_GAP;
                drawnLines++;
            }
        }

        if (lines.isEmpty()) {
            LINE_Y.clear();
            LINE_ALPHA.clear();
            containerTop = Float.NaN;
            containerBottom = Float.NaN;
            containerAlpha = 0.0F;
            return;
        }

        int targetTop = lines.stream().mapToInt(RenderLine::targetY).min().orElse(bottom) - 5;
        int targetBottom = lines.stream().mapToInt(line -> line.targetY + LINE_HEIGHT).max().orElse(bottom) + 5;
        float targetContainerAlpha = 0.0F;
        for (RenderLine line : lines) {
            targetContainerAlpha = Math.max(targetContainerAlpha, line.alpha);
        }

        if (Float.isNaN(containerTop) || Float.isNaN(containerBottom)) {
            containerTop = targetBottom;
            containerBottom = targetBottom;
            containerAlpha = 0.0F;
        }
        float containerMoveSpeed = targetContainerAlpha < containerAlpha ? 0.55F : 0.20F;
        float containerAlphaSpeed = targetContainerAlpha < containerAlpha ? 0.70F : 0.24F;
        containerTop = lerp(containerTop, targetTop, containerMoveSpeed);
        containerBottom = lerp(containerBottom, targetBottom, containerMoveSpeed);
        containerAlpha = lerp(containerAlpha, targetContainerAlpha, containerAlphaSpeed);
        if (Math.abs(containerTop - targetTop) < 0.35F) {
            containerTop = targetTop;
        }
        if (Math.abs(containerBottom - targetBottom) < 0.35F) {
            containerBottom = targetBottom;
        }
        if (Math.abs(containerAlpha - targetContainerAlpha) < 0.01F) {
            containerAlpha = targetContainerAlpha;
        }

        int animatedTop = Math.round(containerTop);
        int animatedBottom = Math.round(containerBottom);
        ShapeRenderer.roundRect(graphics, CHAT_X, animatedTop, CHAT_WIDTH, animatedBottom - animatedTop, PANEL_RADIUS,
                alphaColor(VisualSettings.betterChatBackgroundColor(), containerAlpha));
        ShapeRenderer.roundRectBorder(graphics, CHAT_X, animatedTop, CHAT_WIDTH, animatedBottom - animatedTop,
                PANEL_RADIUS, 1, alphaColor(PANEL_BORDER_COLOR, containerAlpha));

        List<String> visibleKeys = new ArrayList<>();
        for (int i = lines.size() - 1; i >= 0; i--) {
            RenderLine line = lines.get(i);
            visibleKeys.add(line.key);
            float currentY = LINE_Y.getOrDefault(line.key, (float) line.targetY + 8.0F);
            float currentAlpha = LINE_ALPHA.getOrDefault(line.key, 0.0F);
            currentY = lerp(currentY, line.targetY, line.alpha < currentAlpha ? 0.62F : 0.22F);
            currentAlpha = line.alpha < currentAlpha ? line.alpha : lerp(currentAlpha, line.alpha, 0.24F);
            if (Math.abs(currentY - line.targetY) < 0.25F) {
                currentY = line.targetY;
            }
            if (Math.abs(currentAlpha - line.alpha) < 0.01F) {
                currentAlpha = line.alpha;
            }
            LINE_Y.put(line.key, currentY);
            LINE_ALPHA.put(line.key, currentAlpha);
            graphics.drawString(font, line.text, CHAT_X + 5, Math.round(currentY) + 1,
                    alphaColor(0xFFFFFFFF, currentAlpha), true);
        }
        LINE_Y.keySet().removeIf(key -> !visibleKeys.contains(key));
        LINE_ALPHA.keySet().removeIf(key -> !visibleKeys.contains(key));
        renderClosingInput(graphics, focused);
    }

    public static boolean renderInputReplacement(ChatScreen chatScreen, GuiGraphics graphics, int mouseX, int mouseY,
                                                 float partialTick) {
        if (!VisualSettings.betterChatEnabled()) {
            return false;
        }
        EditBox input = chatInput(chatScreen);
        if (input == null) {
            return false;
        }
        int x = INPUT_MARGIN_X;
        int targetY = graphics.guiHeight() - INPUT_TARGET_OFFSET;
        int vanillaY = graphics.guiHeight() - 16;
        int w = graphics.guiWidth() - INPUT_MARGIN_X * 2;
        int h = INPUT_HEIGHT;
        if (Float.isNaN(inputY)) {
            inputY = graphics.guiHeight() + 2.0F;
        }
        inputY = lerp(inputY, targetY, 0.26F);
        if (Math.abs(inputY - targetY) < 0.35F) {
            inputY = targetY;
        }
        int y = Math.round(inputY);
        ShapeRenderer.roundRect(graphics, x, y, w, h, PANEL_RADIUS, VisualSettings.betterChatBackgroundColor());
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, PANEL_RADIUS, 1, PANEL_BORDER_COLOR);
        drawInputText(graphics, input, x, y, w, h);

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, inputY - vanillaY, 0.0F);
        CommandSuggestions suggestions = chatSuggestions(chatScreen);
        if (suggestions != null) {
            suggestions.render(graphics, mouseX, mouseY);
        }
        graphics.pose().popPose();
        return true;
    }

    private static void renderClosingInput(GuiGraphics graphics, boolean focused) {
        if (focused || Float.isNaN(inputY)) {
            return;
        }
        float targetY = graphics.guiHeight() + 2.0F;
        inputY = lerp(inputY, targetY, 0.26F);
        if (inputY >= graphics.guiHeight() + 1.0F) {
            inputY = Float.NaN;
            return;
        }
        int x = INPUT_MARGIN_X;
        int y = Math.round(inputY);
        int w = graphics.guiWidth() - INPUT_MARGIN_X * 2;
        int h = INPUT_HEIGHT;
        ShapeRenderer.roundRect(graphics, x, y, w, h, PANEL_RADIUS, VisualSettings.betterChatBackgroundColor());
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, PANEL_RADIUS, 1, PANEL_BORDER_COLOR);
    }

    private static void drawInputText(GuiGraphics graphics, EditBox input, int x, int y, int w, int h) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        String value = input.getValue();
        int cursor = Math.max(0, Math.min(input.getCursorPosition(), value.length()));
        int padding = Math.max(3, Math.round((h - font.lineHeight) * 0.5F));
        int textX = x + padding;
        int textY = y + padding;
        int maxTextW = Math.max(10, w - padding * 2);
        VisibleInput visible = visibleInput(font, value, cursor, maxTextW);
        graphics.drawString(font, visible.text(), textX, textY, 0xFFEDEDED, false);
        if (((Util.getMillis() / 500L) & 1L) == 0L) {
            int cursorX = textX + font.width(value.substring(visible.start(), cursor));
            graphics.vLine(cursorX, y + padding, y + h - padding - 1, 0xFFEDEDED);
        }
    }

    private static EditBox chatInput(ChatScreen screen) {
        try {
            if (chatInputField == null) {
                chatInputField = findField(EditBox.class);
            }
            if (chatInputField == null) {
                return null;
            }
            Object value = chatInputField.get(screen);
            return value instanceof EditBox input ? input : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static CommandSuggestions chatSuggestions(ChatScreen screen) {
        try {
            if (chatSuggestionsField == null) {
                chatSuggestionsField = findField(CommandSuggestions.class);
            }
            if (chatSuggestionsField == null) {
                return null;
            }
            Object value = chatSuggestionsField.get(screen);
            return value instanceof CommandSuggestions suggestions ? suggestions : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Field findField(Class<?> type) {
        for (Field field : ChatScreen.class.getDeclaredFields()) {
            if (type.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    private static float messageExitProgress(long ageMs) {
        if (ageMs <= MESSAGE_LIFETIME_MS) {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, (ageMs - MESSAGE_LIFETIME_MS) / (float) MESSAGE_EXIT_MS));
    }

    private static int alphaColor(int color, float alpha) {
        int baseAlpha = (color >>> 24) & 0xFF;
        int nextAlpha = Math.max(0, Math.min(255, Math.round(baseAlpha * alpha)));
        return (color & 0x00FFFFFF) | (nextAlpha << 24);
    }

    private static int forceMinAlpha(int color, int minAlpha) {
        int alpha = Math.max(minAlpha, (color >>> 24) & 0xFF);
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private static String trimInputToWidth(Font font, String value, int cursor, int maxWidth) {
        if (font.width(value) <= maxWidth) {
            return value;
        }
        int start = 0;
        if (cursor > 0) {
            start = Math.max(0, cursor - 1);
            while (start > 0 && font.width(value.substring(start, cursor)) < maxWidth - 12) {
                start--;
            }
        }
        String visible = value.substring(start);
        while (!visible.isEmpty() && font.width(visible) > maxWidth) {
            visible = visible.substring(1);
        }
        return visible;
    }

    private static VisibleInput visibleInput(Font font, String value, int cursor, int maxWidth) {
        if (font.width(value) <= maxWidth) {
            return new VisibleInput(value, 0);
        }
        int start = 0;
        while (start < cursor && font.width(value.substring(start, cursor)) > maxWidth - 8) {
            start++;
        }
        int end = value.length();
        while (end > cursor && font.width(value.substring(start, end)) > maxWidth) {
            end--;
        }
        while (start < cursor && font.width(value.substring(start, end)) > maxWidth) {
            start++;
        }
        return new VisibleInput(value.substring(start, end), start);
    }

    private static float lerp(float current, float target, float speed) {
        return current + (target - current) * speed;
    }

    private record Entry(long id, Component message, long timeMs) {
    }

    private record RenderLine(String key, FormattedCharSequence text, int targetY, float alpha) {
    }

    private record VisibleInput(String text, int start) {
    }
}
