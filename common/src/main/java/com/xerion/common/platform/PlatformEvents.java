package com.xerion.common.platform;

public interface PlatformEvents {
    Registration registerClientTick(TickPhase phase, Runnable handler);

    Registration registerHudRender(Runnable handler);

    Registration registerWorldRender(Runnable handler);

    enum TickPhase {
        START,
        END
    }

    interface Registration extends AutoCloseable {
        @Override
        void close();
    }
}