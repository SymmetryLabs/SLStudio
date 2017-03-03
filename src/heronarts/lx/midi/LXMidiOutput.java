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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

public class LXMidiOutput extends LXMidiDevice implements Receiver {

    private Receiver receiver = null;
    private boolean isOpen = false;

    LXMidiOutput(LXMidiEngine engine, MidiDevice device) {
        super(engine, device);
    }

    @Override
    public void close() {
        this.receiver.close();
    }

    public void send(MidiMessage message) {
        send(message, -1);
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        if (!this.enabled.isOn()) {
            throw new UnsupportedOperationException("Cannot send() to an LXMidiOutput that is not enabled");
        }
        this.receiver.send(message, timeStamp);
    }

    public void sendSysex(byte[] sysex) {
        try {
            SysexMessage message = new SysexMessage();
            message.setMessage(sysex, sysex.length);
            send(message);
        } catch (InvalidMidiDataException imdx) {
            imdx.printStackTrace();
        }
    }

    private void sendShortMessage(int command, int channel, int data1, int data2) {
        try {
            ShortMessage message = new ShortMessage();
            message.setMessage(command, channel, data1, data2);
            send(message);
        } catch (InvalidMidiDataException imdx) {
            imdx.printStackTrace();
        }
    }

    public void sendNoteOn(int channel, int pitch, int velocity) {
        sendShortMessage(ShortMessage.NOTE_ON, channel, pitch, velocity);
    }

    public void sendNoteOff(int channel, int pitch) {
        sendNoteOff(channel, pitch, 0);
    }

    public void sendNoteOff(int channel, int pitch, int velocity) {
        sendShortMessage(ShortMessage.NOTE_OFF, channel, pitch, velocity);
    }

    public void sendControlChange(int channel, int cc, int value) {
        sendShortMessage(ShortMessage.CONTROL_CHANGE, channel, cc, value);
    }

    @Override
    protected void onEnabled(boolean enabled) {
        if (enabled && !this.isOpen) {
            try {
                this.device.open();
                this.receiver = device.getReceiver();
                this.isOpen = true;
            } catch (MidiUnavailableException mux) {
                System.err.println(mux.getLocalizedMessage());
                this.enabled.setValue(false);
            }
        }
    }

}
