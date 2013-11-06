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

/**
 * Simple square wave LFO. Not damped. Oscillates between a low and high value.
 */
public class SquareLFO extends LXRangeModulator {

    /**
     * Constructs a SquareLFO
     * 
     * @param startValue Initial value
     * @param endValue Final value
     * @param periodMs Period, in milliseconds
     */
    public SquareLFO(double startValue, double endValue, double periodMs) {
        super(startValue, endValue, periodMs);
    }

    @Override
    protected double computeNormalizedValue(double deltaMs) {
        return (getBasis() < 0.5) ? 0 : 1;
    }
    
    @Override
    protected double computeBasisFromNormalizedValue(double normalizedValue) {
        return (normalizedValue == 0) ? 0 : 0.5;
    }

}
