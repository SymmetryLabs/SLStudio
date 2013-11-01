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

import heronarts.lx.control.LXListenableParameter;
import heronarts.lx.control.LXParameter;

/**
 * Utility subclass for modulators which oscillate in a range of values between a
 * minimum and a maximum. Includes a set of common methods to change the bounds
 * while the modulator is running. 
 */
public abstract class RangeModulator extends LXModulator implements LXParameter.Listener {

    private LXListenableParameter startValueParameter = null;
    private double minStartValue = 0;
    private double maxStartValue = 0;
    
    private LXListenableParameter endValueParameter = null;
    private double minEndValue = 0;
    private double maxEndValue = 0;
    
    protected double startValue;
    protected double endValue;
    
    protected RangeModulator(double startValue, double endValue, double periodMs) {
        super(periodMs);
        this.startValue = startValue;
        this.endValue = endValue;
    }
    
    /**
     * Updates the range of the modulator.
     * 
     * @param startValue New start value
     * @param endValue New final value
     * @param periodMs New period, in milliseconds
     * @return this
     */
    public final RangeModulator setRange(double startValue, double endValue, double periodMs) {
        this.setPeriod(periodMs);
        this.startValue = startValue;
        this.endValue = endValue;
        checkValueBounds();
        return this;
    }
    
    /**
     * Sets the range of the modulator, maintaining the period.
     * 
     * @param startValue New start value
     * @param endValue New end value
     * @return this
     */
    public final RangeModulator setRange(double startValue, double endValue) {
        return setRange(startValue, endValue, getPeriod());
    }
    
    /**
     * Updates the initial value
     * 
     * @param startValue New start value
     * @return this
     */
    public final RangeModulator setStartValue(double startValue) {
        return setRange(startValue, this.endValue);
    }
    
    /**
     * Updates the final value
     * 
     * @param endValue New final value
     * @return this
     */
    public final RangeModulator setEndValue(double endValue) {
        return setRange(this.startValue, endValue);
    }
    
    /**
     * Updates the range to proceed from the current value to a new end value.
     * Future oscillations will use the current value as the starting value. 
     * 
     * @param endValue New end value
     * @return this
     */
    public final RangeModulator setRangeFromHereTo(double endValue) {
        return setRange(getValue(), endValue);
    }
    
    /**
     * Updates the range to proceed from the current value to a new end value
     * with a new period. Future oscillations will use the current value as the
     * starting value.
     * 
     * @param endValue New end value
     * @param periodMs New period, in milliseconds
     * @return this
     */
    public final RangeModulator setRangeFromHereTo(double endValue, double periodMs) {
        return setRange(getValue(), endValue, periodMs);
    }

    private void checkValueBounds() {
        double min = Math.min(this.startValue, this.endValue);
        double max = Math.max(this.startValue, this.endValue);        
        if (getValue() < min) {
            this.setValue(min);
        } else if (getValue() > max) {
            this.setValue(max);
        } else {
            this.updateBasis();
        }
    }
    
    /**
     * Assigns a parameter to modulate the start value of this modulator. The
     * parameter should have a value between 0 and 1, which will be used to
     * interpolate the start value. 
     * 
     * @param startValueParameter A listenable parameter to modify the start value
     * @param minStartValue The low start value
     * @param maxStartValue The high start value
     * @return this
     */
    public RangeModulator modulateStartValueBy(LXListenableParameter startValueParameter, double minStartValue, double maxStartValue) {
        if (this.startValueParameter != null) {
            this.startValueParameter.removeListener(this); 
        }
        this.startValueParameter = startValueParameter;
        this.minStartValue = minStartValue;
        this.maxStartValue = maxStartValue;
        if (this.startValueParameter != null) {
            this.startValueParameter.addListener(this);
        }
        return this;
    }
    
    /**
     * Assigns a parameter to modulate the end value of this modulator. The
     * parameter should have a value between 0 and 1, which will be used to
     * interpolate the end value. 
     * 
     * @param endValueParameter A listenable parameter to modify the end value
     * @param minEndValue The low end value
     * @param maxEndValue The high end value
     * @return this
     */
    public RangeModulator modulateEndValueBy(LXListenableParameter endValueParameter, double minEndValue, double maxEndValue) {
        if (this.endValueParameter != null) {
            this.endValueParameter.removeListener(this);
        }
        this.endValueParameter = endValueParameter;
        this.minEndValue = minEndValue;
        this.maxEndValue = maxEndValue;
        if (this.endValueParameter != null) {
            this.endValueParameter.addListener(this);
        }
        return this;
    }
    
    public void onParameterChanged(LXParameter parameter) {
        if (parameter == this.startValueParameter) {
            this.setStartValue(this.minStartValue + (this.maxStartValue - this.minStartValue) * parameter.getValue());
        } else if (parameter == this.endValueParameter) {
            this.setEndValue(this.minEndValue + (this.maxEndValue - this.minEndValue) * parameter.getValue());
        }
    }
    
    @Override
    public LXModulator setValue(double value) {
        double min = Math.min(this.startValue, this.endValue);
        double max = Math.max(this.startValue, this.endValue);        
        if (value < min) {
            super.setValue(min);
        } else if (value > max) {
            super.setValue(max);
        } else {
            super.setValue(value);
        }
        return this;
    }    
    
    @Override
    protected final double computeValue(double deltaMs) {
        if (this.startValue == this.endValue) {
            return this.startValue;
        }
        return this.startValue + this.computeNormalizedValue(deltaMs) * (this.endValue - this.startValue);
    }

    @Override
    protected final double computeBasis() {
        if (this.startValue == this.endValue) {
            return 0;
        }
        double normalizedValue = (getValue() - this.startValue) / (this.endValue - this.startValue);
        return computeBasisFromNormalizedValue(normalizedValue);
    }
    
    /**
     * Subclasses implement this which returns their value from a 0-1 scale. This
     * class automatically takes care of scaling to the startValue/endValue range.
     * 
     * @param deltaMs
     */
    protected abstract double computeNormalizedValue(double deltaMs);
    
    /**
     * Subclasses determine the basis based on a normalized value from 0 to 1.
     * 
     * @param normalizedValue A normalize value from 0 to 1
     */
    protected abstract double computeBasisFromNormalizedValue(double normalizedValue);    
    
}
