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
 * A triangular LFO is a simple linear modulator that oscillates between a low
 * and hi value over a specified time period.
 */
public class TriangleLFO extends LXRangeModulator {

    /**
     * Constructs a TriangleLFO
     * 
     * @param startValue Initial value
     * @param endValue Final value
     * @param periodMs Period, in milliseconds
     */
    public TriangleLFO(double startValue, double endValue, double periodMs) {
        this("TRI", startValue, endValue, periodMs);
    }

    /**
     * Constructs a TriangleLFO
     * 
     * @param label Label
     * @param startValue Initial value
     * @param endValue Final value
     * @param periodMs Period, in milliseconds
     */
    public TriangleLFO(String label, double startValue, double endValue, double periodMs) {
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