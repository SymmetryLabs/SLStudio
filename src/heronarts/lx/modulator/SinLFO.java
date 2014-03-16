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
import heronarts.lx.parameter.FixedParameter;
import heronarts.lx.parameter.LXParameter;

/**
 * A classic sinusoidal oscillator.
 */
public class SinLFO extends LXRangeModulator {

    public SinLFO(double startValue, double endValue, double periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue), new FixedParameter(periodMs));
    }

    public SinLFO(LXParameter startValue, double endValue, double periodMs) {
        this(startValue, new FixedParameter(endValue), new FixedParameter(periodMs));
    }
    
    public SinLFO(double startValue, LXParameter endValue, double periodMs) {
        this(new FixedParameter(startValue), endValue, new FixedParameter(periodMs));
    }
    
    public SinLFO(double startValue, double endValue, LXParameter periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue), periodMs);
    }
    
    public SinLFO(LXParameter startValue, LXParameter endValue, double periodMs) {
        this(startValue, endValue, new FixedParameter(periodMs));
    }
    
    public SinLFO(LXParameter startValue, double endValue, LXParameter periodMs) {
        this(startValue, new FixedParameter(endValue), periodMs);
    }

    public SinLFO(double startValue, LXParameter endValue, LXParameter periodMs) {
        this(new FixedParameter(startValue), endValue, periodMs);
    }
    
    public SinLFO(LXParameter startValue, LXParameter endValue, LXParameter periodMs) {
        this("SIN", startValue, endValue, periodMs);
    }
    
    public SinLFO(String label, double startValue, double endValue, double periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue), new FixedParameter(periodMs));
    }

    public SinLFO(String label, LXParameter startValue, double endValue, double periodMs) {
        this(label, startValue, new FixedParameter(endValue), new FixedParameter(periodMs));
    }
    
    public SinLFO(String label, double startValue, LXParameter endValue, double periodMs) {
        this(label, new FixedParameter(startValue), endValue, new FixedParameter(periodMs));
    }

    public SinLFO(String label, double startValue, double endValue, LXParameter periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue), periodMs);
    }

    public SinLFO(String label, LXParameter startValue, LXParameter endValue, double periodMs) {
        this(label, startValue, endValue, new FixedParameter(periodMs));
    }
    
    public SinLFO(String label, LXParameter startValue, double endValue, LXParameter periodMs) {
        this(label, startValue, new FixedParameter(endValue), periodMs);
    }
    
    public SinLFO(String label, double startValue, LXParameter endValue, LXParameter periodMs) {
        this(label, new FixedParameter(startValue), endValue, periodMs);
    }
    
    public SinLFO(String label, LXParameter startValue, LXParameter endValue, LXParameter periodMs) {
        super(label, startValue, endValue, periodMs);
    }
    
    @Override
    protected double computeNormalizedValue(double deltaMs, double basis) {
        return (1 + Math.sin(basis * TWO_PI - HALF_PI)) / 2.;
    }
    
    @Override
    protected double computeNormalizedBasis(double basis, double normalizedValue) {
        double sinValue = -1 + 2. * normalizedValue;
        double angle = Math.asin(sinValue);
        if (basis > 0.5) {
            angle = Math.PI - angle;
        }
        return (angle + HALF_PI) / TWO_PI;
    }
}
