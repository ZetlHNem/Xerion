package com.xerion.forge.config;

import com.xerion.common.config.ConfigMigrationRules;

import com.xerion.forge.misc.*;

import com.xerion.forge.render.*;

import com.xerion.forge.world.*;

import com.xerion.forge.player.*;

import com.xerion.forge.movement.*;

import com.xerion.forge.combat.*;

import com.xerion.forge.*;

import com.xerion.forge.runtime.*;

import com.xerion.forge.event.*;

import com.xerion.forge.util.*;

import net.minecraft.client.Minecraft;
import com.xerion.forge.gui.dashboard.WidgetSurface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

public final class ProfileConfigStore {
    private static final String DEFAULT_PROFILE = "default";
    private static final String STATE_FILE = "state.properties";
    private static final String LAST_LOADED_KEY = "lastLoadedProfile";

    private static final List<Entry> ENTRIES = discoverEntries();

    private ProfileConfigStore() {
    }

    public record ProfileInfo(String name, long modifiedMillis) {
    }

    public static Path directory() {
        Minecraft minecraft = Minecraft.getInstance();
        Path gameDir = minecraft != null && minecraft.gameDirectory != null
                ? minecraft.gameDirectory.toPath()
                : Path.of(".");
        return rootDirectory().resolve("profiles");
    }

    public static Path rootDirectory() {
        Minecraft minecraft = Minecraft.getInstance();
        Path gameDir = minecraft != null && minecraft.gameDirectory != null
                ? minecraft.gameDirectory.toPath()
                : Path.of(".");
        return gameDir.resolve("config").resolve("xerion");
    }

    public static List<String> listConfigs() {
        ensureDefaultProfile();
        return listProfiles().stream()
                .map(ProfileInfo::name)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public static List<ProfileInfo> listProfiles() {
        ensureDefaultProfile();
        try {
            Path dir = directory();
            if (!Files.isDirectory(dir)) {
                return List.of();
            }
            try (var stream = Files.list(dir)) {
                return stream
                        .filter(Files::isRegularFile)
                        .map(path -> path.getFileName().toString())
                        .filter(name -> name.endsWith(ConfigSchema.PROFILE_EXTENSION))
                        .map(name -> profileInfo(dir.resolve(name),
                                name.substring(0, name.length() - ConfigSchema.PROFILE_EXTENSION.length())))
                        .sorted(Comparator.comparingLong(ProfileInfo::modifiedMillis).reversed()
                                .thenComparing(ProfileInfo::name, String.CASE_INSENSITIVE_ORDER))
                        .toList();
            }
        } catch (IOException ignored) {
            return List.of();
        }
    }

    private static ProfileInfo profileInfo(Path path, String name) {
        try {
            return new ProfileInfo(name, Files.getLastModifiedTime(path).toMillis());
        } catch (IOException ignored) {
            return new ProfileInfo(name, 0L);
        }
    }

    public static int save(String name) throws IOException {
        String safeName = sanitizeName(name);
        if (isProtectedDefault(safeName)) {
            throw new IOException("Default config is protected");
        }
        return saveInternal(safeName);
    }

    private static int saveInternal(String safeName) throws IOException {
        Files.createDirectories(directory());
        Properties properties = new Properties();
        properties.setProperty(ConfigSchema.VERSION_KEY, Integer.toString(ConfigSchema.CURRENT_VERSION));
        int savedEntries = 0;
        for (Entry entry : ENTRIES) {
            Object value = entry.read();
            if (value != null) {
                properties.setProperty(entry.key(), valueToString(value));
                savedEntries++;
            }
        }
        int hotkeyCount = WidgetSurface.saveHotkeys(properties, ConfigSchema.HOTKEYS_PREFIX);
        try (OutputStream output = Files.newOutputStream(configPath(safeName))) {
            properties.store(output, ConfigSchema.FILE_COMMENT);
        }
        return savedEntries + hotkeyCount;
    }

    public static int load(String name) throws IOException {
        return load(name, true);
    }

    private static int load(String name, boolean remember) throws IOException {
        String safeName = sanitizeName(name);
        ensureDefaultProfile();
        Path path = configPath(safeName);
        if (!Files.isRegularFile(path)) {
            throw new IOException("Config not found: " + safeName);
        }
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
        }
        ConfigMigrationRules.migrate(properties, entryKeys());
        int loaded = 0;
        for (Entry entry : ENTRIES) {
            String value = properties.getProperty(entry.key());
            if (value == null) {
                continue;
            }
            if (entry.write(value)) {
                loaded++;
            }
        }
        loaded += WidgetSurface.loadHotkeys(properties, ConfigSchema.HOTKEYS_PREFIX);
        if (remember) {
            rememberLastLoaded(safeName);
        }
        return loaded;
    }

