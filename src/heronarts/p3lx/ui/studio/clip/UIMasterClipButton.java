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

package heronarts.p3lx.ui.studio.clip;

import heronarts.lx.LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.studio.mixer.UIMixer;
import processing.event.KeyEvent;

public class UIMasterClipButton extends UIClipButton {

    public UIMasterClipButton(UI ui, UIMixer mixer, LX lx, int index, float x, float y) {
        super(ui, mixer, lx, index, x, y);
    }

    @Override
    protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        super.onKeyPressed(keyEvent, keyChar, keyCode);
        if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
            consumeKeyEvent();
            this.mixer.channelStrips.get(this.lx.engine.getChannel(this.lx.engine.channels.size()-1)).clipLauncher.clips.get(this.index).focus();
        }
    }
}
