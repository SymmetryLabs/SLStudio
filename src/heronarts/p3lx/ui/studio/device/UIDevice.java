/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
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

package heronarts.p3lx.ui.studio.device;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LXComponent;
import heronarts.lx.LXDeviceComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.StringParameter;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIContainer;
import heronarts.p3lx.ui.UIKeyFocus;
import heronarts.p3lx.ui.UIMouseFocus;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UISwitch;
import heronarts.p3lx.ui.component.UITextBox;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public abstract class UIDevice extends UI2dContainer implements UIMouseFocus, UIKeyFocus {

    protected final static int HEIGHT = 168;
    protected static final int PADDING = 4;
    protected static final int MARGIN = UIDeviceBin.PADDING;
    protected static final int TITLE_MARGIN = 4;
    protected static final int TITLE_PADDING = 20;
    protected static final int ENABLED_BUTTON_PADDING = 18;
    protected static final int DEVICE_BAR_WIDTH = 20;
    protected static final int CHEVRON_PADDING = 21;
    protected static final int MODULATOR_SIZE = 20;

    private String title = "Device";
    private boolean titleParameter;
    private UITextBox titleBox;
    private final UIButton enabledButton;
    private boolean hasEnabledButton = false;

    private final boolean hasModulators;
    private boolean modulatorsExpanded = false;
    private float modulatorY;
    protected final UI2dContainer modulatorContent;

    private boolean contentExpanded = true;

    protected final UI2dContainer content;
    protected final LXComponent component;

    UIDevice(final UI ui, final LXComponent component, float contentWidth) {
        super(0, 0, contentWidth + 2*PADDING + DEVICE_BAR_WIDTH, HEIGHT);
        setBackgroundColor(ui.theme.getDeviceBackgroundColor());
        setBorderRounding(4);
        this.component = component;

        this.component.controlSurfaceSemaphore.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (component.controlSurfaceSemaphore.getValue() > 0) {
                    setBorderColor(ui.theme.getSurfaceColor());
                } else {
                    setBorder(false);
                }
            }
        });

        this.enabledButton = new UIButton(PADDING, PADDING, 12, 12);
        this.enabledButton
        .setLabel("")
        .setBorderRounding(4)
        .setVisible(false);

        this.modulatorY = 0;

        // TODO(mcslee): add vertical orientation option to TextBox
        this.titleBox = new UITextBox(DEVICE_BAR_WIDTH + TITLE_MARGIN, TITLE_MARGIN, 0, 12);
        this.titleBox
        .setBorder(false)
        .setBackground(false)
        .setFont(ui.theme.getLabelFont())
        .setFontColor(ui.theme.getControlTextColor())
        .setTextAlignment(PConstants.LEFT)
        .setVisible(false);
        setTitleBoxWidth();

        if (this.component instanceof LXDeviceComponent) {
            this.hasModulators = true;
            this.modulatorContent = (UIDeviceModulators)
                new UIDeviceModulators(ui, (LXDeviceComponent) this.component, DEVICE_BAR_WIDTH, 0, 80, height)
                .setVisible(false);
        } else {
            this.hasModulators = false;
            this.modulatorContent = null;
        }

        this.content = new UI2dContainer(DEVICE_BAR_WIDTH + PADDING, PADDING, contentWidth, height - 2*PADDING);

        this.enabledButton.addToContainer(this);
        // this.titleBox.addToContainer(this);
        if (this.hasModulators) {
            this.modulatorContent.addToContainer(this);
        }
        setContentTarget(this.content);
    }

    @Override
    public UI2dComponent addToContainer(UIContainer container, int index) {
        if (!(container instanceof UIDeviceBin)) {
            throw new UnsupportedOperationException("Can only add UIDevice to UIDeviceBin");
        }
        return super.addToContainer(container, index);
    }

    private void setTitleBoxWidth() {
        this.titleBox.setWidth(this.width - TITLE_MARGIN - DEVICE_BAR_WIDTH - PADDING);
    }

    @Override
    protected void reflow() {
        float width = DEVICE_BAR_WIDTH;
        if (this.modulatorsExpanded) {
            width += this.modulatorContent.getWidth();
        }
        this.content.setX(width + PADDING);
        if (this.contentExpanded) {
            width += 2*PADDING + this.content.getWidth();
        }
        setWidth(width);
        setTitleBoxWidth();
    }

    protected UIDevice setEnabledButton(BooleanParameter p) {
        return setEnabledButton(p, false);
    }

    protected UIDevice setEnabledButton(BooleanParameter p, boolean isMomentary) {
        this.hasEnabledButton = true;
        this.enabledButton.setParameter(p).setMomentary(isMomentary).setVisible(true);
        this.modulatorY = this.enabledButton.getY() + this.enabledButton.getHeight();
        setTitleBoxWidth();
        return this;
    }

    protected UIDevice editTitle() {
        if (this.contentExpanded && this.titleParameter) {
            this.titleBox.focus();
            this.titleBox.edit();
        }
        return this;
    }

    protected UIDevice setTitle(StringParameter title) {
        this.titleParameter = true;
        this.titleBox.setParameter(title);
        this.titleBox.setVisible(true);
        title.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                redraw();
            }
        });
        return this;
    }

    protected UIDevice setTitle(String title) {
        if (!this.title.equals(title)) {
            this.title = title;
            redraw();
        }
        return this;
    }

    protected UIDevice setExpanded(boolean expanded) {
        if (this.contentExpanded != expanded) {
            this.contentExpanded = expanded;
            this.titleBox.setVisible(expanded && this.titleParameter);
            this.content.setVisible(expanded);
        }
        return this;
    }

    protected void buildDefaultControlUI(LXComponent component) {
        List<LXListenableNormalizedParameter> params = new ArrayList<LXListenableNormalizedParameter>();
        for (LXParameter parameter : component.getParameters()) {
            if (parameter instanceof BoundedParameter || parameter instanceof DiscreteParameter ||
                    parameter instanceof BooleanParameter
            ) {
                params.add((LXListenableNormalizedParameter) parameter);
            } // else, ignore unsupported types
        }
        int perRow;
        if (params.size() <= 3) {
            perRow = 1;
        } else {
            perRow = (int) Math.ceil(params.size() / 3.);
            if (perRow < 4) {
                perRow = 4;
            }
        }
        int ki = 0;
        for (LXListenableNormalizedParameter param : params) {
            float x = (ki % perRow) * (UIKnob.WIDTH + 4);
            float y = 7 + (ki / perRow) * (UIKnob.HEIGHT + 10);
            if (param instanceof BoundedParameter || param instanceof DiscreteParameter) {
                new UIKnob(x, y)
                        .setParameter(param)
                        .addToContainer(this);
            } else if (param instanceof BooleanParameter) {
                new UISwitch(x, y)
                        .setParameter(param)
                        .addToContainer(this);
            } else {
                // Hey developer: probably added a type in the for-loop above that wasn't handled down here.
                throw new RuntimeException("Cannot generate control, unsupported pattern parameter type: " + param.getClass());
            }

            ++ki;
        }
        setContentWidth(perRow * (UIKnob.WIDTH + 4) - 4);
    }

    @Override
    public void drawFocus(UI ui, PGraphics pg) {}

    @Override
    public void onFocus() {
        setBackgroundColor(getUI().theme.getDeviceFocusedBackgroundColor());
    }

    @Override
    public void onBlur() {
        setBackgroundColor(getUI().theme.getDeviceBackgroundColor());
    }

    @Override
    public void onDraw(UI ui, PGraphics pg) {
        pg.textFont(ui.theme.getDeviceFont());
        pg.fill(ui.theme.getLabelColor());
        pg.textAlign(PConstants.LEFT, PConstants.TOP);
        String titleString = this.titleParameter ? this.titleBox.getValue() : this.title;
        if (this.contentExpanded || this.modulatorsExpanded) {
            pg.stroke(0xff333333);
            pg.line(DEVICE_BAR_WIDTH, 1, DEVICE_BAR_WIDTH, this.height-2);
        }

        if (this.hasModulators) {
            pg.tint(this.modulatorsExpanded ? ui.theme.getPrimaryColor() : 0xff999999);
            pg.image(ui.theme.iconLfo, PADDING, this.modulatorY + PADDING);
            pg.noTint();
        }

        float tx = DEVICE_BAR_WIDTH / 2;
        float ty = this.height - CHEVRON_PADDING;
        float availableWidth = ty - MODULATOR_SIZE - (this.hasEnabledButton ? ENABLED_BUTTON_PADDING : 0);
        pg.translate(tx, ty);
        pg.rotate(-PConstants.HALF_PI);
        pg.textAlign(PConstants.LEFT, PConstants.CENTER);
        pg.text(clipTextToWidth(pg, titleString, availableWidth), 0, 0);
        pg.rotate(PConstants.HALF_PI);
        pg.translate(-tx, -ty);

        pg.noStroke();
        pg.fill(0xff333333);
        pg.beginShape();
        float x = PADDING + 1;
        float y = this.height - PADDING - 1;
        if (this.contentExpanded) {
            pg.vertex(x, y-10);
            pg.vertex(x+10, y-10);
            pg.vertex(x+10, y);
        } else {
            pg.vertex(x, y);
            pg.vertex(x+10, y);
            pg.vertex(x, y-10);
        }
        pg.endShape(PConstants.CLOSE);
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_SPACE || keyCode == java.awt.event.KeyEvent.VK_ENTER) {
            consumeKeyEvent();
            setExpanded(!this.contentExpanded);
        }
        if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
            if (keyCode == java.awt.event.KeyEvent.VK_R) {
                consumeKeyEvent();
                editTitle();
            }
        }
    }

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        if (mx < DEVICE_BAR_WIDTH){
            if (my > this.height - CHEVRON_PADDING) {
                setExpanded(!this.contentExpanded);
            } else if (this.hasModulators && my >= this.modulatorY && my < this.modulatorY + MODULATOR_SIZE) {
                this.modulatorsExpanded = !this.modulatorsExpanded;
                this.modulatorContent.setVisible(this.modulatorsExpanded);
            }
        }
    }
}