    public static AutoLoadResult autoLoad() {
        ensureDefaultProfile();
        String lastLoaded = readLastLoaded();
        if (lastLoaded != null && profileExists(lastLoaded)) {
            try {
                return new AutoLoadResult(lastLoaded, load(lastLoaded, true), true, null);
            } catch (IOException exception) {
                return new AutoLoadResult(lastLoaded, 0, false, exception);
            }
        }
        if (profileExists(DEFAULT_PROFILE)) {
            try {
                return new AutoLoadResult(DEFAULT_PROFILE, load(DEFAULT_PROFILE, true), true, null);
            } catch (IOException exception) {
                return new AutoLoadResult(DEFAULT_PROFILE, 0, false, exception);
            }
        }
        return AutoLoadResult.none();
    }

    public static boolean delete(String name) throws IOException {
        String safeName = sanitizeName(name);
        if (isProtectedDefault(safeName)) {
            throw new IOException("Default config is protected");
        }
        boolean deleted = Files.deleteIfExists(configPath(safeName));
        if (deleted && safeName.equals(readLastLoaded())) {
            clearLastLoaded();
        }
        return deleted;
    }

    public static String duplicate(String sourceName) throws IOException {
        String sourceSafeName = sanitizeName(sourceName);
        ensureDefaultProfile();
        Path source = configPath(sourceSafeName);
        if (!Files.isRegularFile(source)) {
            throw new IOException("Config not found: " + sourceSafeName);
        }
        Files.createDirectories(directory());
        String targetName = nextAvailableName(sourceSafeName + "_copy");
        Files.copy(source, configPath(targetName), StandardCopyOption.REPLACE_EXISTING);
        return targetName;
    }

    public static String rename(String sourceName, String targetName) throws IOException {
        String sourceSafeName = sanitizeName(sourceName);
        String targetSafeName = sanitizeName(targetName);
        if (isProtectedDefault(sourceSafeName) || isProtectedDefault(targetSafeName)) {
            throw new IOException("Default config is protected");
        }
        if (sourceSafeName.equals(targetSafeName)) {
            return sourceSafeName;
        }
        Path source = configPath(sourceSafeName);
        Path target = configPath(targetSafeName);
        if (!Files.isRegularFile(source)) {
            throw new IOException("Config not found: " + sourceSafeName);
        }
        if (Files.exists(target)) {
            targetSafeName = nextAvailableName(targetSafeName);
            target = configPath(targetSafeName);
        }
        Files.createDirectories(directory());
        Files.move(source, target);
        if (sourceSafeName.equals(readLastLoaded())) {
            rememberLastLoaded(targetSafeName);
        }
        return targetSafeName;
    }

    public static String nextAvailableName(String baseName) {
        String safeBase = sanitizeName(baseName);
        if (!Files.exists(configPath(safeBase))) {
            return safeBase;
        }
        for (int index = 1; index < 10_000; index++) {
            String candidate = safeBase + "_" + index;
            if (!Files.exists(configPath(candidate))) {
                return candidate;
            }
        }
        return safeBase + "_" + System.currentTimeMillis();
    }

    public static String sanitizeName(String name) {
        String raw = name == null || name.isBlank() ? "default" : name.trim();
        StringBuilder builder = new StringBuilder(raw.length());
        for (int offset = 0; offset < raw.length(); ) {
            int codePoint = raw.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (Character.isWhitespace(codePoint)) {
                builder.append('_');
            } else if (!Character.isISOControl(codePoint)
                    && "<>:\"/\\|?*".indexOf(codePoint) < 0) {
                builder.appendCodePoint(codePoint);
            }
        }
        String safe = builder.toString();
        return safe.isBlank() ? "default" : safe;
    }

    public static int entryCount() {
        return ENTRIES.size();
    }

    public static String lastLoadedProfile() {
        ensureDefaultProfile();
        return readLastLoaded();
    }

    public static boolean isProtectedDefault(String name) {
        return DEFAULT_PROFILE.equalsIgnoreCase(sanitizeName(name));
    }

    public static void ensureDefaultProfile() {
        Path path = configPath(DEFAULT_PROFILE);
        if (Files.isRegularFile(path)) {
            return;
        }
        try {
            saveInternal(DEFAULT_PROFILE);
        } catch (IOException ignored) {
        }
    }

    private static Path configPath(String safeName) {
        return directory().resolve(safeName + ConfigSchema.PROFILE_EXTENSION);
    }

    private static Path statePath() {
        return rootDirectory().resolve(STATE_FILE);
    }

    private static boolean profileExists(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return Files.isRegularFile(configPath(sanitizeName(name)));
    }

