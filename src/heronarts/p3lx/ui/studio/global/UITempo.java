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
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.p3lx.ui.studio.global;

import heronarts.lx.LX;
import heronarts.lx.Tempo;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UITimerTask;
import heronarts.p3lx.ui.UITriggerSource;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIDoubleBox;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.MouseEvent;

public class UITempo extends UI2dContainer {

    private static final int PADDING = 4;

    public UITempo(UI ui, final LX lx, float x, float y, float w, float h) {
        super(x, y, w, h);
        setBackgroundColor(ui.theme.getDeviceBackgroundColor());
        setBorderRounding(4);

        new UIButton(PADDING, PADDING, 36, 18)
        .setParameter(lx.tempo.tap)
        .setLabel("TAP")
        .setMomentary(true)
        .addToContainer(this);

        new UIDoubleBox(42, PADDING, 72, 18)
        .setParameter(lx.tempo.bpm)
        .setShiftMultiplier(.1f)
        .addToContainer(this);

        new UIButton(116, PADDING, 20, 18)
        .setParameter(lx.tempo.nudgeDown)
        .setLabel("◄")
        .setMomentary(true)
        .addToContainer(this);

         new UIButton(138, PADDING, 20, 18)
        .setParameter(lx.tempo.nudgeUp)
        .setLabel("►")
        .setMomentary(true)
        .addToContainer(this);

        new Blinker(lx.tempo, 160, PADDING, 18, 18).addToContainer(this);
    }

    private class Blinker extends UI2dComponent implements UITriggerSource {

        private final Tempo tempo;

        Blinker(final Tempo tempo, float x, float y, float w, float h) {
            super(x, y, w, h);
            this.tempo = tempo;
            addLoopTask(new UITimerTask(14, UITimerTask.Mode.FPS) {
                @Override
                public void run() {
                    if (tempo.enabled.isOn()) {
                        redraw();
                    }
                }
            });
        }

        @Override
        public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
            if (mouseEvent.isControlDown() || mouseEvent.isMetaDown()) {
                this.getUI().mapTriggerSource(this);
            } else {
                this.tempo.enabled.toggle();
            }
            redraw();
        }

        @Override
        public void onDraw(UI ui, PGraphics pg) {
            pg.noStroke();
            if (this.tempo.enabled.isOn()) {
                int fill = ui.theme.getPrimaryColor();
                fill = (fill & 0xffffff) | (((int) ((1 - this.tempo.ramp()) * 0xff)) << 24);
                pg.fill(fill);
            } else {
                pg.fill(ui.theme.getControlDisabledColor());
            }
            pg.ellipseMode(PConstants.CENTER);
            pg.ellipse(this.width/2, this.height/2, 8, 8);
        }

        @Override
        public BooleanParameter getTriggerSource() {
            return this.tempo.trigger;
        }
    }
}