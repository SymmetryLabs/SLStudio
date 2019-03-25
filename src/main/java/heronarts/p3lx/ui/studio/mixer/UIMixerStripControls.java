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
import heronarts.lx.LXBus;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.LXStudio;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import heronarts.lx.LXLook;

public abstract class UIMixerStripControls extends UI2dContainer {

    public final static int PADDING = 2;
    public final static int WIDTH = UIMixerStrip.WIDTH;

    public final static int HEIGHT = 168;

    protected final static int FADER_WIDTH = 22;
    protected final static int FADER_HEIGHT = 80;

    protected final static int ACTIVE_BUTTON_Y = 28;

    protected final UI ui;
    protected final LX lx;
    protected final LXBus bus;

    UIMixerStripControls(final UI ui, final LX lx, final LXBus bus) {
        super(0, 0, WIDTH, HEIGHT);
        this.ui = ui;
        this.lx = lx;
        this.bus = bus;

        setBackground();
        this.lx.engine.getFocusedLook().focusedChannel.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                setBackground();
            }
        });
    }

    protected UIMixer getMixer() {
        return getMixerStrip().getMixer();
    }

    protected UIMixerStrip getMixerStrip() {
        return (UIMixerStrip) getParent();
    }

    protected void setBackground() {
        if (lx.engine.getFocusedChannel() == this.bus) {
            setBackgroundColor(this.ui.theme.getDeviceFocusedBackgroundColor());
        } else {
            setBackgroundColor(this.ui.theme.getDeviceBackgroundColor());
        }
    }

    @Override
    public void onDraw(UI ui, PGraphics pg) {
        pg.stroke(0xff333333);
        pg.line(1, 20, this.width-2, 20);
        pg.line(1, this.height - 64, this.width-2, this.height - 64);
    }

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        if (my < 24) {
            this.lx.engine.setFocusedChannel(this.bus);
        }
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        LXLook look = lx.engine.getFocusedLook();
        if (!(keyEvent.isMetaDown() || keyEvent.isControlDown())) {
            if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
                if (look.focusedChannel.getValuei() > 0) {
                    consumeKeyEvent();
                    look.focusedChannel.decrement();
                    getMixer().focusStrip(look.getFocusedChannel());
                }
            } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
                if (look.focusedChannel.getValuei() < look.focusedChannel.getRange() - 1) {
                    consumeKeyEvent();
                    look.focusedChannel.increment();
                    getMixer().focusStrip(look.getFocusedChannel());
                }
            }
        }
    }

}
