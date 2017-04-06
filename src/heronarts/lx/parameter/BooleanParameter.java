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

/**
 * A simple parameter that has a binary value of off or on
 */
public class BooleanParameter extends LXListenableNormalizedParameter {

    public enum Mode {
        TOGGLE,
        MOMENTARY;
    }

    private Mode mode = Mode.TOGGLE;

    public BooleanParameter(String label) {
        this(label, false);
    }

    public BooleanParameter(String label, boolean on) {
        super(label, on ? 1. : 0.);
    }

    @Override
    public BooleanParameter setDescription(String description) {
        return (BooleanParameter) super.setDescription(description);
    }

    public BooleanParameter setMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public Mode getMode() {
        return this.mode;
    }

    public boolean isOn() {
        return getValueb();
    }

    public boolean getValueb() {
        return this.getValue() > 0.;
    }

    public BooleanParameter setValue(boolean value) {
        setValue(value ? 1. : 0.);
        return this;
    }

    public BooleanParameter toggle() {
        setValue(!isOn());
        return this;
    }

    @Override
    protected double updateValue(double value) {
        return (value > 0) ? 1. : 0.;
    }

    public double getNormalized() {
        return (getValue() > 0) ? 1. : 0.;
    }

    public float getNormalizedf() {
        return (float) getNormalized();
    }

    public BooleanParameter setNormalized(double normalized) {
        setValue(normalized >= 0.5);
        return this;
    }

}
