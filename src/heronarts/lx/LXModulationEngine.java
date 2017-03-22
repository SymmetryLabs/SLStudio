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

package heronarts.lx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonObject;

import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.LXParameterModulation;

public class LXModulationEngine extends LXRunnableComponent {

    public interface Listener {
        public void modulationAdded(LXModulationEngine engine, LXParameterModulation modulation);
        public void modulationRemoved(LXModulationEngine engine, LXParameterModulation modulation);
        public void modulatorAdded(LXModulationEngine engine, LXModulator modulator);
        public void modulatorRemoved(LXModulationEngine engine, LXModulator modulator);
    }

    private final List<Listener> listeners = new ArrayList<Listener>();

    private final List<LXParameterModulation> internalModulations = new ArrayList<LXParameterModulation>();
    public final List<LXParameterModulation> modulations = Collections.unmodifiableList(this.internalModulations);

    LXModulationEngine(LX lx) {
        super(lx);
    }

    public LXModulationEngine addListener(Listener listener) {
        this.listeners.add(listener);
        return this;
    }

    public LXModulationEngine removeListener(Listener listener) {
        this.listeners.remove(listener);
        return this;
    }

    public LXModulationEngine addModulation(LXParameterModulation modulation) {
        if (this.internalModulations.contains(modulation)) {
            throw new IllegalStateException("Cannot add same modulation twice");
        }
        ((LXComponent) modulation).setParent(this);
        this.internalModulations.add(modulation);
        for (Listener listener : this.listeners) {
            listener.modulationAdded(this, modulation);
        }
        return this;
    }

    public LXModulationEngine removeModulation(LXParameterModulation modulation) {
        this.internalModulations.remove(modulation);
        for (Listener listener : this.listeners) {
            listener.modulationRemoved(this, modulation);
        }
        modulation.dispose();
        return this;
    }

    public LXModulationEngine removeModulations(LXComponent component) {
        Iterator<LXParameterModulation> iterator = this.internalModulations.iterator();
        while (iterator.hasNext()) {
            LXParameterModulation modulation = iterator.next();
            System.out.println(modulation);
            if (modulation.source == component || modulation.source.getComponent() == component || modulation.target.getComponent() == component) {
                System.out.println("oijasdf");
                iterator.remove();
                for (Listener listener : this.listeners) {
                    listener.modulationRemoved(this, modulation);
                }
                modulation.dispose();
            }
        }
        return this;
    }

    @Override
    public LXModulator addModulator(LXModulator modulator) {
        super.addModulator(modulator);
        for (Listener listener : this.listeners) {
            listener.modulatorAdded(this, modulator);
        }
        return modulator;
    }

    @Override
    public LXModulator removeModulator(LXModulator modulator) {
        super.removeModulator(modulator);
        for (Listener listener : this.listeners) {
            listener.modulatorRemoved(this, modulator);
        }
        return modulator;
    }

    @Override
    public void dispose() {
        for (LXParameterModulation modulation : this.internalModulations) {
            modulation.dispose();
        }
        this.internalModulations.clear();
        super.dispose();
    }

    @Override
    public String getLabel() {
        return "Mod";
    }

    @Override
    public void save(JsonObject obj) {
        super.save(obj);
        // TODO(mcslee): implement saving these!
    }

    @Override
    public void load(JsonObject obj) {
        super.load(obj);
        // TODO(mcslee): implement loading these!
    }

}
