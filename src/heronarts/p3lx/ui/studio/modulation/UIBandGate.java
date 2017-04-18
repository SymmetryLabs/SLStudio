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
import heronarts.lx.LXComponent;
import heronarts.lx.LXLoopTask;
import heronarts.lx.audio.BandGate;
import heronarts.lx.audio.FourierTransform;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIModulationSource;
import heronarts.p3lx.ui.component.UIDoubleBox;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UILabel;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.MouseEvent;

public class UIBandGate extends UIModulator {
    private final BandGate bandGate;

    private final static int MARGIN = 4;
    private final static int HEIGHT = 166;
    private final static int ENV_WIDTH = 12;
    private final static int METER_HEIGHT = 60;

    private final UIBandSelector bandSelector;
    private final UILevelMeter levelMeter;
    private final UIEnvMeter envMeter;

    public UIBandGate(UI ui, final LX lx, BandGate bandGate, float x, float y, float w) {
        super(ui, lx, bandGate, x, y, w, HEIGHT);

        // TODO(mcslee): this needs to work better...
        //float tempoX = this.width - 2*(PADDING + MAP_WIDTH);
        //addTopLevelComponent(
        //  new UIButton(tempoX, PADDING-1, MAP_WIDTH, 14)
        //  .setLabel("TAP")
        //  .setParameter(bandGate.teachTempo)
        //);
        //this.title.setWidth(tempoX - PADDING - this.title.getX());

        this.bandGate = bandGate;
        this.bandSelector = new UIBandSelector(ui, 0, 0, getContentWidth() - 2*MARGIN - 2*ENV_WIDTH, METER_HEIGHT);
        this.bandSelector.addToContainer(this);

        this.levelMeter = new UILevelMeter(ui, getContentWidth()-2*ENV_WIDTH-MARGIN, 0, ENV_WIDTH, METER_HEIGHT);
        this.levelMeter.addToContainer(this);

        this.envMeter = new UIEnvMeter(ui, getContentWidth()-ENV_WIDTH, 0, ENV_WIDTH, METER_HEIGHT);
        this.envMeter.addToContainer(this);

        float yp = METER_HEIGHT + 2;
        addControlRow(yp, "Gain", bandGate.gain, "Range", bandGate.range, .1f);
        addControlRow(yp + 20, "Min", bandGate.minFreq, "Max", bandGate.maxFreq);
        addControlRow(yp + 40, "Thresh", bandGate.threshold, "Floor", bandGate.floor);

        UI2dContainer knobs = new UI2dContainer(6, yp + 62, getContentWidth(), UIKnob.HEIGHT);
        knobs.setLayout(UI2dContainer.Layout.HORIZONTAL).setChildMargin(12).addToContainer(this);
        new UIKnob(bandGate.attack).addToContainer(knobs);
        new UIKnob(bandGate.release).addToContainer(knobs);
        new UIKnob(bandGate.slope).addToContainer(knobs);
        new UIKnob(bandGate.decay).addToContainer(knobs);

    }

    void addControlRow(float yp, String label1, BoundedParameter p1, String label2, BoundedParameter p2) {
        addControlRow(yp, label1, p1, label2, p2, 0);
    }

    void addControlRow(float yp, String label1, BoundedParameter p1, String label2, BoundedParameter p2, float shiftMultiplier) {
        new UILabel(0, yp, 32, 12).setLabel(label1).addToContainer(this);
        UIDoubleBox d1 = new UIDoubleBox(36, yp, 64, 16);
        if (shiftMultiplier > 0) {
            d1.setShiftMultiplier(shiftMultiplier);
        }
        d1.setParameter(p1).addToContainer(this);
        new UILabel(108, yp, 32, 12).setLabel(label2).addToContainer(this);
        UIDoubleBox d2 = new UIDoubleBox(144, yp, 64, 16);
        if (shiftMultiplier > 0) {
            d2.setShiftMultiplier(shiftMultiplier);
        }
        d2.setParameter(p2).addToContainer(this);
    }

    @Override
    public UIModulationSource getModulationSourceUI() {
        return this.envMeter;
    }

    class UIBandSelector extends UI2dComponent {

        private final float bandWidth;
        private final int[] bandX;

