package com.xerion.common.platform;

import java.nio.file.Path;
import java.util.Properties;

public interface PlatformConfig {
    Path configDirectory();

    Properties loadProperties(String profileName);

    void saveProperties(String profileName, Properties properties, String comment);
}