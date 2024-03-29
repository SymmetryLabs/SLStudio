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

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.Event;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import heronarts.lx.osc.LXOscEngine;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.color.LXColor;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIControlTarget;
import heronarts.p3lx.ui.UIModulationSource;
import heronarts.p3lx.ui.UIModulationTarget;

public abstract class UIParameterControl extends UIInputBox implements UIControlTarget, UIModulationTarget, UIModulationSource, LXParameterListener {

    protected final static int LABEL_MARGIN = 2;

    protected final static int LABEL_HEIGHT = 12;

    private final static int TEXT_MARGIN = 1;

    private boolean showValue = false;

    protected LXListenableNormalizedParameter parameter = null;

    protected LXParameter.Polarity polarity = LXParameter.Polarity.UNIPOLAR;

    protected boolean enabled = true;

    private String label = null;

    private boolean showLabel = true;

    protected boolean keyEditable = false;

    protected UIParameterControl(float x, float y, float w, float h) {
        super(x, y, w, h + LABEL_MARGIN + LABEL_HEIGHT);
        setBackground(false);
        setBorder(false);
    }

    @Override
    public UIParameterControl setEnabled(boolean enabled) {
        if (enabled != this.enabled) {
            this.enabled = enabled;
            redraw();
        }
        return this;
    }

    public static String getDescription(LXParameter parameter) {
        if (parameter != null) {
            String label = parameter.getLabel();
            String description = parameter.getDescription();
            if (description != null) {
                label += ": " + description;
            }
            String oscAddress = LXOscEngine.getOscAddress(parameter);
            if (oscAddress != null) {
                label += "  —  " + oscAddress;
            }
            return label;
        }
        return null;
    }

    @Override
    public String getDescription() {
        return getDescription(this.parameter);
    }

    public boolean isEnabled() {
        return (this.parameter != null) && this.enabled;
    }

    public UIParameterControl setShowLabel(boolean showLabel) {
        if (this.showLabel != showLabel) {
            this.showLabel = showLabel;
            if (this.showLabel) {
                setSize(this.width, this.height + LABEL_MARGIN + LABEL_HEIGHT);
            } else {
                setSize(this.width, this.height - LABEL_MARGIN - LABEL_HEIGHT);
            }
            redraw();
        }
        return this;
    }

    public UIParameterControl setLabel(String label) {
        if (this.label != label) {
            this.label = label;
            redraw();
        }
        return this;
    }

    @Override
    protected int getFocusColor(UI ui) {
        if (!isEnabled()) {
            return ui.theme.getControlDisabledColor();
        }
        return super.getFocusColor(ui);
    }

    public void onParameterChanged(LXParameter parameter) {
        redraw();
    }

    protected double getNormalized() {
        if (this.parameter != null) {
            if (this.parameter instanceof CompoundParameter) {
                return ((CompoundParameter) this.parameter).getBaseNormalized();
            }
            return this.parameter.getNormalized();
        }
        return 0;
    }

    protected UIParameterControl setNormalized(double normalized) {
        if (this.parameter != null) {
            this.parameter.setNormalized(normalized);
        }
        return this;
    }

    public LXListenableNormalizedParameter getParameter() {
        return this.parameter;
    }

    public UIParameterControl setPolarity(LXParameter.Polarity polarity) {
        if (this.polarity != polarity) {
            this.polarity = polarity;
            redraw();
        }
        return this;
    }

    public UIParameterControl setParameter(LXListenableNormalizedParameter parameter) {
        if (this.parameter != null) {
            this.parameter.removeListener(this);
        }
        this.parameter = parameter;
        if (this.parameter != null) {
            this.polarity = this.parameter.getPolarity();
            this.parameter.addListener(this);
        }
        redraw();
        return this;
    }

    private void setShowValue(boolean showValue) {
        if (showValue != this.showValue) {
            this.showValue = showValue;
            redraw();
        }
    }

    @Override
    protected String getValueString() {
        if (this.parameter != null) {
            if (this.parameter instanceof DiscreteParameter) {
                return ((DiscreteParameter) this.parameter).getOption();
            } else if (this.parameter instanceof BooleanParameter) {
                return ((BooleanParameter) this.parameter).isOn() ? "ON" : "OFF";
            } else if (this.parameter instanceof CompoundParameter) {
                return this.parameter.getFormatter().format(((CompoundParameter) this.parameter).getBaseValue());
            } else {
                return this.parameter.getFormatter().format(this.parameter.getValue());
            }
        }
        return "-";
    }

    protected String getLabelString() {
        if (this.parameter != null) {
            return this.parameter.getLabel();
        } else if (this.label != null) {
            return this.label;
        }
        return "-";
    }

    @Override
    protected boolean isValidCharacter(char keyChar) {
        return UIDoubleBox.isValidInputCharacter(keyChar);
    }