        UIBandSelector(UI ui, float x, float y, float w, float h) {
            super(x, y, w, h);
            setBorderColor(ui.theme.getControlBorderColor());
            setBackgroundColor(ui.theme.getDarkBackgroundColor());
            bandWidth = ((width-2) - (bandGate.meter.numBands-1)) / bandGate.meter.numBands;
            bandX = new int[bandGate.meter.numBands+1];
            bandX[0] = 1;
            for (int i = 1; i < bandX.length; ++i) {
                bandX[i] = Math.round(1 + i * (bandWidth+1));
            }

            addLoopTask(new LXLoopTask() {
                public void loop(double deltaMs) {
                    redraw();
                }
            });
        }

        @Override
        public String getDescription() {
            return LXComponent.getCanonicalLabel(bandGate) + ": selects the audio band response, click and drag to adjust frequency, threshold, and floor";
        }

        private float hzToX(float hz) {
            float norm;
            if (hz <= FourierTransform.BASE_BAND_HZ) {
                norm = (hz / FourierTransform.BASE_BAND_HZ) / bandGate.meter.getNumBands();
            } else {
                float exp = (float) Math.log(hz / FourierTransform.BASE_BAND_HZ) / FourierTransform.LOG_2;
                norm = (1 + exp / bandGate.meter.fft.getBandOctaveRatio()) / bandGate.meter.getNumBands();
            }
            return norm * (width-1);
        }

        private float xToHz(float x) {
            float norm = x / (width-1);
            float exp = norm * bandGate.meter.getNumBands();
            if (exp <= 1) {
                return FourierTransform.BASE_BAND_HZ * exp;
            }
            return FourierTransform.BASE_BAND_HZ * (float) Math.pow(2, (exp-1) * bandGate.meter.fft.getBandOctaveRatio());
        }

        @Override
        public void onDraw(UI ui, PGraphics pg) {

            int minX = Math.round(hzToX(bandGate.minFreq.getValuef()));
            int maxX = Math.round(hzToX(bandGate.maxFreq.getValuef()));
            pg.noStroke();
            for (int i = 0; i < bandGate.meter.numBands; ++i) {
                float x = bandX[i];
                float w = bandX[i+1] - bandX[i] - 1;
                pg.fill(((x+w) >= minX && x < maxX) ? ui.theme.getPrimaryColor() : ui.theme.getControlDisabledColor());
                float h = (float) bandGate.getBand(i) * (height-2);
                pg.rect(x, (height-1-h), w, h);
            }

            pg.fill(ui.theme.getSecondaryColor());
            drawSelector(ui, pg, minX);
            drawSelector(ui, pg, maxX);

            pg.stroke(ui.theme.getSecondaryColor());
            float y = 1 + (height-3) * (1-bandGate.threshold.getValuef());
            pg.line(1, y, width-2, y);

            float y2 = 1 + (height-3) * (1-bandGate.floor.getValuef() * bandGate.threshold.getValuef());
            if (Math.abs(y2 - y) >= 1) {
                pg.line(1, y2, width-2, y2);
            }
        }

        void drawSelector(UI ui, PGraphics pg, int x) {
            if (x - bandWidth/2 <= 1) {
                x = Math.max(x, 1);
                float desc = x - 1;
                pg.beginShape();
                pg.vertex(x, height-1-bandWidth);
                pg.vertex(x+bandWidth/2, height-1);
                pg.vertex(x-desc, height-1);
                pg.vertex(x-desc, height-1-bandWidth+2*desc);
                pg.endShape(PConstants.CLOSE);
                pg.rect(x, 1, 1, height-2);
            } else if (x + bandWidth/2 >= width-1) {
                x = (int) Math.min(width-2, x);
                float desc = width-2-x;
                pg.beginShape();
                pg.vertex(x, height-1-bandWidth);
                pg.vertex(x-bandWidth/2, height-1);
                pg.vertex(x+desc, height-1);
                pg.vertex(x+desc, height-1-bandWidth+2*desc);
                pg.endShape(PConstants.CLOSE);
                pg.rect(x, 1, 1, height-2);
            } else {
                pg.beginShape();
                pg.vertex(x, height-1-bandWidth);
                pg.vertex(x-bandWidth/2, height-1);
                pg.vertex(x+bandWidth/2, height-1);
                pg.endShape(PConstants.CLOSE);
                pg.rect(x, 1, 1, height-2);
            }
        }

        private boolean moveMin = false;
        private boolean moveMax = false;
        private boolean moveThreshold  = false;
        private boolean moveFloor = false;

        private static final int FREQ_THRESH = 4;

