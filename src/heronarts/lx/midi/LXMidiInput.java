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

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;

public class LXMidiInput extends LXMidiDevice {

    private final List<LXMidiListener> listeners = new ArrayList<LXMidiListener>();
    private boolean isOpen = false;

    LXMidiInput(LXMidiEngine engine, MidiDevice device) {
        super(engine, device);
    }

    /**
     * Opens the midi input.
     *
     * @return
     */
    @Override
    public LXMidiInput open() {
        this.enabled.setValue(true);
        return this;
    }

    @Override
    protected void onEnabled(boolean enabled) {
        if (enabled && !this.isOpen) {
            try {
                this.device.open();
                this.device.getTransmitter().setReceiver(new Receiver());
                this.isOpen = true;
            } catch (MidiUnavailableException mux) {
                System.err.println(mux.getLocalizedMessage());
                this.enabled.setValue(false);
            }
        }
    }

    public LXMidiInput addListener(LXMidiListener listener) {
        this.listeners.add(listener);
        return this;
    }

    public LXMidiInput removeListener(LXMidiListener listener) {
        this.listeners.remove(listener);
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
                    message = new MidiNoteOn(sm);
                    break;
                case ShortMessage.NOTE_OFF:
                    message = new MidiNoteOff(sm);
                    break;
                case ShortMessage.CONTROL_CHANGE:
                    message = new MidiControlChange(sm);
                    break;
                case ShortMessage.PROGRAM_CHANGE:
                    message = new MidiProgramChange(sm);
                    break;
                case ShortMessage.PITCH_BEND:
                    message = new MidiPitchBend(sm);
                    break;
                case ShortMessage.CHANNEL_PRESSURE:
                    message = new MidiAftertouch(sm);
                    break;
                }
                if (message != null) {
                    engine.queueInputMessage(message.setInput(LXMidiInput.this));
                }
            }
        }
    }

    /**
     * This method is invoked on the engine thread to process the MIDI message.
     *
     * @param message Midi message
     */
    void dispatch(LXShortMessage message) {
        for (LXMidiListener listener : this.listeners) {
            this.engine.dispatch(message, listener);
        }
    }

}
