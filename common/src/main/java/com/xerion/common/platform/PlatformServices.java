package com.xerion.common.platform;

import java.util.Optional;

public final class PlatformServices {
    private static PlatformEvents events;
    private static PlatformInput input;
    private static PlatformRender render;
    private static PlatformConfig config;
    private static PlatformResources resources;
    private static PlatformNetworking networking;
    private static PlatformGame game;

    private PlatformServices() {
    }

    public static synchronized void registerEvents(PlatformEvents service) {
        events = service;
    }

    public static synchronized void registerInput(PlatformInput service) {
        input = service;
    }

    public static synchronized void registerRender(PlatformRender service) {
        render = service;
    }

    public static synchronized void registerConfig(PlatformConfig service) {
        config = service;
    }

    public static synchronized void registerResources(PlatformResources service) {
        resources = service;
    }

    public static synchronized void registerNetworking(PlatformNetworking service) {
        networking = service;
    }

    public static synchronized void registerGame(PlatformGame service) {
        game = service;
    }

    public static synchronized Optional<PlatformEvents> events() {
        return Optional.ofNullable(events);
    }

    public static synchronized Optional<PlatformInput> input() {
        return Optional.ofNullable(input);
    }

    public static synchronized Optional<PlatformRender> render() {
        return Optional.ofNullable(render);
    }

    public static synchronized Optional<PlatformConfig> config() {
        return Optional.ofNullable(config);
    }

    public static synchronized Optional<PlatformResources> resources() {
        return Optional.ofNullable(resources);
    }

    public static synchronized Optional<PlatformNetworking> networking() {
        return Optional.ofNullable(networking);
    }

    public static synchronized Optional<PlatformGame> game() {
        return Optional.ofNullable(game);
    }
}
