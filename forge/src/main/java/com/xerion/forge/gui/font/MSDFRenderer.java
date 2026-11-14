package com.xerion.forge.gui.font;

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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.fml.StartupMessageManager;
import net.minecraftforge.fml.loading.progress.ProgressMeter;
import org.joml.Matrix4f;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MSDFRenderer {
    private static final float DEFAULT_SIZE = 12.0f;
    private static final float SCREEN_TEXT_SCALE = 1.3f;
    private static final float SHADOW_OFFSET_RATIO = 1.0f / 12.0f;
    private static final float SHADOW_ALPHA_RATIO = 0.82f;
    private static final float WEIGHT = 0.0f;
    private static final float DISPLAY_WEIGHT = 0.008f;
    private static final float GAMMA = 1.0f;
    private static final float SHARPNESS = 0.72f;
    private static final float DISPLAY_SHARPNESS = 0.58f;

    private static ShaderInstance shader;
    private static final ResourceLocation TEXT_FONT = ResourceLocation.fromNamespaceAndPath(
            XerionForge.MOD_ID, "fonts/misans_regular.ttf");
    private static final ResourceLocation TEXT_BOLD_FONT = ResourceLocation.fromNamespaceAndPath(
            XerionForge.MOD_ID, "fonts/misans_bold.ttf");
    private static final FontFallbackSpec[] TEXT_FONT_FALLBACKS = {
            fallback("misans_latin_regular.ttf", "misans_latin", 512),
            fallback("misans_tc_regular.ttf", "misans_tc", 1024),
            fallback("misans_l3.ttf", "misans_l3", 1024),
            fallback("noto_sans_kr_regular.ttf", "noto_sans_kr", 1024),
            fallback("misans_arabic_ui_regular.ttf", "misans_arabic", 512),
            fallback("misans_devanagari_regular.ttf", "misans_devanagari", 512),
            fallback("misans_gurmukhi_regular.ttf", "misans_gurmukhi", 512),
            fallback("misans_tibetan_regular.ttf", "misans_tibetan", 512),
            fallback("misans_thai_regular.ttf", "misans_thai", 512),
            fallback("misans_lao_regular.ttf", "misans_lao", 512),
            fallback("misans_myanmar_regular.ttf", "misans_myanmar", 512),
            fallback("misans_khmer_regular.ttf", "misans_khmer", 512)
    };
    private static final ResourceLocation ICON_FONT = ResourceLocation.fromNamespaceAndPath(
            XerionForge.MOD_ID, "fonts/fontawesome_solid.ttf");
    private static TtfFontSet fontSet;
    private static TtfFontSet boldFontSet;
    private static TtfFontSet iconFont;
    private static volatile boolean fullTextAtlasReady;
    private static volatile boolean iconAtlasReady;
    private static volatile boolean preloadFailed;
    private static volatile String preloadStatus = "Preparing render resources";
    private static ProgressMeter renderResourceProgress;
    private static CompletableFuture<Void> asyncPreloadFuture;
    private static CompletableFuture<Void> earlyCacheFuture;
    private static boolean irisLookupDone;
    private static Method irisApiGetInstanceMethod;
    private static Method irisApiIsShaderPackInUseMethod;

    private MSDFRenderer() {
    }

    public static void init(ShaderInstance loadedShader) {
        shader = loadedShader;
    }

    public static void warmUpBase() {
        ensureFontSet();
        ensureBoldFontSet();
        ensureIconFont();
    }

    public static void startEarlyCachePreload() {
        if (earlyCacheFuture != null) {
            return;
        }
        updatePreloadStatus("Loading render resources", 5);
        earlyCacheFuture = CompletableFuture.completedFuture(null);
    }

    public static void startAsyncPreload() {
        if (asyncPreloadFuture != null) {
            return;
        }
        startEarlyCachePreload();
        asyncPreloadFuture = new CompletableFuture<>();
        Minecraft.getInstance().execute(() -> {
            try {
                installTtfFonts();
                asyncPreloadFuture.complete(null);
            } catch (Throwable throwable) {
                preloadFailed = true;
                asyncPreloadFuture.completeExceptionally(throwable);
            }
        });
    }

    public static boolean asyncPreloadComplete() {
        return renderResourcesReady() || preloadFailed;
    }

    public static boolean renderResourcesReady() {
        return fullTextAtlasReady && iconAtlasReady && fontSet != null && boldFontSet != null && iconFont != null;
    }

    public static boolean preloadFailed() {
        return preloadFailed;
    }

    public static String preloadStatus() {
        return preloadFailed ? "Render resource preload failed" : preloadStatus;
    }

    public static ReloadResources prepareReloadResources(ResourceManager resourceManager) {
        startEarlyCachePreload();
        updatePreloadStatus("Preparing TTF font resources", 86);
        return new ReloadResources();
    }

    public static void applyReloadResources(ReloadResources resources) {
        try {
            installTtfFonts();
        } catch (Throwable throwable) {
            preloadFailed = true;
            updatePreloadStatus("TTF font initialization failed", 100);
            completeRenderResourceProgress();
        }
    }

    public static void draw(GuiGraphics graphics, String text, float x, float y, int color) {
        draw(graphics, text, x, y, DEFAULT_SIZE, color);
    }

    public static void draw(GuiGraphics graphics, String text, float x, float y, float size, int color) {
        if (text == null || text.isEmpty() || size <= 0.0f) {
            return;
        }
        TtfFontSet activeFont = ensureFontSet();
        if (shader == null || activeFont == null) {
            return;
        }
        float scaled = scaledSize(size);
        if (!supportsAll(activeFont, text)) {
            drawVanillaScreenText(graphics, text, x, y, scaled, color,
                    false, 0.0f, 0.0f, 0);
            return;
        }
        drawWithFontSet(graphics.pose().last().pose(), activeFont, text, x, y,
                scaled, color, false, true);
    }

    /** Draws with MiSans Bold rather than synthesizing a heavier stroke. */
    public static void drawBold(GuiGraphics graphics, String text, float x, float y, float size, int color) {
        if (text == null || text.isEmpty() || size <= 0.0f) {
            return;
        }
        TtfFontSet activeFont = ensureBoldFontSet();
        if (shader == null || activeFont == null || !supportsAll(activeFont, text)) {
            draw(graphics, text, x, y, size, color);
            return;
        }
        drawWithFontSet(graphics.pose().last().pose(), activeFont, text, x, y,
                scaledSize(size), color, false, true);
    }

    public static void drawStyled(GuiGraphics graphics, String text, float x, float y, float size, int color,
                                  boolean bold, boolean italic) {
        if (text == null || text.isEmpty() || size <= 0.0f) {
            return;
        }
        TtfFontSet activeFont = bold ? ensureBoldFontSet() : ensureFontSet();
        if (shader == null || activeFont == null || !supportsAll(activeFont, text)) {
            if (bold) {
                drawBold(graphics, text, x, y, size, color);
            } else {
                draw(graphics, text, x, y, size, color);
            }
            return;
        }
        float scaled = scaledSize(size);
        Matrix4f matrix = graphics.pose().last().pose();
        if (italic) {
            float baselineY = y - activeFont.ascender() * scaled;
            matrix = new Matrix4f(matrix)
                    .translate(0.0f, baselineY, 0.0f)
                    .mul(new Matrix4f().m10(-0.18f))
                    .translate(0.0f, -baselineY, 0.0f);
        }
        drawWithFontSet(matrix, activeFont, text, x, y, scaled, color, false, true);
    }

    public static float textBaselineY(float y, float size, boolean bold) {
        TtfFontSet activeFont = bold ? ensureBoldFontSet() : ensureFontSet();
        return activeFont == null ? y + size : y - activeFont.ascender() * scaledSize(size);
    }

    public static int textDecorationThickness(float size) {
        return Math.max(1, Math.round(scaledSize(size) * 0.055f));
    }

    public static void drawDisplay(GuiGraphics graphics, String text, float x, float y, float size, int color) {
        if (text == null || text.isEmpty() || size <= 0.0f) {
            return;
        }
        TtfFontSet activeFont = ensureFontSet();
        if (shader == null || activeFont == null) {
            return;
        }
        float scaled = scaledSize(size);
        if (!supportsAll(activeFont, text)) {
            drawVanillaScreenText(graphics, text, x, y, scaled, color,
                    false, 0.0f, 0.0f, 0);
            return;
        }
        drawWithFontSet(graphics.pose().last().pose(), activeFont, text, x, y,
                scaled, color, false, true, 0.0f, 0.0f, 0,
                DISPLAY_WEIGHT, DISPLAY_SHARPNESS);
    }

    public static void drawScreenTextWithShadow(GuiGraphics graphics, String text, float x, float y,
                                                float size, int color) {
        if (text == null || text.isEmpty() || size <= 0.0f) {
            return;
        }
        TtfFontSet activeFont = ensureFontSet();
        if (shader == null || activeFont == null) {
            return;
        }
        int alpha = (color >>> 24) & 0xFF;
        if (alpha <= 0) {
            return;
        }
        int shadowAlpha = Math.max(0, Math.min(255, Math.round(alpha * SHADOW_ALPHA_RATIO)));
        float shadowOffset = size * SHADOW_OFFSET_RATIO;
        float scaled = scaledSize(size);
        if (!supportsAll(activeFont, text)) {
            drawVanillaScreenText(graphics, text, x, y, scaled, color,
                    true, shadowOffset * SCREEN_TEXT_SCALE,
                    shadowOffset * SCREEN_TEXT_SCALE, shadowAlpha << 24);
            return;
        }
        drawWithFontSet(graphics.pose().last().pose(), activeFont, text, x, y,
                scaled, color, false, true, shadowOffset * SCREEN_TEXT_SCALE,
                shadowOffset * SCREEN_TEXT_SCALE, shadowAlpha << 24);
    }

    public static void drawInWorld(PoseStack poseStack, String text, float x, float y, float size, int color,
                                   boolean throughWalls) {
        if (text == null || text.isEmpty() || size <= 0.0f) {
            return;
        }
        TtfFontSet activeFont = ensureFontSet();
        if (shader == null || activeFont == null) {
            return;
        }
        if (!supportsAll(activeFont, text)) {
            try (WorldTextBatch fallback = WorldTextBatch.vanillaFallback(activeFont, throughWalls)) {
                fallback.draw(poseStack.last().pose(), text, x, y, size, color);
            }
            return;
        }
        drawWithFontSet(poseStack.last().pose(), activeFont, text, x, y, size, color, !throughWalls, false);
    }

    public static WorldTextBatch beginWorldTextBatch(boolean throughWalls) {
        if (isShaderPackInUse()) {
            TtfFontSet activeFont = ensureFontSet();
            prepareWorldTextRenderState(!throughWalls);
            return WorldTextBatch.vanillaFallback(activeFont, throughWalls);
        }
        TtfFontSet activeFont = ensureFontSet();
        if (shader == null || activeFont == null) {
            return WorldTextBatch.noop();
        }
        prepareWorldTextRenderState(!throughWalls);
        RenderSystem.setShader(() -> shader);
        setUniform("uWeight", WEIGHT);
        setUniform("uGamma", GAMMA);
        setUniform("uSharpness", SHARPNESS);
        setShadow(false, 0.0f, 0.0f, 0);
        return new WorldTextBatch(activeFont, Tesselator.getInstance().getBuilder(), true);
    }

    public static void drawWorldTextWithShadow(WorldTextBatch batch, Matrix4f matrix, String text,
                                               float x, float y, float size, int color) {
        int alpha = (color >>> 24) & 0xFF;
        if (alpha <= 0) {
            return;
        }
        int shadowAlpha = Math.max(0, Math.min(255, Math.round(alpha * SHADOW_ALPHA_RATIO)));
        float shadowOffset = size * SHADOW_OFFSET_RATIO;
        batch.drawWithShadow(matrix, text, x, y, size, color, shadowOffset, shadowOffset, shadowAlpha << 24);
    }

    public static void drawIcon(GuiGraphics graphics, FontAwesomeIcon icon, float x, float y, float size, int color) {
        if (icon == null || size <= 0.0f) {
            return;
        }
        drawIconCodepoint(graphics, icon.codepoint(), x, y, size, color);
    }

    public static void drawIconCodepoint(GuiGraphics graphics, int codepoint, float x, float y, float size, int color) {
        if (size <= 0.0f) {
            return;
        }
        TtfFontSet activeFont = ensureIconFont();
        if (shader == null || activeFont == null) {
            return;
        }
        drawWithFontSet(graphics.pose().last().pose(), activeFont,
                new String(Character.toChars(codepoint)), x, y, size, color, false, true);
    }

    public static float iconWidth(FontAwesomeIcon icon, float size) {
        if (icon == null || size <= 0.0f) {
            return 0.0f;
        }
        return iconWidth(icon.codepoint(), size);
    }

    public static float iconWidth(int codepoint, float size) {
        if (size <= 0.0f) {
            return 0.0f;
        }
        TtfFontSet activeFont = ensureIconFont();
        if (activeFont == null) {
            return 0.0f;
        }
        activeFont.glyph(codepoint);
        return activeFont.advance(codepoint) * size;
    }

    public static float iconCenteredY(int codepoint, float boxY, float boxHeight, float size) {
        if (size <= 0.0f) {
            return boxY + boxHeight * 0.5f;
        }
        TtfFontSet activeFont = ensureIconFont();
        if (activeFont == null) {
            return boxY + (boxHeight - size) * 0.5f;
        }
        TtfFontSet.GlyphRef ref = activeFont.glyph(codepoint);
        if (ref == null || !ref.glyph().drawable()) {
            return boxY + (boxHeight - size) * 0.5f;
        }
        TtfGlyph.Bounds plane = ref.glyph().planeBounds();
        float planeCenter = (plane.top() + plane.bottom()) * 0.5f;
        float targetBaseline = boxY + boxHeight * 0.5f - planeCenter * size;
        return targetBaseline + activeFont.ascender() * size;
    }

    public static float textCenteredY(float boxY, float boxHeight, float size) {
        if (size <= 0.0f) {
            return boxY + boxHeight * 0.5f;
        }
        TtfFontSet activeFont = ensureFontSet();
        float lineHeight = activeFont == null ? 1.0f : activeFont.lineHeight();
        return boxY + (boxHeight - lineHeight * scaledSize(size)) * 0.5f;
    }

    private static void drawWithFontSet(Matrix4f matrix, TtfFontSet activeFont, String text, float x, float y,
                                        float size, int color, boolean depthTest, boolean restoreUiState) {
        drawWithFontSet(matrix, activeFont, text, x, y, size, color, depthTest, restoreUiState,
                0.0f, 0.0f, 0);
    }

    private static void drawWithFontSet(Matrix4f matrix, TtfFontSet activeFont, String text, float x, float y,
                                        float size, int color, boolean depthTest, boolean restoreUiState,
                                        float shadowOffsetX, float shadowOffsetY, int shadowColor) {
        drawWithFontSet(matrix, activeFont, text, x, y, size, color, depthTest, restoreUiState,
                shadowOffsetX, shadowOffsetY, shadowColor, WEIGHT, SHARPNESS);
    }

    private static void drawWithFontSet(Matrix4f matrix, TtfFontSet activeFont, String text, float x, float y,
                                        float size, int color, boolean depthTest, boolean restoreUiState,
                                        float shadowOffsetX, float shadowOffsetY, int shadowColor,
                                        float weight, float sharpness) {
        if (activeFont == null) {
            return;
        }
        prepareRenderState(depthTest);

        RenderSystem.setShader(() -> shader);
        setColor(color);
        setShadow((shadowColor >>> 24) != 0, shadowOffsetX, shadowOffsetY, shadowColor);
        setUniform("uWeight", weight);
        setUniform("uGamma", GAMMA);
        setUniform("uSharpness", sharpness);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        TtfAtlas currentFont = null;
        boolean drawing = false;
        float cursorX = snap(x);
        float baselineY = snap(y - activeFont.ascender() * size);

        try {
            for (int offset = 0; offset < text.length(); ) {
                int codePoint = text.codePointAt(offset);
                offset += Character.charCount(codePoint);

                TtfFontSet.GlyphRef ref = activeFont.glyph(codePoint);
                if (ref == null) {
                    cursorX += activeFont.advance(codePoint) * size;
                    continue;
                }

                TtfGlyph glyph = ref.glyph();
                if (glyph.drawable()) {
                    if (currentFont != ref.atlas()) {
                        if (drawing) {
                            flush(buffer, currentFont);
                            drawing = false;
                        }
                        currentFont = ref.atlas();
                    }
                    if (!drawing) {
                        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                        drawing = true;
                    }
                    float glyphBaselineY = baselineY + (activeFont.ascender() - ref.ascender()) * size;
                    emitGlyph(buffer, matrix, ref.atlas(), glyph, cursorX, glyphBaselineY, size);
                }
                cursorX += glyph.advance() * size;
            }

            if (drawing) {
                flush(buffer, currentFont);
            }
        } finally {
            if (restoreUiState) {
                RenderSystem.enableDepthTest();
                RenderSystem.enableCull();
                RenderSystem.disableBlend();
            }
            setShadow(false, 0.0f, 0.0f, 0);
        }
    }

    private static void prepareRenderState(boolean depthTest) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        if (depthTest) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
    }

    private static void prepareWorldTextRenderState(boolean depthTest) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            minecraft.getMainRenderTarget().bindWrite(false);
        }
        prepareRenderState(depthTest);
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void flush(BufferBuilder buffer, TtfAtlas font) {
        RenderSystem.setShader(() -> shader);
        shader.setSampler("uTexture", font.texture().getId());
        setUniform("uPxRange", font.distanceRange());
        BufferUploader.drawWithShader(buffer.end());
    }

    private static void flushWorld(BufferBuilder buffer, TtfAtlas font) {
        prepareWorldTextRenderState(false);
        flush(buffer, font);
    }

    private static boolean isShaderPackInUse() {
        if (!irisLookupDone) {
            irisLookupDone = true;
            try {
                Class<?> irisApi = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                irisApiGetInstanceMethod = irisApi.getMethod("getInstance");
                irisApiIsShaderPackInUseMethod = irisApi.getMethod("isShaderPackInUse");
            } catch (ReflectiveOperationException ignored) {
                irisApiGetInstanceMethod = null;
                irisApiIsShaderPackInUseMethod = null;
            }
        }
        if (irisApiGetInstanceMethod == null || irisApiIsShaderPackInUseMethod == null) {
            return false;
        }
        try {
            Object api = irisApiGetInstanceMethod.invoke(null);
            return api != null && Boolean.TRUE.equals(irisApiIsShaderPackInUseMethod.invoke(api));
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    public static int width(String text) {
        return Math.round(width(text, DEFAULT_SIZE));
    }

    public static float width(String text, float size) {
        if (text == null || text.isEmpty() || size <= 0.0f) {
            return 0.0f;
        }
        TtfFontSet activeFont = ensureFontSet();
        if (activeFont == null) {
            return 0.0f;
        }

        float width = 0.0f;
        float scaled = scaledSize(size);
        if (!supportsAll(activeFont, text)) {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft == null || minecraft.font == null
                    ? 0.0f
                    : minecraft.font.width(text) * scaled / 9.0f;
        }
        for (int offset = 0; offset < text.length(); ) {
            int codePoint = text.codePointAt(offset);
            offset += Character.charCount(codePoint);
            activeFont.glyph(codePoint);
            width += activeFont.advance(codePoint) * scaled;
        }
        return width;
    }

    public static float boldWidth(String text, float size) {
        if (text == null || text.isEmpty() || size <= 0.0f) {
            return 0.0f;
        }
        TtfFontSet activeFont = ensureBoldFontSet();
        if (activeFont == null || !supportsAll(activeFont, text)) {
            return width(text, size);
        }
        float width = 0.0f;
        float scaled = scaledSize(size);
        for (int offset = 0; offset < text.length(); ) {
            int codePoint = text.codePointAt(offset);
            offset += Character.charCount(codePoint);
            activeFont.glyph(codePoint);
            width += activeFont.advance(codePoint) * scaled;
        }
        return width;
    }

    public static boolean supportsAll(String text) {
        if (text == null || text.isEmpty()) {
            return true;
        }
        TtfFontSet activeFont = ensureFontSet();
        if (activeFont == null) {
            return false;
        }

        return supportsAll(activeFont, text);
    }

    private static boolean supportsAll(TtfFontSet activeFont, String text) {
        for (int offset = 0; offset < text.length(); ) {
            int codePoint = text.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (Character.isWhitespace(codePoint)) {
                continue;
            }
            if (!activeFont.supports(codePoint)) {
                return false;
            }
        }
        return true;
    }

    private static void drawVanillaScreenText(GuiGraphics graphics, String text, float x, float y,
                                              float size, int color, boolean shadowEnabled,
                                              float shadowOffsetX, float shadowOffsetY, int shadowColor) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.font == null || size <= 0.0f) {
            return;
        }
        float scale = size / 9.0f;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        if (shadowEnabled && ((shadowColor >>> 24) & 0xFF) > 0) {
            graphics.drawString(minecraft.font, text,
                    shadowOffsetX / scale, shadowOffsetY / scale, shadowColor, false);
        }
        graphics.drawString(minecraft.font, text, 0.0f, 0.0f, color, false);
        graphics.pose().popPose();
    }

    private static void emitGlyph(BufferBuilder buffer, Matrix4f matrix, TtfAtlas font, TtfGlyph glyph,
                                  float cursorX, float baselineY, float size) {
        TtfGlyph.Bounds plane = glyph.planeBounds();
        TtfGlyph.Bounds atlas = glyph.atlasBounds();

        float x0 = cursorX + plane.left() * size;
        float y0 = baselineY + plane.top() * size;
        float x1 = cursorX + plane.right() * size;
        float y1 = baselineY + plane.bottom() * size;

        float u0 = atlas.left() / font.width();
        float v0 = atlas.top() / font.height();
        float u1 = atlas.right() / font.width();
        float v1 = atlas.bottom() / font.height();

        buffer.vertex(matrix, x0, y1, 0.0f).uv(u0, v1).endVertex();
        buffer.vertex(matrix, x1, y1, 0.0f).uv(u1, v1).endVertex();
        buffer.vertex(matrix, x1, y0, 0.0f).uv(u1, v0).endVertex();
        buffer.vertex(matrix, x0, y0, 0.0f).uv(u0, v0).endVertex();
    }

    private static float snap(float value) {
        return Math.round(value);
    }

    private static float scaledSize(float size) {
        return size * SCREEN_TEXT_SCALE;
    }

    private static TtfFontSet ensureFontSet() {
        if (fontSet != null) {
            return fontSet;
        }
        startAsyncPreload();
        return null;
    }

    private static TtfFontSet ensureBoldFontSet() {
        if (boldFontSet != null) {
            return boldFontSet;
        }
        startAsyncPreload();
        return null;
    }

    private static TtfFontSet ensureIconFont() {
        if (iconFont != null) {
            return iconFont;
        }
        startAsyncPreload();
        return null;
    }

    private static void installTtfFonts() {
        updatePreloadStatus("Loading MiSans Global and Font Awesome TTF", 92);
        TtfFontSet newTextFont = new TtfFontSet(TEXT_FONT, "misans_sdf", 0xFFFD, 2048);
        TtfFontSet newBoldTextFont = new TtfFontSet(TEXT_BOLD_FONT, "misans_bold_sdf", 0xFFFD, 2048);
        TtfFontSet newIconFont = null;
        try {
            for (FontFallbackSpec fallback : TEXT_FONT_FALLBACKS) {
                newTextFont.addFallback(new TtfFontSet(
                        fallback.location(), fallback.atlasPrefix(), 0, fallback.atlasSize()));
            }
            newIconFont = new TtfFontSet(ICON_FONT, "fontawesome_sdf", 0, 1024);
            newTextFont.preload("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 .,:;!?+-_/()[]{}");
            newBoldTextFont.preload("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 .,:;!?+-_/()[]{}");
            newIconFont.preload(
                    FontAwesomeIcons.ICON_FA_USER,
                    FontAwesomeIcons.ICON_FA_GEAR,
                    FontAwesomeIcons.ICON_FA_XMARK,
                    FontAwesomeIcons.ICON_FA_CHECK,
                    FontAwesomeIcons.ICON_FA_ANGLE_DOWN,
                    FontAwesomeIcons.ICON_FA_ANGLE_LEFT,
                    FontAwesomeIcons.ICON_FA_ANGLE_RIGHT,
                    FontAwesomeIcons.ICON_FA_MAGNIFYING_GLASS
            );
            replaceFontSet(newTextFont);
            replaceBoldFontSet(newBoldTextFont);
            replaceIconFont(newIconFont);
            fullTextAtlasReady = true;
            iconAtlasReady = true;
            preloadFailed = false;
            updatePreloadStatus("MiSans Global font resources loaded", 100);
            completeRenderResourceProgress();
        } catch (Throwable throwable) {
            closeQuietly(newTextFont);
            closeQuietly(newBoldTextFont);
            closeQuietly(newIconFont);
            throw throwable;
        }
    }

    private static void replaceFontSet(TtfFontSet replacement) {
        TtfFontSet previous = fontSet;
        fontSet = replacement;
        if (previous != null && previous != replacement) {
            previous.close();
        }
    }

    private static void replaceBoldFontSet(TtfFontSet replacement) {
        TtfFontSet previous = boldFontSet;
        boldFontSet = replacement;
        if (previous != null && previous != replacement) {
            previous.close();
        }
    }

    private static FontFallbackSpec fallback(String fileName, String atlasPrefix, int atlasSize) {
        return new FontFallbackSpec(ResourceLocation.fromNamespaceAndPath(
                XerionForge.MOD_ID, "fonts/" + fileName), atlasPrefix, atlasSize);
    }

    private static void replaceIconFont(TtfFontSet replacement) {
        TtfFontSet previous = iconFont;
        iconFont = replacement;
        if (previous != null && previous != replacement) {
            previous.close();
        }
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }

    public static String diagnosticsSummary() {
        TtfFontSet activeSet = fontSet;
        int loadedPages = activeSet == null ? 0 : activeSet.loadedGlyphCount();
        int loadingPages = activeSet == null ? 0 : activeSet.pendingGlyphCount();
        int failedPages = activeSet == null ? 0 : activeSet.failedGlyphCount();
        return String.format(Locale.ROOT,
                "msdfReady=%s iconReady=%s loadedPages=%d loadingPages=%d failedPages=%d preloadFailed=%s",
                fullTextAtlasReady,
                iconAtlasReady,
                loadedPages,
                loadingPages,
                failedPages,
                preloadFailed);
    }

    private static synchronized void updatePreloadStatus(String status, int progress) {
        preloadStatus = status;
        ProgressMeter meter = ensureRenderResourceProgress();
        meter.label(status);
        meter.setAbsolute(Math.max(0, Math.min(100, progress)));
    }

    private static synchronized ProgressMeter ensureRenderResourceProgress() {
        if (renderResourceProgress == null) {
            StartupMessageManager.addModMessage("Loading render resources");
            renderResourceProgress = StartupMessageManager.prependProgressBar("Loading render resources", 100);
        }
        return renderResourceProgress;
    }

    private static synchronized void completeRenderResourceProgress() {
        if (renderResourceProgress != null) {
            renderResourceProgress.setAbsolute(100);
            renderResourceProgress.complete();
        }
    }

    public record ReloadResources() implements AutoCloseable {
        @Override
        public void close() {
        }
    }

    private static void setColor(int color) {
        if (shader.getUniform("uColor") == null) {
            return;
        }
        shader.getUniform("uColor").set(
                ((color >> 16) & 0xFF) / 255.0f,
                ((color >> 8) & 0xFF) / 255.0f,
                (color & 0xFF) / 255.0f,
                ((color >> 24) & 0xFF) / 255.0f
        );
    }

    private static void setUniform(String name, float value) {
        if (shader.getUniform(name) != null) {
            shader.getUniform(name).set(value);
        }
    }

    private static void setShadow(boolean enabled, float offsetX, float offsetY, int color) {
        setUniform("uShadowEnabled", enabled ? 1.0f : 0.0f);
        if (shader.getUniform("uShadowOffset") != null) {
            shader.getUniform("uShadowOffset").set(offsetX, offsetY);
        }
        if (shader.getUniform("uShadowColor") != null) {
            shader.getUniform("uShadowColor").set(
                    ((color >> 16) & 0xFF) / 255.0f,
                    ((color >> 8) & 0xFF) / 255.0f,
                    (color & 0xFF) / 255.0f,
                    ((color >> 24) & 0xFF) / 255.0f
            );
        }
    }

    public static final class WorldTextBatch implements AutoCloseable {
        private static final WorldTextBatch NOOP = new WorldTextBatch(null, null, false);

        private final TtfFontSet activeFont;
        private final BufferBuilder buffer;
        private final boolean restoreState;
        private final boolean vanillaFallback;
        private final boolean throughWalls;
        private TtfAtlas currentFont;
        private boolean drawing;
        private boolean colorBound;
        private boolean shadowBound;
        private int currentColor;
        private boolean currentShadowEnabled;
        private float currentShadowOffsetX;
        private float currentShadowOffsetY;
        private int currentShadowColor;
        private boolean closed;

        private WorldTextBatch(TtfFontSet activeFont, BufferBuilder buffer, boolean restoreState) {
            this(activeFont, buffer, restoreState, false, false);
        }

        private WorldTextBatch(TtfFontSet activeFont, BufferBuilder buffer, boolean restoreState,
                               boolean vanillaFallback, boolean throughWalls) {
            this.activeFont = activeFont;
            this.buffer = buffer;
            this.restoreState = restoreState;
            this.vanillaFallback = vanillaFallback;
            this.throughWalls = throughWalls;
        }

        private static WorldTextBatch noop() {
            return NOOP;
        }

        private static WorldTextBatch vanillaFallback(TtfFontSet activeFont, boolean throughWalls) {
            return new WorldTextBatch(activeFont, null, true, true, throughWalls);
        }

        public void draw(Matrix4f matrix, String text, float x, float y, float size, int color) {
            drawInternal(matrix, text, x, y, size, color, false, 0.0f, 0.0f, 0);
        }

        private void drawWithShadow(Matrix4f matrix, String text, float x, float y, float size, int color,
                                    float shadowOffsetX, float shadowOffsetY, int shadowColor) {
            drawInternal(matrix, text, x, y, size, color, true, shadowOffsetX, shadowOffsetY, shadowColor);
        }

        private void drawInternal(Matrix4f matrix, String text, float x, float y, float size, int color,
                                  boolean shadowEnabled, float shadowOffsetX, float shadowOffsetY, int shadowColor) {
            if (closed || text == null || text.isEmpty() || size <= 0.0f) {
                return;
            }
            if (vanillaFallback) {
                drawVanilla(matrix, text, x, y, size, color, shadowEnabled, shadowOffsetX, shadowOffsetY, shadowColor);
                return;
            }
            if (activeFont == null) {
                return;
            }
            if (!supportsAll(activeFont, text)) {
                flushActive();
                drawVanilla(matrix, text, x, y, size, color,
                        shadowEnabled, shadowOffsetX, shadowOffsetY, shadowColor);
                return;
            }
            boolean shadowChanged = !shadowBound
                    || currentShadowEnabled != shadowEnabled
                    || currentShadowColor != shadowColor
                    || Float.compare(currentShadowOffsetX, shadowOffsetX) != 0
                    || Float.compare(currentShadowOffsetY, shadowOffsetY) != 0;
            if (!colorBound || currentColor != color || shadowChanged) {
                flushActive();
                setColor(color);
                setShadow(shadowEnabled, shadowOffsetX, shadowOffsetY, shadowColor);
                currentColor = color;
                currentShadowEnabled = shadowEnabled;
                currentShadowOffsetX = shadowOffsetX;
                currentShadowOffsetY = shadowOffsetY;
                currentShadowColor = shadowColor;
                colorBound = true;
                shadowBound = true;
            }

            float cursorX = snap(x);
            float baselineY = snap(y - activeFont.ascender() * size);
            for (int offset = 0; offset < text.length(); ) {
                int codePoint = text.codePointAt(offset);
                offset += Character.charCount(codePoint);

                TtfFontSet.GlyphRef ref = activeFont.glyph(codePoint);
                if (ref == null) {
                    cursorX += activeFont.advance(codePoint) * size;
                    continue;
                }

                TtfGlyph glyph = ref.glyph();
                if (glyph.drawable()) {
                    if (currentFont != ref.atlas()) {
                        flushActive();
                        currentFont = ref.atlas();
                    }
                    if (!drawing) {
                        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                        drawing = true;
                    }
                    float glyphBaselineY = baselineY + (activeFont.ascender() - ref.ascender()) * size;
                    emitGlyph(buffer, matrix, ref.atlas(), glyph, cursorX, glyphBaselineY, size);
                }
                cursorX += glyph.advance() * size;
            }
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            try {
                flushActive();
            } finally {
                if (restoreState) {
                    RenderSystem.depthMask(true);
                    RenderSystem.enableDepthTest();
                    RenderSystem.enableCull();
                    RenderSystem.disableBlend();
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                }
                closed = true;
            }
        }

        private void flushActive() {
            if (!drawing || currentFont == null) {
                return;
            }
            flushWorld(buffer, currentFont);
            drawing = false;
            currentFont = null;
        }

        private void drawVanilla(Matrix4f matrix, String text, float x, float y, float size, int color,
                                 boolean shadowEnabled, float shadowOffsetX, float shadowOffsetY, int shadowColor) {
            if (text == null || text.isEmpty() || size <= 0.0f) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null || minecraft.font == null) {
                return;
            }
            prepareWorldTextRenderState(!throughWalls);
            Font.DisplayMode mode = throughWalls ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL;
            float scale = size / 9.0f;
            float drawX = vanillaAlignedX(minecraft.font, text, x, size, scale);
            MultiBufferSource.BufferSource source = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            if (shadowEnabled && ((shadowColor >>> 24) & 0xFF) > 0) {
                Matrix4f shadowMatrix = new Matrix4f(matrix)
                        .translate(drawX + shadowOffsetX, y + shadowOffsetY, 0.0f)
                        .scale(scale, scale, 1.0f);
                minecraft.font.drawInBatch(text, 0.0f, 0.0f, shadowColor, false, shadowMatrix, source,
                        mode, 0, LightTexture.FULL_BRIGHT);
            }
            Matrix4f textMatrix = new Matrix4f(matrix)
                    .translate(drawX, y, 0.0f)
                    .scale(scale, scale, 1.0f);
            minecraft.font.drawInBatch(text, 0.0f, 0.0f, color, false, textMatrix, source,
                    mode, 0, LightTexture.FULL_BRIGHT);
            source.endBatch();
        }

        private float vanillaAlignedX(Font font, String text, float x, float size, float scale) {
            if (activeFont == null) {
                return x;
            }
            float msdfWidth = MSDFRenderer.width(text, size);
            float vanillaWidth = font.width(text) * scale;
            if (msdfWidth <= 0.0f || vanillaWidth <= 0.0f) {
                return x;
            }
            if (Math.abs(x + msdfWidth * 0.5f) <= 1.0f) {
                return -vanillaWidth * 0.5f;
            }
            if (Math.abs(x + msdfWidth) <= 1.0f) {
                return -vanillaWidth;
            }
            return x;
        }
    }

    private record FontFallbackSpec(ResourceLocation location, String atlasPrefix, int atlasSize) {
    }
}
