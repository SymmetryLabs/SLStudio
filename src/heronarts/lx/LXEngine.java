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
import heronarts.lx.output.LXOutput;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.pattern.IteratorTestPattern;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.transition.LXTransition;

import java.lang.System;

import java.util.ArrayList;
import java.util.List;

/**
 * The engine is the core class that runs the internal animations. An
 * engine is comprised of top-level modulators, then a number of decks,
 * each of which has a set of patterns that it may transition between.
 * These decks are blended together, and effects are then applied.
 * 
 * <pre>
 *   -----------------------------
 *  | Engine                      |
 *  |  -------------------------  |
 *  | | Modulators              | |
 *  |  -------------------------  |
 *  |  --------    --------       |
 *  | | Deck 0 |  | Deck 1 |  ... |
 *  |  --------    --------       |
 *  |  -------------------------  |
 *  | | Effects                 | |
 *  |  -------------------------  |
 *   -----------------------------
 * </pre>
 *  
 *  The result of all this generates a display buffer of node values.
 */
public class LXEngine {
        
    private final LX lx;
    
    private final List<LXModulator> modulators = new ArrayList<LXModulator>();
    private final List<LXDeck> decks = new ArrayList<LXDeck>();
    private final List<LXEffect> effects = new ArrayList<LXEffect>();
    private final List<LXOutput> outputs = new ArrayList<LXOutput>();
    
    public final BasicParameter framesPerSecond = new BasicParameter("FPS", 60, 0, 300); 
    
    float frameRate = 0;
    
    public class Timer {
        public long runNanos = 0;
        public long deckNanos = 0;
        public long copyNanos = 0;
        public long fxNanos = 0;
        public long outputNanos = 0;
    }
    
    public final Timer timer = new Timer();
    
    /**
     * Utility class for a threaded engine to do buffer-flipping. One buffer
     * is actively being worked on, while another copy is held for shuffling
     * off to another thread.
     */
    private class DoubleBuffer {
        
        // Concrete buffer instances, these are real memory
        private final int[] buffer1;        
        private final int[] buffer2;
        
        // References, these flip between pointing to buffer1 and buffer 2
        private int[] render;
        private int[] copy;
        
        private DoubleBuffer() {
            this.render = this.buffer1 = new int[lx.total];
            this.copy = this.buffer2 = new int[lx.total];
            for (int i = 0; i < lx.total; ++i) {
                this.buffer1[i] = this.buffer2[i] = 0xff000000;
            }
        }
        
        private void flip() {
            int[] tmp = this.render;
            this.render = this.copy;
            this.copy = tmp;
        }
    }
    
    private final DoubleBuffer buffer;
    private final int[] black;
    
    private long lastMillis;
        
    private boolean isThreaded = false;
    private Thread engineThread = null;
    
    private double speed = 1;
    private boolean paused = false;
    
