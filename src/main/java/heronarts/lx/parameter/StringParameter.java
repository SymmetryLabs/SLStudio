/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
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
 * Parameter which contains a mutable String value.
 */
public class StringParameter extends LXListenableParameter {

    private String defaultString, string;

    public StringParameter(String label) {
        this(label, "");
    }

    public StringParameter(String label, String string) {
        super(label);
        this.defaultString = this.string = string;
    }

    @Override
    public StringParameter setDescription(String description) {
        return (StringParameter) super.setDescription(description);
    }

    @Override
    public LXParameter reset() {
        this.string = this.defaultString;
        super.reset();
        return this;
    }

    @Override
    public LXParameter reset(double value) {
        throw new UnsupportedOperationException("StringParameter cannot be reset to a numeric value");
    }

    public StringParameter setValue(String string) {
        if (this.string == null) {
            if (string != null) {
                this.string = string;
                incrementValue(1);
            }
        } else if (!this.string.equals(string)) {
            this.string = string;
            incrementValue(1);
        }
        return this;
    }

    @Override
    protected double updateValue(double value) {
        return value;
    }

    public String getString() {
        return this.string;
    }

}
