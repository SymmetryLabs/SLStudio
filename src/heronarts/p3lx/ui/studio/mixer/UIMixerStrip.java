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
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public abstract class UIMixerStrip extends UI2dContainer {

    public final static int MARGIN = UIMixer.PADDING;
    public final static int PADDING = 2;
    public final static int WIDTH = 72;
    public final static int SPACING = 1;

    private final static int HEIGHT = 168;

    protected final static int FADER_WIDTH = 22;
    protected final static int FADER_HEIGHT = 80;

    private final UI ui;
    protected final LX lx;
    protected final LXBus bus;

    UIMixerStrip(final UI ui, final LX lx, final LXBus bus, float x) {
        super(x, MARGIN, WIDTH, HEIGHT);
        this.ui = ui;
        this.lx = lx;
        this.bus = bus;
        setBackground();

        lx.engine.focusedChannel.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (lx.engine.getFocusedChannel() == bus) {
                    if (!hasFocus()) {
                        focus();
                    }
                }
                setBackground();
            }
        });
    }

    protected void setBackground() {
        if (lx.engine.getFocusedChannel() == this.bus) {
            setBackgroundColor(this.ui.theme.getWindowFocusedBackgroundColor());
        } else {
            setBackgroundColor(this.ui.theme.getWindowBackgroundColor());
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
            lx.engine.setFocusedChannel(this.bus);
        }
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (!(keyEvent.isMetaDown() || keyEvent.isControlDown())) {
            if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
                if (lx.engine.focusedChannel.getValuei() > 0) {
                    consumeKeyEvent();
                    lx.engine.focusedChannel.decrement();
                }
            } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
                if (lx.engine.focusedChannel.getValuei() < lx.engine.focusedChannel.getRange() - 1) {
                    consumeKeyEvent();
                    lx.engine.focusedChannel.increment();
                }
            }
        }
    }

}