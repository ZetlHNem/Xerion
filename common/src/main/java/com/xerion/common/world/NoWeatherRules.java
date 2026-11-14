package com.xerion.common.world;

public final class NoWeatherRules {
    private NoWeatherRules() {
    }

    public static boolean hidePrecipitation(boolean enabled, boolean rain, boolean snow) {
        return enabled && (rain || snow);
    }

    public static boolean hideAllPrecipitation(boolean enabled, boolean rain, boolean snow) {
        return enabled && rain && snow;
    }

    public static boolean hideRain(boolean enabled, boolean rain) {
        return enabled && rain;
    }

    public static boolean hideSnow(boolean enabled, boolean snow) {
        return enabled && snow;
    }

    public static boolean hideThunder(boolean enabled, boolean thunder) {
        return enabled && thunder;
    }
}