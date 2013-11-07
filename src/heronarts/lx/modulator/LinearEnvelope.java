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

import heronarts.lx.control.FixedParameter;
import heronarts.lx.control.LXParameter;

import java.lang.Math;

/**
 * This modulator is a simple linear ramp from one value to another over a
 * specified number of milliseconds.
 */
public class LinearEnvelope extends SawLFO {

    public LinearEnvelope(double startValue) {
        this(startValue, startValue, 0);
    }
    
    public LinearEnvelope(double startValue, double endValue, double periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue), new FixedParameter(periodMs));
    }

    public LinearEnvelope(FixedParameter startValue, double endValue, double periodMs) {
        this(startValue, new FixedParameter(endValue), new FixedParameter(periodMs));
    }
    
    public LinearEnvelope(double startValue, FixedParameter endValue, double periodMs) {
        this(new FixedParameter(startValue), endValue, new FixedParameter(periodMs));
    }
    
    public LinearEnvelope(double startValue, double endValue, FixedParameter periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue), periodMs);
    }
    
    public LinearEnvelope(FixedParameter startValue, FixedParameter endValue, double periodMs) {
        this(startValue, endValue, new FixedParameter(periodMs));
    }
    
    public LinearEnvelope(FixedParameter startValue, double endValue, FixedParameter periodMs) {
        this(startValue, new FixedParameter(endValue), periodMs);
    }

    public LinearEnvelope(double startValue, FixedParameter endValue, FixedParameter periodMs) {
        this(new FixedParameter(startValue), endValue, periodMs);
    }
    
    public LinearEnvelope(FixedParameter startValue, FixedParameter endValue, FixedParameter periodMs) {
        this("LENV", startValue, endValue, periodMs);
    }
    
    public LinearEnvelope(String label, double startValue, double endValue, double periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue), new FixedParameter(periodMs));
    }

    public LinearEnvelope(String label, FixedParameter startValue, double endValue, double periodMs) {
        this(label, startValue, new FixedParameter(endValue), new FixedParameter(periodMs));
    }
    
    public LinearEnvelope(String label, double startValue, FixedParameter endValue, double periodMs) {
        this(label, new FixedParameter(startValue), endValue, new FixedParameter(periodMs));
    }

    public LinearEnvelope(String label, double startValue, double endValue, FixedParameter periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue), periodMs);
    }

    public LinearEnvelope(String label, FixedParameter startValue, FixedParameter endValue, double periodMs) {
        this(label, startValue, endValue, new FixedParameter(periodMs));
    }
    
    public LinearEnvelope(String label, FixedParameter startValue, double endValue, FixedParameter periodMs) {
        this(label, startValue, new FixedParameter(endValue), periodMs);
    }
    
    public LinearEnvelope(String label, double startValue, FixedParameter endValue, FixedParameter periodMs) {
        this(label, new FixedParameter(startValue), endValue, periodMs);
    }
    
    public LinearEnvelope(String label, FixedParameter startValue, FixedParameter endValue, FixedParameter periodMs) {
        super(label, startValue, endValue, periodMs);
    }
    
    /**
     * @deprecated Use setRangeFromHereTo(endValue)
     */
    @Deprecated public final LinearEnvelope setEndVal(double endValue) {
        setRangeFromHereTo(endValue);
        return this;
    }    

    /**
     * @deprecated Use setRangeFromHereTo(endValue, periodMs)
     */
    @Deprecated public final LinearEnvelope setEndVal(double endValue, double periodMs) {
        setRangeFromHereTo(endValue, periodMs);
        return this;
    }    
    
}
