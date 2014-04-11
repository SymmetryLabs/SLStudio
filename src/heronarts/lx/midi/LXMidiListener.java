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

    public void noteOnReceived(LXMidiNote note);

    public void noteOffReceived(LXMidiNote note);

    public void controlChangeReceived(LXMidiControlChange cc);
}
