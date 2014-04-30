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

public class LXMidiProgramChange extends LXShortMessage {
    LXMidiProgramChange(ShortMessage message) {
        super(message, ShortMessage.PROGRAM_CHANGE);
    }

    public int getProgram() {
        return getData1();
    }
}
