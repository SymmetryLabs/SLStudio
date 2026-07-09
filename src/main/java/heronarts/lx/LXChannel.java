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

import com.jogamp.common.util.ArrayHashSet;
import heronarts.lx.blend.LXBlend;
import heronarts.lx.midi.LXMidiEngine;
import heronarts.lx.midi.LXShortMessage;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.parameter.ObjectParameter;
import heronarts.lx.parameter.BooleanParameter;

import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.symmetrylabs.slstudio.ApplicationState;
import java.util.Collection;
import com.symmetrylabs.slstudio.presets.ChannelPresetLibrary;
import com.symmetrylabs.slstudio.effect.SpeedEffect;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;

import static heronarts.lx.PolyBuffer.Space.RGB16;
import static heronarts.lx.PolyBuffer.Space.SRGB8;

/**
 * A channel is a single component of the engine that has a set of patterns from
 * which it plays and rotates. It also has a fader to control how this channel
 * is blended with the channels before it.
 */
public class LXChannel extends LXBus implements LXComponent.Renamable, PolyBufferProvider {

    public class Timer extends LXModulatorComponent.Timer {
        public long blendNanos;
    }

    @Override
    protected LXModulatorComponent.Timer constructTimer() {
        return new Timer();
    }

    public final LXModulatorComponent.Timer timer = constructTimer();

    /**
     * Listener interface for objects which want to be notified when the internal
     * channel state is modified.
     */
    public interface Listener extends LXBus.Listener {
        public void indexChanged(LXChannel channel);

        public void patternAdded(LXChannel channel, LXPattern pattern);

        public void patternRemoved(LXChannel channel, LXPattern pattern);

        public void patternMoved(LXChannel channel, LXPattern pattern);

        public void patternWillChange(LXChannel channel, LXPattern pattern, LXPattern nextPattern);

        public void patternDidChange(LXChannel channel, LXPattern pattern);

        /** Fired when an effect is added to a specific pattern's chain (per-pattern effects). */
        default void patternEffectAdded(LXChannel channel, LXPattern pattern, LXEffect effect) {}

        /** Fired when an effect is removed from a specific pattern's chain. */
        default void patternEffectRemoved(LXChannel channel, LXPattern pattern, LXEffect effect) {}

        /** Fired when a pattern-scoped effect is reordered. */
        default void patternEffectMoved(LXChannel channel, LXPattern pattern, LXEffect effect) {}

        /** Fired when a warp is added to a specific pattern's chain (per-pattern warps). */
        default void patternWarpAdded(LXChannel channel, LXPattern pattern, LXWarp warp) {}

        /** Fired when a warp is removed from a specific pattern's chain. */
        default void patternWarpRemoved(LXChannel channel, LXPattern pattern, LXWarp warp) {}

        /** Fired when a pattern-scoped warp is reordered. */
        default void patternWarpMoved(LXChannel channel, LXPattern pattern, LXWarp warp) {}
    }

    public interface MidiListener {
        public void midiReceived(LXChannel channel, LXShortMessage message);
    }

    /**
     * Utility class to extend in cases where only some methods need overriding.
     */
    public abstract static class AbstractListener implements Listener {

        @Override
        public void indexChanged(LXChannel channel) {
        }

        @Override
        public void warpAdded(LXBus channel, LXWarp warp) {
        }

        @Override
        public void warpRemoved(LXBus channel, LXWarp warp) {
        }

        @Override
        public void warpMoved(LXBus channel, LXWarp warp) {
        }

        @Override
        public void effectAdded(LXBus channel, LXEffect effect) {
        }

        @Override
        public void effectRemoved(LXBus channel, LXEffect effect) {
        }

        @Override
        public void effectMoved(LXBus channel, LXEffect effect) {
        }

        @Override
        public void patternAdded(LXChannel channel, LXPattern pattern) {
        }

        @Override
        public void patternRemoved(LXChannel channel, LXPattern pattern) {
        }

        @Override
        public void patternMoved(LXChannel channel, LXPattern pattern) {
        }

        @Override
        public void patternWillChange(LXChannel channel, LXPattern pattern,
                                                                    LXPattern nextPattern) {
        }

        @Override
        public void patternDidChange(LXChannel channel, LXPattern pattern) {
        }
    }

    private final List<Listener> listeners = new ArrayList<Listener>();
    private final List<Listener> listenerSnapshot = new ArrayList<Listener>();
    private final List<MidiListener> midiListeners = new ArrayList<MidiListener>();

    public enum CrossfadeGroup {
        BYPASS,
        A,
        B
    }

    ;

    /**
     * The index of this channel in the engine.
     */
    private int index;

    /**
     * Which pattern is focused in the channel
     */
    public final DiscreteParameter focusedPattern;

    /**
     * Whether this channel is enabled.
     */
    public final BooleanParameter enabled =
            new BooleanParameter("On", true)
                    .setDescription("Sets whether this channel is on or off");

    /**
     * The color space that this channel renders to.
     */
    public final EnumParameter<PolyBuffer.Space> colorSpace =
            new EnumParameter<>("Color Space", RGB16)
                    .setDescription("Selects the color space for this channel");

