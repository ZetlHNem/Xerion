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

import com.xerion.common.config.UiSettings;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import java.lang.reflect.Field;

public final class KawaseBlurPipeline {
    private static final float BLUR_RESOLUTION_SCALE = 0.35f;
    private static final float BLUR_STRENGTH = 30.0f;
    private static final float FULLSCREEN_BLUR_OPACITY = 1.0f;
    private static final float SURFACE_BACKDROP_OPACITY = 1.0f;
    private static final float SURFACE_BACKDROP_STRENGTH = 28.0f;
    private static final float TINT_STRENGTH = 0.0f;
    private static final int TINT_COLOR = 0xFFF7F8FC;
    private static final int PASS_COUNT = 8;

    private static ShaderInstance shader;
    private static int sourceTexture;
    private static int sourceFbo;
    private static int sourceW;
    private static int sourceH;
    private static int pingTexture;
    private static int pongTexture;
    private static int pingFbo;
    private static int pongFbo;
    private static int blurW;
    private static int blurH;
    private static boolean framebufferReady;
    private static int lastOutputTexture;
    private static int lastOutputW;
    private static int lastOutputH;
    private static int frameSerial;
    private static int capturedFrame = -1;
    private static int preparedFrame = -1;
    private static float preparedStrength = Float.NaN;
    private static boolean watutLookupDone;
    private static Field watutPerformingOwnRenderField;

    private KawaseBlurPipeline() {
    }

    public static void init(ShaderInstance loadedShader) {
        shader = loadedShader;
    }

    public static void beginFrame() {
        if (isForeignOffscreenRender()) {
            return;
        }
        frameSerial++;
    }

    public static void beginOverlayStage() {
        if (isForeignOffscreenRender()) {
            return;
        }
        frameSerial++;
    }

    public static void refreshOverlaySample() {
        if (isForeignOffscreenRender()) {
            return;
        }
        capturedFrame = -1;
        preparedFrame = -1;
        frameSerial++;
    }

    public static void clearResources() {
        if (sourceTexture == 0 && pingTexture == 0 && pongTexture == 0
                && sourceFbo == 0 && pingFbo == 0 && pongFbo == 0) {
            return;
        }
        if (RenderSystem.isOnRenderThread()) {
            releaseTargets();
        } else {
            RenderSystem.recordRenderCall(KawaseBlurPipeline::releaseTargets);
        }
    }

