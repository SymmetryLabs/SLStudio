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

import com.google.gson.JsonObject;

import heronarts.lx.LX;

public class LXCompoundModulation extends LXParameterModulation {

    public final LXNormalizedParameter source;

    public final CompoundParameter target;

    public final EnumParameter<LXParameter.Polarity> polarity =
        new EnumParameter<LXParameter.Polarity>("Polarity", LXParameter.Polarity.UNIPOLAR)
        .setDescription("Species whether this modulation is unipolar (one-directional) or bipolar (bi-directional)");

    public final BoundedParameter range = (BoundedParameter)
        new BoundedParameter("Range", 0, -1, 1)
        .setDescription("Species the depth of this modulation, may be positive or negative")
        .setPolarity(LXParameter.Polarity.BIPOLAR);

    public LXCompoundModulation(LX lx, JsonObject obj) {
        this(
            (LXNormalizedParameter) getParameter(lx, obj.getAsJsonObject(KEY_SOURCE)),
            (CompoundParameter) getParameter(lx, obj.getAsJsonObject(KEY_TARGET))
        );
    }

    public LXCompoundModulation(LXNormalizedParameter source, CompoundParameter target) {
        super(source, target);
        if (source == null) {
            throw new IllegalArgumentException("LXCompoundModulation source may not be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("LXCompoundModulation target may not be null");
        }
        this.source = source;
        this.target = target;
        this.polarity.setValue(source.getPolarity());
        addParameter(this.polarity);
        addParameter(this.range);
        target.addModulation(this);
    }

    public LXCompoundModulation setPolarity(LXParameter.Polarity polarity) {
        this.polarity.setValue(polarity);
        return this;
    }

    public LXParameter.Polarity getPolarity() {
        return this.polarity.getEnum();
    }

    @Override
    public void dispose() {
        this.target.removeModulation(this);
        super.dispose();
    }

}
