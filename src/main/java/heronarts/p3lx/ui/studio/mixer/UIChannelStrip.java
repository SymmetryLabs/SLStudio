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

package heronarts.p3lx.ui.studio.mixer;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIObject;

public class UIChannelStrip extends UIMixerStrip {
    private boolean isSelected = false;
    protected final LXChannel channel;

    @Override
    public void onDraw(UI ui, processing.core.PGraphics pg) {
        UIObject parent = this.getParent();
        UIMixer mixer = null;
        if (parent instanceof UIMixer) {
            mixer = (UIMixer) parent;
        } else if (parent instanceof UILookStrip) {
            mixer = ((UILookStrip) parent).getMixer();
        }
        boolean isSelected = (mixer != null) && mixer.isChannelSelected(this.channel);
        if (isSelected) {
            pg.pushStyle();
            pg.noStroke();
            pg.fill(0xFF3399FF, 64); // semi-transparent blue
            pg.rect(0, 0, this.getWidth(), this.getHeight());
            pg.popStyle();
        }
        super.onDraw(ui, pg);
    }

    // To select, call this method from parent on mouse event
    public void select(boolean shift) {
        UIMixer mixer = (UIMixer) this.getParent();
        mixer.toggleChannelSelection(this.channel, shift);
    }

    @Override
    public void onMousePressed(processing.event.MouseEvent mouseEvent, float mx, float my) {
        System.out.println("UIChannelStrip.onMousePressed: channel=" + channel.getLabel() + ", shift=" + mouseEvent.isShiftDown());
        select(mouseEvent.isShiftDown());
        super.onMousePressed(mouseEvent, mx, my);
    }

    public UIChannelStrip(UI ui, UIMixer mixer, LX lx, LXChannel channel) {
        super(ui, mixer, lx, channel, 0, UIMixer.PADDING);
        this.channel = channel;
    }
}
