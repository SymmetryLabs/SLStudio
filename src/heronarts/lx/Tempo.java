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

import heronarts.lx.modulator.Click;
import heronarts.lx.modulator.LinearEnvelope;

/**
 * Class to represent a musical tempo at which patterns are operating.
 * This can be updated in real-time via invoking the tap() method.
 * 
 * Quarter-note "beats" are indicated by the return value of the beat()
 * method, and ramp() returns a double value indicating the current
 * 4/4 phase from 0 to 1.
 */
public class Tempo {

    private final Click click = new Click(500);
    private final LinearEnvelope ramp = new LinearEnvelope(0, 1, 500);

    private long firstTap = 0;
    private long lastTap = 0;
    private int tapCount = 0;

    public Tempo() {
        this.click.start();
        this.ramp.start();
    }

    /**
     * Method to indicate when we are on-beat, assuming quarter-notes
     * being given one beat.
     * 
     * @return true if we are on a quarter-note beat
     */
    public boolean beat() {
        return this.click.click();
    }

    /**
     * Indicates phase of the current beat. On the beat the value will
     * be 0, then ramp up to 1 before the next beat triggers.
     * 
     * @return value from 0-1 indicating phase of beat
     */
    public double ramp() {
        return this.ramp.getValue();
    }
    
    /**
     * Indicates beat phase in floating point
     * 
     * @return value from 0-1 indicating phase of beat
     */
    public float rampf() {
        return (float)this.ramp();
    }
    
    /**
     * Returns the current tempo in Beats Per Minute
     * 
     * @return Current tempo
     */
    public double bpm() {
        return 60000. / this.click.getDuration();
    }
    
    /**
     * Returns the tempo in floating point
     * 
     * @return Current tempo in float
     */
    public float bpmf() {
        return (float)this.bpm();
    }
    

    /**
     * Adjusts the tempo by the given amount
     * 
     * @param deltaMs Number of milliseconds to nudge beat by
     */
    public void adjustBpm(double deltaMs) {
        this.setBpm(this.bpm() + deltaMs);
    }
    
    /**
     * Sets the BPM to the given value
     * 
     * @param bpm Number of beats per minute
     */
    public void setBpm(double bpm) {
        double duration = 60000. / bpm;
        this.click.setDuration(duration);
        this.ramp.setDuration(duration);
    }

    /**
     * Re-triggers the metronome, so that it immediately beats.
     */
    public void trigger() {
        this.click.fire();
        this.ramp.trigger();
    }

    /**
     * Adjusts the tempo in realtime by tapping. Whenever tap() is invoked the
     * time between previous taps is averaged to compute a new tempo. At least
     * three taps are required to compute a tempo. Otherwise, tapping just
     * re-triggers the beat. It is better to use the trigger() method directly
     * if this is all that is desired.
     */
    public void tap() {
        long now = System.currentTimeMillis();
        if (now - this.lastTap > 2000) {
            this.firstTap = now;
            this.tapCount = 0;
        }
        this.lastTap = now;
        ++this.tapCount;
        if (this.tapCount > 3) {
            double duration = (this.lastTap - this.firstTap) / (double) (this.tapCount - 1);
            this.click.setDuration(duration);
            this.ramp.setDuration(duration);
            this.trigger();
        } else {
            this.trigger();
        }
    }
    
    void run(int deltaMs) {
        this.click.run(deltaMs);
        if (this.beat()) {
            this.ramp.trigger();
        }
        this.ramp.run(deltaMs);
    }
}
