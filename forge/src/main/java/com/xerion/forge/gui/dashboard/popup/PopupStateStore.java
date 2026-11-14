package com.xerion.forge.gui.dashboard;

final class PopupStateStore {
    String openSelectId;
    String openMultiSelectId;
    String openSettingsId;
    String openColorId;
    String openHotkeyId;
    String openHotkeyDetailControlId;
    int openHotkeyDetailBindingId = -1;
    String openHotkeyValueSelectKey;
    String openHotkeyValueMultiKey;
    String openHotkeyValueColorKey;

    SelectPopup openSelectPopup;
    SelectPopup pendingSelectPopup;
    MultiSelectPopup openMultiSelectPopup;
    MultiSelectPopup pendingMultiSelectPopup;
    SettingsPopup openSettingsPopup;
    SettingsPopup pendingSettingsPopup;
    ColorPopup openColorPopup;
    ColorPopup pendingColorPopup;
    HotkeyPopup openHotkeyPopup;
    HotkeyPopup pendingHotkeyPopup;
    HotkeyDetailPopup openHotkeyDetailPopup;
    HotkeyDetailPopup pendingHotkeyDetailPopup;
    HotkeyValueSelectPopup openHotkeyValueSelectPopup;
    HotkeyValueSelectPopup pendingHotkeyValueSelectPopup;
    HotkeyValueMultiPopup openHotkeyValueMultiPopup;
    HotkeyValueMultiPopup pendingHotkeyValueMultiPopup;
    HotkeyValueColorPopup openHotkeyValueColorPopup;
    HotkeyValueColorPopup pendingHotkeyValueColorPopup;
    MenuPopup openMenuPopup;
    MenuPopup pendingMenuPopup;

    SelectPopup closingSelectPopup;
    MultiSelectPopup closingMultiSelectPopup;
    SettingsPopup closingSettingsPopup;
    ColorPopup closingColorPopup;
    HotkeyPopup closingHotkeyPopup;
    HotkeyDetailPopup closingHotkeyDetailPopup;
    HotkeyValueSelectPopup closingHotkeyValueSelectPopup;
    HotkeyValueMultiPopup closingHotkeyValueMultiPopup;
    HotkeyValueColorPopup closingHotkeyValueColorPopup;
    MenuPopup closingMenuPopup;

    boolean renderingPopup;
    boolean renderingColorPopup;
    boolean keepHotkeyDetailThisFrame;

    void beginFrame() {
        pendingSelectPopup = null;
        pendingMultiSelectPopup = null;
        pendingSettingsPopup = null;
        pendingColorPopup = null;
        pendingHotkeyPopup = null;
        pendingHotkeyDetailPopup = null;
        pendingHotkeyValueSelectPopup = null;
        pendingHotkeyValueMultiPopup = null;
        pendingHotkeyValueColorPopup = null;
        pendingMenuPopup = null;
        keepHotkeyDetailThisFrame = false;
    }
}
