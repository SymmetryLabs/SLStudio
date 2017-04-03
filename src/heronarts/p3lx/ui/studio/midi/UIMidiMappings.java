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
import heronarts.lx.midi.LXMidiMapping;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.UIObject;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UIParameterLabel;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;

public class UIMidiMappings extends UICollapsibleSection {

    private static final int SPACING = 2;
    private final LXMidiEngine midiEngine;
    private final UI ui;

    public UIMidiMappings(UI ui, final LXMidiEngine midiEngine, float x, float y, float w) {
        super(ui, x, y, w, 0);
        setTitle("MAPPINGS");
        this.ui = ui;
        this.midiEngine = midiEngine;

        for (LXMidiMapping mapping : midiEngine.getMappings()) {
            addMapping(mapping);
        }

        midiEngine.addMappingListener(new LXMidiEngine.MappingListener() {
            public void mappingAdded(LXMidiEngine engine, LXMidiMapping mapping) {
                addMapping(mapping);
            }

            public void mappingRemoved(LXMidiEngine engine, LXMidiMapping mapping) {
                removeMapping(mapping);
            }
        });

        setLayout(UI2dContainer.Layout.VERTICAL);
        setChildMargin(SPACING);
    }

    private void addMapping(LXMidiMapping mapping) {
        new UIMidiMapping(ui, mapping, 0, 0, getContentWidth()).addToContainer(this);
    }

    private void removeMapping(LXMidiMapping mapping) {
        for (UIObject child : this) {
            UIMidiMapping mappingUI = (UIMidiMapping) child;
            if (mappingUI.mapping == mapping) {
                mappingUI.removeFromContainer();
                return;
            }
        }
    }

    private class UIMidiMapping extends UI2dContainer implements UIFocus {

        private static final int PADDING = 4;
        private static final int HEIGHT = 20;
        private final LXMidiMapping mapping;

        UIMidiMapping(UI ui, LXMidiMapping mapping, float x, float y, float w) {
            super(x, y, w, HEIGHT);
            this.mapping = mapping;

            setBackgroundColor(ui.theme.getDarkBackgroundColor());

            new UILabel(PADDING, 0, 14, HEIGHT).setLabel(Integer.toString(mapping.channel + 1)).setTextAlignment(PConstants.LEFT, PConstants.CENTER).addToContainer(this);
            new UILabel(20, 0, 30, HEIGHT).setLabel(mapping.getDescription()).setTextAlignment(PConstants.LEFT, PConstants.CENTER).addToContainer(this);
            new UIParameterLabel(60, 0, getContentWidth() - 60 - PADDING, HEIGHT).setParameter(mapping.parameter).setTextAlignment(PConstants.LEFT, PConstants.CENTER).addToContainer(this);
        }

        @Override
        public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
            // TODO(mcslee): add up/down key navigation thru this list of items
            if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE ||
                    ((keyEvent.isControlDown() || keyEvent.isMetaDown()) && keyCode == java.awt.event.KeyEvent.VK_D)) {
                midiEngine.removeMapping(this.mapping);
                // TODO(mcslee): send keyfocus to neighbor
            }
            if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
                getUI().focusNext();
            } else if (keyCode == java.awt.event.KeyEvent.VK_UP) {
                getUI().focusPrev();
            }
        }

        @Override
        public void drawFocus(UI ui, PGraphics pg) {
            pg.noFill();
            // TODO(mcslee): this color should be in UITheme...
            pg.stroke(0xff555555);
            pg.rect(0, 0, width-1, height-1);
        }
    }
}
