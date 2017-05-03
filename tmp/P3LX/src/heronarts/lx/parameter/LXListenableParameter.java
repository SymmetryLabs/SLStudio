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

package heronarts.lx.parameter;

import java.util.HashSet;
import java.util.Set;

import heronarts.lx.LXComponent;

/**
 * This is a parameter instance that can be listened to, meaning we are able to
 * deterministically know when the value has changed. This means that all
 * modifications *must* come through setValue().
 */
public abstract class LXListenableParameter implements LXParameter {

    private final String label;

    private double defaultValue, value;

    private final Set<LXParameterListener> listeners = new HashSet<LXParameterListener>();

    private LXComponent component;
    private String path;

    private Units units = LXParameter.Units.NONE;

    private Polarity polarity = LXParameter.Polarity.UNIPOLAR;

    protected String description = null;

    protected LXListenableParameter() {
        this(null, 0);
    }

    protected LXListenableParameter(String label) {
        this(label, 0);
    }

    protected LXListenableParameter(double value) {
        this(null, value);
    }

    protected LXListenableParameter(String label, double value) {
        this.label = label;
        this.defaultValue = this.value = value;
    }

    public LXParameter.Units getUnits() {
        return this.units;
    }

    public LXListenableParameter setUnits(LXParameter.Units units) {
        this.units = units;
        return this;
    }

    public LXParameter.Polarity getPolarity() {
        return this.polarity;
    }

    public LXListenableParameter setPolarity(LXParameter.Polarity polarity) {
        this.polarity = polarity;
        return this;
    }

    public final LXListenableParameter addListener(LXParameterListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Cannot add null parameter listener");
        }
        if (this.listeners.contains(listener)) {
            throw new IllegalStateException("Cannot add duplicate listener " + this + " " + listener);
        }
        this.listeners.add(listener);
        return this;
    }

    public final LXListenableParameter removeListener(LXParameterListener listener) {
        this.listeners.remove(listener);
        return this;
    }

    public LXListenableParameter setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public LXParameter setComponent(LXComponent component, String path) {
        if (component == null || path == null) {
            throw new IllegalArgumentException("May not set null component or path");
        }
        if (this.component != null || this.path != null) {
            throw new IllegalStateException("Component already set on this modulator: " + this);
        }
        this.component = component;
        this.path = path;
        return this;
    }

    @Override
    public LXComponent getComponent() {
        return this.component;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public void dispose() {

    }

    public LXParameter reset() {
        return setValue(this.defaultValue);
    }

    /**
     * Resets the value of the parameter, giving it a new default. Future calls to
     * reset() with no parameter will use this value.
     *
     * @param value New default value
     * @return this
     */
    public LXParameter reset(double value) {
        this.defaultValue = value;
        return setValue(this.defaultValue);
    }

    public final LXParameter incrementValue(double amount) {
        return setValue(this.value + amount);
    }

    public final LXParameter setValue(double value) {
        if (this.value != value) {
            value = updateValue(value);
            if (this.value != value) {
                this.value = value;
                for (LXParameterListener l : listeners) {
                    l.onParameterChanged(this);
                }
            }
        }
        return this;
    }

    public double getValue() {
        return this.value;
    }

    public final float getValuef() {
        return (float) getValue();
    }

    public String getLabel() {
        return this.label;
    }

    /**
     * Manually notify all listeners of this parameter's current value.
     * Useful in some situations to force state reset.
     */
    public LXListenableParameter bang() {
        for (LXParameterListener l : listeners) {
            l.onParameterChanged(this);
        }
        return this;
    }

    /**
     * Invoked when the value has changed. Subclasses should update any special
     * internal state according to this new value.
     *
     * @param value New value
     * @return this
     */
    protected abstract double updateValue(double value);

}
