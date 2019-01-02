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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

public class MidiPitchBend extends LXShortMessage {

    public MidiPitchBend(int channel, int msb) throws InvalidMidiDataException {
        this(channel, 0, msb);
    }

    public MidiPitchBend(int channel, int lsb, int msb) throws InvalidMidiDataException {
        super(ShortMessage.PITCH_BEND, channel, lsb, msb);
    }

    MidiPitchBend(ShortMessage message) {
        super(message, ShortMessage.PITCH_BEND);
    }

    /**
     * Returns the pitch bend value, signed from [-8192, +8191]
     *
     * @return Pitch bend value
     */
    public int getPitchBend() {
        return (getData1() + (getData2() << 7)) - 0x2000;
    }

    /**
     * Returns the pitch bend value normalized space from [-1, +1]
     *
     * @return Normalized pitch bend amount
     */
    public double getNormalized() {
        int pitchBend = getPitchBend();
        return (pitchBend > 0) ? (pitchBend / 8191.) : (pitchBend / 8192.);
    }

    @Override
    public String toString() {
        return "MidiPitchBend:" + getChannel() + ":Bend:" + getPitchBend();
    }
}
