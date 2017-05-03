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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import heronarts.lx.LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.studio.mixer.UIMixer;
import heronarts.p3lx.ui.studio.mixer.UIMixerStripControls;

public abstract class UIClipLauncher extends UI2dContainer {

    public static final int NUM_CLIPS = 4;
    public static final int WIDTH = UIMixerStripControls.WIDTH;
    public static final int SPACING = 1;
    public static final int HEIGHT = (NUM_CLIPS + 1) * UIClipButton.HEIGHT + NUM_CLIPS * SPACING;

    protected final LX lx;
    protected final UIMixer mixer;

    protected final List<UIClipButton> mutableClips = new ArrayList<UIClipButton>(NUM_CLIPS);
    public final List<UIClipButton> clips = Collections.unmodifiableList(this.mutableClips);

    protected UIClipLauncher(UI ui, UIMixer mixer, LX lx) {
        super(0, 0, WIDTH, HEIGHT);
        this.mixer = mixer;
        this.lx = lx;
        setLayout(UI2dContainer.Layout.VERTICAL);
        setChildMargin(SPACING);
        setArrowKeyFocus(UI2dContainer.ArrowKeyFocus.VERTICAL);
    }
}
