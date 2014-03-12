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

import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.ui.UI;
import heronarts.lx.ui.UIObject;
import processing.core.PConstants;
import processing.core.PGraphics;

public class UIButton extends UIObject {

    protected boolean active = false;
    protected boolean isMomentary = false;
    protected int borderColor = 0xff666666;
    protected int inactiveColor = 0xff222222;
    protected int activeColor = 0xff669966;
    protected int labelColor = 0xff999999;
    protected String label = "";
    
    private BooleanParameter parameter = null;

    private final LXParameterListener parameterListener = new LXParameterListener() {
        public void onParameterChanged(LXParameter p) {
            setActive(parameter.isOn());
        }
    };
    
    public UIButton() {
        this(0, 0, 0, 0);
    }
    
    public UIButton(float x, float y, float w, float h) {
        super(x, y, w, h);
    }
    
    public UIButton setParameter(BooleanParameter parameter) {
        if (this.parameter != null) {
            this.parameter.removeListener(this.parameterListener);
        }
        this.parameter = parameter;
        if (parameter != null) {
            parameter.addListener(this.parameterListener);
            setActive(parameter.isOn());
        }
        return this;
    }

    public UIButton setMomentary(boolean momentary) {
        this.isMomentary = momentary;
        return this;
    }

    protected void onDraw(UI ui, PGraphics pg) {
        pg.stroke(this.borderColor);
        pg.fill(this.active ? this.activeColor : this.inactiveColor);
        pg.rect(0, 0, this.width, this.height);
        if ((this.label != null) && (this.label.length() > 0)) {
            pg.fill(this.active ? 0xffffffff : this.labelColor);
            pg.textFont(ui.getItemFont());
            pg.textAlign(PConstants.CENTER);
            pg.text(label, this.width / 2, this.height - 5);
        }
    }

    protected void onMousePressed(float mx, float my) {
        setActive(this.isMomentary ? true : !this.active);
    }

    protected void onMouseReleased(float mx, float my) {
        if (this.isMomentary) {
            setActive(false);
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public UIButton setActive(boolean active) {
        if (this.active != active) {
            if (this.parameter != null) {
                this.parameter.setValue(active);
            }
            onToggle(this.active = active);
            redraw();
        }
        return this;
    }

    public UIButton toggle() {
        return setActive(!this.active);
    }

    protected void onToggle(boolean active) {
    }

    public UIButton setBorderColor(int borderColor) {
        if (this.borderColor != borderColor) {
            this.borderColor = borderColor;
            redraw();
        }
        return this;
    }

    public UIButton setActiveColor(int activeColor) {
        if (this.activeColor != activeColor) {
            this.activeColor = activeColor;
            if (active) {
                redraw();
            }
        }
        return this;
    }

    public UIButton setInactiveColor(int inactiveColor) {
        if (this.inactiveColor != inactiveColor) {
            this.inactiveColor = inactiveColor;
            if (!this.active) {
                redraw();
            }
        }
        return this;
    }

    public UIButton setLabelColor(int labelColor) {
        if (this.labelColor != labelColor) {
            this.labelColor = labelColor;
            redraw();
        }
        return this;
    }

    public UIButton setLabel(String label) {
        if (!this.label.equals(label)) {
            this.label = label;
            redraw();
        }
        return this;
    }

    public void onMousePressed() {
        setActive(!this.active);
    }
}
