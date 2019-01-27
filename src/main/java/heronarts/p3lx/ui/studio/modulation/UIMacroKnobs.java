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

package heronarts.p3lx.ui.studio.modulation;

import heronarts.lx.LX;
import heronarts.lx.modulator.MacroKnobs;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIModulationSource;
import heronarts.p3lx.ui.component.UIKnob;

public class UIMacroKnobs extends UIModulator {

    private final static int TOP_PADDING = 4;

    public UIMacroKnobs(UI ui, LX lx, MacroKnobs macroKnobs, float x, float y, float w) {
        super(ui, lx, macroKnobs, true, x, y, w, UIKnob.HEIGHT + TOP_PADDING);
        setLayout(UI2dContainer.Layout.HORIZONTAL);
        setChildMargin(2);
        new UIKnob(macroKnobs.macro1).setY(TOP_PADDING).addToContainer(this);
        new UIKnob(macroKnobs.macro2).setY(TOP_PADDING).addToContainer(this);
        new UIKnob(macroKnobs.macro3).setY(TOP_PADDING).addToContainer(this);
        new UIKnob(macroKnobs.macro4).setY(TOP_PADDING).addToContainer(this);
        new UIKnob(macroKnobs.macro5).setY(TOP_PADDING).addToContainer(this);
    }

    @Override
    protected UIModulationSource getModulationSourceUI() {
        return null;
    }
}