    private static String readLastLoaded() {
        Path path = statePath();
        if (!Files.isRegularFile(path)) {
            return null;
        }
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
        } catch (IOException ignored) {
            return null;
        }
        String value = properties.getProperty(LAST_LOADED_KEY);
        if (value == null || value.isBlank()) {
            return null;
        }
        return sanitizeName(value);
    }

    private static void rememberLastLoaded(String name) throws IOException {
        Files.createDirectories(rootDirectory());
        Properties properties = new Properties();
        properties.setProperty(LAST_LOADED_KEY, sanitizeName(name));
        try (OutputStream output = Files.newOutputStream(statePath())) {
            properties.store(output, "Xerion state");
        }
    }

    private static void clearLastLoaded() {
        try {
            Files.deleteIfExists(statePath());
        } catch (IOException ignored) {
        }
    }

    private static List<Entry> discoverEntries() {
        List<Entry> entries = new ArrayList<>();
        for (ConfigSchema.Owner schemaOwner : ConfigSchema.OWNERS) {
            Class<?> owner = schemaOwner.type();
            for (Method getter : owner.getMethods()) {
                if (!isGetter(getter)) {
                    continue;
                }
                Method setter = findSetter(owner, getter);
                if (setter == null) {
                    continue;
                }
                entries.add(new Entry(ConfigSchema.category(owner), owner.getSimpleName() + "." + getter.getName(), getter, setter));
            }
        }
        entries.sort(Comparator.comparing(Entry::category).thenComparing(Entry::key));
        return List.copyOf(entries);
    }

    private static List<String> entryKeys() {
        return ENTRIES.stream()
                .map(Entry::key)
                .toList();
    }

    private static boolean isGetter(Method method) {
        return Modifier.isStatic(method.getModifiers())
                && Modifier.isPublic(method.getModifiers())
                && method.getParameterCount() == 0
                && supportedType(method.getReturnType())
                && method.getDeclaringClass() != Object.class;
    }

    private static Method findSetter(Class<?> owner, Method getter) {
        String name = "set" + Character.toUpperCase(getter.getName().charAt(0)) + getter.getName().substring(1);
        for (Method setter : owner.getMethods()) {
            if (!setter.getName().equals(name)
                    || !Modifier.isStatic(setter.getModifiers())
                    || !Modifier.isPublic(setter.getModifiers())
                    || setter.getParameterCount() != 1) {
                continue;
            }
            if (canAssign(getter.getReturnType(), setter.getParameterTypes()[0])) {
                return setter;
            }
        }
        return null;
    }

    private static boolean supportedType(Class<?> type) {
        Class<?> wrapped = wrap(type);
        return wrapped == Boolean.class
                || wrapped == Integer.class
                || wrapped == Float.class
                || wrapped == Double.class
                || wrapped == Long.class
                || wrapped == String.class
                || type.isEnum();
    }

    private static boolean canAssign(Class<?> getterType, Class<?> setterType) {
        Class<?> getterWrapped = wrap(getterType);
        Class<?> setterWrapped = wrap(setterType);
        return setterWrapped.isAssignableFrom(getterWrapped)
                || getterWrapped == Integer.class && setterWrapped == Float.class
                || getterWrapped == Float.class && setterWrapped == Double.class;
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        return type;
    }

    private static String valueToString(Object value) {
        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        return Objects.toString(value);
    }

    private record Entry(String category, String key, Method getter, Method setter) {
        private Object read() {
            try {
                return getter.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                return null;
            }
        }

        private boolean write(String raw) {
            try {
                Object parsed = parseValue(raw, setter.getParameterTypes()[0]);
                setter.invoke(null, parsed);
                return true;
            } catch (IllegalAccessException | InvocationTargetException | RuntimeException ignored) {
                return false;
            }
        }
    }

    public record AutoLoadResult(String profile, int loadedCount, boolean loaded, Exception exception) {
        private static AutoLoadResult none() {
            return new AutoLoadResult(null, 0, false, null);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object parseValue(String raw, Class<?> targetType) {
        Class<?> wrapped = wrap(targetType);
        if (wrapped == Boolean.class) {
            return parseBoolean(raw);
        }
        if (wrapped == Integer.class) {
            return Integer.parseInt(raw.trim());
        }
        if (wrapped == Float.class) {
            return Float.parseFloat(raw.trim());
        }
        if (wrapped == Double.class) {
            return Double.parseDouble(raw.trim());
        }
        if (wrapped == Long.class) {
            return Long.parseLong(raw.trim());
        }
        if (wrapped == String.class) {
            return raw;
        }
        if (targetType.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) targetType, raw.trim().toUpperCase(Locale.ROOT));
        }
        throw new IllegalArgumentException("Unsupported type: " + targetType);
    }

    private static boolean parseBoolean(String raw) {
        String normalized = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(normalized)) {
            return true;
        }
        if ("false".equals(normalized)) {
            return false;
        }
        throw new IllegalArgumentException("Invalid boolean: " + raw);
    }
}
