package com.symmetrylabs.slstudio.cue;

    import heronarts.lx.LX;
    import heronarts.lx.osc.LXOscEngine;
    import heronarts.lx.osc.OscMessage;
    import heronarts.lx.parameter.BoundedParameter;
    import heronarts.lx.parameter.StringParameter;

    import java.io.IOException;
    import java.net.SocketException;
    import java.net.UnknownHostException;

public class TriggerVezerCue extends Cue {
    private LX lx;
    private LXOscEngine.Transmitter oscTransmitter = null;
    public StringParameter showName = new StringParameter("show name");

    public TriggerVezerCue(LX lx, BoundedParameter cuedParameter) {
        super(cuedParameter);
        try {
            oscTransmitter = lx.engine.osc.transmitter("localhost", 7777);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.lx = lx;
    }

    public void triggerVezerShow(String showname){
        // logic to trigger vezer show
        OscMessage playShowMessage = new OscMessage("/vezer/" + showname + "/play").add(1); // something like this.
        try {
            oscTransmitter.send(playShowMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
