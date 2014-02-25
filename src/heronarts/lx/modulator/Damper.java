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

import heronarts.lx.parameter.FixedParameter;
import heronarts.lx.parameter.LXParameter;

public class Damper extends LXModulator {
    
    private final LXParameter parameter;
    
    private final LXParameter maxVelocity;
        
    public Damper(LXParameter parameter) {
        this(parameter, 1);
    }
    
    public Damper(LXParameter parameter, double maxVelocity) {
        this(parameter, new FixedParameter(maxVelocity));
    }
    
    public Damper(LXParameter parameter, LXParameter maxVelocity) {
        super("DAMP-" + parameter.getLabel());
        this.parameter = parameter;
        this.maxVelocity = maxVelocity;
    }
    
    protected double computeValue(double deltaMs) {
        double value = getValue();
        double target = this.parameter.getValue();
        if (value == target) {
            return value;
        }
        double range = this.maxVelocity.getValue() * deltaMs / 1000.;
        if (target > value) {
            return Math.min(value + range, target);
        } else {
            return Math.max(value - range, target);
        }
    }

}
