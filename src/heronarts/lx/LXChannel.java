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
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.transition.DissolveTransition;
import heronarts.lx.transition.LXTransition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A channel is a single component of the engine that has a set of patterns from
 * which it plays and rotates. It also has a fader to control how this channel
 * is blended with the channels before it.
 */
public class LXChannel extends LXComponent {

    /**
     * Listener interface for objects which want to be notified when the internal
     * channel state is modified.
     */
    public interface Listener {

        public void effectAdded(LXChannel channel, LXEffect effect);

        public void effectRemoved(LXChannel channel, LXEffect effect);

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
        public void effectAdded(LXChannel channel, LXEffect effect) {
        }

        @Override
        public void effectRemoved(LXChannel channel, LXEffect effect) {
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
        public void faderTransitionDidChange(LXChannel channel,
                LXTransition faderTransition) {
        }
    }

    /**
     * This channel bypasses the crossfader
     */
    public static final int CROSSFADE_GROUP_BYPASS = 0;

    /**
     * This channel belongs to the left crossfade group
     */
    public static final int CROSSFADE_GROUP_LEFT = 1;

    /**
     * This channel belongs to the right crossfade group
     */
    public static final int CROSSFADE_GROUP_RIGHT = 2;

    private final LX lx;

    /**
     * The index of this channel in the engine.
     */
    private int index;

    /**
     * Whether this channel is enabled.
     */
    public final BooleanParameter enabled = new BooleanParameter("ON", true);

    /**
     * Crossfade group this channel belongs to
     */
    public final DiscreteParameter crossfadeGroup = new DiscreteParameter("GROUP", 3);

    /**
     * Whether this channel should listen to MIDI events
     */
    public final BooleanParameter midiEnabled = new BooleanParameter("MIDI", false);

    /**
     * Whether auto pattern transition is enabled on this channel
     */
    public final BooleanParameter autoTransitionEnabled = new BooleanParameter("AUTO", false);

    /**
     * Time in milliseconds after which transition thru the pattern set is automatically initiated.
     */
    public final MutableParameter autoTransitionTime = new MutableParameter("AUTO-TIME", 60000);

    private final List<LXPattern> patterns = new ArrayList<LXPattern>();
    private final List<LXPattern> unmodifiablePatterns = Collections.unmodifiableList(patterns);

    private final List<LXEffect> effects = new ArrayList<LXEffect>();
    private final List<LXEffect> unmodifiableEffects = Collections.unmodifiableList(effects);

    /**
     * This is a local buffer used for transition blending on this channel
     */
    private final ModelBuffer blendBuffer;

    private int[] colors;

    private int activePatternIndex = 0;
    private int nextPatternIndex = 0;

    private LXTransition faderTransition = null;
    private final BoundedParameter fader = new BoundedParameter("FADER", 0);

    private LXTransition transition = null;
    private long transitionMillis = 0;

    private final List<Listener> listeners = new ArrayList<Listener>();

    LXChannel(LX lx, int index, LXPattern[] patterns) {
        super(lx);
        this.lx = lx;
        this.index = index;
        this.blendBuffer = new ModelBuffer(lx);
        this.faderTransition = new DissolveTransition(lx);
        this.transitionMillis = System.currentTimeMillis();
        _updatePatterns(patterns);
        this.colors = this.getActivePattern().getColors();

        addParameter(this.enabled);
        addParameter(this.midiEnabled);
        addParameter(this.autoTransitionEnabled);

    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == this.autoTransitionEnabled) {
            if (this.transition == null) {
                this.transitionMillis = System.currentTimeMillis();
            }
        }
    }

    @Override
    protected void onModelChanged(LXModel model) {
        for (LXPattern pattern : this.patterns) {
            pattern.setModel(model);
        }
    }

    @Override
    protected void onPaletteChanged(LXPalette palette) {
        for (LXPattern pattern : this.patterns) {
            pattern.setPalette(palette);
        }
    }

    public synchronized final void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public synchronized final void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    final synchronized LXChannel setIndex(int index) {
        this.index = index;
        return this;
    }

    public synchronized final int getIndex() {
        return this.index;
    }

    public final BoundedParameter getFader() {
        return this.fader;
    }

    public synchronized final LXTransition getFaderTransition() {
        return this.faderTransition;
    }

    public synchronized final LXChannel setFaderTransition(LXTransition transition) {
        if (this.faderTransition != transition) {
            this.faderTransition = transition;
            for (Listener listener : this.listeners) {
                listener.faderTransitionDidChange(this, this.faderTransition);
            }
        }
        return this;
    }

    public synchronized final LXChannel addEffect(LXEffect effect) {
        this.effects.add(effect);
        for (Listener listener : this.listeners) {
            listener.effectAdded(this, effect);
        }
        return this;
    }

    public synchronized final LXChannel removeEffect(LXEffect effect) {
        this.effects.remove(effect);
        for (Listener listener : this.listeners) {
            listener.effectRemoved(this, effect);
        }
        return this;
    }

