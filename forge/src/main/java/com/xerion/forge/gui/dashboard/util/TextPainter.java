package com.xerion.forge.gui.dashboard;

import com.xerion.forge.gui.font.MSDFRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class TextPainter {
    private TextPainter() {
    }

    public static void draw(GuiGraphics graphics, String value, int x, int y, int color, float alpha) {
        draw(graphics, value, x, y, 12.0f, color, alpha);
    }

    public static void draw(GuiGraphics graphics, String value, int x, int y, float size, int color, float alpha) {
        if (value == null || value.isEmpty() || size <= 0.0f) {
            return;
        }
        MSDFRenderer.draw(graphics, value, x, y, size, UiColor.alpha(color, alpha));
    }

    public static void drawClipped(GuiGraphics graphics, String value, int x, int y, int maxWidth,
                                   float size, int color, float alpha) {
        drawClippedInternal(graphics, value, x, y, maxWidth, size, color, alpha);
    }

    public static void drawFormattedClipped(GuiGraphics graphics, String value, int x, int y,
                                            int maxWidth, float size, int color, float alpha) {
        if (value == null || value.isEmpty() || maxWidth <= 0 || size <= 0.0f) {
            return;
        }

        drawFormattedRunsClipped(graphics, parseFormatting(value, color), x, y, maxWidth, size, color, alpha);
    }

    public static void drawFormattedClipped(GuiGraphics graphics, Component value, int x, int y,
                                            int maxWidth, float size, int color, float alpha) {
        if (value == null || value.getString().isEmpty() || maxWidth <= 0 || size <= 0.0f) {
            return;
        }
        drawFormattedRunsClipped(graphics, parseFormatting(value, color), x, y, maxWidth, size, color, alpha);
    }

    private static void drawFormattedRunsClipped(GuiGraphics graphics, List<StyledRun> runs,
                                                 int x, int y, int maxWidth, float size,
                                                 int color, float alpha) {
        String ellipsis = "...";
        float available = Math.max(0.0f, maxWidth - MSDFRenderer.width(ellipsis, size));
        List<StyledRun> visible = new ArrayList<>();
        float visibleWidth = 0.0f;
        boolean truncated = false;
        StyledRun lastRun = new StyledRun("", color, false, false, false, false, false);

        for (StyledRun run : runs) {
            StringBuilder accepted = new StringBuilder();
            for (int offset = 0; offset < run.text().length();) {
                int codePoint = run.text().codePointAt(offset);
                String glyph = new String(Character.toChars(codePoint));
                float glyphWidth = styledWidth(glyph, size, run.bold());
                if (visibleWidth + glyphWidth > available) {
                    truncated = true;
                    break;
                }
                accepted.append(glyph);
                visibleWidth += glyphWidth;
                offset += Character.charCount(codePoint);
            }
            if (accepted.length() > 0) {
                StyledRun acceptedRun = run.withText(accepted.toString());
                visible.add(acceptedRun);
                lastRun = acceptedRun;
            }
            if (truncated) {
                break;
            }
        }

        float drawX = x;
        for (StyledRun run : visible) {
            drawStyledRun(graphics, run, drawX, y, size, alpha);
            drawX += styledWidth(run.text(), size, run.bold());
        }
        if (truncated) {
            drawStyledRun(graphics, lastRun.withText(ellipsis), drawX, y, size, alpha);
        }
    }

    public static void drawFormatted(GuiGraphics graphics, String value, float x, int y,
                                     float size, int color, float alpha) {
        int width = Math.max(1, Math.round(formattedWidth(value, size)) + 4);
        drawFormattedClipped(graphics, value, Math.round(x), y, width, size, color, alpha);
    }

    public static void drawFormatted(GuiGraphics graphics, Component value, float x, int y,
                                     float size, int color, float alpha) {
        int width = Math.max(1, Math.round(formattedWidth(value, size)) + 4);
        drawFormattedClipped(graphics, value, Math.round(x), y, width, size, color, alpha);
    }

    public static int formattedWidth(String value, float size) {
        if (value == null || value.isEmpty() || size <= 0.0f) {
            return 0;
        }
        return (int) Math.round(parseFormatting(value, 0xFFFFFFFF).stream()
                .mapToDouble(run -> styledWidth(run.text(), size, run.bold()))
                .sum());
    }

    public static int formattedWidth(Component value, float size) {
        if (value == null || value.getString().isEmpty() || size <= 0.0f) {
            return 0;
        }
        return (int) Math.round(parseFormatting(value, 0xFFFFFFFF).stream()
                .mapToDouble(run -> styledWidth(run.text(), size, run.bold()))
                .sum());
    }

    private static void drawStyledRun(GuiGraphics graphics, StyledRun run, float x, int y,
                                      float size, float alpha) {
        if (run.text().isEmpty()) {
            return;
        }
        String text = run.obfuscated() ? obfuscate(run.text()) : run.text();
        int drawColor = UiColor.alpha(run.color(), alpha);
        MSDFRenderer.drawStyled(graphics, text, x, y, size, drawColor, run.bold(), run.italic());

        float textWidth = styledWidth(text, size, run.bold());
        if (run.underlined() || run.strikethrough()) {
            int thickness = MSDFRenderer.textDecorationThickness(size);
            int baseline = Math.round(MSDFRenderer.textBaselineY(y, size, run.bold()));
            if (run.underlined()) {
                int lineY = baseline + Math.max(1, Math.round(size * 0.075f));
                graphics.fill(Math.round(x), lineY, Math.round(x + textWidth), lineY + thickness, drawColor);
            }
            if (run.strikethrough()) {
                int lineY = baseline - Math.max(1, Math.round(size * 0.28f));
                graphics.fill(Math.round(x), lineY, Math.round(x + textWidth), lineY + thickness, drawColor);
            }
        }
    }

    private static float styledWidth(String text, float size, boolean bold) {
        return bold ? MSDFRenderer.boldWidth(text, size) : MSDFRenderer.width(text, size);
    }

    private static String obfuscate(String text) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder(text.length());
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int offset = 0; offset < text.length();) {
            int codePoint = text.codePointAt(offset);
            if (Character.isWhitespace(codePoint)) {
                result.appendCodePoint(codePoint);
            } else {
                result.append(alphabet.charAt(random.nextInt(alphabet.length())));
            }
            offset += Character.charCount(codePoint);
        }
        return result.toString();
    }

    public static void drawUnicode(GuiGraphics graphics, String value, int x, int y,
                                   float size, int color, float alpha) {
        draw(graphics, value, x, y, size, color, alpha);
    }

    public static void drawUnicodeClipped(GuiGraphics graphics, String value, int x, int y, int maxWidth,
                                          float size, int color, float alpha) {
        drawClippedInternal(graphics, value, x, y, maxWidth, size, color, alpha);
    }

    public static void drawMsdf(GuiGraphics graphics, String value, int x, int y,
                                float size, int color, float alpha) {
        draw(graphics, value, x, y, size, color, alpha);
    }

    public static void drawMsdfClipped(GuiGraphics graphics, String value, int x, int y, int maxWidth,
                                       float size, int color, float alpha) {
        drawClippedInternal(graphics, value, x, y, maxWidth, size, color, alpha);
    }

    public static int msdfWidth(String value, float size) {
        return Math.round(MSDFRenderer.width(value, size));
    }

    public static int unicodeWidth(String value, float size) {
        return msdfWidth(value, size);
    }

    public static int width(String value) {
        return MSDFRenderer.width(value);
    }

    public static int width(String value, float size) {
        return msdfWidth(value, size);
    }

    private static void drawClippedInternal(GuiGraphics graphics, String value, int x, int y, int maxWidth,
                                            float size, int color, float alpha) {
        if (value == null || value.isEmpty() || maxWidth <= 0 || size <= 0.0f) {
            return;
        }
        String text = value;
        while (!text.isEmpty() && msdfWidth(text, size) > maxWidth) {
            text = text.substring(0, previousCodePointIndex(text, text.length()));
        }
        if (text.length() < value.length() && text.length() >= 3) {
            String suffix = "...";
            while (!text.isEmpty() && msdfWidth(text + suffix, size) > maxWidth) {
                text = text.substring(0, previousCodePointIndex(text, text.length()));
            }
            text += suffix;
        }
        draw(graphics, text, x, y, size, color, alpha);
    }

    private static List<StyledRun> parseFormatting(String value, int baseColor) {
        List<StyledRun> runs = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        FormattingState state = new FormattingState(baseColor);
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character == '\u00a7' && i + 1 < value.length()) {
                char code = Character.toLowerCase(value.charAt(++i));
                if (code == 'x') {
                    int hexColor = parseHexColor(value, i - 1);
                    if (hexColor >= 0) {
                        addRun(runs, text, state);
                        state.color = (baseColor & 0xFF000000) | hexColor;
                        i += 12;
                        continue;
                    }
                }
                Integer formattedColor = formattingColor(code);
                if (formattedColor != null) {
                    addRun(runs, text, state);
                    state.color = (baseColor & 0xFF000000) | formattedColor;
                    continue;
                }
                if (code == 'r') {
                    addRun(runs, text, state);
                    state.reset(baseColor);
                    continue;
                }
                if (isFormattingStyle(code)) {
                    addRun(runs, text, state);
                    state.apply(code);
                }
                continue;
            }
            text.append(character);
        }
        addRun(runs, text, state);
        return runs;
    }

    private static List<StyledRun> parseFormatting(Component value, int baseColor) {
        List<StyledRun> runs = new ArrayList<>();
        value.visit((style, text) -> {
            if (text.isEmpty()) {
                return Optional.empty();
            }
            TextColor textColor = style.getColor();
            int styledColor = textColor == null
                    ? baseColor
                    : (baseColor & 0xFF000000) | textColor.getValue();
            for (StyledRun run : parseFormatting(text, styledColor)) {
                runs.add(new StyledRun(run.text(), run.color(),
                        run.bold() || style.isBold(),
                        run.italic() || style.isItalic(),
                        run.underlined() || style.isUnderlined(),
                        run.strikethrough() || style.isStrikethrough(),
                        run.obfuscated() || style.isObfuscated()));
            }
            return Optional.empty();
        }, Style.EMPTY);
        return runs;
    }

    private static void addRun(List<StyledRun> runs, StringBuilder text, FormattingState state) {
        if (text.length() == 0) {
            return;
        }
        runs.add(new StyledRun(text.toString(), state.color, state.bold, state.italic,
                state.underlined, state.strikethrough, state.obfuscated));
        text.setLength(0);
    }

    private static int parseHexColor(String value, int markerIndex) {
        if (markerIndex < 0 || markerIndex + 13 >= value.length()) {
            return -1;
        }
        int color = 0;
        for (int i = 0; i < 6; i++) {
            int separator = markerIndex + 2 + i * 2;
            int digit = separator + 1;
            if (value.charAt(separator) != '\u00a7') {
                return -1;
            }
            int nibble = Character.digit(value.charAt(digit), 16);
            if (nibble < 0) {
                return -1;
            }
            color = (color << 4) | nibble;
        }
        return color;
    }

    private static boolean isFormattingStyle(char code) {
        return code == 'k' || code == 'l' || code == 'm' || code == 'n' || code == 'o';
    }

    private static Integer formattingColor(char code) {
        return switch (code) {
            case '0' -> 0x000000;
            case '1' -> 0x0000AA;
            case '2' -> 0x00AA00;
            case '3' -> 0x00AAAA;
            case '4' -> 0xAA0000;
            case '5' -> 0xAA00AA;
            case '6' -> 0xFFAA00;
            case '7' -> 0xAAAAAA;
            case '8' -> 0x555555;
            case '9' -> 0x5555FF;
            case 'a' -> 0x55FF55;
            case 'b' -> 0x55FFFF;
            case 'c' -> 0xFF5555;
            case 'd' -> 0xFF55FF;
            case 'e' -> 0xFFFF55;
            case 'f' -> 0xFFFFFF;
            default -> null;
        };
    }

    private record StyledRun(String text, int color, boolean bold, boolean italic,
                             boolean underlined, boolean strikethrough, boolean obfuscated) {
        private StyledRun withText(String replacement) {
            return new StyledRun(replacement, color, bold, italic, underlined, strikethrough, obfuscated);
        }
    }

    private static final class FormattingState {
        private int color;
        private boolean bold;
        private boolean italic;
        private boolean underlined;
        private boolean strikethrough;
        private boolean obfuscated;

        private FormattingState(int color) {
            reset(color);
        }

        private void reset(int color) {
            this.color = color;
            bold = false;
            italic = false;
            underlined = false;
            strikethrough = false;
            obfuscated = false;
        }

        private void apply(char code) {
            switch (code) {
                case 'k' -> obfuscated = true;
                case 'l' -> bold = true;
                case 'm' -> strikethrough = true;
                case 'n' -> underlined = true;
                case 'o' -> italic = true;
                default -> {
                }
            }
        }
    }

    private static int previousCodePointIndex(String value, int index) {
        return index <= 0 ? 0 : Character.offsetByCodePoints(value, index, -1);
    }
}
