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

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.modulator.LXModulator;

/**
 * A component which may have its own scoped user-level modulators. The concrete subclasses
 * of this are Patterns and Effects.
 */
public abstract class LXDeviceComponent extends LXLayeredComponent {

    public interface DeviceListener {
        public void lfoAdded(LXDeviceComponent device, LXModulator modulator);
        public void lfoRemoved(LXDeviceComponent device, LXModulator modulator);
    }

    private final List<DeviceListener> listeners = new ArrayList<DeviceListener>();

    protected LXDeviceComponent(LX lx) {
        super(lx);
    }

    public LXDeviceComponent addDeviceListener(DeviceListener listener) {
        this.listeners.add(listener);
        return this;
    }

    public LXDeviceComponent removeDeviceListener(DeviceListener listener) {
        this.listeners.remove(listener);
        return this;
    }

}
