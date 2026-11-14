package com.xerion.common.game;

import java.util.List;
import java.util.Optional;

public interface GameInventory {
    List<GameItemStack> slots();

    int selectedHotbarSlot();

    Optional<GameItemStack> slot(int index);

    Optional<GameItemStack> offhand();
}
