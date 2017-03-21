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

package heronarts.lx;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import heronarts.lx.modulator.LXModulator;

public abstract class LXRunnableComponent extends LXComponent implements LXLoopTask {

    private final Map<String, LXModulator> modulators = new LinkedHashMap<String, LXModulator>();

    private final Collection<LXModulator> unmodifiableModulators = Collections.unmodifiableCollection(this.modulators.values());

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

    protected LXModulator addModulator(String path, LXModulator modulator) {
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

    protected LXModulator removeModulator(LXModulator modulator) {
        this.modulators.remove(modulator);
        modulator.dispose();
        return modulator;
    }

    public Collection<LXModulator> getModulators() {
        return this.unmodifiableModulators;
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
