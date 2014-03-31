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
 * A simple parameter that has a binary value of off or on
 */
public class BooleanParameter extends LXListenableNormalizedParameter {

    public BooleanParameter(String label) {
        this(label, false);
    }

    public BooleanParameter(String label, boolean on) {
        super(label, on ? 1. : 0.);
    }

    public boolean isOn() {
        return getValueb();
    }

    public boolean getValueb() {
        return this.getValue() > 0.;
    }

    public BooleanParameter setValue(boolean value) {
        setValue(value ? 1. : 0.);
        return this;
    }

    public BooleanParameter toggle() {
        setValue(!isOn());
        return this;
    }

    @Override
    protected double updateValue(double value) {
        return (value > 0) ? 1. : 0.;
    }

    public double getNormalized() {
        return (getValue() > 0) ? 1. : 0.;
    }

    public float getNormalizedf() {
        return (float) getNormalized();
    }

    public BooleanParameter setNormalized(double normalized) {
        setValue(normalized >= 0.5);
        return this;
    }

}
