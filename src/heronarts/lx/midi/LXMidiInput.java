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

    private final LXMidiEngine midiEngine;

    private final List<LXMidiListener> listeners = new ArrayList<LXMidiListener>();

    private boolean isEngineInput = false;

    public LXMidiInput(LX lx, MidiDevice device) throws MidiUnavailableException {
        this(lx.engine.midiEngine, device);
    }

    public LXMidiInput(LXMidiEngine midiEngine, MidiDevice device)
            throws MidiUnavailableException {
        this(midiEngine, device, null);
    }

    public LXMidiInput(LXMidiEngine midiEngine, MidiDevice device, LXMidiListener listener) throws MidiUnavailableException {
        this.midiEngine = midiEngine;
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

    LXMidiInput setEngineInput(boolean isEngineInput) {
        this.isEngineInput = isEngineInput;
        return this;
    }

    /**
     * This receiver is called by a MIDI thread, it just puts messages
     * into a queue that can then be called by the engine thread.
     */
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
                    midiEngine.queueMessage(message.setInput(LXMidiInput.this));
                }
            }
        }
    }

    /**
     * This method is invoked on the main thread to process the MIDI message.
     */
    public void dispatch(LXShortMessage message) {
        for (LXMidiListener listener : this.listeners) {
            this.midiEngine.dispatch(message, listener);
        }
        if (this.isEngineInput) {
            this.midiEngine.dispatch(message);
        }
    }
}
