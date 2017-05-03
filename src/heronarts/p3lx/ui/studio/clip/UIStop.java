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

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public abstract class UIStop extends UI2dComponent implements UIFocus {

    private static final int STOP_BUTTON_WIDTH = 20;

    private boolean stopButtonDown = false;

    protected UIStop(UI ui, float width) {
        super(0, 0, width, UIClipButton.HEIGHT);
        setBackgroundColor(ui.theme.getDarkBackgroundColor());
        setBorderColor(ui.theme.getControlBorderColor());
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        pg.stroke(ui.theme.getControlBorderColor());
        pg.fill(this.stopButtonDown ? ui.theme.getPrimaryColor() : ui.theme.getControlDisabledColor());
        pg.rect(4, 5, 9, 9);
    }


    protected abstract void stop();

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        if (mx < STOP_BUTTON_WIDTH) {
            stop();
            this.stopButtonDown = true;
            redraw();
        }
    }

    @Override
    public void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {
        if (this.stopButtonDown) {
            this.stopButtonDown = false;
            redraw();
        }
    }

    @Override
    protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_SPACE) {
            consumeKeyEvent();
            stop();
            this.stopButtonDown = true;
            redraw();
        }
    }

    @Override
    protected void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (this.stopButtonDown && keyCode == java.awt.event.KeyEvent.VK_SPACE) {
            this.stopButtonDown = false;
            redraw();
        }
    }
}
