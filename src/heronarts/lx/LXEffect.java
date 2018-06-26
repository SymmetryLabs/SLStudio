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
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.transform.LXVector;

import java.util.List;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

/**
 * Class to represent an effect that may be applied to the color array. Effects
 * may be stateless or stateful, though typically they operate on a single
 * frame. Only the current frame is provided at runtime.
 */
public abstract class LXEffect extends LXDeviceComponent implements LXComponent.Renamable, LXMidiListener, LXOscComponent, LXUtils.IndexedElement {

    public final BooleanParameter enabled =
        new BooleanParameter("Enabled", false)
        .setDescription("Whether the effect is enabled");

    protected final MutableParameter enabledDampingAttack = new MutableParameter(100);
    protected final MutableParameter enabledDampingRelease = new MutableParameter(100);
    protected final LinearEnvelope enabledDamped = new LinearEnvelope(0, 0, 0);

    public class Timer {
        public long runNanos = 0;
    }

    public final Timer timer = new Timer();

    private int index = -1;

    // An alias for the 8-bit color buffer array, for compatibility with old-style
    // implementations of run(deltaMs, amount) that directly read from and write
    // into the "colors" array.  Newer subclasses should instead implement
    // run(deltaMs, amount, preferredSpace) and use getArray(space) to get the array.
    protected int[] colors = null;

    protected LXEffect(LX lx) {
        super(lx);
        this.label.setDescription("The name of this effect");
        String simple = getClass().getSimpleName();
        if (simple.endsWith("Effect")) {
            simple = simple.substring(0, simple.length() - "Effect".length());
        }
        this.label.setValue(simple);

        this.enabled.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (LXEffect.this.enabled.isOn()) {
                    enabledDamped.setRangeFromHereTo(1, enabledDampingAttack.getValue()).start();
                    onEnable();
                } else {
                    enabledDamped.setRangeFromHereTo(0, enabledDampingRelease.getValue()).start();
                    onDisable();
                }
            }
        });

        addParameter("enabled", this.enabled);
        addModulator(this.enabledDamped);
    }

    public String getOscAddress() {
        LXBus bus = getBus();
        if (bus != null) {
            return bus.getOscAddress() + "/effect/" + (this.index+1);
        }
        return null;
    }

    public final void setIndex(int index) {
        this.index = index;
    }

    public final int getIndex() {
        return index;
    }

    public LXEffect setBus(LXBus bus) {
        setParent(bus);
        return this;
    }

    public LXBus getBus() {
        return (LXBus) getParent();
    }

    public LXVector[] getVectors() {
        return getBus().getVectors();
    }

    public List<LXVector> getVectorList() {
        return getBus().getVectorList();
    }

    /**
     * @return whether the effect is currently enabled
     */
    public final boolean isEnabled() {
        return this.enabled.isOn();
    }

    /**
     * Toggles the effect.
     *
     * @return this
     */
    public final LXEffect toggle() {
        this.enabled.toggle();
        return this;
    }

    /**
     * Enables the effect.
     *
     * @return this
     */
    public final LXEffect enable() {
        this.enabled.setValue(true);
        return this;
    }

    /**
     * Disables the effect.
     *
     * @return this
     */
    public final LXEffect disable() {
        this.enabled.setValue(false);
        return this;
    }

    protected/* abstract */void onEnable() {
    }

    protected/* abstract */void onDisable() {
    }

    /**
     * Applies this effect to the current frame
     *
     * @param deltaMs Milliseconds since last frame
     */
    @Override
    public final void onLoop(double deltaMs) {
        long runStart = System.nanoTime();
        if (enabledDamped.getValue() > 0) {
            run(deltaMs, enabledDamped.getValue(), preferredSpace);
        }
        this.timer.runNanos = System.nanoTime() - runStart;
    }

    /**
     * Old-style subclasses override this method to implement the effect
     * by reading and modifying the "colors" array.  New-style subclasses
     * should override the other run() method instead; see below.
     *
     * @param deltaMs Number of milliseconds elapsed since last invocation
     * @param enabledAmount The amount of the effect to apply, scaled from 0-1
     */
    @Deprecated
    protected /* abstract */ void run(double deltaMs, double enabledAmount) { }

    /**
     * Implements the effect.  Subclasses should override this method to
     * apply their effect to an array obtained from the polyBuffer.
     *
     * @param deltaMs Number of milliseconds elapsed since last invocation
     * @param enabledAmount The amount of the effect to apply, scaled from 0-1
     * @param preferredSpace A hint as to which color space to use (the implementation
     *     is free to use any space, though doing so may sacrifice quality or efficiency)
     */
    protected /* abstract */ void run(double deltaMs, double enabledAmount, PolyBuffer.Space preferredSpace) {
        // For compatibility, this invokes the method that previous subclasses were
        // supposed to implement.  Implementations of run(deltaMs, enabledAmount)
        // are assumed to operate only on the "colors" array, and are not expected
        // to have marked the buffer, so we mark the buffer modified here.
        colors = (int[]) getArray(SRGB8);
        run(deltaMs, enabledAmount);
        markModified(SRGB8);

        // New subclasses should override and replace this method with one that
        // obtains a color array using getArray(space), writes into that array,
        // and then calls markModified(space).
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
