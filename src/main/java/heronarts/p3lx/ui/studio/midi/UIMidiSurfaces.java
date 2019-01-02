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

package heronarts.p3lx.ui.studio.midi;

import heronarts.lx.midi.LXMidiEngine;
import heronarts.lx.midi.surface.LXMidiSurface;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import processing.core.PConstants;

public class UIMidiSurfaces extends UICollapsibleSection {
    public UIMidiSurfaces(final UI ui, final LXMidiEngine midiEngine, float x, float y, float w) {
        super(ui, x, y, w, 0);
        setTitle("MIDI SURFACES");
        setLayout(UI2dContainer.Layout.VERTICAL);
        setChildMargin(2);
        setArrowKeyFocus(UI2dContainer.ArrowKeyFocus.VERTICAL);
        setVisible(false);

        midiEngine.whenReady(new Runnable() {
            public void run() {
                for (LXMidiSurface surface : midiEngine.surfaces) {
                    new UIMidiSurface(ui, surface, getContentWidth()).addToContainer(UIMidiSurfaces.this);
                }
                setVisible(midiEngine.surfaces.size() > 0);
            }
        });
    }

    class UIMidiSurface extends UI2dContainer {
        private static final int HEIGHT = 24;
        private static final int PADDING = 4;
        private static final int BUTTON_WIDTH = 16;

        UIMidiSurface(UI ui, LXMidiSurface surface, float w) {
            super(0, 0, w, HEIGHT);
            setBackgroundColor(ui.theme.getDarkBackgroundColor());
            setBorderRounding(4);

            new UILabel(PADDING, PADDING, w - 3*PADDING - BUTTON_WIDTH, 16)
            .setLabel(surface.getDescription())
            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
            .addToContainer(this);

            new UIButton(w - PADDING - BUTTON_WIDTH, PADDING, BUTTON_WIDTH, 16)
            .setParameter(surface.enabled)
            .setIcon(ui.theme.iconControl)
            .setBorder(false)
            .setFocusColor(0xff555555)
            .setMappable(false)
            .addToContainer(this);
        }
    }
}
