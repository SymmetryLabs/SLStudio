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

package heronarts.lx.modulator;

import heronarts.lx.LXComponent;
import heronarts.lx.LXRunnable;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.parameter.LXParameter;

/**
 * A Modulator is an abstraction for a variable with a value that varies over
 * time, such as an envelope or a low frequency oscillator. Some modulators run
 * continuously, others may halt after they reach a certain value.
 */
public abstract class LXModulator extends LXRunnable implements LXParameter, LXOscComponent {

    private LXComponent component;

    private LXParameter.Units units = LXParameter.Units.NONE;

    private LXParameter.Polarity polarity = LXParameter.Polarity.UNIPOLAR;

    public final ColorParameter color =
        new ColorParameter("Modulation Color", LXColor.hsb(Math.random() * 360, 100, 100))
        .setDescription("The color used to indicate this modulation source");

    // Hack so that Processing IDE can access it...
    public final ColorParameter clr = this.color;

    private String contextualHelp = null;

    /**
     * The current computed value of this modulator.
     */
    private double value = 0;

    /**
     * Quick helper to get half of PI.
     */
    public static final double HALF_PI = Math.PI / 2.;

    /**
     * Quick helper to get two times PI.
     */
    public static final double TWO_PI = Math.PI * 2.;

    /**
     * Utility default constructor
     *
     * @param label Label
     */
    protected LXModulator(String label) {
        this.label.setValue((label == null) ? getClass().getSimpleName() : label);
        addParameter("color", this.color);
    }

    public String getOscAddress() {
        if (getParent() instanceof LXOscComponent) {
            return ((LXOscComponent) getParent()).getOscAddress() + "/" + getLabel();
        }
        return null;
    }

    public LXParameter setContextualHelp(String contextualHelp) {
        this.contextualHelp = contextualHelp;
        return this;
    }

    @Override
    public String getDescription() {
        return this.contextualHelp;
    }

    @Override
    public LXParameter setComponent(LXComponent component, String path) {
        if (path != null) {
            throw new UnsupportedOperationException("setComponent() path not supported for LXModulator");
        }
        if (this.component != null) {
            throw new IllegalStateException("LXModulator already has component");
        }
        if (component == null) {
            throw new IllegalArgumentException("Cannot setComponent() with null value");
        }
        this.component = component;
        return this;
    }

    @Override
    public LXComponent getComponent() {
        return this.component;
    }

    @Override
    public String getPath() {
        throw new UnsupportedOperationException("getPath() not supported for LXModulator");
    }

    public LXModulator setUnits(LXParameter.Units units) {
        this.units = units;
        return this;
    }

    public LXParameter.Units getUnits() {
        return this.units;
    }

    public LXModulator setPolarity(LXParameter.Polarity polarity) {
        this.polarity = polarity;
        return this;
    }

    public LXParameter.Polarity getPolarity() {
        return this.polarity;
    }

    /**
     * Retrieves the current value of the modulator in full precision
     *
     * @return Current value of the modulator
     */
    public final double getValue() {
        return this.value;
    }

    /**
     * Retrieves the current value of the modulator in floating point precision.
     *
     * @return Current value of the modulator, cast to float
     */
    public final float getValuef() {
        return (float) this.getValue();
    }

    /**
     * Set the modulator to a certain value in its cycle.
     *
     * @param value The value to apply
     * @return This modulator, for method chaining
     */
    public final LXModulator setValue(double value) {
        this.value = value;
        this.onSetValue(value);
        return this;
    }

    /**
     * Subclasses may override when actions are necessary on value change.
     *
     * @param value New value
     */
    protected/* abstract */void onSetValue(double value) {
    }

    /**
     * Helper for subclasses to update value in situations where it needs to be
     * recomputed. This cannot be overriden, and subclasses may assume that it
     * ONLY updates the internal value without triggering any other
     * recomputations.
     *
     * @param value Value for modulator
     * @return this, for method chaining
     */
    protected final LXModulator updateValue(double value) {
        this.value = value;
        return this;
    }

    /**
     * Applies updates to the modulator for the specified number of milliseconds.
     * This method is invoked by the core engine.
     *
     * @param deltaMs Milliseconds to advance by
     */
    @Override
    protected final void run(double deltaMs) {
        this.value = this.computeValue(deltaMs);
    }

    /**
     * Implementation method to advance the modulator's internal state. Subclasses
     * must provide and update value appropriately.
     *
     * @param deltaMs Number of milliseconds to advance by
     * @return Computed value
     */
    protected abstract double computeValue(double deltaMs);

}
