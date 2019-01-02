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

package heronarts.p3lx;

import processing.core.PConstants;
import processing.core.PGraphics;

public abstract class P3LXGraphicsPattern extends P3LXPattern {

    private final PGraphics pg;

    protected P3LXGraphicsPattern(P3LX lx) {
        super(lx);
        this.pg = this.applet.createGraphics(lx.width, lx.height, PConstants.P2D);
    }

    @Override
    final protected void run(double deltaMs) {
        this.pg.beginDraw();
        this.run(deltaMs, this.pg);
        this.pg.endDraw();
        this.pg.loadPixels();
        for (int i = 0; i < this.lx.total; ++i) {
            this.colors[i] = this.pg.pixels[i];
        }
    }

    abstract protected void run(double deltaMs, PGraphics pg);

}
