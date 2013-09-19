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

import heronarts.lx.control.BasicParameter;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.pattern.IteratorTestPattern;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.transition.DissolveTransition;
import heronarts.lx.transition.LXTransition;

import java.lang.System;

import java.util.ArrayList;
import java.util.List;

public class Engine {
    
    /**
     * Listener interface for objects which want to be notified when the
     * internal engine state is modified
     */
    public interface Listener {
        public void patternWillChange(Deck deck, LXPattern pattern, LXPattern nextPattern);
        public void patternDidChange(Deck deck, LXPattern pattern);
    }        
    
    public class Deck {
        private LXPattern[] patterns;
        private int activePatternIndex = 0;
        private int nextPatternIndex = 0;
        
        private boolean autoTransitionEnabled = false;
        private int autoTransitionThreshold = 0;
        
        private LXTransition blendTransition = new DissolveTransition(lx);
        private BasicParameter crossfader = new BasicParameter("BLND", 0);
        
        private LXTransition transition = null;
        private long transitionMillis = 0;

        private final List<Listener> listeners = new ArrayList<Listener>();
        
        Deck(LXPattern[] patterns) {
            this.patterns = patterns;
            this.transitionMillis = System.currentTimeMillis();
        }
        
        public final void addListener(Listener listener) {
            listeners.add(listener);
        }
        
        public final void removeListener(Listener listener) {
            listeners.remove(listener);
        }
        
        protected final void notifyPatternWillChange(LXPattern pattern, LXPattern nextPattern) {
            for (Listener listener : listeners) {
                listener.patternWillChange(this, pattern, nextPattern);
            }    
        }
        
        protected final void notifyPatternDidChange(LXPattern pattern) {
            for (Listener listener : listeners) {
                listener.patternDidChange(this, pattern);
            }
        }
        
        final public BasicParameter getCrossfader() {
            return this.crossfader;
        }
        
        final public LXPattern[] getPatterns() {
            return this.patterns;
        }
        
        final public LXTransition getBlendTransition() {
            return this.blendTransition;
        }
        
        final public Deck setBlendTransition(LXTransition transition) {
            this.blendTransition = transition;
            return this;
        }
        
        final public Deck setPatterns(LXPattern[] patterns) {
            this.getActivePattern().didResignActive();        
            this.patterns = patterns;
            this.activePatternIndex = this.nextPatternIndex = 0;
            this.getActivePattern().willBecomeActive();
            return this;
        }

        final public LXPattern getActivePattern() {
            return this.patterns[this.activePatternIndex];
        }

        final public LXPattern getNextPattern() {
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
            } while ((this.nextPatternIndex != this.activePatternIndex) &&
                     !this.getNextPattern().isEligible());
            if (this.nextPatternIndex != this.activePatternIndex) {
                this.startTransition();
            }
        }

