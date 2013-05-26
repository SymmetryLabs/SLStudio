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

import heronarts.lx.HeronLX;
import heronarts.lx.control.LXParameterized;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.transition.LXTransition;

import java.util.ArrayList;


import processing.core.PConstants;

public abstract class LXPattern extends LXParameterized {

    final protected HeronLX lx;
    final protected int[] colors;
    final private ArrayList<LXModulator> modulators;
    protected LXTransition transition;
    protected int intervalBegin = -1;
    protected int intervalEnd = -1;
    private boolean eligible = true;
    
    protected LXPattern(HeronLX lx) {
        this.lx = lx;
        this.colors = new int[lx.total];
        this.modulators = new ArrayList<LXModulator>();
        this.transition = null;
    }
    
    final public LXPattern runDuringInterval(int begin, int end) {
        this.intervalBegin = begin;
        this.intervalEnd = end;
        return this;
    }
    
    final public boolean hasInterval() {
        return (this.intervalBegin >= 0) && (this.intervalEnd >= 0);
    }
    
    final public boolean isInInterval() {
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
    
    final public LXPattern setEligible(boolean eligible) {
        this.eligible = eligible;
        return this;
    }
    
    final public LXPattern toggleEligible() {
        this.setEligible(!this.eligible);
        return this;
    }
    
    final public boolean isEligible() {
        return
            this.eligible &&
            (!this.hasInterval() || this.isInInterval());
    }
    
    final public LXPattern setTransition(LXTransition transition) {
        this.transition = transition;
        return this;
    }
    
    final public LXTransition getTransition() {
        return transition;
    }
    
    final protected int addColor(int i, int c) {
        return this.colors[i] = this.lx.applet.blendColor(this.colors[i], c, PConstants.ADD);
    }
    
    final protected int setColor(int i, int c) {
        return this.colors[i] = c;
    }
    
    final protected int addColor(int x, int y, int c) {
        return this.addColor(x + y * this.lx.width, c);
    }
    
    final protected int setColor(int x, int y, int c) {
        return this.colors[x + y * this.lx.width] = c;
    }

    final protected int getColor(int x, int y) {
        return this.colors[x + y * this.lx.width];
    }


    final protected LXModulator addModulator(LXModulator m) {
        this.modulators.add(m);
        return m;
    }

    final protected void setColors(int c) {
        for (int i = 0; i < colors.length; ++i) {
            this.colors[i] = c;
        }
    }

    final protected void clearColors() {
        this.setColors(0);
    }

    final public int[] getColors() {
        return this.colors;
    }

    final public void go(int deltaMs) {
        for (LXModulator m : this.modulators) {
            m.run(deltaMs);
        }
        this.run(deltaMs);
    }
    
    /**
     * Main pattern loop function. Invoked in a render loop.
     * 
     * @param deltaMs Number of milliseconds elapsed since last invocation
     */
    abstract protected void run(int deltaMs);

    final public void willBecomeActive() {
        this.onActive();
    }
    
    final public void didResignActive() {
        this.onInactive();
    }
    
    /* abstract */ protected void onActive() {}
    
    /* abstract */ protected void onInactive() {}
    
    /* abstract */ public void onTransitionStart() {}

    /* abstract */ public void onKnob(int num, int value) {}

    /* abstract */ public void onSlider(int num, int value) {}

    /* abstract */ public void onButtonDown(int num) {}

    /* abstract */ public void onTouchStart() {}

    /* abstract */ public void onTouchEnd() {}

}

