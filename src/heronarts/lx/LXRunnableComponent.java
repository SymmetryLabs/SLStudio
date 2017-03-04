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

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import heronarts.lx.modulator.LXModulator;

public abstract class LXRunnableComponent extends LXComponent implements LXLoopTask {

    private final Map<String, LXModulator> modulators = new LinkedHashMap<String, LXModulator>();

    public class Timer {
        public long loopNanos;
    }

    protected Timer constructTimer() {
        return new Timer();
    }

    public final Timer timer = constructTimer();

    protected LXRunnableComponent(LX lx) {
        super(lx);
    }

    protected final LXModulator addModulator(LXModulator modulator) {
        return addModulator(modulator.getLabel(), modulator);
    }

    protected final LXModulator addModulator(String path, LXModulator modulator) {
        if (path == null) {
            throw new IllegalArgumentException("Cannot add modulator to null path: " + modulator);
        }
        if (this.modulators.containsKey(path)) {
            throw new IllegalStateException("Cannot add modulator to existing path: " + path);
        }
        if (this.modulators.containsValue(modulator)) {
            throw new IllegalStateException("Cannot add modulator twice: " + modulator);
        }
        this.modulators.put(path, modulator);
        modulator.setComponent(this, path);
        ((LXComponent) modulator).setParent(this);
        return modulator;
    }

    protected final LXModulator startModulator(LXModulator modulator) {
        addModulator(modulator).start();
        return modulator;
    }

    protected final LXModulator removeModulator(LXModulator modulator) {
        this.modulators.remove(modulator);
        modulator.dispose();
        return modulator;
    }

    @Override
    public void dispose() {
        for (LXModulator modulator : this.modulators.values()) {
            modulator.dispose();
        }
        this.modulators.clear();
        super.dispose();
    }

    public final LXModulator getModulator(String path) {
        return this.modulators.get(path);
    }

    @Override
    public void loop(double deltaMs) {
        for (LXModulator modulator : this.modulators.values()) {
            modulator.loop(deltaMs);
        }
    }

    @Override
    public void save(JsonObject obj) {
        obj.addProperty("class", getClass().getName());
        super.save(obj);
    }

}
