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
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXCompoundModulation;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PGraphics;
import processing.event.MouseEvent;

public class UISlider extends UICompoundParameterControl implements UIFocus {

    public enum Direction {
        HORIZONTAL, VERTICAL
    };

    private final Direction direction;

    private static final int HANDLE_SIZE = 6;
    private static final int HANDLE_ROUNDING = 2;
    private static final int PADDING = 2;
    private static final int GROOVE = 4;

    private final static int BRIGHTEN = 20;
    private final static int HANDLE_COLOR = 0xff5f5f5f;


    private final float handleHeight;

    private boolean hasFillColor = false;

    private int fillColor = 0;;

    public UISlider() {
        this(0, 0, 0, 0);
    }

    public UISlider(float x, float y, float w, float h) {
        this(Direction.HORIZONTAL, x, y, w, h);
    }

    public UISlider(Direction direction, float x, float y, float w, float h) {
        super(x, y, w, h);
        this.keyEditable = true;
        enableImmediateEdit(true);
        this.direction = direction;
        this.handleHeight = h;
    }

    public UISlider resetFillColor() {
        if (this.hasFillColor) {
            this.hasFillColor = false;
            redraw();
        }
        return this;
    }

    public UISlider setFillColor(int color) {
        if (!this.hasFillColor || (this.fillColor != color)) {
            this.hasFillColor = true;
            this.fillColor = color;
            redraw();
        }
        return this;
    }

