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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import heronarts.lx.modulator.LXModulator;

public abstract class LXModulatorComponent extends LXComponent implements LXLoopTask {

    private final List<LXModulator> modulators = new ArrayList<LXModulator>();

    private final List<LXModulator> unmodifiableModulators = Collections.unmodifiableList(this.modulators);

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
        if (this.modulators.contains(modulator)) {
            throw new IllegalStateException("Cannot add modulator twice: " + modulator);
        }
        this.modulators.add(modulator);
        modulator.setComponent(this, null);
        ((LXComponent) modulator).setParent(this);
        return modulator;
    }

    protected final LXModulator startModulator(LXModulator modulator) {
        addModulator(modulator).start();
        return modulator;
    }

    public LXModulator removeModulator(LXModulator modulator) {
        this.modulators.remove(modulator);
        modulator.dispose();
        return modulator;
    }

    public Collection<LXModulator> getModulators() {
        return this.unmodifiableModulators;
    }

    @Override
    public void dispose() {
        for (LXModulator modulator : this.modulators) {
            modulator.dispose();
        }
        this.modulators.clear();
        super.dispose();
    }

    @Override
    public void loop(double deltaMs) {
        for (LXModulator modulator : this.modulators) {
            modulator.loop(deltaMs);
        }
    }

}
