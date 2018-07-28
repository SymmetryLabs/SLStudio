package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;

import java.util.ArrayList;
import java.util.List;

import com.symmetrylabs.layouts.tree.TreeModel;


public abstract class TreeThreadedPattern extends TreePattern {

    private static final int DEFAULT_NUM_THREADS = 8;

    private double deltaMs;
    private final WorkerThread[] threads;

    public TreeThreadedPattern(LX lx) {
        super(lx);

        // Create threads
        int numThreads = getNumThreads();
        this.threads = new WorkerThread[numThreads];
        for (int i = 0; i < numThreads; ++i) {
            this.threads[i] = new WorkerThread(getClass().getName() + "-Thread" + i);
        }

        // Distribute branches over the threads
        allocateBranches();

        // Start the threads
        for (WorkerThread thread : this.threads) {
            thread.start();
        }
    }

    // Override this if you want a different number of worker threads
    public int getNumThreads() {
        return DEFAULT_NUM_THREADS;
    }

    // Your subclass may want to override this method to allocate
    // branches in a different manner
    public void allocateBranches() {
        int i = 0;
        for (TreeModel.Branch branch : model.branches) {
            this.threads[i % this.threads.length].branches.add(branch);
            ++i;
        }
    }

    public void run(double deltaMs) {
        // Store frame's deltaMs for threads
        this.deltaMs = deltaMs;

        // Notify every thread that it has work to do
        for (WorkerThread thread : this.threads) {
            synchronized (thread) {
                thread.hasWork = true;
                thread.notify();
            }
        }

        // Wait for all the sub-threads to complete
        for (WorkerThread thread : this.threads) {
            synchronized (thread) {
                while (!thread.workDone) {
                    try {
                        thread.wait();
                    } catch (InterruptedException ix) {
                        ix.printStackTrace();
                    }
                }
                thread.workDone = false;
            }
        }

        // The colors array should be fully updated now,
        // each worker thread will have updated its own portion
    }

    // Your subclass should extend this method, and compute the colors only for the
    // branches specified, taking care to note that you are running in a unique
    // thread context and should not be depending upon or modifying global state that
    // would affect how *other* branches are rendered!
    abstract void runThread(List<TreeModel.Branch> branches, double deltaMs); /* {
        for (Branch branch : branches) {
            // Per-branch computation, e.g.
            for (Leaf leaf : branch.leaves) {
                // Per-leaf computation, e.g.
                setColor(leaf, computedColor);
            }
        }
    } */

    // Implementation details of the individual worker threads
    class WorkerThread extends Thread {

        final List<TreeModel.Branch> branches = new ArrayList<TreeModel.Branch>();
        boolean hasWork = false;
        boolean workDone = false;

        WorkerThread(String name) {
            super(name);
        }

        public void run() {
            while (!isInterrupted()) {
                // Wait until we have work to do...
                synchronized (this) {
                    try {
                        while (!this.hasWork) {
                            wait();
                        }
                    } catch (InterruptedException ix) {
                        // Channel is finished
                        break;
                    }
                    this.hasWork = false;
                }

                // Do our work
                runThread(this.branches, deltaMs);

                // Signal to the main thread that we are done
                synchronized (this) {
                    this.workDone = true;
                    notify();
                }
            }
        }
    }
}
