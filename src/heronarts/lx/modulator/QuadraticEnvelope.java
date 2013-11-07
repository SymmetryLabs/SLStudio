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

/**
 * A quadratic envelope moves from one value to another along a quadratic curve. 
 */
public class QuadraticEnvelope extends LXRangeModulator {

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
    
    public QuadraticEnvelope(double startValue, double endValue, double periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue), new FixedParameter(periodMs));
    }

    public QuadraticEnvelope(FixedParameter startValue, double endValue, double periodMs) {
        this(startValue, new FixedParameter(endValue), new FixedParameter(periodMs));
    }
    
    public QuadraticEnvelope(double startValue, FixedParameter endValue, double periodMs) {
        this(new FixedParameter(startValue), endValue, new FixedParameter(periodMs));
    }
    
    public QuadraticEnvelope(double startValue, double endValue, FixedParameter periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue), periodMs);
    }
    
    public QuadraticEnvelope(FixedParameter startValue, FixedParameter endValue, double periodMs) {
        this(startValue, endValue, new FixedParameter(periodMs));
    }
    
    public QuadraticEnvelope(FixedParameter startValue, double endValue, FixedParameter periodMs) {
        this(startValue, new FixedParameter(endValue), periodMs);
    }

    public QuadraticEnvelope(double startValue, FixedParameter endValue, FixedParameter periodMs) {
        this(new FixedParameter(startValue), endValue, periodMs);
    }
    
    public QuadraticEnvelope(FixedParameter startValue, FixedParameter endValue, FixedParameter periodMs) {
        this("QENV", startValue, endValue, periodMs);
    }
    
    public QuadraticEnvelope(String label, double startValue, double endValue, double periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue), new FixedParameter(periodMs));
    }

    public QuadraticEnvelope(String label, FixedParameter startValue, double endValue, double periodMs) {
        this(label, startValue, new FixedParameter(endValue), new FixedParameter(periodMs));
    }
    
    public QuadraticEnvelope(String label, double startValue, FixedParameter endValue, double periodMs) {
        this(label, new FixedParameter(startValue), endValue, new FixedParameter(periodMs));
    }

    public QuadraticEnvelope(String label, double startValue, double endValue, FixedParameter periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue), periodMs);
    }

    public QuadraticEnvelope(String label, FixedParameter startValue, FixedParameter endValue, double periodMs) {
        this(label, startValue, endValue, new FixedParameter(periodMs));
    }
    
    public QuadraticEnvelope(String label, FixedParameter startValue, double endValue, FixedParameter periodMs) {
        this(label, startValue, new FixedParameter(endValue), periodMs);
    }
    
    public QuadraticEnvelope(String label, double startValue, FixedParameter endValue, FixedParameter periodMs) {
        this(label, new FixedParameter(startValue), endValue, periodMs);
    }
    
    public QuadraticEnvelope(String label, FixedParameter startValue, FixedParameter endValue, FixedParameter periodMs) {
        super(label, startValue, endValue, periodMs);
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
