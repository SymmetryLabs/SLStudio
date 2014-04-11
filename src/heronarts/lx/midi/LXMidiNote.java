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

public abstract class LXMidiNote extends LXShortMessage {

    protected LXMidiNote(ShortMessage message, int command) {
        super(message, command);
    }

    public int getPitch() {
        return getData1();
    }

    public int getVelocity() {
        return getData2();
    }
}
