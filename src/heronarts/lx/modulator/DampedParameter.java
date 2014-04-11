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

import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.FixedParameter;
import heronarts.lx.parameter.LXParameter;

public class DampedParameter extends LXModulator {

    private final LXParameter parameter;

    private LXParameter velocity;

    public DampedParameter(String label, double velocity) {
        this(new BasicParameter(label, 0, Double.MIN_VALUE, Double.MAX_VALUE),
                velocity);
    }

    public DampedParameter(LXParameter parameter, double velocity) {
        this(parameter, new FixedParameter(velocity));
    }

    public DampedParameter(LXParameter parameter, LXParameter velocity) {
        this("DAMPED-" + parameter.getLabel(), parameter, velocity);
    }

    public DampedParameter(String label, LXParameter parameter,
            LXParameter velocity) {
        super(label);
        this.parameter = parameter;
        this.velocity = velocity;
        updateValue(parameter.getValue());
    }

    @Override
    protected double computeValue(double deltaMs) {
        double value = getValue();
        double target = this.parameter.getValue();
        if (value == target) {
            return value;
        }
        double range = this.velocity.getValue() * deltaMs / 1000.;
        double after;
        if (target > value) {
            after = Math.min(value + range, target);
        } else {
            after = Math.max(value - range, target);
        }
        return after;
    }

    public LXParameter getParameter() {
        return this.parameter;
    }

}
