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

import heronarts.lx.LXLoopTask;

public abstract class UITimerTask implements LXLoopTask {

    private double accum = 0;

    private final double period;

    public enum Mode {
        MILLISECONDS,
        FPS
    }

    protected UITimerTask(double period) {
        this(period, Mode.MILLISECONDS);
    }

    protected UITimerTask(double period, Mode mode) {
        this.period = (mode == Mode.FPS) ? (1000. / period) : period;
    }

    @Override
    public final void loop(double deltaMs) {
        this.accum += deltaMs;
        if (this.accum >= this.period) {
            this.accum = this.accum % this.period;
            run();
        }
    }

    /**
     * Subclasses implement this method to perform the operation
     */
    protected abstract void run();

}
