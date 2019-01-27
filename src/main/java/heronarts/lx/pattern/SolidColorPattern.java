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

package heronarts.lx.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.LXPalette;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.PaletteParameter;
import heronarts.lx.transform.LXVector;

import java.util.Arrays;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class SolidColorPattern extends LXPattern {

    public final ColorParameter color = new ColorParameter("Color");
  public final BooleanParameter usePalette = new BooleanParameter("use palette");

    public SolidColorPattern(LX lx) {
    this(lx, LXColor.BLACK);
    }

    public SolidColorPattern(LX lx, int color) {
        super(lx);
        this.color.setColor(color);
        addParameter("color", this.color);
        addParameter("bool_palette", this.usePalette);
        render();
    }

    public void render() {
//      LXPalette activePalette = getActivePalette();
//      LXPalette activePalette  = lx.palettes.get(paletteParameter.getValuei());

        int c; // local color var
        if (this.usePalette.getValueb()){
            c = palette.getColor();
        }
        else {
            c = color.getColor();
        }



        int[] colors = (int[]) getArray(SRGB8);
        for (LXVector v : getVectors()) {
            colors[v.index] = c;
        }
        markModified(SRGB8);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == this.color) render();
    if (p instanceof PaletteParameter || p instanceof BooleanParameter) render();
    }

    @Override
    public void onVectorsChanged() {
        super.onVectorsChanged();
        render();
    }

    @Override
    public void run(double deltaMs) {
    }
}
