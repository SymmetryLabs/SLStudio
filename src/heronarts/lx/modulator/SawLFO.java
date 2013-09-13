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

import java.lang.Math;


/**
 * A sawtooth LFO oscillates from one extreme value to another. When the later
 * value is hit, the oscillator rests to its initial value.
 */
public class SawLFO extends RangeModulator {

    public SawLFO(double startValue, double endValue, double periodMs) {
        super(startValue, endValue, periodMs);
    }

    @Override
    protected final double computeNormalizedValue(int deltaMs) {
        return getBasis();
    }
    
    @Override
    protected final double computeBasisFromNormalizedValue(double normalizedValue) {
        return normalizedValue;
    }
}