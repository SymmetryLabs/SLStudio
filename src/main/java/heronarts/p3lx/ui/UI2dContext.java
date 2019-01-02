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

package heronarts.p3lx.ui;

import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * A UIContext is a container that owns a graphics buffer. This buffer is
 * persistent across frames and is only redrawn as necessary. It is simply
 * bitmapped onto the UI that is a part of.
 */
public class UI2dContext extends UI2dContainer {

    /**
     * Graphics context for this container.
     */
    private final PGraphics pg;

    /**
     * Constructs a new UI2dContext
     *
     * @param ui the UI to place it in
     * @param x x-position
     * @param y y-position
     * @param w width
     * @param h height
     */
    public UI2dContext(UI ui, float x, float y, float w, float h) {
        super(x, y, w, h);
        this.pg = ui.applet.createGraphics((int) w, (int) h, PConstants.JAVA2D);
        this.pg.smooth();
    }

    @Override
    protected void onResize() {
        this.pg.setSize((int) this.width, (int) this.height);
        redraw();
    }

    @Override
    void draw(UI ui, PGraphics pg) {
        if (!isVisible()) {
            return;
        }
        if (this.needsRedraw || this.childNeedsRedraw) {
            this.pg.beginDraw();
            super.draw(ui, this.pg);
            this.pg.endDraw();
        }
        pg.image(this.pg, 0, 0);
    }

    protected PGraphics getGraphics() {
        return this.pg;
    }
}
