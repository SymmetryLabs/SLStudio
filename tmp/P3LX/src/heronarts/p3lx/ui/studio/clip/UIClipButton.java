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
import heronarts.lx.clip.LXClip;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.studio.mixer.UIMixer;
import processing.core.PConstants;
import processing.core.PGraphics;

public abstract class UIClipButton extends UI2dContainer implements UIFocus {

    public static final int HEIGHT = 20;
    public static final int PADDING = 2;
    protected static final int LABEL_X = 16;

    protected final UIMixer mixer;
    protected final LX lx;
    protected final int index;

    protected UIClipButton(UI ui, UIMixer mixer, LX lx, int index, float x, float y) {
        super(x, y, UIClipLauncher.WIDTH, HEIGHT);
        this.mixer = mixer;
        this.lx = lx;
        this.index = index;
        setBorderColor(ui.theme.getControlBorderColor());
        setBackgroundColor(ui.theme.getControlBackgroundColor());
    }

    protected void setClip(LXClip clip) {}

    protected void drawPlayTriangle(UI ui, PGraphics pg) {
        pg.stroke(ui.theme.getControlBorderColor());
        pg.beginShape();
        pg.vertex(4, 4);
        pg.vertex(14, 10);
        pg.vertex(4, 16);
        pg.endShape(PConstants.CLOSE);

    }

}