    /**
     * Crossfade group this channel belongs to
     */
    public final EnumParameter<CrossfadeGroup> crossfadeGroup =
            new EnumParameter<CrossfadeGroup>("Group", CrossfadeGroup.BYPASS)
                    .setDescription("Assigns this channel to crossfader group A or B");

    /**
     * Whether this channel should listen to MIDI events
     */
    public final BooleanParameter midiMonitor =
            new BooleanParameter("MIDI Monitor", false)
                    .setDescription("Enables or disables monitoring of live MIDI input on this channel");

    /**
     * Which channel MIDI messages this channel observes
     */
    public final EnumParameter<LXMidiEngine.Channel> midiChannel =
            new EnumParameter<LXMidiEngine.Channel>("MIDI Channel", LXMidiEngine.Channel.OMNI)
                    .setDescription("Determines which MIDI channel is responded to");

    /**
     * Whether this channel should show in the cue UI.
     */
    public final BooleanParameter cueActive =
            new BooleanParameter("Cue", false)
                    .setDescription("Toggles the channel CUE state, determining whether it is shown in the preview window");

    /**
     * Whether auto pattern transition is enabled on this channel
     */
    public final BooleanParameter autoCycleEnabled =
            new BooleanParameter("Auto-Cycle", false)
                    .setDescription("When enabled, this channel will automatically cycle between its patterns");

    /**
     * Time in milliseconds after which transition thru the pattern set is automatically initiated.
     */
    public final BoundedParameter autoCycleTimeSecs = (BoundedParameter)
            new BoundedParameter("Cycle Time", 60, .1, 10 * 60)
                    .setDescription("Sets the number of seconds after which the channel cycles to the next pattern")
                    .setUnits(LXParameter.Units.SECONDS);

    public final BoundedParameter transitionTimeSecs = (BoundedParameter)
            new BoundedParameter("Trans Time", 5, .1, 180)
                    .setDescription("Sets the duration of blending transitions between patterns")
                    .setUnits(LXParameter.Units.SECONDS);

    public final BooleanParameter transitionEnabled =
            new BooleanParameter("Transitions", false)
                    .setDescription("When enabled, transitions between patterns use a blend");

    public final ObjectParameter<LXBlend> transitionBlendMode;

    public final CompoundParameter fader =
            new CompoundParameter("Fader", 0)
                    .setDescription("Sets the alpha level of the output of this channel");

    public final BooleanParameter autoDisable =
                    new BooleanParameter("Auto Disable", true)
                                    .setDescription("If true, disables the channel when the fader goes to zero");

    public final ObjectParameter<LXBlend> blendMode;
    public final ObjectParameter<LXBlend> patternBlendMode;

    public final MutableParameter controlSurfaceFocusIndex = (MutableParameter)
            new MutableParameter("Surface Focus Index", 0)
                    .setDescription("Control surface focus index");

    public final MutableParameter controlSurfaceFocusLength = (MutableParameter)
            new MutableParameter("Surface Focus Length", 0)
                    .setDescription("Control surface focus length");

    public final BoundedParameter speed =
            new BoundedParameter("Speed", 1, 0, 2)
                    .setDescription("Overall speed adjustement to all components in this channel");

    public final BooleanParameter editorVisible =
            new BooleanParameter("Editor Visible", true)
                    .setDescription("Sets whether this channel is visible for editing in the look editor");

//    public final BooleanParameter blendPatterns =
//        new BooleanParameter("BlendPatterns", ApplicationState.inVolumeMode()) // defaults to true for Volume, false for SLStudio
//            .setDescription("If true, all patterns in the channel are run and blended together. If false, only the active pattern is run.");
    public final BooleanParameter blendPatterns =
        new BooleanParameter("Blend Patterns", false) // defaults to true for Volume, false for SLStudio
            .setDescription("If true, all patterns in the channel are run and blended together. If false, only the active pattern is run.");

    public final BooleanParameter acceptSwatches =
        new BooleanParameter("Accept Swatches", true)
            .setDescription("If true, components in this channel will be updated when swatches are applied.");

    private final List<LXPattern> mutablePatterns = new ArrayList<LXPattern>();
    public final List<LXPattern> patterns = Collections.unmodifiableList(mutablePatterns);
    public static final Map<String, LXPattern> allPatterns = new HashMap<String, LXPattern>();

    /** Per-pattern effect chains. Effects in here are only run when their key pattern is active. */
    private final Map<LXPattern, List<LXEffect>> patternEffects = new IdentityHashMap<>();
    /** Per-pattern warp chains. Warps in here are only applied when their key pattern is active. */
    private final Map<LXPattern, List<LXWarp>> patternWarps = new IdentityHashMap<>();

    /**
     * A local buffer used for transition blending and effects on this channel
     */
    private final PolyBuffer polyBuffer;

    private double autoCycleProgress = 0;
    private double transitionProgress = 0;
    private int activePatternIndex = 0;
    private int nextPatternIndex = 0;

    private LXBlend transition = null;
    private long transitionMillis = 0;

    public int linkedPreset = 0;

    ChannelThread thread = new ChannelThread();

    private static int channelThreadCount = 1;

    class ChannelThread extends Thread {

