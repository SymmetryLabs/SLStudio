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
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameterModulation;
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

        float arcSize = KNOB_SIZE;

        pg.ellipseMode(PConstants.CENTER);
        pg.noStroke();

        // Modulations!
        if (this.parameter instanceof CompoundParameter) {
            CompoundParameter compound = (CompoundParameter) this.parameter;
            for (LXParameterModulation modulation : compound.modulations) {
                float modStart = valueEnd;
                float modEnd = LXUtils.constrainf(modStart + modulation.range.getValuef() * ARC_RANGE, ARC_START, ARC_START + ARC_RANGE);
                // Light ring of value
                pg.noFill();
                pg.fill(isEnabled() ? ui.theme.getSecondaryColor() : ui.theme.getControlDisabledColor());
                pg.arc(ARC_CENTER_X, ARC_CENTER_Y, arcSize, arcSize, Math.min(modStart, modEnd), Math.max(modStart, modEnd));
                arcSize -= 6;
            }
        }

        // Full outer dark ring
        pg.fill(ui.theme.getControlBackgroundColor());
        pg.arc(ARC_CENTER_X, ARC_CENTER_Y, arcSize, arcSize, ARC_START, ARC_START + ARC_RANGE);

        // Light ring of value
        pg.fill(isEnabled() ? ui.theme.getPrimaryColor() : ui.theme.getControlDisabledColor());
        pg.arc(ARC_CENTER_X, ARC_CENTER_Y, arcSize, arcSize, Math.min(valueStart, valueEnd), Math.max(valueStart, valueEnd));

        // Center tick mark for bipolar knobs
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
        float delta = dy / 100.f;
        if (mouseEvent.isShiftDown()) {
            delta /= 10;
        }
        this.dragValue = LXUtils.constrain(this.dragValue - delta, 0, 1);
        setNormalized(this.dragValue);
    }

}
