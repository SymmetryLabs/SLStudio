/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.symmetrylabs.util.artnet.ArtNetEngine;
import com.symmetrylabs.util.dmx.DMXEngine;
import com.symmetrylabs.util.dmx.LXEngineDMXManager;
import heronarts.lx.audio.LXAudioEngine;
import heronarts.lx.blend.AddBlend;
import heronarts.lx.blend.DarkestBlend;
import heronarts.lx.blend.DifferenceBlend;
import heronarts.lx.blend.DissolveBlend;
import heronarts.lx.blend.LXBlend;
import heronarts.lx.blend.LightestBlend;
import heronarts.lx.blend.MultiplyBlend;
import heronarts.lx.blend.NormalBlend;
import heronarts.lx.blend.SubtractBlend;
import heronarts.lx.midi.LXMidiEngine;
import heronarts.lx.model.LXPoint;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.osc.LXOscEngine;
import heronarts.lx.output.LXOutput;
import heronarts.lx.output.LXOutputGroup;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.script.LXScriptEngine;
import heronarts.lx.warp.LXWarp;
import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.commons.collections4.iterators.IteratorIterable;
import java.util.Collection;
import heronarts.lx.mutation.LXMutationQueue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static heronarts.lx.PolyBuffer.Space.RGB16;
import static heronarts.lx.PolyBuffer.Space.SRGB8;

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
public class LXEngine extends LXComponent implements LXOscComponent, LXModulationComponent {

    private final LX lx;

    public final LXMidiEngine midi;

    public final LXAudioEngine audio;

    public final LXMappingEngine mapping = new LXMappingEngine();

    public final LXOscEngine osc;

    public final ArtNetEngine artNet;
    public final DMXEngine dmx;

    public final LXScriptEngine script;

    public final LXMutationQueue mutations;

    private Dispatch inputDispatch = null;

    private final List<LXLoopTask> loopTasks = new ArrayList<LXLoopTask>();
    private final List<Runnable> networkTaskQueue = new ArrayList<Runnable>();
    private final List<Runnable> threadSafeTaskQueue = Collections.synchronizedList(new ArrayList<Runnable>());
    private final List<Runnable> engineThreadTaskQueue = new ArrayList<Runnable>();
    private final Map<String, LXComponent> components = new HashMap<String, LXComponent>();

    private final List<LXLook> mutableLooks = new ArrayList<>();
    public final List<LXLook> looks = Collections.unmodifiableList(this.mutableLooks);

    public final LXMasterChannel masterChannel;

    public final Output output;

    private final List<MessageListener> messageListeners = new ArrayList<MessageListener>();

    public final BoundedParameter framesPerSecond = new BoundedParameter("FPS", 60, 0, 300);
    public int conversionsPerFrame = 0;

    LXBlend[] channelBlends;
    private final AddBlend addBlend;

    final LXBlend[] crossfaderBlends;

    public final BoundedParameter speed =
        new BoundedParameter("Speed", 1, 0, 2)
        .setDescription("Overall speed adjustement to the entire engine (does not apply to master tempo and audio)");

    /** The color space that the engine renders to. */
    public final EnumParameter<PolyBuffer.Space> colorSpace =
            new EnumParameter<>("Color Space", RGB16)
                    .setDescription("Selects the color space for the engine");

    public final LXModulationEngine modulation;

    private boolean logTimers = false;

    private float frameRate = 0;

    /* the number of frames we've processed */
    private int tickCount = 0;

    public class Output extends LXOutputGroup implements LXOscComponent {
        Output(LX lx) {
            super(lx);
        }

        public String getOscAddress() {
            return "/lx/output";
        }

        public void onSend(PolyBuffer src) { }
    }

    public interface Dispatch {
        public void dispatch();
    }

    @Deprecated
    public interface MessageListener {
        public void onMessage(LXEngine engine, String message);
    }

    @Deprecated
    public final LXEngine addMessageListener(MessageListener listener) {
        this.messageListeners.add(listener);
        return this;
    }

    @Deprecated
    public final LXEngine removeMessageListener(MessageListener listener) {
        this.messageListeners.remove(listener);
        return this;
    }

    @Deprecated
    public LXEngine broadcastMessage(String message) {
        for (MessageListener listener : this.messageListeners) {
            listener.onMessage(this, message);
        }
        return this;
    }

    public class TimerStatistics {
        private class TimerPair {
            long elapsedNanos;
            long recordedAtNanos;
            TimerPair(long elapsedNanos, long recordedAtNanos) {
                this.elapsedNanos = elapsedNanos;
                this.recordedAtNanos = recordedAtNanos;
            }
        }

        private static final long KEEP_TIME_PAIRS_FOR_NANOS = 100000000l;
        private static final long KEEP_TIME_PAIRS_FOR_AVG_NANOS = 5000000000l;
        private static final long KEEP_TIME_PAIRS_COUNT = 100;
        private final Queue<TimerPair> runTimerPairs = new ArrayDeque<>();

