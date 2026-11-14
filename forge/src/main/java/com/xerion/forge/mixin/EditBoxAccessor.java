package com.xerion.forge.mixin;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(EditBox.class)
public interface EditBoxAccessor {
    @Accessor("f_94100_")
    int xerion$getDisplayPos();

    @Accessor("f_94101_")
    int xerion$getCursorPos();

    @Accessor("f_94102_")
    int xerion$getHighlightPos();

    @Accessor("f_94095_")
    int xerion$getFrame();

    @Accessor("f_94094_")
    int xerion$getMaxLength();

    @Accessor("f_94098_")
    boolean xerion$isEditable();

    @Accessor("f_94103_")
    int xerion$getTextColor();

    @Accessor("f_94104_")
    int xerion$getTextColorUneditable();

    @Nullable
    @Accessor("f_256828_")
    Component xerion$getHint();
}
