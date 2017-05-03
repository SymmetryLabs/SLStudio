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
import heronarts.lx.LXLoopTask;
import heronarts.lx.LXUtils;
import heronarts.lx.modulator.MultiStageEnvelope;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.UIModulationSource;
import heronarts.p3lx.ui.component.UIDoubleBox;
import heronarts.p3lx.ui.component.UIKnob;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIMultiStageEnvelope extends UIModulator {

    private static final int WAVE_HEIGHT = UIKnob.HEIGHT + 20;
    private static final int HEIGHT = WAVE_HEIGHT;

    private final MultiStageEnvelope envelope;
    private final UIWave wave;

    public UIMultiStageEnvelope(UI ui, LX lx, MultiStageEnvelope envelope, float x, float y, float w) {
        super(ui, lx, envelope, x, y, w, HEIGHT);
        this.envelope = envelope;
        this.wave = (UIWave) new UIWave(ui, 0, 0, getContentWidth()-44, WAVE_HEIGHT).addToContainer(this);
        new UIKnob(getContentWidth() - 40, 0).setParameter(this.envelope.period).addToContainer(this);
        new UIDoubleBox(getContentWidth() - 40, 44, 40, 16).setParameter(this.envelope.period).addToContainer(this);
    }

    @Override
    protected UIModulationSource getModulationSourceUI() {
        return this.wave;
    }

    private class UIWave extends UI2dComponent implements UIModulationSource, UIFocus {

        private PGraphics g;
        private int basisX = 0;

        private UIWave(final UI ui, float x, float y, float w, float h) {
            super(x, y, w, h);
            setBackgroundColor(ui.theme.getDarkBackgroundColor());
            setBorderColor(ui.theme.getControlBorderColor());

            this.g = ui.applet.createGraphics((int) (w-2), (int) (h-2));
            drawEnvelope(ui);

            envelope.monitor.addListener(new LXParameterListener() {
                public void onParameterChanged(LXParameter p) {
                    drawEnvelope(ui);
                    redraw();
                }
            });

            addLoopTask(new LXLoopTask() {
                @Override
                public void loop(double deltaMs) {
                    int bX = (int) Math.round(1 + envelope.getBasisf() * (width-3.));
                    if (bX != basisX) {
                        basisX = bX;
                        redraw();
                    }
                }
            });
        }

        private void drawEnvelope(UI ui) {
            this.g.beginDraw();
            this.g.background(getBackgroundColor());

            // Lines
            this.g.stroke(ui.theme.getPrimaryColor());
            float py = 0;
            for (int x = 0; x < this.g.width; ++x) {
                float y = (float) ((this.g.height-1) - (this.g.height-1) * envelope.compute(x / (this.g.width-1.)));
                if (x > 0) {
                    this.g.line(x-1, py, x, y);
                }
                py = y;
            }

            // Stage boxes
            for (MultiStageEnvelope.Stage stage : envelope.stages) {
                float tx = (float) LXUtils.lerp(1, this.g.width-1, stage.getBasis());
                float ty = (float) LXUtils.lerp(this.g.height-1, 0, stage.getValue());
                float lx = LXUtils.constrainf(tx-3, 0, this.g.width-1);
                float rx = LXUtils.constrainf(tx+3, 0, this.g.width-1);
                float ly = LXUtils.constrainf(ty-3, 0, this.g.height-1);
                float ry = LXUtils.constrainf(ty+3, 0, this.g.height-1);
                if (stage == this.editing) {
                    this.g.fill(envelope.color.getColor());
                    this.g.noStroke();
                    this.g.rect(lx, ly, rx-lx+1, ry-ly+1);
                } else {
                    this.g.fill(ui.theme.getDarkBackgroundColor());
                    this.g.stroke(ui.theme.getPrimaryColor());
                    this.g.rect(lx, ly, rx-lx, ry-ly);
                }
            }

            this.g.endDraw();
        }

        @Override
        protected void onDraw(UI ui, PGraphics pg) {
            pg.image(this.g, 1, 1);
            pg.stroke(ui.theme.getCursorColor());
            pg.line(this.basisX, 1, this.basisX, this.height-2);
        }

        private MultiStageEnvelope.Stage editing = null;

        @Override
        public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
            double basis = LXUtils.constrain((mx-1) / (this.width-3), 0, 1);
            double value = LXUtils.constrain(1 - (my-1) / (this.height-3), 0, 1);

            this.editing = null;
            if (mouseEvent.getCount() == 2) {
                this.editing = envelope.addStage(basis, value);
            } else {
                double xThresh = 6 / (this.width-2);
                double yThresh = 6 / (this.height-2);
                for (MultiStageEnvelope.Stage stage : envelope.stages) {
                    if (Math.abs(stage.getBasis() - basis) < xThresh && Math.abs(stage.getValue() - value) < yThresh) {
                        this.editing = stage;
                        break;
                    }
                }
            }

            redraw();
        }

        @Override
        public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
            double basis = LXUtils.constrain((mx-1) / (this.width-3), 0, 1);
            if (this.editing != null) {
                double value = LXUtils.constrain(1 - (my-1) / (this.height-3), 0, 1);
                this.editing.setPosition(basis, value);
            } else {
                MultiStageEnvelope.Stage previous = null;
                for (MultiStageEnvelope.Stage stage : envelope.stages) {
                    if (basis < stage.getBasis()) {
                        double sign = (previous == null) ? 1 : ((previous.getValue() < stage.getValue() ? 1 : -1));
                        stage.setShape(stage.getShape() * (1 + sign * dy/this.height));
                        break;
                    }
                    previous = stage;
                }
            }
        }

        @Override
        public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
            if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE ||
                    (keyCode == java.awt.event.KeyEvent.VK_D && (keyEvent.isControlDown() || keyEvent.isMetaDown()))) {
                consumeKeyEvent();
                if (this.editing != null) {
                    envelope.removeStage(this.editing);
                }
            }
        }

        @Override
        public LXNormalizedParameter getModulationSource() {
            return envelope;
        }
    }

}
