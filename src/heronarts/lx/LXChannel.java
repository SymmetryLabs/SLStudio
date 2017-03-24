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

import heronarts.lx.blend.LXBlend;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transition.LXTransition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A channel is a single component of the engine that has a set of patterns from
 * which it plays and rotates. It also has a fader to control how this channel
 * is blended with the channels before it.
 */
public class LXChannel extends LXBus {

    public class Timer extends LXModulatorComponent.Timer {
        public long blendNanos;
    }

    @Override
    protected LXModulatorComponent.Timer constructTimer() {
        return new Timer();
    }

    /**
     * Listener interface for objects which want to be notified when the internal
     * channel state is modified.
     */
    public interface Listener extends LXBus.Listener {

        public void indexChanged(LXChannel channel);

        public void patternAdded(LXChannel channel, LXPattern pattern);

        public void patternRemoved(LXChannel channel, LXPattern pattern);

        public void patternWillChange(LXChannel channel, LXPattern pattern, LXPattern nextPattern);

        public void patternDidChange(LXChannel channel, LXPattern pattern);

        public void faderTransitionDidChange(LXChannel channel, LXTransition faderTransition);
    }

    /**
     * Utility class to extend in cases where only some methods need overriding.
     */
    public abstract static class AbstractListener implements Listener {

        @Override
        public void indexChanged(LXChannel channel) {
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
        public void patternWillChange(LXChannel channel, LXPattern pattern,
                LXPattern nextPattern) {
        }

        @Override
        public void patternDidChange(LXChannel channel, LXPattern pattern) {
        }

        @Override
        public void faderTransitionDidChange(LXChannel channel, LXTransition faderTransition) {
        }
    }

    private final List<Listener> listeners = new ArrayList<Listener>();

    /**
     * This channel bypasses the crossfader
     */
    public static final int CROSSFADE_GROUP_BYPASS = 1;

    /**
     * This channel belongs to the left crossfade group
     */
    public static final int CROSSFADE_GROUP_LEFT = 0;

    /**
     * This channel belongs to the right crossfade group
     */
    public static final int CROSSFADE_GROUP_RIGHT = 2;

    public static final String[] CROSSFADE_OPTIONS = { "A", "X", "B" };

    /**
     * The index of this channel in the engine.
     */
    private int index;

    /**
     * Whether this channel is enabled.
     */
    public final BooleanParameter enabled = new BooleanParameter("On", true);

    /**
     * Crossfade group this channel belongs to
     */
    public final DiscreteParameter crossfadeGroup = new DiscreteParameter("Group", CROSSFADE_OPTIONS, CROSSFADE_GROUP_BYPASS);

    /**
     * Whether this channel should listen to MIDI events
     */
    public final BooleanParameter midiMonitor = new BooleanParameter("MIDI", false);

    /**
     * Whether this channel should show in the cue UI.
     */
    public final BooleanParameter cueActive = new BooleanParameter("Cue", false);

    /**
     * Whether auto pattern transition is enabled on this channel
     */
    public final BooleanParameter autoCycleEnabled = new BooleanParameter("Cycle", false);

    /**
     * Time in milliseconds after which transition thru the pattern set is automatically initiated.
     */
    public final BoundedParameter autoCycleTimeSecs = (BoundedParameter)
        new BoundedParameter("CycleTime", 60, .1, 60*60*4).setUnits(LXParameter.Units.SECONDS);

    public final BoundedParameter transitionTimeSecs = (BoundedParameter)
        new BoundedParameter("TransitionTime", 5, .1, 180).setUnits(LXParameter.Units.SECONDS);

    public final BooleanParameter transitionsEnabled = new BooleanParameter("Transitions", false);
    public final DiscreteParameter transitionBlendMode;

    public final BoundedParameter fader = new BoundedParameter("Fader", 0);

    public final DiscreteParameter blendMode;

    private final List<LXPattern> patterns = new ArrayList<LXPattern>();
    private final List<LXPattern> unmodifiablePatterns = Collections.unmodifiableList(patterns);

    /**
     * This is a local buffer used for transition blending on this channel
     */
    private final ModelBuffer blendBuffer;

    private int[] colors;

    private double autoCycleProgress = 0;
    private double transitionProgress = 0;
    private int activePatternIndex = 0;
    private int nextPatternIndex = 0;

    private LXBlend transition = null;
    private long transitionMillis = 0;

