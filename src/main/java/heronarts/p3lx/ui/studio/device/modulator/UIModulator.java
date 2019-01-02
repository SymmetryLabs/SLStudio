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

import java.util.concurrent.CopyOnWriteArrayList;

import heronarts.lx.LXComponent;
import heronarts.lx.LXModulationEngine;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.LXPeriodicModulator;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXCompoundModulation;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIFocus;
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
    private final float lineX;

    private final UIModulations uiModulations;

    protected UIModulator(UI ui, LXModulator modulator, float x, float y, float w, float h) {
        super(x, y, w, h);
        this.ui = ui;
        this.modulator = modulator;

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

        new UIColorBox(ui, this.modulator.modulationColor, this.width - COLOR_WIDTH, 1, COLOR_WIDTH, COLOR_WIDTH)
        .addToContainer(this);

        this.uiModulations = (UIModulations)
            new UIModulations(0, getContentHeight() - UIModulations.HEIGHT, getContentWidth())
            .addToContainer(this);

        UI2dContainer content = new UI2dContainer(0, CONTENT_Y, w, this.height - CONTENT_Y - UIModulations.HEIGHT);
        setContentTarget(content);
    }

    public void addModulation(LXCompoundModulation modulation) {
        this.uiModulations.addModulation(modulation);
    }

    public void removeModulation(LXCompoundModulation modulation) {
        this.uiModulations.removeModulation(modulation);
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        pg.stroke(ui.theme.getControlBackgroundColor());
        pg.line(this.lineX, 6, this.width - PADDING - COLOR_WIDTH, 6);
    }

    public abstract UIModulationSource getModulationSource();

    class UIModulations extends UI2dContainer {

        private static final float PADDING = 4;
        private static final float HEIGHT = 14;
        private static final float POLARITY_WIDTH = 24;
        private static final float AMOUNT_WIDTH = 40;

        private int modulationIndex = 0;

        private final CopyOnWriteArrayList<LXCompoundModulation> modulations =
            new CopyOnWriteArrayList<LXCompoundModulation>();

        private final UILabel label;
        private final UIDoubleBox range;
        private final UIButton polarity;

        UIModulations(float x, float y, float w) {
            super(x, y, w, HEIGHT);

            this.label = (UILabel) new UIModulationLabel(0, 0, w - 2*PADDING - AMOUNT_WIDTH - POLARITY_WIDTH, HEIGHT)
            .setLabel("")
            .setPadding(4)
            .setBackgroundColor(ui.theme.getControlBackgroundColor())
            .setBorderColor(ui.theme.getControlBorderColor())
            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
            .setFont(ui.theme.getControlFont())
            .setVisible(false);

            this.label.addToContainer(this);

            this.polarity = (UIButton) new UIButton(w - AMOUNT_WIDTH - PADDING - POLARITY_WIDTH, 0, POLARITY_WIDTH, HEIGHT)
            .setVisible(false);

            this.polarity.addToContainer(this);

            this.range = (UIDoubleBox) new UIDoubleBox(w - AMOUNT_WIDTH, 0, AMOUNT_WIDTH, HEIGHT)
            .setEnabled(false)
            .setVisible(false);

            this.range.addToContainer(this);
        }

        class UIModulationLabel extends UILabel implements UIFocus {

            UIModulationLabel(float x, float y, float w, float h) {
                super(x, y, w, h);
            }

            @Override
            public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
                if (keyCode == java.awt.event.KeyEvent.VK_UP) {
                    consumeKeyEvent();
                    selectModulation(-1);
                } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
                    consumeKeyEvent();
                    selectModulation(1);
                }
            }
        }

        private void removeModulation() {
            if (this.modulationIndex < this.modulations.size()) {
                LXCompoundModulation modulation = this.modulations.get(this.modulationIndex);
                ((LXModulationEngine) modulator.getParent()).removeModulation(modulation);
            }
        }

        private void addModulation(LXCompoundModulation modulation) {
            this.modulations.add(modulation);
            this.modulationIndex = this.modulations.size() - 1;
            selectModulation(0);
        }

        private void removeModulation(LXCompoundModulation modulation) {
            int index = this.modulations.indexOf(modulation);
            if (index >= 0) {
                this.modulations.remove(index);
                if (this.modulationIndex >= index) {
                    this.modulationIndex = Math.max(0, this.modulationIndex - 1);
                    selectModulation(0);
                }
            }
        }

        private void selectModulation(int delta) {
            int numModulations = this.modulations.size();
            if (numModulations > 0) {
                this.modulationIndex = (this.modulationIndex + delta + numModulations) % numModulations;
            } else {
                this.modulationIndex = 0;
            }
            if (this.modulationIndex < numModulations) {
                LXCompoundModulation modulation = this.modulations.get(this.modulationIndex);
                LXComponent device = modulator.getParent().getParent();
                this.label.setLabel(LXComponent.getCanonicalLabel(modulation.target, device));
                this.label.setVisible(true);
                this.polarity.setParameter(modulation.polarity);
                this.polarity.setVisible(true);
                this.range.setParameter(modulation.range);
                this.range.setEnabled(true);
                this.range.setVisible(true);
            } else {
                this.label.setVisible(false);
                this.label.setLabel("");
                this.polarity.setVisible(false);
                this.polarity.setParameter((BooleanParameter) null);
                this.range.setVisible(false);
                this.range.setParameter(null);
                this.range.setEnabled(false);

            }
        }
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE ||
            (keyEvent.isControlDown() || keyEvent.isMetaDown()) && keyCode == java.awt.event.KeyEvent.VK_D) {
            consumeKeyEvent();
            this.uiModulations.removeModulation();
        }
    }

}
