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

import heronarts.lx.LXComponent;
import heronarts.lx.LXUtils;

/**
 * Simple normalized parameter which is not listenable.
 */
public class NormalizedParameter implements LXNormalizedParameter {

    private LXComponent component = null;
    private String path = null;
    private final String label;
    private String description = null;
    private double value = 0;

    public NormalizedParameter(String label) {
        this(label, 0);
    }

    public NormalizedParameter(String label, double value) {
        this.label = label;
        this.value = value;
    }

    @Override
    public NormalizedParameter setComponent(LXComponent component, String path) {
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

    public NormalizedParameter setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public Formatter getFormatter() {
        return getUnits();
    }

    @Override
    public Units getUnits() {
        return Units.NONE;
    }

    @Override
    public Polarity getPolarity() {
        return Polarity.UNIPOLAR;
    }

    @Override
    public void dispose() {
    }

    @Override
    public NormalizedParameter reset() {
        this.value = 0;
        return this;
    }

    @Override
    public NormalizedParameter setValue(double value) {
        this.value = LXUtils.constrain(value, 0, 1);;
        return this;
    }

    @Override
    public double getValue() {
        return this.value;
    }

    @Override
    public float getValuef() {
        return (float) getValue();
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public NormalizedParameter setNormalized(double value) {
        return setValue(value);
    }

    @Override
    public double getNormalized() {
        return this.value;
    }

    @Override
    public float getNormalizedf() {
        return (float) getNormalized();
    }

    @Override
    public double getExponent() {
        return 1;
    }

}
