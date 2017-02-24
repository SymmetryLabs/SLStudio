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

import heronarts.lx.color.LXPalette;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transition.LXTransition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A channel is a single component of the engine that has a set of patterns from
 * which it plays and rotates. It also has a fader to control how this channel
 * is blended with the channels before it.
 */
public class LXChannel extends LXBus{

    public class Timer extends LXComponent.Timer {
        public long blendNanos;
    }

    @Override
    protected LXComponent.Timer constructTimer() {
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
     * The symbolic name of this channel.
     */
    public final StringParameter name;

    /**
     * Whether this channel is enabled.
     */
    public final BooleanParameter enabled = new BooleanParameter("ON", true);

    /**
     * Crossfade group this channel belongs to
     */
    public final DiscreteParameter crossfadeGroup = new DiscreteParameter("GROUP", CROSSFADE_OPTIONS, CROSSFADE_GROUP_BYPASS);

    /**
     * Whether this channel should listen to MIDI events
     */
    public final BooleanParameter midiMonitor = new BooleanParameter("MIDI", false);

    /**
     * Whether this channel should show in the cue UI.
     */
    public final BooleanParameter cueActive = new BooleanParameter("CUE", false);

    /**
     * Whether auto pattern transition is enabled on this channel
     */
    public final BooleanParameter autoTransitionEnabled = new BooleanParameter("AUTO", false);

    /**
     * Time in milliseconds after which transition thru the pattern set is automatically initiated.
     */
    public final BoundedParameter autoTransitionTimeSecs = new BoundedParameter("AUTO-TIME", 60, .1, 60*60*4);

    private double autoTransitionProgress = 0;

    private final List<LXPattern> patterns = new ArrayList<LXPattern>();
    private final List<LXPattern> unmodifiablePatterns = Collections.unmodifiableList(patterns);

    /**
     * This is a local buffer used for transition blending on this channel
     */
    private final ModelBuffer blendBuffer;

    private int[] colors;

    private int activePatternIndex = 0;
    private int nextPatternIndex = 0;

    public final BoundedParameter fader = new BoundedParameter("FADER", 0);

    public final DiscreteParameter blendMode;

    private LXTransition transition = null;
    private long transitionMillis = 0;

    LXChannel(LX lx, int index, LXPattern[] patterns) {
        super(lx);
        this.index = index;
        this.name = new StringParameter("Name", "Channel-" + (index+1));
        this.blendBuffer = new ModelBuffer(lx);
        this.blendMode = new DiscreteParameter("BLEND", lx.engine.channelBlends);
        this.transitionMillis = System.currentTimeMillis();
        _updatePatterns(patterns);
        this.colors = this.getActivePattern().getColors();

        addParameter(this.enabled);
        addParameter(this.cueActive);
        addParameter(this.midiMonitor);
        addParameter(this.autoTransitionEnabled);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == this.autoTransitionEnabled) {
            if (this.transition == null) {
                this.transitionMillis = System.currentTimeMillis();
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

    @Override
    protected void onPaletteChanged(LXPalette palette) {
        super.onPaletteChanged(palette);
        for (LXPattern pattern : this.patterns) {
            pattern.setPalette(palette);
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

    public final LXBus setPatterns(LXPattern[] patterns) {
        getActivePattern().onInactive();
        _updatePatterns(patterns);
        this.activePatternIndex = this.nextPatternIndex = 0;
        this.transition = null;
        getActivePattern().onActive();
        return this;
    }

    public final LXBus addPattern(LXPattern pattern) {
        pattern.setChannel(this);
        ((LXComponent)pattern).setModel(this.model);
        ((LXComponent)pattern).setPalette(this.palette);
        this.patterns.add(pattern);
        for (Listener listener : this.listeners) {
            listener.patternAdded(this, pattern);
        }
        return this;
    }

    public final LXBus removePattern(LXPattern pattern) {
        if (this.patterns.size() <= 1) {
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
            // TODO(mcslee): turn this into pattern.destroy() and remove listeners
            // for garbage collectability
            pattern.setChannel(null);
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
            for (Listener listener : this.listeners) {
                listener.patternRemoved(this, pattern);
            }
            if (wasActive) {
                LXPattern newActive = getActivePattern();
                newActive.onActive();
                for (Listener listener : this.listeners) {
                    listener.patternDidChange(this, newActive);
                }
            }
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

    protected final LXTransition getActiveTransition() {
        return this.transition;
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
                && !getNextPattern().isEligible());
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
        this.autoTransitionEnabled.setValue(false);
        return this;
    }

    /**
     * Enable automatic transition from pattern to pattern on this channel
     *
     * @param autoTransitionThresholdTransition time in seconds
     * @return
     */
    public LXBus enableAutoTransition(double autoTransitionThreshold) {
        this.autoTransitionTimeSecs.setValue(autoTransitionThreshold);
        this.autoTransitionEnabled.setValue(true);
        return this;
    }

    /**
     * Return progress towards making a transition
     *
     * @return amount of progress towards the next cycle
     */
    public double getAutoTransitionProgress() {
        return this.autoTransitionProgress;
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
        this.transition = nextPattern.getTransition();
        if (this.transition == null) {
            finishTransition();
        } else {
            nextPattern.onTransitionStart();
            this.transition.blend(activePattern.getColors(), nextPattern.getColors(), 0);
            this.transitionMillis = System.currentTimeMillis();
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
        this.transitionMillis = System.currentTimeMillis();
        for (Listener listener : listeners) {
            listener.patternDidChange(this, activePattern);
        }
    }

    @Override
    public void loop(double deltaMs) {
        long loopStart = System.nanoTime();

        // Run modulators and components
        super.loop(deltaMs);

        // Run active pattern
        LXPattern activePattern = getActivePattern();
        activePattern.loop(deltaMs);

        // Run transition if applicable
        if (this.transition != null) {
            this.autoTransitionProgress = 1.;
            int transitionMs = (int) (this.lx.engine.nowMillis - this.transitionMillis);
            if (transitionMs >= this.transition.getDuration()) {
                finishTransition();
            } else {
                getNextPattern().loop(deltaMs);
                this.transition.loop(deltaMs);
                this.transition.blend(
                    getActivePattern().getColors(),
                    getNextPattern().getColors(),
                    transitionMs / this.transition.getDuration()
                );
            }
        } else {
            this.autoTransitionProgress = (this.lx.engine.nowMillis - this.transitionMillis) / (1000 * this.autoTransitionTimeSecs.getValue());
            if (this.autoTransitionProgress >= 1) {
                this.autoTransitionProgress = 1;
                if (this.autoTransitionEnabled.isOn()) {
                    goNext();
                }
            }
        }

        int[] colors = (this.transition != null) ? this.transition.getColors() : getActivePattern().getColors();

        if (this.effects.size() > 0) {
            int[] array = this.blendBuffer.getArray();
            System.arraycopy(colors, 0, array, 0, colors.length);
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

}
