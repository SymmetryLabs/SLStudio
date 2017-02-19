/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx;

import heronarts.lx.blend.AddBlend;
import heronarts.lx.blend.DifferenceBlend;
import heronarts.lx.blend.LXBlend;
import heronarts.lx.blend.MultiplyBlend;
import heronarts.lx.blend.NormalBlend;
import heronarts.lx.blend.SubtractBlend;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.midi.LXMidiEngine;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.osc.LXOscEngine;
import heronarts.lx.output.LXOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.LXParameterized;
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
public class LXEngine extends LXParameterized {

    private final LX lx;

    public final LXMidiEngine midi;

    public final LXOscEngine osc;

    private Dispatch inputDispatch = null;

    private LXBlend[] channelBlendModes;
    private LXBlend[] patternBlendModes;

    private final AddBlend addBlend;

    private final List<LXLoopTask> loopTasks = new ArrayList<LXLoopTask>();
    private final List<LXChannel> channels = new ArrayList<LXChannel>();
    private final List<LXEffect> effects = new ArrayList<LXEffect>();

    /**
     * The master output drive
     */
    public  final LXOutput output;

    private final List<Listener> listeners = new ArrayList<Listener>();
    private final List<MessageListener> messageListeners = new ArrayList<MessageListener>();

    private final List<LXChannel> unmodifiableChannels = Collections.unmodifiableList(this.channels);
    private final List<LXEffect> unmodifiableEffects = Collections.unmodifiableList(this.effects);

    public final DiscreteParameter focusedChannel = new DiscreteParameter("CHANNEL", 1);

    public final BoundedParameter framesPerSecond = new BoundedParameter("FPS", 60, 0, 300);

    public final BoundedParameter crossfader = new BoundedParameter("CROSSFADE", 0.5);
    public final DiscreteParameter crossfaderBlendMode;

    public final BooleanParameter cueLeft = new BooleanParameter("CUE-L", false);
    public final BooleanParameter cueRight = new BooleanParameter("CUE-R", false);

    public final BoundedParameter speed = new BoundedParameter("SPEED", 1, 0, 2);

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

    long nowMillis = 0;
    private long lastMillis;

    private volatile boolean isThreaded = false;

    private Thread engineThread = null;

    private boolean hasStarted = false;

    private boolean paused = false;

    private static final long INIT_RUN = -1;

    LXEngine(LX lx) {
        this.lx = lx;
        LX.initTimer.log("Engine: Init");

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

        // Blend modes
        this.channelBlendModes = this.patternBlendModes = new LXBlend[] {
            this.addBlend = new AddBlend(lx),
            new NormalBlend(lx),
            new MultiplyBlend(lx),
            new SubtractBlend(lx),
            new DifferenceBlend(lx)
        };
        LX.initTimer.log("Engine: Blends");

        // Crossfader blend mode
        this.crossfaderBlendMode = new DiscreteParameter("BLEND", this.channelBlendModes);

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
        this.output = new LXOutput(lx) {
            @Override
            protected void onSend(int[] colors) {
                // Master output is a dummy container for child outputs
            }
        };
        LX.initTimer.log("Engine: Output");

        // Midi engine
        this.midi = new LXMidiEngine(lx);
        LX.initTimer.log("Engine: Midi");

        // OSC engine
        this.osc = new LXOscEngine(lx);
        LX.initTimer.log("Engine: Osc");

        // Default color palette
        addComponent(lx.palette);

        // Initialize timer
        this.lastMillis = INIT_RUN;
    }

    public LXEngine setInputDispatch(Dispatch inputDispatch) {
        this.inputDispatch = inputDispatch;
        return this;
    }

    /**
     * Gets the available blend modes
     *
     * @return Blend modes
     */
    public LXBlend[] getBlendModes() {
        return this.channelBlendModes;
    }

    /**
     * Sets the available blend modes. Must be invoked before the engine
     * is started or UI is created
     *
     * @param blendModes Array of blend instances
     * @return this
     */
    public LXEngine setBlendModes(LXBlend[] blendModes) {
        if (this.hasStarted) {
            throw new UnsupportedOperationException("setBlendModes() may only be invoked before engine has started");
        }
        setChannelBlendModes(blendModes);
        return this;
    }

    /**
     * Sets the blend modes available to the channel mixer
     *
     * @param channelBlendModes
     * @return this
     */
    public LXEngine setChannelBlendModes(LXBlend[] channelBlendModes) {
        if (this.hasStarted) {
            throw new UnsupportedOperationException("setChannelBlendModes() may only be invoked before engine has started");
        }
        this.channelBlendModes = channelBlendModes;
        this.crossfaderBlendMode.setObjects(channelBlendModes);
        for (LXChannel channel : this.channels) {
            channel.blendMode.setObjects(channelBlendModes);
        }
        return this;
    }

