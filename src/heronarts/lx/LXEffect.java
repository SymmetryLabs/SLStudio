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

package heronarts.lx;

import heronarts.lx.midi.LXMidiAftertouch;
import heronarts.lx.midi.LXMidiControlChange;
import heronarts.lx.midi.LXMidiListener;
import heronarts.lx.midi.LXMidiNote;
import heronarts.lx.midi.LXMidiNoteOn;
import heronarts.lx.midi.LXMidiPitchBend;
import heronarts.lx.midi.LXMidiProgramChange;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.parameter.StringParameter;

/**
 * Class to represent an effect that may be applied to the color array. Effects
 * may be stateless or stateful, though typically they operate on a single
 * frame. Only the current frame is provided at runtime.
 */
public abstract class LXEffect extends LXLayeredComponent implements LXMidiListener {

    public final BooleanParameter enabled = new BooleanParameter("ENABLED", false);

    protected final MutableParameter enabledDampingAttack = new MutableParameter(100);
    protected final MutableParameter enabledDampingRelease = new MutableParameter(100);
    protected final LinearEnvelope enabledDamped = new LinearEnvelope(0, 0, 0);

    public class Timer {
        public long runNanos = 0;
    }

    public final Timer timer = new Timer();

    public final StringParameter name;

    private int index = -1;

    protected LXEffect(LX lx) {
        super(lx);

        String simple = getClass().getSimpleName();
        if (simple.endsWith("Effect")) {
            simple = simple.substring(0, simple.length() - "Effect".length());
        }
        this.name = new StringParameter("Name", simple);

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

        addParameter("__name", this.name);
        addParameter("__enabled", this.enabled);
        addModulator(this.enabledDamped);
    }

    /**
     * Called by the engine to assign index on this effect. Should never
     * be called otherwise.
     *
     * @param index
     * @return
     */
    final LXEffect setIndex(int index) {
        this.index = index;
        return this;
    }

    /**
     * Gets the index of this effect in the channel FX bus.
     *
     * @return index of this effect in the channel FX bus
     */
    public final int getIndex() {
        return this.index;
    }

    /**
     * Gets the name of the effect
     *
     * @return Effect name
     */
    public String getName() {
        return this.name.getString();
    }

    /**
     * Sets the name of the effect, useful for method chaining
     *
     * @param name Name
     * @return this
     */
    public LXEffect setName(String name) {
        this.name.setValue(name);
        return this;
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
        double enabledDamped = this.enabledDamped.getValue();
        if (enabledDamped > 0) {
            run(deltaMs, enabledDamped);
        }
        this.timer.runNanos = System.nanoTime() - runStart;
    }

    /**
     * Implementation of the effect. Subclasses need to override this to implement
     * their functionality.
     *
     * @param deltaMs Number of milliseconds elapsed since last invocation
     * @param enabledAmount The amount of the effect to apply, scaled from 0-1
     */
    protected abstract void run(double deltaMs, double enabledAmount);

    @Override
    public void noteOnReceived(LXMidiNoteOn note) {

    }

    @Override
    public void noteOffReceived(LXMidiNote note) {

    }

    @Override
    public void controlChangeReceived(LXMidiControlChange cc) {

    }

    @Override
    public void programChangeReceived(LXMidiProgramChange cc) {

    }

    @Override
    public void pitchBendReceived(LXMidiPitchBend pitchBend) {

    }

    @Override
    public void aftertouchReceived(LXMidiAftertouch aftertouch) {

    }

}
