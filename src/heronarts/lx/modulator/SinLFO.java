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

/**
 * A classic sinusoidal oscillator.
 */
public class SinLFO extends LXRangeModulator {

    /**
     * Constructs a SinLFO
     * 
     * @param startValue Initial value
     * @param endValue Final value
     * @param periodMs Period, in milliseconds
     */
    public SinLFO(double startValue, double endValue, double periodMs) {
        this("SIN", startValue, endValue, periodMs);
    }
    
    /**
     * Constructs a SinLFO
     * 
     * @param label Label
     * @param startValue Initial value
     * @param endValue Final value
     * @param periodMs Period, in milliseconds
     */
    public SinLFO(String label, double startValue, double endValue, double periodMs) {
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
