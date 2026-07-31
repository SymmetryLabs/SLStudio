package com.symmetrylabs.slstudio.logging;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Keeps a misbehaving pattern from taking down the render thread.
 *
 * <p>A pattern that throws is logged once (with a full crash report) and then
 * quarantined so it is skipped on subsequent frames instead of throwing every
 * frame or killing the channel thread.
 */
public final class PatternErrorGuard {

    /** Number of failures tolerated before a component is quarantined. */
    private static final int MAX_FAILURES = 3;

    /**
     * Minimum time between crash logs for the same description. Writing a crash log
     * captures a full thread dump, which is far too expensive to do every frame.
     */
    private static final long REPORT_INTERVAL_MS = 60_000;

    private static final Map<String, Long> lastReportTime = new HashMap<>();

    private static final Map<Object, AtomicInteger> failureCounts =
        Collections.synchronizedMap(new WeakHashMap<>());
    private static final Set<Object> quarantined =
        Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private PatternErrorGuard() {}

    /** Returns true if this component has been quarantined and should be skipped. */
    public static boolean isQuarantined(Object component) {
        return quarantined.contains(component);
    }

    /**
     * Runs the given work, containing any {@link Throwable} it raises.
     *
     * @param component the pattern/effect being run, used for quarantine tracking
     * @param description human-readable context for the log message
     * @param work the work to run
     * @return true if the work completed without error
     */
    public static boolean run(Object component, String description, Runnable work) {
        if (isQuarantined(component)) {
            return false;
        }
        try {
            work.run();
            return true;
        } catch (Throwable t) {
            recordFailure(component, description, t);
            return false;
        }
    }

    /**
     * Logs a throwable that escaped a guarded region without quarantining anything.
     * Used as a last-resort safety net so a render thread is never killed.
     *
     * <p>Because this can be reached from the render loop, crash logs are throttled
     * per description; repeat failures within the interval are dropped entirely.
     */
    public static void report(String description, Throwable t) {
        long now = System.currentTimeMillis();
        synchronized (lastReportTime) {
            Long last = lastReportTime.get(description);
            if (last != null && now - last < REPORT_INTERVAL_MS) {
                return;
            }
            lastReportTime.put(description, now);
        }

        System.err.println("Error in " + description + ": " + t);
        t.printStackTrace(System.err);
        try {
            File log = CrashLog.writeCrashLog(Thread.currentThread(), t);
            System.err.println("Crash log written to: " + log.getAbsolutePath());
        } catch (Throwable ignored) {
            System.err.println("Failed to write crash log: " + ignored.getMessage());
        }
    }

    private static void recordFailure(Object component, String description, Throwable t) {
        AtomicInteger counter;
        synchronized (failureCounts) {
            counter = failureCounts.computeIfAbsent(component, k -> new AtomicInteger());
        }
        int failures = counter.incrementAndGet();

        System.err.println(String.format(
            "Error in %s (failure %d of %d): %s", description, failures, MAX_FAILURES, t));

        if (failures == 1) {
            t.printStackTrace(System.err);
            try {
                File log = CrashLog.writeCrashLog(Thread.currentThread(), t);
                System.err.println("Crash log written to: " + log.getAbsolutePath());
            } catch (Throwable ignored) {
                System.err.println("Failed to write crash log: " + ignored.getMessage());
            }
        }

        if (failures >= MAX_FAILURES) {
            quarantined.add(component);
            System.err.println(
                "Disabling " + description + " after " + failures + " failures. "
                    + "Remove or fix the component and restart to re-enable it.");
        }
    }

    /** Clears the failure state for a component, allowing it to run again. */
    public static void reset(Object component) {
        quarantined.remove(component);
        failureCounts.remove(component);
    }
}
