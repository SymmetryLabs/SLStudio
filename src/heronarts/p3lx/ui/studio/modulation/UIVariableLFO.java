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
import heronarts.lx.LXUtils;
import heronarts.lx.modulator.VariableLFO;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIModulationSource;
import heronarts.p3lx.ui.UITimerTask;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UIToggleSet;
import processing.core.PGraphics;
import processing.event.MouseEvent;

public class UIVariableLFO extends UIModulator {

    private static final int WAVE_HEIGHT = 40;
    private static final int HEIGHT = 110;

    private final VariableLFO lfo;
    private final UIWave wave;

    public UIVariableLFO(UI ui, LX lx, VariableLFO lfo, float x, float y, float w) {
        super(ui, lx, lfo, x, y, w, HEIGHT);
        this.lfo = lfo;

        this.wave = new UIWave(ui, 0, 0, getContentWidth(), WAVE_HEIGHT);
        this.wave.addToContainer(this);

        // TODO(mcslee): improve this Basis UI...
        // new UIBasis(ui, 0, WAVE_HEIGHT, getContentWidth(), 4).addToContainer(this);

        new UIToggleSet(0, 44, getContentWidth(), 16)
        .setParameter(lfo.waveshape)
        .addToContainer(this);

        UI2dContainer knobs = new UI2dContainer(0, 68, getContentWidth(), UIKnob.HEIGHT);
        knobs.setLayout(UI2dContainer.Layout.HORIZONTAL).setChildMargin(2).addToContainer(this);

        new UIKnob(lfo.skew).addToContainer(knobs);
        new UIKnob(lfo.shape).addToContainer(knobs);
        new UIKnob(lfo.exp).addToContainer(knobs);
        new UIKnob(lfo.phase).addToContainer(knobs);
        new UIKnob(lfo.rate).addToContainer(knobs);

    }

    @Override
    protected UIModulationSource getModulationSourceUI() {
        return this.wave;
    }

    private class UIBasis extends UI2dComponent {
        private int basisX;

        private UIBasis(UI ui, float x, float y, float w, float h) {
            super(x, y, w, h);
            setBackgroundColor(ui.theme.getControlDisabledColor());

            addLoopTask(new UITimerTask(60, UITimerTask.Mode.FPS) {
                @Override
                protected void run() {
                    int bX = (int) Math.round(1 + lfo.getBasisf() * (width-3.));
                    if (bX != basisX) {
                        basisX = bX;
                        redraw();
                    }
                }
            });
        }

        @Override
        protected void onDraw(UI ui, PGraphics pg) {
            pg.fill(ui.theme.getPrimaryColor());
            pg.noStroke();
            pg.rect(LXUtils.constrainf(this.basisX, 0, this.width-2), 0, 2, this.height-1);
        }

        @Override
        public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
            lfo.setBasis((mx-1) / (this.width-2));
        }

        @Override
        public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
            lfo.setBasis((mx-1) / (this.width-2));
        }
    }

    private class UIWave extends UI2dComponent implements UIModulationSource {


        private UIWave(UI ui, float x, float y, float w, float h) {
            super(x, y, w, h);
            setBackgroundColor(ui.theme.getDarkBackgroundColor());
            setBorderColor(ui.theme.getControlBorderColor());

            LXParameterListener redraw = new LXParameterListener() {
                public void onParameterChanged(LXParameter p) {
                    redraw();
                }
            };

            lfo.waveshape.addListener(redraw);
            lfo.shape.addListener(redraw);
            lfo.skew.addListener(redraw);
            lfo.exp.addListener(redraw);
            lfo.phase.addListener(redraw);
        }

        @Override
        protected void onDraw(UI ui, PGraphics pg) {
            pg.stroke(ui.theme.getPrimaryColor());
            int py = 0;
            for (int x = 1; x < width-2; ++x) {
                int y = (int) Math.round((height-4.) * lfo.computeBase((x-1) / (width-2.)));
                if (x > 1) {
                    pg.line(x-1, height-2-py, x, height-2-y);
                }
                py = y;
            }
        }

        public LXNormalizedParameter getModulationSource() {
            return lfo;
        }
    }
}
