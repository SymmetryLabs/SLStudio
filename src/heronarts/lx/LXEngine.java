/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx;

import heronarts.lx.effect.LXEffect;
import heronarts.lx.midi.LXMidiSystem;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.output.LXOutput;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.IteratorTestPattern;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.pattern.SolidColorPattern;
import heronarts.lx.transition.LXTransition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The engine is the core class that runs the internal animations. An engine is
 * comprised of top-level modulators, then a number of channels, each of which
 * has a set of patterns that it may transition between. These channels are
 * blended together, and effects are then applied.
 *
 * <pre>
 *   -----------------------------
 *  | Engine                      |
 *  |  -------------------------  |
 *  | | Modulators              | |
 *  |  -------------------------  |
 *  |  --------    --------       |
 *  | | Chnl 0 |  | Chnl 1 |  ... |
 *  |  --------    --------       |
 *  |  -------------------------  |
 *  | | Effects                 | |
 *  |  -------------------------  |
 *   -----------------------------
 * </pre>
 *
 * The result of all this generates a display buffer of node values.
 */
public class LXEngine {

    private final LX lx;

    public final LXMidiSystem midiSystem = new LXMidiSystem();

    private Dispatch inputDispatch = null;

    private final List<LXLoopTask> loopTasks = new ArrayList<LXLoopTask>();
    private final List<LXChannel> channels = new ArrayList<LXChannel>();
    private final List<LXEffect> effects = new ArrayList<LXEffect>();
    private final List<LXOutput> outputs = new ArrayList<LXOutput>();
    private final List<Listener> listeners = new ArrayList<Listener>();

    private final List<LXChannel> unmodifiableChannels = Collections.unmodifiableList(this.channels);
    private final List<LXEffect> unmodifiableEffects = Collections.unmodifiableList(this.effects);

    public final DiscreteParameter focusedChannel = new DiscreteParameter("CHANNEL", 1);

    public final BasicParameter framesPerSecond = new BasicParameter("FPS", 60, 0, 300);

    float frameRate = 0;

    public interface Dispatch {
        public void dispatch();
    }

    public interface Listener {
        public void channelAdded(LXEngine engine, LXChannel channel);
        public void channelRemoved(LXEngine engine, LXChannel channel);
    }

    public final synchronized LXEngine addListener(Listener listener) {
        this.listeners.add(listener);
        return this;
    }

    public final synchronized LXEngine removeListener(Listener listener) {
        this.listeners.remove(listener);
        return this;
    }

    public class Timer {
        public long runNanos = 0;
        public long channelNanos = 0;
        public long copyNanos = 0;
        public long fxNanos = 0;
        public long inputNanos = 0;
        public long midiNanos = 0;
        public long outputNanos = 0;
    }

    public final Timer timer = new Timer();

    /**
     * Utility class for a threaded engine to do buffer-flipping. One buffer is
     * actively being worked on, while another copy is held for shuffling off to
     * another thread.
     */
    private class DoubleBuffer implements LXBuffer {

        // Concrete buffer instances, these are real memory
        private final int[] buffer1;
        private final int[] buffer2;

        // References, these flip between pointing to buffer1 and buffer 2
        private int[] render;
        private int[] copy;

        private DoubleBuffer() {
            this.render = this.buffer1 = new int[lx.total];
            this.copy = this.buffer2 = new int[lx.total];
            for (int i = 0; i < lx.total; ++i) {
                this.buffer1[i] = this.buffer2[i] = 0xff000000;
            }
        }

        private void flip() {
            int[] tmp = this.render;
            this.render = this.copy;
            this.copy = tmp;
        }

        @Override
        public int[] getArray() {
            return this.render;
        }
    }

    private final DoubleBuffer buffer;

    private final int[] black;

    long nowMillis = 0;
    private long lastMillis;

    private boolean isThreaded = false;
    private Thread engineThread = null;

    public final BasicParameter speed = new BasicParameter("SPEED", 1, 0, 2);

    private boolean paused = false;

    LXEngine(LX lx) {
        this.lx = lx;
        this.black = new int[lx.total];
        for (int i = 0; i < black.length; ++i) {
            this.black[i] = 0xff000000;
        }
        this.buffer = new DoubleBuffer();

        addChannel(new LXPattern[] { new IteratorTestPattern(lx) });
        channels.get(0).getFader().setValue(1);
        this.lastMillis = System.currentTimeMillis();
    }

    public LXEngine setInputDispatch(Dispatch inputDispatch) {
        this.inputDispatch = inputDispatch;
        return this;
    }

    /**
     * Gets the active frame rate of the engine when in threaded mode
     *
     * @return How many FPS the engine is running
     */
    public float frameRate() {
        return this.isThreaded ? this.frameRate : 0;
    }

    /**
     * Whether the engine is threaded. Should only be called from Processing
     * animation thread.
     *
     * @return Whether the engine is threaded
     */
    public synchronized boolean isThreaded() {
        return this.isThreaded;
    }

