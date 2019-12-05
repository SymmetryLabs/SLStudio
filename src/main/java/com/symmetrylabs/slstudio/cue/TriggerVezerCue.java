package com.symmetrylabs.slstudio.cue;

    import com.google.gson.JsonObject;
    import heronarts.lx.LX;
    import heronarts.lx.osc.LXOscEngine;
    import heronarts.lx.osc.OscMessage;
    import heronarts.lx.parameter.BoundedParameter;
    import heronarts.lx.parameter.StringParameter;

    import java.io.IOException;
    import java.net.SocketException;
    import java.net.UnknownHostException;

public class TriggerVezerCue extends Cue implements CueTypeAdapter{
    private LX lx;
    private LXOscEngine.Transmitter oscTransmitter = null;
    public StringParameter showName = new StringParameter("show name");

    static String CUE_TYPE="vezer";

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
        this.durationMs.setValue(100); // it's ok to fire a few times. Set duration to 100ms
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

    public String getCueType(){
        return CUE_TYPE;
    }

    @Override
    public void save(JsonObject obj) {
        obj.addProperty("showName", showName.getString());
        super.save(obj);
    }

    @Override
    public void load(JsonObject obj) {
        showName.setValue(obj.get("showName").getAsString());
        super.load(obj);
    }
}
