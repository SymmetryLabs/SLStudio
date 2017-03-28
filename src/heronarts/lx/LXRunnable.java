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

import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;

public abstract class LXRunnable extends LXComponent implements LXLoopTask {

    /**
     * Whether this modulator is currently running.
     */
    public final BooleanParameter running = new BooleanParameter("Running", false);

    protected LXRunnable(LX lx) {
        super(lx);
        addParameter("__running", this.running);
    }

    protected LXRunnable() {
        this(null);
    }

    @Override
    public void onParameterChanged(LXParameter parameter) {
        if (parameter == this.running) {
            if (this.running.isOn()) {
                onStart();
            } else {
                onStop();
            }
        }
    }

    /**
     * Sets the runnable in motion
     *
     * @return this
     */
    public final LXRunnable start() {
        this.running.setValue(true);
        return this;
    }

    /**
     * Pauses the runnable wherever it is. Internal state should be maintained. A
     * subsequent call to start() should result in the runnable continuing as it
     * was running before.
     *
     * @return this
     */
    public final LXRunnable stop() {
        this.running.setValue(false);
        return this;
    }

    /**
     * Indicates whether this runnable is running.
     *
     * @return Whether running
     */
    public final boolean isRunning() {
        return this.running.isOn();
    }

    /**
     * Invoking the trigger() method restarts a runnable from its initial state,
     * and should also start the runnable if it is not already running.
     *
     * @return this
     */
    public final LXRunnable trigger() {
        return this.reset().start();
    }

    /**
     * Resets the runnable to its default condition and stops it.
     *
     * @return this
     */
    public final LXRunnable reset() {
        this.stop();
        this.onReset();
        return this;
    }

    /**
     * Optional subclass method when start happens.
     */
    protected/* abstract */void onStart() {

    }

    /**
     * Optional subclass method when stop happens.
     */
    protected/* abstract */void onStop() {

    }

    /**
     * Optional subclass method when reset happens.
     */
    protected/* abstract */void onReset() {
    }

    @Override
    public void loop(double deltaMs) {
        if (this.running.isOn()) {
            run(deltaMs);
        }
    }

    protected abstract void run(double deltaMs);

}
