package com.symmetrylabs.slstudio;

public class ApplicationState {
    public interface Provider {
        String showName();
    }

    private static Provider provider = null;

    public static void setProvider(Provider p) {
        provider = p;
    }

    public static Provider get() {
        if (provider == null) {
            throw new RuntimeException("no application state provider set");
        }
        return provider;
    }
}
