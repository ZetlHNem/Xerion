package com.xerion.common.config;

public final class WatermarkElementOrder {
    private WatermarkElementOrder() {
    }

    public static int[] normalize(String rawOrder) {
        int[] defaults = defaultOrder();
        boolean[] seen = new boolean[defaults.length];
        int[] result = new int[defaults.length];
        int count = 0;
        if (rawOrder != null && !rawOrder.isBlank()) {
            String[] parts = rawOrder.split(",");
            for (String part : parts) {
                int element = parseElement(part);
                int index = elementIndex(element);
                if (index >= 0 && !seen[index]) {
                    seen[index] = true;
                    result[count++] = element;
                }
            }
        }
        for (int element : defaults) {
            int index = elementIndex(element);
            if (index >= 0 && !seen[index]) {
                seen[index] = true;
                result[count++] = element;
            }
        }
        if (count == result.length) {
            return result;
        }
        int[] compact = new int[count];
        System.arraycopy(result, 0, compact, 0, count);
        return compact;
    }

    public static String serialize(int[] order) {
        int[] defaults = defaultOrder();
        boolean[] seen = new boolean[defaults.length];
        StringBuilder builder = new StringBuilder();
        if (order != null) {
            for (int element : order) {
                int index = elementIndex(element);
                if (index >= 0 && !seen[index]) {
                    appendElement(builder, element);
                    seen[index] = true;
                }
            }
        }
        for (int element : defaults) {
            int index = elementIndex(element);
            if (index >= 0 && !seen[index]) {
                appendElement(builder, element);
                seen[index] = true;
            }
        }
        return builder.toString();
    }

    private static void appendElement(StringBuilder builder, int element) {
        if (!builder.isEmpty()) {
            builder.append(',');
        }
        builder.append(element);
    }

    private static int parseElement(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static int elementIndex(int element) {
        return switch (element) {
            case WatermarkElements.LOGO -> 0;
            case WatermarkElements.PLAYER -> 1;
            case WatermarkElements.CONFIG -> 2;
            case WatermarkElements.PING -> 3;
            case WatermarkElements.FPS -> 4;
            case WatermarkElements.SERVER -> 5;
            case WatermarkElements.TIME -> 6;
            default -> -1;
        };
    }

    private static int[] defaultOrder() {
        return new int[] {
                WatermarkElements.LOGO,
                WatermarkElements.PLAYER,
                WatermarkElements.CONFIG,
                WatermarkElements.PING,
                WatermarkElements.FPS,
                WatermarkElements.SERVER,
                WatermarkElements.TIME
        };
    }
}