        ChannelThread() {
            super("LXChannel thread #" + channelThreadCount++);
            setDaemon(true);
        }

        boolean hasStarted = false;
        boolean workReady = true;
        double deltaMs;

        class Signal {
            boolean workDone = false;
        }

        Signal signal = new Signal();

        @Override
        public void run() {
            System.out.println("LXEngine Channel thread started [" + getLabel() + "]");
            while (!isInterrupted()) {
                synchronized (this) {
                    try {
                        while (!this.workReady) {
                            wait();
                        }
                    } catch (InterruptedException ix) {
                        // Channel is finished
                        break;
                    }
                    this.workReady = false;
                }
                loop(this.deltaMs);
                synchronized (this.signal) {
                    this.signal.workDone = true;
                    this.signal.notify();
                }
            }
            System.out.println("LXEngine Channel thread finished [" + getLabel() + "]");
        }
    }

    ;

    LXChannel(LX lx, int index, LXPattern[] patterns) {
        super(lx, "Channel-" + (index + 1));
        this.index = index;
        this.label.setDescription("The name of this channel");
        this.polyBuffer = new PolyBuffer(lx);

        this.focusedPattern =
                new DiscreteParameter("Focused Pattern", 0, Integer.max(patterns.length, 1))
                        .setDescription("Which pattern has focus in the UI");

        this.blendMode = new ObjectParameter<LXBlend>("Blend", lx.engine.channelBlends)
                .setDescription("Specifies the blending function used for the channel fader");
        this.patternBlendMode = new ObjectParameter<LXBlend>("Blending", lx.engine.channelBlends)
            .setDescription("Specifies the blending function used for blending patterns together when blendPatterns is set");

        this.transitionBlendMode = new ObjectParameter<LXBlend>("Trans Blend", lx.engine.crossfaderBlends)
                .setDescription("Specifies the blending function used for transitions between patterns on the channel");

        this.transitionMillis = lx.engine.nowMillis;
        _updatePatterns(patterns);

        this.autoCycleEnabled.addListener(p -> {
            if (((BooleanParameter) p).isOn()) {
                this.blendPatterns.setValue(false);
            }
        });

        this.blendPatterns.addListener(p -> {
            if (this.autoCycleEnabled.isOn()) {
                this.blendPatterns.setValue(false);
            }
        });

        addParameter("enabled", this.enabled);
        addParameter("cue", this.cueActive);
        addParameter("midiMonitor", this.midiMonitor);
        addParameter("midiChannel", this.midiChannel);
        addParameter("autoCycleEnabled", this.autoCycleEnabled);
        addParameter("autoCycleTimeSecs", this.autoCycleTimeSecs);
        addParameter("fader", this.fader);
        addParameter("crossfadeGroup", this.crossfadeGroup);
        addParameter("blendMode", this.blendMode);
        addParameter("transitionEnabled", this.transitionEnabled);
        addParameter("transitionTimeSecs", this.transitionTimeSecs);
        addParameter("transitionBlendMode", this.transitionBlendMode);
        addParameter("autoDisable", this.autoDisable);
        addParameter("speed", this.speed);
        addParameter("editorVisible", this.editorVisible);
        addParameter("patternBlendMode", this.patternBlendMode);
        addParameter("blendPatterns", this.blendPatterns);
    }

    boolean shouldRun() {
//        if (!this.enabled.isOn()) {
//            return false;
//        }
//        return !this.autoDisable.isOn() || this.fader.getValue() != 0;
        if (this.fader.getValuef() < 0.05){
            this.enabled.setValue(false);
            return false;
        }
        else {
            this.enabled.setValue(true);
            return true;
        }
    }

