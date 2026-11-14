package com.xerion.forge.gui.dashboard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class WidgetAnimationState {
    final Map<String, Float> controlAnimations = new HashMap<>();
    final Map<String, ControlAnimationState> controlAnimationStates = new HashMap<>();
    final Map<String, Float> popupAnimations = new HashMap<>();
    final Map<String, Float> closingPopupAnimations = new HashMap<>();
    final Map<String, Float> popupHeightAnimations = new HashMap<>();
    final Set<String> activePopupAnimations = new HashSet<>();

    void clear() {
        controlAnimations.clear();
        controlAnimationStates.clear();
        popupAnimations.clear();
        closingPopupAnimations.clear();
        popupHeightAnimations.clear();
        activePopupAnimations.clear();
    }
}
