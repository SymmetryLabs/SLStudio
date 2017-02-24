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
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import processing.core.PConstants;
import processing.event.Event;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIIntegerBox extends UINumberBox {

    private int minValue = 0;
    private int maxValue = PConstants.MAX_INT;
    private int value = 0;
    private DiscreteParameter parameter = null;

    private final LXParameterListener parameterListener = new LXParameterListener() {
        public void onParameterChanged(LXParameter p) {
            setValue(parameter.getValuei());
        }
    };

    public UIIntegerBox() {
        this(0, 0, 0, 0);
    }

    public UIIntegerBox(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    public UIIntegerBox setParameter(final DiscreteParameter parameter) {
        if (this.parameter != null) {
            this.parameter.removeListener(this.parameterListener);
        }
        this.parameter = parameter;
        if (parameter != null) {
            this.minValue = parameter.getMinValue();
            this.maxValue = parameter.getMaxValue();
            this.value = parameter.getValuei();
            this.parameter.addListener(this.parameterListener);
        }
        return this;
    }

    public UIIntegerBox setRange(int minValue, int maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        setValue(LXUtils.constrain(this.value, minValue, maxValue));
        return this;
    }

    public int getValue() {
        return this.value;
    }

    @Override
    public String getValueString() {
        return Integer.toString(this.value);
    }

    public UIIntegerBox setValue(int value) {
        if (this.value != value) {
            int range = (this.maxValue - this.minValue + 1);
            while (value < this.minValue) {
                value += range;
            }
            this.value = this.minValue + (value - this.minValue) % range;
            if (this.parameter != null) {
                this.parameter.setValue(this.value);
            }
            this.onValueChange(this.value);
            redraw();
        }
        return this;
    }

    /**
     * Subclasses may override to handle value changes
     *
     * @param value
     */
    protected void onValueChange(int value) {}


    @Override
    protected void saveEditBuffer() {
        try {
            setValue(Integer.parseInt(this.editBuffer));
        } catch (NumberFormatException nfx) {}
    }

    @Override
    protected boolean isValidCharacter(char keyChar) {
        // TODO Auto-generated method stub
        return false;
    }

    private int getIncrement(Event inputEvent) {
        int increment = 1;
        if (inputEvent.isShiftDown()) {
            if (this.hasShiftMultiplier) {
                increment *= this.shiftMultiplier;
            } else if (this.parameter != null) {
                increment = Math.max(1, this.parameter.getRange() / 10);
            } else {
                increment *= 10;
            }
        }
        return increment;
    }

    @Override
    protected void incrementValue(KeyEvent keyEvent) {
        setValue(this.value + getIncrement(keyEvent));
    }

    @Override
    protected void decrementValue(KeyEvent keyEvent) {
        setValue(this.value - getIncrement(keyEvent));
    }

    @Override
    protected void incrementMouseValue(MouseEvent mouseEvent, int offset) {
        setValue(this.value + getIncrement(mouseEvent) * offset);
    }

}
