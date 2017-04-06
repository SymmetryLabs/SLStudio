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
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.LXMidiEngine;
import heronarts.lx.osc.LXOscEngine;
import heronarts.lx.output.LXOutput;
import heronarts.lx.output.LXOutputGroup;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.pattern.SolidColorPattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
public class LXEngine extends LXComponent {

    private final LX lx;

    public final LXMidiEngine midi;

    public final LXAudioEngine audio;

    public final LXMappingEngine mapping = new LXMappingEngine();

    public final LXOscEngine osc;

    private Dispatch inputDispatch = null;

    private final List<LXLoopTask> loopTasks = new ArrayList<LXLoopTask>();
    private final List<Runnable> threadSafeTaskQueue = Collections.synchronizedList(new ArrayList<Runnable>());
    private final List<Runnable> engineThreadTaskQueue = new ArrayList<Runnable>();
    private final Map<String, LXComponent> components = new HashMap<String, LXComponent>();

    private final List<LXChannel> channels = new ArrayList<LXChannel>();
    public final LXMasterChannel masterChannel;

    public final LXOutput output;

    private final List<Listener> listeners = new ArrayList<Listener>();
    private final List<MessageListener> messageListeners = new ArrayList<MessageListener>();

    private final List<LXChannel> unmodifiableChannels = Collections.unmodifiableList(this.channels);

    public final DiscreteParameter focusedChannel = new DiscreteParameter("Channel", 1);

    public final BoundedParameter framesPerSecond = new BoundedParameter("FPS", 60, 0, 300);

    LXBlend[] channelBlends;
    private final AddBlend addBlend;

    public final CompoundParameter crossfader = (CompoundParameter)
        new CompoundParameter("Crossfader", 0.5).setPolarity(LXParameter.Polarity.BIPOLAR);

    final LXBlend[] crossfaderBlends;
    public final DiscreteParameter crossfaderBlendMode;

    public final BooleanParameter cueLeft =
        new BooleanParameter("Cue-L", false)
        .setDescription("Enables cue preview of crossfade group A");

    public final BooleanParameter cueRight =
        new BooleanParameter("Cue-R", false)
        .setDescription("Enables cue preview of crossfade group B");

    public final BoundedParameter speed =
        new BoundedParameter("Speed", 1, 0, 2)
        .setDescription("Overall speed adjustement to the entire engine (does not apply to master tempo and audio)");

    public final LXModulationEngine modulation;

    private float frameRate = 0;

    public interface Dispatch {
        public void dispatch();
    }

    public interface Listener {
        public void channelAdded(LXEngine engine, LXChannel channel);
        public void channelRemoved(LXEngine engine, LXChannel channel);
        public void channelMoved(LXEngine engine, LXChannel channel);
    }

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

    public final LXEngine addMessageListener(MessageListener listener) {
        this.messageListeners.add(listener);
        return this;
    }

    public final LXEngine removeMessageListener(MessageListener listener) {
        this.messageListeners.remove(listener);
        return this;
    }

    public LXEngine broadcastMessage(String message) {
        for (MessageListener listener : this.messageListeners) {
            listener.onMessage(this, message);
        }
        return this;
    }

    public class Timer {
        public long runNanos = 0;
        public long channelNanos = 0;
        public long copyNanos = 0;
        public long fxNanos = 0;
        public long inputNanos = 0;
        public long midiNanos = 0;
        public long oscNanos = 0;
        public long outputNanos = 0;
    }

    public final Timer timer = new Timer();

    private final ModelBuffer uiBuffer;

    private final ModelBuffer background;
    private final ModelBuffer blendBufferMain;
    private final ModelBuffer blendBufferLeft;
    private final ModelBuffer blendBufferRight;
    private final ModelBuffer blendBufferCue;

    private volatile boolean isThreaded = false;

    private Thread engineThread = null;

    private boolean hasStarted = false;

    private boolean paused = false;

    private static final long INIT_RUN = -1;
    private long lastMillis = INIT_RUN;
    long nowMillis = System.currentTimeMillis();