        final public void goPattern(LXPattern pattern) {
            for (int i = 0; i < this.patterns.length; ++i) {
                if (this.patterns[i] == pattern) {
                    this.goIndex(i);
                    return;
                }
            }
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

            
        private void startTransition() {
            if (getActivePattern() == getNextPattern()) {
                return;
            }
            getNextPattern().willBecomeActive();
            notifyPatternWillChange(getActivePattern(), getNextPattern());
            this.transition = getNextPattern().getTransition();
            if (this.transition == null) {
                finishTransition();
            } else {
                getNextPattern().onTransitionStart();
                this.transitionMillis = System.currentTimeMillis();
            }
        }
        
        private void finishTransition() {
            getActivePattern().didResignActive();        
            this.activePatternIndex = this.nextPatternIndex;
            this.transition = null;
            this.transitionMillis = System.currentTimeMillis();
            notifyPatternDidChange(getActivePattern());        
        }
        
        private void run(long nowMillis, int deltaMs) {
            // Run active pattern
            this.getActivePattern().go(deltaMs);
            
            // Run transition if applicable
            if (this.transition != null) {
                int transitionMs = (int) (nowMillis - this.transitionMillis);
                if (transitionMs >= this.transition.getDuration()) {
                    this.finishTransition();
                } else {
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
        }
        
        public int[] getColors() {
            return (this.transition != null) ? this.transition.getColors() : this.getActivePattern().getColors();
        }
    }

    private final HeronLX lx;
    
    private final List<Deck> decks = new ArrayList<Deck>();
    private final List<LXModulator> modulators = new ArrayList<LXModulator>();
    private final List<LXEffect> effects = new ArrayList<LXEffect>();        
    
    private final int[] black;
    private final int[] colors;
    private long lastMillis;
    private double speed = 1;
    private boolean paused = false;
    
    Engine(HeronLX lx) {
        this.lx = lx;
        this.black = new int[lx.total];
        this.colors = new int[lx.total];
        for (int i = 0; i < lx.total; ++i) {
            this.black[i] = this.colors[i] = 0;
        }
        Deck deck = new Deck(new LXPattern[] {
            new IteratorTestPattern(lx)
        });
        deck.crossfader.setValue(1);
        this.decks.add(deck);
        this.lastMillis = System.currentTimeMillis();
    }

    void setSpeed(double speed) {
        this.speed = speed;
    }
    
    /**
     * Pause the engine from running
     * 
     * @param paused Whether to pause the engine to pause
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
    
    public boolean isPaused() {
        return this.paused;
    }
    
    final public LXModulator addModulator(LXModulator m) {
        this.modulators.add(m);
        return m;
    }
    
    final public void removeModulator(LXModulator m) {
        this.modulators.remove(m);
    }
    
    final public List<LXEffect> getEffects() {
        return this.effects;
    }
    
    final public LXEffect addEffect(LXEffect fx) {
        this.effects.add(fx);
        return fx;
    }

    final public void removeEffect(LXEffect fx) {
        this.effects.remove(fx);
    }
    
    final public List<Deck> getDecks() {
        return this.decks;
    }
    
    final public Deck getDefaultDeck() {
        return this.decks.get(0);
    }
    
    final public Deck getDeck(int deckIndex) {
        return this.decks.get(deckIndex);
    }
    
    final public void addDeck(LXPattern[] patterns) {
        this.decks.add(new Deck(patterns));
    }
        
    final public void setPatterns(LXPattern[] patterns) {
        this.getDefaultDeck().setPatterns(patterns);
    }
        
    final public LXPattern[] getPatterns() {
        return this.getDefaultDeck().getPatterns();
    }
    
    final protected LXPattern getActivePattern() {
        return this.getDefaultDeck().getActivePattern();
    }
    
    final protected LXPattern getNextPattern() {
        return this.getDefaultDeck().getNextPattern();
    }

    final protected LXTransition getActiveTransition() {
        return this.getDefaultDeck().getActiveTransition();
    }
    
    final public void goPrev() {
        this.getDefaultDeck().goPrev();
    }

    final public void goNext() {
        this.getDefaultDeck().goNext();
    }
    
    final public void goPattern(LXPattern pattern) {
        this.getDefaultDeck().goPattern(pattern);
    }
    
    final public void goIndex(int index) {
        this.getDefaultDeck().goIndex(index);
    }
    
    protected void disableAutoTransition() {
        getDefaultDeck().disableAutoTransition();
    }

    protected void enableAutoTransition(int autoTransitionThreshold) {
        getDefaultDeck().enableAutoTransition(autoTransitionThreshold);
    }
        
    protected boolean isAutoTransitionEnabled() {
        return getDefaultDeck().isAutoTransitionEnabled();
    }

    public void run() {
        // Compute elapsed time
        long nowMillis = System.currentTimeMillis();
        int deltaMs = (int) (nowMillis - this.lastMillis);
        this.lastMillis = nowMillis;
        
        if (this.paused) {
            return;
        }
        
        // Mutate by speed
        deltaMs = (int) (deltaMs * this.speed);
        
        // Run modulators
        for (LXModulator m : this.modulators) {
            m.run(deltaMs);
        }
        
        // Run tempo
        this.lx.tempo.run(deltaMs);
        
        // Run and blend all of our decks
        int[] bufferColors = black;
        for (Deck deck : this.decks) {
            deck.run(nowMillis, deltaMs);
            if (deck.crossfader.getValue() >= 1) {
                // Fully crossfaded, just use this deck
                bufferColors = deck.getColors();
            } else {
                // Apply the crossfader to this deck
                deck.blendTransition.blend(bufferColors, deck.getColors(), deck.crossfader.getValue());
                bufferColors = deck.blendTransition.getColors();
            }
        }
        
        // Copy colors into our own memory
        for (int i = 0; i < this.colors.length; ++i) {
            this.colors[i] = bufferColors[i];
        }
                
        // Apply effects
        for (LXEffect fx : this.effects) {
            fx.apply(this.colors, deltaMs);
        }
    }

    public int[] getColors() {
        return this.colors;
    }
}