    @Override
    @SuppressWarnings("fallthrough")
    protected void onDraw(UI ui, PGraphics pg) {


        double norm = getNormalized();
        double base = getBaseNormalized();
        boolean modulated = base != norm;
        int baseHandleEdge;
        int modHandleEdge;
        float grooveDim;
        switch (this.direction) {
        case HORIZONTAL:
            baseHandleEdge = (int) Math.round(PADDING + base * (this.width - 2*PADDING - HANDLE_SIZE));
            modHandleEdge = (int) Math.round(PADDING + norm * (this.width - 2*PADDING - HANDLE_SIZE));
            grooveDim = this.width - 2*PADDING;
            break;
        default:
        case VERTICAL:
            baseHandleEdge = (int) Math.round(PADDING + (1 - base) * (this.handleHeight - 2*PADDING - HANDLE_SIZE));
            modHandleEdge =  (int) Math.round(PADDING + (1 - norm) * (this.handleHeight - 2*PADDING - HANDLE_SIZE));

            grooveDim = this.handleHeight - 2*PADDING;
            break;
        }
        int baseHandleCenter = baseHandleEdge + 1 + HANDLE_SIZE/2;
        int modHandleCenter = modHandleEdge + 1 + HANDLE_SIZE/2;



        // Modulations!
        if (this.parameter instanceof CompoundParameter) {
            CompoundParameter compound = (CompoundParameter) this.parameter;
            for (int i = 0; i < compound.modulations.size() && i < 3; ++i) {
                LXCompoundModulation modulation = compound.modulations.get(i);
                int modColor = ui.theme.getControlDisabledColor();
                int modColorInv = modColor;
                if (isEnabled()) {
                    modColor = modulation.color.getColor();
                    modColorInv = LXColor.hsb(LXColor.h(modColor), 50, 75);
                }
                pg.strokeWeight(2);
                boolean drawn = false;
                switch (this.direction) {
                case HORIZONTAL:
                    float y = this.handleHeight/2 - GROOVE/2 - 2*(i+1);
                    if (y > 0) {
                        drawn = true;
                        float xw = grooveDim * modulation.range.getValuef();
                        float xf;
                        switch (modulation.getPolarity()) {
                        case BIPOLAR:
                            pg.stroke(modColorInv);
                            xf = LXUtils.constrainf(baseHandleCenter - xw, PADDING, PADDING + grooveDim - 1);
                            pg.line(baseHandleCenter, y, xf, y);
                            // Pass-thru
                        case UNIPOLAR:
                            pg.stroke(modColor);
                            xf = LXUtils.constrainf(baseHandleCenter + xw, PADDING, PADDING + grooveDim - 1);
                            pg.line(baseHandleCenter, y, xf, y);
                            break;
                        }
                    }
                    break;
                case VERTICAL:
                    float x = this.width/2 + GROOVE/2 + 2*(i+1);
                    if (x < this.width-1) {
                        drawn = true;
                        float yw =  grooveDim * modulation.range.getValuef();
                        float yf;
                        switch (modulation.getPolarity()) {
                        case BIPOLAR:
                            pg.stroke(modColorInv);
                            yf = LXUtils.constrainf(baseHandleCenter + yw, PADDING, PADDING + grooveDim - 1);
                            pg.line(x, baseHandleCenter, x, yf);
                            // Pass thru
                        case UNIPOLAR:
                            pg.stroke(modColor);
                            yf = LXUtils.constrainf(baseHandleCenter - yw, PADDING, PADDING + grooveDim - 1);
                            pg.line(x, baseHandleCenter, x, yf);
                            break;
                        }
                    }
                    break;
                }
                if (drawn) {
                    registerModulation(modulation);
                }
            }
        }

        int baseColor;
        int valueColor;
        int originalColor;
        if (isEnabled()) {
            int dim = this.hasFillColor ? this.fillColor : ui.theme.getPrimaryColor();
            int bright = LXColor.hsb(LXColor.h(dim), LXColor.s(dim), LXColor.b(dim) + BRIGHTEN);
         
            baseColor = bright;
            valueColor = dim;
            originalColor = dim;
        } else {
            int disabled = ui.theme.getControlDisabledColor();
            baseColor = disabled;
            valueColor = disabled;
            originalColor = disabled;
        }


        pg.strokeWeight(1);
        pg.noStroke();
        pg.fill(ui.theme.getControlBackgroundColor());

    

        switch (this.direction) {
        case HORIZONTAL:
            // Dark groove
            pg.rect(PADDING, this.handleHeight / 2 - GROOVE/2, this.width - 2*PADDING, GROOVE);

            int fillX, modFillX, fillWidth, modFillWidth;
            switch (this.polarity) {
            case BIPOLAR:
                fillX = (int) (this.width / 2);
                fillWidth = (int) ((base - 0.5) * (this.width - 2*PADDING));
                modFillX = fillX + fillWidth;
                modFillWidth = (int) ((norm - base)* (this.width - 2*PADDING));
                break;
            default:
            case UNIPOLAR:
                fillX = PADDING;
                fillWidth = (int) ((this.width - 2*PADDING) * base);
                modFillX = fillX + fillWidth;
                modFillWidth = (int) ((this.width - 2*PADDING) * (norm - base));
                break;
            }

            float topY = this.handleHeight / 2 - GROOVE/2;

            // Groove value fill
            pg.fill(modulated ? baseColor : originalColor);
            pg.rect(fillX, topY, fillWidth, GROOVE);
            if (modulated) {
                pg.fill(valueColor);
                pg.rect(modFillX, topY, modFillWidth, GROOVE);
            }

            // If we're modulating accross the center, draw a small divider
            if ((base > 0.5 && norm < 0.5) || (base < 0.5 && norm > 0.5)) {
                float centerX = this.width / 2; 
                pg.stroke(ui.theme.getControlBackgroundColor());
                pg.strokeWeight(1);
                pg.line(centerX, topY, centerX, topY + GROOVE);
            }


            // Handle
            pg.fill(HANDLE_COLOR);
            pg.stroke(ui.theme.getControlBorderColor());
            pg.rect(baseHandleEdge, PADDING, HANDLE_SIZE, this.handleHeight - 2*PADDING, HANDLE_ROUNDING);
            break;
        case VERTICAL:
            pg.rect(this.width / 2 - GROOVE/2, PADDING, GROOVE, this.handleHeight - 2*PADDING);
            int fillY;
            int modFillY;
            int fillSize;
            int modFillSize;
            switch (this.polarity) {
            case BIPOLAR:
                fillY = (int) (this.handleHeight / 2);
                modFillY = fillY;
                fillSize = (int) ((0.5 - base) * (this.handleHeight - 2*PADDING));
                modFillSize = (int) ((0.5 - norm) * (this.handleHeight - 2*PADDING));
                break;
            default:
            case UNIPOLAR:
                fillSize = (int) (base * (this.handleHeight - 2*PADDING));
                fillY = (int) (this.handleHeight - PADDING - fillSize);
                modFillSize = (int) ((norm - base) * (this.handleHeight - 2*PADDING));
                modFillY = fillY - modFillSize;
                break;
            }


            pg.fill(modulated ? baseColor : originalColor);
            pg.rect(this.width / 2 - GROOVE/2, fillY, GROOVE, fillSize);
            if (modulated) {
                pg.fill(valueColor);
                pg.rect(this.width / 2 - GROOVE/2, modFillY, GROOVE, modFillSize);
            }

            pg.fill(0xff5f5f5f);
            pg.stroke(ui.theme.getControlBorderColor());
            pg.rect(PADDING, baseHandleEdge, this.width - 2*PADDING, HANDLE_SIZE, HANDLE_ROUNDING);
            break;
        }

        super.onDraw(ui, pg);
    }