    LXEngine(LX lx) {
        super(lx, LXComponent.ID_ENGINE);
        LX.initTimer.log("Engine: Init");
        this.lx = lx;

        // Background and blending buffers
        this.background = new ModelBuffer(lx);
        this.blendBufferMain = new ModelBuffer(lx);
        this.blendBufferLeft = new ModelBuffer(lx);
        this.blendBufferRight = new ModelBuffer(lx);
        this.blendBufferCue = new ModelBuffer(lx);

        // Double-buffer for the UI thread
        this.uiBuffer = new ModelBuffer(lx);

        // Initilize UI and background to black
        int[] uiArray = this.uiBuffer.getArray();
        int[] backgroundArray = this.background.getArray();
        for (int i = 0; i < backgroundArray.length; ++i) {
            uiArray[i] = backgroundArray[i] = LXColor.BLACK;
        }
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
        this.crossfaderBlendMode = new DiscreteParameter("Crossfader Blend", this.crossfaderBlends);
        LX.initTimer.log("Engine: Blends");

        // Modulation matrix
        this.modulation = new LXModulationEngine(lx);

        // Master channel
        this.masterChannel = new LXMasterChannel(lx);
        LX.initTimer.log("Engine: Master Channel");

        // Cue setup
        this.cueLeft.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (cueLeft.isOn()) {
                    cueRight.setValue(false);
                    for (LXChannel channel : channels) {
                        channel.cueActive.setValue(false);
                    }
                }
            }
        });
        this.cueRight.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (cueRight.isOn()) {
                    cueLeft.setValue(false);
                    for (LXChannel channel : channels) {
                        channel.cueActive.setValue(false);
                    }
                }
            }
        });
        LX.initTimer.log("Engine: Cue");

        // Master output
        this.output = new LXOutputGroup(lx);
        ((LXComponent)this.output).setParent(this);
        LX.initTimer.log("Engine: Output");

        // Midi engine
        this.midi = new LXMidiEngine(lx);
        LX.initTimer.log("Engine: Midi");

        this.audio = new LXAudioEngine(lx);
        LX.initTimer.log("Engine: Audio");

        // OSC engine
        this.osc = new LXOscEngine(lx);
        LX.initTimer.log("Engine: Osc");

        // Parameters
        addParameter("crossfader", this.crossfader);
        addParameter("crossfaderBlendMode", this.crossfaderBlendMode);
        addParameter("speed", this.speed);
        addParameter("focusedChannel", this.focusedChannel);
        addParameter("cueLeft", this.cueLeft);
        addParameter("cueRight", this.cueRight);
    }

    @Override
    public String getLabel() {
        return "Engine";
    }

    public LXEngine setInputDispatch(Dispatch inputDispatch) {
        this.inputDispatch = inputDispatch;
        return this;
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
        for (LXChannel channel : this.channels) {
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
     * @return this
     */
    public synchronized LXEngine setThreaded(boolean threaded) {
        if (threaded == this.isThreaded) {
            return this;
        }
        if (!threaded) {
            // Set interrupt flag on the engine thread
            this.engineThread.interrupt();
            if (Thread.currentThread() != this.engineThread) {
                // Called from another thread? If so, wait for engine thread to finish
                try {
                    this.engineThread.join();
                } catch (InterruptedException ix) {
                    throw new IllegalThreadStateException(
                        "Interrupted waiting to join LXEngine thread");
                }
            }
        } else {
            this.isThreaded = true;
            this.engineThread = new Thread("LX Engine Thread") {
                @Override
                public void run() {
                    System.out.println("LX Engine Thread started");
                    while (!isInterrupted()) {
                        long frameStart = System.currentTimeMillis();
                        LXEngine.this.run();
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
                    isThreaded = false;

                    System.out.println("LX Engine Thread finished");
                }
            };
            this.engineThread.start();
        }
        return this;
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
     * Sets the output driver
     *
     * @param output Output driver, or null for no output
     * @return this
     */
    public LXEngine addOutput(LXOutput output) {
        this.output.addChild(output);
        return this;
    }

    public List<LXChannel> getChannels() {
        return this.unmodifiableChannels;
    }

    public LXChannel getDefaultChannel() {
        return this.channels.get(0);
    }

    public LXChannel getChannel(int channelIndex) {
        return this.channels.get(channelIndex);
    }

    public LXChannel getChannel(String label) {
        for (LXChannel channel : this.channels) {
            if (channel.getLabel().equals(label)) {
                return channel;
            }
        }
        return null;
    }

    public LXBus getFocusedChannel() {
        if (this.focusedChannel.getValuei() == this.channels.size()) {
            return this.masterChannel;
        }
        return getChannel(this.focusedChannel.getValuei());
    }

    public LXEngine setFocusedChannel(LXBus channel) {
        if (channel == this.masterChannel) {
            this.focusedChannel.setValue(this.channels.size());
        } else {
            this.focusedChannel.setValue(this.channels.indexOf(channel));
        }
        return this;
    }

    public LXChannel addChannel() {
        return addChannel(new LXPattern[] { new SolidColorPattern(lx) });
    }

    public LXChannel addChannel(LXPattern[] patterns) {
        LXChannel channel = new LXChannel(lx, this.channels.size(), patterns);
        channel.setParent(this);
        this.channels.add(channel);
        this.focusedChannel.setRange(this.channels.size() + 1);
        for (Listener listener : this.listeners) {
            listener.channelAdded(this, channel);
        }
        return channel;
    }

    public void removeChannel(LXChannel channel) {
        removeChannel(channel, true);
    }

    private void removeChannel(LXChannel channel, boolean checkLast) {
        if (checkLast && (this.channels.size() == 1)) {
            throw new UnsupportedOperationException("Cannot remove last channel from LXEngine");
        }
        if (this.channels.remove(channel)) {
            int i = 0;
            for (LXChannel c : this.channels) {
                c.setIndex(i++);
            }
            boolean notified = false;
            if (this.focusedChannel.getValuei() > this.channels.size()) {
                notified = true;
                this.focusedChannel.decrement();
            }
            this.focusedChannel.setRange(this.channels.size() + 1);
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
        this.channels.remove(channel);
        this.channels.add(index, channel);
        int i = 0;
        for (LXChannel c: this.channels) {
            c.setIndex(i++);
        }
        if (focused) {
            this.focusedChannel.setValue(index);
        }
        for (Listener listener : this.listeners) {
            listener.channelMoved(this, channel);
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
            this.timer.copyNanos = 0;
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

        // Run and blend all of our channels
        long channelStart = System.nanoTime();
        int[] backgroundArray = this.background.getArray();
        int[] blendOutputMain = this.blendBufferMain.getArray();
        int[] blendOutputLeft = this.blendBufferLeft.getArray();
        int[] blendOutputRight = this.blendBufferRight.getArray();
        int[] blendOutputCue = this.blendBufferCue.getArray();
        int[] blendDestinationCue = backgroundArray;

        double crossfadeValue = this.crossfader.getValue();

        boolean leftOn = crossfadeValue < 1.;
        boolean rightOn = crossfadeValue > 0.;
        boolean cueOn = false;

        int leftChannelCount = 0;
        int rightChannelCount = 0;
        int mainChannelCount = 0;

        for (LXChannel channel : this.channels) {
            boolean channelIsEnabled = channel.enabled.isOn();
            boolean channelIsCue = channel.cueActive.isOn();
            if (channelIsEnabled || channelIsCue) {
                channel.loop(deltaMs);

                long blendStart = System.nanoTime();
                if (channelIsEnabled) {
                    boolean doBlend = false;
                    int[] blendDestination;
                    int[] blendOutput;
                    switch (channel.crossfadeGroup.getEnum()) {
                    case A:
                        blendDestination = (leftChannelCount++ > 0) ? blendOutputLeft : backgroundArray;
                        blendOutput = blendOutputLeft;
                        doBlend = leftOn || this.cueLeft.isOn();
                        break;
                    case B:
                        blendDestination = (rightChannelCount++ > 0) ? blendOutputRight: backgroundArray;
                        blendOutput = blendOutputRight;
                        doBlend = rightOn || this.cueRight.isOn();
                        break;
                    default:
                    case BYPASS:
                        blendDestination = (mainChannelCount++ > 0) ? blendOutputMain : backgroundArray;
                        blendOutput = blendOutputMain;
                        doBlend = channelIsEnabled;
                        break;
                    }
                    if (doBlend) {
                        double alpha = channel.fader.getValue();
                        if (alpha > 0) {
                            LXBlend blend = (LXBlend) channel.blendMode.getObject();
                            blend.blend(blendDestination, channel.getColors(), alpha, blendOutput);
                        } else if (blendDestination != blendOutput) {
                            // Edge-case: copy the blank buffer into the destination blend buffer when
                            // the channel fader is set to 0
                            System.arraycopy(blendDestination, 0, blendOutput, 0, blendDestination.length);
                        }
                    }
                }

                if (channelIsCue) {
                    cueOn = true;
                    this.addBlend.blend(blendDestinationCue, channel.getColors(), 1, blendOutputCue);
                    blendDestinationCue = blendOutputCue;
                }

                ((LXChannel.Timer)channel.timer).blendNanos = System.nanoTime() - blendStart;
            }
        }

        if (this.cueLeft.isOn()) {
            if (leftChannelCount > 0) {
                blendDestinationCue = blendOutputLeft;
            }
            cueOn = true;
        } else if (this.cueRight.isOn()) {
            if (rightChannelCount > 0) {
                blendDestinationCue = blendOutputRight;
            }
            cueOn = true;
        }

        boolean leftContent = leftOn && (leftChannelCount > 0);
        boolean rightContent = rightOn && (rightChannelCount > 0);

        if (leftContent && rightContent) {
            // There are left and right channels assigned!
            int[] crossfadeSource, crossfadeDestination;
            double crossfadeAlpha;
            if (crossfadeValue <= 0.5) {
                crossfadeDestination = blendOutputLeft;
                crossfadeSource = blendOutputRight;
                crossfadeAlpha = Math.min(1, 2. * crossfadeValue);
            } else {
                crossfadeDestination = blendOutputRight;
                crossfadeSource = blendOutputLeft;
                crossfadeAlpha = Math.min(1, 2. * (1-crossfadeValue));
            }

            // Compute the crossfade mix
            LXBlend blend = (LXBlend) this.crossfaderBlendMode.getObject();
            blend.blend(crossfadeDestination, crossfadeSource, crossfadeAlpha, crossfadeDestination);

            // Add the crossfaded groups to the main buffer
            int[] blendDestination = (mainChannelCount > 0) ? blendOutputMain : backgroundArray;
            addBlend.blend(blendDestination, crossfadeDestination, 1., blendOutputMain);

        } else if (leftContent) {
            // Add the left group to the main buffer
            int[] blendDestination = (mainChannelCount > 0) ? blendOutputMain : backgroundArray;
            double blendAlpha = Math.min(1, 2. * (1-crossfadeValue));
            addBlend.blend(blendDestination, blendOutputLeft, blendAlpha, blendOutputMain);
        } else if (rightContent) {
            // Add the right group to the main buffer
            int[] blendDestination = (mainChannelCount > 0) ? blendOutputMain : backgroundArray;
            double blendAlpha = Math.min(1, 2. * crossfadeValue);
            addBlend.blend(blendDestination, blendOutputRight, blendAlpha, blendOutputMain);
        }
        this.timer.channelNanos = System.nanoTime() - channelStart;

        // Check for edge case of all channels being off, don't leave stale data in blend buffer
        if ((leftChannelCount + rightChannelCount + mainChannelCount) == 0) {
            System.arraycopy(backgroundArray, 0, blendOutputMain, 0, backgroundArray.length);
        }

        // Time to apply master FX to the main blended output
        long fxStart = System.nanoTime();
        for (LXEffect effect : this.masterChannel.getEffects()) {
            effect.setBuffer(this.blendBufferMain);
            effect.loop(deltaMs);
        }
        this.timer.fxNanos = System.nanoTime() - fxStart;

        // Frame is now ready, copy into the UI buffer
        long copyStart = System.nanoTime();
        synchronized (this.uiBuffer) {
            if (cueOn) {
                System.arraycopy(blendDestinationCue, 0, this.uiBuffer.getArray(), 0, blendDestinationCue.length);
            } else {
                System.arraycopy(blendOutputMain, 0, this.uiBuffer.getArray(), 0, blendOutputMain.length);
            }
        }
        this.timer.copyNanos = System.nanoTime() - copyStart;

        // Send to outputs
        long outputStart = System.nanoTime();
        this.output.send(blendOutputMain);
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
    public void copyUIBuffer(int[] copy) {
        synchronized (this.uiBuffer) {
            System.arraycopy(this.uiBuffer.getArray(), 0, copy, 0, copy.length);
        }
    }

    /**
     * This is used when not in threaded mode. It provides direct access to the
     * engine's render buffer.
     *
     * @return The internal render buffer
     */
    public int[] getUIBufferNonThreadSafe() {
        return this.uiBuffer.getArray();
    }

    private static final String KEY_PALETTE = "palette";
    private static final String KEY_CHANNELS = "channels";
    private static final String KEY_MASTER = "master";
    private static final String KEY_AUDIO = "audio";
    private static final String KEY_COMPONENTS = "components";
    private static final String KEY_OUTPUT = "output";
    private static final String KEY_MODULATION = "modulation";
    private static final String KEY_OSC = "osc";
    private static final String KEY_MIDI = "midi";


    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);
        obj.add(KEY_PALETTE, LXSerializable.Utils.toObject(lx, this.lx.palette));
        obj.add(KEY_CHANNELS, LXSerializable.Utils.toArray(lx, this.channels));
        obj.add(KEY_MASTER, LXSerializable.Utils.toObject(lx, this.masterChannel));
        obj.add(KEY_AUDIO, LXSerializable.Utils.toObject(lx, this.audio));
        obj.add(KEY_OUTPUT, LXSerializable.Utils.toObject(lx, this.output));
        obj.add(KEY_COMPONENTS, LXSerializable.Utils.toObject(lx, this.components));
        obj.add(KEY_MODULATION, LXSerializable.Utils.toObject(lx, this.modulation));
        obj.add(KEY_OSC, LXSerializable.Utils.toObject(lx, this.osc));
        obj.add(KEY_MIDI, LXSerializable.Utils.toObject(lx, this.midi));
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        // TODO(mcslee): remove loop tasks that other things might have added? maybe
        // need to separate application-owned loop tasks from project-specific ones...

        // Remove all channels
        for (int i = this.channels.size() - 1; i >= 0; --i) {
            removeChannel(this.channels.get(i), false);
        }
        // Add the new channels
        JsonArray channelsArray = obj.getAsJsonArray(KEY_CHANNELS);
        for (JsonElement channelElement : channelsArray) {
            // TODO(mcslee): improve efficiency, allow no-patterns in a channel?
            LXChannel channel = addChannel();
            channel.load(lx, (JsonObject) channelElement);
        }
        // Master channel settings
        this.masterChannel.load(lx, obj.getAsJsonObject(KEY_MASTER));

        // Palette
        if (obj.has(KEY_PALETTE)) {
            lx.palette.load(lx, obj.getAsJsonObject(KEY_PALETTE));
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
        if (obj.has(KEY_MODULATION)) {
            this.modulation.load(lx, obj.getAsJsonObject(KEY_MODULATION));
        }

        // OSC
        if (obj.has(KEY_OSC)) {
            this.osc.load(lx, obj.getAsJsonObject(KEY_OSC));
        }

        // Midi
        if (obj.has(KEY_MIDI)) {
            this.midi.load(lx, obj.getAsJsonObject(KEY_MIDI));
        }

        // Parameters etc.
        super.load(lx, obj);
    }
}
