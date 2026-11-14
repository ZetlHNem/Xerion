package city.windmill.ingameime.client.jni;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ExternalBaseIME {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ExternalBaseIME INSTANCE = new ExternalBaseIME();
    private static volatile Listener listener;
    private static boolean loaded;
    private static boolean initialized;
    private static boolean state;
    private static boolean fullscreen;

    private ExternalBaseIME() {
    }

    public static synchronized boolean initialize(long windowHandle, Listener nextListener) {
        listener = nextListener;
        try {
            if (!loaded) {
                loadNativeLibrary();
                loaded = true;
            }
            if (!initialized) {
                INSTANCE.nInitialize(windowHandle);
                initialized = true;
            }
            return true;
        } catch (Throwable throwable) {
            LOGGER.warn("Failed to load the Xerion TSF/IMM native library", throwable);
            return false;
        }
    }

    public static void setListener(Listener nextListener) {
        listener = nextListener;
    }

    public static synchronized void setState(boolean active) {
        if (!initialized || state == active) {
            return;
        }
        state = active;
        LOGGER.info("Calling native IME nSetState({})", active);
        INSTANCE.nSetState(active);
    }

    public static synchronized void setFullscreen(boolean active) {
        if (!initialized || fullscreen == active) {
            return;
        }
        fullscreen = active;
        INSTANCE.nSetFullScreen(active);
        if (state) {
            INSTANCE.nSetState(false);
            INSTANCE.nSetState(true);
        }
    }

    private static void loadNativeLibrary() throws Exception {
        boolean x64 = System.getProperty("os.arch", "").contains("64");
        String resource = x64
                ? "/assets/xerion/natives/contingameime-jni.dll"
                : "/assets/xerion/natives/contingameime-jni-x86.dll";
        try (InputStream input = ExternalBaseIME.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("Missing native IME resource " + resource);
            }
            Path library = Files.createTempFile("xerion-ime-", ".dll");
            Files.copy(input, library, StandardCopyOption.REPLACE_EXISTING);
            library.toFile().deleteOnExit();
            System.load(library.toAbsolutePath().toString());
        }
    }

    private void onCandidateList(String[] nextCandidates) {
        Listener current = listener;
        if (current != null) {
            current.onCandidates(nextCandidates);
        }
    }

    private void onComposition(String text, int caret, CompositionState compositionState) {
        Listener current = listener;
        if (current != null) {
            current.onComposition(text, caret, compositionState);
        }
    }

    private int[] onGetCompExt() {
        Listener current = listener;
        return current == null ? new int[] {0, 0, 2, 18} : current.compositionRectangle();
    }

    private void onAlphaMode(boolean alphaMode) {
        Listener current = listener;
        if (current != null) {
            current.onAlphaMode(alphaMode);
        }
    }

    private native void nInitialize(long windowHandle);

    @SuppressWarnings("unused")
    private native void nUninitialize();

    private native void nSetState(boolean active);

    private native void nSetFullScreen(boolean active);

    public interface Listener {
        void onCandidates(String[] candidates);

        void onComposition(String text, int caret, CompositionState state);

        int[] compositionRectangle();

        void onAlphaMode(boolean alphaMode);
    }

    public enum CompositionState {
        Start,
        Update,
        End,
        Commit
    }
}
