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
 * A virtual parameter is one that wraps or forwards to another real
 * parameter. Typically this is done in situations in which the parameter
 * to forward to varies based on some other contextual action or UI, for
 * instance a virtual knob that maps to whatever pattern is currently active.
 * 
 * This type of parameter is not listenable, since the underlying parameter
 * is dynamic.
 */
public abstract class LXVirtualParameter extends LXParameter {

    /**
     * The parameter to operate on.
     * 
     * @return The underlying real parameter to operate on.
     */
    protected abstract LXParameter getRealParameter();
    
    @Override
    public final LXParameter reset() {
        LXParameter p = getRealParameter();
        if (p != null) {
            p.reset();
        }
        return this;
    }
    
    @Override
    public final LXParameter setValue(double value) {
        LXParameter p = getRealParameter();
        if (p != null) {
            p.setValue(value);
        }
        return this;
    }

    @Override
    public double getValue() {
        LXParameter p = getRealParameter();
        if (p != null) {
            return p.getValue();
        }
        return 0;
    }
    
    @Override
    public String getLabel() {
        LXParameter p = getRealParameter();
        if (p != null) {
            return p.getLabel();
        }
        return null;
    }

}
