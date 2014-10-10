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

package heronarts.lx.parameter;

/**
 * Parameter type with a discrete set of possible integer values.
 */
public class DiscreteParameter extends LXListenableNormalizedParameter {

    private int minValue;

    private int maxValue;

    private int range;

    private String[] options = null;

    /**
     * Parameter with values from [0, range-1], 0 by default
     *
     * @param label Name of parameter
     * @param range range of values
     */
    public DiscreteParameter(String label, int range) {
        this(label, 0, range);
    }

    /**
     * Parameter with values from [min, max-1], min by default
     *
     * @param label Label
     * @param min Minimum value
     * @param max Maximum value is 1 less than this
     */
    public DiscreteParameter(String label, int min, int max) {
        this(label, min, min, max);
    }

    /**
     * Parameter with values from [min, max-1], value by default
     *
     * @param label Label
     * @param value Default value
     * @param min Minimum value
     * @param max Maximum value
     */
    public DiscreteParameter(String label, int value, int min, int max) {
        super(label, value);
        setRange(min, max);
    }

    /**
     * Parameter with set of String label values
     *
     * @param label Label
     * @param options Values
     */
    public DiscreteParameter(String label, String[] options) {
        this(label, options.length);
        this.options = options;
    }

    @Override
    protected double updateValue(double value) {
        if (value < this.minValue) {
            return this.minValue
                    + (this.range - ((int) (this.minValue - value) % this.range))
                    % this.range;
        }
        return this.minValue + ((int) (value - this.minValue) % this.range);
    }

    public int getMinValue() {
        return this.minValue;
    }

    public int getMaxValue() {
        return this.maxValue;
    }

    public int getRange() {
        return this.range;
    }

    /**
     * The set of string labels for these parameters
     *
     * @return Strings, may be null
     */
    public String[] getOptions() {
        return this.options;
    }

    /**
     * The currently selected option
     *
     * @return String description, or numerical value
     */
    public String getOption() {
        return (this.options != null) ? this.options[getValuei()] : ("" + getValuei());
    }

    /**
     * Sets the range from [minValue, maxValue-1] inclusive
     *
     * @param minValue Minimum value
     * @param maxValue Maximum value, exclusive
     * @return this
     */
    public DiscreteParameter setRange(int minValue, int maxValue) {
        if (this.options != null) {
            throw new UnsupportedOperationException("May not call setRange on a DiscreteParameter with String options");
        }
        this.minValue = minValue;
        this.maxValue = maxValue - 1;
        if (this.maxValue < this.minValue - 1) {
            throw new IllegalArgumentException(
                    "DiscreteParameter must have range of at least 1");
        }
        this.range = maxValue - minValue;
        setValue(updateValue(this.getValue()));
        return this;
    }

    /**
     * Sets range from [0, range-1] inclusive
     *
     * @param range Number of discrete values
     * @return this
     */
    public DiscreteParameter setRange(int range) {
        return setRange(0, range);
    }

    public DiscreteParameter increment() {
        this.setValue(getValuei() + 1);
        return this;
    }

    public DiscreteParameter decrement() {
        this.setValue(getValuei() - 1);
        return this;
    }

    public int getValuei() {
        return (int) getValue();
    }

    public double getNormalized() {
        return (getValue() - this.minValue) / (this.range - 1);
    }

    public float getNormalizedf() {
        return (float) getNormalized();
    }

    public DiscreteParameter setNormalized(double normalized) {
        int value = (int) Math.floor(normalized * this.range);
        if (value == this.range) {
            --value;
        }
        setValue(this.minValue + value);
        return this;
    }

}
