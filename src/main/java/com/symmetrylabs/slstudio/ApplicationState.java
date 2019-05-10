package com.symmetrylabs.slstudio;

import com.symmetrylabs.slstudio.output.OutputControl;

public class ApplicationState {
    public enum Mode {
        SLSTUDIO,
        VOLUME,
    };

    public interface Provider {
        String showName();
        OutputControl outputControl();
        void setWarning(String key, String message);
        Mode interfaceMode();
    }

    private static Provider provider = null;

    public static void setProvider(Provider p) {
        provider = p;
    }

    private static Provider get() {
        if (provider == null) {
            throw new RuntimeException("no application state provider set");
        }
        return provider;
    }

    public static String showName() {
        return get().showName();
    }

    public static OutputControl outputControl() {
        return get().outputControl();
    }

    public static void setWarning(String key, String message) {
        get().setWarning(key, message);
    }

    public static Mode interfaceMode() {
        return get().interfaceMode();
    }

    public static boolean inVolumeMode() {
        return get().interfaceMode() == Mode.VOLUME;
    }

    public static boolean inSLStudioMode() {
        return get().interfaceMode() == Mode.SLSTUDIO;
    }

    public static class DummyProvider implements Provider {
        public String showName() {
            return "DummyProvider.testshow";
        }

        public OutputControl outputControl() {
            return null;
        }

        public void setWarning(String key, String message) {
            System.out.println(String.format("WARN %s: %s", key, message));
        }

        public Mode interfaceMode() {
            return Mode.VOLUME;
        }
    }
}
