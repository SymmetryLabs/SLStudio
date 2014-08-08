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

public interface LXMidiListener {

    public void noteOnReceived(LXMidiNoteOn note);

    public void noteOffReceived(LXMidiNoteOff note);

    public void controlChangeReceived(LXMidiControlChange cc);

    public void programChangeReceived(LXMidiProgramChange pc);

    public void pitchBendReceived(LXMidiPitchBend pitchBend);

    public void aftertouchReceived(LXMidiAftertouch aftertouch);

}
