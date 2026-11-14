package com.xerion.common.game;

import java.util.List;
import java.util.Optional;

public interface GameWorld {
    long gameTime();

    int minBuildHeight();

    int maxBuildHeight();

    List<GameEntity> entities();

    Optional<GameEntity> entityById(int entityId);

    GameBlockState blockState(GameBlockPos pos);
}
