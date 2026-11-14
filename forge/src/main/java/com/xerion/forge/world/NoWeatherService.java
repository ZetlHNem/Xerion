package com.xerion.forge.world;

import com.xerion.common.world.NoWeatherRules;
import com.xerion.forge.config.VisualSettings;

public final class NoWeatherService {
    private NoWeatherService() {
    }

    public static boolean hidePrecipitation() {
        return NoWeatherRules.hidePrecipitation(
                VisualSettings.noWeatherEnabled(),
                VisualSettings.noWeatherRain(),
                VisualSettings.noWeatherSnow());
    }

    public static boolean hideAllPrecipitation() {
        return NoWeatherRules.hideAllPrecipitation(
                VisualSettings.noWeatherEnabled(),
                VisualSettings.noWeatherRain(),
                VisualSettings.noWeatherSnow());
    }

    public static boolean hideRain() {
        return NoWeatherRules.hideRain(VisualSettings.noWeatherEnabled(), VisualSettings.noWeatherRain());
    }

    public static boolean hideSnow() {
        return NoWeatherRules.hideSnow(VisualSettings.noWeatherEnabled(), VisualSettings.noWeatherSnow());
    }

    public static boolean hideThunder() {
        return NoWeatherRules.hideThunder(VisualSettings.noWeatherEnabled(), VisualSettings.noWeatherThunder());
    }
}