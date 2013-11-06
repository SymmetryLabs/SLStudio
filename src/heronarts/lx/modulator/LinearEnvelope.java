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

import java.lang.Math;

/**
 * This modulator is a simple linear ramp from one value to another over a
 * specified number of milliseconds.
 */
public class LinearEnvelope extends SawLFO {

    private final static String DEFAULT_LABEL = "LENV";
    
    /**
     * @param startValue Start value, same as end value, no duration
     */
    public LinearEnvelope(double startValue) {
        this(DEFAULT_LABEL, startValue, startValue, 0);
    }
    
    /**
     * @param startValue Start value, same as end value, no duration
     */
    public LinearEnvelope(String label, double startValue) {
        this(label, startValue, startValue, 0);
    }
    
    /**
     * @param startValue Initial value
     * @param endValue End value
     * @param periodMs Period, in milliseconds
     */
    public LinearEnvelope(double startValue, double endValue, double periodMs) {
        this(DEFAULT_LABEL, startValue, endValue, periodMs);
    }
    
    /**
     * @param label Label
     * @param startValue Initial value
     * @param endValue End value
     * @param periodMs Period, in milliseconds
     */
    public LinearEnvelope(String label, double startValue, double endValue, double periodMs) {
        super(label, startValue, endValue, periodMs);
        setLooping(false);
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
