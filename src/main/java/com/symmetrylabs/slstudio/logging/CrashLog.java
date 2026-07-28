package com.symmetrylabs.slstudio.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Writes persistent crash/freeze logs to a dedicated directory.
 */
public final class CrashLog {
    private static final String DEFAULT_DIR = "logs" + File.separator + "crashes";
    private static volatile File logDir = new File(System.getProperty("com.symmetrylabs.crashLogDir", DEFAULT_DIR));

    private static final SimpleDateFormat TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");

    private CrashLog() {}

    public static synchronized void setLogDirectory(File dir) {
        logDir = dir;
    }

    public static File getLogDirectory() {
        return logDir;
    }

    public static File writeCrashLog(Thread thread, Throwable throwable) {
        return writeLog("crash", thread, throwable, -1);
    }

    public static File writeFreezeLog(Thread thread, long elapsedMs) {
        return writeLog("freeze", thread, null, elapsedMs);
    }

    private static synchronized File writeLog(String kind, Thread thread, Throwable throwable, long elapsedMs) {
        File dir = logDir;
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, kind + "-" + TIMESTAMP.format(new Date()) + ".log");
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println(kind.toUpperCase() + " REPORT: " + new Date());
            if (thread != null) {
                out.println("Thread: " + thread.getName() + " (id=" + thread.getId() + ")");
            }
            if (elapsedMs >= 0) {
                out.println("No UI pulse for " + elapsedMs + " ms");
            }
            out.println();

            if (throwable != null) {
                out.println("--- Exception ---");
                out.println("Exception: " + throwable);
                throwable.printStackTrace(out);
                out.println();
            }

            appendEnvironment(out);
            appendThreadDump(out);
            out.flush();
        } catch (IOException e) {
            System.err.println("Failed to write " + kind + " log to " + file.getAbsolutePath() + ": " + e.getMessage());
        }

        return file;
    }

    private static void appendEnvironment(PrintWriter out) {
        out.println("--- Environment ---");
        out.println("java.version=" + System.getProperty("java.version"));
        out.println("java.vendor=" + System.getProperty("java.vendor"));
        out.println("os.name=" + System.getProperty("os.name"));
        out.println("os.version=" + System.getProperty("os.version"));
        out.println("os.arch=" + System.getProperty("os.arch"));

        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        out.println("jvm.name=" + runtime.getVmName());
        out.println("jvm.version=" + runtime.getVmVersion());
        out.println("jvm.args=" + runtime.getInputArguments());

        Runtime r = Runtime.getRuntime();
        out.println("memory.free=" + r.freeMemory());
        out.println("memory.max=" + r.maxMemory());
        out.println("memory.total=" + r.totalMemory());
        out.println();
    }

    private static void appendThreadDump(PrintWriter out) {
        out.println("--- Thread dump ---");
        Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
        List<Thread> threads = new ArrayList<>(stacks.keySet());
        threads.sort(Comparator.comparing(Thread::getName));
        for (Thread t : threads) {
            out.println("\"" + t.getName() + "\" " + (t.isDaemon() ? "daemon " : "")
                + "prio=" + t.getPriority() + " tid=" + t.getId() + " state=" + t.getState());
            for (StackTraceElement e : stacks.get(t)) {
                out.println("\tat " + e);
            }
            out.println();
        }
    }
}