    /**
     * Starts the engine thread.
     */
    public void start() {
        setThreaded(true);
    }

    /**
     * Stops the engine thread.
     */
    public void stop() {
        setThreaded(false);
    }

    /**
     * Sets the engine to threaded or non-threaded mode. Should only be called
     * from the Processing animation thread.
     *
     * @param threaded Whether engine should run on its own thread
     */
    public synchronized LXEngine setThreaded(boolean threaded) {
        if (threaded == this.isThreaded) {
            return this;
        }
        this.isThreaded = threaded;
        if (!threaded) {
            this.engineThread.interrupt();
            try {
                this.engineThread.join();
            } catch (InterruptedException ix) {
            }
            this.engineThread = null;
        } else {
            // Copy the current frame to avoid a black frame
            for (int i = 0; i < this.buffer.render.length; ++i) {
                this.buffer.copy[i] = this.buffer.render[i];
            }
            this.engineThread = new Thread("LX Engine Thread") {
                @Override
                public void run() {
                    System.out.println("LX Engine Thread started.");
                    while (!isInterrupted()) {
                        long frameStart = System.currentTimeMillis();
                        LXEngine.this.run();
                        synchronized (buffer) {
                            buffer.flip();
                        }
                        long frameMillis = System.currentTimeMillis() - frameStart;
                        frameRate = 1000.f / frameMillis;
                        float targetFPS = framesPerSecond.getValuef();
                        if (targetFPS > 0) {
                            long minMillisPerFrame = (long) (1000. / targetFPS);
                            if (frameMillis < minMillisPerFrame) {
                                frameRate = targetFPS;
                                try {
                                    sleep(minMillisPerFrame - frameMillis);
                                } catch (InterruptedException ix) {
                                    break;
                                }
                            }
                        }
                    }
                    System.out.println("LX Engine Thread finished.");
                }
            };
            this.engineThread.start();
        }
        return this;
    }

    public LXEngine setSpeed(double speed) {
        this.speed.setValue(speed);
        return this;
    }

    /**
     * Pause the engine from running
     *
     * @param paused Whether to pause the engine to pause
     */
    public synchronized LXEngine setPaused(boolean paused) {
        this.paused = paused;
        return this;
    }

    public synchronized boolean isPaused() {
        return this.paused;
    }

    public LXEngine addModulator(LXModulator modulator) {
        return addLoopTask(modulator);
    }

    public LXEngine removeModulator(LXModulator modulator) {
        return removeLoopTask(modulator);
    }

    public synchronized LXEngine addLoopTask(LXLoopTask loopTask) {
        this.loopTasks.add(loopTask);
        return this;
    }

    public synchronized LXEngine removeLoopTask(LXLoopTask loopTask) {
        this.loopTasks.remove(loopTask);
        return this;
    }

    public synchronized List<LXEffect> getEffects() {
        return this.unmodifiableEffects;
    }

    public synchronized LXEngine addEffect(LXEffect fx) {
        this.effects.add(fx);
        return this;
    }

    public synchronized LXEngine removeEffect(LXEffect fx) {
        this.effects.remove(fx);
        return this;
    }

    /**
     * Adds an output driver
     *
     * @param output
     * @return this
     */
    public synchronized LXEngine addOutput(LXOutput output) {
        this.outputs.add(output);
        return this;
    }

    /**
     * Removes an output driver
     *
     * @param output
     * @return this
     */
    public synchronized LXEngine removeOutput(LXOutput output) {
        this.outputs.remove(output);
        return this;
    }

    public List<LXChannel> getChannels() {
        return this.unmodifiableChannels;
    }

    public synchronized LXChannel getDefaultChannel() {
        return this.channels.get(0);
    }

    public synchronized LXChannel getChannel(int channelIndex) {
        return this.channels.get(channelIndex);
    }

    public LXChannel getFocusedChannel() {
        return getChannel(focusedChannel.getValuei());
    }

    public LXChannel addChannel() {
        return addChannel(new LXPattern[] { new SolidColorPattern(lx, 0xff000000) });
    }

    public synchronized LXChannel addChannel(LXPattern[] patterns) {
        LXChannel channel = new LXChannel(lx, this.channels.size(), patterns);
        this.channels.add(channel);
        this.focusedChannel.setRange(this.channels.size());
        for (Listener listener : this.listeners) {
            listener.channelAdded(this, channel);
        }
        return channel;
    }

    public synchronized void removeChannel(LXChannel channel) {
        if (this.channels.remove(channel)) {
            int i = 0;
            for (LXChannel c : this.channels) {
                c.setIndex(i++);
            }
            this.focusedChannel.setRange(this.channels.size());
            for (Listener listener : this.listeners) {
                listener.channelRemoved(this, channel);
            }
        }
    }

    public void setPatterns(LXPattern[] patterns) {
        this.getDefaultChannel().setPatterns(patterns);
    }

