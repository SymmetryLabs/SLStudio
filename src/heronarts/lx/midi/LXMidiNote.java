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

public class LXMidiNote extends LXShortMessage {

    LXMidiNote(ShortMessage message) {
        super(message);
    }

    public boolean isNoteOn() {
        return getCommand() == ShortMessage.NOTE_ON;
    }

    public boolean isNoteOff() {
        return getCommand() == ShortMessage.NOTE_OFF;
    }

    public int getPitch() {
        return getData1();
    }

    public int getVelocity() {
        return getData2();
    }
}