    @Override
    protected void saveEditBuffer() {
        if (this.parameter != null) {
            try {
                this.parameter.setValue(Double.parseDouble(this.editBuffer));
            } catch (NumberFormatException nfx) {}
        }
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        if (this.showLabel) {
            drawLabel(ui, pg);
        }
    }

    private void drawLabel(UI ui, PGraphics pg) {
        if (this.editing) {
            pg.fill(ui.theme.getControlBackgroundColor());
            pg.noStroke();
            pg.rect(0, this.height - LABEL_HEIGHT, this.width, LABEL_HEIGHT);
            pg.fill(ui.theme.getPrimaryColor());
            pg.textFont(ui.theme.getControlFont());
            pg.textAlign(PConstants.CENTER, PConstants.BOTTOM);
            pg.text(clipTextToWidth(pg, this.editBuffer, this.width - TEXT_MARGIN), this.width/2, this.height - TEXT_MARGIN);
        } else {
            String labelText = this.showValue ? getValueString() : getLabelString();
            pg.fill(ui.theme.getControlTextColor());
            pg.textAlign(PConstants.CENTER, PConstants.BOTTOM);
            pg.textFont(ui.theme.getControlFont());
            pg.text(clipTextToWidth(pg, labelText, this.width - TEXT_MARGIN), this.width/2, this.height - TEXT_MARGIN);
        }
    }

    private double getIncrement(Event inputEvent) {
        return inputEvent.isShiftDown() ? .1 : .02;
    }

    /**
     * Subclasses may optionally override to decrement value in response to arrows.
     * Decrement is invoked for the left or down arrow keys.
     *
     * @param keyEvent
     */
    @Override
    protected void decrementValue(KeyEvent keyEvent) {
        if (this.parameter != null) {
            consumeKeyEvent();
            if (this.parameter instanceof DiscreteParameter) {
                DiscreteParameter dp = (DiscreteParameter) this.parameter;
                dp.decrement(keyEvent.isShiftDown() ? dp.getRange() / 10 : 1);
            } else if (this.parameter instanceof BooleanParameter) {
                ((BooleanParameter)this.parameter).setValue(false);
            } else {
                setNormalized(getNormalized() - getIncrement(keyEvent));
            }
        }
    }

    /**
     * Subclasses may optionally override to decrement value in response to arrows.
     * Increment is invoked for the right or up keys.
     *
     * @param keyEvent
     */
    @Override
    protected void incrementValue(KeyEvent keyEvent) {
        if (this.parameter != null) {
            consumeKeyEvent();
            if (this.parameter instanceof DiscreteParameter) {
                DiscreteParameter dp = (DiscreteParameter) this.parameter;
                dp.increment(keyEvent.isShiftDown() ? dp.getRange() / 10 : 1);
            } else if (this.parameter instanceof BooleanParameter) {
                ((BooleanParameter)this.parameter).setValue(true);
            } else {
                setNormalized(getNormalized() + getIncrement(keyEvent));
            }
        }
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (!this.editing) {
            if ((keyCode == java.awt.event.KeyEvent.VK_SPACE) || (keyCode == java.awt.event.KeyEvent.VK_ENTER)) {
                consumeKeyEvent();
                setShowValue(true);
            } else if (this.enabled && keyEvent.isShiftDown() && keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                consumeKeyEvent();
                if (this.parameter != null) {
                    this.parameter.reset();
                }
            }
        }

        if (this.keyEditable && !keyEventConsumed()) {
            super.onKeyPressed(keyEvent, keyChar, keyCode);
        }
    }

    @Override
    public void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
        if ((keyCode == java.awt.event.KeyEvent.VK_SPACE) || (keyCode == java.awt.event.KeyEvent.VK_ENTER)) {
            consumeKeyEvent();
            setShowValue(false);
        }
    }

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        setShowValue(true);
    }

    @Override
    public void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {
        setShowValue(false);
    }

    @Override
    protected void onBlur() {
        setShowValue(false);
    }

    @Override
    public LXParameter getControlTarget() {
        return isMappable() ? this.parameter : null;
    }

    @Override
    public LXNormalizedParameter getModulationSource() {
        return isMappable() ? this.parameter : null;
    }

    @Override
    public CompoundParameter getModulationTarget() {
        if (this.parameter instanceof CompoundParameter) {
            return isMappable() ? (CompoundParameter) this.parameter : null;
        }
        return null;
    }

    /**
     * Given a base color for a control, return the color used to display the modulated component of its value.
     * Currently, just dims the base color.
     * @param baseColor
     */
     public int getModulatedValueColor(int baseColor) {
        int DIM_AMOUNT = 20;
        float h = LXColor.h(baseColor);
        float s = LXColor.s(baseColor);
        float b = LXColor.b(baseColor);
        float dimmedB = Math.max(0, b - DIM_AMOUNT);
        return LXColor.hsb(h, s, dimmedB);
    }

}
