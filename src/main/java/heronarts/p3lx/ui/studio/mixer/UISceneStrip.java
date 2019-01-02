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
import heronarts.p3lx.LXStudio;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.studio.clip.UIClipLauncher;
import heronarts.p3lx.ui.studio.clip.UISceneLauncher;
import processing.core.PGraphics;
import processing.event.MouseEvent;

public class UISceneStrip extends UI2dContainer {

    public final UISceneLauncher sceneLauncher;
    public final UIClipViewToggle clipViewToggle;

    public UISceneStrip(UI ui, UIMixer mixer, LX lx) {
        super(0, UIMixer.PADDING, UISceneLauncher.WIDTH, UIMixerStrip.HEIGHT);
        this.sceneLauncher = (UISceneLauncher) new UISceneLauncher(ui, mixer, lx, 0, 0).addToContainer(this);
        this.clipViewToggle = (UIClipViewToggle) new UIClipViewToggle().addToContainer(this);
    }

    public class UIClipViewToggle extends UI2dComponent {

        private boolean clipViewVisible = true;

        private UIClipViewToggle() {
            super(0, UIClipLauncher.HEIGHT + UIMixerStrip.SPACING, UISceneLauncher.WIDTH, UIMixerStripControls.HEIGHT);
            setBackgroundColor(0xff393939);
        }

        @Override
        protected void onDraw(UI ui, PGraphics pg) {
            pg.noStroke();
            pg.fill(this.clipViewVisible ? ui.theme.getControlDisabledColor() : ui.theme.getDarkBackgroundColor());
            for (int x = 0; x < 3; ++x) {
                for (int y = 0; y < 3; ++y) {
                    pg.rect(3 + 5*x, 3 + 5*y, 3, 3);
                }
            }
        }

        @Override
        public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
            UI ui = getUI();
            if (ui instanceof LXStudio.UI) {
                this.clipViewVisible = ((LXStudio.UI) ui).toggleClipView();
                redraw();
            }
        }
    }

}
