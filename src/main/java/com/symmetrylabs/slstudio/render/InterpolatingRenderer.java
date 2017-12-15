package com.symmetrylabs.slstudio.render;

import java.util.Arrays;

import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.util.MathUtils;

public class InterpolatingRenderer extends Renderer {
    private int[] frameA, frameB, active;
    private long lastRenderTimeNanos;
    private long lastRenderElapsedNanos;

    private RenderThread renderThread;
    private int fps = 60;

    private boolean runLoopStarted = false;

    public InterpolatingRenderer(LXFixture fixture, int[] colors, com.symmetrylabs.slstudio.render.Renderable renderable) {
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

        double f = (System.nanoTime() - lastRenderTimeNanos) / (double)lastRenderElapsedNanos;
        //System.out.println("Showing: " + frameA + " and " + frameB + " (" + f + ")");

        final double fFinal = f > 1 ? 1 : f;
        points.parallelStream().forEach(point -> {
            int c1 = frameA[point.index];
            int c2 = frameB[point.index];
            colors[point.index] = LXColor.lerp(c1, c2, fFinal);
        });
    }

    private class RenderThread extends Thread {
        private boolean running = true;

        public void shutdown() {
            running = false;
            interrupt();
        }

        private void sleepNanosFromElapsed(long elapsedNanos) {
            long periodNanos = 1_000_000_000l / fps;
            if (elapsedNanos >= periodNanos)
                return;

            try {
                long nanos = periodNanos - elapsedNanos;
                long millisPart = nanos / 1_000_000l;
                sleep(millisPart, (int)(nanos - millisPart * 1_000_000l));
            }
            catch (InterruptedException e) { /* pass */ }
        }

        @Override
        public void run() {
            long lastTimeNanos = System.nanoTime();
            while (running) {
                if (!runLoopStarted) {
                    sleepNanosFromElapsed(0);
                    lastTimeNanos = System.nanoTime();
                    continue;
                }

                long timeNanos = System.nanoTime();
                double deltaMs = (timeNanos - lastTimeNanos) / 1_000_000d;

                Arrays.fill(active, 0);
                renderable.render(deltaMs, points, active);

                long elapsedNanos = System.nanoTime() - lastTimeNanos;
                lastTimeNanos = timeNanos;

                synchronized (InterpolatingRenderer.this) {
                    int[] temp = frameB;
                    frameB = active;
                    active = frameA;
                    frameA = temp;

                    lastRenderTimeNanos = lastTimeNanos;
                    lastRenderElapsedNanos = elapsedNanos;
                }

                //System.out.println("Rendering: " + active);

                sleepNanosFromElapsed(elapsedNanos);
            }
        }
    }
}
