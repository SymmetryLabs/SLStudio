package com.symmetrylabs.slstudio.render;

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;

import java.util.Arrays;

public class InterpolatingRenderer extends Renderer {
    private final SLModel.PointBatches pointBatches;
    private int[] frameA, frameB, active;
    private double lastRenderTimeMillis;
    private double lastRenderElapsedMillis;

    private RenderThread renderThread;
    private int fps = 60;

    private boolean runLoopStarted = false;

    public InterpolatingRenderer(LXFixture fixture, int[] colors, com.symmetrylabs.slstudio.render.Renderable renderable) {
        super(fixture, colors, renderable);

        frameA = new int[colors.length];
        frameB = new int[colors.length];
        active = new int[colors.length];

        pointBatches = new SLModel.PointBatches(points, SLModel.PointBatches.NUM_POINT_BATCHES);
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
        double f = (t - lastRenderTimeMillis) / lastRenderElapsedMillis;
        final double fFinal = f > 1 ? 1 : f;
        //System.out.println("Showing: " + frameA + " and " + frameB + " (" + f + ")");

        pointBatches.forEachPoint((start, end) -> {
            for (int i = start; i < end; i++) {
                final LXPoint point = points.get(i);

                int c1 = frameA[point.index];
                int c2 = frameB[point.index];
          /*
                colors[point.index] = LXColor.rgb(
                        (int)(LXColor.red(c1) * f + LXColor.red(c2) * (1 - f)),
                        (int)(LXColor.green(c1) * f + LXColor.green(c2) * (1 - f)),
                        (int)(LXColor.blue(c1) * f + LXColor.blue(c2) * (1 - f))
                );
                */
                colors[point.index] = LXColor.lerp(c1, c2, fFinal);
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
                    } catch (InterruptedException e) { /* pass */ }

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
                    } catch (InterruptedException e) { /* pass */ }
                }
            }
        }
    }
}
