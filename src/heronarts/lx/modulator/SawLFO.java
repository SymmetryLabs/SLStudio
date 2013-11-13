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

package heronarts.lx.modulator;

import heronarts.lx.LXUtils;
import heronarts.lx.control.FixedParameter;
import heronarts.lx.control.LXParameter;

import java.lang.Math;

/**
 * A sawtooth LFO oscillates from one extreme value to another. When the later
 * value is hit, the oscillator rests to its initial value.
 */
public class SawLFO extends LXRangeModulator {

    public SawLFO(double startValue, double endValue, double periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue), new FixedParameter(periodMs));
    }

    public SawLFO(LXParameter startValue, double endValue, double periodMs) {
        this(startValue, new FixedParameter(endValue), new FixedParameter(periodMs));
    }
    
    public SawLFO(double startValue, LXParameter endValue, double periodMs) {
        this(new FixedParameter(startValue), endValue, new FixedParameter(periodMs));
    }
    
    public SawLFO(double startValue, double endValue, LXParameter periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue), periodMs);
    }
    
    public SawLFO(LXParameter startValue, LXParameter endValue, double periodMs) {
        this(startValue, endValue, new FixedParameter(periodMs));
    }
    
    public SawLFO(LXParameter startValue, double endValue, LXParameter periodMs) {
        this(startValue, new FixedParameter(endValue), periodMs);
    }

    public SawLFO(double startValue, LXParameter endValue, LXParameter periodMs) {
        this(new FixedParameter(startValue), endValue, periodMs);
    }
    
    public SawLFO(LXParameter startValue, LXParameter endValue, LXParameter periodMs) {
        this("SAW", startValue, endValue, periodMs);
    }
    
    public SawLFO(String label, double startValue, double endValue, double periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue), new FixedParameter(periodMs));
    }

    public SawLFO(String label, LXParameter startValue, double endValue, double periodMs) {
        this(label, startValue, new FixedParameter(endValue), new FixedParameter(periodMs));
    }
    
    public SawLFO(String label, double startValue, LXParameter endValue, double periodMs) {
        this(label, new FixedParameter(startValue), endValue, new FixedParameter(periodMs));
    }

    public SawLFO(String label, double startValue, double endValue, LXParameter periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue), periodMs);
    }

    public SawLFO(String label, LXParameter startValue, LXParameter endValue, double periodMs) {
        this(label, startValue, endValue, new FixedParameter(periodMs));
    }
    
    public SawLFO(String label, LXParameter startValue, double endValue, LXParameter periodMs) {
        this(label, startValue, new FixedParameter(endValue), periodMs);
    }
    
    public SawLFO(String label, double startValue, LXParameter endValue, LXParameter periodMs) {
        this(label, new FixedParameter(startValue), endValue, periodMs);
    }
    
    public SawLFO(String label, LXParameter startValue, LXParameter endValue, LXParameter periodMs) {
        super(label, startValue, endValue, periodMs);
    }

    
    @Override
    protected final double computeNormalizedValue(double deltaMs) {
        return getBasis();
    }
    
    @Override
    protected final double computeBasisFromNormalizedValue(double normalizedValue) {
        return normalizedValue;
    }
}