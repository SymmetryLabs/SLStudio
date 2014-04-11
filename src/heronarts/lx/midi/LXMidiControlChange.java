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

public class LXMidiControlChange extends LXShortMessage {
    LXMidiControlChange(ShortMessage message) {
        super(message);
    }

    public int getCC() {
        return getData1();
    }

    public int getValue() {
        return getData2();
    }
}
