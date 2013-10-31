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

package heronarts.lx.control;

/**
 * Simple parameter class with a double value.
 */
public class BasicParameter extends LXListenableParameter {
    
    public BasicParameter(String label) {
        this(null, label);
    }
    
    public BasicParameter(String label, double value) {
        this(null, label, value);
    }
    
    public BasicParameter(Listener listener, String label) {
        this(listener, label, 0);
    }
    
    public BasicParameter(Listener listener, String label, double value) {
        super(label, value);
        if (listener != null) {
            this.addListener(listener);
        }
    }
    
    @Override
    protected double updateValue(double value) {
        return value;
    }

}
