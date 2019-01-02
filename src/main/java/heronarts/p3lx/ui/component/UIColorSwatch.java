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

import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import heronarts.lx.LXUtils;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.LXPalette;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIFocus;

public class UIColorSwatch extends UI2dComponent implements UIFocus {

    public final ColorParameter color;

    private boolean enabled = true;

    private final static int RECT_SIZE = 3;

    public UIColorSwatch(LXPalette palette, float x, float y, float w, float h) {
        this(palette.color, x, y, w, h);
    }

    public UIColorSwatch(ColorParameter color, float x, float y, float w, float h) {
        super(x, y, w, h);
        this.color = color;
        color.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                redraw();
            }
        });
    }

    public UIColorSwatch setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            redraw();
        }
        return this;
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        // Gradient
        pg.noStroke();
        float maxBright = this.color.brightness.getValuef();
        float minBright = Math.min(20, maxBright);
        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.height; ++y) {
                pg.fill(LXColor.hsb(
                    360 * x / this.width,
                    (this.enabled ? 100 : 50) * (1 - y / this.height),
                    (!this.enabled ? (minBright + (maxBright-minBright) *(y / this.height)) : maxBright)
                ));
                pg.rect(x, y, 1, 1);
            }
        }

        // Cursor
        pg.strokeWeight(1);
        pg.noFill();
        pg.stroke(this.enabled ? ui.theme.getDeviceBorderColor() : ui.theme.getControlDisabledColor());
        pg.rect(
            this.color.hue.getNormalizedf() * (this.width-RECT_SIZE-1),
            (1-this.color.saturation.getNormalizedf()) * (this.height-RECT_SIZE-1),
            RECT_SIZE, RECT_SIZE
        );
    }

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        if (this.enabled) {
            updateColor(mx, my);
        }
    }

    @Override
    public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
        if (this.enabled) {
            updateColor(mx, my);
        }
    }

    private void updateColor(float mx, float my) {
        double mxn = LXUtils.constrain(mx / (this.width-RECT_SIZE-1), 0, 1);
        double myn = LXUtils.constrain(my / (this.height-RECT_SIZE-1), 0, 1);
        this.color.hue.setNormalized(mxn);
        this.color.saturation.setNormalized(1 - myn);
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (!this.enabled) {
            return;
        }
        double amount = keyEvent.isShiftDown() ? .05 : .01;
        if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
            this.color.hue.setNormalized(this.color.hue.getNormalized() - amount);
            consumeKeyEvent();
        } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
            this.color.hue.setNormalized(this.color.hue.getNormalized() + amount);
            consumeKeyEvent();
        } else if (keyCode == java.awt.event.KeyEvent.VK_UP) {
            this.color.saturation.setNormalized(this.color.saturation.getNormalized() + amount);
            consumeKeyEvent();
        } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
            this.color.saturation.setNormalized(this.color.saturation.getNormalized() - amount);
            consumeKeyEvent();
        }
    }
}
