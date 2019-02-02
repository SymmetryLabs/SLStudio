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

package heronarts.lx.midi.surface;

import com.google.gson.JsonObject;

import heronarts.lx.LX;
import heronarts.lx.LXSerializable;
import heronarts.lx.midi.LXMidiEngine;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.LXMidiListener;
import heronarts.lx.midi.LXMidiOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

public abstract class LXMidiSurface implements LXMidiListener {

    public static final String APC40_MK2 = "APC40 mkII";

    private static LXMidiOutput findOutput(LXMidiEngine engine, String description) {
        for (LXMidiOutput output : engine.outputs) {
            if (output.getDescription().equals(description)) {
                return output;
            }
        }
        return null;
    }

    public static LXMidiSurface get(LX lx, LXMidiEngine engine, LXMidiInput input) {
        if (input.getDescription().equals(APC40_MK2) || (input.getName() != null && input.getName().equals(APC40_MK2))) {
            return new APC40Mk2(lx, input, findOutput(engine, APC40_MK2));
        }
        return null;
    }

    protected final LX lx;
    protected final LXMidiInput input;
    protected final LXMidiOutput output;

    public final BooleanParameter enabled =
        new BooleanParameter("Enabled")
        .setDescription("Whether the control surface is enabled");

    protected LXMidiSurface(LX lx, final LXMidiInput input, final LXMidiOutput output) {
        this.lx = lx;
        this.input = input;
        this.output = output;
        this.enabled.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (enabled.isOn()) {
                    input.open();
                    output.open();
                    input.addListener(LXMidiSurface.this);
                } else {
                    input.removeListener(LXMidiSurface.this);
                }
                onEnable(enabled.isOn());
            }
        });

    }

    public String getDescription() {
        return this.input.getDescription();
    }

    protected void onEnable(boolean isOn) {}

    protected void sendNoteOn(int channel, int note, int velocity) {
        if (this.enabled.isOn()) {
            this.output.sendNoteOn(channel, note, velocity);
        }
    }

    protected void sendControlChange(int channel, int cc, int value) {
        if (this.enabled.isOn()) {
            this.output.sendControlChange(channel, cc, value);
        }
    }
}
