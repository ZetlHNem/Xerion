package com.xerion.forge.gui.dashboard;

import java.util.HashMap;
import java.util.Map;

final class WidgetValueRegistry {
    final Map<String, Boolean> boolValues = new HashMap<>();
    final Map<String, Float> sliderValues = new HashMap<>();
    final Map<String, String> textValues = new HashMap<>();
    final Map<String, String> selectValues = new HashMap<>();
    final Map<String, Integer> multiSelectMasks = new HashMap<>();
    final Map<String, Integer> colorValues = new HashMap<>();

    void clearControlValues() {
        boolValues.clear();
        sliderValues.clear();
        textValues.clear();
        selectValues.clear();
        multiSelectMasks.clear();
        colorValues.clear();
    }
}