        /** The time taken in the last run */
        public long lastNanos = 0;
        /** Technically, both current and avg are averages, but currentNanos
         *  averages over a much smaller window of time and is meant to
         *  represent a stable view of what is happening right now. */
        public long currentNanos = 0;
        /** A longer-windowed running average */
        public long avgNanos = 0;
        /** The best time in the queue (using the AVG_NANOS window) */
        public long bestNanos = 0;
        /** The worst time in the queue (using the AVG_NANOS window) */
        public long worstNanos = 0;

        public void addTime(long elapsedNanos, long nowNanos) {
            long expireNanos = nowNanos - KEEP_TIME_PAIRS_FOR_NANOS;
            long expireAvgNanos = nowNanos - KEEP_TIME_PAIRS_FOR_AVG_NANOS;
            while (this.runTimerPairs.size() > KEEP_TIME_PAIRS_COUNT
                            && this.runTimerPairs.peek().recordedAtNanos < expireAvgNanos) {
                this.runTimerPairs.remove();
            }
            this.runTimerPairs.add(new TimerPair(elapsedNanos, nowNanos));
            long accum = 0;
            int count = 0;
            long accumAvg = 0;
            long worst = 0;
            long best = Long.MAX_VALUE;
            for (TimerPair pair : this.runTimerPairs) {
                if (pair.recordedAtNanos > expireNanos) {
                    accum += pair.elapsedNanos;
                    count++;
                }
                accumAvg += pair.elapsedNanos;
                if (pair.elapsedNanos > worst) {
                    worst = pair.elapsedNanos;
                }
                if (pair.elapsedNanos < best) {
                    best = pair.elapsedNanos;
                }
            }
            this.lastNanos = elapsedNanos;
            this.currentNanos = count > 0 ? accum / count : elapsedNanos;
            this.avgNanos = accumAvg / this.runTimerPairs.size();
            this.worstNanos = worst;
            this.bestNanos = best;
        }
    }

    public class Timer {
        private TimerStatistics ts = new TimerStatistics();
        public long runNanos = 0;
        public long runLastNanos = 0;
        public long runCurrentNanos = 0;
        public long runAvgNanos = 0;
        public long runBestNanos = 0;
        public long runWorstNanos = 0;
        public long channelNanos = 0;
        public long fxNanos = 0;
        public long inputNanos = 0;
        public long midiNanos = 0;
        public long oscNanos = 0;
        public long artNetNanos = 0;
        public long outputNanos = 0;

        private void addRunTime(long runNanos, long nowNanos) {
            ts.addTime(runNanos, nowNanos);
            this.runLastNanos = ts.lastNanos;
            this.runCurrentNanos = ts.currentNanos;
            this.runAvgNanos = ts.avgNanos;
            this.runWorstNanos = ts.worstNanos;
            this.runBestNanos = ts.bestNanos;
        }
    }

    public final Timer timer = new Timer();

    class EngineBuffer {
        boolean cueOn;
        DoubleBuffer main;
        DoubleBuffer cue;

        EngineBuffer(LX lx) {
            this.main = new DoubleBuffer(lx);
            this.cue = new DoubleBuffer(lx);
        }

        void sync() {
            PolyBuffer.Space space = colorSpace.getEnum();
            main.frozen.copyFrom(main.render, space);
            cue.frozen.copyFrom(cue.render, space);
        }

        void flip() {
            this.main.flip();
            this.cue.flip();
        }

        class DoubleBuffer {
            PolyBuffer render;
            PolyBuffer frozen;

            DoubleBuffer(LX lx) {
                this.render = new PolyBuffer(lx);
                this.frozen = new PolyBuffer(lx);
            }

            void flip() {
                PolyBuffer tmp = this.frozen;
                this.frozen = this.render;
                this.render = tmp;
            }

            void copyTo(PolyBuffer dest, PolyBuffer.Space space) {
                dest.copyFrom(frozen, space);
            }
        }
    }

    private final PolyBuffer black;  // always black, read-only
    private final EngineBuffer buffer;  // destination buffers for final output

    public final BooleanParameter isMultithreaded = (BooleanParameter) new BooleanParameter("Threaded", false)
        .setDescription("Whether the engine and UI are on separate threads")
        .setSupportsOscTransmit(false);

    public final BooleanParameter isChannelMultithreaded = (BooleanParameter) new BooleanParameter("Channel Threaded", false)
        .setDescription("Whether the engine is multi-threaded per channel")
        .setSupportsOscTransmit(false);

    public final BooleanParameter isNetworkMultithreaded = (BooleanParameter) new BooleanParameter("Network Threaded", false)
        .setDescription("Whether the network output is on a separate thread")
        .setSupportsOscTransmit(false);

    private volatile boolean isEngineThreadRunning = false;

    private boolean isNetworkThreadStarted = false;
    public final NetworkThread network;

