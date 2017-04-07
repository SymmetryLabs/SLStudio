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
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PGraphics;
import processing.event.MouseEvent;

public class UISlider extends UICompoundParameterControl implements UIFocus {

    public enum Direction {
        HORIZONTAL, VERTICAL
    };

    private final Direction direction;

    private static final float HANDLE_SIZE = 6;
    private static final int HANDLE_ROUNDING = 2;
    private static final float PADDING = 2;
    private static final float GROOVE = 4;

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
    protected void onDraw(UI ui, PGraphics pg) {
        pg.noStroke();
        pg.fill(ui.theme.getControlBackgroundColor());

        int controlColor = this.hasFillColor ? this.fillColor :
            (isEnabled() ? ui.theme.getPrimaryColor() : ui.theme.getControlDisabledColor());

        switch (this.direction) {
        case HORIZONTAL:
            pg.rect(PADDING, this.handleHeight / 2 - GROOVE/2, this.width - 2*PADDING, GROOVE);
            pg.fill(controlColor);

            int fillX, fillWidth;
            switch (this.polarity) {
            case BIPOLAR:
                fillX = (int) (this.width / 2);
                fillWidth = (int) ((getNormalized() - 0.5) * (this.width - 2*PADDING));
                break;
            default:
            case UNIPOLAR:
                fillX = (int) PADDING;
                fillWidth = (int) ((this.width - 2*PADDING) * getNormalized());
                break;
            }
            pg.rect(fillX, this.handleHeight / 2 - GROOVE/2, fillWidth, GROOVE);
            pg.fill(0xff5f5f5f);
            pg.stroke(ui.theme.getControlBorderColor());
            pg.rect((int) (PADDING + getNormalized() * (this.width - 2*PADDING - HANDLE_SIZE)), PADDING,
                    HANDLE_SIZE, this.handleHeight - 2*PADDING, HANDLE_ROUNDING);
            break;
        case VERTICAL:
            pg.rect(this.width / 2 - GROOVE/2, PADDING, GROOVE, this.handleHeight - 2*PADDING);
            pg.fill(controlColor);
            int fillY;
            int fillSize;
            switch (this.polarity) {
            case BIPOLAR:
                fillY = (int) (this.handleHeight / 2);
                fillSize = (int) ((0.5 - getNormalized()) * (this.handleHeight - 2*PADDING));
                break;
            default:
            case UNIPOLAR:
                fillSize = (int) (getNormalized() * (this.handleHeight - 2*PADDING));
                fillY = (int) (this.handleHeight - PADDING - fillSize);
                break;
            }
            pg.rect(this.width / 2 - GROOVE/2, fillY, GROOVE, fillSize);
            pg.fill(0xff5f5f5f);
            pg.stroke(ui.theme.getControlBorderColor());
            pg.rect(PADDING, (int) (PADDING + (1 - getNormalized())
                    * (this.handleHeight - 2*PADDING - HANDLE_SIZE)), this.width - 2*PADDING, HANDLE_SIZE, HANDLE_ROUNDING);
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
                float delta = dv / (dim - HANDLE_SIZE);
                if (mouseEvent.isShiftDown()) {
                    delta /= 10;
                }
                setNormalized(LXUtils.constrain(getNormalized() + delta, 0, 1));
            }
        }
    }
}
