package com.symmetrylabs.slstudio.render;

import java.util.Arrays;

import heronarts.lx.model.LXFixture;

import com.symmetrylabs.util.TripleBuffer;

public class TripleBufferedRenderer extends Renderer {
    protected final TripleBuffer<RenderFrame> tripleBuffer;

    private RenderThread renderThread;
    private int fps = 60;

    // TODO: why is this even necessary?
    protected volatile boolean runLoopStarted = false;

    public TripleBufferedRenderer(LXFixture fixture, int[] colors, com.symmetrylabs.slstudio.render.Renderable renderable) {
        super(fixture, colors, renderable);

        tripleBuffer = new TripleBuffer<>(() -> new RenderFrame(colors.length));
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

        if (!tripleBuffer.hasSnapshotChanged())
            return;

        RenderFrame frame = tripleBuffer.takeSnapshot();
        System.arraycopy(frame.buffer, 0, colors, 0, frame.buffer.length);
    }

    protected class RenderFrame {
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
                renderable.render(deltaMs, vectors, active.buffer);

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