    private EngineThread engineThread = null;

    private boolean hasStarted = false;

    private boolean paused = false;

    private static final long INIT_RUN = -1;
    private long lastMillis = INIT_RUN;
    long nowMillis = System.currentTimeMillis();

    LXEngine(final LX lx) {
        super(lx, LXComponent.ID_ENGINE, "Engine");
        LX.initTimer.log("Engine: Init");
        this.lx = lx;

        // An all-black buffer is used as the initial base for blending.
        // Its arrays are filled with zeroes on allocation and then never modified.
        black = new PolyBuffer(lx);

        // Blending buffers
        buffer = new EngineBuffer(lx);

        // Initialize network thread (don't start it yet)
        this.network = new NetworkThread(lx);

        LX.initTimer.log("Engine: Buffers");

        // Channel blend modes
        this.channelBlends = new LXBlend[] {
            this.addBlend = new AddBlend(lx),
            new MultiplyBlend(lx),
            new SubtractBlend(lx),
            new DifferenceBlend(lx),
            new NormalBlend(lx)
        };
        // Crossfader blend mode
        this.crossfaderBlends = new LXBlend[] {
            new DissolveBlend(lx),
            new AddBlend(lx),
            new MultiplyBlend(lx),
            new LightestBlend(lx),
            new DarkestBlend(lx),
            new DifferenceBlend(lx)
        };
        LX.initTimer.log("Engine: Blends");

        // Modulation matrix
        this.modulation = new LXModulationEngine(lx, this);
        LX.initTimer.log("Engine: Modulation");

        // Master channel
        this.masterChannel = new LXMasterChannel(lx);
        LX.initTimer.log("Engine: Master Channel");

        // Master output
        this.output = new Output(lx);
        LX.initTimer.log("Engine: Output");

        // Midi engine
        this.midi = new LXMidiEngine(lx);
        LX.initTimer.log("Engine: Midi");

        this.audio = new LXAudioEngine(lx);
        LX.initTimer.log("Engine: Audio");

        // OSC engine
        this.osc = new LXOscEngine(lx);
        LX.initTimer.log("Engine: Osc");

        this.artNet = new ArtNetEngine(lx);
        this.dmx = this.artNet.getDMXEngine();
        LXEngineDMXManager.configure(this);
        LX.initTimer.log("Engine: ArtNet/DMX");

        // Script engine
        this.script = new LXScriptEngine(lx);
        LX.initTimer.log("Engine: Script");

        // Remote control
        this.mutations = new LXMutationQueue(lx);
        LX.initTimer.log("Engine: Remote control");

        // Parameters
        addParameter("speed", this.speed);
        addParameter("multithreaded", this.isMultithreaded);
        addParameter("channelMultithreaded", this.isChannelMultithreaded);
        addParameter("networkMultithreaded", this.isNetworkMultithreaded);
    }

    public void logTimers() {
        this.logTimers = true;
    }

    public int getTickCount() {
        return tickCount;
    }

    @Override
    public String getOscAddress() {
        return "/lx/engine";
    }

    public LXEngine setInputDispatch(Dispatch inputDispatch) {
        this.inputDispatch = inputDispatch;
        return this;
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        super.onParameterChanged(p);
        if (p == this.isNetworkMultithreaded) {
            if (this.isNetworkMultithreaded.isOn()) {
                synchronized (this.buffer) {
                    this.buffer.sync();
                }
                if (!this.isNetworkThreadStarted) {
                    this.isNetworkThreadStarted = true;
                    this.network.start();
                }
            }
        }
    }

    /**
     * Sets the blend modes available to the channel mixer
     *
     * @param channelBlends
     * @return this
     */
    public LXEngine setChannelBlends(LXBlend[] channelBlends) {
        if (this.hasStarted) {
            throw new UnsupportedOperationException("setChannelBlends() may only be invoked before engine has started");
        }
        this.channelBlends = channelBlends;
        for (LXChannel channel : getAllSubChannels()) {
            channel.blendMode.setObjects(channelBlends);
        }
        return this;
    }

    /**
     * Gets the active frame rate of the engine when in threaded mode
     *
     * @return How many FPS the engine is running
     */
    public float frameRate() {
        return this.frameRate;
    }

    /**
     * Whether the engine is threaded. Generally, this should only be called
     * from the Processing animation thread.
     *
     * @return Whether the engine is threaded
     */
    public boolean isThreaded() {
        return this.isEngineThreadRunning;
    }

