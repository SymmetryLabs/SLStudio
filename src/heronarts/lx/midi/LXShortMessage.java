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

import javax.sound.midi.ShortMessage;

public abstract class LXShortMessage extends ShortMessage {

    private LXMidiInput input = null;

    public static LXShortMessage fromShortMessage(ShortMessage message) {
        switch (message.getCommand()) {
        case ShortMessage.NOTE_ON:
            return new LXMidiNoteOn(message);
        case ShortMessage.NOTE_OFF:
            return new LXMidiNoteOff(message);
        case ShortMessage.CONTROL_CHANGE:
            return new LXMidiControlChange(message);
        case ShortMessage.PROGRAM_CHANGE:
            return new LXMidiProgramChange(message);
        case ShortMessage.PITCH_BEND:
            return new LXMidiPitchBend(message);
        case ShortMessage.CHANNEL_PRESSURE:
            return new LXMidiAftertouch(message);
        }
        throw new IllegalArgumentException("Unsupported LXMidi message command: " + message.getCommand());
    }

    LXShortMessage(ShortMessage message, int command) {
        super(message.getMessage());
        if (getCommand() != command) {
            throw new IllegalArgumentException(
                    "LXShortMessage constructed with command " + command
                            + " but has actual command " + getCommand());
        }
    }

    LXShortMessage setInput(LXMidiInput input) {
        this.input = input;
        return this;
    }

    LXMidiInput getInput() {
        return this.input;
    }

}
