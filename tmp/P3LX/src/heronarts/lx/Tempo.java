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
import java.util.List;

import heronarts.lx.modulator.Click;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.MutableParameter;

/**
 * Class to represent a musical tempo at which patterns are operating. This can
 * be updated in real-time via invoking the tap() method.
 *
 * Quarter-note "beats" are indicated by the return value of the beat() method,
 * and ramp() returns a double value indicating the current 4/4 phase from 0 to
 * 1.
 */
public class Tempo extends LXModulatorComponent implements LXOscComponent {

    public interface Listener {
        public void onBeat(Tempo tempo);
        public void onHalf(Tempo tempo);
        public void onMeasure(Tempo tempo);
    }

    private final static double MINUTE = 60000;
    private final static double DEFAULT_BPM = 120;
    public final static double MIN_BPM = 20;
    public final static double MAX_BPM = 240;

    public final BoundedParameter bpm =
        new BoundedParameter("BPM", DEFAULT_BPM, MIN_BPM, MAX_BPM)
        .setDescription("Beats per minute of the master tempo object");

    public final BooleanParameter tap =
        new BooleanParameter("Tap")
        .setDescription("When pressed repeatedlly, tempo is learned from the timing between taps")
        .setMode(BooleanParameter.Mode.MOMENTARY);

    public final BooleanParameter nudgeUp =
        new BooleanParameter("Nudge+")
        .setDescription("Temporarily increases tempo while engaged");

    public final BooleanParameter nudgeDown =
        new BooleanParameter("Nudge-")
        .setDescription("Temporarily decreases tempo while engaged");

    private final MutableParameter period = new MutableParameter(MINUTE / DEFAULT_BPM);

    private final List<Listener> listeners = new ArrayList<Listener>();

    private final Click click = new Click(period);

    private long firstTap = 0;
    private long lastTap = 0;
    private int tapCount = 0;

    private int beatCount = 0;
    private boolean manuallyTriggered = false;

    private boolean manualPeriodUpdate = false;

    public Tempo(LX lx) {
        super(lx);
        addParameter("bpm", this.bpm);
        addParameter("tap", this.tap);
        addParameter("nudgeUp", this.nudgeUp);
        addParameter("nudgeDown", this.nudgeDown);
        startModulator(this.click);
    }

    public String getOscAddress() {
        return "/lx/tempo";
    }

    @Override
    public String getLabel() {
        return "Tempo";
    }

    @Override
    public void onParameterChanged(LXParameter parameter) {
        if (parameter == this.bpm) {
            if (!this.manualPeriodUpdate) {
                this.period.setValue(MINUTE / this.bpm.getValue());
            }
        } else if (parameter == this.tap) {
            if (this.tap.isOn()) {
                tap();
            }
        } else if (parameter == this.nudgeUp) {
            adjustBpm(this.nudgeUp.isOn() ? .1 : -.1);
        } else if (parameter == this.nudgeDown) {
            adjustBpm(this.nudgeDown.isOn() ? -.1 : .1);
        }
    }

    public Tempo addListener(Listener listener) {
        this.listeners.add(listener);
        return this;
    }

    public Tempo removeListener(Listener listener) {
        this.listeners.remove(listener);
        return this;
    }

    /**
     * Method to indicate when we are on-beat, assuming quarter-notes being given
     * one beat.
     *
     * @return true if we are on a quarter-note beat
     */
    public boolean beat() {
        return this.click.click();
    }

    /**
     * Method to indicate when we are on-beat, for half a measure.
     *
     * @return true if we are on a half-note beat
     */
    public boolean half() {
        return beat() && (this.beatCount % 2 == 0);
    }

    /**
     * Method to indicate the start of a measure.
     *
     * @return true if we are on a measure-beat
     */
    public boolean measure() {
        return beat() && (this.beatCount % 4 == 0);
    }

    /**
     * Indicates phase of the current beat. On the beat the value will be 0, then
     * ramp up to 1 before the next beat triggers.
     *
     * @return value from 0-1 indicating phase of beat
     */
    public double ramp() {
        return this.click.getBasis();
    }

    /**
     * Indicates beat phase in floating point
     *
     * @return value from 0-1 indicating phase of beat
     */
    public float rampf() {
        return (float) this.ramp();
    }

    /**
     * Returns the current tempo in Beats Per Minute
     *
     * @return Current tempo
     */
    public double bpm() {
        return this.bpm.getValue();
    }

    /**
     * Returns the tempo in floating point
     *
     * @return Current tempo in float
     */
    public float bpmf() {
        return (float) this.bpm();
    }

    /**
     * Sets the BPM to the given value
     *
     * @param bpm Number of beats per minute
     * @return this
     */
    public Tempo setBpm(double bpm) {
        this.bpm.setValue(bpm);
        return this;
    }

    /**
     * Adjust the BPM by the given amount
     *
     * @param amount Amount to adjust BPM by
     * @return this
     */
    public Tempo adjustBpm(double amount) {
        this.bpm.setValue(this.bpm.getValue() + amount);
        return this;
    }

    /**
     * Sets the period of one beat
     *
     * @param beatMillis
     * @return
     */
    public Tempo setPeriod(double beatMillis) {
        this.manualPeriodUpdate = true;
        this.period.setValue(beatMillis);
        this.bpm.setValue(MINUTE / beatMillis);
        return this;
    }

    /**
     * Re-triggers the metronome, so that it immediately beats. Also resetting the
     * beat count to be at the beginning of a measure.
     */
    public void trigger() {
        trigger(true);
    }

    public void trigger(int beat) {
        this.beatCount = beat;
        if (!beat()) {
            this.click.fire();
        }
        this.manuallyTriggered = true;
    }

    /**
     * Triggers a beat, optionally resetting the beat count
     *
     * @param resetBeat True if the beat count should be reset to 0
     */
    public void trigger(boolean resetBeat) {
        if (!beat()) {
            this.beatCount = resetBeat ? 0 : this.beatCount + 1;
            this.click.fire();
        } else if (resetBeat) {
            this.beatCount = 0;
        }
        this.manuallyTriggered = true;
    }

    /**
     * Adjusts the tempo in realtime by tapping. Whenever tap() is invoked the
     * time between previous taps is averaged to compute a new tempo. At least
     * three taps are required to compute a tempo. Otherwise, tapping just
     * re-triggers the beat. It is better to use the trigger() method directly if
     * this is all that is desired.
     */
    public void tap() {
        tap(System.currentTimeMillis());
    }

    /**
     * Adjusts the tempo, specificying an exact timestamp in milliseconds
     * of when the tap event occurred.
     *
     * @param now Timestamp of event, should be equivalent to System.currentTimeMillis()
     */
    public void tap(long now) {
        if (now - this.lastTap > 2000) {
            this.firstTap = now;
            this.tapCount = 0;
        }
        this.lastTap = now;
        ++this.tapCount;
        if (this.tapCount > 3) {
            double beatPeriod = (this.lastTap - this.firstTap) / (double) (this.tapCount - 1);
            setBpm(MINUTE / beatPeriod);
        }
        trigger();
    }

    @Override
    public void loop(double deltaMs) {
        super.loop(deltaMs);
        if (beat() && !this.manuallyTriggered) {
            ++this.beatCount;
        }
        this.manuallyTriggered = false;
    }
}
