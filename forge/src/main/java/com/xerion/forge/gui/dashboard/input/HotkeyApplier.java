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

import java.util.function.Consumer;

interface HotkeyApplier {
    default void applyBoolean(boolean value) {
    }

    default void applySelect(String value) {
    }

    default void applyMultiSelect(int value) {
    }

    default void applySlider(float value) {
    }

    default void applyColor(int value) {
    }

    static HotkeyApplier forBoolean(Consumer<Boolean> consumer) {
        if (consumer == null) {
            return null;
        }
        return new HotkeyApplier() {
            @Override
            public void applyBoolean(boolean value) {
                consumer.accept(value);
            }
        };
    }

    static HotkeyApplier forSelect(Consumer<String> consumer) {
        if (consumer == null) {
            return null;
        }
        return new HotkeyApplier() {
            @Override
            public void applySelect(String value) {
                consumer.accept(value);
            }
        };
    }

    static HotkeyApplier forMultiSelect(Consumer<Integer> consumer) {
        if (consumer == null) {
            return null;
        }
        return new HotkeyApplier() {
            @Override
            public void applyMultiSelect(int value) {
                consumer.accept(value);
            }
        };
    }

    static HotkeyApplier forSlider(Consumer<Float> consumer) {
        if (consumer == null) {
            return null;
        }
        return new HotkeyApplier() {
            @Override
            public void applySlider(float value) {
                consumer.accept(value);
            }
        };
    }

    static HotkeyApplier forColor(Consumer<Integer> consumer) {
        if (consumer == null) {
            return null;
        }
        return new HotkeyApplier() {
            @Override
            public void applyColor(int value) {
                consumer.accept(value);
            }
        };
    }
}
