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
 * A triangular LFO is a simple linear modulator that oscillates between a low
 * and hi value over a specified time period.
 */
public class TriangleLFO extends LXRangeModulator {

    public TriangleLFO(double startValue, double endValue, double periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue), new FixedParameter(periodMs));
    }

    public TriangleLFO(FixedParameter startValue, double endValue, double periodMs) {
        this(startValue, new FixedParameter(endValue), new FixedParameter(periodMs));
    }
    
    public TriangleLFO(double startValue, FixedParameter endValue, double periodMs) {
        this(new FixedParameter(startValue), endValue, new FixedParameter(periodMs));
    }
    
    public TriangleLFO(double startValue, double endValue, FixedParameter periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue), periodMs);
    }
    
    public TriangleLFO(FixedParameter startValue, FixedParameter endValue, double periodMs) {
        this(startValue, endValue, new FixedParameter(periodMs));
    }
    
    public TriangleLFO(FixedParameter startValue, double endValue, FixedParameter periodMs) {
        this(startValue, new FixedParameter(endValue), periodMs);
    }

    public TriangleLFO(double startValue, FixedParameter endValue, FixedParameter periodMs) {
        this(new FixedParameter(startValue), endValue, periodMs);
    }
    
    public TriangleLFO(FixedParameter startValue, FixedParameter endValue, FixedParameter periodMs) {
        this("TRI", startValue, endValue, periodMs);
    }
    
    public TriangleLFO(String label, double startValue, double endValue, double periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue), new FixedParameter(periodMs));
    }

    public TriangleLFO(String label, FixedParameter startValue, double endValue, double periodMs) {
        this(label, startValue, new FixedParameter(endValue), new FixedParameter(periodMs));
    }
    
    public TriangleLFO(String label, double startValue, FixedParameter endValue, double periodMs) {
        this(label, new FixedParameter(startValue), endValue, new FixedParameter(periodMs));
    }

    public TriangleLFO(String label, double startValue, double endValue, FixedParameter periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue), periodMs);
    }

    public TriangleLFO(String label, FixedParameter startValue, FixedParameter endValue, double periodMs) {
        this(label, startValue, endValue, new FixedParameter(periodMs));
    }
    
    public TriangleLFO(String label, FixedParameter startValue, double endValue, FixedParameter periodMs) {
        this(label, startValue, new FixedParameter(endValue), periodMs);
    }
    
    public TriangleLFO(String label, double startValue, FixedParameter endValue, FixedParameter periodMs) {
        this(label, new FixedParameter(startValue), endValue, periodMs);
    }
    
    public TriangleLFO(String label, FixedParameter startValue, FixedParameter endValue, FixedParameter periodMs) {
        super(label, startValue, endValue, periodMs);
    }
    
    @Override
    protected double computeNormalizedValue(double deltaMs) {
        double bv = getBasis();
        if (bv < 0.5) {
            return 2.*bv;
        } else {
            return 1. - 2.*(bv - 0.5);
        }
    }
    
    @Override
    protected double computeBasisFromNormalizedValue(double normalizedValue) {
        if (getBasis() < 0.5) {
            return normalizedValue / 2.;
        } else {
            return 1 - (normalizedValue / 2.);
        }        
    }
}