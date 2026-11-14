package com.xerion.common.config;

public final class VisualDefaults {
    private VisualDefaults() {
    }

    public static int espItemColor(long item) {
        if (item == VisualEspItems.ARMOR_BAR) {
            return 0xF2559CFF;
        }
        if (item == VisualEspItems.ABSORPTION) {
            return 0xF2FFD64D;
        }
        if (item == VisualEspItems.HURT_TIME) {
            return 0xF2FF4D4D;
        }
        if (item == VisualEspItems.SNEAKING) {
            return 0xF2A7C7FF;
        }
        if (item == VisualEspItems.SPRINTING) {
            return 0xF24DFF88;
        }
        if (item == VisualEspItems.INVISIBLE) {
            return 0xF2B88CFF;
        }
        if (item == VisualEspItems.BURNING) {
            return 0xF2FF5A3D;
        }
        if (item == VisualEspItems.BLOCKING) {
            return 0xF24DD2FF;
        }
        if (item == VisualEspItems.USING_ITEM) {
            return 0xF2FFD166;
        }
        if (item == VisualEspItems.EATING) {
            return 0xF27CFF6B;
        }
        if (item == VisualEspItems.DRINKING) {
            return 0xF26EC6FF;
        }
        if (item == VisualEspItems.BOW_CHARGING) {
            return 0xF2FF9F43;
        }
        if (item == VisualEspItems.CROSSBOW_CHARGED) {
            return 0xF2D17BFF;
        }
        if (item == VisualEspItems.ELYTRA) {
            return 0xF26EDCFF;
        }
        if (item == VisualEspItems.SWIMMING) {
            return 0xF24DBBFF;
        }
        if (item == VisualEspItems.GLOWING) {
            return 0xF2FFF275;
        }
        if (item == VisualEspItems.POTION) {
            return 0xF2B967FF;
        }
        if (item == VisualEspItems.LOW_HEALTH) {
            return 0xF2FF4D4D;
        }
        if (item == VisualEspItems.BOT) {
            return 0xF2FF4DA6;
        }
        if (item == VisualEspItems.FRIEND_TEAM) {
            return 0xF24DFFB8;
        }
        if (item == VisualEspItems.TARGET) {
            return 0xF2FF5A7A;
        }
        if (item == VisualEspItems.THREAT) {
            return 0xF2FF9F43;
        }
        if (item == VisualEspItems.DISTANCE_COLOR) {
            return 0xF24DFFF0;
        }
        return 0xF2FFFFFF;
    }

    public static String visualModelMode(String mode) {
        return switch (mode) {
            case "Glow Outline" -> "Glow";
            case "Shaded", "Solid", "Glow", "Iridescent", "Water Flow", "Glossy" -> mode;
            default -> "Shaded";
        };
    }
}
