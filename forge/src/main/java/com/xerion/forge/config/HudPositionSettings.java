package com.xerion.forge.config;

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

final class HudPositionSettings {
    private HudPositionSettings() {
    }

    static int watermarkX() {
        return VisualSettings.watermarkX;
    }

    static int watermarkY() {
        return VisualSettings.watermarkY;
    }

    static void setWatermarkPosition(int x, int y) {
        VisualSettings.watermarkX = x;
        VisualSettings.watermarkY = y;
    }

    static int hotkeyHudX() {
        return VisualSettings.hotkeyHudX;
    }

    static int hotkeyHudY() {
        return VisualSettings.hotkeyHudY;
    }

    static void setHotkeyHudPosition(int x, int y) {
        VisualSettings.hotkeyHudX = x;
        VisualSettings.hotkeyHudY = y;
    }

    static int arrayListHudX() {
        return VisualSettings.arrayListHudX;
    }

    static int arrayListHudY() {
        return VisualSettings.arrayListHudY;
    }

    static void setArrayListHudPosition(int x, int y) {
        VisualSettings.arrayListHudX = x;
        VisualSettings.arrayListHudY = y;
    }

    static int arrayListHudRightOffset() {
        return VisualSettings.arrayListHudRightOffset;
    }

    static void setArrayListHudRightOffset(int offset) {
        VisualSettings.arrayListHudRightOffset = Math.max(0, offset);
    }

    static boolean arrayListHudRightAnchored() {
        return VisualSettings.arrayListHudRightAnchored;
    }

    static void setArrayListHudRightAnchored(boolean anchored) {
        VisualSettings.arrayListHudRightAnchored = anchored;
    }

    static int targetsHudX() {
        return VisualSettings.targetsHudX;
    }

    static int targetsHudY() {
        return VisualSettings.targetsHudY;
    }

    static void setTargetsHudPosition(int x, int y) {
        VisualSettings.targetsHudX = x;
        VisualSettings.targetsHudY = y;
    }

    static int backpackHudX() {
        return VisualSettings.backpackHudX;
    }

    static int backpackHudY() {
        return VisualSettings.backpackHudY;
    }

    static void setBackpackHudPosition(int x, int y) {
        VisualSettings.backpackHudX = x;
        VisualSettings.backpackHudY = y;
    }

    static int potionBuffHudX() {
        return VisualSettings.potionBuffHudX;
    }

    static int potionBuffHudY() {
        return VisualSettings.potionBuffHudY;
    }

    static void setPotionBuffHudPosition(int x, int y) {
        VisualSettings.potionBuffHudX = x;
        VisualSettings.potionBuffHudY = y;
    }

    static int speedHudX() {
        return VisualSettings.speedHudX;
    }

    static int speedHudY() {
        return VisualSettings.speedHudY;
    }

    static void setSpeedHudPosition(int x, int y) {
        VisualSettings.speedHudX = x;
        VisualSettings.speedHudY = y;
    }

    static int musicLyricsHudX() {
        return VisualSettings.musicLyricsHudX;
    }

    static int musicLyricsHudY() {
        return VisualSettings.musicLyricsHudY;
    }

    static void setMusicLyricsHudPosition(int x, int y) {
        VisualSettings.musicLyricsHudX = x;
        VisualSettings.musicLyricsHudY = y;
    }
}