    public static void render(GuiGraphics graphics, int guiW, int guiH, float alpha) {
        if (isForeignOffscreenRender()
                || !UiSettings.fullscreenBlurEnabled()
                || shader == null
                || guiW <= 0
                || guiH <= 0
                || alpha <= 0.01f) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        int framebufferW = window.getWidth();
        int framebufferH = window.getHeight();
        if (framebufferW <= 0 || framebufferH <= 0) {
            return;
        }

        RenderSystem.assertOnRenderThread();
        if (!prepareBlurTexture(BLUR_STRENGTH)) {
            return;
        }

        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        RenderSystem.applyModelViewMatrix();

        try {
            minecraft.getMainRenderTarget().bindWrite(false);
            RenderSystem.viewport(0, 0, framebufferW, framebufferH);
            RenderSystem.setProjectionMatrix(previousProjection, VertexSorting.ORTHOGRAPHIC_Z);
            modelViewStack.setIdentity();
            RenderSystem.applyModelViewMatrix();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            renderFinal(graphics, lastOutputTexture, lastOutputW, lastOutputH, guiW, guiH, alpha);
        } finally {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
            minecraft.getMainRenderTarget().bindWrite(false);
            RenderSystem.viewport(0, 0, framebufferW, framebufferH);
            RenderSystem.setProjectionMatrix(previousProjection, VertexSorting.ORTHOGRAPHIC_Z);
            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    public static void renderSurfaceBackdrop(GuiGraphics graphics, int guiW, int guiH, float x, float y, float w, float h,
                                           float radius, float alpha) {
        renderSurfaceBackdrop(graphics, guiW, guiH, x, y, w, h, radius, alpha, SURFACE_BACKDROP_STRENGTH);
    }

    public static void renderSurfaceBackdrop(GuiGraphics graphics, int guiW, int guiH, float x, float y, float w, float h,
                                           float radius, float alpha, float strength) {
        if (shader == null
                || isForeignOffscreenRender()
                || guiW <= 0
                || guiH <= 0
                || w <= 0.0f
                || h <= 0.0f
                || alpha <= 0.01f) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        int framebufferW = window.getWidth();
        int framebufferH = window.getHeight();
        recaptureCurrentFrame();
        if (!prepareBlurTexture(strength) || lastOutputTexture == 0) {
            return;
        }

        if (x + w <= 0.0f || y + h <= 0.0f || x >= guiW || y >= guiH) {
            return;
        }
        float maskRadius = Math.max(0.0f, Math.min(radius, Math.min(w, h) * 0.5f));

        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        RenderSystem.applyModelViewMatrix();

        try {
            minecraft.getMainRenderTarget().bindWrite(false);
            RenderSystem.viewport(0, 0, framebufferW, framebufferH);
            RenderSystem.setProjectionMatrix(ortho(guiW, guiH), VertexSorting.ORTHOGRAPHIC_Z);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(() -> shader);
            shader.setSampler("uTexture", lastOutputTexture);
            shader.getUniform("uTexelSize").set(1.0f / lastOutputW, 1.0f / lastOutputH);
            shader.getUniform("uOffset").set(0.0f);
            shader.getUniform("uOpacity").set(SURFACE_BACKDROP_OPACITY * alpha);
            shader.getUniform("uFlipY").set(0.0f);
            shader.getUniform("uTint").set(1.0f, 1.0f, 1.0f, 0.0f);
            shader.getUniform("uUvRect").set(
                    x / guiW,
                    y / guiH,
                    (x + w) / guiW,
                    (y + h) / guiH
            );
            shader.getUniform("uQuadSize").set(w, h);
            shader.getUniform("uMaskRadius").set(maskRadius);
            drawQuad(new Matrix4f(), x, y, w, h);
        } finally {
            minecraft.getMainRenderTarget().bindWrite(false);
            RenderSystem.viewport(0, 0, framebufferW, framebufferH);
            RenderSystem.setProjectionMatrix(previousProjection, VertexSorting.ORTHOGRAPHIC_Z);
            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    private static boolean prepareBlurTexture(float strength) {
        if (isForeignOffscreenRender()) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        int framebufferW = window.getWidth();
        int framebufferH = window.getHeight();
        if (framebufferW <= 0 || framebufferH <= 0) {
            return false;
        }

        RenderSystem.assertOnRenderThread();
        ensureTargets(framebufferW, framebufferH);
        if (!framebufferReady) {
            return false;
        }
        if (preparedFrame == frameSerial && lastOutputTexture != 0 && Float.compare(preparedStrength, strength) == 0) {
            return true;
        }

        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        RenderSystem.applyModelViewMatrix();

        try {
            RenderSystem.disableBlend();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.setProjectionMatrix(ortho(blurW, blurH), VertexSorting.ORTHOGRAPHIC_Z);
            RenderSystem.applyModelViewMatrix();

            if (!captureSourceIfNeeded(minecraft, framebufferW, framebufferH)) {
                return false;
            }
            if (sourceTexture == 0) {
                return false;
            }
            renderPass(sourceTexture, sourceW, sourceH, pongFbo, blurW, blurH, 0.0f, false, 1.0f, 0.0f);

            int sourceTexture = pongTexture;
            int targetFbo = pingFbo;
            int outputTexture = pongTexture;

            float[] offsets = buildPassSequence(strength);
            for (float offset : offsets) {
                renderPass(sourceTexture, blurW, blurH, targetFbo, blurW, blurH, offset, false, 1.0f, 0.0f);
                if (targetFbo == pingFbo) {
                    sourceTexture = pingTexture;
                    outputTexture = pingTexture;
                    targetFbo = pongFbo;
                } else {
                    sourceTexture = pongTexture;
                    outputTexture = pongTexture;
                    targetFbo = pingFbo;
                }
            }
            lastOutputTexture = outputTexture;
            lastOutputW = blurW;
            lastOutputH = blurH;
            preparedFrame = frameSerial;
            preparedStrength = strength;
            return true;
        } finally {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
            minecraft.getMainRenderTarget().bindWrite(false);
            RenderSystem.viewport(0, 0, framebufferW, framebufferH);
            RenderSystem.setProjectionMatrix(previousProjection, VertexSorting.ORTHOGRAPHIC_Z);
            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    private static void recaptureCurrentFrame() {
        capturedFrame = -1;
        preparedFrame = -1;
    }

    private static void renderPass(int sourceTexture, int sourceW, int sourceH, int targetFbo, int targetW, int targetH, float offset, boolean flipY, float opacity, float tintAlpha) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, targetFbo);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        RenderSystem.viewport(0, 0, targetW, targetH);
        RenderSystem.setShader(() -> shader);
        shader.setSampler("uTexture", sourceTexture);
        shader.getUniform("uTexelSize").set(1.0f / sourceW, 1.0f / sourceH);
        shader.getUniform("uOffset").set(offset);
        shader.getUniform("uOpacity").set(opacity);
        shader.getUniform("uFlipY").set(flipY ? 1.0f : 0.0f);
        shader.getUniform("uTint").set(
                ((TINT_COLOR >> 16) & 0xFF) / 255.0f,
                ((TINT_COLOR >> 8) & 0xFF) / 255.0f,
                (TINT_COLOR & 0xFF) / 255.0f,
                tintAlpha
        );
        shader.getUniform("uUvRect").set(0.0f, 0.0f, 1.0f, 1.0f);
        shader.getUniform("uQuadSize").set((float) targetW, (float) targetH);
        shader.getUniform("uMaskRadius").set(0.0f);
        drawQuad(new Matrix4f(), 0.0f, 0.0f, targetW, targetH);
    }

    private static void renderFinal(GuiGraphics graphics, int sourceTexture, int sourceW, int sourceH, int guiW, int guiH, float alpha) {
        RenderSystem.setProjectionMatrix(ortho(guiW, guiH), VertexSorting.ORTHOGRAPHIC_Z);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShader(() -> shader);
        shader.setSampler("uTexture", sourceTexture);
        shader.getUniform("uTexelSize").set(1.0f / sourceW, 1.0f / sourceH);
        shader.getUniform("uOffset").set(0.0f);
        shader.getUniform("uOpacity").set(FULLSCREEN_BLUR_OPACITY * alpha);
        shader.getUniform("uFlipY").set(0.0f);
        shader.getUniform("uTint").set(
                ((TINT_COLOR >> 16) & 0xFF) / 255.0f,
                ((TINT_COLOR >> 8) & 0xFF) / 255.0f,
                (TINT_COLOR & 0xFF) / 255.0f,
                TINT_STRENGTH * alpha
        );
        shader.getUniform("uUvRect").set(0.0f, 0.0f, 1.0f, 1.0f);
        shader.getUniform("uQuadSize").set((float) guiW, (float) guiH);
        shader.getUniform("uMaskRadius").set(0.0f);
        drawQuad(new Matrix4f(), 0.0f, 0.0f, guiW, guiH);
    }

    private static void ensureTargets(int framebufferW, int framebufferH) {
        int targetBlurW = Math.max(1, Math.round(framebufferW * BLUR_RESOLUTION_SCALE));
        int targetBlurH = Math.max(1, Math.round(framebufferH * BLUR_RESOLUTION_SCALE));
        if (sourceTexture != 0
                && pingTexture != 0
                && sourceW == framebufferW
                && sourceH == framebufferH
                && blurW == targetBlurW
                && blurH == targetBlurH) {
            return;
        }
        releaseTargets();

        sourceW = framebufferW;
        sourceH = framebufferH;
        blurW = targetBlurW;
        blurH = targetBlurH;

        sourceTexture = createTexture(sourceW, sourceH);
        sourceFbo = createFramebuffer(sourceTexture);
        pingTexture = createTexture(blurW, blurH);
        pongTexture = createTexture(blurW, blurH);
        pingFbo = createFramebuffer(pingTexture);
        pongFbo = createFramebuffer(pongTexture);
        framebufferReady = sourceFbo != 0 && pingFbo != 0 && pongFbo != 0;
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        if (!framebufferReady) {
            releaseTargets();
        }
    }

    private static void copyMainTargetToSource(Minecraft minecraft, int framebufferW, int framebufferH) {
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, minecraft.getMainRenderTarget().frameBufferId);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, sourceFbo);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GL30.glBlitFramebuffer(
                0, 0, framebufferW, framebufferH,
                0, 0, sourceW, sourceH,
                GL11.GL_COLOR_BUFFER_BIT,
                GL11.GL_NEAREST
        );
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    private static boolean captureSourceIfNeeded(Minecraft minecraft, int framebufferW, int framebufferH) {
        if (capturedFrame == frameSerial && sourceTexture != 0) {
            return true;
        }
        copyMainTargetToSource(minecraft, framebufferW, framebufferH);
        capturedFrame = frameSerial;
        return sourceTexture != 0;
    }

    private static int createTexture(int w, int h) {
        int id = GL11.glGenTextures();
        RenderSystem.bindTexture(id);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0L);
        return id;
    }

    private static int createFramebuffer(int texture) {
        int fbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture, 0);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            GL30.glDeleteFramebuffers(fbo);
            return 0;
        }
        return fbo;
    }

