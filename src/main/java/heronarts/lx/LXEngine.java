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
import com.symmetrylabs.color.Spaces;
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
import heronarts.lx.clip.LXClip;
import heronarts.lx.midi.LXMidiEngine;
import heronarts.lx.model.LXPoint;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.osc.LXOscEngine;
import heronarts.lx.output.LXOutput;
import heronarts.lx.output.LXOutputGroup;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.parameter.ObjectParameter;
import heronarts.lx.pattern.SolidColorPattern;
import heronarts.lx.script.LXScriptEngine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static heronarts.lx.LXChannel.CrossfadeGroup.A;
import static heronarts.lx.LXChannel.CrossfadeGroup.B;
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

    private static final int MAX_SCENES = 5;

    private final LX lx;

    public final LXMidiEngine midi;

    public final LXAudioEngine audio;

    public final LXMappingEngine mapping = new LXMappingEngine();

    public final LXOscEngine osc;

    public final ArtNetEngine artNet;
    public final DMXEngine dmx;

    public final LXScriptEngine script;

    private Dispatch inputDispatch = null;

    private final List<LXLoopTask> loopTasks = new ArrayList<LXLoopTask>();
    private final List<Runnable> networkTaskQueue = new ArrayList<Runnable>();
    private final List<Runnable> threadSafeTaskQueue = Collections.synchronizedList(new ArrayList<Runnable>());
    private final List<Runnable> engineThreadTaskQueue = new ArrayList<Runnable>();
    private final Map<String, LXComponent> components = new HashMap<String, LXComponent>();

    private final List<LXChannel> mutableChannels = new ArrayList<LXChannel>();
    public final List<LXChannel> channels = Collections.unmodifiableList(this.mutableChannels);

    public final LXMasterChannel masterChannel;

    public final Output output;

    private final List<Listener> listeners = new ArrayList<Listener>();
    private final List<MessageListener> messageListeners = new ArrayList<MessageListener>();

    public final DiscreteParameter focusedChannel = new DiscreteParameter("Channel", 1);

    public final BoundedParameter framesPerSecond = new BoundedParameter("FPS", 60, 0, 300);
    public int conversionsPerFrame = 0;

    LXBlend[] channelBlends;
    private final AddBlend addBlend;

    public final CompoundParameter crossfader = (CompoundParameter)
        new CompoundParameter("Crossfader", 0.5)
        .setDescription("Applies blending between output groups A and B")
        .setPolarity(LXParameter.Polarity.BIPOLAR);

    final LXBlend[] crossfaderBlends;
    public final ObjectParameter<LXBlend> crossfaderBlendMode;

    public final BooleanParameter cueA =
        new BooleanParameter("Cue-A", false)
        .setDescription("Enables cue preview of crossfade group A");

    public final BooleanParameter cueB =
        new BooleanParameter("Cue-B", false)
        .setDescription("Enables cue preview of crossfade group B");

    public final BoundedParameter speed =
        new BoundedParameter("Speed", 1, 0, 2)
        .setDescription("Overall speed adjustement to the entire engine (does not apply to master tempo and audio)");

    /** The color space that the engine renders to. */
    public final EnumParameter<PolyBuffer.Space> colorSpace =
            new EnumParameter<>("Color Space", RGB16)
                    .setDescription("Selects the color space for the engine");

    private final BooleanParameter[] scenes = new BooleanParameter[MAX_SCENES];

    public final LXModulationEngine modulation;

    private boolean logTimers = false;

    public class FocusedClipParameter extends MutableParameter {

        private LXClip clip = null;

        private FocusedClipParameter() {
            super("Focused Clip");
            setDescription("Parameter which indicate the globally focused clip");
        }

        public FocusedClipParameter setClip(LXClip clip) {
            if (this.clip != clip) {
                this.clip = clip;
                bang();
            }
            return this;
        }

        public LXClip getClip() {
            return this.clip;
        }
    };

    public final FocusedClipParameter focusedClip = new FocusedClipParameter();

    private float frameRate = 0;

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

    public interface Listener {
        public void channelAdded(LXEngine engine, LXChannel channel);
        public void channelRemoved(LXEngine engine, LXChannel channel);
        public void channelMoved(LXEngine engine, LXChannel channel);
    }

    @Deprecated
    public interface MessageListener {
        public void onMessage(LXEngine engine, String message);
    }

    public final LXEngine addListener(Listener listener) {
        this.listeners.add(listener);
        return this;
    }

    public final LXEngine removeListener(Listener listener) {
        this.listeners.remove(listener);
        return this;
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
    private final BlendTarget groupA;  // working area for blending group A
    private final BlendTarget groupB;  // working area for blending group B
    private final EngineBuffer buffer;  // destination buffers for final output

    public final BooleanParameter isMultithreaded = new BooleanParameter("Threaded", false)
        .setDescription("Whether the engine and UI are on separate threads");

    public final BooleanParameter isChannelMultithreaded = new BooleanParameter("Channel Threaded", false)
        .setDescription("Whether the engine is multi-threaded per channel");

    public final BooleanParameter isNetworkMultithreaded = new BooleanParameter("Network Threaded", false)
        .setDescription("Whether the network output is on a separate thread");

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
        groupA = new BlendTarget(lx);
        groupB = new BlendTarget(lx);
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
        this.crossfaderBlendMode =
            new ObjectParameter<LXBlend>("Crossfader Blend", this.crossfaderBlends)
            .setDescription("Sets the blend mode used for the master crossfader");
        LX.initTimer.log("Engine: Blends");

        // Modulation matrix
        this.modulation = new LXModulationEngine(lx, this);
        LX.initTimer.log("Engine: Modulation");

        // Master channel
        this.masterChannel = new LXMasterChannel(lx);
        LX.initTimer.log("Engine: Master Channel");

        // Cue setup
        this.cueA.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (cueA.isOn()) {
                    cueB.setValue(false);
                    lx.palette.cue.setValue(false);
                    for (LXChannel channel : mutableChannels) {
                        channel.cueActive.setValue(false);
                    }
                }
            }
        });
        this.cueB.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (cueB.isOn()) {
                    cueA.setValue(false);
                    lx.palette.cue.setValue(false);
                    for (LXChannel channel : mutableChannels) {
                        channel.cueActive.setValue(false);
                    }
                }
            }
        });
        lx.palette.cue.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (lx.palette.cue.isOn()) {
                    cueA.setValue(false);
                    cueB.setValue(false);
                    for (LXChannel channel : mutableChannels) {
                        channel.cueActive.setValue(false);
                    }
                }
            }
        });
        LX.initTimer.log("Engine: Cue");

        // Scenes
        for (int i = 0; i < this.scenes.length; ++i) {
            final int sceneIndex = i;
            this.scenes[i] = new BooleanParameter("Scene-" + (i+1));
            addParameter("scene-" + (i+1), this.scenes[i]);
            this.scenes[i].addListener(new LXParameterListener() {
                public void onParameterChanged(LXParameter p) {
                    BooleanParameter scene = (BooleanParameter) p;
                    if (scene.isOn()) {
                        launchScene(sceneIndex);
                        scene.setValue(false);
                    }
                }
            });
        }

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

        // Listener
        this.focusedChannel.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                LXClip clip = focusedClip.getClip();
                if (clip != null && clip.bus != getFocusedChannel()) {
                    focusedClip.setClip(null);
                }
            }
        });
        LX.initTimer.log("Engine: Focus Listener");

        // Parameters
        addParameter("crossfader", this.crossfader);
        addParameter("crossfaderBlendMode", this.crossfaderBlendMode);
        addParameter("speed", this.speed);
        addParameter("focusedChannel", this.focusedChannel);
        addParameter("cueA", this.cueA);
        addParameter("cueB", this.cueB);
        addParameter("multithreaded", this.isMultithreaded);
        addParameter("channelMultithreaded", this.isChannelMultithreaded);
        addParameter("networkMultithreaded", this.isNetworkMultithreaded);
    }

    public void logTimers() {
        this.logTimers = true;
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
        for (LXChannel channel : this.mutableChannels) {
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

    public List<LXChannel> getChannels() {
        return this.channels;
    }

    public LXChannel getDefaultChannel() {
        return this.mutableChannels.get(0);
    }

    public LXChannel getChannel(int channelIndex) {
        return this.mutableChannels.get(channelIndex);
    }

    public LXChannel getChannel(String label) {
        for (LXChannel channel : this.mutableChannels) {
            if (channel.getLabel().equals(label)) {
                return channel;
            }
        }
        return null;
    }

    public LXBus getFocusedChannel() {
        if (this.focusedChannel.getValuei() == this.mutableChannels.size()) {
            return this.masterChannel;
        }
        return getChannel(this.focusedChannel.getValuei());
    }

    public LXEngine setFocusedChannel(LXBus channel) {
        if (channel == this.masterChannel) {
            this.focusedChannel.setValue(this.mutableChannels.size());
        } else {
            this.focusedChannel.setValue(this.mutableChannels.indexOf(channel));
        }
        return this;
    }

    public LXChannel addChannel() {
        return addChannel(new LXPattern[0]);
    }

    public LXChannel addChannel(LXPattern[] patterns) {
        LXChannel channel = new LXChannel(lx, this.mutableChannels.size(), patterns);
        channel.setParent(this);
        this.mutableChannels.add(channel);
        this.focusedChannel.setRange(this.mutableChannels.size() + 1);
        for (Listener listener : this.listeners) {
            listener.channelAdded(this, channel);
        }
        return channel;
    }

    public void removeChannel(LXChannel channel) {
        removeChannel(channel, true);
    }

    private void removeChannel(LXChannel channel, boolean checkLast) {
        if (checkLast && (this.mutableChannels.size() == 1)) {
            throw new UnsupportedOperationException("Cannot remove last channel from LXEngine");
        }
        if (this.mutableChannels.remove(channel)) {
            int i = 0;
            for (LXChannel c : this.mutableChannels) {
                c.setIndex(i++);
            }
            boolean notified = false;
            if (this.focusedChannel.getValuei() > this.mutableChannels.size()) {
                notified = true;
                this.focusedChannel.decrement();
            }
            this.focusedChannel.setRange(this.mutableChannels.size() + 1);
            if (!notified) {
                this.focusedChannel.bang();
            }
            for (Listener listener : this.listeners) {
                listener.channelRemoved(this, channel);
            }

            channel.dispose();
        }
    }

    public void moveChannel(LXChannel channel, int index) {
        boolean focused = channel.getIndex() == this.focusedChannel.getValuei();
        this.mutableChannels.remove(channel);
        this.mutableChannels.add(index, channel);
        int i = 0;
        for (LXChannel c: this.mutableChannels) {
            c.setIndex(i++);
        }
        if (focused) {
            this.focusedChannel.setValue(index);
        }
        for (Listener listener : this.listeners) {
            listener.channelMoved(this, channel);
        }
    }

    public void duplicateChannel(LXChannel channel) {
        LXChannel chan = addChannel();
        chan.load(lx, LXSerializable.Utils.toObject(lx, channel));
        chan.label.setValue(chan.label.getString() + " copy");
    }

    /**
     * Get the boolean parameter that launches a scene
     *
     * @param index
     * @return
     */
    public BooleanParameter getScene(int index) {
        return this.scenes[index];
    }

    public LXEngine launchScene(int index) {
        LXClip clip;
        for (LXChannel channel : this.lx.engine.channels) {
            clip = channel.getClip(index);
            if (clip != null) {
                clip.trigger();
            }
        }
        clip = this.masterChannel.getClip(index);
        if (clip != null) {
            clip.trigger();
        }
        return this;
    }

    public LXEngine stopClips() {
        for (LXChannel channel : this.lx.engine.channels) {
            for (LXClip clip : channel.clips) {
                if (clip != null) {
                    clip.stop();
                }
            }
        }
        for (LXClip clip : this.masterChannel.clips) {
            if (clip != null) {
                clip.stop();
            }
        }
        return this;
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

        // The main work: run patterns, blend channels, send to outputs.
        long channelStart = System.nanoTime();
        loopAllChannels(deltaMs);
        blendChannels(deltaMs, channelStart, colorSpace.getEnum());
        this.artNet.processOutput();
        sendToOutputs(runStart);
        long nowNanos = System.nanoTime();
        this.timer.addRunTime(nowNanos - runStart, nowNanos);

        if (this.logTimers) {
            StringBuilder sb = new StringBuilder();
            sb.append("LXEngine::run() " + ((int) (this.timer.runNanos / 1000000)) + "ms\n");
            sb.append("LXEngine::run()::channels " + ((int) (this.timer.channelNanos / 1000000)) + "ms\n");
            for (LXChannel channel : this.channels) {
                sb.append("LXEngine::" + channel.getLabel() + "::loop() " + ((int) (channel.timer.loopNanos / 1000000)) + "ms\n");
                LXPattern pattern = channel.getActivePattern();
                sb.append("LXEngine::" + channel.getLabel() + "::" + pattern.getLabel() + "::run() " + ((int) (pattern.timer.runNanos / 1000000)) + "ms\n");
            }
            System.out.println(sb);
            this.logTimers = false;
        }

        conversionsPerFrame = PolyBuffer.getConversionCount() - initialConversionCount;
    }

    /** Runs loop() on all enabled or cue-enabled channels. */
    void loopAllChannels(double deltaMs) {
        // If we are in super-threaded mode, run the channels on their own threads!
        if (isChannelMultithreaded.isOn()) {
            // Kick off threads per channel
            for (LXChannel channel : this.mutableChannels) {
                if (channel.shouldRun() || channel.cueActive.isOn()) {
                    synchronized (channel.thread) {
                        channel.thread.signal.workDone = false;
                        channel.thread.deltaMs = deltaMs;
                        channel.thread.workReady = true;
                        channel.thread.notify();
                        if (!channel.thread.hasStarted) {
                            channel.thread.hasStarted = true;
                            channel.thread.start();
                        }
                    }
                }
            }

            // Wait for all the channel threads to finish
            for (LXChannel channel : this.mutableChannels) {
                if (channel.shouldRun() || channel.cueActive.isOn()) {
                    synchronized (channel.thread.signal) {
                        while (!channel.thread.signal.workDone) {
                            try {
                                channel.thread.signal.wait();
                            } catch (InterruptedException ix) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                        channel.thread.signal.workDone = false;
                    }
                }
            }
        } else {
            for (LXChannel channel : this.mutableChannels) {
                if (channel.shouldRun() || channel.cueActive.isOn()) {
                    // TODO(mcslee): should clips still run even if channel is disabled??
                    channel.loop(deltaMs);
                }
            }
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

    void blendChannels(double deltaMs, long channelStart, PolyBuffer.Space space) {
        groupA.reset();
        groupB.reset();
        BlendTarget main = new BlendTarget(buffer.main.render);
        BlendTarget cue = new BlendTarget(buffer.cue.render);
        boolean cueOn = false;

        for (LXChannel channel : mutableChannels) {
            long blendStart = System.nanoTime();
            double alpha = Spaces.cie_lightness_to_luminance(channel.fader.getValue());

            if (channel.shouldRun() && alpha > 0) {
                LXChannel.CrossfadeGroup group = channel.crossfadeGroup.getEnum();
                LXBlend blend = channel.blendMode.getObject();
                (group == A ? groupA : group == B ? groupB : main).blendFrom(channel, alpha, blend, space);
            }
            if (channel.cueActive.isOn()) {
                cue.copyFrom(channel, space);
                cueOn = true;
            }
            if (channel.shouldRun() || channel.cueActive.isOn()) {
                ((LXChannel.Timer) channel.timer).blendNanos = System.nanoTime() - blendStart;
            }
        }

        // Run the master channel (may have clips)
        this.masterChannel.loop(deltaMs);

        if (cueA.isOn()) {
            cue.copyFrom(groupA, space);
            cueOn = true;
        } else if (cueB.isOn()) {
            cue.copyFrom(groupB, space);
            cueOn = true;
        }

        // Crossfade between the A and B groups, and add that to the main buffer
        double crossfadeValue = crossfader.getValue();
        boolean useGroupA = crossfadeValue < 1 && !groupA.isBlack();
        boolean useGroupB = crossfadeValue > 0 && !groupB.isBlack();
        double fadeTowardB = Math.min(1, 2 * crossfadeValue);
        double fadeTowardA = Math.min(1, 2 * (1 - crossfadeValue));

        if (useGroupA && useGroupB) {
            LXBlend blend = crossfaderBlendMode.getObject();
            if (crossfadeValue <= 0.5) {
                groupA.blendFrom(groupB, fadeTowardB, blend, space);
                main.blendFrom(groupA, 1, addBlend, space);
            } else {
                groupB.blendFrom(groupA, fadeTowardA, blend, space);
                main.blendFrom(groupB, 1, addBlend, space);
            }
        } else if (useGroupA) {
            main.blendFrom(groupA, fadeTowardA, addBlend, space);
        } else if (useGroupB) {
            main.blendFrom(groupB, fadeTowardB, addBlend, space);
        }
        this.timer.channelNanos = System.nanoTime() - channelStart;

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

        if (this.logTimers) {
            StringBuilder sb = new StringBuilder();
            sb.append("LXEngine::run() " + ((int) (this.timer.runNanos / 1000000)) + "ms\n");
            sb.append("LXEngine::run()::channels " + ((int) (this.timer.channelNanos / 1000000)) + "ms\n");
            for (LXChannel channel : this.channels) {
                sb.append("LXEngine::" + channel.getLabel() + "::loop() " + ((int) (channel.timer.loopNanos / 1000000)) + "ms\n");
                LXPattern pattern = channel.getActivePattern();
                sb.append("LXEngine::" + channel.getLabel() + "::" + pattern.getLabel() + "::run() " + ((int) (pattern.timer.runNanos / 1000000)) + "ms\n");
            }
            System.out.println(sb);
            this.logTimers = false;
        }
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
    private static final String KEY_CHANNELS = "channels";
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
        obj.add(KEY_CHANNELS, LXSerializable.Utils.toArray(lx, this.mutableChannels));
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

        // Remove all channels
        for (int i = this.mutableChannels.size() - 1; i >= 0; --i) {
            removeChannel(this.mutableChannels.get(i), false);
        }
        // Add the new channels
        if (obj.has(KEY_CHANNELS)) {
            JsonArray channelsArray = obj.getAsJsonArray(KEY_CHANNELS);
            for (JsonElement channelElement : channelsArray) {
                // TODO(mcslee): improve efficiency, allow no-patterns in a channel?
                LXChannel channel = addChannel();
                channel.load(lx, (JsonObject) channelElement);
            }
        } else {
            addChannel().fader.setValue(1);
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
