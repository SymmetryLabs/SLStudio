package com.symmetrylabs.slstudio.logging;

import java.io.File;

/**
 * Global uncaught exception handler that writes a persistent crash log.
 */
public final class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final CrashHandler INSTANCE = new CrashHandler();

    private CrashHandler() {}

    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    public static void install() {
        Thread.setDefaultUncaughtExceptionHandler(INSTANCE);
    }

    public static void applyTo(Thread thread) {
        if (thread != null) {
            thread.setUncaughtExceptionHandler(INSTANCE);
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        System.err.println("Uncaught exception in thread \"" + thread.getName() + "\"");
        throwable.printStackTrace(System.err);
        try {
            File log = CrashLog.writeCrashLog(thread, throwable);
            System.err.println("Crash log written to: " + log.getAbsolutePath());
        } catch (Throwable t) {
            System.err.println("Failed to write crash log: " + t.getMessage());
        }
    }
}
