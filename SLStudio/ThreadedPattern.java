package com.symmetrylabs.pattern;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

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

    protected int[] render(List<LXPoint> cs) {
        int[] pointColors = new int[cs.size()];
        for (int i = 0; i < cs.size(); ++i) {
            pointColors[i] = render(cs.get(i));
        }
        return pointColors;
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
                List<LXPoint> points = new ArrayList<LXPoint>();
                for (int i = pointCount * index / numThreads; i < pointCount * (index + 1) / numThreads; ++i) {
                    LXPoint p = lx.model.points[i];
                    points.add(p);
                }

                int[] pointColors = render(points);
                for (int i = 0; i < points.size(); ++i) {
                    LXPoint p = points.get(i);
                    colors[p.index] = pointColors[i];
                }

                waitRender.release();
            }
        }
    }
}
