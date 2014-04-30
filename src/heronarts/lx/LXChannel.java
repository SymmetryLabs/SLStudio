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
public class LXChannel {

    /**
     * Listener interface for objects which want to be notified when the internal
     * channel state is modified.
     */
    public interface Listener {

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

    public final BooleanParameter enabled = new BooleanParameter("ON", true);

    public final BooleanParameter midiEnabled = new BooleanParameter("MIDI", false);

    private final List<LXPattern> patterns = new ArrayList<LXPattern>();
    private final List<LXPattern> unmodifiablePatterns = Collections.unmodifiableList(patterns);

    private int activePatternIndex = 0;
    private int nextPatternIndex = 0;

    private boolean autoTransitionEnabled = false;
    private int autoTransitionThreshold = 0;

    private LXTransition faderTransition = null;
    private final BasicParameter fader = new BasicParameter("FADER", 0);

    private LXTransition transition = null;
    private long transitionMillis = 0;

    private final List<Listener> listeners = new ArrayList<Listener>();

    LXChannel(LX lx, int index, LXPattern[] patterns) {
        this.index = index;
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

    final LXChannel setIndex(int index) {
        this.index = index;
        return this;
    }

    public final int getIndex() {
        return this.index;
    }

    public final BasicParameter getFader() {
        return this.fader;
    }

    public synchronized final List<LXPattern> getPatterns() {
        return this.unmodifiablePatterns;
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

    protected synchronized LXChannel disableAutoTransition() {
        this.autoTransitionEnabled = false;
        return this;
    }

    protected synchronized LXChannel enableAutoTransition(int autoTransitionThreshold) {
        this.autoTransitionEnabled = true;
        this.autoTransitionThreshold = autoTransitionThreshold;
        if (this.transition == null) {
            this.transitionMillis = System.currentTimeMillis();
        }
        return this;
    }

    protected synchronized boolean isAutoTransitionEnabled() {
        return this.autoTransitionEnabled;
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
            this.transition.blend(activePattern.getColors(), nextPattern.getColors(), 0, 0);
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

    synchronized void run(long nowMillis, double deltaMs) {
        long runStart = System.nanoTime();

        // Run active pattern
        getActivePattern().go(deltaMs);

        // Run transition if applicable
        if (this.transition != null) {
            int transitionMs = (int) (nowMillis - this.transitionMillis);
            if (transitionMs >= this.transition.getDuration()) {
                finishTransition();
            } else {
                getNextPattern().go(deltaMs);
                this.transition.blend(getActivePattern().getColors(), getNextPattern()
                        .getColors(),
                        transitionMs / this.transition.getDuration(), deltaMs);
            }
        } else {
            if (this.autoTransitionEnabled
                    && (nowMillis - this.transitionMillis > this.autoTransitionThreshold)) {
                goNext();
            }
        }

        this.timer.runNanos = System.nanoTime() - runStart;
    }

    public synchronized void copyBuffer(int[] buffer) {
        int[] colors = getColors();
        for (int i = 0; i < colors.length; ++i) {
            buffer[i] = colors[i];
        }
    }

    public synchronized int[] getColors() {
        return (this.transition != null) ? this.transition.getColors() : this
                .getActivePattern().getColors();
    }
}
