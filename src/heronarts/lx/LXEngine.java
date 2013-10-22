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
import java.util.List;

public class LXEngine {
        
    private final HeronLX lx;
    
    private final List<LXDeck> decks = new ArrayList<LXDeck>();
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
    
    LXEngine(HeronLX lx) {
        this.lx = lx;
        this.black = new int[lx.total];
        for (int i = 0; i < black.length; ++i) {
            this.black[i] = 0xff000000;
        }
        this.buffer = new DoubleBuffer();
        LXDeck deck = new LXDeck(lx, 0, new LXPattern[] {
            new IteratorTestPattern(lx)
        });
        deck.getFader().setValue(1);
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
                        LXEngine.this.run();
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
    
    public synchronized List<LXDeck> getDecks() {
        return this.decks;
    }
    
    public synchronized LXDeck getDefaultDeck() {
        return this.decks.get(0);
    }
    
    public synchronized LXDeck getDeck(int deckIndex) {
        return this.decks.get(deckIndex);
    }
    
    public synchronized void addDeck(LXPattern[] patterns) {
        this.decks.add(new LXDeck(lx, this.decks.size(), patterns));
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
        for (LXDeck deck : this.decks) {
            deck.run(nowMillis, deltaMs);
            if (deck.getFader().getValue() >= 1) {
                // Fully crossfaded, just use this deck
                bufferColors = deck.getColors();
            } else {
                // Apply the crossfader to this deck
                deck.faderTransition.blend(bufferColors, deck.getColors(), deck.fader.getValue());
                bufferColors = deck.faderTransition.getColors();
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
