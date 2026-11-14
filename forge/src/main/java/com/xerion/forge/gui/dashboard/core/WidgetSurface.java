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

import com.xerion.forge.config.VisualSettings;

import com.xerion.common.module.Module;
import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.xerion.forge.gui.dashboard.widget.Button;
import com.xerion.forge.gui.dashboard.widget.Checkbox;
import com.xerion.forge.gui.dashboard.widget.Color;
import com.xerion.forge.gui.dashboard.widget.GearButton;
import com.xerion.forge.gui.dashboard.widget.Input;
import com.xerion.forge.gui.dashboard.widget.Select;
import com.xerion.forge.gui.dashboard.widget.Slider;
import com.xerion.forge.gui.dashboard.widget.ToggleSwitch;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public final class WidgetSurface {
    private static WidgetSurface activeInstance;

    private static int SURFACE = UiPalette.surface();
    private static int FIELD = UiPalette.field();
    private static int TRACK = UiPalette.track();
    private static int BORDER = UiPalette.border();
    private static int TEXT = UiPalette.text();
    private static int MUTED = UiPalette.muted();
    private static int SUBTLE = UiPalette.subtle();
    private static int ACCENT = UiPalette.accent();
    private static int ACCENT_DARK = UiPalette.accentDark();
    private static int ACCENT_SOFT = UiPalette.accentSoft();
    private static int DANGER = UiPalette.danger();

    private static final int CARD_PAD_X = UiMetrics.Control.CARD_PAD_X;
    private static final int CARD_CONTENT_TOP = UiMetrics.Control.CARD_CONTENT_TOP;
    private static final int CARD_BOTTOM_PAD = UiMetrics.Control.CARD_BOTTOM_PAD;
    private static final int FONT_SIZE = UiMetrics.Control.FONT_SIZE;
    private static final int FRAME_PADDING_X = UiMetrics.Control.FRAME_PADDING_X;
    private static final int FRAME_PADDING_Y = UiMetrics.Control.FRAME_PADDING_Y;
    private static final int ITEM_SPACING_X = UiMetrics.Control.ITEM_SPACING_X;
    private static final int ITEM_SPACING_Y = UiMetrics.Control.ITEM_SPACING_Y;
    private static final int ITEM_INNER_SPACING_Y = UiMetrics.Control.ITEM_INNER_SPACING_Y;
    private static final int FRAME_HEIGHT = FONT_SIZE + FRAME_PADDING_Y * 2;
    private static final int SELECT_HEIGHT = UiMetrics.Control.SELECT_HEIGHT;
    private static final int SELECT_LABEL_GAP = UiMetrics.Control.SELECT_LABEL_GAP;
    private static final int SELECT_POPUP_GAP = UiMetrics.Control.SELECT_POPUP_GAP;
    private static final int SELECT_OPTION_HEIGHT = UiMetrics.Control.SELECT_OPTION_HEIGHT;
    private static final int SELECT_OPTION_GAP = UiMetrics.Control.SELECT_OPTION_GAP;
    private static final int SELECT_POPUP_PAD_Y = UiMetrics.Control.SELECT_POPUP_PAD_Y;
    private static final int GEAR_BUTTON_SIZE = UiMetrics.Control.GEAR_BUTTON_SIZE;
    private static final int HOTKEY_POPUP_W = UiMetrics.HotkeyPopup.POPUP_W;
    private static final int HOTKEY_DETAIL_W = UiMetrics.HotkeyPopup.DETAIL_W;
    private static final int HOTKEY_DETAIL_OVERLAP = UiMetrics.HotkeyPopup.DETAIL_OVERLAP;
    private static final int HOTKEY_POPUP_PAD = UiMetrics.HotkeyPopup.POPUP_PAD;
    private static final int HOTKEY_ROW_H = UiMetrics.HotkeyPopup.ROW_H;
    private static final int HOTKEY_DIVIDER_GAP = UiMetrics.HotkeyPopup.DIVIDER_GAP;
    private static final int HOTKEY_RIGHT_PAD = UiMetrics.HotkeyPopup.RIGHT_PAD;
    private static final int HOTKEY_SWITCH_W = UiMetrics.HotkeyPopup.SWITCH_W;
    private static final int HOTKEY_SWITCH_H = UiMetrics.HotkeyPopup.SWITCH_H;
    private static final int HOTKEY_VALUE_W = UiMetrics.HotkeyPopup.VALUE_W;
    private static final int HOTKEY_VALUE_H = UiMetrics.HotkeyPopup.VALUE_H;
    private static final int HOTKEY_VALUE_POPUP_W = UiMetrics.HotkeyPopup.VALUE_POPUP_W;
    private static final float HOTKEY_VALUE_TEXT_SIZE = UiMetrics.HotkeyPopup.VALUE_TEXT_SIZE;
    private static final float HOTKEY_ICON_SIZE = UiMetrics.HotkeyPopup.ICON_SIZE;
    private static final float HOTKEY_ARROW_ICON_SIZE = UiMetrics.HotkeyPopup.ARROW_ICON_SIZE;
    private static final int HOTKEY_TEXT_Y_OFFSET = UiMetrics.HotkeyPopup.TEXT_Y_OFFSET;
    private static final int SETTINGS_POPUP_W = UiMetrics.Control.SETTINGS_POPUP_W;
    private static final int SETTINGS_POPUP_PAD_X = UiMetrics.Control.SETTINGS_POPUP_PAD_X;
    private static final int SETTINGS_POPUP_PAD_Y = UiMetrics.Control.SETTINGS_POPUP_PAD_Y;
    private static final int TEXT_LINE_HEIGHT = FONT_SIZE;
    private static final int LABELED_FRAME_HEIGHT = TEXT_LINE_HEIGHT + ITEM_INNER_SPACING_Y + FRAME_HEIGHT;
    private static final int LABELED_SELECT_HEIGHT = TEXT_LINE_HEIGHT + SELECT_LABEL_GAP + SELECT_HEIGHT;
    private static final int COLOR_BUTTON_W = UiMetrics.ColorPopup.BUTTON_W;
    private static final int COLOR_POPUP_W = UiMetrics.ColorPopup.POPUP_W;
    private static final int COLOR_POPUP_PAD_X = UiMetrics.ColorPopup.POPUP_PAD_X;
    private static final int COLOR_POPUP_PAD_Y = UiMetrics.ColorPopup.POPUP_PAD_Y;
    private static final int COLOR_POPUP_PREVIEW_H = UiMetrics.ColorPopup.PREVIEW_H;
    private static final int COLOR_ROW_HEIGHT = UiMetrics.ColorPopup.ROW_H;
    private static final int COLOR_ROW_GAP = UiMetrics.ColorPopup.ROW_GAP;
    private static final int SECTION_SPACING = ITEM_SPACING_Y * 2;
    private static final int DIVIDER_H = 1;
    private static final int INFO_ROW_H = FRAME_HEIGHT;
    private static final int ALL_HOTKEYS_W = 620;
    private static final int ALL_HOTKEYS_H = 410;
    private static final int ALL_HOTKEYS_TITLE_H = 42;
    private static final int ALL_HOTKEYS_PAD = 14;
    private static final int ALL_HOTKEYS_SECTION_GAP = 14;
    private static final int ALL_HOTKEYS_SECTION_TITLE_H = 26;
    private static final int ALL_HOTKEYS_HEADER_H = 24;
    private static final int ALL_HOTKEYS_ROW_H = 34;
    private static final int ALL_HOTKEYS_VALUE_POPUP_W = 118;
    private static float renderAlpha = 1.0f;

    private final WidgetValueRegistry widgetRegistry = new WidgetValueRegistry();
    private final WidgetAnimationState animationSystem = new WidgetAnimationState();
    private static final Map<String, Boolean> pendingHotkeyBoolValues = new HashMap<>();
    private static final Map<String, String> pendingHotkeySelectValues = new HashMap<>();
    private static final Map<String, Integer> pendingHotkeyMultiSelectMasks = new HashMap<>();
    private static final Map<String, Float> pendingHotkeySliderValues = new HashMap<>();
    private static final Map<String, Integer> pendingHotkeyColorValues = new HashMap<>();
    private static final Map<String, Boolean> holdRestoreBoolValues = new HashMap<>();
    private static final Map<String, String> holdRestoreSelectValues = new HashMap<>();
    private static final Map<String, Integer> holdRestoreMultiSelectMasks = new HashMap<>();
    private static final Map<String, Float> holdRestoreSliderValues = new HashMap<>();
    private static final Map<String, Integer> holdRestoreColorValues = new HashMap<>();
    private static final Map<String, Boolean> toggleRestoreBoolValues = new HashMap<>();
    private static final Map<String, String> toggleRestoreSelectValues = new HashMap<>();
    private static final Map<String, Integer> toggleRestoreMultiSelectMasks = new HashMap<>();
    private static final Map<String, Float> toggleRestoreSliderValues = new HashMap<>();
    private static final Map<String, Integer> toggleRestoreColorValues = new HashMap<>();
    private static final Map<String, HotkeyControlState> hotkeyStates = new HashMap<>();
    private static final Map<String, HotkeyMeta> hotkeyMetas = new HashMap<>();
    private static final Set<Integer> pressedHotkeyKeys = new HashSet<>();
    private static final Set<Integer> pressedHotkeyMouseButtons = new HashSet<>();
    private static volatile boolean blocksGameMovementInput;

    private double mouseX;
    private double mouseY;
    private boolean click;
    private int clickButton;
    private double clickX;
    private double clickY;
    private boolean clickConsumed;
    private String activeId;
    private boolean interactionLocked;
    private boolean clipInteraction;
    private int clipX;
    private int clipY;
    private int clipW;
    private int clipH;
    private boolean clipRender;
    private int renderClipX;
    private int renderClipY;
    private int renderClipW;
    private int renderClipH;
    private final Deque<RenderClipState> renderClipStack = new ArrayDeque<>();
    private float renderOffsetX;
    private float renderOffsetY;
    private float renderScale = 1.0f;
    private final PopupStateStore popupSystem = new PopupStateStore();

    private String listeningHotkeyControlId;
    private int listeningHotkeyBindingId = -1;

    private boolean suspendLayoutAnimations;
    private boolean hotkeyPreregistering;

    public WidgetSurface() {
        activeInstance = this;
    }
    private float conditionalAlpha = 1.0f;
    private boolean conditionalVisible = true;
    private boolean conditionalAnimating;
    private int conditionalDepth;
    private boolean conditionalInteractionDisabled;
    private final Deque<ConditionalRenderState> conditionalStack = new ArrayDeque<>();
    private String editingSliderId;
    private String editingSliderText = "";
    private SliderValueAdapter editingSliderAdapter;
    private boolean editingSliderReplaceOnType;
    private int editingSliderX;
    private int editingSliderY;
    private int editingSliderW;
    private int editingSliderH;
    private String editingHotkeySliderControlId;
    private int editingHotkeySliderBindingId = -1;
    private String editingHotkeySliderText = "";
    private Function<String, Float> editingHotkeySliderParser;
    private int editingHotkeySliderX;
    private int editingHotkeySliderY;
    private int editingHotkeySliderW;
    private int editingHotkeySliderH;
    private boolean allHotkeysOpen;
    private float allHotkeysAlpha;
    private int allHotkeysX = Integer.MIN_VALUE;
    private int allHotkeysY = Integer.MIN_VALUE;
    private boolean allHotkeysDragging;
    private double allHotkeysDragOffsetX;
    private double allHotkeysDragOffsetY;
    private int allHotkeysScrollY;
    private float allHotkeysAnimatedScrollY;
    private int allHotkeysMaxScrollY;
    private float allHotkeysScrollbarReserve;
    private float allHotkeysScrollbarAlpha;
    private final ScrollbarControl.DragState allHotkeysScrollbarDrag = new ScrollbarControl.DragState();
    private final List<AllHotkeyDraft> allHotkeyDrafts = new ArrayList<>();
    private final Map<String, AllHotkeySectionAnimation> allHotkeySectionAnimations = new HashMap<>();
    private final Map<String, AllHotkeyRowAnimation> allHotkeyRowAnimations = new HashMap<>();
    private int lastAllHotkeySectionAnimationBaseY = Integer.MIN_VALUE;
    private int lastAllHotkeyAnimationBaseY = Integer.MIN_VALUE;
    private String allHotkeyListeningControlId;
    private int allHotkeyListeningBindingId = -1;
    private AllHotkeyComboPopup allHotkeyComboPopup;
    private AllHotkeyComboPopup closingAllHotkeyComboPopup;
    private final InputFocusState focusSystem = new InputFocusState();
    private char pendingTextInputHighSurrogate;

    void begin(double mouseX, double mouseY, boolean mouseDown, boolean click, int clickButton, double clickX, double clickY) {
        refreshTheme();
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.click = click;
        this.clickButton = clickButton;
        this.clickX = clickX;
        this.clickY = clickY;
        this.clickConsumed = false;
        popupSystem.beginFrame();
        updateAllHotkeysScrollbarDrag(mouseY, mouseDown);
        updateAllHotkeysDrag(mouseX, mouseY, mouseDown);
        if (allHotkeysOpen) {
            handleAllHotkeysClick();
            this.click = false;
        }
        updateGameMovementInputBlock();
        if (click && clickButton == 0 && editingSliderId != null
                && !PopupGeometry.contains(clickX, clickY, editingSliderX, editingSliderY, editingSliderW, editingSliderH)) {
            commitSliderEdit();
        }
        if (click && clickButton == 0 && editingHotkeySliderControlId != null
                && !PopupGeometry.contains(clickX, clickY, editingHotkeySliderX, editingHotkeySliderY,
                editingHotkeySliderW, editingHotkeySliderH)) {
            commitHotkeySliderEdit();
        }
        if (click && clickButton == 0 && focusSystem.textInputId != null
                && !PopupGeometry.contains(clickX, clickY, focusSystem.textInputX, focusSystem.textInputY,
                focusSystem.textInputW, focusSystem.textInputH)) {
            clearTextInputFocus();
        }
        handlePopupClick();
        if (!mouseDown) {
            activeId = null;
            allHotkeysDragging = false;
            allHotkeysScrollbarDrag.stop();
        }
    }

    boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editingSliderId != null) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                commitSliderEdit();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                cancelSliderEdit();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                editingSliderReplaceOnType = false;
                if (!editingSliderText.isEmpty()) {
                    editingSliderText = editingSliderText.substring(0, editingSliderText.length() - 1);
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                editingSliderText = "";
                editingSliderReplaceOnType = false;
                return true;
            }
            return true;
        }

        if (editingHotkeySliderControlId != null) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                commitHotkeySliderEdit();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                cancelHotkeySliderEdit();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!editingHotkeySliderText.isEmpty()) {
                    editingHotkeySliderText = editingHotkeySliderText.substring(0, editingHotkeySliderText.length() - 1);
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                editingHotkeySliderText = "";
                return true;
            }
            return true;
        }

        if (focusSystem.textInputId != null) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                clearTextInputFocus();
                return true;
            }
            String text = widgetRegistry.textValues.getOrDefault(focusSystem.textInputId, "");
            normalizeTextInputSelection(text);
            boolean control = (modifiers & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SUPER)) != 0;
            boolean selecting = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
            if (control && keyCode == GLFW.GLFW_KEY_A) {
                focusSystem.selectAll(text.length());
                return true;
            }
            if (control && keyCode == GLFW.GLFW_KEY_C) {
                copyTextInputSelection(text);
                return true;
            }
            if (control && keyCode == GLFW.GLFW_KEY_X) {
                copyTextInputSelection(text);
                deleteTextInputSelection(text);
                return true;
            }
            if (control && keyCode == GLFW.GLFW_KEY_V) {
                replaceTextInputSelection(Minecraft.getInstance().keyboardHandler.getClipboard());
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!deleteTextInputSelection(text) && focusSystem.cursor > 0) {
                    int start = control
                            ? previousWordIndex(text, focusSystem.cursor)
                            : previousCodePointIndex(text, focusSystem.cursor);
                    deleteTextInputRange(text, start, focusSystem.cursor);
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                if (!deleteTextInputSelection(text) && focusSystem.cursor < text.length()) {
                    int end = control
                            ? nextWordIndex(text, focusSystem.cursor)
                            : nextCodePointIndex(text, focusSystem.cursor);
                    deleteTextInputRange(text, focusSystem.cursor, end);
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_LEFT) {
                int next = !selecting && focusSystem.hasSelection()
                        ? focusSystem.selectionStart()
                        : control
                        ? previousWordIndex(text, focusSystem.cursor)
                        : previousCodePointIndex(text, focusSystem.cursor);
                focusSystem.moveCursor(next, selecting, text.length());
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                int next = !selecting && focusSystem.hasSelection()
                        ? focusSystem.selectionEnd()
                        : control
                        ? nextWordIndex(text, focusSystem.cursor)
                        : nextCodePointIndex(text, focusSystem.cursor);
                focusSystem.moveCursor(next, selecting, text.length());
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_HOME) {
                focusSystem.moveCursor(0, selecting, text.length());
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_END) {
                focusSystem.moveCursor(text.length(), selecting, text.length());
                return true;
            }
            return true;
        }

        if (allHotkeysOpen && allHotkeyListeningControlId != null) {
            AllHotkeyDraft draft = findAllHotkeyDraft(allHotkeyListeningControlId, allHotkeyListeningBindingId);
            if (draft == null || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                clearAllHotkeyListening();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                draft.keyCode = GLFW.GLFW_KEY_UNKNOWN;
                draft.scanCode = 0;
                draft.mouseButton = -1;
                draft.keyName = "-";
                applyAllHotkeyDraft(draft);
                clearAllHotkeyListening();
                return true;
            }
            draft.keyCode = keyCode;
            draft.scanCode = scanCode;
            draft.mouseButton = -1;
            draft.keyName = InputDisplayNames.keyName(keyCode, scanCode);
            applyAllHotkeyDraft(draft);
            clearAllHotkeyListening();
            return true;
        }

        if (allHotkeysOpen) {
            return true;
        }

        if (listeningHotkeyControlId == null) {
            return globalKeyPressed(keyCode, scanCode);
        }

        HotkeyBinding binding = findHotkeyBinding(listeningHotkeyControlId, listeningHotkeyBindingId);
        if (binding == null) {
            clearListeningHotkey();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            clearListeningHotkey();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
            binding.clearKey();
            clearListeningHotkey();
            return true;
        }

        binding.bindKeyboard(keyCode, scanCode);
        clearListeningHotkey();
        return true;
    }

    boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (allHotkeysOpen) {
            return true;
        }
        if (listeningHotkeyControlId != null) {
            return false;
        }
        return globalKeyReleased(keyCode, scanCode);
    }

    boolean mouseClickedForHotkey(int button) {
        if (allHotkeysOpen && allHotkeyListeningControlId != null) {
            AllHotkeyDraft draft = findAllHotkeyDraft(allHotkeyListeningControlId, allHotkeyListeningBindingId);
            if (draft != null) {
                draft.keyCode = GLFW.GLFW_KEY_UNKNOWN;
                draft.scanCode = 0;
                draft.mouseButton = button;
                draft.keyName = InputDisplayNames.mouseButtonName(button);
                applyAllHotkeyDraft(draft);
            }
            clearAllHotkeyListening();
            return true;
        }
        if (listeningHotkeyControlId == null) {
            return false;
        }
        HotkeyBinding binding = findHotkeyBinding(listeningHotkeyControlId, listeningHotkeyBindingId);
        if (binding == null) {
            clearListeningHotkey();
            return true;
        }
        binding.bindMouse(button);
        clearListeningHotkey();
        return true;
    }

    void closePopupsForScroll() {
        popupSystem.openSettingsId = null;
        popupSystem.pendingSettingsPopup = null;
        closeSettingsPopupVisual();

        popupSystem.openSelectId = null;
        popupSystem.pendingSelectPopup = null;
        closeSelectPopupVisual();

        popupSystem.openMultiSelectId = null;
        popupSystem.pendingMultiSelectPopup = null;
        closeMultiSelectPopupVisual();

        popupSystem.openColorId = null;
        popupSystem.pendingColorPopup = null;
        closeColorPopupVisual();

        closeHotkeyPopups();
        popupSystem.pendingMenuPopup = null;
        closeMenuPopupVisual();
        activeId = null;
    }

    void clearTextInputFocusNow() {
        clearTextInputFocus();
    }

    boolean insertImeText(String text) {
        if (focusSystem.textInputId == null || text == null || text.isEmpty()) {
            return false;
        }
        pendingTextInputHighSurrogate = 0;
        replaceTextInputSelection(text);
        return true;
    }

    boolean hasFocusedTextInput() {
        return focusSystem.textInputId != null;
    }

    int focusedTextInputCaretX() {
        if (focusSystem.textInputId == null) {
            return 0;
        }
        String text = widgetRegistry.textValues.getOrDefault(focusSystem.textInputId, "");
        normalizeTextInputSelection(text);
        int available = Math.max(0, focusSystem.textInputW - ITEM_SPACING_X * 2);
        int prefixWidth = inputTextWidth(text.substring(0, focusSystem.cursor));
        return focusSystem.textInputX + ITEM_SPACING_X + Math.min(prefixWidth, available);
    }

    int focusedTextInputCaretY() {
        return focusSystem.textInputY + focusSystem.textInputH;
    }

    void resetControlValuesAfterConfigLoad() {
        widgetRegistry.clearControlValues();

        closePopupsForScroll();
        cancelSliderEdit();
        cancelHotkeySliderEdit();
        clearTextInputFocus();
        clearListeningHotkey();
        activeId = null;
        clickConsumed = true;
    }

    boolean charTyped(char codePoint, int modifiers) {
        if (focusSystem.textInputId != null) {
            if (Character.isISOControl(codePoint)) {
                return false;
            }
            if (Character.isHighSurrogate(codePoint)) {
                pendingTextInputHighSurrogate = codePoint;
                return true;
            }
            if (Character.isLowSurrogate(codePoint)) {
                if (pendingTextInputHighSurrogate != 0) {
                    replaceTextInputSelection(new String(new char[] {
                            pendingTextInputHighSurrogate, codePoint
                    }));
                    pendingTextInputHighSurrogate = 0;
                }
                return true;
            }
            pendingTextInputHighSurrogate = 0;
            replaceTextInputSelection(String.valueOf(codePoint));
            return true;
        }
        if (editingSliderId == null || Character.isISOControl(codePoint)) {
            if (editingHotkeySliderControlId == null || Character.isISOControl(codePoint)) {
                return false;
            }
            if (editingHotkeySliderText.length() < 16) {
                editingHotkeySliderText += codePoint;
            }
            return true;
        }
        editingSliderReplaceOnType = false;
        if (editingSliderText.length() < 16) {
            editingSliderText += codePoint;
        }
        return true;
    }

    public static boolean globalKeyPressed(int keyCode, int scanCode) {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return false;
        }
        if (!pressedHotkeyKeys.add(keyCode)) {
            return false;
        }
        return triggerHotkey(keyCode, scanCode);
    }

    public static boolean globalKeyReleased(int keyCode, int scanCode) {
        syncHotkeyPhysicalState();
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return false;
        }
        pressedHotkeyKeys.remove(keyCode);
        return releaseHoldHotkeys(keyCode, scanCode);
    }

    public static boolean globalMousePressed(int button) {
        if (button < 0 || !pressedHotkeyMouseButtons.add(button)) {
            return false;
        }
        return triggerMouseHotkey(button);
    }

    public static boolean globalMouseReleased(int button) {
        syncHotkeyPhysicalState();
        if (button < 0) {
            return false;
        }
        pressedHotkeyMouseButtons.remove(button);
        return releaseHoldMouseHotkeys(button);
    }

    public static void resetHotkeyPressedState() {
        pressedHotkeyKeys.clear();
        pressedHotkeyMouseButtons.clear();
    }

    public static int saveHotkeys(Properties properties, String prefix) {
        if (properties == null || prefix == null || prefix.isBlank()) {
            return 0;
        }
        List<String> controlIds = new ArrayList<>(hotkeyStates.keySet());
        controlIds.sort(String.CASE_INSENSITIVE_ORDER);
        int controlIndex = 0;
        int savedBindings = 0;
        for (String controlId : controlIds) {
            if (shouldSkipHotkeyPersistence(controlId)) {
                continue;
            }
            HotkeyControlState state = hotkeyStates.get(controlId);
            if (state == null || state.bindings.isEmpty()) {
                continue;
            }
            String controlPrefix = prefix + "." + controlIndex;
            properties.setProperty(controlPrefix + ".id", controlId);
            HotkeyMeta meta = hotkeyMetas.getOrDefault(controlId, fallbackHotkeyMeta(controlId));
            properties.setProperty(controlPrefix + ".group", meta.group == null ? "" : meta.group);
            properties.setProperty(controlPrefix + ".label", meta.label == null ? "" : meta.label);
            properties.setProperty(controlPrefix + ".kind", meta.kind == null ? "" : meta.kind);
            properties.setProperty(controlPrefix + ".valueType", meta.valueType.name());
            properties.setProperty(controlPrefix + ".nextBindingId", Integer.toString(state.nextBindingId));
            if (state.anchor != null) {
                properties.setProperty(controlPrefix + ".anchorX", Integer.toString(state.anchor.x));
                properties.setProperty(controlPrefix + ".anchorY", Integer.toString(state.anchor.y));
            }
            properties.setProperty(controlPrefix + ".bindingCount", Integer.toString(state.bindings.size()));
            for (int bindingIndex = 0; bindingIndex < state.bindings.size(); bindingIndex++) {
                HotkeyBinding binding = state.bindings.get(bindingIndex);
                String bindingPrefix = controlPrefix + ".binding." + bindingIndex;
                properties.setProperty(bindingPrefix + ".id", Integer.toString(binding.id));
                properties.setProperty(bindingPrefix + ".keyCode", Integer.toString(binding.keyCode));
                properties.setProperty(bindingPrefix + ".scanCode", Integer.toString(binding.scanCode));
                properties.setProperty(bindingPrefix + ".mouseButton", Integer.toString(binding.mouseButton));
                properties.setProperty(bindingPrefix + ".keyName", binding.keyName == null ? "-" : binding.keyName);
                properties.setProperty(bindingPrefix + ".mode", binding.mode == null ? "Toggle" : binding.mode);
                properties.setProperty(bindingPrefix + ".visible", Boolean.toString(binding.visible));
                properties.setProperty(bindingPrefix + ".valueLabel", binding.valueLabel == null ? "-" : binding.valueLabel);
                if (binding.boolValue != null) {
                    properties.setProperty(bindingPrefix + ".boolValue", Boolean.toString(binding.boolValue));
                }
                properties.setProperty(bindingPrefix + ".selectedValue",
                        binding.selectedValue == null ? "-" : binding.selectedValue);
                properties.setProperty(bindingPrefix + ".multiMask", Integer.toString(binding.multiMask));
                properties.setProperty(bindingPrefix + ".sliderValue", Float.toString(binding.sliderValue));
                properties.setProperty(bindingPrefix + ".colorValue", Integer.toString(binding.colorValue));
                savedBindings++;
            }
            controlIndex++;
        }
        properties.setProperty(prefix + ".controlCount", Integer.toString(controlIndex));
        return savedBindings;
    }

    public static int loadHotkeys(Properties properties, String prefix) {
        if (properties == null || prefix == null || prefix.isBlank()) {
            return 0;
        }
        hotkeyStates.clear();
        pressedHotkeyKeys.clear();
        pressedHotkeyMouseButtons.clear();
        pendingHotkeyBoolValues.clear();
        pendingHotkeySelectValues.clear();
        pendingHotkeyMultiSelectMasks.clear();
        pendingHotkeySliderValues.clear();
        pendingHotkeyColorValues.clear();
        holdRestoreBoolValues.clear();
        holdRestoreSelectValues.clear();
        holdRestoreMultiSelectMasks.clear();
        holdRestoreSliderValues.clear();
        holdRestoreColorValues.clear();
        toggleRestoreBoolValues.clear();
        toggleRestoreSelectValues.clear();
        toggleRestoreMultiSelectMasks.clear();
        toggleRestoreSliderValues.clear();
        toggleRestoreColorValues.clear();

        int controlCount = WidgetSurfaceValueTools.parseInt(properties.getProperty(prefix + ".controlCount"), 0);
        int loadedBindings = 0;
        for (int controlIndex = 0; controlIndex < controlCount; controlIndex++) {
            String controlPrefix = prefix + "." + controlIndex;
            String controlId = properties.getProperty(controlPrefix + ".id");
            if (controlId == null || controlId.isBlank() || shouldSkipHotkeyPersistence(controlId)) {
                continue;
            }
            HotkeyControlState state = new HotkeyControlState();
            state.nextBindingId = WidgetSurfaceValueTools.parseInt(properties.getProperty(controlPrefix + ".nextBindingId"), 1);
            if (properties.containsKey(controlPrefix + ".anchorX") && properties.containsKey(controlPrefix + ".anchorY")) {
                state.anchor = new HotkeyAnchor(
                        WidgetSurfaceValueTools.parseInt(properties.getProperty(controlPrefix + ".anchorX"), 0),
                        WidgetSurfaceValueTools.parseInt(properties.getProperty(controlPrefix + ".anchorY"), 0));
            }
            HotkeyMeta meta = fallbackHotkeyMeta(controlId);
            String group = properties.getProperty(controlPrefix + ".group", meta.group);
            String label = properties.getProperty(controlPrefix + ".label", meta.label);
            String kind = properties.getProperty(controlPrefix + ".kind", meta.kind);
            HotkeyValueType valueType = parseHotkeyValueType(properties.getProperty(controlPrefix + ".valueType"), meta.valueType);
            hotkeyMetas.put(controlId, new HotkeyMeta(group, label, kind, meta.valueLabel, meta.boolValue,
                    valueType, meta.selectedValue, meta.options, meta.multiMask, meta.sliderValue, meta.colorValue,
                    meta.applier, meta.sliderFormatter, meta.sliderParser));
            int bindingCount = WidgetSurfaceValueTools.parseInt(properties.getProperty(controlPrefix + ".bindingCount"), 0);
            for (int bindingIndex = 0; bindingIndex < bindingCount; bindingIndex++) {
                String bindingPrefix = controlPrefix + ".binding." + bindingIndex;
                int bindingId = WidgetSurfaceValueTools.parseInt(properties.getProperty(bindingPrefix + ".id"), state.nextBindingId++);
                HotkeyBinding binding = new HotkeyBinding(bindingId);
                binding.keyCode = WidgetSurfaceValueTools.parseInt(properties.getProperty(bindingPrefix + ".keyCode"), GLFW.GLFW_KEY_UNKNOWN);
                binding.scanCode = WidgetSurfaceValueTools.parseInt(properties.getProperty(bindingPrefix + ".scanCode"), 0);
                binding.mouseButton = WidgetSurfaceValueTools.parseInt(properties.getProperty(bindingPrefix + ".mouseButton"), -1);
                binding.keyName = properties.getProperty(bindingPrefix + ".keyName",
                        binding.mouseButton >= 0 ? InputDisplayNames.mouseButtonName(binding.mouseButton) : InputDisplayNames.keyName(binding.keyCode, binding.scanCode));
                binding.mode = properties.getProperty(bindingPrefix + ".mode", "Toggle");
                if (!"Hold".equals(binding.mode)) {
                    binding.mode = "Toggle";
                }
                binding.visible = Boolean.parseBoolean(properties.getProperty(bindingPrefix + ".visible", "true"));
                binding.valueLabel = properties.getProperty(bindingPrefix + ".valueLabel", "-");
                String boolValue = properties.getProperty(bindingPrefix + ".boolValue");
                binding.boolValue = boolValue == null ? null : Boolean.parseBoolean(boolValue);
                binding.selectedValue = properties.getProperty(bindingPrefix + ".selectedValue", "-");
                binding.multiMask = WidgetSurfaceValueTools.parseInt(properties.getProperty(bindingPrefix + ".multiMask"), 0);
                binding.sliderValue = WidgetSurfaceValueTools.parseFloat(properties.getProperty(bindingPrefix + ".sliderValue"), 0.0f);
                binding.colorValue = WidgetSurfaceValueTools.parseInt(properties.getProperty(bindingPrefix + ".colorValue"), 0);
                binding.toggleActive = false;
                binding.holdActive = false;
                binding.deleting = false;
                state.bindings.add(binding);
                state.nextBindingId = Math.max(state.nextBindingId, bindingId + 1);
                loadedBindings++;
            }
            hotkeyStates.put(controlId, state);
        }
        return loadedBindings;
    }

    public static void releaseActiveHoldHotkeys() {
        for (Map.Entry<String, HotkeyControlState> entry : hotkeyStates.entrySet()) {
            if (shouldSkipHotkeyPersistence(entry.getKey())) {
                continue;
            }
            HotkeyMeta meta = hotkeyMetas.getOrDefault(entry.getKey(), HotkeyMeta.EMPTY);
            for (HotkeyBinding binding : entry.getValue().bindings) {
                if (!binding.holdActive) {
                    continue;
                }
                applyHoldHotkeyBinding(entry.getKey(), meta, binding, false);
            }
        }
    }

    public static void reconcileHotkeyRuntimeMetas() {
        List<HotkeyMeta> runtimeMetas = hotkeyMetas.values().stream()
                .filter(meta -> meta != null && meta.applier != null)
                .toList();
        if (runtimeMetas.isEmpty()) {
            return;
        }
        for (Map.Entry<String, HotkeyMeta> entry : new ArrayList<>(hotkeyMetas.entrySet())) {
            HotkeyMeta meta = entry.getValue();
            if (meta == null || meta.applier != null) {
                continue;
            }
            String feature = normalizedHotkeyFeature(entry.getKey(), meta);
            for (HotkeyMeta runtime : runtimeMetas) {
                String runtimeFeature = HotkeyTextFormatter.cleanGroup(runtime.group).toLowerCase(Locale.ROOT)
                        + "/" + HotkeyTextFormatter.displayLabel(entry.getKey(), runtime.label).toLowerCase(Locale.ROOT);
                if (!feature.equals(runtimeFeature) || meta.valueType != runtime.valueType) {
                    continue;
                }
                hotkeyMetas.put(entry.getKey(), new HotkeyMeta(
                        meta.group == null || meta.group.isBlank() ? runtime.group : meta.group,
                        meta.label == null || meta.label.isBlank() ? runtime.label : meta.label,
                        meta.kind == null || meta.kind.isBlank() ? runtime.kind : meta.kind,
                        runtime.valueLabel, runtime.boolValue, runtime.valueType, runtime.selectedValue,
                        runtime.options == null ? null : runtime.options.clone(), runtime.multiMask,
                        runtime.sliderValue, runtime.colorValue, runtime.applier,
                        runtime.sliderFormatter, runtime.sliderParser));
                break;
            }
        }
    }

    public static boolean blocksGameMovementInput() {
        return blocksGameMovementInput;
    }

    public static void clearGameMovementInputBlock() {
        blocksGameMovementInput = false;
    }

    public static List<HotkeyHudEntry> hotkeyHudEntries() {
        syncHotkeyPhysicalState();
        List<HotkeyHudEntry> entries = new ArrayList<>();
        for (Map.Entry<String, HotkeyControlState> entry : hotkeyStates.entrySet()) {
            if (shouldSkipAllHotkeysEntry(entry.getKey())) {
                continue;
            }
            HotkeyMeta meta = hotkeyMetas.getOrDefault(entry.getKey(), HotkeyMeta.EMPTY);
            if (meta.label == null || meta.label.isBlank()) {
                continue;
            }
            for (HotkeyBinding binding : entry.getValue().bindings) {
                if (!binding.visible || !binding.bound() || !binding.active()) {
                    continue;
                }
                String group = meta.group == null || meta.group.isBlank() ? HotkeyTextFormatter.groupFromId(entry.getKey()) : meta.group;
                String name = HotkeyTextFormatter.hudName(group, HotkeyTextFormatter.displayLabel(entry.getKey(), meta.label));
                String key = entry.getKey() + "\u0000" + binding.keyName;
                entries.add(new HotkeyHudEntry(binding.valueLabel, name, key));
            }
        }
        entries.sort((left, right) -> left.name.compareToIgnoreCase(right.name));
        return entries;
    }

    public static String hotkeyBindingsForLabel(String label) {
        if (label == null || label.isBlank()) {
            return "";
        }
        String normalizedLabel = normalizeHotkeyLookup(label);
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, HotkeyControlState> entry : hotkeyStates.entrySet()) {
            if (shouldSkipAllHotkeysEntry(entry.getKey())) {
                continue;
            }
            HotkeyMeta meta = hotkeyMetas.getOrDefault(entry.getKey(), HotkeyMeta.EMPTY);
            String display = HotkeyTextFormatter.displayLabel(entry.getKey(), meta.label);
            if (!normalizeHotkeyLookup(display).equals(normalizedLabel)) {
                continue;
            }
            for (HotkeyBinding binding : entry.getValue().bindings) {
                if (!binding.visible || !binding.bound() || binding.keyName == null || binding.keyName.isBlank()
                        || "-".equals(binding.keyName)) {
                    continue;
                }
                if (!keys.contains(binding.keyName)) {
                    keys.add(binding.keyName);
                }
            }
        }
        return String.join(", ", keys);
    }

    private static String normalizeHotkeyLookup(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
    }

    public static void updateHotkeyState() {
        syncHotkeyPhysicalState();
    }

    void setInteractionClip(int x, int y, int w, int h) {
        clipInteraction = true;
        clipX = x;
        clipY = y;
        clipW = w;
        clipH = h;
    }

    void clearInteractionClip() {
        clipInteraction = false;
    }

    private InteractionClipState pushInteractionClip(int x, int y, int w, int h) {
        InteractionClipState previous = new InteractionClipState(clipInteraction, clipX, clipY, clipW, clipH);
        setInteractionClip(x, y, w, h);
        return previous;
    }

    private InteractionClipState clearInteractionClipTemporarily() {
        InteractionClipState previous = new InteractionClipState(clipInteraction, clipX, clipY, clipW, clipH);
        clearInteractionClip();
        return previous;
    }

    private void popInteractionClip(InteractionClipState previous) {
        if (previous.enabled) {
            setInteractionClip(previous.x, previous.y, previous.w, previous.h);
        } else {
            clearInteractionClip();
        }
    }

    void setRenderClip(int x, int y, int w, int h) {
        clipRender = true;
        renderClipX = x;
        renderClipY = y;
        renderClipW = w;
        renderClipH = h;
    }

    void clearRenderClip() {
        clipRender = false;
        renderClipStack.clear();
    }

    void setRenderOffset(float x, float y) {
        renderOffsetX = x;
        renderOffsetY = y;
    }

    void setRenderScale(float scale) {
        renderScale = Math.max(0.01f, scale);
    }

    void setLayoutAnimationsSuspended(boolean suspended) {
        this.suspendLayoutAnimations = suspended;
    }

    void setHotkeyPreregistering(boolean preregistering) {
        this.hotkeyPreregistering = preregistering;
    }

    void setInteractionLocked(boolean locked) {
        this.interactionLocked = locked;
    }

    Frame beginCard(GuiGraphics graphics, String scope, int originX, int originY, int x, int y, int w) {
        return beginCard(graphics, scope, originX, originY, x, y, w, y);
    }

    Frame beginCard(GuiGraphics graphics, String scope, int originX, int originY, int x, int y, int w, int stableY) {
        return beginCard(graphics, scope, null, originX, originY, x, y, w, stableY);
    }

    Frame beginCard(GuiGraphics graphics, String scope, String hotkeyGroup, int originX, int originY, int x, int y, int w, int stableY) {
        return beginCard(graphics, scope, hotkeyGroup, originX, originY, x, y, w, stableY,
                x, y, w, Integer.MAX_VALUE / 4);
    }

    Frame beginCard(GuiGraphics graphics, String scope, String hotkeyGroup, int originX, int originY, int x, int y, int w,
                    int stableY, int clipX, int clipY, int clipW, int clipH) {
        UiLayout layout = new UiLayout(originX, originY, x + CARD_PAD_X, y + CARD_CONTENT_TOP, stableY + CARD_CONTENT_TOP, w - CARD_PAD_X * 2);
        return new Frame(graphics, scope, hotkeyGroup, layout, true, clipX, clipY, clipW, clipH);
    }

    Frame measureCard(String scope, int originX, int originY, int x, int y, int w) {
        UiLayout layout = new UiLayout(originX, originY, x + CARD_PAD_X, y + CARD_CONTENT_TOP, y + CARD_CONTENT_TOP, w - CARD_PAD_X * 2);
        return new Frame(null, scope, null, layout, false, x, y, w, Integer.MAX_VALUE / 4);
    }

    Frame overlay(GuiGraphics graphics, String scope, int originX, int originY) {
        UiLayout layout = new UiLayout(originX, originY, 0, 0, 0, 0);
        return new Frame(graphics, scope, null, layout, true, 0, 0, Integer.MAX_VALUE / 4, Integer.MAX_VALUE / 4);
    }

    void clearCompactActionRowAnimation(String scope, String idKey) {
        String rowId = scope + "/compact-row/" + idKey;
        animationSystem.controlAnimations.remove(animationKey(rowId, "appear"));
        animationSystem.controlAnimations.remove(animationKey(rowId, "rowY"));
        animationSystem.controlAnimations.remove(animationKey(scope + "/compact-action/" + idKey, "hover"));
    }

    boolean selectField(GuiGraphics graphics, String scope, int originX, int originY, int x, int y, int w, String idKey, String value) {
        return selectField(graphics, scope, originX, originY, x, y, w, idKey, value, y, "", y);
    }

    boolean selectField(GuiGraphics graphics, String scope, int originX, int originY, int x, int y, int w, String idKey, String value, int idY) {
        return selectField(graphics, scope, originX, originY, x, y, w, idKey, value, idY, "", y);
    }

    boolean selectField(GuiGraphics graphics, String scope, int originX, int originY, int x, int y, int w,
                        String idKey, String value, int idY, String label, int labelY) {
        String id = controlId(scope, "select", idKey, originX, originY, x, idY);
        ControlTransition transition = controlTransition(id, y, SELECT_HEIGHT);
        int yDelta = transition.y - y;
        y = transition.y;
        labelY += yDelta;
        String[] options = WidgetSurfaceValueTools.selectOptions(value);
        String selected = widgetRegistry.selectValues.computeIfAbsent(id, key -> options[0]);
        selected = applyPendingHotkeySelect(id, selected, options);
        widgetRegistry.selectValues.put(id, selected);
        registerHotkeyArea(id, idKey, "Select", selected, null, x, y, w, SELECT_HEIGHT,
                HotkeyValueType.SELECT, selected, options, 0, 0.0f, 0);
        boolean hovered = hovered(x, y, w, SELECT_HEIGHT);
        boolean clicked = button(id, x, y, w, SELECT_HEIGHT);
        if (clicked) {
            boolean openNow = !id.equals(popupSystem.openSelectId);
            popupSystem.openSelectId = openNow ? id : null;
            if (openNow) {
                if (popupSystem.openSelectPopup != null && !id.equals(popupSystem.openSelectPopup.id)) {
                    closeSelectPopupVisual();
                }
                popupSystem.openMultiSelectId = null;
                closeMultiSelectPopupVisual();
                popupSystem.openColorId = null;
                closeColorPopupVisual();
                closeHotkeyPopups();
            }
        }
        boolean open = id.equals(popupSystem.openSelectId);
        float focus = controlAnim(id, "focus", open || hovered ? 1.0f : 0.0f);
        float previousAlpha = beginControlAlpha(transition);
        if (label != null && !label.isEmpty()) {
            text(graphics, label, x, labelY, SUBTLE);
        }
        Select.renderField(graphics, x, y, w, SELECT_HEIGHT, FIELD,
                lerpColor(themeColor(0xFFD4D9E8, BORDER), ACCENT, focus),
                lerpColor(MUTED, ACCENT_DARK, focus), renderAlpha);
        drawClippedText(graphics, selected, x + ITEM_SPACING_X, y + (SELECT_OPTION_HEIGHT - FONT_SIZE) / 2 - 1,
                w - ITEM_SPACING_X - 24, TEXT);
        endControlAlpha(previousAlpha);
        if (open) {
            popupSystem.pendingSelectPopup = new SelectPopup(id, x, y + SELECT_HEIGHT + SELECT_POPUP_GAP,
                    w, options, selected, null);
        }
        return clicked;
    }

    String comboField(GuiGraphics graphics, String scope, int originX, int originY, int x, int y, int w,
                      String idKey, String defaultValue, String values, int idY) {
        return comboField(graphics, scope, originX, originY, x, y, w, idKey, defaultValue, values, idY, null, "", y);
    }

    String comboField(GuiGraphics graphics, String scope, int originX, int originY, int x, int y, int w,
                      String idKey, String defaultValue, String values, int idY, HotkeyApplier applier) {
        return comboField(graphics, scope, originX, originY, x, y, w, idKey, defaultValue, values, idY, applier, "", y);
    }

    String comboField(GuiGraphics graphics, String scope, int originX, int originY, int x, int y, int w,
                      String idKey, String defaultValue, String values, int idY, HotkeyApplier applier,
                      String label, int labelY) {
        String id = controlId(scope, "combo", idKey, originX, originY, x, idY);
        ControlTransition transition = controlTransition(id, y, SELECT_HEIGHT);
        int yDelta = transition.y - y;
        y = transition.y;
        labelY += yDelta;
        String[] options = WidgetSurfaceValueTools.selectOptions(values);
        String selected = widgetRegistry.selectValues.computeIfAbsent(id, key -> WidgetSurfaceValueTools.selectDefault(defaultValue, options));
        if (!WidgetSurfaceValueTools.containsOption(selected, options)) {
            selected = WidgetSurfaceValueTools.selectDefault(defaultValue, options);
            widgetRegistry.selectValues.put(id, selected);
        }
        selected = applyPendingHotkeySelect(id, selected, options);
        widgetRegistry.selectValues.put(id, selected);
        registerHotkeyArea(id, idKey, "Combo", selected, null, x, y, w, SELECT_HEIGHT,
                HotkeyValueType.SELECT, selected, options, 0, 0.0f, 0, applier);
        boolean hovered = hovered(x, y, w, SELECT_HEIGHT);
        boolean clicked = button(id, x, y, w, SELECT_HEIGHT);
        if (clicked) {
            boolean openNow = !id.equals(popupSystem.openSelectId);
            popupSystem.openSelectId = openNow ? id : null;
            if (openNow) {
                if (popupSystem.openSelectPopup != null && !id.equals(popupSystem.openSelectPopup.id)) {
                    closeSelectPopupVisual();
                }
                popupSystem.openMultiSelectId = null;
                closeMultiSelectPopupVisual();
                popupSystem.openColorId = null;
                closeColorPopupVisual();
                closeHotkeyPopups();
            }
        }
        boolean open = id.equals(popupSystem.openSelectId);
        float focus = controlAnim(id, "focus", open || hovered ? 1.0f : 0.0f);
        float previousAlpha = beginControlAlpha(transition);
        if (label != null && !label.isEmpty()) {
            text(graphics, label, x, labelY, SUBTLE);
        }
        Select.renderField(graphics, x, y, w, SELECT_HEIGHT, FIELD,
                lerpColor(themeColor(0xFFD4D9E8, BORDER), ACCENT, focus),
                lerpColor(MUTED, ACCENT_DARK, focus), renderAlpha);
        drawClippedText(graphics, selected, x + ITEM_SPACING_X, y + (SELECT_OPTION_HEIGHT - FONT_SIZE) / 2 - 1,
                w - ITEM_SPACING_X - 24, TEXT);
        endControlAlpha(previousAlpha);
        if (open) {
            popupSystem.pendingSelectPopup = new SelectPopup(id, x, y + SELECT_HEIGHT + SELECT_POPUP_GAP,
                    w, options, selected, applier);
        }
        return selected;
    }

    int multiSelectField(GuiGraphics graphics, String scope, int originX, int originY, int x, int y, int w,
                         String idKey, int defaultMask, String values, int idY) {
        return multiSelectField(graphics, scope, originX, originY, x, y, w, idKey, defaultMask, values, idY, null, "", y);
    }

    int multiSelectField(GuiGraphics graphics, String scope, int originX, int originY, int x, int y, int w,
                         String idKey, int defaultMask, String values, int idY, HotkeyApplier applier) {
        return multiSelectField(graphics, scope, originX, originY, x, y, w, idKey, defaultMask, values, idY, applier, "", y);
    }

    int multiSelectField(GuiGraphics graphics, String scope, int originX, int originY, int x, int y, int w,
                         String idKey, int defaultMask, String values, int idY, HotkeyApplier applier,
                         String label, int labelY) {
        String id = controlId(scope, "multi", idKey, originX, originY, x, idY);
        ControlTransition transition = controlTransition(id, y, SELECT_HEIGHT);
        int yDelta = transition.y - y;
        y = transition.y;
        labelY += yDelta;
        String[] options = WidgetSurfaceValueTools.selectOptions(values);
        int allMask = WidgetSurfaceValueTools.optionMask(options.length);
        int selectedMask = widgetRegistry.multiSelectMasks.computeIfAbsent(id, key -> defaultMask & allMask) & allMask;
        selectedMask = applyPendingHotkeyMultiSelect(id, selectedMask, allMask);
        widgetRegistry.multiSelectMasks.put(id, selectedMask);
        String summary = WidgetSurfaceValueTools.multiSelectSummary(selectedMask, options, allMask);
        registerHotkeyArea(id, idKey, "Multi Select", summary, null, x, y, w, SELECT_HEIGHT,
                HotkeyValueType.MULTI_SELECT, summary, options, selectedMask, 0.0f, 0, applier);
        boolean hovered = hovered(x, y, w, SELECT_HEIGHT);
        boolean clicked = button(id, x, y, w, SELECT_HEIGHT);
        if (clicked) {
            boolean openNow = !id.equals(popupSystem.openMultiSelectId);
            popupSystem.openMultiSelectId = openNow ? id : null;
            if (openNow) {
                if (popupSystem.openMultiSelectPopup != null && !id.equals(popupSystem.openMultiSelectPopup.id)) {
                    closeMultiSelectPopupVisual();
                }
                popupSystem.openSelectId = null;
                closeSelectPopupVisual();
                popupSystem.openColorId = null;
                closeColorPopupVisual();
                closeHotkeyPopups();
            }
        }
        boolean open = id.equals(popupSystem.openMultiSelectId);
        float focus = controlAnim(id, "focus", open || hovered ? 1.0f : 0.0f);
        float previousAlpha = beginControlAlpha(transition);
        if (label != null && !label.isEmpty()) {
            text(graphics, label, x, labelY, SUBTLE);
        }
        Select.renderField(graphics, x, y, w, SELECT_HEIGHT, FIELD,
                lerpColor(themeColor(0xFFD4D9E8, BORDER), ACCENT, focus),
                lerpColor(MUTED, ACCENT_DARK, focus), renderAlpha);
        drawClippedText(graphics, WidgetSurfaceValueTools.multiSelectSummary(selectedMask, options, allMask), x + ITEM_SPACING_X,
                y + (SELECT_OPTION_HEIGHT - FONT_SIZE) / 2 - 1, w - ITEM_SPACING_X - 24, TEXT);
        endControlAlpha(previousAlpha);
        if (open) {
            popupSystem.pendingMultiSelectPopup = new MultiSelectPopup(id, x, y + SELECT_HEIGHT + SELECT_POPUP_GAP, w, options, selectedMask);
        }
        return selectedMask;
    }

    void renderPopup(GuiGraphics graphics) {
        animationSystem.activePopupAnimations.clear();
        if (popupSystem.pendingSettingsPopup != null) {
            SettingsPopup popup = popupSystem.pendingSettingsPopup;
            popupSystem.openSettingsPopup = popup;
            int h = settingsPopupHeight(popup.frame.cardHeight());
            PopupAnimation animation = beginPopupAnimation(graphics, PopupGeometry.settingsPopupId(popup.id), false);
            try {
                renderSettingsPopup(graphics, popup);
            } finally {
                endPopupAnimation(graphics);
            }
        } else {
            if (popupSystem.openSettingsPopup != null) {
                popupSystem.closingSettingsPopup = popupSystem.openSettingsPopup;
                startClosingPopupAnimation(PopupGeometry.settingsPopupId(popupSystem.openSettingsPopup.id));
            }
            popupSystem.openSettingsPopup = null;
        }

        if (popupSystem.pendingSelectPopup != null) {
            SelectPopup popup = popupSystem.pendingSelectPopup;
            popupSystem.openSelectPopup = popup;
            int h = popupHeight(popup.options.length);
            PopupAnimation animation = beginPopupAnimation(graphics, PopupGeometry.selectPopupId(popup.id), true);
            try {
                renderSelectPopup(graphics, popup);
            } finally {
                endPopupAnimation(graphics);
            }
        } else {
            if (popupSystem.openSelectPopup != null) {
                popupSystem.closingSelectPopup = popupSystem.openSelectPopup;
                startClosingPopupAnimation(PopupGeometry.selectPopupId(popupSystem.openSelectPopup.id));
            }
            popupSystem.openSelectPopup = null;
        }

        if (popupSystem.pendingMultiSelectPopup != null) {
            MultiSelectPopup popup = popupSystem.pendingMultiSelectPopup;
            popupSystem.openMultiSelectPopup = popup;
            int h = popupHeight(popup.options.length);
            PopupAnimation animation = beginPopupAnimation(graphics, PopupGeometry.multiSelectPopupId(popup.id), true);
            try {
                renderMultiSelectPopup(graphics, popup);
            } finally {
                endPopupAnimation(graphics);
            }
        } else {
            if (popupSystem.openMultiSelectPopup != null) {
                popupSystem.closingMultiSelectPopup = popupSystem.openMultiSelectPopup;
                startClosingPopupAnimation(PopupGeometry.multiSelectPopupId(popupSystem.openMultiSelectPopup.id));
            }
            popupSystem.openMultiSelectPopup = null;
        }

        if (popupSystem.pendingColorPopup != null) {
            ColorPopup popup = popupSystem.pendingColorPopup;
            popupSystem.openColorPopup = popup;
            PopupAnimation animation = beginPopupAnimation(graphics, PopupGeometry.colorPopupId(popup.id), false);
            try {
                renderColorPopup(graphics, popup);
            } finally {
                endPopupAnimation(graphics);
            }
        } else {
            if (popupSystem.openColorPopup != null) {
                popupSystem.closingColorPopup = popupSystem.openColorPopup;
                startClosingPopupAnimation(PopupGeometry.colorPopupId(popupSystem.openColorPopup.id));
            }
            popupSystem.openColorPopup = null;
        }

        if (popupSystem.pendingHotkeyPopup != null) {
            HotkeyPopup popup = popupSystem.pendingHotkeyPopup;
            popupSystem.openHotkeyPopup = popup;
            PopupAnimation animation = beginPopupAnimation(graphics, PopupGeometry.hotkeyPopupId(popup.id), false);
            try {
                renderHotkeyPopup(graphics, popup);
            } finally {
                endPopupAnimation(graphics);
            }
        } else {
            if (popupSystem.openHotkeyPopup != null) {
                popupSystem.closingHotkeyPopup = popupSystem.openHotkeyPopup;
                startClosingPopupAnimation(PopupGeometry.hotkeyPopupId(popupSystem.openHotkeyPopup.id));
            }
            popupSystem.openHotkeyPopup = null;
        }

        if (popupSystem.pendingHotkeyDetailPopup != null) {
            HotkeyDetailPopup popup = popupSystem.pendingHotkeyDetailPopup;
            popupSystem.openHotkeyDetailPopup = popup;
            PopupAnimation animation = beginPopupAnimation(graphics, PopupGeometry.hotkeyDetailPopupId(popup.controlId, popup.bindingId), false);
            try {
                renderHotkeyDetailPopup(graphics, popup);
            } finally {
                endPopupAnimation(graphics);
            }
        } else {
            if (popupSystem.openHotkeyDetailPopup != null) {
                popupSystem.closingHotkeyDetailPopup = popupSystem.openHotkeyDetailPopup;
                startClosingPopupAnimation(PopupGeometry.hotkeyDetailPopupId(popupSystem.openHotkeyDetailPopup.controlId, popupSystem.openHotkeyDetailPopup.bindingId));
            }
            popupSystem.openHotkeyDetailPopup = null;
        }

        if (popupSystem.pendingHotkeyValueSelectPopup != null) {
            HotkeyValueSelectPopup popup = popupSystem.pendingHotkeyValueSelectPopup;
            popupSystem.openHotkeyValueSelectPopup = popup;
            PopupAnimation animation = beginPopupAnimation(graphics, PopupGeometry.hotkeyValueSelectPopupId(popup.controlId, popup.bindingId), true);
            try {
                renderHotkeyValueSelectPopup(graphics, popup);
            } finally {
                endPopupAnimation(graphics);
            }
        } else {
            if (popupSystem.openHotkeyValueSelectPopup != null) {
                popupSystem.closingHotkeyValueSelectPopup = popupSystem.openHotkeyValueSelectPopup;
                startClosingPopupAnimation(PopupGeometry.hotkeyValueSelectPopupId(popupSystem.openHotkeyValueSelectPopup.controlId, popupSystem.openHotkeyValueSelectPopup.bindingId));
            }
            popupSystem.openHotkeyValueSelectPopup = null;
        }

        if (popupSystem.pendingHotkeyValueMultiPopup != null) {
            HotkeyValueMultiPopup popup = popupSystem.pendingHotkeyValueMultiPopup;
            popupSystem.openHotkeyValueMultiPopup = popup;
            PopupAnimation animation = beginPopupAnimation(graphics, PopupGeometry.hotkeyValueMultiPopupId(popup.controlId, popup.bindingId), true);
            try {
                renderHotkeyValueMultiPopup(graphics, popup);
            } finally {
                endPopupAnimation(graphics);
            }
        } else {
            if (popupSystem.openHotkeyValueMultiPopup != null) {
                popupSystem.closingHotkeyValueMultiPopup = popupSystem.openHotkeyValueMultiPopup;
                startClosingPopupAnimation(PopupGeometry.hotkeyValueMultiPopupId(popupSystem.openHotkeyValueMultiPopup.controlId, popupSystem.openHotkeyValueMultiPopup.bindingId));
            }
            popupSystem.openHotkeyValueMultiPopup = null;
        }

        if (allHotkeysOpen) {
            // All Hotkeys renders this popup after the modal so it stays above the table.
        } else if (popupSystem.pendingHotkeyValueColorPopup != null) {
            HotkeyValueColorPopup popup = popupSystem.pendingHotkeyValueColorPopup;
            popupSystem.openHotkeyValueColorPopup = popup;
            PopupAnimation animation = beginPopupAnimation(graphics, PopupGeometry.hotkeyValueColorPopupId(popup.controlId, popup.bindingId), false);
            try {
                renderHotkeyValueColorPopup(graphics, popup);
            } finally {
                endPopupAnimation(graphics);
            }
        } else {
            if (popupSystem.openHotkeyValueColorPopup != null) {
                popupSystem.closingHotkeyValueColorPopup = popupSystem.openHotkeyValueColorPopup;
                startClosingPopupAnimation(PopupGeometry.hotkeyValueColorPopupId(popupSystem.openHotkeyValueColorPopup.controlId, popupSystem.openHotkeyValueColorPopup.bindingId));
            }
            popupSystem.openHotkeyValueColorPopup = null;
        }

        if (popupSystem.pendingMenuPopup != null) {
            MenuPopup popup = popupSystem.pendingMenuPopup;
            popupSystem.openMenuPopup = popup;
            PopupAnimation animation = beginPopupAnimation(graphics, PopupGeometry.menuPopupId(popup.id), false);
            try {
                renderMenuPopup(graphics, popup);
            } finally {
                endPopupAnimation(graphics);
            }
        } else {
            if (popupSystem.openMenuPopup != null) {
                popupSystem.closingMenuPopup = popupSystem.openMenuPopup;
                startClosingPopupAnimation(PopupGeometry.menuPopupId(popupSystem.openMenuPopup.id));
            }
            popupSystem.openMenuPopup = null;
        }
        renderClosingPopups(graphics);
        renderAllHotkeysWindow(graphics);
        animationSystem.popupAnimations.keySet().removeIf(id -> !animationSystem.activePopupAnimations.contains(id));
    }

    private void renderSettingsPopup(GuiGraphics graphics, SettingsPopup popup) {
        int h = animatedPopupHeight(PopupGeometry.settingsPopupId(popup.id), settingsPopupHeight(popup.frame.cardHeight()));
        drawPopupSurface(graphics, popup.x, popup.y, popup.w, h, 8);
        Frame frame = beginSettingsFrame(graphics, popup.id, popup.x, popup.y, popup.w);
        popupSystem.renderingPopup = true;
        boolean clipped = pushRenderClip(popup.x, popup.y, popup.w, h);
        InteractionClipState previousInteractionClip = clearInteractionClipTemporarily();
        try {
            popup.content.render(frame);
        } finally {
            popInteractionClip(previousInteractionClip);
            popRenderClip(clipped);
            popupSystem.renderingPopup = false;
        }
    }

    private void renderSelectPopup(GuiGraphics graphics, SelectPopup popup) {
        int h = popupHeight(popup.options.length);
        drawPopupSurface(graphics, popup.x, popup.y, popup.w, h, 7);
        for (int i = 0; i < popup.options.length; i++) {
            int optionY = popup.y + SELECT_POPUP_PAD_Y + i * (SELECT_OPTION_HEIGHT + SELECT_OPTION_GAP);
            String option = popup.options[i];
            boolean selected = option.equals(popup.selected);
            boolean hovered = PopupGeometry.contains(mouseX, mouseY, popup.x, optionY, popup.w, SELECT_OPTION_HEIGHT);
            String optionId = "select-option/" + popup.id + "/" + i;
            float previousAlpha = beginSelectOptionAnimation(graphics, i);
            try {
                drawSelectOptionBackground(graphics, optionId, popup.x + 3, optionY, popup.w - 6, selected, hovered);
                text(graphics, option, popup.x + ITEM_SPACING_X,
                        optionY + (SELECT_OPTION_HEIGHT - FONT_SIZE) / 2 - 2,
                        selectOptionTextColor(optionId, selected, hovered));
            } finally {
                endSelectOptionAnimation(graphics, previousAlpha);
            }
        }
    }

    private void renderMultiSelectPopup(GuiGraphics graphics, MultiSelectPopup popup) {
        int h = popupHeight(popup.options.length);
        drawPopupSurface(graphics, popup.x, popup.y, popup.w, h, 7);
        for (int i = 0; i < popup.options.length; i++) {
            int optionY = popup.y + SELECT_POPUP_PAD_Y + i * (SELECT_OPTION_HEIGHT + SELECT_OPTION_GAP);
            String option = popup.options[i];
            boolean selected = (popup.selectedMask & (1 << i)) != 0;
            boolean hovered = PopupGeometry.contains(mouseX, mouseY, popup.x, optionY, popup.w, SELECT_OPTION_HEIGHT);
            String optionId = "multi-option/" + popup.id + "/" + i;
            float previousAlpha = beginSelectOptionAnimation(graphics, i);
            try {
                drawSelectOptionBackground(graphics, optionId, popup.x + 3, optionY, popup.w - 6, selected, hovered);
                drawCheckbox(graphics, popup.x + 7, optionY + 2, selected,
                        controlAnim(optionId, "checked", selected ? 1.0f : 0.0f));
                text(graphics, option, popup.x + 32,
                        optionY + (SELECT_OPTION_HEIGHT - FONT_SIZE) / 2 - 2,
                        selectOptionTextColor(optionId, selected, hovered));
            } finally {
                endSelectOptionAnimation(graphics, previousAlpha);
            }
        }
    }

    private float beginSelectOptionAnimation(GuiGraphics graphics, int index) {
        float previousAlpha = renderAlpha;
        float reveal = selectOptionReveal(index);
        graphics.pose().pushPose();
        graphics.pose().translate(0.0f, 4.0f * (1.0f - reveal), 0.0f);
        renderAlpha = previousAlpha * reveal;
        ShapeRenderer.setGlobalAlpha(renderAlpha);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, renderAlpha);
        return previousAlpha;
    }

    private void endSelectOptionAnimation(GuiGraphics graphics, float previousAlpha) {
        renderAlpha = previousAlpha;
        ShapeRenderer.setGlobalAlpha(previousAlpha);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, previousAlpha);
        graphics.pose().popPose();
    }

    private static float selectOptionReveal(int index) {
        float delay = Math.min(0.32f, index * 0.045f);
        float progress = clamp01((renderAlpha - delay) / Math.max(0.001f, 1.0f - delay));
        return progress * progress * (3.0f - 2.0f * progress);
    }

    private void drawSelectOptionBackground(GuiGraphics graphics, String id, int x, int y, int w,
                                            boolean selected, boolean hovered) {
        float target = selected ? 1.0f : hovered ? 0.72f : 0.0f;
        float active = controlAnim(id, "bg", target);
        if (active <= 0.01f) {
            return;
        }
        int color = lerpColor(themeColor(0x00F4F5FA, 0x00222734), selected ? ACCENT_SOFT : themeColor(0xFFF4F5FA, FIELD), active);
        Select.renderOptionBackground(graphics, x, y, w, SELECT_OPTION_HEIGHT, color, active);
    }

    private int selectOptionTextColor(String id, boolean selected, boolean hovered) {
        float target = selected ? 1.0f : hovered ? 0.28f : 0.0f;
        return lerpColor(TEXT, ACCENT_DARK, controlAnim(id, "text", target));
    }

    private void renderClosingPopups(GuiGraphics graphics) {
        if (popupSystem.closingSettingsPopup != null) {
            String id = PopupGeometry.settingsPopupId(popupSystem.closingSettingsPopup.id);
            if (shouldDropClosingPopup(id)) {
                popupSystem.closingSettingsPopup = null;
            } else {
                PopupAnimation animation = beginClosingPopupAnimation(graphics, id, false);
                try {
                    renderSettingsPopup(graphics, popupSystem.closingSettingsPopup);
                } finally {
                    endPopupAnimation(graphics);
                }
                if (animation.alpha <= 0.0f) {
                    popupSystem.closingSettingsPopup = null;
                    clearPopupAnimation(id);
                }
            }
        }
        if (popupSystem.closingSelectPopup != null) {
            String id = PopupGeometry.selectPopupId(popupSystem.closingSelectPopup.id);
            if (shouldDropClosingPopup(id)) {
                popupSystem.closingSelectPopup = null;
            } else {
                PopupAnimation animation = beginClosingPopupAnimation(graphics, id, true);
                try {
                    renderSelectPopup(graphics, popupSystem.closingSelectPopup);
                } finally {
                    endPopupAnimation(graphics);
                }
                if (animation.alpha <= 0.0f) {
                    popupSystem.closingSelectPopup = null;
                    clearPopupAnimation(id);
                }
            }
        }
        if (popupSystem.closingMultiSelectPopup != null) {
            String id = PopupGeometry.multiSelectPopupId(popupSystem.closingMultiSelectPopup.id);
            if (shouldDropClosingPopup(id)) {
                popupSystem.closingMultiSelectPopup = null;
            } else {
                PopupAnimation animation = beginClosingPopupAnimation(graphics, id, true);
                try {
                    renderMultiSelectPopup(graphics, popupSystem.closingMultiSelectPopup);
                } finally {
                    endPopupAnimation(graphics);
                }
                if (animation.alpha <= 0.0f) {
                    popupSystem.closingMultiSelectPopup = null;
                    clearPopupAnimation(id);
                }
            }
        }
        if (popupSystem.closingColorPopup != null) {
            String id = PopupGeometry.colorPopupId(popupSystem.closingColorPopup.id);
            if (shouldDropClosingPopup(id)) {
                popupSystem.closingColorPopup = null;
            } else {
                PopupAnimation animation = beginClosingPopupAnimation(graphics, id, false);
                try {
                    renderColorPopup(graphics, popupSystem.closingColorPopup);
                } finally {
                    endPopupAnimation(graphics);
                }
                if (animation.alpha <= 0.0f) {
                    popupSystem.closingColorPopup = null;
                    clearPopupAnimation(id);
                }
            }
        }
        if (popupSystem.closingHotkeyPopup != null) {
            String id = PopupGeometry.hotkeyPopupId(popupSystem.closingHotkeyPopup.id);
            if (shouldDropClosingPopup(id)) {
                popupSystem.closingHotkeyPopup = null;
            } else {
                PopupAnimation animation = beginClosingPopupAnimation(graphics, id, false);
                try {
                    renderHotkeyPopup(graphics, popupSystem.closingHotkeyPopup);
                } finally {
                    endPopupAnimation(graphics);
                }
                if (animation.alpha <= 0.0f) {
                    popupSystem.closingHotkeyPopup = null;
                    clearPopupAnimation(id);
                }
            }
        }
        if (popupSystem.closingHotkeyDetailPopup != null) {
            String id = PopupGeometry.hotkeyDetailPopupId(popupSystem.closingHotkeyDetailPopup.controlId, popupSystem.closingHotkeyDetailPopup.bindingId);
            if (shouldDropClosingPopup(id)) {
                popupSystem.closingHotkeyDetailPopup = null;
            } else {
                PopupAnimation animation = beginClosingPopupAnimation(graphics, id, false);
                try {
                    renderHotkeyDetailPopup(graphics, popupSystem.closingHotkeyDetailPopup);
                } finally {
                    endPopupAnimation(graphics);
                }
                if (animation.alpha <= 0.0f) {
                    popupSystem.closingHotkeyDetailPopup = null;
                    clearPopupAnimation(id);
                }
            }
        }
        if (popupSystem.closingHotkeyValueSelectPopup != null) {
            String id = PopupGeometry.hotkeyValueSelectPopupId(popupSystem.closingHotkeyValueSelectPopup.controlId, popupSystem.closingHotkeyValueSelectPopup.bindingId);
            if (shouldDropClosingPopup(id)) {
                popupSystem.closingHotkeyValueSelectPopup = null;
            } else {
                PopupAnimation animation = beginClosingPopupAnimation(graphics, id, true);
                try {
                    renderHotkeyValueSelectPopup(graphics, popupSystem.closingHotkeyValueSelectPopup);
                } finally {
                    endPopupAnimation(graphics);
                }
                if (animation.alpha <= 0.0f) {
                    popupSystem.closingHotkeyValueSelectPopup = null;
                    clearPopupAnimation(id);
                }
            }
        }
        if (popupSystem.closingHotkeyValueMultiPopup != null) {
            String id = PopupGeometry.hotkeyValueMultiPopupId(popupSystem.closingHotkeyValueMultiPopup.controlId, popupSystem.closingHotkeyValueMultiPopup.bindingId);
            if (shouldDropClosingPopup(id)) {
                popupSystem.closingHotkeyValueMultiPopup = null;
            } else {
                PopupAnimation animation = beginClosingPopupAnimation(graphics, id, true);
                try {
                    renderHotkeyValueMultiPopup(graphics, popupSystem.closingHotkeyValueMultiPopup);
                } finally {
                    endPopupAnimation(graphics);
                }
                if (animation.alpha <= 0.0f) {
                    popupSystem.closingHotkeyValueMultiPopup = null;
                    clearPopupAnimation(id);
                }
            }
        }
        if (popupSystem.closingHotkeyValueColorPopup != null) {
            String id = PopupGeometry.hotkeyValueColorPopupId(popupSystem.closingHotkeyValueColorPopup.controlId, popupSystem.closingHotkeyValueColorPopup.bindingId);
            if (shouldDropClosingPopup(id)) {
                popupSystem.closingHotkeyValueColorPopup = null;
            } else {
                PopupAnimation animation = beginClosingPopupAnimation(graphics, id, false);
                try {
                    renderHotkeyValueColorPopup(graphics, popupSystem.closingHotkeyValueColorPopup);
                } finally {
                    endPopupAnimation(graphics);
                }
                if (animation.alpha <= 0.0f) {
                    popupSystem.closingHotkeyValueColorPopup = null;
                    clearPopupAnimation(id);
                }
            }
        }
        if (popupSystem.closingMenuPopup != null) {
            String id = PopupGeometry.menuPopupId(popupSystem.closingMenuPopup.id);
            if (shouldDropClosingPopup(id)) {
                popupSystem.closingMenuPopup = null;
            } else {
                PopupAnimation animation = beginClosingPopupAnimation(graphics, id, false);
                try {
                    renderMenuPopup(graphics, popupSystem.closingMenuPopup);
                } finally {
                    endPopupAnimation(graphics);
                }
                if (animation.alpha <= 0.0f) {
                    popupSystem.closingMenuPopup = null;
                    clearPopupAnimation(id);
                }
            }
        }
    }

    boolean button(String id, int x, int y, int w, int h) {
        if (interactionLocked || conditionalInteractionDisabled) {
            return false;
        }
        if (!click || clickButton != 0 || clickConsumed) {
            return false;
        }
        if (!popupSystem.renderingColorPopup && insideOpenColorPopup(clickX, clickY)) {
            return false;
        }
        if (insideOpenHotkeyPopup(clickX, clickY) || insideOpenHotkeyDetailPopup(clickX, clickY)) {
            return false;
        }
        if (insideOpenHotkeyValuePopup(clickX, clickY)) {
            return false;
        }
        if (!popupSystem.renderingPopup && insideOpenMenuPopup(clickX, clickY)) {
            return false;
        }
        if (insideOpenMultiSelectPopup(clickX, clickY)) {
            return false;
        }
        if (!popupSystem.renderingPopup && insideOpenSettingsPopup(clickX, clickY)) {
            return false;
        }
        if (!insideInteractionClip(clickX, clickY)) {
            return false;
        }
        if (!PopupGeometry.contains(clickX, clickY, x, y, w, h)) {
            return false;
        }
        clickConsumed = true;
        return true;
    }

    private boolean rightClicked(int x, int y, int w, int h) {
        if (interactionLocked || conditionalInteractionDisabled) {
            return false;
        }
        if (!click || clickButton != 1 || clickConsumed) {
            return false;
        }
        if (!popupSystem.renderingColorPopup && insideOpenColorPopup(clickX, clickY)) {
            return false;
        }
        if (insideOpenHotkeyPopup(clickX, clickY) || insideOpenHotkeyDetailPopup(clickX, clickY)) {
            return false;
        }
        if (insideOpenHotkeyValuePopup(clickX, clickY)) {
            return false;
        }
        if (!popupSystem.renderingPopup && insideOpenMenuPopup(clickX, clickY)) {
            return false;
        }
        if (insideOpenMultiSelectPopup(clickX, clickY)) {
            return false;
        }
        if (!popupSystem.renderingPopup && insideOpenSettingsPopup(clickX, clickY)) {
            return false;
        }
        if (!insideInteractionClip(clickX, clickY)) {
            return false;
        }
        if (!PopupGeometry.contains(clickX, clickY, x, y, w, h)) {
            return false;
        }
        clickConsumed = true;
        return true;
    }

    boolean hovered(int x, int y, int w, int h) {
        if (interactionLocked || conditionalInteractionDisabled) {
            return false;
        }
        if (!popupSystem.renderingColorPopup && insideOpenColorPopup(mouseX, mouseY)) {
            return false;
        }
        if (insideOpenHotkeyPopup(mouseX, mouseY) || insideOpenHotkeyDetailPopup(mouseX, mouseY)) {
            return false;
        }
        if (insideOpenHotkeyValuePopup(mouseX, mouseY)) {
            return false;
        }
        if (!popupSystem.renderingPopup && insideOpenMenuPopup(mouseX, mouseY)) {
            return false;
        }
        if (insideOpenMultiSelectPopup(mouseX, mouseY)) {
            return false;
        }
        if (!popupSystem.renderingPopup && insideOpenSettingsPopup(mouseX, mouseY)) {
            return false;
        }
        if (!insideInteractionClip(mouseX, mouseY)) {
            return false;
        }
        return PopupGeometry.contains(mouseX, mouseY, x, y, w, h);
    }

    boolean hasClick() {
        return click;
    }

    int clickButton() {
        return clickButton;
    }

    boolean clickConsumed() {
        return clickConsumed;
    }

    void consumeClick() {
        clickConsumed = true;
    }

    boolean insideAnyPopup(double px, double py) {
        return insideOpenColorPopup(px, py)
                || insideOpenHotkeyPopup(px, py)
                || insideOpenHotkeyDetailPopup(px, py)
                || insideOpenHotkeyValuePopup(px, py)
                || insideOpenMenuPopup(px, py)
                || insideOpenMultiSelectPopup(px, py)
                || insideOpenSelectPopup(px, py)
                || insideOpenSettingsPopup(px, py);
    }

    boolean hasOpenSettingsPopup() {
        return popupSystem.openSettingsPopup != null;
    }

    void settingsPopup(String id, int triggerX, int triggerY, int triggerW, int triggerH,
                       int popupX, int popupY, PopupContent content) {
        boolean open = id.equals(popupSystem.openSettingsId);
        if (rightClicked(triggerX, triggerY, triggerW, triggerH)) {
            boolean openNow = !open;
            popupSystem.openSettingsId = openNow ? id : null;
            if (openNow && popupSystem.openSettingsPopup != null && !id.equals(popupSystem.openSettingsPopup.id)) {
                closeSettingsPopupVisual();
            }
            popupSystem.openColorId = null;
            closeColorPopupVisual();
            popupSystem.openSelectId = null;
            closeSelectPopupVisual();
            popupSystem.openMultiSelectId = null;
            closeMultiSelectPopupVisual();
            closeHotkeyPopups();
            open = openNow;
        }
        if (open && content != null) {
            Frame frame = measureSettingsFrame(id, popupX, popupY, SETTINGS_POPUP_W);
            content.render(frame);
            popupSystem.pendingSettingsPopup = new SettingsPopup(id, popupX, popupY, SETTINGS_POPUP_W, frame, content);
        }
    }

    double clickX() {
        return clickX;
    }

    double clickY() {
        return clickY;
    }

    final class Frame {
        private final GuiGraphics graphics;
        private final String scope;
        private final String hotkeyGroup;
        private final UiLayout layout;
        private final boolean render;
        private final int clipX;
        private final int clipY;
        private final int clipW;
        private final int clipH;
        private int lastRowY;
        private String lastInputTextId;

        private Frame(GuiGraphics graphics, String scope, String hotkeyGroup, UiLayout layout, boolean render,
                      int clipX, int clipY, int clipW, int clipH) {
            this.graphics = graphics;
            this.scope = scope;
            this.hotkeyGroup = HotkeyTextFormatter.cleanGroup(hotkeyGroup);
            this.layout = layout;
            this.render = render;
            this.clipX = clipX;
            this.clipY = clipY;
            this.clipW = clipW;
            this.clipH = clipH;
        }

        private void registerHotkeyArea(String id, String label, String kind, String valueLabel, Boolean boolValue,
                                        int x, int y, int w, int h) {
            WidgetSurface.this.registerHotkeyArea(id, label, kind, valueLabel, boolValue, x, y, w, h,
                    boolValue == null ? HotkeyValueType.LABEL : HotkeyValueType.BOOL,
                    valueLabel, null, 0, 0.0f, 0, null, null, null, hotkeyGroup, render);
        }

        private void registerHotkeyArea(String id, String label, String kind, String valueLabel, Boolean boolValue,
                                        int x, int y, int w, int h, HotkeyApplier applier) {
            WidgetSurface.this.registerHotkeyArea(id, label, kind, valueLabel, boolValue, x, y, w, h,
                    boolValue == null ? HotkeyValueType.LABEL : HotkeyValueType.BOOL,
                    valueLabel, null, 0, 0.0f, 0, applier, null, null, hotkeyGroup, render);
        }

        private void registerHotkeyArea(String id, String label, String kind, String valueLabel, Boolean boolValue,
                                        int x, int y, int w, int h, HotkeyValueType valueType, String selectedValue,
                                        String[] options, int multiMask, float sliderValue, int colorValue) {
            WidgetSurface.this.registerHotkeyArea(id, label, kind, valueLabel, boolValue, x, y, w, h, valueType,
                    selectedValue, options, multiMask, sliderValue, colorValue, null, null, null, hotkeyGroup,
                    render);
        }

        private void registerHotkeyArea(String id, String label, String kind, String valueLabel, Boolean boolValue,
                                        int x, int y, int w, int h, HotkeyValueType valueType, String selectedValue,
                                        String[] options, int multiMask, float sliderValue, int colorValue,
                                        HotkeyApplier applier) {
            WidgetSurface.this.registerHotkeyArea(id, label, kind, valueLabel, boolValue, x, y, w, h, valueType,
                    selectedValue, options, multiMask, sliderValue, colorValue, applier, null, null, hotkeyGroup,
                    render);
        }

        private void registerHotkeyArea(String id, String label, String kind, String valueLabel, Boolean boolValue,
                                        int x, int y, int w, int h, HotkeyValueType valueType, String selectedValue,
                                        String[] options, int multiMask, float sliderValue, int colorValue,
                                        HotkeyApplier applier, Function<Float, String> sliderFormatter) {
            WidgetSurface.this.registerHotkeyArea(id, label, kind, valueLabel, boolValue, x, y, w, h, valueType,
                    selectedValue, options, multiMask, sliderValue, colorValue, applier, sliderFormatter, null,
                    hotkeyGroup, render);
        }

        private void registerHotkeyArea(String id, String label, String kind, String valueLabel, Boolean boolValue,
                                        int x, int y, int w, int h, HotkeyValueType valueType, String selectedValue,
                                        String[] options, int multiMask, float sliderValue, int colorValue,
                                        HotkeyApplier applier, Function<Float, String> sliderFormatter,
                                        Function<String, Float> sliderParser) {
            WidgetSurface.this.registerHotkeyArea(id, label, kind, valueLabel, boolValue, x, y, w, h, valueType,
                    selectedValue, options, multiMask, sliderValue, colorValue, applier, sliderFormatter,
                    sliderParser, hotkeyGroup, render);
        }

        boolean moduleToggle(Module module) {
            int x = layout.x;
            int y = layout.nextFrame();
            int idY = layout.idY();
            int w = layout.w;
            String id = "module/" + module.id();
            boolean currentValue = module.enabled();
            boolean value = render ? applyPendingHotkeyBool(id, currentValue) : currentValue;
            boolean hotkeyChanged = render && value != currentValue;
            registerHotkeyArea(id, "Enable", "Module", onOff(value), value, x, y, w, FRAME_HEIGHT,
                    HotkeyApplier.forBoolean(enabled -> {
                        if (enabled) {
                            com.xerion.common.XerionCoreHolder.get()
                                    .moduleManager().enable(module.id());
                        } else {
                            com.xerion.common.XerionCoreHolder.get()
                                    .moduleManager().disable(module.id());
                        }
                    }));
            boolean clicked = render && WidgetSurface.this.button(id, x, y, w, FRAME_HEIGHT);
            if (clicked && value != currentValue) {
                NotificationCenter.module(module.displayName(), value);
            }
            if (render) {
                drawCheckbox(graphics, x, y, value, controlAnim(id, "checked", value ? 1.0f : 0.0f));
                int labelWidth = Math.max(0, w - 26);
                drawClippedText(graphics, module.displayName(), x + 26, y, labelWidth, TEXT);
                int suffixX = x + 26 + Math.min(textWidth(module.displayName()), labelWidth) + 6;
                if (suffixX + textWidth("[-]") < x + w - 2) {
                    text(graphics, "[-]", suffixX, y, MUTED);
                }
            }
            return clicked || hotkeyChanged;
        }

        boolean checkbox(String label, boolean defaultValue) {
            int x = layout.x;
            int y = layout.nextFrame();
            int idY = layout.idY();
            int w = layout.w;
            String id = controlId(scope, "check", label, layout.originX, layout.originY, x, idY);
            ControlTransition transition = render ? controlTransition(id, y, FRAME_HEIGHT) : new ControlTransition(y, 1.0f);
            y = transition.y;
            boolean value = widgetRegistry.boolValues.computeIfAbsent(id, key -> defaultValue);
            if (render) {
                value = applyPendingHotkeyBool(id, value);
                widgetRegistry.boolValues.put(id, value);
            }
            if (render) {
                registerHotkeyArea(id, label, "Checkbox", onOff(value), value, x, y, w, FRAME_HEIGHT);
            }
            if (render && WidgetSurface.this.button(id, x, y, w, FRAME_HEIGHT)) {
                value = !value;
                widgetRegistry.boolValues.put(id, value);
                notifyToggle(label, value);
            }
            if (render) {
                float previousAlpha = beginControlAlpha(transition);
                drawCheckbox(graphics, x, y, value, controlAnim(id, "checked", value ? 1.0f : 0.0f));
                drawClippedText(graphics, label, x + 26, y, Math.max(0, w - 26), TEXT);
                endControlAlpha(previousAlpha);
            }
            return value;
        }

        boolean controlledCheckbox(String label, boolean value) {
            return controlledCheckbox(label, value, null);
        }

        boolean controlledCheckbox(String label, boolean value, Consumer<Boolean> applier) {
            int x = layout.x;
            int y = layout.nextFrame();
            int idY = layout.idY();
            int w = layout.w;
            String id = controlId(scope, "check", label, layout.originX, layout.originY, x, idY);
            ControlTransition transition = render ? controlTransition(id, y, FRAME_HEIGHT) : new ControlTransition(y, 1.0f);
            y = transition.y;
            if (render) {
                value = applyPendingHotkeyBool(id, value);
            }
            widgetRegistry.boolValues.put(id, value);
            registerHotkeyArea(id, label, "Checkbox", onOff(value), value, x, y, w, FRAME_HEIGHT,
                    HotkeyApplier.forBoolean(applier));
            if (render && WidgetSurface.this.button(id, x, y, w, FRAME_HEIGHT)) {
                value = !value;
                widgetRegistry.boolValues.put(id, value);
                notifyToggle(label, value);
            }
            if (render) {
                float previousAlpha = beginControlAlpha(transition);
                drawCheckbox(graphics, x, y, value, controlAnim(id, "checked", value ? 1.0f : 0.0f));
                drawClippedText(graphics, label, x + 26, y, Math.max(0, w - 26), TEXT);
                endControlAlpha(previousAlpha);
            }
            return value;
        }

        boolean checkboxWithSettings(String label, boolean defaultValue, PopupContent content) {
            int x = layout.x;
            int y = layout.nextFrame();
            int idY = layout.idY();
            int w = layout.w;
            int gearX = x + w - GEAR_BUTTON_SIZE;
            int toggleW = Math.max(0, gearX - x - 6);
            int labelWidth = Math.max(0, gearX - (x + 26) - 8);
            String id = controlId(scope, "check", label, layout.originX, layout.originY, x, idY);
            ControlTransition transition = render ? controlTransition(id, y, FRAME_HEIGHT) : new ControlTransition(y, 1.0f);
            y = transition.y;
            gearX = x + w - GEAR_BUTTON_SIZE;
            boolean value = widgetRegistry.boolValues.computeIfAbsent(id, key -> defaultValue);
            if (render) {
                value = applyPendingHotkeyBool(id, value);
                widgetRegistry.boolValues.put(id, value);
            }
            if (render) {
                registerHotkeyArea(id, label, "Checkbox", onOff(value), value, x, y, toggleW, FRAME_HEIGHT);
            }
            boolean gearClickArea = render && PopupGeometry.contains(clickX, clickY, gearX - 2, y - 2,
                    GEAR_BUTTON_SIZE + 4, GEAR_BUTTON_SIZE + 4);
            if (render && !gearClickArea && WidgetSurface.this.button(id, x, y, toggleW, FRAME_HEIGHT)) {
                value = !value;
                widgetRegistry.boolValues.put(id, value);
                notifyToggle(label, value);
            }
            if (render) {
                float previousAlpha = beginControlAlpha(transition);
                drawCheckbox(graphics, x, y, value, controlAnim(id, "checked", value ? 1.0f : 0.0f));
                drawClippedText(graphics, label, x + 26, y, labelWidth, TEXT);
                drawGearButton(graphics, id, gearX, y, content);
                endControlAlpha(previousAlpha);
            }
            return value;
        }

        boolean controlledCheckboxWithSettings(String label, boolean value, PopupContent content) {
            return controlledCheckboxWithSettings(label, value, content, null);
        }

        boolean controlledCheckboxWithSettings(String label, boolean value, PopupContent content, Consumer<Boolean> applier) {
            int x = layout.x;
            int y = layout.nextFrame();
            int idY = layout.idY();
            int w = layout.w;
            int gearX = x + w - GEAR_BUTTON_SIZE;
            int toggleW = Math.max(0, gearX - x - 6);
            int labelWidth = Math.max(0, gearX - (x + 26) - 8);
            String id = controlId(scope, "check", label, layout.originX, layout.originY, x, idY);
            ControlTransition transition = render ? controlTransition(id, y, FRAME_HEIGHT) : new ControlTransition(y, 1.0f);
            y = transition.y;
            gearX = x + w - GEAR_BUTTON_SIZE;
            if (render) {
                value = applyPendingHotkeyBool(id, value);
            }
            widgetRegistry.boolValues.put(id, value);
            registerHotkeyArea(id, label, "Checkbox", onOff(value), value, x, y, toggleW, FRAME_HEIGHT,
                    HotkeyApplier.forBoolean(applier));
            boolean gearClickArea = render && PopupGeometry.contains(clickX, clickY, gearX - 2, y - 2,
                    GEAR_BUTTON_SIZE + 4, GEAR_BUTTON_SIZE + 4);
            if (render && !gearClickArea && WidgetSurface.this.button(id, x, y, toggleW, FRAME_HEIGHT)) {
                value = !value;
                widgetRegistry.boolValues.put(id, value);
                notifyToggle(label, value);
            }
            if (render) {
                float previousAlpha = beginControlAlpha(transition);
                drawCheckbox(graphics, x, y, value, controlAnim(id, "checked", value ? 1.0f : 0.0f));
                drawClippedText(graphics, label, x + 26, y, labelWidth, TEXT);
                drawGearButton(graphics, id, gearX, y, content);
                endControlAlpha(previousAlpha);
            }
            return value;
        }

        boolean controlledCheckboxWithColor(String label, boolean value, int color,
                                            Consumer<Boolean> applier, Consumer<Integer> colorApplier) {
            return controlledCheckboxWithColor(label, label, value, color, applier, colorApplier, true);
        }

        boolean controlledCheckboxWithColor(String label, String idKey, boolean value, int color,
                                            Consumer<Boolean> applier, Consumer<Integer> colorApplier) {
            return controlledCheckboxWithColor(label, idKey, value, color, applier, colorApplier, true);
        }

        boolean controlledCheckboxWithColor(String label, String idKey, boolean value, int color,
                                            Consumer<Boolean> applier, Consumer<Integer> colorApplier,
                                            boolean activeAlpha) {
            int x = layout.x;
            int y = layout.nextFrame();
            int idY = layout.idY();
            int w = layout.w;
            int buttonX = x + w - COLOR_BUTTON_W;
            int toggleW = Math.max(0, buttonX - x - 6);
            int labelWidth = Math.max(0, buttonX - (x + 26) - 8);
            String id = controlId(scope, "checkColor", idKey, layout.originX, layout.originY, x, idY);
            String colorId = id + "/color";
            ControlTransition transition = render ? controlTransition(id, y, FRAME_HEIGHT) : new ControlTransition(y, 1.0f);
            y = transition.y;
            buttonX = x + w - COLOR_BUTTON_W;
            if (render) {
                value = applyPendingHotkeyBool(id, value);
            }
            widgetRegistry.boolValues.put(id, value);
            registerHotkeyArea(id, label, "Checkbox", onOff(value), value, x, y, toggleW, FRAME_HEIGHT,
                    HotkeyApplier.forBoolean(applier));
            if (!render) {
                return value;
            }
            boolean colorClickArea = PopupGeometry.contains(clickX, clickY, buttonX, y, COLOR_BUTTON_W, FRAME_HEIGHT);
            if (!colorClickArea && WidgetSurface.this.button(id, x, y, toggleW, FRAME_HEIGHT)) {
                value = !value;
                widgetRegistry.boolValues.put(id, value);
                notifyToggle(label, value);
            }

            int defaultColor = color;
            color = widgetRegistry.colorValues.computeIfAbsent(colorId, key -> defaultColor);
            color = applyPendingHotkeyColor(colorId, color);
            widgetRegistry.colorValues.put(colorId, color);
            if (colorApplier != null) {
                colorApplier.accept(color);
            }
            registerHotkeyArea(colorId, label + " Color", "Color", colorHex(color), null,
                    buttonX, y, COLOR_BUTTON_W, FRAME_HEIGHT, HotkeyValueType.COLOR, colorHex(color),
                    null, 0, 0.0f, color, HotkeyApplier.forColor(colorApplier));

            boolean open = colorId.equals(popupSystem.openColorId);
            boolean hovered = hovered(buttonX, y, COLOR_BUTTON_W, FRAME_HEIGHT);
            if (WidgetSurface.this.button(colorId + "/button", buttonX, y, COLOR_BUTTON_W, FRAME_HEIGHT)) {
                boolean openNow = !open;
                popupSystem.openColorId = openNow ? colorId : null;
                if (openNow && popupSystem.openColorPopup != null && !colorId.equals(popupSystem.openColorPopup.id)) {
                    closeColorPopupVisual();
                }
                popupSystem.openSelectId = null;
                closeSelectPopupVisual();
                popupSystem.openMultiSelectId = null;
                closeMultiSelectPopupVisual();
                closeHotkeyPopups();
                open = openNow;
            }

            float previousAlpha = beginControlAlpha(transition);
            drawCheckbox(graphics, x, y, value, controlAnim(id, "checked", value ? 1.0f : 0.0f));
            drawClippedText(graphics, label, x + 26, y, labelWidth, TEXT);
            float focus = controlAnim(colorId, "focus", open || hovered ? 1.0f : 0.0f);
            Color.renderButton(graphics, buttonX, y, COLOR_BUTTON_W, FRAME_HEIGHT, color,
                    lerpColor(BORDER, ACCENT, focus));
            endControlAlpha(previousAlpha);
            if (open) {
                popupSystem.pendingColorPopup = new ColorPopup(colorId, buttonX + COLOR_BUTTON_W - COLOR_POPUP_W,
                        y + FRAME_HEIGHT + 4, COLOR_POPUP_W, buttonX, y, colorApplier, activeAlpha);
            }
            return value;
        }

        void settingsRow(String label, PopupContent content) {
            int x = layout.x;
            int y = layout.nextFrame();
            int idY = layout.idY();
            int w = layout.w;
            int gearX = x + w - GEAR_BUTTON_SIZE;
            int labelWidth = Math.max(0, gearX - x - 8);
            String id = controlId(scope, "settings", label, layout.originX, layout.originY, x, idY);
            if (render) {
                ControlTransition transition = controlTransition(id, y, FRAME_HEIGHT);
                y = transition.y;
                gearX = x + w - GEAR_BUTTON_SIZE;
                float previousAlpha = beginControlAlpha(transition);
                drawClippedText(graphics, label, x, y, labelWidth, TEXT);
                drawGearButton(graphics, id, gearX, y, content);
                endControlAlpha(previousAlpha);
            }
        }

        boolean toggleSwitch(String label, boolean defaultValue) {
            int x = layout.x;
            int y = layout.nextFrame();
            int idY = layout.idY();
            int w = layout.w;
            String id = controlId(scope, "switch", label, layout.originX, layout.originY, x, idY);
            boolean value = widgetRegistry.boolValues.computeIfAbsent(id, key -> defaultValue);
            if (render) {
                value = applyPendingHotkeyBool(id, value);
                widgetRegistry.boolValues.put(id, value);
            }
            if (render) {
                registerHotkeyArea(id, label, "Switch", onOff(value), value, x, y, w, FRAME_HEIGHT);
            }
            if (render && WidgetSurface.this.button(id, x, y, w, FRAME_HEIGHT)) {
                value = !value;
                widgetRegistry.boolValues.put(id, value);
                notifyToggle(label, value);
            }
            if (render) {
                float toggle = controlAnim(id, "toggle", value ? 1.0f : 0.0f);
                ToggleSwitch.render(graphics, x, y, toggle, lerpColor(themeColor(0xFFD2D7E4, TRACK), ACCENT, toggle));
                drawClippedText(graphics, label, x + 31, y + 3, Math.max(0, w - 31), TEXT);
            }
            return value;
        }

        boolean select(String label, String value) {
            int x = layout.x;
            int y = label.isEmpty() ? layout.nextSelect() : layout.nextLabeledSelect();
            int labelY = y;
            int idY = layout.idY();
            int w = layout.w;
            if (!label.isEmpty()) {
                y += TEXT_LINE_HEIGHT + SELECT_LABEL_GAP;
            }
            int fieldIdY = label.isEmpty() ? idY : idY + TEXT_LINE_HEIGHT + SELECT_LABEL_GAP;
            String idKey = label.isEmpty() ? value : label;
            return render && selectField(graphics, scope, layout.originX, layout.originY, x, y, w, idKey, value,
                    fieldIdY, label, labelY);
        }

        String combo(String label, String defaultValue, String values) {
            return combo(label, defaultValue, values, null);
        }

        String combo(String label, String defaultValue, String values, Consumer<String> applier) {
            int x = layout.x;
            int y = label.isEmpty() ? layout.nextSelect() : layout.nextLabeledSelect();
            int labelY = y;
            int idY = layout.idY();
            int w = layout.w;
            if (!label.isEmpty()) {
                y += TEXT_LINE_HEIGHT + SELECT_LABEL_GAP;
            }
            int fieldIdY = label.isEmpty() ? idY : idY + TEXT_LINE_HEIGHT + SELECT_LABEL_GAP;
            String idKey = label.isEmpty() ? values : label;
            if (!render) {
                String id = controlId(scope, "combo", idKey, layout.originX, layout.originY, x, fieldIdY);
                String[] options = WidgetSurfaceValueTools.selectOptions(values);
                String selected = WidgetSurfaceValueTools.selectDefault(defaultValue, options);
                registerHotkeyArea(id, idKey, "Combo", selected, null, x, y, w, SELECT_HEIGHT,
                        HotkeyValueType.SELECT, selected, options, 0, 0.0f, 0,
                        HotkeyApplier.forSelect(applier));
                return defaultValue;
            }
            return comboField(graphics, scope, layout.originX, layout.originY, x, y, w, idKey, defaultValue, values,
                    fieldIdY, HotkeyApplier.forSelect(applier), label, labelY);
        }

        String syncedCombo(String label, String currentValue, String values, Consumer<String> applier) {
            int x = layout.x;
            int y = label.isEmpty() ? layout.nextSelect() : layout.nextLabeledSelect();
            int labelY = y;
            int idY = layout.idY();
            int w = layout.w;
            if (!label.isEmpty()) {
                y += TEXT_LINE_HEIGHT + SELECT_LABEL_GAP;
            }
            int fieldIdY = label.isEmpty() ? idY : idY + TEXT_LINE_HEIGHT + SELECT_LABEL_GAP;
            String idKey = label.isEmpty() ? values : label;
            if (!render) {
                String id = controlId(scope, "combo", idKey, layout.originX, layout.originY, x, fieldIdY);
                String[] options = WidgetSurfaceValueTools.selectOptions(values);
                String selected = WidgetSurfaceValueTools.selectDefault(currentValue, options);
                registerHotkeyArea(id, idKey, "Combo", selected, null, x, y, w, SELECT_HEIGHT,
                        HotkeyValueType.SELECT, selected, options, 0, 0.0f, 0,
                        HotkeyApplier.forSelect(applier));
                return currentValue;
            }
            String id = controlId(scope, "combo", idKey, layout.originX, layout.originY, x, fieldIdY);
            if (!clickConsumed) {
                String[] options = WidgetSurfaceValueTools.selectOptions(values);
                widgetRegistry.selectValues.put(id, WidgetSurfaceValueTools.selectDefault(currentValue, options));
            }
            return comboField(graphics, scope, layout.originX, layout.originY, x, y, w, idKey, currentValue, values,
                    fieldIdY, HotkeyApplier.forSelect(applier), label, labelY);
        }

        int multiSelect(String label, int defaultMask, String values) {
            return multiSelect(label, defaultMask, values, null);
        }

        int multiSelect(String label, int defaultMask, String values, Consumer<Integer> applier) {
            int x = layout.x;
            int y = label.isEmpty() ? layout.nextSelect() : layout.nextLabeledSelect();
            int labelY = y;
            int idY = layout.idY();
            int w = layout.w;
            if (!label.isEmpty()) {
                y += TEXT_LINE_HEIGHT + SELECT_LABEL_GAP;
            }
            int fieldIdY = label.isEmpty() ? idY : idY + TEXT_LINE_HEIGHT + SELECT_LABEL_GAP;
            String idKey = label.isEmpty() ? values : label;
            if (!render) {
                String id = controlId(scope, "multi", idKey, layout.originX, layout.originY, x, fieldIdY);
                String[] options = WidgetSurfaceValueTools.selectOptions(values);
                int allMask = WidgetSurfaceValueTools.optionMask(options.length);
                int selectedMask = defaultMask & allMask;
                String summary = WidgetSurfaceValueTools.multiSelectSummary(selectedMask, options, allMask);
                registerHotkeyArea(id, idKey, "Multi Select", summary, null, x, y, w, SELECT_HEIGHT,
                        HotkeyValueType.MULTI_SELECT, summary, options, selectedMask, 0.0f, 0,
                        HotkeyApplier.forMultiSelect(applier));
                return defaultMask;
            }
            return multiSelectField(graphics, scope, layout.originX, layout.originY, x, y, w, idKey, defaultMask, values,
                    fieldIdY, HotkeyApplier.forMultiSelect(applier), label, labelY);
        }

        float slider(String label, float defaultValue, String suffix) {
            return slider(label, defaultValue, suffix, null, null, null);
        }

        float slider(String label, float defaultValue, String suffix, Consumer<Float> applier) {
            return slider(label, defaultValue, suffix, null, null, applier);
        }

        float slider(String label, float defaultValue, String suffix, Function<Float, String> valueFormatter,
                     Consumer<Float> applier) {
            return slider(label, defaultValue, suffix, valueFormatter, null, applier);
        }

        float slider(String label, float defaultValue, String suffix, Function<Float, String> valueFormatter,
                     Function<String, Float> valueParser, Consumer<Float> applier) {
            int x = layout.x;
            int y = label.isEmpty() ? layout.nextFrame() : layout.nextLabeledFrame();
            int idY = layout.idY();
            int w = layout.w;
            int frameY = y + (label.isEmpty() ? 0 : TEXT_LINE_HEIGHT + ITEM_INNER_SPACING_Y);
            int trackY = frameY + FRAME_HEIGHT / 2;
            int valueW = 42;
            int valueX = x + w - valueW;
            int trackW = Math.max(20, valueX - x - 4);
            String id = controlId(scope, "slider", label, layout.originX, layout.originY, x, idY);
            ControlTransition transition = render ? controlTransition(id, y, label.isEmpty() ? FRAME_HEIGHT : LABELED_FRAME_HEIGHT) : new ControlTransition(y, 1.0f);
            int yDelta = transition.y - y;
            y = transition.y;
            frameY += yDelta;
            trackY += yDelta;
            float current = widgetRegistry.sliderValues.computeIfAbsent(id, key -> defaultValue);
            if (render) {
                current = applyPendingHotkeySlider(id, current);
            }
            SliderValueAdapter valueAdapter = sliderValueAdapter(defaultValue, suffix, valueFormatter, valueParser, applier);
            String displayValue = valueAdapter.format(current);
            registerHotkeyArea(id, label.isEmpty() ? "Slider" : label, "Slider", displayValue, null,
                    x, label.isEmpty() ? frameY : y, w, label.isEmpty() ? FRAME_HEIGHT : LABELED_FRAME_HEIGHT,
                    HotkeyValueType.SLIDER, displayValue, null, 0, current, 0,
                    HotkeyApplier.forSlider(applier), valueAdapter.formatter, valueAdapter.parser);
            if (render) {
                if (click && clickButton == 0 && !clickConsumed
                        && PopupGeometry.contains(clickX, clickY, valueX, frameY, valueW, FRAME_HEIGHT)
                        && insideInteractionClip(clickX, clickY)
                        && !insideOpenHotkeyPopup(clickX, clickY)
                        && !insideOpenHotkeyDetailPopup(clickX, clickY)
                        && !insideOpenHotkeyValuePopup(clickX, clickY)
                        && !insideOpenMultiSelectPopup(clickX, clickY)
                        && (popupSystem.renderingPopup || !insideOpenSettingsPopup(clickX, clickY))) {
                    startSliderEdit(id, displayValue, valueAdapter, valueX, frameY, valueW, FRAME_HEIGHT);
                    clickConsumed = true;
                }
            }
            if (render && !id.equals(editingSliderId)) {
                current = sliderFloat(id, x, frameY, trackW, FRAME_HEIGHT, current);
            }
            widgetRegistry.sliderValues.put(id, current);
            if (render) {
                float previousAlpha = beginControlAlpha(transition);
                if (!label.isEmpty()) {
                    text(graphics, label, x, y, MUTED);
                }
                float displayCurrent = controlAnim(id, "value", current);
                Slider.renderTrack(graphics, x, trackY, trackW, displayCurrent, TRACK, ACCENT, ACCENT);
                String valueText = id.equals(editingSliderId) ? editingSliderText : valueAdapter.format(current);
                boolean editing = id.equals(editingSliderId);
                boolean valueHovered = hovered(valueX, frameY, valueW, FRAME_HEIGHT);
                int inputX = valueX + 2;
                int inputY = frameY;
                int inputW = valueW - 2;
                int inputH = FRAME_HEIGHT;
                int textX = inputX + 6;
                int textY = inputY + Math.max(0, (inputH - FONT_SIZE) / 2) - 2;
                int textMaxW = inputW - 10;
                float inputFocus = controlAnim(id, "inputFocus", editing || valueHovered ? 1.0f : 0.0f);
                Input.renderSmallField(graphics, inputX, inputY, inputW, inputH, FIELD,
                        lerpColor(BORDER, editing ? ACCENT : themeColor(0xFFC9D0E4, BORDER), inputFocus));
                drawClippedText(graphics, valueText, textX, textY, textMaxW, editing ? TEXT : ACCENT_DARK);
                if (editing && ((System.currentTimeMillis() / 500L) & 1L) == 0L) {
                    int cursorX = textX + Math.min(textWidth(valueText), Math.max(0, textMaxW));
                    graphics.vLine(cursorX + 1, inputY + 3, inputY + inputH - 3, alphaColor(ACCENT_DARK, renderAlpha));
                }
                endControlAlpha(previousAlpha);
            }
            return current;
        }

        int colorPicker(String label, int defaultColor) {
            return colorPicker(label, defaultColor, null);
        }

        int colorPicker(String label, int defaultColor, Consumer<Integer> applier) {
            int x = layout.x;
            int y = layout.nextFrame();
            int idY = layout.idY();
            int w = layout.w;
            String id = controlId(scope, "color", label, layout.originX, layout.originY, x, idY);
            ControlTransition transition = render ? controlTransition(id, y, FRAME_HEIGHT) : new ControlTransition(y, 1.0f);
            y = transition.y;
            int color = widgetRegistry.colorValues.computeIfAbsent(id, key -> defaultColor);
            registerHotkeyArea(id, label, "Color", colorHex(color), null, x, y, w, FRAME_HEIGHT,
                    HotkeyValueType.COLOR, colorHex(color), null, 0, 0.0f, color,
                    HotkeyApplier.forColor(applier));
            if (!render) {
                return color;
            }
            color = applyPendingHotkeyColor(id, color);
            widgetRegistry.colorValues.put(id, color);

            int buttonX = x + w - COLOR_BUTTON_W;
            String buttonId = id + "/button";
            boolean open = id.equals(popupSystem.openColorId);
            boolean hovered = hovered(buttonX, y, COLOR_BUTTON_W, FRAME_HEIGHT);
            if (WidgetSurface.this.button(buttonId, buttonX, y, COLOR_BUTTON_W, FRAME_HEIGHT)) {
                boolean openNow = !open;
                popupSystem.openColorId = openNow ? id : null;
                if (openNow && popupSystem.openColorPopup != null && !id.equals(popupSystem.openColorPopup.id)) {
                    closeColorPopupVisual();
                }
                popupSystem.openSelectId = null;
                closeSelectPopupVisual();
                popupSystem.openMultiSelectId = null;
                closeMultiSelectPopupVisual();
                closeHotkeyPopups();
                open = openNow;
            }
            float previousAlpha = beginControlAlpha(transition);
            int labelY = y + Math.max(0, (FRAME_HEIGHT - FONT_SIZE) / 2) - 2;
            drawClippedText(graphics, label, x, labelY, Math.max(0, buttonX - x - 8), TEXT);
            float focus = controlAnim(id, "focus", open || hovered ? 1.0f : 0.0f);
            Color.renderButton(graphics, buttonX, y, COLOR_BUTTON_W, FRAME_HEIGHT, color,
                    lerpColor(BORDER, ACCENT, focus));
            endControlAlpha(previousAlpha);
            if (open) {
                popupSystem.pendingColorPopup = new ColorPopup(id, buttonX + COLOR_BUTTON_W - COLOR_POPUP_W, y + FRAME_HEIGHT + 4,
                        COLOR_POPUP_W, buttonX, y, applier, true);
            }
            return color;
        }

        String inputText(String label, String defaultValue) {
            return inputText(label, defaultValue, null);
        }

        String inputText(String label, String defaultValue, Consumer<String> applier) {
            int x = layout.x;
            int y = label.isEmpty() ? layout.nextSelect() : layout.nextLabeledSelect();
            int idY = layout.idY();
            int w = layout.w;
            int fieldY = y + (label.isEmpty() ? 0 : TEXT_LINE_HEIGHT + SELECT_LABEL_GAP);
            int fieldIdY = label.isEmpty() ? idY : idY + TEXT_LINE_HEIGHT + SELECT_LABEL_GAP;
            String idKey = label.isEmpty() ? defaultValue : label;
            String id = controlId(scope, "input", idKey, layout.originX, layout.originY, x, fieldIdY);
            lastInputTextId = id;
            ControlTransition transition = render ? controlTransition(id, y, label.isEmpty() ? SELECT_HEIGHT : LABELED_SELECT_HEIGHT) : new ControlTransition(y, 1.0f);
            int yDelta = transition.y - y;
            y = transition.y;
            fieldY += yDelta;
            String value = widgetRegistry.textValues.computeIfAbsent(id, key -> defaultValue == null ? "" : defaultValue);
            if (!render) {
                return value;
            }

            boolean focused = id.equals(focusSystem.textInputId);
            boolean hovered = hovered(x, fieldY, w, SELECT_HEIGHT);
            if (WidgetSurface.this.button(id, x, fieldY, w, SELECT_HEIGHT)) {
                focusTextInput(id, x, fieldY, w, SELECT_HEIGHT);
                closeFieldPopups();
                focused = true;
            }

            value = widgetRegistry.textValues.getOrDefault(id, "");
            float inputFocus = controlAnim(id, "focus", focused || hovered ? 1.0f : 0.0f);
            float previousAlpha = beginControlAlpha(transition);
            if (!label.isEmpty()) {
                text(graphics, label, x, y, SUBTLE);
            }
            Input.renderField(graphics, x, fieldY, w, SELECT_HEIGHT, FIELD,
                    lerpColor(BORDER, focused ? ACCENT : themeColor(0xFFC9D0E4, BORDER), inputFocus));
            int textX = x + ITEM_SPACING_X;
            int textY = fieldY + (SELECT_HEIGHT - FONT_SIZE) / 2 - 2;
            int textMaxW = w - ITEM_SPACING_X * 2;
            drawInputTextValue(graphics, id, value, textX, textY,
                    textMaxW, fieldY, SELECT_HEIGHT);
            endControlAlpha(previousAlpha);
            if (applier != null) {
                applier.accept(value);
            }
            return value;
        }

        String inputText(String idKey, String defaultValue, int x, int y, int w, int h,
                         Consumer<String> applier) {
            return inputText(idKey, defaultValue, x, y, w, h, applier, false, 0);
        }

        String inputText(String idKey, String defaultValue, int x, int y, int w, int h,
                         Consumer<String> applier, boolean focusNow) {
            return inputText(idKey, defaultValue, x, y, w, h, applier, focusNow, 0);
        }

        String inputText(String idKey, String defaultValue, int x, int y, int w, int h,
                         Consumer<String> applier, boolean focusNow, int textYOffset) {
            return inputText(idKey, defaultValue, x, y, w, h,
                    applier, focusNow, textYOffset, false);
        }

        String inputText(String idKey, String defaultValue, int x, int y, int w, int h,
                         Consumer<String> applier, boolean focusNow, int textYOffset,
                         boolean msdfText) {
            String id = controlId(scope, "input", idKey, layout.originX, layout.originY, x, y);
            lastInputTextId = id;
            String value = widgetRegistry.textValues.computeIfAbsent(id, key -> defaultValue == null ? "" : defaultValue);
            if (!id.equals(focusSystem.textInputId) && defaultValue != null && !defaultValue.equals(value)) {
                widgetRegistry.textValues.put(id, defaultValue);
                value = defaultValue;
            }
            boolean focused = id.equals(focusSystem.textInputId);
            boolean hovered = hovered(x, y, w, h);
            if (focusNow || WidgetSurface.this.button(id, x, y, w, h)) {
                focusTextInput(id, x, y, w, h);
                closeFieldPopups();
                focused = true;
            }

            value = widgetRegistry.textValues.getOrDefault(id, "");
            float inputFocus = controlAnim(id, "focus", focused || hovered ? 1.0f : 0.0f);
            Input.renderField(graphics, x, y, w, h, FIELD,
                    lerpColor(BORDER, focused ? ACCENT : themeColor(0xFFC9D0E4, BORDER), inputFocus));
            int textX = x + ITEM_SPACING_X;
            int textY = y + Math.max(0, (h - FONT_SIZE) / 2) - 2 + textYOffset;
            int textMaxW = Math.max(0, w - ITEM_SPACING_X * 2);
            drawInputTextValue(graphics, id, value, textX, textY, textMaxW, y, h, msdfText);
            if (applier != null) {
                applier.accept(value);
            }
            return value;
        }

        void setLastInputText(String value) {
            if (lastInputTextId != null) {
                widgetRegistry.textValues.put(lastInputTextId, value == null ? "" : value);
            }
        }

        boolean button(String label) {
            return button(label, layout.w, FRAME_HEIGHT);
        }

        int contentWidth() {
            return layout.w;
        }

        int contentX() {
            return layout.x;
        }

        int cursorY() {
            return layout.cursorY;
        }

        int reserveRow(int height) {
            return layout.next(Math.max(1, height));
        }

        void reserveSpace(int height) {
            int safeHeight = Math.max(0, height);
            layout.cursorY += safeHeight;
            layout.idCursorY += safeHeight;
        }

        boolean rendering() {
            return render;
        }

        int actionButtonPair(String leftId, String leftLabel, int leftIcon, boolean leftAccent,
                             String rightId, String rightLabel, int rightIcon, boolean rightAccent,
                             int height) {
            int rowH = Math.max(1, height);
            int y = layout.next(rowH);
            if (!render) {
                return 0;
            }
            int gap = ITEM_SPACING_X;
            int buttonW = Math.max(1, (layout.w - gap) / 2);
            int leftX = layout.x;
            int rightX = layout.x + buttonW + gap;
            int result = 0;
            if (positionedButton(leftId, leftLabel, leftX, y, buttonW, rowH, leftIcon, leftAccent, false)) {
                result |= 1;
            }
            if (positionedButton(rightId, rightLabel, rightX, y, buttonW, rowH, rightIcon, rightAccent, false)) {
                result |= 2;
            }
            return result;
        }

        boolean compactActionRow(String idKey, String label, String actionLabel, int actionIcon, int height) {
            return compactActionRow(idKey, label, actionLabel, actionIcon, height, true);
        }

        boolean compactActionRow(String idKey, String label, String actionLabel, int actionIcon, int height,
                                 boolean visible) {
            return compactActionRow(idKey, label, actionLabel, actionIcon, height, visible, true);
        }

        boolean compactActionRow(String idKey, String label, String actionLabel, int actionIcon, int height,
                                 boolean visible, boolean divider) {
            int rowH = Math.max(1, height);
            String rowId = scope + "/compact-row/" + idKey;
            int targetY = layout.next(rowH);
            int stableY = layout.idY();
            if (!render) {
                return false;
            }
            int y = animatedRowY(rowId, targetY, stableY, layout.originY, rowH);
            lastRowY = y;
            return renderCompactActionRow(idKey, label, actionLabel, actionIcon, y, rowH, visible, visible, divider);
        }

        boolean compactActionRowAt(String idKey, String label, String actionLabel, int actionIcon, int y, int height,
                                   boolean visible) {
            return compactActionRowAt(idKey, label, actionLabel, actionIcon, y, height, visible, true);
        }

        boolean compactActionRowAt(String idKey, String label, String actionLabel, int actionIcon, int y, int height,
                                   boolean visible, boolean divider) {
            int rowH = Math.max(1, height);
            if (!render) {
                return false;
            }
            return renderCompactActionRow(idKey, label, actionLabel, actionIcon, y, rowH, visible, visible, divider);
        }

        int lastRowY() {
            return lastRowY;
        }

        void clearCompactActionRowAnimation(String idKey) {
            WidgetSurface.this.clearCompactActionRowAnimation(scope, idKey);
        }

        private boolean renderCompactActionRow(String idKey, String label, String actionLabel, int actionIcon, int y,
                                               int rowH, boolean visible, boolean clickable, boolean divider) {
            String rowId = scope + "/compact-row/" + idKey;
            float alpha = visible ? appearAnim(rowId, "appear") : disappearAnim(rowId, "appear");
            if (alpha <= 0.01f) {
                return false;
            }
            if (!intersectsClip(y, rowH)) {
                return false;
            }
            boolean clipped = pushRenderClip(clipX, clipY, clipW, clipH);
            InteractionClipState previousInteractionClip = pushInteractionClip(clipX, clipY, clipW, clipH);
            float previousAlpha = renderAlpha;
            try {
                renderAlpha = previousAlpha * alpha * conditionalAlpha;
                ShapeRenderer.setGlobalAlpha(renderAlpha);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, renderAlpha);
                int x = layout.x + 2;
                int w = layout.w - 4;
                int buttonH = Math.min(rowH - 2, 22);
                int buttonW = Math.min(86, Math.max(72, textWidth(actionLabel) + 34));
                int buttonX = x + w - buttonW;
                int textW = Math.max(0, buttonX - x - ITEM_SPACING_X);
                float iconSize = Math.max(8.0f, rowH * 0.38f);
                float iconW = MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_USER, iconSize);
                int iconY = y + Math.round(rowH * 0.5f - iconSize * 0.5f);
                MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_USER, x, iconY, iconSize,
                        alphaColor(MUTED, renderAlpha));
                int textX = x + Math.round(iconW) + 8;
                drawClippedText(graphics, label, textX, y + Math.max(0, (rowH - FONT_SIZE) / 2) - 2,
                        Math.max(0, textW - (textX - x)), TEXT);
                boolean clicked = clickable && visible && positionedButtonWithId(scope + "/compact-action/" + idKey,
                        actionLabel, buttonX, y + Math.max(0, (rowH - buttonH) / 2), buttonW, buttonH,
                        actionIcon, false, false);
                if (divider) {
                    dividerLine(graphics, x, y + rowH, w);
                }
                return clicked;
            } finally {
                endControlAlpha(previousAlpha);
                popInteractionClip(previousInteractionClip);
                popRenderClip(clipped);
            }
        }

        void emptyState(String label, String value, int height) {
            emptyState(label, value, height, true);
        }

        void emptyState(String label, String value, int height, boolean divider) {
            int rowH = Math.max(36, height);
            int y = layout.next(rowH);
            if (!render) {
                return;
            }
            if (!intersectsClip(y, rowH)) {
                return;
            }
            boolean clipped = pushRenderClip(clipX, clipY, clipW, clipH);
            int x = layout.x + 2;
            int w = layout.w - 4;
            int centerY = y + rowH / 2;
            int labelW = textWidth(label);
            int valueW = textWidth(value);
            try {
                text(graphics, label, x + Math.max(0, (w - labelW) / 2), centerY - FONT_SIZE - 2, MUTED);
                text(graphics, value, x + Math.max(0, (w - valueW) / 2), centerY + 2, SUBTLE);
                if (divider) {
                    dividerLine(graphics, x, y + rowH, w);
                }
            } finally {
                popRenderClip(clipped);
            }
        }

        boolean button(String label, int width, int height) {
            int x = layout.x;
            int buttonW = Math.max(1, Math.min(layout.w, width));
            int buttonH = Math.max(1, height);
            int y = layout.next(buttonH);
            int idY = layout.idY();
            String id = controlId(scope, "button", label, layout.originX, layout.originY, x, idY);
            ControlTransition transition = render ? controlTransition(id, y, buttonH) : new ControlTransition(y, 1.0f);
            y = transition.y;
            if (!render) {
                return false;
            }
            boolean hovered = hovered(x, y, buttonW, buttonH);
            boolean clicked = WidgetSurface.this.button(id, x, y, buttonW, buttonH);
            int radius = Math.min(8, Math.max(4, buttonH / 3));
            float hover = controlAnim(id, "hover", clicked || hovered ? 1.0f : 0.0f);
            float previousAlpha = beginControlAlpha(transition);
            Button.render(graphics, x, y, buttonW, buttonH, radius,
                    lerpColor(ACCENT, ACCENT_DARK, hover), ACCENT_DARK);
            int textW = textWidth(label);
            drawClippedText(graphics, label, x + Math.max(6, (buttonW - textW) / 2),
                    y + Math.max(0, (buttonH - FONT_SIZE) / 2) - 3, Math.max(0, buttonW - 12), 0xFFFFFFFF);
            endControlAlpha(previousAlpha);
            return clicked;
        }

        boolean button(String label, int x, int y, int width, int height, int icon, boolean accent) {
            return positionedButton(label, label, x, y, width, height, icon, accent, false);
        }

        boolean button(String idKey, String label, int x, int y, int width, int height, int icon, boolean accent) {
            return positionedButton(idKey, label, x, y, width, height, icon, accent, false);
        }

        void primeButtonState(String kind, String idKey, String label, int x, int y, boolean accent) {
            String resolvedIdKey = idKey == null || idKey.equals(label) ? label : idKey;
            String id = controlId(scope, kind, resolvedIdKey, layout.originX, layout.originY, x, y);
            WidgetSurface.this.primeControlAnim(id, "hover", 0.0f);
            WidgetSurface.this.primeControlAnim(id, "active", accent ? 1.0f : 0.0f);
        }

        void primeFloatingButtonState(String kind, String idKey, boolean accent) {
            String id = floatingControlId(kind, idKey);
            WidgetSurface.this.primeControlAnim(id, "hover", 0.0f);
            WidgetSurface.this.primeControlAnim(id, "active", accent ? 1.0f : 0.0f);
        }

        boolean dangerButton(String label, int x, int y, int width, int height, int icon) {
            return positionedButton(label, label, x, y, width, height, icon, false, true);
        }

        boolean iconButton(String label, int x, int y, int size, int icon) {
            return iconButton(label, x, y, size, icon, false);
        }

        boolean iconButton(String label, int x, int y, int size, int icon, boolean active) {
            String id = controlId(scope, "iconButton", label, layout.originX, layout.originY, x, y);
            return renderIconButton(id, x, y, size, icon, active);
        }

        boolean floatingIconButton(String idKey, int x, int y, int size, int icon, boolean active) {
            String id = floatingControlId("iconButton", idKey);
            return renderIconButton(id, x, y, size, icon, active);
        }

        private boolean renderIconButton(String id, int x, int y, int size, int icon, boolean active) {
            boolean hovered = hovered(x, y, size, size);
            boolean clicked = WidgetSurface.this.button(id, x, y, size, size);
            float hover = controlAnim(id, "hover", clicked || hovered ? 1.0f : 0.0f);
            float activeAnim = controlAnim(id, "active", active ? 1.0f : 0.0f);
            int normalFill = lerpColor(themeColor(0x00FFFFFF, 0x33212634), themeColor(0x80FFFFFF, 0x55212634), hover);
            int activeFill = lerpColor(ACCENT, ACCENT_DARK, hover);
            int normalBorder = lerpColor(themeColor(0xFFE0E4EF, 0x443E4656), themeColor(0xFFC9D0E4, 0x664B5568), hover);
            int activeBorder = lerpColor(ACCENT, ACCENT_DARK, hover);
            int fill = lerpColor(normalFill, activeFill, activeAnim);
            int border = lerpColor(normalBorder, activeBorder, activeAnim);
            Button.render(graphics, x, y, size, size, Math.min(8, Math.max(4, size / 3)), fill, border);
            float iconSize = Math.max(8.0f, size * 0.42f);
            float iconW = MSDFRenderer.iconWidth(icon, iconSize);
            int normalIcon = lerpColor(MUTED, ACCENT_DARK, hover);
            int activeIcon = lerpColor(0xFFFFFFFF, 0xFFFFFFFF, hover);
            int iconColor = lerpColor(normalIcon, activeIcon, activeAnim);
            MSDFRenderer.drawIconCodepoint(graphics, icon, x + size * 0.5f - iconW * 0.5f,
                    y + size * 0.5f - iconSize * 0.5f, iconSize,
                    alphaColor(iconColor, renderAlpha));
            return clicked;
        }

        void popupSurface(int x, int y, int w, int h, int radius) {
            drawPopupSurface(graphics, x, y, w, h, radius);
        }

        void menuPopup(String id, int x, int y, MenuContent content) {
            if (content == null) {
                return;
            }
            MenuBuilder builder = new MenuBuilder();
            content.build(builder);
            if (!builder.items.isEmpty()) {
                popupSystem.pendingMenuPopup = new MenuPopup(id, x, y, List.copyOf(builder.items));
            }
        }

        boolean menuButton(String label, int x, int y, int w, int h, int icon, boolean danger) {
            String id = controlId(scope, danger ? "dangerMenuButton" : "menuButton", label,
                    layout.originX, layout.originY, x, y);
            int rowX = x + HOTKEY_POPUP_PAD;
            int rowW = w - HOTKEY_POPUP_PAD * 2;
            boolean hovered = hovered(rowX, y, rowW, HOTKEY_ROW_H);
            boolean clicked = WidgetSurface.this.button(id, rowX, y, rowW, HOTKEY_ROW_H);
            drawHotkeyMenuRow(graphics, x, y, w, icon, label, false, danger ? DANGER : TEXT, hovered, false);
            return clicked;
        }

        private boolean positionedButton(String idKey, String label, int x, int y, int width, int height, int icon,
                                         boolean accent, boolean danger) {
            String id = controlId(scope, danger ? "dangerButton" : "button", label,
                    layout.originX, layout.originY, x, y);
            if (!idKey.equals(label)) {
                id = controlId(scope, danger ? "dangerButton" : "button", idKey,
                        layout.originX, layout.originY, x, y);
            }
            boolean hovered = hovered(x, y, width, height);
            boolean clicked = WidgetSurface.this.button(id, x, y, width, height);
            float hover = controlAnim(id, "hover", clicked || hovered ? 1.0f : 0.0f);
            float active = danger ? 0.0f : controlAnim(id, "active", accent ? 1.0f : 0.0f);
            int radius = Math.min(8, Math.max(4, height / 3));
            int fill;
            int border;
            int textColor;
            if (danger) {
                fill = lerpColor(0x00000000, 0x20FF4655, hover);
                border = lerpColor(0x00000000, 0x33FF4655, hover);
                textColor = DANGER;
            } else {
                int normalFill = lerpColor(themeColor(0xFFFFFFFF, 0x99212634), ACCENT_SOFT, hover);
                int activeFill = lerpColor(ACCENT, ACCENT_DARK, hover);
                int normalBorder = lerpColor(themeColor(0xFFE0E4EF, 0x553E4656), ACCENT_SOFT, hover);
                int activeBorder = alphaColor(0xFFFFFFFF, hovered ? 0.28f : 0.16f);
                fill = lerpColor(normalFill, activeFill, active);
                border = lerpColor(normalBorder, activeBorder, active);
                textColor = lerpColor(TEXT, 0xFFFFFFFF, active);
            }
            Button.render(graphics, x, y, width, height, radius, fill, border);
            int contentPad = 8;
            int availableW = Math.max(0, width - contentPad * 2);
            int textW = textWidth(label);
            int textMaxW = textW;
            float iconSize = 0.0f;
            float iconW = 0.0f;
            int iconGap = 0;
            if (icon != 0) {
                iconSize = Math.max(8.0f, height * 0.42f);
                iconW = MSDFRenderer.iconWidth(icon, iconSize);
                iconGap = 6;
            }
            int wantedW = Math.round(iconW) + iconGap + textW;
            int contentW = Math.min(availableW, wantedW);
            if (wantedW > availableW) {
                textMaxW = Math.max(0, availableW - Math.round(iconW) - iconGap);
                contentW = Math.round(iconW) + (textMaxW > 0 ? iconGap + textMaxW : 0);
            }
            int contentX = x + Math.max(contentPad, (width - contentW) / 2);
            int textX = contentX;
            if (icon != 0) {
                MSDFRenderer.drawIconCodepoint(graphics, icon, contentX, y + height * 0.5f - iconSize * 0.5f,
                        iconSize, alphaColor(textColor, renderAlpha));
                textX = contentX + Math.round(iconW) + iconGap;
            }
            drawClippedText(graphics, label, textX, y + Math.max(0, (height - FONT_SIZE) / 2) - 2,
                    textMaxW, textColor);
            return clicked;
        }

        boolean floatingButton(String idKey, String label, int x, int y, int width, int height, int icon, boolean accent) {
            return positionedButtonWithId(floatingControlId("button", idKey), label, x, y, width, height, icon,
                    accent, false);
        }

        private boolean positionedButtonWithId(String id, String label, int x, int y, int width, int height, int icon,
                                               boolean accent, boolean danger) {
            boolean hovered = hovered(x, y, width, height);
            boolean clicked = WidgetSurface.this.button(id, x, y, width, height);
            float hover = controlAnim(id, "hover", clicked || hovered ? 1.0f : 0.0f);
            float active = danger ? 0.0f : controlAnim(id, "active", accent ? 1.0f : 0.0f);
            int radius = Math.min(8, Math.max(4, height / 3));
            int fill;
            int border;
            int textColor;
            if (danger) {
                fill = lerpColor(0x00000000, 0x20FF4655, hover);
                border = lerpColor(0x00000000, 0x33FF4655, hover);
                textColor = DANGER;
            } else {
                int normalFill = lerpColor(themeColor(0xFFFFFFFF, 0x99212634), ACCENT_SOFT, hover);
                int activeFill = lerpColor(ACCENT, ACCENT_DARK, hover);
                int normalBorder = lerpColor(themeColor(0xFFE0E4EF, 0x553E4656), ACCENT_SOFT, hover);
                int activeBorder = alphaColor(0xFFFFFFFF, hovered ? 0.28f : 0.16f);
                fill = lerpColor(normalFill, activeFill, active);
                border = lerpColor(normalBorder, activeBorder, active);
                textColor = lerpColor(TEXT, 0xFFFFFFFF, active);
            }
            Button.render(graphics, x, y, width, height, radius, fill, border);
            int contentPad = 8;
            int availableW = Math.max(0, width - contentPad * 2);
            int textW = textWidth(label);
            int textMaxW = textW;
            float iconSize = 0.0f;
            float iconW = 0.0f;
            int iconGap = 0;
            if (icon != 0) {
                iconSize = Math.max(8.0f, height * 0.42f);
                iconW = MSDFRenderer.iconWidth(icon, iconSize);
                iconGap = 6;
            }
            int wantedW = Math.round(iconW) + iconGap + textW;
            int contentW = Math.min(availableW, wantedW);
            if (wantedW > availableW) {
                textMaxW = Math.max(0, availableW - Math.round(iconW) - iconGap);
                contentW = Math.round(iconW) + (textMaxW > 0 ? iconGap + textMaxW : 0);
            }
            int contentX = x + Math.max(contentPad, (width - contentW) / 2);
            int textX = contentX;
            if (icon != 0) {
                MSDFRenderer.drawIconCodepoint(graphics, icon, contentX, y + height * 0.5f - iconSize * 0.5f,
                        iconSize, alphaColor(textColor, renderAlpha));
                textX = contentX + Math.round(iconW) + iconGap;
            }
            drawClippedText(graphics, label, textX, y + Math.max(0, (height - FONT_SIZE) / 2) - 2,
                    textMaxW, textColor);
            return clicked;
        }

        private String floatingControlId(String kind, String idKey) {
            return scope + "/" + kind + "/" + idKey;
        }

        boolean actionButton(String label) {
            return button(label);
        }

        boolean actionButton(String label, int width, int height) {
            return button(label, width, height);
        }

        void infoRow(String label, String value) {
            infoRow(label, value, true);
        }

        void infoRow(String label, String value, boolean divider) {
            int x = layout.x + 2;
            int y = layout.next(INFO_ROW_H);
            int w = layout.w - 4;
            if (render) {
                if (!intersectsClip(y, INFO_ROW_H)) {
                    return;
                }
                boolean clipped = pushRenderClip(clipX, clipY, clipW, clipH);
                String id = controlId(scope, "info", label, layout.originX, layout.originY, x, layout.idY());
                ControlTransition transition = controlTransition(id, y, INFO_ROW_H);
                y = transition.y;
                float previousAlpha = beginControlAlpha(transition);
                try {
                    int textY = y + Math.max(0, (INFO_ROW_H - FONT_SIZE) / 2) - 2;
                    text(graphics, label, x, textY, MUTED);
                    text(graphics, value, x + w - textWidth(value), textY, TEXT);
                    if (divider) {
                        dividerLine(graphics, x, y + INFO_ROW_H, w);
                    }
                } finally {
                    endControlAlpha(previousAlpha);
                    popRenderClip(clipped);
                }
            }
        }

        private boolean intersectsClip(int y, int h) {
            return clipW > 0 && clipH > 0 && y + h > clipY && y < clipY + clipH;
        }

        void divider() {
            layout.sectionGap();
            int y = layout.next(DIVIDER_H);
            if (render) {
                dividerLine(graphics, layout.x, y, layout.w);
            }
            layout.sectionGap();
        }

        void visible(String idKey, boolean visible, Consumer<Frame> content) {
            animatedVisibilityGroup(idKey, visible, content);
        }

        void mode(String idKey, String value, String expected, Consumer<Frame> content) {
            animatedVisibilityGroup(idKey + "/" + expected, expected.equals(value), content);
        }

        void conditional(String idKey, boolean visible, Consumer<Frame> content) {
            animatedVisibilityGroup(idKey, visible, content);
        }

        private void animatedVisibilityGroup(String idKey, boolean visible, Consumer<Frame> content) {
            if (content == null) {
                return;
            }
            String id = controlId(scope, "visible", idKey, layout.originX, layout.originY, layout.x, layout.idCursorY);
            int startY = layout.cursorY;
            int startIdY = layout.idCursorY;

            UiLayout measureLayout = new UiLayout(layout.originX, layout.originY, layout.x, startY, startIdY, layout.w);
            Frame measureFrame = new Frame(null, scope, hotkeyGroup, measureLayout, false, clipX, clipY, clipW, clipH);
            content.accept(measureFrame);
            int fullHeight = Math.max(0, measureLayout.cursorY - startY);
            int fullIdHeight = Math.max(0, measureLayout.idCursorY - startIdY);
            if (fullHeight <= 0) {
                return;
            }

            float progress = render
                    ? conditionalVisibleProgress(id, visible)
                    : currentConditionalVisibleProgress(id, visible);
            int reservedHeight = Math.round(fullHeight * progress);
            layout.cursorY = startY + reservedHeight;
            layout.idCursorY = startIdY + fullIdHeight;
            layout.lastIdY = startIdY;

            if (!render || progress <= 0.01f) {
                return;
            }

            ConditionalRenderState previous = pushConditional(visible, progress < 0.999f, !visible);
            UiLayout renderLayout = new UiLayout(layout.originX, layout.originY, layout.x, startY, startIdY, layout.w);
            Frame renderFrame = new Frame(graphics, scope, hotkeyGroup, renderLayout, true, clipX, clipY, clipW, clipH);
            boolean clipped = pushRenderClip(clipX, clipY, clipW, clipH);
            try {
                content.accept(renderFrame);
            } finally {
                popRenderClip(clipped);
                popConditional(previous);
            }
        }

        int cardHeight() {
            return CARD_CONTENT_TOP + layout.contentHeight() + CARD_BOTTOM_PAD;
        }
    }

    private int colorChannelSlider(GuiGraphics graphics, String id, String label, int value, int x, int y, int w, int color) {
        int labelW = 12;
        int valueW = 24;
        int trackX = x + labelW + 5;
        int trackW = Math.max(20, w - labelW - valueW - 12);
        float current = value / 255.0f;
        current = sliderFloat(id, trackX, y - 2, trackW, COLOR_ROW_HEIGHT + 4, current);
        int next = Math.round(current * 255.0f);
        float displayCurrent = controlAnim(id, "value", current);
        text(graphics, label, x, y, MUTED);
        Slider.renderCompactTrack(graphics, trackX, y + 5, trackW, displayCurrent, TRACK, color, color);
        text(graphics, Integer.toString(next), x + w - valueW, y, MUTED);
        return next;
    }

    private void renderColorPopup(GuiGraphics graphics, ColorPopup popup) {
        int h = colorPopupHeight(popup.activeAlpha);
        int color = widgetRegistry.colorValues.computeIfAbsent(popup.id, key -> 0xFFFFFFFF);
        drawPopupSurface(graphics, popup.x, popup.y, popup.w, h, 8);

        int contentX = popup.x + COLOR_POPUP_PAD_X;
        int contentY = popup.y + COLOR_POPUP_PAD_Y;
        int contentW = popup.w - COLOR_POPUP_PAD_X * 2;
        Color.renderPreview(graphics, contentX, contentY, contentW, COLOR_POPUP_PREVIEW_H, color, BORDER);
        text(graphics, colorHex(color), contentX + 7, contentY + 1, TEXT);

        int red = (color >>> 16) & 0xFF;
        int green = (color >>> 8) & 0xFF;
        int blue = color & 0xFF;
        int alpha = (color >>> 24) & 0xFF;
        int rowY = contentY + COLOR_POPUP_PREVIEW_H + ITEM_INNER_SPACING_Y;
        popupSystem.renderingColorPopup = true;
        red = colorChannelSlider(graphics, popup.id + "/r", "R", red, contentX, rowY, contentW, 0xFFFF5F67);
        green = colorChannelSlider(graphics, popup.id + "/g", "G", green, contentX, rowY + COLOR_ROW_HEIGHT + COLOR_ROW_GAP, contentW, 0xFF2FCB8B);
        blue = colorChannelSlider(graphics, popup.id + "/b", "B", blue, contentX, rowY + (COLOR_ROW_HEIGHT + COLOR_ROW_GAP) * 2, contentW, 0xFF6E72FF);
        if (popup.activeAlpha) {
            alpha = colorChannelSlider(graphics, popup.id + "/a", "A", alpha, contentX, rowY + (COLOR_ROW_HEIGHT + COLOR_ROW_GAP) * 3, contentW, 0xFF8A91A8);
        }
        popupSystem.renderingColorPopup = false;
        int nextColor = composeColor(alpha, red, green, blue);
        widgetRegistry.colorValues.put(popup.id, nextColor);
        if (popup.applier != null) {
            popup.applier.accept(nextColor);
        }
    }

    private float sliderFloat(String id, int x, int y, int w, int h, float value) {
        if (interactionLocked || conditionalInteractionDisabled) {
            if (id.equals(activeId)) {
                activeId = null;
            }
            return value;
        }
        if (click && clickButton == 0 && sliderBlockedByPopup(clickX, clickY)) {
            if (id.equals(activeId)) {
                activeId = null;
            }
            return value;
        }
        if (sliderBlockedByPopup(mouseX, mouseY) && id.equals(activeId)) {
            activeId = null;
            return value;
        }
        if (click && !clickConsumed && PopupGeometry.contains(clickX, clickY, x, y, w, h)) {
            activeId = id;
            clickConsumed = true;
        }
        if (!id.equals(activeId)) {
            return value;
        }
        float next = (float) ((mouseX - x) / Math.max(1.0, w));
        return Math.max(0.0f, Math.min(1.0f, next));
    }

    private boolean sliderBlockedByPopup(double px, double py) {
        if (!popupSystem.renderingColorPopup && insideOpenColorPopup(px, py)) {
            return true;
        }
        if (!popupSystem.renderingPopup && !popupSystem.renderingColorPopup && insideOpenSettingsPopup(px, py)) {
            return true;
        }
        return insideOpenHotkeyPopup(px, py)
                || insideOpenHotkeyDetailPopup(px, py)
                || insideOpenHotkeyValuePopup(px, py)
                || insideOpenMenuPopup(px, py)
                || insideOpenMultiSelectPopup(px, py)
                || insideOpenSelectPopup(px, py);
    }

    private void handlePopupClick() {
        if (click && !clickConsumed && popupSystem.openMenuPopup != null) {
            MenuPopup popup = popupSystem.openMenuPopup;
            if (!insideOpenMenuPopup(clickX, clickY)) {
                closeMenuPopupVisual();
                clickConsumed = true;
                return;
            }
        }

        if (click && !clickConsumed && hasOpenHotkeyValuePopup()) {
            if (handleHotkeyValuePopupClick()) {
                return;
            }
        }

        if (click && !clickConsumed && (popupSystem.openHotkeyDetailPopup != null || popupSystem.openHotkeyPopup != null)) {
            if (handleHotkeyPopupClick()) {
                return;
            }
        }

        if (click && !clickConsumed && popupSystem.openColorPopup != null) {
            ColorPopup popup = popupSystem.openColorPopup;
            boolean insidePopup = insideOpenColorPopup(clickX, clickY);
            boolean insideButton = PopupGeometry.contains(clickX, clickY, popup.buttonX, popup.buttonY, COLOR_BUTTON_W, FRAME_HEIGHT);
            if (insidePopup) {
                return;
            }
            if (!insideButton) {
                popupSystem.openColorId = null;
                closeColorPopupVisual();
                clickConsumed = true;
                return;
            }
        }

        if (click && !clickConsumed && popupSystem.openSelectPopup != null) {
            SelectPopup popup = popupSystem.openSelectPopup;
            int h = popupHeight(popup.options.length);
            if (!PopupGeometry.contains(clickX, clickY, popup.x, popup.y, popup.w, h)) {
                popupSystem.openSelectId = null;
                closeSelectPopupVisual();
                clickConsumed = true;
                return;
            }
            for (int i = 0; i < popup.options.length; i++) {
                int optionY = popup.y + SELECT_POPUP_PAD_Y + i * (SELECT_OPTION_HEIGHT + SELECT_OPTION_GAP);
                if (PopupGeometry.contains(clickX, clickY, popup.x, optionY, popup.w, SELECT_OPTION_HEIGHT)) {
                    String selected = popup.options[i];
                    widgetRegistry.selectValues.put(popup.id, selected);
                    if (popup.applier != null) {
                        popup.applier.applySelect(selected);
                    }
                    popupSystem.openSelectId = null;
                    closeSelectPopupVisual();
                    clickConsumed = true;
                    return;
                }
            }
            clickConsumed = true;
            return;
        }

        if (click && !clickConsumed && popupSystem.openMultiSelectPopup != null) {
            MultiSelectPopup popup = popupSystem.openMultiSelectPopup;
            int h = popupHeight(popup.options.length);
            if (!PopupGeometry.contains(clickX, clickY, popup.x, popup.y, popup.w, h)) {
                popupSystem.openMultiSelectId = null;
                closeMultiSelectPopupVisual();
                clickConsumed = true;
                return;
            }
            for (int i = 0; i < popup.options.length; i++) {
                int optionY = popup.y + SELECT_POPUP_PAD_Y + i * (SELECT_OPTION_HEIGHT + SELECT_OPTION_GAP);
                if (PopupGeometry.contains(clickX, clickY, popup.x, optionY, popup.w, SELECT_OPTION_HEIGHT)) {
                    int bit = 1 << i;
                    int nextMask = popup.selectedMask ^ bit;
                    widgetRegistry.multiSelectMasks.put(popup.id, nextMask & WidgetSurfaceValueTools.optionMask(popup.options.length));
                    clickConsumed = true;
                    return;
                }
            }
            clickConsumed = true;
            return;
        }

        if (click && !clickConsumed && popupSystem.openSettingsPopup != null) {
            SettingsPopup popup = popupSystem.openSettingsPopup;
            int h = settingsPopupHeight(popup.frame.cardHeight());
            if (!PopupGeometry.contains(clickX, clickY, popup.x, popup.y, popup.w, h)) {
                popupSystem.openSettingsId = null;
                closeSettingsPopupVisual();
                popupSystem.openColorId = null;
                closeColorPopupVisual();
                popupSystem.openSelectId = null;
                closeSelectPopupVisual();
                popupSystem.openMultiSelectId = null;
                closeMultiSelectPopupVisual();
                clickConsumed = true;
                return;
            }
        }
    }

    private static float clamp01(float value) {
        return UiMath.clamp01(value);
    }

    private static float smoothstep(float value) {
        return UiMath.smoothstep(value);
    }

    private static String sliderValueLabel(float value) {
        return WidgetSurfaceValueTools.sliderValueLabel(value);
    }

    private SliderValueAdapter sliderValueAdapter(float defaultValue, String suffix,
                                                  Function<Float, String> valueFormatter,
                                                  Function<String, Float> valueParser,
                                                  Consumer<Float> applier) {
        Function<Float, String> formatter = valueFormatter != null
                ? valueFormatter
                : WidgetSurfaceValueTools.fallbackSliderFormatter(defaultValue, suffix);
        Function<String, Float> parser = valueParser != null
                ? valueParser
                : WidgetSurfaceValueTools.fallbackSliderParser(defaultValue, suffix);
        return new SliderValueAdapter(formatter, parser, applier);
    }

    private void startSliderEdit(String id, String valueText, SliderValueAdapter adapter, int x, int y, int w, int h) {
        editingSliderId = id;
        editingSliderText = valueText == null ? "" : valueText;
        editingSliderAdapter = adapter;
        editingSliderReplaceOnType = false;
        editingSliderX = x;
        editingSliderY = y;
        editingSliderW = w;
        editingSliderH = h;
        updateGameMovementInputBlock();
    }

    private void commitSliderEdit() {
        if (editingSliderId != null && editingSliderAdapter != null) {
            Float value = editingSliderAdapter.parse(editingSliderText);
            if (value != null && Float.isFinite(value)) {
                float next = clamp01(value);
                widgetRegistry.sliderValues.put(editingSliderId, next);
                if (editingSliderAdapter.applier != null) {
                    editingSliderAdapter.applier.accept(next);
                }
            }
        }
        cancelSliderEdit();
    }

    private void cancelSliderEdit() {
        editingSliderId = null;
        editingSliderText = "";
        editingSliderAdapter = null;
        editingSliderReplaceOnType = false;
        editingSliderX = 0;
        editingSliderY = 0;
        editingSliderW = 0;
        editingSliderH = 0;
        updateGameMovementInputBlock();
    }

    private void startHotkeySliderEdit(String controlId, HotkeyBinding binding, String valueText,
                                       int x, int y, int w, int h) {
        editingHotkeySliderControlId = controlId;
        editingHotkeySliderBindingId = binding.id;
        editingHotkeySliderText = valueText == null ? "" : valueText;
        editingHotkeySliderParser = binding.sliderParser != null
                ? binding.sliderParser
                : hotkeyMetas.getOrDefault(controlId, HotkeyMeta.EMPTY).sliderParser;
        activeId = null;
        editingHotkeySliderX = x;
        editingHotkeySliderY = y;
        editingHotkeySliderW = w;
        editingHotkeySliderH = h;
        updateGameMovementInputBlock();
    }

    private void commitHotkeySliderEdit() {
        HotkeyBinding binding = findHotkeyBinding(editingHotkeySliderControlId, editingHotkeySliderBindingId);
        if (binding != null) {
            Float value = null;
            if (editingHotkeySliderParser != null) {
                value = editingHotkeySliderParser.apply(editingHotkeySliderText);
            }
            if (value == null) {
                value = WidgetSurfaceValueTools.parseSliderNumber(editingHotkeySliderText);
            }
            if (value != null && Float.isFinite(value)) {
                binding.sliderValue = clamp01(value);
                binding.valueLabel = hotkeySliderValueLabel(editingHotkeySliderControlId, binding);
            }
        }
        cancelHotkeySliderEdit();
    }

    private void cancelHotkeySliderEdit() {
        editingHotkeySliderControlId = null;
        editingHotkeySliderBindingId = -1;
        editingHotkeySliderText = "";
        editingHotkeySliderParser = null;
        editingHotkeySliderX = 0;
        editingHotkeySliderY = 0;
        editingHotkeySliderW = 0;
        editingHotkeySliderH = 0;
        updateGameMovementInputBlock();
    }

    private boolean isEditingHotkeySlider(String controlId, int bindingId) {
        return controlId != null
                && controlId.equals(editingHotkeySliderControlId)
                && bindingId == editingHotkeySliderBindingId;
    }

    private void focusTextInput(String id, int x, int y, int w, int h) {
        String text = widgetRegistry.textValues.getOrDefault(id, "");
        focusSystem.focusTextInput(id, x, y, w, h, text.length());
        activeId = null;
        updateGameMovementInputBlock();
    }

    private void drawInputTextValue(GuiGraphics graphics, String id, String value,
                                    int textX, int textY, int maxWidth, int fieldY, int fieldH) {
        drawInputTextValue(graphics, id, value, textX, textY, maxWidth, fieldY, fieldH, false);
    }

    private void drawInputTextValue(GuiGraphics graphics, String id, String value,
                                    int textX, int textY, int maxWidth, int fieldY, int fieldH,
                                    boolean msdfText) {
        if (maxWidth <= 0) {
            return;
        }
        boolean focused = id.equals(focusSystem.textInputId);
        if (!focused) {
            drawInputClippedText(graphics, value, textX, textY, maxWidth, TEXT, msdfText);
            return;
        }

        normalizeTextInputSelection(value);
        int cursor = focusSystem.cursor;
        int visibleStart = 0;
        int innerWidth = Math.max(1, maxWidth - 2);
        while (visibleStart < cursor
                && inputTextWidth(value.substring(visibleStart, cursor), msdfText) > innerWidth) {
            visibleStart = nextCodePointIndex(value, visibleStart);
        }

        int visibleEnd = visibleStart;
        while (visibleEnd < value.length()) {
            int next = nextCodePointIndex(value, visibleEnd);
            if (inputTextWidth(value.substring(visibleStart, next), msdfText) > innerWidth) {
                break;
            }
            visibleEnd = next;
        }

        int selectionStart = Math.max(visibleStart, focusSystem.selectionStart());
        int selectionEnd = Math.min(visibleEnd, focusSystem.selectionEnd());
        if (selectionEnd > selectionStart) {
            int selectionX = textX + inputTextWidth(
                    value.substring(visibleStart, selectionStart), msdfText);
            int selectionW = Math.max(1, inputTextWidth(
                    value.substring(selectionStart, selectionEnd), msdfText));
            graphics.fill(selectionX, fieldY + 4, selectionX + selectionW,
                    fieldY + fieldH - 4, alphaColor(ACCENT, renderAlpha * 0.28f));
        }

        drawInputClippedText(graphics, value.substring(visibleStart, visibleEnd),
                textX, textY, maxWidth, TEXT, msdfText);
        if (((System.currentTimeMillis() / 500L) & 1L) == 0L) {
            int cursorX = textX + inputTextWidth(
                    value.substring(visibleStart, cursor), msdfText);
            graphics.vLine(cursorX + 1, fieldY + 5, fieldY + fieldH - 6,
                    alphaColor(ACCENT_DARK, renderAlpha));
        }
    }

    private void normalizeTextInputSelection(String text) {
        focusSystem.cursor = normalizeTextIndex(text, focusSystem.cursor);
        focusSystem.selectionAnchor = normalizeTextIndex(text, focusSystem.selectionAnchor);
    }

    private void copyTextInputSelection(String text) {
        if (!focusSystem.hasSelection()) {
            return;
        }
        Minecraft.getInstance().keyboardHandler.setClipboard(
                text.substring(focusSystem.selectionStart(), focusSystem.selectionEnd()));
    }

    private boolean deleteTextInputSelection(String text) {
        if (!focusSystem.hasSelection()) {
            return false;
        }
        deleteTextInputRange(text, focusSystem.selectionStart(), focusSystem.selectionEnd());
        return true;
    }

    private void deleteTextInputRange(String text, int start, int end) {
        int safeStart = normalizeTextIndex(text, Math.min(start, end));
        int safeEnd = normalizeTextIndex(text, Math.max(start, end));
        String next = text.substring(0, safeStart) + text.substring(safeEnd);
        widgetRegistry.textValues.put(focusSystem.textInputId, next);
        focusSystem.moveCursor(safeStart, false, next.length());
    }

    private void replaceTextInputSelection(String insertedText) {
        if (focusSystem.textInputId == null || insertedText == null || insertedText.isEmpty()) {
            return;
        }
        String current = widgetRegistry.textValues.getOrDefault(focusSystem.textInputId, "");
        normalizeTextInputSelection(current);
        int start = focusSystem.selectionStart();
        int end = focusSystem.selectionEnd();
        String clean = sanitizeTextInput(insertedText);
        int retainedCodePoints = current.codePointCount(0, start)
                + current.codePointCount(end, current.length());
        clean = limitCodePoints(clean, Math.max(0, 128 - retainedCodePoints));
        if (clean.isEmpty()) {
            return;
        }
        String next = current.substring(0, start) + clean + current.substring(end);
        widgetRegistry.textValues.put(focusSystem.textInputId, next);
        focusSystem.moveCursor(start + clean.length(), false, next.length());
    }

    static String sanitizeTextInput(String value) {
        StringBuilder clean = new StringBuilder(value.length());
        value.codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint)
                        && Character.getType(codePoint) != Character.LINE_SEPARATOR
                        && Character.getType(codePoint) != Character.PARAGRAPH_SEPARATOR)
                .forEach(clean::appendCodePoint);
        return clean.toString();
    }

    static String limitCodePoints(String value, int maxCodePoints) {
        if (maxCodePoints <= 0) {
            return "";
        }
        int count = value.codePointCount(0, value.length());
        if (count <= maxCodePoints) {
            return value;
        }
        return value.substring(0, value.offsetByCodePoints(0, maxCodePoints));
    }

    static int normalizeTextIndex(String value, int index) {
        int safe = Math.max(0, Math.min(index, value.length()));
        if (safe > 0 && safe < value.length()
                && Character.isHighSurrogate(value.charAt(safe - 1))
                && Character.isLowSurrogate(value.charAt(safe))) {
            safe--;
        }
        return safe;
    }

    static int previousCodePointIndex(String value, int index) {
        int safe = normalizeTextIndex(value, index);
        return safe <= 0 ? 0 : Character.offsetByCodePoints(value, safe, -1);
    }

    static int nextCodePointIndex(String value, int index) {
        int safe = normalizeTextIndex(value, index);
        return safe >= value.length() ? value.length() : Character.offsetByCodePoints(value, safe, 1);
    }

    static int previousWordIndex(String value, int index) {
        int cursor = normalizeTextIndex(value, index);
        while (cursor > 0) {
            int previous = previousCodePointIndex(value, cursor);
            if (!Character.isWhitespace(value.codePointAt(previous))) {
                break;
            }
            cursor = previous;
        }
        if (cursor <= 0) {
            return 0;
        }
        int previous = previousCodePointIndex(value, cursor);
        boolean word = isWordCodePoint(value.codePointAt(previous));
        while (cursor > 0) {
            previous = previousCodePointIndex(value, cursor);
            int codePoint = value.codePointAt(previous);
            if (Character.isWhitespace(codePoint) || isWordCodePoint(codePoint) != word) {
                break;
            }
            cursor = previous;
        }
        return cursor;
    }

    static int nextWordIndex(String value, int index) {
        int cursor = normalizeTextIndex(value, index);
        if (cursor >= value.length()) {
            return value.length();
        }
        boolean word = isWordCodePoint(value.codePointAt(cursor));
        while (cursor < value.length()) {
            int codePoint = value.codePointAt(cursor);
            if (Character.isWhitespace(codePoint) || isWordCodePoint(codePoint) != word) {
                break;
            }
            cursor = nextCodePointIndex(value, cursor);
        }
        while (cursor < value.length() && Character.isWhitespace(value.codePointAt(cursor))) {
            cursor = nextCodePointIndex(value, cursor);
        }
        return cursor;
    }

    private static boolean isWordCodePoint(int codePoint) {
        return Character.isLetterOrDigit(codePoint) || codePoint == '_';
    }

    private void clearTextInputFocus() {
        focusSystem.clearTextInputFocus();
        pendingTextInputHighSurrogate = 0;
        updateGameMovementInputBlock();
    }

    private void closeFieldPopups() {
        popupSystem.openSelectId = null;
        closeSelectPopupVisual();
        popupSystem.openMultiSelectId = null;
        closeMultiSelectPopupVisual();
        popupSystem.openColorId = null;
        closeColorPopupVisual();
        closeHotkeyPopups();
    }

    private static int popupHeight(int optionCount) {
        if (optionCount <= 0) {
            return SELECT_POPUP_PAD_Y * 2;
        }
        return SELECT_POPUP_PAD_Y * 2 + optionCount * SELECT_OPTION_HEIGHT + (optionCount - 1) * SELECT_OPTION_GAP;
    }

    private static int settingsPopupHeight(int contentHeight) {
        return SETTINGS_POPUP_PAD_Y * 2 + Math.max(0, contentHeight - CARD_CONTENT_TOP - CARD_BOTTOM_PAD);
    }

    private static int colorPopupHeight() {
        return colorPopupHeight(true);
    }

    private static int colorPopupHeight(boolean activeAlpha) {
        int rows = activeAlpha ? 4 : 3;
        return COLOR_POPUP_PAD_Y * 2 + COLOR_POPUP_PREVIEW_H + ITEM_INNER_SPACING_Y
                + COLOR_ROW_HEIGHT * rows + COLOR_ROW_GAP * (rows - 1);
    }

    private void registerHotkeyArea(String id, String label, String kind, String valueLabel, Boolean boolValue,
                                    int x, int y, int w, int h) {
        registerHotkeyArea(id, label, kind, valueLabel, boolValue, x, y, w, h,
                boolValue == null ? HotkeyValueType.LABEL : HotkeyValueType.BOOL,
                valueLabel, null, 0, 0.0f, 0, null);
    }

    private void registerHotkeyArea(String id, String label, String kind, String valueLabel, Boolean boolValue,
                                    int x, int y, int w, int h, HotkeyApplier applier) {
        registerHotkeyArea(id, label, kind, valueLabel, boolValue, x, y, w, h,
                boolValue == null ? HotkeyValueType.LABEL : HotkeyValueType.BOOL,
                valueLabel, null, 0, 0.0f, 0, applier);
    }

    private void registerHotkeyArea(String id, String label, String kind, String valueLabel, Boolean boolValue,
                                    int x, int y, int w, int h, HotkeyValueType valueType, String selectedValue,
                                    String[] options, int multiMask, float sliderValue, int colorValue) {
        registerHotkeyArea(id, label, kind, valueLabel, boolValue, x, y, w, h, valueType, selectedValue,
                options, multiMask, sliderValue, colorValue, null);
    }

    private void registerHotkeyArea(String id, String label, String kind, String valueLabel, Boolean boolValue,
                                    int x, int y, int w, int h, HotkeyValueType valueType, String selectedValue,
                                    String[] options, int multiMask, float sliderValue, int colorValue,
                                    HotkeyApplier applier) {
        registerHotkeyArea(id, label, kind, valueLabel, boolValue, x, y, w, h, valueType, selectedValue,
                options, multiMask, sliderValue, colorValue, applier, null);
    }

    private void registerHotkeyArea(String id, String label, String kind, String valueLabel, Boolean boolValue,
                                    int x, int y, int w, int h, HotkeyValueType valueType, String selectedValue,
                                    String[] options, int multiMask, float sliderValue, int colorValue,
                                    HotkeyApplier applier, Function<Float, String> sliderFormatter) {
        registerHotkeyArea(id, label, kind, valueLabel, boolValue, x, y, w, h, valueType, selectedValue,
                options, multiMask, sliderValue, colorValue, applier, sliderFormatter, null);
    }

    private void registerHotkeyArea(String id, String label, String kind, String valueLabel, Boolean boolValue,
                                    int x, int y, int w, int h, HotkeyValueType valueType, String selectedValue,
                                    String[] options, int multiMask, float sliderValue, int colorValue,
                                    HotkeyApplier applier, Function<Float, String> sliderFormatter,
                                    Function<String, Float> sliderParser) {
        registerHotkeyArea(id, label, kind, valueLabel, boolValue, x, y, w, h, valueType, selectedValue,
                options, multiMask, sliderValue, colorValue, applier, sliderFormatter, sliderParser,
                HotkeyTextFormatter.groupFromId(id));
    }

    private void registerHotkeyArea(String id, String label, String kind, String valueLabel, Boolean boolValue,
                                    int x, int y, int w, int h, HotkeyValueType valueType, String selectedValue,
                                    String[] options, int multiMask, float sliderValue, int colorValue,
                                    HotkeyApplier applier, Function<Float, String> sliderFormatter,
                                    Function<String, Float> sliderParser, String group) {
        registerHotkeyArea(id, label, kind, valueLabel, boolValue, x, y, w, h, valueType, selectedValue,
                options, multiMask, sliderValue, colorValue, applier, sliderFormatter, sliderParser, group, true);
    }

    private void registerHotkeyArea(String id, String label, String kind, String valueLabel, Boolean boolValue,
                                    int x, int y, int w, int h, HotkeyValueType valueType, String selectedValue,
                                    String[] options, int multiMask, float sliderValue, int colorValue,
                                    HotkeyApplier applier, Function<Float, String> sliderFormatter,
                                    Function<String, Float> sliderParser, String group, boolean interactive) {
        if (!interactive && !hotkeyPreregistering) {
            return;
        }
        hotkeyStates.computeIfAbsent(id, key -> new HotkeyControlState());
        putHotkeyMeta(id, label, kind, valueLabel, boolValue, valueType, selectedValue, options, multiMask,
                sliderValue, colorValue, applier, sliderFormatter, sliderParser, group);
        if (!interactive) {
            return;
        }
        if (rightClicked(x, y, w, h)) {
            HotkeyControlState state = hotkeyStates.computeIfAbsent(id, key -> new HotkeyControlState());
            boolean openNow = !id.equals(popupSystem.openHotkeyId);
            popupSystem.openHotkeyId = openNow ? id : null;
            if (openNow && popupSystem.openHotkeyPopup != null && !id.equals(popupSystem.openHotkeyPopup.id)) {
                closeHotkeyPopupVisual();
            }
            clearHotkeyDetail();
            clearListeningHotkey();
            if (openNow) {
                state.anchor = hotkeyAnchorAtClick();
                popupSystem.openSelectId = null;
                closeSelectPopupVisual();
                popupSystem.openMultiSelectId = null;
                closeMultiSelectPopupVisual();
                popupSystem.openColorId = null;
                closeColorPopupVisual();
            }
        }

        if (id.equals(popupSystem.openHotkeyId)) {
            HotkeyAnchor anchor = hotkeyStates.computeIfAbsent(id, key -> new HotkeyControlState()).anchor;
            int popupX = anchor == null ? x : anchor.x;
            int popupY = anchor == null ? y : anchor.y;
            popupSystem.pendingHotkeyPopup = new HotkeyPopup(id, popupX, popupY);
        }
    }

    private static void putHotkeyMeta(String id, String label, String kind, String valueLabel, Boolean boolValue,
                                      HotkeyValueType valueType, String selectedValue, String[] options,
                                      int multiMask, float sliderValue, int colorValue, HotkeyApplier applier,
                                      Function<Float, String> sliderFormatter, Function<String, Float> sliderParser,
                                      String group) {
        hotkeyMetas.put(id, new HotkeyMeta(HotkeyTextFormatter.cleanGroup(group), label, kind, valueLabel, boolValue,
                valueType, selectedValue, options == null ? null : options.clone(), multiMask, sliderValue,
                colorValue, applier, sliderFormatter, sliderParser));
    }


    private void renderHotkeyPopup(GuiGraphics graphics, HotkeyPopup popup) {
        HotkeyControlState state = hotkeyStates.computeIfAbsent(popup.id, key -> new HotkeyControlState());
        float[] bindingReveals = hotkeyBindingReveals(popup.id, state);
        boolean resetDeleting = stateResetDeleting(state);
        int h = animatedHotkeyPopupHeight(bindingReveals, resetDeleting);
        drawPopupSurface(graphics, popup.x, popup.y, HOTKEY_POPUP_W, h, 7);
        float rowY = popup.y + HOTKEY_POPUP_PAD;
        boolean detailHovered = insideOpenHotkeyDetailPopup(mouseX, mouseY) || insideOpenHotkeyValuePopup(mouseX, mouseY);

        HotkeyBinding hoveredBinding = null;
        int hoveredBindingY = popup.y;
        int selectedBindingY = popup.y;
        for (int i = 0; i < state.bindings.size(); i++) {
            HotkeyBinding binding = state.bindings.get(i);
            float reveal = bindingReveals[i];
            int rowOffsetY = -Math.round((1.0f - reveal) * 10.0f);
            int drawY = Math.round(rowY) + rowOffsetY;
            boolean hovered = !binding.deleting && reveal > 0.92f && hotkeyPopupRowHovered(popup.x, drawY, HOTKEY_POPUP_W);
            boolean selected = popup.id.equals(popupSystem.openHotkeyDetailControlId) && binding.id == popupSystem.openHotkeyDetailBindingId;
            if (hovered) {
                hoveredBinding = binding;
                hoveredBindingY = drawY;
                selected = true;
            }
            if (selected) {
                selectedBindingY = drawY;
            }
            float previousAlpha = renderAlpha;
            renderAlpha = previousAlpha * reveal;
            ShapeRenderer.setGlobalAlpha(renderAlpha);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, renderAlpha);
            drawHotkeyMenuRow(graphics, popup.x, drawY, HOTKEY_POPUP_W, FontAwesomeIcons.ICON_FA_KEYBOARD,
                    binding.bound() ? binding.keyName : "Unbound", true, TEXT, hovered,
                    selected || detailHovered && selected);
            renderAlpha = previousAlpha;
            ShapeRenderer.setGlobalAlpha(previousAlpha);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, previousAlpha);
            rowY += bindingRowHeight(reveal, resetDeleting);
        }

        int actionY = Math.round(rowY);
        if (hoveredBinding != null) {
            if (!popup.id.equals(popupSystem.openHotkeyDetailControlId) || hoveredBinding.id != popupSystem.openHotkeyDetailBindingId) {
                closeHotkeyValuePopups();
            }
            popupSystem.openHotkeyDetailControlId = popup.id;
            popupSystem.openHotkeyDetailBindingId = hoveredBinding.id;
            popupSystem.pendingHotkeyDetailPopup = new HotkeyDetailPopup(popup.id, hoveredBinding.id,
                    popup.x + HOTKEY_POPUP_W - HOTKEY_DETAIL_OVERLAP, hoveredBindingY);
        } else if (hotkeyPrimaryActionHovered(popup, actionY)) {
            clearHotkeyDetail();
        } else if (popupSystem.openHotkeyDetailControlId != null && popupSystem.openHotkeyDetailControlId.equals(popup.id)) {
            popupSystem.pendingHotkeyDetailPopup = new HotkeyDetailPopup(popupSystem.openHotkeyDetailControlId, popupSystem.openHotkeyDetailBindingId,
                    popup.x + HOTKEY_POPUP_W - HOTKEY_DETAIL_OVERLAP, selectedBindingY);
        }

        drawHotkeyMenuRow(graphics, popup.x, actionY, HOTKEY_POPUP_W, FontAwesomeIcons.ICON_FA_CIRCLE_PLUS,
                "New Hotkey", false, MUTED, PopupGeometry.contains(mouseX, mouseY, popup.x + 4, actionY, HOTKEY_POPUP_W - 8, HOTKEY_ROW_H), false);
        actionY += HOTKEY_ROW_H;
        drawHotkeyMenuRow(graphics, popup.x, actionY, HOTKEY_POPUP_W, FontAwesomeIcons.ICON_FA_LIST,
                "All Hotkeys", false, TEXT, PopupGeometry.contains(mouseX, mouseY, popup.x + 4, actionY, HOTKEY_POPUP_W - 8, HOTKEY_ROW_H), false);
        actionY += HOTKEY_ROW_H + HOTKEY_DIVIDER_GAP;
        dividerLine(graphics, popup.x + HOTKEY_POPUP_PAD, actionY - HOTKEY_DIVIDER_GAP / 2, HOTKEY_POPUP_W - HOTKEY_POPUP_PAD * 2);
        drawHotkeyMenuRow(graphics, popup.x, actionY, HOTKEY_POPUP_W, FontAwesomeIcons.ICON_FA_ROTATE_LEFT,
                "Reset", false, DANGER, PopupGeometry.contains(mouseX, mouseY, popup.x + 4, actionY, HOTKEY_POPUP_W - 8, HOTKEY_ROW_H), false);
        removeFinishedDeletingHotkeys(popup.id, state, bindingReveals);
    }

    private void renderMenuPopup(GuiGraphics graphics, MenuPopup popup) {
        drawPopupSurface(graphics, popup.x, popup.y, HOTKEY_POPUP_W, menuPopupHeight(popup), 7);
        int rowY = popup.y + HOTKEY_POPUP_PAD;
        boolean dividerDrawn = false;
        popupSystem.renderingPopup = true;
        try {
            for (MenuItem item : popup.items) {
                if (item.danger && !dividerDrawn && rowY > popup.y + HOTKEY_POPUP_PAD) {
                    rowY += HOTKEY_DIVIDER_GAP;
                    dividerLine(graphics, popup.x + HOTKEY_POPUP_PAD,
                            rowY - HOTKEY_DIVIDER_GAP / 2,
                            HOTKEY_POPUP_W - HOTKEY_POPUP_PAD * 2);
                    dividerDrawn = true;
                }
                int rowX = popup.x + HOTKEY_POPUP_PAD;
                int rowW = HOTKEY_POPUP_W - HOTKEY_POPUP_PAD * 2;
                boolean hovered = PopupGeometry.contains(mouseX, mouseY, rowX, rowY, rowW, HOTKEY_ROW_H);
                String rowId = PopupGeometry.menuPopupId(popup.id) + "/" + item.label;
                if (button(rowId, rowX, rowY, rowW, HOTKEY_ROW_H)) {
                    item.action.run();
                }
                drawHotkeyMenuRow(graphics, popup.x, rowY, HOTKEY_POPUP_W, item.icon, item.label,
                        false, item.danger ? DANGER : TEXT, hovered, false);
                rowY += HOTKEY_ROW_H;
            }
        } finally {
            popupSystem.renderingPopup = false;
        }
    }

    private void renderAllHotkeysWindow(GuiGraphics graphics) {
        float targetAlpha = allHotkeysOpen ? 1.0f : 0.0f;
        float animationSpeed = allHotkeysOpen ? UiAnimationTimings.POPUP_OPEN : UiAnimationTimings.POPUP_CLOSE;
        allHotkeysAlpha = AnimationClock.lerp(allHotkeysAlpha, targetAlpha, animationSpeed);
        if (Math.abs(allHotkeysAlpha - targetAlpha) < 0.01f) {
            allHotkeysAlpha = targetAlpha;
        }
        if (!allHotkeysOpen && allHotkeysAlpha <= 0.0f) {
            return;
        }

        int x = allHotkeysWindowX();
        int y = allHotkeysWindowY();
        float previousAlpha = renderAlpha;
        float modalAlpha = previousAlpha * smoothstep(allHotkeysAlpha);
        renderAlpha = modalAlpha;
        ShapeRenderer.setGlobalAlpha(modalAlpha);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, modalAlpha);

        drawPopupBlur(graphics, x, y, ALL_HOTKEYS_W, ALL_HOTKEYS_H, 10);
        ShadowRenderer.shadow(graphics, x, y, ALL_HOTKEYS_W, ALL_HOTKEYS_H, 10,
                ShadowStyle.popup(), renderAlpha);
        ShapeRenderer.roundRect(graphics, x, y, ALL_HOTKEYS_W, ALL_HOTKEYS_H, 10, popupSurfaceColor());
        ShapeRenderer.roundRectBorder(graphics, x, y, ALL_HOTKEYS_W, ALL_HOTKEYS_H, 10, 1, BORDER);

        String title = "All Hotkeys";
        int titleW = textWidth(title, 14.0f);
        drawClippedText(graphics, title, x + (ALL_HOTKEYS_W - titleW) / 2, y + 14,
                Math.max(0, ALL_HOTKEYS_W - 80), 14.0f, TEXT);

        int closeSize = 24;
        int closeX = x + ALL_HOTKEYS_W - closeSize - 10;
        int closeY = y + 9;
        drawAllHotkeyCloseButton(graphics, "all-hotkeys/close", closeX, closeY, closeSize);

        int contentX = x + ALL_HOTKEYS_PAD;
        int contentY = y + ALL_HOTKEYS_TITLE_H;
        int contentH = ALL_HOTKEYS_H - ALL_HOTKEYS_TITLE_H - ALL_HOTKEYS_PAD;
        boolean hasScrollbar = allHotkeysMaxScrollY > 0;
        allHotkeysScrollbarReserve = ScrollbarControl.updateReserve(allHotkeysScrollbarReserve, hasScrollbar);
        ScrollbarControl.Layout scrollbarLayout = ScrollbarControl.layout(x, y, ALL_HOTKEYS_W,
                contentY, contentH, ALL_HOTKEYS_W - ALL_HOTKEYS_PAD * 2, allHotkeysScrollbarReserve);
        int contentW = scrollbarLayout.contentW();
        List<AllHotkeySection> sections = allHotkeySections();
        primeAllHotkeyRowAnimations(sections);
        int totalH = allHotkeySectionsHeight(sections);
        allHotkeysMaxScrollY = Math.max(0, totalH - contentH);
        allHotkeysScrollY = UiMath.clamp(allHotkeysScrollY, 0, allHotkeysMaxScrollY);
        allHotkeysAnimatedScrollY = updateAllHotkeyAnimatedScroll(allHotkeysAnimatedScrollY,
                allHotkeysScrollY, allHotkeysMaxScrollY);

        boolean clipped = pushRenderClip(contentX, contentY, contentW, contentH);
        try {
            if (sections.isEmpty()) {
                drawClippedText(graphics, "No bound hotkeys", contentX + 8, contentY + 16, contentW - 16, MUTED);
            } else {
                int drawY = contentY - Math.round(allHotkeysAnimatedScrollY);
                syncAllHotkeySectionAnimationsToBase(drawY);
                List<AllHotkeySectionEntry> animatedSections = updateAllHotkeySectionAnimations(sections, drawY);
                for (AllHotkeySectionEntry section : animatedSections) {
                    int sectionY = Math.round(section.animation.y);
                    int sectionH = allHotkeySectionAnimatedHeight(section.section, section.animation);
                    if (sectionY + sectionH >= contentY && sectionY <= contentY + contentH) {
                        renderAllHotkeySection(graphics, section.section, contentX, sectionY, contentW, section.animation.alpha);
                    }
                }
            }
        } finally {
            popRenderClip(clipped);
        }
        allHotkeysScrollbarAlpha = ScrollbarControl.updateAlpha(allHotkeysScrollbarAlpha, allHotkeysMaxScrollY > 0);
        if (allHotkeysScrollbarAlpha > 0.01f) {
            float progress = allHotkeysMaxScrollY <= 0 ? 0.0f : allHotkeysAnimatedScrollY / (float) allHotkeysMaxScrollY;
            ScrollbarControl.draw(graphics, scrollbarLayout.trackX(), scrollbarLayout.trackY(),
                    scrollbarLayout.trackH(), progress, contentH, totalH, allHotkeysScrollbarAlpha * renderAlpha);
        }

        renderAllHotkeyComboPopup(graphics);
        renderAllHotkeyValueColorOverlay(graphics);

        renderAlpha = previousAlpha;
        ShapeRenderer.setGlobalAlpha(previousAlpha);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, previousAlpha);
    }

    private void renderAllHotkeySection(GuiGraphics graphics, AllHotkeySection section, int x, int y, int w, float sectionAlpha) {
        float previousAlpha = renderAlpha;
        float alpha = previousAlpha * sectionAlpha;
        renderAlpha = alpha;
        ShapeRenderer.setGlobalAlpha(alpha);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        ShapeRenderer.roundRect(graphics, x, y, w, ALL_HOTKEYS_SECTION_TITLE_H, 7,
                themeColor(0xFFF3F6FC, 0xFF242B38));
        drawClippedText(graphics, section.title, x + 10, y + 7, w - 20, 10.0f, ACCENT_DARK);

        int headerY = y + ALL_HOTKEYS_SECTION_TITLE_H + 4;
        ShapeRenderer.roundRect(graphics, x, headerY, w, ALL_HOTKEYS_HEADER_H, 6,
                themeColor(0xFFE9EDF7, 0xFF1C222E));
        int[] cols = allHotkeyColumns(w);
        int colX = x;
        drawAllHotkeyHeader(graphics, "Feature", colX + 8, headerY, cols[0] - 12);
        colX += cols[0];
        drawAllHotkeyHeader(graphics, "Key", colX + 6, headerY, cols[1] - 10);
        colX += cols[1];
        drawAllHotkeyHeader(graphics, "Mode", colX + 6, headerY, cols[2] - 10);
        colX += cols[2];
        drawAllHotkeyHeader(graphics, "Visible", colX + 6, headerY, cols[3] - 10);
        colX += cols[3];
        drawAllHotkeyHeader(graphics, "New Value", colX + 6, headerY, cols[4] - 10);

        int firstRowY = headerY + ALL_HOTKEYS_HEADER_H + 2;
        List<AllHotkeyRowEntry> rows = updateAllHotkeyRowAnimations(section, firstRowY);
        for (AllHotkeyRowEntry row : rows) {
            int drawY = Math.round(row.animation.y);
            float rowAlpha = renderAlpha * row.animation.alpha;
            float previousRowAlpha = renderAlpha;
            renderAlpha = rowAlpha;
            ShapeRenderer.setGlobalAlpha(rowAlpha);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, rowAlpha);
            renderAllHotkeyTableRow(graphics, row.draft, x, drawY, w, cols);
            renderAlpha = previousRowAlpha;
            ShapeRenderer.setGlobalAlpha(previousRowAlpha);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, previousRowAlpha);
        }
        renderAlpha = previousAlpha;
        ShapeRenderer.setGlobalAlpha(previousAlpha);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, previousAlpha);
    }

    private List<AllHotkeySectionEntry> updateAllHotkeySectionAnimations(List<AllHotkeySection> sections, int firstY) {
        Set<String> visibleIds = new HashSet<>();
        List<AllHotkeySectionEntry> entries = new ArrayList<>();
        int targetY = firstY;
        for (AllHotkeySection section : sections) {
            String id = section.title;
            visibleIds.add(id);
            AllHotkeySectionAnimation animation = allHotkeySectionAnimations.get(id);
            if (animation == null) {
                animation = new AllHotkeySectionAnimation(section, targetY + 12);
                allHotkeySectionAnimations.put(id, animation);
            }
            animation.section = section;
            animation.targetY = targetY;
            animation.deleting = !allHotkeySectionHasActiveRows(section);
            updateAllHotkeySectionAnimation(animation, false);
            entries.add(new AllHotkeySectionEntry(section, animation));
            targetY += allHotkeySectionAnimatedHeight(section, animation) + allHotkeySectionAnimatedGap(animation);
        }

        for (Map.Entry<String, AllHotkeySectionAnimation> entry : new ArrayList<>(allHotkeySectionAnimations.entrySet())) {
            if (visibleIds.contains(entry.getKey())) {
                continue;
            }
            AllHotkeySectionAnimation animation = entry.getValue();
            if (!animation.deleting) {
                animation.deleting = true;
                animation.targetY = Math.round(animation.y);
            }
            updateAllHotkeySectionAnimation(animation, false);
            if (animation.alpha <= 0.02f) {
                allHotkeySectionAnimations.remove(entry.getKey());
                continue;
            }
            entries.add(new AllHotkeySectionEntry(animation.section, animation));
        }

        entries.sort((left, right) -> Float.compare(left.animation.y, right.animation.y));
        removeFinishedAllHotkeySections();
        return entries;
    }

    private void updateAllHotkeySectionAnimation(AllHotkeySectionAnimation animation, boolean snapY) {
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

    private void syncAllHotkeySectionAnimationsToBase(int baseY) {
        if (lastAllHotkeySectionAnimationBaseY == Integer.MIN_VALUE) {
            lastAllHotkeySectionAnimationBaseY = baseY;
            return;
        }
        int deltaY = baseY - lastAllHotkeySectionAnimationBaseY;
        lastAllHotkeySectionAnimationBaseY = baseY;
        if (deltaY == 0) {
            return;
        }
        for (AllHotkeySectionAnimation animation : allHotkeySectionAnimations.values()) {
            animation.y += deltaY;
            animation.targetY += deltaY;
        }
        for (AllHotkeyRowAnimation animation : allHotkeyRowAnimations.values()) {
            animation.y += deltaY;
            animation.targetY += deltaY;
        }
    }

    private void removeFinishedAllHotkeySections() {
        allHotkeySectionAnimations.entrySet().removeIf(entry -> {
            AllHotkeySectionAnimation animation = entry.getValue();
            return animation == null || animation.alpha <= 0.02f && animation.deleting;
        });
    }

    private boolean allHotkeySectionHasActiveRows(AllHotkeySection section) {
        for (AllHotkeyDraft draft : section.rows) {
            if (!draft.deleted) {
                return true;
            }
        }
        return false;
    }

    private int allHotkeySectionAnimatedHeight(AllHotkeySection section, AllHotkeySectionAnimation animation) {
        float alpha = animation == null ? 1.0f : animation.alpha;
        int chromeH = ALL_HOTKEYS_SECTION_TITLE_H + 4 + ALL_HOTKEYS_HEADER_H + 2;
        int rowsH = 0;
        for (AllHotkeyDraft row : section.rows) {
            rowsH += allHotkeyRowAnimatedHeight(row);
        }
        return Math.max(0, Math.round(chromeH * clamp01(alpha)) + rowsH);
    }

    private int allHotkeySectionAnimatedGap(AllHotkeySectionAnimation animation) {
        float alpha = animation == null ? 1.0f : animation.alpha;
        return Math.max(0, Math.round(ALL_HOTKEYS_SECTION_GAP * clamp01(alpha)));
    }

    private void primeAllHotkeyRowAnimations(List<AllHotkeySection> sections) {
        Set<String> currentIds = new HashSet<>();
        for (AllHotkeySection section : sections) {
            for (AllHotkeyDraft draft : section.rows) {
                String id = draft.animationId();
                currentIds.add(id);
                AllHotkeyRowAnimation animation = allHotkeyRowAnimations.get(id);
                if (animation == null) {
                    animation = new AllHotkeyRowAnimation(draft, 0);
                    allHotkeyRowAnimations.put(id, animation);
                }
                animation.draft = draft;
                animation.deleting = draft.deleted;
                updateAllHotkeyRowAlpha(animation);
            }
        }
        for (Map.Entry<String, AllHotkeyRowAnimation> entry : new ArrayList<>(allHotkeyRowAnimations.entrySet())) {
            if (currentIds.contains(entry.getKey())) {
                continue;
            }
            AllHotkeyRowAnimation animation = entry.getValue();
            animation.deleting = true;
            updateAllHotkeyRowAlpha(animation);
            if (animation.alpha <= 0.02f) {
                allHotkeyRowAnimations.remove(entry.getKey());
            }
        }
    }

    private List<AllHotkeyRowEntry> updateAllHotkeyRowAnimations(AllHotkeySection section, int firstY) {
        Set<String> visibleIds = new HashSet<>();
        List<AllHotkeyRowEntry> rows = new ArrayList<>();
        int targetY = firstY;
        for (AllHotkeyDraft draft : section.rows) {
            String id = draft.animationId();
            AllHotkeyRowAnimation animation = allHotkeyRowAnimations.get(id);
            if (animation == null) {
                animation = new AllHotkeyRowAnimation(draft, targetY + 12);
                allHotkeyRowAnimations.put(id, animation);
            }
            animation.draft = draft;
            animation.deleting = draft.deleted;
            animation.targetY = targetY;
            updateAllHotkeyRowPosition(animation);
            if (animation.alpha > 0.02f || !draft.deleted) {
                visibleIds.add(id);
                rows.add(new AllHotkeyRowEntry(draft, animation));
                targetY += allHotkeyRowAnimatedHeight(draft);
            }
        }

        for (Map.Entry<String, AllHotkeyRowAnimation> entry : new ArrayList<>(allHotkeyRowAnimations.entrySet())) {
            if (visibleIds.contains(entry.getKey())) {
                continue;
            }
            AllHotkeyRowAnimation animation = entry.getValue();
            AllHotkeyDraft draft = animation.draft;
            if (draft == null || !draft.section.equals(section.title)) {
                continue;
            }
            updateAllHotkeyRowPosition(animation);
            if (animation.alpha <= 0.02f) {
                allHotkeyRowAnimations.remove(entry.getKey());
                continue;
            }
            rows.add(new AllHotkeyRowEntry(draft, animation));
        }

        rows.sort((left, right) -> Float.compare(left.animation.y, right.animation.y));
        removeFinishedAllHotkeyRows();
        return rows;
    }

    private void updateAllHotkeyRowPosition(AllHotkeyRowAnimation animation) {
        float targetY = animation.targetY;
        animation.y = targetY;
    }

    private void updateAllHotkeyRowAlpha(AllHotkeyRowAnimation animation) {
        float targetAlpha = animation.deleting ? 0.0f : 1.0f;
        animation.alpha = AnimationClock.lerp(animation.alpha, targetAlpha, UiAnimationTimings.POPUP_CLOSE);
        if (Math.abs(animation.alpha - targetAlpha) < 0.01f) {
            animation.alpha = targetAlpha;
        }
    }

    private void syncAllHotkeyRowAnimationsToBase(int baseY) {
        if (lastAllHotkeyAnimationBaseY == Integer.MIN_VALUE) {
            lastAllHotkeyAnimationBaseY = baseY;
            return;
        }
        int deltaY = baseY - lastAllHotkeyAnimationBaseY;
        lastAllHotkeyAnimationBaseY = baseY;
        if (deltaY == 0) {
            return;
        }
        for (AllHotkeyRowAnimation animation : allHotkeyRowAnimations.values()) {
            animation.y += deltaY;
            animation.targetY += deltaY;
        }
    }

    private int allHotkeyRowAnimatedHeight(AllHotkeyDraft draft) {
        AllHotkeyRowAnimation animation = allHotkeyRowAnimations.get(draft.animationId());
        float alpha = animation == null ? (draft.deleted ? 0.0f : 1.0f) : animation.alpha;
        return Math.max(0, Math.round(ALL_HOTKEYS_ROW_H * clamp01(alpha)));
    }

    private float updateAllHotkeyAnimatedScroll(float current, int target, int maxScroll) {
        if (maxScroll <= 0) {
            return 0.0f;
        }
        float clampedCurrent = Math.max(0.0f, Math.min(current, maxScroll));
        float next = AnimationClock.lerp(clampedCurrent, target, UiAnimationTimings.CONTENT_SCROLL);
        if (Math.abs(next - target) < 0.35f) {
            next = target;
        }
        return Math.max(0.0f, Math.min(next, maxScroll));
    }

    private void removeFinishedAllHotkeyRows() {
        for (int i = allHotkeyDrafts.size() - 1; i >= 0; i--) {
            AllHotkeyDraft draft = allHotkeyDrafts.get(i);
            AllHotkeyRowAnimation animation = allHotkeyRowAnimations.get(draft.animationId());
            if (draft.deleted && (animation == null || animation.alpha <= 0.02f)) {
                deleteHotkeyBinding(draft.controlId, draft.bindingId);
                allHotkeyRowAnimations.remove(draft.animationId());
                allHotkeyDrafts.remove(i);
            }
        }
    }

    private void renderAllHotkeyTableRow(GuiGraphics graphics, AllHotkeyDraft draft, int x, int y, int w, int[] cols) {
        boolean hovered = PopupGeometry.contains(mouseX, mouseY, x, y, w, ALL_HOTKEYS_ROW_H);
        float hover = controlAnim("all-hotkeys/row/" + draft.controlId + "/" + draft.bindingId, "hover", hovered ? 1.0f : 0.0f);
        ShapeRenderer.roundRect(graphics, x, y, w, ALL_HOTKEYS_ROW_H - 2, 6,
                lerpColor(themeColor(0xF9FFFFFF, 0xDD202633), themeColor(0xFFFFFFFF, 0xF02B3342), hover));
        ShapeRenderer.roundRectBorder(graphics, x, y, w, ALL_HOTKEYS_ROW_H - 2, 6, 1,
                lerpColor(themeColor(0xFFE0E5F1, 0xFF333B4A), ACCENT, hover));

        int colX = x;
        drawClippedText(graphics, draft.label, colX + 8, y + 8, cols[0] - 12, TEXT);
        colX += cols[0];
        drawAllHotkeyCellButton(graphics, draft.cellId("key"), listeningAllHotkeyDraft(draft) ? "Press..." : draft.keyName,
                colX + 4, y + 5, cols[1] - 8, 22, false);
        colX += cols[1];
        drawAllHotkeyCellButton(graphics, draft.cellId("mode"), draft.mode,
                colX + 4, y + 5, cols[2] - 8, 22, true);
        colX += cols[2];
        drawAllHotkeyCellButton(graphics, draft.cellId("visible"), allHotkeyVisibleLabel(draft.visibility),
                colX + 4, y + 5, cols[3] - 8, 22, true);
        colX += cols[3];
        drawAllHotkeyValueControl(graphics, draft, colX + 4, y + 5, cols[4] - 42, 22);
        drawAllHotkeyIconButton(graphics, draft.cellId("delete"), colX + cols[4] - 34, y + 5, 22,
                FontAwesomeIcons.ICON_FA_TRASH, true);
    }

    private void drawAllHotkeyValueControl(GuiGraphics graphics, AllHotkeyDraft draft, int x, int y, int w, int h) {
        switch (draft.meta.valueType) {
            case SLIDER -> {
                HotkeyBinding binding = findHotkeyBinding(draft.controlId, draft.bindingId);
                if (binding != null) {
                    drawHotkeyValueSlider(graphics, draft.controlId, binding, x, y, w);
                }
            }
            case COLOR -> {
                HotkeyBinding binding = findHotkeyBinding(draft.controlId, draft.bindingId);
                if (binding != null) {
                    drawHotkeyValueColor(graphics, draft.controlId, binding, x, y, w);
                }
            }
            default -> drawAllHotkeyCellButton(graphics, draft.cellId("value"), draft.valueLabel(), x, y, w, h, true);
        }
    }

    private List<AllHotkeySection> allHotkeySections() {
        Map<String, List<AllHotkeyDraft>> grouped = new HashMap<>();
        for (AllHotkeyDraft draft : allHotkeyDrafts) {
            AllHotkeyRowAnimation animation = allHotkeyRowAnimations.get(draft.animationId());
            if (!draft.deleted || (animation != null && animation.alpha > 0.02f)) {
                grouped.computeIfAbsent(draft.section, key -> new ArrayList<>()).add(draft);
            }
        }
        List<String> keys = new ArrayList<>(grouped.keySet());
        keys.sort(String.CASE_INSENSITIVE_ORDER);
        List<AllHotkeySection> sections = new ArrayList<>();
        for (String key : keys) {
            List<AllHotkeyDraft> rows = grouped.get(key);
            rows.sort((left, right) -> left.label.compareToIgnoreCase(right.label));
            sections.add(new AllHotkeySection(key, rows));
        }
        return sections;
    }

    private int allHotkeySectionsHeight(List<AllHotkeySection> sections) {
        if (sections.isEmpty()) {
            return 0;
        }
        int h = 0;
        for (AllHotkeySection section : sections) {
            h += allHotkeySectionHeight(section) + ALL_HOTKEYS_SECTION_GAP;
        }
        return Math.max(0, h - ALL_HOTKEYS_SECTION_GAP);
    }

    private int allHotkeySectionHeight(AllHotkeySection section) {
        int visibleRows = 0;
        for (AllHotkeyDraft row : section.rows) {
            visibleRows += allHotkeyRowAnimatedHeight(row);
        }
        return ALL_HOTKEYS_SECTION_TITLE_H + 4 + ALL_HOTKEYS_HEADER_H + 2 + visibleRows;
    }

    private static int[] allHotkeyColumns(int w) {
        int feature = 132;
        int key = 92;
        int mode = 82;
        int visible = 130;
        int value = Math.max(120, w - feature - key - mode - visible);
        return new int[] { feature, key, mode, visible, value };
    }

    private void drawAllHotkeyHeader(GuiGraphics graphics, String label, int x, int y, int w) {
        drawClippedText(graphics, label, x, y + 7, w, 9.0f, MUTED);
    }

    private void drawAllHotkeyCellButton(GuiGraphics graphics, String id, String label,
                                         int x, int y, int w, int h, boolean arrow) {
        boolean hovered = PopupGeometry.contains(mouseX, mouseY, x, y, w, h);
        float hover = controlAnim(id, "hover", hovered ? 1.0f : 0.0f);
        ShapeRenderer.roundRect(graphics, x, y, w, h, 6,
                lerpColor(FIELD, themeColor(0xFFF1F4FB, 0xFF333B4A), hover));
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 6, 1,
                lerpColor(BORDER, ACCENT, hover));
        int textW = arrow ? w - 22 : w - 12;
        drawClippedText(graphics, label == null || label.isBlank() ? "-" : label, x + 7, y + 5, textW, 9.5f, TEXT);
        if (arrow) {
            float iconSize = 8.0f;
            float iconW = MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_ANGLE_DOWN, iconSize);
            MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_ANGLE_DOWN,
                    x + w - 12 - iconW * 0.5f, y + (h - iconSize) * 0.5f,
                    iconSize, alphaColor(MUTED, renderAlpha));
        }
    }

    private void drawAllHotkeyIconButton(GuiGraphics graphics, String id, int x, int y, int size, int icon, boolean danger) {
        boolean hovered = PopupGeometry.contains(mouseX, mouseY, x, y, size, size);
        float hover = controlAnim(id, "hover", hovered ? 1.0f : 0.0f);
        ShapeRenderer.roundRect(graphics, x, y, size, size, 6,
                lerpColor(danger ? themeColor(0xFFFFF3F4, 0xFF352126) : themeColor(0x00FFFFFF, 0x33212634),
                        danger ? themeColor(0xFFFFE1E4, 0xFF4A252B) : themeColor(0xFFEFF2FA, FIELD), hover));
        float iconSize = Math.max(8.0f, size * 0.42f);
        float iconW = MSDFRenderer.iconWidth(icon, iconSize);
        MSDFRenderer.drawIconCodepoint(graphics, icon,
                x + (size - iconW) * 0.5f,
                y + (size - iconSize) * 0.5f,
                iconSize, alphaColor(danger ? DANGER : MUTED, renderAlpha));
    }

    private void drawAllHotkeyCloseButton(GuiGraphics graphics, String id, int x, int y, int size) {
        boolean hovered = PopupGeometry.contains(mouseX, mouseY, x, y, size, size);
        boolean active = hovered && click && clickButton == 0 && !clickConsumed;
        float hover = controlAnim(id, "hover", hovered ? 1.0f : 0.0f);
        float pressed = controlAnim(id, "active", active ? 1.0f : 0.0f);
        CloseButtonPainter.render(graphics, x, y, size, hover, pressed, renderAlpha);
    }

    private void drawAllHotkeyActionButton(GuiGraphics graphics, String id, String label,
                                           int x, int y, int w, int h, boolean accent) {
        boolean hovered = PopupGeometry.contains(mouseX, mouseY, x, y, w, h);
        float hover = controlAnim(id, "hover", hovered ? 1.0f : 0.0f);
        int fill = accent
                ? lerpColor(ACCENT, ACCENT_DARK, hover)
                : lerpColor(FIELD, themeColor(0xFFF1F4FB, 0xFF333B4A), hover);
        int textColor = accent ? 0xFFFFFFFF : TEXT;
        ShapeRenderer.roundRect(graphics, x, y, w, h, 7, fill);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 7, 1, accent ? ACCENT_DARK : BORDER);
        int labelW = textWidth(label);
        drawClippedText(graphics, label, x + Math.max(8, (w - labelW) / 2),
                y + Math.max(0, (h - FONT_SIZE) / 2) - 2, w - 16, textColor);
    }

    private void handleAllHotkeysClick() {
        if (!click || clickButton != 0 || !allHotkeysOpen) {
            return;
        }
        if (popupSystem.openHotkeyValueColorPopup != null) {
            HotkeyValueColorPopup popup = popupSystem.openHotkeyValueColorPopup;
            int h = colorPopupHeight();
            if (PopupGeometry.contains(clickX, clickY, popup.x, popup.y, popup.w, h)) {
                handleHotkeyColorPopupClick(popup);
                AllHotkeyDraft draft = findAllHotkeyDraft(popup.controlId, popup.bindingId);
                HotkeyBinding binding = findHotkeyBinding(popup.controlId, popup.bindingId);
                if (draft != null && binding != null) {
                    draft.colorValue = binding.colorValue;
                    applyAllHotkeyDraft(draft);
                }
                clickConsumed = true;
                return;
            }
            popupSystem.openHotkeyValueColorKey = null;
            closeHotkeyValueColorPopupVisual();
            clickConsumed = true;
            return;
        }
        int x = allHotkeysWindowX();
        int y = allHotkeysWindowY();
        int closeSize = 24;
        int closeX = x + ALL_HOTKEYS_W - closeSize - 10;
        int closeY = y + 9;
        if (PopupGeometry.contains(clickX, clickY, closeX, closeY, closeSize, closeSize)) {
            closeAllHotkeysWindow();
            clickConsumed = true;
            return;
        }
        int contentX = x + ALL_HOTKEYS_PAD;
        int contentY = y + ALL_HOTKEYS_TITLE_H;
        int contentH = ALL_HOTKEYS_H - ALL_HOTKEYS_TITLE_H - ALL_HOTKEYS_PAD;
        ScrollbarControl.Layout scrollbarLayout = ScrollbarControl.layout(x, y, ALL_HOTKEYS_W,
                contentY, contentH, ALL_HOTKEYS_W - ALL_HOTKEYS_PAD * 2, allHotkeysScrollbarReserve);
        int contentW = scrollbarLayout.contentW();

        if (startAllHotkeysScrollbarDrag(scrollbarLayout, contentH)) {
            closeAllHotkeyComboPopup();
            popupSystem.openHotkeyValueColorKey = null;
            closeHotkeyValueColorPopupVisual();
            clickConsumed = true;
            return;
        }

        if (handleAllHotkeyComboClick()) {
            clickConsumed = true;
            return;
        }

        if (!PopupGeometry.contains(clickX, clickY, contentX, contentY, contentW, contentH)) {
            closeAllHotkeyComboPopup();
            popupSystem.openHotkeyValueColorKey = null;
            closeHotkeyValueColorPopupVisual();
            if (PopupGeometry.contains(clickX, clickY, x, y, ALL_HOTKEYS_W, ALL_HOTKEYS_H)) {
                allHotkeysDragging = true;
                allHotkeysDragOffsetX = clickX - x;
                allHotkeysDragOffsetY = clickY - y;
            }
            clickConsumed = true;
            return;
        }

        List<AllHotkeySection> sections = allHotkeySections();
        int drawY = contentY - Math.round(allHotkeysAnimatedScrollY);
        int[] cols = allHotkeyColumns(contentW);
        for (AllHotkeySection section : sections) {
            int rowY = drawY + ALL_HOTKEYS_SECTION_TITLE_H + 4 + ALL_HOTKEYS_HEADER_H + 2;
            for (AllHotkeyDraft draft : section.rows) {
                if (draft.deleted) {
                    continue;
                }
                if (PopupGeometry.contains(clickX, clickY, contentX, rowY, contentW, ALL_HOTKEYS_ROW_H)) {
                    handleAllHotkeyDraftClick(draft, contentX, rowY, cols);
                    clickConsumed = true;
                    return;
                }
                rowY += ALL_HOTKEYS_ROW_H;
            }
            drawY += allHotkeySectionHeight(section) + ALL_HOTKEYS_SECTION_GAP;
        }
        if (PopupGeometry.contains(clickX, clickY, x, y, ALL_HOTKEYS_W, ALL_HOTKEYS_H)) {
            allHotkeysDragging = true;
            allHotkeysDragOffsetX = clickX - x;
            allHotkeysDragOffsetY = clickY - y;
            clickConsumed = true;
            return;
        }
        clickConsumed = true;
    }

    private void updateAllHotkeysDrag(double mouseX, double mouseY, boolean mouseDown) {
        if (!allHotkeysOpen || !mouseDown || !allHotkeysDragging || allHotkeysScrollbarDrag.dragging) {
            return;
        }
        Window window = Minecraft.getInstance().getWindow();
        int maxX = window.getWidth() - 40;
        int maxY = window.getHeight() - 40;
        allHotkeysX = UiMath.clamp((int) Math.round(mouseX - allHotkeysDragOffsetX), 40 - ALL_HOTKEYS_W, maxX);
        allHotkeysY = UiMath.clamp((int) Math.round(mouseY - allHotkeysDragOffsetY), 40 - ALL_HOTKEYS_H, maxY);
    }

    private boolean startAllHotkeysScrollbarDrag(ScrollbarControl.Layout layout, int contentH) {
        if (allHotkeysMaxScrollY <= 0) {
            return false;
        }
        int contentTotalH = contentH + allHotkeysMaxScrollY;
        if (!ScrollbarControl.beginDrag(allHotkeysScrollbarDrag, clickX, clickY, layout,
                contentH, contentTotalH, allHotkeysScrollY, allHotkeysMaxScrollY)) {
            return false;
        }
        allHotkeysDragging = false;
        return true;
    }

    private void updateAllHotkeysScrollbarDrag(double mouseY, boolean mouseDown) {
        if (!allHotkeysOpen || !mouseDown || !allHotkeysScrollbarDrag.dragging) {
            return;
        }
        int x = allHotkeysWindowX();
        int y = allHotkeysWindowY();
        int contentY = y + ALL_HOTKEYS_TITLE_H;
        int contentH = ALL_HOTKEYS_H - ALL_HOTKEYS_TITLE_H - ALL_HOTKEYS_PAD;
        ScrollbarControl.Layout layout = ScrollbarControl.layout(x, y, ALL_HOTKEYS_W,
                contentY, contentH, ALL_HOTKEYS_W - ALL_HOTKEYS_PAD * 2, allHotkeysScrollbarReserve);
        int[] nextScroll = {allHotkeysScrollY};
        if (!ScrollbarControl.updateDrag(allHotkeysScrollbarDrag, mouseY, layout,
                contentH, contentH + allHotkeysMaxScrollY, allHotkeysMaxScrollY, nextScroll)) {
            return;
        }
        allHotkeysScrollY = nextScroll[0];
        allHotkeysAnimatedScrollY = allHotkeysScrollY;
        allHotkeysScrollbarAlpha = Math.max(allHotkeysScrollbarAlpha, 1.0f);
    }

    private void handleAllHotkeyDraftClick(AllHotkeyDraft draft, int x, int y, int[] cols) {
        int colX = x + cols[0];
        if (PopupGeometry.contains(clickX, clickY, colX + 4, y + 5, cols[1] - 8, 22)) {
            allHotkeyListeningControlId = draft.controlId;
            allHotkeyListeningBindingId = draft.bindingId;
            closeAllHotkeyComboPopup();
            updateGameMovementInputBlock();
            return;
        }
        colX += cols[1];
        if (PopupGeometry.contains(clickX, clickY, colX + 4, y + 5, cols[2] - 8, 22)) {
            openAllHotkeyCombo(draft, AllHotkeyComboKind.MODE, colX + 4, y + 29, cols[2] - 8,
                    new String[] { "Toggle", "Hold" });
            return;
        }
        colX += cols[2];
        if (PopupGeometry.contains(clickX, clickY, colX + 4, y + 5, cols[3] - 8, 22)) {
            openAllHotkeyCombo(draft, AllHotkeyComboKind.VISIBLE, colX + 4, y + 29, cols[3] - 8,
                    new String[] { "On", "Off" });
            return;
        }
        colX += cols[3];
        if (PopupGeometry.contains(clickX, clickY, colX + 4, y + 5, cols[4] - 42, 22)) {
            if (draft.meta.valueType == HotkeyValueType.SLIDER) {
                HotkeyBinding binding = findHotkeyBinding(draft.controlId, draft.bindingId);
                if (binding == null) {
                    return;
                }
                popupSystem.openHotkeyValueColorKey = null;
                closeHotkeyValueColorPopupVisual();
                closeAllHotkeyComboPopup();
                int valueX = colX + 4;
                int valueY = y + 5;
                int valueW = cols[4] - 42;
                int valueTextW = 38;
                int dividerX = valueX + valueW - valueTextW - 1;
                int textX = dividerX + 1;
                int textW = valueX + valueW - textX;
                if (PopupGeometry.contains(clickX, clickY, textX, valueY, textW, HOTKEY_VALUE_H)) {
                    startHotkeySliderEdit(draft.controlId, binding, hotkeySliderValueLabel(draft.controlId, binding),
                            textX, valueY, textW, HOTKEY_VALUE_H);
                    return;
                }
                int trackX = valueX + 6;
                int trackW = Math.max(20, dividerX - trackX - 7);
                if (!PopupGeometry.contains(clickX, clickY, trackX - 4, valueY, trackW + 8, HOTKEY_VALUE_H)) {
                    return;
                }
                cancelHotkeySliderEdit();
                activeId = PopupGeometry.hotkeyBindingKey(draft.controlId, draft.bindingId) + "/slider";
                binding.sliderValue = clamp01((float) ((clickX - trackX) / Math.max(1.0, trackW)));
                binding.valueLabel = hotkeySliderValueLabel(draft.controlId, binding);
                draft.sliderValue = binding.sliderValue;
                applyAllHotkeyDraft(draft);
            } else if (draft.meta.valueType == HotkeyValueType.COLOR) {
                String key = PopupGeometry.hotkeyBindingKey(draft.controlId, draft.bindingId);
                boolean openNow = !key.equals(popupSystem.openHotkeyValueColorKey);
                if (popupSystem.openHotkeyValueColorPopup != null && (!openNow
                        || !draft.controlId.equals(popupSystem.openHotkeyValueColorPopup.controlId)
                        || draft.bindingId != popupSystem.openHotkeyValueColorPopup.bindingId)) {
                    closeHotkeyValueColorPopupVisual();
                }
                popupSystem.openHotkeyValueColorKey = openNow ? key : null;
                closeAllHotkeyComboPopup();
            } else {
                openAllHotkeyCombo(draft, AllHotkeyComboKind.VALUE, colX + 4, y + 29,
                        Math.min(ALL_HOTKEYS_VALUE_POPUP_W, Math.max(72, cols[4] - 42)),
                        allHotkeyValueOptions(draft));
            }
            return;
        }
        if (PopupGeometry.contains(clickX, clickY, colX + cols[4] - 34, y + 5, 22, 22)) {
            draft.deleted = true;
            clearAllHotkeyListening();
            closeAllHotkeyComboPopup();
        }
    }

    private void openAllHotkeysWindow() {
        allHotkeysOpen = true;
        if (allHotkeysX == Integer.MIN_VALUE || allHotkeysY == Integer.MIN_VALUE) {
            Window window = Minecraft.getInstance().getWindow();
            allHotkeysX = Math.round((window.getWidth() - ALL_HOTKEYS_W) * 0.5f);
            allHotkeysY = Math.round((window.getHeight() - ALL_HOTKEYS_H) * 0.5f);
        }
        allHotkeysScrollY = 0;
        allHotkeysAnimatedScrollY = 0.0f;
        lastAllHotkeyAnimationBaseY = Integer.MIN_VALUE;
        lastAllHotkeySectionAnimationBaseY = Integer.MIN_VALUE;
        allHotkeySectionAnimations.clear();
        allHotkeyRowAnimations.clear();
        clearAllHotkeyListening();
        closeAllHotkeyComboPopup();
        allHotkeyDrafts.clear();
        Set<String> seenBindings = new HashSet<>();
        List<String> controlIds = new ArrayList<>(hotkeyStates.keySet());
        controlIds.sort(String.CASE_INSENSITIVE_ORDER);
        for (String controlId : controlIds) {
            if (shouldSkipAllHotkeysEntry(controlId)) {
                continue;
            }
            HotkeyControlState state = hotkeyStates.get(controlId);
            HotkeyMeta meta = hotkeyMetas.getOrDefault(controlId, HotkeyMeta.EMPTY);
            if (state == null || state.bindings.isEmpty()) {
                continue;
            }
            for (HotkeyBinding binding : state.bindings) {
                if (binding.deleting || !binding.bound()) {
                    continue;
                }
                String bindingKey = allHotkeyDraftKey(controlId, meta, binding);
                if (!seenBindings.add(bindingKey)) {
                    continue;
                }
                allHotkeyDrafts.add(new AllHotkeyDraft(controlId, meta, binding));
            }
        }
    }

    private static boolean shouldSkipHotkeyPersistence(String controlId) {
        return shouldSkipAllHotkeysEntry(controlId);
    }

    private static boolean shouldSkipAllHotkeysEntry(String controlId) {
        if (controlId == null || controlId.isBlank()) {
            return true;
        }
        String lower = controlId.toLowerCase(java.util.Locale.ROOT);
        return lower.startsWith("config/") || lower.startsWith("user/")
                || isInternalHotkeyStateEntry(controlId);
    }

    private static boolean isInternalHotkeyStateEntry(String controlId) {
        if (controlId == null || controlId.isBlank()) {
            return true;
        }
        String lower = controlId.toLowerCase(java.util.Locale.ROOT);
        return lower.contains("/gear/")
                && (lower.contains("/check/enabled/")
                || lower.contains("/check/visible/")
                || lower.contains("/switch/visible/"));
    }

    private static String allHotkeyDraftKey(String controlId, HotkeyMeta meta, HotkeyBinding binding) {
        return normalizedHotkeyFeature(controlId, meta) + "#" + binding.keyCode + "#" + binding.scanCode + "#"
                + binding.mouseButton;
    }

    private void closeAllHotkeysWindow() {
        allHotkeysOpen = false;
        clearAllHotkeyListening();
        closeAllHotkeyComboPopup();
        popupSystem.openHotkeyValueColorKey = null;
        popupSystem.pendingHotkeyValueColorPopup = null;
        closeHotkeyValueColorPopupVisual();
        cancelHotkeySliderEdit();
    }

    private int allHotkeysWindowX() {
        if (allHotkeysX == Integer.MIN_VALUE) {
            Window window = Minecraft.getInstance().getWindow();
            allHotkeysX = Math.round((window.getWidth() - ALL_HOTKEYS_W) * 0.5f);
        }
        return allHotkeysX;
    }

    private int allHotkeysWindowY() {
        if (allHotkeysY == Integer.MIN_VALUE) {
            Window window = Minecraft.getInstance().getWindow();
            allHotkeysY = Math.round((window.getHeight() - ALL_HOTKEYS_H) * 0.5f);
        }
        return allHotkeysY;
    }

    private void applyAllHotkeyDraft(AllHotkeyDraft draft) {
        if (draft.deleted) {
            deleteHotkeyBinding(draft.controlId, draft.bindingId);
            return;
        }
        HotkeyBinding binding = findHotkeyBinding(draft.controlId, draft.bindingId);
        if (binding == null) {
            return;
        }
        binding.keyCode = draft.keyCode;
        binding.scanCode = draft.scanCode;
        binding.mouseButton = draft.mouseButton;
        binding.keyName = draft.keyName;
        binding.mode = "Hold".equals(draft.mode) ? "Hold" : "Toggle";
        binding.visible = draft.visibility == AllHotkeyVisibility.ON;
        binding.boolValue = draft.boolValue;
        binding.selectedValue = draft.selectedValue;
        binding.multiMask = draft.multiMask;
        binding.sliderValue = draft.sliderValue;
        binding.colorValue = draft.colorValue;
        binding.valueLabel = draft.valueLabel();
        clearToggleRestoreValue(draft.controlId, binding);
    }

    private void cycleAllHotkeyDraftValue(AllHotkeyDraft draft) {
        switch (draft.meta.valueType) {
            case BOOL -> draft.boolValue = draft.boolValue == null || !draft.boolValue;
            case SELECT -> {
                String[] options = draft.meta.options == null ? new String[0] : draft.meta.options;
                if (options.length > 0) {
                    int index = 0;
                    for (int i = 0; i < options.length; i++) {
                        if (options[i].equals(draft.selectedValue)) {
                            index = i;
                            break;
                        }
                    }
                    draft.selectedValue = options[(index + 1) % options.length];
                }
            }
            case MULTI_SELECT -> {
                String[] options = draft.meta.options == null ? new String[0] : draft.meta.options;
                int allMask = WidgetSurfaceValueTools.optionMask(options.length);
                draft.multiMask = draft.multiMask == allMask ? 0 : allMask;
            }
            case SLIDER -> draft.sliderValue = UiMath.clamp(draft.sliderValue + 0.1f, 0.0f, 1.0f);
            case COLOR, LABEL -> {
            }
        }
    }

    private String[] allHotkeyValueOptions(AllHotkeyDraft draft) {
        return switch (draft.meta.valueType) {
            case BOOL -> new String[] { "On", "Off" };
            case SELECT, MULTI_SELECT -> draft.meta.options == null || draft.meta.options.length == 0
                    ? new String[] { draft.valueLabel() }
                    : draft.meta.options;
            case LABEL -> new String[] { draft.valueLabel() };
            case SLIDER, COLOR -> new String[] { draft.valueLabel() };
        };
    }

    private void openAllHotkeyCombo(AllHotkeyDraft draft, AllHotkeyComboKind kind, int x, int y, int w, String[] options) {
        AllHotkeyComboPopup next = new AllHotkeyComboPopup(draft.controlId, draft.bindingId, kind, x, y,
                Math.max(64, w), options == null || options.length == 0 ? new String[] { "-" } : options);
        if (allHotkeyComboPopup != null && !allHotkeyComboId(allHotkeyComboPopup).equals(allHotkeyComboId(next))) {
            closeAllHotkeyComboPopup();
        }
        allHotkeyComboPopup = next;
    }

    private void renderAllHotkeyComboPopup(GuiGraphics graphics) {
        boolean closing = allHotkeyComboPopup == null && closingAllHotkeyComboPopup != null;
        AllHotkeyComboPopup popup = closing ? closingAllHotkeyComboPopup : allHotkeyComboPopup;
        if (popup == null) {
            return;
        }
        String popupId = allHotkeyComboId(popup);
        PopupAnimation animation = closing
                ? beginClosingPopupAnimation(graphics, popupId, true)
                : beginPopupAnimation(graphics, popupId, true);
        try {
            if (animation.alpha <= 0.01f) {
                if (closing) {
                    closingAllHotkeyComboPopup = null;
                }
                return;
            }
            int optionCount = popup.options.length;
            int h = popupHeight(optionCount);
            drawPopupSurface(graphics, popup.x, popup.y, popup.w, h, 7);
            AllHotkeyDraft draft = findAllHotkeyDraft(popup.controlId, popup.bindingId);
            for (int i = 0; i < optionCount; i++) {
                int optionY = popup.y + SELECT_POPUP_PAD_Y + i * (SELECT_OPTION_HEIGHT + SELECT_OPTION_GAP);
                String option = popup.options[i];
                boolean selected = draft != null && allHotkeyComboOptionSelected(draft, popup.kind, option);
                boolean hovered = PopupGeometry.contains(mouseX, mouseY, popup.x, optionY, popup.w, SELECT_OPTION_HEIGHT);
                String optionId = popupId + "/" + i;
                float previousAlpha = beginSelectOptionAnimation(graphics, i);
                try {
                    drawSelectOptionBackground(graphics, optionId, popup.x + 3, optionY, popup.w - 6, selected, hovered);
                    drawClippedText(graphics, option, popup.x + ITEM_SPACING_X,
                            optionY + (SELECT_OPTION_HEIGHT - FONT_SIZE) / 2 - 2, popup.w - ITEM_SPACING_X * 2,
                            selectOptionTextColor(optionId, selected, hovered));
                } finally {
                    endSelectOptionAnimation(graphics, previousAlpha);
                }
            }
        } finally {
            endPopupAnimation(graphics);
        }
        if (closing) {
            float current = animationSystem.closingPopupAnimations.getOrDefault(popupId, 0.0f);
            if (current <= 0.01f) {
                closingAllHotkeyComboPopup = null;
            }
        }
    }

    private void renderAllHotkeyValueColorOverlay(GuiGraphics graphics) {
        if (!allHotkeysOpen) {
            return;
        }
        HotkeyValueColorPopup popup = popupSystem.pendingHotkeyValueColorPopup != null ? popupSystem.pendingHotkeyValueColorPopup : popupSystem.openHotkeyValueColorPopup;
        if (popup != null) {
            popupSystem.openHotkeyValueColorPopup = popup;
            popupSystem.pendingHotkeyValueColorPopup = null;
            String id = PopupGeometry.hotkeyValueColorPopupId(popup.controlId, popup.bindingId);
            beginPopupAnimation(graphics, id, false);
            try {
                renderHotkeyValueColorPopup(graphics, popup);
            } finally {
                endPopupAnimation(graphics);
            }
        } else if (popupSystem.closingHotkeyValueColorPopup != null) {
            String id = PopupGeometry.hotkeyValueColorPopupId(popupSystem.closingHotkeyValueColorPopup.controlId, popupSystem.closingHotkeyValueColorPopup.bindingId);
            PopupAnimation animation = beginClosingPopupAnimation(graphics, id, false);
            try {
                if (animation.alpha <= 0.01f) {
                    popupSystem.closingHotkeyValueColorPopup = null;
                    clearPopupAnimation(id);
                    return;
                }
                renderHotkeyValueColorPopup(graphics, popupSystem.closingHotkeyValueColorPopup);
            } finally {
                endPopupAnimation(graphics);
            }
            return;
        } else {
            return;
        }
        AllHotkeyDraft draft = findAllHotkeyDraft(popup.controlId, popup.bindingId);
        HotkeyBinding binding = findHotkeyBinding(popup.controlId, popup.bindingId);
        if (draft != null && binding != null) {
            draft.colorValue = binding.colorValue;
            applyAllHotkeyDraft(draft);
        }
    }

    private boolean handleAllHotkeyComboClick() {
        if (allHotkeyComboPopup == null) {
            return false;
        }
        AllHotkeyComboPopup popup = allHotkeyComboPopup;
        int h = popupHeight(popup.options.length);
        if (!PopupGeometry.contains(clickX, clickY, popup.x, popup.y, popup.w, h)) {
            closeAllHotkeyComboPopup();
            return true;
        }
        AllHotkeyDraft draft = findAllHotkeyDraft(popup.controlId, popup.bindingId);
        if (draft == null) {
            closeAllHotkeyComboPopup();
            return true;
        }
        for (int i = 0; i < popup.options.length; i++) {
            int optionY = popup.y + SELECT_POPUP_PAD_Y + i * (SELECT_OPTION_HEIGHT + SELECT_OPTION_GAP);
            if (!PopupGeometry.contains(clickX, clickY, popup.x, optionY, popup.w, SELECT_OPTION_HEIGHT)) {
                continue;
            }
            applyAllHotkeyComboOption(draft, popup.kind, popup.options[i], i);
            applyAllHotkeyDraft(draft);
            closeAllHotkeyComboPopup();
            return true;
        }
        return true;
    }

    private void applyAllHotkeyComboOption(AllHotkeyDraft draft, AllHotkeyComboKind kind, String option, int index) {
        switch (kind) {
            case MODE -> draft.mode = option;
            case VISIBLE -> draft.visibility = switch (option) {
                case "On" -> AllHotkeyVisibility.ON;
                default -> AllHotkeyVisibility.OFF;
            };
            case VALUE -> {
                switch (draft.meta.valueType) {
                    case BOOL -> draft.boolValue = "On".equals(option);
                    case SELECT -> draft.selectedValue = option;
                    case MULTI_SELECT -> draft.multiMask = (draft.multiMask ^ (1 << index))
                            & WidgetSurfaceValueTools.optionMask(draft.meta.options == null ? 0 : draft.meta.options.length);
                    case SLIDER, COLOR, LABEL -> {
                    }
                }
            }
        }
    }

    private boolean allHotkeyComboOptionSelected(AllHotkeyDraft draft, AllHotkeyComboKind kind, String option) {
        return switch (kind) {
            case MODE -> option.equals(draft.mode);
            case VISIBLE -> option.equals(allHotkeyVisibleLabel(draft.visibility));
            case VALUE -> switch (draft.meta.valueType) {
                case BOOL -> ("On".equals(option)) == (draft.boolValue != null && draft.boolValue);
                case SELECT -> option.equals(draft.selectedValue);
                case MULTI_SELECT -> {
                    String[] options = draft.meta.options == null ? new String[0] : draft.meta.options;
                    int idx = -1;
                    for (int i = 0; i < options.length; i++) {
                        if (options[i].equals(option)) {
                            idx = i;
                            break;
                        }
                    }
                    yield idx >= 0 && (draft.multiMask & (1 << idx)) != 0;
                }
                case SLIDER, COLOR, LABEL -> option.equals(draft.valueLabel());
            };
        };
    }

    private void closeAllHotkeyComboPopup() {
        if (allHotkeyComboPopup != null) {
            closingAllHotkeyComboPopup = allHotkeyComboPopup;
            startClosingPopupAnimation(allHotkeyComboId(allHotkeyComboPopup));
        }
        allHotkeyComboPopup = null;
    }

    private static String allHotkeyComboId(AllHotkeyComboPopup popup) {
        return "all-hotkeys/combo/" + popup.controlId + "/" + popup.bindingId + "/" + popup.kind;
    }

    private AllHotkeyDraft findAllHotkeyDraft(String controlId, int bindingId) {
        for (AllHotkeyDraft draft : allHotkeyDrafts) {
            if (draft.controlId.equals(controlId) && draft.bindingId == bindingId) {
                return draft;
            }
        }
        return null;
    }

    private boolean listeningAllHotkeyDraft(AllHotkeyDraft draft) {
        return draft.controlId.equals(allHotkeyListeningControlId)
                && draft.bindingId == allHotkeyListeningBindingId;
    }

    private void clearAllHotkeyListening() {
        allHotkeyListeningControlId = null;
        allHotkeyListeningBindingId = -1;
        updateGameMovementInputBlock();
    }

    private static String allHotkeyVisibleLabel(AllHotkeyVisibility visibility) {
        return switch (visibility) {
            case ON -> "On";
            case OFF -> "Off";
        };
    }

    boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!allHotkeysOpen) {
            return false;
        }
        int x = allHotkeysWindowX();
        int y = allHotkeysWindowY();
        if (!PopupGeometry.contains(mouseX, mouseY, x, y, ALL_HOTKEYS_W, ALL_HOTKEYS_H)) {
            return allHotkeysOpen;
        }
        closeAllHotkeyComboPopup();
        allHotkeysScrollY = UiMath.clamp(allHotkeysScrollY - (int) Math.round(delta * 28.0), 0, allHotkeysMaxScrollY);
        return true;
    }


    private float[] hotkeyBindingReveals(String popupId, HotkeyControlState state) {
        float[] reveals = new float[state.bindings.size()];
        for (int i = 0; i < state.bindings.size(); i++) {
            HotkeyBinding binding = state.bindings.get(i);
            String animationId = "hotkey-binding/" + popupId + "/" + binding.id;
            reveals[i] = binding.deleting
                    ? disappearAnim(animationId, "reveal")
                    : appearAnim(animationId, "reveal");
        }
        return reveals;
    }

    private void removeFinishedDeletingHotkeys(String popupId, HotkeyControlState state, float[] bindingReveals) {
        for (int i = state.bindings.size() - 1; i >= 0; i--) {
            HotkeyBinding binding = state.bindings.get(i);
            if (!binding.deleting || bindingReveals[i] > 0.01f) {
                continue;
            }
            clearHotkeyAnimation(popupId, binding);
            state.bindings.remove(i);
        }
    }

    private static int animatedHotkeyPopupHeight(float[] bindingReveals, boolean resetDeleting) {
        float bindingHeight = 0.0f;
        for (float reveal : bindingReveals) {
            bindingHeight += bindingRowHeight(reveal, resetDeleting);
        }
        return Math.round(HOTKEY_POPUP_PAD * 2 + bindingHeight + HOTKEY_ROW_H * 3 + HOTKEY_DIVIDER_GAP);
    }

    private static float bindingRowHeight(float reveal, boolean resetDeleting) {
        if (resetDeleting) {
            return HOTKEY_ROW_H * clamp01(reveal);
        }
        return HOTKEY_ROW_H * smoothstep(clamp01(reveal));
    }

    private static boolean stateResetDeleting(HotkeyControlState state) {
        return !state.bindings.isEmpty() && state.bindings.stream().allMatch(binding -> binding.deleting);
    }

    private static void syncHotkeyPhysicalState() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null) {
            return;
        }
        long window = minecraft.getWindow().getWindow();
        pressedHotkeyKeys.removeIf(keyCode -> keyCode == GLFW.GLFW_KEY_UNKNOWN
                || GLFW.glfwGetKey(window, keyCode) != GLFW.GLFW_PRESS);
        pressedHotkeyMouseButtons.removeIf(button -> button < 0
                || GLFW.glfwGetMouseButton(window, button) != GLFW.GLFW_PRESS);

        for (Map.Entry<String, HotkeyControlState> entry : hotkeyStates.entrySet()) {
            if (shouldSkipAllHotkeysEntry(entry.getKey())) {
                continue;
            }
            for (HotkeyBinding binding : entry.getValue().bindings) {
                if (!binding.holdActive || !"Hold".equals(binding.mode) || bindingPhysicalDown(window, binding)) {
                    continue;
                }
                applyHotkeyBinding(entry.getKey(), binding, false);
            }
        }
    }

    private static boolean physicalKeyDown(int keyCode) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null || keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return false;
        }
        return GLFW.glfwGetKey(minecraft.getWindow().getWindow(), keyCode) == GLFW.GLFW_PRESS;
    }

    private static boolean physicalMouseDown(int button) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null || button < 0) {
            return false;
        }
        return GLFW.glfwGetMouseButton(minecraft.getWindow().getWindow(), button) == GLFW.GLFW_PRESS;
    }

    private static boolean bindingPhysicalDown(long window, HotkeyBinding binding) {
        if (binding.mouseButton >= 0) {
            return GLFW.glfwGetMouseButton(window, binding.mouseButton) == GLFW.GLFW_PRESS;
        }
        return binding.keyCode != GLFW.GLFW_KEY_UNKNOWN
                && GLFW.glfwGetKey(window, binding.keyCode) == GLFW.GLFW_PRESS;
    }

    private static boolean triggerHotkey(int keyCode, int scanCode) {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return false;
        }
        boolean handled = false;
        for (Map.Entry<String, HotkeyControlState> entry : hotkeyStates.entrySet()) {
            if (shouldSkipAllHotkeysEntry(entry.getKey())) {
                continue;
            }
            for (HotkeyBinding binding : entry.getValue().bindings) {
                if (binding.deleting || !binding.matches(keyCode, scanCode)) {
                    continue;
                }
                applyHotkeyBinding(entry.getKey(), binding, true);
                handled = true;
            }
        }
        return handled;
    }

    private static boolean triggerMouseHotkey(int button) {
        if (button < 0) {
            return false;
        }
        boolean handled = false;
        for (Map.Entry<String, HotkeyControlState> entry : hotkeyStates.entrySet()) {
            if (shouldSkipAllHotkeysEntry(entry.getKey())) {
                continue;
            }
            for (HotkeyBinding binding : entry.getValue().bindings) {
                if (binding.deleting || !binding.matchesMouse(button)) {
                    continue;
                }
                applyHotkeyBinding(entry.getKey(), binding, true);
                handled = true;
            }
        }
        return handled;
    }

    private static boolean releaseHoldHotkeys(int keyCode, int scanCode) {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return false;
        }
        boolean handled = false;
        for (Map.Entry<String, HotkeyControlState> entry : hotkeyStates.entrySet()) {
            if (shouldSkipAllHotkeysEntry(entry.getKey())) {
                continue;
            }
            for (HotkeyBinding binding : entry.getValue().bindings) {
                if (binding.deleting || !"Hold".equals(binding.mode) || !binding.matches(keyCode, scanCode)) {
                    continue;
                }
                applyHotkeyBinding(entry.getKey(), binding, false);
                handled = true;
            }
        }
        return handled;
    }

    private static boolean releaseHoldMouseHotkeys(int button) {
        if (button < 0) {
            return false;
        }
        boolean handled = false;
        for (Map.Entry<String, HotkeyControlState> entry : hotkeyStates.entrySet()) {
            if (shouldSkipAllHotkeysEntry(entry.getKey())) {
                continue;
            }
            for (HotkeyBinding binding : entry.getValue().bindings) {
                if (binding.deleting || !"Hold".equals(binding.mode) || !binding.matchesMouse(button)) {
                    continue;
                }
                applyHotkeyBinding(entry.getKey(), binding, false);
                handled = true;
            }
        }
        return handled;
    }

    private static void applyHotkeyBinding(String controlId, HotkeyBinding binding, boolean pressed) {
        HotkeyMeta meta = hotkeyMetas.getOrDefault(controlId, HotkeyMeta.EMPTY);
        if ("Hold".equals(binding.mode)) {
            applyHoldHotkeyBinding(controlId, meta, binding, pressed);
            return;
        }
        if (pressed) {
            applyToggleHotkeyBinding(controlId, meta, binding);
        }
    }

    private static void applyToggleHotkeyBinding(String controlId, HotkeyMeta meta, HotkeyBinding binding) {
        if (binding.toggleActive) {
            restoreToggleValue(controlId, meta, binding);
            binding.toggleActive = false;
            notifyHotkeyTriggered(controlId, currentHotkeyMeta(controlId, meta), binding,
                    currentHotkeyValueLabel(controlId, meta));
            return;
        }
        cancelSiblingHotkeysForActivation(controlId, meta, binding);
        rememberToggleRestoreValue(controlId, meta, binding);
        applyHotkeyValue(controlId, meta, binding);
        binding.toggleActive = true;
        notifyHotkeyTriggered(controlId, currentHotkeyMeta(controlId, meta), binding,
                currentHotkeyValueLabel(controlId, meta));
    }

    private static void applyHoldHotkeyBinding(String controlId, HotkeyMeta meta, HotkeyBinding binding, boolean pressed) {
        if (pressed) {
            cancelSiblingHotkeysForActivation(controlId, meta, binding);
            rememberHoldRestoreValue(controlId, meta);
            applyHotkeyValue(controlId, meta, binding);
            binding.holdActive = true;
            notifyHotkeyTriggered(controlId, currentHotkeyMeta(controlId, meta), binding,
                    currentHotkeyValueLabel(controlId, meta));
        } else {
            if (!binding.holdActive) {
                return;
            }
            restoreHoldValue(controlId, meta);
            binding.holdActive = false;
            notifyHotkeyTriggered(controlId, currentHotkeyMeta(controlId, meta), binding,
                    currentHotkeyValueLabel(controlId, meta));
        }
    }

    private static void notifyHotkeyTriggered(String controlId, HotkeyMeta meta, HotkeyBinding binding, String valueLabel) {
        String group = meta == null || meta.group == null || meta.group.isBlank()
                ? HotkeyTextFormatter.groupFromId(controlId)
                : meta.group;
        String label = meta == null ? "" : HotkeyTextFormatter.displayLabel(controlId, meta.label);
        NotificationCenter.hotkey(HotkeyTextFormatter.hudName(group, label), valueLabel, binding.keyName);
    }

    private static HotkeyMeta currentHotkeyMeta(String controlId, HotkeyMeta fallback) {
        HotkeyMeta current = hotkeyMetas.get(controlId);
        return current == null ? fallback : current;
    }

    private static String currentHotkeyValueLabel(String controlId, HotkeyMeta fallback) {
        HotkeyMeta meta = currentHotkeyMeta(controlId, fallback);
        if (meta == null) {
            return "Restored";
        }
        return switch (meta.valueType) {
            case BOOL -> meta.boolValue == null ? "Restored" : onOff(meta.boolValue);
            case SELECT -> meta.selectedValue == null || meta.selectedValue.isBlank() ? "Restored" : meta.selectedValue;
            case MULTI_SELECT -> {
                String[] options = meta.options == null ? new String[0] : meta.options;
                yield WidgetSurfaceValueTools.multiSelectSummary(meta.multiMask, options, WidgetSurfaceValueTools.optionMask(options.length));
            }
            case SLIDER -> meta.sliderFormatter == null ? WidgetSurfaceValueTools.sliderValueLabel(meta.sliderValue)
                    : meta.sliderFormatter.apply(meta.sliderValue);
            case COLOR -> colorHex(meta.colorValue);
            case LABEL -> meta.valueLabel == null || meta.valueLabel.isBlank() ? "Restored" : meta.valueLabel;
        };
    }

    private static void applyHotkeyValue(String controlId, HotkeyMeta meta, HotkeyBinding binding) {
        switch (meta.valueType) {
            case BOOL -> {
                if (binding.boolValue != null) {
                    binding.valueLabel = onOff(binding.boolValue);
                    syncHotkeyControlBool(controlId, binding.boolValue);
                    if (usesPendingHotkeyValue(meta)) {
                        pendingHotkeyBoolValues.put(controlId, binding.boolValue);
                    }
                    applyHotkeyBool(meta, binding.boolValue);
                    updateHotkeyMetaBool(controlId, meta, binding.boolValue);
                }
            }
            case SELECT -> {
                binding.selectedValue = binding.selectedValue == null || binding.selectedValue.isBlank()
                        ? "-"
                        : binding.selectedValue;
                binding.valueLabel = binding.selectedValue;
                syncHotkeyControlSelect(controlId, binding.selectedValue);
                if (usesPendingHotkeyValue(meta)) {
                    pendingHotkeySelectValues.put(controlId, binding.selectedValue);
                }
                applyHotkeySelect(meta, binding.selectedValue);
                updateHotkeyMetaSelect(controlId, meta, binding.selectedValue);
            }
            case MULTI_SELECT -> {
                String[] options = binding.options == null ? new String[0] : binding.options;
                int allMask = WidgetSurfaceValueTools.optionMask(options.length);
                binding.multiMask &= allMask;
                binding.valueLabel = WidgetSurfaceValueTools.multiSelectSummary(binding.multiMask, options, allMask);
                syncHotkeyControlMultiSelect(controlId, binding.multiMask);
                if (usesPendingHotkeyValue(meta)) {
                    pendingHotkeyMultiSelectMasks.put(controlId, binding.multiMask);
                }
                applyHotkeyMultiSelect(meta, binding.multiMask);
                updateHotkeyMetaMultiSelect(controlId, meta, binding.multiMask);
            }
            case SLIDER -> {
                float value = clamp01(binding.sliderValue);
                binding.sliderValue = value;
                binding.valueLabel = hotkeySliderValueLabel(controlId, binding);
                syncHotkeyControlSlider(controlId, value);
                if (usesPendingHotkeyValue(meta)) {
                    pendingHotkeySliderValues.put(controlId, value);
                }
                applyHotkeySlider(meta, value);
                updateHotkeyMetaSlider(controlId, meta, value);
            }
            case COLOR -> {
                int color = binding.colorValue;
                binding.colorValue = color;
                binding.valueLabel = colorHex(color);
                syncHotkeyControlColor(controlId, color);
                if (usesPendingHotkeyValue(meta)) {
                    pendingHotkeyColorValues.put(controlId, color);
                }
                applyHotkeyColor(meta, color);
                updateHotkeyMetaColor(controlId, meta, color);
            }
            case LABEL -> {
            }
        }
    }

    private static void rememberHoldRestoreValue(String controlId, HotkeyMeta meta) {
        switch (meta.valueType) {
            case BOOL -> {
                if (meta.boolValue != null) {
                    holdRestoreBoolValues.putIfAbsent(controlId, meta.boolValue);
                }
            }
            case SELECT -> holdRestoreSelectValues.putIfAbsent(controlId, meta.selectedValue);
            case MULTI_SELECT -> holdRestoreMultiSelectMasks.putIfAbsent(controlId, meta.multiMask);
            case SLIDER -> holdRestoreSliderValues.putIfAbsent(controlId, meta.sliderValue);
            case COLOR -> holdRestoreColorValues.putIfAbsent(controlId, meta.colorValue);
            case LABEL -> {
            }
        }
    }

    private static void rememberToggleRestoreValue(String controlId, HotkeyMeta meta, HotkeyBinding binding) {
        String key = toggleRestoreKey(controlId, binding);
        switch (meta.valueType) {
            case BOOL -> {
                if (meta.boolValue != null) {
                    toggleRestoreBoolValues.putIfAbsent(key, meta.boolValue);
                }
            }
            case SELECT -> toggleRestoreSelectValues.putIfAbsent(key, meta.selectedValue);
            case MULTI_SELECT -> toggleRestoreMultiSelectMasks.putIfAbsent(key, meta.multiMask);
            case SLIDER -> toggleRestoreSliderValues.putIfAbsent(key, meta.sliderValue);
            case COLOR -> toggleRestoreColorValues.putIfAbsent(key, meta.colorValue);
            case LABEL -> {
            }
        }
    }

    private static void cancelSiblingHotkeysForActivation(String controlId, HotkeyMeta meta, HotkeyBinding activating) {
        HotkeyControlState state = hotkeyStates.get(controlId);
        if (state == null) {
            return;
        }
        HotkeyRestoreSnapshot restore = null;
        boolean canceled = false;
        for (HotkeyBinding binding : state.bindings) {
            if (binding == activating || !binding.active()) {
                continue;
            }
            if (restore == null) {
                restore = binding.holdActive
                        ? holdRestoreSnapshot(controlId, meta)
                        : toggleRestoreSnapshot(controlId, meta, binding);
            }
            clearToggleRestoreValue(controlId, binding);
            binding.holdActive = false;
            canceled = true;
        }
        if (!canceled) {
            return;
        }
        if (restore == null) {
            restore = currentHotkeyRestoreSnapshot(meta);
        }
        if (restore == null) {
            return;
        }
        if ("Hold".equals(activating.mode)) {
            storeHoldRestoreSnapshot(controlId, restore);
        } else {
            clearHoldRestoreValue(controlId);
            storeToggleRestoreSnapshot(controlId, activating, restore);
        }
    }

    private static HotkeyRestoreSnapshot holdRestoreSnapshot(String controlId, HotkeyMeta meta) {
        return switch (meta.valueType) {
            case BOOL -> holdRestoreBoolValues.containsKey(controlId)
                    ? HotkeyRestoreSnapshot.bool(holdRestoreBoolValues.get(controlId))
                    : null;
            case SELECT -> holdRestoreSelectValues.containsKey(controlId)
                    ? HotkeyRestoreSnapshot.select(holdRestoreSelectValues.get(controlId))
                    : null;
            case MULTI_SELECT -> holdRestoreMultiSelectMasks.containsKey(controlId)
                    ? HotkeyRestoreSnapshot.multi(holdRestoreMultiSelectMasks.get(controlId))
                    : null;
            case SLIDER -> holdRestoreSliderValues.containsKey(controlId)
                    ? HotkeyRestoreSnapshot.slider(holdRestoreSliderValues.get(controlId))
                    : null;
            case COLOR -> holdRestoreColorValues.containsKey(controlId)
                    ? HotkeyRestoreSnapshot.color(holdRestoreColorValues.get(controlId))
                    : null;
            case LABEL -> null;
        };
    }

    private static HotkeyRestoreSnapshot toggleRestoreSnapshot(String controlId, HotkeyMeta meta, HotkeyBinding binding) {
        String key = toggleRestoreKey(controlId, binding);
        return switch (meta.valueType) {
            case BOOL -> toggleRestoreBoolValues.containsKey(key)
                    ? HotkeyRestoreSnapshot.bool(toggleRestoreBoolValues.get(key))
                    : null;
            case SELECT -> toggleRestoreSelectValues.containsKey(key)
                    ? HotkeyRestoreSnapshot.select(toggleRestoreSelectValues.get(key))
                    : null;
            case MULTI_SELECT -> toggleRestoreMultiSelectMasks.containsKey(key)
                    ? HotkeyRestoreSnapshot.multi(toggleRestoreMultiSelectMasks.get(key))
                    : null;
            case SLIDER -> toggleRestoreSliderValues.containsKey(key)
                    ? HotkeyRestoreSnapshot.slider(toggleRestoreSliderValues.get(key))
                    : null;
            case COLOR -> toggleRestoreColorValues.containsKey(key)
                    ? HotkeyRestoreSnapshot.color(toggleRestoreColorValues.get(key))
                    : null;
            case LABEL -> null;
        };
    }

    private static HotkeyRestoreSnapshot currentHotkeyRestoreSnapshot(HotkeyMeta meta) {
        return switch (meta.valueType) {
            case BOOL -> meta.boolValue == null ? null : HotkeyRestoreSnapshot.bool(meta.boolValue);
            case SELECT -> HotkeyRestoreSnapshot.select(meta.selectedValue);
            case MULTI_SELECT -> HotkeyRestoreSnapshot.multi(meta.multiMask);
            case SLIDER -> HotkeyRestoreSnapshot.slider(meta.sliderValue);
            case COLOR -> HotkeyRestoreSnapshot.color(meta.colorValue);
            case LABEL -> null;
        };
    }

    private static void storeHoldRestoreSnapshot(String controlId, HotkeyRestoreSnapshot restore) {
        switch (restore.valueType) {
            case BOOL -> holdRestoreBoolValues.put(controlId, restore.boolValue);
            case SELECT -> holdRestoreSelectValues.put(controlId, restore.selectedValue);
            case MULTI_SELECT -> holdRestoreMultiSelectMasks.put(controlId, restore.multiMask);
            case SLIDER -> holdRestoreSliderValues.put(controlId, restore.sliderValue);
            case COLOR -> holdRestoreColorValues.put(controlId, restore.colorValue);
            case LABEL -> {
            }
        }
    }

    private static void storeToggleRestoreSnapshot(String controlId, HotkeyBinding binding, HotkeyRestoreSnapshot restore) {
        String key = toggleRestoreKey(controlId, binding);
        switch (restore.valueType) {
            case BOOL -> toggleRestoreBoolValues.put(key, restore.boolValue);
            case SELECT -> toggleRestoreSelectValues.put(key, restore.selectedValue);
            case MULTI_SELECT -> toggleRestoreMultiSelectMasks.put(key, restore.multiMask);
            case SLIDER -> toggleRestoreSliderValues.put(key, restore.sliderValue);
            case COLOR -> toggleRestoreColorValues.put(key, restore.colorValue);
            case LABEL -> {
            }
        }
    }

    private static void restoreToggleValue(String controlId, HotkeyMeta meta, HotkeyBinding binding) {
        String key = toggleRestoreKey(controlId, binding);
        switch (meta.valueType) {
            case BOOL -> {
                if (toggleRestoreBoolValues.containsKey(key)) {
                    boolean value = toggleRestoreBoolValues.remove(key);
                    syncHotkeyControlBool(controlId, value);
                    if (usesPendingHotkeyValue(meta)) {
                        pendingHotkeyBoolValues.put(controlId, value);
                    }
                    applyHotkeyBool(meta, value);
                    updateHotkeyMetaBool(controlId, meta, value);
                }
            }
            case SELECT -> {
                if (toggleRestoreSelectValues.containsKey(key)) {
                    String value = toggleRestoreSelectValues.remove(key);
                    syncHotkeyControlSelect(controlId, value);
                    if (usesPendingHotkeyValue(meta)) {
                        pendingHotkeySelectValues.put(controlId, value);
                    }
                    applyHotkeySelect(meta, value);
                    updateHotkeyMetaSelect(controlId, meta, value);
                }
            }
            case MULTI_SELECT -> {
                if (toggleRestoreMultiSelectMasks.containsKey(key)) {
                    int value = toggleRestoreMultiSelectMasks.remove(key);
                    syncHotkeyControlMultiSelect(controlId, value);
                    if (usesPendingHotkeyValue(meta)) {
                        pendingHotkeyMultiSelectMasks.put(controlId, value);
                    }
                    applyHotkeyMultiSelect(meta, value);
                    updateHotkeyMetaMultiSelect(controlId, meta, value);
                }
            }
            case SLIDER -> {
                if (toggleRestoreSliderValues.containsKey(key)) {
                    float value = clamp01(toggleRestoreSliderValues.remove(key));
                    syncHotkeyControlSlider(controlId, value);
                    if (usesPendingHotkeyValue(meta)) {
                        pendingHotkeySliderValues.put(controlId, value);
                    }
                    applyHotkeySlider(meta, value);
                    updateHotkeyMetaSlider(controlId, meta, value);
                }
            }
            case COLOR -> {
                if (toggleRestoreColorValues.containsKey(key)) {
                    int value = toggleRestoreColorValues.remove(key);
                    syncHotkeyControlColor(controlId, value);
                    if (usesPendingHotkeyValue(meta)) {
                        pendingHotkeyColorValues.put(controlId, value);
                    }
                    applyHotkeyColor(meta, value);
                    updateHotkeyMetaColor(controlId, meta, value);
                }
            }
            case LABEL -> {
            }
        }
    }

    private static void clearToggleRestoreValue(String controlId, HotkeyBinding binding) {
        String key = toggleRestoreKey(controlId, binding);
        toggleRestoreBoolValues.remove(key);
        toggleRestoreSelectValues.remove(key);
        toggleRestoreMultiSelectMasks.remove(key);
        toggleRestoreSliderValues.remove(key);
        toggleRestoreColorValues.remove(key);
        binding.toggleActive = false;
        binding.holdActive = false;
    }

    private static void clearHoldRestoreValue(String controlId) {
        holdRestoreBoolValues.remove(controlId);
        holdRestoreSelectValues.remove(controlId);
        holdRestoreMultiSelectMasks.remove(controlId);
        holdRestoreSliderValues.remove(controlId);
        holdRestoreColorValues.remove(controlId);
    }

    private static String toggleRestoreKey(String controlId, HotkeyBinding binding) {
        return controlId + "#" + binding.id;
    }

    private record HotkeyRestoreSnapshot(HotkeyValueType valueType, Boolean boolValue, String selectedValue,
                                         Integer multiMask, Float sliderValue, Integer colorValue) {
        static HotkeyRestoreSnapshot bool(boolean value) {
            return new HotkeyRestoreSnapshot(HotkeyValueType.BOOL, value, null, null, null, null);
        }

        static HotkeyRestoreSnapshot select(String value) {
            return new HotkeyRestoreSnapshot(HotkeyValueType.SELECT, null, value, null, null, null);
        }

        static HotkeyRestoreSnapshot multi(int value) {
            return new HotkeyRestoreSnapshot(HotkeyValueType.MULTI_SELECT, null, null, value, null, null);
        }

        static HotkeyRestoreSnapshot slider(float value) {
            return new HotkeyRestoreSnapshot(HotkeyValueType.SLIDER, null, null, null, value, null);
        }

        static HotkeyRestoreSnapshot color(int value) {
            return new HotkeyRestoreSnapshot(HotkeyValueType.COLOR, null, null, null, null, value);
        }
    }

    private static void restoreHoldValue(String controlId, HotkeyMeta meta) {
        switch (meta.valueType) {
            case BOOL -> {
                if (holdRestoreBoolValues.containsKey(controlId)) {
                    boolean value = holdRestoreBoolValues.remove(controlId);
                    syncHotkeyControlBool(controlId, value);
                    if (usesPendingHotkeyValue(meta)) {
                        pendingHotkeyBoolValues.put(controlId, value);
                    }
                    applyHotkeyBool(meta, value);
                    updateHotkeyMetaBool(controlId, meta, value);
                }
            }
            case SELECT -> {
                if (holdRestoreSelectValues.containsKey(controlId)) {
                    String value = holdRestoreSelectValues.remove(controlId);
                    syncHotkeyControlSelect(controlId, value);
                    if (usesPendingHotkeyValue(meta)) {
                        pendingHotkeySelectValues.put(controlId, value);
                    }
                    applyHotkeySelect(meta, value);
                    updateHotkeyMetaSelect(controlId, meta, value);
                }
            }
            case MULTI_SELECT -> {
                if (holdRestoreMultiSelectMasks.containsKey(controlId)) {
                    int value = holdRestoreMultiSelectMasks.remove(controlId);
                    syncHotkeyControlMultiSelect(controlId, value);
                    if (usesPendingHotkeyValue(meta)) {
                        pendingHotkeyMultiSelectMasks.put(controlId, value);
                    }
                    applyHotkeyMultiSelect(meta, value);
                    updateHotkeyMetaMultiSelect(controlId, meta, value);
                }
            }
            case SLIDER -> {
                if (holdRestoreSliderValues.containsKey(controlId)) {
                    float value = clamp01(holdRestoreSliderValues.remove(controlId));
                    syncHotkeyControlSlider(controlId, value);
                    if (usesPendingHotkeyValue(meta)) {
                        pendingHotkeySliderValues.put(controlId, value);
                    }
                    applyHotkeySlider(meta, value);
                    updateHotkeyMetaSlider(controlId, meta, value);
                }
            }
            case COLOR -> {
                if (holdRestoreColorValues.containsKey(controlId)) {
                    int value = holdRestoreColorValues.remove(controlId);
                    syncHotkeyControlColor(controlId, value);
                    if (usesPendingHotkeyValue(meta)) {
                        pendingHotkeyColorValues.put(controlId, value);
                    }
                    applyHotkeyColor(meta, value);
                    updateHotkeyMetaColor(controlId, meta, value);
                }
            }
            case LABEL -> {
            }
        }
    }

    private static void applyHotkeyBool(HotkeyMeta meta, boolean value) {
        if (meta.applier != null) {
            meta.applier.applyBoolean(value);
        }
    }

    private static void applyHotkeySelect(HotkeyMeta meta, String value) {
        if (meta.applier != null) {
            meta.applier.applySelect(value);
        }
    }

    private static void applyHotkeyMultiSelect(HotkeyMeta meta, int value) {
        if (meta.applier != null) {
            meta.applier.applyMultiSelect(value);
        }
    }

    private static void applyHotkeySlider(HotkeyMeta meta, float value) {
        if (meta.applier != null) {
            meta.applier.applySlider(value);
        }
    }

    private static void applyHotkeyColor(HotkeyMeta meta, int value) {
        if (meta.applier != null) {
            meta.applier.applyColor(value);
        }
    }

    private static boolean usesPendingHotkeyValue(HotkeyMeta meta) {
        return meta.applier == null;
    }

    private static void updateHotkeyMetaBool(String controlId, HotkeyMeta meta, boolean value) {
        hotkeyMetas.put(controlId, new HotkeyMeta(meta.group, meta.label, meta.kind, onOff(value), value,
                meta.valueType, meta.selectedValue, meta.options, meta.multiMask, meta.sliderValue,
                meta.colorValue, meta.applier, meta.sliderFormatter, meta.sliderParser));
    }

    private static void updateHotkeyMetaSelect(String controlId, HotkeyMeta meta, String value) {
        hotkeyMetas.put(controlId, new HotkeyMeta(meta.group, meta.label, meta.kind, value, meta.boolValue,
                meta.valueType, value, meta.options, meta.multiMask, meta.sliderValue,
                meta.colorValue, meta.applier, meta.sliderFormatter, meta.sliderParser));
    }

    private static void updateHotkeyMetaMultiSelect(String controlId, HotkeyMeta meta, int value) {
        String[] options = meta.options == null ? new String[0] : meta.options;
        int allMask = WidgetSurfaceValueTools.optionMask(options.length);
        String label = WidgetSurfaceValueTools.multiSelectSummary(value, options, allMask);
        hotkeyMetas.put(controlId, new HotkeyMeta(meta.group, meta.label, meta.kind, label, meta.boolValue,
                meta.valueType, meta.selectedValue, meta.options, value, meta.sliderValue,
                meta.colorValue, meta.applier, meta.sliderFormatter, meta.sliderParser));
    }

    private static void updateHotkeyMetaSlider(String controlId, HotkeyMeta meta, float value) {
        String label = meta.sliderFormatter == null ? WidgetSurfaceValueTools.formatSliderNumber(value) : meta.sliderFormatter.apply(value);
        hotkeyMetas.put(controlId, new HotkeyMeta(meta.group, meta.label, meta.kind, label, meta.boolValue,
                meta.valueType, meta.selectedValue, meta.options, meta.multiMask, value,
                meta.colorValue, meta.applier, meta.sliderFormatter, meta.sliderParser));
    }

    private static void updateHotkeyMetaColor(String controlId, HotkeyMeta meta, int value) {
        hotkeyMetas.put(controlId, new HotkeyMeta(meta.group, meta.label, meta.kind, colorHex(value), meta.boolValue,
                meta.valueType, meta.selectedValue, meta.options, meta.multiMask, meta.sliderValue,
                value, meta.applier, meta.sliderFormatter, meta.sliderParser));
    }

    private static void syncHotkeyControlBool(String controlId, boolean value) {
        WidgetSurface gui = activeInstance;
        if (gui == null) {
            return;
        }
        gui.widgetRegistry.boolValues.put(controlId, value);
    }

    private static void syncHotkeyControlSelect(String controlId, String value) {
        WidgetSurface gui = activeInstance;
        if (gui == null) {
            return;
        }
        gui.widgetRegistry.selectValues.put(controlId, value == null || value.isBlank() ? "-" : value);
    }

    private static void syncHotkeyControlMultiSelect(String controlId, int value) {
        WidgetSurface gui = activeInstance;
        if (gui == null) {
            return;
        }
        gui.widgetRegistry.multiSelectMasks.put(controlId, value);
    }

    private static void syncHotkeyControlSlider(String controlId, float value) {
        WidgetSurface gui = activeInstance;
        if (gui == null) {
            return;
        }
        gui.widgetRegistry.sliderValues.put(controlId, clamp01(value));
    }

    private static void syncHotkeyControlColor(String controlId, int value) {
        WidgetSurface gui = activeInstance;
        if (gui == null) {
            return;
        }
        gui.widgetRegistry.colorValues.put(controlId, value);
    }

    private boolean applyPendingHotkeyBool(String id, boolean value) {
        Boolean pending = pendingHotkeyBoolValues.remove(id);
        return pending == null ? value : pending;
    }

    private String applyPendingHotkeySelect(String id, String value, String[] options) {
        String pending = pendingHotkeySelectValues.remove(id);
        if (pending == null || !WidgetSurfaceValueTools.containsOption(pending, options)) {
            return value;
        }
        return pending;
    }

    private int applyPendingHotkeyMultiSelect(String id, int value, int allMask) {
        Integer pending = pendingHotkeyMultiSelectMasks.remove(id);
        return pending == null ? value : pending & allMask;
    }

    private float applyPendingHotkeySlider(String id, float value) {
        Float pending = pendingHotkeySliderValues.remove(id);
        return pending == null ? value : clamp01(pending);
    }

    private int applyPendingHotkeyColor(String id, int value) {
        Integer pending = pendingHotkeyColorValues.remove(id);
        return pending == null ? value : pending;
    }

    private void renderHotkeyDetailPopup(GuiGraphics graphics, HotkeyDetailPopup popup) {
        HotkeyBinding binding = findHotkeyBinding(popup.controlId, popup.bindingId);
        if (binding == null) {
            clearHotkeyDetail();
            return;
        }
        int h = hotkeyDetailHeight();
        drawPopupSurface(graphics, popup.x, popup.y, HOTKEY_DETAIL_W, h, 8);
        boolean clipped = pushRenderClip(popup.x, popup.y, HOTKEY_DETAIL_W, h);
        try {
        int rowY = popup.y + HOTKEY_POPUP_PAD;
        drawHotkeyDetailRow(graphics, popup.x, rowY, HOTKEY_DETAIL_W, FontAwesomeIcons.ICON_FA_KEY, "Key",
                isListeningHotkey(popup.controlId, popup.bindingId) ? "Press key/mouse" : binding.keyName,
                false, TEXT, hotkeyDetailRowHovered(popup.x, rowY), true);
        rowY += HOTKEY_ROW_H;
        drawHotkeyModeRow(graphics, popup.x, rowY, HOTKEY_DETAIL_W, popup.controlId, binding,
                hotkeyDetailRowHovered(popup.x, rowY));
        rowY += HOTKEY_ROW_H;
        drawHotkeyValueRow(graphics, popup.x, rowY, HOTKEY_DETAIL_W, popup.controlId, binding,
                hotkeyDetailRowHovered(popup.x, rowY));
        rowY += HOTKEY_ROW_H;
        drawHotkeySwitchRow(graphics, popup.x, rowY, HOTKEY_DETAIL_W, FontAwesomeIcons.ICON_FA_EYE,
                "Visible", binding.visible, hotkeyDetailRowHovered(popup.x, rowY));
        rowY += HOTKEY_ROW_H + HOTKEY_DIVIDER_GAP;
        dividerLine(graphics, popup.x + HOTKEY_POPUP_PAD, rowY - HOTKEY_DIVIDER_GAP / 2, HOTKEY_DETAIL_W - HOTKEY_POPUP_PAD * 2);
        drawHotkeyMenuRow(graphics, popup.x, rowY, HOTKEY_DETAIL_W, FontAwesomeIcons.ICON_FA_TRASH,
                "Delete", false, DANGER, hotkeyDetailRowHovered(popup.x, rowY), false);
        } finally {
            popRenderClip(clipped);
        }
    }

    private boolean handleHotkeyPopupClick() {
        if (popupSystem.openHotkeyDetailPopup != null) {
            HotkeyDetailPopup detail = popupSystem.openHotkeyDetailPopup;
            if (PopupGeometry.contains(clickX, clickY, detail.x, detail.y, HOTKEY_DETAIL_W, hotkeyDetailHeight())) {
                int row = hotkeyRowAt(clickY, detail.y, 5);
                HotkeyBinding binding = findHotkeyBinding(detail.controlId, detail.bindingId);
                if (binding == null) {
                    clearHotkeyDetail();
                    clickConsumed = true;
                    return true;
                }
                if (row == 0) {
                    listeningHotkeyControlId = detail.controlId;
                    listeningHotkeyBindingId = detail.bindingId;
                    updateGameMovementInputBlock();
                } else if (row == 1) {
                    binding.mode = "Toggle".equals(binding.mode) ? "Hold" : "Toggle";
                    clearToggleRestoreValue(detail.controlId, binding);
                } else if (row == 2) {
                    handleHotkeyValueRowClick(detail, binding);
                } else if (row == 3) {
                    binding.visible = !binding.visible;
                } else if (row == 4) {
                    deleteHotkeyBinding(detail.controlId, detail.bindingId);
                }
                clickConsumed = true;
                return true;
            }
        }

        if (popupSystem.openHotkeyPopup != null) {
            HotkeyPopup popup = popupSystem.openHotkeyPopup;
            HotkeyControlState state = hotkeyStates.computeIfAbsent(popup.id, key -> new HotkeyControlState());
            if (PopupGeometry.contains(clickX, clickY, popup.x, popup.y, HOTKEY_POPUP_W, hotkeyPopupHeight(state))) {
                int row = hotkeyPrimaryRowAt(clickY, popup.y, state.bindings.size());
                if (row >= 0 && row < state.bindings.size()) {
                    HotkeyBinding binding = state.bindings.get(row);
                    popupSystem.openHotkeyDetailControlId = popup.id;
                    popupSystem.openHotkeyDetailBindingId = binding.id;
                } else if (row == state.bindings.size()) {
                    addHotkeyBinding(popup.id);
                    clearHotkeyDetail();
                } else if (row == state.bindings.size() + 1) {
                    openAllHotkeysWindow();
                    closeHotkeyPopups();
                } else if (row == state.bindings.size() + 2) {
                    resetHotkeys(popup.id);
                }
                clickConsumed = true;
                return true;
            }
        }

        closeHotkeyPopups();
        clickConsumed = true;
        return true;
    }

    private int hotkeyPrimaryRowAt(double py, int popupY, int bindingCount) {
        return hotkeyRowAt(py, popupY, bindingCount + 3);
    }

    private int hotkeyRowAt(double py, int popupY, int rowCount) {
        int y = popupY + HOTKEY_POPUP_PAD;
        for (int i = 0; i < rowCount; i++) {
            if (PopupGeometry.contains(0, py, 0, y, 1, HOTKEY_ROW_H)) {
                return i;
            }
            y += HOTKEY_ROW_H;
            if (i == rowCount - 2) {
                y += HOTKEY_DIVIDER_GAP;
            }
        }
        return -1;
    }

    private boolean hotkeyPopupHovered(HotkeyPopup popup) {
        HotkeyControlState state = hotkeyStates.computeIfAbsent(popup.id, key -> new HotkeyControlState());
        return PopupGeometry.contains(mouseX, mouseY, popup.x, popup.y, HOTKEY_POPUP_W, hotkeyPopupHeight(state));
    }

    private boolean hotkeyPopupRowHovered(int x, int y, int w) {
        return PopupGeometry.contains(mouseX, mouseY, x + HOTKEY_POPUP_PAD, y, w - HOTKEY_POPUP_PAD * 2, HOTKEY_ROW_H);
    }

    private boolean hotkeyPrimaryActionHovered(HotkeyPopup popup, int firstActionY) {
        int rowX = popup.x + HOTKEY_POPUP_PAD;
        int rowW = HOTKEY_POPUP_W - HOTKEY_POPUP_PAD * 2;
        int allY = firstActionY;
        int newY = allY + HOTKEY_ROW_H;
        int resetY = newY + HOTKEY_ROW_H + HOTKEY_DIVIDER_GAP;
        return PopupGeometry.contains(mouseX, mouseY, rowX, allY, rowW, HOTKEY_ROW_H)
                || PopupGeometry.contains(mouseX, mouseY, rowX, newY, rowW, HOTKEY_ROW_H)
                || PopupGeometry.contains(mouseX, mouseY, rowX, resetY, rowW, HOTKEY_ROW_H);
    }

    private void drawPopupSurface(GuiGraphics graphics, int x, int y, int w, int h, int radius) {
        drawPopupBlur(graphics, x, y, w, h, radius);
        ShadowRenderer.shadow(graphics, x, y, w, h, radius, ShadowStyle.popup(), renderAlpha);
        ShapeRenderer.roundRect(graphics, x, y, w, h, radius, popupSurfaceColor());
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, radius, 1, BORDER);
    }

    private void drawPopupBlur(GuiGraphics graphics, int x, int y, int w, int h, int radius) {
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        double guiScale = window.getGuiScale();
        if (guiScale <= 0.0) {
            return;
        }
        double originX = window.getWidth() * 0.5;
        double originY = window.getHeight() * 0.5;
        double logicalX = x + renderOffsetX;
        double logicalY = y + renderOffsetY;
        double scaledX = originX + (logicalX - originX) * renderScale;
        double scaledY = originY + (logicalY - originY) * renderScale;
        KawaseBlurPipeline.renderSurfaceBackdrop(
                graphics,
                window.getGuiScaledWidth(),
                window.getGuiScaledHeight(),
                (float) (scaledX / guiScale),
                (float) (scaledY / guiScale),
                (float) (w * renderScale / guiScale),
                (float) (h * renderScale / guiScale),
                (float) (radius * renderScale / guiScale),
                renderAlpha
        );
    }

    private static int popupSurfaceColor() {
        return themeColor(0xB8FFFFFF, SURFACE);
    }

    private void drawHotkeyMenuRow(GuiGraphics graphics, int x, int y, int w, int icon, String label,
                                   boolean arrow, int color, boolean hovered, boolean selected) {
        int rowX = x + 4;
        int rowW = w - 8;
        float hover = controlAnim("hotkey-row/" + label + "/" + x + "/" + y, "hover", hovered || selected ? 1.0f : 0.0f);
        if (hover > 0.01f) {
            ShapeRenderer.roundRect(graphics, rowX, y, rowW, HOTKEY_ROW_H, 5,
                    lerpColor(themeColor(0x00F4F5FA, 0x00222734), selected ? ACCENT_SOFT : themeColor(0xFFF4F5FA, FIELD), hover));
        }
        drawHotkeyIcon(graphics, icon, x + 18, y, color == DANGER ? DANGER : MUTED);
        drawClippedText(graphics, label, x + 38, y + HOTKEY_TEXT_Y_OFFSET - 1, w - 58, color);
        if (arrow) {
            float arrowX = x + w - 18 - MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_ANGLE_RIGHT, HOTKEY_ARROW_ICON_SIZE) * 0.5f;
            MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_ANGLE_RIGHT,
                    arrowX, y + (HOTKEY_ROW_H - HOTKEY_ARROW_ICON_SIZE) * 0.5f, HOTKEY_ARROW_ICON_SIZE,
                    alphaColor(MUTED, renderAlpha));
        }
    }

    private void drawHotkeyDetailRow(GuiGraphics graphics, int x, int y, int w, int icon, String label,
                                     String value, boolean selectedPill, int color, boolean hovered, boolean rightAlignValue) {
        int rowX = x + 4;
        int rowW = w - 8;
        float hover = controlAnim("hotkey-detail/" + label + "/" + x + "/" + y, "hover", hovered ? 1.0f : 0.0f);
        if (hover > 0.01f) {
            ShapeRenderer.roundRect(graphics, rowX, y, rowW, HOTKEY_ROW_H, 5, lerpColor(themeColor(0x00F4F5FA, 0x00222734), themeColor(0xFFF4F5FA, FIELD), hover));
        }
        drawHotkeyIcon(graphics, icon, x + 18, y, MUTED);
        text(graphics, label, x + 38, y + HOTKEY_TEXT_Y_OFFSET - 1, color);
        String valueText = value == null || value.isEmpty() ? "-" : value;
        if (selectedPill) {
            int pillW = Math.max(54, textWidth(valueText) + 22);
            int pillX = x + w - HOTKEY_RIGHT_PAD - pillW;
            ShapeRenderer.roundRect(graphics, pillX, y + 5, pillW, 18, 8, ACCENT_SOFT);
            ShapeRenderer.roundRect(graphics, pillX + 8, y + 10, 8, 8, 4, ACCENT);
            drawClippedText(graphics, valueText, pillX + 22, y + HOTKEY_TEXT_Y_OFFSET - 1, pillW - 28, ACCENT_DARK);
        } else {
            int valueW = Math.max(0, w - 92);
            int valueTextW = textWidth(valueText);
            int valueX = x + w - HOTKEY_RIGHT_PAD - valueW;
            if (rightAlignValue) {
                valueX = x + w - HOTKEY_RIGHT_PAD - Math.min(valueW, valueTextW);
            }
            drawClippedText(graphics, valueText, valueX, y + HOTKEY_TEXT_Y_OFFSET - 1, valueW, MUTED);
        }
    }

    private void drawHotkeyModeRow(GuiGraphics graphics, int x, int y, int w, String controlId,
                                   HotkeyBinding binding, boolean hovered) {
        int rowX = x + 4;
        int rowW = w - 8;
        float hover = controlAnim("hotkey-mode/" + x + "/" + y, "hover", hovered ? 1.0f : 0.0f);
        if (hover > 0.01f) {
            ShapeRenderer.roundRect(graphics, rowX, y, rowW, HOTKEY_ROW_H, 5, lerpColor(themeColor(0x00F4F5FA, 0x00222734), themeColor(0xFFF4F5FA, FIELD), hover));
        }
        drawHotkeyIcon(graphics, FontAwesomeIcons.ICON_FA_KEYBOARD, x + 18, y, MUTED);
        text(graphics, "Mode", x + 38, y + HOTKEY_TEXT_Y_OFFSET - 1, TEXT);

        boolean toggle = "Toggle".equals(binding.mode);
        float holdAmount = controlAnim("hotkey-mode-choice/" + controlId + "/" + binding.id,
                "hold", toggle ? 0.0f : 1.0f, UiAnimationTimings.HOTKEY_MODE);
        float toggleBg = 1.0f - stagedModeBackground(holdAmount);
        float holdBg = stagedModeBackground(holdAmount);
        float toggleText = 1.0f - stagedModeOutgoingText(holdAmount);
        float holdText = stagedModeIncomingText(holdAmount);
        float right = x + w - HOTKEY_RIGHT_PAD;
        float holdW = modeOptionWidth("Hold", FontAwesomeIcons.ICON_FA_HAND, holdBg);
        float toggleW = modeOptionWidth("Toggle", FontAwesomeIcons.ICON_FA_TOGGLE_ON, toggleBg);
        float gap = 2.0f;
        float holdX = right - holdW;
        float toggleX = holdX - gap - toggleW;
        drawModeOption(graphics, toggleX, y + 4, toggleW, "Toggle", FontAwesomeIcons.ICON_FA_TOGGLE_ON, toggleBg, toggleText);
        drawModeOption(graphics, holdX, y + 4, holdW, "Hold", FontAwesomeIcons.ICON_FA_HAND, holdBg, holdText);
    }

    private static float stagedModeOutgoingText(float holdAmount) {
        return smoothstep(holdAmount / 0.50f);
    }

    private static float stagedModeBackground(float holdAmount) {
        return smoothstep((holdAmount - 0.20f) / 0.70f);
    }

    private static float stagedModeIncomingText(float holdAmount) {
        return smoothstep((holdAmount - 0.50f) / 0.50f);
    }

    private static float modeOptionWidth(String label, int icon, float backgroundProgress) {
        float iconSize = 9.5f;
        float iconW = MSDFRenderer.iconWidth(icon, iconSize);
        float iconOnlyW = Math.max(22.0f, iconW + 12.0f);
        float fullW = Math.max(36.0f, 8.0f + iconW + 5.0f + textWidth(label) + 8.0f);
        return iconOnlyW + (fullW - iconOnlyW) * smoothstep(backgroundProgress);
    }

    private void drawModeOption(GuiGraphics graphics, float x, int y, float w, String label, int icon,
                                float backgroundProgress, float textAlpha) {
        float bg = smoothstep(backgroundProgress);
        float text = smoothstep(textAlpha);
        int color = lerpColor(MUTED, ACCENT_DARK, Math.max(bg, text));
        int h = 20;
        float iconSize = 9.5f;
        float iconW = MSDFRenderer.iconWidth(icon, iconSize);
        if (bg > 0.01f) {
            ShapeRenderer.roundRect(graphics, Math.round(x), y, Math.round(w), h, 8, alphaColor(ACCENT_SOFT, bg));
        }

        float centeredIconX = x + (w - iconW) * 0.5f;
        float expandedIconX = x + 8.0f;
        float iconX = centeredIconX + (expandedIconX - centeredIconX) * bg;
        MSDFRenderer.drawIconCodepoint(graphics, icon, iconX, y + (h - iconSize) * 0.5f, iconSize,
                alphaColor(color, renderAlpha));

        if (text > 0.02f) {
            int textAlphaColor = alphaColor(ACCENT_DARK, renderAlpha * text);
            int textX = Math.round(x + 8.0f + iconW + 5.0f);
            int textW = Math.max(0, textWidth(label) + 2);
            drawClippedText(graphics, label, textX, y + 2, textW, textAlphaColor);
        }
    }

    private void drawHotkeySwitchRow(GuiGraphics graphics, int x, int y, int w, int icon, String label,
                                     boolean value, boolean hovered) {
        int rowX = x + 4;
        int rowW = w - 8;
        float hover = controlAnim("hotkey-switch-row/" + label + "/" + x + "/" + y, "hover", hovered ? 1.0f : 0.0f);
        if (hover > 0.01f) {
            ShapeRenderer.roundRect(graphics, rowX, y, rowW, HOTKEY_ROW_H, 5, lerpColor(themeColor(0x00F4F5FA, 0x00222734), themeColor(0xFFF4F5FA, FIELD), hover));
        }
        drawHotkeyIcon(graphics, icon, x + 18, y, MUTED);
        text(graphics, label, x + 37, y + HOTKEY_TEXT_Y_OFFSET - 1, TEXT);
        drawSmallSwitch(graphics, x + w - HOTKEY_RIGHT_PAD - HOTKEY_SWITCH_W, y + 7, value);
    }

    private void drawHotkeyValueRow(GuiGraphics graphics, int x, int y, int w, String controlId,
                                    HotkeyBinding binding, boolean hovered) {
        HotkeyMeta meta = hotkeyMetas.getOrDefault(controlId, HotkeyMeta.EMPTY);
        int rowX = x + 4;
        int rowW = w - 8;
        float hover = controlAnim("hotkey-value/" + controlId + "/" + binding.id, "hover", hovered ? 1.0f : 0.0f);
        if (hover > 0.01f) {
            ShapeRenderer.roundRect(graphics, rowX, y, rowW, HOTKEY_ROW_H, 5, lerpColor(themeColor(0x00F4F5FA, 0x00222734), themeColor(0xFFF4F5FA, FIELD), hover));
        }
        drawHotkeyIcon(graphics, FontAwesomeIcons.ICON_FA_LINK, x + 18, y, MUTED);
        text(graphics, "Value", x + 37, y + HOTKEY_TEXT_Y_OFFSET - 1, TEXT);

        int controlX = hotkeyValueControlX(x, w);
        int controlY = y + 4;
        switch (meta.valueType) {
            case BOOL -> drawSmallSwitch(graphics, x + w - HOTKEY_RIGHT_PAD - HOTKEY_SWITCH_W, y + 7,
                    binding.boolValue != null && binding.boolValue);
            case SELECT -> drawHotkeyValueSelect(graphics, controlId, binding, controlX, controlY);
            case MULTI_SELECT -> drawHotkeyValueMulti(graphics, controlId, binding, controlX, controlY);
            case SLIDER -> drawHotkeyValueSlider(graphics, controlId, binding, controlX, controlY);
            case COLOR -> drawHotkeyValueColor(graphics, controlId, binding, controlX, controlY);
            case LABEL -> drawClippedText(graphics, binding.valueLabel, controlX, y + HOTKEY_TEXT_Y_OFFSET - 1,
                    HOTKEY_VALUE_W, MUTED);
        }
    }

    private void drawHotkeyValueSelect(GuiGraphics graphics, String controlId, HotkeyBinding binding, int x, int y) {
        String key = PopupGeometry.hotkeyBindingKey(controlId, binding.id);
        boolean open = key.equals(popupSystem.openHotkeyValueSelectKey);
        ShapeRenderer.roundRect(graphics, x, y, HOTKEY_VALUE_W, HOTKEY_VALUE_H, 6, FIELD);
        ShapeRenderer.roundRectBorder(graphics, x, y, HOTKEY_VALUE_W, HOTKEY_VALUE_H, 6, 1, open ? ACCENT : BORDER);
        drawClippedText(graphics, normalizedHotkeySelectValue(binding), x + 7, y + 2, HOTKEY_VALUE_W - 24, TEXT);
        MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_ANGLE_DOWN, x + HOTKEY_VALUE_W - 15,
                y + 6, 7.0f, alphaColor(open ? ACCENT_DARK : MUTED, renderAlpha));
        if (open) {
            popupSystem.pendingHotkeyValueSelectPopup = new HotkeyValueSelectPopup(controlId, binding.id, x,
                    y + HOTKEY_VALUE_H + 2, Math.max(HOTKEY_VALUE_POPUP_W, HOTKEY_VALUE_W), hotkeyOptions(controlId, binding));
        }
    }

    private void drawHotkeyValueMulti(GuiGraphics graphics, String controlId, HotkeyBinding binding, int x, int y) {
        String key = PopupGeometry.hotkeyBindingKey(controlId, binding.id);
        boolean open = key.equals(popupSystem.openHotkeyValueMultiKey);
        ShapeRenderer.roundRect(graphics, x, y, HOTKEY_VALUE_W, HOTKEY_VALUE_H, 6, FIELD);
        ShapeRenderer.roundRectBorder(graphics, x, y, HOTKEY_VALUE_W, HOTKEY_VALUE_H, 6, 1, open ? ACCENT : BORDER);
        String[] options = hotkeyOptions(controlId, binding);
        int allMask = WidgetSurfaceValueTools.optionMask(options.length);
        drawClippedText(graphics, WidgetSurfaceValueTools.multiSelectSummary(binding.multiMask, options, allMask), x + 7, y + 2,
                HOTKEY_VALUE_W - 24, TEXT);
        MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_ANGLE_DOWN, x + HOTKEY_VALUE_W - 15,
                y + 6, 7.0f, alphaColor(open ? ACCENT_DARK : MUTED, renderAlpha));
        if (open) {
            popupSystem.pendingHotkeyValueMultiPopup = new HotkeyValueMultiPopup(controlId, binding.id, x,
                    y + HOTKEY_VALUE_H + 2, Math.max(HOTKEY_VALUE_POPUP_W, HOTKEY_VALUE_W), options);
        }
    }

    private void drawHotkeyValueSlider(GuiGraphics graphics, String controlId, HotkeyBinding binding, int x, int y) {
        drawHotkeyValueSlider(graphics, controlId, binding, x, y, HOTKEY_VALUE_W);
    }

    private void drawHotkeyValueSlider(GuiGraphics graphics, String controlId, HotkeyBinding binding, int x, int y, int w) {
        String id = PopupGeometry.hotkeyBindingKey(controlId, binding.id) + "/slider";
        int valueTextW = 38;
        int dividerX = x + w - valueTextW - 1;
        int trackX = x + 6;
        int trackW = Math.max(20, dividerX - trackX - 7);
        int trackY = y + 9;
        if (!isEditingHotkeySlider(controlId, binding.id)) {
            binding.sliderValue = hotkeySliderFloat(id, trackX, trackW, binding.sliderValue);
        }
        binding.valueLabel = hotkeySliderValueLabel(controlId, binding);

        ShapeRenderer.roundRect(graphics, x, y, w, HOTKEY_VALUE_H, 6, FIELD);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, HOTKEY_VALUE_H, 6, 1,
                id.equals(activeId) ? ACCENT : BORDER);
        ShapeRenderer.roundRect(graphics, trackX, trackY, trackW, 3, 2, TRACK);
        float displayValue = controlAnim(id, "value", clamp01(binding.sliderValue));
        int filled = Math.round(trackW * displayValue);
        ShapeRenderer.roundRect(graphics, trackX, trackY, filled, 3, 2, ACCENT);
        ShapeRenderer.roundRect(graphics, trackX + filled - 3, trackY - 3, 7, 9, 4, ACCENT_DARK);
        graphics.vLine(dividerX, y + 4, y + HOTKEY_VALUE_H - 5, alphaColor(BORDER, renderAlpha));

        boolean editing = isEditingHotkeySlider(controlId, binding.id);
        String valueText = editing ? editingHotkeySliderText
                : (binding.valueLabel == null || binding.valueLabel.isBlank() ? "-" : binding.valueLabel);
        int valueX = dividerX + 1;
        int valueW = x + w - valueX;
        boolean hovered = hovered(valueX, y, valueW, HOTKEY_VALUE_H);
        if (editing || hovered) {
            ShapeRenderer.roundRect(graphics, valueX + 1, y + 2, valueW - 2, HOTKEY_VALUE_H - 4, 5,
                    editing ? ACCENT_SOFT : themeColor(0xFFF7F8FC, FIELD));
        }
        int textMaxW = valueW - 9;
        int textW = textWidth(valueText, HOTKEY_VALUE_TEXT_SIZE);
        int textX = valueX + valueW - 5 - Math.min(textMaxW, textW);
        int textY = y + Math.max(0, (HOTKEY_VALUE_H - Math.round(HOTKEY_VALUE_TEXT_SIZE)) / 2) - 1;
        drawClippedText(graphics, valueText, textX, textY, textMaxW, HOTKEY_VALUE_TEXT_SIZE, editing ? TEXT : MUTED);
        if (editing && ((System.currentTimeMillis() / 500L) & 1L) == 0L) {
            int cursorX = valueX + valueW - 4;
            graphics.vLine(cursorX, y + 3, y + HOTKEY_VALUE_H - 3, alphaColor(ACCENT_DARK, renderAlpha));
        }
    }

    private void drawHotkeyValueColor(GuiGraphics graphics, String controlId, HotkeyBinding binding, int x, int y) {
        drawHotkeyValueColor(graphics, controlId, binding, x, y, HOTKEY_VALUE_W);
    }

    private void drawHotkeyValueColor(GuiGraphics graphics, String controlId, HotkeyBinding binding, int x, int y, int w) {
        String key = PopupGeometry.hotkeyBindingKey(controlId, binding.id);
        boolean open = key.equals(popupSystem.openHotkeyValueColorKey);
        binding.valueLabel = colorHex(binding.colorValue);
        ShapeRenderer.roundRect(graphics, x, y, w, HOTKEY_VALUE_H, 6, FIELD);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, HOTKEY_VALUE_H, 6, 1, open ? ACCENT : BORDER);
        ShapeRenderer.roundRect(graphics, x + 6, y + 5, 22, 10, 4, binding.colorValue);
        ShapeRenderer.roundRectBorder(graphics, x + 6, y + 5, 22, 10, 4, 1, 0x55FFFFFF);
        drawClippedText(graphics, binding.valueLabel, x + 34, y + 3, w - 38, MUTED);
        if (open) {
            popupSystem.pendingHotkeyValueColorPopup = new HotkeyValueColorPopup(controlId, binding.id, x,
                    y + HOTKEY_VALUE_H + 2, COLOR_POPUP_W);
        }
    }

    private float hotkeySliderFloat(String id, int trackX, int trackW, float value) {
        if (id.equals(activeId)) {
            float next = (float) ((mouseX - trackX) / Math.max(1.0, trackW));
            return clamp01(next);
        }
        return value;
    }

    private void handleHotkeyValueRowClick(HotkeyDetailPopup detail, HotkeyBinding binding) {
        HotkeyMeta meta = hotkeyMetas.getOrDefault(detail.controlId, HotkeyMeta.EMPTY);
        String key = PopupGeometry.hotkeyBindingKey(detail.controlId, binding.id);
        switch (meta.valueType) {
            case BOOL -> {
                binding.boolValue = !(binding.boolValue != null && binding.boolValue);
                binding.valueLabel = onOff(binding.boolValue);
                closeHotkeyValuePopups();
            }
            case SELECT -> {
                boolean openNow = !key.equals(popupSystem.openHotkeyValueSelectKey);
                if (popupSystem.openHotkeyValueSelectPopup != null && (!openNow
                        || !detail.controlId.equals(popupSystem.openHotkeyValueSelectPopup.controlId)
                        || binding.id != popupSystem.openHotkeyValueSelectPopup.bindingId)) {
                    closeHotkeyValueSelectPopupVisual();
                }
                popupSystem.openHotkeyValueSelectKey = openNow ? key : null;
                popupSystem.openHotkeyValueMultiKey = null;
                closeHotkeyValueMultiPopupVisual();
                popupSystem.openHotkeyValueColorKey = null;
                closeHotkeyValueColorPopupVisual();
            }
            case MULTI_SELECT -> {
                boolean openNow = !key.equals(popupSystem.openHotkeyValueMultiKey);
                if (popupSystem.openHotkeyValueMultiPopup != null && (!openNow
                        || !detail.controlId.equals(popupSystem.openHotkeyValueMultiPopup.controlId)
                        || binding.id != popupSystem.openHotkeyValueMultiPopup.bindingId)) {
                    closeHotkeyValueMultiPopupVisual();
                }
                popupSystem.openHotkeyValueMultiKey = openNow ? key : null;
                popupSystem.openHotkeyValueSelectKey = null;
                closeHotkeyValueSelectPopupVisual();
                popupSystem.openHotkeyValueColorKey = null;
                closeHotkeyValueColorPopupVisual();
            }
            case SLIDER -> {
                popupSystem.openHotkeyValueSelectKey = null;
                closeHotkeyValueSelectPopupVisual();
                popupSystem.openHotkeyValueMultiKey = null;
                closeHotkeyValueMultiPopupVisual();
                popupSystem.openHotkeyValueColorKey = null;
                closeHotkeyValueColorPopupVisual();
                int valueX = hotkeyValueControlX(detail.x, HOTKEY_DETAIL_W);
                int valueY = hotkeyValueRowY(detail.y) + 4;
                int valueTextW = 38;
                int dividerX = valueX + HOTKEY_VALUE_W - valueTextW - 1;
                int textX = dividerX + 1;
                int textW = valueX + HOTKEY_VALUE_W - textX;
                if (PopupGeometry.contains(clickX, clickY, textX, valueY, textW, HOTKEY_VALUE_H)) {
                    startHotkeySliderEdit(detail.controlId, binding, hotkeySliderValueLabel(detail.controlId, binding),
                            textX, valueY, textW, HOTKEY_VALUE_H);
                    return;
                }
                int trackX = valueX + 6;
                int trackW = Math.max(20, dividerX - trackX - 7);
                if (!PopupGeometry.contains(clickX, clickY, trackX - 4, valueY, trackW + 8, HOTKEY_VALUE_H)) {
                    return;
                }
                cancelHotkeySliderEdit();
                activeId = key + "/slider";
                binding.sliderValue = clamp01((float) ((clickX - trackX) / Math.max(1.0, trackW)));
                binding.valueLabel = hotkeySliderValueLabel(detail.controlId, binding);
            }
            case COLOR -> {
                boolean openNow = !key.equals(popupSystem.openHotkeyValueColorKey);
                if (popupSystem.openHotkeyValueColorPopup != null && (!openNow
                        || !detail.controlId.equals(popupSystem.openHotkeyValueColorPopup.controlId)
                        || binding.id != popupSystem.openHotkeyValueColorPopup.bindingId)) {
                    closeHotkeyValueColorPopupVisual();
                }
                popupSystem.openHotkeyValueColorKey = openNow ? key : null;
                popupSystem.openHotkeyValueSelectKey = null;
                closeHotkeyValueSelectPopupVisual();
                popupSystem.openHotkeyValueMultiKey = null;
                closeHotkeyValueMultiPopupVisual();
            }
            case LABEL -> closeHotkeyValuePopups();
        }
    }

    private void renderHotkeyValueSelectPopup(GuiGraphics graphics, HotkeyValueSelectPopup popup) {
        HotkeyBinding binding = findHotkeyBinding(popup.controlId, popup.bindingId);
        if (binding == null) {
            closeHotkeyValuePopups();
            return;
        }
        int h = popupHeight(popup.options.length);
        drawPopupSurface(graphics, popup.x, popup.y, popup.w, h, 7);
        boolean clipped = pushRenderClip(popup.x, popup.y, popup.w, h);
        try {
        String selected = normalizedHotkeySelectValue(binding);
        for (int i = 0; i < popup.options.length; i++) {
            int optionY = popup.y + SELECT_POPUP_PAD_Y + i * (SELECT_OPTION_HEIGHT + SELECT_OPTION_GAP);
            String option = popup.options[i];
            boolean current = option.equals(selected);
            boolean hovered = PopupGeometry.contains(mouseX, mouseY, popup.x, optionY, popup.w, SELECT_OPTION_HEIGHT);
            String optionId = "hotkey-select-option/" + popup.controlId + "/" + popup.bindingId + "/" + i;
            float previousAlpha = beginSelectOptionAnimation(graphics, i);
            try {
                drawSelectOptionBackground(graphics, optionId, popup.x + 3, optionY, popup.w - 6, current, hovered);
                drawClippedText(graphics, option, popup.x + ITEM_SPACING_X,
                        optionY + (SELECT_OPTION_HEIGHT - FONT_SIZE) / 2 - 2, popup.w - ITEM_SPACING_X * 2,
                        selectOptionTextColor(optionId, current, hovered));
            } finally {
                endSelectOptionAnimation(graphics, previousAlpha);
            }
        }
        } finally {
            popRenderClip(clipped);
        }
    }

    private void renderHotkeyValueMultiPopup(GuiGraphics graphics, HotkeyValueMultiPopup popup) {
        HotkeyBinding binding = findHotkeyBinding(popup.controlId, popup.bindingId);
        if (binding == null) {
            closeHotkeyValuePopups();
            return;
        }
        int h = popupHeight(popup.options.length);
        drawPopupSurface(graphics, popup.x, popup.y, popup.w, h, 7);
        boolean clipped = pushRenderClip(popup.x, popup.y, popup.w, h);
        try {
        for (int i = 0; i < popup.options.length; i++) {
            int optionY = popup.y + SELECT_POPUP_PAD_Y + i * (SELECT_OPTION_HEIGHT + SELECT_OPTION_GAP);
            boolean selected = (binding.multiMask & (1 << i)) != 0;
            boolean hovered = PopupGeometry.contains(mouseX, mouseY, popup.x, optionY, popup.w, SELECT_OPTION_HEIGHT);
            String optionId = "hotkey-multi-option/" + popup.controlId + "/" + popup.bindingId + "/" + i;
            float previousAlpha = beginSelectOptionAnimation(graphics, i);
            try {
                drawSelectOptionBackground(graphics, optionId, popup.x + 3, optionY, popup.w - 6, selected, hovered);
                drawCheckbox(graphics, popup.x + 7, optionY + 2, selected,
                        controlAnim(optionId, "checked", selected ? 1.0f : 0.0f));
                drawClippedText(graphics, popup.options[i], popup.x + 32,
                        optionY + (SELECT_OPTION_HEIGHT - FONT_SIZE) / 2 - 2, popup.w - 38,
                        selectOptionTextColor(optionId, selected, hovered));
            } finally {
                endSelectOptionAnimation(graphics, previousAlpha);
            }
        }
        } finally {
            popRenderClip(clipped);
        }
    }

    private void renderHotkeyValueColorPopup(GuiGraphics graphics, HotkeyValueColorPopup popup) {
        HotkeyBinding binding = findHotkeyBinding(popup.controlId, popup.bindingId);
        if (binding == null) {
            closeHotkeyValuePopups();
            return;
        }
        int h = colorPopupHeight();
        drawPopupSurface(graphics, popup.x, popup.y, popup.w, h, 8);

        int contentX = popup.x + COLOR_POPUP_PAD_X;
        int contentY = popup.y + COLOR_POPUP_PAD_Y;
        int contentW = popup.w - COLOR_POPUP_PAD_X * 2;
        ShapeRenderer.roundRect(graphics, contentX, contentY, contentW, COLOR_POPUP_PREVIEW_H, 6, binding.colorValue);
        ShapeRenderer.roundRectBorder(graphics, contentX, contentY, contentW, COLOR_POPUP_PREVIEW_H, 6, 1, BORDER);
        text(graphics, colorHex(binding.colorValue), contentX + 7, contentY + 1, TEXT);

        int red = (binding.colorValue >>> 16) & 0xFF;
        int green = (binding.colorValue >>> 8) & 0xFF;
        int blue = binding.colorValue & 0xFF;
        int alpha = (binding.colorValue >>> 24) & 0xFF;
        int rowY = contentY + COLOR_POPUP_PREVIEW_H + ITEM_INNER_SPACING_Y;
        red = hotkeyColorChannelSlider(graphics, PopupGeometry.hotkeyBindingKey(popup.controlId, popup.bindingId) + "/r",
                "R", red, contentX, rowY, contentW, 0xFFFF5F67);
        green = hotkeyColorChannelSlider(graphics, PopupGeometry.hotkeyBindingKey(popup.controlId, popup.bindingId) + "/g",
                "G", green, contentX, rowY + COLOR_ROW_HEIGHT + COLOR_ROW_GAP, contentW, 0xFF2FCB8B);
        blue = hotkeyColorChannelSlider(graphics, PopupGeometry.hotkeyBindingKey(popup.controlId, popup.bindingId) + "/b",
                "B", blue, contentX, rowY + (COLOR_ROW_HEIGHT + COLOR_ROW_GAP) * 2, contentW, 0xFF6E72FF);
        alpha = hotkeyColorChannelSlider(graphics, PopupGeometry.hotkeyBindingKey(popup.controlId, popup.bindingId) + "/a",
                "A", alpha, contentX, rowY + (COLOR_ROW_HEIGHT + COLOR_ROW_GAP) * 3, contentW, 0xFF8A91A8);
        binding.colorValue = composeColor(alpha, red, green, blue);
        binding.valueLabel = colorHex(binding.colorValue);
    }

    private int hotkeyColorChannelSlider(GuiGraphics graphics, String id, String label, int value, int x, int y, int w, int color) {
        text(graphics, label, x, y + 1, MUTED);
        int trackX = x + 16;
        int trackW = w - 16;
        int next = value;
        if (id.equals(activeId)) {
            next = Math.round(clamp01((float) ((mouseX - trackX) / Math.max(1.0, trackW))) * 255.0f);
        }
        float displayValue = controlAnim(id, "value", next / 255.0f);
        ShapeRenderer.roundRect(graphics, trackX, y + 5, trackW, 3, 2, TRACK);
        int filled = Math.round(trackW * displayValue);
        ShapeRenderer.roundRect(graphics, trackX, y + 5, filled, 3, 2, color);
        ShapeRenderer.roundRect(graphics, trackX + filled - 3, y + 3, 7, 7, 4, color);
        return Math.max(0, Math.min(255, next));
    }

    private boolean handleHotkeyValuePopupClick() {
        if (popupSystem.openHotkeyValueSelectPopup != null) {
            HotkeyValueSelectPopup popup = popupSystem.openHotkeyValueSelectPopup;
            int h = popupHeight(popup.options.length);
            if (PopupGeometry.contains(clickX, clickY, popup.x, popup.y, popup.w, h)) {
                HotkeyBinding binding = findHotkeyBinding(popup.controlId, popup.bindingId);
                if (binding != null) {
                    for (int i = 0; i < popup.options.length; i++) {
                        int optionY = popup.y + SELECT_POPUP_PAD_Y + i * (SELECT_OPTION_HEIGHT + SELECT_OPTION_GAP);
                        if (PopupGeometry.contains(clickX, clickY, popup.x, optionY, popup.w, SELECT_OPTION_HEIGHT)) {
                            binding.selectedValue = popup.options[i];
                            binding.valueLabel = binding.selectedValue;
                            closeHotkeyValuePopups();
                            popupSystem.keepHotkeyDetailThisFrame = true;
                            clickConsumed = true;
                            return true;
                        }
                    }
                }
                clickConsumed = true;
                return true;
            }
        }

        if (popupSystem.openHotkeyValueMultiPopup != null) {
            HotkeyValueMultiPopup popup = popupSystem.openHotkeyValueMultiPopup;
            int h = popupHeight(popup.options.length);
            if (PopupGeometry.contains(clickX, clickY, popup.x, popup.y, popup.w, h)) {
                HotkeyBinding binding = findHotkeyBinding(popup.controlId, popup.bindingId);
                if (binding != null) {
                    for (int i = 0; i < popup.options.length; i++) {
                        int optionY = popup.y + SELECT_POPUP_PAD_Y + i * (SELECT_OPTION_HEIGHT + SELECT_OPTION_GAP);
                        if (PopupGeometry.contains(clickX, clickY, popup.x, optionY, popup.w, SELECT_OPTION_HEIGHT)) {
                            binding.multiMask = (binding.multiMask ^ (1 << i)) & WidgetSurfaceValueTools.optionMask(popup.options.length);
                            binding.valueLabel = WidgetSurfaceValueTools.multiSelectSummary(binding.multiMask, popup.options, WidgetSurfaceValueTools.optionMask(popup.options.length));
                            popupSystem.keepHotkeyDetailThisFrame = true;
                            clickConsumed = true;
                            return true;
                        }
                    }
                }
                clickConsumed = true;
                return true;
            }
        }

        if (popupSystem.openHotkeyValueColorPopup != null) {
            HotkeyValueColorPopup popup = popupSystem.openHotkeyValueColorPopup;
            int h = colorPopupHeight();
            if (PopupGeometry.contains(clickX, clickY, popup.x, popup.y, popup.w, h)) {
                handleHotkeyColorPopupClick(popup);
                popupSystem.keepHotkeyDetailThisFrame = true;
                clickConsumed = true;
                return true;
            }
        }

        if (clickInsideOpenHotkeyValueControl()) {
            closeHotkeyValuePopups();
            popupSystem.keepHotkeyDetailThisFrame = true;
            clickConsumed = true;
            return true;
        }

        if (clickInsideOpenHotkeyValueRow()) {
            return false;
        }

        closeHotkeyValuePopups();
        return false;
    }

    private void handleHotkeyColorPopupClick(HotkeyValueColorPopup popup) {
        int contentX = popup.x + COLOR_POPUP_PAD_X;
        int contentY = popup.y + COLOR_POPUP_PAD_Y;
        int contentW = popup.w - COLOR_POPUP_PAD_X * 2;
        int trackX = contentX + 16;
        int trackW = contentW - 16;
        int rowY = contentY + COLOR_POPUP_PREVIEW_H + ITEM_INNER_SPACING_Y;
        String key = PopupGeometry.hotkeyBindingKey(popup.controlId, popup.bindingId);
        for (int i = 0; i < 4; i++) {
            int y = rowY + (COLOR_ROW_HEIGHT + COLOR_ROW_GAP) * i;
            if (PopupGeometry.contains(clickX, clickY, trackX, y - 2, trackW, COLOR_ROW_HEIGHT + 4)) {
                activeId = key + (i == 0 ? "/r" : i == 1 ? "/g" : i == 2 ? "/b" : "/a");
                return;
            }
        }
    }

    private int hotkeyValueControlX(int popupX, int popupW) {
        return popupX + popupW - HOTKEY_RIGHT_PAD - HOTKEY_VALUE_W;
    }

    private int hotkeyValueRowY(int popupY) {
        return popupY + HOTKEY_POPUP_PAD + HOTKEY_ROW_H * 2;
    }

    private boolean hotkeyDetailRowHovered(int popupX, int rowY) {
        return !insideOpenHotkeyValuePopup(mouseX, mouseY)
                && PopupGeometry.contains(mouseX, mouseY, popupX + 4, rowY, HOTKEY_DETAIL_W - 8, HOTKEY_ROW_H);
    }

    private boolean clickInsideOpenHotkeyValueControl() {
        if (popupSystem.openHotkeyDetailPopup == null) {
            return false;
        }
        int x = hotkeyValueControlX(popupSystem.openHotkeyDetailPopup.x, HOTKEY_DETAIL_W);
        int y = hotkeyValueRowY(popupSystem.openHotkeyDetailPopup.y) + 4;
        return PopupGeometry.contains(clickX, clickY, x, y, HOTKEY_VALUE_W, HOTKEY_VALUE_H);
    }

    private boolean clickInsideOpenHotkeyValueRow() {
        if (popupSystem.openHotkeyDetailPopup == null) {
            return false;
        }
        int y = hotkeyValueRowY(popupSystem.openHotkeyDetailPopup.y);
        return PopupGeometry.contains(clickX, clickY, popupSystem.openHotkeyDetailPopup.x + 4, y,
                HOTKEY_DETAIL_W - 8, HOTKEY_ROW_H);
    }

    private String[] hotkeyOptions(String controlId, HotkeyBinding binding) {
        if (binding.options != null && binding.options.length > 0) {
            return binding.options;
        }
        HotkeyMeta meta = hotkeyMetas.getOrDefault(controlId, HotkeyMeta.EMPTY);
        return meta.options == null ? new String[0] : meta.options;
    }

    private String normalizedHotkeySelectValue(HotkeyBinding binding) {
        if (binding.options != null && binding.options.length > 0 && !WidgetSurfaceValueTools.containsOption(binding.selectedValue, binding.options)) {
            binding.selectedValue = binding.options[0];
        }
        if (binding.selectedValue == null || binding.selectedValue.isBlank()) {
            binding.selectedValue = "-";
        }
        binding.valueLabel = binding.selectedValue;
        return binding.selectedValue;
    }

    private static String hotkeySliderValueLabel(String controlId, HotkeyBinding binding) {
        Function<Float, String> formatter = binding.sliderFormatter;
        if (formatter == null) {
            HotkeyMeta meta = hotkeyMetas.getOrDefault(controlId, HotkeyMeta.EMPTY);
            formatter = meta.sliderFormatter;
        }
        if (formatter != null) {
            String value = formatter.apply(clamp01(binding.sliderValue));
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return WidgetSurfaceValueTools.sliderValueLabel(binding.sliderValue);
    }

    private void drawHotkeyIcon(GuiGraphics graphics, int codepoint, float centerX, float y, int color) {
        float size = HOTKEY_ICON_SIZE;
        float iconX = centerX - MSDFRenderer.iconWidth(codepoint, size) * 0.5f;
        MSDFRenderer.drawIconCodepoint(graphics, codepoint, iconX, y + (HOTKEY_ROW_H - size) * 0.5f, size,
                alphaColor(color, renderAlpha));
    }

    private void drawSmallSwitch(GuiGraphics graphics, int x, int y, boolean enabled) {
        float value = controlAnim("small-switch/" + x + "/" + y, "value", enabled ? 1.0f : 0.0f);
        ShapeRenderer.roundRect(graphics, x, y, HOTKEY_SWITCH_W, HOTKEY_SWITCH_H, HOTKEY_SWITCH_H / 2,
                lerpColor(themeColor(0xFFE1E4EE, TRACK), ACCENT, value));
        ShapeRenderer.roundRect(graphics, Math.round(x + 2 + (HOTKEY_SWITCH_W - 14) * value), y + 2, 10, 10, 5, themeColor(0xFFFFFFFF, TEXT));
    }

    private HotkeyBinding addHotkeyBinding(String id) {
        HotkeyControlState state = hotkeyStates.computeIfAbsent(id, key -> new HotkeyControlState());
        HotkeyMeta meta = hotkeyMetas.getOrDefault(id, HotkeyMeta.EMPTY);
        HotkeyBinding binding = new HotkeyBinding(state.nextBindingId++);
        animationSystem.controlAnimations.put(animationKey("hotkey-binding/" + id + "/" + binding.id, "reveal"), 0.0f);
        binding.valueLabel = meta.valueLabel == null || meta.valueLabel.isEmpty() ? "-" : meta.valueLabel;
        binding.boolValue = meta.boolValue;
        binding.selectedValue = meta.selectedValue;
        binding.options = meta.options == null ? null : meta.options.clone();
        binding.multiMask = meta.multiMask;
        binding.sliderValue = meta.sliderValue;
        binding.sliderFormatter = meta.sliderFormatter;
        binding.sliderParser = meta.sliderParser;
        binding.colorValue = meta.colorValue;
        state.bindings.add(binding);
        return binding;
    }

    private void cycleHotkeyValue(String controlId, HotkeyBinding binding) {
        HotkeyMeta meta = hotkeyMetas.getOrDefault(controlId, HotkeyMeta.EMPTY);
        switch (meta.valueType) {
            case BOOL -> {
                binding.boolValue = !(binding.boolValue != null && binding.boolValue);
                binding.valueLabel = onOff(binding.boolValue);
            }
            case SELECT -> {
                binding.selectedValue = WidgetSurfaceValueTools.nextOption(binding.selectedValue, meta.options);
                binding.valueLabel = binding.selectedValue;
            }
            case MULTI_SELECT -> {
                int allMask = WidgetSurfaceValueTools.optionMask(meta.options == null ? 0 : meta.options.length);
                binding.multiMask = WidgetSurfaceValueTools.nextMultiMask(binding.multiMask, allMask);
                binding.valueLabel = WidgetSurfaceValueTools.multiSelectSummary(binding.multiMask, meta.options == null ? new String[0] : meta.options, allMask);
            }
            case SLIDER -> {
                binding.sliderValue = clamp01(binding.sliderValue + 0.1f);
                binding.valueLabel = hotkeySliderValueLabel(controlId, binding);
            }
            case COLOR -> {
                binding.colorValue = WidgetSurfaceValueTools.nextHotkeyColor(binding.colorValue);
                binding.valueLabel = colorHex(binding.colorValue);
            }
            case LABEL -> {
            }
        }
    }

    private HotkeyBinding findHotkeyBinding(String controlId, int bindingId) {
        if (controlId == null || bindingId < 0) {
            return null;
        }
        HotkeyControlState state = hotkeyStates.get(controlId);
        if (state == null) {
            return null;
        }
        for (HotkeyBinding binding : state.bindings) {
            if (binding.id == bindingId) {
                return binding;
            }
        }
        return null;
    }

    private void deleteHotkeyBinding(String controlId, int bindingId) {
        HotkeyControlState state = hotkeyStates.get(controlId);
        if (state != null) {
            for (HotkeyBinding binding : state.bindings) {
                if (binding.id == bindingId) {
                    clearToggleRestoreValue(controlId, binding);
                    binding.deleting = true;
                    break;
                }
            }
        }
        if (controlId != null && controlId.equals(popupSystem.openHotkeyDetailControlId) && bindingId == popupSystem.openHotkeyDetailBindingId) {
            clearHotkeyDetail();
        }
        if (isListeningHotkey(controlId, bindingId)) {
            clearListeningHotkey();
        }
    }

    private void resetHotkeys(String id) {
        HotkeyControlState state = hotkeyStates.computeIfAbsent(id, key -> new HotkeyControlState());
        for (HotkeyBinding binding : state.bindings) {
            clearToggleRestoreValue(id, binding);
            binding.deleting = true;
        }
        pendingHotkeyBoolValues.remove(id);
        pendingHotkeySelectValues.remove(id);
        pendingHotkeyMultiSelectMasks.remove(id);
        pendingHotkeySliderValues.remove(id);
        pendingHotkeyColorValues.remove(id);
        holdRestoreBoolValues.remove(id);
        holdRestoreSelectValues.remove(id);
        holdRestoreMultiSelectMasks.remove(id);
        holdRestoreSliderValues.remove(id);
        holdRestoreColorValues.remove(id);
        clearHotkeyDetail();
        clearListeningHotkey();
    }

    private boolean isListeningHotkey(String controlId, int bindingId) {
        return controlId != null
                && controlId.equals(listeningHotkeyControlId)
                && bindingId == listeningHotkeyBindingId;
    }

    private void closeHotkeyPopups() {
        popupSystem.openHotkeyId = null;
        clearHotkeyDetail();
        closeHotkeyPopupVisual();
        popupSystem.pendingHotkeyPopup = null;
        closeHotkeyValuePopups();
        clearListeningHotkey();
        cancelHotkeySliderEdit();
    }

    private void clearHotkeyDetail() {
        popupSystem.openHotkeyDetailControlId = null;
        popupSystem.openHotkeyDetailBindingId = -1;
        closeHotkeyDetailPopupVisual();
        popupSystem.pendingHotkeyDetailPopup = null;
        closeHotkeyValuePopups();
        cancelHotkeySliderEdit();
    }

    private void closeHotkeyValuePopups() {
        popupSystem.openHotkeyValueSelectKey = null;
        popupSystem.openHotkeyValueMultiKey = null;
        popupSystem.openHotkeyValueColorKey = null;
        closeHotkeyValueSelectPopupVisual();
        popupSystem.pendingHotkeyValueSelectPopup = null;
        closeHotkeyValueMultiPopupVisual();
        popupSystem.pendingHotkeyValueMultiPopup = null;
        closeHotkeyValueColorPopupVisual();
        popupSystem.pendingHotkeyValueColorPopup = null;
    }

    private void clearListeningHotkey() {
        listeningHotkeyControlId = null;
        listeningHotkeyBindingId = -1;
        updateGameMovementInputBlock();
    }

    private void updateGameMovementInputBlock() {
        blocksGameMovementInput = editingSliderId != null
                || editingHotkeySliderControlId != null
                || focusSystem.textInputId != null
                || listeningHotkeyControlId != null;
    }

    private HotkeyAnchor hotkeyAnchorAtClick() {
        return new HotkeyAnchor((int) Math.round(clickX) + 6, (int) Math.round(clickY) - 10);
    }

    private static int hotkeyPopupHeight(HotkeyControlState state) {
        return HOTKEY_POPUP_PAD * 2 + HOTKEY_ROW_H * (state.bindings.size() + 3) + HOTKEY_DIVIDER_GAP;
    }

    private static int hotkeyDetailHeight() {
        return HOTKEY_POPUP_PAD * 2 + HOTKEY_ROW_H * 5 + HOTKEY_DIVIDER_GAP;
    }


    private static String onOff(boolean value) {
        return value ? "On" : "Off";
    }

    private static void notifyToggle(String label, boolean value) {
        if (label == null || label.isBlank()
                || "Notifications".equals(label)
                || !com.xerion.forge.config.VisualSettings.notificationsModuleToggles()) {
            return;
        }
        NotificationCenter.module(label, value);
    }

    private void drawGearButton(GuiGraphics graphics, String id, int x, int y, PopupContent content) {
        String gearId = id + "/gear";
        boolean open = gearId.equals(popupSystem.openSettingsId);
        boolean hovered = hovered(x, y, GEAR_BUTTON_SIZE, GEAR_BUTTON_SIZE);
        if (button(gearId, x, y, GEAR_BUTTON_SIZE, GEAR_BUTTON_SIZE)) {
            boolean openNow = !open;
            popupSystem.openSettingsId = openNow ? gearId : null;
            if (openNow && popupSystem.openSettingsPopup != null && !gearId.equals(popupSystem.openSettingsPopup.id)) {
                closeSettingsPopupVisual();
            }
            popupSystem.openColorId = null;
            closeColorPopupVisual();
            popupSystem.openSelectId = null;
            closeSelectPopupVisual();
            popupSystem.openMultiSelectId = null;
            closeMultiSelectPopupVisual();
            closeHotkeyPopups();
            clickConsumed = true;
            open = openNow;
        }
        float focus = controlAnim(gearId, "focus", open ? 1.0f : hovered ? 0.58f : 0.0f);
        GearButton.render(graphics, x, y, GEAR_BUTTON_SIZE, open,
                lerpColor(themeColor(0xFFF7F8FC, FIELD), ACCENT_SOFT, focus),
                lerpColor(BORDER, ACCENT, focus),
                alphaColor(lerpColor(MUTED, ACCENT_DARK, focus), renderAlpha),
                focus);

        if (open && content != null) {
            int popupX = x - SETTINGS_POPUP_W - 6;
            int popupY = y;
            Frame frame = measureSettingsFrame(gearId, popupX, popupY, SETTINGS_POPUP_W);
            content.render(frame);
            popupSystem.pendingSettingsPopup = new SettingsPopup(gearId, popupX, popupY, SETTINGS_POPUP_W, frame, content);
        }
    }

    private Frame beginSettingsFrame(GuiGraphics graphics, String scope, int x, int y, int w) {
        UiLayout layout = new UiLayout(x, y, x + SETTINGS_POPUP_PAD_X, y + SETTINGS_POPUP_PAD_Y, y + SETTINGS_POPUP_PAD_Y, w - SETTINGS_POPUP_PAD_X * 2);
        int h = popupSystem.openSettingsPopup != null && scope.equals(popupSystem.openSettingsPopup.id)
                ? currentPopupHeight(PopupGeometry.settingsPopupId(popupSystem.openSettingsPopup.id),
                settingsPopupHeight(popupSystem.openSettingsPopup.frame.cardHeight()))
                : Integer.MAX_VALUE / 4;
        return new Frame(graphics, scope, HotkeyTextFormatter.groupFromId(scope), layout, true, x, y, w, h);
    }

    private Frame measureSettingsFrame(String scope, int x, int y, int w) {
        UiLayout layout = new UiLayout(x, y, x + SETTINGS_POPUP_PAD_X, y + SETTINGS_POPUP_PAD_Y, y + SETTINGS_POPUP_PAD_Y, w - SETTINGS_POPUP_PAD_X * 2);
        return new Frame(null, scope, HotkeyTextFormatter.groupFromId(scope), layout, false, x, y, w, Integer.MAX_VALUE / 4);
    }

    private void drawCheckbox(GuiGraphics graphics, int x, int y, boolean checked) {
        drawCheckbox(graphics, x, y, checked, checked ? 1.0f : 0.0f);
    }

    private void drawCheckbox(GuiGraphics graphics, int x, int y, boolean checked, float progress) {
        Checkbox.render(graphics, x, y, checked, progress, themeColor(0xFFFFFFFF, FIELD), ACCENT, BORDER, renderAlpha);
    }

    private static void dividerLine(GuiGraphics graphics, int x, int y, int w) {
        graphics.hLine(x, x + w, y, alphaColor(BORDER, renderAlpha));
    }

    private static void text(GuiGraphics graphics, String value, int x, int y, int color) {
        TextPainter.draw(graphics, value, x, y, color, renderAlpha);
    }

    private static void drawClippedText(GuiGraphics graphics, String value, int x, int y, int maxWidth, int color) {
        drawClippedText(graphics, value, x, y, maxWidth, FONT_SIZE, color);
    }

    private static void drawClippedText(GuiGraphics graphics, String value, int x, int y, int maxWidth,
                                        float size, int color) {
        TextPainter.drawClipped(graphics, value, x, y, maxWidth, size, color, renderAlpha);
    }

    private static int textWidth(String value) {
        return TextPainter.width(value);
    }

    private static int textWidth(String value, float size) {
        return TextPainter.width(value, size);
    }

    private void drawInputClippedText(GuiGraphics graphics, String value, int x, int y,
                                      int maxWidth, int color) {
        drawInputClippedText(graphics, value, x, y, maxWidth, color, false);
    }

    private void drawInputClippedText(GuiGraphics graphics, String value, int x, int y,
                                      int maxWidth, int color, boolean msdfText) {
        if (msdfText) {
            TextPainter.drawMsdfClipped(graphics, value, x, y, maxWidth,
                    FONT_SIZE, color, renderAlpha);
            return;
        }
        TextPainter.drawUnicodeClipped(graphics, value, x, y, maxWidth,
                FONT_SIZE, color, renderAlpha);
    }

    private static int inputTextWidth(String value) {
        return inputTextWidth(value, false);
    }

    private static int inputTextWidth(String value, boolean msdfText) {
        if (msdfText) {
            return TextPainter.msdfWidth(value, FONT_SIZE);
        }
        return TextPainter.unicodeWidth(value, FONT_SIZE);
    }

    private static int withOpaqueAlpha(int color) {
        return 0xFF000000 | (color & 0x00FFFFFF);
    }

    private static int composeColor(int alpha, int red, int green, int blue) {
        return ((alpha & 0xFF) << 24)
                | ((red & 0xFF) << 16)
                | ((green & 0xFF) << 8)
                | (blue & 0xFF);
    }

    private float controlAnim(String id, String channel, float target) {
        return controlAnim(id, channel, target, UiAnimationTimings.CONTROL);
    }

    private float controlAnim(String id, String channel, float target, float speed) {
        String key = animationKey(id, channel);
        float current = animationSystem.controlAnimations.getOrDefault(key, target);
        float next = AnimationClock.lerp(current, target, speed);
        if (Math.abs(next - target) < 0.001f) {
            next = target;
        }
        animationSystem.controlAnimations.put(key, next);
        return next;
    }

    private void primeControlAnim(String id, String channel, float value) {
        animationSystem.controlAnimations.putIfAbsent(animationKey(id, channel), value);
    }

    private float conditionalVisibleProgress(String id, boolean visible) {
        String key = animationKey(id, "visible");
        float target = visible ? 1.0f : 0.0f;
        float current = animationSystem.controlAnimations.getOrDefault(key, visible ? 1.0f : 0.0f);
        float next = AnimationClock.lerp(current, target, UiAnimationTimings.CONTROL);
        if (Math.abs(next - target) < 0.001f) {
            next = target;
        }
        animationSystem.controlAnimations.put(key, next);
        return clamp01(next);
    }

    private float currentConditionalVisibleProgress(String id, boolean visible) {
        String key = animationKey(id, "visible");
        return clamp01(animationSystem.controlAnimations.getOrDefault(key, visible ? 1.0f : 0.0f));
    }

    private ControlVisibilityTransition controlVisibilityTransition(String id, boolean visible, int height) {
        ControlAnimationState state = animationSystem.controlAnimationStates.computeIfAbsent(id, key -> new ControlAnimationState());
        if (state.height != height) {
            state.height = height;
        }
        if (!state.initialized) {
            state.visibleProgress = visible && !conditionalAnimating ? 1.0f : 0.0f;
            state.alpha = state.visibleProgress;
            state.initialized = true;
        }
        float target = visible ? 1.0f : 0.0f;
        state.visibleProgress = AnimationClock.lerp(state.visibleProgress, target, UiAnimationTimings.CONTROL);
        state.alpha = AnimationClock.lerp(state.alpha, target, UiAnimationTimings.CONTROL);
        if (Math.abs(state.visibleProgress - target) < 0.001f) {
            state.visibleProgress = target;
        }
        if (Math.abs(state.alpha - target) < 0.001f) {
            state.alpha = target;
        }
        int distance = Math.max(1, state.height);
        int offsetY = visible
                ? Math.round(distance * (1.0f - state.alpha))
                : -Math.round(distance * (1.0f - state.alpha));
        int reservedHeight = Math.round(height * state.visibleProgress);
        return new ControlVisibilityTransition(reservedHeight, offsetY, clamp01(state.alpha));
    }

    private float appearAnim(String id, String channel) {
        String key = animationKey(id, channel);
        float current = animationSystem.controlAnimations.getOrDefault(key, 0.0f);
        float next = AnimationClock.lerp(current, 1.0f, UiAnimationTimings.CONTROL);
        if (Math.abs(1.0f - next) < 0.001f) {
            next = 1.0f;
        }
        animationSystem.controlAnimations.put(key, next);
        return next;
    }

    private float disappearAnim(String id, String channel) {
        String key = animationKey(id, channel);
        float current = animationSystem.controlAnimations.getOrDefault(key, 1.0f);
        float next = AnimationClock.lerp(current, 0.0f, UiAnimationTimings.CONTROL);
        if (next < 0.01f) {
            next = 0.0f;
        }
        animationSystem.controlAnimations.put(key, next);
        return next;
    }

    private int animatedRowY(String id, int renderTargetY, int stableTargetY, int originY, int height) {
        String key = animationKey(id, "rowY");
        float targetStableY = stableTargetY - originY;
        float startStableY = targetStableY + Math.max(1, height);
        float current = animationSystem.controlAnimations.getOrDefault(key, startStableY);
        float next = AnimationClock.lerp(current, targetStableY, UiAnimationTimings.CONTROL);
        if (Math.abs(next - targetStableY) < 0.5f) {
            next = targetStableY;
        }
        animationSystem.controlAnimations.put(key, next);
        return renderTargetY + Math.round(next - targetStableY);
    }

    private ControlTransition controlTransition(String id, int targetY) {
        return controlTransition(id, targetY, 0);
    }

    private ControlTransition controlTransition(String id, int targetY, int height) {
        if (conditionalDepth <= 0) {
            return new ControlTransition(targetY, 1.0f);
        }
        int safeHeight = Math.max(1, height);
        ControlVisibilityTransition visibility = controlVisibilityTransition(id, conditionalVisible, safeHeight);
        return new ControlTransition(targetY + visibility.offsetY, visibility.alpha);
    }

    private float beginControlAlpha(ControlTransition transition) {
        float previousAlpha = renderAlpha;
        renderAlpha = previousAlpha * transition.alpha * conditionalAlpha;
        ShapeRenderer.setGlobalAlpha(renderAlpha);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, renderAlpha);
        return previousAlpha;
    }

    private void endControlAlpha(float previousAlpha) {
        renderAlpha = previousAlpha;
        ShapeRenderer.setGlobalAlpha(previousAlpha);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, previousAlpha);
    }

    private ConditionalRenderState pushConditional(boolean visible, boolean animating, boolean interactionDisabled) {
        ConditionalRenderState previous = new ConditionalRenderState(conditionalAlpha, conditionalVisible,
                conditionalAnimating, conditionalDepth, conditionalInteractionDisabled);
        conditionalStack.push(previous);
        conditionalVisible = conditionalVisible && visible;
        conditionalAnimating = conditionalAnimating || animating;
        conditionalDepth++;
        conditionalInteractionDisabled = conditionalInteractionDisabled || interactionDisabled;
        return previous;
    }

    private void popConditional(ConditionalRenderState previous) {
        if (!conditionalStack.isEmpty()) {
            conditionalStack.pop();
        }
        conditionalAlpha = previous.alpha;
        conditionalVisible = previous.visible;
        conditionalAnimating = previous.animating;
        conditionalDepth = previous.depth;
        conditionalInteractionDisabled = previous.interactionDisabled;
    }

    private boolean pushRenderClip(int x, int y, int w, int h) {
        renderClipStack.push(new RenderClipState(clipRender, renderClipX, renderClipY, renderClipW, renderClipH));
        if (!clipRender) {
            setRenderClip(x, y, w, h);
        } else {
            int left = Math.max(renderClipX, x);
            int top = Math.max(renderClipY, y);
            int right = Math.min(renderClipX + renderClipW, x + w);
            int bottom = Math.min(renderClipY + renderClipH, y + h);
            setRenderClip(left, top, Math.max(0, right - left), Math.max(0, bottom - top));
        }
        applyRenderClip();
        return true;
    }

    private void popRenderClip(boolean pushed) {
        if (!pushed || renderClipStack.isEmpty()) {
            return;
        }
        RenderClipState previous = renderClipStack.pop();
        clipRender = previous.enabled;
        renderClipX = previous.x;
        renderClipY = previous.y;
        renderClipW = previous.w;
        renderClipH = previous.h;
        applyRenderClip();
    }

    private void applyRenderClip() {
        if (!clipRender || renderClipW <= 0 || renderClipH <= 0) {
            RenderSystem.disableScissor();
            return;
        }
        Window window = Minecraft.getInstance().getWindow();
        double originX = window.getWidth() * 0.5;
        double originY = window.getHeight() * 0.5;
        double logicalX = renderClipX + renderOffsetX;
        double logicalY = renderClipY + renderOffsetY;
        int x = (int) Math.floor(originX + (logicalX - originX) * renderScale);
        int y = (int) Math.floor(originY + (logicalY - originY) * renderScale);
        int w = Math.max(1, (int) Math.ceil(renderClipW * renderScale));
        int h = Math.max(1, (int) Math.ceil(renderClipH * renderScale));
        RenderSystem.enableScissor(x, window.getHeight() - y - h, w, h);
    }

    private void clearHotkeyAnimation(String popupId, HotkeyBinding binding) {
        animationSystem.controlAnimations.remove(animationKey("hotkey-binding/" + popupId + "/" + binding.id, "reveal"));
    }

    private static String animationKey(String id, String channel) {
        return id + "/" + channel;
    }

    private PopupAnimation beginPopupAnimation(GuiGraphics graphics, String id, boolean slideFromTop) {
        animationSystem.activePopupAnimations.add(id);
        animationSystem.closingPopupAnimations.remove(id);
        float current = animationSystem.popupAnimations.getOrDefault(id, 0.0f);
        float next = AnimationClock.lerp(current, 1.0f, UiAnimationTimings.POPUP_OPEN);
        if (Math.abs(1.0f - next) < 0.001f) {
            next = 1.0f;
        }
        animationSystem.popupAnimations.put(id, next);
        float alpha = clamp01(next);
        float yOffset = slideFromTop ? -6.0f * (1.0f - alpha) : 0.0f;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0f, yOffset, 0.0f);
        renderAlpha = alpha;
        ShapeRenderer.setGlobalAlpha(alpha);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        return new PopupAnimation(id, alpha);
    }

    private boolean shouldDropClosingPopup(String id) {
        if (!animationSystem.activePopupAnimations.contains(id)) {
            return false;
        }
        animationSystem.closingPopupAnimations.remove(id);
        return true;
    }

    private PopupAnimation beginClosingPopupAnimation(GuiGraphics graphics, String id, boolean slideUp) {
        float current = animationSystem.closingPopupAnimations.getOrDefault(id, 1.0f);
        float next = AnimationClock.lerp(current, 0.0f, UiAnimationTimings.POPUP_CLOSE);
        if (next < 0.01f) {
            next = 0.0f;
        }
        animationSystem.closingPopupAnimations.put(id, next);
        float alpha = clamp01(next);
        float yOffset = slideUp ? -6.0f * (1.0f - alpha) : 0.0f;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0f, yOffset, 0.0f);
        renderAlpha = alpha;
        ShapeRenderer.setGlobalAlpha(alpha);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        return new PopupAnimation(id, alpha);
    }

    private void endPopupAnimation(GuiGraphics graphics) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        ShapeRenderer.resetGlobalAlpha();
        renderAlpha = 1.0f;
        graphics.pose().popPose();
    }

    private void startClosingPopupAnimation(String id) {
        if (id != null) {
            float current = animationSystem.popupAnimations.getOrDefault(id, 1.0f);
            animationSystem.closingPopupAnimations.put(id, current);
            animationSystem.popupAnimations.remove(id);
        }
    }

    private void clearPopupAnimation(String id) {
        if (id != null) {
            animationSystem.popupAnimations.remove(id);
            animationSystem.closingPopupAnimations.remove(id);
            animationSystem.popupHeightAnimations.remove(id);
        }
    }

    private int animatedPopupHeight(String id, int targetH) {
        float current = animationSystem.popupHeightAnimations.getOrDefault(id, (float) targetH);
        float next = AnimationClock.lerp(current, targetH, UiAnimationTimings.POPUP_SIZE);
        if (Math.abs(next - targetH) < 0.5f) {
            next = targetH;
        }
        animationSystem.popupHeightAnimations.put(id, next);
        return Math.max(1, Math.round(next));
    }

    private int currentPopupHeight(String id, int targetH) {
        return Math.max(1, Math.round(animationSystem.popupHeightAnimations.getOrDefault(id, (float) targetH)));
    }

    private void closeSelectPopupVisual() {
        if (popupSystem.openSelectPopup != null) {
            popupSystem.closingSelectPopup = popupSystem.openSelectPopup;
            startClosingPopupAnimation(PopupGeometry.selectPopupId(popupSystem.openSelectPopup.id));
        }
        popupSystem.openSelectPopup = null;
    }

    private void closeMultiSelectPopupVisual() {
        if (popupSystem.openMultiSelectPopup != null) {
            popupSystem.closingMultiSelectPopup = popupSystem.openMultiSelectPopup;
            startClosingPopupAnimation(PopupGeometry.multiSelectPopupId(popupSystem.openMultiSelectPopup.id));
        }
        popupSystem.openMultiSelectPopup = null;
    }

    private void closeSettingsPopupVisual() {
        if (popupSystem.openSettingsPopup != null) {
            popupSystem.closingSettingsPopup = popupSystem.openSettingsPopup;
            startClosingPopupAnimation(PopupGeometry.settingsPopupId(popupSystem.openSettingsPopup.id));
        }
        popupSystem.openSettingsPopup = null;
    }

    private void closeColorPopupVisual() {
        if (popupSystem.openColorPopup != null) {
            popupSystem.closingColorPopup = popupSystem.openColorPopup;
            startClosingPopupAnimation(PopupGeometry.colorPopupId(popupSystem.openColorPopup.id));
        }
        popupSystem.openColorPopup = null;
    }

    private void closeHotkeyPopupVisual() {
        if (popupSystem.openHotkeyPopup != null) {
            popupSystem.closingHotkeyPopup = popupSystem.openHotkeyPopup;
            startClosingPopupAnimation(PopupGeometry.hotkeyPopupId(popupSystem.openHotkeyPopup.id));
        }
        popupSystem.openHotkeyPopup = null;
    }

    private void closeHotkeyDetailPopupVisual() {
        if (popupSystem.openHotkeyDetailPopup != null) {
            popupSystem.closingHotkeyDetailPopup = popupSystem.openHotkeyDetailPopup;
            startClosingPopupAnimation(PopupGeometry.hotkeyDetailPopupId(popupSystem.openHotkeyDetailPopup.controlId, popupSystem.openHotkeyDetailPopup.bindingId));
        }
        popupSystem.openHotkeyDetailPopup = null;
    }

    private void closeHotkeyValueSelectPopupVisual() {
        if (popupSystem.openHotkeyValueSelectPopup != null) {
            popupSystem.closingHotkeyValueSelectPopup = popupSystem.openHotkeyValueSelectPopup;
            startClosingPopupAnimation(PopupGeometry.hotkeyValueSelectPopupId(popupSystem.openHotkeyValueSelectPopup.controlId, popupSystem.openHotkeyValueSelectPopup.bindingId));
        }
        popupSystem.openHotkeyValueSelectPopup = null;
    }

    private void closeHotkeyValueMultiPopupVisual() {
        if (popupSystem.openHotkeyValueMultiPopup != null) {
            popupSystem.closingHotkeyValueMultiPopup = popupSystem.openHotkeyValueMultiPopup;
            startClosingPopupAnimation(PopupGeometry.hotkeyValueMultiPopupId(popupSystem.openHotkeyValueMultiPopup.controlId, popupSystem.openHotkeyValueMultiPopup.bindingId));
        }
        popupSystem.openHotkeyValueMultiPopup = null;
    }

    private void closeHotkeyValueColorPopupVisual() {
        if (popupSystem.openHotkeyValueColorPopup != null) {
            popupSystem.closingHotkeyValueColorPopup = popupSystem.openHotkeyValueColorPopup;
            startClosingPopupAnimation(PopupGeometry.hotkeyValueColorPopupId(popupSystem.openHotkeyValueColorPopup.controlId, popupSystem.openHotkeyValueColorPopup.bindingId));
        }
        popupSystem.openHotkeyValueColorPopup = null;
    }

    private static int alphaColor(int color, float alpha) {
        return UiColor.alpha(color, alpha);
    }

    private static void refreshTheme() {
        SURFACE = UiPalette.surface();
        FIELD = UiPalette.field();
        TRACK = UiPalette.track();
        BORDER = UiPalette.border();
        TEXT = UiPalette.text();
        MUTED = UiPalette.muted();
        SUBTLE = UiPalette.subtle();
        ACCENT = UiPalette.accent();
        ACCENT_DARK = UiPalette.accentDark();
        ACCENT_SOFT = UiPalette.accentSoft();
        DANGER = UiPalette.danger();
    }

    static void setRenderAlpha(float alpha) {
        renderAlpha = clamp01(alpha);
    }

    private static int lerpColor(int from, int to, float t) {
        return UiColor.lerp(from, to, t);
    }

    private static int themeColor(int light, int dark) {
        return UiPalette.dark() ? dark : light;
    }

    private static String colorHex(int color) {
        return String.format("#%08X", color);
    }

    private static String controlId(String scope, String kind, String label, int originX, int originY, int x, int y) {
        return scope + "/" + kind + "/" + label + "/" + (x - originX) + "/" + (y - originY);
    }





    private static String normalizedHotkeyFeature(String controlId, HotkeyMeta meta) {
        String group = meta == null || meta.group == null || meta.group.isBlank()
                ? HotkeyTextFormatter.groupFromId(controlId)
                : meta.group;
        String label = meta == null ? "" : HotkeyTextFormatter.displayLabel(controlId, meta.label);
        return HotkeyTextFormatter.cleanGroup(group).toLowerCase(java.util.Locale.ROOT)
                + "/" + label.toLowerCase(java.util.Locale.ROOT);
    }

    private static HotkeyMeta fallbackHotkeyMeta(String controlId) {
        String group = HotkeyTextFormatter.groupFromId(controlId);
        String label = HotkeyTextFormatter.fallbackLabel(controlId);
        if (controlId != null && controlId.startsWith("module/")) {
            return new HotkeyMeta(group, label, "Module", "-", null, HotkeyValueType.BOOL,
                    "-", null, 0, 0.0f, 0, null, null, null);
        }
        return new HotkeyMeta(group, label, "Hotkey", "-", null, HotkeyValueType.LABEL,
                "-", null, 0, 0.0f, 0, null, null, null);
    }




    private static HotkeyValueType parseHotkeyValueType(String value, HotkeyValueType fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return HotkeyValueType.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }













    private boolean insideInteractionClip(double px, double py) {
        return !clipInteraction || PopupGeometry.contains(px, py, clipX, clipY, clipW, clipH);
    }

    private boolean insideOpenSettingsPopup(double px, double py) {
        if (popupSystem.openSettingsPopup == null) {
            return false;
        }
        int targetH = settingsPopupHeight(popupSystem.openSettingsPopup.frame.cardHeight());
        int h = currentPopupHeight(PopupGeometry.settingsPopupId(popupSystem.openSettingsPopup.id), targetH);
        return PopupGeometry.contains(px, py, popupSystem.openSettingsPopup.x, popupSystem.openSettingsPopup.y, popupSystem.openSettingsPopup.w, h);
    }

    private boolean insideOpenColorPopup(double px, double py) {
        if (popupSystem.openColorPopup == null) {
            return false;
        }
        return PopupGeometry.contains(px, py, popupSystem.openColorPopup.x, popupSystem.openColorPopup.y, popupSystem.openColorPopup.w,
                colorPopupHeight(popupSystem.openColorPopup.activeAlpha));
    }

    private boolean insideOpenSelectPopup(double px, double py) {
        if (popupSystem.openSelectPopup == null) {
            return false;
        }
        return PopupGeometry.contains(px, py, popupSystem.openSelectPopup.x, popupSystem.openSelectPopup.y, popupSystem.openSelectPopup.w,
                popupHeight(popupSystem.openSelectPopup.options.length));
    }

    private boolean insideOpenMultiSelectPopup(double px, double py) {
        if (popupSystem.openMultiSelectPopup == null) {
            return false;
        }
        return PopupGeometry.contains(px, py, popupSystem.openMultiSelectPopup.x, popupSystem.openMultiSelectPopup.y, popupSystem.openMultiSelectPopup.w,
                popupHeight(popupSystem.openMultiSelectPopup.options.length));
    }

    private boolean insideOpenHotkeyPopup(double px, double py) {
        if (popupSystem.openHotkeyPopup == null) {
            return false;
        }
        HotkeyControlState state = hotkeyStates.computeIfAbsent(popupSystem.openHotkeyPopup.id, key -> new HotkeyControlState());
        return PopupGeometry.contains(px, py, popupSystem.openHotkeyPopup.x, popupSystem.openHotkeyPopup.y, HOTKEY_POPUP_W, hotkeyPopupHeight(state));
    }

    private boolean insideOpenHotkeyDetailPopup(double px, double py) {
        if (popupSystem.openHotkeyDetailPopup == null) {
            return false;
        }
        return PopupGeometry.contains(px, py, popupSystem.openHotkeyDetailPopup.x, popupSystem.openHotkeyDetailPopup.y, HOTKEY_DETAIL_W, hotkeyDetailHeight());
    }

    private boolean hasOpenHotkeyValuePopup() {
        return popupSystem.openHotkeyValueSelectPopup != null || popupSystem.openHotkeyValueMultiPopup != null || popupSystem.openHotkeyValueColorPopup != null;
    }

    private boolean insideOpenHotkeyValuePopup(double px, double py) {
        if (popupSystem.openHotkeyValueSelectPopup != null) {
            return PopupGeometry.contains(px, py, popupSystem.openHotkeyValueSelectPopup.x, popupSystem.openHotkeyValueSelectPopup.y,
                    popupSystem.openHotkeyValueSelectPopup.w, popupHeight(popupSystem.openHotkeyValueSelectPopup.options.length));
        }
        if (popupSystem.openHotkeyValueMultiPopup != null) {
            return PopupGeometry.contains(px, py, popupSystem.openHotkeyValueMultiPopup.x, popupSystem.openHotkeyValueMultiPopup.y,
                    popupSystem.openHotkeyValueMultiPopup.w, popupHeight(popupSystem.openHotkeyValueMultiPopup.options.length));
        }
        if (popupSystem.openHotkeyValueColorPopup != null) {
            return PopupGeometry.contains(px, py, popupSystem.openHotkeyValueColorPopup.x, popupSystem.openHotkeyValueColorPopup.y,
                    popupSystem.openHotkeyValueColorPopup.w, colorPopupHeight());
        }
        return false;
    }

    private boolean insideOpenMenuPopup(double px, double py) {
        if (popupSystem.openMenuPopup == null) {
            return false;
        }
        return PopupGeometry.contains(px, py, popupSystem.openMenuPopup.x, popupSystem.openMenuPopup.y, HOTKEY_POPUP_W, menuPopupHeight(popupSystem.openMenuPopup));
    }

    private static int menuPopupHeight(MenuPopup popup) {
        boolean divider = false;
        boolean seenNormal = false;
        for (MenuItem item : popup.items) {
            if (item.danger && seenNormal) {
                divider = true;
                break;
            }
            if (!item.danger) {
                seenNormal = true;
            }
        }
        return HOTKEY_POPUP_PAD * 2 + HOTKEY_ROW_H * popup.items.size() + (divider ? HOTKEY_DIVIDER_GAP : 0);
    }

    private void closeMenuPopupVisual() {
        if (popupSystem.openMenuPopup != null) {
            popupSystem.closingMenuPopup = popupSystem.openMenuPopup;
            startClosingPopupAnimation(PopupGeometry.menuPopupId(popupSystem.openMenuPopup.id));
        }
        popupSystem.openMenuPopup = null;
    }

    @FunctionalInterface
    interface PopupContent {
        void render(Frame gui);
    }

    @FunctionalInterface
    interface MenuContent {
        void build(MenuBuilder menu);
    }

}
