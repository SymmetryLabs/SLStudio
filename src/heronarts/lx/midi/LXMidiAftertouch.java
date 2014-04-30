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

public class LXMidiAftertouch extends LXShortMessage {
    LXMidiAftertouch(ShortMessage message) {
        super(message, ShortMessage.CHANNEL_PRESSURE);
    }

    public int getAftertouch() {
        return getData1();
    }
}
