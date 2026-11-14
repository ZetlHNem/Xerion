package com.xerion.common.event;

@FunctionalInterface
public interface Subscription extends AutoCloseable {
    @Override
    void close();
}
