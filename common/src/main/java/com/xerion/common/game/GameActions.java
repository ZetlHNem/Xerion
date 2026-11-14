package com.xerion.common.game;

public interface GameActions {
    void attackEntity(int entityId);

    void useSelectedItem();

    void swapToHotbarSlot(int slot);

    void clickInventorySlot(int slot, int button, ClickType type);

    void rotate(GameRotation rotation);

    void setMotion(GameVec3 motion);

    enum ClickType {
        PICKUP,
        QUICK_MOVE,
        SWAP,
        THROW,
        QUICK_CRAFT,
        PICKUP_ALL
    }
}
