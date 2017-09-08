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
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXCompoundModulation;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.MouseEvent;

public class UIKnob extends UICompoundParameterControl implements UIFocus {

    public final static int KNOB_MARGIN = 6;
    public final static int KNOB_SIZE = 28;
    public final static int WIDTH = KNOB_SIZE + 2*KNOB_MARGIN;
    public final static int HEIGHT = KNOB_SIZE + LABEL_MARGIN + LABEL_HEIGHT;

    private final static float KNOB_INDENT = .4f;
    private final static int ARC_CENTER_X = WIDTH / 2;
    private final static int ARC_CENTER_Y = KNOB_SIZE / 2;
    private final static float ARC_START = PConstants.HALF_PI + KNOB_INDENT;
    private final static float ARC_RANGE = PConstants.TWO_PI - 2 * KNOB_INDENT;
    private final static float ARC_END = ARC_START + ARC_RANGE;

    private final static int BRIGHTEN = 20;

    public UIKnob(LXListenableNormalizedParameter parameter) {
        this();
        setParameter(parameter);
    }

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

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        float knobValue = (float) getNormalized();
        float baseValue = (float) getBaseNormalized();
        float valueEnd = ARC_START + knobValue * ARC_RANGE;
        float baseValueEnd = ARC_START + baseValue * ARC_RANGE;
        float valueStart;
        switch (this.polarity) {
        case BIPOLAR: valueStart = ARC_START + ARC_RANGE/2; break;
        default: case UNIPOLAR: valueStart = ARC_START; break;
        }

        float arcSize = KNOB_SIZE;
        pg.noStroke();
        pg.ellipseMode(PConstants.CENTER);

        // Modulations!
        if (this.parameter instanceof CompoundParameter) {
            CompoundParameter compound = (CompoundParameter) this.parameter;
            for (int i = compound.modulations.size()-1; i >= 0; --i) {
                LXCompoundModulation modulation = compound.modulations.get(i);
                registerModulation(modulation);

                float modStart, modEnd;
                switch (modulation.getPolarity()) {
                case BIPOLAR:
                    modStart = LXUtils.constrainf(baseValueEnd - modulation.range.getValuef() * ARC_RANGE, ARC_START, ARC_END);
                    modEnd = LXUtils.constrainf(baseValueEnd + modulation.range.getValuef() * ARC_RANGE, ARC_START, ARC_END);
                    break;
                default:
                case UNIPOLAR:
                    modStart = baseValueEnd;
                    modEnd = LXUtils.constrainf(modStart + modulation.range.getValuef() * ARC_RANGE, ARC_START, ARC_END);
                    break;
                }

                // Light ring of value
                ColorParameter modulationColor = modulation.color;
                int modColor = ui.theme.getControlDisabledColor();
                int modColorInv = modColor;
                if (isEnabled()) {
                    modColor = modulationColor.getColor();
                    modColorInv = LXColor.hsb(LXColor.h(modColor), 50, 75);
                }
                pg.fill(modColor);
                switch (modulation.getPolarity()) {
                case BIPOLAR:
                    if (modEnd >= modStart) {
                        pg.arc(ARC_CENTER_X, ARC_CENTER_Y, arcSize, arcSize, baseValueEnd, Math.min(ARC_END, modEnd+.1f));
                        pg.fill(modColorInv);
                        pg.arc(ARC_CENTER_X, ARC_CENTER_Y, arcSize, arcSize, Math.max(ARC_START, modStart-.1f), baseValueEnd);
                    } else {
                        pg.arc(ARC_CENTER_X, ARC_CENTER_Y, arcSize, arcSize, Math.max(ARC_START, modEnd-.1f), baseValueEnd);
                        pg.fill(modColorInv);
                        pg.arc(ARC_CENTER_X, ARC_CENTER_Y, arcSize, arcSize, baseValueEnd, Math.min(ARC_END, modStart+.1f));
                    }
                    break;
                case UNIPOLAR:
                    pg.arc(ARC_CENTER_X, ARC_CENTER_Y, arcSize, arcSize, Math.max(ARC_START, Math.min(modStart, modEnd)-.1f), Math.min(ARC_END, Math.max(modStart, modEnd)+.1f));
                    break;
                }
                arcSize -= 3;
                pg.fill(ui.theme.getDeviceBackgroundColor());
                pg.ellipse(ARC_CENTER_X, ARC_CENTER_Y, arcSize, arcSize);
                arcSize -= 1;
                if (arcSize < 6) {
                    break;
                }

            }
        }

