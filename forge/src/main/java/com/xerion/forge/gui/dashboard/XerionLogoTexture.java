package com.xerion.forge.gui.dashboard;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class XerionLogoTexture {
    private static final int SDF_RANGE = 16;
    private static final int COLOR_DILATION_PASSES = SDF_RANGE + 2;
    private static final float DIAGONAL_DISTANCE = 1.41421356f;
    private static final float INFINITE_DISTANCE = 1_000_000.0f;
    private static final ResourceLocation SOURCE = ResourceLocation.fromNamespaceAndPath(
            "xerion", "textures/gui/brand/xerion_logo.png");
    private static final ResourceLocation RUNTIME = ResourceLocation.fromNamespaceAndPath(
            "xerion", "textures/runtime/xerion_logo_stb");
    private static ResourceLocation loadedTexture;
    private static ShaderInstance shader;
    private static int textureWidth = 64;
    private static int textureHeight = 64;

    private XerionLogoTexture() {
    }

    public static void init(ShaderInstance loadedShader) {
        shader = loadedShader;
    }

    public static void draw(GuiGraphics graphics, float x, float y,
                            float width, float height, float alpha) {
        ResourceLocation texture = ensureLoaded();
        if (texture.equals(RUNTIME) && shader != null
                && drawSdf(graphics, x, y, width, height, alpha)) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0f);
        graphics.setColor(1.0f, 1.0f, 1.0f, UiMath.clamp01(alpha));
        graphics.blit(texture, 0, 0, Math.round(width), Math.round(height),
                0.0f, 0.0f, textureWidth, textureHeight, textureWidth, textureHeight);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.pose().popPose();
    }

    private static boolean drawSdf(GuiGraphics graphics, float x, float y,
                                   float width, float height, float alpha) {
        if (width <= 0.0f || height <= 0.0f || alpha <= 0.0f) {
            return false;
        }
        int textureId = Minecraft.getInstance().getTextureManager().getTexture(RUNTIME).getId();
        if (textureId <= 0) {
            return false;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(() -> shader);
        try {
            shader.setSampler("uTexture", textureId);
            shader.getUniform("uPxRange").set((float) SDF_RANGE);
            shader.getUniform("uAlpha").set(UiMath.clamp01(alpha));
            drawQuad(graphics.pose().last().pose(), x, y, width, height);
        } finally {
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
        return true;
    }

    private static void drawQuad(Matrix4f matrix, float x, float y, float width, float height) {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, x, y + height, 0.0f).uv(0.0f, 1.0f).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0.0f).uv(1.0f, 1.0f).endVertex();
        buffer.vertex(matrix, x + width, y, 0.0f).uv(1.0f, 0.0f).endVertex();
        buffer.vertex(matrix, x, y, 0.0f).uv(0.0f, 0.0f).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }

    private static ResourceLocation ensureLoaded() {
        if (loadedTexture != null) {
            return loadedTexture;
        }
        try (InputStream input = Minecraft.getInstance().getResourceManager().open(SOURCE)) {
            byte[] encodedBytes = input.readAllBytes();
            ByteBuffer encoded = MemoryUtil.memAlloc(encodedBytes.length);
            encoded.put(encodedBytes).flip();
            try (MemoryStack stack = MemoryStack.stackPush()) {
                var width = stack.mallocInt(1);
                var height = stack.mallocInt(1);
                var channels = stack.mallocInt(1);
                ByteBuffer pixels = STBImage.stbi_load_from_memory(encoded, width, height, channels, 4);
                if (pixels == null) {
                    throw new IOException("STB image decode failed: " + STBImage.stbi_failure_reason());
                }
                try {
                    textureWidth = width.get(0);
                    textureHeight = height.get(0);
                    NativeImage image = createNativeImage(pixels, textureWidth, textureHeight);
                    DynamicTexture texture = new DynamicTexture(image);
                    texture.upload();
                    RenderSystem.bindTexture(texture.getId());
                    GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
                    texture.setFilter(true, true);
                    Minecraft.getInstance().getTextureManager().register(RUNTIME, texture);
                    loadedTexture = RUNTIME;
                } finally {
                    STBImage.stbi_image_free(pixels);
                }
            } finally {
                MemoryUtil.memFree(encoded);
            }
        } catch (IOException | RuntimeException exception) {
            Minecraft.getInstance().getTextureManager().getTexture(SOURCE).setFilter(true, false);
            loadedTexture = SOURCE;
        }
        return loadedTexture;
    }

    private static NativeImage createNativeImage(ByteBuffer pixels, int width, int height) {
        int count = width * height;
        int[] red = new int[count];
        int[] green = new int[count];
        int[] blue = new int[count];
        int[] alpha = new int[count];
        boolean[] colored = new boolean[count];
        for (int i = 0; i < count; i++) {
            int offset = i * 4;
            red[i] = pixels.get(offset) & 0xFF;
            green[i] = pixels.get(offset + 1) & 0xFF;
            blue[i] = pixels.get(offset + 2) & 0xFF;
            alpha[i] = pixels.get(offset + 3) & 0xFF;
            colored[i] = alpha[i] > 0;
        }

        for (int pass = 0; pass < COLOR_DILATION_PASSES; pass++) {
            boolean[] nextColored = colored.clone();
            int[] nextRed = red.clone();
            int[] nextGreen = green.clone();
            int[] nextBlue = blue.clone();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int index = y * width + x;
                    if (colored[index]) {
                        continue;
                    }
                    int totalRed = 0;
                    int totalGreen = 0;
                    int totalBlue = 0;
                    int samples = 0;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            int sampleX = x + dx;
                            int sampleY = y + dy;
                            if ((dx == 0 && dy == 0) || sampleX < 0 || sampleY < 0
                                    || sampleX >= width || sampleY >= height) {
                                continue;
                            }
                            int sample = sampleY * width + sampleX;
                            if (!colored[sample]) {
                                continue;
                            }
                            totalRed += red[sample];
                            totalGreen += green[sample];
                            totalBlue += blue[sample];
                            samples++;
                        }
                    }
                    if (samples > 0) {
                        nextRed[index] = totalRed / samples;
                        nextGreen[index] = totalGreen / samples;
                        nextBlue[index] = totalBlue / samples;
                        nextColored[index] = true;
                    }
                }
            }
            red = nextRed;
            green = nextGreen;
            blue = nextBlue;
            colored = nextColored;
        }

        int[] sdfAlpha = createSignedDistanceAlpha(alpha, width, height);

        NativeImage image = new NativeImage(width, height, false);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                image.setPixelRGBA(x, y, packRgba(red[index], green[index], blue[index], sdfAlpha[index]));
            }
        }
        return image;
    }

    private static int[] createSignedDistanceAlpha(int[] sourceAlpha, int width, int height) {
        int count = width * height;
        boolean[] inside = new boolean[count];
        for (int index = 0; index < count; index++) {
            inside[index] = sourceAlpha[index] >= 128;
        }

        float[] distanceToInside = distanceTransform(inside, width, height);
        boolean[] outside = new boolean[count];
        for (int index = 0; index < count; index++) {
            outside[index] = !inside[index];
        }
        float[] distanceToOutside = distanceTransform(outside, width, height);

        int[] result = new int[count];
        for (int index = 0; index < count; index++) {
            float coverage = sourceAlpha[index] / 255.0f;
            float signedDistance = distanceToOutside[index] - distanceToInside[index];
            signedDistance += 2.0f * (coverage - (inside[index] ? 1.0f : 0.0f));
            float encoded = 0.5f + signedDistance / (2.0f * SDF_RANGE);
            result[index] = Math.round(UiMath.clamp01(encoded) * 255.0f);
        }
        return result;
    }

    private static float[] distanceTransform(boolean[] targets, int width, int height) {
        float[] distance = new float[targets.length];
        for (int index = 0; index < targets.length; index++) {
            distance[index] = targets[index] ? 0.0f : INFINITE_DISTANCE;
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                float value = distance[index];
                if (x > 0) {
                    value = Math.min(value, distance[index - 1] + 1.0f);
                }
                if (y > 0) {
                    value = Math.min(value, distance[index - width] + 1.0f);
                    if (x > 0) {
                        value = Math.min(value, distance[index - width - 1] + DIAGONAL_DISTANCE);
                    }
                    if (x + 1 < width) {
                        value = Math.min(value, distance[index - width + 1] + DIAGONAL_DISTANCE);
                    }
                }
                distance[index] = value;
            }
        }

        for (int y = height - 1; y >= 0; y--) {
            for (int x = width - 1; x >= 0; x--) {
                int index = y * width + x;
                float value = distance[index];
                if (x + 1 < width) {
                    value = Math.min(value, distance[index + 1] + 1.0f);
                }
                if (y + 1 < height) {
                    value = Math.min(value, distance[index + width] + 1.0f);
                    if (x > 0) {
                        value = Math.min(value, distance[index + width - 1] + DIAGONAL_DISTANCE);
                    }
                    if (x + 1 < width) {
                        value = Math.min(value, distance[index + width + 1] + DIAGONAL_DISTANCE);
                    }
                }
                distance[index] = value;
            }
        }
        return distance;
    }

    private static int packRgba(int red, int green, int blue, int alpha) {
        return (alpha & 0xFF) << 24
                | (blue & 0xFF) << 16
                | (green & 0xFF) << 8
                | red & 0xFF;
    }
}
