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

public abstract class LXAbstractMidiListener implements LXMidiListener {

    @Override
    public void noteOnReceived(LXMidiNoteOn note) {
    }

    @Override
    public void noteOffReceived(LXMidiNoteOff note) {
    }

    @Override
    public void controlChangeReceived(LXMidiControlChange cc) {
    }

    @Override
    public void programChangeReceived(LXMidiProgramChange pc) {
    }

    @Override
    public void pitchBendReceived(LXMidiPitchBend pitchBend) {
    }

    @Override
    public void aftertouchReceived(LXMidiAftertouch aftertouch) {
    }

}
