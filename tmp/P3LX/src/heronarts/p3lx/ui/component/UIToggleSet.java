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
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIControlTarget;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIToggleSet extends UI2dComponent implements UIFocus, UIControlTarget, LXParameterListener {

    private String[] options = null;

    private int[] boundaries = null;

    private int value = -1;

    private boolean evenSpacing = false;

    private DiscreteParameter parameter = null;

    public UIToggleSet() {
        this(0, 0, 0, 0);
    }

    public UIToggleSet(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    @Override
    public String getDescription() {
        return UIParameterControl.getDescription(this.parameter);
    }

    @Override
    protected void onResize() {
        computeBoundaries();
    }

    public UIToggleSet setOptions(String[] options) {
        if (this.options != options) {
            this.options = options;
            this.value = 0;
            this.boundaries = new int[options.length];
            computeBoundaries();
            redraw();
        }
        return this;
    }

    public UIToggleSet setParameter(DiscreteParameter parameter) {
        if (this.parameter != parameter) {
            if (this.parameter != null) {
                this.parameter.removeListener(this);
            }
            this.parameter = parameter;
            if (this.parameter != null) {
                this.parameter.addListener(this);
                String[] options = this.parameter.getOptions();
                if (options != null) {
                    setOptions(options);
                }
                setValue(this.parameter.getValuei());
            }
        }
        return this;
    }

    public void onParameterChanged(LXParameter parameter) {
        if (parameter == this.parameter) {
            setValue(this.options[this.parameter.getValuei()]);
        }
    }

    private void computeBoundaries() {
        if (this.boundaries == null) {
            return;
        }
        if (this.evenSpacing) {
            for (int i = 0; i < this.boundaries.length; ++i) {
                this.boundaries[i] = (int) ((i + 1) * (this.width-1) / this.boundaries.length);
            }
        } else {
            int totalLength = 0;
            for (String s : this.options) {
                totalLength += s.length();
            }
            int lengthSoFar = 0;
            for (int i = 0; i < this.options.length; ++i) {
                lengthSoFar += this.options[i].length();
                this.boundaries[i] = (int) (lengthSoFar * (this.width-1) / totalLength);
            }
        }
    }

    public UIToggleSet setEvenSpacing() {
        if (!this.evenSpacing) {
            this.evenSpacing = true;
            computeBoundaries();
            redraw();
        }
        return this;
    }

    public int getValueIndex() {
        return this.value;
    }

    public String getValue() {
        return this.options[this.value];
    }

    public UIToggleSet setValue(String value) {
        for (int i = 0; i < this.options.length; ++i) {
            if (this.options[i] == value) {
                return setValue(i);
            }
        }

        // That string doesn't exist
        String optStr = "{";
        for (String str : this.options) {
            optStr += str + ",";
        }
        optStr = optStr.substring(0, optStr.length() - 1) + "}";
        throw new IllegalArgumentException("Not a valid option in UIToggleSet: "
                + value + " " + optStr);
    }

    public UIToggleSet setValue(int value) {
        if (this.value != value) {
            if (value < 0 || value >= this.options.length) {
                throw new IllegalArgumentException("Invalid index to setValue(): "
                        + value);
            }
            this.value = value;
            if (this.parameter != null) {
                this.parameter.setValue(value);
            }
            onToggle(getValue());
            redraw();
        }
        return this;
    }

    @Override
    public void onDraw(UI ui, PGraphics pg) {
        pg.stroke(ui.theme.getControlBorderColor());
        pg.fill(ui.theme.getControlBackgroundColor());
        pg.rect(0, 0, this.width-1, this.height-1);
        for (int b : this.boundaries) {
            pg.line(b, 1, b, this.height - 2);
        }

        pg.noStroke();
        pg.textAlign(PConstants.CENTER, PConstants.CENTER);
        pg.textFont(hasFont() ? getFont() : ui.theme.getControlFont());
        int leftBoundary = 0;

        for (int i = 0; i < this.options.length; ++i) {
            boolean isActive = (i == this.value);
            if (isActive) {
                pg.fill(ui.theme.getPrimaryColor());
                pg.rect(leftBoundary + 1, 1, this.boundaries[i] - leftBoundary - 2, this.height - 2);
            }
            pg.fill(isActive ? UI.WHITE : ui.theme.getControlTextColor());
            pg.text(this.options[i], (leftBoundary + this.boundaries[i]) / 2.f, this.height/2);
            leftBoundary = this.boundaries[i];
        }
    }

    protected void onToggle(String option) {
    }

    @Override
    protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        for (int i = 0; i < this.boundaries.length; ++i) {
            if (mx < this.boundaries[i]) {
                setValue(i);
                break;
            }
        }
    }

    @Override
    protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if ((keyCode == java.awt.event.KeyEvent.VK_LEFT)
                || (keyCode == java.awt.event.KeyEvent.VK_DOWN)) {
            consumeKeyEvent();
            setValue(LXUtils.constrain(this.value - 1, 0, this.options.length - 1));
        } else if ((keyCode == java.awt.event.KeyEvent.VK_RIGHT)
                || (keyCode == java.awt.event.KeyEvent.VK_UP)) {
            consumeKeyEvent();
            setValue(LXUtils.constrain(this.value + 1, 0, this.options.length - 1));
        }
    }

    @Override
    public LXParameter getControlTarget() {
        return this.parameter;
    }

}
