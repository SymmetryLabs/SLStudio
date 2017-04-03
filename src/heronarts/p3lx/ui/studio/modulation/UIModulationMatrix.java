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
import heronarts.lx.LXModulationEngine;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.LXParameterModulation;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.UIObject;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIColorBox;
import heronarts.p3lx.ui.component.UIParameterLabel;
import heronarts.p3lx.ui.component.UISlider;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;

public class UIModulationMatrix extends UICollapsibleSection {

    private static final int SPACING = 4;
    private final UI ui;
    private final LX lx;

    public UIModulationMatrix(UI ui, final LX lx, float x, float y, float w) {
        super(ui, x, y, w, 0);
        this.ui = ui;
        this.lx = lx;
        setTitle("MAPPINGS");
        setLayout(UI2dContainer.Layout.VERTICAL);
        setChildMargin(SPACING);

        for (LXParameterModulation modulation : lx.engine.modulation.modulations) {
            addModulation(modulation);
        }

        lx.engine.modulation.addListener(new LXModulationEngine.Listener() {
            public void modulationAdded(LXModulationEngine engine, LXParameterModulation modulation) {
                addModulation(modulation);
            }
            public void modulationRemoved(LXModulationEngine engine, LXParameterModulation modulation) {
                removeModulation(modulation);
            }
            public void modulatorAdded(LXModulationEngine engine, LXModulator modulator) {}
            public void modulatorRemoved(LXModulationEngine engine, LXModulator modulator) {}
        });
    }

    private void addModulation(LXParameterModulation modulation) {
        new UIModulation(this.ui, modulation, 0, 0, getContentWidth()).addToContainer(this);
    }

    private void removeModulation(LXParameterModulation modulation) {
        for (UIObject child : this) {
            UIModulation modulationUI = (UIModulation) child;
            if (modulationUI.modulation == modulation) {
                modulationUI.removeFromContainer();
                return;
            }
        }
    }

    private class UIModulation extends UI2dContainer implements UIFocus {

        private static final int PADDING = 4;
        private static final int HEIGHT = 48 + 2*PADDING;
        private static final int COLOR_SIZE = 10;
        private static final int COLOR_X = PADDING+2;
        private static final int COLOR_Y = PADDING+2;
        private final LXParameterModulation modulation;

        UIModulation(UI ui, LXParameterModulation modulation, float x, float y, float w) {
            super(x, y, w, HEIGHT);
            this.modulation = modulation;
            this.modulation.clr.addListener(new LXParameterListener() {
                public void onParameterChanged(LXParameter p) {
                    redraw();
                }
            });

            setBackgroundColor(ui.theme.getDarkBackgroundColor());

            new UIColorBox(ui, modulation.clr, PADDING+2, COLOR_Y, COLOR_SIZE, COLOR_SIZE).addToContainer(this);
            new UIParameterLabel(2*PADDING + COLOR_SIZE, PADDING, width - 3*PADDING - COLOR_SIZE, 12).setParameter(modulation.source).setTextAlignment(PConstants.LEFT, PConstants.CENTER).addToContainer(this);
            new UIParameterLabel(2*PADDING + COLOR_SIZE, PADDING + 16, width - 3*PADDING - COLOR_SIZE, 12).setParameter(modulation.target).setTextAlignment(PConstants.LEFT, PConstants.CENTER).addToContainer(this);
            new UIButton(PADDING, PADDING + 34, 24, 12).setParameter(modulation.polarity).addToContainer(this);
            new UISlider(2*PADDING + 24, PADDING + 32, width-3*PADDING - 24, 16).setShowLabel(false).setParameter(modulation.range).addToContainer(this);
        }

        @Override
        public void onDraw(UI ui, PGraphics pg) {
            pg.textFont(ui.theme.getControlFont());
            pg.stroke(this.modulation.clr.getColor());
            pg.line(COLOR_X+COLOR_SIZE/2, COLOR_Y+COLOR_SIZE, COLOR_X+COLOR_SIZE/2, COLOR_Y + 21);
            pg.line(COLOR_X+COLOR_SIZE/2, COLOR_Y + 21, COLOR_X+COLOR_SIZE, COLOR_Y + 21);
        }

        @Override
        public void drawFocus(UI ui, PGraphics pg) {
            pg.noFill();
            pg.stroke(0xff555555);
            pg.rect(0, 0, width-1, height-1);
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