    /**
     * Sets the blend modes available to patterns
     *
     * @param patternBlendModes
     * @return this
     */
    public LXEngine setPatternBlendModes(LXBlend[] patternBlendModes) {
        if (this.hasStarted) {
            throw new UnsupportedOperationException("setPatternBlendModes() may only be invoked before engine has started");
        }
        this.patternBlendModes = patternBlendModes;
        for (LXChannel channel : this.channels) {
            for (LXPattern pattern : channel.getPatterns()) {
                // TODO(mcslee): set this jonx
                // pattern.blendMode.setObjects(patternBlendModes);
            }
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
                    System.out.println("LX Engine Thread started.");
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
     * @return this
     */
    public LXEngine setPaused(boolean paused) {
        this.paused = paused;
        return this;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public LXEngine addComponent(LXComponent component) {
        return addLoopTask(component);
    }

    public LXEngine removeComponent(LXComponent component) {
        return removeLoopTask(component);
    }

    public LXEngine addModulator(LXModulator modulator) {
        return addLoopTask(modulator);
    }

    public LXEngine removeModulator(LXModulator modulator) {
        return removeLoopTask(modulator);
    }

    public LXEngine addLoopTask(LXLoopTask loopTask) {
        if (!this.loopTasks.contains(loopTask)) {
            this.loopTasks.add(loopTask);
        }
        return this;
    }

    public LXEngine removeLoopTask(LXLoopTask loopTask) {
        this.loopTasks.remove(loopTask);
        return this;
    }

    public List<LXEffect> getEffects() {
        return this.unmodifiableEffects;
    }

    public LXEngine addEffect(LXEffect fx) {
        this.effects.add(fx);
        return this;
    }

    public LXEngine removeEffect(LXEffect fx) {
        this.effects.remove(fx);
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

    public LXChannel getFocusedChannel() {
        return getChannel(focusedChannel.getValuei());
    }

    public LXChannel addChannel() {
        return addChannel(new LXPattern[] { new SolidColorPattern(lx, 0xff000000) });
    }

    public LXChannel addChannel(LXPattern[] patterns) {
        LXChannel channel = new LXChannel(lx, this.channels.size(), patterns);
        this.channels.add(channel);
        this.focusedChannel.setRange(this.channels.size());
        for (Listener listener : this.listeners) {
            listener.channelAdded(this, channel);
        }
        return channel;
    }

    public void removeChannel(LXChannel channel) {
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

    public void moveChannel(LXChannel channel, int index) {
        this.channels.remove(channel);
        this.channels.add(index, channel);
        int i = 0;
        for (LXChannel c: this.channels) {
            c.setIndex(i++);
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

        // Run tempo, always using real-time
        this.lx.tempo.loop(deltaMs);

        // Run top-level loop tasks
        for (LXLoopTask loopTask : this.loopTasks) {
            loopTask.loop(deltaMs);
        }

        // Mutate by speed for channels and effects
        deltaMs *= this.speed.getValue();

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
            if (channel.enabled.isOn() || channel.cueActive.isOn()) {
                channel.loop(deltaMs);

                boolean doBlend = false;
                int[] blendDestination;
                int[] blendOutput;
                switch (channel.crossfadeGroup.getValuei()) {
                case LXChannel.CROSSFADE_GROUP_LEFT:
                    blendDestination = (leftChannelCount++ > 0) ? blendOutputLeft : backgroundArray;
                    blendOutput = blendOutputLeft;
                    doBlend = leftOn || this.cueLeft.isOn();
                    break;
                case LXChannel.CROSSFADE_GROUP_RIGHT:
                    blendDestination = (rightChannelCount++ > 0) ? blendOutputRight: backgroundArray;
                    blendOutput = blendOutputRight;
                    doBlend = rightOn || this.cueRight.isOn();
                    break;
                default:
                case LXChannel.CROSSFADE_GROUP_BYPASS:
                    blendDestination = (mainChannelCount++ > 0) ? blendOutputMain : backgroundArray;
                    blendOutput = blendOutputMain;
                    doBlend = true;
                    break;
                }

                long blendStart = System.nanoTime();
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
                if (channel.cueActive.isOn()) {
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
        for (LXEffect fx : this.effects) {
            ((LXLayeredComponent) fx).setBuffer(this.blendBufferMain);
            fx.loop(deltaMs);
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
}