        @Override
        public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
            this.moveMin = moveMax = moveThreshold = moveFloor = false;
            float minPressX = hzToX(bandGate.minFreq.getValuef());
            float maxPressX = hzToX(bandGate.maxFreq.getValuef());
            if (mx < minPressX && Math.abs(mx - minPressX) < FREQ_THRESH) {
                this.moveMin = true;
            } else if (mx > maxPressX && Math.abs(mx - maxPressX) < FREQ_THRESH) {
                this.moveMax = true;
            } else {
                float minDist = Math.abs(mx - minPressX);
                float maxDist = Math.abs(mx - maxPressX);
                this.moveMin = (minDist < maxDist) && (minDist < FREQ_THRESH);
                this.moveMax = (maxDist < minDist) && (maxDist < FREQ_THRESH);
            }
            if (!this.moveMin && !this.moveMax) {
                if (my <= (1-bandGate.threshold.getValue()) * height) {
                    this.moveThreshold = true;
                } else if (my >= (1-bandGate.threshold.getValue() * bandGate.floor.getValue()) * height) {
                    this.moveFloor = true;
                } else {
                    float threshY = (float) (1-bandGate.threshold.getValue()) * height;
                    float floorY = (float) (1-bandGate.threshold.getValue() * bandGate.floor.getValue()) * height;
                    this.moveThreshold = Math.abs(my - threshY) < Math.abs(my - floorY);
                    this.moveFloor = !moveThreshold;
                }
            }
        }

        @Override
        public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
            if (moveMin) {
                if ((mx > 0 || dx < 0) && (mx < this.width || dx > 0)) {
                    bandGate.minFreq.setValue(xToHz(mx));
                }
            } else if (moveMax) {
                if ((mx > 0 || dx < 0) && (mx < this.width || dx > 0)) {
                    bandGate.maxFreq.setValue(xToHz(mx));
                }
            }
            if (moveThreshold) {
                if ((my > 0 || dy < 0) && (my < this.height || dy > 0)) {
                    bandGate.threshold.setValue(bandGate.threshold.getValue() - dy / (this.height-2));
                }
            } else if (moveFloor) {
                if ((my > (1 + (1-bandGate.threshold.getValue()) * (this.height-2)) || dy < 0) && (my < this.height || dy > 0)) {
                    bandGate.floor.setValue(bandGate.floor.getValue() - dy / (bandGate.threshold.getValue() * (this.height-2)));
                }
            }
        }
    }

    class UILevelMeter extends UI2dComponent implements UIModulationSource {

        private float dh = 0;

        UILevelMeter(UI ui, float x, float y, float w, float h) {
            super(x, y, w, h);
            setBorderColor(ui.theme.getControlBorderColor());
            setBackgroundColor(ui.theme.getDarkBackgroundColor());

            addLoopTask(new LXLoopTask() {
                public void loop(double deltaMs) {
                    float dhv = (height-2)*bandGate.average.getValuef();
                    if (dhv != dh) {
                        dh = dhv;
                        redraw();
                    }
                }
            });
        }

        @Override
        public String getDescription() {
            return LXComponent.getCanonicalLabel(bandGate.average) + ": " + bandGate.average.getDescription();
        }

        @Override
        public void onDraw(UI ui, PGraphics pg) {
            pg.noStroke();
            pg.fill(ui.theme.getPrimaryColor());
            pg.rect(1, height-1-dh, width-2, dh);
        }

        public LXNormalizedParameter getModulationSource() {
            return bandGate.average;
        }
    }

    class UIEnvMeter extends UI2dComponent implements UIModulationSource {

        private float dh = 0;

        UIEnvMeter(UI ui, float x, float y, float w, float h) {
            super(x, y, w, h);
            setBorderColor(ui.theme.getControlBorderColor());
            setBackgroundColor(ui.theme.getDarkBackgroundColor());

            addLoopTask(new LXLoopTask() {
                public void loop(double deltaMs) {
                    float dhv = (height-2)*bandGate.getValuef();
                    if (dhv != dh) {
                        dh = dhv;
                        redraw();
                    }
                }
            });
        }

        @Override
        public String getDescription() {
            return LXComponent.getCanonicalLabel(bandGate) + ": shows the output envelope value";
        }

        @Override
        public void onDraw(UI ui, PGraphics pg) {
            pg.noStroke();
            pg.fill(ui.theme.getPrimaryColor());
            pg.rect(1, height-1-dh, width-2, dh);
        }

        public LXNormalizedParameter getModulationSource() {
            return bandGate;
        }
    }
}