    LXChannel(LX lx, int index, LXPattern[] patterns) {
        super(lx);
        this.index = index;
        this.label.setValue("Channel-" + (index+1));
        this.blendBuffer = new ModelBuffer(lx);
        this.blendMode = new DiscreteParameter("Blend", lx.engine.channelBlends);
        this.transitionBlendMode = new DiscreteParameter("TransitionBlend", lx.engine.crossfaderBlends);
        this.transitionMillis = lx.engine.nowMillis;
        _updatePatterns(patterns);
        this.colors = this.getActivePattern().getColors();

        addParameter("channelEnabled", this.enabled);
        addParameter("cueActive", this.cueActive);
        addParameter("midiMonitor", this.midiMonitor);
        addParameter("autoCycleEnabled", this.autoCycleEnabled);
        addParameter("autoCycleTimeSecs", this.autoCycleTimeSecs);
        addParameter("fader", this.fader);
        addParameter("crossfadeGroup", this.crossfadeGroup);
        addParameter("blendMode", this.blendMode);
        addParameter("transitionsEnabled", this.transitionsEnabled);
        addParameter("transitionsTimeSecs", this.transitionTimeSecs);
        addParameter("transitionBlendMode", this.transitionBlendMode);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == this.autoCycleEnabled) {
            if (this.transition == null) {
                this.transitionMillis = this.lx.engine.nowMillis;
            }
        } else if (p == this.cueActive) {
            if (this.cueActive.isOn()) {
                this.lx.engine.cueLeft.setValue(false);
                this.lx.engine.cueRight.setValue(false);
            }
        }
    }

