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

abstract class LXShortMessage extends ShortMessage {

    private LXMidiInput input = null;

    LXShortMessage(ShortMessage message) {
        super(message.getMessage());
    }

    LXShortMessage setInput(LXMidiInput input) {
        this.input = input;
        return this;
    }

    LXMidiInput getInput() {
        return this.input;
    }

}
