package com.xerion.forge.gui.font;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.xerion.forge.XerionForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** Independent STB TrueType/SDF font backend used for both text and icons. */
final class TtfFontSet implements AutoCloseable {
    private static final int RASTER_HEIGHT = 96;
    private static final int PADDING = 12;
    private static final int EM_PIXELS = RASTER_HEIGHT - PADDING * 2;
    private static final int GUTTER = 2;
    private static final byte EDGE_VALUE = (byte) 128;
    private static final AtomicInteger ATLAS_ID = new AtomicInteger();
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Xerion-TTF-Glyph-Worker");
        thread.setDaemon(true);
        return thread;
    });

    private final ResourceLocation fontLocation;
    private final String atlasPrefix;
    private final int fallbackCodePoint;
    private final int atlasSize;
    private final ByteBuffer fontData;
    private final STBTTFontinfo fontInfo;
    private final float scale;
    private final float lineHeight;
    private final float ascender;
    private final float descender;
    private final Map<Integer, Float> advances = new ConcurrentHashMap<>();
    private final Map<Integer, GlyphRef> glyphs = new ConcurrentHashMap<>();
    private final Map<Integer, Boolean> directSupport = new ConcurrentHashMap<>();
    private final Map<Integer, TtfFontSet> resolvedFonts = new ConcurrentHashMap<>();
    private final Set<Integer> unsupported = ConcurrentHashMap.newKeySet();
    private final List<TtfFontSet> fallbacks = new ArrayList<>();
    private final Set<Integer> pending = ConcurrentHashMap.newKeySet();
    private final Set<Integer> failed = ConcurrentHashMap.newKeySet();
    private final Queue<GeneratedGlyph> completed = new ConcurrentLinkedQueue<>();
    private final List<AtlasPage> atlases = new ArrayList<>();
    private final AtomicInteger activeJobs = new AtomicInteger();
    private final AtomicBoolean fontDataFreed = new AtomicBoolean();
    private volatile boolean closed;
    private AtlasPage currentAtlas;

    TtfFontSet(ResourceLocation fontLocation, String atlasPrefix, int fallbackCodePoint, int atlasSize) {
        this.fontLocation = fontLocation;
        this.atlasPrefix = atlasPrefix;
        this.fallbackCodePoint = fallbackCodePoint;
        this.atlasSize = Math.max(512, atlasSize);
        this.fontData = loadFontData(fontLocation);
        this.fontInfo = STBTTFontinfo.create();
        if (!STBTruetype.stbtt_InitFont(fontInfo, fontData)) {
            MemoryUtil.memFree(fontData);
            throw new IllegalStateException("Failed to initialize TTF: " + fontLocation);
        }
        this.scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, EM_PIXELS);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var ascentBuffer = stack.mallocInt(1);
            var descentBuffer = stack.mallocInt(1);
            var lineGapBuffer = stack.mallocInt(1);
            synchronized (fontInfo) {
                STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascentBuffer, descentBuffer, lineGapBuffer);
            }
            float ascent = ascentBuffer.get(0) * scale / EM_PIXELS;
            float descent = descentBuffer.get(0) * scale / EM_PIXELS;
            float lineGap = lineGapBuffer.get(0) * scale / EM_PIXELS;
            this.lineHeight = ascent - descent + lineGap;
            this.ascender = -ascent;
            this.descender = -descent;
        }
        createAtlas();
    }

    GlyphRef glyph(int codePoint) {
        TtfFontSet resolved = resolveFont(codePoint);
        return resolved == null ? null : resolved.glyphDirect(codePoint);
    }

    private GlyphRef glyphDirect(int codePoint) {
        drainReadyGlyphs();
        GlyphRef ready = glyphs.get(codePoint);
        if (ready != null) {
            return ready;
        }
        requestDirect(codePoint);
        return null;
    }

    float advance(int codePoint) {
        TtfFontSet resolved = resolveFont(codePoint);
        return resolved == null ? 0.5f : resolved.advanceDirect(codePoint);
    }

    private float advanceDirect(int codePoint) {
        Float cached = advances.get(codePoint);
        if (cached != null) {
            return cached;
        }
        int glyphIndex = resolveGlyphIndex(codePoint);
        float value = 0.5f;
        if (glyphIndex != 0) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                var advance = stack.mallocInt(1);
                var lsb = stack.mallocInt(1);
                synchronized (fontInfo) {
                    STBTruetype.stbtt_GetGlyphHMetrics(fontInfo, glyphIndex, advance, lsb);
                }
                value = advance.get(0) * scale / EM_PIXELS;
            }
        }
        advances.put(codePoint, value);
        return value;
    }

    boolean supports(int codePoint) {
        return resolveFont(codePoint) != null;
    }

    private boolean supportsDirect(int codePoint) {
        return directSupport.computeIfAbsent(codePoint, ignored -> findGlyphIndex(codePoint) != 0);
    }

    void addFallback(TtfFontSet fallback) {
        if (fallback == null || fallback == this) {
            return;
        }
        fallbacks.add(fallback);
        resolvedFonts.clear();
        unsupported.clear();
    }

    private TtfFontSet resolveFont(int codePoint) {
        TtfFontSet cached = resolvedFonts.get(codePoint);
        if (cached != null) {
            return cached;
        }
        if (unsupported.contains(codePoint)) {
            return null;
        }
        if (supportsDirect(codePoint)) {
            resolvedFonts.put(codePoint, this);
            return this;
        }
        for (TtfFontSet fallback : fallbacks) {
            if (fallback.supportsDirect(codePoint)) {
                resolvedFonts.put(codePoint, fallback);
                return fallback;
            }
        }
        unsupported.add(codePoint);
        return null;
    }

    private int findGlyphIndex(int codePoint) {
        synchronized (fontInfo) {
            return STBTruetype.stbtt_FindGlyphIndex(fontInfo, codePoint);
        }
    }

    float lineHeight() {
        return lineHeight;
    }

    float ascender() {
        return ascender;
    }

    float descender() {
        return descender;
    }

    int loadedGlyphCount() {
        return glyphs.size() + fallbacks.stream().mapToInt(TtfFontSet::loadedGlyphCount).sum();
    }

    int pendingGlyphCount() {
        return pending.size() + fallbacks.stream().mapToInt(TtfFontSet::pendingGlyphCount).sum();
    }

    int failedGlyphCount() {
        return failed.size() + fallbacks.stream().mapToInt(TtfFontSet::failedGlyphCount).sum();
    }

    void preload(String text) {
        if (text == null) {
            return;
        }
        text.codePoints().forEach(this::request);
    }

    void preload(int... codePoints) {
        if (codePoints == null) {
            return;
        }
        for (int codePoint : codePoints) {
            request(codePoint);
        }
    }

    private void request(int codePoint) {
        TtfFontSet resolved = resolveFont(codePoint);
        if (resolved != null) {
            resolved.requestDirect(codePoint);
        }
    }

    private void requestDirect(int codePoint) {
        if (closed || failed.contains(codePoint) || Character.isISOControl(codePoint)
                || Character.isWhitespace(codePoint) || glyphs.containsKey(codePoint)
                || !pending.add(codePoint)) {
            return;
        }
        if (!supportsDirect(codePoint)) {
            pending.remove(codePoint);
            failed.add(codePoint);
            return;
        }
        activeJobs.incrementAndGet();
        CompletableFuture.supplyAsync(() -> generate(codePoint), WORKER)
                .whenComplete((glyph, throwable) -> {
                    pending.remove(codePoint);
                    if (closed) {
                        free(glyph);
                    } else if (throwable == null && glyph != null) {
                        completed.add(glyph);
                    } else {
                        failed.add(codePoint);
                    }
                    if (activeJobs.decrementAndGet() == 0 && closed) {
                        freeFontData();
                    }
                });
    }

    private GeneratedGlyph generate(int codePoint) {
        int glyphIndex = resolveGlyphIndex(codePoint);
        if (glyphIndex == 0) {
            return null;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var width = stack.mallocInt(1);
            var height = stack.mallocInt(1);
            var xOffset = stack.mallocInt(1);
            var yOffset = stack.mallocInt(1);
            ByteBuffer pixels;
            synchronized (fontInfo) {
                pixels = STBTruetype.stbtt_GetGlyphSDF(fontInfo, scale, glyphIndex, PADDING,
                        EDGE_VALUE, (float) EDGE_VALUE / PADDING,
                        width, height, xOffset, yOffset);
            }
            if (pixels == null) {
                return null;
            }
            return new GeneratedGlyph(codePoint, pixels, width.get(0), height.get(0),
                    xOffset.get(0) / (float) EM_PIXELS,
                    yOffset.get(0) / (float) EM_PIXELS,
                    (xOffset.get(0) + width.get(0)) / (float) EM_PIXELS,
                    (yOffset.get(0) + height.get(0)) / (float) EM_PIXELS,
                    advanceDirect(codePoint));
        }
    }

    private void drainReadyGlyphs() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || !minecraft.isSameThread()) {
            return;
        }
        for (GeneratedGlyph glyph; (glyph = completed.poll()) != null; ) {
            append(glyph);
        }
    }

    private void append(GeneratedGlyph generated) {
        if (closed) {
            free(generated);
            return;
        }
        AtlasPage page = currentAtlas;
        int cellWidth = generated.width + GUTTER * 2;
        int cellHeight = generated.height + GUTTER * 2;
        if (page.x + cellWidth > atlasSize) {
            page.x = 0;
            page.y += page.rowHeight;
            page.rowHeight = 0;
        }
        if (page.y + cellHeight > atlasSize) {
            page = createAtlas();
        }

        int x = page.x + GUTTER;
        int y = page.y + GUTTER;
        NativeImage image = page.texture.getPixels();
        ByteBuffer pixels = generated.pixels;
        for (int row = 0; row < generated.height; row++) {
            for (int column = 0; column < generated.width; column++) {
                int distance = pixels.get(row * generated.width + column) & 0xFF;
                image.setPixelRGBA(x + column, y + row, rgba(distance, distance, distance, 255));
            }
        }
        RenderSystem.bindTexture(page.texture.getId());
        image.upload(0, x, y, x, y, generated.width, generated.height, false, false);

        TtfGlyph glyph = new TtfGlyph(generated.codePoint, generated.advance,
                new TtfGlyph.Bounds(generated.left, generated.top, generated.right, generated.bottom),
                new TtfGlyph.Bounds(x, y, x + generated.width, y + generated.height));
        page.glyphs.put(generated.codePoint, glyph);
        glyphs.put(generated.codePoint, new GlyphRef(page.atlas, glyph, ascender));
        page.x += cellWidth;
        page.rowHeight = Math.max(page.rowHeight, cellHeight);
        free(generated);
    }

    private AtlasPage createAtlas() {
        NativeImage image = new NativeImage(atlasSize, atlasSize, false);
        image.fillRect(0, 0, atlasSize, atlasSize, rgba(255, 255, 255, 255));
        DynamicTexture texture = new DynamicTexture(image);
        texture.setFilter(true, false);
        texture.upload();
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(XerionForge.MOD_ID,
                "fonts/" + atlasPrefix + "_" + ATLAS_ID.incrementAndGet());
        Minecraft.getInstance().getTextureManager().register(location, texture);
        Map<Integer, TtfGlyph> atlasGlyphs = new ConcurrentHashMap<>();
        TtfAtlas atlas = new TtfAtlas(texture, atlasSize, atlasSize, PADDING, atlasGlyphs);
        AtlasPage page = new AtlasPage(location, texture, atlas, atlasGlyphs);
        atlases.add(page);
        currentAtlas = page;
        return page;
    }

    private int resolveGlyphIndex(int codePoint) {
        synchronized (fontInfo) {
            int index = STBTruetype.stbtt_FindGlyphIndex(fontInfo, codePoint);
            if (index == 0 && fallbackCodePoint > 0 && fallbackCodePoint != codePoint) {
                index = STBTruetype.stbtt_FindGlyphIndex(fontInfo, fallbackCodePoint);
            }
            return index;
        }
    }

    private static ByteBuffer loadFontData(ResourceLocation location) {
        try (InputStream input = Minecraft.getInstance().getResourceManager().open(location)) {
            byte[] bytes = input.readAllBytes();
            ByteBuffer data = MemoryUtil.memAlloc(bytes.length);
            data.put(bytes).flip();
            return data;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load bundled TTF: " + location, ex);
        }
    }

    private static int rgba(int r, int g, int b, int a) {
        return (a & 0xFF) << 24 | (b & 0xFF) << 16 | (g & 0xFF) << 8 | (r & 0xFF);
    }

    private static void free(GeneratedGlyph glyph) {
        if (glyph != null && glyph.pixels != null) {
            STBTruetype.stbtt_FreeSDF(glyph.pixels);
        }
    }

    private void freeFontData() {
        if (fontDataFreed.compareAndSet(false, true)) {
            MemoryUtil.memFree(fontData);
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        for (GeneratedGlyph glyph; (glyph = completed.poll()) != null; ) {
            free(glyph);
        }
        for (AtlasPage page : atlases) {
            Minecraft.getInstance().getTextureManager().release(page.location);
        }
        atlases.clear();
        glyphs.clear();
        pending.clear();
        resolvedFonts.clear();
        unsupported.clear();
        for (TtfFontSet fallback : fallbacks) {
            fallback.close();
        }
        fallbacks.clear();
        if (activeJobs.get() == 0) {
            freeFontData();
        }
    }

    record GlyphRef(TtfAtlas atlas, TtfGlyph glyph, float ascender) {
    }

    private record GeneratedGlyph(int codePoint, ByteBuffer pixels, int width, int height,
                                  float left, float top, float right, float bottom, float advance) {
    }

    private static final class AtlasPage {
        private final ResourceLocation location;
        private final DynamicTexture texture;
        private final TtfAtlas atlas;
        private final Map<Integer, TtfGlyph> glyphs;
        private int x;
        private int y;
        private int rowHeight;

        private AtlasPage(ResourceLocation location, DynamicTexture texture, TtfAtlas atlas,
                          Map<Integer, TtfGlyph> glyphs) {
            this.location = location;
            this.texture = texture;
            this.atlas = atlas;
            this.glyphs = glyphs;
        }
    }
}
