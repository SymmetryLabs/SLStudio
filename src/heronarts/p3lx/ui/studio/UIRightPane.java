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

package heronarts.p3lx.ui.studio;

import heronarts.lx.LX;
import heronarts.lx.LXModulationEngine;
import heronarts.lx.audio.BandGate;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.VariableLFO;
import heronarts.lx.parameter.LXParameterModulation;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UI2dScrollContext;
import heronarts.p3lx.ui.UIObject;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.studio.midi.UIMidiManager;
import heronarts.p3lx.ui.studio.midi.UIMidiMappings;
import heronarts.p3lx.ui.studio.modulation.UIBandGate;
import heronarts.p3lx.ui.studio.modulation.UIModulationMatrix;
import heronarts.p3lx.ui.studio.modulation.UIModulator;
import heronarts.p3lx.ui.studio.modulation.UIVariableLFO;
import processing.core.PConstants;

public class UIRightPane extends UIPane {

    private final LX lx;
    private final UI ui;

    public final UI2dScrollContext midi;
    public final UI2dScrollContext modulation;

    public static final int PADDING = 4;
    public static final int WIDTH = 244;

    private int lfoCount = 1;
    private int beatCount = 1;

    public UIRightPane(UI ui, final LX lx) {
        super(ui, lx, new String[] { "MODULATION", "MIDI" }, ui.getWidth() - WIDTH, WIDTH);
        this.ui = ui;
        this.lx = lx;
        this.modulation = this.sections[0];
        this.midi = this.sections[1];

        new UIMidiManager(ui, lx.engine.midi, 0, 0, this.midi.getContentWidth(), 144).addToContainer(this.midi);
        new UIMidiMappings(ui, lx.engine.midi, 0, 0, this.midi.getContentWidth()).addToContainer(this.midi);

        UI2dContainer bar = new UI2dContainer(0, 0, this.modulation.getContentWidth(), 16);
        bar.addToContainer(this.modulation);

        new UILabel(0, 0, 24, 16)
        .setLabel("Add")
        .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
        .addToContainer(bar);

        new UIButton(28, 0, 48, 16) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    VariableLFO lfo = new VariableLFO("LFO " + lfoCount++);
                    lx.engine.modulation.addModulator(lfo);
                    lfo.start();
                }
            }
        }
        .setLabel("LFO")
        .setMomentary(true)
        .setInactiveColor(ui.theme.getWindowBackgroundColor())
        .setBorderRounding(4)
        .addToContainer(bar);

        new UIButton(80, 0, 48, 16) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    BandGate beatDetect = new BandGate("Beat " + beatCount++, lx);
                    lx.engine.modulation.addModulator(beatDetect);
                    beatDetect.start();
                }
            }
        }
        .setLabel("Beat")
        .setMomentary(true)
        .setInactiveColor(ui.theme.getWindowBackgroundColor())
        .setBorderRounding(4)
        .addToContainer(bar);

        new UIModulationMatrix(ui, lx, 0, 0, this.modulation.getContentWidth()).addToContainer(this.modulation);

        for (LXModulator modulator : lx.engine.modulation.getModulators()) {
            addModulator(modulator);
        }

        lx.engine.modulation.addListener(new LXModulationEngine.Listener() {
            public void modulationAdded(LXModulationEngine engine, LXParameterModulation modulation) {}
            public void modulationRemoved(LXModulationEngine engine, LXParameterModulation modulation) {}
            public void modulatorAdded(LXModulationEngine engine, LXModulator modulator) {
                addModulator(modulator);
            }
            public void modulatorRemoved(LXModulationEngine engine, LXModulator modulator) {
                removeModulator(modulator);
            }
        });
    }

    @Override
    protected void onUIResize(UI ui) {
        setX(ui.getWidth() - WIDTH);
        super.onUIResize(ui);
    }

    private void addModulator(LXModulator modulator) {
        if (modulator instanceof VariableLFO) {
            new UIVariableLFO(this.ui, this.lx, (VariableLFO) modulator, 0, 0, this.modulation.getContentWidth()).addToContainer(this.modulation, 1);
        } else if (modulator instanceof BandGate) {
            new UIBandGate(this.ui, this.lx, (BandGate) modulator, 0, 0, this.modulation.getContentWidth()).addToContainer(this.modulation, 1);
        } else {
            System.err.println("No UI available for modulator type: " + modulator.getClass().getName());
        }
    }

    private void removeModulator(LXModulator modulator) {
        for (UIObject child : this.modulation) {
            if (child instanceof UIModulator) {
                UIModulator modulatorUI = (UIModulator) child;
                if (modulatorUI.modulator == modulator) {
                    modulatorUI.removeFromContainer();
                    return;
                }
            }
        }
    }
}
