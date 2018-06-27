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

import com.google.gson.JsonObject;

import heronarts.lx.midi.LXMidiListener;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.midi.MidiPitchBend;
import heronarts.lx.midi.MidiProgramChange;
import heronarts.lx.model.LXPoint;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXVector;

import java.util.List;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

/**
 * A pattern is the core object that the animation engine uses to generate
 * colors for all the points.
 */
public abstract class LXPattern extends LXDeviceComponent implements LXComponent.Renamable, LXLayeredComponent.Buffered, LXMidiListener, LXOscComponent, LXUtils.IndexedElement {
    private int index = -1;
    private int intervalBegin = -1;
    private int intervalEnd = -1;
    protected double runMs = 0;

    // An alias for the 8-bit color buffer array, for compatibility with old-style
    // implementations of run(deltaMs) that directly read from and write
    // into the "colors" array.  Newer subclasses should instead implement
    // run(deltaMs, preferredSpace) and use getArray(space) to get the array.
    protected int[] colors = null;

    public final BooleanParameter autoCycleEligible = new BooleanParameter("Cycle", true);
    public final Timer timer = new Timer();

    public class Timer {
        public long runNanos = 0;
    }

    protected LXPattern(LX lx) {
        super(lx);
        this.label.setDescription("The name of this pattern");
        this.label.setValue(getClass().getSimpleName().replaceAll("Pattern$", ""));
    }

    protected LXVector[] getVectors() {
        return LXBus.getVectors(getChannel(), model);
    }

    protected List<LXVector> getVectorList() {
        return LXBus.getVectorList(getChannel(), model);
    }

    protected List<LXVector> getVectorList(Iterable<LXPoint> points) {
        return LXBus.getVectorList(getChannel(), model, points);
    }

    protected List<LXVector> getVectorList(LXPoint[] points) {
        return LXBus.getVectorList(getChannel(), model, points);
    }

    protected List<LXVector> getVectorList(int start, int stop) {
        return LXBus.getVectorList(getChannel(), model, start, stop);
    }

    public String getOscAddress() {
        LXChannel channel = getChannel();
        if (channel != null) {
            return channel.getOscAddress() + "/pattern/" + (this.index + 1);
        }
        return null;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
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
        this.runMs += deltaMs;
        this.run(deltaMs, preferredSpace);
        this.timer.runNanos = System.nanoTime() - runStart;
    }

    /**
     * Old-style subclasses override this method to implement the pattern
     * by writing colors into the "colors" array.  New-style subclasses
     * should override the other run() method instead; see below.
     *
     * @param deltaMs Number of milliseconds elapsed since last invocation
     */
    @Deprecated
    protected /* abstract */ void run(double deltaMs) { }

    /**
     * Implements the pattern.  Subclasses should override this method to
     * write colors into an array obtained from the polyBuffer.
     *
     * @param deltaMs Number of milliseconds elapsed since last invocation
     * @param preferredSpace A hint as to which color space to use (the implementation
     *     is free to use any space, though doing so may sacrifice quality or efficiency)
     */
    protected /* abstract */ void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        // For compatibility, this invokes the method that previous subclasses
        // were supposed to implement.  Implementations of run(deltaMs) are
        // assumed to operate only on the "colors" array, and are not expected
        // to have marked the buffer, so we mark the buffer modified here.
        colors = (int[]) getArray(SRGB8);
        run(deltaMs);
        markModified(SRGB8);

        // New subclasses should override and replace this method with one that
        // obtains a color array using getArray(space), writes into that array,
        // and then calls markModified(space).
    }

    /**
     * Subclasses may override this method. It will be invoked when the pattern is
     * about to become active. Patterns may take care of any initialization needed
     * or reset parameters if desired.
     */
    public /* abstract */ void onActive() {
    }

    /**
     * Subclasses may override this method. It will be invoked when the pattern is
     * no longer active. Resources may be freed if desired.
     */
    public /* abstract */ void onInactive() {
    }

    /**
     * Subclasses may override this method. It will be invoked if a transition
     * into this pattern is taking place. This will be called after onActive. This
     * is not invoked on an already-running pattern. It is only called on the new
     * pattern.
     */
    public /* abstract */ void onTransitionStart() {
    }

    /**
     * Subclasses may override this method. It will be invoked when the transition
     * into this pattern is complete.
     */
    public /* abstract */ void onTransitionEnd() {
    }

    /** This method is invoked whenever the output of getVectors()/getVectorList() changes. */
    public /* abstract */ void onVectorsUpdated() { }

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

    private static final String KEY_AUTO_CYCLE = "autoCycleEnabled";

    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);
        obj.addProperty(KEY_AUTO_CYCLE, this.autoCycleEligible.isOn());
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        super.load(lx, obj);
        if (obj.has(KEY_AUTO_CYCLE)) {
            this.autoCycleEligible.setValue(obj.get(KEY_AUTO_CYCLE).getAsBoolean());
        }
    }
}
