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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import heronarts.lx.modulator.LXModulator;

public abstract class LXModulatorComponent extends LXComponent implements LXLoopTask {

    private final List<LXModulator> internalModulators = new ArrayList<LXModulator>();

    public final List<LXModulator> modulators = Collections.unmodifiableList(this.internalModulators);

    public class Timer {
        public long loopNanos;
    }

    protected Timer constructTimer() {
        return new Timer();
    }

    public final Timer timer = constructTimer();

    protected LXModulatorComponent(LX lx) {
        super(lx);
    }

    public LXModulator addModulator(LXModulator modulator) {
        if (modulator == null) {
            throw new IllegalArgumentException("Cannot add null modulator");
        }
        if (this.internalModulators.contains(modulator)) {
            throw new IllegalStateException("Cannot add modulator twice: " + modulator);
        }
        this.internalModulators.add(modulator);
        modulator.setComponent(this, null);
        ((LXComponent) modulator).setParent(this);
        return modulator;
    }

    protected final LXModulator startModulator(LXModulator modulator) {
        addModulator(modulator).start();
        return modulator;
    }

    public LXModulator removeModulator(LXModulator modulator) {
        this.internalModulators.remove(modulator);
        modulator.dispose();
        return modulator;
    }

    public LXModulator getModulator(String label) {
        for (LXModulator modulator : this.modulators) {
            if (modulator.getLabel().equals(label)) {
                return modulator;
            }
        }
        return null;
    }

    public List<LXModulator> getModulators() {
        return this.modulators;
    }

    @Override
    public void dispose() {
        for (LXModulator modulator : this.internalModulators) {
            modulator.dispose();
        }
        this.internalModulators.clear();
        super.dispose();
    }

    @Override
    public void loop(double deltaMs) {
        for (LXModulator modulator : this.internalModulators) {
            modulator.loop(deltaMs);
        }
    }

}
