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

package heronarts.p3lx.ui.studio.modulation;

import heronarts.lx.LX;
import heronarts.lx.LXMappingEngine;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.LXPeriodicModulator;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.LXParameterModulation;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.UIModulationSource;
import heronarts.p3lx.ui.UIMouseFocus;
import heronarts.p3lx.ui.UIObject;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIColorBox;
import heronarts.p3lx.ui.component.UIParameterLabel;
import heronarts.p3lx.ui.component.UISlider;
import heronarts.p3lx.ui.component.UITextBox;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public abstract class UIModulator extends UI2dContainer implements UIMouseFocus {

    protected static final int PADDING = 4;
    private static final int MODULATION_SPACING = 2;
    protected static final int TITLE_X = 18;
    protected static final int CONTENT_Y = 20;
    protected static final int MAP_WIDTH = 24;
    protected static final int LOOP_WIDTH = MAP_WIDTH;
    protected static final int COLOR_WIDTH = 10;

    private final UI ui;
    private final LX lx;
    public final LXParameter parameter;
    public final LXModulator modulator;
    protected final UI2dComponent toggleTarget;
    protected final UITextBox title;
    private final UI2dContainer content;
    private final UI2dContainer modulations;
    private final ColorParameter color;

    private float expandedHeight;
    private boolean expanded = true;

    public UIModulator(final UI ui, final LX lx, final LXParameter parameter, float x, float y, float w, float h) {
        super(x, y, w, CONTENT_Y + h + PADDING);
        this.expandedHeight = this.height;
        this.ui = ui;
        this.lx = lx;
        this.parameter = parameter;
        this.modulator = (this.parameter instanceof LXModulator) ? (LXModulator) this.parameter : null;
        setBackgroundColor(ui.theme.getWindowBackgroundColor());
        setBorderRounding(4);

        float colorX = this.width - 2*PADDING - MAP_WIDTH - COLOR_WIDTH;
        UIButton loopingButton = null;

        if (this.modulator != null) {
            this.color = this.modulator.clr;

            new UIButton(PADDING, PADDING, 12, 12)
            .setParameter(modulator.running)
            .setLabel("")
            .setBorderRounding(4)
            .addToContainer(this);

            if (this.modulator instanceof LXPeriodicModulator) {
                loopingButton = new UIButton(this.width - 2*(PADDING + MAP_WIDTH), PADDING-1, MAP_WIDTH, 14);
                colorX -= PADDING + LOOP_WIDTH;
            }

            this.title = new UITextBox(TITLE_X, PADDING, colorX - TITLE_X - PADDING, 12);
            this.title
            .setParameter(modulator.label)
            .setBorder(false)
            .setBackground(false)
            .setFont(ui.theme.getLabelFont())
            .setFontColor(ui.theme.getControlTextColor())
            .setTextAlignment(PConstants.LEFT)
            .addToContainer(this);
            this.toggleTarget = this.title;
        } else {
            this.color = new ColorParameter("Modulator", LXColor.hsb(360*Math.random(), 100, 100));
            this.title = null;
            this.toggleTarget = new UIParameterLabel(2, 1, colorX - PADDING - 2, 12).setParameter(parameter).addToContainer(this);

            this.color.addListener(new LXParameterListener() {
                public void onParameterChanged(LXParameter p) {
                    for (UIObject uiModulation : modulations) {
                        ((UIModulation) uiModulation).modulation.clr.setColor(color.getColor());
                    }
                }
            });
        }

        new UIColorBox(ui, this.color, colorX, PADDING+1, COLOR_WIDTH, COLOR_WIDTH)
        .addToContainer(this);

        if (loopingButton != null) {
            loopingButton
            .setLabel("\u21BA")
            .setParameter(((LXPeriodicModulator) modulator).looping)
            .addToContainer(this);
        }

        final UIButton mapButton = new UIButton(this.width - PADDING - MAP_WIDTH, PADDING-1, MAP_WIDTH, 14) {
            @Override
            public void onToggle(boolean on) {
                ui.mapModulationSource(on ? getModulationSourceUI() : null);
            }
        };
        mapButton
        .setLabel("→")
        .addToContainer(this);

        lx.engine.mapping.mode.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (lx.engine.mapping.getMode() == LXMappingEngine.Mode.OFF || lx.engine.mapping.getMode() == LXMappingEngine.Mode.MIDI) {
                    mapButton.setActive(false);
                }
            }
        });

        this.content = new UI2dContainer(PADDING, CONTENT_Y, width-2*PADDING, h) {
            @Override
            public void onResize() {
                recomputeHeight();
            }
        };
        setContentTarget(this.content);

        this.modulations = new UI2dContainer(PADDING, this.content.getY() + this.content.getHeight() + PADDING, width-2*PADDING, 0) {
            @Override
            public void onResize() {
                recomputeHeight();
            }
        };
        this.modulations.setBackgroundColor(ui.theme.getDarkBackgroundColor());
        this.modulations.setLayout(UI2dContainer.Layout.VERTICAL).setPadding(MODULATION_SPACING).setChildMargin(4*MODULATION_SPACING);
        addTopLevelComponent(this.modulations);
    }

    private void recomputeHeight() {
        float mappingHeight = this.modulations.getHeight();
        if (mappingHeight > 0) {
            mappingHeight += PADDING;
        }
        UIModulator.this.setHeight(expandedHeight = CONTENT_Y + this.content.getHeight() + mappingHeight + PADDING);
    }

    public UIModulator addModulation(LXParameterModulation modulation) {
        if (this.modulator == null) {
            modulation.clr.setColor(this.color.getColor());
        }
        new UIModulation(this.ui, modulation, 0, 0, this.modulations.getContentWidth()).addToContainer(this.modulations);
        return this;
    }

    public UIModulator removeModulation(LXParameterModulation modulation) {
        for (UIObject child : this.modulations) {
            if (child instanceof UIModulation) {
                UIModulation uiModulation = (UIModulation) child;
                if (uiModulation.modulation == modulation) {
                    uiModulation.removeFromContainer();
                }
            }
        }
        return this;
    }

    protected abstract UIModulationSource getModulationSourceUI();

    private void toggleExpanded() {
        this.expanded = !this.expanded;
        this.content.setVisible(this.expanded);
        this.modulations.setVisible(this.expanded);
        setHeight(this.expanded ? this.expandedHeight : CONTENT_Y);
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
            if (this.modulator != null && keyCode == java.awt.event.KeyEvent.VK_R) {
                consumeKeyEvent();
                this.title.focus();
                this.title.edit();
            } else if (keyCode == java.awt.event.KeyEvent.VK_D || keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                if (this.modulator != null) {
                    this.lx.engine.modulation.removeModulator(this.modulator);
                } else {
                    for (UIObject uiModulation : this.modulations) {
                        this.lx.engine.modulation.removeModulation(((UIModulation) uiModulation).modulation);
                    }
                    removeFromContainer();
                }
            }
        }
    }

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        if (mouseEvent.getCount() == 2 && this.toggleTarget.contains(mx, my)) {
            toggleExpanded();
        }
    }

    @Override
    public void drawFocus(UI ui, PGraphics pg) {}

    @Override
    public void onFocus() {
        setBackgroundColor(ui.theme.getWindowFocusedBackgroundColor());
    }

    @Override
    public void onBlur() {
        setBackgroundColor(ui.theme.getWindowBackgroundColor());
    }

    private class UIModulation extends UI2dContainer implements UIFocus {

        private static final int PADDING = 4;
        private static final int HEIGHT = 34 + 2*PADDING;
        private final LXParameterModulation modulation;

        UIModulation(UI ui, final LXParameterModulation modulation, float x, float y, float w) {
            super(x, y, w, HEIGHT);
            this.modulation = modulation;

            new UIParameterLabel(PADDING, PADDING, width - 2*PADDING - 14, 12).setParameter(modulation.target).setTextAlignment(PConstants.LEFT, PConstants.CENTER).addToContainer(this);
            new UIButton(PADDING + 2, PADDING + 18, 24, 12).setParameter(modulation.polarity).addToContainer(this);
            final UISlider slider = (UISlider) new UISlider(2*PADDING + 26, PADDING + 16, width-3*PADDING - 26, 16)
            .setFillColor(modulation.clr.getColor())
            .setShowLabel(false)
            .setParameter(modulation.range)
            .addToContainer(this);

            modulation.clr.addListener(new LXParameterListener() {
                public void onParameterChanged(LXParameter p) {
                    slider.setFillColor(modulation.clr.getColor());
                    redraw();
                }
            });
        }

        @Override
        public void drawFocus(UI ui, PGraphics pg) {
            pg.stroke(this.modulation.clr.getColor());
            pg.line(0, 0, 0, this.height-1);
        }

        @Override
        public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
            if ((keyCode == java.awt.event.KeyEvent.VK_D && (keyEvent.isControlDown() || keyEvent.isMetaDown())) ||
                keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                consumeKeyEvent();
                lx.engine.modulation.removeModulation(this.modulation);
            }
        }
    }
}
