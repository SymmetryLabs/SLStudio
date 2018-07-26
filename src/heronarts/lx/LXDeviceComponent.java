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

package heronarts.lx;

import com.google.gson.JsonObject;

import heronarts.lx.osc.LXOscComponent;

/**
 * A component which may have its own scoped user-level modulators.
 */
public abstract class LXDeviceComponent extends LXLayeredComponent implements LXModulationComponent, LXOscComponent {

    public final LXModulationEngine modulation;

    protected LXDeviceComponent(LX lx) {
        super(lx);
        this.modulation = new LXModulationEngine(lx, this);
    }

    @Override
    public void loop(double deltaMs) {
        super.loop(deltaMs);
        this.modulation.loop(deltaMs);
    }

    public LXModulationEngine getModulation() {
        return this.modulation;
    }

    private static final String KEY_MODULATION = "modulation";

    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);
        obj.add(KEY_MODULATION, LXSerializable.Utils.toObject(lx, this.modulation));
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        super.load(lx, obj);
        if (obj.has(KEY_MODULATION)) {
            this.modulation.load(lx,  obj.getAsJsonObject(KEY_MODULATION));
        }
    }
}
