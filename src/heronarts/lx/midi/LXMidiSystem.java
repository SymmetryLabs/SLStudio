/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.midi;

import heronarts.lx.LX;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

public class LXMidiSystem {

    private static final List<MidiDevice> inputs = new ArrayList<MidiDevice>();

    private static final List<MidiDevice> outputs = new ArrayList<MidiDevice>();

    private static final List<MidiDevice> unmodifiableInputs =
        Collections.unmodifiableList(inputs);

    private static final List<MidiDevice> unmodifiableOutputs =
        Collections.unmodifiableList(outputs);

    private static final Map<MidiDevice, LXMidiInput> inputDeviceMap =
        new HashMap<MidiDevice, LXMidiInput>();

    private static final Map<MidiDevice, LXMidiOutput> outputDeviceMap =
        new HashMap<MidiDevice, LXMidiOutput>();

    private static boolean initialized = false;

    public static LXMidiInput matchInput(LX lx, String substring) {
        return matchInput(lx, new String[] { substring });
    }

    public static LXMidiInput matchInput(LX lx, String[] substrings) {
        MidiDevice device = matchInputDevice(substrings);
        if (device != null) {
            LXMidiInput input = inputDeviceMap.get(device);
            if (input != null) {
                return input;
            }
            try {
                inputDeviceMap.put(device, input = new LXMidiInput(lx, device));
                return input;
            } catch (MidiUnavailableException mux) {
                mux.printStackTrace();
            }
        }
        return null;
    }

    public static MidiDevice matchInputDevice(String substring) {
        return matchInputDevice(new String[] { substring });
    }

    public static MidiDevice matchInputDevice(String[] substrings) {
        return matchDevice(substrings, getInputs());
    }

    public static LXMidiOutput matchOutput(LX lx, String substring) {
        return matchOutput(lx, new String[] { substring });
    }

    public static LXMidiOutput matchOutput(LX lx, String[] substrings) {
        MidiDevice device = matchOutputDevice(substrings);
        if (device != null) {
            LXMidiOutput output = outputDeviceMap.get(device);
            if (output != null) {
                return output;
            }
            try {
                outputDeviceMap.put(device, output = new LXMidiOutput(lx, device));
                return output;
            } catch (MidiUnavailableException mux) {
                mux.printStackTrace();
            }
        }
        return null;
    }

    public static MidiDevice matchOutputDevice(String substring) {
        return matchOutputDevice(new String[] { substring });
    }

    public static MidiDevice matchOutputDevice(String[] substrings) {
        return matchDevice(substrings, getOutputs());
    }

    private static MidiDevice matchDevice(String[] substrings,
            List<MidiDevice> devices) {
        for (MidiDevice device : devices) {
            for (String substring : substrings) {
                if (device.getDeviceInfo().getName().contains(substring)) {
                    return device;
                }
            }
        }
        return null;
    }

    public static List<MidiDevice> getInputs() {
        return getInputs(false);
    }

    public static List<MidiDevice> getInputs(boolean refresh) {
        initializeIfNecessary(refresh);
        return LXMidiSystem.unmodifiableInputs;
    }

    public static List<MidiDevice> getOutputs() {
        return getOutputs(false);
    }

    public static List<MidiDevice> getOutputs(boolean refresh) {
        initializeIfNecessary(refresh);
        return LXMidiSystem.unmodifiableOutputs;
    }

    private static void initializeIfNecessary(boolean refresh) {
        if (refresh || !LXMidiSystem.initialized) {
            inputs.clear();
            outputs.clear();
            LXMidiSystem.initialized = true;
            for (MidiDevice.Info deviceInfo : MidiSystem.getMidiDeviceInfo()) {
                try {
                    MidiDevice device = MidiSystem.getMidiDevice(deviceInfo);
                    if (device.getMaxTransmitters() != 0) {
                        inputs.add(device);
                    }
                    if (device.getMaxReceivers() != 0) {
                        outputs.add(device);
                    }
                } catch (MidiUnavailableException mux) {
                    mux.printStackTrace();
                }
            }
        }
    }
}
