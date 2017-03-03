/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
