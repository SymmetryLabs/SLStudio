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
import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;

public class LXMidiInput {

    private final MidiDevice device;

    private final LXMidiSystem midiSystem;

    private final List<LXMidiListener> listeners = new ArrayList<LXMidiListener>();

    public LXMidiInput(LX lx, MidiDevice device) throws MidiUnavailableException {
        this(lx.engine.midiSystem, device);
    }

    public LXMidiInput(LXMidiSystem midiSystem, MidiDevice device)
            throws MidiUnavailableException {
        this(midiSystem, device, null);
    }

    public LXMidiInput(LXMidiSystem midiSystem, MidiDevice device,
            LXMidiListener listener) throws MidiUnavailableException {
        this.midiSystem = midiSystem;
        this.device = device;
        if (listener != null) {
            addListener(listener);
        }
        device.open();
        device.getTransmitter().setReceiver(new Receiver());
    }

    public String getName() {
        return this.device.getDeviceInfo().getName();
    }

    public LXMidiInput addListener(LXMidiListener listener) {
        this.listeners.add(listener);
        return this;
    }

    public LXMidiInput removeListener(LXMidiListener listener) {
        this.listeners.remove(listener);
        return this;
    }

    private class Receiver implements javax.sound.midi.Receiver {
        @Override
        public void close() {
            listeners.clear();
        }

        @Override
        public void send(MidiMessage midiMessage, long timeStamp) {
            if (midiMessage instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) midiMessage;
                LXShortMessage message = null;
                switch (sm.getCommand()) {
                case ShortMessage.NOTE_ON:
                    message = new LXMidiNoteOn(sm);
                    break;
                case ShortMessage.NOTE_OFF:
                    message = new LXMidiNoteOff(sm);
                    break;
                case ShortMessage.CONTROL_CHANGE:
                    message = new LXMidiControlChange(sm);
                    break;
                case ShortMessage.PROGRAM_CHANGE:
                    message = new LXMidiProgramChange(sm);
                    break;
                case ShortMessage.PITCH_BEND:
                    message = new LXMidiPitchBend(sm);
                    break;
                case ShortMessage.CHANNEL_PRESSURE:
                    message = new LXMidiAftertouch(sm);
                    break;
                }
                if (message != null) {
                    midiSystem.queueMessage(message.setInput(LXMidiInput.this));
                }
            }
        }
    }

    void dispatch(LXShortMessage message) {
        for (LXMidiListener listener : this.listeners) {
            switch (message.getCommand()) {
            case ShortMessage.NOTE_ON:
                listener.noteOnReceived((LXMidiNoteOn) message);
                break;
            case ShortMessage.NOTE_OFF:
                listener.noteOffReceived((LXMidiNoteOff) message);
                break;
            case ShortMessage.CONTROL_CHANGE:
                listener.controlChangeReceived((LXMidiControlChange) message);
                break;
            case ShortMessage.PROGRAM_CHANGE:
                listener.programChangeReceived((LXMidiProgramChange) message);
                break;
            case ShortMessage.PITCH_BEND:
                listener.pitchBendReceived((LXMidiPitchBend) message);
                break;
            case ShortMessage.CHANNEL_PRESSURE:
                listener.aftertouchReceived((LXMidiAftertouch) message);
                break;
            }
        }
    }
}
