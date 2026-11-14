package com.xerion.common;

public final class XerionCoreHolder {
    private static final XerionCore INSTANCE = new XerionCore();

    private XerionCoreHolder() {
    }

    public static XerionCore get() {
        return INSTANCE;
    }

    public static void initialize() {
        INSTANCE.initialize();
    }
}