    LXEngine(LX lx) {
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
     * Gets the active frame rate of the engine when in threaded mode
     * 
     * @return How many FPS the engine is running
     */
    public float frameRate() {
        return this.isThreaded ? this.frameRate : 0;
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
    public synchronized LXEngine setThreaded(boolean threaded) {
        if (threaded == this.isThreaded) {
            return this;
        }
        this.isThreaded = threaded;
        if (!threaded) {
            this.engineThread.interrupt();
            try {
                this.engineThread.join();
            } catch (InterruptedException ix) {}
            this.engineThread = null;
        } else {
            // Copy the current frame to avoid a black frame
            for (int i = 0; i < this.buffer.render.length; ++i) {
                this.buffer.copy[i] = this.buffer.render[i];
            }
            this.engineThread = new Thread("LX Engine Thread") {
                public void run() {
                    System.out.println("LX Engine Thread started.");
                    while (!isInterrupted()) {
                        long frameStart = System.currentTimeMillis();
                        LXEngine.this.run();
                        synchronized(buffer) {
                            buffer.flip();
                        }
                        long frameMillis = System.currentTimeMillis() - frameStart;
                        frameRate = 1000.f / frameMillis;
                        float targetFPS = framesPerSecond.getValuef();
                        if (targetFPS > 0) {
                            long minMillisPerFrame = (long) (1000. / targetFPS);
                            if (frameMillis < minMillisPerFrame) {
                                frameRate = targetFPS;
                                try {
                                    sleep(minMillisPerFrame - frameMillis);
                                } catch (InterruptedException ix) {
                                    break;
                                }
                            }
                        }
                    }
                    System.out.println("LX Engine Thread finished.");
                }
            };
            this.engineThread.start();
        }
        return this;
    }

    public synchronized LXEngine setSpeed(double speed) {
        this.speed = speed;
        return this;
    }
    
    /**
     * Pause the engine from running
     * 
     * @param paused Whether to pause the engine to pause
     */
    public synchronized LXEngine setPaused(boolean paused) {
        this.paused = paused;
        return this;
    }
    
    public synchronized boolean isPaused() {
        return this.paused;
    }
    
    public synchronized LXEngine addModulator(LXModulator m) {
        this.modulators.add(m);
        return this;
    }
    
    public synchronized LXEngine removeModulator(LXModulator m) {
        this.modulators.remove(m);
        return this;
    }
    
    public synchronized List<LXEffect> getEffects() {
        return this.effects;
    }
    
    public synchronized LXEngine addEffect(LXEffect fx) {
        this.effects.add(fx);
        return this;
    }

    public synchronized LXEngine removeEffect(LXEffect fx) {
        this.effects.remove(fx);
        return this;
    }
    
    /**
     * Adds an output driver
     * 
     * @param output
     * @return this
     */
    public synchronized LXEngine addOutput(LXOutput output) {
        this.outputs.add(output);
        return this;
    }
    
    /**
     * Removes an output driver
     * 
     * @param output
     * @return this
     */
    public synchronized LXEngine removeOutput(LXOutput output) {
        this.outputs.remove(output);
        return this;
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
        long runStart = System.nanoTime();
        
        // Compute elapsed time
        long nowMillis = System.currentTimeMillis();
        double deltaMs = nowMillis - this.lastMillis;
        this.lastMillis = nowMillis;
        
        if (this.paused) {
            this.timer.deckNanos = 0;
            this.timer.copyNanos = 0;
            this.timer.fxNanos = 0;
            this.timer.runNanos = System.nanoTime() - runStart;
            return;
        }
        
        // Run tempo, always using real-time
        this.lx.tempo.run(deltaMs);
        
        // Mutate by speed for everything else
        deltaMs *= this.speed;
        
        // Run modulators
        for (LXModulator m : this.modulators) {
            m.run(deltaMs);
        }
        
        // Run and blend all of our decks
        long deckStart = System.nanoTime();
        int[] bufferColors = this.black;
        for (LXDeck deck : this.decks) {
            deck.run(nowMillis, deltaMs);
            deck.getFaderTransition().timer.blendNanos = 0;
            
            // This optimization assumed that all transitions do
            // nothing at 0 and completely take over at 1. That's
            // not always the case. Leaving this here for reference.
            
//            if (deck.getFader().getValue() == 0) {
//                // No blending on this deck, leave colors as they were
//            } else if (deck.getFader().getValue() >= 1) {
//                // Fully faded in, just use this deck
//                bufferColors = deck.getColors();
//            } else {

            // Apply the fader to this deck
            deck.faderTransition.blend(bufferColors, deck.getColors(), deck.fader.getValue());
            bufferColors = deck.faderTransition.getColors();
                
        }
        this.timer.deckNanos = System.nanoTime() - deckStart;
        
        // Copy colors into our own rendering buffer
        long copyStart = System.nanoTime();
        for (int i = 0; i < bufferColors.length; ++i) {
            this.buffer.render[i] = bufferColors[i];
        }
        this.timer.copyNanos = System.nanoTime() - copyStart;
                
        // Apply effects in our rendering buffer
        long fxStart = System.nanoTime();
        for (LXEffect fx : this.effects) {
            fx.run(deltaMs, this.buffer.render);
        }
        this.timer.fxNanos = System.nanoTime() - fxStart;

        // Send to outputs
        long outputStart = System.nanoTime(); 
        for (LXOutput output : this.outputs) {
            output.send(this.buffer.render);
        }
        this.timer.outputNanos = System.nanoTime() - outputStart;

        this.timer.runNanos = System.nanoTime() - runStart;
    }
    
    /**
     * This should be used when in threaded mode. It synchronizes on the
     * double-buffer and duplicates the internal copy buffer into the provided
     * buffer.
     * 
     * @param copy Buffer to copy into
     */
    void copyBuffer(int[] copy) {
        synchronized(this.buffer) {
            for (int i = 0; i < this.buffer.copy.length; ++i) {
                copy[i] = this.buffer.copy[i]; 
            }
        }
    }
    
    /**
     * This is used when not in threaded mode. It provides direct access to
     * the engine's render buffer.
     * 
     * @return The internal render buffer
     */
    int[] renderBuffer() {
        return this.buffer.render;
    }
}