    public String getOscAddress() {
        return "/lx/channel/" + (this.index+1);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == this.autoCycleEnabled) {
            if (this.transition == null) {
                this.transitionMillis = this.lx.engine.nowMillis;
            }
        } else if (p == this.cueActive) {
            if (this.cueActive.isOn()) {
                LXLook parent = getParentLook();
                if (parent != null) {
                    parent.cueA.setValue(false);
                    parent.cueB.setValue(false);
                }
            }
        }
    }

    @Override
    protected void onModelChanged(LXModel model) {
        super.onModelChanged(model);
        for (LXPattern pattern : this.mutablePatterns) {
            pattern.setModel(model);
        }
    }

    public final void addListener(Listener listener) {
        super.addListener(listener);
        this.listeners.add(listener);
    }

    public final void removeListener(Listener listener) {
        super.removeListener(listener);
        this.listeners.remove(listener);
    }

    public LXChannel addMidiListener(MidiListener listener) {
        this.midiListeners.add(listener);
        return this;
    }

    public LXChannel removeMidiListener(MidiListener listener) {
        this.midiListeners.remove(listener);
        return this;
    }

    public void midiMessage(LXShortMessage message) {
        for (MidiListener listener : this.midiListeners) {
            listener.midiReceived(this, message);
        }
        midiDispatch(message);
    }

    public void midiDispatch(LXShortMessage message) {
        LXPattern activePattern = getActivePattern();
        message.dispatch(activePattern);
        LXPattern nextPattern = getNextPattern();
        if (nextPattern != null && nextPattern != activePattern) {
            message.dispatch(nextPattern);
        }
    }

    final LXChannel setIndex(int index) {
        if (this.index != index) {
            this.index = index;
            for (LXBus.Listener listener : this.listeners) {
                ((LXChannel.Listener)listener).indexChanged(this);
            }
        }
        return this;
    }

    public final int getIndex() {
        return this.index;
    }

    public final List<LXPattern> getPatterns() {
        return this.patterns;
    }

    public final LXPattern getPattern(int index) {
        return this.mutablePatterns.get(index);
    }

    public final LXPattern getPattern(String label) {
        for (LXPattern pattern : this.patterns) {
            if (pattern.getLabel().equals(label)) {
                return pattern;
            }
        }
        return null;
    }

    public final LXPattern getPatternByClassName(String className) {
        for (LXPattern pattern : this.patterns) {
            if (pattern.getClass().getName().equals(className)) {
                return pattern;
            }
        }
        return null;
    }

    public final LXChannel setPatterns(LXPattern[] patterns) {
        if (this.transition != null) {
            finishTransition();
        } else if (getActivePattern() != null) {
            getActivePattern().onInactive();
        }
        _updatePatterns(patterns);
        this.activePatternIndex = this.nextPatternIndex = 0;
        this.transition = null;
        LXPattern active = getActivePattern();
        if (active != null) {
            active.onActive();
        }
        return this;
    }

    private int noiseNumber = 1;
    public final LXChannel addPattern(LXPattern pattern) {
        pattern.setChannel(this);
        pattern.setModel(this.model);
        String patternString = pattern.toString();
        if (patternString.equals("Noise[Channel-9 | Noise]")) {

            allPatterns.put(patternString + noiseNumber, pattern);
            noiseNumber++;
        }
        else {
            allPatterns.put(patternString, pattern);
        }
        System.out.println(patternString);

        this.mutablePatterns.add(pattern);
        LXUtils.updateIndexes(mutablePatterns);
        this.focusedPattern.setRange(this.mutablePatterns.size());
        this.listenerSnapshot.clear();
        this.listenerSnapshot.addAll(this.listeners);
        for (Listener listener : this.listenerSnapshot) {
            listener.patternAdded(this, pattern);
        }
        if (this.mutablePatterns.size() == 1) {
            this.focusedPattern.bang();
        }

        return this;
    }

    public final LXChannel removePattern(LXPattern pattern) {
        int index = this.mutablePatterns.indexOf(pattern);
        if (index >= 0) {
            boolean wasActive = (this.activePatternIndex == index);
            int focusedPatternIndex = this.focusedPattern.getValuei();
            if ((this.transition != null) && (
                    (this.activePatternIndex == index) ||
                    (this.nextPatternIndex == index)
                 )) {
                finishTransition();
            }
            this.mutablePatterns.remove(index);
            LXUtils.updateIndexes(mutablePatterns);

            if (this.activePatternIndex > index) {
                --this.activePatternIndex;
            } else if (this.activePatternIndex >= this.mutablePatterns.size()) {
                this.activePatternIndex = this.mutablePatterns.size() - 1;
            }
            if (this.nextPatternIndex > index) {
                --this.nextPatternIndex;
            } else if (this.nextPatternIndex >= this.mutablePatterns.size()) {
                this.nextPatternIndex = this.mutablePatterns.size() - 1;
            }
            if (focusedPatternIndex > index) {
                --focusedPatternIndex;
            } else if (focusedPatternIndex >= this.mutablePatterns.size()) {
                focusedPatternIndex = this.mutablePatterns.size() - 1;
            }
            if (this.activePatternIndex < 0) {
                this.activePatternIndex = 0;
                this.nextPatternIndex = 0;
            }
            if (focusedPatternIndex >= 0) {
                if (this.focusedPattern.getValuei() != focusedPatternIndex) {
                    this.focusedPattern.setValue(focusedPatternIndex);
                } else {
                    this.focusedPattern.bang();
                }
            }
            this.focusedPattern.setRange(Math.max(1, this.mutablePatterns.size()));
            this.listenerSnapshot.clear();
            this.listenerSnapshot.addAll(this.listeners);
            for (Listener listener : this.listenerSnapshot) {
                listener.patternRemoved(this, pattern);
            }
            if (wasActive && (this.mutablePatterns.size() > 0)) {
                LXPattern newActive = getActivePattern();
                if (newActive != null) {
                    newActive.onActive();
                }
                for (Listener listener : this.listeners) {
                    listener.patternDidChange(this, newActive);
                }
            }
            disposePatternScopedComponents(pattern);
            pattern.dispose();
        }
        return this;
    }

    private void _updatePatterns(LXPattern[] patterns) {
        if (patterns == null) {
            throw new IllegalArgumentException("May not set null pattern array");
        }
        for (LXPattern pattern : this.mutablePatterns) {
            pattern.dispose();
        }
        this.mutablePatterns.clear();
        for (LXPattern pattern : patterns) {
            if (pattern == null) {
                throw new IllegalArgumentException("Pattern array may not include null elements");
            }
            addPattern(pattern);
        }
    }

    public LXChannel movePattern(LXPattern pattern, int index) {
        LXPattern focusedPattern = getFocusedPattern();
        LXPattern activePattern = getActivePattern();
        LXPattern nextPattern = getNextPattern();
        this.mutablePatterns.remove(pattern);
        this.mutablePatterns.add(index, pattern);
        LXUtils.updateIndexes(mutablePatterns);
        this.activePatternIndex = activePattern.getIndex();
        this.nextPatternIndex = nextPattern.getIndex();
        for (Listener listener : this.listeners) {
            listener.patternMoved(this, pattern);
        }
        if (pattern == focusedPattern) {
            this.focusedPattern.setValue(pattern.getIndex());
        }
        return this;
    }

    public Collection<LXComponent> allComponents() {
        ArrayList<LXComponent> components = new ArrayList<>();
        components.addAll(mutablePatterns);
        components.addAll(mutableEffects);
        components.addAll(mutableWarps);
        for (List<LXEffect> es : patternEffects.values()) components.addAll(es);
        for (List<LXWarp> ws : patternWarps.values()) components.addAll(ws);
        return components;
    }

    // -----------------------------------------------------------------------
    // Per-pattern effects / warps API
    // -----------------------------------------------------------------------

    /**
     * Returns the (mutable, live) list of effects scoped to the given pattern.
     * Effects in this list only run while the given pattern is active.
     */
    public List<LXEffect> getPatternEffects(LXPattern pattern) {
        return patternEffects.computeIfAbsent(pattern, k -> new ArrayList<>());
    }

    /**
     * Returns the (mutable, live) list of warps scoped to the given pattern.
     * Warps in this list only apply while the given pattern is active.
     */
    public List<LXWarp> getPatternWarps(LXPattern pattern) {
        return patternWarps.computeIfAbsent(pattern, k -> new ArrayList<>());
    }

    public LXChannel addPatternEffect(LXPattern pattern, LXEffect effect) {
        List<LXEffect> list = getPatternEffects(pattern);
        list.add(effect);
        effect.setBus(this);
        effect.setPattern(pattern);
        LXUtils.updateIndexes(list);
        for (Listener l : this.listeners) {
            l.patternEffectAdded(this, pattern, effect);
        }
        return this;
    }

    public LXChannel removePatternEffect(LXPattern pattern, LXEffect effect) {
        List<LXEffect> list = getPatternEffects(pattern);
        int idx = list.indexOf(effect);
        if (idx >= 0) {
            effect.setIndex(-1);
            effect.setPattern(null);
            list.remove(idx);
            LXUtils.updateIndexes(list);
            for (Listener l : this.listeners) {
                l.patternEffectRemoved(this, pattern, effect);
            }
            effect.dispose();
        }
        return this;
    }

    public LXChannel movePatternEffect(LXPattern pattern, LXEffect effect, int index) {
        List<LXEffect> list = getPatternEffects(pattern);
        list.remove(effect);
        list.add(index, effect);
        LXUtils.updateIndexes(list);
        for (Listener l : this.listeners) {
            l.patternEffectMoved(this, pattern, effect);
        }
        return this;
    }

    public LXChannel addPatternWarp(LXPattern pattern, LXWarp warp) {
        List<LXWarp> list = getPatternWarps(pattern);
        list.add(warp);
        warp.setBus(this);
        warp.setPattern(pattern);
        LXUtils.updateIndexes(list);
        for (Listener l : this.listeners) {
            l.patternWarpAdded(this, pattern, warp);
        }
        return this;
    }

    public LXChannel removePatternWarp(LXPattern pattern, LXWarp warp) {
        List<LXWarp> list = getPatternWarps(pattern);
        int idx = list.indexOf(warp);
        if (idx >= 0) {
            warp.setIndex(-1);
            warp.setPattern(null);
            list.remove(idx);
            LXUtils.updateIndexes(list);
            for (Listener l : this.listeners) {
                l.patternWarpRemoved(this, pattern, warp);
            }
            warp.dispose();
        }
        return this;
    }

    public LXChannel movePatternWarp(LXPattern pattern, LXWarp warp, int index) {
        List<LXWarp> list = getPatternWarps(pattern);
        list.remove(warp);
        list.add(index, warp);
        LXUtils.updateIndexes(list);
        for (Listener l : this.listeners) {
            l.patternWarpMoved(this, pattern, warp);
        }
        return this;
    }

    private void disposePatternScopedComponents(LXPattern pattern) {
        List<LXEffect> es = patternEffects.remove(pattern);
        if (es != null) {
            for (LXEffect e : es) e.dispose();
        }
        List<LXWarp> ws = patternWarps.remove(pattern);
        if (ws != null) {
            for (LXWarp w : ws) w.dispose();
        }
    }

    public final int getFocusedPatternIndex() {
        return this.focusedPattern.getValuei();
    }

    public final LXPattern getFocusedPattern() {
        if (mutablePatterns.isEmpty()) {
            return null;
        }
        return this.mutablePatterns.get(this.focusedPattern.getValuei());
    }

    public final int getActivePatternIndex() {
        return this.activePatternIndex;
    }

    public final LXPattern getActivePattern() {
        if (mutablePatterns.isEmpty()) {
            return null;
        }
        return this.mutablePatterns.get(this.activePatternIndex);
    }

    public final int getNextPatternIndex() {
        return this.nextPatternIndex;
    }

    public final LXPattern getNextPattern() {
        if (mutablePatterns.isEmpty()) {
            return null;
        }
        return this.mutablePatterns.get(this.nextPatternIndex);
    }

    public final LXBus goPrev() {
        if (this.transition != null || mutablePatterns.isEmpty()) {
            return this;
        }
        this.nextPatternIndex = this.activePatternIndex - 1;
        if (this.nextPatternIndex < 0) {
            this.nextPatternIndex = this.mutablePatterns.size() - 1;
        }
        startTransition();
        return this;
    }

    public final LXBus goNext() {
        if (this.transition != null || mutablePatterns.isEmpty()) {
            return this;
        }
        this.nextPatternIndex = this.activePatternIndex;
        do {
            this.nextPatternIndex = (this.nextPatternIndex + 1)
                    % this.mutablePatterns.size();
        } while ((this.nextPatternIndex != this.activePatternIndex)
                && !getNextPattern().isAutoCycleEligible());
        if (this.nextPatternIndex != this.activePatternIndex) {
            startTransition();
        }
        return this;
    }

    public final LXBus goPattern(LXPattern pattern) {
        int pi = 0;
        for (LXPattern p : this.mutablePatterns) {
            if (p == pattern) {
                return goIndex(pi);
            }
            ++pi;
        }
        return this;
    }

    public final LXBus goIndex(int i) {
        if (i < 0 || i >= this.mutablePatterns.size()) {
            return this;
        }
        if (this.transition != null) {
            finishTransition();
        }
        this.nextPatternIndex = i;
        startTransition();
        return this;
    }

    public LXLook getParentLook() {
        LXComponent parent = getParent();
        if (parent != null && parent instanceof LXLook) {
            return (LXLook) parent;
        }
        return null;
    }

    public LXBus disableAutoTransition() {
        this.autoCycleEnabled.setValue(false);
        return this;
    }

    /**
     * Enable automatic transition from pattern to pattern on this channel
     *
     * @param autoTransitionThreshold time in seconds
     * @return
     */
    public LXBus enableAutoTransition(double autoTransitionThreshold) {
        this.autoCycleTimeSecs.setValue(autoTransitionThreshold);
        this.autoCycleEnabled.setValue(true);
        return this;
    }

    /**
     * Return progress towards making a cycle
     *
     * @return amount of progress towards the next cycle
     */
    public double getAutoCycleProgress() {
        return this.autoCycleProgress;
    }

    /**
     * Return progress through a transition
     *
     * @return amount of progress thru current transition
     */
    public double getTransitionProgress() {
        return this.transitionProgress;
    }

    private void startTransition() {
        LXPattern activePattern = getActivePattern();
        LXPattern nextPattern = getNextPattern();
        if (activePattern == nextPattern || activePattern == null || nextPattern == null) {
            return;
        }
        nextPattern.onActive();
        for (Listener listener : this.listeners) {
            listener.patternWillChange(this, activePattern, nextPattern);
        }
        if (this.transitionEnabled.isOn()) {
            this.transition = lx.engine.crossfaderBlends[this.transitionBlendMode.getValuei()];
            nextPattern.onTransitionStart();
            this.transitionMillis = this.lx.engine.nowMillis;
        } else {
            finishTransition();
        }
    }

    private void finishTransition() {
        getActivePattern().onInactive();
        this.activePatternIndex = this.nextPatternIndex;
        LXPattern activePattern = getActivePattern();
        if (this.transition != null) {
            activePattern.onTransitionEnd();
        }
        this.transition = null;
        this.transitionMillis = this.lx.engine.nowMillis;
        for (Listener listener : listeners) {
            listener.patternDidChange(this, activePattern);
        }
    }

    protected void setVectorArray(LXVector[] newVectorArray, LXWarp newVectorSource) {
        super.setVectorArray(newVectorArray, newVectorSource);
        for (LXPattern pattern : patterns) {
            pattern.onVectorsChanged();
        }
    }

    @Override
    public void loop(double deltaMs) {
        long loopStart = System.nanoTime();
        deltaMs *= this.speed.getValue();
        for (LXEffect effect : effects) {
            if (effect instanceof SpeedEffect && effect.isEnabled()) {
                deltaMs *= ((SpeedEffect) effect).speed.getValue();
                break;
            }
        }

        // Run modulators and components
        super.loop(deltaMs);

        // Apply channel warps
        LXWarp nextInputSource = null;
        LXVector[] nextInputVectors = model.getVectorArray();
        boolean nextInputChanged = false;
        for (LXWarp warp : warps) {
            if (warp.isEnabled()) {
                warp.setInputVectors(nextInputSource, nextInputVectors, nextInputChanged);
                nextInputChanged = warp.applyWarp(deltaMs);
                nextInputSource = warp;
                nextInputVectors = warp.getOutputVectors();
            }
        }
        if (nextInputVectors != vectorArray || nextInputSource != vectorSource || nextInputChanged) {
            setVectorArray(nextInputVectors, nextInputSource);
        }

        // Snapshot channel-warped vectors so per-pattern warps can temporarily
        // override and we can restore before running channel-level effects.
        LXVector[] channelVectors = vectorArray;
        LXWarp channelVectorSource = vectorSource;

        if (!blendPatterns.isOn()) {
            loopNoPatternBlend(deltaMs, channelVectors, channelVectorSource);
        } else {
            loopWithPatternBlend(deltaMs, channelVectors, channelVectorSource);
        }

        // Restore channel vectors so channel-level effects see the channel chain output,
        // not whatever the last pattern's warps left behind.
        if (vectorArray != channelVectors || vectorSource != channelVectorSource) {
            setVectorArray(channelVectors, channelVectorSource);
        }

        // Apply channel-level effects
        for (LXEffect effect : effects) {
            effect.setPolyBuffer(polyBuffer);
            effect.loop(deltaMs);
        }

        this.timer.loopNanos = System.nanoTime() - loopStart;
    }

    /**
     * Run a single pattern with its pattern-scoped warps and effects.
     * Warps temporarily mutate the channel's vectorArray; the caller is responsible
     * for restoring it after all patterns have run.
     */
    private void runPatternScoped(LXPattern pat, double deltaMs, PolyBuffer.Space space,
                                  LXVector[] baseVectors, LXWarp baseVectorSource) {
        List<LXWarp> pWarps = patternWarps.get(pat);
        LXVector[] curVecs = baseVectors;
        LXWarp curSource = baseVectorSource;
        boolean changed = false;
        if (pWarps != null) {
            for (LXWarp w : pWarps) {
                if (w.isEnabled()) {
                    w.setInputVectors(curSource, curVecs, changed);
                    changed = w.applyWarp(deltaMs);
                    curSource = w;
                    curVecs = w.getOutputVectors();
                }
            }
        }
        if (curVecs != vectorArray || curSource != vectorSource || changed) {
            setVectorArray(curVecs, curSource);
        }

        // Scale deltaMs by any SpeedEffect in this pattern's per-pattern effects
        List<LXEffect> pEffects = patternEffects.get(pat);
        double patDeltaMs = deltaMs;
        if (pEffects != null) {
            for (LXEffect e : pEffects) {
                if (e instanceof SpeedEffect && e.isEnabled()) {
                    patDeltaMs *= ((SpeedEffect) e).speed.getValue();
                    break;
                }
            }
        }

        pat.setPreferredSpace(space);
        pat.loop(patDeltaMs);

        // Per-pattern effects modify the pattern's own polyBuffer before blending.
        if (pEffects != null) {
            for (LXEffect e : pEffects) {
                e.setPolyBuffer(pat.getPolyBuffer());
                e.loop(patDeltaMs);
            }
        }
    }

    public void loopWithPatternBlend(double deltaMs) {
        loopWithPatternBlend(deltaMs, vectorArray, vectorSource);
    }

    public void loopWithPatternBlend(double deltaMs, LXVector[] baseVectors, LXWarp baseVectorSource) {
        PolyBuffer.Space space = colorSpace.getEnum();
        LXBlend blend = patternBlendMode.getObject();

        polyBuffer.setZero();
        boolean first = true;
        for (int i = 0; i < patterns.size(); i++) {
            LXPattern pat = patterns.get(i);
            if (!pat.enabled.isOn()) {
                continue;
            }
            runPatternScoped(pat, deltaMs, space, baseVectors, baseVectorSource);

            if (first) {
                polyBuffer.copyFrom(pat, space);
                first = false;
            } else {
                blend.blend(polyBuffer, pat, 1, polyBuffer, space);
            }
        }
    }

    /**
     * Implements the old one-pattern-per-channel behavior of LXChannel, called only if blendPatterns is off.
     */
    public void loopNoPatternBlend(double deltaMs) {
        loopNoPatternBlend(deltaMs, vectorArray, vectorSource);
    }

    public void loopNoPatternBlend(double deltaMs, LXVector[] baseVectors, LXWarp baseVectorSource) {
        // Check for transition completion
        if (this.transition != null) {
            double transitionMs = this.lx.engine.nowMillis - this.transitionMillis;
            double transitionDone = 1000 * this.transitionTimeSecs.getValue();
            if (transitionMs >= transitionDone) {
                finishTransition();
            }
        }

        // Auto-cycle if appropriate
        if (this.transition == null) {
            this.autoCycleProgress = (this.lx.engine.nowMillis - this.transitionMillis) / (1000 * this.autoCycleTimeSecs.getValue());
            if (this.autoCycleProgress >= 1) {
                this.autoCycleProgress = 1;
                if (this.autoCycleEnabled.isOn()) {
                    goNext();
                }
            }
        }

        // Run active pattern (with its per-pattern warps & effects)
        PolyBuffer.Space space = colorSpace.getEnum();
        LXPattern active = getActivePattern();
        if (active != null) {
            runPatternScoped(active, deltaMs, space, baseVectors, baseVectorSource);
        }

        // Run transition!
        if (this.transition != null) {
            this.autoCycleProgress = 1;
            this.transitionProgress = (this.lx.engine.nowMillis - this.transitionMillis) / (1000 * this.transitionTimeSecs.getValue());
            LXPattern next = getNextPattern();
            runPatternScoped(next, deltaMs, space, baseVectors, baseVectorSource);
            // TODO(mcslee): this is incorrect. the blend objects are shared, so the same one may be run on multiple
            // channels. either they need to be per-channel instances, or they are not loopable with modulators etc.
            this.transition.loop(deltaMs);

            if (transitionProgress < 0.5) {
                transition.blend(getActivePattern(), getNextPattern(),
                        transitionProgress * 2, polyBuffer, space);
            } else {
                transition.blend(getNextPattern(), getActivePattern(),
                        (1 - transitionProgress) * 2, polyBuffer, space);
            }
        } else if (active != null) {
            this.transitionProgress = 0;
            polyBuffer.copyFrom(active, space);
        } else {
            polyBuffer.setZero();
        }
    }

    public PolyBuffer getPolyBuffer() {
        return polyBuffer;
    }

    @Deprecated
    int[] getColors() {
        return (int[]) polyBuffer.getArray(SRGB8);
    }

    @Override
    public void dispose() {
        /* Kill the channel thread before mutating any internal state to
         * prevent the thread from accessing dead data. */
        if (this.thread.hasStarted) {
            this.thread.interrupt();
            /* block on dispose until the channel thread has been shut down */
            try {
                this.thread.join();
            } catch (InterruptedException e) {}
        }
        for (LXPattern pattern : this.mutablePatterns) {
            disposePatternScopedComponents(pattern);
            pattern.dispose();
        }
        this.mutablePatterns.clear();
        patternEffects.clear();
        patternWarps.clear();
        super.dispose();
    }

    private static final String KEY_PATTERNS = "patterns";
    private static final String KEY_PATTERN_INDEX = "patternIndex";
    private static final String KEY_PATTERN_EFFECTS = "patternEffects";
    private static final String KEY_PATTERN_WARPS = "patternWarps";

    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);
        obj.addProperty(KEY_PATTERN_INDEX, this.activePatternIndex);
        // Patch each pattern's JSON with its per-pattern effect/warp chains so
        // they're saved alongside the pattern (and loaded together).
        JsonArray patternsArr = new JsonArray();
        for (LXPattern p : this.mutablePatterns) {
            JsonObject pObj = new JsonObject();
            p.save(lx, pObj);
            List<LXEffect> es = patternEffects.get(p);
            if (es != null && !es.isEmpty()) {
                pObj.add(KEY_PATTERN_EFFECTS, LXSerializable.Utils.toArray(lx, es));
            }
            List<LXWarp> ws = patternWarps.get(p);
            if (ws != null && !ws.isEmpty()) {
                pObj.add(KEY_PATTERN_WARPS, LXSerializable.Utils.toArray(lx, ws));
            }
            patternsArr.add(pObj);
        }
        obj.add(KEY_PATTERNS, patternsArr);
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        // Remove patterns
        for (int i = this.mutablePatterns.size() - 1; i >= 0; --i) {
            removePattern(this.mutablePatterns.get(i));
        }

        // Add patterns
        JsonArray patternsArray = obj.getAsJsonArray(KEY_PATTERNS);
        for (JsonElement patternElement : patternsArray) {
            JsonObject patternObj = (JsonObject) patternElement;
            LXPattern pattern = this.lx.instantiatePattern(patternObj.get(KEY_CLASS).getAsString());
            if (pattern != null) {
                pattern.load(lx, patternObj);
                addPattern(pattern);

                // Restore per-pattern effects
                if (patternObj.has(KEY_PATTERN_EFFECTS)) {
                    for (JsonElement el : patternObj.getAsJsonArray(KEY_PATTERN_EFFECTS)) {
                        JsonObject eObj = (JsonObject) el;
                        LXEffect e = this.lx.instantiateEffect(eObj.get("class").getAsString());
                        if (e != null) {
                            e.load(lx, eObj);
                            addPatternEffect(pattern, e);
                        }
                    }
                }
                // Restore per-pattern warps
                if (patternObj.has(KEY_PATTERN_WARPS)) {
                    for (JsonElement el : patternObj.getAsJsonArray(KEY_PATTERN_WARPS)) {
                        JsonObject wObj = (JsonObject) el;
                        LXWarp w = this.lx.instantiateWarp(wObj.get("class").getAsString());
                        if (w != null) {
                            w.load(lx, wObj);
                            addPatternWarp(pattern, w);
                        }
                    }
                }
            }
        }

        // Set the active index instantly, do not transition!
        this.activePatternIndex = this.nextPatternIndex = 0;
        if (obj.has(KEY_PATTERN_INDEX)) {
            int patternIndex = obj.get(KEY_PATTERN_INDEX).getAsInt();
            if (patternIndex < this.patterns.size()) {
                this.activePatternIndex = this.nextPatternIndex = patternIndex;
            }
        }
        LXPattern activePattern = getActivePattern();
        if (activePattern != null) {
            activePattern.onActive();
            for (Listener listener : listeners) {
                listener.patternDidChange(this, activePattern);
            }
        }

        // Set the focused pattern to the active one
        this.focusedPattern.setValue(this.activePatternIndex);

        super.load(lx, obj);
    }

}
