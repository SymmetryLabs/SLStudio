package com.symmetrylabs.pattern;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.nio.IntBuffer;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.LXPattern;

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

    protected int render(LXPoint p) {
        return LXColor.BLACK;
    }

    protected void render(List<LXPoint> points, IntBuffer pointColors) {
        for (int i = 0; i < points.size(); ++i) {
            pointColors.put(i, render(points.get(i)));
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
            }
            catch (InterruptedException e) { /* pass */ }
        }

        public void shutdown() {
            running = false;
            waitRender.release();
            interrupt();
        }

        @Override
        public void run() {
            while (running) {
                try {
                    triggerRender.acquire();
                }
                catch (InterruptedException e) {
                    continue;
                }

                int numThreads = renderThreads.size();
                int pointCount = lx.model.points.length;
                int startInclusive = pointCount * index / numThreads;
                int endExclusive = pointCount * (index + 1) / numThreads;

                List<LXPoint> points = Arrays.asList(lx.model.points).subList(startInclusive, endExclusive);
                IntBuffer pointColors = IntBuffer.wrap(colors, startInclusive, endExclusive - startInclusive).slice();

                render(points, pointColors);

                for (int i = 0; i < points.size(); ++i) {
                    LXPoint p = points.get(i);
                    colors[p.index] = pointColors.get(i);
                }

                waitRender.release();
            }
        }
    }
}
