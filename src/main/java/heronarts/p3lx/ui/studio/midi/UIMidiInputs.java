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
import heronarts.lx.midi.LXMidiInput;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import processing.core.PConstants;
import processing.core.PGraphics;

public class UIMidiInputs extends UICollapsibleSection {

    public UIMidiInputs(final UI ui, final LXMidiEngine midiEngine, float x, float y, float w) {
        super(ui, x, y, w, 0);
        setTitle("MIDI INPUT");
        setLayout(UI2dContainer.Layout.VERTICAL);
        setChildMargin(2);
        setArrowKeyFocus(UI2dContainer.ArrowKeyFocus.VERTICAL);

        midiEngine.whenReady(new Runnable() {
            public void run() {
                for (LXMidiInput input : midiEngine.getInputs()) {
                    new UIMidiInput(ui, input, getContentWidth()).addToContainer(UIMidiInputs.this);
                }
            }
        });
    }

    private class UIMidiInput extends UI2dContainer implements UIFocus {

        private static final int HEIGHT = 24;
        private static final int PADDING = 4;
        private static final int BUTTON_WIDTH = 16;

        UIMidiInput(UI ui, LXMidiInput input, float w) {
            super(0, 0, w, HEIGHT);
            setBackgroundColor(ui.theme.getDarkBackgroundColor());
            setBorderRounding(4);

            float buttonX = w - 3*(PADDING + BUTTON_WIDTH);

            new UILabel(PADDING, PADDING, buttonX - 2*PADDING, 16)
            .setLabel(input.getDescription())
            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
            .addToContainer(this);

            new UIButton(buttonX, PADDING, BUTTON_WIDTH, 16)
            .setParameter(input.channelEnabled)
            .setIcon(ui.theme.iconNote)
            .setMappable(false)
            .setBorder(false)
            .setFocusColor(ui.theme.getCursorColor())
            .addToContainer(this);
            buttonX += BUTTON_WIDTH + PADDING;

            new UIButton(buttonX, PADDING, BUTTON_WIDTH, 16)
            .setParameter(input.controlEnabled)
            .setIcon(ui.theme.iconMap)
            .setMappable(false)
            .setBorder(false)
            .setFocusColor(ui.theme.getCursorColor())
            .addToContainer(this);
            buttonX += BUTTON_WIDTH + PADDING;

            new UIButton(buttonX, PADDING, BUTTON_WIDTH, 16)
            .setParameter(input.syncEnabled)
            .setIcon(ui.theme.iconTempo)
            .setMappable(false)
            .setBorder(false)
            .setFocusColor(ui.theme.getCursorColor())
            .addToContainer(this);
            buttonX += BUTTON_WIDTH + PADDING;
        }

        @Override
        public void drawFocus(UI ui, PGraphics pg) {
            pg.stroke(ui.theme.getPrimaryColor());
            pg.line(0, 0, 0, this.height-1);
        }

    }

}

