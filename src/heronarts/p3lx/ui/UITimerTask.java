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

package heronarts.p3lx.ui;

import heronarts.lx.LXLoopTask;

public abstract class UITimerTask implements LXLoopTask {

    private double accum = 0;

    private final double period;

    public static final int MS = 1;
    public static final int FPS = 2;

    protected UITimerTask(double period) {
        this(period, MS);
    }

    protected UITimerTask(double period, int mode) {
        this.period = (mode == FPS) ? (1000. / period) : period;
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