        // Outer fill
        pg.noStroke();
        pg.fill(ui.theme.getControlBackgroundColor());
        pg.arc(ARC_CENTER_X, ARC_CENTER_Y, arcSize, arcSize, ARC_START, ARC_END);

        // Value indication
        int baseColor;
        int valueColor;
        if (isEnabled()) {
            int dim = ui.theme.getPrimaryColor();
            int bright = LXColor.hsb(LXColor.h(dim), LXColor.s(dim), LXColor.b(dim) + BRIGHTEN);
            baseColor = bright;
            valueColor = dim;
        } else {
            int disabled = ui.theme.getControlDisabledColor();
            baseColor = disabled;
            valueColor = disabled;
        }


        pg.fill(baseColor);
        pg.arc(ARC_CENTER_X, ARC_CENTER_Y, arcSize, arcSize, Math.min(valueStart, baseValueEnd), Math.max(valueStart, baseValueEnd));
        pg.fill(valueColor);
        pg.arc(ARC_CENTER_X, ARC_CENTER_Y, arcSize, arcSize, Math.min(baseValueEnd, valueEnd), Math.max(baseValueEnd, valueEnd));

        // Center tick mark for bipolar knobs
        if (this.polarity == LXParameter.Polarity.BIPOLAR) {
            pg.stroke(0xff333333);
            pg.line(ARC_CENTER_X, ARC_CENTER_Y, ARC_CENTER_X, ARC_CENTER_Y - arcSize/2);
        }

        // Center dot
        int dotSize = 8;
        pg.noStroke();
        pg.fill(0xff333333);
        pg.ellipse(ARC_CENTER_X, ARC_CENTER_Y, dotSize, dotSize);

        super.onDraw(ui,  pg);
    }

    private double dragValue;

    @Override
    protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        super.onMousePressed(mouseEvent, mx, my);
        this.dragValue = getBaseNormalized();
        if ((this.parameter != null) && (mouseEvent.getCount() > 1)) {
            LXCompoundModulation modulation = getModulation(mouseEvent.isShiftDown());
            if (modulation != null && (mouseEvent.isControlDown() || mouseEvent.isMetaDown())) {
                modulation.range.reset();
            } else {
                this.parameter.reset();
            }
        }
    }

    private LXCompoundModulation getModulation(boolean secondary) {
        if (this.parameter != null && this.parameter instanceof CompoundParameter) {
            CompoundParameter compound = (CompoundParameter) this.parameter;
            int size = compound.modulations.size();
            if (size > 0) {
                if (secondary && (size > 1)) {
                    return compound.modulations.get(size-2);
                } else {
                    return compound.modulations.get(size-1);
                }
            }
        }
        return null;
    }

    @Override
    protected void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
        if (!isEnabled()) {
            return;
        }

        float delta = dy / 100.f;
        LXCompoundModulation modulation = getModulation(mouseEvent.isShiftDown());
        if (modulation != null && (mouseEvent.isMetaDown() || mouseEvent.isControlDown())) {
            modulation.range.setValue(modulation.range.getValue() - delta);
        } else {
            if (mouseEvent.isShiftDown()) {
                delta /= 10;
            }
            this.dragValue = LXUtils.constrain(this.dragValue - delta, 0, 1);
            setNormalized(this.dragValue);
        }
    }

}
