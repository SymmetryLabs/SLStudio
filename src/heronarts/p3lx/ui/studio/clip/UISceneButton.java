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
import heronarts.lx.parameter.BooleanParameter;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.UITriggerTarget;
import heronarts.p3lx.ui.studio.mixer.UIMixer;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UISceneButton extends UI2dComponent implements UIFocus, UITriggerTarget {

    private final UIMixer mixer;
    private final LX lx;
    private final int index;
    private boolean playButtonDown = false;

    public UISceneButton(UI ui, UIMixer mixer, LX lx, int index) {
        super(0, 0, UISceneLauncher.WIDTH, UIClipButton.HEIGHT);
        this.mixer = mixer;
        this.lx = lx;
        this.index = index;
        setBorderColor(ui.theme.getControlBorderColor());
        setBackgroundColor(ui.theme.getControlBackgroundColor());
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        pg.noStroke();
        pg.fill(this.playButtonDown ? ui.theme.getPrimaryColor() : ui.theme.getControlDisabledColor());
        UIClipButton.drawPlayTriangle(ui, pg);
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        super.onKeyPressed(keyEvent, keyChar, keyCode);
        if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
            consumeKeyEvent();
            this.mixer.masterStrip.clipLauncher.clips.get(this.index).focus();
        } else if (keyCode == java.awt.event.KeyEvent.VK_SPACE) {
            consumeKeyEvent();
            this.lx.engine.launchScene(this.index);
            this.playButtonDown = true;
            redraw();
        }
    }

    @Override
    public void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (this.playButtonDown && keyCode == java.awt.event.KeyEvent.VK_SPACE) {
            this.playButtonDown = false;
            redraw();
        }
    }

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        this.lx.engine.launchScene(this.index);
        this.playButtonDown = true;
        redraw();
    }

    @Override
    public void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {
        if (this.playButtonDown) {
            this.playButtonDown = false;
            redraw();
        }
    }

    @Override
    public BooleanParameter getTriggerTarget() {
        return lx.engine.getScene(this.index);
    }
}
