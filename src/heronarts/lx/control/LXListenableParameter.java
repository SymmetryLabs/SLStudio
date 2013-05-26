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

public abstract class LXListenableParameter extends LXParameter {

    private final Set<Listener> listeners = new HashSet<Listener>();
    
    public final void addListener(Listener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }
    
    public final void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    @Override
    public final void setValue(double value) {
        if (value != getValue()) {
            updateValue(value);
            for (Listener l : listeners) {
                l.onParameterChanged(this);
            }
        }
    }
    
    protected abstract void updateValue(double value);
    
}
