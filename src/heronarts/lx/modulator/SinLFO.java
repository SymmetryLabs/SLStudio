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

/**
 * A classic sinusoidal oscillator.
 */
public class SinLFO extends LXRangeModulator {

    public SinLFO(double startValue, double endValue, double periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue), new FixedParameter(periodMs));
    }

    public SinLFO(FixedParameter startValue, double endValue, double periodMs) {
        this(startValue, new FixedParameter(endValue), new FixedParameter(periodMs));
    }
    
    public SinLFO(double startValue, FixedParameter endValue, double periodMs) {
        this(new FixedParameter(startValue), endValue, new FixedParameter(periodMs));
    }
    
    public SinLFO(double startValue, double endValue, FixedParameter periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue), periodMs);
    }
    
    public SinLFO(FixedParameter startValue, FixedParameter endValue, double periodMs) {
        this(startValue, endValue, new FixedParameter(periodMs));
    }
    
    public SinLFO(FixedParameter startValue, double endValue, FixedParameter periodMs) {
        this(startValue, new FixedParameter(endValue), periodMs);
    }

    public SinLFO(double startValue, FixedParameter endValue, FixedParameter periodMs) {
        this(new FixedParameter(startValue), endValue, periodMs);
    }
    
    public SinLFO(FixedParameter startValue, FixedParameter endValue, FixedParameter periodMs) {
        this("SIN", startValue, endValue, periodMs);
    }
    
    public SinLFO(String label, double startValue, double endValue, double periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue), new FixedParameter(periodMs));
    }

    public SinLFO(String label, FixedParameter startValue, double endValue, double periodMs) {
        this(label, startValue, new FixedParameter(endValue), new FixedParameter(periodMs));
    }
    
    public SinLFO(String label, double startValue, FixedParameter endValue, double periodMs) {
        this(label, new FixedParameter(startValue), endValue, new FixedParameter(periodMs));
    }

    public SinLFO(String label, double startValue, double endValue, FixedParameter periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue), periodMs);
    }

    public SinLFO(String label, FixedParameter startValue, FixedParameter endValue, double periodMs) {
        this(label, startValue, endValue, new FixedParameter(periodMs));
    }
    
    public SinLFO(String label, FixedParameter startValue, double endValue, FixedParameter periodMs) {
        this(label, startValue, new FixedParameter(endValue), periodMs);
    }
    
    public SinLFO(String label, double startValue, FixedParameter endValue, FixedParameter periodMs) {
        this(label, new FixedParameter(startValue), endValue, periodMs);
    }
    
    public SinLFO(String label, FixedParameter startValue, FixedParameter endValue, FixedParameter periodMs) {
        super(label, startValue, endValue, periodMs);
    }
    
    @Override
    protected double computeNormalizedValue(double deltaMs) {
        return (1 + Math.sin(getBasis() * TWO_PI - HALF_PI)) / 2.;
    }
    
    @Override
    protected double computeBasisFromNormalizedValue(double normalizedValue) {
        double sinValue = -1 + 2. * normalizedValue;
        double angle = Math.asin(sinValue);
        if (getBasis() > 0.5) {
            angle = Math.PI - angle;
        }
        return (angle + HALF_PI) / TWO_PI;
    }
}
