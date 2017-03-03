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

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;

public class LXMidiEngine {

    private final List<LXMidiListener> listeners = new ArrayList<LXMidiListener>();

    private final List<LXShortMessage> threadSafeInputQueue =
        Collections.synchronizedList(new ArrayList<LXShortMessage>());

    private final List<LXShortMessage> engineThreadInputQueue =
        new ArrayList<LXShortMessage>();

    private final List<LXMidiInput> inputs = new ArrayList<LXMidiInput>();
    private final List<LXMidiOutput> outputs = new ArrayList<LXMidiOutput>();

    private final List<LXMidiInput> unmodifiableInputs = Collections.unmodifiableList(this.inputs);
    private final List<LXMidiOutput> unmodifiableOutputs = Collections.unmodifiableList(this.outputs);

    private final LX lx;

    public LXMidiEngine(LX lx) {
        this.lx = lx;
        for (MidiDevice.Info deviceInfo : MidiSystem.getMidiDeviceInfo()) {
            try {
                MidiDevice device = MidiSystem.getMidiDevice(deviceInfo);
                if (device.getMaxTransmitters() != 0) {
                    this.inputs.add(new LXMidiInput(this, device));
                }
                if (device.getMaxReceivers() != 0) {
                    this.outputs.add(new LXMidiOutput(this, device));
                }
            } catch (MidiUnavailableException mux) {
                mux.printStackTrace();
            }
        }
    }

    public List<LXMidiInput> getInputs() {
        return this.unmodifiableInputs;
    }

    public List<LXMidiOutput> getOutputs() {
        return this.unmodifiableOutputs;
    }

    public LXMidiInput matchInput(String name) {
        return matchInput(new String[] { name });
    }

    public LXMidiInput matchInput(String[] names) {
        return (LXMidiInput) matchDevice(this.inputs, names);
    }

    public LXMidiOutput matchOutput(String name) {
        return matchOutput(new String[] { name });
    }

    public LXMidiOutput matchOutput(String[] names) {
        return (LXMidiOutput) matchDevice(this.outputs, names);
    }

    private LXMidiDevice matchDevice(List<? extends LXMidiDevice> devices, String[] names) {
        for (LXMidiDevice device : devices) {
            String deviceName = device.getName();
            for (String name : names) {
                if (deviceName.contains(name)) {
                    return device;
                }
            }
        }
        return null;
    }

    public LXMidiEngine addListener(LXMidiListener listener) {
        this.listeners.add(listener);
        return this;
    }

    public LXMidiEngine removeListener(LXMidiListener listener) {
        this.listeners.remove(listener);
        return this;
    }

    void queueInputMessage(LXShortMessage message) {
        this.threadSafeInputQueue.add(message);
    }

    /**
     * Invoked by the main engine to dispatch all midi messages on the
     * input queue.
     */
    public void dispatch() {
        this.engineThreadInputQueue.clear();
        synchronized (this.threadSafeInputQueue) {
            this.engineThreadInputQueue.addAll(this.threadSafeInputQueue);
            this.threadSafeInputQueue.clear();
        }
        for (LXShortMessage message : this.engineThreadInputQueue) {
            message.getInput().dispatch(message);
        }
    }

    private final List<LXMidiListener> listenerSnapshot = new ArrayList<LXMidiListener>();

    public void dispatch(LXShortMessage message) {
        this.listenerSnapshot.clear();
        this.listenerSnapshot.addAll(this.listeners);
        for (LXMidiListener listener : this.listenerSnapshot) {
            dispatch(message, listener);
        }
        for (LXChannel channel : this.lx.engine.getChannels()) {
            if (channel.midiMonitor.isOn()) {
                dispatch(message, channel.getActivePattern());
                LXPattern nextPattern = channel.getNextPattern();
                if (nextPattern != null) {
                    dispatch(message, nextPattern);
                }
                for (LXEffect effect : channel.getEffects()) {
                    dispatch(message, effect);
                }
            }
        }
        // TODO(mcslee): send MIDI to the master FX bus? should effects really
        // monitor all MIDI input, or just patterns? with a richer MIDI mapping
        // implementation effects could just get control...
    }

    void dispatch(LXShortMessage message, LXMidiListener listener) {
        switch (message.getCommand()) {
        case ShortMessage.NOTE_ON:
            MidiNoteOn note = (MidiNoteOn) message;
            if (note.getVelocity() == 0) {
                listener.noteOffReceived(note);
            } else {
                listener.noteOnReceived(note);
            }
            break;
        case ShortMessage.NOTE_OFF:
            listener.noteOffReceived((MidiNoteOff) message);
            break;
        case ShortMessage.CONTROL_CHANGE:
            listener.controlChangeReceived((MidiControlChange) message);
            break;
        case ShortMessage.PROGRAM_CHANGE:
            listener.programChangeReceived((MidiProgramChange) message);
            break;
        case ShortMessage.PITCH_BEND:
            listener.pitchBendReceived((MidiPitchBend) message);
            break;
        case ShortMessage.CHANNEL_PRESSURE:
            listener.aftertouchReceived((MidiAftertouch) message);
            break;
        }
    }

}
