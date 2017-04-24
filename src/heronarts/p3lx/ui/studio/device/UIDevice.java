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

import heronarts.lx.LXComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.StringParameter;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIContainer;
import heronarts.p3lx.ui.UIMouseFocus;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UITextBox;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public abstract class UIDevice extends UI2dContainer implements UIMouseFocus {

    protected final static int HEIGHT = UIDeviceBin.HEIGHT - 2*UIDeviceBin.PADDING;
    protected static final int PADDING = 4;
    protected static final int MARGIN = UIDeviceBin.PADDING;
    protected static final int TITLE_MARGIN = 4;
    protected static final int TITLE_PADDING = 20;
    protected static final int ENABLED_BUTTON_PADDING = 18;
    protected static final int CLOSED_WIDTH = 20;

    private String title = "Device";
    private boolean titleParameter;
    private UITextBox titleBox;
    private final UIButton enabledButton;
    private boolean hasEnabledButton = false;

    private boolean expanded = true;
    private float expandedWidth;

    protected final UI2dContainer content;
    protected final LXComponent component;

    UIDevice(final UI ui, final LXComponent component, float w) {
        super(0, UIDeviceBin.PADDING, w, HEIGHT);
        setBackgroundColor(ui.theme.getWindowBackgroundColor());
        setBorderRounding(4);
        this.component = component;
        this.expandedWidth = w;

        this.component.controlSurfaceSempahore.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (component.controlSurfaceSempahore.getValue() > 0) {
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
        .addToContainer(this)
        .setVisible(false);

        this.titleBox = new UITextBox(TITLE_MARGIN, TITLE_MARGIN, this.width-2*TITLE_MARGIN, 12);
        this.titleBox
        .setBorder(false)
        .setBackground(false)
        .setFont(ui.theme.getLabelFont())
        .setFontColor(ui.theme.getControlTextColor())
        .setTextAlignment(PConstants.LEFT)
        .addToContainer(this)
        .setVisible(false);

        this.content = new UI2dContainer(PADDING, TITLE_PADDING + PADDING, w - 2*PADDING, height-TITLE_PADDING-2*PADDING) {
            @Override
            public void onResize() {
                setExpandedWidth(getWidth() + 2*PADDING);
            }
        };
        setContentTarget(this.content);
    }

    @Override
    public UI2dComponent addToContainer(UIContainer container, int index) {
        if (!(container instanceof UIDeviceBin)) {
            throw new UnsupportedOperationException("Can only add UIDevice to UIDeviceBin");
        }
        return super.addToContainer(container, index);
    }

    private UIDevice setExpandedWidth(float w) {
        this.expandedWidth = w;
        if (this.expanded) {
            setWidth(w);
        }
        this.titleBox.setWidth(this.hasEnabledButton ?
            this.width - TITLE_MARGIN - ENABLED_BUTTON_PADDING :
            this.width - 2*TITLE_MARGIN
        );
        return this;
    }

    protected UIDevice setEnabledButton(BooleanParameter p) {
        return setEnabledButton(p, false);
    }

    protected UIDevice setEnabledButton(BooleanParameter p, boolean isMomentary) {
        this.hasEnabledButton = true;
        this.enabledButton.setParameter(p).setMomentary(isMomentary).setVisible(true);
        this.titleBox.setX(ENABLED_BUTTON_PADDING);
        this.titleBox.setWidth(this.width - TITLE_MARGIN - ENABLED_BUTTON_PADDING);
        return this;
    }

    protected UIDevice editTitle() {
        if (this.expanded && this.titleParameter) {
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
        if (this.expanded != expanded) {
            this.expanded = expanded;
            this.titleBox.setVisible(expanded && this.titleParameter);
            this.content.setVisible(expanded);
            setWidth(expanded ? this.expandedWidth : CLOSED_WIDTH);
            redraw();
        }
        return this;
    }

    @Override
    public void drawFocus(UI ui, PGraphics pg) {}

    @Override
    public void onFocus() {
        setBackgroundColor(getUI().theme.getWindowFocusedBackgroundColor());
    }

    @Override
    public void onBlur() {
        setBackgroundColor(getUI().theme.getWindowBackgroundColor());
    }

    @Override
    public void onDraw(UI ui, PGraphics pg) {
        pg.textFont(ui.theme.getLabelFont());
        pg.fill(ui.theme.getLabelColor());
        pg.textAlign(PConstants.LEFT, PConstants.TOP);
        String titleString = this.titleParameter ? this.titleBox.getValue() : this.title;
        if (this.expanded) {
            if (!this.titleBox.isVisible()) {
                pg.text(titleString, (this.enabledButton != null) ? ENABLED_BUTTON_PADDING + 2 : 10, 6);
            }
            pg.stroke(0xff333333);
            pg.line(1, TITLE_PADDING, width-2, TITLE_PADDING);
        } else {
            pg.translate(5, height-6);
            pg.rotate(-PConstants.HALF_PI);
            pg.text(clipTextToWidth(pg, titleString, height-8), 0, 0);
            pg.rotate(PConstants.HALF_PI);
            pg.translate(-5, -height+6);
        }
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_SPACE || keyCode == java.awt.event.KeyEvent.VK_ENTER) {
            consumeKeyEvent();
            setExpanded(!this.expanded);
        }
        if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
            if (keyCode == java.awt.event.KeyEvent.VK_R) {
                consumeKeyEvent();
                editTitle();
            } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
                consumeKeyEvent();
                setExpanded(false);
            } else if (keyCode == java.awt.event.KeyEvent.VK_UP) {
                consumeKeyEvent();
                setExpanded(true);
            }
        }
    }

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        if (mouseEvent.getCount() == 2) {
            if (this.expanded) {
                if ((my < TITLE_PADDING) && ((this.enabledButton == null) || (mx > ENABLED_BUTTON_PADDING))) {
                    setExpanded(false);
                }
            } else {
                if ((this.enabledButton == null) || my > ENABLED_BUTTON_PADDING) {
                    setExpanded(true);
                }
            }
        }
    }
}