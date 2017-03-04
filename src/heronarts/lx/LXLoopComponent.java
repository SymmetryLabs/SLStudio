/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
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
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonObject;

import heronarts.lx.color.LXPalette;
import heronarts.lx.model.LXModel;
import heronarts.lx.modulator.LXModulator;

public abstract class LXLoopComponent extends LXComponent implements LXLoopTask {

    private final List<LXModulator> modulators = new ArrayList<LXModulator>();
    private final List<LXModulator> unmodifiableModulators = Collections.unmodifiableList(this.modulators);

    protected LXModel model;

    protected LXPalette palette;

    public class Timer {
        public long loopNanos;
    }

    protected Timer constructTimer() {
        return new Timer();
    }

    public final Timer timer = constructTimer();

    protected LXLoopComponent(LX lx) {
        this.model = lx.model;
        this.palette = lx.palette;
    }

    public LXModel getModel() {
        return this.model;
    }

    public LXLoopComponent setModel(LXModel model) {
        if (model == null) {
            throw new IllegalArgumentException("May not set null model");
        }
        if (this.model != model) {
            this.model = model;
            onModelChanged(model);
        }
        return this;
    }

    /**
     * Subclasses should override to handle changes to which model
     * they are addressing.
     *
     * @param model New model
     */
    protected void onModelChanged(LXModel model) {}

    public LXPalette getPalette() {
        return this.palette;
    }

    public LXLoopComponent setPalette(LXPalette palette) {
        if (palette == null) {
            throw new IllegalArgumentException("May not set null palette");
        }
        if (this.palette != palette) {
            this.palette = palette;
            onPaletteChanged(palette);
        }
        return this;
    }


    /**
     * Subclasses should override to handle changes to which palette
     * they are using.
     *
     * @param palette New palette
     */
    protected void onPaletteChanged(LXPalette palette) {}

    protected final LXModulator addModulator(LXModulator modulator) {
        if (!this.modulators.contains(modulator)) {
            this.modulators.add(modulator);
        }
        return modulator;
    }

    protected final LXModulator startModulator(LXModulator modulator) {
        addModulator(modulator).start();
        return modulator;
    }

    protected final LXModulator removeModulator(LXModulator modulator) {
        this.modulators.remove(modulator);
        return modulator;
    }

    public final List<LXModulator> getModulators() {
        return this.unmodifiableModulators;
    }

    @Override
    public void loop(double deltaMs) {
        for (LXModulator modulator : this.modulators) {
            modulator.loop(deltaMs);
        }
    }

    @Override
    public void save(JsonObject obj) {
        obj.addProperty("class", getClass().getName());
        super.save(obj);
    }

}
