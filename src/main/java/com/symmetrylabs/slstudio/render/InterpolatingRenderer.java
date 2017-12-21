package com.symmetrylabs.slstudio.render;

import java.util.Arrays;

import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.util.MathUtils;
import com.symmetrylabs.util.TripleBuffer;

public class InterpolatingRenderer extends Renderer {
    private final TripleBuffer<RenderFrame> tripleBuffer;

    RenderFrame frameA, frameB;

    private long lastRenderTimeNanos;
    private long lastRenderElapsedNanos;

    private RenderThread renderThread;
    private int fps = 60;

    private volatile boolean runLoopStarted = false;

    public InterpolatingRenderer(LXFixture fixture, int[] colors, com.symmetrylabs.slstudio.render.Renderable renderable) {
        super(fixture, colors, renderable);

        tripleBuffer = new TripleBuffer<>(() -> new RenderFrame(colors.length));
        frameA = tripleBuffer.getSnapshotBuffer();
        frameB = frameA;
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
    public void run(double deltaMs) {
        synchronized (this) {
            runLoopStarted = true;
        }

        RenderFrame frame = tripleBuffer.takeSnapshot();
        //System.arraycopy(frame, 0, colors, 0, frame.length);

        if (frame.renderEndNanos != frameB.renderEndNanos) {
            //System.out.println("SNAPSHOT");
            frameA = frameB;
            frameB = frame.copy();
        }

        double f = (System.nanoTime() - frameB.renderEndNanos) / (double)(frameB.renderEndNanos - frameA.renderEndNanos);
        //System.out.println(f);
        //System.out.println(frameA);
        //System.out.println(frameB);

        final double fFinal = f > 1 ? 1 : f < 0 ? 0 : f;
        Arrays.parallelSetAll(colors, i -> LXColor.lerp(frameA.buffer[i], frameB.buffer[i], fFinal));
    }

    private class RenderFrame {
        public int[] buffer;
        public volatile long renderStartNanos;
        public volatile long renderEndNanos;

        public RenderFrame(int n) {
            buffer = new int[n];
        }

        public RenderFrame copy() {
            RenderFrame f = new RenderFrame(buffer.length);
            System.arraycopy(buffer, 0, f.buffer, 0, buffer.length);
            f.renderStartNanos = renderStartNanos;
            f.renderEndNanos = renderEndNanos;
            return f;
        }

        public String toString() {
            return "RenderFrame[ buffer=" + buffer
                + " renderStartNanos=" + renderStartNanos
                + " renderEndNanos=" + renderEndNanos + "]";
        }
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

                RenderFrame active = tripleBuffer.getWriteBuffer();

                active.renderStartNanos = timeNanos;

                Arrays.fill(active.buffer, 0);
                renderable.render(deltaMs, points, active.buffer);

                long renderEndNanos = System.nanoTime();
                active.renderEndNanos = renderEndNanos;

                long elapsedNanos = renderEndNanos - lastTimeNanos;
                lastTimeNanos = timeNanos;

                tripleBuffer.flipWriteBuffer();
                //System.out.println("FLIP WRITER");

                //System.out.println("Rendering: " + active);

                sleepNanosFromElapsed(elapsedNanos);
            }
        }
    }
}
