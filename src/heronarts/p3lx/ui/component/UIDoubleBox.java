/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.ui.component;

import heronarts.lx.LXUtils;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UIControlTarget;
import heronarts.p3lx.ui.UIModulationTarget;
import processing.event.Event;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIDoubleBox extends UINumberBox implements UIControlTarget, UIModulationTarget {

    private double minValue = 0;
    private double maxValue = Double.MAX_VALUE;
    private double value = 0;
    private BoundedParameter parameter = null;

    private final LXParameterListener parameterListener = new LXParameterListener() {
        public void onParameterChanged(LXParameter p) {
            setValue(parameter.getValue());
        }
    };

    public UIDoubleBox() {
        this(0, 0, 0, 0);
    }

    public UIDoubleBox(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    @Override
    public String getDescription() {
        return UIParameterControl.getDescription(this.parameter);
    }

    public UIDoubleBox setParameter(final BoundedParameter parameter) {
        if (this.parameter != null) {
            this.parameter.removeListener(this.parameterListener);
        }
        this.parameter = parameter;
        if (parameter != null) {
            this.minValue = parameter.range.min;
            this.maxValue = parameter.range.max;
            this.parameter.addListener(this.parameterListener);
            setValue(parameter.getValue());
        }
        return this;
    }

    public UIDoubleBox setRange(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        setValue(LXUtils.constrain(this.value, minValue, maxValue));
        return this;
    }

    @Override
    protected double getFillWidthNormalized() {
        if (this.parameter != null) {
            return this.parameter.getNormalized();
        }
        return (this.value - this.minValue) / (this.maxValue - this.minValue);
    }

    public double getValue() {
        return this.value;
    }

    public UIDoubleBox setValue(double value) {
        value = LXUtils.constrain(value, this.minValue, this.maxValue);
        if (this.value != value) {
            this.value = value;
            if (this.parameter != null) {
                this.parameter.setValue(this.value);
            }
            this.onValueChange(this.value);
            redraw();
        }
        return this;
    }

    @Override
    protected String getValueString() {
        return getUnits().format(this.value);
    }

    /**
     * Invoked when value changes, subclasses may override to handle.
     *
     * @param value
     */
    protected /* abstract */ void onValueChange(double value) {}

    @Override
    protected void saveEditBuffer() {
        try {
            setValue(Double.parseDouble(this.editBuffer));
        } catch (NumberFormatException nfx) {}
    }

    public static boolean isValidInputCharacter(char keyChar) {
        return (keyChar >= '0' && keyChar <= '9') || (keyChar == '.') || (keyChar == '-');
    }

    @Override
    protected boolean isValidCharacter(char keyChar) {
        return isValidInputCharacter(keyChar);
    }

    private LXParameter.Units getUnits() {
        if (this.parameter != null) {
            return this.parameter.getUnits();
        }
        return LXParameter.Units.NONE;
    }

    private double getBaseIncrement() {
        double range = Math.abs(this.parameter.range.max - this.parameter.range.min);
        switch (getUnits()) {
        case MILLISECONDS:
            if (range > 10000) {
                return 1000;
            } else if (range > 1000) {
                return 10;
            }
            return 1;
        default:
            return (range > 100) ? 1 : (range / 100.);
        }
    }

    private double getIncrement(Event inputEvent) {
        double increment = getBaseIncrement();
        if (inputEvent.isShiftDown()) {
            if (this.hasShiftMultiplier) {
                increment *= this.shiftMultiplier;
            } else if (this.parameter != null) {
                increment = (float) (this.parameter.getRange() / 10.);
            } else {
                increment *= .1;
            }
        }
        return increment;
    }

    @Override
    protected void decrementValue(KeyEvent keyEvent) {
        consumeKeyEvent();
        setValue(getValue() - getIncrement(keyEvent));
    }

    @Override
    protected void incrementValue(KeyEvent keyEvent) {
        consumeKeyEvent();
        setValue(getValue() + getIncrement(keyEvent));
    }

    @Override
    protected void incrementMouseValue(MouseEvent mouseEvent, int offset) {
        setValue(this.value + offset * getIncrement(mouseEvent));
    }

    @Override
    public LXParameter getControlTarget() {
        return isMappable() ? this.parameter : null;
    }

    @Override
    public CompoundParameter getModulationTarget() {
        if (isMappable() && (this.parameter instanceof CompoundParameter)) {
            return (CompoundParameter) this.parameter;
        }
        return null;
    }

}
