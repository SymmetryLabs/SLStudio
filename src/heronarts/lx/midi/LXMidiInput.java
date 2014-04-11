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
        public void send(MidiMessage message, long timeStamp) {
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                switch (sm.getCommand()) {
                case ShortMessage.NOTE_ON:
                    midiSystem
                            .queueMessage(new LXMidiNote(sm).setInput(LXMidiInput.this));
                    break;
                case ShortMessage.NOTE_OFF:
                    midiSystem
                            .queueMessage(new LXMidiNote(sm).setInput(LXMidiInput.this));
                    break;
                case ShortMessage.CONTROL_CHANGE:
                    midiSystem.queueMessage(new LXMidiControlChange(sm)
                            .setInput(LXMidiInput.this));
                    break;
                }
            }
        }
    }

    void dispatch(LXShortMessage message) {
        for (LXMidiListener listener : this.listeners) {
            switch (message.getCommand()) {
            case ShortMessage.NOTE_ON:
                listener.noteOnReceived((LXMidiNote) message);
                break;
            case ShortMessage.NOTE_OFF:
                listener.noteOffReceived((LXMidiNote) message);
                break;
            case ShortMessage.CONTROL_CHANGE:
                listener.controlChangeReceived((LXMidiControlChange) message);
                break;
            }
        }
    }
}
