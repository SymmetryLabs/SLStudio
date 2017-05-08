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
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.clip;

import com.google.gson.JsonObject;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.LXUtils;
import heronarts.lx.parameter.LXNormalizedParameter;

public class ParameterClipLane extends LXClipLane {

    public final LXNormalizedParameter parameter;

    ParameterClipLane(LXClip clip, LXNormalizedParameter parameter) {
        super(clip);
        this.parameter = parameter;
    }

    @Override
    public String getLabel() {
        LXComponent component = this.parameter.getComponent();
        if (component != this.clip.bus) {
            return this.parameter.getComponent().getLabel() + " | " + this.parameter.getLabel();
        }
        return this.parameter.getLabel();
    }

    public ParameterClipLane appendEvent(ParameterClipEvent event) {
        super.appendEvent(event);
        return this;
    }

    public ParameterClipLane insertEvent(double basis, double normalized) {
        super.insertEvent(
            new ParameterClipEvent(this, this.parameter, normalized)
            .setCursor(basis * this.clip.length.getValue())
        );
        return this;
    }

    @Override
    void advanceCursor(double from, double to) {
        if (this.events.size() == 0) {
            return;
        }
        LXClipEvent prior = null;
        LXClipEvent next = null;
        for (LXClipEvent event : this.events) {
            prior = next;
            next = event;
            if (next.cursor > to) {
                break;
            }
        }
        if (prior == null) {
            this.parameter.setNormalized(((ParameterClipEvent) next).getNormalized());
        } else {
            this.parameter.setNormalized(LXUtils.lerp(
                ((ParameterClipEvent) prior).getNormalized(),
                ((ParameterClipEvent) next).getNormalized(),
                (to - prior.cursor) / (next.cursor - prior.cursor)
            ));
        }
    }

    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);
        obj.addProperty(LXComponent.KEY_COMPONENT_ID, this.parameter.getComponent().getId());
        obj.addProperty(LXComponent.KEY_PARAMETER_PATH, this.parameter.getPath());
    }

    @Override
    protected LXClipEvent loadEvent(LX lx, JsonObject eventObj) {
        double normalized = eventObj.get(ParameterClipEvent.KEY_NORMALIZED).getAsDouble();
        return new ParameterClipEvent(this, this.parameter, normalized);
    }
}

