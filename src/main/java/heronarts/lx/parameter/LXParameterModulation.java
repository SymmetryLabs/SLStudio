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
import heronarts.lx.LXModulationEngine;
import heronarts.lx.color.ColorParameter;

public abstract class LXParameterModulation extends LXComponent {

    private final LXParameter source;
    private final LXParameter target;

    public final ColorParameter color;

    // Hack so that Processing IDE can access it...
    public final ColorParameter clr;

    public final BooleanParameter enabled =
        new BooleanParameter("Enabled", true)
        .setDescription("Whether this modulation is enabled");

    protected LXParameterModulation(LXParameter source, LXParameter target) {
        this.source = source;
        this.target = target;
        LXComponent component = source.getComponent();
        if (component instanceof LXModulationEngine) {
            this.color = ((LXComponent) source).modulationColor;
        } else {
            this.color = component.modulationColor;
        }
        this.clr = this.color;
        addParameter("enabled", this.enabled);
    }

    @Override
    public String getLabel() {
        return this.source.getLabel() + " > " + this.target.getLabel();
    }

    protected static final String KEY_SOURCE = "source";
    protected static final String KEY_TARGET = "target";

    protected static LXParameter getParameter(LX lx, JsonObject obj) {
        if (obj.has(KEY_ID)) {
            return (LXParameter) lx.getProjectComponent(obj.get(KEY_ID).getAsInt());
        }
        LXComponent component = lx.getProjectComponent(obj.get(KEY_COMPONENT_ID).getAsInt());
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
