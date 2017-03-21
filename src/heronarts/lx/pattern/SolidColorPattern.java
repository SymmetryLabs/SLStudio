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
import heronarts.lx.parameter.LXParameter;

public class SolidColorPattern extends LXPattern {

    public final ColorParameter color = new ColorParameter("Color");

    public SolidColorPattern(LX lx) {
        this(lx, LXColor.RED);
    }

    public SolidColorPattern(LX lx, int color) {
        super(lx);
        this.color.setColor(color);
        addParameter(this.color);
        setColors(this.color.getColor());
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        setColors(this.color.getColor());
    }

    @Override
    public void run(double deltaMs) {
    }
}
