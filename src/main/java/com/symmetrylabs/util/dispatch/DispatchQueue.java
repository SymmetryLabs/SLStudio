package com.symmetrylabs.util.dispatch;

import java.util.concurrent.ConcurrentLinkedQueue;


class DispatchQueue {

    private volatile String threadName;
    private final ConcurrentLinkedQueue<Runnable> queuedRunnables = new ConcurrentLinkedQueue<Runnable>();

    DispatchQueue() {
        this(null);
    }

    DispatchQueue(String threadName) {
        this.threadName = threadName;
    }

    synchronized void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    String getThreadName() {
        return threadName;
    }

    void executeAll() {
        Runnable runnable;
        while ((runnable = queuedRunnables.poll()) != null) {
            runnable.run();
        }
    }

    void queue(Runnable runnable) {
        boolean shouldRunNow = false;
        synchronized (this) {
            if (threadName != null && Thread.currentThread().getName().equals(threadName)) {
                shouldRunNow = true;
            }
        }
        if (shouldRunNow) {
            runnable.run();
        } else {
            queuedRunnables.add(runnable);
        }
    }
}
