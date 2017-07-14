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
 * A FixedParameter is an immutable parameter. It will throw a RuntimeException
 * if setValue() is attempted. Useful for anonymous placeholder values in places
 * that expect to use LXParameters.
 */
public class FixedParameter implements LXParameter {

    private final double value;

    private LXComponent component;
    private String path;

    public FixedParameter(double value) {
        this.value = value;
    }

    @Override
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

    public Formatter getFormatter() {
        return getUnits();
    }

    public Units getUnits() {
        return Units.NONE;
    }

    public Polarity getPolarity() {
        return Polarity.UNIPOLAR;
    }

    @Override
    public void dispose() {

    }

    @Override
    public LXParameter reset() {
        return this;
    }

    @Override
    public LXParameter setValue(double value) {
        throw new RuntimeException("Cannot invoke setValue on a FixedParameter");
    }

    @Override
    public double getValue() {
        return this.value;
    }

    @Override
    public float getValuef() {
        return (float) this.value;
    }

    @Override
    public String getLabel() {
        return null;
    }

}
