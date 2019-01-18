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
 * @author Mark C. Slee <mark@heronarts.com>
 */

package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.transform.LXVector;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIControlTarget;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.studio.device.UIPattern;
import heronarts.p3lx.ui.studio.device.UIPatternDevice;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.Arrays;

//public class UIPalettePattern extends heronarts.lx.pattern.SolidColorPattern implements UIPattern {
public class UIPalettePattern extends SLPattern<SLModel> implements UIPattern {

    public final ColorParameter color = new ColorParameter("Color");

    public UIPalettePattern(LX lx) {
        super(lx);
    }

    private static final int SLIDER_WIDTH = 16;
    private static final int SLIDER_MARGIN = 4;
    private static final int SLIDER_SPACING = SLIDER_WIDTH + SLIDER_MARGIN;
    private static final int SELECTOR_WIDTH = 76;
    private static final int SELECTOR_SPACING = SELECTOR_WIDTH + SLIDER_MARGIN;

    @Override
    public void run(double elapsedMs, PolyBuffer.Space preferredSpace) {
        int[] colors = (int[]) getArray(PolyBuffer.Space.RGB8);
        Arrays.fill(colors, 0);
        for (LXVector v : getVectors()) {
            colors[v.index] = LX.hsb(color.hue.getValuef(), 100, 100);
        }
        markModified(PolyBuffer.Space.RGB8);
    }

    @Override
    public void buildDeviceUI(UI ui, UIPatternDevice device) {
        float xp = 0;
        new PaletteSlider(ui, xp, 0, SELECTOR_WIDTH, device.getContentHeight()).addToContainer(device);
        xp += SELECTOR_SPACING;
//    new HueSlider(ui, xp, 0, SLIDER_WIDTH, device.getContentHeight()).addToContainer(device);
//    xp += SLIDER_SPACING;
//    new SaturationSlider(ui, xp, 0, SLIDER_WIDTH, device.getContentHeight()).addToContainer(device);
//    xp += SLIDER_SPACING;
//    new BrightnessSlider(ui, xp, 0, SLIDER_WIDTH, device.getContentHeight()).addToContainer(device);
//    xp += SLIDER_SPACING;
        device.setContentWidth(xp - SLIDER_MARGIN);
    }

    private abstract class Slider extends UI2dComponent implements UIFocus, UIControlTarget {

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

        @Override
        public LXParameter getControlTarget() {
            return this.parameter;
        }
    }

    private class PaletteSlider extends Slider {
        DiscreteParameter selection;
        static final double NOT_SELECTED_DARKEN = 0.7;  // at a value of 1.0 not selected palettes will be black in the ui

        PaletteSlider(UI ui, float x, float y, float w, float h) {
            super(ui, color.hue, x, y, w, h);
            selection = new DiscreteParameter("selected palette", 0, 0, LX.NUM_PALLETS);
        }

        @Override
        public void onDraw(UI ui, PGraphics pg) {
            // draw sections for each of our palettes
            int section_height = (int)((this.height-2)/LX.NUM_PALLETS); // -2 to account for ui border?
            for (int p = 0; p < LX.NUM_PALLETS; p++){
//                for (int i = p*section_height; i < (p+1)*section_height; ++i) {
//                    pg.stroke(LX.hsb(i * 360.f / (this.height-1), 100, 50));
//                    pg.line(0, this.height-1-i, this.width-1, this.height-1-i);
//                }
                for (int i = 0; i < this.width; ++i) {
                    // get palette color for a given x-val, and reference to given palette.
//                    pg.stroke(LX.hsb(i * 360.f / (this.width), 100, (p+1)*25));
                    int p_color = lx.palettes.get(p).getColorByRange(i, this.width);

                    if (p != selection.getValuei()){
                        p_color = Ops8.multiply(p_color, LXColor.BLACK, NOT_SELECTED_DARKEN);
                    }

                    pg.stroke(p_color);
                    pg.line(i, section_height*p, i, section_height*(p+1));
                }
            }
            drawValue(ui, pg);
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
