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

package heronarts.p3lx.ui.studio;

import heronarts.lx.midi.LXMidiEngine;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIItemList;

public class UIMidiManager extends UICollapsibleSection {

    private final UIItemList midiInputs;

    public UIMidiManager(UI ui, final LXMidiEngine midiEngine, float x, float y, float w, float h) {
        super(ui, x, y, w, h);
        setTitle("DEVICES");

        this.midiInputs = new UIItemList(ui, 0, 0, getContentWidth(), getContentHeight());
        this.midiInputs.addToContainer(this);

        midiEngine.whenReady(new Runnable() {
            public void run() {
                for (LXMidiInput input : midiEngine.getInputs()) {
                    midiInputs.addItem(new MidiListItem(input));
                }
            }
        });
    }

    private class MidiListItem extends UIItemList.AbstractItem implements LXParameterListener {

        private final LXMidiInput input;

        MidiListItem(LXMidiInput input) {
            this.input = input;
            input.enabled.addListener(this);
        }

        @Override
        public void onParameterChanged(LXParameter p) {
            redraw();
        }

        @Override
        public boolean isActive() {
            return this.input.enabled.isOn();
        }

        @Override
        public int getActiveColor(UI ui) {
            return ui.theme.getPrimaryColor();
        }

        @Override
        public void onActivate() {
            this.input.enabled.toggle();
            midiInputs.redraw();
        }

        public String getLabel() {
            return this.input.getDescription();
        }
    }
}

