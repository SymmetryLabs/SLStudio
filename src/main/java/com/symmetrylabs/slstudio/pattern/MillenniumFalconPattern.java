package com.symmetrylabs.millenniumfalcon;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;

import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.BooleanParameter;

public class MillenniumFalconPattern extends LXPattern {

    private final BooleanParameter trigger = new BooleanParameter("trigger");
    private double elapsedTime = 0;
    private double effectDuration = 2000;
    private boolean isRunning = false;

    public MillenniumFalconPattern(LX lx) {
        super(lx);
        addParameter(trigger);
        trigger.setMode(BooleanParameter.Mode.MOMENTARY);
        trigger.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                isRunning = true;
            }
        });
    }

    public void run(double deltaMs) {
        setColors(0xffff0000);
        if (isRunning) {
            if ((elapsedTime += deltaMs) > effectDuration) {
                isRunning = false;
                elapsedTime = 0;
            }
            setColors(0xff00ff00);
        }
    }
}