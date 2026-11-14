package com.xerion.forge.gui.dashboard;

import com.mojang.logging.LogUtils;
import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.OptimizeWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class XerionEditWorldScreen extends XerionSetupScreen {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final BooleanConsumer callback;
    private final LevelStorageSource.LevelStorageAccess levelAccess;
    private AlignedEditBox nameBox;
    private XerionMenuActionButton saveButton;
    private XerionMenuActionButton resetIconButton;

    public XerionEditWorldScreen(BooleanConsumer callback,
                                 LevelStorageSource.LevelStorageAccess levelAccess) {
        super(Component.translatable("selectWorld.edit.title"));
        this.callback = callback;
        this.levelAccess = levelAccess;
    }

    @Override
    protected void init() {
        layoutPanel(300, 330);
        int contentX = panelX + 20;
        int contentW = panelW - 40;
        int fieldY = panelY + HEADER_H + 28;

        LevelSummary summary = levelAccess.getSummary();
        String worldName = summary == null ? "" : summary.getLevelName();
        nameBox = new AlignedEditBox(font, contentX + 10, fieldY,
                Math.max(40, contentW - 20), 28,
                Component.translatable("selectWorld.enterName"), 8);
        nameBox.setBordered(false);
        nameBox.setTextColor(UiPalette.inputText());
        nameBox.setTextColorUneditable(UiPalette.muted());
        nameBox.setHint(Component.translatable("selectWorld.enterName"));
        nameBox.setValue(worldName == null ? "" : worldName);
        nameBox.setResponder(ignored -> updateSaveState());
        addWidget(nameBox);

        int gap = 6;
        int halfW = (contentW - gap) / 2;
        int actionY = fieldY + 43;
        resetIconButton = addRenderableWidget(new XerionMenuActionButton(
                contentX, actionY, halfW, 26,
                Component.translatable("selectWorld.edit.resetIcon"),
                FontAwesomeIcons.ICON_FA_IMAGE, XerionMenuActionButton.Style.NORMAL,
                this::resetIcon));
        addRenderableWidget(new XerionMenuActionButton(
                contentX + halfW + gap, actionY, contentW - halfW - gap, 26,
                Component.translatable("selectWorld.edit.openFolder"),
                FontAwesomeIcons.ICON_FA_FOLDER_OPEN, XerionMenuActionButton.Style.NORMAL,
                this::openWorldFolder));

        actionY += 32;
        addRenderableWidget(new XerionMenuActionButton(
                contentX, actionY, halfW, 26,
                Component.translatable("selectWorld.edit.backup"),
                FontAwesomeIcons.ICON_FA_BOX_ARCHIVE, XerionMenuActionButton.Style.NORMAL,
                this::createBackup));
        addRenderableWidget(new XerionMenuActionButton(
                contentX + halfW + gap, actionY, contentW - halfW - gap, 26,
                Component.translatable("selectWorld.edit.backupFolder"),
                FontAwesomeIcons.ICON_FA_FOLDER, XerionMenuActionButton.Style.NORMAL,
                this::openBackupFolder));

        actionY += 32;
        addRenderableWidget(new XerionMenuActionButton(
                contentX, actionY, contentW, 26,
                Component.translatable("selectWorld.edit.optimize"),
                FontAwesomeIcons.ICON_FA_GAUGE_HIGH, XerionMenuActionButton.Style.NORMAL,
                this::optimizeWorld));

        int buttonY = panelY + panelH - 40;
        int buttonW = Math.min(132, (panelW - 50) / 2);
        int right = panelX + panelW - 18;
        saveButton = addRenderableWidget(new XerionMenuActionButton(
                right - buttonW, buttonY, buttonW, 26,
                Component.translatable("selectWorld.edit.save"),
                FontAwesomeIcons.ICON_FA_FLOPPY_DISK,
                XerionMenuActionButton.Style.PRIMARY, this::save));
        addRenderableWidget(new XerionMenuActionButton(
                right - buttonW * 2 - gap, buttonY, buttonW, 26,
                Component.translatable("gui.cancel"), FontAwesomeIcons.ICON_FA_XMARK,
                XerionMenuActionButton.Style.NORMAL, this::cancel));

        resetIconButton.active = levelAccess.getIconFile()
                .filter(Files::isRegularFile)
                .isPresent();
        updateSaveState();
        setInitialFocus(nameBox);
    }

    private void updateSaveState() {
        if (saveButton != null && nameBox != null) {
            saveButton.active = !nameBox.getValue().trim().isEmpty();
        }
    }

    private void resetIcon() {
        levelAccess.getIconFile().ifPresent(path -> FileUtils.deleteQuietly(path.toFile()));
        resetIconButton.active = false;
    }

    private void openWorldFolder() {
        Util.getPlatform().openFile(levelAccess.getLevelPath(LevelResource.ROOT).toFile());
    }

    private void createBackup() {
        boolean created = EditWorldScreen.makeBackupAndShowToast(levelAccess);
        callback.accept(!created);
    }

    private void openBackupFolder() {
        Path backupPath = minecraft.getLevelSource().getBackupPath();
        try {
            FileUtil.createDirectoriesSafe(backupPath);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        Util.getPlatform().openFile(backupPath.toFile());
    }

    private void optimizeWorld() {
        minecraft.setScreen(new BackupConfirmScreen(this, (backup, eraseCache) -> {
            if (backup) {
                EditWorldScreen.makeBackupAndShowToast(levelAccess);
            }
            minecraft.setScreen(OptimizeWorldScreen.create(minecraft, callback,
                    minecraft.getFixerUpper(), levelAccess, eraseCache));
        }, Component.translatable("optimizeWorld.confirm.title"),
                Component.translatable("optimizeWorld.confirm.description"), true));
    }

    private void save() {
        try {
            levelAccess.renameLevel(nameBox.getValue().trim());
            callback.accept(true);
        } catch (IOException exception) {
            LOGGER.error("Failed to access world '{}'", levelAccess.getLevelId(), exception);
            SystemToast.onWorldAccessFailure(minecraft, levelAccess.getLevelId());
            callback.accept(true);
        }
    }

    private void cancel() {
        callback.accept(false);
    }

    @Override
    public void tick() {
        nameBox.tick();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String currentName = nameBox == null ? "" : nameBox.getValue();
        init(minecraft, width, height);
        nameBox.setValue(currentName);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float alpha = beginXerionRender(graphics);
        LevelSummary summary = levelAccess.getSummary();
        String subtitle = summary == null ? levelAccess.getLevelId() : summary.getLevelId();
        drawHeader(graphics, Component.literal(subtitle), alpha);

        int fieldX = panelX + 20;
        int fieldY = panelY + HEADER_H + 28;
        int fieldW = panelW - 40;
        MSDFRenderer.draw(graphics, Component.translatable("selectWorld.enterName").getString(),
                fieldX, fieldY - 13, 7.0f, UiColor.alpha(UiPalette.muted(), alpha));
        ShapeRenderer.roundRect(graphics, fieldX, fieldY, fieldW, 28, 6,
                UiColor.alpha(UiPalette.field(), alpha * 0.88f));
        ShapeRenderer.roundRectBorder(graphics, fieldX, fieldY, fieldW, 28, 6, 1,
                UiColor.alpha(nameBox.isFocused() ? UiPalette.accent() : UiPalette.border(), alpha));

        drawFooterDivider(graphics, alpha);
        nameBox.setAlpha(alpha);
        nameBox.render(graphics, mouseX, mouseY, partialTick);
        prepareWidgets(alpha);
        for (var renderable : renderables) {
            if (renderable instanceof XerionMenuActionButton button) {
                button.render(graphics, mouseX, mouseY, partialTick);
            }
        }
        finishXerionRender();
    }

    @Override
    public void onClose() {
        cancel();
    }
}