    private float doubleClickMode = 0;
    private float doubleClickP = 0;

    @Override
    protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        super.onMousePressed(mouseEvent, mx, my);
        float mp, dim;
        boolean isVertical = false;
        switch (this.direction) {
        case VERTICAL:
            mp = my;
            dim = this.handleHeight;
            isVertical = true;
            break;
        default:
        case HORIZONTAL:
            mp = mx;
            dim = this.width;
            break;
        }
        if ((mouseEvent.getCount() > 1) && Math.abs(mp - this.doubleClickP) < 3) {
            setNormalized(this.doubleClickMode);
        }
        this.doubleClickP = mp;
        if (mp < dim * .25) {
            this.doubleClickMode = isVertical ? 1 : 0;
        } else if (mp > dim * .75) {
            this.doubleClickMode = isVertical ? 0 : 1;
        } else {
            this.doubleClickMode = 0.5f;
        }
    }

    @Override
    protected void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {
        super.onMouseReleased(mouseEvent, mx, my);
        this.editing = false;
    }

    private LXCompoundModulation getModulation(boolean secondary) {
        if (this.parameter != null && this.parameter instanceof CompoundParameter) {
            CompoundParameter compound = (CompoundParameter) this.parameter;
            int size = compound.modulations.size();
            if (size > 0) {
                if (secondary && (size > 1)) {
                    return compound.modulations.get(1);
                } else {
                    return compound.modulations.get(0);
                }
            }
        }
        return null;
    }

    @Override
    protected void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
        if (isEnabled()) {
            float dv, dim;
            boolean valid;
            switch (this.direction) {
            case VERTICAL:
                dv = -dy;
                dim = this.handleHeight;
                valid = (my > 0 && dy > 0) || (my < dim && dy < 0);
                break;
            default:
            case HORIZONTAL:
                dv = dx;
                dim = this.width;
                valid = (mx > 0 && dx > 0) || (mx < dim && dx < 0);
                break;
            }
            if (valid) {
                float delta = dv / (dim - HANDLE_SIZE - 2*PADDING);
                LXCompoundModulation modulation = getModulation(mouseEvent.isShiftDown());
                if (modulation != null && (mouseEvent.isMetaDown() || mouseEvent.isControlDown())) {
                    modulation.range.setValue(modulation.range.getValue() + delta);
                } else {
                    if (mouseEvent.isShiftDown()) {
                        delta /= 10;
                    }
                    setNormalized(LXUtils.constrain(getBaseNormalized() + delta, 0, 1));
                }
            }
        }
    }
    
}
