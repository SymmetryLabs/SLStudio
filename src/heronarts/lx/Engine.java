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
        public void blendTransitionDidChange(Deck deck, LXTransition blendTransition);
    }
    
    public abstract static class AbstractListener implements Listener {
        public void patternWillChange(Deck deck, LXPattern pattern, LXPattern nextPattern) {}
        public void patternDidChange(Deck deck, LXPattern pattern) {}
        public void blendTransitionDidChange(Deck deck, LXTransition blendTransition) {}
    }
    
    public class Deck {
        private LXPattern[] patterns;
        private int activePatternIndex = 0;
        private int nextPatternIndex = 0;
        
        private boolean autoTransitionEnabled = false;
        private int autoTransitionThreshold = 0;
        
        private LXTransition blendTransition = new DissolveTransition(lx);
        private final BasicParameter crossfader = new BasicParameter("BLND", 0);
        
        private LXTransition transition = null;
        private long transitionMillis = 0;

        private final List<Listener> listeners = new ArrayList<Listener>();
        
        Deck(LXPattern[] patterns) {
            this.patterns = patterns;
            this.transitionMillis = System.currentTimeMillis();
        }
        
        public final void addListener(Listener listener) {
            synchronized(listeners) {
                listeners.add(listener);
            }
        }
        
        public final void removeListener(Listener listener) {
            synchronized(listeners) {
                listeners.remove(listener);
            }
        }
        
        protected final void notifyPatternWillChange(LXPattern pattern, LXPattern nextPattern) {
            synchronized(listeners) {
                for (Listener listener : listeners) {
                    listener.patternWillChange(this, pattern, nextPattern);
                }
            }
        }
        
        protected final void notifyPatternDidChange(LXPattern pattern) {
            synchronized(listeners) {
                for (Listener listener : listeners) {
                    listener.patternDidChange(this, pattern);
                }
            }
        }

        protected final void notifyBlendTransitionDidChange(LXTransition transition) {
            synchronized(listeners) {
                for (Listener listener : listeners) {
                    listener.blendTransitionDidChange(this, transition);
                }
            }
        }
        
        public final BasicParameter getCrossfader() {
            return this.crossfader;
        }
        
        public synchronized final LXPattern[] getPatterns() {
            return this.patterns;
        }
        
        public synchronized final LXTransition getBlendTransition() {
            return this.blendTransition;
        }
        
        public synchronized final Deck setBlendTransition(LXTransition transition) {
            if (this.blendTransition != transition) {
                this.blendTransition = transition;
                notifyBlendTransitionDidChange(transition);
            }
            return this;
        }
        
        public synchronized final Deck setPatterns(LXPattern[] patterns) {
            this.getActivePattern().didResignActive();        
            this.patterns = patterns;
            this.activePatternIndex = this.nextPatternIndex = 0;
            this.getActivePattern().willBecomeActive();
            return this;
        }

        public synchronized final LXPattern getActivePattern() {
            return this.patterns[this.activePatternIndex];
        }

        public synchronized final LXPattern getNextPattern() {
            return this.patterns[this.nextPatternIndex];
        }
        
        protected synchronized final LXTransition getActiveTransition() {
            return this.transition;
        }

        public synchronized final void goPrev() {
            if (this.transition != null) {
                return;
            }
            this.nextPatternIndex = this.activePatternIndex - 1;
            if (this.nextPatternIndex < 0) {
                this.nextPatternIndex = this.patterns.length - 1;
            }
            this.startTransition();
        }
        
        public synchronized final void goNext() {
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

        public synchronized final void goPattern(LXPattern pattern) {
            for (int i = 0; i < this.patterns.length; ++i) {
                if (this.patterns[i] == pattern) {
                    this.goIndex(i);
                    return;
                }
            }
        }    
        
        public synchronized final void goIndex(int i) {
            if (this.transition != null) {
                return;
            }
            if (i < 0 || i >= this.patterns.length) {
                return;
            }
            this.nextPatternIndex = i;
            this.startTransition();
        }
        
        protected synchronized void disableAutoTransition() {
            this.autoTransitionEnabled = false;
        }
        
        protected synchronized void enableAutoTransition(int autoTransitionThreshold) {
            this.autoTransitionEnabled = true;
            this.autoTransitionThreshold = autoTransitionThreshold;
            if (this.transition == null) {
                this.transitionMillis = System.currentTimeMillis(); 
            }
        }
        
        protected synchronized boolean isAutoTransitionEnabled() {
            return this.autoTransitionEnabled;
        }

            
        private synchronized void startTransition() {
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
        
        private synchronized void finishTransition() {
            getActivePattern().didResignActive();        
            this.activePatternIndex = this.nextPatternIndex;
            if (this.transition != null) {
                getNextPattern().onTransitionEnd();
            }
            this.transition = null;
            this.transitionMillis = System.currentTimeMillis();
            notifyPatternDidChange(getActivePattern());        
        }
        
        private synchronized void run(long nowMillis, double deltaMs) {
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
        
        public synchronized int[] getColors() {
            return (this.transition != null) ? this.transition.getColors() : this.getActivePattern().getColors();
        }
    }

    private final HeronLX lx;
    
    private final List<Deck> decks = new ArrayList<Deck>();
    private final List<LXModulator> modulators = new ArrayList<LXModulator>();
    private final List<LXEffect> effects = new ArrayList<LXEffect>();        
    
    class DoubleBuffer {
        
        final int[] buffer1;
        final int[] buffer2;
        int[] render;
        int[] copy;
        
        DoubleBuffer() {
            render = buffer1 = new int[lx.total];
            copy = buffer2 = new int[lx.total];
            for (int i = 0; i < lx.total; ++i) {
                buffer1[i] = buffer2[i] = 0xff000000;
            }
        }
        
        void flip() {
            int[] tmp = render;
            render = copy;
            copy = tmp;
        }
    }
    
    private final DoubleBuffer buffer;
    private final int[] black;
    
    private long lastMillis;
        
    private boolean isThreaded = false;
    private Thread engineThread = null;
    
    private double speed = 1;
    private boolean paused = false;
    
    Engine(HeronLX lx) {
        this.lx = lx;
        this.black = new int[lx.total];
        for (int i = 0; i < black.length; ++i) {
            this.black[i] = 0xff000000;
        }
        this.buffer = new DoubleBuffer();
        Deck deck = new Deck(new LXPattern[] {
            new IteratorTestPattern(lx)
        });
        deck.crossfader.setValue(1);
        this.decks.add(deck);
        this.lastMillis = System.currentTimeMillis();
    }
    
    /**
     * Whether the engine is threaded. Should only be called from Processing
     * animation thread.
     * 
     * @return Whether the engine is threaded
     */
    public synchronized boolean isThreaded() {
        return this.isThreaded;
    }
    
    /**
     * Sets the engine to threaded or non-threaded mode. Should only be called
     * from the Processing animation thread.
     * 
     * @param threaded Whether engine should run on its own thread
     */
    public synchronized void setThreaded(boolean threaded) {
        if (threaded == this.isThreaded) {
            return;
        }
        this.isThreaded = threaded;
        if (!threaded) {
            engineThread.interrupt();
            try {
                engineThread.join();
            } catch (InterruptedException ix) {}
            engineThread = null;
        } else {
            // Copy the current frame to avoid a black frame
            for (int i = 0; i < buffer.render.length; ++i) {
                buffer.copy[i] = buffer.render[i];
            }
            engineThread = new Thread("HeronLX Engine Thread") {
                public void run() {
                    System.out.println("HeronLX Engine Thread started.");
                    final int minMillisPerFrame = 15;
                    while (!isInterrupted()) {
                        long frameStart = System.currentTimeMillis();
                        Engine.this.run();
                        synchronized(buffer) {
                            buffer.flip();
                        }
                        long frameMillis = System.currentTimeMillis() - frameStart;
                        if (frameMillis < minMillisPerFrame) {
                            try {
                                sleep(minMillisPerFrame - frameMillis);
                            } catch (InterruptedException ix) {
                                break;
                            }
                        }
                    }
                    System.out.println("HeronLX Engine Thread finished.");
                }
            };
            engineThread.start();
        }
    }

    synchronized void setSpeed(double speed) {
        this.speed = speed;
    }
    
    /**
     * Pause the engine from running
     * 
     * @param paused Whether to pause the engine to pause
     */
    public synchronized void setPaused(boolean paused) {
        this.paused = paused;
    }
    
    public synchronized boolean isPaused() {
        return this.paused;
    }
    
    public synchronized LXModulator addModulator(LXModulator m) {
        this.modulators.add(m);
        return m;
    }
    
    public synchronized void removeModulator(LXModulator m) {
        this.modulators.remove(m);
    }
    
    public synchronized List<LXEffect> getEffects() {
        return this.effects;
    }
    
    public synchronized LXEffect addEffect(LXEffect fx) {
        this.effects.add(fx);
        return fx;
    }

    public synchronized void removeEffect(LXEffect fx) {
        this.effects.remove(fx);
    }
    
    public synchronized List<Deck> getDecks() {
        return this.decks;
    }
    
    public synchronized Deck getDefaultDeck() {
        return this.decks.get(0);
    }
    
    public synchronized Deck getDeck(int deckIndex) {
        return this.decks.get(deckIndex);
    }
    
    public synchronized void addDeck(LXPattern[] patterns) {
        this.decks.add(new Deck(patterns));
    }
        
    public void setPatterns(LXPattern[] patterns) {
        this.getDefaultDeck().setPatterns(patterns);
    }
        
    public LXPattern[] getPatterns() {
        return this.getDefaultDeck().getPatterns();
    }
    
    protected LXPattern getActivePattern() {
        return this.getDefaultDeck().getActivePattern();
    }
    
    protected LXPattern getNextPattern() {
        return this.getDefaultDeck().getNextPattern();
    }

    protected LXTransition getActiveTransition() {
        return this.getDefaultDeck().getActiveTransition();
    }
    
    public void goPrev() {
        this.getDefaultDeck().goPrev();
    }

    public final void goNext() {
        this.getDefaultDeck().goNext();
    }
    
    public void goPattern(LXPattern pattern) {
        this.getDefaultDeck().goPattern(pattern);
    }
    
    public void goIndex(int index) {
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

    public synchronized void run() {
        // Compute elapsed time
        long nowMillis = System.currentTimeMillis();
        double deltaMs = nowMillis - this.lastMillis;
        this.lastMillis = nowMillis;
        
        if (this.paused) {
            return;
        }
        
        // Mutate by speed
        deltaMs *= this.speed;
        
        // Run modulators
        for (LXModulator m : this.modulators) {
            m.run(deltaMs);
        }
        
        // Run tempo
        this.lx.tempo.run(deltaMs);
        
        // Run and blend all of our decks
        int[] bufferColors = this.black;
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
        
        // Copy colors into our own rendering buffer
        for (int i = 0; i < bufferColors.length; ++i) {
            this.buffer.render[i] = bufferColors[i];
        }
                
        // Apply effects
        for (LXEffect fx : this.effects) {
            fx.apply(this.buffer.render, deltaMs);
        }
    }
    
    void copyColors(int[] copy) {
        synchronized(this.buffer) {
            for (int i = 0; i < this.buffer.copy.length; ++i) {
                copy[i] = this.buffer.copy[i]; 
            }
        }
    }
    
    int[] renderColors() {
        return this.buffer.render;
    }
}
