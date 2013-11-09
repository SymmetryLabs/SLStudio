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

package heronarts.lx.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.LXDeck;
import heronarts.lx.LXLayer;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.transition.LXTransition;

import java.util.ArrayList;
import java.util.List;

import processing.core.PConstants;

public abstract class LXPattern extends LXComponent {

    protected final LX lx;
    private LXDeck deck = null;
    
    protected final int[] colors;
    protected LXTransition transition = null;
    protected int intervalBegin = -1;
    protected int intervalEnd = -1;
    private boolean eligible = true;
    private final Timer timer = new Timer();
    
    public class Timer {
        public long goNanos = 0;
    }
    
    protected LXPattern(LX lx) {
        this.lx = lx;
        this.colors = new int[lx.total];
    }
    
    public LXDeck getDeck() {
        return this.deck;
    }
    
    public LXPattern setDeck(LXDeck deck) {
        if (this.deck != null) {
            throw new RuntimeException("LXPattern instance can only be added to LXDeck once.");
        }
        this.deck = deck;
        return this;
    }
    
    public LXPattern runDuringInterval(int begin, int end) {
        this.intervalBegin = begin;
        this.intervalEnd = end;
        return this;
    }
    
    public final boolean hasInterval() {
        return (this.intervalBegin >= 0) && (this.intervalEnd >= 0);
    }
    
    public final boolean isInInterval() {
        if (!this.hasInterval()) {
            return false;
        }
        int now = this.lx.applet.hour()*60 + this.lx.applet.minute();
        System.out.println("now is : " + now + " int: " + this.intervalBegin + " " + this.intervalEnd);
        if (this.intervalBegin < this.intervalEnd) {
            // Normal daytime interval
            return (now >= this.intervalBegin) && (now < this.intervalEnd);
        } else {
            // Wrapping around midnight
            return (now >= this.intervalBegin) || (now < this.intervalEnd);
        }
    }
    
    public final LXPattern setEligible(boolean eligible) {
        this.eligible = eligible;
        return this;
    }
    
    public final LXPattern toggleEligible() {
        this.setEligible(!this.eligible);
        return this;
    }
    
    public final boolean isEligible() {
        return
            this.eligible &&
            (!this.hasInterval() || this.isInInterval());
    }
    
    public final LXPattern setTransition(LXTransition transition) {
        this.transition = transition;
        return this;
    }
    
    public final LXTransition getTransition() {
        return transition;
    }
    
    protected final int addColor(int i, int c) {
        return this.colors[i] = this.lx.applet.blendColor(this.colors[i], c, PConstants.ADD);
    }
    
    protected final int setColor(int i, int c) {
        return this.colors[i] = c;
    }
    
    protected final int addColor(int x, int y, int c) {
        return this.addColor(x + y * this.lx.width, c);
    }
    
    protected final int setColor(int x, int y, int c) {
        return this.colors[x + y * this.lx.width] = c;
    }

    protected final int getColor(int x, int y) {
        return this.colors[x + y * this.lx.width];
    }
        
    protected final void setColors(int c) {
        for (int i = 0; i < colors.length; ++i) {
            this.colors[i] = c;
        }
    }

    protected final void clearColors() {
        this.setColors(0);
    }

    public final int[] getColors() {
        return this.colors;
    }

    public final void go(double deltaMs) {
        long goStart = System.nanoTime();
        for (LXModulator m : this.modulators) {
            m.run(deltaMs);
        }
        this.run(deltaMs);
        for (LXLayer layer : this.layers) {
            for (LXModulator m : layer.getModulators()) {
                m.run(deltaMs);
            }
            layer.run(deltaMs, this.colors);
        }
        this.timer.goNanos = System.nanoTime() - goStart;
    }
    
    /**
     * Main pattern loop function. Invoked in a render loop.
     * 
     * @param deltaMs Number of milliseconds elapsed since last invocation
     */
    abstract protected void run(double deltaMs);

    public final void willBecomeActive() {
        this.onActive();
    }
    
    public final void didResignActive() {
        this.onInactive();
    }
    
    /* abstract */ protected void onActive() {}
    
    /* abstract */ protected void onInactive() {}
    
    /* abstract */ public void onTransitionStart() {}
    
    /* abstract */ public void onTransitionEnd() {}

}

