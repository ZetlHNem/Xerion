package com.xerion.common.visual;

public enum ChamsMaterial {
    SHADED("Shaded"),
    SOLID("Solid"),
    GLOW("Glow"),
    IRIDESCENT("Iridescent"),
    WATER_FLOW("Water Flow"),
    GLOSSY("Glossy");

    private final String label;

    ChamsMaterial(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static ChamsMaterial fromLabel(String label) {
        if ("Glow Outline".equals(label)) {
            return GLOW;
        }
        for (ChamsMaterial material : values()) {
            if (material.label.equals(label)) {
                return material;
            }
        }
        return SHADED;
    }
}
