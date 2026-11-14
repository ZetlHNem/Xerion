package com.xerion.common.platform;

import java.io.InputStream;
import java.util.Optional;

public interface PlatformResources {
    Optional<InputStream> openResource(String namespace, String path);

    void registerReloadListener(String id, Runnable listener);
}