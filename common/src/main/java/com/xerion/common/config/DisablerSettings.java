package com.xerion.common.config;

public final class DisablerSettings {
    public static final String MODE_HEYPIXEL = "Heypixel";
    public static final String MODE_CUBECRAFT = "CubeCraft";
    public static final String MODE_MINIBLOX = "MiniBlox";

    private static boolean enabled;
    private static String mode = MODE_HEYPIXEL;
    private static boolean debug;
    private static boolean onlyRemoteServer;
    private static boolean logging;

    // Heypixel options.
    private static boolean heypixelGrimBadPacketsA = true;
    private static boolean heypixelGrimDuplicateRotPlace = true;
    private static boolean heypixelAcaFastSwitch = true;
    private static boolean heypixelAcaInventoryFrequency;
    private static boolean heypixelAcaAimStep = true;
    private static boolean heypixelAcaPerfectRotation = true;
    private static boolean heypixelThemisBlink = true;

    private DisablerSettings() {
    }

    public static boolean enabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static String mode() {
        return mode;
    }

    public static void setMode(String value) {
        if (MODE_CUBECRAFT.equals(value) || MODE_MINIBLOX.equals(value)) {
            mode = value;
        } else {
            mode = MODE_HEYPIXEL;
        }
    }

    public static boolean debug() {
        return debug;
    }

    public static void setDebug(boolean value) {
        debug = value;
    }

    public static boolean onlyRemoteServer() {
        return onlyRemoteServer;
    }

    public static void setOnlyRemoteServer(boolean value) {
        onlyRemoteServer = value;
    }

    public static boolean logging() {
        return logging;
    }

    public static void setLogging(boolean value) {
        logging = value;
    }

    public static boolean heypixelGrimBadPacketsA() {
        return heypixelGrimBadPacketsA;
    }

    public static void setHeypixelGrimBadPacketsA(boolean value) {
        heypixelGrimBadPacketsA = value;
    }

    public static boolean heypixelGrimDuplicateRotPlace() {
        return heypixelGrimDuplicateRotPlace;
    }

    public static void setHeypixelGrimDuplicateRotPlace(boolean value) {
        heypixelGrimDuplicateRotPlace = value;
    }

    public static boolean heypixelAcaFastSwitch() {
        return heypixelAcaFastSwitch;
    }

    public static void setHeypixelAcaFastSwitch(boolean value) {
        heypixelAcaFastSwitch = value;
    }

    public static boolean heypixelAcaInventoryFrequency() {
        return heypixelAcaInventoryFrequency;
    }

    public static void setHeypixelAcaInventoryFrequency(boolean value) {
        heypixelAcaInventoryFrequency = value;
    }

    public static boolean heypixelAcaAimStep() {
        return heypixelAcaAimStep;
    }

    public static void setHeypixelAcaAimStep(boolean value) {
        heypixelAcaAimStep = value;
    }

    public static boolean heypixelAcaPerfectRotation() {
        return heypixelAcaPerfectRotation;
    }

    public static void setHeypixelAcaPerfectRotation(boolean value) {
        heypixelAcaPerfectRotation = value;
    }

    public static boolean heypixelThemisBlink() {
        return heypixelThemisBlink;
    }

    public static void setHeypixelThemisBlink(boolean value) {
        heypixelThemisBlink = value;
    }
}
