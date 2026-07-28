package com.symmetrylabs.slstudio.logging;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Watchdog that detects when the UI/render thread stops pulsing and writes a
 * freeze log with a full thread dump. Pulse from the main animation thread each frame.
 */
public final class FreezeDetector {
    private static final long CHECK_INTERVAL_MS = 1000;
    private static final long THRESHOLD_MS = 5000;
    private static final long COOLDOWN_MS = 30000;

    private static final AtomicLong lastPulse = new AtomicLong(Long.MAX_VALUE);
    private static final AtomicLong hangStartedAt = new AtomicLong(0);
    private static final AtomicBoolean hangReported = new AtomicBoolean(false);
    private static volatile Thread uiThread;
    private static volatile Thread watchdogThread;
    private static volatile boolean running = false;
    private static volatile long watchdogStartTime;

    private FreezeDetector() {}

    /**
     * Starts the watchdog, recording the given thread as the UI/render thread.
     * Call this from the UI thread (e.g., Processing setup()).
     */
    public static synchronized void start(Thread thread) {
        uiThread = thread;
        if (running) {
            return;
        }
        running = true;
        watchdogStartTime = System.currentTimeMillis();
        watchdogThread = new Thread(FreezeDetector::watch, "FreezeDetector");
        watchdogThread.setDaemon(true);
        watchdogThread.start();
    }

    /**
     * Call each frame from the UI/render thread to reset the watchdog timer.
     */
    public static void pulse() {
        lastPulse.set(System.currentTimeMillis());
        if (hangReported.compareAndSet(true, false)) {
            System.out.println("FreezeDetector: UI thread resumed");
        }
    }

    public static synchronized void stop() {
        running = false;
        if (watchdogThread != null) {
            watchdogThread.interrupt();
        }
    }

    private static void watch() {
        while (running) {
            try {
                Thread.sleep(CHECK_INTERVAL_MS);
            } catch (InterruptedException e) {
                return;
            }

            long now = System.currentTimeMillis();
            long pulse = lastPulse.get();
            long elapsed;
            boolean neverPulsed = false;
            if (pulse == Long.MAX_VALUE) {
                // UI thread has not emitted its first pulse yet.
                elapsed = now - watchdogStartTime;
                neverPulsed = true;
            } else {
                elapsed = now - pulse;
            }

            if (elapsed > THRESHOLD_MS) {
                if (hangReported.compareAndSet(false, true)) {
                    hangStartedAt.set(now);
                    reportFreeze(elapsed, neverPulsed);
                }
            } else if (hangReported.compareAndSet(true, false)) {
                long hangMs = now - hangStartedAt.get();
                if (hangMs > 0) {
                    System.out.println("FreezeDetector: UI thread resumed after " + hangMs + " ms");
                }
            }
        }
    }

    private static void reportFreeze(long elapsedMs, boolean neverPulsed) {
        String msg = neverPulsed
            ? "UI freeze detected: no pulse since watchdog start (" + elapsedMs + " ms)"
            : "UI freeze detected: no pulse for " + elapsedMs + " ms";
        System.err.println(msg);
        try {
            File log = CrashLog.writeFreezeLog(uiThread, elapsedMs);
            System.err.println("Freeze log written to: " + log.getAbsolutePath());
        } catch (Throwable t) {
            System.err.println("Failed to write freeze log: " + t.getMessage());
        }
    }
}
