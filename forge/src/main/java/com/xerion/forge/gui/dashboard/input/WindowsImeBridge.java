package com.xerion.forge.gui.dashboard;

import city.windmill.ingameime.client.jni.ExternalBaseIME;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public final class WindowsImeBridge {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile CommitHandler commitHandler;
    private static volatile CaretProvider caretProvider;
    private static volatile String composition;
    private static volatile int compositionCaret;
    private static volatile String[] candidates;
    private static boolean initialized;
    private static boolean inputActive;
    private static boolean fullscreen;
    private static boolean usingImBlocker;
    private static Method imBlockerSetState;
    private static Method imBlockerSetFocusOwner;
    private static Method imBlockerUpdateCompositionPosition;
    private static Constructor<?> imBlockerPointConstructor;
    private static Constructor<?> imBlockerRectangleConstructor;
    private static Object imBlockerFocusObject;
    private static int lastImBlockerCaretX = Integer.MIN_VALUE;
    private static int lastImBlockerCaretY = Integer.MIN_VALUE;

    private static final ExternalBaseIME.Listener NATIVE_LISTENER = new ExternalBaseIME.Listener() {
        @Override
        public void onCandidates(String[] nextCandidates) {
            candidates = nextCandidates == null ? null : Arrays.copyOf(nextCandidates, nextCandidates.length);
            LOGGER.info("Xerion native IME candidate callback (count={})",
                    nextCandidates == null ? 0 : nextCandidates.length);
        }

        @Override
        public void onComposition(String text, int caret, ExternalBaseIME.CompositionState state) {
            LOGGER.info("Xerion native IME composition callback (state={}, length={}, caret={})",
                    state, text == null ? 0 : text.length(), caret);
            if (state == ExternalBaseIME.CompositionState.Commit) {
                composition = null;
                candidates = null;
                if (text != null && !text.isEmpty()) {
                    Minecraft.getInstance().execute(() -> commit(text));
                }
                return;
            }
            if (state == ExternalBaseIME.CompositionState.End) {
                composition = null;
                candidates = null;
                return;
            }
            composition = text == null || text.isEmpty() ? null : text;
            compositionCaret = Math.max(0, caret);
        }

        @Override
        public int[] compositionRectangle() {
            CaretProvider provider = caretProvider;
            ClientPoint point = provider == null ? null : provider.caretPosition();
            if (point == null) {
                return new int[] {0, 0, 2, 18};
            }
            return new int[] {point.x(), point.y(), point.x() + 2, point.y() + 18};
        }

        @Override
        public void onAlphaMode(boolean alphaMode) {
            // The operating system owns the language-mode indicator in windowed mode.
        }
    };

    private WindowsImeBridge() {
    }

    public static synchronized void initializeGlobal() {
        if (initialized || !isWindows()) {
            return;
        }
        try {
            Class<?> imManager = Class.forName(
                    "io.github.reserveword.imblocker.common.IMManager",
                    false,
                    WindowsImeBridge.class.getClassLoader());
            imBlockerSetState = imManager.getMethod("setState", boolean.class);
            imBlockerUpdateCompositionPosition = imManager.getMethod("updateCompositionWindowPos");
            Class<?> focusableObject = Class.forName(
                    "io.github.reserveword.imblocker.common.gui.FocusableObject",
                    false,
                    WindowsImeBridge.class.getClassLoader());
            Class<?> focusManager = Class.forName(
                    "io.github.reserveword.imblocker.common.gui.FocusManager",
                    false,
                    WindowsImeBridge.class.getClassLoader());
            Class<?> point = Class.forName(
                    "io.github.reserveword.imblocker.common.gui.Point",
                    false,
                    WindowsImeBridge.class.getClassLoader());
            Class<?> rectangle = Class.forName(
                    "io.github.reserveword.imblocker.common.gui.Rectangle",
                    false,
                    WindowsImeBridge.class.getClassLoader());
            imBlockerSetFocusOwner = focusManager.getMethod("setFocusOwner", focusableObject);
            imBlockerPointConstructor = point.getConstructor(int.class, int.class);
            imBlockerRectangleConstructor = rectangle.getConstructor(
                    int.class, int.class, int.class, int.class);
            imBlockerFocusObject = Proxy.newProxyInstance(
                    WindowsImeBridge.class.getClassLoader(),
                    new Class<?>[] {focusableObject},
                    WindowsImeBridge::imBlockerFocusValue);
            usingImBlocker = true;
            initialized = true;
            LOGGER.info("Xerion IME integrated with the installed IMBlocker window context");
            return;
        } catch (ClassNotFoundException ignored) {
            // Use Xerion's bundled native engine when IMBlocker is not installed.
        } catch (ReflectiveOperationException throwable) {
            LOGGER.warn("Unable to bind the installed IMBlocker input API", throwable);
        }

        try {
            long glfwWindow = Minecraft.getInstance().getWindow().getWindow();
            long nativeWindow = GLFWNativeWin32.glfwGetWin32Window(glfwWindow);
            initialized = nativeWindow != 0L
                    && ExternalBaseIME.initialize(nativeWindow, NATIVE_LISTENER);
            if (!initialized) {
                LOGGER.warn("Unable to initialize the Xerion native IME engine");
                return;
            }
            fullscreen = Minecraft.getInstance().getWindow().isFullscreen();
            ExternalBaseIME.setFullscreen(fullscreen);
            LOGGER.info("Xerion native TSF/IMM input engine initialized on {} ({})",
                    Thread.currentThread().getName(), Long.toUnsignedString(nativeWindow));
        } catch (Throwable throwable) {
            initialized = false;
            LOGGER.warn("Unable to initialize the Xerion native IME engine", throwable);
        }
    }

    static synchronized void install(CommitHandler nextCommitHandler, CaretProvider nextCaretProvider) {
        commitHandler = nextCommitHandler;
        caretProvider = nextCaretProvider;
        if (!isWindows()) {
            return;
        }
        initializeGlobal();
        if (!initialized) {
            return;
        }
        if (!usingImBlocker) {
            ExternalBaseIME.setListener(NATIVE_LISTENER);
            fullscreen = Minecraft.getInstance().getWindow().isFullscreen();
            ExternalBaseIME.setFullscreen(fullscreen);
        }
    }

    static synchronized void uninstall() {
        setInputActive(false);
        commitHandler = null;
        caretProvider = null;
        composition = null;
        candidates = null;
    }

    static synchronized void updateInputState(boolean active) {
        if (!initialized) {
            return;
        }
        if (!usingImBlocker) {
            boolean nextFullscreen = Minecraft.getInstance().getWindow().isFullscreen();
            if (nextFullscreen != fullscreen) {
                fullscreen = nextFullscreen;
                ExternalBaseIME.setFullscreen(fullscreen);
            }
        }
        setInputActive(active);
        if (usingImBlocker && active) {
            updateImBlockerCompositionPosition();
        }
    }

    static String composition() {
        return composition;
    }

    static int compositionCaret() {
        return compositionCaret;
    }

    static String[] candidates() {
        String[] current = candidates;
        return current == null ? null : Arrays.copyOf(current, current.length);
    }

    private static void setInputActive(boolean active) {
        if (!initialized || inputActive == active) {
            return;
        }
        inputActive = active;
        LOGGER.info("Xerion native IME state -> {}", active ? "enabled" : "disabled");
        if (usingImBlocker) {
            try {
                if (active) {
                    imBlockerSetFocusOwner.invoke(null, imBlockerFocusObject);
                    lastImBlockerCaretX = Integer.MIN_VALUE;
                    lastImBlockerCaretY = Integer.MIN_VALUE;
                }
                imBlockerSetState.invoke(null, active);
                if (!active) {
                    imBlockerSetFocusOwner.invoke(null, new Object[] {null});
                }
            } catch (ReflectiveOperationException throwable) {
                LOGGER.warn("Unable to update IMBlocker state for the Xerion input field", throwable);
            }
        } else {
            ExternalBaseIME.setState(active);
        }
        if (!active) {
            composition = null;
            candidates = null;
        }
    }

    private static Object imBlockerFocusValue(Object proxy, Method method, Object[] arguments)
            throws ReflectiveOperationException {
        return switch (method.getName()) {
            case "getPreferredState" -> true;
            case "getPreferredEnglishState" -> false;
            case "getGuiScale" -> 1.0d;
            case "getFontHeight" -> 18;
            case "getCaretPos" -> {
                ClientPoint caret = currentCaretPosition();
                yield imBlockerPointConstructor.newInstance(caret.x(), caret.y());
            }
            case "getBoundsAbs" -> {
                com.mojang.blaze3d.platform.Window window = Minecraft.getInstance().getWindow();
                yield imBlockerRectangleConstructor.newInstance(0, 0, window.getWidth(), window.getHeight());
            }
            case "isTrulyFocused" -> inputActive;
            case "toString" -> "XerionTextInputFocus";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> arguments != null && arguments.length == 1 && proxy == arguments[0];
            default -> null;
        };
    }

    private static void updateImBlockerCompositionPosition() {
        ClientPoint caret = currentCaretPosition();
        if (caret.x() == lastImBlockerCaretX && caret.y() == lastImBlockerCaretY) {
            return;
        }
        lastImBlockerCaretX = caret.x();
        lastImBlockerCaretY = caret.y();
        try {
            imBlockerUpdateCompositionPosition.invoke(null);
        } catch (ReflectiveOperationException throwable) {
            LOGGER.warn("Unable to update the Xerion IME composition position", throwable);
        }
    }

    private static ClientPoint currentCaretPosition() {
        CaretProvider provider = caretProvider;
        ClientPoint caret = provider == null ? null : provider.caretPosition();
        return caret == null ? new ClientPoint(0, 0) : caret;
    }

    private static void commit(String text) {
        CommitHandler handler = commitHandler;
        if (handler != null) {
            handler.commit(text);
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "")
                .toLowerCase(java.util.Locale.ROOT).contains("win");
    }

    @FunctionalInterface
    interface CommitHandler {
        boolean commit(String text);
    }

    @FunctionalInterface
    interface CaretProvider {
        ClientPoint caretPosition();
    }

    record ClientPoint(int x, int y) {
    }
}
