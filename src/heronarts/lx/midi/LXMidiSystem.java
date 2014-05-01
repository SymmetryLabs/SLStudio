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
import java.util.List;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

public class LXMidiSystem {

    private static final List<MidiDevice> inputs = new ArrayList<MidiDevice>();
    private static final List<MidiDevice> outputs = new ArrayList<MidiDevice>();

    private static final List<MidiDevice> unmodifiableInputs = Collections
            .unmodifiableList(inputs);

    private static final List<MidiDevice> unmodifiableOutputs = Collections
            .unmodifiableList(outputs);

    private static boolean initialized = false;

    public static LXMidiInput matchInput(LX lx, String substring) {
        return matchInput(lx, new String[] { substring });
    }

    public static LXMidiInput matchInput(LX lx, String[] substrings) {
        MidiDevice device = matchInputDevice(substrings);
        if (device != null) {
            try {
                return new LXMidiInput(lx, device);
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
            try {
                return new LXMidiOutput(lx, device);
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

    private final List<LXShortMessage> midiThreadEventQueue =
        Collections.synchronizedList(new ArrayList<LXShortMessage>());

    private final List<LXShortMessage> localThreadEventQueue =
        new ArrayList<LXShortMessage>();

    void queueMessage(LXShortMessage message) {
        this.midiThreadEventQueue.add(message);
    }

    public void dispatch() {
        this.localThreadEventQueue.clear();
        synchronized (this.midiThreadEventQueue) {
            this.localThreadEventQueue.addAll(this.midiThreadEventQueue);
            this.midiThreadEventQueue.clear();
        }
        for (LXShortMessage message : this.localThreadEventQueue) {
            message.getInput().dispatch(message);
        }
    }
}
