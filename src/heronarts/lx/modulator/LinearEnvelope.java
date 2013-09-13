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

    public LinearEnvelope(double startValue, double endValue, double periodMs) {
        super(startValue, endValue, periodMs);
        this.looping = false;
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
