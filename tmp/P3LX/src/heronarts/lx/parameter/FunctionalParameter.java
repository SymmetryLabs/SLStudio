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

import heronarts.lx.LXComponent;

/**
 * An LXParameter that has a value computed by a function, which may combine the
 * values of other parameters, or call some function, etc.
 */
public abstract class FunctionalParameter implements LXParameter {

    private final String label;

    private LXComponent component;
    private String path;

    protected FunctionalParameter() {
        this("FUNC-PARAM");
    }

    protected FunctionalParameter(String label) {
        this.label = label;
    }

    public String getDescription() {
        return null;
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
    public LXParameter.Polarity getPolarity() {
        return LXParameter.Polarity.UNIPOLAR;
    }

    @Override
    public LXParameter.Units getUnits() {
        return LXParameter.Units.NONE;
    }

    @Override
    public void dispose() {}

    /**
     * Does nothing, subclass may override.
     */
    public FunctionalParameter reset() {
        return this;
    }

    /**
     * Not supported for this parameter type unless subclass overrides.
     *
     * @param value The value
     */
    public LXParameter setValue(double value) {
        throw new UnsupportedOperationException(
                "FunctionalParameter does not support setValue()");
    }

    /**
     * Retrieves the value of the parameter, subclass must implement.
     *
     * @return Parameter value
     */
    public abstract double getValue();

    /**
     * Utility helper function to get the value of the parameter as a float.
     *
     * @return Parameter value as float
     */
    public float getValuef() {
        return (float) getValue();
    }

    /**
     * Gets the label for this parameter
     *
     * @return Label of parameter
     */
    public final String getLabel() {
        return this.label;
    }

}
