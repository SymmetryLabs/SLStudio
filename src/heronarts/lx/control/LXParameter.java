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

public abstract class LXParameter {

    public interface Listener {
        public void onParameterChanged(LXParameter parameter);
    }

    public abstract void setValue(double value);
    public abstract double getValue();
    
    public float getValuef() {
        return (float) getValue();
    }
    
    public abstract String getLabel();
}
