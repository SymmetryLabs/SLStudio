/**
 * Copyright 2016- Mark C. Slee, Heron Arts LLC
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

package heronarts.lx.audio;

import com.google.gson.JsonObject;

import heronarts.lx.LX;
import heronarts.lx.LXModulatorComponent;
import heronarts.lx.LXSerializable;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;

public class LXAudioEngine extends LXModulatorComponent implements LXOscComponent {

    public BooleanParameter enabled =
        new BooleanParameter("Enabled", false)
        .setDescription("Sets whether the audio engine is active");

    /**
     * Audio input object
     */
    public final LXAudioInput input;

    public final LXAudioOutput output;

    public final GraphicMeter meter;

    public enum Mode {
        INPUT,
        OUTPUT
    };

    public final EnumParameter<Mode> mode = new EnumParameter<Mode>("Mode", Mode.INPUT);

    public LXAudioEngine(LX lx) {
        super(lx, "Audio");
        addParameter("enabled", this.enabled);
        addParameter("mode", this.mode);

        this.mode.setOptions(new String[] { "Input", "Output" });

        this.input = new LXAudioInput(lx);
        this.output = new LXAudioOutput(lx);

        this.meter = new GraphicMeter("Meter", this.input.mix);
        addModulator(this.meter);
    }

    public String getOscAddress() {
        return "/lx/audio";
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == this.enabled) {
            if (this.enabled.isOn()) {
                this.input.open();
                this.input.start();
                // TODO(mcslee): start/stop output?
            } else {
                this.input.stop();
                // TODO(mcslee): start/stop output?
            }
            this.meter.running.setValue(this.enabled.isOn());
        } else if (p == this.mode) {
            switch (this.mode.getEnum()) {
            case INPUT: this.meter.setBuffer(this.input.mix); break;
            case OUTPUT: this.meter.setBuffer(this.output.mix); break;
            }
        }
    }

    /**
     * Retrieves the audio input object at default sample rate of 44.1kHz
     *
     * @return Audio input object
     */
    public final LXAudioInput getInput() {
        return this.input;
    }

    @Override
    public void dispose() {
        this.input.close();
        super.dispose();
    }

    private static final String KEY_METER = "meter";
    private static final String KEY_INPUT = "input";
    private static final String KEY_OUTPUT = "output";

    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);
        obj.add(KEY_METER, LXSerializable.Utils.toObject(lx, this.meter));
        obj.add(KEY_INPUT, LXSerializable.Utils.toObject(lx, this.input));
        obj.add(KEY_OUTPUT, LXSerializable.Utils.toObject(lx, this.output));
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        this.output.reset();
        if (obj.has(KEY_METER)) {
            this.meter.load(lx, obj.getAsJsonObject(KEY_METER));
        }
        if (obj.has(KEY_INPUT)) {
            this.input.load(lx, obj.getAsJsonObject(KEY_INPUT));
        }
        if (obj.has(KEY_OUTPUT)) {
            this.output.load(lx, obj.getAsJsonObject(KEY_OUTPUT));
        }
        super.load(lx, obj);
    }

}
