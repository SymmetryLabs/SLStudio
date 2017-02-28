/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.p3lx.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.studio.UIPatternControl;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class SolidColorPattern extends heronarts.lx.pattern.SolidColorPattern implements LXPatternUI {

    public SolidColorPattern(LX lx) {
        super(lx);
    }

    private static final int SLIDER_WIDTH = 16;
    private static final int SLIDER_MARGIN = 4;
    private static final int SLIDER_SPACING = SLIDER_WIDTH + SLIDER_MARGIN;

    @Override
    public void buildControlUI(UI ui, UIPatternControl container) {
        float xp = 0;
        new HueSlider(ui, xp, 0, SLIDER_WIDTH, container.getContentHeight()).addToContainer(container);
        xp += SLIDER_SPACING;
        new SaturationSlider(ui, xp, 0, SLIDER_WIDTH, container.getContentHeight()).addToContainer(container);
        xp += SLIDER_SPACING;
        new BrightnessSlider(ui, xp, 0, SLIDER_WIDTH, container.getContentHeight()).addToContainer(container);
        xp += SLIDER_SPACING;
        container.setContentWidth(xp - SLIDER_MARGIN);
    }

    private class Slider extends UI2dComponent implements UIFocus {

        private final BoundedParameter parameter;

        Slider(UI ui, BoundedParameter parameter, float x, float y, float w, float h) {
            super(x, y, w, h);
            this.parameter = parameter;
            setBorderColor(ui.theme.getControlBorderColor());
            color.addListener(new LXParameterListener() {
                public void onParameterChanged(LXParameter p) {
                    redraw();
                }
            });
        }

        private void updateParameter(float mx, float my) {
            this.parameter.setNormalized(1. - my / (this.height-1));
        }

        @Override
        public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
            double amt = .025;
            if (keyEvent.isShiftDown()) {
                amt = .1;
            }
            if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
                this.parameter.setNormalized(this.parameter.getNormalized() - amt);
            } else if (keyCode == java.awt.event.KeyEvent.VK_UP) {
                this.parameter.setNormalized(this.parameter.getNormalized() + amt);
            }
        }

        @Override
        public void onMousePressed(MouseEvent MouseEvent, float mx, float my) {
            updateParameter(mx, my);
        }

        @Override
        public void onMouseDragged(MouseEvent MouseEvent, float mx, float my, float dx, float dy) {
            updateParameter(mx, my);
        }

        protected void drawValue(UI ui, PGraphics pg) {
            pg.stroke(0xffe9e9e9);
            float y = LXUtils.constrainf(this.height-1 - this.parameter.getNormalizedf() * (this.height-1), 1, this.height-2);
            pg.line(0, y, this.width-1, y);
        }
    }

    private class HueSlider extends Slider {
        HueSlider(UI ui, float x, float y, float w, float h) {
            super(ui, color.hue, x, y, w, h);
        }

        @Override
        public void onDraw(UI ui, PGraphics pg) {
            for (int i = 0; i < this.height-1; ++i) {
                pg.stroke(LX.hsb(i * 360.f / (this.height-1), 100, 100));
                pg.line(0, this.height-1-i, this.width-1, this.height-1-i);
            }
            drawValue(ui, pg);
        }
    }

    private class SaturationSlider extends Slider {
        SaturationSlider(UI ui, float x, float y, float w, float h) {
            super(ui, color.saturation, x, y, w, h);
        }

        @Override
        public void onDraw(UI ui, PGraphics pg) {
            for (int i = 0; i < this.height; ++i) {
                pg.stroke(LX.hsb(color.hue.getValuef(), i * 100.f / (this.height-1), color.brightness.getValuef()));
                pg.line(0, this.height-1-i, this.width-1, this.height-1-i);
            }
            drawValue(ui, pg);
        }
    }

    private class BrightnessSlider extends Slider {
        BrightnessSlider(UI ui, float x, float y, float w, float h) {
            super(ui, color.brightness, x, y, w, h);
        }

        @Override
        public void onDraw(UI ui, PGraphics pg) {
            for (int i = 0; i < this.height; ++i) {
                pg.stroke(LX.hsb(color.hue.getValuef(), color.saturation.getValuef(), i * 100.f / (this.height-1)));
                pg.line(0, this.height-1-i, this.width-1, this.height-1-i);
            }
            drawValue(ui, pg);
        }
    }

}