    private static void releaseTargets() {
        framebufferReady = false;
        if (sourceFbo != 0) {
            GL30.glDeleteFramebuffers(sourceFbo);
            sourceFbo = 0;
        }
        if (pingFbo != 0) {
            GL30.glDeleteFramebuffers(pingFbo);
            pingFbo = 0;
        }
        if (pongFbo != 0) {
            GL30.glDeleteFramebuffers(pongFbo);
            pongFbo = 0;
        }
        if (pingTexture != 0) {
            GL11.glDeleteTextures(pingTexture);
            pingTexture = 0;
        }
        if (sourceTexture != 0) {
            GL11.glDeleteTextures(sourceTexture);
            sourceTexture = 0;
        }
        if (pongTexture != 0) {
            GL11.glDeleteTextures(pongTexture);
            pongTexture = 0;
        }
        sourceW = 0;
        sourceH = 0;
        lastOutputTexture = 0;
        lastOutputW = 0;
        lastOutputH = 0;
        preparedFrame = -1;
        capturedFrame = -1;
        preparedStrength = Float.NaN;
    }

    private static Matrix4f ortho(int w, int h) {
        return new Matrix4f().setOrtho(0.0f, w, h, 0.0f, 1000.0f, -1000.0f);
    }

    private static float[] buildPassSequence(float strength) {
        float normalized = Math.max(0.0f, Math.min(strength / 100.0f, 1.0f));
        if (normalized <= 0.0001f) {
            return new float[0];
        }
        if (normalized < 0.02f) {
            float t = normalized / 0.02f;
            normalized = 0.02f * t * t;
        }
        float radiusScale = normalized * 12.0f * BLUR_RESOLUTION_SCALE;
        float[] offsets = new float[PASS_COUNT];
        for (int i = 0; i < offsets.length; i++) {
            float passT = (i + 1.0f) / offsets.length;
            float passCurve = 0.35f + passT * 1.65f;
            offsets[i] = radiusScale * passCurve;
        }
        return offsets;
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static boolean isForeignOffscreenRender() {
        if (!watutLookupDone) {
            watutLookupDone = true;
            try {
                Class<?> renderHelper = Class.forName("com.corosus.watut.client.screen.RenderHelper");
                watutPerformingOwnRenderField = renderHelper.getDeclaredField("performingOwnRender");
                watutPerformingOwnRenderField.setAccessible(true);
            } catch (ReflectiveOperationException ignored) {
                watutPerformingOwnRenderField = null;
            }
        }
        if (watutPerformingOwnRenderField == null) {
            return false;
        }
        try {
            return watutPerformingOwnRenderField.getBoolean(null);
        } catch (IllegalAccessException ignored) {
            return false;
        }
    }

    private static void drawQuad(Matrix4f matrix, float x, float y, float w, float h) {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, x, y + h, 0.0f).uv(0.0f, 1.0f).endVertex();
        buffer.vertex(matrix, x + w, y + h, 0.0f).uv(1.0f, 1.0f).endVertex();
        buffer.vertex(matrix, x + w, y, 0.0f).uv(1.0f, 0.0f).endVertex();
        buffer.vertex(matrix, x, y, 0.0f).uv(0.0f, 0.0f).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }
}
