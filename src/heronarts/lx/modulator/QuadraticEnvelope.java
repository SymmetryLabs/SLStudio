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
 * A quadratic envelope moves from one value to another along a quadratic curve. 
 */
public class QuadraticEnvelope extends RangeModulator {

    /**
     * Different modes of quadratic easing.
     */
    public enum Ease {
        /**
         * The quadratic curve accelerates towards the final value
         */
        IN,
        
        /**
         * The quadratic curve decelerates towards the final value
         */
        OUT,
        
        /**
         * The curve slops on both the start and end values
         */
        BOTH
    };

    private Ease ease = Ease.IN;
    
    /**
     * Constructs a new envelope
     * 
     * @param startValue Initial value
     * @param endValue Ending value
     * @param periodMs Period, in milliseconds
     */
    public QuadraticEnvelope(double startValue, double endValue, double periodMs) {
        super(startValue, endValue, periodMs);
        this.looping = false;
    }
    
    /**
     * Sets the easing type
     * 
     * @param ease easing type
     * @return this
     */
    public QuadraticEnvelope setEase(Ease ease) {
        this.ease = ease;
        return this;
    }
    
    @Override
    protected double computeNormalizedValue(double deltaMs) {
        final double bv = getBasis();
        switch (this.ease) {
        case IN:
            return bv*bv;
        case OUT:
            return 1 - (1-bv)*(1-bv);
        case BOTH:
            if (bv < 0.5) {
                return (bv*2)*(bv*2) / 2.;
            } else {
                final double biv = 1 - (bv-0.5) * 2.;
                return 0.5 + (1-biv*biv) / 2.;
            }
        }
        return 0;
    }
            
    @Override
    protected double computeBasisFromNormalizedValue(double normalizedValue) {
        switch (this.ease) {
        case IN:
            return Math.sqrt(normalizedValue);
        case OUT:
            return 1 - Math.sqrt(1 - normalizedValue);
        case BOTH:
            if (normalizedValue < 0.5) {
                normalizedValue = normalizedValue*2;
                return Math.sqrt(normalizedValue) / 2.;
            } else {
                normalizedValue = (normalizedValue-0.5)*2;
                return 0.5 + (1 - Math.sqrt(1 - normalizedValue)) / 2.;
            }
        }
        return 0;
    }
        
}
