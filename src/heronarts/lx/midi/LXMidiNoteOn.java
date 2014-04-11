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

public class LXMidiNoteOn extends LXShortMessage {

    LXMidiNoteOn(ShortMessage message) {
        super(message, ShortMessage.NOTE_ON);
    }

    public int getPitch() {
        return getData1();
    }

    public int getVelocity() {
        return getData2();
    }
}
