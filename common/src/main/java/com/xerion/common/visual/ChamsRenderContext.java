package com.xerion.common.visual;

public final class ChamsRenderContext {
    private static final ThreadLocal<Boolean> PREVIEW_RENDER = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> OUTLINE_MASK_RENDER = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Integer> PREVIEW_GLOW_COLOR = new ThreadLocal<>();

    private ChamsRenderContext() {
    }

    public static void beginPreviewRender() {
        PREVIEW_RENDER.set(true);
    }

    public static void endPreviewRender() {
        PREVIEW_RENDER.set(false);
    }

    public static void beginOutlineMaskRender() {
        OUTLINE_MASK_RENDER.set(true);
    }

    public static void endOutlineMaskRender() {
        OUTLINE_MASK_RENDER.set(false);
    }

    public static boolean previewRender() {
        return PREVIEW_RENDER.get();
    }

    public static boolean outlineMaskRender() {
        return OUTLINE_MASK_RENDER.get();
    }

    public static void beginPreviewGlowRender(int color) {
        PREVIEW_GLOW_COLOR.set(color);
    }

    public static void endPreviewGlowRender() {
        PREVIEW_GLOW_COLOR.remove();
    }

    public static boolean previewGlowRender() {
        return PREVIEW_GLOW_COLOR.get() != null;
    }

    public static int previewGlowColor() {
        Integer color = PREVIEW_GLOW_COLOR.get();
        return color == null ? 0 : color;
    }
}
