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

    private final String label;
    private double value, defaultValue;
    
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
        this.label = label;
        this.value = this.defaultValue = value;
        if (listener != null) {
            this.addListener(listener);
        }
    }
    
    @Override
    public LXParameter reset() {
        setValue(this.defaultValue);
        return this;
    }
    
    @Override
    protected void updateValue(double value) {
        this.value = value;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public String getLabel() {
        return label;
    }

}
