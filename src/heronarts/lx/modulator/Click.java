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

package heronarts.lx.modulator;

import heronarts.lx.parameter.FixedParameter;
import heronarts.lx.parameter.LXParameter;

/**
 * A click is a simple modulator that fires a value of 1 every time its period
 * has passed. Otherwise it always returns 0.
 */
public class Click extends LXPeriodicModulator {

    public Click(double periodMs) {
        this(new FixedParameter(periodMs));
    }

    public Click(LXParameter periodMs) {
        this("CLICK", periodMs);
    }

    public Click(String label, double periodMs) {
        this(label, new FixedParameter(periodMs));
    }

    public Click(String label, LXParameter periodMs) {
        super(label, periodMs);
    }

    /**
     * Stops the modulator and sets it back to its initial state.
     *
     * @return this
     */
    public Click stopAndReset() {
        this.stop();
        this.setBasis(0);
        return this;
    }

    /**
     * Sets the value of the click to 1, so that code querying it in this frame of
     * execution sees it as active. On the next iteration of the runloop it will
     * be off again.
     *
     * @return this
     */
    public LXModulator fire() {
        setValue(1);
        start();
        return this;
    }

    /**
     * Helper to conditionalize logic based on the click. Typical use is to query
     * as follows:
     *
     * <pre>
     * if (clickInstance.click()) {
     *   // perform periodic operation
     * }
     * </pre>
     *
     * @return true if the value is 1, otherwise false
     */
    public boolean click() {
        return this.getValue() == 1;
    }

    @Override
    protected double computeValue(double deltaMs, double basis) {
        return loop() || finished() ? 1 : 0;
    }

    @Override
    protected double computeBasis(double basis, double value) {
        // The basis is indeterminate for this modulator, it can only be
        // specifically known when the value is 1.
        return value < 1 ? 0 : 1;
    }
}