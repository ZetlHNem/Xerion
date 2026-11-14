package com.xerion.forge.gui.dashboard;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.RealmsMainScreen;
import com.xerion.common.XerionCoreHolder;
import com.xerion.forge.XerionForge;
import com.xerion.forge.gui.ClickGuiScreen;
import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.ModListScreen;

import java.util.ArrayList;
import java.util.List;

public final class XerionTitleScreen extends Screen {
    private static final int PANEL_MAX_W = 282;
    private static final int PANEL_MIN_W = 238;
    private static final int BACKGROUND_W = 1536;
    private static final int BACKGROUND_H = 1024;
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            "xerion", "textures/gui/title/xerion_menu_background.png");
    private final List<XerionMenuButton> menuButtons = new ArrayList<>();
    private float screenAlpha;

    public XerionTitleScreen() {
        super(Component.literal("Xerion"));
    }

    @Override
    protected void init() {
        menuButtons.clear();
        minecraft.setConnectedToRealms(false);

        boolean compact = height < 380;
        int panelW = Math.max(PANEL_MIN_W, Math.min(PANEL_MAX_W, width - 36));
        int buttonW = panelW - 44;
        int buttonH = compact ? 22 : 26;
        int gap = compact ? 4 : 6;
        int x = 22;
        int menuY = compact ? 66 : Math.max(108, height / 2 - 82);
        int halfGap = 7;
        int halfW = (buttonW - halfGap) / 2;

        addMenuButton(x, menuY, buttonW, buttonH,
                Component.translatable("menu.singleplayer"), FontAwesomeIcons.ICON_FA_GAMEPAD, true,
                () -> minecraft.setScreen(new XerionWorldSelectionScreen(this)));
        menuY += buttonH + gap;
        XerionMenuButton multiplayer = addMenuButton(x, menuY, buttonW, buttonH,
                Component.translatable("menu.multiplayer"), FontAwesomeIcons.ICON_FA_SERVER, false,
                this::openMultiplayer);
        multiplayer.active = minecraft.allowsMultiplayer();
        menuY += buttonH + gap;
        addMenuButton(x, menuY, buttonW, buttonH,
                Component.translatable("xerion.menu.dashboard"), FontAwesomeIcons.ICON_FA_SLIDERS, false,
                () -> minecraft.setScreen(new ClickGuiScreen(XerionCoreHolder.get())));
        menuY += buttonH + gap;

        XerionMenuButton realms = addMenuButton(x, menuY, halfW, buttonH,
                Component.translatable("menu.online"), FontAwesomeIcons.ICON_FA_EARTH, false,
                () -> minecraft.setScreen(new RealmsMainScreen(this)));
        realms.active = minecraft.allowsMultiplayer();
        addMenuButton(x + halfW + halfGap, menuY, halfW, buttonH,
                Component.translatable("fml.menu.mods"), FontAwesomeIcons.ICON_FA_PUZZLE_PIECE, false,
                () -> minecraft.setScreen(new ModListScreen(this)));
        menuY += buttonH + gap;

        addMenuButton(x, menuY, halfW, buttonH,
                Component.translatable("menu.options"), FontAwesomeIcons.ICON_FA_GEAR, false,
                () -> minecraft.setScreen(new OptionsScreen(this, minecraft.options)));
        addMenuButton(x + halfW + halfGap, menuY, halfW, buttonH,
                Component.translatable("options.language"), FontAwesomeIcons.ICON_FA_LANGUAGE, false,
                () -> minecraft.setScreen(new LanguageSelectScreen(
                        this, minecraft.options, minecraft.getLanguageManager())));
        menuY += buttonH + gap;

        addMenuButton(x, menuY, halfW, buttonH,
                Component.translatable("options.accessibility.title"), FontAwesomeIcons.ICON_FA_UNIVERSAL_ACCESS, false,
                () -> minecraft.setScreen(new AccessibilityOptionsScreen(this, minecraft.options)));
        addMenuButton(x + halfW + halfGap, menuY, halfW, buttonH,
                Component.translatable("menu.quit"), FontAwesomeIcons.ICON_FA_POWER_OFF, false, minecraft::stop);
    }

    private XerionMenuButton addMenuButton(int x, int y, int w, int h, Component label,
                                            int icon, boolean primary, Runnable action) {
        XerionMenuButton button = new XerionMenuButton(x, y, w, h,
                label, icon, primary, action);
        menuButtons.add(button);
        return addRenderableWidget(button);
    }

    private void openMultiplayer() {
        Screen next = minecraft.options.skipMultiplayerWarning
                ? new XerionMultiplayerScreen(this)
                : new SafetyScreen(this);
        minecraft.setScreen(next);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        AnimationClock.beginFrame();
        KawaseBlurPipeline.beginFrame();
        screenAlpha = UiMath.lerp(screenAlpha, 1.0f, UiAnimationTimings.PANEL_FADE);
        float alpha = UiMath.clamp01(screenAlpha);
        drawBackgroundImage(graphics, width, height, alpha);

        int panelW = Math.max(PANEL_MIN_W, Math.min(PANEL_MAX_W, width - 36));
        graphics.flush();
        KawaseBlurPipeline.renderSurfaceBackdrop(
                graphics, width, height,
                0.0f, 0.0f, panelW + 14.0f, height,
                0.0f, alpha, 22.0f);
        graphics.fill(0, 0, width, height, UiColor.alpha(0xFF070A10, alpha * 0.36f));
        graphics.fill(0, 0, panelW + 14, height, UiColor.alpha(UiPalette.shell(), alpha * 0.30f));
        graphics.fill(panelW + 14, 0, panelW + 15, height,
                UiColor.alpha(UiPalette.shellBorder(), alpha * 0.80f));

        drawBrand(graphics, alpha);
        if (height >= 380) {
            drawSession(graphics, alpha);
        }
        drawBuildInfo(graphics, alpha);

        for (XerionMenuButton button : menuButtons) {
            button.setAlpha(alpha);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    static void drawBackgroundImage(GuiGraphics graphics, int screenWidth, int screenHeight, float alpha) {
        float screenAspect = screenWidth / (float) Math.max(1, screenHeight);
        float imageAspect = BACKGROUND_W / (float) BACKGROUND_H;
        int sourceX = 0;
        int sourceY = 0;
        int sourceW = BACKGROUND_W;
        int sourceH = BACKGROUND_H;
        if (screenAspect > imageAspect) {
            sourceH = Math.max(1, Math.round(BACKGROUND_W / screenAspect));
            sourceY = (BACKGROUND_H - sourceH) / 2;
        } else {
            sourceW = Math.max(1, Math.round(BACKGROUND_H * screenAspect));
            sourceX = (BACKGROUND_W - sourceW) / 2;
        }
        graphics.setColor(1.0f, 1.0f, 1.0f, alpha);
        graphics.blit(BACKGROUND, 0, 0, screenWidth, screenHeight,
                sourceX, sourceY, sourceW, sourceH, BACKGROUND_W, BACKGROUND_H);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawBrand(GuiGraphics graphics, float alpha) {
        boolean compact = height < 380;
        int x = 22;
        int y = compact ? 16 : 30;
        int logoSize = compact ? 32 : 40;
        int textX = x + logoSize + 10;
        XerionLogoTexture.draw(graphics, x, y, logoSize, logoSize, alpha);
        MSDFRenderer.drawDisplay(graphics, "Xerion", textX, y + (compact ? 0 : 1),
                compact ? 21.0f : 25.0f, UiColor.alpha(UiPalette.text(), alpha));
        drawText(graphics, "Minecraft Forge 1.20.1", textX, y + (compact ? 23 : 30),
                8.0f, UiPalette.muted(), alpha);
    }

    private void drawSession(GuiGraphics graphics, float alpha) {
        String username = minecraft.getUser().getName();
        int x = 22;
        int y = height - 44;
        MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_USER,
                x, y + 1, 9.0f, UiColor.alpha(UiPalette.accent(), alpha));
        drawText(graphics, username, x + 17, y, 9.0f, UiPalette.text(), alpha);
        drawText(graphics, Component.translatable("xerion.menu.local_session").getString(),
                x + 17, y + 15, 7.0f, UiPalette.muted(), alpha);
    }

    private void drawBuildInfo(GuiGraphics graphics, float alpha) {
        String implementationVersion = XerionForge.class.getPackage().getImplementationVersion();
        String clientVersion = implementationVersion == null || implementationVersion.isBlank()
                ? "1.0.5" : implementationVersion;
        String line = "Xerion " + clientVersion + "  |  Minecraft "
                + SharedConstants.getCurrentVersion().getName() + "  |  Forge";
        int textW = Math.round(MSDFRenderer.width(line, 7.0f));
        drawText(graphics, line, Math.max(8, width - textW - 12), height - 18,
                7.0f, 0xFFE4E7EE, alpha * 0.82f);
    }

    private void drawText(GuiGraphics graphics, String value, float x, float y,
                          float size, int color, float alpha) {
        int resolvedColor = UiColor.alpha(color, alpha);
        MSDFRenderer.draw(graphics, value, x, y, size, resolvedColor);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private static final class XerionMenuButton extends AbstractButton {
        private final int icon;
        private final boolean primary;
        private final Runnable action;
        private float hoverProgress;

        private XerionMenuButton(int x, int y, int width, int height, Component message,
                                 int icon, boolean primary, Runnable action) {
            super(x, y, width, height, message);
            this.icon = icon;
            this.primary = primary;
            this.action = action;
        }

        @Override
        public void onPress() {
            action.run();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            hoverProgress = UiMath.lerp(hoverProgress,
                    isHoveredOrFocused() && active ? 1.0f : 0.0f,
                    UiAnimationTimings.CONTROL);
            float widgetAlpha = UiMath.clamp01(alpha) * (active ? 1.0f : 0.48f);
            int normalFill = UiColor.alpha(UiPalette.surface(), widgetAlpha * 0.78f);
            int hoverFill = UiColor.alpha(primary ? UiPalette.accent() : UiPalette.accentSoft(),
                    widgetAlpha * (primary ? 0.94f : 0.72f));
            int fill = UiColor.lerp(normalFill, hoverFill, primary ? 0.58f + hoverProgress * 0.42f : hoverProgress);
            int border = UiColor.lerp(UiPalette.border(), UiPalette.accent(), hoverProgress);
            ShapeRenderer.roundRect(graphics, getX(), getY(), getWidth(), getHeight(), 6, fill);
            ShapeRenderer.roundRectBorder(graphics, getX(), getY(), getWidth(), getHeight(),
                    6, 1, UiColor.alpha(border, widgetAlpha));
            if (!primary) {
                ShapeRenderer.roundRect(graphics, getX(), getY() + 6,
                        3, Math.max(1, getHeight() - 12), 2,
                        UiColor.alpha(UiPalette.accent(), widgetAlpha * hoverProgress));
            }

            float iconSize = getHeight() <= 22 ? 8.0f : 10.0f;
            float iconX = getX() + 11;
            float iconY = MSDFRenderer.iconCenteredY(icon, getY(), getHeight(), iconSize);
            int iconColor = primary ? 0xFFFFFFFF
                    : UiColor.lerp(UiPalette.muted(), UiPalette.accentDark(), hoverProgress);
            if (MSDFRenderer.renderResourcesReady()) {
                String label = getMessage().getString();
                float textSize = getHeight() <= 22 ? 7.5f : 8.5f;
                float minTextSize = getHeight() <= 22 ? 6.0f : 6.5f;
                int textMaxW = Math.max(12, getWidth() - 39);
                while (textSize > minTextSize && TextPainter.width(label, textSize) > textMaxW) {
                    textSize -= 0.25f;
                }
                float textY = MSDFRenderer.textCenteredY(getY(), getHeight(), textSize);
                MSDFRenderer.drawIconCodepoint(graphics, icon, iconX, iconY, iconSize,
                        UiColor.alpha(iconColor, widgetAlpha));
                TextPainter.drawClipped(graphics, label,
                        getX() + 31, Math.round(textY), textMaxW, textSize,
                        primary ? 0xFFFFFFFF : UiPalette.text(), widgetAlpha);
            } else {
                float textSize = getHeight() <= 22 ? 7.5f : 8.5f;
                int textMaxW = Math.max(12, getWidth() - 39);
                TextPainter.drawClipped(graphics, getMessage().getString(),
                        getX() + 31, Math.round(MSDFRenderer.textCenteredY(getY(), getHeight(), textSize)),
                        textMaxW, textSize, primary ? 0xFFFFFFFF : UiPalette.text(), widgetAlpha);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
