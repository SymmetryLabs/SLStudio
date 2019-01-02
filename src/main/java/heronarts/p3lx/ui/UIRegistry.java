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

package heronarts.p3lx.ui;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import heronarts.lx.audio.BandGate;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.MacroKnobs;
import heronarts.lx.modulator.MultiStageEnvelope;
import heronarts.lx.modulator.VariableLFO;
import heronarts.p3lx.ui.studio.modulation.UIBandGate;
import heronarts.p3lx.ui.studio.modulation.UIMacroKnobs;
import heronarts.p3lx.ui.studio.modulation.UIModulator;
import heronarts.p3lx.ui.studio.modulation.UIMultiStageEnvelope;
import heronarts.p3lx.ui.studio.modulation.UIVariableLFO;

public class UIRegistry {

    private final Map<Class<? extends LXModulator>, UIModulator.Factory<? extends LXModulator>> modulatorUIRegistry =
        new HashMap<Class<? extends LXModulator>, UIModulator.Factory<? extends LXModulator>>();

    private final Deque<Class<? extends LXModulator>> modulatorClasses = new LinkedList<Class<? extends LXModulator>>();

    UIRegistry() {
        registerModulatorUI(VariableLFO.class, UIVariableLFO.class);
        registerModulatorUI(MultiStageEnvelope.class, UIMultiStageEnvelope.class);
        registerModulatorUI(BandGate.class, UIBandGate.class);
        registerModulatorUI(MacroKnobs.class, UIMacroKnobs.class);
    }

    public <T extends LXModulator> void registerModulatorUI(Class<T> modulatorClass, Class<? extends UIModulator> uiClass) {
        registerModulatorUI(modulatorClass, new UIModulator.DefaultFactory<T>(modulatorClass, uiClass));
    }

    public <T extends LXModulator> void registerModulatorUI(Class<T> modulatorClass, UIModulator.Factory<T> uiFactory) {
        if (!this.modulatorUIRegistry.containsKey(modulatorClass)) {
            this.modulatorClasses.addFirst(modulatorClass);
        }
        this.modulatorUIRegistry.put(modulatorClass, uiFactory);
    }

    public UIModulator.Factory<? extends LXModulator> getModulatorUIFactory(LXModulator modulator) {
        Class<? extends LXModulator> modulatorClass = modulator.getClass();
        UIModulator.Factory<? extends LXModulator> uiFactory = this.modulatorUIRegistry.get(modulatorClass);
        if (uiFactory == null) {
            // If direct hash lookup fails, fall back to instance checks
            for (Class<? extends LXModulator> clazz : this.modulatorClasses) {
                if (clazz.isInstance(modulator)) {
                    uiFactory = this.modulatorUIRegistry.get(clazz);
                }
            }
        }
        return uiFactory;
    }
}
