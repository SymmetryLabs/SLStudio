package com.symmetrylabs.util;

import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

public abstract class LayeredRenderer {
    private static final int DEFAULT_THREAD_COUNT = 4;

    private final LXFixture fixture;
    private final int numThreads;
    private final List<RenderThread> renderThreads = new ArrayList<>();

    private final List<LXPoint> points;
    private final int[] colors;

    public LayeredRenderer(LXFixture fixture, int[] colors) {
        this(fixture, colors, DEFAULT_THREAD_COUNT);
    }

    public LayeredRenderer(LXFixture fixture, int[] colors, int numThreads) {
        this.fixture = fixture;
        this.colors = colors;
        this.numThreads = numThreads;

        points = fixture.getPoints();

        for (int i = renderThreads.size(); i < numThreads; ++i) {
            RenderThread rt = new RenderThread(i);
            renderThreads.add(rt);
            rt.start();
        }
    }

    protected abstract void render(double deltaMs, List<LXPoint> points, int[] layer, int threadIndex, int numThreads);

    public void run(double deltaMs) {
        for (RenderThread rt : renderThreads) {
            rt.startRender();
        }
        for (RenderThread rt : renderThreads) {
            rt.waitFinished();
        }

        Arrays.fill(colors, 0);

        for (RenderThread rt : renderThreads) {
            for (int i = 0; i < colors.length; ++i) {
                colors[i] = LXColor.blend(colors[i], rt.layer[i], LXColor.Blend.ADD);
            }
        }
    }

    private class RenderThread extends Thread {
        private boolean running = true;

        protected final int index;

        public final int[] layer;

        private final Semaphore triggerRender = new Semaphore(0);
        private final Semaphore waitRender = new Semaphore(0);

        public RenderThread(int index) {
            this.index = index;

            layer = new int[colors.length];
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

                long t = System.currentTimeMillis();
                double deltaMs = t - lastTime;

                render(deltaMs, points, layer, index, numThreads);

                lastTime = t;

                waitRender.release();
            }
        }
    }
}
