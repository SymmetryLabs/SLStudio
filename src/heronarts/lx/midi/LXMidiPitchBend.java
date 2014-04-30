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

public class LXMidiPitchBend extends LXShortMessage {
    LXMidiPitchBend(ShortMessage message) {
        super(message, ShortMessage.PITCH_BEND);
    }

    public int getPitchBend() {
        return (getData1() + (getData2() << 7)) - 0x2000;
    }
}
