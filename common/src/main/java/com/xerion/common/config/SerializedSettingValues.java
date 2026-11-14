package com.xerion.common.config;

public final class SerializedSettingValues {
    private SerializedSettingValues() {
    }

    public static int intValue(String serialized, long key, int fallback) {
        String value = value(serialized, key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return (int) Long.parseLong(value, 16);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    public static String stringValue(String serialized, long key, String fallback) {
        String value = value(serialized, key);
        return value == null || value.isBlank() ? fallback : value;
    }

    public static boolean booleanValue(String serialized, long key, boolean fallback) {
        String value = value(serialized, key);
        return value == null || value.isBlank() ? fallback : Boolean.parseBoolean(value);
    }

    public static String putIntValue(String serialized, long key, int value) {
        return putStringValue(serialized, key, String.format(java.util.Locale.ROOT, "%08X", value));
    }

    public static String putBooleanValue(String serialized, long key, boolean value) {
        return putStringValue(serialized, key, Boolean.toString(value));
    }

    public static String putStringValue(String serialized, long key, String value) {
        String keyText = Long.toUnsignedString(key);
        StringBuilder builder = new StringBuilder();
        boolean written = false;
        if (serialized != null && !serialized.isBlank()) {
            String[] entries = serialized.split("\\|");
            for (String entry : entries) {
                int split = entry.indexOf('=');
                if (split <= 0) {
                    continue;
                }
                String existingKey = entry.substring(0, split);
                String nextValue = keyText.equals(existingKey) ? value : entry.substring(split + 1);
                if (keyText.equals(existingKey)) {
                    written = true;
                }
                if (builder.length() > 0) {
                    builder.append('|');
                }
                builder.append(existingKey).append('=').append(nextValue);
            }
        }
        if (!written) {
            if (builder.length() > 0) {
                builder.append('|');
            }
            builder.append(keyText).append('=').append(value == null ? "" : value);
        }
        return builder.toString();
    }

    private static String value(String serialized, long key) {
        if (serialized == null || serialized.isBlank()) {
            return null;
        }
        String keyText = Long.toUnsignedString(key);
        String[] entries = serialized.split("\\|");
        for (String entry : entries) {
            int split = entry.indexOf('=');
            if (split <= 0) {
                continue;
            }
            if (keyText.equals(entry.substring(0, split))) {
                return entry.substring(split + 1);
            }
        }
        return null;
    }
}
