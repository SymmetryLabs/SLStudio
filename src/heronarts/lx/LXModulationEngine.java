/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        return this;
    }

    @Override
    protected LXModulator addModulator(String path, LXModulator modulator) {
        super.addModulator(path,  modulator);
        for (Listener listener : this.listeners) {
            listener.modulatorAdded(this, modulator);
        }
        return modulator;
    }

    @Override
    protected LXModulator removeModulator(LXModulator modulator) {
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