    public boolean isEngineThreadRunning() {
        return isThreaded() && engineThread.isAlive();
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
     * @return this
     */
    public LXEngine setThreaded(boolean threaded) {
        this.isMultithreaded.setValue(threaded);
        return this;
    }

    public void onDraw() {
        if (this.isMultithreaded.isOn() != this.isEngineThreadRunning) {
            _setThreaded(this.isMultithreaded.isOn());
        }
    }

    private synchronized void _setThreaded(boolean threaded) {
        if (threaded == this.isEngineThreadRunning) {
            return;
        }
        if (!threaded) {
            // Set interrupt flag on the engine thread
            EngineThread engineThread = this.engineThread;
            engineThread.interrupt();
            // Called from another thread? If so, wait for engine thread to finish
            if (Thread.currentThread() != engineThread) {
                try {
                    engineThread.join();
                } catch (InterruptedException ix) {
                    throw new IllegalThreadStateException("Interrupted waiting to join LXEngine thread");
                }
            }
        } else {
            // Synchronize the two buffers, flip so that the engine thread doesn't start
            // rendering over the top of the buffer that the UI thread might be currently
            // working on drawing
            this.buffer.sync();
            this.buffer.flip();
            this.isEngineThreadRunning = true;
            this.engineThread = new EngineThread();
            this.engineThread.start();
        }
    }

    private class EngineThread extends Thread {

        private EngineThread() {
            super("LXEngine Render Thread");
        }

        @Override
        public void run() {
            System.out.println("LXEngine Render Thread started");
            while (!isInterrupted()) {
                long frameStart = System.currentTimeMillis();
                LXEngine.this.run();
                if (isInterrupted()) {
                    break;
                };

                // Sleep until next frame
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
                            // We're done!
                            break;
                        }
                    }
                }
            }

            // We are done threading
            frameRate = 0;
            engineThread = null;
            isEngineThreadRunning = false;

