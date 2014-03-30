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

import java.util.HashSet;
import java.util.Set;

/**
 * This is a parameter instance that can be listened to, meaning we are able
 * to deterministically know when the value has changed. This means that all
 * modifications *must* come through setValue(). 
 */
public abstract class LXListenableParameter implements LXParameter {

    private final String label;
    
    private double defaultValue, value;
    
    private final Set<LXParameterListener> listeners = new HashSet<LXParameterListener>();
    
    protected LXListenableParameter() {
        this(null, 0);
    }
    
    protected LXListenableParameter(String label) {
        this(label, 0);
    }
    
    protected LXListenableParameter(double value) {
        this(null, value);
    }
    
    protected LXListenableParameter(String label, double value) {
        this.label = label;
        setValue(this.defaultValue = this.value = value);
    }
    
    public final LXListenableParameter addListener(LXParameterListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
        return this;
    }
    
    public final LXListenableParameter removeListener(LXParameterListener listener) {
        listeners.remove(listener);
        return this;
    }
    
    public final LXParameter reset() {
        return setValue(this.defaultValue);
    }
    
    /**
     * Resets the value of the parameter, giving it a new default. Future calls
     * to reset() with no parameter will use this value.
     * 
     * @param value New default value
     */
    public final LXParameter reset(double value) {
        this.defaultValue = value;
        return setValue(this.defaultValue);
    }

    public final LXParameter setValue(double value) {
        if (this.value != value) {
            this.value = updateValue(value); 
            for (LXParameterListener l : listeners) {
                l.onParameterChanged(this);
            }
        }
        return this;
    }
    
    public final double getValue() {
        return this.value;
    }
    
    public final float getValuef() {
        return (float) getValue();
    }
    
    public String getLabel() {
        return this.label;
    }
    
    /**
     * Invoked when the value has changed. Subclasses should update any
     * special internal state according to this new value.
     * 
     * @param value New value
     */
    protected abstract double updateValue(double value);
    
}
