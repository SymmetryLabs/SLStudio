package com.symmetrylabs.slstudio.pattern.base;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

public abstract class ThreadedPattern extends LXPattern {
    private static final int DEFAULT_THREAD_COUNT = 4;

    private List<RenderThread> renderThreads = new ArrayList<RenderThread>();

    //public final MutableParameter threadCount = new MutableParameter("thread-count", 0);

    public ThreadedPattern(LX lx) {
        this(lx, DEFAULT_THREAD_COUNT);
    }

    public ThreadedPattern(LX lx, int numThreads) {
        super(lx);

        for (int i = renderThreads.size(); i < numThreads; ++i) {
            RenderThread rt = new RenderThread(i);
            renderThreads.add(rt);
            rt.start();
        }
    }

    protected int render(double deltaMs, LXVector v) {
        return LXColor.BLACK;
    }

    protected void render(double deltaMs, List<LXVector> vectors, int[] colors) {
        for (int i = 0; i < vectors.size(); ++i) {
            LXVector v = vectors.get(i);
            colors[v.index] = render(deltaMs, v);
        }
    }

    @Override
    public void run(double deltaMs) {
        for (RenderThread rt : renderThreads) {
            rt.startRender();
        }
        for (RenderThread rt : renderThreads) {
            rt.waitFinished();
        }
    }

    private class RenderThread extends Thread {
        private final int index;
        private boolean running = true;

        private final Semaphore triggerRender = new Semaphore(0);
        private final Semaphore waitRender = new Semaphore(0);

        public RenderThread(int index) {
            this.index = index;
        }

        public void startRender() {
            triggerRender.release();
        }

        public void waitFinished() {
            try {
                waitRender.acquire();
            } catch (InterruptedException e) { /* pass */ }
        }

        public void shutdown() {
            running = false;
            waitRender.release();
            interrupt();
        }

        @Override
        public void run() {
            long lastTime = System.currentTimeMillis();
            while (running) {
                try {
                    triggerRender.acquire();
                } catch (InterruptedException e) {
                    continue;
                }

                int numThreads = renderThreads.size();
                List<LXVector> vectors = getVectorList();
                int pointCount = vectors.size();
                int startInclusive = pointCount * index / numThreads;
                int endExclusive = pointCount * (index + 1) / numThreads;
                List<LXVector> slice = vectors.subList(startInclusive, endExclusive);

                long t = System.currentTimeMillis();
                double deltaMs = t - lastTime;
                render(deltaMs, slice, colors);
                lastTime = t;

                waitRender.release();
            }
        }
    }
}
