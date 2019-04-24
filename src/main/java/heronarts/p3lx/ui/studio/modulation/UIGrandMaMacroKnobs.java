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

import com.symmetrylabs.util.dmx.LXParameterChangeDMXHandler;
import heronarts.lx.LX;
import heronarts.lx.modulator.GrandMaMacroKnobs;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIModulationSource;
import heronarts.p3lx.ui.component.UIKnob;

public class UIGrandMaMacroKnobs extends UIModulator {

    private final static int TOP_PADDING = 4;

    private final static int WIDTH_PANE = 6;
    private final static int NUM_MACRO = 64;
    private final static int HEIGHT_MACRO = 64/WIDTH_PANE;

    public UIGrandMaMacroKnobs(UI ui, LX lx, GrandMaMacroKnobs macroKnobs, float x, float y, float w) {
        super(ui, lx, macroKnobs, true, x, y, w, UIKnob.HEIGHT*HEIGHT_MACRO + TOP_PADDING);
        setLayout(Layout.HORIZONTAL_GRID);
        setChildMargin(3);

        for (int i = 0; i < macroKnobs.NUM_MACRO; i++ ){
//            int offY = (i/6) * UIKnob.HEIGHT;
            new UIKnob(macroKnobs.macros[i]).setY(TOP_PADDING).addToContainer(this);
            lx.engine.dmx.addHandler(new LXParameterChangeDMXHandler(i, macroKnobs.macros[i]));
        }
    }

    @Override
    protected UIModulationSource getModulationSourceUI() {
        return null;
    }
}
