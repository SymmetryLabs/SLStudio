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
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.pattern.IteratorTestPattern;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.transition.LXTransition;

import java.lang.System;

import java.util.ArrayList;


class Engine {

    private final ArrayList<LXModulator> modulators = new ArrayList<LXModulator>();
    private final ArrayList<LXEffect> effects = new ArrayList<LXEffect>();
    
    private final HeronLX lx;
    
    private LXPattern[] patterns;
    private int activePatternIndex = 0;
    private int nextPatternIndex = 0;
    private long lastMillis;
    
    private LXTransition transition = null;
    private long transitionMillis = 0;
    private boolean autoTransitionEnabled = false;
    private int autoTransitionThreshold = 0;
    
    private final int[] colors;
        
    Engine(HeronLX lx) {
        this.lx = lx;
        this.patterns = new LXPattern[] {
            new IteratorTestPattern(lx)
        };
        this.colors = new int[lx.total];
        this.lastMillis = this.transitionMillis = System.currentTimeMillis();
    }
    
    final public LXModulator addModulator(LXModulator m) {
        this.modulators.add(m);
        return m;
    }
    
    final public void removeModulator(LXModulator m) {
        this.modulators.remove(m);
    }
    
    final public LXEffect addEffect(LXEffect fx) {
        this.effects.add(fx);
        return fx;
    }

    final public void removeEffect(LXEffect fx) {
        this.effects.remove(fx);
    }
    
    final public void setPatterns(LXPattern[] patterns) {
        this.getActivePattern().didResignActive();        
        this.patterns = patterns;
        this.activePatternIndex = this.nextPatternIndex = 0;
        this.getActivePattern().willBecomeActive();
    }
    
    final protected LXPattern getActivePattern() {
        return this.patterns[this.activePatternIndex];
    }

    final protected LXPattern getNextPattern() {
        return this.patterns[this.nextPatternIndex];
    }

    final protected LXTransition getActiveTransition() {
        return this.transition;
    }
    
    final public void goPrev() {
        if (this.transition != null) {
            return;
        }
        this.nextPatternIndex = this.activePatternIndex - 1;
        if (this.nextPatternIndex < 0) {
            this.nextPatternIndex = this.patterns.length - 1;
        }
        this.startTransition();
    }

    final public void goNext() {
        if (this.transition != null) {
            return;
        }
        this.nextPatternIndex = this.activePatternIndex;
        do {
            this.nextPatternIndex = (this.nextPatternIndex + 1) % this.patterns.length;
        } while (!this.getNextPattern().isEligible());
        this.startTransition();
    }
    
    final public void goIndex(int i) {
        if (this.transition != null) {
            return;
        }
        if (i < 0 || i >= this.patterns.length) {
            return;
        }
        this.nextPatternIndex = i;
        this.startTransition();
    }
        
    private void startTransition() {
        if (this.getActivePattern() == this.getNextPattern()) {
            return;
        }
        this.getNextPattern().willBecomeActive();
        this.transition = this.getNextPattern().getTransition();
        if (this.transition == null) {
            this.finishTransition();
        } else {
            this.getNextPattern().onTransitionStart();
            this.transitionMillis = System.currentTimeMillis();
        }
    }
    
    private void finishTransition() {
        this.getActivePattern().didResignActive();
        this.activePatternIndex = this.nextPatternIndex;
        this.transition = null;
        this.transitionMillis = System.currentTimeMillis();
    }
    
    protected void disableAutoTransition() {
        this.autoTransitionEnabled = false;
    }
    
    protected void enableAutoTransition(int autoTransitionThreshold) {
        this.autoTransitionEnabled = true;
        this.autoTransitionThreshold = autoTransitionThreshold;
        if (this.transition == null) {
            this.transitionMillis = System.currentTimeMillis();
        }
    }
    
    protected boolean isAutoTransitionEnabled() {
        return this.autoTransitionEnabled;
    }
    
    public void run() {
        // Compute elapsed time
        long nowMillis = System.currentTimeMillis();
        int deltaMs = (int) (nowMillis - this.lastMillis);
        this.lastMillis = nowMillis;
        
        // Run modulators
        for (LXModulator m : this.modulators) {
            m.run(deltaMs);
        }
        
        // Run tempo
        this.lx.tempo.run(deltaMs);
        
        // Run active pattern
        this.getActivePattern().go(deltaMs);
        
        // Run transition if applicable
        boolean useTransitionColors = false;
        if (this.transition != null) {
            int transitionMs = (int) (nowMillis - this.transitionMillis);
            if (transitionMs >= this.transition.getDuration()) {
                this.finishTransition();
            } else {
                useTransitionColors = true;
                this.getNextPattern().go(deltaMs);
                this.transition.blend(
                        this.getActivePattern().getColors(),
                        this.getNextPattern().getColors(),
                        (double) transitionMs / this.transition.getDuration()
                        );
            }
        } else {
            if (this.autoTransitionEnabled &&
                (nowMillis - this.transitionMillis > this.autoTransitionThreshold) &&
                !this.getActivePattern().isInInterval()) {
                this.goNext();
            }
        }
        
        // Apply effects
        int[] copyColors = useTransitionColors ? this.transition.getColors() : this.getActivePattern().getColors();
        for (int i = 0; i < this.colors.length; ++i) {
            this.colors[i] = copyColors[i];
        }
        for (LXEffect fx : this.effects) {
            fx.apply(this.colors, deltaMs);
        }
    }

    public int[] getColors() {
        return this.colors;
    }
}
