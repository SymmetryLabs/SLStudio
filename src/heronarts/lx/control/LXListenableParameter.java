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

import java.util.HashSet;
import java.util.Set;

/**
 * This is a parameter instance that can be listened to, meaning we are able
 * to deterministically know when the value has changed. This means that all
 * modifications *must* come through setValue(). 
 */
public abstract class LXListenableParameter extends LXParameter {

    private final String label;
    
    private double defaultValue, value;
    
    private final Set<Listener> listeners = new HashSet<Listener>();
    
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
    
    public final LXListenableParameter addListener(Listener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
        return this;
    }
    
    public final LXListenableParameter removeListener(Listener listener) {
        listeners.remove(listener);
        return this;
    }
    
    @Override
    public final LXParameter reset() {
        return setValue(this.defaultValue);
    }

    @Override
    public final LXParameter setValue(double value) {
        if (this.value != value) {
            this.value = updateValue(value);
            for (Listener l : listeners) {
                l.onParameterChanged(this);
            }
        }
        return this;
    }
    
    @Override
    public final double getValue() {
        return this.value;
    }
    
    @Override
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
