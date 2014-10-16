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
