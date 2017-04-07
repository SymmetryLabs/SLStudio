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
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import processing.core.PConstants;

public class UIOscManager extends UICollapsibleSection {

    private static final int HEIGHT = 68;

    public UIOscManager(UI ui, LX lx, float x, float y, float w) {
        super(ui, x, y, w, HEIGHT);
        setTitle("OSC I/O");

        UI2dContainer border = (UI2dContainer) new UI2dContainer(0, 0, getContentWidth(), getContentHeight())
            .setBorderRounding(4)
            .setBackgroundColor(ui.theme.getDarkBackgroundColor())
            .addToContainer(this);

        float yp = 4;
        new UILabel(6, yp+2, 36, 12).setLabel("Rx Port").setTextAlignment(PConstants.LEFT, PConstants.CENTER).addToContainer(border);
        new UIIntegerBox(46, yp, 64, 16).setParameter(lx.engine.osc.receivePort).addToContainer(border);
        new UIButton(114, yp, border.getContentWidth() - 120, 16).setLabel("Receive").setParameter(lx.engine.osc.receiveActive).addToContainer(border);

        yp += 20;
        new UILabel(6, yp+2, 36, 12).setLabel("Tx Port").setTextAlignment(PConstants.LEFT, PConstants.CENTER).addToContainer(border);
        new UIIntegerBox(46, yp, 64, 16).setParameter(lx.engine.osc.transmitPort).addToContainer(border);
        new UIButton(114, yp, border.getContentWidth() - 120, 16).setLabel("Transmit").setParameter(lx.engine.osc.transmitActive).addToContainer(border);
    }
}
