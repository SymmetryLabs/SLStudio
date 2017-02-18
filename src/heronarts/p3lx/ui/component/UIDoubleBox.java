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
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIDoubleBox extends UI2dComponent implements UIFocus {

    private double minValue = 0;
    private double maxValue = Double.MAX_VALUE;
    private double value = 0;
    private BoundedParameter parameter = null;

    private boolean editing = false;
    private String editBuffer = "";

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
        setBorderColor(UI.get().theme.getControlBorderColor());
        setBackgroundColor(UI.get().theme.getControlBackgroundColor());
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
    protected void onDraw(UI ui, PGraphics pg) {
        pg.textFont(hasFont() ? getFont() : ui.theme.getControlFont());
        pg.textAlign(PConstants.CENTER, PConstants.BOTTOM);
        pg.fill(this.editing ? ui.theme.getPrimaryColor() : ui.theme.getControlTextColor());
        // TODO(mcslee): handle text overflowing buffer
        pg.text(this.editing ? this.editBuffer : String.format("%.2f", this.value), this.width / 2, this.height - 2);
    }

    protected void onValueChange(double value) {
    }

    float dAccum = 0;

    @Override
    protected void onBlur() {
        super.onBlur();
        if (this.editing) {
            this.editing = false;
            try {
                setValue(Double.parseDouble(this.editBuffer));
            } catch (NumberFormatException nfx) {
                redraw();
            }
        }
    }

    @Override
    protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        this.dAccum = 0;
    }

    @Override
    protected void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
        this.dAccum -= dy;
        int offset = (int) (this.dAccum / 5);
        this.dAccum = this.dAccum - (offset * 5);
        if (!this.editing) {
            setValue(this.value + offset);
        }
    }

    @Override
    protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if ((keyChar >= '0' && keyChar <= '9') || (keyChar == '.')) {
            if (!this.editing) {
                this.editing = true;
                this.editBuffer = "";
            }
            this.editBuffer += keyChar;
            redraw();
        }
        if (this.editing) {
            if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                this.editing = false;
                try {
                    setValue(Double.parseDouble(this.editBuffer));
                } catch (NumberFormatException nfx) {}
                redraw();
            } else if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                if (this.editBuffer.length() > 0) {
                    this.editBuffer = this.editBuffer.substring(0, this.editBuffer.length() - 1);
                    redraw();
                }
            } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
                this.editing = false;
                redraw();
            }
        }

        if (!this.editing) {
            double increment = 1;
            if (keyEvent.isShiftDown()) {
                increment = .1;
            }
            if ((keyCode == java.awt.event.KeyEvent.VK_LEFT)
                || (keyCode == java.awt.event.KeyEvent.VK_DOWN)) {
                setValue(getValue() - increment);
            } else if ((keyCode == java.awt.event.KeyEvent.VK_RIGHT)
                || (keyCode == java.awt.event.KeyEvent.VK_UP)) {
                setValue(getValue() + increment);
            }
        }
    }
}
