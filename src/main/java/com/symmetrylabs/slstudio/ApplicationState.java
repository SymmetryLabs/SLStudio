package com.symmetrylabs.slstudio;

import com.symmetrylabs.slstudio.output.OutputControl;

public class ApplicationState {
    public interface Provider {
        String showName();
        OutputControl outputControl();
        void setWarning(String key, String message);
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
        if (message != null && !message.isEmpty()) {
            System.err.println("WARNING: " + key + ": " + message);
        }
        get().setWarning(key, message);
    }
}
