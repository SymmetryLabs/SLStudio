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

package heronarts.lx.modulator;

import heronarts.lx.parameter.BoundedParameter;

public class MacroKnobs extends LXModulator {

    public final BoundedParameter macro1 = new BoundedParameter("M1")
        .setDescription("Macro control parameter");

    public final BoundedParameter macro2 = new BoundedParameter("M2")
        .setDescription("Macro control parameter");

    public final BoundedParameter macro3 = new BoundedParameter("M3")
        .setDescription("Macro control parameter");

    public final BoundedParameter macro4 = new BoundedParameter("M4")
        .setDescription("Macro control parameter");

    public final BoundedParameter macro5 = new BoundedParameter("M5")
        .setDescription("Macro control parameter");

    public MacroKnobs() {
        this("MACRO");
    }

    public MacroKnobs(String label) {
        super(label);
        addParameter("macro1", this.macro1);
        addParameter("macro2", this.macro2);
        addParameter("macro3", this.macro3);
        addParameter("macro4", this.macro4);
        addParameter("macro5", this.macro5);
    }

    @Override
    protected double computeValue(double deltaMs) {
        // Not relevant
        return 0;
    }

}
