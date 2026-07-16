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
import heronarts.lx.osc.LXOscEngine;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIIntegerBox;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UITextBox;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.HashMap;
import java.util.Map;

public class UIOscManager extends UICollapsibleSection implements LXOscEngine.DestinationListener {

    private static final int ROW_HEIGHT = 44;
    private static final int BASE_HEIGHT = ROW_HEIGHT + 8;

    private final UI ui;
    private final LX lx;
    private final Map<LXOscEngine.OscDestination, UI2dContainer> destWidgets = new HashMap<>();

    public UIOscManager(UI ui, LX lx, float x, float y, float w) {
        super(ui, x, y, w, BASE_HEIGHT);
        this.ui = ui;
        this.lx = lx;
        setTitle("OSC I/O");
        setTitleX(4);
        setLayout(UI2dContainer.Layout.VERTICAL);
        setChildMargin(4);

        // "+" button in the title bar area
        UIButton addButton = (UIButton) new UIButton((int)(w - 44), 4, 16, 12) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    lx.engine.osc.addDestination();
                }
            }
        }
        .setLabel("+")
        .setMomentary(true)
        .setBorderRounding(4)
        .setDescription("Add an extra OSC source/destination");
        addTopLevelComponent(addButton);

        // "−" button next to "+" to remove the last extra destination
        UIButton removeButton = (UIButton) new UIButton((int)(w - 62), 4, 16, 12) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    java.util.List<LXOscEngine.OscDestination> dests = lx.engine.osc.getExtraDestinations();
                    if (!dests.isEmpty()) {
                        lx.engine.osc.removeDestination(dests.get(dests.size() - 1));
                    }
                }
            }
        }
        .setLabel("-")
        .setMomentary(true)
        .setBorderRounding(4)
        .setDescription("Remove the last extra OSC source/destination");
        addTopLevelComponent(removeButton);

        // Main (primary) OSC row
        addOscRow(lx.engine.osc.receivePort, lx.engine.osc.receiveHost,
            lx.engine.osc.receiveActive, lx.engine.osc.transmitPort, lx.engine.osc.transmitHost,
            lx.engine.osc.transmitActive);

        // Add rows for any existing extra destinations (e.g. restored from save)
        for (LXOscEngine.OscDestination dest : lx.engine.osc.getExtraDestinations()) {
            addExtraDestRow(dest);
        }

        lx.engine.osc.addDestinationListener(this);
    }

    private UI2dContainer addOscRow(
        heronarts.lx.parameter.DiscreteParameter rxPort,
        heronarts.lx.parameter.StringParameter rxHost,
        heronarts.lx.parameter.BooleanParameter rxActive,
        heronarts.lx.parameter.DiscreteParameter txPort,
        heronarts.lx.parameter.StringParameter txHost,
        heronarts.lx.parameter.BooleanParameter txActive
    ) {
        float bw = getContentWidth();

        UI2dContainer row = (UI2dContainer) new UI2dContainer(0, 0, bw, ROW_HEIGHT)
            .addToContainer(this);

        UI2dContainer border = (UI2dContainer) new UI2dContainer(0, 0, bw, ROW_HEIGHT)
            .setBackgroundColor(ui.theme.getDarkBackgroundColor())
            .setBorderRounding(4)
            .addToContainer(row);

        float yp = 4;
        float portX = 48;
        float portW = 56;
        float hostX = portX + portW + 4;
        float hostW = bw - hostX - 24;
        float activeX = bw - 20;

        new UILabel(6, yp + 2, 40, 12).setLabel("Input").setTextAlignment(PConstants.LEFT, PConstants.CENTER).addToContainer(border);
        new UIIntegerBox((int) portX, (int) yp, (int) portW, 16).setParameter(rxPort).setMappable(false).addToContainer(border);
        new UITextBox((int) hostX, (int) yp, (int) hostW, 16).setParameter(rxHost).addToContainer(border);
        new UIButton((int) activeX, (int) yp, 16, 16).setParameter(rxActive).setMappable(false).setBorderRounding(4).addToContainer(border);

        yp += 20;
        new UILabel(6, yp + 2, 40, 12).setLabel("Output").setTextAlignment(PConstants.LEFT, PConstants.CENTER).addToContainer(border);
        new UIIntegerBox((int) portX, (int) yp, (int) portW, 16).setParameter(txPort).setMappable(false).addToContainer(border);
        new UITextBox((int) hostX, (int) yp, (int) hostW, 16).setParameter(txHost).addToContainer(border);
        new UIButton((int) activeX, (int) yp, 16, 16).setParameter(txActive).setMappable(false).setBorderRounding(4).addToContainer(border);


        return row;
    }

    private void addExtraDestRow(LXOscEngine.OscDestination dest) {
        UI2dContainer row = addOscRow(dest.receivePort, dest.receiveHost,
            dest.receiveActive, dest.transmitPort, dest.transmitHost,
            dest.transmitActive);
        destWidgets.put(dest, row);
    }

    @Override
    public void destinationAdded(LXOscEngine engine, LXOscEngine.OscDestination destination) {
        addExtraDestRow(destination);
    }

    @Override
    public void destinationRemoved(LXOscEngine engine, LXOscEngine.OscDestination destination) {
        UI2dContainer row = destWidgets.remove(destination);
        if (row != null) {
            row.removeFromContainer();
        }
    }
}
