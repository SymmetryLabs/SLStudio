/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
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

package heronarts.p3lx.ui.control;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dContext;
import heronarts.p3lx.ui.UIWindow;
import heronarts.p3lx.ui.component.UIKnob;

public class UICameraControl extends UIWindow {

    public final static int WIDTH = 140;
    public final static int HEIGHT = 72;

    public UICameraControl(UI ui, UI3dContext context, float x, float y) {
        super(ui, "CAMERA", x, y, WIDTH, HEIGHT);

        float xp = 5;
        float yp = UIWindow.TITLE_LABEL_HEIGHT;
        new UIKnob(xp, yp).setParameter(context.perspective).addToContainer(this);
        xp += 34;
        new UIKnob(xp, yp).setParameter(context.depth).addToContainer(this);
    }

}
