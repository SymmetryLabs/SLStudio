/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import processing.event.Event;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIDoubleBox extends UINumberBox {

    private double minValue = 0;
    private double maxValue = Double.MAX_VALUE;
    private double value = 0;
    private BoundedParameter parameter = null;
    private double baseIncrement = 1;

    public enum Units {
        NONE,
        SECONDS,
        MILLISECONDS,
        DECIBELS
    };

    private Units units = Units.NONE;

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

    /**
     * Set units display after the box content
     *
     * @param units
     * @return
     */
    public UIDoubleBox setUnits(Units units) {
        if (this.units != units) {
            this.units = units;
            this.baseIncrement = (units == Units.MILLISECONDS) ? 1000 : 1;
            redraw();
        }
        return this;
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

    @SuppressWarnings("fallthrough")
    private static String formatValue(Units units, double value) {
        switch (units) {
        case SECONDS:
            value *= 1000;
            // pass through!
        case MILLISECONDS:
            if (value < 1000) {
                return String.format("%dms", (int) value);
            } else if (value < 60000) {
                return String.format("%.2fs", value / 1000);
            } else if (value < 3600000) {
                int minutes = (int) (value / 60000);
                int seconds = (int) ((value % 60000) / 1000);
                return String.format("%dmin %ds", minutes, seconds);
            }
            int hours = (int) (value / 3600000);
            value = value % 3600000;
            int minutes = (int) (value / 60000);
            int seconds = (int) ((value % 60000) / 1000);
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        case DECIBELS:
            return String.format("%.1fdB", value);
        default:
        case NONE:
            return String.format("%.2f", value);
        }
    }

    @Override
    protected String getValueString() {
        return formatValue(this.units, this.value);
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

    private double getIncrement(Event inputEvent) {
        double increment = this.baseIncrement;
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

}
