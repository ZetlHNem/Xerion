package com.xerion.common.game;

import java.util.Optional;

public interface GameClient {
    Optional<GamePlayer> player();

    Optional<GameWorld> world();

    Optional<GameScreen> screen();

    GameInput input();

    GameRenderer renderer();

    GameNetwork network();

    GameActions actions();
}