    public List<LXPattern> getPatterns() {
        return this.getDefaultChannel().getPatterns();
    }

    protected LXPattern getActivePattern() {
        return this.getDefaultChannel().getActivePattern();
    }

    protected LXPattern getNextPattern() {
        return this.getDefaultChannel().getNextPattern();
    }

    protected LXTransition getActiveTransition() {
        return this.getDefaultChannel().getActiveTransition();
    }

    public void goPrev() {
        this.getDefaultChannel().goPrev();
    }

    public final void goNext() {
        this.getDefaultChannel().goNext();
    }

    public void goPattern(LXPattern pattern) {
        this.getDefaultChannel().goPattern(pattern);
    }

    public void goIndex(int index) {
        this.getDefaultChannel().goIndex(index);
    }

    protected void disableAutoTransition() {
        getDefaultChannel().disableAutoTransition();
    }

    protected void enableAutoTransition(int autoTransitionThreshold) {
        getDefaultChannel().enableAutoTransition(autoTransitionThreshold);
    }

    protected boolean isAutoTransitionEnabled() {
        return getDefaultChannel().isAutoTransitionEnabled();
    }

    public synchronized void run() {
        long runStart = System.nanoTime();

        // Compute elapsed time
        this.nowMillis = System.currentTimeMillis();
        double deltaMs = this.nowMillis - this.lastMillis;
        this.lastMillis = this.nowMillis;

        if (this.paused) {
            this.timer.channelNanos = 0;
            this.timer.copyNanos = 0;
            this.timer.fxNanos = 0;
            this.timer.runNanos = System.nanoTime() - runStart;
            return;
        }

        // Run tempo, always using real-time
        this.lx.tempo.loop(deltaMs);

        // Mutate by speed for everything else
        deltaMs *= this.speed.getValue();

        // Run loop tasks
        for (LXLoopTask loopTask : this.loopTasks) {
            loopTask.loop(deltaMs);
        }

        // Run and blend all of our channels
        long channelStart = System.nanoTime();
        int[] bufferColors = this.black;
        for (LXChannel channel : this.channels) {
            if (channel.enabled.isOn()) {
                channel.loop(deltaMs);
                channel.getFaderTransition().timer.blendNanos = 0;

                // This optimization assumed that all transitions do
                // nothing at 0 and completely take over at 1. That's
                // not always the case. Leaving this here for reference.

                // if (channel.getFader().getValue() == 0) {
                // // No blending on this channel, leave colors as they were
                // } else if (channel.getFader().getValue() >= 1) {
                // // Fully faded in, just use this channel
                // bufferColors = channel.getColors();
                // } else {

                // Apply the fader to this channel
                channel.getFaderTransition().loop(deltaMs);
                channel.getFaderTransition().blend(
                    bufferColors,
                    channel.getColors(),
                    channel.getFader().getValue()
                );
                bufferColors = channel.getFaderTransition().getColors();
            }

        }
        this.timer.channelNanos = System.nanoTime() - channelStart;

        // Copy colors into our own rendering buffer
        long copyStart = System.nanoTime();
        for (int i = 0; i < bufferColors.length; ++i) {
            this.buffer.render[i] = bufferColors[i];
        }
        this.timer.copyNanos = System.nanoTime() - copyStart;

        // Apply effects in our rendering buffer
        long fxStart = System.nanoTime();
        for (LXEffect fx : this.effects) {
            ((LXLayerComponent) fx).setBuffer(this.buffer);
            fx.loop(deltaMs);
        }
        this.timer.fxNanos = System.nanoTime() - fxStart;

        // Process UI input events
        if (this.inputDispatch == null) {
            this.timer.inputNanos = 0;
        } else {
            long inputStart = System.nanoTime();
            this.inputDispatch.dispatch();
            this.timer.inputNanos = System.nanoTime() - inputStart;
        }

        // Process MIDI events
        long midiStart = System.nanoTime();
        this.midiSystem.dispatch();
        this.timer.midiNanos = System.nanoTime() - midiStart;

        // Send to outputs
        long outputStart = System.nanoTime();
        for (LXOutput output : this.outputs) {
            output.send(this.buffer.render);
        }
        this.timer.outputNanos = System.nanoTime() - outputStart;

        this.timer.runNanos = System.nanoTime() - runStart;
    }

    /**
     * This should be used when in threaded mode. It synchronizes on the
     * double-buffer and duplicates the internal copy buffer into the provided
     * buffer.
     *
     * @param copy Buffer to copy into
     */
    public void copyBuffer(int[] copy) {
        synchronized (this.buffer) {
            for (int i = 0; i < this.buffer.copy.length; ++i) {
                copy[i] = this.buffer.copy[i];
            }
        }
    }

    /**
     * This is used when not in threaded mode. It provides direct access to the
     * engine's render buffer.
     *
     * @return The internal render buffer
     */
    int[] renderBuffer() {
        return this.buffer.render;
    }
}