    public synchronized final List<LXEffect> getEffects() {
        return this.unmodifiableEffects;
    }

    public synchronized final List<LXPattern> getPatterns() {
        return this.unmodifiablePatterns;
    }

    public synchronized final LXPattern getPattern(String className) {
        for (LXPattern pattern : this.unmodifiablePatterns) {
            if (pattern.getClass().getName().equals(className)) {
                return pattern;
            }
        }
        return null;
    }

    public synchronized final LXChannel setPatterns(LXPattern[] patterns) {
        getActivePattern().onInactive();
        _updatePatterns(patterns);
        this.activePatternIndex = this.nextPatternIndex = 0;
        this.transition = null;
        getActivePattern().onActive();
        return this;
    }

    public synchronized final LXChannel addPattern(LXPattern pattern) {
        pattern.setChannel(this);
        ((LXComponent)pattern).setModel(this.model);
        ((LXComponent)pattern).setPalette(this.palette);
        this.patterns.add(pattern);
        for (Listener listener : this.listeners) {
            listener.patternAdded(this, pattern);
        }
        return this;
    }

    public synchronized final LXChannel removePattern(LXPattern pattern) {
        if (this.patterns.size() <= 1) {
            throw new UnsupportedOperationException("LXChannel must have at least one pattern");
        }
        int index = this.patterns.indexOf(pattern);
        if (index >= 0) {
            this.patterns.remove(index);
            pattern.setChannel(null);
            if (this.activePatternIndex >= index) {
                --this.activePatternIndex;
                if (this.activePatternIndex < 0) {
                    this.activePatternIndex = this.patterns.size() - 1;
                }
            }
            if (this.nextPatternIndex >= index) {
                --this.nextPatternIndex;
                if (this.nextPatternIndex < 0) {
                    this.nextPatternIndex = this.patterns.size() - 1;
                }
            }
            for (Listener listener : this.listeners) {
                listener.patternRemoved(this, pattern);
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

    public synchronized final int getActivePatternIndex() {
        return this.activePatternIndex;
    }

    public synchronized final LXPattern getActivePattern() {
        return this.patterns.get(this.activePatternIndex);
    }

    public synchronized final int getNextPatternIndex() {
        return this.nextPatternIndex;
    }

    public synchronized final LXPattern getNextPattern() {
        return this.patterns.get(this.nextPatternIndex);
    }

    protected synchronized final LXTransition getActiveTransition() {
        return this.transition;
    }

    public synchronized final LXChannel goPrev() {
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

    public synchronized final LXChannel goNext() {
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

    public synchronized final LXChannel goPattern(LXPattern pattern) {
        int pi = 0;
        for (LXPattern p : this.patterns) {
            if (p == pattern) {
                return goIndex(pi);
            }
            ++pi;
        }
        return this;
    }

    public synchronized final LXChannel goIndex(int i) {
        if (this.transition != null) {
            return this;
        }
        if (i < 0 || i >= this.patterns.size()) {
            return this;
        }
        this.nextPatternIndex = i;
        startTransition();
        return this;
    }

    public synchronized LXChannel disableAutoTransition() {
        this.autoTransitionEnabled.setValue(false);
        return this;
    }

    public synchronized LXChannel enableAutoTransition(int autoTransitionThreshold) {
        this.autoTransitionTime.setValue(autoTransitionThreshold);
        this.autoTransitionEnabled.setValue(true);
        return this;
    }

    private synchronized void startTransition() {
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

    private synchronized void finishTransition() {
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
    public synchronized void loop(double deltaMs) {
        long loopStart = System.nanoTime();

        // Run modulators and components
        super.loop(deltaMs);

        // Run active pattern
        LXPattern activePattern = getActivePattern();
        activePattern.loop(deltaMs);

        // Run transition if applicable
        if (this.transition != null) {
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
            if (this.autoTransitionEnabled.isOn() &&
                    (this.lx.engine.nowMillis - this.transitionMillis > this.autoTransitionTime.getValue())) {
                goNext();
            }
        }

        int[] colors = (this.transition != null) ? this.transition.getColors() : getActivePattern().getColors();

        if (this.effects.size() > 0) {
            int[] array = this.blendBuffer.getArray();
            System.arraycopy(colors, 0, array, 0, colors.length);
            colors = array;
            for (LXEffect effect : this.effects) {
                ((LXLayeredComponent)effect).setBuffer(this.blendBuffer);
                effect.loop(deltaMs);
            }
        }

        this.colors = colors;

        this.timer.loopNanos = System.nanoTime() - loopStart;
    }

    public synchronized int[] getColors() {
        return this.colors;
    }

    public synchronized void copyColors(int[] copy) {
        for (int i = 0; i < this.colors.length; ++i) {
            copy[i] = this.colors[i];
        }
    }

}
