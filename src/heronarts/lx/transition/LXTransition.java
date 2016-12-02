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

package heronarts.lx.transition;

import heronarts.lx.LX;
import heronarts.lx.LXBufferedComponent;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.FixedParameter;
import heronarts.lx.parameter.LXParameter;

/**
 * An object interface to blend two patterns together. All transitions have a
 * duration and generate a new array of colors based upon a source pattern and a
 * destination pattern. Transitions also support easing.
 */
public abstract class LXTransition extends LXBufferedComponent {

    /**
     * Constants for easing that should be applied to the transition.
     */
    public enum Ease {
        /**
         * Apply no easing to the transition
         */
        NONE,

        /**
         * Apply easing to the start of the transition
         */
        IN,

        /**
         * Apply easing to the end of the transition
         */
        OUT,

        /**
         * Apply easing to both the start and end of the transition
         */
        INOUT
    }

    public enum Mode {
        FULL, HALF
    };

    private Mode mode = Mode.FULL;

    public class Timer {
        public long blendNanos = 0;
    }

    public final Timer timer = new Timer();

    private final static double DEFAULT_DURATION = 1000;

    private LXParameter duration;

    private Ease ease;

    /**
     * Default abstract constructor.
     *
     * @param lx LX instance
     */
    protected LXTransition(LX lx) {
        super(lx);
        this.duration = new BoundedParameter("DURATION", 0);
        setDuration(LXTransition.DEFAULT_DURATION);
        addParameter(this.duration);
        this.ease = Ease.NONE;
    }

    public final LXTransition setMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    /**
     * Returns the duration of the transition
     *
     * @return The number of milliseconds this transition lasts for
     */
    final public double getDuration() {
        return this.duration.getValue();
    }

    /**
     * Sets the duration to the value of a parameter
     *
     * @param duration Parameter to control duration of transition
     * @return this
     */
    public LXTransition setDuration(LXParameter duration) {
        this.duration = duration;
        return this;
    }

    /**
     * Modifies the duration of this transition
     *
     * @param duration Number of milliseconds the transition lasts for
     * @return This transition, for method chaining
     */
    public LXTransition setDuration(double duration) {
        this.duration = new FixedParameter(duration);
        return this;
    }

    /**
     * Modifies the easing of this transition
     *
     * @param ease Easing value to apply to transition
     * @return This transition, for method chaining
     */
    public LXTransition setEase(Ease ease) {
        this.ease = ease;
        return this;
    }

    private double easeIn(double value) {
        return (4. * value) * (4. * value) / 16.;
    }

    private double easeOut(double value) {
        return 1. - this.easeIn(1. - value);
    }

    private double ease(double value) {
        switch (this.ease) {
        case NONE:
            break;
        case IN:
            value = this.easeIn(value);
            break;
        case OUT:
            value = this.easeOut(value);
            break;
        case INOUT:
            if (value < 0.5) {
                value = this.easeIn(2. * value) / 2.;
            } else {
                value = 0.5 + this.easeOut(2. * (value - 0.5)) / 2.;
            }
            break;
        }
        return value;
    }

    /**
     * Invoked by the Engine. After this method is called getColors() will return
     * the blended colors.
     *
     * @param c1 Colors being blended from
     * @param c2 Colors being blended to
     * @param progress Progress of blend, from 0 to 1
     */
    public final void blend(int[] c1, int[] c2, double progress) {
        long blendStart = System.nanoTime();
        progress = this.ease(progress);
        if (this.mode == Mode.HALF) {
            progress /= 2.;
        }
        this.computeBlend(c1, c2, progress);
        this.timer.blendNanos = System.nanoTime() - blendStart;
    }

    /**
     * Method that concrete transition classes should implement. Should update the
     * internal colors array appropriately.
     *
     * @param c1 Colors being blended from
     * @param c2 Colors being blended to
     * @param progress Progress of blend, easing already applied, from 0 to 1
     */
    protected abstract void computeBlend(int[] c1, int[] c2, double progress);

}
