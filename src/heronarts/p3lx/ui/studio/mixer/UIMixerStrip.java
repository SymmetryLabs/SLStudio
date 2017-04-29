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
import heronarts.p3lx.ui.UI2dContainer;

public abstract class UIMixerStrip extends UI2dContainer {

    public final static int SPACING = 6;
    public final static int HEIGHT = UIMixerStripControls.HEIGHT + SPACING + UIClipLauncher.HEIGHT;
    public final static int WIDTH = 72;

    protected final LX lx;
    protected final UIClipLauncher clipLauncher;
    protected final UIMixerStripControls controls;

    protected UIMixerStrip(final UI ui, final LX lx, float x, float y) {
        super(x, y, WIDTH, HEIGHT);
        this.lx = lx;
        this.clipLauncher = (UIClipLauncher) new UIClipLauncher(ui, lx, lx.engine.masterChannel).addToContainer(this);
        this.controls = (UIMixerStripControls) new UIMasterStripControls(ui, lx).addToContainer(this);
    }

    protected UIMixerStrip(final UI ui, final LX lx, LXChannel channel, float x, float y) {
        super(x, y, WIDTH, HEIGHT);
        this.lx = lx;
        this.clipLauncher = (UIClipLauncher) new UIClipLauncher(ui, lx, channel).addToContainer(this);
        this.controls = (UIMixerStripControls) new UIChannelStripControls(ui, lx, channel).addToContainer(this);
    }

}
