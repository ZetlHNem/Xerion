package com.xerion.common.game;

public record GameBlockState(
        String blockId,
        boolean air,
        boolean liquid,
        boolean replaceable,
        float hardness
) {
}
