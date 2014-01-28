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

package heronarts.lx.ui.component;

import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.ui.UIObject;

import processing.core.PGraphics;

public abstract class UIParameterControl extends UIObject implements
        LXParameterListener {

    protected LXListenableNormalizedParameter parameter = null;

    protected UIParameterControl(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    public void onParameterChanged(LXParameter parameter) {
        redraw();
    }

    protected double getNormalized() {
        if (this.parameter != null) {
            return this.parameter.getNormalized();
        }
        return 0;
    }

    protected UIParameterControl setNormalized(double normalized) {
        if (this.parameter != null) {
            this.parameter.setNormalized(normalized);
        }
        return this;
    }

    public UIParameterControl setParameter(LXListenableNormalizedParameter parameter) {
        if (this.parameter != null) {
            this.parameter.removeListener(this);
        }
        this.parameter = parameter;
        if (this.parameter != null) {
            this.parameter.addListener(this);
        }
        redraw();
        return this;
    }
}
