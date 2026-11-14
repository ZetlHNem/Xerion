package com.xerion.common.combat;

import java.util.Locale;
import java.util.UUID;

public final class AntiBotRules {
    private static final UUID ZERO_UUID = new UUID(0L, 0L);

    private AntiBotRules() {
    }

    public static boolean invalidProfile(String name, UUID uuid) {
        return name == null
                || name.isBlank()
                || name.length() > 16
                || uuid == null
                || ZERO_UUID.equals(uuid);
    }

    public static String normalizeName(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean sameUuid(UUID checkedUuid, UUID uuid) {
        return checkedUuid != null && checkedUuid.equals(uuid);
    }

    public static boolean sameNormalizedName(String checkedName, String name) {
        return checkedName != null && !checkedName.isEmpty() && checkedName.equals(name);
    }

    public static boolean duplicateDetected(int sameUuid, int sameName) {
        return sameUuid > 1 || sameName > 1;
    }

    public static boolean zeroPing(int latency) {
        return latency <= 0;
    }
}
