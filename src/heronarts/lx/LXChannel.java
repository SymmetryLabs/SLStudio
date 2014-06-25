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
import heronarts.lx.parameter.BasicParameter;
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
public class LXChannel implements LXLoopTask {

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

    public class Timer {
        public long runNanos = 0;
    }

    public final Timer timer = new Timer();

    /**
     * The index of this channel in the engine.
     */
    private int index;

    private final LX lx;

    public final BooleanParameter enabled = new BooleanParameter("ON", true);
    public final BooleanParameter midiEnabled = new BooleanParameter("MIDI", false);
    public final BooleanParameter autoTransitionEnabled = new BooleanParameter("AUTO", false);

    private final List<LXPattern> patterns = new ArrayList<LXPattern>();
    private final List<LXPattern> unmodifiablePatterns = Collections.unmodifiableList(patterns);

    private final List<LXEffect> effects = new ArrayList<LXEffect>();
    private final List<LXEffect> unmodifiableEffects = Collections.unmodifiableList(effects);

    private final ModelBuffer buffer;
    private int[] colors;

    private int activePatternIndex = 0;
    private int nextPatternIndex = 0;

    private int autoTransitionThreshold = 60000;

    private LXTransition faderTransition = null;
    private final BasicParameter fader = new BasicParameter("FADER", 0);

    private LXTransition transition = null;
    private long transitionMillis = 0;

    private final List<Listener> listeners = new ArrayList<Listener>();

    LXChannel(LX lx, int index, LXPattern[] patterns) {
        this.lx = lx;
        this.index = index;
        this.buffer = new ModelBuffer(lx);
        this.faderTransition = new DissolveTransition(lx);
        this.transitionMillis = System.currentTimeMillis();
        _updatePatterns(patterns);
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

    public final BasicParameter getFader() {
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
        if (pattern.getChannel() != this) {
            pattern.setChannel(this);
        }
        this.patterns.add(pattern);
        for (Listener listener : this.listeners) {
            listener.patternAdded(this, pattern);
        }
        return this;
    }

    public synchronized final LXChannel removePattern(LXPattern pattern) {
        if (this.patterns.remove(pattern)) {
            for (Listener listener : this.listeners) {
                listener.patternRemoved(this, pattern);
            }
        }
        return this;
    }

    private void _updatePatterns(LXPattern[] patterns) {
        this.patterns.clear();
        for (LXPattern pattern : patterns) {
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
        this.autoTransitionThreshold = autoTransitionThreshold;
        if (!this.autoTransitionEnabled.isOn()) {
            this.autoTransitionEnabled.setValue(true);
            if (this.transition == null) {
                this.transitionMillis = System.currentTimeMillis();
            }
        }
        return this;
    }

    public synchronized int getAutoTransitionThreshold() {
        return this.autoTransitionThreshold;
    }

    public synchronized boolean isAutoTransitionEnabled() {
        return this.autoTransitionEnabled.isOn();
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
        long runStart = System.nanoTime();

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
                    (this.lx.engine.nowMillis - this.transitionMillis > this.autoTransitionThreshold)) {
                goNext();
            }
        }

        int[] colors = (this.transition != null) ? this.transition.getColors() : getActivePattern().getColors();

        if (this.effects.size() > 0) {
            int[] array = this.buffer.getArray();
            for (int i = 0; i < colors.length; ++i) {
                array[i] = colors[i];
            }
            colors = array;
            for (LXEffect effect : this.effects) {
                ((LXLayerComponent)effect).setBuffer(this.buffer);
                effect.loop(deltaMs);
            }
        }

        this.colors = colors;

        this.timer.runNanos = System.nanoTime() - runStart;
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
