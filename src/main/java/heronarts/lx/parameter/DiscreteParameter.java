/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * @author Mark C. Slee <mark@heronarts.com>
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

    /**
     * Parameter with set of String label values, and a default
     *
     * @param label Label
     * @param options Values
     * @param value Default index
     */
    public DiscreteParameter(String label, String[] options, int value) {
        this(label, value, 0, options.length);
        this.options = options;
    }

    @Override
    public DiscreteParameter setDescription(String description) {
        return (DiscreteParameter) super.setDescription(description);
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
        return (this.options != null) ? this.options[getValuei()] : Integer.toString(getValuei());
    }

    /**
     * Set the range and option strings for the parameter
     *
     * @param options Array of string labels
     * @return this
     */
    public DiscreteParameter setOptions(String[] options) {
        this.options = options;
        return setRange(options.length);
    }

    /**
     * Sets the range from [minValue, maxValue-1] inclusive
     *
     * @param minValue Minimum value
     * @param maxValue Maximum value, exclusive
     * @return this
     */
    public DiscreteParameter setRange(int minValue, int maxValue) {
        if (this.options != null && (this.options.length != maxValue - minValue)) {
            throw new UnsupportedOperationException("May not call setRange on a DiscreteParameter with String options of different length");
        }
        if (maxValue <= minValue) {
            throw new IllegalArgumentException("DiscreteParameter must have range of at least 1");
        }
        this.minValue = minValue;
        this.maxValue = maxValue - 1;
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
        return increment(1, true);
    }

    public DiscreteParameter increment(boolean wrap) {
        return increment(1, wrap);
    }

    public DiscreteParameter increment(int amt) {
        return increment(amt, true);
    }

    public DiscreteParameter increment(int amt, boolean wrap) {
        if (wrap) {
            setValue(getValuei() + amt);
        } else {
            setValue(Math.min(this.minValue + this.range - 1, getValuei() + amt));
        }
        return this;
    }

    public DiscreteParameter decrement() {
        return decrement(1, true);
    }

    public DiscreteParameter decrement(boolean wrap) {
        return decrement(1, wrap);
    }

    public DiscreteParameter decrement(int amt) {
        return decrement(amt, true);
    }

    public DiscreteParameter decrement(int amt, boolean wrap) {
        if (wrap) {
            setValue(getValuei() - amt);
        } else {
            setValue(Math.max(this.minValue, getValuei() - amt));
        }
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
