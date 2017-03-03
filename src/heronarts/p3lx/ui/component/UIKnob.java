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
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.MouseEvent;

public class UIKnob extends UIParameterControl implements UIFocus {

    public final static int KNOB_MARGIN = 6;
    public final static int KNOB_SIZE = 28;
    public final static int WIDTH = KNOB_SIZE + 2*KNOB_MARGIN;

    private final static float KNOB_INDENT = .4f;
    private final static int ARC_CENTER_X = WIDTH / 2;
    private final static int ARC_CENTER_Y = KNOB_SIZE / 2;
    private final static float ARC_START = PConstants.HALF_PI + KNOB_INDENT;
    private final static float ARC_RANGE = PConstants.TWO_PI - 2 * KNOB_INDENT;

    public enum ArcMode {
        UNIPOLAR,
        BIPOLAR
    };

    private ArcMode arcMode = ArcMode.UNIPOLAR;

    public UIKnob() {
        this(0, 0);
    }

    public UIKnob(float x, float y) {
        this(x, y, WIDTH, KNOB_SIZE);
    }

    public UIKnob(float x, float y, float w, float h) {
        super(x, y, w, h);
        this.keyEditable = true;
        enableImmediateEdit(true);
    }

    public UIKnob setArcMode(ArcMode arcMode) {
        if (this.arcMode != arcMode) {
            this.arcMode = arcMode;
            redraw();
        }
        return this;
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        float knobValue = (float) getNormalized();
        float valueEnd = ARC_START + knobValue * ARC_RANGE;
        float valueStart;
        switch (this.arcMode) {
        case BIPOLAR: valueStart = ARC_START + ARC_RANGE/2; break;
        default: case UNIPOLAR: valueStart = ARC_START; break;
        }

        // Full outer dark ring
        pg.ellipseMode(PConstants.CENTER);
        pg.noStroke();
        pg.fill(ui.theme.getControlBackgroundColor());
        pg.arc(ARC_CENTER_X, ARC_CENTER_Y, KNOB_SIZE, KNOB_SIZE, ARC_START, ARC_START + ARC_RANGE);

        // Light ring of value
        pg.fill(isEnabled() ? ui.theme.getPrimaryColor() : ui.theme.getControlDisabledColor());
        pg.arc(ARC_CENTER_X, ARC_CENTER_Y, KNOB_SIZE, KNOB_SIZE, Math.min(valueStart, valueEnd), Math.max(valueStart, valueEnd));

        if ((this.arcMode == ArcMode.BIPOLAR) && (valueStart == valueEnd)) {
            pg.stroke(0xff333333);
            pg.line(ARC_CENTER_X, ARC_CENTER_Y - KNOB_SIZE / 4, ARC_CENTER_X, ARC_CENTER_Y - KNOB_SIZE/2);
        }

        // Center circle of knob
        pg.noStroke();
        pg.fill(0xff333333);
        pg.ellipse(ARC_CENTER_X, ARC_CENTER_Y, KNOB_SIZE/2, KNOB_SIZE/2);

        super.onDraw(ui, pg);
    }

    private double dragValue;

    @Override
    protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        super.onMousePressed(mouseEvent, mx, my);
        this.dragValue = getNormalized();
        if ((this.parameter != null) && (mouseEvent.getCount() > 1)) {
            this.parameter.reset();
        }
    }

    @Override
    protected void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
        if (!isEnabled()) {
            return;
        }
        this.dragValue = LXUtils.constrain(this.dragValue - dy / 100., 0, 1);
        setNormalized(this.dragValue);
    }

}
