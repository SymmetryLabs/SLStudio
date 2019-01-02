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

public class UISceneLauncher extends UI2dContainer {

    public static final float WIDTH = 20;

    private final List<UISceneButton> mutableScenes = new ArrayList<UISceneButton>();
    public final List<UISceneButton> scenes = Collections.unmodifiableList(this.mutableScenes);
    public final UISceneStop stop;

    public UISceneLauncher(UI ui, UIMixer mixer, LX lx, float x, float y) {
        super(x, y, WIDTH, UIClipLauncher.HEIGHT);
        setLayout(UI2dContainer.Layout.VERTICAL);
        setChildMargin(UIClipLauncher.SPACING);
        setArrowKeyFocus(UI2dContainer.ArrowKeyFocus.VERTICAL);
        for (int i = 0; i < UIClipLauncher.NUM_CLIPS; ++i) {
            UISceneButton scene = new UISceneButton(ui, mixer, lx, i);
            this.mutableScenes.add(scene);
            scene.addToContainer(this);
        }
        this.stop = (UISceneStop) new UISceneStop(ui, mixer, lx).addToContainer(this);
    }
}
