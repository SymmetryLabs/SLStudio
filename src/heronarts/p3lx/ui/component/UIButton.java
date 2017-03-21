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

import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.UIControlTarget;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIButton extends UI2dComponent implements UIControlTarget, UIFocus {

    protected boolean active = false;
    protected boolean isMomentary = false;

    protected int inactiveColor = UI.get().theme.getControlBackgroundColor();
    protected int activeColor = UI.get().theme.getPrimaryColor();

    private String activeLabel = "";
    private String inactiveLabel = "";

    private BooleanParameter parameter = null;

    protected long exactToggleTime = -1;

    private final LXParameterListener parameterListener = new LXParameterListener() {
        public void onParameterChanged(LXParameter p) {
            setActive(parameter.isOn());
        }
    };

    public UIButton() {
        this(0, 0, 0, 0);
    }

    public UIButton(float x, float y, float w, float h) {
        super(x, y, w, h);
        setBorderColor(UI.get().theme.getControlBorderColor());
        setFontColor(UI.get().theme.getControlTextColor());
        setBackgroundColor(this.inactiveColor);
    }

    public BooleanParameter getParameter() {
        return this.parameter;
    }

    public UIButton setParameter(BooleanParameter parameter) {
        if (this.parameter != null) {
            this.parameter.removeListener(this.parameterListener);
        }
        this.parameter = parameter;
        if (parameter != null) {
            parameter.addListener(this.parameterListener);
            setActive(parameter.isOn());
        }
        return this;
    }

    public UIButton setMomentary(boolean momentary) {
        this.isMomentary = momentary;
        return this;
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        String label = this.active ? this.activeLabel : this.inactiveLabel;
        if ((label != null) && (label.length() > 0)) {
            pg.fill(this.active ? UI.WHITE : getFontColor());
            pg.textFont(hasFont() ? getFont() : ui.theme.getControlFont());
            if (this.textAlignVertical == PConstants.CENTER) {
                pg.textAlign(PConstants.CENTER, PConstants.CENTER);
                pg.text(label, this.width / 2 + this.textOffsetX, this.height / 2 - 2 + this.textOffsetY);
            } else {
                pg.textAlign(PConstants.CENTER);
                pg.text(label, this.width / 2 + this.textOffsetX, (int) (this.height * .75) + this.textOffsetY);
            }
        }
    }

    @Override
    protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        this.exactToggleTime = mouseEvent.getMillis();
        setActive(this.isMomentary ? true : !this.active);
    }

    @Override
    protected void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {
        if (this.isMomentary) {
            this.exactToggleTime = mouseEvent.getMillis();
            setActive(false);
        }
    }

    @Override
    protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if ((keyCode == java.awt.event.KeyEvent.VK_SPACE) || (keyCode == java.awt.event.KeyEvent.VK_ENTER)) {
            this.exactToggleTime = keyEvent.getMillis();
            setActive(this.isMomentary ? true : !this.active);
            consumeKeyEvent();
        }
    }

    @Override
    protected void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
        if ((keyCode == java.awt.event.KeyEvent.VK_SPACE) || (keyCode == java.awt.event.KeyEvent.VK_ENTER)) {
            if (this.isMomentary) {
                this.exactToggleTime = keyEvent.getMillis();
                setActive(false);
                consumeKeyEvent();
            }
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public UIButton setActive(boolean active) {
        if (this.active != active) {
            if (this.parameter != null) {
                this.parameter.setValue(active);
            }
            setBackgroundColor(active ? this.activeColor : this.inactiveColor);
            onToggle(this.active = active);
            redraw();
        }
        return this;
    }

    public UIButton toggle() {
        return setActive(!this.active);
    }

    /**
     * Subclasses may override this to handle changes to the button's state
     *
     * @param active Whether button is active
     */
    protected void onToggle(boolean active) {
    }

    public UIButton setActiveColor(int activeColor) {
        if (this.activeColor != activeColor) {
            this.activeColor = activeColor;
            if (this.active) {
                setBackgroundColor(activeColor);
            }
        }
        return this;
    }

    public UIButton setInactiveColor(int inactiveColor) {
        if (this.inactiveColor != inactiveColor) {
            this.inactiveColor = inactiveColor;
            if (!this.active) {
                setBackgroundColor(inactiveColor);
            }
        }
        return this;
    }

    public UIButton setLabel(String label) {
        setActiveLabel(label);
        setInactiveLabel(label);
        return this;
    }

    public UIButton setActiveLabel(String activeLabel) {
        if (!this.activeLabel.equals(activeLabel)) {
            this.activeLabel = activeLabel;
            if (this.active) {
                redraw();
            }
        }
        return this;
    }

    public UIButton setInactiveLabel(String inactiveLabel) {
        if (!this.inactiveLabel.equals(inactiveLabel)) {
            this.inactiveLabel = inactiveLabel;
            if (!this.active) {
                redraw();
            }
        }
        return this;
    }

    @Override
    public LXParameter getControlTarget() {
        return isMappable() ? this.parameter : null;
    }
}
