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

package heronarts.lx.midi;

import javax.sound.midi.MidiDevice;

import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

public abstract class LXMidiDevice {

    protected final LXMidiEngine engine;
    protected final MidiDevice device;

    public final BooleanParameter enabled = new BooleanParameter("Enabled", false);

    protected LXMidiDevice(LXMidiEngine engine, MidiDevice device) {
        this.engine = engine;
        this.device = device;
        this.enabled.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                onEnabled(enabled.isOn());
            }
        });
    }

    /**
     * Open the device for input or output
     *
     * @return this
     */
    public LXMidiDevice open() {
        this.enabled.setValue(true);
        return this;
    }

    /**
     * Get the name of the device.
     *
     * @return Device name
     */
    public String getName() {
        return this.device.getDeviceInfo().getName();
    }

    /**
     * Get a description of this device
     *
     * @return Device description
     */
    public String getDescription() {
        return this.device.getDeviceInfo().getDescription();
    }

    protected abstract void onEnabled(boolean enabled);

}
