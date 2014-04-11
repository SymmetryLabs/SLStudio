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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

public class LXMidiOutput implements Receiver {

    private final Receiver receiver;

    public LXMidiOutput(LX lx, MidiDevice device) throws MidiUnavailableException {
        device.open();
        this.receiver = device.getReceiver();
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

}
