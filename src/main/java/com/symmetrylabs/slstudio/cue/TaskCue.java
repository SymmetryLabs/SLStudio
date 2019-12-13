package com.symmetrylabs.slstudio.cue;

import heronarts.lx.LX;
import heronarts.lx.osc.OscMessage;
import heronarts.lx.parameter.BoundedParameter;

public class TaskCue extends Cue {
    private LX lx;
    public TaskCue(LX lx, BoundedParameter cuedParameter) {
        super(cuedParameter);
        this.lx = lx;
    }

    public void executeTask(){
        // logic to trigger vezer show
        OscMessage playShowMessage = new OscMessage("/vezer/composition/1/play"); // something like this.
//        lx.engine.osc.send(playShowMessage);
    }
}
