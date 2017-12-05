package com.symmetrylabs.render;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.concurrent.Semaphore;

import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXFixture;
import heronarts.lx.color.LXColor;

public class InterpolatingRenderer extends Renderer {
    private int[] frameA, frameB, active;
    private double lastRenderTimeMillis;
    private double lastRenderElapsedMillis;

    private RenderThread renderThread;
    private int fps = 60;

    private boolean runLoopStarted = false;

    public InterpolatingRenderer(LXFixture fixture, int[] colors, Renderable renderable) {
        super(fixture, colors, renderable);

        frameA = new int[colors.length];
        frameB = new int[colors.length];
        active = new int[colors.length];
    }

    @Override
    public synchronized void start() {
        renderThread = new RenderThread();
        renderThread.start();
    }

    @Override
    public synchronized void stop() {
        if (renderThread != null) {
            renderThread.shutdown();
            renderThread = null;
            runLoopStarted = false;
        }
    }

    public void setFPS(int fps) {
        this.fps = fps;
    }

    public int getFPS() {
        return fps;
    }

    @Override
    public synchronized void run(double deltaMs) {
        runLoopStarted = true;

        double t = System.nanoTime() / 1000000.0;
        final double f = (t - lastRenderTimeMillis) / lastRenderElapsedMillis;
        //System.out.println("Showing: " + frameA + " and " + frameB + " (" + f + ")");
        points.parallelStream().forEach(new Consumer<LXPoint>() {
            public void accept(LXPoint point) {
                colors[point.index] = LXColor.lerp(frameA[point.index], frameB[point.index], f);
            }
        });
    }

    private class RenderThread extends Thread {
        private boolean running = true;

        public void shutdown() {
            running = false;
            interrupt();
        }

        @Override
        public void run() {
            long lastTimeNanos = System.nanoTime();
            while (running) {
                if (!runLoopStarted) {
                    try {
                        sleep(1000 / fps);
                    }
                    catch (InterruptedException e) { /* pass */ }

                    lastTimeNanos = System.nanoTime();
                    continue;
                }

                long timeNanos = System.nanoTime();
                double deltaMs = (timeNanos - lastTimeNanos) / 1000000.0;

                Arrays.fill(active, 0);
                renderable.render(deltaMs, points, active);

                long elapsedNanos = System.nanoTime() - lastTimeNanos;
                lastTimeNanos = timeNanos;

                synchronized (InterpolatingRenderer.this) {
                    int[] temp = frameB;
                    frameB = active;
                    active = frameA;
                    frameA = temp;

                    lastRenderTimeMillis = lastTimeNanos / 1000000.0;
                    lastRenderElapsedMillis = elapsedNanos / 1000000.0;
                }

                //System.out.println("Rendering: " + active);

                long periodNanos = 1000000 / fps;
                if (elapsedNanos < periodNanos) {
                    try {
                        sleep((periodNanos - elapsedNanos) / 1000000);
                    }
                    catch (InterruptedException e) { /* pass */ }
                }
            }
        }
    }
}
