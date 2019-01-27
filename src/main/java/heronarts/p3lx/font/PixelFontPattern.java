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

package heronarts.p3lx.font;

import heronarts.p3lx.P3LX;
import heronarts.p3lx.P3LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SawLFO;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * Example pattern to render a text string using PixelFont.
 */
public class PixelFontPattern extends P3LXPattern {

    final private SawLFO hMod = new SawLFO(0, 360, 10000);
    final private SawLFO pMod = new SawLFO(0, 0, 10000);
    final private PImage image;

    public PixelFontPattern(P3LX lx) {
        this(lx, "The quick brown fox jumped over the lazy dog.");
    }

    public PixelFontPattern(P3LX lx, String s) {
        super(lx);
        this.image = (new PixelFont(lx)).drawString(s);
        this.addModulator(this.hMod).trigger();
        this.addModulator(
                this.pMod.setRange(-lx.width, this.image.width, this.image.width * 250))
                .trigger();
    }

    @Override
    public void run(double deltaMs) {
        for (int i = 0; i < this.colors.length; ++i) {
            double col = this.lx.column(i) + this.pMod.getValue();
            int floor = (int) Math.floor(col);
            int ceil = (int) Math.ceil(col);
            float b1 = this.applet
                    .brightness(this.image.get(floor, this.lx.row(i)));
            float b2 = this.applet
                    .brightness(this.image.get(ceil, this.lx.row(i)));

            this.colors[i] = LXColor.hsb(this.hMod.getValue(), 100.,
                    PApplet.lerp(b1, b2, (float) (col - floor)));
        }
    }
}
