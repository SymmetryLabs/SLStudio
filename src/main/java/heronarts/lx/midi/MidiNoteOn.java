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

public class MidiNoteOn extends MidiNote {

    public MidiNoteOn(int channel, int pitch, int velocity) throws InvalidMidiDataException {
        super(ShortMessage.NOTE_ON, channel, pitch, velocity);
    }

    MidiNoteOn(ShortMessage message) {
        super(message, ShortMessage.NOTE_ON);
    }

    @Override
    public String toString() {
        return "MidiNoteOn:" + getChannel() + ":Pitch:" + getPitch() + ":Vel:" + getVelocity();
    }
}