            System.out.println("LXEngine Render Thread finished");
        }
    }

    /**
     * Sets a global speed factor on the core animation engine.
     * This does not impact the tempo object.
     *
     * @param speed
     * @return
     */
    public LXEngine setSpeed(double speed) {
        this.speed.setValue(speed);
        return this;
    }

    /**
     * Pause the engine from running
     *
     * @param paused Whether to pause the engine to pause
     * @return this
     */
    public LXEngine setPaused(boolean paused) {
        this.paused = paused;
        return this;
    }

    /*
     * Whether execution of the engine is paused.
     */
    public boolean isPaused() {
        return this.paused;
    }

    /**
     * Register a component with the engine. It will be saved and loaded.
     */
    public LXEngine registerComponent(String key, LXComponent component) {
        this.components.put(key, component);
        return this;
    }

    /**
     * Add a task to be run once on the engine thread.
     *
     * @param runnable Task to run
     * @return this
     */
    public LXEngine addTask(Runnable runnable) {
        this.threadSafeTaskQueue.add(runnable);
        return this;
    }

    /**
     * Add a task to be run on every loop of the engine thread.
     *
     * @param loopTask
     * @return
     */
    public LXEngine addLoopTask(LXLoopTask loopTask) {
        if (this.loopTasks.contains(loopTask)) {
            throw new IllegalStateException("Cannot add task to engine twice: " + loopTask);
        }
        this.loopTasks.add(loopTask);
        return this;
    }

    /**
     * Remove a task from the list run on every loop invocation
     *
     * @param loopTask
     * @return
     */
    public LXEngine removeLoopTask(LXLoopTask loopTask) {
        this.loopTasks.remove(loopTask);
        return this;
    }

    /**
     * Add a task to be run on every loop of the network thread.
     *
     * @param runnable
     * @return
     */
    public LXEngine addNetworkTask(Runnable runnable) {
        if (this.networkTaskQueue.contains(runnable)) {
            throw new IllegalStateException("Cannot add task to engine twice: " + runnable);
        }
        this.networkTaskQueue.add(runnable);
        return this;
    }

    /**
     * Remove a task from the list run on every network loop invocation
     *
     * @param runnable
     * @return
     */
    public LXEngine removeNetworkTask(Runnable runnable) {
        this.networkTaskQueue.remove(runnable);
        return this;
    }

    /**
     * Sets the output driver
     *
     * @param output Output driver, or null for no output
     * @return this
     */
    public LXEngine addOutput(LXOutput output) {
        this.output.addChild(output);
        return this;
    }

    /**
     * Removes an output driver
     *
     * @param output Output driver
     * @return this
     */
    public LXEngine removeOutput(LXOutput output) {
        this.output.removeChild(output);
        return this;
    }

    public LXLook addLook() {
        LXLook look = new LXLook(lx);
        look.setParent(this);
        look.index = mutableLooks.size();
        mutableLooks.add(look);
        return look;
    }

    public void removeLook(LXLook look) {
        removeLook(look, true);
    }

    private void removeLook(LXLook look, boolean checkLast) {
        if (checkLast && (this.mutableLooks.size() == 1)) {
            throw new UnsupportedOperationException("Cannot remove last look from LXEngine");
        }
        if (mutableLooks.remove(look)) {
            look.dispose();
        }
    }

    @Deprecated
    public List<LXChannel> getChannels() {
        return getFocusedLook().channels;
    }

    public Iterable<LXChannel> getAllSubChannels() {
        IteratorChain<LXChannel> chained = new IteratorChain<>();
        for (LXLook look : mutableLooks) {
            chained.addIterator(look.channels.iterator());
        }
        return new IteratorIterable<LXChannel>(chained);
    }

    @Deprecated
    public LXChannel getDefaultChannel() {
        return getFocusedLook().channels.get(0);
    }

    @Deprecated
    public LXChannel getChannel(int channelIndex) {
        return getFocusedLook().getChannel(channelIndex);
    }

    @Deprecated
    public LXChannel getChannel(String label) {
        return getFocusedLook().getChannel(label);
    }

    @Deprecated
    public LXBus getFocusedChannel() {
        return getFocusedLook().getFocusedChannel();
    }

    @Deprecated
    public LXEngine setFocusedChannel(LXBus channel) {
        getFocusedLook().setFocusedChannel(channel);
        return this;
    }

    @Deprecated
    public LXChannel addChannel() {
        return addChannel(new LXPattern[0]);
    }

    @Deprecated
    public LXChannel addChannel(LXPattern[] patterns) {
        LXChannel channel = getFocusedLook().addChannel();
        channel.setPatterns(patterns);
        return channel;
    }

    @Deprecated
    public void removeChannel(LXChannel channel) {
        getFocusedLook().removeChannel(channel);
    }

    @Deprecated
    public void moveChannel(LXChannel channel, int index) {
        getFocusedLook().moveChannel(channel, index);
    }

    @Deprecated
    public void duplicateChannel(LXChannel channel) {
        getFocusedLook().duplicateChannel(channel);
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

    public Collection<LXLook> getLooks() {
        return Collections.unmodifiableList(mutableLooks);
    }

    public LXLook getFocusedLook() {
        // TODO: support more than one look
        return mutableLooks.get(0);
    }

    public LXLook getLook(int i) {
        return mutableLooks.get(i);
    }

    public LXLook getLook(String name) {
        for (LXLook look : mutableLooks) {
            if (name.equals(look.getLabel())) {
                return look;
            }
        }
        return null;
    }

    public void run() {
        this.hasStarted = true;
        int initialConversionCount = PolyBuffer.getConversionCount();

        long runStart = System.nanoTime();

        // Compute elapsed time
        this.nowMillis = System.currentTimeMillis();
        if (this.lastMillis == INIT_RUN) {
            // Initial frame is arbitrarily 16 milliseconds (~60 fps)
            this.lastMillis = this.nowMillis - 16;
        }
        double deltaMs = this.nowMillis - this.lastMillis;
        this.lastMillis = this.nowMillis;

        if (this.paused) {
            this.timer.channelNanos = 0;
            this.timer.fxNanos = 0;
            this.timer.runNanos = System.nanoTime() - runStart;
            return;
        }

        // Process MIDI events
        long midiStart = System.nanoTime();
        this.midi.dispatch();
        this.timer.midiNanos = System.nanoTime() - midiStart;

        // Process OSC events
        long oscStart = System.nanoTime();
        this.osc.dispatch();
        this.timer.oscNanos = System.nanoTime() - oscStart;

        // Process Art-Net events
        long artNetStart = System.nanoTime();
        this.artNet.dispatch();
        this.timer.artNetNanos = System.nanoTime() - artNetStart;

        // Process UI input events
        if (this.inputDispatch == null) {
            this.timer.inputNanos = 0;
        } else {
            long inputStart = System.nanoTime();
            this.inputDispatch.dispatch();
            this.timer.inputNanos = System.nanoTime() - inputStart;
        }

        // Run tempo and audio, always using real-time
        this.lx.tempo.loop(deltaMs);
        this.audio.loop(deltaMs);

        // Mutate by master speed for everything else
        deltaMs *= this.speed.getValue();

        this.modulation.loop(deltaMs);
        this.lx.palette.loop(deltaMs);
        this.lx.swatches.loop(deltaMs);

        // Run top-level loop tasks
        for (LXLoopTask loopTask : this.loopTasks) {
            loopTask.loop(deltaMs);
        }

        // Run once-tasks
        if (this.threadSafeTaskQueue.size() > 0) {
            this.engineThreadTaskQueue.clear();
            synchronized (this.threadSafeTaskQueue) {
                this.engineThreadTaskQueue.addAll(this.threadSafeTaskQueue);
                this.threadSafeTaskQueue.clear();
            }
            for (Runnable runnable : this.engineThreadTaskQueue) {
                runnable.run();
            }
        }

        // Apply all mutations in our mutation queue
        mutations.dispatchAll();

        // The main work: run patterns, blend channels, send to outputs.
        long channelStart = System.nanoTime();
        loopAllLooks(deltaMs);
        blendLooks(deltaMs, channelStart, colorSpace.getEnum());
        this.artNet.processOutput();
        sendToOutputs(runStart);
        long nowNanos = System.nanoTime();
        this.timer.addRunTime(nowNanos - runStart, nowNanos);

        doLogTimers();

        conversionsPerFrame = PolyBuffer.getConversionCount() - initialConversionCount;

        tickCount++;
        /* roll over manually (so we don't roll over into negative values */
        if (tickCount > (1L << 30)) {
            tickCount = 0;
        }
    }

    /** Runs loop() on all enabled or cue-enabled channels. */
    void loopAllLooks(double deltaMs) {
        for (LXLook look : this.mutableLooks) {
            look.loop(deltaMs, colorSpace.getEnum());
        }
    }

    /**
     * A target for blending or copying color data.  Maintains a reference to the
     * PolyBuffer where the blend or copy was last written, and uses that as the
     * base for the next blend.  You can provide a destination buffer to the
     * constructor, or let it allocate a new destination buffer automatically.
     */
    class BlendTarget implements PolyBufferProvider {
        private PolyBuffer lastResult = black;
        private final PolyBuffer dest;

        public BlendTarget(LX lx) {
            this(new PolyBuffer(lx));
        }

        public BlendTarget(PolyBuffer dest) {
            this.dest = dest;
        }

        public void reset() {
            lastResult = black;
        }

        public PolyBuffer getPolyBuffer() {
            return lastResult;
        }

        public boolean isBlack() {
            return lastResult == black;
        }

        /** Copies a given buffer into the current destination buffer. */
        public void copyFrom(PolyBufferProvider src, PolyBuffer.Space space) {
            dest.copyFrom(src.getPolyBuffer(), space);
            lastResult = dest;
        }

        /** Blends an overlay buffer on top of the current destination buffer. */
        public void blendFrom(PolyBufferProvider overlay, double alpha, LXBlend blend, PolyBuffer.Space space) {
            blend.blend(lastResult, overlay, alpha, dest, space);
            lastResult = dest;
        }

        /** Ensures that the result is written to the destination buffer. */
        public void finish(PolyBuffer.Space space) {
            copyFrom(lastResult, space);
        }
    }

    void blendLooks(double deltaMs, long channelStart, PolyBuffer.Space space) {
        BlendTarget main = new BlendTarget(buffer.main.render);
        BlendTarget cue = new BlendTarget(buffer.cue.render);
        boolean cueOn = false;

        // TODO: this is where scenes would plug in
        for (LXLook look : mutableLooks) {
            main.blendFrom(look.buffer.main, 1, addBlend, space);
            cue.blendFrom(look.buffer.cue, 1, addBlend, space);
            cueOn = cueOn || look.buffer.cueOn;
        }

        // Run the master channel (may have clips)
        this.masterChannel.loop(deltaMs);

        // Ensure the main buffer is written even if nothing was blended up to this point
        main.finish(space);

        // Time to apply master FX to the main blended output
        long fxStart = System.nanoTime();
        for (LXEffect effect : masterChannel.getEffects()) {
            effect.setPolyBuffer(buffer.main.render);
            effect.loop(deltaMs);
        }
        this.timer.fxNanos = System.nanoTime() - fxStart;

        // If cue-ing the palette!
        if (lx.palette.cue.isOn()) {
            int[] colors = (int[]) buffer.cue.render.getArray(SRGB8);
            for (LXPoint p : this.lx.model.points) {
                colors[p.index] = lx.palette.getColor(p);
            }
            buffer.cue.render.markModified(SRGB8);
            cueOn = true;
        }

        // Frame is now ready
        if (isEngineThreadRunning || isNetworkMultithreaded.isOn()) {
            // If multi-threading UI, lock the double buffer and clip it
            synchronized (buffer) {
                buffer.cueOn = cueOn;
                buffer.flip();
            }
        } else {
            // Otherwise lock-free!
            buffer.cueOn = cueOn;
        }
    }

    void sendToOutputs(long runStart) {
        if (isNetworkMultithreaded.isOn()) {
            // Just notify the network thread!
            synchronized (this.network) {
                this.network.notify();
            }
        } else {
            for (Runnable runnable : LXEngine.this.networkTaskQueue) {
                runnable.run();
            }
            // Otherwise do it ourself here
            long outputStart = System.nanoTime();
            LXEngine.this.artNet.sendOutput();
            output.send(buffer.main.render);
            long outputEnd = System.nanoTime();
            this.timer.outputNanos = outputEnd - outputStart;
            this.timer.runNanos = outputEnd - runStart;
            this.timer.addRunTime(this.timer.runNanos, outputEnd);
        }

        long nowNanos = System.nanoTime();
        this.timer.addRunTime(nowNanos - runStart, nowNanos);

        doLogTimers();
    }

    private void doLogTimers() {
        if (!logTimers) {
            return;
        }

        StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "Engine:%4.0ffps (capped at %4.0f)   Net:%4.0ffps, max%5.1fms    Frame:%5.1fms, avg%5.1fms, max%5.1fms    16-bit conv:%3d",
            Math.min(frameRate(), 999),  // frameRate() sometimes returns Infinity
            framesPerSecond.getValuef(),
            Math.min(network.frameRate(), 999),
            network.timer.ts.worstNanos / 1e6,
            timer.runCurrentNanos / 1e6,
            timer.runAvgNanos / 1e6,
            timer.runWorstNanos / 1e6,
            conversionsPerFrame));
        sb.append("LXEngine::run() ").append((int) (this.timer.runNanos / 1000000)).append("ms\n");
        sb.append("LXEngine::run()::channels ").append((int) (this.timer.channelNanos / 1000000)).append("ms\n");
        for (LXChannel channel : getAllSubChannels()) {
            sb.append("LXEngine::").append(channel.getLabel()).append("::loop() ").append((int) (channel.timer.loopNanos / 1000000)).append("ms\n");
            for (LXPattern pattern : channel.patterns) {
                sb.append("LXEngine::").append(channel.getLabel()).append("::").append(pattern.getLabel()).append("::run() ")
                    .append((int) (pattern.timer.runNanos / 1000000)).append("ms\n");
            }
            for (LXEffect effect : channel.effects) {
                sb.append("LXEngine::").append(channel.getLabel()).append("::").append(effect.getLabel()).append("::run() ")
                    .append((int) (effect.timer.runNanos / 1000000)).append("ms\n");
            }
        }
        System.out.println(sb);
        this.logTimers = false;
    }

    public class NetworkThread extends Thread {

        public class Timer {
            public long copyNanos = 0;
            public long sendNanos = 0;
            public final TimerStatistics ts = new TimerStatistics();
        }

        private long lastFrame = System.nanoTime();

        private float frameRate = 0;

        public final Timer timer = new Timer();

        private final PolyBuffer networkBuffer;

        NetworkThread(LX lx) {
            super("LXEngine Network Thread");
            this.networkBuffer = new PolyBuffer(lx);
            setDaemon(true);
        }

        @Override
        public void run() {
            System.out.println("LXEngine Network Thread started");
            while (!isInterrupted()) {
                try {
                    synchronized(this) {
                        wait();
                    }
                } catch (InterruptedException ix) {
                    System.out.println("LXEngine Network Thread interrupted");
                    break;
                }

                for (Runnable runnable : LXEngine.this.networkTaskQueue) {
                    runnable.run();
                }

                LXEngine.this.artNet.sendOutput();

                if (output.enabled.isOn()) {
                    // Copy from the double-buffer into our local storage and send from here
                    long copyStart = System.nanoTime();
                    synchronized (buffer) {
                        networkBuffer.copyFrom(buffer.main.frozen, colorSpace.getEnum());
                    }
                    long copyEnd = System.nanoTime();
                    this.timer.copyNanos = copyEnd- copyStart;

                    output.send(networkBuffer);
                    this.timer.sendNanos = System.nanoTime() - copyEnd;
                }

                // Compute network framerate
                long now = System.nanoTime();
                timer.ts.addTime(now - lastFrame, now);
                frameRate = 1e9f / timer.ts.currentNanos;
                lastFrame = now;
            }

            System.out.println("LXEngine Network Thread finished");
        }

        public float frameRate() {
            return this.frameRate;
        }
    }

    /**
     * This should be used when in threaded mode. It safely copies the
     * frozen internal buffer to the provided buffer.
     * @param dest Buffer to copy into
     */
    public void copyUIBuffer(PolyBuffer dest, PolyBuffer.Space space) {
        synchronized (buffer) {
            (buffer.cueOn ? buffer.cue : buffer.main).copyTo(dest, space);
        }
    }

    /**
     * This is used when not in threaded mode. It provides direct access to the
     * engine's render buffer.
     *
     * @return The internal render buffer
     */
    public PolyBuffer getUIPolyBufferNonThreadSafe() {
        return buffer.cueOn ? buffer.cue.render : buffer.main.render;
    }

    /**
     * This method is deprecated; use copyUIBuffer(PolyBuffer...) instead.
     * @param array Array to copy into
     */
    @Deprecated
    public void copyUIBuffer(int[] array) {
        copyUIBuffer(PolyBuffer.wrapArray(lx, array), SRGB8);
    }

    /**
     * This method is deprecated; use getUIPolyBufferNonThreadSafe instead.
     */
    @Deprecated
    public int[] getUIBufferNonThreadSafe() {
        return (int[]) getUIPolyBufferNonThreadSafe().getArray(SRGB8);
    }

    private static final String KEY_PALETTE = "palette";
    private static final String KEY_LOOKS = "looks";
    private static final String KEY_MASTER = "master";
    private static final String KEY_TEMPO = "tempo";
    private static final String KEY_AUDIO = "audio";
    private static final String KEY_COMPONENTS = "components";
    private static final String KEY_OUTPUT = "output";
    private static final String KEY_MODULATION = "modulation";
    private static final String KEY_OSC = "osc";
    private static final String KEY_ARTNET = "artnet";
    private static final String KEY_MIDI = "midi";


    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);
        obj.add(KEY_PALETTE, LXSerializable.Utils.toObject(lx, this.lx.palette));
        obj.add(KEY_LOOKS, LXSerializable.Utils.toArray(lx, this.mutableLooks));
        obj.add(KEY_MASTER, LXSerializable.Utils.toObject(lx, this.masterChannel));
        obj.add(KEY_TEMPO, LXSerializable.Utils.toObject(lx, this.lx.tempo));
        obj.add(KEY_AUDIO, LXSerializable.Utils.toObject(lx, this.audio));
        obj.add(KEY_OUTPUT, LXSerializable.Utils.toObject(lx, this.output));
        obj.add(KEY_COMPONENTS, LXSerializable.Utils.toObject(lx, this.components));
        obj.add(KEY_MODULATION, LXSerializable.Utils.toObject(lx, this.modulation));
        obj.add(KEY_OSC, LXSerializable.Utils.toObject(lx, this.osc));
        obj.add(KEY_ARTNET, LXSerializable.Utils.toObject(lx, this.artNet));
        obj.add(KEY_MIDI, LXSerializable.Utils.toObject(lx, this.midi));
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        if (engineThread != null && engineThread.isAlive() && Thread.currentThread() != engineThread) {
            throw new RuntimeException("When engine thread is running, LXEngine.load must be called from engine thread");
        }

        // TODO(mcslee): remove loop tasks that other things might have added? maybe
        // need to separate application-owned loop tasks from project-specific ones...

        // Clear all the modulation
        this.modulation.clear();

        // Add the new channels
        if (obj.has(KEY_LOOKS)) {
            JsonArray looksArray = obj.getAsJsonArray(KEY_LOOKS);
            /* avoid creating new look objects if we only have the one, as SLStudio does
               not deal well at all with the look list changing. */
            if (looksArray.size() == 1 && looks.size() == 1) {
                getFocusedLook().load(lx, (JsonObject) looksArray.get(0));
            } else {
                for (int i = this.mutableLooks.size() - 1; i >= 0; --i) {
                    removeLook(this.mutableLooks.get(i), false);
                }
                for (JsonElement lookElement : looksArray) {
                    LXLook look = addLook();
                    look.load(lx, (JsonObject) lookElement);
                }
            }
        } else {
            /* reuse look object to not confuse SLStudio */
            if (looks.size() == 1) {
                looks.get(0).load(lx, new JsonObject());
            } else {
                /* but if we have more than one, just delete all of them
                   and add a fresh one, SLStudio already wasn't doing well
                   with what was open. */
                for (int i = this.mutableLooks.size() - 1; i >= 0; --i) {
                    removeLook(this.mutableLooks.get(i), false);
                }
                addLook();
            }
        }

        // Master channel settings
        if (obj.has(KEY_MASTER)) {
            this.masterChannel.load(lx, obj.getAsJsonObject(KEY_MASTER));
        }

        // Palette
        if (obj.has(KEY_PALETTE)) {
            lx.palette.load(lx, obj.getAsJsonObject(KEY_PALETTE));
        }

        // Tempo
        if (obj.has(KEY_TEMPO)) {
            lx.tempo.load(lx, obj.getAsJsonObject(KEY_TEMPO));
        }

        // Audio setup
        if (obj.has(KEY_AUDIO)) {
            this.audio.load(lx, obj.getAsJsonObject(KEY_AUDIO));
        }

        // Generic components
        if (obj.has(KEY_COMPONENTS)) {
            JsonObject componentsObj = obj.getAsJsonObject(KEY_COMPONENTS);
            for (String key : this.components.keySet()) {
                if (componentsObj.has(key)) {
                    this.components.get(key).load(lx, componentsObj.getAsJsonObject(key));
                }
            }
        }

        // Output setup
        if (obj.has(KEY_OUTPUT)) {
            this.output.load(lx, obj.getAsJsonObject(KEY_OUTPUT));
        }

        // Modulation matrix
        this.modulation.load(lx, obj.has(KEY_MODULATION) ? obj.getAsJsonObject(KEY_MODULATION) : new JsonObject());

        // OSC
        if (obj.has(KEY_OSC)) {
            this.osc.load(lx, obj.getAsJsonObject(KEY_OSC));
        }

        // Art-Net
        if (obj.has(KEY_ARTNET)) {
            this.artNet.load(lx, obj.getAsJsonObject(KEY_ARTNET));
        }

        // Midi
        this.midi.load(lx, obj.has(KEY_MIDI) ? obj.getAsJsonObject(KEY_MIDI) : new JsonObject());

        // Parameters etc.
        super.load(lx, obj);
    }

    @Override
    public LXModulationEngine getModulation() {
        return this.modulation;
    }
}
