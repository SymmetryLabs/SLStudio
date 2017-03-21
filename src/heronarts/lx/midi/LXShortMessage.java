/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.midi;

import javax.sound.midi.ShortMessage;

public abstract class LXShortMessage extends ShortMessage {

    private LXMidiInput input = null;

    public static LXShortMessage fromShortMessage(ShortMessage message) {
        switch (message.getCommand()) {
        case ShortMessage.NOTE_ON:
            return new MidiNoteOn(message);
        case ShortMessage.NOTE_OFF:
            return new MidiNoteOff(message);
        case ShortMessage.CONTROL_CHANGE:
            return new MidiControlChange(message);
        case ShortMessage.PROGRAM_CHANGE:
            return new MidiProgramChange(message);
        case ShortMessage.PITCH_BEND:
            return new MidiPitchBend(message);
        case ShortMessage.CHANNEL_PRESSURE:
            return new MidiAftertouch(message);
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
