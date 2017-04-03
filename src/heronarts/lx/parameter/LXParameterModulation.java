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
import heronarts.lx.LXComponent;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.LXModulator;

public class LXParameterModulation extends LXComponent {

    public final LXNormalizedParameter source;

    public final CompoundParameter target;

    public final ColorParameter color;

    // Hack so that Processing IDE can access it...
    public final ColorParameter clr;

    public final EnumParameter<LXParameter.Polarity> polarity =
        new EnumParameter<LXParameter.Polarity>("Polarity", LXParameter.Polarity.UNIPOLAR);

    public final BoundedParameter range = (BoundedParameter)
        new BoundedParameter("Range", 0, -1, 1).setPolarity(LXParameter.Polarity.BIPOLAR);

    public LXParameterModulation(LX lx, JsonObject obj) {
        this(
            (LXNormalizedParameter) getParameter(lx, obj.getAsJsonObject(KEY_SOURCE)),
            (CompoundParameter) getParameter(lx, obj.getAsJsonObject(KEY_TARGET))
        );
    }

    public LXParameterModulation(LXNormalizedParameter source, CompoundParameter target) {
        this.source = source;
        this.target = target;
        if (source instanceof LXModulator) {
            this.color = ((LXModulator)source).color;
        } else {
            this.color = new ColorParameter("Color", LXColor.hsb(Math.random() * 360, 100, 100));
            addParameter(this.color);
        }
        this.clr = this.color;
        this.polarity.setValue(source.getPolarity());
        addParameter(this.polarity);
        addParameter(this.range);
        target.addModulation(this);
    }

    public LXParameterModulation setPolarity(LXParameter.Polarity polarity) {
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

    @Override
    public String getLabel() {
        return this.source.getLabel() + " > " + this.target.getLabel();
    }

    private static final String KEY_SOURCE = "source";
    private static final String KEY_TARGET = "target";
    private static final String KEY_COMPONENT_ID = "componentId";
    private static final String KEY_PARAMETER_PATH = "parameterPath";

    private static LXParameter getParameter(LX lx, JsonObject obj) {
        if (obj.has(KEY_ID)) {
            return (LXParameter) lx.getComponent(obj.get(KEY_ID).getAsInt());
        }
        LXComponent component = lx.getComponent(obj.get(KEY_COMPONENT_ID).getAsInt());
        String path = obj.get(KEY_PARAMETER_PATH).getAsString();
        return component.getParameter(path);
    }


    @Override
    public void save(LX lx, JsonObject obj) {
        JsonObject sourceObj = new JsonObject();
        if (this.source instanceof LXComponent) {
            LXComponent sourceComponent = (LXComponent) this.source;
            sourceObj.addProperty(KEY_ID, sourceComponent.getId());
        } else {
            sourceObj.addProperty(KEY_COMPONENT_ID, this.source.getComponent().getId());
            sourceObj.addProperty(KEY_PARAMETER_PATH, this.source.getPath());
        }
        obj.add(KEY_SOURCE, sourceObj);
        JsonObject targetObj = new JsonObject();
        targetObj.addProperty(KEY_COMPONENT_ID, this.target.getComponent().getId());
        targetObj.addProperty(KEY_PARAMETER_PATH, this.target.getPath());
        obj.add(KEY_TARGET, targetObj);
        super.save(lx, obj);
    }
}