    @Override
    protected void onModelChanged(LXModel model) {
        super.onModelChanged(model);
        for (LXPattern pattern : this.patterns) {
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
        return this.unmodifiablePatterns;
    }

    public final LXPattern getPattern(String className) {
        for (LXPattern pattern : this.unmodifiablePatterns) {
            if (pattern.getClass().getName().equals(className)) {
                return pattern;
            }
        }
        return null;
    }

    public final LXChannel setPatterns(LXPattern[] patterns) {
        if (this.transition != null) {
            finishTransition();
        } else {
            getActivePattern().onInactive();
        }
        _updatePatterns(patterns);
        this.activePatternIndex = this.nextPatternIndex = 0;
        this.transition = null;
        getActivePattern().onActive();
        return this;
    }

    public final LXChannel addPattern(LXPattern pattern) {
        pattern.setChannel(this);
        pattern.setModel(this.model);
        this.patterns.add(pattern);
        for (Listener listener : this.listeners) {
            listener.patternAdded(this, pattern);
        }
        return this;
    }

    public final LXChannel removePattern(LXPattern pattern) {
        return removePattern(pattern, true);
    }

    private final LXChannel removePattern(LXPattern pattern, boolean checkLast) {
        if (checkLast && (this.patterns.size() <= 1)) {
            throw new UnsupportedOperationException("LXChannel must have at least one pattern");
        }
        int index = this.patterns.indexOf(pattern);
        if (index >= 0) {
            boolean wasActive = (this.activePatternIndex == index);
            if ((this.transition != null) && (
                    (this.activePatternIndex == index) ||
                    (this.nextPatternIndex == index)
                 )) {
                finishTransition();
            }
            this.patterns.remove(index);
            if (this.activePatternIndex > index) {
                --this.activePatternIndex;
            } else if (this.activePatternIndex >= this.patterns.size()) {
                this.activePatternIndex = this.patterns.size() - 1;
            }
            if (this.nextPatternIndex > index) {
                --this.nextPatternIndex;
            } else if (this.nextPatternIndex >= this.patterns.size()) {
                this.nextPatternIndex = this.patterns.size() - 1;
            }
            if (this.activePatternIndex < 0) {
                this.activePatternIndex = 0;
                this.nextPatternIndex = 0;
            }
            for (Listener listener : this.listeners) {
                listener.patternRemoved(this, pattern);
            }
            if (wasActive && (this.patterns.size() > 0)) {
                LXPattern newActive = getActivePattern();
                newActive.onActive();
                for (Listener listener : this.listeners) {
                    listener.patternDidChange(this, newActive);
                }
            }
            pattern.dispose();
        }
        return this;
    }

    private void _updatePatterns(LXPattern[] patterns) {
        if (patterns == null) {
            throw new IllegalArgumentException("May not set null pattern array");
        }
        if (patterns.length == 0) {
            throw new IllegalArgumentException("LXChannel must have at least one pattern");
        }
        for (LXPattern pattern : this.patterns) {
            pattern.dispose();
        }
        this.patterns.clear();
        for (LXPattern pattern : patterns) {
            if (pattern == null) {
                throw new IllegalArgumentException("Pattern array may not include null elements");
            }
            addPattern(pattern);
        }
    }

    public final int getActivePatternIndex() {
        return this.activePatternIndex;
    }

    public final LXPattern getActivePattern() {
        return this.patterns.get(this.activePatternIndex);
    }

    public final int getNextPatternIndex() {
        return this.nextPatternIndex;
    }

    public final LXPattern getNextPattern() {
        return this.patterns.get(this.nextPatternIndex);
    }

    public final LXBus goPrev() {
        if (this.transition != null) {
            return this;
        }
        this.nextPatternIndex = this.activePatternIndex - 1;
        if (this.nextPatternIndex < 0) {
            this.nextPatternIndex = this.patterns.size() - 1;
        }
        startTransition();
        return this;
    }

    public final LXBus goNext() {
        if (this.transition != null) {
            return this;
        }
        this.nextPatternIndex = this.activePatternIndex;
        do {
            this.nextPatternIndex = (this.nextPatternIndex + 1)
                    % this.patterns.size();
        } while ((this.nextPatternIndex != this.activePatternIndex)
                && !getNextPattern().isAutoCycleEligible());
        if (this.nextPatternIndex != this.activePatternIndex) {
            startTransition();
        }
        return this;
    }

    public final LXBus goPattern(LXPattern pattern) {
        int pi = 0;
        for (LXPattern p : this.patterns) {
            if (p == pattern) {
                return goIndex(pi);
            }
            ++pi;
        }
        return this;
    }

    public final LXBus goIndex(int i) {
        if (i < 0 || i >= this.patterns.size()) {
            return this;
        }
        if (this.transition != null) {
            finishTransition();
        }
        this.nextPatternIndex = i;
        startTransition();
        return this;
    }

    public LXBus disableAutoTransition() {
        this.autoCycleEnabled.setValue(false);
        return this;
    }

    /**
     * Enable automatic transition from pattern to pattern on this channel
     *
     * @param autoTransitionThresholdTransition time in seconds
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
        if (activePattern == nextPattern) {
            return;
        }
        nextPattern.onActive();
        for (Listener listener : this.listeners) {
            listener.patternWillChange(this, activePattern, nextPattern);
        }
        if (this.transitionsEnabled.isOn()) {
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

    @Override
    public void loop(double deltaMs) {
        long loopStart = System.nanoTime();

        // Run modulators and components
        super.loop(deltaMs);

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

        // Run active pattern
        LXPattern activePattern = getActivePattern();
        activePattern.loop(deltaMs);
        int[] colors = activePattern.getColors();

        // Run transition!
        if (this.transition != null) {
            this.autoCycleProgress = 1.;
            this.transitionProgress = (this.lx.engine.nowMillis - this.transitionMillis) / (1000 * this.transitionTimeSecs.getValue());
            getNextPattern().loop(deltaMs);;
            // TODO(mcslee): this is incorrect. the blend objects are shared, so the same one may be run on multiple
            // channels. either they need to be per-channel instances, or they are not loopable with modulators etc.
            this.transition.loop(deltaMs);
            colors = this.blendBuffer.getArray();
            if (this.transitionProgress < .5) {
                double alpha = Math.min(1, this.transitionProgress*2.);
                this.transition.blend(
                    getActivePattern().getColors(),
                    getNextPattern().getColors(),
                    alpha,
                    colors
                );
            } else {
                double alpha = Math.max(0, (1-this.transitionProgress)*2.);
                this.transition.blend(
                    getNextPattern().getColors(),
                    getActivePattern().getColors(),
                    alpha,
                    colors
                );
            }
        } else {
            this.transitionProgress = 0;
        }

        // Apply effects
        if (this.effects.size() > 0) {
            int[] array = this.blendBuffer.getArray();
            if (colors != array) {
                System.arraycopy(colors, 0, array, 0, colors.length);
            }
            colors = array;
            for (LXEffect effect : this.effects) {
                effect.setBuffer(this.blendBuffer);
                effect.loop(deltaMs);
            }
        }

        this.colors = colors;
        this.timer.loopNanos = System.nanoTime() - loopStart;
    }

    int[] getColors() {
        return this.colors;
    }

    @Override
    public void dispose() {
        for (LXPattern pattern : this.patterns) {
            pattern.dispose();
        }
        this.patterns.clear();
        super.dispose();
    }

    private static final String KEY_PATTERNS = "patterns";
    private static final String KEY_PATTERN_INDEX = "patternIndex";

    @Override
    public void save(JsonObject obj) {
        super.save(obj);
        JsonArray patterns = new JsonArray();
        for (LXPattern pattern : this.patterns) {
            JsonObject patternObj = new JsonObject();
            pattern.save(patternObj);
            patterns.add(patternObj);
        }
        obj.addProperty(KEY_PATTERN_INDEX, this.activePatternIndex);
        obj.add(KEY_PATTERNS, patterns);
    }

    @Override
    public void load(JsonObject obj) {
        super.load(obj);
        // Remove patterns
        for (int i = this.patterns.size() - 1; i >= 0; --i) {
            removePattern(this.patterns.get(i), false);
        }
        // Add patterns
        JsonArray patternsArray = obj.getAsJsonArray(KEY_PATTERNS);
        for (JsonElement patternElement : patternsArray) {
            JsonObject patternObj = (JsonObject) patternElement;
            LXPattern pattern = this.lx.instantiatePattern(patternObj.get(KEY_CLASS).getAsString());
            pattern.load(patternObj);
            addPattern(pattern);
        }
        // Set the active index instantly, do not transition!
        if (obj.has(KEY_PATTERN_INDEX)) {
            int patternIndex = obj.get(KEY_PATTERN_INDEX).getAsInt();
            if (this.activePatternIndex != patternIndex) {
                getActivePattern().onInactive();
                this.activePatternIndex = this.nextPatternIndex = patternIndex;
                LXPattern activePattern = getActivePattern();
                activePattern.onActive();
                for (Listener listener : listeners) {
                    listener.patternDidChange(this, activePattern);
                }
            }
        }
    }

}
