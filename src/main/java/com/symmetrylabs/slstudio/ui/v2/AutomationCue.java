package com.symmetrylabs.slstudio.cue;

import com.google.gson.JsonObject;
import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;

public class AutomationCue extends Cue implements CueTypeAdapter {
    private LX lx;

    static String CUE_TYPE = "automation";

    public AutomationCue(LX lx, BoundedParameter cuedParameter) {
        super(cuedParameter);
        this.lx = lx;
        this.durationMs.setValue(100); // it's ok to fire a few times. Set duration to 100ms
    }

    public void triggerAutomation() {
        lx.engine.automation.trigger();
    }

    public String getCueType(){
        return CUE_TYPE;
    }

    @Override
    public void save(JsonObject obj) {
        //obj.addProperty("showName", showName.getString());
        super.save(obj);
    }

    @Override
    public void load(JsonObject obj) {
        //showName.setValue(obj.get("showName").getAsString());
        super.load(obj);
    }
}
