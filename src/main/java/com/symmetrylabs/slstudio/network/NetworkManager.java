package com.symmetrylabs.slstudio.network;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.ref.WeakReference;

public class NetworkManager {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private static WeakReference<NetworkManager> instance = new WeakReference<>(null);

    public static NetworkManager getInstance() {
        NetworkManager ref = instance.get();
        if (ref == null) {
            instance = new WeakReference<>(ref = new NetworkManager());
        }
        return ref;
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
