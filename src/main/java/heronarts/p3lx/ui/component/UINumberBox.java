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

public abstract class UINumberBox extends UIInputBox {

    protected boolean hasShiftMultiplier = false;
    protected float shiftMultiplier = 1;

    protected UINumberBox() {
        this(0, 0, 0, 0);
    }

    protected UINumberBox(float x, float y, float w, float h) {
        super(x, y, w, h);
        enableImmediateEdit(true);
    }

    public UINumberBox setFill(boolean hasFill) {
        if (this.hasFill != hasFill) {
            this.hasFill = hasFill;
            redraw();
        }
        return this;
    }

    public UINumberBox setFillColor(int fillColor) {
        if (!this.hasFill || (this.fillColor != fillColor)) {
            this.hasFill = true;
            this.fillColor = fillColor;
            redraw();
        }
        return this;
    }

    /**
     * Sets a multiplier by which the amount value changes are modulated
     * when the shift key is down. Either for more precise control or
     * larger jumps, depending on the component.
     *
     * @param shiftMultiplier Amount to multiply by
     * @return
     */
    public UINumberBox setShiftMultiplier(float shiftMultiplier) {
        this.hasShiftMultiplier = true;
        this.shiftMultiplier = shiftMultiplier;
        return this;
    }

}
