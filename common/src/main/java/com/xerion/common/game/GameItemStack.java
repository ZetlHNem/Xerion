package com.xerion.common.game;

import java.util.Set;

public record GameItemStack(
        String itemId,
        int count,
        int maxCount,
        int damage,
        int maxDamage,
        Set<String> tags
) {
    public boolean empty() {
        return count <= 0 || itemId == null || itemId.isBlank();
    }

    public boolean damaged() {
        return maxDamage > 0 && damage > 0;
    }
}
