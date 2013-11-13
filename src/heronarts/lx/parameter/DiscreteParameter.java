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
 * Parameter type with a discrete set of possible integer values from [0, range-1].
 */
public class DiscreteParameter extends LXListenableParameter {

    private final int range;
    
    public DiscreteParameter(String label, int range) {
        this(label, range, 0);
    }
    
    public DiscreteParameter(String label, int range, int value) {
        super(label, value);
        this.range = range;
    }
            
    @Override
    protected double updateValue(double value) {
        return ((int)value % this.range);
    }

}
