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

import heronarts.lx.midi.MidiAftertouch;
import heronarts.lx.midi.MidiControlChange;
import heronarts.lx.midi.LXMidiListener;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.midi.MidiPitchBend;
import heronarts.lx.midi.MidiProgramChange;
import heronarts.lx.parameter.BooleanParameter;

/**
 * A pattern is the core object that the animation engine uses to generate
 * colors for all the points.
 */
public abstract class LXPattern extends LXBufferedComponent implements LXMidiListener {

    private int intervalBegin = -1;

    private int intervalEnd = -1;

    public final BooleanParameter autoCycleEligible = new BooleanParameter("Cycle", true);

    public final Timer timer = new Timer();

    public class Timer {
        public long runNanos = 0;
    }

    protected LXPattern(LX lx) {
        super(lx);

        String simple = getClass().getSimpleName();
        if (simple.endsWith("Pattern")) {
            simple = simple.substring(0, simple.length() - "Pattern".length());
        }
        this.label.setValue(simple);
    }

    /**
     * Sets the name of the pattern, useful for method chaining
     *
     * @param name Name
     * @return this
     */
    public LXPattern setLabel(String label) {
        this.label.setValue(label);
        return this;
    }

    /**
     * Gets the channel that this pattern is loaded in. May be null if the pattern is
     * not yet loaded onto any channel.
     *
     * @return Channel pattern is loaded onto
     */
    public final LXChannel getChannel() {
        return (LXChannel) getParent();
    }

    /**
     * Called by the engine when pattern is loaded onto a channel. This may only be
     * called once, by the engine. Do not call directly.
     *
     * @param channel Channel pattern is loaded onto
     * @return this
     */
    final LXPattern setChannel(LXChannel channel) {
        setParent(channel);
        return this;
    }

    /**
     * Set an interval during which this pattern is allowed to run. Begin and end
     * times are specified in minutes of the daytime. So midnight corresponds to
     * the value of 0, 360 would be 6:00am, 1080 would be 18:00 (or 6:00pm)
     *
     * @param begin Interval start time
     * @param end Interval end time
     * @return this
     */
    public LXPattern setInterval(int begin, int end) {
        this.intervalBegin = begin;
        this.intervalEnd = end;
        return this;
    }

    /**
     * Clears a timer interval set to this pattern.
     *
     * @return this
     */
    public LXPattern clearInterval() {
        this.intervalBegin = this.intervalEnd = -1;
        return this;
    }

    /**
     * Tests whether there is an interval for this pattern.
     *
     * @return true if there is an interval
     */
    public final boolean hasInterval() {
        return (this.intervalBegin >= 0) && (this.intervalEnd >= 0);
    }

    /**
     * Tests whether this pattern is in an eligible interval.
     *
     * @return true if the pattern has an interval, and is currently in it.
     */
    public final boolean isInInterval() {
        if (!this.hasInterval()) {
            return false;
        }
        int now = LXTime.hour() * 60 + LXTime.minute();
        if (this.intervalBegin < this.intervalEnd) {
            // Normal daytime interval
            return (now >= this.intervalBegin) && (now < this.intervalEnd);
        } else {
            // Wrapping around midnight
            return (now >= this.intervalBegin) || (now < this.intervalEnd);
        }
    }

    /**
     * Sets whether this pattern is eligible for automatic selection.
     *
     * @param eligible Whether eligible for auto-rotation
     * @return this
     */
    public final LXPattern setAutoCycleEligible(boolean eligible) {
        this.autoCycleEligible.setValue(eligible);
        return this;
    }

    /**
     * Toggles the eligibility state of this pattern.
     *
     * @return this
     */
    public final LXPattern toggleAutoCycleEligible() {
        this.autoCycleEligible.toggle();
        return this;
    }

    /**
     * Determines whether this pattern is eligible to be run at the moment. A
     * pattern is eligible if its eligibility flag has not been set to false, and
     * if it either has no interval, or is currently in its interval.
     *
     * @return True if pattern is eligible to run now
     */
    public final boolean isAutoCycleEligible() {
        return this.autoCycleEligible.isOn() && (!this.hasInterval() || this.isInInterval());
    }

    @Override
    protected final void onLoop(double deltaMs) {
        long runStart = System.nanoTime();
        this.run(deltaMs);
        this.timer.runNanos = System.nanoTime() - runStart;
    }

    /**
     * Main pattern loop function. Invoked in a render loop. Subclasses must
     * implement this function.
     *
     * @param deltaMs Number of milliseconds elapsed since last invocation
     */
    protected abstract void run(double deltaMs);

    /**
     * Subclasses may override this method. It will be invoked when the pattern is
     * about to become active. Patterns may take care of any initialization needed
     * or reset parameters if desired.
     */
    public/* abstract */void onActive() {
    }

    /**
     * Subclasses may override this method. It will be invoked when the pattern is
     * no longer active. Resources may be freed if desired.
     */
    public/* abstract */void onInactive() {
    }

    /**
     * Subclasses may override this method. It will be invoked if a transition
     * into this pattern is taking place. This will be called after onActive. This
     * is not invoked on an already-running pattern. It is only called on the new
     * pattern.
     */
    public/* abstract */void onTransitionStart() {
    }

    /**
     * Subclasses may override this method. It will be invoked when the transition
     * into this pattern is complete.
     */
    public/* abstract */void onTransitionEnd() {
    }

    @Override
    public void noteOnReceived(MidiNoteOn note) {

    }

    @Override
    public void noteOffReceived(MidiNote note) {

    }

    @Override
    public void controlChangeReceived(MidiControlChange cc) {

    }

    @Override
    public void programChangeReceived(MidiProgramChange cc) {

    }

    @Override
    public void pitchBendReceived(MidiPitchBend pitchBend) {

    }

    @Override
    public void aftertouchReceived(MidiAftertouch aftertouch) {

    }

}
