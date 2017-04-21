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

package heronarts.p3lx.ui.studio.osc;

import heronarts.lx.LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIIntegerBox;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UITextBox;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import processing.core.PConstants;

public class UIOscManager extends UICollapsibleSection {

    private static final int HEIGHT = 68;

    public UIOscManager(UI ui, LX lx, float x, float y, float w) {
        super(ui, x, y, w, HEIGHT);
        setTitle("OSC I/O");

        UI2dContainer border = (UI2dContainer) new UI2dContainer(0, 0, getContentWidth(), getContentHeight())
            .setBackgroundColor(ui.theme.getDarkBackgroundColor())
            .setBorderRounding(4)
            .addToContainer(this);

        float yp = 4;
        new UILabel(6, yp+2, 36, 12).setLabel("Input").setTextAlignment(PConstants.LEFT, PConstants.CENTER).addToContainer(border);
        new UIIntegerBox(46, yp, 64, 16).setParameter(lx.engine.osc.receivePort).setMappable(false).addToContainer(border);
        new UITextBox(114, yp, 70, 16).setParameter(lx.engine.osc.receiveHost).addToContainer(border);
        new UIButton(188, yp, 16, 16).setParameter(lx.engine.osc.receiveActive).setMappable(false).setBorderRounding(4).addToContainer(border);

        yp += 20;
        new UILabel(6, yp+2, 36, 12).setLabel("Output").setTextAlignment(PConstants.LEFT, PConstants.CENTER).addToContainer(border);
        new UIIntegerBox(46, yp, 64, 16).setParameter(lx.engine.osc.transmitPort).setMappable(false).addToContainer(border);
        new UITextBox(114, yp, 70, 16).setParameter(lx.engine.osc.transmitHost).addToContainer(border);
        new UIButton(188, yp, 16, 16).setParameter(lx.engine.osc.transmitActive).setMappable(false).setBorderRounding(4).addToContainer(border);
    }
}
