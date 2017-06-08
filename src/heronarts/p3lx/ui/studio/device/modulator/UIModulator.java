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

package heronarts.p3lx.ui.studio.device.modulator;

import java.util.HashMap;
import java.util.Map;

import heronarts.lx.LXModulationEngine;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.LXPeriodicModulator;
import heronarts.lx.parameter.LXCompoundModulation;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIModulationSource;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIColorBox;
import heronarts.p3lx.ui.component.UIDoubleBox;
import heronarts.p3lx.ui.component.UILabel;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;

public abstract class UIModulator extends UI2dContainer {
    protected final LXModulator modulator;

    private static final float PADDING = 4;
    private static final float CONTENT_Y = 16;

    private static final int COLOR_WIDTH = 10;
    protected static final int RUNNING_WIDTH = 12;
    protected static final int LOOP_WIDTH = 18;
    protected static final int TRIGGER_WIDTH = 16;

    private final UI ui;
    private final ColorParameter color;
    private final float lineX;

    private final Map<LXCompoundModulation, UIModulation> uiModulations = new HashMap<LXCompoundModulation, UIModulation>();

    protected UIModulator(UI ui, LXModulator modulator, float x, float y, float w, float h) {
        super(x, y, w, h);
        this.ui = ui;
        this.modulator = modulator;

        this.color = this.modulator.clr;

        new UIButton(0, 0, RUNNING_WIDTH, RUNNING_WIDTH)
        .setParameter(modulator.running)
        .setLabel("")
        .setBorderRounding(4)
        .addToContainer(this);

        if (this.modulator instanceof LXPeriodicModulator) {
            new UIButton(RUNNING_WIDTH + PADDING, 0, TRIGGER_WIDTH, 12)
            .setIcon(ui.theme.iconTrigger)
            .setParameter(this.modulator.trigger)
            .setBorderRounding(4)
            .addToContainer(this);

            new UIButton(2*PADDING + RUNNING_WIDTH + TRIGGER_WIDTH, 0, LOOP_WIDTH, 12)
            .setIcon(ui.theme.iconLoop)
            .setParameter(((LXPeriodicModulator) this.modulator).looping)
            .addToContainer(this);

            this.lineX = 3*PADDING + RUNNING_WIDTH + TRIGGER_WIDTH + LOOP_WIDTH;
        } else {
            this.lineX = PADDING + RUNNING_WIDTH;
        }

        new UIColorBox(ui, this.color, this.width - COLOR_WIDTH, 1, COLOR_WIDTH, COLOR_WIDTH)
        .addToContainer(this);

        UI2dContainer content = new UI2dContainer(0, CONTENT_Y, w, this.height - CONTENT_Y);
        setContentTarget(content);
    }

    public void addModulation(LXCompoundModulation modulation) {
        UIModulation uiModulation = (UIModulation) new UIModulation(modulation, 0, getContentHeight() - UIModulation.HEIGHT, getContentWidth()).addToContainer(this);
        this.uiModulations.put(modulation,  uiModulation);
    }

    public void removeModulation(LXCompoundModulation modulation) {
        UIModulation uiModulation = this.uiModulations.remove(modulation);
        if (uiModulation != null) {
            uiModulation.removeFromContainer();
        }
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        pg.stroke(ui.theme.getControlBackgroundColor());
        pg.line(this.lineX, 6, this.width - PADDING - COLOR_WIDTH, 6);
    }

    public abstract UIModulationSource getModulationSource();

    class UIModulation extends UI2dContainer {

        private static final float PADDING = 4;
        private static final float HEIGHT = 14;
        private static final float AMOUNT_WIDTH = 40;

        private final LXCompoundModulation modulation;

        UIModulation(LXCompoundModulation modulation, float x, float y, float w) {
            super(x, y, w, HEIGHT);

            this.modulation = modulation;

            new UILabel(0, 0, w - PADDING - AMOUNT_WIDTH, HEIGHT)
            .setLabel(modulation.target.getLabel())
            .setPadding(4)
            .setBackgroundColor(ui.theme.getControlBackgroundColor())
            .setBorderColor(ui.theme.getControlBorderColor())
            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
            .addToContainer(this);

            new UIDoubleBox(w - AMOUNT_WIDTH, 0, AMOUNT_WIDTH, HEIGHT)
            .setParameter(modulation.range)
            .addToContainer(this);
        }

        @Override
        public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
            if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE ||
                (keyEvent.isControlDown() || keyEvent.isMetaDown()) && keyCode == java.awt.event.KeyEvent.VK_D) {
                consumeKeyEvent();
                ((LXModulationEngine) modulator.getParent()).removeModulation(this.modulation);
            }
        }
    }


}
